package com.sesame.onespace.fragments.dashboardFragments.youtubeFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.sesame.onespace.R;
import com.sesame.onespace.interfaces.recyclerViewInterfaces.EndlessRecyclerOnScrollListener;
import com.sesame.onespace.utils.connect.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Thian on 19/12/2559.
 */

public final class YoutubeFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static Context context;
    private static Activity activity;

    private View view;

    private String url;

    private YoutubeFragment.YoutubeAdapter adapter;
    private List<YoutubeFragment.YoutubeItem> items;

    private static final String API_KEY = "AIzaSyAK7wfA2dCabaZCU64VvCtJ4ghZjXdTmg8";

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        YoutubeFragment.this.init(inflater, container);

        YoutubeFragment.context = YoutubeFragment.this.getContext();
        YoutubeFragment.activity = YoutubeFragment.this.getActivity();

        return YoutubeFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        YoutubeFragment.this.initDefaultValue(inflater, container);
        YoutubeFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        YoutubeFragment.this.context = YoutubeFragment.this.getContext();
        YoutubeFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_youtube, container, false);

        YoutubeFragment.this.url = YoutubeFragment.this.getArguments().getString("url");
        YoutubeFragment.this.items = YoutubeFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        YoutubeFragment.SpacesItemDecoration spacesItemDecoration = new YoutubeFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(YoutubeFragment.this.context);
        YoutubeFragment.this.adapter = new YoutubeFragment.YoutubeAdapter(YoutubeFragment.this.context, YoutubeFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) YoutubeFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(YoutubeFragment.this.adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                YoutubeFragment.this.addRecyclerView();

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

        new YoutubeFragment.DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = connectToServer();
        JSONArray jsonArray = getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);
                YoutubeFragment.this.items.add(new YoutubeFragment.YoutubeItem(String.valueOf(object.get("title")), String.valueOf(object.get("url"))));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        JSONObject jsonObject = Connection.getJSON(YoutubeFragment.this.url);

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

            YoutubeFragment.this.loadData();

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            YoutubeFragment.this.adapter.notifyDataSetChanged();

        }

    }

    public final class YoutubeAdapter
            extends RecyclerView.Adapter<YoutubeFragment.ViewHolder> {

        private List<YoutubeFragment.YoutubeItem> items;
        private Context mContext;

        public YoutubeAdapter(Context context, List<YoutubeFragment.YoutubeItem> items) {
            YoutubeAdapter.this.items = items;
            YoutubeAdapter.this.mContext = context;
        }

        @Override
        public YoutubeFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_youtube, null);

            final YoutubeFragment.ViewHolder viewHolder = new YoutubeFragment.ViewHolder(view);

            viewHolder.url.initialize(API_KEY, viewHolder);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final YoutubeFragment.ViewHolder viewHolder, int i) {

            final YoutubeFragment.YoutubeItem item = YoutubeAdapter.this.items.get(i);

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
            return YoutubeAdapter.this.items.size();
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

    private final static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, YouTubeThumbnailView.OnInitializedListener{

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
            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.title = (TextView)itemView.findViewById(R.id.title);
            ViewHolder.this.url = (YouTubeThumbnailView)itemView.findViewById(R.id.url);
            ViewHolder.this.playButton=(ImageView)itemView.findViewById(R.id.btnYoutube_player);
            ViewHolder.this.playButton.setOnClickListener(this);
            ViewHolder.this.relativeLayoutOverYouTubeThumbnailView = (RelativeLayout) itemView.findViewById(R.id.relativeLayout_over_youtube_thumbnail);

            ViewHolder.this.videoID = null;
            ViewHolder.this.viewHolder = this;
            ViewHolder.this.youTubeThumbnailLoader = null;

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

            ViewHolder.this.videoID = videoID;

            if (ViewHolder.this.youTubeThumbnailLoader != null){

                ViewHolder.this.youTubeThumbnailLoader.setVideo(ViewHolder.this.videoID);

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
                    ViewHolder.this.viewHolder.relativeLayoutOverYouTubeThumbnailView.setVisibility(View.VISIBLE);
                }
            };

            ViewHolder.this.youTubeThumbnailLoader = youTubeThumbnailLoader;
            ViewHolder.this.youTubeThumbnailLoader.setOnThumbnailLoadedListener(onThumbnailLoadedListener);
            ViewHolder.this.youTubeThumbnailLoader.setVideo(ViewHolder.this.videoID);

        }

        @Override
        public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

        }
    }

    public final static class YoutubeItem
            implements Parcelable {

        public static final Creator<YoutubeFragment.YoutubeItem> CREATOR = new Creator<YoutubeFragment.YoutubeItem>() {
            @Override
            public YoutubeFragment.YoutubeItem createFromParcel(Parcel in) {
                return new YoutubeFragment.YoutubeItem(in.readBundle());
            }

            @Override
            public YoutubeFragment.YoutubeItem[] newArray(int size) {
                return new YoutubeFragment.YoutubeItem[size];
            }
        };

        private String title;
        private String url;

        private YoutubeItem(Bundle bundle) {
            YoutubeItem.this.title = bundle.getString("title");
            YoutubeItem.this.url = bundle.getString("url");
        }

        public YoutubeItem(String title, String url) {
            YoutubeItem.this.title = title;
            YoutubeItem.this.url = url;
        }

        public String getTitle() {
            return YoutubeItem.this.title;
        }

        public String getUrl() {
            return YoutubeItem.this.url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("title", YoutubeItem.this.title);
            bundle.putString("url", YoutubeItem.this.url);
            dest.writeBundle(bundle);
        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
