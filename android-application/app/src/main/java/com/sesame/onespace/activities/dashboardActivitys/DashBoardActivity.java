package com.sesame.onespace.activities.dashboardActivitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sesame.onespace.R;
import com.sesame.onespace.activities.baseActivity.BaseActivity;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.CanNotConnectedToServerFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.InternetNotAvailableFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.LoadingFragment;
import com.sesame.onespace.fragments.dashboardFragments.notificationFragment.NoDataFragment;
import com.sesame.onespace.managers.location.UserLocationManager;
import com.sesame.onespace.network.OneSpaceApi;

import java.util.concurrent.CountDownLatch;

import retrofit.GsonConverterFactory;

/**
 * Created by Thian on 12/12/2559.
 */

public abstract class DashBoardActivity
        extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private final static String STEP_DRAWERLAYOUT = "DrawerLayout";

    protected final static int EVERYTHING_OK = -1;
    protected final static int INTERNET_NOT_AVAILABLE = 0;
    protected final static int CAN_NOT_CONNECT_TO_SERVER = 1;
    protected final static int NO_DATA = 2;

    private CountDownLatch latch;

    private GPSBroadcastReceiver gpsBroadcastReceiver;

    protected OneSpaceApi.Service api;

    protected int idFragment;
    protected boolean result;
    protected int caseFail;

    //===========================================================================================================//
    //  MAIN BLOCK                                                                                  MAIN BLOCK
    //===========================================================================================================//

    @Override
    protected void initDefaultValue() {

        DashBoardActivity.this.initDefaultValueForParallelRun();
        DashBoardActivity.this.initDefaultValueForGPSReceiver();
        DashBoardActivity.this.initDefaultValueForAPI();

    }

    @Override
    protected void initView() {

        DashBoardActivity.this.initStatusBar();
        DashBoardActivity.this.initAppBarLayout();
        DashBoardActivity.this.initDrawerLayout();
        DashBoardActivity.this.initNavigationView();
        DashBoardActivity.this.initToolbar();

    }

    @Override
    protected void doAfterCreate() {

        DashBoardActivity.this.playAnimationForOpenActivity();

    }

    @Override
    protected void doBeforeResume() {

        DashBoardActivity.this.registerGPSReceiver();

    }

    @Override
    protected void doBeforeStop() {

        DashBoardActivity.this.unregisterGPSReceiver();

    }

    @Override
    protected void doAfterClose() {

        DashBoardActivity.this.playAnimationForCloseActivity();

    }

    @Override
    protected void doInBackToPreviousStep(String stepName) {

        switch (stepName) {

            case  DashBoardActivity.STEP_DRAWERLAYOUT:
                DashBoardActivity.this.backToPreviousStepForDrawerLayoutStep();
                break;

        }

    }

    @Override
    protected void doInSwipeRight() {

        DashBoardActivity.super.close();

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initDefaultValueForParallelRun(){

        //init
        DashBoardActivity.this.latch = null;

        //before


        //main


        //after

    }

    private void initDefaultValueForGPSReceiver(){

        //init
        DashBoardActivity.this.gpsBroadcastReceiver = new GPSBroadcastReceiver();

        //before


        //main


        //after

    }

    private void initDefaultValueForAPI(){

        //init
        DashBoardActivity.this.api = new OneSpaceApi.Builder(getApplicationContext()).addConverterFactory(GsonConverterFactory.create()).build();

        //before


        //main


        //after

    }

    //===========================================================================================================//
    //  SET VIEW                                                                                    SET VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void initStatusBar(){

        //init
        Window window;

        //before


        //main
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window = DashBoardActivity.super.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);

        }

        //after


    }

    private void initAppBarLayout(){

        //init
        AppBarLayout appBarLayout = (AppBarLayout) DashBoardActivity.super.findViewById(R.id.app_bar_layout);

        //before


        //main
        appBarLayout.setExpanded(true, true);

        //after

    }

    private void initDrawerLayout(){

        //init
        Toolbar toolbar = (Toolbar) DashBoardActivity.super.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) DashBoardActivity.super.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(DashBoardActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View drawerView) {

                //init


                //before


                //main
                super.onDrawerOpened(drawerView);
                DashBoardActivity.super.addStep(DashBoardActivity.STEP_DRAWERLAYOUT);

                //after


            }

            public void onDrawerClosed(View view) {

                //init


                //before


                //main
                super.onDrawerClosed(view);
                DashBoardActivity.super.popStep();

                //after
                if (DashBoardActivity.this.latch != null){

                    DashBoardActivity.this.latch.countDown();

                }

            }

        };

        //before


        //main
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //after


    }

    private void initNavigationView(){

        //init
        NavigationView navigationView = (NavigationView) DashBoardActivity.super.findViewById(R.id.nav_view);

        //before


        //main
        navigationView.setNavigationItemSelectedListener(DashBoardActivity.this);
        navigationView.setItemIconTintList(null);

        //after

    }

    private void initToolbar(){

        //init
        Toolbar toolbar = (Toolbar) DashBoardActivity.super.findViewById(R.id.toolbar);

        //before


        //main
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //init

                //before

                //main
                DashBoardActivity.super.close();

                //after

            }
        });
        DashBoardActivity.super.setSupportActionBar(toolbar);

        //after


    }

    //===========================================================================================================//
    //  CREATE                                                                                      CREATE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void playAnimationForOpenActivity(){

        //init


        //before


        //main
        DashBoardActivity.super.overridePendingTransition(R.anim.slide_in_from_right, R.anim.nothing);

        //after

    }

    //===========================================================================================================//
    //  RESUME                                                                                      RESUME
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void registerGPSReceiver(){

        //init


        //before


        //main
        DashBoardActivity.super.registerReceiver(DashBoardActivity.this.gpsBroadcastReceiver, new IntentFilter("GPSTrackerService"));

        //after

    }

    //===========================================================================================================//
    //  STOP                                                                                        STOP
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void unregisterGPSReceiver(){

        //init


        //before


        //main
        DashBoardActivity.super.unregisterReceiver(DashBoardActivity.this.gpsBroadcastReceiver);

        //after

    }

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void playAnimationForCloseActivity(){

        //init


        //before


        //main
        DashBoardActivity.super.overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);

        //after

    }

    //===========================================================================================================//
    //  STEP BACK                                                                                   STEP BACK
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void backToPreviousStepForDrawerLayoutStep(){

        //init
        final DrawerLayout drawerLayout = (DrawerLayout) DashBoardActivity.super.findViewById(R.id.drawer_layout);

        //before
        if (DashBoardActivity.this.isDrawerLayoutOpen() == false){

            return;

        }

        //main
        DashBoardActivity.this.latch = new CountDownLatch(1);

        DashBoardActivity.super.handler.post(new Runnable() {
            @Override
            public void run() {

                //init


                //before


                //main
                drawerLayout.closeDrawer(GravityCompat.END);

                //after

            }

        });

        try {
            DashBoardActivity.this.latch.await();
            DashBoardActivity.this.latch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //after


    }

    private boolean isDrawerLayoutOpen(){

        //init
        DrawerLayout drawerLayout = (DrawerLayout) DashBoardActivity.super.findViewById(R.id.drawer_layout);
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
    //  NAVIGATION VIEW                                                                             NAVIGATION VIEW
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //init


        //before


        //main
        DashBoardActivity.super.getMenuInflater().inflate(R.menu.menu_dashboard_toolbar, menu);

        //after

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //init
        DrawerLayout drawerLayout = (DrawerLayout) DashBoardActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        //before


        //main
        if (id == R.id.action_openRight) {

            drawerLayout.openDrawer(GravityCompat.END);

        }

        //after

        return DashBoardActivity.super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //init
        DrawerLayout drawerLayout = (DrawerLayout) DashBoardActivity.super.findViewById(R.id.drawer_layout);
        final int id = item.getItemId();

        //before


        //main
        DashBoardActivity.this.doWhenSelectNavigation(id);

        //after
        drawerLayout.closeDrawer(GravityCompat.END);

        return true;

    }

    protected abstract void doWhenSelectNavigation(int id);

    //===========================================================================================================//
    //  LOCATION CHANGE                                                                             LOCATION CHANGE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    protected abstract void doWhenLocationChange();

    //  private class  -----------------------------------------------------------------------------****private class****

    private class GPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //init
            Bundle b = intent.getExtras();

            //before
            UserLocationManager.setLatitude(b.getDouble("latitude", 0));
            UserLocationManager.setLongitude(b.getDouble("longitude", 0));

            //main
            DashBoardActivity.this.doWhenLocationChange();

            //after

        }

    }

    //===========================================================================================================//
    //  START FRAGMENT                                                                              START FRAGMENT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    protected void startFragment(int index){

        //init
        OpenFragmentThread openFragmentThread = new OpenFragmentThread();

        //before
        DashBoardActivity.this.openLoadingFragment();
        DashBoardActivity.this.idFragment = index;

        //main
        openFragmentThread.start();

        //after


    }

    private void openLoadingFragment(){

        //init
        LoadingFragment fragment;

        //before


        //main
        fragment = new LoadingFragment();
        DashBoardActivity.super.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();

        //after


    }

    protected abstract void doInPrepareForOpenFragment();

    protected abstract void doInFailToOpenFragment();

    protected abstract void doInSuccessToOpenFragment();

    //  private class  -----------------------------------------------------------------------------****private class****

    private class OpenFragmentThread extends Thread{

        @Override
        public void run(){

            //init
            DashBoardActivity.this.result = true;
            DashBoardActivity.this.caseFail = DashBoardActivity.EVERYTHING_OK;

            //before
            OpenFragmentThread.this.prepareForOpenFragment();

            //main
            OpenFragmentThread.this.OpenFragment();

            //after


        }

        private void prepareForOpenFragment(){

            //init


            //before


            //main
            DashBoardActivity.this.doInPrepareForOpenFragment();

            //after

        }

        private void OpenFragment(){

            //init
            InternetNotAvailableFragment internetNotAvailableFragment = new InternetNotAvailableFragment();
            CanNotConnectedToServerFragment notConnectingToServerFragment = new CanNotConnectedToServerFragment();
            NoDataFragment noDataFragment = new NoDataFragment();

            //before


            //main
            if (DashBoardActivity.this.result == false){

                if (DashBoardActivity.this.caseFail == DashBoardActivity.INTERNET_NOT_AVAILABLE){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, internetNotAvailableFragment, internetNotAvailableFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                    Thread thread = new Thread(){

                        @Override
                        public void run(){

                            DashBoardActivity.this.getWindow().getDecorView().getRootView().setOnTouchListener(new View.OnTouchListener() {

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {

                                    DashBoardActivity.this.startFragment(DashBoardActivity.this.idFragment);

                                    return false;
                                }

                            });

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            DashBoardActivity.this.startFragment(DashBoardActivity.this.idFragment);

                        }

                    };

                    thread.run();

                }

                if(DashBoardActivity.this.caseFail == DashBoardActivity.CAN_NOT_CONNECT_TO_SERVER){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, notConnectingToServerFragment, notConnectingToServerFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                }

                if(DashBoardActivity.this.caseFail == DashBoardActivity.NO_DATA){

                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, noDataFragment, noDataFragment.getClass().getSimpleName()).addToBackStack(null).commit();

                }

                DashBoardActivity.this.doInFailToOpenFragment();

            }
            else{

                DashBoardActivity.this.doInSuccessToOpenFragment();

            }

            //after


        }

    }

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//


}
