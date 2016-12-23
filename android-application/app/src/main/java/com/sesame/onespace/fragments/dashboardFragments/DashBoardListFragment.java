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
import com.sesame.onespace.activities.dashboardActivities.FlickrActivity;
import com.sesame.onespace.activities.dashboardActivities.InstagramActivity;
import com.sesame.onespace.activities.dashboardActivities.TwitterActivity;
import com.sesame.onespace.activities.dashboardActivities.YoutubeActivity;
import com.sesame.onespace.activities.dashboardActivities.oldDashboardActivities.BusInfoLTAActivity;
import com.sesame.onespace.activities.dashboardActivities.oldDashboardActivities.CarparksLTAActivity;
import com.sesame.onespace.activities.dashboardActivities.oldDashboardActivities.WeatherNEAListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thian on 2/11/2559.
 */

public class DashBoardListFragment extends Fragment {

    private RecyclerView recyclerView;
    private DashBoardListAdapter adapter;

    public static final int ACTIVITY_LastTweets = 1;
    public static final int ACTIVITY_Youtube = 2;
    public static final int ACTIVITY_Flickr = 3;
    public static final int ACTIVITY_Instagram = 4;
    public static final int ACTIVITY_WeatherNEA = 5;
    public static final int ACTIVITY_CarparksLTA = 6;
    public static final int ACTIVITY_BusOnfoLTA = 7;

    public DashBoardListFragment(){


    }

    public static DashBoardListFragment newInstance() {

        DashBoardListFragment fragment = new DashBoardListFragment();
        return fragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_main, container, false);

        List<DashBoardListFragment.DashboardItem> items = new ArrayList<>();
        items.add(new DashBoardListFragment.DashboardItem("Latest Tweets", R.drawable.ic_dashboard_twitter, ACTIVITY_LastTweets, 0));
        items.add(new DashBoardListFragment.DashboardItem("Youtube", R.drawable.ic_dashboard_youtube, ACTIVITY_Youtube, 1));
        items.add(new DashBoardListFragment.DashboardItem("Flickr", R.drawable.ic_dashboard_flickr, ACTIVITY_Flickr, 2));
        items.add(new DashBoardListFragment.DashboardItem("Instagram", R.drawable.ic_dashboard_instagram, ACTIVITY_Instagram, 2));
        items.add(new DashBoardListFragment.DashboardItem("Weather(NEA)", R.drawable.ic_dashboard_weather, ACTIVITY_WeatherNEA, 3));
        items.add(new DashBoardListFragment.DashboardItem("Carparks(LTA)", R.drawable.ic_dashboard_carpark, ACTIVITY_CarparksLTA, 3));
        items.add(new DashBoardListFragment.DashboardItem("Bus Info(LTA)", R.drawable.ic_dashboard_bus_info, ACTIVITY_BusOnfoLTA, 3));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        //add space between items.
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_dashboard_items);
        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(spacingInPixels);
        //

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);
        adapter = new DashBoardListAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);
        return view;
    }

    public class DashBoardListAdapter extends RecyclerView.Adapter<DashBoardListFragment.ViewHolder> {

        private List<DashboardItem> items;
        private Context mContext;

        public DashBoardListAdapter(Context context, List<DashboardItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_main_menu, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {

            final DashboardItem item = items.get(i);

            String colorStart = "#B0D1D1";
            String colorEnd = "#B0D1D1";

            if (item.getGroup() == 0){

                colorStart = "#B0D1D1";
                colorEnd = "#2F7170";

            }

            if (item.getGroup() == 1){

                colorStart = "#FF7B57";
                colorEnd = "#931000";

            }

            if (item.getGroup() == 2){

                colorStart = "#FAE899";
                colorEnd = "#DD7326";

            }

            if (item.getGroup() == 3){

                colorStart = "#FDFF99";
                colorEnd = "#2C845A";

            }

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(item.getTitle());
            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;

                    if (item.getActivityID() == 1){

                        intent = new Intent(getContext(), TwitterActivity.class);

                    }

                    if (item.getActivityID() == 2){

                        intent = new Intent(getContext(), YoutubeActivity.class);

                    }

                    if (item.getActivityID() == 3){

                        intent = new Intent(getContext(), FlickrActivity.class);

                    }

                    if (item.getActivityID() == 4){

                        intent = new Intent(getContext(), InstagramActivity.class);

                    }

                    if (item.getActivityID() == 5){

                        intent = new Intent(getContext(), WeatherNEAListActivity.class);

                    }

                    if (item.getActivityID() == 6){

                        intent = new Intent(getContext(), CarparksLTAActivity.class);

                    }

                    if (item.getActivityID() == 7){

                        intent = new Intent(getContext(), BusInfoLTAActivity.class);

                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("Name", null);
                    bundle.putString("Vloc", null);
                    bundle.putDouble("Lat", 0);
                    bundle.putDouble("Lng", 0);
                    intent.putExtra("bundle", bundle);

                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        TextView title;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            title = (TextView)itemView.findViewById(R.id.title);
            icon = (ImageView)itemView.findViewById(R.id.icon);
        }
    }

    public static class DashboardItem implements Parcelable {

        private String title;
        private int activityID;
        private int icon;
        private int group;

        private DashboardItem(Bundle bundle) {
            title = bundle.getString("title");
            activityID = bundle.getInt("activity_id");
            icon = bundle.getInt("icon");
            group = bundle.getInt("group");
        }

        public DashboardItem(String title, int icon, int activityID, int group) {
            this.title = title;
            this.icon = icon;
            this.activityID = activityID;
            this.group = group;
        }

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

        public String getTitle() {
            return title;
        }

        public int getActivityID() {
            return activityID;
        }

        public int getIcon() {
            return icon;
        }

        public int getGroup(){
            return group;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putInt("activity_id", activityID);
            bundle.putInt("icon", icon);
            bundle.putInt("group", group);
            dest.writeBundle(bundle);
        }

    }

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

}
