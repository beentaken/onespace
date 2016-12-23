package com.sesame.onespace.activities.dashboardActivities.oldDashboardActivities;

import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sesame.onespace.R;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.utils.connect.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Thian on 25/11/2559.
 */

public class WeatherGraphListActivity extends DashboardActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private Intent intentOfPreviousActivity;

    private String place;

    private JSONParser jsonParser;

    private List<WeatherGraphListActivity.WeatherGraphItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private WeatherGraphListActivity.WeatherGraphListAdapter adapter;

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

        this.initStatusActivity();
        this.initStatusBar();
        this.initActivity();
        this.initToolbar();
        this.initRecyclerView();

    }

    private void initStatusActivity(){

        this.intentOfPreviousActivity = getIntent();

        this.place = this.intentOfPreviousActivity.getStringExtra("place");

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
        this.mToolBar.setTitle("Weather Graph");
        this.mToolBar.setSubtitle(this.place);
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

        setContentView(R.layout.activity_weather_graph_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initRecyclerView(){

        this.initConnection();
        this.initItem();

        //add space between items.
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        WeatherGraphListActivity.SpacesItemDecoration spacesItemDecoration = new WeatherGraphListActivity.SpacesItemDecoration(spacingInPixels);
        //

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);
        this.adapter = new WeatherGraphListActivity.WeatherGraphListAdapter(this.context, this.items);
        this.recyclerView.setAdapter(this.adapter);

    }

    private void initConnection(){

        this.jsonParser = new JSONParser();

    }

    private void initItem(){

        this.items = new ArrayList<WeatherGraphListActivity.WeatherGraphItem>();

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
            outRect.bottom = space/2;
            outRect.top = space/2;

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

        ArrayList<String> dateList = getDateList(jsonObject);

        String tempHeaderText = "Temperature Call 5 day / 3 hour";
        ArrayList<Double> tempList = getTemperature(jsonObject);
        String tempType = "°C";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(tempHeaderText, dateList, tempList, tempType));

        String pressureHeaderText = "Pressure Call 5 day / 3 hour";
        ArrayList<Double> pressureList = getPressure(jsonObject);
        String pressureType = "hpa";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(pressureHeaderText, dateList, pressureList, pressureType));

        String humidityHeaderText = "Humidity Call 5 day / 3 hour";
        ArrayList<Double> humidityList = getHumidity(jsonObject);
        String humidityType = "%";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(humidityHeaderText, dateList, humidityList, humidityType));

        String windSpeedHeaderText = "Wind Speed Call 5 day / 3 hour";
        ArrayList<Double> windSpeedList = getWindSpeed(jsonObject);
        String windSpeedType = "m/s";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(windSpeedHeaderText, dateList, windSpeedList, windSpeedType));

        String windDirectionHeaderText = "Wind Direction Call 5 day / 3 hour";
        ArrayList<Double> windDirectionList = getWindDirection(jsonObject);
        String windDirectionType = "deg";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(windDirectionHeaderText, dateList, windDirectionList, windDirectionType));

        String cloudinessHeaderText = "Cloudiness Call 5 day / 3 hour";
        ArrayList<Double> cloudinessList = getCloudiness(jsonObject);
        String cloudinessType = "%";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(cloudinessHeaderText, dateList, cloudinessList, cloudinessType));

        String rainHeaderText = "Rain Volume Call 5 day / 3 hour";
        ArrayList<Double> rainList = getRainVolume(jsonObject);
        String rainType = "mm";

        items.add(new WeatherGraphListActivity.WeatherGraphItem(rainHeaderText, dateList, rainList, rainType));

    }

    private JSONObject connectToServer(){

        String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + UserLocationManager.getLatitude() + "&lon=" + UserLocationManager.getLongitude() + "&APPID=878736638c5a1aa40f155942c7460a31";

        JSONObject jsonObject = this.jsonParser.makeHttpRequest(url, "GET");

        return jsonObject;

    }

    private ArrayList<String> getDateList(JSONObject jsonObject){

        ArrayList<String> dateList = new ArrayList<String>();

        try {

            JSONArray jsonArray = (JSONArray)jsonObject.get("list");

            int index = 0;

            while(index < jsonArray.length()){

                Object object = jsonArray.get(index);
                dateList.add(getDate(((Integer) ((JSONObject)object).get("dt")) + ""));

                index = index + 1;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dateList;

    }

    private String getDate(String timeStampStr){

        try{

            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            Date netDate = (new Date(Long.parseLong(String.valueOf(Long.parseLong(timeStampStr) * 1000))));

            return sdf.format(netDate);
        }
        catch(Exception ex){



        }

        return "";
    }

    private ArrayList<Double> getTemperature(JSONObject jsonObject){

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

    private double convertKelvinToCelsius(double kelvin){

        double celsius = kelvin - 273.15;

        return celsius;

    }

    private ArrayList<Double> getPressure(JSONObject jsonObject){

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

    private ArrayList<Double> getHumidity(JSONObject jsonObject){

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

    private ArrayList<Double> getWindSpeed(JSONObject jsonObject){

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

    private ArrayList<Double> getWindDirection(JSONObject jsonObject){

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

    private ArrayList<Double> getCloudiness(JSONObject jsonObject){

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

    private ArrayList<Double> getRainVolume(JSONObject jsonObject){

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

    public class WeatherGraphListAdapter extends RecyclerView.Adapter<WeatherGraphListActivity.ViewHolder> {

        private List<WeatherGraphListActivity.WeatherGraphItem> items;
        private Context mContext;

        public WeatherGraphListAdapter(Context context, List<WeatherGraphListActivity.WeatherGraphItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public WeatherGraphListActivity.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_weather_graph_item, null);
            return new WeatherGraphListActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeatherGraphListActivity.ViewHolder viewHolder, int i) {

            final WeatherGraphListActivity.WeatherGraphItem item = items.get(i);

            String color1 = "#FFFFFF";
            String color2 = "#FFFFFF";

            int colors[] = { Color.parseColor(color1), Color.parseColor(color2)};
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.textHeader.setText(item.getTextHeader());

            viewHolder.graphView.removeAllSeries();
            viewHolder.graphView.getViewport().setScalable(true);
            viewHolder.graphView.getViewport().setScrollable(true);
            //graph.getViewport().setScalableY(true);
            //graph.getViewport().setScrollableY(true);

            viewHolder.graphView.getViewport().setXAxisBoundsManual(true);
            viewHolder.graphView.getViewport().setMinX(0);
            viewHolder.graphView.getViewport().setMaxX(40);

            DataPoint[] dataPoints = new DataPoint[item.getDataList().size()];
            int max = 0;
            int min = 0;
            boolean cMax = false;
            boolean cMin = false;
            int point = 0;
            for (double d : item.getDataList()){

                dataPoints[point] = new DataPoint(point, d);

                if (cMax == false){

                    cMax = true;
                    max = (int) d;

                }

                if (d > max){

                    max = (int) d;

                }

                if (cMin == false){

                    cMin = true;
                    min = (int) d;

                }

                if (d < min){

                    min = (int) d;

                }

                point = point + 1;

            }

            viewHolder.graphView.getViewport().setYAxisBoundsManual(true);
            viewHolder.graphView.getViewport().setMaxY(max + 1);
            viewHolder.graphView.getViewport().setMinY(min);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);

            series.setTitle(item.getTextHeader());
            series.setColor(Color.BLACK);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
            series.setThickness(8);

            viewHolder.graphView.addSeries(series);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(3);
            nf.setMinimumIntegerDigits(2);

            viewHolder.graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));

            viewHolder.graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values

                        String date;
                        try{

                            date = item.getDateList().get((int) value);

                        }catch (Exception e){

                            date = "";

                        }

                        return date;
                    } else {
                        // show currency for y values
                        return super.formatLabel(value, isValueX) + " " + item.getType();
                    }
                }
            });

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
        TextView textHeader;
        GraphView graphView;

        ViewHolder(View itemView) {
            super(itemView);

            this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            this.textHeader = (TextView) itemView.findViewById(R.id.header_text);
            this.graphView = (GraphView) itemView.findViewById(R.id.graph);

        }
    }

    public static class WeatherGraphItem implements Parcelable {

        private String textHeader;
        private ArrayList<Double> dataList;
        private ArrayList<String> dateList;
        private String type;

        private WeatherGraphItem(Bundle bundle) {

            this.textHeader = bundle.getString("textHeader");
            this.dataList = new ArrayList(Arrays.asList(bundle.getDoubleArray("dataList")));
            this.dateList = new ArrayList(Arrays.asList(bundle.getStringArray("dateList")));
            this.type = bundle.getString("type");

        }

        public WeatherGraphItem(String textHeader, ArrayList<String> dateList, ArrayList<Double> dataList, String type) {

            this.textHeader = textHeader;
            this.dataList = dataList;
            this.dateList = dateList;
            this.type = type;

        }

        public static final Creator<WeatherGraphListActivity.WeatherGraphItem> CREATOR = new Creator<WeatherGraphListActivity.WeatherGraphItem>() {
            @Override
            public WeatherGraphListActivity.WeatherGraphItem createFromParcel(Parcel in) {
                return new WeatherGraphListActivity.WeatherGraphItem(in.readBundle());
            }

            @Override
            public WeatherGraphListActivity.WeatherGraphItem[] newArray(int size) {
                return new WeatherGraphListActivity.WeatherGraphItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        public String getTextHeader(){

            return this.textHeader;

        }

        public ArrayList<Double> getDataList(){

            return this.dataList;

        }

        public ArrayList<String> getDateList(){

            return this.dateList;

        }

        public String getType(){

            return this.type;

        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("textHeader", this.textHeader);

            double[] dataArray = new double[this.dataList.size()];

            for (double d : this.dataList){

                dataArray[this.dataList.indexOf(d)] = d;

            }

            bundle.putDoubleArray("dataList", dataArray);

            String[] dateArray = new String[this.dateList.size()];

            for (String s : this.dateList){

                dateArray[this.dateList.indexOf(s)] = s;

            }

            bundle.putStringArray("dateList", dateArray);
            bundle.putString("type", this.type);
            dest.writeBundle(bundle);
        }

    }

    //

    @Override
    public void onSwipe(int direction) {



    }

}
