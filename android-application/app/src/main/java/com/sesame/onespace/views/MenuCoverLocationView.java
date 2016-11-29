package com.sesame.onespace.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;

import java.util.HashMap;

/**
 * Created by chongos on 12/1/15 AD.
 */
public class MenuCoverLocationView implements View.OnClickListener {

    private Context context;
    private View mContainer;
    private ImageView mIconPin;
    private TextView mAmHere;
    private TextView mLocation;
    private TextView mAddress;
    private TextView mHintHelp;
    private TextView mLocationUnavailable;

    private boolean locationAvailable;
    private HashMap<Boolean, View.OnClickListener> listenerHashMap;

    public MenuCoverLocationView(Context context, View layoutInflate) {
        this.context = context;
        mContainer = layoutInflate.findViewById(R.id.main_card_location_info);
        mIconPin = (ImageView) layoutInflate.findViewById(R.id.main_pin_icon);
        mAmHere = (TextView) layoutInflate.findViewById(R.id.main_textview_am_here);
        mLocation = (TextView) layoutInflate.findViewById(R.id.main_textview_location);
        mAddress = (TextView) layoutInflate.findViewById(R.id.main_textview_address);
        mHintHelp = (TextView) layoutInflate.findViewById(R.id.main_textview_hint_help);
        mLocationUnavailable = (TextView) layoutInflate.findViewById(R.id.main_textview_location_unavailable);

        listenerHashMap = new HashMap<>();
        mContainer.setOnClickListener(this);
        setLocationAvailable(true);
    }

    public void setOnClickCoverListener(boolean onAvailable, View.OnClickListener listener) {
        listenerHashMap.put(onAvailable, listener);
    }

    public void setLocationAvailable(boolean available) {
        if(locationAvailable == available)
            return;
        locationAvailable = available;
        if(available)
            displayLocationAvailable();
        else 
            displayLocationUnavailable();
    }

    public void setLocationAddress(String location, String address) {
        displayLocationAvailable();
        setLocation(location);
        setAddress(address);
    }

    public void setLocation(String location) {
        mIconPin.setImageResource(R.drawable.ic_pin_location_48dp);
        mLocation.setText(location);
    }

    public void setAddress(String address) {
        mIconPin.setImageResource(R.drawable.ic_pin_location_48dp);
        mAddress.setText(address);
    }

    private void displayLocationAvailable() {
        mIconPin.setImageResource(R.drawable.ic_location_searching_48dp);
        mHintHelp.setText("Click here to create a corner!");
        mLocationUnavailable.setVisibility(View.GONE);
        mLocation.setVisibility(View.VISIBLE);
        mAddress.setVisibility(View.VISIBLE);
        mAmHere.setVisibility(View.VISIBLE);
        mLocation.setText("---");
        mAddress.setText("-");
    }

    private void displayLocationUnavailable() {
        mIconPin.setImageResource(R.drawable.ic_location_off_48dp);
        mHintHelp.setText("Go to Setting");
        mLocationUnavailable.setVisibility(View.VISIBLE);
        mLocation.setVisibility(View.GONE);
        mAddress.setVisibility(View.GONE);
        mAmHere.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        View.OnClickListener onClicklistener = listenerHashMap.get(locationAvailable);
        if(onClicklistener != null)
            onClicklistener.onClick(v);
    }
}
