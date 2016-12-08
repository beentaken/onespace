package com.sesame.onespace.activities.dashboardActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.models.dashboard.MyLink;
import com.sesame.onespace.models.dashboard.Tweet;
import com.sesame.onespace.utils.connectToServer.JSONParser;
import com.sesame.onespace.interfaces.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.views.DividerItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thian on 6/11/2559.
 */

public class TweetsListActivity extends DashboardActivity{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private JSONParser jsonParser;

    private MyLink startMyLink;
    private MyLink currentMyLink;
    private MyLink nextMyLink;
    private List<TweetsListActivity.TweetsItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private TweetsListAdapter adapter;

    private final String url = "http://172.29.33.45:11090/data/?tabid=0&type=twitter&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"; //demo

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

        this.initActivity();
        this.initToolbar();
        this.initRecyclerView();

    }

    private void initActivity(){

        setContentView(R.layout.activity_tweets_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initToolbar(){

        this.mToolBar = (Toolbar) findViewById(R.id.toolbar);
        this.mToolBar.setTitle("Last Tweets");
        this.mToolBar.setSubtitle("test sub title");
        this.mToolBar.setLogo(R.drawable.ic_dashboard_twitter);
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

    private void initRecyclerView(){

        this.initConnection();
        this.initItem();

        //add space between items.
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        SpacesItemDecoration spacesItemDecoration = new SpacesItemDecoration(spacingInPixels);
        //

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this.context, 1000, 0)); //This is length of underline record : 76,this is a original number by chongos

        this.adapter = new TweetsListActivity.TweetsListAdapter(this.context, this.items);
        this.recyclerView.setAdapter(this.adapter);

        this.recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                addRecyclerView();

            }
        });

    }

    private void initConnection(){

        this.jsonParser = new JSONParser();

    }

    private void initItem(){

        this.startMyLink = null;
        this.currentMyLink = null;
        this.nextMyLink = null;

        this.items = new ArrayList<TweetsListActivity.TweetsItem>();

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
        JSONArray jsonArray = getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);
                createTweet(String.valueOf(object.get("tweet_timestamp")), String.valueOf(object.get("tweet_screen_name")), String.valueOf(object.get("tweet_text")));

            } catch (JSONException e) {

                e.printStackTrace();

            }

            String tweetScreenName = ((Tweet)currentMyLink.getObject()).getTweet_screen_name();
            String tweetTimestamp = " tweeted on " +  getDate(((Tweet)currentMyLink.getObject()).getTweet_timestamp()) + " GMT: ";
            String tweetsText = ((Tweet)currentMyLink.getObject()).getTweet_text();

            items.add(new TweetsListActivity.TweetsItem(tweetScreenName, tweetTimestamp, tweetsText, R.drawable.ic_dashboard_twitter, 1, 0));

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        List<NameValuePair> params =  new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tabid", "0"));
        params.add(new BasicNameValuePair("type", "twitter"));
        params.add(new BasicNameValuePair("vloc", "www.marinabaysands.com%2F%3F"));
        params.add(new BasicNameValuePair("vlocsha1", "9ae3562a174ccf1de97ad7939d39b505075bdc7a"));
        params.add(new BasicNameValuePair("limit", "10"));

        JSONObject jsonObject = this.jsonParser.makeHttpRequest(this.url, "GET");

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

    private void createTweet(String tweet_timestamp,String tweet_screen_name, String tweet_text){

        Tweet tweet = new Tweet(tweet_timestamp, tweet_screen_name, tweet_text);

        if (this.startMyLink == null){

            this.startMyLink = new MyLink(tweet);
            this.currentMyLink = this.startMyLink;

        }
        else{

            this.nextMyLink = new MyLink(tweet);
            this.currentMyLink.setNextLink(this.nextMyLink);
            this.currentMyLink = this.nextMyLink;
            this.nextMyLink = null;

        }

    }

    private String getDate(String timeStampStr){

        try{

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
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

    public class TweetsListAdapter extends RecyclerView.Adapter<TweetsListActivity.ViewHolder> {

        private List<TweetsListActivity.TweetsItem> items;
        private Context mContext;

        public TweetsListAdapter(Context context, List<TweetsListActivity.TweetsItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public TweetsListActivity.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_last_tweets_item2, null);
            return new TweetsListActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TweetsListActivity.ViewHolder viewHolder, int i) {

            final TweetsListActivity.TweetsItem item = items.get(i);

            String colorStart = "#B0D1D1";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.tweet_screen_name.setText(item.getTweetScreenName());
            viewHolder.tweet_timestamp.setText(item.getTweetTimestamp());
            viewHolder.tweet_text.setText(item.getTweetText());
            viewHolder.icon.setImageResource(item.getIcon());

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
        TextView tweet_screen_name;
        TextView tweet_timestamp;
        TextView tweet_text;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            tweet_screen_name = (TextView)itemView.findViewById(R.id.tweet_screen_name);
            tweet_timestamp = (TextView)itemView.findViewById(R.id.tweet_timestamp);
            tweet_text = (TextView)itemView.findViewById(R.id.tweet_text);
            icon = (ImageView)itemView.findViewById(R.id.icon);
        }
    }

    public static class TweetsItem implements Parcelable {

        private int index;
        private String tweet_screen_name;
        private String tweet_timestamp;
        private String tweet_text;
        private int icon;
        private int group;

        private TweetsItem(Bundle bundle) {
            tweet_screen_name = bundle.getString("tweet_screen_name");
            tweet_timestamp = bundle.getString("tweet_timestamp");
            tweet_text = bundle.getString("tweet_text");
            index = bundle.getInt("index");
            icon = bundle.getInt("icon");
            group = bundle.getInt("group");
        }

        public TweetsItem(String tweet_screen_name, String tweet_timestamp, String tweet_text, int icon, int index, int group) {
            this.tweet_screen_name = tweet_screen_name;
            this.tweet_timestamp = tweet_timestamp;
            this.tweet_text = tweet_text;
            this.icon = icon;
            this.index = index;
            this.group = group;
        }

        public static final Creator<TweetsListActivity.TweetsItem> CREATOR = new Creator<TweetsListActivity.TweetsItem>() {
            @Override
            public TweetsListActivity.TweetsItem createFromParcel(Parcel in) {
                return new TweetsListActivity.TweetsItem(in.readBundle());
            }

            @Override
            public TweetsListActivity.TweetsItem[] newArray(int size) {
                return new TweetsListActivity.TweetsItem[size];
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

        public int getIndex() {
            return index;
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
            bundle.putString("tweet_screen_name", tweet_screen_name);
            bundle.putString("tweet_timestamp", tweet_timestamp);
            bundle.putString("tweet_text", tweet_text);
            bundle.putInt("index", index);
            bundle.putInt("icon", icon);
            bundle.putInt("group", group);
            dest.writeBundle(bundle);
        }

    }

}
