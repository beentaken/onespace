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
import com.sesame.onespace.fragments.dashboardFragments.carparkFragment.CarparkFragment;
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

public final class CarparkActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String CARPARK_FRAGMENT = "carpark fragment";

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

    private CarparkActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private CarparkActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        CarparkActivity.super.onCreate(savedInstanceState);

        //main
        CarparkActivity.super.setContentView(R.layout.activity_dashboard_carpark);
        CarparkActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //forced to action
        CarparkActivity.super.onStart();

        //main
        CarparkActivity.this.setDefault();
        CarparkActivity.this.start();

        CarparkActivity.super.registerReceiver(CarparkActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        CarparkActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) CarparkActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            CarparkActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        CarparkActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        CarparkActivity.super.onStop();

        //main
        CarparkActivity.super.unregisterReceiver(CarparkActivity.this.gpsBroadcastReceiver);

        if (CarparkActivity.this.alertDialog != null){

            CarparkActivity.this.alertDialog.dismiss();

        }

        if (CarparkActivity.this.call != null){

            CarparkActivity.this.call.cancel();

        }

        CarparkActivity.this.openFragmentTask.cancel(true);

        CarparkActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        CarparkActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        CarparkActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        CarparkActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) CarparkActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return CarparkActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) CarparkActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_carpark:

                CarparkActivity.this.idFragment = CarparkActivity.CARPARK_FRAGMENT;
                CarparkActivity.this.prepareToStart();
                CarparkActivity.this.start();

                break;


            case  R.id.nav_place_nearest:

                CarparkActivity.this.placeNearest = null;
                CarparkActivity.this.prepareToStart();
                CarparkActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        CarparkActivity.this.detector.onTouchEvent(me);

        return CarparkActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) CarparkActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    CarparkActivity.this.close();

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

        CarparkActivity.this.setDefaultValue();
        CarparkActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        CarparkActivity.this.idFragment = CarparkActivity.CARPARK_FRAGMENT;

        //for start
        CarparkActivity.this.result = true;
        CarparkActivity.this.caseFail = CarparkActivity.EVERYTHING_OK;
        CarparkActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        CarparkActivity.this.call = null;
        CarparkActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                CarparkActivity.this.placeNearest = place;

            }
            else{

                CarparkActivity.this.placeNearest = null;

            }

        }
        else{

            CarparkActivity.this.placeNearest = null;

        }

        CarparkActivity.this.url = null;
        CarparkActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        CarparkActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        CarparkActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        CarparkActivity.this.gpsBroadcastReceiver = new CarparkActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        CarparkActivity.this.setDefaultStatusBar();
        CarparkActivity.this.setDefaultAppBarLayout();
        CarparkActivity.this.setDefaultDrawerLayout();
        CarparkActivity.this.setDefaultNavigationView();
        CarparkActivity.this.setDefaultToolbar();
        CarparkActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = CarparkActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) CarparkActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#629B41"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) CarparkActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) CarparkActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(CarparkActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        CarparkActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) CarparkActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_carpark_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_carpark_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(CarparkActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) CarparkActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_carpark);
        toolbar.setTitle("Car Park");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#629B41"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) CarparkActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        CarparkActivity.this.result = true;
        CarparkActivity.this.caseFail = CarparkActivity.EVERYTHING_OK;
        CarparkActivity.this.call = null;
        CarparkActivity.this.callResponse = false;
        //placenearest
        CarparkActivity.this.url = null;
        CarparkActivity.this.items = new ArrayList<Parcelable>();

        CarparkActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (CarparkActivity.this.caseFail == CarparkActivity.EVERYTHING_OK){

            CarparkActivity.this.openWaitingFragment();

            CarparkActivity.this.openFragmentTask = new CarparkActivity.OpenFragmentTask();

            CarparkActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        CarparkActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            CarparkActivity.this.checkUserLocation();
            CarparkActivity.this.searchPlaceNearest();

            switch (CarparkActivity.this.idFragment){

                case CarparkActivity.CARPARK_FRAGMENT :

                    CarparkActivity.this.getURLCarpark();
                    CarparkActivity.this.getItemsCarpark();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            CarparkActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            CarparkActivity.this.result = false;
            CarparkActivity.this.caseFail = CarparkActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        CarparkActivity.this.countDownLatch = new CountDownLatch(1);
        CarparkActivity.this.call = CarparkActivity.this.api.getPlaces(userLocation.getLatitude()+ CarparkActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + CarparkActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - CarparkActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - CarparkActivity.DISTANCE_AROUND_USER,
                10);
        CarparkActivity.this.callResponse = false;

        //before
        if (CarparkActivity.this.result == false){

            return;

        }

        if (CarparkActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            CarparkActivity.this.result = false;
            CarparkActivity.this.caseFail = CarparkActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        CarparkActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    CarparkActivity.this.callResponse = true;

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

                    CarparkActivity.this.callResponse = true;
                    CarparkActivity.this.result = false;
                    CarparkActivity.this.caseFail = CarparkActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    CarparkActivity.this.placeNearest = this.placeMin;
                    CarparkActivity.this.result = false;
                    CarparkActivity.this.caseFail = CarparkActivity.NO_DATA;
                    CarparkActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        CarparkActivity.this.placeNearest = this.placeMin;

                        CarparkActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(CarparkActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                CarparkActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                CarparkActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                CarparkActivity.this.alertDialog = null;

                            }

                        });

                        CarparkActivity.this.alertDialog = builder.create();
                        CarparkActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        CarparkActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    CarparkActivity.this.alertDialog.dismiss();
                                    CarparkActivity.this.alertDialog = null;
                                    CarparkActivity.this.close();

                                }
                                return true;
                            }
                        });

                        CarparkActivity.this.alertDialog.show();

                        CarparkActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (CarparkActivity.this.callResponse == false && CarparkActivity.this.call != null){

                    CarparkActivity.this.call.cancel();
                    CarparkActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            CarparkActivity.this.countDownLatch.await();
            CarparkActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (CarparkActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                CarparkActivity.this.result = false;
                CarparkActivity.this.caseFail = CarparkActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                CarparkActivity.this.result = false;
                CarparkActivity.this.caseFail = CarparkActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        CarparkActivity.this.call = null;
        CarparkActivity.this.callResponse = false;

    }

    //url

    //items

    // car park process------------------------------------------------

    private void getURLCarpark(){

        //before
        if (CarparkActivity.this.result == false){

            return;

        }

        //main
        CarparkActivity.this.url = "http://172.29.33.45:11090/data/?tabid=0&type=lta&vloc=" + CarparkActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

    }

    private void getItemsCarpark(){

        //before
        if (CarparkActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            CarparkActivity.this.result = false;
            CarparkActivity.this.caseFail = CarparkActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(CarparkActivity.this.url);

        if (jsonObject.length() == 0){

            CarparkActivity.this.result = false;
            CarparkActivity.this.caseFail = CarparkActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            CarparkActivity.this.result = false;
            CarparkActivity.this.caseFail = CarparkActivity.NO_DATA;

            return;

        }

        int length = jsonArray.length();
        int index = 0;

        while (index < length) {

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                CarparkActivity.this.items.add(new CarparkFragment.CarparkItem(R.drawable.ic_dashboard_carpark_car, String.valueOf(object.get("area")), String.valueOf(object.get("development")), String.valueOf(object.get("available_lots")), String.valueOf(object.get("distance_in_km"))));


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

        CarparkFragment carparkFragment = new CarparkFragment();

        //main
        if (CarparkActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) CarparkActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) CarparkActivity.super.findViewById(R.id.text_distance);

            if (CarparkActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(CarparkActivity.this.placeNearest.getLat());
                location.setLongitude(CarparkActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(CarparkActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }

            if (CarparkActivity.this.caseFail == CarparkActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        CarparkActivity.this.start();

                    }

                };

                CarparkActivity.this.prepareToStart();
                thread.start();

            }

            if (CarparkActivity.this.caseFail == CarparkActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        CarparkActivity.this.prepareToStart();
                        CarparkActivity.this.start();

                    }

                };

                thread.start();

            }

            if (CarparkActivity.this.caseFail == CarparkActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        CarparkActivity.this.start();

                    }

                };

                CarparkActivity.this.prepareToStart();
                thread.start();

            }

            if (CarparkActivity.this.caseFail == CarparkActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            CarparkActivity.this.caseFail = CarparkActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) CarparkActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) CarparkActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(CarparkActivity.this.placeNearest.getLat());
            location.setLongitude(CarparkActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(CarparkActivity.this.placeNearest.getName());
            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            switch (CarparkActivity.this.idFragment){

                case CarparkActivity.CARPARK_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", CarparkActivity.this.url);
                    bundle.putParcelableArrayList("items", CarparkActivity.this.items);
                    carparkFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, carparkFragment, carparkFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            CarparkActivity.this.caseFail = CarparkActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        CarparkActivity.super.finish();

        CarparkActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) CarparkActivity.super.findViewById(R.id.text_distance);

            if (CarparkActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(CarparkActivity.this.placeNearest.getLat());
                location.setLongitude(CarparkActivity.this.placeNearest.getLng());

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
