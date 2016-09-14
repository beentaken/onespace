package com.sesame.onespace.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sesame.onespace.R;

/**
 * Created by chongos on 8/28/15 AD.
 */
public class SplashScreenFragment extends Fragment {

    private ImageView mIcon;
//    private ImageView mIconText;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash, container, false);
        mIcon = (ImageView) view.findViewById(R.id.app_icon);
//        mIconText = (ImageView) view.findViewById(R.id.iv_text);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        return view;
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

}