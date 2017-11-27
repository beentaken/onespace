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
import com.sesame.onespace.fragments.MainMenuFragment;
import com.sesame.onespace.fragments.dashboardFragments.instagramFragment.InstagramFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.DoNotHaveLocationFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.WaitingFragment;
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

public final class InstagramActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String INSTAGRAM_FRAGMENT = "instagram fragment";

    //for start ------------------------------------------------------------------------------------
    private boolean result;
    private String caseFail;

    private final static String EVERYTHING_OK = "everything ok";
    private final static String DO_NOT_HAVE_LOCATION = "do not have location";
    private final static String INTERNET_NOT_AVAILABLE = "internet not available";
    private final static String CAN_NOT_CONNECT_TO_SERVER = "can not connect to server";
    private final static String NO_DATA = "no data";
    private final static String END = "end";

    private final static String DEFAULT_URL__IMAGE_NOT_FOUND = "http://ubicomp-web.d1.comp.nus.edu.sg/onespace/media/images/onespace-default-no-image-found.jpg";

    private OneSpaceApi.Service api;
    private Call call;
    private boolean callResponse;  //bad code

    private final static double DISTANCE_AROUND_USER = 0.001;

    private Place placeNearest;
    private String url;
    private ArrayList<Parcelable> items;

    private InstagramActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private InstagramActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    private SettingsManager settingManager;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        InstagramActivity.super.onCreate(savedInstanceState);

        //main
        InstagramActivity.super.setContentView(R.layout.activity_dashboard_instagram);
        InstagramActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

        this.settingManager = SettingsManager.getSettingsManager(getApplicationContext());
    }

    @Override
    protected void onStart(){

        //forced to action
        InstagramActivity.super.onStart();

        //main
        InstagramActivity.this.setDefault();
        InstagramActivity.this.start();

        InstagramActivity.super.registerReceiver(InstagramActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        InstagramActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) InstagramActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            InstagramActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        InstagramActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        InstagramActivity.super.onStop();

        //main
        InstagramActivity.super.unregisterReceiver(InstagramActivity.this.gpsBroadcastReceiver);

        if (InstagramActivity.this.alertDialog != null){

            InstagramActivity.this.alertDialog.dismiss();

        }

        if (InstagramActivity.this.call != null){

            InstagramActivity.this.call.cancel();

        }

        InstagramActivity.this.openFragmentTask.cancel(true);

        InstagramActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        InstagramActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        InstagramActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        InstagramActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) InstagramActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return InstagramActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawerLayout = (DrawerLayout) InstagramActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_instagram:

                InstagramActivity.this.idFragment = InstagramActivity.INSTAGRAM_FRAGMENT;
                InstagramActivity.this.prepareToStart();
                InstagramActivity.this.start();

                break;

            case  R.id.nav_place_nearest:

                InstagramActivity.this.placeNearest = null;
                InstagramActivity.this.prepareToStart();
                InstagramActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        InstagramActivity.this.detector.onTouchEvent(me);

        return InstagramActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) InstagramActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    InstagramActivity.this.close();

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

        InstagramActivity.this.setDefaultValue();
        InstagramActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        InstagramActivity.this.idFragment = InstagramActivity.INSTAGRAM_FRAGMENT;

        //for start
        InstagramActivity.this.result = true;
        InstagramActivity.this.caseFail = InstagramActivity.EVERYTHING_OK;
        InstagramActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        InstagramActivity.this.call = null;
        InstagramActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                InstagramActivity.this.placeNearest = place;

            }
            else{

                InstagramActivity.this.placeNearest = null;

            }

        }
        else{

            InstagramActivity.this.placeNearest = null;

        }

        InstagramActivity.this.url = null;
        InstagramActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        InstagramActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        InstagramActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        InstagramActivity.this.gpsBroadcastReceiver = new InstagramActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        InstagramActivity.this.setDefaultStatusBar();
        InstagramActivity.this.setDefaultAppBarLayout();
        InstagramActivity.this.setDefaultDrawerLayout();
        InstagramActivity.this.setDefaultNavigationView();
        InstagramActivity.this.setDefaultToolbar();
        InstagramActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = InstagramActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) InstagramActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#C32A00"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) InstagramActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) InstagramActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(InstagramActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        InstagramActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) InstagramActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_instagram_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_instagram_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(InstagramActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) InstagramActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_instagram);
        toolbar.setTitle("Instagram");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#C32A00"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) InstagramActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        InstagramActivity.this.result = true;
        InstagramActivity.this.caseFail = InstagramActivity.EVERYTHING_OK;
        InstagramActivity.this.call = null;
        InstagramActivity.this.callResponse = false;
        //placenearest
        InstagramActivity.this.url = null;
        InstagramActivity.this.items = new ArrayList<Parcelable>();

        InstagramActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (InstagramActivity.this.caseFail == InstagramActivity.EVERYTHING_OK){

            InstagramActivity.this.openWaitingFragment();

            InstagramActivity.this.openFragmentTask = new InstagramActivity.OpenFragmentTask();

            InstagramActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        InstagramActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            InstagramActivity.this.checkUserLocation();
            InstagramActivity.this.searchPlaceNearest();

            switch (InstagramActivity.this.idFragment){

                case InstagramActivity.INSTAGRAM_FRAGMENT :

                    InstagramActivity.this.getURLInstagram();
                    InstagramActivity.this.getItemsInstagram();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            InstagramActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            InstagramActivity.this.result = false;
            InstagramActivity.this.caseFail = InstagramActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        InstagramActivity.this.countDownLatch = new CountDownLatch(1);
        InstagramActivity.this.call = InstagramActivity.this.api.getPlaces(userLocation.getLatitude()+ InstagramActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + InstagramActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - InstagramActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - InstagramActivity.DISTANCE_AROUND_USER,
                10);
        InstagramActivity.this.callResponse = false;

        //before
        if (InstagramActivity.this.result == false){

            return;

        }

        if (InstagramActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            InstagramActivity.this.result = false;
            InstagramActivity.this.caseFail = InstagramActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        InstagramActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    InstagramActivity.this.callResponse = true;

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

                    InstagramActivity.this.callResponse = true;
                    InstagramActivity.this.result = false;
                    InstagramActivity.this.caseFail = InstagramActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    InstagramActivity.this.placeNearest = this.placeMin;
                    InstagramActivity.this.result = false;
                    InstagramActivity.this.caseFail = InstagramActivity.NO_DATA;
                    InstagramActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        InstagramActivity.this.placeNearest = this.placeMin;

                        InstagramActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(InstagramActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                InstagramActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                InstagramActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                InstagramActivity.this.alertDialog = null;

                            }

                        });

                        InstagramActivity.this.alertDialog = builder.create();
                        InstagramActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        InstagramActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    InstagramActivity.this.alertDialog.dismiss();
                                    InstagramActivity.this.alertDialog = null;
                                    InstagramActivity.this.close();

                                }
                                return true;
                            }
                        });

                        InstagramActivity.this.alertDialog.show();

                        InstagramActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (InstagramActivity.this.callResponse == false && InstagramActivity.this.call != null){

                    InstagramActivity.this.call.cancel();
                    InstagramActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            InstagramActivity.this.countDownLatch.await();
            InstagramActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (InstagramActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                InstagramActivity.this.result = false;
                InstagramActivity.this.caseFail = InstagramActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                InstagramActivity.this.result = false;
                InstagramActivity.this.caseFail = InstagramActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        InstagramActivity.this.call = null;
        InstagramActivity.this.callResponse = false;

    }

    //url

    //items

    // instagram process------------------------------------------------

    private void getURLInstagram(){

        //before
        if (InstagramActivity.this.result == false){

            return;

        }

        //main
        InstagramActivity.this.url = this.settingManager.getOnespaceServerURL() + "/data/?tabid=0&type=instagram&vloc=" + InstagramActivity.this.placeNearest.getVloc() + "&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10";

    }

    private void getItemsInstagram(){

        //before
        if (InstagramActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            InstagramActivity.this.result = false;
            InstagramActivity.this.caseFail = InstagramActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(InstagramActivity.this.url);

        if (jsonObject.length() == 0){

            InstagramActivity.this.result = false;
            InstagramActivity.this.caseFail = InstagramActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        if (jsonArray.length() == 0){

            InstagramActivity.this.result = false;
            InstagramActivity.this.caseFail = InstagramActivity.NO_DATA;

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
                boolean imagesExists = false;
                try {
                    bitmap = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
                    imagesExists = true;
                } catch (IOException e) {
                    //e.printStackTrace();
                    Log.e("Original Instagram images not available");
                }

                if (!imagesExists) {
                    URL altUrl = null;
                    try {
                        altUrl = new URL(DEFAULT_URL__IMAGE_NOT_FOUND);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    try {
                        bitmap = BitmapFactory.decodeStream(altUrl.openConnection() .getInputStream());
                    } catch (IOException e) {
                        //e.printStackTrace();
                        Log.e("Default images not available");
                    }
                }


                InstagramActivity.this.items.add(new InstagramFragment.InstagramItem(String.valueOf(object.get("title")), bitmap));


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

        InstagramFragment instagramFragment = new InstagramFragment();

        //main
        if (InstagramActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) InstagramActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) InstagramActivity.super.findViewById(R.id.text_distance);

            if (InstagramActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(InstagramActivity.this.placeNearest.getLat());
                location.setLongitude(InstagramActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(InstagramActivity.this.placeNearest.getName());
                double distance = MainMenuFragment.roundToDecimal((UserLocationManager.getLocation().distanceTo(location)) / 1000, 2);
                textView.setText("DISTANCE " + distance + " km");

            }

            if (InstagramActivity.this.caseFail == InstagramActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        InstagramActivity.this.start();

                    }

                };

                InstagramActivity.this.prepareToStart();
                thread.start();

            }

            if (InstagramActivity.this.caseFail == InstagramActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        InstagramActivity.this.prepareToStart();
                        InstagramActivity.this.start();

                    }

                };

                thread.start();

            }

            if (InstagramActivity.this.caseFail == InstagramActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        InstagramActivity.this.start();

                    }

                };

                InstagramActivity.this.prepareToStart();
                thread.start();

            }

            if (InstagramActivity.this.caseFail == InstagramActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            InstagramActivity.this.caseFail = InstagramActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) InstagramActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) InstagramActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(InstagramActivity.this.placeNearest.getLat());
            location.setLongitude(InstagramActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(InstagramActivity.this.placeNearest.getName());
            double distance = MainMenuFragment.roundToDecimal((UserLocationManager.getLocation().distanceTo(location)) / 1000, 2);
            textView.setText("DISTANCE " + distance + " km");

            switch (InstagramActivity.this.idFragment){

                case InstagramActivity.INSTAGRAM_FRAGMENT:

                    Bundle bundle = new Bundle();
                    bundle.putString("url", InstagramActivity.this.url);
                    bundle.putParcelableArrayList("items", InstagramActivity.this.items);
                    instagramFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, instagramFragment, instagramFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            InstagramActivity.this.caseFail = InstagramActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        InstagramActivity.super.finish();

        InstagramActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) InstagramActivity.super.findViewById(R.id.text_distance);

            if (InstagramActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(InstagramActivity.this.placeNearest.getLat());
                location.setLongitude(InstagramActivity.this.placeNearest.getLng());

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
