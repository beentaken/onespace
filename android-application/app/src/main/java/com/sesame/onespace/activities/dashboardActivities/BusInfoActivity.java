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
import com.sesame.onespace.fragments.dashboardFragments.busInfoFragment.BusInfoFragment;
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

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;

/**
 * Created by Thian on 4/1/2560.
 */

public final class BusInfoActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String BUSINFO_FRAGMENT = "bus info fragment";

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

    private BusInfoActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private BusInfoActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        BusInfoActivity.super.onCreate(savedInstanceState);

        //main
        BusInfoActivity.super.setContentView(R.layout.activity_dashboard_bus_info);
        BusInfoActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //forced to action
        BusInfoActivity.super.onStart();

        //main
        BusInfoActivity.this.setDefault();
        BusInfoActivity.this.start();

        BusInfoActivity.super.registerReceiver(BusInfoActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        BusInfoActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) BusInfoActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            BusInfoActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        BusInfoActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        BusInfoActivity.super.onStop();

        //main
        BusInfoActivity.super.unregisterReceiver(BusInfoActivity.this.gpsBroadcastReceiver);

        if (BusInfoActivity.this.alertDialog != null){

            BusInfoActivity.this.alertDialog.dismiss();

        }

        if (BusInfoActivity.this.call != null){

            BusInfoActivity.this.call.cancel();

        }

        BusInfoActivity.this.openFragmentTask.cancel(true);

        BusInfoActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        BusInfoActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        BusInfoActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        BusInfoActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) BusInfoActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return BusInfoActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) BusInfoActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_bus_info:

                BusInfoActivity.this.idFragment = BusInfoActivity.BUSINFO_FRAGMENT;
                BusInfoActivity.this.prepareToStart();
                BusInfoActivity.this.start();

                break;


            case  R.id.nav_place_nearest:

                BusInfoActivity.this.placeNearest = null;
                BusInfoActivity.this.prepareToStart();
                BusInfoActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        BusInfoActivity.this.detector.onTouchEvent(me);

        return BusInfoActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) BusInfoActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    BusInfoActivity.this.close();

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

        BusInfoActivity.this.setDefaultValue();
        BusInfoActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        BusInfoActivity.this.idFragment = BusInfoActivity.BUSINFO_FRAGMENT;

        //for start
        BusInfoActivity.this.result = true;
        BusInfoActivity.this.caseFail = BusInfoActivity.EVERYTHING_OK;
        BusInfoActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        BusInfoActivity.this.call = null;
        BusInfoActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                BusInfoActivity.this.placeNearest = place;

            }
            else{

                BusInfoActivity.this.placeNearest = null;

            }

        }
        else{

            BusInfoActivity.this.placeNearest = null;

        }

        BusInfoActivity.this.url = null;
        BusInfoActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        BusInfoActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        BusInfoActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        BusInfoActivity.this.gpsBroadcastReceiver = new BusInfoActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        BusInfoActivity.this.setDefaultStatusBar();
        BusInfoActivity.this.setDefaultAppBarLayout();
        BusInfoActivity.this.setDefaultDrawerLayout();
        BusInfoActivity.this.setDefaultNavigationView();
        BusInfoActivity.this.setDefaultToolbar();
        BusInfoActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = BusInfoActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) BusInfoActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#629B41"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) BusInfoActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) BusInfoActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(BusInfoActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        BusInfoActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) BusInfoActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_bus_info_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_bus_info_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(BusInfoActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) BusInfoActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_bus_info);
        toolbar.setTitle("Bus Info");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#629B41"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) BusInfoActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        BusInfoActivity.this.result = true;
        BusInfoActivity.this.caseFail = BusInfoActivity.EVERYTHING_OK;
        BusInfoActivity.this.call = null;
        BusInfoActivity.this.callResponse = false;
        //placenearest
        BusInfoActivity.this.url = null;
        BusInfoActivity.this.items = new ArrayList<Parcelable>();

        BusInfoActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (BusInfoActivity.this.caseFail == BusInfoActivity.EVERYTHING_OK){

            BusInfoActivity.this.openWaitingFragment();

            BusInfoActivity.this.openFragmentTask = new BusInfoActivity.OpenFragmentTask();

            BusInfoActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        BusInfoActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            BusInfoActivity.this.checkUserLocation();
            BusInfoActivity.this.searchPlaceNearest();

            switch (BusInfoActivity.this.idFragment){

                case BusInfoActivity.BUSINFO_FRAGMENT :

                    BusInfoActivity.this.getURLBusInfo();
                    BusInfoActivity.this.getItemsBusInfo();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            BusInfoActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            BusInfoActivity.this.result = false;
            BusInfoActivity.this.caseFail = BusInfoActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        BusInfoActivity.this.countDownLatch = new CountDownLatch(1);
        BusInfoActivity.this.call = BusInfoActivity.this.api.getPlaces(userLocation.getLatitude()+ BusInfoActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + BusInfoActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - BusInfoActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - BusInfoActivity.DISTANCE_AROUND_USER,
                10);
        BusInfoActivity.this.callResponse = false;

        //before
        if (BusInfoActivity.this.result == false){

            return;

        }

        if (BusInfoActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            BusInfoActivity.this.result = false;
            BusInfoActivity.this.caseFail = BusInfoActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        BusInfoActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    BusInfoActivity.this.callResponse = true;

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

                    BusInfoActivity.this.callResponse = true;
                    BusInfoActivity.this.result = false;
                    BusInfoActivity.this.caseFail = BusInfoActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    BusInfoActivity.this.placeNearest = this.placeMin;
                    BusInfoActivity.this.result = false;
                    BusInfoActivity.this.caseFail = BusInfoActivity.NO_DATA;
                    BusInfoActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        BusInfoActivity.this.placeNearest = this.placeMin;

                        BusInfoActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(BusInfoActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                BusInfoActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                BusInfoActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                BusInfoActivity.this.alertDialog = null;

                            }

                        });

                        BusInfoActivity.this.alertDialog = builder.create();
                        BusInfoActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        BusInfoActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    BusInfoActivity.this.alertDialog.dismiss();
                                    BusInfoActivity.this.alertDialog = null;
                                    BusInfoActivity.this.close();

                                }
                                return true;
                            }
                        });

                        BusInfoActivity.this.alertDialog.show();

                        BusInfoActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (BusInfoActivity.this.callResponse == false && BusInfoActivity.this.call != null){

                    BusInfoActivity.this.call.cancel();
                    BusInfoActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            BusInfoActivity.this.countDownLatch.await();
            BusInfoActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (BusInfoActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                BusInfoActivity.this.result = false;
                BusInfoActivity.this.caseFail = BusInfoActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                BusInfoActivity.this.result = false;
                BusInfoActivity.this.caseFail = BusInfoActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        BusInfoActivity.this.call = null;
        BusInfoActivity.this.callResponse = false;

    }

    //url

    //items

    // bus info process------------------------------------------------

    private void getURLBusInfo(){

        //before
        if (BusInfoActivity.this.result == false){

            return;

        }

        //main
        BusInfoActivity.this.url = "http://172.29.33.45:11090/data/?tabid=0&type=ltabuses&vloc=" + BusInfoActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

    }

    private void getItemsBusInfo(){

        //before
        if (BusInfoActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            BusInfoActivity.this.result = false;
            BusInfoActivity.this.caseFail = BusInfoActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(BusInfoActivity.this.url);

        if (jsonObject.length() == 0){

            BusInfoActivity.this.result = false;
            BusInfoActivity.this.caseFail = BusInfoActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            BusInfoActivity.this.result = false;
            BusInfoActivity.this.caseFail = BusInfoActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                JSONArray arrivalArray = (JSONArray) object.get("arrival_times");

                int arrivalIndex = 0;

                String arrivalString = "";

                while(arrivalIndex < arrivalArray.length()){

                    JSONObject arrivalObject = (JSONObject) arrivalArray.get(arrivalIndex);

                    String arrivalTime = ((String)arrivalObject.get("arrival_time")).substring(11, 19);

                    String hour = (Integer.parseInt(arrivalTime.substring(0, 2)) + 8) + "";

                    arrivalTime = hour + arrivalTime.substring(2, 8);

                    arrivalString = arrivalString + "\nService : " + arrivalObject.get("service_nr") + "\nArrival Time : " + arrivalTime + "\n";

                    arrivalIndex = arrivalIndex + 1;

                }

                BusInfoActivity.this.items.add(new BusInfoFragment.BusInfoItem(R.drawable.ic_dashboard_bus_info_bus, String.valueOf(object.get("description")), String.valueOf(object.get("distance_in_km")), arrivalString));


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

        BusInfoFragment busInfoFragment = new BusInfoFragment();

        //main
        if (BusInfoActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) BusInfoActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) BusInfoActivity.super.findViewById(R.id.text_distance);

            if (BusInfoActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(BusInfoActivity.this.placeNearest.getLat());
                location.setLongitude(BusInfoActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(BusInfoActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }

            if (BusInfoActivity.this.caseFail == BusInfoActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        BusInfoActivity.this.start();

                    }

                };

                BusInfoActivity.this.prepareToStart();
                thread.start();

            }

            if (BusInfoActivity.this.caseFail == BusInfoActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        BusInfoActivity.this.prepareToStart();
                        BusInfoActivity.this.start();

                    }

                };

                thread.start();

            }

            if (BusInfoActivity.this.caseFail == BusInfoActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        BusInfoActivity.this.start();

                    }

                };

                BusInfoActivity.this.prepareToStart();
                thread.start();

            }

            if (BusInfoActivity.this.caseFail == BusInfoActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            BusInfoActivity.this.caseFail = BusInfoActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) BusInfoActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) BusInfoActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(BusInfoActivity.this.placeNearest.getLat());
            location.setLongitude(BusInfoActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(BusInfoActivity.this.placeNearest.getName());
            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            switch (BusInfoActivity.this.idFragment){

                case BusInfoActivity.BUSINFO_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", BusInfoActivity.this.url);
                    bundle.putParcelableArrayList("items", BusInfoActivity.this.items);
                    busInfoFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, busInfoFragment, busInfoFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            BusInfoActivity.this.caseFail = BusInfoActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        BusInfoActivity.super.finish();

        BusInfoActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) BusInfoActivity.super.findViewById(R.id.text_distance);

            if (BusInfoActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(BusInfoActivity.this.placeNearest.getLat());
                location.setLongitude(BusInfoActivity.this.placeNearest.getLng());

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
