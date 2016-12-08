package com.sesame.onespace.activities.dashboardActivitys;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.MapActivity;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.LoadingFragment;
import com.sesame.onespace.fragments.dashboardFragments.lastTweetsFragment.LastTweetsListFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.managers.dashboard.DashboardPlaceMarkerManager;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.connectToServer.MyConnect;
import com.sesame.onespace.utils.date.MyDateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;

/**
 * Created by Thian on 1/12/2559.
 */

public class TweetsActivity
        extends DashBoardActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    final int LAST_TWEETS_FRAGMENT = 0;

    private Handler handler;

    private Call call;
    private OneSpaceApi.Service api;
    private ArrayList<String> urlList;

    private Place placeNearly;

    private GPSBroadcastReceiver broadcastReceiver;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE (MAIN BLOCK)                                                             ACTIVITY LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //init


        //before


        //main
        super.onCreate(savedInstanceState);

        //after
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.app_bar_layout);
        appBarLayout.setExpanded(true, true);

    }

    @Override
    protected void onStart(){

        //init


        //before


        //main
        super.onStart();

        //after

    }

    @Override
    protected void onResume() {

        //init


        //before


        //main
        super.onResume();
        registerReceiver(this.broadcastReceiver, new IntentFilter("GPSTrackerService"));

        //after

    }

    @Override
    public void onBackPressed() {

        //init


        //before


        //main
        super.onBackPressed();

        //after

    }

    @Override
    protected  void onStop(){

        //init


        //before


        //main
        super.onStop();
        unregisterReceiver(this.broadcastReceiver);

        //after

    }

    @Override
    protected void onRestart(){

        //init


        //before


        //main
        super.onRestart();

        //after

    }

    @Override
    protected  void onDestroy(){

        //init


        //before


        //main
        super.onDestroy();

        //after

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    @Override
    protected void initDefaultValueForChild() {

        //init
        super.mainActivity = this;
        super.context = getApplicationContext();

        this.handler = new Handler();

        this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        this.call = null;
        this.urlList = new ArrayList<String>();

        this.placeNearly = null;

        this.broadcastReceiver = new GPSBroadcastReceiver();

        //before


        //main


        //after

    }

    @Override
    protected void initActivityForChild(){

        //init


        //before


        //main
        setContentView(R.layout.activity_dashboard_tweets);

        //after


    }

    @Override
    protected void initStatusBarForChild() {

        //init


        //before


        //main


        //after


    }

    @Override
    protected void initToolbarForChild(){

        //init


        //before


        //main
        super.toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        super.toolbar.setLogo(R.drawable.ic_dashboard_twitter);

        //after
        this.initSwitch();


    }

    private void initSwitch(){

        //init
        Switch onOffSwitch = (Switch)  this.navigationView.getHeaderView(0).findViewById(R.id.on_off_switch);

        //before


        //main
        onOffSwitch.setChecked(DashboardPlaceMarkerManager.isPlaceMarker());
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked == true){

                    DashboardPlaceMarkerManager.setPlace(placeNearly);

                }
                else{

                    DashboardPlaceMarkerManager.setPlace(null);

                }

            }

        });

        //after


    }

    @Override
    protected void initContentViewForChild(){

        //init


        //before


        //main
        this.startFragment(this.LAST_TWEETS_FRAGMENT);

        //after


    }

    //===========================================================================================================//
    //  SELECT NAVIGATION                                                                           SELECT NAVIGATION
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    @Override
    protected void selectNavigation(int id) {

        //init


        //before


        //main
        if (id == R.id.nav_lastTweets) {

            startFragment(this.LAST_TWEETS_FRAGMENT);

        }

        if (id == R.id.nav_goToChat) {

        }

        if (id == R.id.nav_goToMap) {

            Intent intent = new Intent(this, MapActivity.class);
            this.startActivity(intent);

        }

        if (id == R.id.nav_backToMainMenu) {

            super.close();

        }

        //after

    }

    //===========================================================================================================//
    //  START FRAGMENT                                                                              START FRAGMENT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void startFragment(final int index){

        //init
        OpenMainFragmentThread openMainFragmentThread = new OpenMainFragmentThread(index);

        //before
        this.urlList.clear();
        this.openLoadingFragment();

        //main
        openMainFragmentThread.start();

        //after


    }

    private void openLoadingFragment(){

        //init
        LoadingFragment fragment;

        //before


        //main
        fragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();

        //after


    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private class OpenMainFragmentThread extends Thread{

        private final int EVERYTHING_OK = -1;
        private final int INTERNET_NOT_AVAILABLE = 0;
        private final int CAN_NOT_CONNECT_TO_SERVER = 1;
        private final int NO_DATA = 2;

        private boolean result;
        private int caseFail;

        private int id;
        private ArrayList<Parcelable> items;

        public OpenMainFragmentThread(int id){

            this.init(id);

        }

        private void init(int id){

            this.id = id;
            this.items = new ArrayList<Parcelable>();

        }

        @Override
        public void run(){

            //init
            this.result = true;
            this.caseFail = this.EVERYTHING_OK;

            //before
            this.prepareForOpenMainFragment();

            //main
            this.OpenMainFragment();

            //after


        }

        private void prepareForOpenMainFragment(){

            //init
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            final Location userLocation = UserLocationManager.getLocation();
            call = api.getPlaces(userLocation.getLatitude()+ 0.0010,
                    userLocation.getLongitude() + 0.0010,
                    userLocation.getLatitude() - 0.0010,
                    userLocation.getLongitude() - 0.0010,
                    10);

            //before


            //main

            if (DashboardPlaceMarkerManager.isPlaceMarker() == false){

                if (MyConnect.isInternetAvailable() == false){

                    this.result = false;
                    this.caseFail = this.INTERNET_NOT_AVAILABLE;

                    return;

                }

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

                            caseFail = CAN_NOT_CONNECT_TO_SERVER;

                        }

                        //after
                        if (this.placesNearlyList.size() == 0){

                            placeNearly = null;

                        }
                        else{

                            if (this.placesNearlyList.size() == 1){

                                placeNearly = this.placeMin;

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

                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.MyAlertDialogStyle);
                                builder.setTitle("The places near you.(Please select)");
                                builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int item) {

                                        selectItem = item;

                                    }

                                });

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                        placeNearly = placesNearlyList.get(selectItem);

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

            }
            else{

                placeNearly = DashboardPlaceMarkerManager.getPlace();

            }

            if (placeNearly != null){

                urlList.add("http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=" + placeNearly.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10");

            }
            else{

                this.result = false;

                if (this.caseFail == this.EVERYTHING_OK){

                    this.caseFail = this.NO_DATA;

                }

                return;

            }

            String url = urlList.get(this.id);

            if (MyConnect.isInternetAvailable() == false){

                this.result = false;
                this.caseFail = this.INTERNET_NOT_AVAILABLE;

                return;

            }

            JSONObject jsonObject = MyConnect.getJSON(url);

            if (jsonObject.length() == 0){

                this.result = false;
                this.caseFail = this.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

            JSONArray jsonArray = null;

            try {

                jsonArray = jsonObject.getJSONArray("data");

            } catch (JSONException e) {

                e.printStackTrace();

            }

            if (jsonArray.length() == 0){

                this.result = false;
                this.caseFail = this.NO_DATA;

                return;

            }

            int length = jsonArray.length();
            int index = 0;

            while (index < length) {

                try {

                    JSONObject object = jsonArray.getJSONObject(index);

                    if (this.id == 0) {

                        items.add(new LastTweetsListFragment.LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), MyDateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));

                    }


                } catch (JSONException e) {

                    e.printStackTrace();

                }

                index = index + 1;


            }

            //after


        }

        private void OpenMainFragment(){

            //init
            InternetNotAvailableFragment internetNotAvailableFragment = new InternetNotAvailableFragment();
            CanNotConnectedToServerFragment notConnectingToServerFragment = new CanNotConnectedToServerFragment();
            NoDataFragment noDataFragment = new NoDataFragment();

            LastTweetsListFragment fragment = new LastTweetsListFragment();

            //before
            if (placeNearly == null){

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //init
                        TextView textView = (TextView) findViewById(R.id.text_distance);

                        //before


                        //main
                        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);

                        textView.setText("NO DISTANCE TO SHOW");

                        //after


                    }

                });

            }
            else{

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //init
                        TextView textView = (TextView) findViewById(R.id.text_distance);

                        Location location = new Location("placeNearly");

                        //before


                        //main
                        toolbar.setSubtitle(placeNearly.getName());

                        location.setLatitude(placeNearly.getLat());
                        location.setLongitude(placeNearly.getLng());
                        textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

                        //after


                    }

                });

            }

            //main
            if (this.result == false){

                if (this.caseFail == this.INTERNET_NOT_AVAILABLE){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    Thread thread = new Thread(){

                        @Override
                        public void run(){

                            mainActivity.getWindow().getDecorView().getRootView().setOnTouchListener(new View.OnTouchListener() {

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {

                                    startFragment(id);

                                    return false;
                                }

                            });

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            startFragment(id);

                        }

                    };

                    thread.run();

                }

                if(this.caseFail == this.CAN_NOT_CONNECT_TO_SERVER){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                }

                if(this.caseFail == this.NO_DATA){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                }

            }
            else{

                if (this.id == 0){

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("items", this.items);
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();

                }

            }

            //after


        }

    }

    //===========================================================================================================//
    //  RECEIVER                                                                                    RECEIVER
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****


    //  private class  -----------------------------------------------------------------------------****private class****

    private class GPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            UserLocationManager.setLatitude(b.getDouble("latitude", 0));
            UserLocationManager.setLongitude(b.getDouble("longitude", 0));

            TextView textView = (TextView) findViewById(R.id.text_distance);

            if (placeNearly != null){

                Location location = new Location("placeNearly");
                location.setLatitude(placeNearly.getLat());
                location.setLongitude(placeNearly.getLng());

                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }
            else{

                textView.setText("NO DISTANCE TO SHOW");

            }

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    @Override
    protected void sensitiveCaseForDestroyForChild() {

        //init


        //before


        //main


        //after

    }

//    private void showDialog()
//    {
//        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("The places near you.");
//        builder.setItems(commandArray, new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
////        builder.setNegativeButton("cancel",
////                new DialogInterface.OnClickListener() {
////
////                    public void onClick(DialogInterface dialog, int which) {
////                        dialog.dismiss();
////                    }
////                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

    //    ************2/12/2016 by Thianchai************

    //setContentView(R.layout.fragment_dashboard_last_tweets);

    //----------------------------------------------------------------------------------------------

    //    ************5/12/2016 by Thianchai************

//    getsupportactionbar.hide();
//    getsupportactionbar.show();

    //----------------------------------------------------------------------------------------------

//    AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
//        @Override
//        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
////
////                Log.i("test", appBarLayout.getY() + "");
//
//        }
//    };
//
//    appBarLayout.addOnOffsetChangedListener(mListener);

    //----------------------------------------------------------------------------------------------

    //    ************6/12/2016 by Thianchai************

    //                                    builder.setItems(placesTextArray, new DialogInterface.OnClickListener() {
//
//                                        public void onClick(DialogInterface dialog, int which) {
//
//                                            placeNearly = placesNearlyList.get(which);
//
//                                            countDownLatch.countDown();
//
//                                            dialog.dismiss();
//                                        }
//                                    });

    //----------------------------------------------------------------------------------------------

    //    ************7/12/2016 by Thianchai************

    //    private class ConnectToServerTask extends AsyncTask<Integer, Integer, Boolean> {
//
//        private final int INTERNET_NOT_AVAILABLE = 0;
//        private final int CAN_NOT_CONNECT_TO_SERVER = 1;
//        private final int NO_DATA = 2;
//
//        private int caseFail;
//
//        private int id;
//        private ArrayList<Parcelable> items = new ArrayList<Parcelable>();
//
//        protected Boolean doInBackground(Integer... integers) {
//
//            this.id = integers[0];
//
//            if (MyConnect.isInternetAvailable() == false){
//
//                this.caseFail = this.INTERNET_NOT_AVAILABLE;
//
//                return false;
//
//            }
//
//            final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//            api = new OneSpaceApi.Builder(getApplicationContext())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            call = api.getPlaces(UserLocationManager.getLatitude()+ 0.0010,
//                    UserLocationManager.getLongitude() + 0.0010,
//                    UserLocationManager.getLatitude() - 0.0010,
//                    UserLocationManager.getLongitude() - 0.0010,
//                    10);
//
//            call.enqueue(new Callback<ArrayList<Place>>() {
//                @Override
//                public void onResponse(final Response<ArrayList<Place>> response) {
//                    if (response.isSuccess()) {
//
//                        Place placeMin = null;
//
//                        for (Place place : response.body()) {
//
//                            if (placeMin == null){
//
//                                placeMin = place;
//
//                            }
//                            else{
//
//                                Location l1 = new Location("l1");
//                                l1.setLatitude(placeMin.getLat());
//                                l1.setLongitude(placeMin.getLng());
//
//                                Location l2 = new Location("l2");
//                                l2.setLatitude(place.getLat());
//                                l2.setLongitude(place.getLng());
//
//                                if (UserLocationManager.getLocation().distanceTo(l1) >= UserLocationManager.getLocation().distanceTo(l2)){
//
//                                    if (Math.random() > 0.5){
//
//                                        placeMin = place;
//
//                                    }
//
//                                }
//
//                            }
//
//                        }
//
//                        placeNearly = placeMin;
//
//                        //countDownLatch.countDown();
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    Log.e("GetPlace", t.toString());
//                }
//            });
//
////            try {
////                countDownLatch.await();
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
//            urlList.clear();
//            if (placeNearly != null){
//
//                urlList.add("http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=" + placeNearly.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10");
//
//            }
//            else{
//
//                urlList.add("");
//
//            }
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                    if (placeNearly != null){
//
//                        toolbar.setSubtitle(placeNearly.getName());
//
//                    }
//                    else{
//
//                        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
//
//                    }
//
//                }
//
//            });
//
//            String url = urlList.get(this.id);
//
//            if (url.equals("")){
//
//                this.caseFail = this.NO_DATA;
//
//                return false;
//
//            }
//
//            JSONObject jsonObject = MyConnect.getJSON(url);
//
//            if (jsonObject.length() == 0){
//
//                this.caseFail = this.CAN_NOT_CONNECT_TO_SERVER;
//
//                return false;
//
//            }
//            else{
//
//                JSONArray jsonArray = null;
//
//                try {
//
//                    jsonArray = jsonObject.getJSONArray("data");
//
//                } catch (JSONException e) {
//
//                    e.printStackTrace();
//
//                }
//
//                if (jsonArray.length() == 0){
//
//                    this.caseFail = this.NO_DATA;
//
//                    return false;
//
//                }
//                else{
//
//                    int length = jsonArray.length();
//                    int index = 0;
//
//                    while (index < length){
//
//                        try {
//
//                            JSONObject object = jsonArray.getJSONObject(index);
//
//                            if (this.id == 0){
//
//                                items.add(new LastTweetsListFragment.LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), MyDateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));
//
//                            }
//
//
//                        } catch (JSONException e) {
//
//                            e.printStackTrace();
//
//                        }
//
//                        index = index + 1;
//
//                    }
//
//                }
//
//            }
//
//            return true;
//        }
//
//        protected void onProgressUpdate(Integer... progress) {
//
//        }
//
//        protected void onPostExecute(Boolean result) {
//
//            InternetNotAvailableFragment internetNotAvailableFragment;
//            CanNotConnectedToServerFragment notConnectingToServerFragment;
//            NoDataFragment noDataFragment;
//
//            LastTweetsListFragment fragment;
//
//            if (result == false){
//
//                if (this.caseFail == this.INTERNET_NOT_AVAILABLE){
//
//                    internetNotAvailableFragment = new InternetNotAvailableFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                    Thread thread = new Thread(){
//
//                        @Override
//                        public void run(){
//
//                            mainActivity.getWindow().getDecorView().getRootView().setOnTouchListener(new View.OnTouchListener() {
//
//                                @Override
//                                public boolean onTouch(View v, MotionEvent event) {
//
//                                    startFragment(id);
//
//                                    return false;
//                                }
//
//                            });
//
//                            try {
//                                Thread.sleep(10000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            startFragment(id);
//
//                        }
//
//                    };
//
//                    thread.run();
//
//                }
//
//                if(this.caseFail == this.CAN_NOT_CONNECT_TO_SERVER){
//
//                    notConnectingToServerFragment = new CanNotConnectedToServerFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//                if(this.caseFail == this.NO_DATA){
//
//                    noDataFragment = new NoDataFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//            }
//            else{
//
//                if (this.id == 0){
//
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelableArrayList("items", this.items);
//
//                    fragment = new LastTweetsListFragment();
//                    fragment.setArguments(bundle);
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//            }
//
//        }
//    }

    //----------------------------------------------------------------------------------------------

//    Thread thread = new Thread(){
//
//        private final int INTERNET_NOT_AVAILABLE = 0;
//        private final int CAN_NOT_CONNECT_TO_SERVER = 1;
//        private final int NO_DATA = 2;
//
//        private boolean result;
//        private int caseFail;
//
//        private int id;
//        private ArrayList<Parcelable> items = new ArrayList<Parcelable>();
//
//        private ArrayList<Place> placesNearlyList;
//
//        private int selectItem;
//
//        @Override
//        public void run(){
//
//            this.result = true;
//            this.caseFail = -1;
//
//            this.id = index;
//
//            this.placesNearlyList = new ArrayList<Place>();
//
//            placeNearly = null;
//
//            if (MyConnect.isInternetAvailable() == false){
//
//                this.result = false;
//                this.caseFail = this.INTERNET_NOT_AVAILABLE;
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        TextView textView = (TextView) findViewById(R.id.text_distance);
//
//                        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
//
//                        textView.setText("NO DISTANCE TO SHOW");
//
//                    }
//
//                });
//
//            }
//            else{
//
//                final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//                api = new OneSpaceApi.Builder(getApplicationContext())
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//
//                call = api.getPlaces(UserLocationManager.getLatitude()+ 0.0010,
//                        UserLocationManager.getLongitude() + 0.0010,
//                        UserLocationManager.getLatitude() - 0.0010,
//                        UserLocationManager.getLongitude() - 0.0010,
//                        10);
//
//                call.enqueue(new Callback<ArrayList<Place>>() {
//                    @Override
//                    public void onResponse(final Response<ArrayList<Place>> response) {
//                        if (response.isSuccess()) {
//
//                            Place placeMin = null;
//
//                            for (Place place : response.body()) {
//
//                                if (placeMin == null){
//
//                                    placeMin = place;
//
//                                }
//                                else{
//
//                                    Location l1 = new Location("l1");
//                                    l1.setLatitude(placeMin.getLat());
//                                    l1.setLongitude(placeMin.getLng());
//
//                                    Location l2 = new Location("l2");
//                                    l2.setLatitude(place.getLat());
//                                    l2.setLongitude(place.getLng());
//
//                                    if (UserLocationManager.getLocation().distanceTo(l1) >= UserLocationManager.getLocation().distanceTo(l2)){
//
//                                        if (UserLocationManager.getLocation().distanceTo(l1) == UserLocationManager.getLocation().distanceTo(l2)){
//
//                                            placesNearlyList.add(place);
//
//                                        }
//                                        else{
//
//                                            placesNearlyList.clear();
//                                            placesNearlyList.add(place);
//                                            placeMin = place;
//
//                                        }
//
//                                    }
//
//                                }
//
//                            }
//
//                            if (placesNearlyList.size() == 0){
//
//                                placeNearly = null;
//
//                            }
//                            else{
//
//                                if (placesNearlyList.size() == 1){
//
//                                    placeNearly = placeMin;
//
//                                    countDownLatch.countDown();
//
//                                }
//                                else{
//
//                                    String[] placesTextArray = new String[placesNearlyList.size()];
//                                    Location userLocation = UserLocationManager.getLocation();
//
//                                    selectItem = 0;
//
//                                    int i = 0;
//                                    while (i < placesNearlyList.size()){
//
//                                        Location placeLocation = new Location("place");
//                                        placeLocation.setLatitude(placesNearlyList.get(i).getLat());
//                                        placeLocation.setLongitude(placesNearlyList.get(i).getLng());
//
//                                        placesTextArray[i] = (i+1) + ". " + placesNearlyList.get(i).getName() + "\n   ( distance : " + userLocation.distanceTo(placeLocation) + " m )";
//
//                                        i = i + 1;
//
//                                    }
//
//                                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.MyAlertDialogStyle);
//                                    builder.setTitle("The places near you.(Please select)");
//                                    builder.setSingleChoiceItems(placesTextArray, -1,
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int item) {
//
//                                                    selectItem = item;
//
//                                                }
//                                            });
//
//                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int id) {
//
//                                            placeNearly = placesNearlyList.get(selectItem);
//
//                                            countDownLatch.countDown();
//
//                                            dialog.dismiss();
//
//                                        }
//                                    });
//
//                                    AlertDialog alert = builder.create();
//                                    alert.show();
//
//                                    alert.getListView().setItemChecked(selectItem, true);
//
//                                }
//
//                            }
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        Log.e("GetPlace", t.toString());
//                    }
//                });
//
//                try {
//                    countDownLatch.await();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                urlList.clear();
//                if (placeNearly != null){
//
//                    urlList.add("http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=" + placeNearly.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10");
//
//                }
//                else{
//
//                    urlList.add("");
//
//                }
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        TextView textView = (TextView) findViewById(R.id.text_distance);
//
//                        if (placeNearly != null){
//
//                            toolbar.setSubtitle(placeNearly.getName());
//
//                            Location location = new Location("placeNearly");
//                            location.setLatitude(placeNearly.getLat());
//                            location.setLongitude(placeNearly.getLng());
//
//                            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");
//
//                        }
//                        else{
//
//                            toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
//
//                            textView.setText("NO DISTANCE TO SHOW");
//
//                        }
//
//                    }
//
//                });
//
//                String url = urlList.get(this.id);
//
//                if (url.equals("")){
//
//                    this.result = false;
//                    this.caseFail = this.NO_DATA;
//
//                }
//                else{
//
//                    if (MyConnect.isInternetAvailable() == false){
//
//                        this.result = false;
//                        this.caseFail = this.INTERNET_NOT_AVAILABLE;
//
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                TextView textView = (TextView) findViewById(R.id.text_distance);
//
//                                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
//
//                                textView.setText("NO DISTANCE TO SHOW");
//
//                            }
//
//                        });
//
//                    }
//                    else{
//
//                        JSONObject jsonObject = MyConnect.getJSON(url);
//
//                        if (jsonObject.length() == 0){
//
//                            this.result = false;
//                            this.caseFail = this.CAN_NOT_CONNECT_TO_SERVER;
//
//                        }
//                        else{
//
//                            JSONArray jsonArray = null;
//
//                            try {
//
//                                jsonArray = jsonObject.getJSONArray("data");
//
//                            } catch (JSONException e) {
//
//                                e.printStackTrace();
//
//                            }
//
//                            if (jsonArray.length() == 0){
//
//                                this.result = false;
//                                this.caseFail = this.NO_DATA;
//
//                            }
//                            else{
//
//                                int length = jsonArray.length();
//                                int index = 0;
//
//                                while (index < length) {
//
//                                    try {
//
//                                        JSONObject object = jsonArray.getJSONObject(index);
//
//                                        if (this.id == 0) {
//
//                                            items.add(new LastTweetsListFragment.LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), MyDateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));
//
//                                        }
//
//
//                                    } catch (JSONException e) {
//
//                                        e.printStackTrace();
//
//                                    }
//
//                                    index = index + 1;
//
//
//                                }
//
//                            }
//
//                        }
//
//                    }
//
//                }
//
//            }
//
//            InternetNotAvailableFragment internetNotAvailableFragment;
//            CanNotConnectedToServerFragment notConnectingToServerFragment;
//            NoDataFragment noDataFragment;
//
//            LastTweetsListFragment fragment;
//
//            if (result == false){
//
//                if (this.caseFail == this.INTERNET_NOT_AVAILABLE){
//
//                    internetNotAvailableFragment = new InternetNotAvailableFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                    Thread thread = new Thread(){
//
//                        @Override
//                        public void run(){
//
//                            mainActivity.getWindow().getDecorView().getRootView().setOnTouchListener(new View.OnTouchListener() {
//
//                                @Override
//                                public boolean onTouch(View v, MotionEvent event) {
//
//                                    startFragment(id);
//
//                                    return false;
//                                }
//
//                            });
//
//                            try {
//                                Thread.sleep(10000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            startFragment(id);
//
//                        }
//
//                    };
//
//                    thread.run();
//
//                }
//
//                if(this.caseFail == this.CAN_NOT_CONNECT_TO_SERVER){
//
//                    notConnectingToServerFragment = new CanNotConnectedToServerFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//                if(this.caseFail == this.NO_DATA){
//
//                    noDataFragment = new NoDataFragment();
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//            }
//            else{
//
//                if (this.id == 0){
//
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelableArrayList("items", this.items);
//
//                    fragment = new LastTweetsListFragment();
//                    fragment.setArguments(bundle);
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
//
//                }
//
//            }
//
//        }
//
//    };

}
