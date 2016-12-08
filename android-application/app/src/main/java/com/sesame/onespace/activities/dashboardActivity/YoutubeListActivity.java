package com.sesame.onespace.activities.dashboardActivity;

import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.sesame.onespace.R;
import com.sesame.onespace.models.dashboard.MyLink;
import com.sesame.onespace.models.dashboard.Youtube;
import com.sesame.onespace.utils.connectToServer.JSONParser;
import com.sesame.onespace.interfaces.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.views.DividerItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Thian on 8/11/2559.
 */

public class YoutubeListActivity extends DashboardActivity {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static Context context;
    private static Activity activity;
    private View view;

    private JSONParser jsonParser;

    private MyLink startMyLink;
    private MyLink currentMyLink;
    private MyLink nextMyLink;
    private List<YoutubeListActivity.YoutubeItem> items;
    private static final String API_KEY = "AIzaSyAK7wfA2dCabaZCU64VvCtJ4ghZjXdTmg8";

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private YoutubeListActivity.YoutubeListAdapter adapter;

    private final String url = "http://172.29.33.45:11090/data/?tabid=0&type=youtube&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"; //demo

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.init();

        this.addRecyclerView();

        this.context = getApplicationContext();
        activity = this;

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        this.initActivity();
        this.initToolbar();
        this.initRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

    }

    private void initActivity(){

        setContentView(R.layout.activity_youtube_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initToolbar(){

        AppBarLayout item = (AppBarLayout) findViewById(R.id.layout);
        item.setBackgroundColor(Color.parseColor("#D40000"));

        this.mToolBar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        this.mToolBar.setTitle("Youtube");
        this.mToolBar.setSubtitle("test sub title");
        this.mToolBar.setLogo(R.drawable.ic_dashboard_youtube);
        this.mToolBar.setBackgroundColor(Color.parseColor("#D40000"));
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
        YoutubeListActivity.SpacesItemDecoration spacesItemDecoration = new YoutubeListActivity.SpacesItemDecoration(spacingInPixels);
        //

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this.context, 1000, 0));

        this.adapter = new YoutubeListActivity.YoutubeListAdapter(this.context, this.items);
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

        this.items = new ArrayList<YoutubeListActivity.YoutubeItem>();

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
                createYoutube(String.valueOf(object.get("dom")), String.valueOf(object.get("title")), String.valueOf(object.get("url")));

            } catch (JSONException e) {

                e.printStackTrace();

            }

            String title = ((Youtube)currentMyLink.getObject()).getTitle();
            String url = ((Youtube)currentMyLink.getObject()).getUrl();

            items.add(new YoutubeListActivity.YoutubeItem(title, url));

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        List<NameValuePair> params =  new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tabid", "0"));
        params.add(new BasicNameValuePair("type", "youtube"));
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

    private void createYoutube(String dom,String title, String url){

        Youtube youtube = new Youtube(dom, title, url);

        if (this.startMyLink == null){

            this.startMyLink = new MyLink(youtube);
            this.currentMyLink = this.startMyLink;

        }
        else{

            this.nextMyLink = new MyLink(youtube);
            this.currentMyLink.setNextLink(this.nextMyLink);
            this.currentMyLink = this.nextMyLink;
            this.nextMyLink = null;

        }

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

    public class YoutubeListAdapter extends RecyclerView.Adapter<YoutubeListActivity.ViewHolder> {

        private List<YoutubeListActivity.YoutubeItem> items;
        private Context mContext;

        public YoutubeListAdapter(Context context, List<YoutubeListActivity.YoutubeItem> items) {
            this.items = items;
            this.mContext = context;
        }

        @Override
        public YoutubeListActivity.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_youtube_item, null);

            final YoutubeListActivity.ViewHolder viewHolder = new YoutubeListActivity.ViewHolder(view);

            viewHolder.url.initialize(API_KEY, viewHolder);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final YoutubeListActivity.ViewHolder viewHolder, int i) {

            final YoutubeListActivity.YoutubeItem item = items.get(i);

            String colorStart = "#D4D4D4";
            String colorEnd = "#FFFFFF";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(item.getTitle());

            //I create API and add libraly

            viewHolder.setVideoID(getYoutubeID(item.getUrl()));

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String getYoutubeID(String url){

            String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(url);

            if(matcher.find()){
                return matcher.group();
            }

            return "";

        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, YouTubeThumbnailView.OnInitializedListener{
        CardView rootView;
        TextView title;
        YouTubeThumbnailView url;
        ImageView playButton;
        RelativeLayout relativeLayoutOverYouTubeThumbnailView;

        String videoID;
        ViewHolder viewHolder;
        YouTubeThumbnailLoader youTubeThumbnailLoader;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            title = (TextView)itemView.findViewById(R.id.title);
            url = (YouTubeThumbnailView)itemView.findViewById(R.id.url);
            playButton=(ImageView)itemView.findViewById(R.id.btnYoutube_player);
            playButton.setOnClickListener(this);
            relativeLayoutOverYouTubeThumbnailView = (RelativeLayout) itemView.findViewById(R.id.relativeLayout_over_youtube_thumbnail);

            videoID = null;
            viewHolder = this;
            youTubeThumbnailLoader = null;
        }

        @Override
        public void onClick(View v) {
            //Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, API_KEY, listKeyID.get(getLayoutPosition() + ""));
            //http://stackoverflow.com/questions/22285129/in-listview-activity-has-leaked-serviceconnection-youtube-player-internal-tha

            try{

                Intent intent = YouTubeStandalonePlayer.createVideoIntent(activity, API_KEY, videoID);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
            catch (Exception e){



            }

        }

        public void setVideoID(String videoID){

            this.videoID = videoID;

            if (this.youTubeThumbnailLoader != null){

                this.youTubeThumbnailLoader.setVideo(this.videoID);

            }

        }

        @Override
        public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {

            YouTubeThumbnailLoader.OnThumbnailLoadedListener  onThumbnailLoadedListener = new YouTubeThumbnailLoader.OnThumbnailLoadedListener(){
                @Override
                public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                }

                @Override
                public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                    youTubeThumbnailView.setVisibility(View.VISIBLE);
                    viewHolder.relativeLayoutOverYouTubeThumbnailView.setVisibility(View.VISIBLE);
                }
            };

            this.youTubeThumbnailLoader = youTubeThumbnailLoader;
            this.youTubeThumbnailLoader.setOnThumbnailLoadedListener(onThumbnailLoadedListener);
            this.youTubeThumbnailLoader.setVideo(videoID);

        }

        @Override
        public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

        }
    }

    public static class YoutubeItem implements Parcelable {

        private String title;
        private String url;

        private YoutubeItem(Bundle bundle) {
            title = bundle.getString("title");
            url = bundle.getString("url");
        }

        public YoutubeItem(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public static final Creator<YoutubeListActivity.YoutubeItem> CREATOR = new Creator<YoutubeListActivity.YoutubeItem>() {
            @Override
            public YoutubeListActivity.YoutubeItem createFromParcel(Parcel in) {
                return new YoutubeListActivity.YoutubeItem(in.readBundle());
            }

            @Override
            public YoutubeListActivity.YoutubeItem[] newArray(int size) {
                return new YoutubeListActivity.YoutubeItem[size];
            }
        };

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("url", url);
            dest.writeBundle(bundle);
        }

    }
}
