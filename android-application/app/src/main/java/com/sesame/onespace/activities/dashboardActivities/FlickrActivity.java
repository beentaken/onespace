package com.sesame.onespace.activities.dashboardActivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.sesame.onespace.fragments.dashboardFragments.flickrFragment.FlickrFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.DoNotHaveLocationFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.WaitingFragment;
import com.sesame.onespace.interfaces.activityInterfaces.SimpleGestureFilter;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.connect.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;

/**
 * Created by Thian on 19/12/2559.
 */

public final class FlickrActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
        NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String FLICKR_FRAGMENT = "flickr fragment";

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

    private FlickrActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private FlickrActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        FlickrActivity.super.onCreate(savedInstanceState);

        //main
        FlickrActivity.super.setContentView(R.layout.activity_dashboard_flickr);
        FlickrActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //forced to action
        FlickrActivity.super.onStart();

        //main
        FlickrActivity.this.setDefault();
        FlickrActivity.this.start();

        FlickrActivity.super.registerReceiver(FlickrActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        FlickrActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) FlickrActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            FlickrActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        FlickrActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        FlickrActivity.super.onStop();

        //main
        FlickrActivity.super.unregisterReceiver(FlickrActivity.this.gpsBroadcastReceiver);

        if (FlickrActivity.this.alertDialog != null){

            FlickrActivity.this.alertDialog.dismiss();

        }

        if (FlickrActivity.this.call != null){

            FlickrActivity.this.call.cancel();

        }

        FlickrActivity.this.openFragmentTask.cancel(true);

        FlickrActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        FlickrActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        FlickrActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        FlickrActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) FlickrActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return FlickrActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawerLayout = (DrawerLayout) FlickrActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_flickr:

                FlickrActivity.this.idFragment = FlickrActivity.FLICKR_FRAGMENT;
                FlickrActivity.this.prepareToStart();
                FlickrActivity.this.start();

                break;

            case  R.id.nav_place_nearest:

                FlickrActivity.this.placeNearest = null;
                FlickrActivity.this.prepareToStart();
                FlickrActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        FlickrActivity.this.detector.onTouchEvent(me);

        return FlickrActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) FlickrActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    FlickrActivity.this.close();

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

        FlickrActivity.this.setDefaultValue();
        FlickrActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        FlickrActivity.this.idFragment = FlickrActivity.FLICKR_FRAGMENT;

        //for start
        FlickrActivity.this.result = true;
        FlickrActivity.this.caseFail = FlickrActivity.EVERYTHING_OK;
        FlickrActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        FlickrActivity.this.call = null;
        FlickrActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                FlickrActivity.this.placeNearest = place;

            }
            else{

                FlickrActivity.this.placeNearest = null;

            }

        }
        else{

            FlickrActivity.this.placeNearest = null;

        }

        FlickrActivity.this.url = null;
        FlickrActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        FlickrActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        FlickrActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        FlickrActivity.this.gpsBroadcastReceiver = new FlickrActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        FlickrActivity.this.setDefaultStatusBar();
        FlickrActivity.this.setDefaultAppBarLayout();
        FlickrActivity.this.setDefaultDrawerLayout();
        FlickrActivity.this.setDefaultNavigationView();
        FlickrActivity.this.setDefaultToolbar();
        FlickrActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = FlickrActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) FlickrActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#3F474A"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) FlickrActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) FlickrActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(FlickrActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        FlickrActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) FlickrActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_flickr_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_flickr_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(FlickrActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) FlickrActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_flickr);
        toolbar.setTitle("Flickr");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#3F474A"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) FlickrActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        FlickrActivity.this.result = true;
        FlickrActivity.this.caseFail = FlickrActivity.EVERYTHING_OK;
        FlickrActivity.this.call = null;
        FlickrActivity.this.callResponse = false;
        //placenearest
        FlickrActivity.this.url = null;
        FlickrActivity.this.items = new ArrayList<Parcelable>();

        FlickrActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (FlickrActivity.this.caseFail == FlickrActivity.EVERYTHING_OK){

            FlickrActivity.this.openWaitingFragment();

            FlickrActivity.this.openFragmentTask = new FlickrActivity.OpenFragmentTask();

            FlickrActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        FlickrActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            FlickrActivity.this.checkUserLocation();
            FlickrActivity.this.searchPlaceNearest();

            switch (FlickrActivity.this.idFragment){

                case FlickrActivity.FLICKR_FRAGMENT :

                    FlickrActivity.this.getURLFlickr();
                    FlickrActivity.this.getItemsFlickr();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            FlickrActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            FlickrActivity.this.result = false;
            FlickrActivity.this.caseFail = FlickrActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        FlickrActivity.this.countDownLatch = new CountDownLatch(1);
        FlickrActivity.this.call = FlickrActivity.this.api.getPlaces(userLocation.getLatitude()+ FlickrActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + FlickrActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - FlickrActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - FlickrActivity.DISTANCE_AROUND_USER,
                10);
        FlickrActivity.this.callResponse = false;

        //before
        if (FlickrActivity.this.result == false){

            return;

        }

        if (FlickrActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            FlickrActivity.this.result = false;
            FlickrActivity.this.caseFail = FlickrActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        FlickrActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    FlickrActivity.this.callResponse = true;

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

                    FlickrActivity.this.callResponse = true;
                    FlickrActivity.this.result = false;
                    FlickrActivity.this.caseFail = FlickrActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    FlickrActivity.this.placeNearest = this.placeMin;
                    FlickrActivity.this.result = false;
                    FlickrActivity.this.caseFail = FlickrActivity.NO_DATA;
                    FlickrActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        FlickrActivity.this.placeNearest = this.placeMin;

                        FlickrActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(FlickrActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                FlickrActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                FlickrActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                FlickrActivity.this.alertDialog = null;

                            }

                        });

                        FlickrActivity.this.alertDialog = builder.create();
                        FlickrActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        FlickrActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    FlickrActivity.this.alertDialog.dismiss();
                                    FlickrActivity.this.alertDialog = null;
                                    FlickrActivity.this.close();

                                }
                                return true;
                            }
                        });

                        FlickrActivity.this.alertDialog.show();

                        FlickrActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (FlickrActivity.this.callResponse == false && FlickrActivity.this.call != null){

                    FlickrActivity.this.call.cancel();
                    FlickrActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            FlickrActivity.this.countDownLatch.await();
            FlickrActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (FlickrActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                FlickrActivity.this.result = false;
                FlickrActivity.this.caseFail = FlickrActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                FlickrActivity.this.result = false;
                FlickrActivity.this.caseFail = FlickrActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        FlickrActivity.this.call = null;
        FlickrActivity.this.callResponse = false;

    }

    //url

    //items

    // flickr process------------------------------------------------

    private void getURLFlickr(){

        //before
        if (FlickrActivity.this.result == false){

            return;

        }

        //main
        FlickrActivity.this.url = "http://172.29.33.45:11090/data/?tabid=0&type=flickr&vloc=" + FlickrActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

    }

    private void getItemsFlickr(){

        //before
        if (FlickrActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            FlickrActivity.this.result = false;
            FlickrActivity.this.caseFail = FlickrActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(FlickrActivity.this.url);

        if (jsonObject.length() == 0){

            FlickrActivity.this.result = false;
            FlickrActivity.this.caseFail = FlickrActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            FlickrActivity.this.result = false;
            FlickrActivity.this.caseFail = FlickrActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                URL newurl = null;
                try {
                    newurl = new URL("http://" + String.valueOf(object.get("url")));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FlickrActivity.this.items.add(new FlickrFragment.FlickrItem(String.valueOf(object.get("title")), bitmap));


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

        FlickrFragment flickrFragment = new FlickrFragment();

        //main
        if (FlickrActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) FlickrActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) FlickrActivity.super.findViewById(R.id.text_distance);

            if (FlickrActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(FlickrActivity.this.placeNearest.getLat());
                location.setLongitude(FlickrActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(FlickrActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }

            if (FlickrActivity.this.caseFail == FlickrActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        FlickrActivity.this.start();

                    }

                };

                FlickrActivity.this.prepareToStart();
                thread.start();

            }

            if (FlickrActivity.this.caseFail == FlickrActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        FlickrActivity.this.prepareToStart();
                        FlickrActivity.this.start();

                    }

                };

                thread.start();

            }

            if (FlickrActivity.this.caseFail == FlickrActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        FlickrActivity.this.start();

                    }

                };

                FlickrActivity.this.prepareToStart();
                thread.start();

            }

            if (FlickrActivity.this.caseFail == FlickrActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            FlickrActivity.this.caseFail = FlickrActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) FlickrActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) FlickrActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(FlickrActivity.this.placeNearest.getLat());
            location.setLongitude(FlickrActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(FlickrActivity.this.placeNearest.getName());
            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            switch (FlickrActivity.this.idFragment){

                case FlickrActivity.FLICKR_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", FlickrActivity.this.url);
                    bundle.putParcelableArrayList("items", FlickrActivity.this.items);
                    flickrFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, flickrFragment, flickrFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            FlickrActivity.this.caseFail = FlickrActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        FlickrActivity.super.finish();

        FlickrActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) FlickrActivity.super.findViewById(R.id.text_distance);

            if (FlickrActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(FlickrActivity.this.placeNearest.getLat());
                location.setLongitude(FlickrActivity.this.placeNearest.getLng());

                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

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
