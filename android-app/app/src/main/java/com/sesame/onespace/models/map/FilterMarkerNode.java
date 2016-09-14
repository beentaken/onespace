package com.sesame.onespace.models.map;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 10/14/15 AD.
 */
public class FilterMarkerNode implements Parcelable {

    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE_RESOURCE = "image_resource";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_SUB_CAT = "sub_categories";

    private String name = null;
    private int activeImageResource = -1;
    private int inactiveImageResource = -1;
    private boolean selected = false;
    private ArrayList<FilterMarkerNode> subCategories = new ArrayList<>();
    private OnFilterChangeListener listener;

    public FilterMarkerNode() {

    }

    public FilterMarkerNode(String name) {
        this.name = name;
    }

    public FilterMarkerNode(String name, int activeImageResource, int inactiveImageResource) {
        this(name);
        this.activeImageResource = activeImageResource;
        this.inactiveImageResource = inactiveImageResource;
    }

    protected FilterMarkerNode(Bundle bundle) {
        name = bundle.getString(KEY_NAME);
        activeImageResource = bundle.getInt(KEY_IMAGE_RESOURCE);
        selected = bundle.getBoolean(KEY_SELECTED);
        subCategories = bundle.getParcelableArrayList(KEY_SUB_CAT);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getActiveImageResource() {
        return activeImageResource;
    }

    public void setActiveImageResource(int activeImageResource) {
        this.activeImageResource = activeImageResource;
    }

    public int getInactiveImageResource() {
        return inactiveImageResource;
    }

    public void setInactiveImageResource(int imageResource) {
        this.inactiveImageResource = imageResource;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if(listener != null)
            listener.onFilterChange(this);
    }

    public void addSubCategory(FilterMarkerNode subCategory) {
        this.subCategories.add(subCategory);
    }

    public FilterMarkerNode getSubCategory(int index) {
        return this.subCategories.get(index);
    }

    public List<FilterMarkerNode> getAllSubCategory() {
        return subCategories;
    }

    public int getSubCategorySize() {
        return this.subCategories.size();
    }

    public int getSelectedSubCategoriesCount() {
        int count = 0;
        for(FilterMarkerNode sub: subCategories) {
            count += sub.isSelected() ? 1 : 0;
        }
        return count;
    }

    public void setOnFilterChageListener(OnFilterChangeListener listener) {
        this.listener = listener;
        for(FilterMarkerNode sub : subCategories)
            sub.setOnFilterChageListener(listener);
    }

    public static final Creator<FilterMarkerNode> CREATOR = new Creator<FilterMarkerNode>() {
        @Override
        public FilterMarkerNode createFromParcel(Parcel in) {
            return new FilterMarkerNode(in.readBundle());
        }

        @Override
        public FilterMarkerNode[] newArray(int size) {
            return new FilterMarkerNode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, name);
        bundle.putInt(KEY_IMAGE_RESOURCE, activeImageResource);
        bundle.putBoolean(KEY_SELECTED, selected);
        bundle.putParcelableArrayList(KEY_SUB_CAT, subCategories);
        dest.writeBundle(bundle);
    }

    public interface OnFilterChangeListener {
        void onFilterChange(FilterMarkerNode filterMarkerNode);
    }

}
