package com.sesame.onespace.activities.dashboardActivity;

import android.content.Context;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.models.dashboard.Flickr;
import com.sesame.onespace.models.dashboard.MyLink;
import com.sesame.onespace.utils.JSONParser;
import com.sesame.onespace.views.DividerItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thian on 8/11/2559.
 */

public class FlickrListActivity extends DashboardActivity{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private FlickrListActivity.FlickrListAdapter adapter;

    private Context context;
    private View view;

    private JSONParser jsonParser;

    private final String url = "http://172.29.33.45:11090/data/?tabid=0&type=flickr&vloc=www.marinabaysands.com%2F%3F&vlocsha1=9ae3562a174ccf1de97ad7939d39b505075bdc7a&limit=10"; //demo

    private MyLink startMyLink;
    private MyLink currentMyLink;
    private MyLink nextMyLink;
    private List<FlickrListActivity.FlickrItem> items;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

    }

    private void initActivity(){

        setContentView(R.layout.activity_flickr_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initToolbar(){

        AppBarLayout item = (AppBarLayout) findViewById(R.id.layout);
        item.setBackgroundColor(Color.parseColor("#3F474A"));

        this.mToolBar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        this.mToolBar.setTitle("Flickr");
        this.mToolBar.setSubtitle("test sub title");
        this.mToolBar.setLogo(R.drawable.ic_dashboard_flickr);
        this.mToolBar.setBackgroundColor(Color.parseColor("#3F474A"));
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
        FlickrListActivity.SpacesItemDecoration spacesItemDecoration = new FlickrListActivity.SpacesItemDecoration(spacingInPixels);
        //

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.addItemDecoration(spacesItemDecoration);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this.context, 1000, 0));

        this.adapter = new FlickrListActivity.FlickrListAdapter(this.context, this.items);
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

        this.items = new ArrayList<FlickrListActivity.FlickrItem>();

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
                createFlickr(String.valueOf(object.get("dom")), String.valueOf(object.get("title")), String.valueOf(object.get("url")));

            } catch (JSONException e) {

                e.printStackTrace();

            }

            String title = ((Flickr)currentMyLink.getObject()).getTitle();
            String url = ((Flickr)currentMyLink.getObject()).getUrl();

            items.add(new FlickrListActivity.FlickrItem(title, url));

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        List<NameValuePair> params =  new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tabid", "0"));
        params.add(new BasicNameValuePair("type", "flickr"));
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

    private void createFlickr(String dom,String title, String url){

        Flickr flickr = new Flickr(dom, title, url);

        if (this.startMyLink == null){

            this.startMyLink = new MyLink(flickr);
            this.currentMyLink = this.startMyLink;

        }
        else{

            this.nextMyLink = new MyLink(flickr);
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

    public class FlickrListAdapter extends RecyclerView.Adapter<FlickrListActivity.ViewHolder> {

        private List<FlickrListActivity.FlickrItem> items;
        private Context mContext;

        public FlickrListAdapter(Context context, List<FlickrListActivity.FlickrItem> items) {
            this.items = items;
            this.mContext = context;
        }

        @Override
        public FlickrListActivity.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_flickr_item, null);
            return new FlickrListActivity.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FlickrListActivity.ViewHolder viewHolder, int i) {

            final FlickrListActivity.FlickrItem item = items.get(i);

            String colorStart = "#A2DCF9";
            String colorEnd = "#FDFADB";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(item.getTitle());

            URL newurl = null;
            try {
                newurl = new URL("http://" + item.getUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                viewHolder.url.setImageBitmap(BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        TextView title;
        ImageView url;

        ViewHolder(View itemView) {
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            title = (TextView)itemView.findViewById(R.id.title);
            url = (ImageView)itemView.findViewById(R.id.url);
        }
    }

    public static class FlickrItem implements Parcelable {

        private String title;
        private String url;

        private FlickrItem(Bundle bundle) {
            title = bundle.getString("title");
            url = bundle.getString("url");
        }

        public FlickrItem(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public static final Creator<FlickrListActivity.FlickrItem> CREATOR = new Creator<FlickrListActivity.FlickrItem>() {
            @Override
            public FlickrListActivity.FlickrItem createFromParcel(Parcel in) {
                return new FlickrListActivity.FlickrItem(in.readBundle());
            }

            @Override
            public FlickrListActivity.FlickrItem[] newArray(int size) {
                return new FlickrListActivity.FlickrItem[size];
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
