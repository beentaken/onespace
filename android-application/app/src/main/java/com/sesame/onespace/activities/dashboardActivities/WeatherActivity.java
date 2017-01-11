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
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.DoNotHaveLocationFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.WaitingFragment;
import com.sesame.onespace.fragments.dashboardFragments.weatherFragment.WeatherForecastFragment;
import com.sesame.onespace.fragments.dashboardFragments.weatherFragment.WeatherFragment;
import com.sesame.onespace.interfaces.activityInterfaces.SimpleGestureFilter;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.network.OneSpaceApi;
import com.sesame.onespace.utils.connect.Connection;
import com.sesame.onespace.utils.date.DateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;

/**
 * Created by Thian on 4/1/2560.
 */

public final class WeatherActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener,
                   NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //for menu -------------------------------------------------------------------------------------
    private String idFragment;

    private final static String WEATHER_FRAGMENT = "weather fragment";
    private final static String WEATHER_FORECAST_FRAGMENT = "weather forecast fragment";

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

    private WeatherActivity.OpenFragmentTask openFragmentTask;
    private CountDownLatch countDownLatch; //bad code

    //for dialog -----------------------------------------------------------------------------------

    private AlertDialog alertDialog;

    //for SimpleGestureFilter.SimpleGestureListener-------------------------------------------------
    private SimpleGestureFilter detector;

    //for GPSBroadcastReceiver
    private WeatherActivity.GPSBroadcastReceiver gpsBroadcastReceiver;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //forced to action
        WeatherActivity.super.onCreate(savedInstanceState);

        //main
        WeatherActivity.super.setContentView(R.layout.activity_dashboard_carpark);
        WeatherActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //forced to action
        WeatherActivity.super.onStart();

        //main
        WeatherActivity.this.setDefault();
        WeatherActivity.this.start();

        WeatherActivity.super.registerReceiver(WeatherActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

    }

    @Override
    protected void onResume() {

        //forced to action
        WeatherActivity.super.onResume();

    }

    @Override
    public void onBackPressed() {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) WeatherActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true){

            drawerLayout.closeDrawer(GravityCompat.END);

        }
        else{

            WeatherActivity.this.close();

        }

    }

    @Override
    public void onPause(){

        //forced to action
        WeatherActivity.super.onPause();

    }

    @Override
    protected void onStop(){

        //forced to action
        WeatherActivity.super.onStop();

        //main
        WeatherActivity.super.unregisterReceiver(WeatherActivity.this.gpsBroadcastReceiver);

        if (WeatherActivity.this.alertDialog != null){

            WeatherActivity.this.alertDialog.dismiss();

        }

        if (WeatherActivity.this.call != null){

            WeatherActivity.this.call.cancel();

        }

        WeatherActivity.this.openFragmentTask.cancel(true);

        WeatherActivity.this.setDefault();

    }

    @Override
    protected void onRestart(){

        //forced to action
        WeatherActivity.super.onRestart();

    }

    @Override
    protected void onDestroy(){

        //forced to action
        WeatherActivity.super.onDestroy();

    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {

        //forced to action
        WeatherActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) WeatherActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        return WeatherActivity.super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //forced to action
        DrawerLayout drawerLayout = (DrawerLayout) WeatherActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        switch (id) {

            case R.id.nav_weather:

                WeatherActivity.this.idFragment = WeatherActivity.WEATHER_FRAGMENT;
                WeatherActivity.this.prepareToStart();
                WeatherActivity.this.start();

                break;

            case R.id.nav_weather_forecast:

                WeatherActivity.this.idFragment = WeatherActivity.WEATHER_FORECAST_FRAGMENT;
                WeatherActivity.this.prepareToStart();
                WeatherActivity.this.start();

                break;


            case  R.id.nav_place_nearest:

                WeatherActivity.this.placeNearest = null;
                WeatherActivity.this.prepareToStart();
                WeatherActivity.this.start();

                break;

            case  R.id.nav_back:

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.END);

        return false;
    }

    @Override
    public final boolean dispatchTouchEvent(MotionEvent me){

        WeatherActivity.this.detector.onTouchEvent(me);

        return WeatherActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        DrawerLayout drawerLayout = (DrawerLayout) WeatherActivity.super.findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.END) == false){

            switch (direction) {

                case SimpleGestureFilter.SWIPE_RIGHT :

                    WeatherActivity.this.close();

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

        WeatherActivity.this.setDefaultValue();
        WeatherActivity.this.setDefaultView();

    }

    private void setDefaultValue(){

        //for menu
        WeatherActivity.this.idFragment = WeatherActivity.WEATHER_FRAGMENT;

        //for start
        WeatherActivity.this.result = true;
        WeatherActivity.this.caseFail = WeatherActivity.EVERYTHING_OK;
        WeatherActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();
        WeatherActivity.this.call = null;
        WeatherActivity.this.callResponse = false;

        Intent intent = getIntent();
        if (intent != null){

            Bundle bundle = intent.getBundleExtra("bundle");

            if (bundle.getString("Name") != null){

                Place place = new Place();
                place.setName(bundle.getString("Name"));
                place.setVloc(bundle.getString("Vloc"));
                place.setLat(bundle.getDouble("Lat"));
                place.setLng(bundle.getDouble("Lng"));

                WeatherActivity.this.placeNearest = place;

            }
            else{

                WeatherActivity.this.placeNearest = null;

            }

        }
        else{

            WeatherActivity.this.placeNearest = null;

        }

        WeatherActivity.this.url = null;
        WeatherActivity.this.items = new ArrayList<Parcelable>();

        //for dialog
        WeatherActivity.this.alertDialog = null;

        //for SimpleGestureFilter.SimpleGestureListener
        WeatherActivity.this.detector = new SimpleGestureFilter(this, this);

        //for gpsBroadcastReceiver
        WeatherActivity.this.gpsBroadcastReceiver = new WeatherActivity.GPSBroadcastReceiver();

    }

    private void setDefaultView(){

        WeatherActivity.this.setDefaultStatusBar();
        WeatherActivity.this.setDefaultAppBarLayout();
        WeatherActivity.this.setDefaultDrawerLayout();
        WeatherActivity.this.setDefaultNavigationView();
        WeatherActivity.this.setDefaultToolbar();
        WeatherActivity.this.setDefaultDistance();

    }

    // sub method ----------------------------------------------------------------------------------sub method

    //setDefaultView()--------------------------------------------------

    private void setDefaultStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = WeatherActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

    }

    private void setDefaultAppBarLayout(){

        AppBarLayout appBarLayout = (AppBarLayout) WeatherActivity.super.findViewById(R.id.app_bar_layout);
        appBarLayout.setBackgroundColor(Color.parseColor("#629B41"));
        appBarLayout.setExpanded(true, true);

    }

    private void setDefaultDrawerLayout(){

        Toolbar toolbar = (Toolbar) WeatherActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) WeatherActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(WeatherActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

        };

        WeatherActivity.super.setSupportActionBar(toolbar);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void setDefaultNavigationView(){

        NavigationView navigationView = (NavigationView) WeatherActivity.super.findViewById(R.id.nav_view);

        navigationView.getMenu().clear();

        Intent intent = getIntent();

        if (intent != null){

            String enterFrom = intent.getStringExtra("enter from");

            if (enterFrom.equals("main screen") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_weather_main);

            }

            if (enterFrom.equals("map") == true){

                navigationView.inflateMenu(R.menu.menu_dashboard_navright_weather_map);

            }

        }

        navigationView.setNavigationItemSelectedListener(WeatherActivity.this);
        navigationView.setItemIconTintList(null);

    }

    private void setDefaultToolbar(){

        Toolbar toolbar = (Toolbar) WeatherActivity.super.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        toolbar.setLogo(R.drawable.ic_dashboard_weather);
        toolbar.setTitle("Weather");
        toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
        toolbar.setBackgroundColor(Color.parseColor("#629B41"));

    }

    private void setDefaultDistance(){

        TextView textView = (TextView) WeatherActivity.super.findViewById(R.id.text_distance);
        textView.setText("NO DISTANCE TO SHOW");

    }

    //===========================================================================================================//
    //  PREPARE TO START                                                                            PREPARE TO START
    //===========================================================================================================//

    private void prepareToStart(){

        //id fragment
        WeatherActivity.this.result = true;
        WeatherActivity.this.caseFail = WeatherActivity.EVERYTHING_OK;
        WeatherActivity.this.call = null;
        WeatherActivity.this.callResponse = false;
        //placenearest
        WeatherActivity.this.url = null;
        WeatherActivity.this.items = new ArrayList<Parcelable>();

        WeatherActivity.this.alertDialog = null;

    }

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//

    private void start(){

        if (WeatherActivity.this.caseFail == WeatherActivity.EVERYTHING_OK){

            WeatherActivity.this.openWaitingFragment();

            WeatherActivity.this.openFragmentTask = new WeatherActivity.OpenFragmentTask();

            WeatherActivity.this.openFragmentTask.execute();

        }

    }

    // sub method ----------------------------------------------------------------------------------sub method
    // waiting process------------------------------------------------------------------------------

    private void openWaitingFragment(){

        //init
        WaitingFragment waitingFragment = new WaitingFragment();

        //main
        WeatherActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, waitingFragment, waitingFragment.getClass().getSimpleName()).addToBackStack(null).commit();

    }

    // prepare process------------------------------------------------------------------------------

    private final class OpenFragmentTask
            extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            //prepare
            WeatherActivity.this.checkUserLocation();
            WeatherActivity.this.searchPlaceNearest();

            switch (WeatherActivity.this.idFragment){

                case WeatherActivity.WEATHER_FRAGMENT :

                    WeatherActivity.this.getURLWeather();
                    WeatherActivity.this.getItemsWeather();

                    break;

                case WeatherActivity.WEATHER_FORECAST_FRAGMENT :

                    WeatherActivity.this.getURLWeatherForecast();
                    WeatherActivity.this.getItemsWeatherForecast();

                    break;

            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {



        }

        protected void onPostExecute(Void result) {

            //main
            WeatherActivity.this.openFragment();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private void checkUserLocation(){

        if (UserLocationManager.isReady() == false){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.DO_NOT_HAVE_LOCATION;

        }

    }

    private void searchPlaceNearest(){

        //init
        final Location userLocation = UserLocationManager.getLocation();
        WeatherActivity.this.countDownLatch = new CountDownLatch(1);
        WeatherActivity.this.call = WeatherActivity.this.api.getPlaces(userLocation.getLatitude()+ WeatherActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() + WeatherActivity.DISTANCE_AROUND_USER,
                userLocation.getLatitude() - WeatherActivity.DISTANCE_AROUND_USER,
                userLocation.getLongitude() - WeatherActivity.DISTANCE_AROUND_USER,
                10);
        WeatherActivity.this.callResponse = false;

        //before
        if (WeatherActivity.this.result == false){

            return;

        }

        if (WeatherActivity.this.placeNearest != null){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        WeatherActivity.this.call.enqueue(new Callback<ArrayList<Place>>() {

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

                    WeatherActivity.this.callResponse = true;

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

                    WeatherActivity.this.callResponse = true;
                    WeatherActivity.this.result = false;
                    WeatherActivity.this.caseFail = WeatherActivity.CAN_NOT_CONNECT_TO_SERVER;

                }

                //after
                if (this.placesNearlyList.size() == 0){

                    WeatherActivity.this.placeNearest = this.placeMin;
                    WeatherActivity.this.result = false;
                    WeatherActivity.this.caseFail = WeatherActivity.NO_DATA;
                    WeatherActivity.this.countDownLatch.countDown();

                }
                else{

                    if (this.placesNearlyList.size() == 1){

                        WeatherActivity.this.placeNearest = this.placeMin;

                        WeatherActivity.this.countDownLatch.countDown();

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

                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(WeatherActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("The places near you.(Please select)");
                        builder.setSingleChoiceItems(placesTextArray, -1, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                selectItem = item;

                            }

                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                WeatherActivity.this.placeNearest = placesNearlyList.get(selectItem);

                                WeatherActivity.this.countDownLatch.countDown();

                                dialog.dismiss();
                                WeatherActivity.this.alertDialog = null;

                            }

                        });

                        WeatherActivity.this.alertDialog = builder.create();
                        WeatherActivity.this.alertDialog.setCanceledOnTouchOutside(false);
                        WeatherActivity.this.alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {

                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {

                                    WeatherActivity.this.alertDialog.dismiss();
                                    WeatherActivity.this.alertDialog = null;
                                    WeatherActivity.this.close();

                                }
                                return true;
                            }
                        });

                        WeatherActivity.this.alertDialog.show();

                        WeatherActivity.this.alertDialog.getListView().setItemChecked(selectItem, true);

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

                if (WeatherActivity.this.callResponse == false && WeatherActivity.this.call != null){

                    WeatherActivity.this.call.cancel();
                    WeatherActivity.this.countDownLatch.countDown();

                }

            }

        };

        thread.start();

        try {
            WeatherActivity.this.countDownLatch.await();
            WeatherActivity.this.countDownLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (WeatherActivity.this.callResponse == false){

            if (Connection.isInternetAvailable() == false){

                WeatherActivity.this.result = false;
                WeatherActivity.this.caseFail = WeatherActivity.INTERNET_NOT_AVAILABLE;

                return;

            }
            else{

                WeatherActivity.this.result = false;
                WeatherActivity.this.caseFail = WeatherActivity.CAN_NOT_CONNECT_TO_SERVER;

                return;

            }

        }

        //after
        WeatherActivity.this.call = null;
        WeatherActivity.this.callResponse = false;

    }

    //url

    //items

    // weather process------------------------------------------------

    private void getURLWeather(){

        //before
        if (WeatherActivity.this.result == false){

            return;

        }

        //main
        WeatherActivity.this.url = "http://api.openweathermap.org/data/2.5/weather?lat=" + WeatherActivity.this.placeNearest.getLat() + "&lon=" + WeatherActivity.this.placeNearest.getLng() + "&APPID=878736638c5a1aa40f155942c7460a31";

    }

    private void getItemsWeather(){

        //before
        if (WeatherActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(WeatherActivity.this.url);

        if (jsonObject.length() == 0){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        if (jsonObject == null){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.NO_DATA;

            return;

        }

        String url = getIconUrl(jsonObject);
        String headerText = WeatherActivity.this.placeNearest.getName();
        String locationArea = getLocationArea(jsonObject);
        String description = getDescription(jsonObject);
        String time = getTime();
        String headerTemp = "Temperature";
        double tempC = round(convertKelvinToCelsius(getTemp(jsonObject)),2);
        double maxTempC = round(convertKelvinToCelsius(getMaxTemp(jsonObject)),2);
        double minTempC = round(convertKelvinToCelsius(getMinTemp(jsonObject)),2);
        String headerDetail = "Detail";
        int pressure = getPressure(jsonObject);
        String humidity = getHumidity(jsonObject);
        String windSpeed = getWindSpeed(jsonObject);
        String windDirection = getWindDirection(jsonObject);
        String cloudiness = getCloudiness(jsonObject);
        String sunrise = DateConvert.convertTimeStampToDate(getSunrise(jsonObject), "HH:mm");
        String sunset = DateConvert.convertTimeStampToDate(getSunset(jsonObject), "HH:mm");

        URL newurl = null;
        try {
            newurl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        WeatherActivity.this.items.add(new WeatherFragment.WeatherItem(bitmap, headerText, locationArea,description, time, headerTemp, String.valueOf(tempC), String.valueOf(maxTempC), String.valueOf(minTempC), headerDetail, String.valueOf(pressure), humidity, windSpeed, windDirection, cloudiness, sunrise, sunset));

    }

    private String getIconUrl(JSONObject jsonObject){

        String url = "";

        try {

            Object object = jsonObject.get("weather");
            url = "http://openweathermap.org/img/w/" + ((JSONObject)(((JSONArray)object).get(0))).get("icon") + ".png";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return url;

    }

    private String getLocationArea(JSONObject jsonObject){

        String locationArea = "";

        try {

            locationArea = (String) (jsonObject.get("name"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locationArea;

    }

    private String getDescription(JSONObject jsonObject){

        String description = "";

        try {

            Object object = jsonObject.get("weather");
            description = (String) ((JSONObject)(((JSONArray)object).get(0))).get("description");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return description;

    }

    private String getTime(){

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = df.format(Calendar.getInstance().getTime());

        return date;

    }

    private double getTemp(JSONObject jsonObject){

        double temp = 0;

        try {

            Object object = jsonObject.get("main");
            temp = (Double) (((JSONObject) object).get("temp"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return temp;

    }

    private double getMaxTemp(JSONObject jsonObject){

        double maxTemp = 0;

        try {

            Object object = jsonObject.get("main");
            maxTemp = (Double) (((JSONObject) object).get("temp_max"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return maxTemp;

    }

    private double getMinTemp(JSONObject jsonObject){

        double minTemp = 0;

        try {

            Object object = jsonObject.get("main");
            minTemp = (Double) (((JSONObject) object).get("temp_min"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return minTemp;

    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private int getPressure(JSONObject jsonObject){

        int pressure = 0;

        try {

            Object object = jsonObject.get("main");

            if ((((JSONObject) object).get("pressure")) instanceof Double){

                pressure = ((Double)((JSONObject) object).get("pressure")).intValue();

            }
            else{

                pressure = (int) ((JSONObject) object).get("pressure");

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  pressure;

    }

    private String getHumidity(JSONObject jsonObject){

        String humidity = "";

        try {

            Object object = jsonObject.get("main");
            humidity = ((JSONObject) object).get("humidity") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  humidity;

    }

    private String getWindSpeed(JSONObject jsonObject){

        String windSpeed = "";

        try {

            Object object = jsonObject.get("wind");
            windSpeed = ((JSONObject) object).get("speed") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  windSpeed;

    }

    private String getWindDirection(JSONObject jsonObject){

        String windDirection = "";

        try {

            Object object = jsonObject.get("wind");
            windDirection = ((JSONObject) object).get("deg") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  windDirection;

    }

    private String getCloudiness(JSONObject jsonObject){

        String cloudiness = "";

        try {

            Object object = jsonObject.get("clouds");
            cloudiness = ((JSONObject) object).get("all") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  cloudiness;

    }

    private String getSunrise(JSONObject jsonObject){

        String sunrise = "";

        try {

            Object object = jsonObject.get("sys");
            sunrise = ((JSONObject) object).get("sunrise") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  sunrise;

    }

    private String getSunset(JSONObject jsonObject){

        String sunset = "";

        try {

            Object object = jsonObject.get("sys");
            sunset = ((JSONObject) object).get("sunset") + "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  sunset;

    }

    private double convertKelvinToCelsius(double kelvin){

        double celsius = kelvin - 273.15;

        return celsius;

    }

    // weather forecast process------------------------------------------------

    private void getURLWeatherForecast(){

        //before
        if (WeatherActivity.this.result == false){

            return;

        }

        //main
        WeatherActivity.this.url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + WeatherActivity.this.placeNearest.getLat() + "&lon=" + WeatherActivity.this.placeNearest.getLng() + "&APPID=878736638c5a1aa40f155942c7460a31";

    }

    private void getItemsWeatherForecast(){

        //before
        if (WeatherActivity.this.result == false){

            return;

        }

        if (Connection.isInternetAvailable() == false){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.INTERNET_NOT_AVAILABLE;

            return;

        }

        //main
        JSONObject jsonObject = Connection.getJSON(WeatherActivity.this.url);

        if (jsonObject.length() == 0){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.CAN_NOT_CONNECT_TO_SERVER;

            return;

        }

        if (jsonObject == null){

            WeatherActivity.this.result = false;
            WeatherActivity.this.caseFail = WeatherActivity.NO_DATA;

            return;

        }

        ArrayList<String> dateList = getDateList(jsonObject);

        String tempHeaderText = "Temperature Call 5 day / 3 hour";
        ArrayList<Double> tempList = getTemperatureForecast(jsonObject);
        String tempType = "°C";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(tempHeaderText, dateList, tempList, tempType));

        String pressureHeaderText = "Pressure Call 5 day / 3 hour";
        ArrayList<Double> pressureList = getPressureForecast(jsonObject);
        String pressureType = "hpa";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(pressureHeaderText, dateList, pressureList, pressureType));

        String humidityHeaderText = "Humidity Call 5 day / 3 hour";
        ArrayList<Double> humidityList = getHumidityForecast(jsonObject);
        String humidityType = "%";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(humidityHeaderText, dateList, humidityList, humidityType));

        String windSpeedHeaderText = "Wind Speed Call 5 day / 3 hour";
        ArrayList<Double> windSpeedList = getWindSpeedForecast(jsonObject);
        String windSpeedType = "m/s";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(windSpeedHeaderText, dateList, windSpeedList, windSpeedType));

        String windDirectionHeaderText = "Wind Direction Call 5 day / 3 hour";
        ArrayList<Double> windDirectionList = getWindDirectionForecast(jsonObject);
        String windDirectionType = "deg";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(windDirectionHeaderText, dateList, windDirectionList, windDirectionType));

        String cloudinessHeaderText = "Cloudiness Call 5 day / 3 hour";
        ArrayList<Double> cloudinessList = getCloudinessForecast(jsonObject);
        String cloudinessType = "%";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(cloudinessHeaderText, dateList, cloudinessList, cloudinessType));

        String rainHeaderText = "Rain Volume Call 5 day / 3 hour";
        ArrayList<Double> rainList = getRainVolumeForecast(jsonObject);
        String rainType = "mm";

        WeatherActivity.this.items.add(new WeatherForecastFragment.WeatherForecastItem(rainHeaderText, dateList, rainList, rainType));

    }

    private ArrayList<String> getDateList(JSONObject jsonObject){

        ArrayList<String> dateList = new ArrayList<String>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                dateList.add(DateConvert.convertTimeStampToDate(((Integer) ((JSONObject)object).get("dt")) + "", "dd"));

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dateList;

    }

    private ArrayList<Double> getTemperatureForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);

                if (((JSONObject)(((JSONObject)object).get("main"))).get("temp") instanceof Double){

                    dataList.add((Double) ((JSONObject)(((JSONObject)object).get("main"))).get("temp"));

                }
                else{

                    dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("main"))).get("temp"))/1.00);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getPressureForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);

                if (((JSONObject)(((JSONObject)object).get("main"))).get("pressure") instanceof Double){

                    dataList.add((Double) ((JSONObject)(((JSONObject)object).get("main"))).get("pressure"));

                }
                else{

                    dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("main"))).get("pressure"))/1.00);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getHumidityForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("main"))).get("humidity"))/1.00);

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getWindSpeedForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                if (((JSONObject)(((JSONObject)object).get("wind"))).get("speed") instanceof Double){

                    dataList.add((Double) ((JSONObject)(((JSONObject)object).get("wind"))).get("speed"));

                }
                else{

                    dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("wind"))).get("speed"))/1.00);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getWindDirectionForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                if (((JSONObject)(((JSONObject)object).get("wind"))).get("deg") instanceof Double){

                    dataList.add((Double) ((JSONObject)(((JSONObject)object).get("wind"))).get("deg"));

                }
                else{

                    dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("wind"))).get("deg"))/1.00);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getCloudinessForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                if (((JSONObject)(((JSONObject)object).get("clouds"))).get("all") instanceof Double){

                    dataList.add((Double) ((JSONObject)(((JSONObject)object).get("clouds"))).get("all"));

                }
                else{

                    dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("clouds"))).get("all"))/1.00);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    private ArrayList<Double> getRainVolumeForecast(JSONObject jsonObject){

        ArrayList<Double> dataList = new ArrayList<Double>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);

                try{

                    if (((JSONObject)(((JSONObject)object).get("rain"))).get("3h") instanceof Double){

                        dataList.add((Double) ((JSONObject)(((JSONObject)object).get("rain"))).get("3h"));

                    }
                    else{

                        dataList.add(((Integer) ((JSONObject)(((JSONObject)object).get("rain"))).get("3h"))/1.00);

                    }

                }catch (Exception e){

                    dataList.add((double) 0);

                }

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;

    }

    // open fragment process------------------------------------------------------------------------

    private void openFragment(){

        //init
        DoNotHaveLocationFragment doNotHaveLocationFragment = new DoNotHaveLocationFragment();
        InternetNotAvailableFragment internetNotAvailableFragment = new InternetNotAvailableFragment();
        CanNotConnectedToServerFragment notConnectingToServerFragment = new CanNotConnectedToServerFragment();
        NoDataFragment noDataFragment = new NoDataFragment();

        WeatherFragment weatherFragment = new WeatherFragment();
        WeatherForecastFragment weatherForecastFragment = new WeatherForecastFragment();

        //main
        if (WeatherActivity.this.result == false) {

            Toolbar toolbar = (Toolbar) WeatherActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) WeatherActivity.super.findViewById(R.id.text_distance);

            if (WeatherActivity.this.placeNearest == null){

                toolbar.setSubtitle(R.string.default_subTitle_activity_dash_board);
                textView.setText("NO DISTANCE TO SHOW");

            }
            else{

                Location location = new Location("placeNearly");
                location.setLatitude(WeatherActivity.this.placeNearest.getLat());
                location.setLongitude(WeatherActivity.this.placeNearest.getLng());

                toolbar.setSubtitle(WeatherActivity.this.placeNearest.getName());
                textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            }

            if (WeatherActivity.this.caseFail == WeatherActivity.DO_NOT_HAVE_LOCATION){

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, doNotHaveLocationFragment, doNotHaveLocationFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        WeatherActivity.this.start();

                    }

                };

                WeatherActivity.this.prepareToStart();
                thread.start();

            }

            if (WeatherActivity.this.caseFail == WeatherActivity.INTERNET_NOT_AVAILABLE) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        WeatherActivity.this.prepareToStart();
                        WeatherActivity.this.start();

                    }

                };

                thread.start();

            }

            if (WeatherActivity.this.caseFail == WeatherActivity.CAN_NOT_CONNECT_TO_SERVER) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        WeatherActivity.this.start();

                    }

                };

                WeatherActivity.this.prepareToStart();
                thread.start();

            }

            if (WeatherActivity.this.caseFail == WeatherActivity.NO_DATA) {

                getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

            }

            WeatherActivity.this.caseFail = WeatherActivity.END;

        }
        else{

            Toolbar toolbar = (Toolbar) WeatherActivity.super.findViewById(R.id.toolbar);
            TextView textView = (TextView) WeatherActivity.super.findViewById(R.id.text_distance);

            Location location = new Location("placeNearly");
            location.setLatitude(WeatherActivity.this.placeNearest.getLat());
            location.setLongitude(WeatherActivity.this.placeNearest.getLng());

            toolbar.setSubtitle(WeatherActivity.this.placeNearest.getName());
            textView.setText("DISTANCE " + UserLocationManager.getLocation().distanceTo(location) + " M");

            Bundle bundle;

            switch (WeatherActivity.this.idFragment){

                case WeatherActivity.WEATHER_FRAGMENT:

                    bundle = new Bundle();
                    bundle.putString("url", WeatherActivity.this.url);
                    bundle.putParcelableArrayList("items", WeatherActivity.this.items);
                    weatherFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, weatherFragment, weatherFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

                case WeatherActivity.WEATHER_FORECAST_FRAGMENT:

                    bundle = new Bundle();
                    bundle.putString("url", WeatherActivity.this.url);
                    bundle.putParcelableArrayList("items", WeatherActivity.this.items);
                    weatherForecastFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, weatherForecastFragment, weatherForecastFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    break;

            }

            WeatherActivity.this.caseFail = WeatherActivity.END;

        }

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//

    private void close(){

        WeatherActivity.super.finish();

        WeatherActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

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
            TextView textView = (TextView) WeatherActivity.super.findViewById(R.id.text_distance);

            if (WeatherActivity.this.placeNearest != null){

                Location location = new Location("placeNearly");
                location.setLatitude(WeatherActivity.this.placeNearest.getLat());
                location.setLongitude(WeatherActivity.this.placeNearest.getLng());

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
