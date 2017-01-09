package com.sesame.onespace.fragments.dashboardFragments.weatherFragment;

import android.content.Context;
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
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sesame.onespace.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Thian on 5/1/2560.
 */

public final class WeatherForecastFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private WeatherForecastFragment.WeatherForecastAdapter adapter;
    private List<WeatherForecastFragment.WeatherForecastItem> items;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        WeatherForecastFragment.this.init(inflater, container);

        return WeatherForecastFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        WeatherForecastFragment.this.initDefaultValue(inflater, container);
        WeatherForecastFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        WeatherForecastFragment.this.context = WeatherForecastFragment.this.getContext();
        WeatherForecastFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_weather_forecast, container, false);

        WeatherForecastFragment.this.url = WeatherForecastFragment.this.getArguments().getString("url");
        WeatherForecastFragment.this.items = WeatherForecastFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        WeatherForecastFragment.SpacesItemDecoration spacesItemDecoration = new WeatherForecastFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WeatherForecastFragment.this.context);
        WeatherForecastFragment.this.adapter = new WeatherForecastFragment.WeatherForecastAdapter(WeatherForecastFragment.this.context, WeatherForecastFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) WeatherForecastFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(WeatherForecastFragment.this.adapter);

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private final class SpacesItemDecoration
            extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {

            SpacesItemDecoration.this.space = space;

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            outRect.left = SpacesItemDecoration.this.space;
            outRect.right = SpacesItemDecoration.this.space;
            outRect.bottom = SpacesItemDecoration.this.space/2;
            outRect.top = SpacesItemDecoration.this.space/2;

        }

    }

    //===========================================================================================================//
    //  START RECYCLER VIEW                                                                         START RECYCLER VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****



    //  private class   ----------------------------------------------------------------------------****private class****

    public final class WeatherForecastAdapter
            extends RecyclerView.Adapter<WeatherForecastFragment.ViewHolder> {

        private List<WeatherForecastFragment.WeatherForecastItem> items;
        private Context mContext;

        public WeatherForecastAdapter(Context context, List<WeatherForecastFragment.WeatherForecastItem> settingItems) {

            WeatherForecastAdapter.this.items = settingItems;
            WeatherForecastAdapter.this.mContext = context;

        }

        @Override
        public WeatherForecastFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_weather_forecast, null);

            return new WeatherForecastFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WeatherForecastFragment.ViewHolder viewHolder, int i) {

            final WeatherForecastFragment.WeatherForecastItem item = items.get(i);

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

        }

        @Override
        public int getItemCount() {

            return WeatherForecastAdapter.this.items.size();

        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView textHeader;
        GraphView graphView;

        ViewHolder(View itemView) {
            super(itemView);

            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.textHeader = (TextView) itemView.findViewById(R.id.header_text);
            ViewHolder.this.graphView = (GraphView) itemView.findViewById(R.id.graph);

        }
    }

    public final static class WeatherForecastItem
            implements Parcelable {

        public static final Creator<WeatherForecastFragment.WeatherForecastItem> CREATOR = new Creator<WeatherForecastFragment.WeatherForecastItem>() {
            @Override
            public WeatherForecastFragment.WeatherForecastItem createFromParcel(Parcel in) {
                return new WeatherForecastFragment.WeatherForecastItem(in.readBundle());
            }

            @Override
            public WeatherForecastFragment.WeatherForecastItem[] newArray(int size) {
                return new WeatherForecastFragment.WeatherForecastItem[size];
            }
        };

        private String textHeader;
        private ArrayList<Double> dataList;
        private ArrayList<String> dateList;
        private String type;


        private WeatherForecastItem(Bundle bundle) {

            WeatherForecastItem.this.textHeader = bundle.getString("textHeader");
            WeatherForecastItem.this.dataList = new ArrayList(Arrays.asList(bundle.getDoubleArray("dataList")));
            WeatherForecastItem.this.dateList = new ArrayList(Arrays.asList(bundle.getStringArray("dateList")));
            WeatherForecastItem.this.type = bundle.getString("type");

        }

        public WeatherForecastItem(String textHeader, ArrayList<String> dateList, ArrayList<Double> dataList, String type) {

            WeatherForecastItem.this.textHeader = textHeader;
            WeatherForecastItem.this.dataList = dataList;
            WeatherForecastItem.this.dateList = dateList;
            WeatherForecastItem.this.type = type;

        }

        public String getTextHeader(){

            return WeatherForecastItem.this.textHeader;

        }

        public ArrayList<Double> getDataList(){

            return WeatherForecastItem.this.dataList;

        }

        public ArrayList<String> getDateList(){

            return WeatherForecastItem.this.dateList;

        }

        public String getType(){

            return WeatherForecastItem.this.type;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            bundle.putString("textHeader", WeatherForecastItem.this.textHeader);

            double[] dataArray = new double[WeatherForecastItem.this.dataList.size()];

            for (double d : WeatherForecastItem.this.dataList){

                dataArray[WeatherForecastItem.this.dataList.indexOf(d)] = d;

            }

            bundle.putDoubleArray("dataList", dataArray);

            String[] dateArray = new String[WeatherForecastItem.this.dateList.size()];

            for (String s : WeatherForecastItem.this.dateList){

                dateArray[WeatherForecastItem.this.dateList.indexOf(s)] = s;

            }

            bundle.putStringArray("dateList", dateArray);
            bundle.putString("type", WeatherForecastItem.this.type);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//



}
