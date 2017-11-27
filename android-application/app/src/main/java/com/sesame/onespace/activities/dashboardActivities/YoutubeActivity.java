package com.sesame.onespace.activities.dashboardActivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.fragments.MainMenuFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.DoNotHaveLocationFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.WaitingFragment;
import com.sesame.onespace.fragments.dashboardFragments.youtubeFragment.YoutubeFragment;
import com.sesame.onespace.interfaces.activityInterfaces.SimpleGestureFilter;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.Log;
import com.sesame.onespace.utils.connect.Connection;

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
 * Created by Thian on 19/12/2559.
 */

public final class YoutubeActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String YOUTUBE_FRAGMENT = "youtube fragment";

    //for start ------------------------------------------------------------------------------------
    private boolean result;
    private String caseFail;

    private final static String EVERYTHING_OK = "everything ok";
    private final static String DO_NOT_HAVE_LOCATION = "do not have location";
    private final static String INTERNET_NOT_AVAILABLE = "internet not available";
    private final static String CAN_NOT_CONNECT_TO_SERVER = "can not connect to server";
    private final static String NO_DATA = "no data";
    private final static String END = "end";

    private OneSpaceApi.Service api;
    private Call call;
    private boolean callResponse;  //bad code

    private final static double DISTANCE_AROUND_USER = 0.001;

    private Place placeNearest;
    private String url;
    private ArrayList<Parcelable> items;

    private YoutubeActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private YoutubeActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    private SettingsManager settingManager;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        YoutubeActivity.super.onCreate(savedInstanceState);

        //main
        YoutubeActivity.super.setContentView(R.layout.activity_dashboard_youtube);
        YoutubeActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

        this.settingManager = SettingsManager.getSettingsManager(getApplicationContext());
    }

    @Override
    protected void onStart(){

        //forced to action
        YoutubeActivity.super.onStart();

        //main
        YoutubeActivity.this.setDefault();
        YoutubeActivity.this.start();

        YoutubeActivity.super.registerReceiver(YoutubeActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        YoutubeActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) YoutubeActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            YoutubeActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        YoutubeActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        YoutubeActivity.super.onStop();

        //main
        YoutubeActivity.super.unregisterReceiver(YoutubeActivity.this.gpsBroadcastReceiver);

        if (YoutubeActivity.this.alertDialog != null){

            YoutubeActivity.this.alertDialog.dismiss();

        }

        if (YoutubeActivity.this.call != null){

            YoutubeActivity.this.call.cancel();

        }

        YoutubeActivity.this.openFragmentTask.cancel(true);

        YoutubeActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        YoutubeActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        YoutubeActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        YoutubeActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) YoutubeActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return YoutubeActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawerLayout = (DrawerLayout) YoutubeActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_youtube:

                YoutubeActivity.this.idFragment = YoutubeActivity.YOUTUBE_FRAGMENT;
                YoutubeActivity.this.prepareToStart();
                YoutubeActivity.this.start();

                break;

            case  R.id.nav_place_nearest:

                YoutubeActivity.this.placeNearest = null;
                YoutubeActivity.this.prepareToStart();
                YoutubeActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        YoutubeActivity.this.detector.onTouchEvent(me);

        return YoutubeActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) YoutubeActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    YoutubeActivity.this.close();

                    break;

            }

        }

    }

    @Override
    public void onDoubleTap() {



    }

    //===========================================================================================================//
    //  SET DEFAULT                                                                                 SET DEFAULT
    //===========================================================================================================//

    private void setDefault(){

        YoutubeActivity.this.setDefaultValue();
        YoutubeActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        YoutubeActivity.this.idFragment = YoutubeActivity.YOUTUBE_FRAGMENT;

        //for start
        YoutubeActivity.this.result = true;
        YoutubeActivity.this.caseFail = YoutubeActivity.EVERYTHING_OK;
        YoutubeActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        YoutubeActivity.this.call = null;
        YoutubeActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                YoutubeActivity.this.placeNearest = place;

            }
            else{

                YoutubeActivity.this.placeNearest = null;

            }

        }
        else{

            YoutubeActivity.this.placeNearest = null;

        }

        YoutubeActivity.this.url = null;
        YoutubeActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        YoutubeActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        YoutubeActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        YoutubeActivity.this.gpsBroadcastReceiver = new YoutubeActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        YoutubeActivity.this.setDefaultStatusBar();
        YoutubeActivity.this.setDefaultAppBarLayout();
        YoutubeActivity.this.setDefaultDrawerLayout();
        YoutubeActivity.this.setDefaultNavigationView();
        YoutubeActivity.this.setDefaultToolbar();
        YoutubeActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = YoutubeActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) YoutubeActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#D40000"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) YoutubeActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) YoutubeActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(YoutubeActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        YoutubeActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) YoutubeActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_youtube_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_youtube_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(YoutubeActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) YoutubeActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_youtube);
        toolbar.setTitle("Youtube");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#D40000"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) YoutubeActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        YoutubeActivity.this.result = true;
        YoutubeActivity.this.caseFail = YoutubeActivity.EVERYTHING_OK;
        YoutubeActivity.this.call = null;
        YoutubeActivity.this.callResponse = false;
        //placenearest
        YoutubeActivity.this.url = null;
        YoutubeActivity.this.items = new ArrayList<Parcelable>();

        YoutubeActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (YoutubeActivity.this.caseFail == YoutubeActivity.EVERYTHING_OK){

            YoutubeActivity.this.openWaitingFragment();

            YoutubeActivity.this.openFragmentTask = new YoutubeActivity.OpenFragmentTask();

            YoutubeActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        YoutubeActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            YoutubeActivity.this.checkUserLocation();
            YoutubeActivity.this.searchPlaceNearest();

            switch (YoutubeActivity.this.idFragment){

                case YoutubeActivity.YOUTUBE_FRAGMENT :

                    YoutubeActivity.this.getURLYoutube();
                    YoutubeActivity.this.getItemsYoutube();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            YoutubeActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            YoutubeActivity.this.result = false;
            YoutubeActivity.this.caseFail = YoutubeActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        YoutubeActivity.this.countDownLatch = new CountDownLatch(1);
        YoutubeActivity.this.call = YoutubeActivity.this.api.getPlaces(userLocation.getLatitude()+ YoutubeActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + YoutubeActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - YoutubeActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - YoutubeActivity.DISTANCE_AROUND_USER,
                10);
        YoutubeActivity.this.callResponse = false;

        //before
        if (YoutubeActivity.this.result == false){

            return;

        }

        if (YoutubeActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            YoutubeActivity.this.result = false;
            YoutubeActivity.this.caseFail = YoutubeActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        YoutubeActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                //main
                if (response.isSuccess()) {

                    YoutubeActivity.this.callResponse = true;

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

                    YoutubeActivity.this.callResponse = true;
                    YoutubeActivity.this.result = false;
                    YoutubeActivity.this.caseFail = YoutubeActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    YoutubeActivity.this.placeNearest = this.placeMin;
                    YoutubeActivity.this.result = false;
                    YoutubeActivity.this.caseFail = YoutubeActivity.NO_DATA;
                    YoutubeActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        YoutubeActivity.this.placeNearest = this.placeMin;

                        YoutubeActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(YoutubeActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                YoutubeActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                YoutubeActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                YoutubeActivity.this.alertDialog = null;

                            }

                        });

                        YoutubeActivity.this.alertDialog = builder.create();
                        YoutubeActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        YoutubeActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    YoutubeActivity.this.alertDialog.dismiss();
                                    YoutubeActivity.this.alertDialog = null;
                                    YoutubeActivity.this.close();

                                }
                                return true;
                            }
                        });

                        YoutubeActivity.this.alertDialog.show();

                        YoutubeActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

                    }

                }
            }

            @Override
            public void onFailure(Throwable t) {

            }

        });

        Thread thread = new Thread(){

            @Override
            public void run(){

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (YoutubeActivity.this.callResponse == false && YoutubeActivity.this.call != null){

                    YoutubeActivity.this.call.cancel();
                    YoutubeActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            YoutubeActivity.this.countDownLatch.await();
            YoutubeActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (YoutubeActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                YoutubeActivity.this.result = false;
                YoutubeActivity.this.caseFail = YoutubeActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                YoutubeActivity.this.result = false;
                YoutubeActivity.this.caseFail = YoutubeActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        YoutubeActivity.this.call = null;
        YoutubeActivity.this.callResponse = false;

    }

    //url

    //items

    // youtube process------------------------------------------------

    private void getURLYoutube(){

        //before
        if (YoutubeActivity.this.result == false){

            return;

        }

        //main
        YoutubeActivity.this.url = this.settingManager.getOnespaceServerURL() + "/data/?tabid=0&type=youtube&vloc=" + YoutubeActivity.this.placeNearest.getVloc() + "&vlocsha1=DOESNOMATTER&limit=10";

    }

    private void getItemsYoutube(){

        //before
        if (YoutubeActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            YoutubeActivity.this.result = false;
            YoutubeActivity.this.caseFail = YoutubeActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(YoutubeActivity.this.url);

        if (jsonObject.length() == 0){

            YoutubeActivity.this.result = false;
            YoutubeActivity.this.caseFail = YoutubeActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            YoutubeActivity.this.result = false;
            YoutubeActivity.this.caseFail = YoutubeActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                YoutubeActivity.this.items.add(new YoutubeFragment.YoutubeItem(String.valueOf(object.get("title")), String.valueOf(object.get("url"))));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;


        }

    }

    // open fragment process------------------------------------------------------------------------

    private void openFragment(){

        //init
        DoNotHaveLocationFragment doNotHaveLocationFragment = new DoNotHaveLocationFragment();
        InternetNotAvailableFragment internetNotAvailableFragment = new InternetNotAvailableFragment();
        CanNotConnectedToServerFragment notConnectingToServerFragment = new CanNotConnectedToServerFragment();
        NoDataFragment noDataFragment = new NoDataFragment();

        YoutubeFragment youtubeFragment = new YoutubeFragment();

        //main
        if (YoutubeActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) YoutubeActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) YoutubeActivity.super.findViewById(R.id.text_distance);

            if (YoutubeActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(YoutubeActivity.this.placeNearest.getLat());
                location.setLongitude(YoutubeActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(YoutubeActivity.this.placeNearest.getName());
                double distance = MainMenuFragment.roundToDecimal((UserLocationManager.getLocation().distanceTo(location)) / 1000, 2);
                textView.setText("DISTANCE " + distance + " km");

            }

            if (YoutubeActivity.this.caseFail == YoutubeActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        YoutubeActivity.this.start();

                    }

                };

                YoutubeActivity.this.prepareToStart();
                thread.start();

            }

            if (YoutubeActivity.this.caseFail == YoutubeActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        YoutubeActivity.this.prepareToStart();
                        YoutubeActivity.this.start();

                    }

                };

                thread.start();

            }

            if (YoutubeActivity.this.caseFail == YoutubeActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        YoutubeActivity.this.start();

                    }

                };

                YoutubeActivity.this.prepareToStart();
                thread.start();

            }

            if (YoutubeActivity.this.caseFail == YoutubeActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            YoutubeActivity.this.caseFail = YoutubeActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) YoutubeActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) YoutubeActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(YoutubeActivity.this.placeNearest.getLat());
            location.setLongitude(YoutubeActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(YoutubeActivity.this.placeNearest.getName());
            double distance = MainMenuFragment.roundToDecimal((UserLocationManager.getLocation().distanceTo(location)) / 1000, 2);
            textView.setText("DISTANCE " + distance + " km");

            switch (YoutubeActivity.this.idFragment){

                case YoutubeActivity.YOUTUBE_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", YoutubeActivity.this.url);
                    bundle.putParcelableArrayList("items", YoutubeActivity.this.items);
                    youtubeFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, youtubeFragment, youtubeFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            YoutubeActivity.this.caseFail = YoutubeActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        YoutubeActivity.super.finish();

        YoutubeActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

    }

    //===========================================================================================================//
    //  CLASS GPSBroadcastReceiver                                                                  CLASS GPSBroadcastReceiver
    //===========================================================================================================//

    private class GPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //init
            Bundle b = intent.getExtras();

            //before
            UserLocationManager.setLatitude(b.getDouble("latitude", 0));
            UserLocationManager.setLongitude(b.getDouble("longitude", 0));

            //main
            TextView textView = (TextView) YoutubeActivity.super.findViewById(R.id.text_distance);

            if (YoutubeActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(YoutubeActivity.this.placeNearest.getLat());
                location.setLongitude(YoutubeActivity.this.placeNearest.getLng());

                double distance = MainMenuFragment.roundToDecimal((UserLocationManager.getLocation().distanceTo(location)) / 1000, 2);
                textView.setText("DISTANCE " + distance + " km");

            }
            else{

                textView.setText("NO DISTANCE TO SHOW");

            }

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

    //countdownlacth

}
