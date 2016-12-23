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

public class YoutubeListFragment extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private static Context context;
    private static Activity activity;

    private View view;

    private List<YoutubeListFragment.YoutubeItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private YoutubeListFragment.YoutubeListAdapter adapter;

    private String url;

    private static final String API_KEY = "AIzaSyAK7wfA2dCabaZCU64VvCtJ4ghZjXdTmg8";

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //init
        this.url = getArguments().getString("url");
        this.items = getArguments().getParcelableArrayList("items");

        //before


        //main
        this.init(inflater, container);

        //after

        context = getContext();
        activity = getActivity();

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
        this.view = inflater.inflate(R.layout.fragment_dashboard_youtube, container, false);

        //before


        //main


        //after

    }

    private void initRecyclerView(){

        //init
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        YoutubeListFragment.SpacesItemDecoration spacesItemDecoration = new YoutubeListFragment.SpacesItemDecoration(spacingInPixels);
        //this.items = new ArrayList<LastTweetsListFragment.LastTweetsItem>();
        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.adapter = new YoutubeListFragment.YoutubeListAdapter(this.context, this.items);

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
        new YoutubeListFragment.DownloadFilesTask().execute();

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
                items.add(new YoutubeListFragment.YoutubeItem(String.valueOf(object.get("title")), String.valueOf(object.get("url"))));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

        //after

    }

    private JSONObject connectToServer(){

        //init
        JSONObject jsonObject = Connection.getJSON(this.url);

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

    public class YoutubeListAdapter extends RecyclerView.Adapter<YoutubeListFragment.ViewHolder> {

        private List<YoutubeListFragment.YoutubeItem> items;
        private Context mContext;

        public YoutubeListAdapter(Context context, List<YoutubeListFragment.YoutubeItem> items) {
            this.items = items;
            this.mContext = context;
        }

        @Override
        public YoutubeListFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_youtube_item, null);

            final YoutubeListFragment.ViewHolder viewHolder = new YoutubeListFragment.ViewHolder(view);

            viewHolder.url.initialize(API_KEY, viewHolder);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final YoutubeListFragment.ViewHolder viewHolder, int i) {

            final YoutubeListFragment.YoutubeItem item = items.get(i);

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

        public static final Creator<YoutubeListFragment.YoutubeItem> CREATOR = new Creator<YoutubeListFragment.YoutubeItem>() {
            @Override
            public YoutubeListFragment.YoutubeItem createFromParcel(Parcel in) {
                return new YoutubeListFragment.YoutubeItem(in.readBundle());
            }

            @Override
            public YoutubeListFragment.YoutubeItem[] newArray(int size) {
                return new YoutubeListFragment.YoutubeItem[size];
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

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
