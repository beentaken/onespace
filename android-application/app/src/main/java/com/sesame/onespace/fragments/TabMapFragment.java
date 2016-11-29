package com.sesame.onespace.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sesame.onespace.R;
/**
 * Created by chongos on 12/10/15 AD.
 */
public class TabMapFragment extends Fragment {

    private View layout;
    private ImageView icon;
    private TextView textView;

    public TabMapFragment() {

    }

    public static TabMapFragment newInstance() {
        TabMapFragment fragment = new TabMapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_map, container, false);
        layout = view.findViewById(R.id.layout);
        icon = (ImageView) view.findViewById(R.id.icon);
        textView = (TextView) view.findViewById(R.id.textview);

        icon.setAlpha(0.0f);
        textView.setAlpha(0.0f);

        return view;
    }

    public void setText(String text) {
        if(textView != null) {
            textView.setText(text);
        }
    }

    public String getText() {
        if(textView != null)
            return textView.getText().toString();
        return "Open Map";
    }

    public void setAlpha(float alpha) {
        alpha *= alpha;
        if(icon != null)
            icon.setAlpha(alpha);
        if(layout != null)
            layout.setAlpha(alpha);
        if(textView != null)
            textView.setAlpha(alpha);
    }

}
