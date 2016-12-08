package com.sesame.onespace.activities.dashboardActivitys;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sesame.onespace.R;
import com.sesame.onespace.interfaces.SimpleGestureFilter;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Thian on 28/11/2559.
 */

public abstract class DashBoardActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
                  ,SimpleGestureFilter.SimpleGestureListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private boolean isClosed;

    protected Stack<String> stackStep;
    private Handler handler;
    private CountDownLatch latch;

    protected DashBoardActivity mainActivity;
    protected Context context;

    protected DrawerLayout drawerLayout;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    protected Toolbar toolbar;
    protected NavigationView navigationView;

    protected SimpleGestureFilter detector;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE (MAIN BLOCK)                                                             ACTIVITY LIFECYCLE (MAIN BLOCK)
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //init


        //before


        //main
        super.onCreate(savedInstanceState);
        this.init();

        //after
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

    }

    @Override
    protected void onStart(){

        //init


        //before


        //main
        super.onStart();

        //after

    }

    @Override
    protected void onResume() {

        //init


        //before


        //main
        super.onResume();

        //after

    }

    @Override
    public void onBackPressed() {

        //init


        //before


        //main
        this.stepBack();

        //after


    }

    @Override
    protected  void onStop(){

        //init


        //before


        //main
        super.onStop();

        //after

    }

    @Override
    protected void onRestart(){

        //init


        //before


        //main
        super.onRestart();

        //after

    }

    @Override
    protected  void onDestroy(){

        //init


        //before
        if (this.isClosed == false){

            this.sensitiveCaseForDestroy();

        }

        //main
        super.onDestroy();

        //after


    }

    //===========================================================================================================//
    //  METHOD (SUB BLOCK)                                                                          METHOD (SUB BLOCK)
    //===========================================================================================================//

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){

        //init


        //before


        //main
        this.detector.onTouchEvent(me);

        //after

        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        //init


        //before
        if (this.stackStep.isEmpty() == false){

            return;

        }

        //main
        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                close();
                break;

            case SimpleGestureFilter.SWIPE_LEFT :
                break;

            case SimpleGestureFilter.SWIPE_DOWN :
                break;

            case SimpleGestureFilter.SWIPE_UP :
                break;

        }

        //after

    }

    @Override
    public void onDoubleTap() {

        //init


        //before


        //main


        //after

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //init


        //before


        //main
        getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        //after

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //init
        final int id = item.getItemId();

        //before


        //main
        if (id == R.id.action_openRight) {

            this.drawerLayout.openDrawer(GravityCompat.END);

        }

        //after

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //init
        final int id = item.getItemId();

        //before


        //main
        selectNavigation(id);

        //after
        this.drawerLayout.closeDrawer(GravityCompat.END);

        return true;

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        this.initDefaultValue();
        this.initActivity();
        this.initStatusBar();
        this.initToolbar();
        initContentViewForChild();

    }

    private void initDefaultValue(){

        //init
        this.isClosed = false;

        this.stackStep = new Stack<>();
        this.handler = new Handler();
        this.latch = null;

        this.mainActivity = null;
        this.context = null;

        this.detector = new SimpleGestureFilter(this, this);

        //before


        //main


        //after
        initDefaultValueForChild();


    }

    protected abstract void initDefaultValueForChild();

    private void initActivity(){

        //init


        //before


        //main


        //after
        initActivityForChild();

    }

    protected abstract void initActivityForChild();

    private void initStatusBar(){

        //init
        Window window;

        //before


        //main
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

        //after
        initStatusBarForChild();


    }

    protected abstract void initStatusBarForChild();

    private void initToolbar(){

        //init
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);

        //before
        this.initDrawerLayout();
        this.initNavigationView();

        //main
        this.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                close();

            }
        });
        setSupportActionBar(this.toolbar);

        //after
        initToolbarForChild();

    }

    private void initDrawerLayout(){

        //init
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.actionBarDrawerToggle = new ActionBarDrawerToggle(this.mainActivity, this.drawerLayout, this.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                stackStep.push("DrawerLayout");

            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                stackStep.pop();

                if (latch != null){

                    latch.countDown();

                }

            }

        };

        //before


        //main
        this.drawerLayout.setDrawerListener(this.actionBarDrawerToggle);
        this.actionBarDrawerToggle.syncState();

        //after


    }

    private void initNavigationView(){

        //init
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);

        //before


        //main
        this.navigationView.setNavigationItemSelectedListener(this.mainActivity);
        this.navigationView.setItemIconTintList(null);

        //after

    }

    protected abstract void initToolbarForChild();

    protected abstract void initContentViewForChild();

    //===========================================================================================================//
    //  SELECT NAVIGATION                                                                           SELECT NAVIGATION
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    protected abstract void selectNavigation(int id);

    //===========================================================================================================//
    //  STEP BACK                                                                                   STEP BACK
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void stepBack(){

        //init
        Thread thread = new Thread(){

            public void run(){

                backToPreviousStep();

            }

        };

        //before


        //main
        thread.start();

        //after


    }

    private void stepBackToDeFault(){

        //init
        Thread thread = new Thread(){

            public void run(){

                while (stackStep.isEmpty() == false){

                    backToPreviousStep();

                }

            }

        };

        //before


        //main
        thread.start();

        //after


    }

    private void backToPreviousStep(){

        //init
        String step = "";

        //before
        if (stackStep.isEmpty() == true){

            close();

            return;

        }

        //main
        step = stackStep.get(stackStep.size() - 1);

        if (step.equals("DrawerLayout") == true){

            if (isDrawerLayoutOpen()){

                latch = new CountDownLatch(1);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawer(GravityCompat.END);
                    }

                });

                try {
                    latch.await();
                    latch = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        //after


    }

    private boolean isDrawerLayoutOpen(){

        //init
        boolean isOpen = false;

        //before


        //main
        if (drawerLayout.isDrawerOpen(GravityCompat.END) == true) {

            isOpen = true;

        }

        //after

        return isOpen;

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    protected void close(){

        //init
        isClosed = true;

        //before
        stepBackToDeFault();

        //main
        finish();

        //after
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);


    }

    private void sensitiveCaseForDestroy(){

        //init


        //before


        //main


        //after
        sensitiveCaseForDestroyForChild();

    }

    protected abstract void sensitiveCaseForDestroyForChild();

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

//    ************1/12/2016 by Thianchai************

//    this.actionBarDrawerToggle = new ActionBarDrawerToggle(this.dashBoardActivity, this.drawerLayout, this.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
//
//        public void onDrawerOpened(View drawerView) {
//            super.onDrawerOpened(drawerView);
//
//        }
//
//        public void onDrawerClosed(View view) {
//            super.onDrawerClosed(view);
//
//        }
//
//    };

    //----------------------------------------------------------------------------------------------

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent me){
//        // Call onTouchEvent of SimpleGestureFilter class
//        this.detector.onTouchEvent(me);
//        return super.dispatchTouchEvent(me);
//    }

    //----------------------------------------------------------------------------------------------

//    @Override
//    public void onDoubleTap() {
//
//        //        this.loadData();
////
////        try {
////
////            this.thread.join();
////            this.adapter.notifyDataSetChanged();
////
////        } catch (InterruptedException e) {
////
////            e.printStackTrace();
////
////        }
//
////        list.remove(position);
////        recycler.removeViewAt(position);
////        mAdapter.notifyItemRemoved(position);
////        mAdapter.notifyItemRangeChanged(position, list.size());
//
//    }

    //----------------------------------------------------------------------------------------------

//    String step = stackStep.pop();
//
//    if (!(step == "DrawerLayout")){
//
//        stackStep.push(step);
//
//    }

    //----------------------------------------------------------------------------------------------

//    this.mainActivity = this;
//    this.context = getApplicationContext();

    //----------------------------------------------------------------------------------------------

//    this.navigationView.inflateMenu(R.menu.activity_dashboard_drawer);

    //----------------------------------------------------------------------------------------------

    //super.onBackPressed();

    //----------------------------------------------------------------------------------------------

//    @Override
//    public void onDrawerSlide(View view, float v) {
//
//    }
//
//    @Override
//    public void onDrawerOpened(View view) {
//
//    }
//
//    @Override
//    public void onDrawerClosed(View view) {
//        // your refresh code can be called from here
//    }
//
//    @Override
//    public void onDrawerStateChanged(int i) {
//
//    }

    //----------------------------------------------------------------------------------------------

    //    ************7/12/2016 by Thianchai************

//    @Override
//    public void onSwipe(int direction) {
//
//        //init
//        boolean isReady = true;
//
//        //before
//        if (this.stackStep.isEmpty() == false){
//
//            isReady = false;
//
//        }
//
//        //main
//
//        if (isReady == true){
//
//            switch (direction) {
//
//                case SimpleGestureFilter.SWIPE_RIGHT :
//                    close();
//                    break;
//
//                case SimpleGestureFilter.SWIPE_LEFT :
//                    break;
//
//                case SimpleGestureFilter.SWIPE_DOWN :
//                    break;
//
//                case SimpleGestureFilter.SWIPE_UP :
//                    break;
//
//            }
//
//        }
//
//        //after
//
//    }

    //----------------------------------------------------------------------------------------------

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        //init
//        final int id = item.getItemId();
//
//        //before
//
//
//        //main
//        if (id == R.id.action_openRight) {
//
//            this.drawerLayout.openDrawer(GravityCompat.END);
//
//            return true;
//
//        }
//
//        //after
//
//        return super.onOptionsItemSelected(item);
//    }

}
