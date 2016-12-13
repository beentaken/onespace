package com.sesame.onespace.activities.dashboardActivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.MapActivity;
import com.sesame.onespace.fragments.dashboardFragments.lastTweetsFragment.LastTweetsListFragment;
import com.sesame.onespace.managers.dashboard.DashboardRecentPlaceManager;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.utils.connectToServer.MyConnect;
import com.sesame.onespace.utils.date.MyDateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by Thian on 12/12/2559.
 */

public class TwitterActivity
        extends DashBoardActivity{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private final static int LAST_TWEETS_FRAGMENT = 0;

    private final static double DISTANCE_AROUND_USER = 0.001;

    private String url;

    private Place placeNearest;
    private ArrayList<Parcelable> items;

    private ArrayList<Place> placeRecentlyArrayList;
    private int placeRecentlyselectItem;

    //===========================================================================================================//
    //  MAIN BLOCK                                                                                  MAIN BLOCK
    //===========================================================================================================//

    @Override
    protected void initContentView() {
        TwitterActivity.super.setContentView(R.layout.activity_dashboard_tweets);

    }

    @Override
    protected void initDefaultValue() {
        TwitterActivity.super.initDefaultValue();

        TwitterActivity.this.initDefaultValueForConnectToServer();
        TwitterActivity.this.initDefaultValueForOpenLastTweets();

    }

    @Override
    protected void initView() {
        TwitterActivity.super.initView();

        TwitterActivity.this.initToolbar();
        TwitterActivity.this.initTextDistance();

    }

    @Override
    protected void doBeforeCreate() {

    }

    @Override
    protected void doInCreate() {

    }

    @Override
    protected void doAfterCreate() {
        TwitterActivity.super.doAfterCreate();

        TwitterActivity.this.startFragment(TwitterActivity.this.LAST_TWEETS_FRAGMENT);

    }

    @Override
    protected void doBeforeStart() {

    }

    @Override
    protected void doInStart() {

    }

    @Override
    protected void doAfterStart() {

    }

    @Override
    protected void doBeforeResume() {
        TwitterActivity.super.doBeforeResume();

    }

    @Override
    protected void doInResume() {

    }

    @Override
    protected void doAfterResume() {

    }

    @Override
    protected void doBeforeStop() {
        TwitterActivity.super.doBeforeStop();

    }

    @Override
    protected void doInStop() {

    }

    @Override
    protected void doAfterStop() {

    }

    @Override
    protected void doBeforeClose() {

    }

    @Override
    protected void doAfterClose() {
        TwitterActivity.super.doAfterClose();

    }

    @Override
    protected void doInSensitiveDestroy() {

    }

    @Override
    protected void doInBackToPreviousStep(String stepName) {
        TwitterActivity.super.doInBackToPreviousStep(stepName);

    }

    @Override
    protected void doInSwipeRight() {
        TwitterActivity.super.doInSwipeRight();

    }

    @Override
    protected void doInSwipeLeft() {

    }

    @Override
    protected void doInSwipeUp() {

    }

    @Override
    protected void doInSwipeDown() {

    }

    @Override
    protected void doInDoubleTap() {

    }

    @Override
    protected void doWhenSelectNavigation(int id) {

        switch (id) {

            case  R.id.nav_lastTweets:
                TwitterActivity.super.startFragment(TwitterActivity.this.LAST_TWEETS_FRAGMENT);
                break;

            case  R.id.nav_goToChat:

                break;

            case  R.id.nav_goToMap:
                Intent intent = new Intent(TwitterActivity.this, MapActivity.class);
                TwitterActivity.super.startActivity(intent);
                break;

            case  R.id.nav_place_nearest:
                TwitterActivity.this.placeNearest = null;
                TwitterActivity.super.startFragment(TwitterActivity.super.idFragment);
                break;

            case  R.id.nav_recently:
                this.placeRecentlyArrayList = DashboardRecentPlaceManager.getPlaceArrayList();

                String[] placesTextArray = new String[this.placeRecentlyArrayList.size()];

                this.placeRecentlyselectItem = 0;

                int index = 0;
                while (index < this.placeRecentlyArrayList.size()){

                    placesTextArray[index] = this.placeRecentlyArrayList.get((this.placeRecentlyArrayList.size() - 1) - index).getName();

                    index = index + 1;

                }

                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(TwitterActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("The place recently .(Please select)");
                builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        placeRecentlyselectItem = item;

                    }

                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        TwitterActivity.this.placeNearest = placeRecentlyArrayList.get((placeRecentlyArrayList.size() - 1) - placeRecentlyselectItem);

                        dialog.dismiss();

                        TwitterActivity.super.startFragment(TwitterActivity.super.idFragment);

                    }

                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                    }

                });

                AlertDialog alert = builder.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();

                alert.getListView().setItemChecked(this.placeRecentlyselectItem, true);
                break;

            case  R.id.nav_backToMainMenu:
                TwitterActivity.super.close();
                break;

        }

    }

    @Override
    protected void doWhenLocationChange() {

        TwitterActivity.this.changeTextDistance();

    }

    @Override
    protected void doInPrepareForOpenFragment() {

        TwitterActivity.this.searchPlaceNearest();
        TwitterActivity.this.searchURL();
        TwitterActivity.this.searchItems();

    }

    @Override
    protected void doInFailToOpenFragment() {

        TwitterActivity.this.changeViewForFailToOpenFragment();

    }

    @Override
    protected void doInSuccessToOpenFragment() {

        TwitterActivity.this.changeViewForSuccessToOpenFragment();

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initDefaultValueForConnectToServer(){

        //init
        TwitterActivity.this.url = null;

        //before


        //main


        //after

    }

    private void initDefaultValueForOpenLastTweets(){

        //init
        TwitterActivity.this.placeNearest = null;
        TwitterActivity.this.items = new ArrayList<Parcelable>();

        //before


        //main


        //after

    }

    //===========================================================================================================//
    //  SET VIEW                                                                                    SET VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initToolbar(){

        //init
        Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);

        //before


        //main
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setLogo(R.drawable.ic_dashboard_twitter);

        //after


    }

    private void initTextDistance(){

        //init
        TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

        //before

        //main
        if (placeNearest != null){

            Location location = new Location("placeNearly");
            location.setLatitude(placeNearest.getLat());
            location.setLongitude(placeNearest.getLng());

            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

        }
        else{

            textView.setText("NO DISTANCE TO SHOW");

        }

        //after


    }

    //===========================================================================================================//
    //  LOCATION CHANGE                                                                             LOCATION CHANGE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void changeTextDistance(){

        //init
        TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

        //before

        //main
        if (placeNearest != null){

            Location location = new Location("placeNearly");
            location.setLatitude(placeNearest.getLat());
            location.setLongitude(placeNearest.getLng());

            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

        }
        else{

            textView.setText("NO DISTANCE TO SHOW");

        }

        //after


    }

    //===========================================================================================================//
    //  START FRAGMENT                                                                              START FRAGMENT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Call call = TwitterActivity.super.api.getPlaces(userLocation.getLatitude()+ TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - TwitterActivity.DISTANCE_AROUND_USER,
                10);

        //before
        if (TwitterActivity.this.placeNearest != null){

            return;

        }

        if (MyConnect.isInternetAvailable() == false){

            TwitterActivity.super.result = false;
            TwitterActivity.super.caseFail = TwitterActivity.super.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        call.enqueue(new Callback<ArrayList<Place>>() {

            private ArrayList<Place> placesNearlyList;
            private Place placeMin;

            private Location l1;
            private Location l2;

            private int selectItem;

            @Override
            public void onResponse(final Response<ArrayList<Place>> response) {

                //init
                this.placesNearlyList = new ArrayList<Place>();
                this.placeMin = null;

                this.l1 = new Location("l1");
                this.l2 = new Location("l2");

                this.selectItem = 0;

                //before


                //main
                if (response.isSuccess()) {

                    for (Place place : response.body()) {

                        if (this.placeMin == null){

                            this.placeMin = place;

                        }
                        else{

                            this.l1.setLatitude(this.placeMin.getLat());
                            this.l1.setLongitude(this.placeMin.getLng());

                            this.l2.setLatitude(place.getLat());
                            this.l2.setLongitude(place.getLng());

                            if (UserLocationManager.getLocation().distanceTo(this.l1) >= UserLocationManager.getLocation().distanceTo(this.l2)){

                                if (UserLocationManager.getLocation().distanceTo(this.l1) == UserLocationManager.getLocation().distanceTo(this.l2)){

                                    this.placesNearlyList.add(place);

                                }
                                else{

                                    this.placesNearlyList.clear();
                                    this.placesNearlyList.add(place);
                                    this.placeMin = place;

                                }

                            }

                        }

                    }

                }
                else{

                    TwitterActivity.super.caseFail = TwitterActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    TwitterActivity.this.placeNearest = null;

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        TwitterActivity.this.placeNearest = this.placeMin;

                        countDownLatch.countDown();

                    }
                    else{

                        String[] placesTextArray = new String[this.placesNearlyList.size()];

                        int i = 0;
                        while (i < this.placesNearlyList.size()){

                            Location placeLocation = new Location("place");
                            placeLocation.setLatitude(this.placesNearlyList.get(i).getLat());
                            placeLocation.setLongitude(this.placesNearlyList.get(i).getLng());

                            placesTextArray[i] = (i+1) + ". " + this.placesNearlyList.get(i).getName() + "\n   ( distance : " + userLocation.distanceTo(placeLocation) + " m )";

                            i = i + 1;

                        }

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(TwitterActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                TwitterActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                countDownLatch.countDown();

                                dialog.dismiss();

                            }

                        });

                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();

                        alert.getListView().setItemChecked(this.selectItem, true);

                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("GetPlace", t.toString());
            }

        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //after
        if (TwitterActivity.this.placeNearest != null){

            DashboardRecentPlaceManager.addPlace(TwitterActivity.this.placeNearest);

        }

    }

    private void searchURL(){

        //init


        //before


        //main
        if (TwitterActivity.this.placeNearest != null){

            TwitterActivity.this.url = "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=" + TwitterActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

        }
        else{

            TwitterActivity.this.url = null;

            TwitterActivity.super.result = false;

            if (TwitterActivity.super.caseFail == TwitterActivity.EVERYTHING_OK){

                TwitterActivity.super.caseFail = TwitterActivity.NO_DATA;

            }

        }

        //after


    }

    private void searchItems(){

        //init


        //before
        if (TwitterActivity.this.url == null){

            return;

        }

        if (MyConnect.isInternetAvailable() == false){

            TwitterActivity.super.result = false;
            TwitterActivity.super.caseFail = TwitterActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = MyConnect.getJSON(TwitterActivity.this.url);

        if (jsonObject.length() == 0){

            TwitterActivity.super.result = false;
            TwitterActivity.super.caseFail = TwitterActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            TwitterActivity.super.result = false;
            TwitterActivity.super.caseFail = TwitterActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                if (TwitterActivity.this.idFragment == TwitterActivity.LAST_TWEETS_FRAGMENT) {

                    TwitterActivity.this.items.add(new LastTweetsListFragment.LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), MyDateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));

                }


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;


        }

        //after

    }

    private void changeViewForFailToOpenFragment(){

        //init


        //before


        //main
        TwitterActivity.super.handler.post(new Runnable() {
            @Override
            public void run() {

                //init
                Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
                TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

                Location location = new Location("placeNearly");
                location.setLatitude(TwitterActivity.this.placeNearest.getLat());
                location.setLongitude(TwitterActivity.this.placeNearest.getLng());

                //before


                //main
                if (TwitterActivity.this.placeNearest == null){

                    toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                    textView.setText("NO DISTANCE TO SHOW");

                }
                else{


                    toolbar.setSubtitle(TwitterActivity.this.placeNearest.getName());
                    textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

                }

                //after

            }

        });

        //after

    }

    private void changeViewForSuccessToOpenFragment(){

        //init
        LastTweetsListFragment lastTweetsListFragment = new LastTweetsListFragment();

        //before


        //main
        TwitterActivity.super.handler.post(new Runnable() {
            @Override
            public void run() {

                //init
                Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
                TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

                Location location = new Location("placeNearly");
                location.setLatitude(TwitterActivity.this.placeNearest.getLat());
                location.setLongitude(TwitterActivity.this.placeNearest.getLng());

                //before


                //main
                toolbar.setSubtitle(TwitterActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

                //after

            }

        });


        if (TwitterActivity.super.idFragment == TwitterActivity.LAST_TWEETS_FRAGMENT){

            Bundle bundle = new Bundle();
            bundle.putString("url", TwitterActivity.this.url);
            bundle.putParcelableArrayList("items", TwitterActivity.this.items);
            lastTweetsListFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, lastTweetsListFragment, lastTweetsListFragment.getClass().getSimpleName()).addToBackStack(null).commit();

        }

        //after

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
