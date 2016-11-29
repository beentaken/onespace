package com.sesame.onespace.network;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sesame.onespace.R;
import com.sesame.onespace.managers.chat.ChatHistoryManager;
import com.sesame.onespace.models.chat.Chat;
import com.sesame.onespace.models.map.Corner;
import com.sesame.onespace.models.map.FilterMarkerNode;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.service.xmpp.Tools;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.views.adapters.CornerListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by chongos on 11/16/15 AD.
 */
public class CornerMarkerLoader extends MapMarkerLoader implements CornerListAdapter.OnCornerListInteractionListener {

    private Call callOtherCorners;
    private Call callMyCorners;
    private Multimap<String, String> mapFilterAndID;
    private List<String> listVisibleCategories;

    private RecyclerView recyclerView;
    private CornerListAdapter listAdapter;

    private LatLngBounds lastBounds;
    private int lastLimit;
    private String userID;

    public CornerMarkerLoader(Context context, OneSpaceApi.Service api, com.squareup.otto.Bus bus) {
        super(context, api, bus);
        mapFilterAndID = ArrayListMultimap.create();
        listVisibleCategories = new ArrayList<>();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listAdapter = new CornerListAdapter(context);
        listAdapter.setOnCornerListInteractionListener(this);

        this.recyclerView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.setAdapter(listAdapter);

        notifyMyCornerChanged();
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void notifyMyCornerChanged() {
        fetchMyCorner();
    }

    public void delete(final Corner corner) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Corner")
                .setMessage(context.getString(R.string.alert_confirm_delete_corner))
                .setPositiveButton(context.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCorner(corner);
                    }
                })
                .setNegativeButton(context.getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void deleteCorner(final Corner corner) {
        if(listAdapter != null)
            listAdapter.deleteCorner(corner);
        deleteCornerFromServer(corner);
        deleteCornerFromMap(corner);
        new AsyncTask<Void, Void, Chat>() {

            @Override
            protected Chat doInBackground(Void... params) {
                Chat chat = null;
                try{
                    chat = ChatHistoryManager.getInstance(context).getChat(corner.getRoomJid());
                } catch (NullPointerException e) {

                }
                return chat;
            }

            @Override
            protected void onPostExecute(Chat chat) {
                super.onPostExecute(chat);
                if(chat != null)
                    Tools.sendToService(context,
                            MessageService.ACTION_XMPP_GROUP_LEAVE,
                            chat);
            }
        }.execute();
    }

    private void deleteCornerFromServer(final Corner corner) {
        Observable<String> observable = new OneSpaceApi.Builder(context)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .deleteCorner(corner.getCreatorId(), corner.getRoomJid());

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {
                        Log.i("Delete completed.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        Log.i("Delete corner : " + s);
                    }

                });
    }

    public void deleteCornerFromMap(final Corner corner) {
        new AsyncTask<Void, Void, ArrayList<Object>>() {

            @Override
            protected ArrayList<Object> doInBackground(Void... params) {
                markerHashMap.remove(String.valueOf(corner.getId()));
                mapFilterAndID.remove(corner.isMine()
                        ? context.getString(R.string.display_corners_my)
                        : context.getString(R.string.display_corners_other)
                        , corner.getId());

                ArrayList<Object> corners = new ArrayList<>();
                corners.add(corner);
                return corners;
            }

            @Override
            protected void onPostExecute(ArrayList<Object> objects) {
                super.onPostExecute(objects);
                publishRemoveResult(objects);
            }

        }.execute();
    }

    @Override
    public void setFilter(FilterMarkerNode filter) {
        super.setFilter(filter);
        for(int i=0; i<filter.getSubCategorySize(); i++) {
            FilterMarkerNode sub = filter.getSubCategory(i);
            if (sub.isSelected()) {
                listVisibleCategories.add(sub.getName());
            }
        }
    }

    @Override
    public void fetch(LatLngBounds bounds, int limit) {
        this.lastBounds = bounds;
        this.lastLimit = limit;

        if(!filter.isSelected())
            return;

        if(listVisibleCategories.contains(context.getString(R.string.display_corners_other)))
            fetchOtherCorner(bounds, limit);

        if(userID != null && listVisibleCategories.contains(context.getString(R.string.display_corners_my)))
            fetchMyCorner();
    }

    @Override
    public void onFilterChange(final FilterMarkerNode filterMarkerNode) {
        if(filterMarkerNode.equals(filter)) {
            if(filterMarkerNode.isSelected()) {
                new AsyncTask<Void, Void, ArrayList<Object>>() {

                    @Override
                    protected ArrayList<Object> doInBackground(Void... params) {
                        ArrayList<Object> corners = new ArrayList<>();

                        for(String visibleCategory : listVisibleCategories) {
                            Collection<String> addCorners = mapFilterAndID.get(visibleCategory);

                            for (String addCornerID : addCorners) {
                                Object aCorner = markerHashMap.get(addCornerID);
                                corners.add(aCorner);
                            }
                        }
                        return corners;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<Object> objects) {
                        super.onPostExecute(objects);
                        publishAddResult(objects);
                    }

                }.execute();
            } else {
                super.onFilterChange(filterMarkerNode);
            }
        } else {
            new AsyncTask<Void, Void, ArrayList<Object>>() {

                @Override
                protected ArrayList<Object> doInBackground(Void... params) {
                    ArrayList<Object> corners = new ArrayList<>();
                    Collection<String> addCorners = mapFilterAndID.get(filterMarkerNode.getName());

                    for (String addCornerID : addCorners) {
                        Object aPlace = markerHashMap.get(addCornerID);
                        corners.add(aPlace);
                    }
                    return corners;
                }

                @Override
                protected void onPostExecute(ArrayList<Object> objects) {
                    super.onPostExecute(objects);
                    if (filterMarkerNode.isSelected()) {
                        listVisibleCategories.add(filterMarkerNode.getName());
                        publishAddResult(objects);
                        if(lastBounds != null)
                            fetch(lastBounds, lastLimit);
                    } else {
                        listVisibleCategories.remove(filterMarkerNode.getName());
                        publishRemoveResult(objects);
                    }
                }

            }.execute();
        }
    }

    private void fetchMyCorner() {
        if(callMyCorners != null)
            callMyCorners.cancel();

        callMyCorners = api.getCorners(userID);
        callMyCorners.enqueue(getCallback(context.getString(R.string.display_corners_my)));
    }

    private void fetchOtherCorner(LatLngBounds bounds, int limit) {
        if(callOtherCorners != null)
            callOtherCorners.cancel();

        callOtherCorners = api.getCorners(bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                limit);

        callOtherCorners.enqueue(getCallback(context.getString(R.string.display_corners_other)));
    }

    private Callback getCallback(final String filterName) {
        return new Callback<ArrayList<Corner>>() {
            @Override
            public void onResponse(final Response<ArrayList<Corner>> response) {
                if (response.isSuccess()) {
                    new AsyncTask<Void, Void, ArrayList<Object>>() {

                        @Override
                        protected ArrayList<Object> doInBackground(Void... params) {
                            ArrayList<Object> corners = new ArrayList<>();
                            for (Corner corner : response.body()) {
                                String creatorID = corner.getCreatorId();
                                String id = String.valueOf(corner.getId());
                                corner.setIsMine(filterName.equals(context.getString(R.string.display_corners_my)));

                                if ((filterName.equals(context.getString(R.string.display_corners_other))
                                        && !creatorID.equals(userID))
                                        || filterName.equals(context.getString(R.string.display_corners_my))) {

                                    if (!markerHashMap.containsKey(id)) {
                                        markerHashMap.put(id, corner);
                                        mapFilterAndID.put(filterName, id);
                                        corners.add(corner);
                                    }
                                }
                            }
                            return corners;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<Object> objects) {
                            super.onPostExecute(objects);
                            if(filter.isSelected() && listVisibleCategories.contains(filterName))
                                publishAddResult(objects);

                            if(listAdapter != null
                                    && filterName.equals(context.getString(R.string.display_corners_my))) {
                                for(Object obj : objects)
                                    listAdapter.addCorner((Corner) obj);
                            }
                        }

                    }.execute();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                android.util.Log.e("GetMyCorners", t.toString());
            }
        };
    }


    @Override
    public void onOpenChat(Corner corner) {
        String roomId = corner.getRoomJid().split("@")[0];
        String roomName = corner.getName();
        Intent i = new Intent(MessageService.ACTION_XMPP_GROUP_JOIN, null,
                context, MessageService.class);
        i.putExtra(MessageService.KEY_BUNDLE_GROUP_ROOM_JID, roomId);
        i.putExtra(MessageService.KEY_BUNDLE_GROUP_NAME, roomName);
        MessageService.sendToServiceHandler(i);
    }

    @Override
    public void onDelete(Corner corner) {
        delete(corner);
    }
}
