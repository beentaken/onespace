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
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.DoNotHaveLocationFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.WaitingFragment;
import com.sesame.onespace.fragments.dashboardFragments.twitterFragment.LastTweetsFragment;
import com.sesame.onespace.interfaces.activityInterfaces.SimpleGestureFilter;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.connect.Connection;
import com.sesame.onespace.utils.date.DateConvert;

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

public final class TwitterActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String LAST_TWITTER_FRAGMENT = "last twitter fragment";

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

    private TwitterActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private TwitterActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        TwitterActivity.super.onCreate(savedInstanceState);

        //main
        TwitterActivity.super.setContentView(R.layout.activity_dashboard_twitter);
        TwitterActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //forced to action
        TwitterActivity.super.onStart();

        //main
        TwitterActivity.this.setDefault();
        TwitterActivity.this.start();

        TwitterActivity.super.registerReceiver(TwitterActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        TwitterActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) TwitterActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            TwitterActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        TwitterActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        TwitterActivity.super.onStop();

        //main
        TwitterActivity.super.unregisterReceiver(TwitterActivity.this.gpsBroadcastReceiver);

        if (TwitterActivity.this.alertDialog != null){

            TwitterActivity.this.alertDialog.dismiss();

        }

        if (TwitterActivity.this.call != null){

            TwitterActivity.this.call.cancel();

        }

        TwitterActivity.this.openFragmentTask.cancel(true);

        TwitterActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        TwitterActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        TwitterActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        TwitterActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) TwitterActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return TwitterActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) TwitterActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_last_tweets:

                TwitterActivity.this.idFragment = TwitterActivity.LAST_TWITTER_FRAGMENT;
                TwitterActivity.this.prepareToStart();
                TwitterActivity.this.start();

                break;


            case  R.id.nav_place_nearest:

                TwitterActivity.this.placeNearest = null;
                TwitterActivity.this.prepareToStart();
                TwitterActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        TwitterActivity.this.detector.onTouchEvent(me);

        return TwitterActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) TwitterActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    TwitterActivity.this.close();

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

        TwitterActivity.this.setDefaultValue();
        TwitterActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        TwitterActivity.this.idFragment = TwitterActivity.LAST_TWITTER_FRAGMENT;

        //for start
        TwitterActivity.this.result = true;
        TwitterActivity.this.caseFail = TwitterActivity.EVERYTHING_OK;
        TwitterActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        TwitterActivity.this.call = null;
        TwitterActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                TwitterActivity.this.placeNearest = place;

            }
            else{

                TwitterActivity.this.placeNearest = null;

            }

        }
        else{

            TwitterActivity.this.placeNearest = null;

        }

        TwitterActivity.this.url = null;
        TwitterActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        TwitterActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        TwitterActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        TwitterActivity.this.gpsBroadcastReceiver = new TwitterActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        TwitterActivity.this.setDefaultStatusBar();
        TwitterActivity.this.setDefaultAppBarLayout();
        TwitterActivity.this.setDefaultDrawerLayout();
        TwitterActivity.this.setDefaultNavigationView();
        TwitterActivity.this.setDefaultToolbar();
        TwitterActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = TwitterActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) TwitterActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) TwitterActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(TwitterActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        TwitterActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) TwitterActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_twitter_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_twitter_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(TwitterActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_twitter);
        toolbar.setTitle("Twitter");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        TwitterActivity.this.result = true;
        TwitterActivity.this.caseFail = TwitterActivity.EVERYTHING_OK;
        TwitterActivity.this.call = null;
        TwitterActivity.this.callResponse = false;
        //placenearest
        TwitterActivity.this.url = null;
        TwitterActivity.this.items = new ArrayList<Parcelable>();

        TwitterActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (TwitterActivity.this.caseFail == TwitterActivity.EVERYTHING_OK){

            TwitterActivity.this.openWaitingFragment();

            TwitterActivity.this.openFragmentTask = new TwitterActivity.OpenFragmentTask();

            TwitterActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        TwitterActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            TwitterActivity.this.checkUserLocation();
            TwitterActivity.this.searchPlaceNearest();

            switch (TwitterActivity.this.idFragment){

                case TwitterActivity.LAST_TWITTER_FRAGMENT :

                    TwitterActivity.this.getURLLastTwitter();
                    TwitterActivity.this.getItemsLastTwitter();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            TwitterActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            TwitterActivity.this.result = false;
            TwitterActivity.this.caseFail = TwitterActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        TwitterActivity.this.countDownLatch = new CountDownLatch(1);
        TwitterActivity.this.call = TwitterActivity.this.api.getPlaces(userLocation.getLatitude()+ TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - TwitterActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - TwitterActivity.DISTANCE_AROUND_USER,
                10);
        TwitterActivity.this.callResponse = false;

        //before
        if (TwitterActivity.this.result == false){

            return;

        }

        if (TwitterActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            TwitterActivity.this.result = false;
            TwitterActivity.this.caseFail = TwitterActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        TwitterActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    TwitterActivity.this.callResponse = true;

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

                    TwitterActivity.this.callResponse = true;
                    TwitterActivity.this.result = false;
                    TwitterActivity.this.caseFail = TwitterActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    TwitterActivity.this.placeNearest = this.placeMin;
                    TwitterActivity.this.result = false;
                    TwitterActivity.this.caseFail = TwitterActivity.NO_DATA;
                    TwitterActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        TwitterActivity.this.placeNearest = this.placeMin;

                        TwitterActivity.this.countDownLatch.countDown();

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

                                TwitterActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                TwitterActivity.this.alertDialog = null;

                            }

                        });

                        TwitterActivity.this.alertDialog = builder.create();
                        TwitterActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        TwitterActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    TwitterActivity.this.alertDialog.dismiss();
                                    TwitterActivity.this.alertDialog = null;
                                    TwitterActivity.this.close();

                                }
                                return true;
                            }
                        });

                        TwitterActivity.this.alertDialog.show();

                        TwitterActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (TwitterActivity.this.callResponse == false && TwitterActivity.this.call != null){

                    TwitterActivity.this.call.cancel();
                    TwitterActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            TwitterActivity.this.countDownLatch.await();
            TwitterActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (TwitterActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                TwitterActivity.this.result = false;
                TwitterActivity.this.caseFail = TwitterActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                TwitterActivity.this.result = false;
                TwitterActivity.this.caseFail = TwitterActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        TwitterActivity.this.call = null;
        TwitterActivity.this.callResponse = false;

    }

    //url

    //items

    // last twitter process------------------------------------------------

    private void getURLLastTwitter(){

        //before
        if (TwitterActivity.this.result == false){

            return;

        }

        //main
        TwitterActivity.this.url = "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=" + TwitterActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

    }

    private void getItemsLastTwitter(){

        //before
        if (TwitterActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            TwitterActivity.this.result = false;
            TwitterActivity.this.caseFail = TwitterActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(TwitterActivity.this.url);

        if (jsonObject.length() == 0){

            TwitterActivity.this.result = false;
            TwitterActivity.this.caseFail = TwitterActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            TwitterActivity.this.result = false;
            TwitterActivity.this.caseFail = TwitterActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                TwitterActivity.this.items.add(new LastTweetsFragment.LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), DateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));


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

        LastTweetsFragment lastTweetsFragment = new LastTweetsFragment();

        //main
        if (TwitterActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

            if (TwitterActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(TwitterActivity.this.placeNearest.getLat());
                location.setLongitude(TwitterActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(TwitterActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }

            if (TwitterActivity.this.caseFail == TwitterActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        TwitterActivity.this.start();

                    }

                };

                TwitterActivity.this.prepareToStart();
                thread.start();

            }

            if (TwitterActivity.this.caseFail == TwitterActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        TwitterActivity.this.prepareToStart();
                        TwitterActivity.this.start();

                    }

                };

                thread.start();

            }

            if (TwitterActivity.this.caseFail == TwitterActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        TwitterActivity.this.start();

                    }

                };

                TwitterActivity.this.prepareToStart();
                thread.start();

            }

            if (TwitterActivity.this.caseFail == TwitterActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            TwitterActivity.this.caseFail = TwitterActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) TwitterActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(TwitterActivity.this.placeNearest.getLat());
            location.setLongitude(TwitterActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(TwitterActivity.this.placeNearest.getName());
            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            switch (TwitterActivity.this.idFragment){

                case TwitterActivity.LAST_TWITTER_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", TwitterActivity.this.url);
                    bundle.putParcelableArrayList("items", TwitterActivity.this.items);
                    lastTweetsFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, lastTweetsFragment, lastTweetsFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            TwitterActivity.this.caseFail = TwitterActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        TwitterActivity.super.finish();

        TwitterActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) TwitterActivity.super.findViewById(R.id.text_distance);

            if (TwitterActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(TwitterActivity.this.placeNearest.getLat());
                location.setLongitude(TwitterActivity.this.placeNearest.getLng());

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
