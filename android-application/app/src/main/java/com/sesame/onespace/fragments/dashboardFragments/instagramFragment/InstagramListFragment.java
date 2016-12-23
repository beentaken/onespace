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

public class InstagramListFragment extends Fragment {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private List<InstagramListFragment.InstagramItem> items;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private InstagramListFragment.InstagramListAdapter adapter;

    private String url;

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
        this.view = inflater.inflate(R.layout.fragment_dashboard_instagram, container, false);

        //before


        //main


        //after

    }

    private void initRecyclerView(){

        //init
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_tweets_items);
        InstagramListFragment.SpacesItemDecoration spacesItemDecoration = new InstagramListFragment.SpacesItemDecoration(spacingInPixels);
        //this.items = new ArrayList<LastTweetsListFragment.LastTweetsItem>();
        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.adapter = new InstagramListFragment.InstagramListAdapter(this.context, this.items);

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
        new InstagramListFragment.DownloadFilesTask().execute();

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

                items.add(new InstagramListFragment.InstagramItem(String.valueOf(object.get("title")), bitmap));


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

    public class InstagramListAdapter extends RecyclerView.Adapter<InstagramListFragment.ViewHolder> {

        private List<InstagramListFragment.InstagramItem> items;
        private Context mContext;

        public InstagramListAdapter(Context context, List<InstagramListFragment.InstagramItem> settingItems) {

            //init


            //before


            //main
            this.items = settingItems;
            this.mContext = context;

            //after

        }

        @Override
        public InstagramListFragment.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            //init
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_dashboard_instagram_list, null);

            //before


            //main


            //after

            return new InstagramListFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(InstagramListFragment.ViewHolder viewHolder, int i) {

            //init
            final InstagramListFragment.InstagramItem item = items.get(i);

            String colorStart = "#A2DCF9";
            String colorEnd = "#FDFADB";

            int colors[] = { Color.parseColor(colorStart), Color.parseColor(colorEnd) };
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

            viewHolder.itemView.setBackgroundDrawable(gradientDrawable);

            viewHolder.title.setText(item.getTitle());

            viewHolder.url.setImageBitmap(item.getBitmap());

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
        TextView title;
        ImageView url;

        ViewHolder(View itemView) {

            //init
            super(itemView);
            rootView = (CardView)itemView.findViewById(R.id.root_view);
            title = (TextView)itemView.findViewById(R.id.title);
            url = (ImageView)itemView.findViewById(R.id.url);

            //before


            //main


            //after
        }
    }

    public static class InstagramItem implements Parcelable {

        private String title;
        private Bitmap bitmap;

        private InstagramItem(Bundle bundle) {
            title = bundle.getString("title");
            //url = bundle.getString("url");
        }

        public InstagramItem(String title, Bitmap bitmap) {
            this.title = title;
            this.bitmap = bitmap;
        }

        public static final Creator<InstagramListFragment.InstagramItem> CREATOR = new Creator<InstagramListFragment.InstagramItem>() {
            @Override
            public InstagramListFragment.InstagramItem createFromParcel(Parcel in) {
                return new InstagramListFragment.InstagramItem(in.readBundle());
            }

            @Override
            public InstagramListFragment.InstagramItem[] newArray(int size) {
                return new InstagramListFragment.InstagramItem[size];
            }
        };

        public String getTitle() {
            return title;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            //bundle.putString("url", url);
            dest.writeBundle(bundle);
        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
