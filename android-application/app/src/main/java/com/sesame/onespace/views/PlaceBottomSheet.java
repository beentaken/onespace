package com.sesame.onespace.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.cocosw.bottomsheet.BottomSheet;
import com.sesame.onespace.R;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.models.map.Vloc;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.service.MessageService;
import com.sesame.onespace.utils.Log;

import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by chongos on 11/5/15 AD.
 */
public class PlaceBottomSheet implements DialogInterface.OnClickListener {

    private Context context;
    private Place place;
    private BottomSheet bottomSheet;

    public PlaceBottomSheet(Context context, Place place) {
        this.context = context;
        this.place = place;
        this.bottomSheet = new BottomSheet.Builder((Activity) context)
                .title(place.getName())
                .sheet(R.menu.menu_marker_place)
                .listener(this)
                .build();
    }

    public void show() {
        this.bottomSheet.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case R.id.join_groupchat:
                joinGroup();
                break;
        }
    }

    private void joinGroup() {
        Observable<Vloc> observable = new OneSpaceApi.Builder(context)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .getVlocRx(place.getVloc(), 0);
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Vloc>() {
                    private Vloc vloc = null;

                    @Override
                    public void onCompleted() {
                        if (vloc == null)
                            onError(null);
                        else if(vloc.getVplaces().size() > 0)
                            new FindGroupChatTask(vloc).execute();
                        else
                            showErrorDialog("Error", "Not found group \"" + place.getName() + "\"");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Vloc vloc) {
                        this.vloc = vloc;
                    }

                });

    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private class FindGroupChatTask extends AsyncTask<Void, Void, Vloc.Vplace>
            implements DialogInterface.OnClickListener {

        private Vloc vloc;

        FindGroupChatTask(Vloc vloc) {
            this.vloc = vloc;
        }

        @Override
        protected Vloc.Vplace doInBackground(Void... params) {
            List<Vloc.Vplace> vplace = vloc.getVplaces();
            for (Vloc.Vplace vp : vplace) {
                Log.i("vplace_id: " + vp.getVplaceId() + ", name: \"" + vp.getName() + "\"");
                if (place.getName().equals(vp.getName())) {
                    return vp;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Vloc.Vplace vplace) {
            super.onPostExecute(vplace);
            if(vplace != null)
                sendToService(vplace);
            else
                showAlertDialogWithListview();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            sendToService(vloc.getVplaces().get(which));
        }

        private void sendToService(Vloc.Vplace vplace) {
            String roomId = vplace.getVplaceId() + "";
            String roomName = vplace.getName();
            Intent i = new Intent(MessageService.ACTION_XMPP_GROUP_JOIN, null,
                    context, MessageService.class);
            i.putExtra(MessageService.KEY_BUNDLE_GROUP_ROOM_JID, roomId);
            i.putExtra(MessageService.KEY_BUNDLE_GROUP_NAME, roomName);
            MessageService.sendToServiceHandler(i);
        }

        private void showAlertDialogWithListview() {
            String[] list = new String[vloc.getVplaces().size()];
            for(int i=0; i<vloc.getVplaces().size(); i++)
                list[i] = vloc.getVplaces().get(i).getName();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle("Join to Group");
            dialogBuilder.setItems(list, this);
            AlertDialog alertDialogObject = dialogBuilder.create();
            alertDialogObject.show();
        }

    }
}
