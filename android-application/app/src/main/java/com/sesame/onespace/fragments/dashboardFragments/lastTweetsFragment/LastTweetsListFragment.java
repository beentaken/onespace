package com.sesame.onespace.fragments.dashboardFragments.lastTweetsFragment;

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
import com.sesame.onespace.interfaces.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.utils.connectToServer.MyConnect;
import com.sesame.onespace.utils.date.MyDateConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Thian on 1/12/2559.
 */

public class LastTweetsListFragment extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private List<LastTweetsItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private LastTweetsListAdapter adapter;

    private final String url = "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"; //demo

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //init
        this.items = getArguments().getParcelableArrayList("items");

        //before


        //main
        this.init(inflater, container);

        //after


        return this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        this.initDefaultValue(inflater, container);
        this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        //init
        this.context = getContext();
        this.view = inflater.inflate(R.layout.fragment_dashboard_last_tweets, container, false);

        //before


        //main


        //after

    }

    private void initRecyclerView(){

        //init
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(spacingInPixels);
        //this.items = new ArrayList<LastTweetsListFragment.LastTweetsItem>();
        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.adapter = new LastTweetsListFragment.LastTweetsListAdapter(this.context, this.items);

        //before


        //main
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);

        this.recyclerView.setAdapter(this.adapter);

        this.recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                addRecyclerView();

            }
        });

        //after



    }

    //  private class  -----------------------------------------------------------------------------****private class****

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            //init


            //before


            //main
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space/2;
            outRect.top = space/2;

            //after

        }

    }

    //===========================================================================================================//
    //  METHOD CONNECTION STATUS                                                                    METHOD CONNECTION STATUS
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****



    //===========================================================================================================//
    //  METHOD START RECYCLER VIEW                                                                  METHOD START RECYCLER VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void addRecyclerView(){

        //init


        //before


        //main
        new LastTweetsListFragment.DownloadFilesTask().execute();

        //after

    }

    private void loadData() {

        //init


        //before


        //main
        JSONObject jsonObject = connectToServer();
        JSONArray jsonArray = getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);
                items.add(new LastTweetsItem(String.valueOf(object.get("tweet_screen_name")), MyDateConvert.convertTimeStampToDate(String.valueOf(object.get("tweet_timestamp")), "EEE, dd MMM yyyy HH:mm:ss"), String.valueOf(object.get("tweet_text")), R.drawable.ic_dashboard_twitter));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

        //after

    }

    private JSONObject connectToServer(){

        //init
        JSONObject jsonObject = MyConnect.getJSON(this.url);

        //before


        //main


        //after

        return jsonObject;

    }

    private JSONArray getData(JSONObject jsonObject){

        //init
        JSONArray jsonArray = null;

        //before


        //main
        try {

            jsonArray = jsonObject.getJSONArray("data");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        //after

        return  jsonArray;

    }

    //  private class   ----------------------------------------------------------------------------****private class****

    private class DownloadFilesTask extends AsyncTask<Void, Integer, Boolean> {

        protected Boolean doInBackground(Void ... voids) {

            //init


            //before


            //main
            loadData();

            //after

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            //init


            //before


            //main
            adapter.notifyDataSetChanged();

            //after

        }

    }

    public class LastTweetsListAdapter extends RecyclerView.Adapter<LastTweetsListFragment.ViewHolder> {

        private List<LastTweetsListFragment.LastTweetsItem> items;
        private Context mContext;

        public LastTweetsListAdapter(Context context, List<LastTweetsListFragment.LastTweetsItem> settingItems) {

            //init


            //before


            //main
            this.items = settingItems;
            this.mContext = context;

            //after

        }

        @Override
        public LastTweetsListFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            //init
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_last_tweets_item, null);

            //before


            //main


            //after

            return new LastTweetsListFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LastTweetsListFragment.ViewHolder viewHolder, int i) {

            //init
            final LastTweetsListFragment.LastTweetsItem item = items.get(i);

            String colorStart = "#B0D1D1";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            //before


            //main
            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.tweet_screen_name.setText(item.getTweetScreenName());
            viewHolder.tweet_timestamp.setText(item.getTweetTimestamp());
            viewHolder.tweet_text.setText(item.getTweetText());
            viewHolder.icon.setImageResource(item.getIcon());

            //after

        }

        @Override
        public int getItemCount() {

            //init


            //before


            //main


            //after

            return items.size();
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView tweet_screen_name;
        TextView tweet_timestamp;
        TextView tweet_text;
        ImageView icon;

        ViewHolder(View itemView) {

            //init
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            tweet_screen_name = (TextView)itemView.findViewById(R.id.tweet_screen_name);
            tweet_timestamp = (TextView)itemView.findViewById(R.id.tweet_timestamp);
            tweet_text = (TextView)itemView.findViewById(R.id.tweet_text);
            icon = (ImageView)itemView.findViewById(R.id.icon);

            //before


            //main


            //after
        }
    }

    public static class LastTweetsItem implements Parcelable {

        private String tweet_screen_name;
        private String tweet_timestamp;
        private String tweet_text;
        private int icon;

        private LastTweetsItem(Bundle bundle) {

            //init
            tweet_screen_name = bundle.getString("tweet_screen_name");
            tweet_timestamp = bundle.getString("tweet_timestamp");
            tweet_text = bundle.getString("tweet_text");
            icon = bundle.getInt("icon");

            //before


            //main


            //after

        }

        public LastTweetsItem(String tweet_screen_name, String tweet_timestamp, String tweet_text, int icon) {

            //init
            this.tweet_screen_name = tweet_screen_name;
            this.tweet_timestamp = tweet_timestamp;
            this.tweet_text = tweet_text;
            this.icon = icon;

            //before


            //main


            //after

        }

        public static final Creator<LastTweetsListFragment.LastTweetsItem> CREATOR = new Creator<LastTweetsListFragment.LastTweetsItem>() {
            @Override
            public LastTweetsListFragment.LastTweetsItem createFromParcel(Parcel in) {
                return new LastTweetsListFragment.LastTweetsItem(in.readBundle());
            }

            @Override
            public LastTweetsListFragment.LastTweetsItem[] newArray(int size) {
                return new LastTweetsListFragment.LastTweetsItem[size];
            }
        };

        public String getTweetScreenName() {
            return tweet_screen_name;
        }

        public String getTweetTimestamp() {
            return tweet_timestamp;
        }

        public String getTweetText() {
            return tweet_text;
        }

        public int getIcon() {
            return icon;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            //init
            Bundle bundle = new Bundle();

            //before


            //main
            bundle.putString("tweet_screen_name", tweet_screen_name);
            bundle.putString("tweet_timestamp", tweet_timestamp);
            bundle.putString("tweet_text", tweet_text);
            bundle.putInt("icon", icon);
            dest.writeBundle(bundle);

            //after


        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

    //    ************2/12/2016 by Thianchai************

    //        //add space between items.
//        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
//        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(spacingInPixels);
//        //

//        this.linearLayoutManager = new LinearLayoutManager(this.context);
//        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
//        this.recyclerView.setLayoutManager(linearLayoutManager);
//        this.recyclerView.addItemDecoration(spacesItemDecoration);
//        this.recyclerView.setHasFixedSize(true);
//
////        this.adapter = new TweetsListActivity.TweetsListAdapter(this.context, this.items);
//        this.recyclerView.setAdapter(this.adapter);
//
//        this.recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int current_page) {
//
//                addRecyclerView();
//
//            }
//        });

    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------

}
