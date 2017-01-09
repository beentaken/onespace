package com.sesame.onespace.fragments.dashboardFragments.busInfoFragment;

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

public final class BusInfoFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private BusInfoFragment.BusInfoAdapter adapter;
    private List<BusInfoFragment.BusInfoItem> items;

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        BusInfoFragment.this.init(inflater, container);

        return BusInfoFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        BusInfoFragment.this.initDefaultValue(inflater, container);
        BusInfoFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        BusInfoFragment.this.context = BusInfoFragment.this.getContext();
        BusInfoFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_bus_info, container, false);

        BusInfoFragment.this.url = BusInfoFragment.this.getArguments().getString("url");
        BusInfoFragment.this.items = BusInfoFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        BusInfoFragment.SpacesItemDecoration spacesItemDecoration = new BusInfoFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BusInfoFragment.this.context);
        BusInfoFragment.this.adapter = new BusInfoFragment.BusInfoAdapter(BusInfoFragment.this.context, BusInfoFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) BusInfoFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(BusInfoFragment.this.adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                BusInfoFragment.this.addRecyclerView();

            }
        });

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    private final class SpacesItemDecoration
            extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {

            BusInfoFragment.SpacesItemDecoration.this.space = space;

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            outRect.left = BusInfoFragment.SpacesItemDecoration.this.space;
            outRect.right = BusInfoFragment.SpacesItemDecoration.this.space;
            outRect.bottom = BusInfoFragment.SpacesItemDecoration.this.space/2;
            outRect.top = BusInfoFragment.SpacesItemDecoration.this.space/2;

        }

    }

    //===========================================================================================================//
    //  START RECYCLER VIEW                                                                         START RECYCLER VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void addRecyclerView(){

        new BusInfoFragment.DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = BusInfoFragment.this.connectToServer();
        JSONArray jsonArray = BusInfoFragment.this.getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

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

                BusInfoFragment.this.items.add(new BusInfoFragment.BusInfoItem(R.drawable.ic_dashboard_bus_info_bus, String.valueOf(object.get("description")), String.valueOf(object.get("distance_in_km")), arrivalString));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        JSONObject jsonObject = Connection.getJSON(BusInfoFragment.this.url);

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

            BusInfoFragment.this.loadData();

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            BusInfoFragment.this.adapter.notifyDataSetChanged();

        }

    }

    public final class BusInfoAdapter
            extends RecyclerView.Adapter<BusInfoFragment.ViewHolder> {

        private List<BusInfoFragment.BusInfoItem> items;
        private Context mContext;

        public BusInfoAdapter(Context context, List<BusInfoFragment.BusInfoItem> settingItems) {

            BusInfoAdapter.this.items = settingItems;
            BusInfoAdapter.this.mContext = context;

        }

        @Override
        public BusInfoFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_bus_info, null);

            return new BusInfoFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BusInfoFragment.ViewHolder viewHolder, int i) {

            final BusInfoFragment.BusInfoItem item = items.get(i);

            String colorStart = "#B6B6B6";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.description.setText("Description : " + item.getDescription());
            viewHolder.distance_in_km.setText("Distance in km : " + item.getDistanceInKm());
            viewHolder.arrival_times.setText(item.getArrivalTimes());

        }

        @Override
        public int getItemCount() {

            return BusInfoAdapter.this.items.size();

        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        ImageView icon;
        TextView description;
        TextView distance_in_km;
        TextView arrival_times;

        ViewHolder(View itemView) {

            super(itemView);
            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);
            ViewHolder.this.description = (TextView)itemView.findViewById(R.id.description);
            ViewHolder.this.distance_in_km = (TextView)itemView.findViewById(R.id.distance_in_km);
            ViewHolder.this.arrival_times = (TextView)itemView.findViewById(R.id.arrival_times);

        }
    }

    public final static class BusInfoItem
            implements Parcelable {

        public static final Creator<BusInfoItem> CREATOR = new Creator<BusInfoItem>() {
            @Override
            public BusInfoItem createFromParcel(Parcel in) {
                return new BusInfoItem(in.readBundle());
            }

            @Override
            public BusInfoItem[] newArray(int size) {
                return new BusInfoItem[size];
            }
        };

        private int icon;
        private String description;
        private String distance_in_km;
        private String arrival_times;

        private BusInfoItem(Bundle bundle) {

            BusInfoItem.this.icon = bundle.getInt("icon");
            BusInfoItem.this.description = bundle.getString("description");
            BusInfoItem.this.distance_in_km = bundle.getString("distance_in_km");
            BusInfoItem.this.arrival_times = bundle.getString("arrival_times");

        }

        public BusInfoItem(int icon, String description, String distance_in_km, String arrival_times) {

            BusInfoItem.this.icon = icon;
            BusInfoItem.this.description = description;
            BusInfoItem.this.distance_in_km = distance_in_km;
            BusInfoItem.this.arrival_times = arrival_times;

        }

        public int getIcon() {
            return icon;
        }

        public String getDescription() {
            return BusInfoItem.this.description;
        }

        public String getDistanceInKm(){
            return BusInfoItem.this.distance_in_km;
        }

        public String getArrivalTimes(){
            return BusInfoItem.this.arrival_times;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            bundle.putInt("icon", icon);
            bundle.putString("description", BusInfoItem.this.description);
            bundle.putString("distance_in_km", BusInfoItem.this.distance_in_km);
            bundle.putString("arrival_times", BusInfoItem.this.arrival_times);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
