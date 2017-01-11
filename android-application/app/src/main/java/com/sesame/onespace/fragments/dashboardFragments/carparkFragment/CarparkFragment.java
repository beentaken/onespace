package com.sesame.onespace.fragments.dashboardFragments.carParkFragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
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
import com.sesame.onespace.interfaces.recyclerViewInterfaces.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.utils.connect.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Thian on 4/1/2560.
 */

public final class CarParkFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private CarParkFragment.CarparkAdapter adapter;
    private List<CarParkFragment.CarparkItem> items;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        CarParkFragment.this.init(inflater, container);

        return CarParkFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        CarParkFragment.this.initDefaultValue(inflater, container);
        CarParkFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        CarParkFragment.this.context = CarParkFragment.this.getContext();
        CarParkFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_carpark, container, false);

        CarParkFragment.this.url = CarParkFragment.this.getArguments().getString("url");
        CarParkFragment.this.items = CarParkFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        CarParkFragment.SpacesItemDecoration spacesItemDecoration = new CarParkFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CarParkFragment.this.context);
        CarParkFragment.this.adapter = new CarParkFragment.CarparkAdapter(CarParkFragment.this.context, CarParkFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) CarParkFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(CarParkFragment.this.adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                CarParkFragment.this.addRecyclerView();

            }
        });

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

    private void addRecyclerView(){

        new CarParkFragment.DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = CarParkFragment.this.connectToServer();
        JSONArray jsonArray = CarParkFragment.this.getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);
                CarParkFragment.this.items.add(new CarParkFragment.CarparkItem(R.drawable.ic_dashboard_carpark_car, String.valueOf(object.get("area")), String.valueOf(object.get("development")), String.valueOf(object.get("available_lots")), String.valueOf(object.get("distance_in_km"))));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        JSONObject jsonObject = Connection.getJSON(CarParkFragment.this.url);

        return jsonObject;

    }

    private JSONArray getData(JSONObject jsonObject){

        JSONArray jsonArray = null;

        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return  jsonArray;

    }

    //  private class   ----------------------------------------------------------------------------****private class****

    private final class DownloadFilesTask
            extends AsyncTask<Void, Integer, Boolean> {

        protected Boolean doInBackground(Void ... voids) {

            CarParkFragment.this.loadData();

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            CarParkFragment.this.adapter.notifyDataSetChanged();

        }

    }

    public final class CarparkAdapter
            extends RecyclerView.Adapter<CarParkFragment.ViewHolder> {

        private List<CarParkFragment.CarparkItem> items;
        private Context mContext;

        public CarparkAdapter(Context context, List<CarParkFragment.CarparkItem> settingItems) {

            CarparkAdapter.this.items = settingItems;
            CarparkAdapter.this.mContext = context;

        }

        @Override
        public CarParkFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_carpark, null);

            return new CarParkFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CarParkFragment.ViewHolder viewHolder, int i) {

            final CarParkFragment.CarparkItem item = items.get(i);

            String colorStart = "#B6B6B6";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.area.setText("Area : " + item.getArea());
            viewHolder.development.setText("Development : " + item.getDevelopment());
            viewHolder.available_lots.setText("Available lots : " + item.getAvailableLots());
            viewHolder.distance_in_km.setText("Distance in km : " + item.getDistanceInKm());

        }

        @Override
        public int getItemCount() {

            return CarparkAdapter.this.items.size();

        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        ImageView icon;
        TextView area;
        TextView development;
        TextView available_lots;
        TextView distance_in_km;

        ViewHolder(View itemView) {

            super(itemView);
            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);
            ViewHolder.this.area = (TextView)itemView.findViewById(R.id.area);
            ViewHolder.this.development = (TextView)itemView.findViewById(R.id.development);
            ViewHolder.this.available_lots = (TextView)itemView.findViewById(R.id.available_lots);
            ViewHolder.this.distance_in_km = (TextView)itemView.findViewById(R.id.distance_in_km);

        }
    }

    public final static class CarparkItem
            implements Parcelable {

        public static final Creator<CarParkFragment.CarparkItem> CREATOR = new Creator<CarParkFragment.CarparkItem>() {
            @Override
            public CarParkFragment.CarparkItem createFromParcel(Parcel in) {
                return new CarParkFragment.CarparkItem(in.readBundle());
            }

            @Override
            public CarParkFragment.CarparkItem[] newArray(int size) {
                return new CarParkFragment.CarparkItem[size];
            }
        };

        private int icon;
        private String area;
        private String development;
        private String available_lots;
        private String distance_in_km;

        private CarparkItem(Bundle bundle) {

            CarparkItem.this.icon = bundle.getInt("icon");
            CarparkItem.this.area = bundle.getString("area");
            CarparkItem.this.development = bundle.getString("development");
            CarparkItem.this.available_lots = bundle.getString("available_lots");
            CarparkItem.this.distance_in_km = bundle.getString("distance_in_km");

        }

        public CarparkItem(int icon, String area, String development, String available_lots, String distance_in_km) {

            CarparkItem.this.icon = icon;
            CarparkItem.this.area = area;
            CarparkItem.this.development = development;
            CarparkItem.this.available_lots = available_lots;
            CarparkItem.this.distance_in_km = distance_in_km;

        }

        public int getIcon() {
            return CarparkItem.this.icon;
        }

        public String getArea() {
            return CarparkItem.this.area;
        }

        public String getDevelopment() {
            return CarparkItem.this.development;
        }

        public String getAvailableLots() {
            return CarparkItem.this.available_lots;
        }

        public String getDistanceInKm(){
            return CarparkItem.this.distance_in_km;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            bundle.putInt("icon", icon);
            bundle.putString("area", CarparkItem.this.area);
            bundle.putString("development", CarparkItem.this.development);
            bundle.putString("available_lots", CarparkItem.this.available_lots);
            bundle.putString("distance_in_km", CarparkItem.this.distance_in_km);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
