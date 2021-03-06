package com.sesame.onespace.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.cocosw.bottomsheet.BottomSheet;
import com.sesame.onespace.R;
import com.sesame.onespace.activities.dashboardActivities.BusInfoActivity;
import com.sesame.onespace.activities.dashboardActivities.CarParkActivity;
import com.sesame.onespace.activities.dashboardActivities.FlickrActivity;
import com.sesame.onespace.activities.dashboardActivities.InstagramActivity;
import com.sesame.onespace.activities.dashboardActivities.TwitterActivity;
import com.sesame.onespace.activities.dashboardActivities.WeatherActivity;
import com.sesame.onespace.activities.dashboardActivities.YoutubeActivity;
import com.sesame.onespace.dialogs.mapDialogs.SelectDashboardDialogBuilder;
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

// Modified code by Thianchai on 19/12/16

public class PlaceBottomSheet implements DialogInterface.OnClickListener {

    private Context context;
    private Place place;
    private BottomSheet bottomSheet;

    //Thianchai (I add this)
    private int selectDashboard;
    //

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

            //Thianchai (I add this for open dashboard from map)

            case R.id.open_dashboard_from_map:

                final SelectDashboardDialogBuilder builder = new SelectDashboardDialogBuilder(context, R.style.MyAlertDialogStyle2);
                builder.setTitle("Please select dashboard");

                String[] array = new String[]{"Twitter", "Youtube", "Flickr", "Instagram", "Weather", "Carpark", "Bus Info"};
                selectDashboard = 0;

                builder.setSingleChoiceItems(array, -1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        selectDashboard = item;

                    }

                });


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = null;
                        Bundle bundle = null;

                        switch (selectDashboard){

                            case 0:

                                intent = new Intent(context, TwitterActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 1:

                                intent = new Intent(context, YoutubeActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 2:

                                intent = new Intent(context, FlickrActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 3:

                                intent = new Intent(context, InstagramActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 4:

                                intent = new Intent(context, WeatherActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 5:

                                intent = new Intent(context, CarParkActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                            case 6:

                                intent = new Intent(context, BusInfoActivity.class);
                                bundle = new Bundle();
                                bundle.putString("Name", place.getName());
                                bundle.putString("Vloc", place.getVloc());
                                bundle.putDouble("Lat", place.getLat());
                                bundle.putDouble("Lng", place.getLng());
                                intent.putExtra("bundle", bundle);
                                intent.putExtra("enter from", "map");
                                context.startActivity(intent);

                                break;

                        }

                    }

                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {


                    }

                });

                android.app.AlertDialog alert = builder.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();

                alert.getListView().setItemChecked(selectDashboard, true);

                break;

            //

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
