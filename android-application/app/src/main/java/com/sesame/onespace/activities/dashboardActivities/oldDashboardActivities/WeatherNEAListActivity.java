package com.sesame.onespace.activities.dashboardActivities.oldDashboardActivities;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.utils.connect.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Thian on 8/11/2559.
 */

public class WeatherNEAListActivity extends DashboardActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private JSONParser jsonParser;

    private List<WeatherNEAListActivity.WeatherNEAItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private WeatherNEAListAdapter adapter;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.init();

        this.addRecyclerView();

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        this.initStatusBar();
        this.initActivity();
        this.initToolbar();
        this.initRecyclerView();

    }

    private void initStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

    }

    private void initToolbar(){

        AppBarLayout item = (AppBarLayout) findViewById(R.id.layout);
        item.setBackgroundColor(Color.parseColor("#9CC703"));

        this.mToolBar = (Toolbar) findViewById(R.id.toolbar);
        this.mToolBar.setTitle("Weather(NEA)");
        this.mToolBar.setSubtitle("test sub title");
        this.mToolBar.setLogo(R.drawable.ic_dashboard_weather);
        this.mToolBar.setBackgroundColor(Color.parseColor("#9CC703"));
        setSupportActionBar(this.mToolBar);

        this.mToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        this.mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);
            }
        });

    }

    private void initActivity(){

        setContentView(R.layout.activity_weathernea_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initRecyclerView(){

        this.initConnection();
        this.initItem();

        //add space between items.
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        WeatherNEAListActivity.SpacesItemDecoration spacesItemDecoration = new WeatherNEAListActivity.SpacesItemDecoration(spacingInPixels);
        //

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);
        this.adapter = new WeatherNEAListActivity.WeatherNEAListAdapter(this.context, this.items);
        this.recyclerView.setAdapter(this.adapter);

    }

    private void initConnection(){

        this.jsonParser = new JSONParser();

    }

    private void initItem(){

        this.items = new ArrayList<WeatherNEAListActivity.WeatherNEAItem>();

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;

        }

    }

    //===========================================================================================================//
    //  METHOD START RECYCLER VIEW                                                                  METHOD START RECYCLER VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void addRecyclerView(){

        new DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = connectToServer();

        String url = getIconUrl(jsonObject);
        String headerText = "User Location";
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
        String sunrise = getDate(getSunrise(jsonObject));
        String sunset = getDate(getSunset(jsonObject));

        items.add(new WeatherNEAListActivity.WeatherNEAItem(url, headerText, locationArea,description, time, headerTemp, String.valueOf(tempC), String.valueOf(maxTempC), String.valueOf(minTempC), headerDetail, String.valueOf(pressure), humidity, windSpeed, windDirection, cloudiness, sunrise, sunset));

    }

    private JSONObject connectToServer(){

        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + UserLocationManager.getLatitude() + "&lon=" + UserLocationManager.getLongitude() + "&APPID=878736638c5a1aa40f155942c7460a31";

        JSONObject jsonObject = this.jsonParser.makeHttpRequest(url, "GET");

        return jsonObject;

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

    private String getDate(String timeStampStr){

        try{

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date netDate = (new Date(Long.parseLong(String.valueOf(Long.parseLong(timeStampStr) * 1000))));

            return sdf.format(netDate);
        }
        catch(Exception ex){



        }

        return "";
    }

    //  private class   ----------------------------------------------------------------------------****private class****

    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {

        protected Long doInBackground(URL ... url) {

            loadData();

            return null;
        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Long result) {

            adapter.notifyDataSetChanged();

        }

    }

    public class WeatherNEAListAdapter extends RecyclerView.Adapter<WeatherNEAListActivity.ViewHolder> {

        private List<WeatherNEAListActivity.WeatherNEAItem> items;
        private Context mContext;

        public WeatherNEAListAdapter(Context context, List<WeatherNEAListActivity.WeatherNEAItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public WeatherNEAListActivity.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_weathernea_item, null);
            return new WeatherNEAListActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeatherNEAListActivity.ViewHolder viewHolder, int i) {

            final WeatherNEAListActivity.WeatherNEAItem item = items.get(i);

            String color1 = "#FFFFFF";
            String color2 = "#FFFFFF";

            int colors[] = { Color.parseColor(color1), Color.parseColor(color2)};
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            URL newurl = null;
            try {
                newurl = new URL(item.getUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                viewHolder.icon.setImageBitmap(BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            viewHolder.headerText.setText(item.getHeaderText());
            viewHolder.locationArea.setText("Location Area : " + item.getLocationArea());
            viewHolder.description.setText("Description : " + item.getDescription());
            viewHolder.time.setText("get at " + item.getTime());
            viewHolder.headerTemp.setText(item.getHeaderTemp());
            viewHolder.temp.setText("Temperature : " + item.getTemp() + " °C (Celsius)");
            viewHolder.maxTemp.setText("Max Temperature : " + item.getMaxTemp() + " °C (Celsius)");
            viewHolder.minTemp.setText("Min Temperature : " + item.getMinTemp() + " °C (Celsius)");
            viewHolder.headerDetail.setText(item.getHeaderDetail());
            viewHolder.pressure.setText("Pressure : " + item.getPressure() + " hpa");
            viewHolder.humidity.setText("Humidity : " + item.getHumidity() + " %");
            viewHolder.windSpeed.setText("Wind Speed : " + item.getWindSpeed() + " m/s");
            viewHolder.windDirection.setText("Wind Direction : " + item.getWindDirection() + " degrees");
            viewHolder.cloudiness.setText("Cloudiness : " + item.getCloudiness() + " %");
            viewHolder.sunrise.setText("Sunrise : " + item.getSunrise() + " ");
            viewHolder.sunset.setText("Sunset : " + item.getSunset() + " ");

            //Thianchai (Note)
//            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//
//                }
//            });
            //**

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        CardView rootView;
        ImageView icon;
        TextView headerText;
        TextView locationArea;
        TextView description;
        TextView time;
        TextView headerTemp;
        TextView temp;
        TextView maxTemp;
        TextView minTemp;
        TextView headerDetail;
        TextView pressure;
        TextView humidity;
        TextView windSpeed;
        TextView windDirection;
        TextView cloudiness;
        TextView sunrise;
        TextView sunset;

        ViewHolder(View itemView) {
            super(itemView);

            this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            this.icon = (ImageView)itemView.findViewById(R.id.icon);
            this.headerText = (TextView)itemView.findViewById(R.id.header_text);
            this.locationArea = (TextView)itemView.findViewById(R.id.location_area);
            this.description = (TextView)itemView.findViewById(R.id.description);
            this.time = (TextView)itemView.findViewById(R.id.time);
            this.headerTemp = (TextView)itemView.findViewById(R.id.header_temp);
            this.temp = (TextView)itemView.findViewById(R.id.temp);
            this.maxTemp = (TextView)itemView.findViewById(R.id.max_temp);
            this.minTemp = (TextView)itemView.findViewById(R.id.min_temp);
            this.headerDetail = (TextView)itemView.findViewById(R.id.header_detail);
            this.pressure = (TextView)itemView.findViewById(R.id.pressure);
            this.humidity = (TextView)itemView.findViewById(R.id.humidity);
            this.windSpeed = (TextView)itemView.findViewById(R.id.wind_speed);
            this.windDirection = (TextView)itemView.findViewById(R.id.wind_direction);
            this.cloudiness = (TextView)itemView.findViewById(R.id.cloudiness);
            this.sunrise = (TextView)itemView.findViewById(R.id.sunrise);
            this.sunset = (TextView)itemView.findViewById(R.id.sunset);

        }
    }

    public static class WeatherNEAItem implements Parcelable {

        private String url;
        private String headerText;
        private String locationArea;
        private String description;
        private String time;
        private String headerTemp;
        private String temp;
        private String maxTemp;
        private String minTemp;
        private String headerDetail;
        private String pressure;
        private String humidity;
        private String windSpeed;
        private String windDirection;
        private String cloudiness;
        private String sunrise;
        private String sunset;

        private WeatherNEAItem(Bundle bundle) {

            this.url = bundle.getString("url");
            this.headerText = bundle.getString("headerText");
            this.locationArea = bundle.getString("locationArea");
            this.description = bundle.getString("description");
            this.time = bundle.getString("time");
            this.headerTemp = bundle.getString("headerTemp");
            this.temp = bundle.getString("temp");
            this.maxTemp = bundle.getString("max_temp");
            this.minTemp = bundle.getString("min_temp");
            this.headerDetail = bundle.getString("headerDetail");
            this.pressure = bundle.getString("pressure");
            this.humidity = bundle.getString("humidity");
            this.windSpeed = bundle.getString("windSpeed");
            this.windDirection = bundle.getString("windDirection");
            this.cloudiness = bundle.getString("cloudiness");
            this.sunrise = bundle.getString("sunrise");
            this.sunset = bundle.getString("sunset");

        }

        public WeatherNEAItem(String url, String headerText, String locationArea, String description, String time, String headerTemp, String temp, String maxTemp, String minTemp, String headerDetail, String pressure, String humidity, String windSpeed, String windDirection, String cloudiness, String sunrise, String sunset) {

            this.url = url;
            this.headerText = headerText;
            this.locationArea = locationArea;
            this.description = description;
            this.time = time;
            this.headerTemp = headerTemp;
            this.temp = temp;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.headerDetail = headerDetail;
            this.pressure = pressure;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
            this.cloudiness = cloudiness;
            this.sunrise = sunrise;
            this.sunset = sunset;

        }

        public static final Creator<WeatherNEAListActivity.WeatherNEAItem> CREATOR = new Creator<WeatherNEAListActivity.WeatherNEAItem>() {
            @Override
            public WeatherNEAListActivity.WeatherNEAItem createFromParcel(Parcel in) {
                return new WeatherNEAListActivity.WeatherNEAItem(in.readBundle());
            }

            @Override
            public WeatherNEAListActivity.WeatherNEAItem[] newArray(int size) {
                return new WeatherNEAListActivity.WeatherNEAItem[size];
            }
        };

        public String getUrl(){

            return this.url;

        }

        public String getHeaderText(){

            return this.headerText;

        }

        public String getLocationArea(){

            return this.locationArea;

        }

        public String getDescription(){

            return  this.description;

        }

        public String getTime(){

            return  this.time;

        }

        public String getHeaderTemp(){

            return this.headerTemp;

        }

        public String getTemp(){

            return this.temp;

        }

        public String getMaxTemp(){

            return this.maxTemp;

        }

        public String getMinTemp(){

            return this.minTemp;

        }

        public String getHeaderDetail(){

            return this.headerDetail;

        }

        public String getPressure(){

            return this.pressure;

        }

        public String getHumidity(){

            return this.humidity;

        }

        public String getWindSpeed(){

            return windSpeed;

        }

        public String getWindDirection(){

            return  this.windDirection;

        }

        public String getCloudiness(){

            return this.cloudiness;

        }

        public String getSunrise(){

            return  this.sunrise;

        }

        public String getSunset(){

            return this.sunset;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("url", this.url);
            bundle.putString("headerText", this.headerText);
            bundle.putString("locationArea", this.locationArea);
            bundle.putString("description", this.description);
            bundle.putString("time", this.time);
            bundle.putString("headerTemp", this.headerTemp);
            bundle.putString("temp", this.temp);
            bundle.putString("maxTemp", this.maxTemp);
            bundle.putString("minTemp", this.minTemp);
            bundle.putString("headerDetail", this.headerDetail);
            bundle.putString("pressure", this.pressure);
            bundle.putString("humidity", this.humidity);
            bundle.putString("windSpeed", this.windSpeed);
            bundle.putString("windDirection", this.windDirection);
            bundle.putString("cloudiness", this.cloudiness);
            bundle.putString("sunrise", this.sunrise);
            bundle.putString("sunset", this.sunset);
            dest.writeBundle(bundle);
        }

    }

    //

    @Override
    public void onDoubleTap() {

        Intent intent = new Intent(this, WeatherGraphListActivity.class);
        intent.putExtra("place", "test");
        this.startActivity(intent);

    }
}
