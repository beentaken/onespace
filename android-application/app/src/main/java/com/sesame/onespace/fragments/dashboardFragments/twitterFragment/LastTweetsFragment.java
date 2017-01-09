package com.sesame.onespace.fragments.dashboardFragments.twitterFragment;

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
import com.sesame.onespace.utils.date.DateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Thian on 1/12/2559.
 */

public final class LastTweetsFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private LastTweetsFragment.LastTweetsAdapter adapter;
    private List<LastTweetsItem> items;

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LastTweetsFragment.this.init(inflater, container);

        return LastTweetsFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        LastTweetsFragment.this.initDefaultValue(inflater, container);
        LastTweetsFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        LastTweetsFragment.this.context = LastTweetsFragment.this.getContext();
        LastTweetsFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_twitter, container, false);

        LastTweetsFragment.this.url = LastTweetsFragment.this.getArguments().getString("url");
        LastTweetsFragment.this.items = LastTweetsFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        LastTweetsFragment.SpacesItemDecoration spacesItemDecoration = new LastTweetsFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LastTweetsFragment.this.context);
        LastTweetsFragment.this.adapter = new LastTweetsFragment.LastTweetsAdapter(LastTweetsFragment.this.context, LastTweetsFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) LastTweetsFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(LastTweetsFragment.this.adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                LastTweetsFragment.this.addRecyclerView();

            }
        });

    }

    //  private class  -----------------------------------------------------------------------------****private class****

    public final class SpacesItemDecoration
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

        new LastTweetsFragment.DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = connectToServer();
        JSONArray jsonArray = getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);
                LastTweetsFragment.this.items.add(new LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), DateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        JSONObject jsonObject = Connection.getJSON(LastTweetsFragment.this.url);

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

            LastTweetsFragment.this.loadData();

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            LastTweetsFragment.this.adapter.notifyDataSetChanged();

        }

    }

    public final class LastTweetsAdapter
            extends RecyclerView.Adapter<LastTweetsFragment.ViewHolder> {

        private List<LastTweetsFragment.LastTweetsItem> items;
        private Context mContext;

        public LastTweetsAdapter(Context context, List<LastTweetsFragment.LastTweetsItem> settingItems) {

            LastTweetsAdapter.this.items = settingItems;
            LastTweetsAdapter.this.mContext = context;

        }

        @Override
        public LastTweetsFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_twitter, null);

            return new LastTweetsFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LastTweetsFragment.ViewHolder viewHolder, int i) {

            final LastTweetsFragment.LastTweetsItem item = LastTweetsAdapter.this.items.get(i);

            String colorStart = "#B0D1D1";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.tweet_screen_name.setText(item.getTweetScreenName());
            viewHolder.tweet_timestamp.setText(item.getTweetTimestamp());
            viewHolder.tweet_text.setText(item.getTweetText());
            viewHolder.icon.setImageResource(item.getIcon());

        }

        @Override
        public int getItemCount() {
            return LastTweetsAdapter.this.items.size();
        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView tweet_screen_name;
        TextView tweet_timestamp;
        TextView tweet_text;
        ImageView icon;

        ViewHolder(View itemView) {

            super(itemView);
            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.tweet_screen_name = (TextView)itemView.findViewById(R.id.tweet_screen_name);
            ViewHolder.this.tweet_timestamp = (TextView)itemView.findViewById(R.id.tweet_timestamp);
            ViewHolder.this.tweet_text = (TextView)itemView.findViewById(R.id.tweet_text);
            ViewHolder.this.icon = (ImageView)itemView.findViewById(R.id.icon);

        }
    }

    public final static class LastTweetsItem
            implements Parcelable {

        public static final Creator<LastTweetsFragment.LastTweetsItem> CREATOR = new Creator<LastTweetsFragment.LastTweetsItem>() {
            @Override
            public LastTweetsFragment.LastTweetsItem createFromParcel(Parcel in) {
                return new LastTweetsFragment.LastTweetsItem(in.readBundle());
            }

            @Override
            public LastTweetsFragment.LastTweetsItem[] newArray(int size) {
                return new LastTweetsFragment.LastTweetsItem[size];
            }
        };

        private String tweet_screen_name;
        private String tweet_timestamp;
        private String tweet_text;
        private int icon;

        private LastTweetsItem(Bundle bundle) {

            LastTweetsItem.this.tweet_screen_name = bundle.getString("tweet_screen_name");
            LastTweetsItem.this.tweet_timestamp = bundle.getString("tweet_timestamp");
            LastTweetsItem.this.tweet_text = bundle.getString("tweet_text");
            LastTweetsItem.this.icon = bundle.getInt("icon");

        }

        public LastTweetsItem(String tweet_screen_name, String tweet_timestamp, String tweet_text, int icon) {

            LastTweetsItem.this.tweet_screen_name = tweet_screen_name;
            LastTweetsItem.this.tweet_timestamp = tweet_timestamp;
            LastTweetsItem.this.tweet_text = tweet_text;
            LastTweetsItem.this.icon = icon;

        }

        public String getTweetScreenName() {
            return LastTweetsItem.this.tweet_screen_name;
        }

        public String getTweetTimestamp() {
            return LastTweetsItem.this.tweet_timestamp;
        }

        public String getTweetText() {
            return LastTweetsItem.this.tweet_text;
        }

        public int getIcon() {
            return LastTweetsItem.this.icon;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            Bundle bundle = new Bundle();
            bundle.putString("tweet_screen_name", LastTweetsItem.this.tweet_screen_name);
            bundle.putString("tweet_timestamp", LastTweetsItem.this.tweet_timestamp);
            bundle.putString("tweet_text", LastTweetsItem.this.tweet_text);
            bundle.putInt("icon", LastTweetsItem.this.icon);
            dest.writeBundle(bundle);

        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
