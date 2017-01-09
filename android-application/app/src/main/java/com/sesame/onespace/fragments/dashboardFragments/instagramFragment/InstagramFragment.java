package com.sesame.onespace.fragments.dashboardFragments.instagramFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Thian on 19/12/2559.
 */

public final class InstagramFragment
        extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private String url;

    private InstagramFragment.InstagramAdapter adapter;
    private List<InstagramFragment.InstagramItem> items;

    //===========================================================================================================//
    //  FRAGMENT LIFECYCLE (MAIN BLOCK)                                                             FRAGMENT LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InstagramFragment.this.init(inflater, container);

        return  InstagramFragment.this.view;
    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(LayoutInflater inflater, ViewGroup container){

        InstagramFragment.this.initDefaultValue(inflater, container);
        InstagramFragment.this.initRecyclerView();

    }

    private void initDefaultValue(LayoutInflater inflater, ViewGroup container){

        InstagramFragment.this.context = InstagramFragment.this.getContext();
        InstagramFragment.this.view = inflater.inflate(R.layout.fragment_dashboard_instagram, container, false);

        InstagramFragment.this.url = InstagramFragment.this.getArguments().getString("url");
        InstagramFragment.this.items = InstagramFragment.this.getArguments().getParcelableArrayList("items");

    }

    private void initRecyclerView(){

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        InstagramFragment.SpacesItemDecoration spacesItemDecoration = new InstagramFragment.SpacesItemDecoration(spacingInPixels);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InstagramFragment.this.context);
        InstagramFragment.this.adapter = new InstagramFragment.InstagramAdapter(InstagramFragment.this.context, InstagramFragment.this.items);

        RecyclerView recyclerView = (RecyclerView) InstagramFragment.this.view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(spacesItemDecoration);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(InstagramFragment.this.adapter);

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {

                InstagramFragment.this.addRecyclerView();

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

        new InstagramFragment.DownloadFilesTask().execute();

    }

    private void loadData() {

        JSONObject jsonObject = connectToServer();
        JSONArray jsonArray = getData(jsonObject);

        int length = jsonArray.length();
        int index = 0;

        while (index < length){

            try {

                JSONObject object = jsonArray.getJSONObject(index);

                URL newurl = null;
                try {
                    newurl = new URL("http://" + String.valueOf(object.get("url")));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                InstagramFragment.this.items.add(new InstagramFragment.InstagramItem(String.valueOf(object.get("title")), bitmap));


            } catch (JSONException e) {

                e.printStackTrace();

            }

            index = index + 1;

        }

    }

    private JSONObject connectToServer(){

        JSONObject jsonObject = Connection.getJSON(InstagramFragment.this.url);

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

            InstagramFragment.this.loadData();

            return true;

        }

        protected void onProgressUpdate(Integer ... progress) {



        }

        protected void onPostExecute(Boolean result) {

            InstagramFragment.this.adapter.notifyDataSetChanged();

        }

    }

    public final class InstagramAdapter
            extends RecyclerView.Adapter<InstagramFragment.ViewHolder> {

        private List<InstagramFragment.InstagramItem> items;
        private Context mContext;

        public InstagramAdapter(Context context, List<InstagramFragment.InstagramItem> settingItems) {

            InstagramAdapter.this.items = settingItems;
            InstagramAdapter.this.mContext = context;

        }

        @Override
        public InstagramFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_instagram, null);

            return new InstagramFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(InstagramFragment.ViewHolder viewHolder, int i) {

            final InstagramFragment.InstagramItem item = InstagramAdapter.this.items.get(i);

            String colorStart = "#A2DCF9";
            String colorEnd = "#FDFADB";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(item.getTitle());

            viewHolder.url.setImageBitmap(item.getBitmap());

        }

        @Override
        public int getItemCount() {
            return InstagramAdapter.this.items.size();
        }

    }

    private final static class ViewHolder
            extends RecyclerView.ViewHolder {

        CardView rootView;
        TextView title;
        ImageView url;

        ViewHolder(View itemView) {

            super(itemView);
            ViewHolder.this.rootView = (CardView)itemView.findViewById(R.id.root_view);
            ViewHolder.this.title = (TextView)itemView.findViewById(R.id.title);
            ViewHolder.this.url = (ImageView)itemView.findViewById(R.id.url);

        }
    }

    public final static class InstagramItem
            implements Parcelable {

        public static final Creator<InstagramFragment.InstagramItem> CREATOR = new Creator<InstagramFragment.InstagramItem>() {
            @Override
            public InstagramFragment.InstagramItem createFromParcel(Parcel in) {
                return new InstagramFragment.InstagramItem(in.readBundle());
            }

            @Override
            public InstagramFragment.InstagramItem[] newArray(int size) {
                return new InstagramFragment.InstagramItem[size];
            }
        };

        private String title;
        private Bitmap bitmap;

        private InstagramItem(Bundle bundle) {
            InstagramItem.this.title = bundle.getString("title");
            //url = bundle.getString("url");
        }

        public InstagramItem(String title, Bitmap bitmap) {
            InstagramItem.this.title = title;
            InstagramItem.this.bitmap = bitmap;
        }

        public String getTitle() {
            return InstagramItem.this.title;
        }

        public Bitmap getBitmap() {
            return InstagramItem.this.bitmap;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("title", InstagramItem.this.title);
            //bundle.putString("url", url);
            dest.writeBundle(bundle);
        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
