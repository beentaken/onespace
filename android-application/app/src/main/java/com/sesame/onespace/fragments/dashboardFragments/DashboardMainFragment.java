package com.sesame.onespace.fragments.dashboardFragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.dashboardActivities.BusInfoActivity;
import com.sesame.onespace.activities.dashboardActivities.CarparkActivity;
import com.sesame.onespace.activities.dashboardActivities.FlickrActivity;
import com.sesame.onespace.activities.dashboardActivities.InstagramActivity;
import com.sesame.onespace.activities.dashboardActivities.TwitterActivity;
import com.sesame.onespace.activities.dashboardActivities.WeatherActivity;
import com.sesame.onespace.activities.dashboardActivities.YoutubeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thian on 2/11/2559.
 */

public final class DashboardMainFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    // RecyclerView ---------------------------------------------
    public static final int Twitter_ACTIVITY = 1;
    public static final int Youtube_ACTIVITY = 2;
    public static final int Flickr_ACTIVITY = 3;
    public static final int Instagram_ACTIVITY = 4;
    public static final int Weather_ACTIVITY = 5;
    public static final int Carparks_ACTIVITY = 6;
    public static final int BusInfo_ACTIVITY = 7;

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public DashboardMainFragment(){


    }

    public static DashboardMainFragment newInstance() {

        DashboardMainFragment dashboardMainFragment = new DashboardMainFragment();
        return dashboardMainFragment;

    }

    //===========================================================================================================//
    //  ON ACTION                                                                                   ON ACTION
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_main, container, false);

        List<DashboardMainFragment.DashboardItem> items = new ArrayList<>();
        items.add(new DashboardMainFragment.DashboardItem("Twitter", R.drawable.ic_dashboard_twitter, DashboardMainFragment.Twitter_ACTIVITY, 0));
        items.add(new DashboardMainFragment.DashboardItem("Youtube", R.drawable.ic_dashboard_youtube, DashboardMainFragment.Youtube_ACTIVITY, 1));
        items.add(new DashboardMainFragment.DashboardItem("Flickr", R.drawable.ic_dashboard_flickr, DashboardMainFragment.Flickr_ACTIVITY, 2));
        items.add(new DashboardMainFragment.DashboardItem("Instagram", R.drawable.ic_dashboard_instagram, DashboardMainFragment.Instagram_ACTIVITY, 2));
        items.add(new DashboardMainFragment.DashboardItem("Weather", R.drawable.ic_dashboard_weather, DashboardMainFragment.Weather_ACTIVITY, 3));
        items.add(new DashboardMainFragment.DashboardItem("Carparks", R.drawable.ic_dashboard_carpark, DashboardMainFragment.Carparks_ACTIVITY, 3));
        items.add(new DashboardMainFragment.DashboardItem("Bus Info", R.drawable.ic_dashboard_bus_info, DashboardMainFragment.BusInfo_ACTIVITY, 3));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        //add space between items.
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_dashboard_items);
        DashboardMainFragment.SpacesItemDecoration spacesItemDecoration = new DashboardMainFragment.SpacesItemDecoration(spacingInPixels);
        //

        DashBoardListAdapter dashBoardListAdapter = new DashboardMainFragment.DashBoardListAdapter(getContext(), items);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(dashBoardListAdapter);

        return view;
    }

    //===========================================================================================================//
    //  RECYCLER VIEW ADAPTER                                                                       RECYCLER VIEW ADAPTER
    //===========================================================================================================//

    private final class DashBoardListAdapter
            extends RecyclerView.Adapter<DashboardMainFragment.ViewHolder> {

        private List<DashboardItem> items;
        private Context mContext;

        public DashBoardListAdapter(Context context, List<DashboardItem> items) {

            DashBoardListAdapter.this.items = items;
            DashBoardListAdapter.this.mContext = context;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_main_menu, null);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {

            final DashboardItem dashboardItem = DashBoardListAdapter.this.items.get(i);

            String colorStart = "#B0D1D1";
            String colorEnd = "#B0D1D1";

            if (dashboardItem.getGroup() == 0){

                colorStart = "#B0D1D1";
                colorEnd = "#2F7170";

            }

            if (dashboardItem.getGroup() == 1){

                colorStart = "#FF7B57";
                colorEnd = "#931000";

            }

            if (dashboardItem.getGroup() == 2){

                colorStart = "#FAE899";
                colorEnd = "#DD7326";

            }

            if (dashboardItem.getGroup() == 3){

                colorStart = "#FDFF99";
                colorEnd = "#2C845A";

            }

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(dashboardItem.getTitle());
            viewHolder.icon.setImageResource(dashboardItem.getIcon());
            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = null;

                    if (dashboardItem.getActivityID() == 1){

                        intent = new Intent(getContext(), TwitterActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 2){

                        intent = new Intent(getContext(), YoutubeActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 3){

                        intent = new Intent(getContext(), FlickrActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 4){

                        intent = new Intent(getContext(), InstagramActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 5){

                        intent = new Intent(getContext(), WeatherActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 6){

                        intent = new Intent(getContext(), CarparkActivity.class);

                    }

                    if (dashboardItem.getActivityID() == 7){

                        intent = new Intent(getContext(), BusInfoActivity.class);

                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("Name", null);
                    bundle.putString("Vloc", null);
                    bundle.putDouble("Lat", 0);
                    bundle.putDouble("Lng", 0);
                    intent.putExtra("bundle", bundle);
                    intent.putExtra("enter from", "main screen");

                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {

            return DashBoardListAdapter.this.items.size();

        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView title;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);

            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.title = (TextView)itemView.findViewById(R.id.title);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);

        }

    }

    private final static class DashboardItem
            implements Parcelable {

        public static final Creator<DashboardItem> CREATOR = new Creator<DashboardItem>() {
            @Override
            public DashboardItem createFromParcel(Parcel in) {
                return new DashboardItem(in.readBundle());
            }

            @Override
            public DashboardItem[] newArray(int size) {
                return new DashboardItem[size];
            }
        };

        private int icon;
        private String title;
        private int group;
        private int activityID;

        private DashboardItem(Bundle bundle) {

            DashboardItem.this.icon = bundle.getInt("icon");
            DashboardItem.this.title = bundle.getString("title");
            DashboardItem.this.group = bundle.getInt("group");
            DashboardItem.this.activityID = bundle.getInt("activity_id");

        }

        public DashboardItem(String title, int icon, int activityID, int group) {

            DashboardItem.this.icon = icon;
            DashboardItem.this.title = title;
            DashboardItem.this.group = group;
            DashboardItem.this.activityID = activityID;

        }

        public int getIcon() {
            return DashboardItem.this.icon;
        }

        public String getTitle() {
            return DashboardItem.this.title;
        }

        public int getGroup(){
            return DashboardItem.this.group;
        }

        public int getActivityID() {
            return DashboardItem.this.activityID;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            bundle.putInt("icon", DashboardItem.this.icon);
            bundle.putString("title", DashboardItem.this.title);
            bundle.putInt("group", DashboardItem.this.group);
            bundle.putInt("activity_id", DashboardItem.this.activityID);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  SPACES ITEM DECORATION                                                                      SPACES ITEM DECORATION
    //===========================================================================================================//

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
            outRect.bottom = SpacesItemDecoration.this.space;
            outRect.top = SpacesItemDecoration.this.space;

        }
    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
