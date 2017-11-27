package com.sesame.onespace.fragments.dashboardFragments.weatherFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Thian on 4/1/2560.
 */

public final class WeatherFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private WeatherFragment.WeatherAdapter adapter;
    private List<WeatherFragment.WeatherItem> items;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WeatherFragment.this.init(inflater, container);

        return this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        WeatherFragment.this.initDefaultValue(inflater, container);
        WeatherFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        WeatherFragment.this.context = WeatherFragment.this.getContext();
        WeatherFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_weather, container, false);

        WeatherFragment.this.url = WeatherFragment.this.getArguments().getString("url");
        WeatherFragment.this.items = WeatherFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        WeatherFragment.SpacesItemDecoration spacesItemDecoration = new WeatherFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WeatherFragment.this.context);
        WeatherFragment.this.adapter = new WeatherFragment.WeatherAdapter(WeatherFragment.this.context, WeatherFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) WeatherFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(WeatherFragment.this.adapter);

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private final class SpacesItemDecoration
            extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {

            WeatherFragment.SpacesItemDecoration.this.space = space;

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            outRect.left = WeatherFragment.SpacesItemDecoration.this.space;
            outRect.right = WeatherFragment.SpacesItemDecoration.this.space;
            outRect.bottom = WeatherFragment.SpacesItemDecoration.this.space/2;
            outRect.top = WeatherFragment.SpacesItemDecoration.this.space/2;

        }

    }

    //===========================================================================================================//
    //  START RECYCLER VIEW                                                                         START RECYCLER VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****



    //  private class   ----------------------------------------------------------------------------****private class****

    public final class WeatherAdapter
            extends RecyclerView.Adapter<WeatherFragment.ViewHolder> {

        private List<WeatherFragment.WeatherItem> items;
        private Context mContext;

        public WeatherAdapter(Context context, List<WeatherFragment.WeatherItem> settingItems) {

            WeatherAdapter.this.items = settingItems;
            WeatherAdapter.this.mContext = context;

        }

        @Override
        public WeatherFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_weather, null);

            return new WeatherFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeatherFragment.ViewHolder viewHolder, int i) {

            final WeatherFragment.WeatherItem item = items.get(i);

            String color1 = "#FFFFFF";
            String color2 = "#FFFFFF";

            int colors[] = { Color.parseColor(color1), Color.parseColor(color2)};
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.icon.setImageBitmap(item.getBitmap());
            viewHolder.headerText.setText(item.getHeaderText());
            //viewHolder.locationArea.setText("Location Area : " + item.getLocationArea());
            viewHolder.description.setText("Description: " + item.getDescription());
            viewHolder.time.setText(item.getTime());
            viewHolder.headerTemp.setText(item.getHeaderTemp());
            viewHolder.temp.setText("Temperature: " + item.getTemp() + "°C");
            viewHolder.maxTemp.setText("Max Temperature: " + item.getMaxTemp() + "°C");
            viewHolder.minTemp.setText("Min Temperature: " + item.getMinTemp() + "°C");
            viewHolder.headerDetail.setText(item.getHeaderDetail());
            viewHolder.pressure.setText("Pressure: " + item.getPressure() + " hpa");
            viewHolder.humidity.setText("Humidity: " + item.getHumidity() + "%");
            viewHolder.windSpeed.setText("Wind Speed: " + item.getWindSpeed() + " m/s");
            viewHolder.windDirection.setText("Wind Direction: " + item.getWindDirection() + " degrees");
            viewHolder.cloudiness.setText("Cloudiness: " + item.getCloudiness() + "%");
            viewHolder.sunrise.setText("Sunrise: " + item.getSunrise() + " ");
            viewHolder.sunset.setText("Sunset: " + item.getSunset() + " ");

        }

        @Override
        public int getItemCount() {

            return WeatherAdapter.this.items.size();

        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

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

            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);
            ViewHolder.this.headerText = (TextView)itemView.findViewById(R.id.header_text);
            //ViewHolder.this.locationArea = (TextView)itemView.findViewById(R.id.location_area);
            ViewHolder.this.description = (TextView)itemView.findViewById(R.id.description);
            ViewHolder.this.time = (TextView)itemView.findViewById(R.id.time);
            ViewHolder.this.headerTemp = (TextView)itemView.findViewById(R.id.header_temp);
            ViewHolder.this.temp = (TextView)itemView.findViewById(R.id.temp);
            ViewHolder.this.maxTemp = (TextView)itemView.findViewById(R.id.max_temp);
            ViewHolder.this.minTemp = (TextView)itemView.findViewById(R.id.min_temp);
            ViewHolder.this.headerDetail = (TextView)itemView.findViewById(R.id.header_detail);
            ViewHolder.this.pressure = (TextView)itemView.findViewById(R.id.pressure);
            ViewHolder.this.humidity = (TextView)itemView.findViewById(R.id.humidity);
            ViewHolder.this.windSpeed = (TextView)itemView.findViewById(R.id.wind_speed);
            ViewHolder.this.windDirection = (TextView)itemView.findViewById(R.id.wind_direction);
            ViewHolder.this.cloudiness = (TextView)itemView.findViewById(R.id.cloudiness);
            ViewHolder.this.sunrise = (TextView)itemView.findViewById(R.id.sunrise);
            ViewHolder.this.sunset = (TextView)itemView.findViewById(R.id.sunset);

        }
    }

    public final static class WeatherItem
            implements Parcelable {

        public static final Creator<WeatherItem> CREATOR = new Creator<WeatherItem>() {
            @Override
            public WeatherItem createFromParcel(Parcel in) {
                return new WeatherItem(in.readBundle());
            }

            @Override
            public WeatherItem[] newArray(int size) {
                return new WeatherItem[size];
            }
        };

        private Bitmap bitmap;
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

        private WeatherItem(Bundle bundle) {

            //WeatherItem.this.url = bundle.getString("url");
            WeatherItem.this.headerText = bundle.getString("headerText");
            WeatherItem.this.locationArea = bundle.getString("locationArea");
            WeatherItem.this.description = bundle.getString("description");
            WeatherItem.this.time = bundle.getString("time");
            WeatherItem.this.headerTemp = bundle.getString("headerTemp");
            WeatherItem.this.temp = bundle.getString("temp");
            WeatherItem.this.maxTemp = bundle.getString("max_temp");
            WeatherItem.this.minTemp = bundle.getString("min_temp");
            WeatherItem.this.headerDetail = bundle.getString("headerDetail");
            WeatherItem.this.pressure = bundle.getString("pressure");
            WeatherItem.this.humidity = bundle.getString("humidity");
            WeatherItem.this.windSpeed = bundle.getString("windSpeed");
            WeatherItem.this.windDirection = bundle.getString("windDirection");
            WeatherItem.this.cloudiness = bundle.getString("cloudiness");
            WeatherItem.this.sunrise = bundle.getString("sunrise");
            WeatherItem.this.sunset = bundle.getString("sunset");

        }

        public WeatherItem(Bitmap bitmap, String headerText, String locationArea, String description, String time, String headerTemp, String temp, String maxTemp, String minTemp, String headerDetail, String pressure, String humidity, String windSpeed, String windDirection, String cloudiness, String sunrise, String sunset) {

            WeatherItem.this.bitmap = bitmap;
            WeatherItem.this.headerText = headerText;
            WeatherItem.this.locationArea = locationArea;
            WeatherItem.this.description = description;
            WeatherItem.this.time = time;
            WeatherItem.this.headerTemp = headerTemp;
            WeatherItem.this.temp = temp;
            WeatherItem.this.maxTemp = maxTemp;
            WeatherItem.this.minTemp = minTemp;
            WeatherItem.this.headerDetail = headerDetail;
            WeatherItem.this.pressure = pressure;
            WeatherItem.this.humidity = humidity;
            WeatherItem.this.windSpeed = windSpeed;
            WeatherItem.this.windDirection = windDirection;
            WeatherItem.this.cloudiness = cloudiness;
            WeatherItem.this.sunrise = sunrise;
            WeatherItem.this.sunset = sunset;

        }

        public Bitmap getBitmap(){

            return WeatherItem.this.bitmap;

        }

        public String getHeaderText(){

            return WeatherItem.this.headerText;

        }

        public String getLocationArea(){

            return WeatherItem.this.locationArea;

        }

        public String getDescription(){

            return  WeatherItem.this.description;

        }

        public String getTime(){

            return  WeatherItem.this.time;

        }

        public String getHeaderTemp(){

            return WeatherItem.this.headerTemp;

        }

        public String getTemp(){

            return WeatherItem.this.temp;

        }

        public String getMaxTemp(){

            return WeatherItem.this.maxTemp;

        }

        public String getMinTemp(){

            return WeatherItem.this.minTemp;

        }

        public String getHeaderDetail(){

            return WeatherItem.this.headerDetail;

        }

        public String getPressure(){

            return WeatherItem.this.pressure;

        }

        public String getHumidity(){

            return WeatherItem.this.humidity;

        }

        public String getWindSpeed(){

            return WeatherItem.this.windSpeed;

        }

        public String getWindDirection(){

            return  WeatherItem.this.windDirection;

        }

        public String getCloudiness(){

            return WeatherItem.this.cloudiness;

        }

        public String getSunrise(){

            return  WeatherItem.this.sunrise;

        }

        public String getSunset(){

            return WeatherItem.this.sunset;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            //bundle.putString("url", WeatherItem.this.url);
            bundle.putString("headerText", WeatherItem.this.headerText);
            bundle.putString("locationArea", WeatherItem.this.locationArea);
            bundle.putString("description", WeatherItem.this.description);
            bundle.putString("time", WeatherItem.this.time);
            bundle.putString("headerTemp", WeatherItem.this.headerTemp);
            bundle.putString("temp", WeatherItem.this.temp);
            bundle.putString("maxTemp", WeatherItem.this.maxTemp);
            bundle.putString("minTemp", WeatherItem.this.minTemp);
            bundle.putString("headerDetail", WeatherItem.this.headerDetail);
            bundle.putString("pressure", WeatherItem.this.pressure);
            bundle.putString("humidity", WeatherItem.this.humidity);
            bundle.putString("windSpeed", WeatherItem.this.windSpeed);
            bundle.putString("windDirection", WeatherItem.this.windDirection);
            bundle.putString("cloudiness", WeatherItem.this.cloudiness);
            bundle.putString("sunrise", WeatherItem.this.sunrise);
            bundle.putString("sunset", WeatherItem.this.sunset);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
