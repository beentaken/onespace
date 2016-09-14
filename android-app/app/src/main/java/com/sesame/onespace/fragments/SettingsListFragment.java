package com.sesame.onespace.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.sesame.onespace.activities.SettingsActivity;
import com.sesame.onespace.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 12/7/15 AD.
 */
public class SettingsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private SettingListAdapter adapter;

    public SettingsListFragment() {

    }

    public static SettingsListFragment newInstance() {
        SettingsListFragment fragment = new SettingsListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_list, container, false);

        List<SettingItem> items = new ArrayList<>();
        items.add(new SettingItem(getString(R.string.pref_header_accounts), R.drawable.ic_header_setting_person, SettingsActivity.FRAGMENT_ACCOUNT));
        items.add(new SettingItem(getString(R.string.pref_header_notifications), R.drawable.ic_header_setting_notifications, SettingsActivity.FRAGMENT_NOTIFICATON));
        items.add(new SettingItem(getString(R.string.pref_header_map), R.drawable.ic_header_setting_map, SettingsActivity.FRAGMENT_MAP));
        items.add(new SettingItem(getString(R.string.pref_header_chats), R.drawable.ic_header_setting_chat, SettingsActivity.FRAGMENT_CHAT));
        items.add(new SettingItem(getString(R.string.pref_header_about), R.drawable.ic_header_setting_info, SettingsActivity.FRAGMENT_ABOUT));
        items.add(new SettingItem(getString(R.string.pref_header_help), R.drawable.ic_header_setting_help, SettingsActivity.FRAGMENT_HELP));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new SettingListAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 76, 0));
        return view;
    }


    public class SettingListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<SettingItem> items;
        private Context mContext;

        public SettingListAdapter(Context context, List<SettingItem> settingItems) {
            this.items = settingItems;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_settings_item, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            final SettingItem item = items.get(i);

            viewHolder.title.setText(item.getTitle());
            viewHolder.icon.setImageResource(item.getIcon());
            viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    intent.putExtra(SettingsActivity.KEY_SETTING_ITEM, item);
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

    public static class SettingItem implements Parcelable {

        private String title;
        private int fragmentID;
        private int icon;

        private SettingItem(Bundle bundle) {
            title = bundle.getString("title");
            fragmentID = bundle.getInt("fragment_id");
            icon = bundle.getInt("icon");
        }

        public SettingItem(String title, int icon, int fragmentID) {
            this.title = title;
            this.icon = icon;
            this.fragmentID = fragmentID;
        }

        public static final Creator<SettingItem> CREATOR = new Creator<SettingItem>() {
            @Override
            public SettingItem createFromParcel(Parcel in) {
                return new SettingItem(in.readBundle());
            }

            @Override
            public SettingItem[] newArray(int size) {
                return new SettingItem[size];
            }
        };

        public String getTitle() {
            return title;
        }

        public int getFragmentID() {
            return fragmentID;
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
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putInt("fragment_id", fragmentID);
            bundle.putInt("icon", icon);
            dest.writeBundle(bundle);
        }

    }
}
