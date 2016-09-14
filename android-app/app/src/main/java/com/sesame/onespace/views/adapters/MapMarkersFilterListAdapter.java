package com.sesame.onespace.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sesame.onespace.R;
import com.sesame.onespace.models.map.FilterMarkerNode;

import java.util.List;

/**
 * Created by chongos on 10/12/15 AD.
 */
public class MapMarkersFilterListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<FilterMarkerNode> mGroupCollection;
    private ExpandableListView mExpandableListView;

    public MapMarkersFilterListAdapter(Context context, ExpandableListView pExpandableListView,
                      FilterMarkerNode pGroupCollection) {
        this.context = context;
        this.mGroupCollection = pGroupCollection.getAllSubCategory();
        this.mExpandableListView = pExpandableListView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupCollection.get(groupPosition).getSubCategory(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    class ChildHolder {
        RelativeLayout rootLayout;
        TextView name;
        ImageView icon;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ChildHolder childHolder;
        if( convertView == null ){
            convertView = LayoutInflater.from(context).inflate(R.layout.row_map_filter_subitem, null);
            childHolder = new ChildHolder();
            childHolder.rootLayout = (RelativeLayout) convertView.findViewById(R.id.root_layout);
            childHolder.name = (TextView) convertView.findViewById(R.id.lbl_name);
            childHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        FilterMarkerNode child = mGroupCollection.get(groupPosition).getSubCategory(childPosition);
        childHolder.rootLayout.setBackgroundColor(context.getResources()
                .getColor(child.isSelected() ? R.color.grey_50 : R.color.grey_100));
        childHolder.name.setText(child.getName());
        childHolder.name.setTextColor(context.getResources().getColor(
                child.isSelected() ? R.color.black : R.color.grey_500));

        if(child.getActiveImageResource() != -1 && child.getInactiveImageResource() != -1)
            childHolder.icon.setImageResource(child.isSelected()
                    ? child.getActiveImageResource() : child.getInactiveImageResource());
        else
            childHolder.icon.setImageDrawable(null);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupCollection.get(groupPosition).getSubCategorySize();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupCollection.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroupCollection.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    class GroupHolder {
        RelativeLayout rootView;
        ImageView icon;
        TextView title;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        GroupHolder groupHolder;
        if( convertView == null ){
            convertView = LayoutInflater.from(context).inflate(R.layout.row_map_filter_item, null);
            groupHolder = new GroupHolder();
            groupHolder.rootView = (RelativeLayout) convertView.findViewById(R.id.coordinator_layout);
            groupHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            groupHolder.title = (TextView) convertView.findViewById(R.id.lbl_name);
//            groupHolder.checked = (ImageView) convertView.findViewById(R.id.ic_checked);
            convertView.setTag(groupHolder);
        } else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        FilterMarkerNode group = mGroupCollection.get(groupPosition);
        groupHolder.rootView.setBackgroundColor(context.getResources()
                .getColor(group.isSelected() ? R.color.blue_50 : R.color.white));
        groupHolder.title.setText(group.getName());
        groupHolder.title.setTextColor(context.getResources()
                .getColor(group.isSelected() ? R.color.color_primary : R.color.grey_800));

        if(group.getActiveImageResource() != -1 && group.getInactiveImageResource() != -1) {
            groupHolder.icon.setImageResource(group.isSelected()
                    ? group.getActiveImageResource() : group.getInactiveImageResource());
        } else {
            groupHolder.icon.setImageDrawable(null);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void refreshList(List<FilterMarkerNode> collection){
        mGroupCollection = collection;
        notifyDataSetChanged();
        for(int g = 0; g < mGroupCollection.size(); g ++){
            if(mGroupCollection.get(g).isSelected())
                mExpandableListView.expandGroup(g);
            else
                mExpandableListView.collapseGroup(g);
        }
    }

}