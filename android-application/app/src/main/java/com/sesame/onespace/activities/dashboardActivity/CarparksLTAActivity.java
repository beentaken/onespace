package com.sesame.onespace.activities.dashboardActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sesame.onespace.R;

/**
 * Created by Thian on 8/11/2559.
 */

public class CarparksLTAActivity extends DashboardActivity{

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Context context;
    private View view;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.init();

    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        this.initActivity();
        this.initToolbar();
        this.initRecyclerView();

    }

    private void initActivity(){

        setContentView(R.layout.activity_carparkslta_list);

        this.context = getApplicationContext();
        this.view = getWindow().getDecorView().getRootView();

    }

    private void initToolbar(){

        AppBarLayout item = (AppBarLayout) findViewById(R.id.layout);
        item.setBackgroundColor(Color.parseColor("#9CC703"));

        this.mToolBar = (Toolbar) findViewById(R.id.toolbar);
        this.mToolBar.setTitle("Carparks(LTA)");
        this.mToolBar.setSubtitle("test sub title");
        this.mToolBar.setLogo(R.drawable.ic_dashboard_parking);
        this.mToolBar.setBackgroundColor(Color.parseColor("#9CC703"));
        setSupportActionBar(this.mToolBar);

        this.mToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        this.mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_out_to_right);
            }
        });

    }

    private void initRecyclerView(){

        this.linearLayoutManager = new LinearLayoutManager(this.context);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

    }
}
