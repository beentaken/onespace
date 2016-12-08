package com.sesame.onespace.activities.dashboardActivity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;

import com.sesame.onespace.R;
import com.sesame.onespace.interfaces.SimpleGestureFilter;

/**
 * Created by Thian on 6/11/2559.
 */

public abstract class DashboardActivity extends ActionBarActivity implements SimpleGestureFilter.SimpleGestureListener {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    protected Toolbar mToolBar;

    protected SimpleGestureFilter detector;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Detect touched area
        detector = new SimpleGestureFilter(this, this);
        //

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);
    }

    @Override
    protected  void onStop(){
        super.onStop();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
    }

    //===========================================================================================================//
    //  METHOD                                                                                      METHOD
    //===========================================================================================================//

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);
                break;

            case SimpleGestureFilter.SWIPE_LEFT :
                break;

            case SimpleGestureFilter.SWIPE_DOWN :
                break;

            case SimpleGestureFilter.SWIPE_UP :
                break;

        }

    }

    @Override
    public void onDoubleTap() {

        //        this.loadData();
//
//        try {
//
//            this.thread.join();
//            this.adapter.notifyDataSetChanged();
//
//        } catch (InterruptedException e) {
//
//            e.printStackTrace();
//
//        }

//        list.remove(position);
//        recycler.removeViewAt(position);
//        mAdapter.notifyItemRemoved(position);
//        mAdapter.notifyItemRangeChanged(position, list.size());

    }

}
