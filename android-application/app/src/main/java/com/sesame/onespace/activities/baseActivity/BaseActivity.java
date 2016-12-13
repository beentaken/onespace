package com.sesame.onespace.activities.baseActivity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.sesame.onespace.interfaces.SimpleGestureFilter;

import java.util.Stack;

/**
 * Created by Thian on 10/12/2559.
 */

public abstract class BaseActivity
        extends AppCompatActivity
        implements SimpleGestureFilter.SimpleGestureListener {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    //For do in backgroud
    protected Handler handler;

    //For close flow
    private boolean bReadyToClose;
    private boolean bClosed;

    //For back flow
    private Stack<String> stackStep;

    //For SimpleGestureFilter.SimpleGestureListener
    private SimpleGestureFilter detector;

    //===========================================================================================================//
    //  ACTIVITY LIFECYCLE                                                                          ACTIVITY LIFECYCLE
    //===========================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //init


        //before


        //main
        BaseActivity.super.onCreate(savedInstanceState);
        BaseActivity.this.init();
        BaseActivity.this.create();

        //after

    }

    @Override
    protected void onStart(){

        //init


        //before


        //main
        BaseActivity.super.onStart();
        BaseActivity.this.start();

        //after

    }

    @Override
    protected void onResume() {

        //init


        //before


        //main
        BaseActivity.super.onResume();
        BaseActivity.this.resume();

        //after

    }

    @Override
    public void onBackPressed() {

        //init


        //before


        //main
        BaseActivity.this.stepBack();

        //after


    }

    @Override
    protected  void onStop(){

        //init


        //before


        //main
        BaseActivity.super.onStop();
        BaseActivity.this.stop();

        //after

    }

    @Override
    protected void onRestart(){

        //init


        //before


        //main
        BaseActivity.super.onRestart();

        //after

    }

    @Override
    protected  void onDestroy(){

        //init


        //before
        if (BaseActivity.this.bClosed == false){

            BaseActivity.this.doInSensitiveDestroy();

        }

        //main
        BaseActivity.super.onDestroy();

        //after


    }

    //===========================================================================================================//
    //  INIT                                                                                        INIT
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void init(){

        //init


        //before


        //main
        BaseActivity.this.initContentView();
        BaseActivity.this.initDefaultValueBase();

        //after
        BaseActivity.this.initDefaultValue();
        BaseActivity.this.initView();

    }

    protected abstract void initContentView();

    private void initDefaultValueBase(){

        //init


        //before


        //main
        BaseActivity.this.bReadyToClose = true;
        BaseActivity.this.bClosed = false;

        BaseActivity.this.handler = new Handler();

        BaseActivity.this.stackStep = new Stack<String>();

        BaseActivity.this.detector = new SimpleGestureFilter(this, this);

        //after


    }

    protected abstract void initDefaultValue();

    protected abstract void initView();

    //===========================================================================================================//
    //  CREATE                                                                                      CREATE
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void create(){

        //init


        //before
        BaseActivity.this.doBeforeCreate();

        //main
        BaseActivity.this.doInCreate();

        //after
        BaseActivity.this.doAfterCreate();

    }

    protected abstract void doBeforeCreate();

    protected abstract void doInCreate();

    protected abstract void doAfterCreate();

    //===========================================================================================================//
    //  START                                                                                       START
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void start(){

        //init


        //before
        BaseActivity.this.doBeforeStart();

        //main
        BaseActivity.this.doInStart();

        //after
        BaseActivity.this.doAfterStart();

    }

    protected abstract void doBeforeStart();

    protected abstract void doInStart();

    protected abstract void doAfterStart();

    //===========================================================================================================//
    //  RESUME                                                                                      RESUME
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void resume(){

        //init


        //before
        BaseActivity.this.doBeforeResume();

        //main
        BaseActivity.this.doInResume();

        //after
        BaseActivity.this.doAfterResume();

    }

    protected abstract void doBeforeResume();

    protected abstract void doInResume();

    protected abstract void doAfterResume();

    //===========================================================================================================//
    //  STOP                                                                                        STOP
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    private void stop(){

        //init


        //before
        BaseActivity.this.doBeforeStop();

        //main
        BaseActivity.this.doInStop();

        //after
        BaseActivity.this.doAfterStop();

    }

    protected abstract void doBeforeStop();

    protected abstract void doInStop();

    protected abstract void doAfterStop();

    //===========================================================================================================//
    //  CLOSE                                                                                       CLOSE
    //===========================================================================================================//
    //  main method  -------------------------------------------------------------------------------****main method****

    protected void close(){

        //init


        //before
        BaseActivity.this.doBeforeClose();

        //main
        BaseActivity.this.doInClose();

        //after
        BaseActivity.this.doAfterClose();

    }

    protected abstract void doBeforeClose();

    private void doInClose(){

        //init


        //before
        if (BaseActivity.this.bReadyToClose == false){

            Log.i("BaseActivity", "==========================" + "bReadyToClose == false" + "==========================");

            return;

        }

        BaseActivity.this.stepBackToDeFault();

        //main
        BaseActivity.this.bClosed = true;
        BaseActivity.super.finish();

        //after


    }

    protected abstract void doAfterClose();

    protected abstract void doInSensitiveDestroy();

    //  set&get method  ----------------------------------------------------------------------------****set&get method****

    protected void setbReadyToClose(boolean bReadyToClose){

        BaseActivity.this.bReadyToClose = bReadyToClose;

    }

    protected boolean getbReadyToClose(){

        return BaseActivity.this.bReadyToClose;

    }

    //setClosed

    protected boolean getbClosed(){

        return BaseActivity.this.bClosed;

    }

    //===========================================================================================================//
    //  STEP BACK                                                                                   STEP BACK
    //===========================================================================================================//
    //  method  ------------------------------------------------------------------------------------****method****

    protected void stepBack(){

        //init
        Thread thread = new Thread(){

            public void run(){

                BaseActivity.this.backToPreviousStep();

            }

        };

        //before


        //main
        thread.start();

        //after


    }

    protected void stepBackToDeFault(){

        //init
        Thread thread = new Thread(){

            public void run(){

                while (BaseActivity.this.stackStep.isEmpty() == false){

                    BaseActivity.this.backToPreviousStep();

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
        String step;

        //before
        if (BaseActivity.this.stackStep.isEmpty() == true){

            BaseActivity.this.close();

            return;

        }

        //main
        step = BaseActivity.this.stackStep.get(BaseActivity.this.stackStep.size() - 1);

        doInBackToPreviousStep(step);

        //after


    }

    protected abstract void doInBackToPreviousStep(String stepName);

    //  set&get method  ----------------------------------------------------------------------------****set&get method****

    protected void addStep(String stepName){

        BaseActivity.this.stackStep.push(stepName);

    }

    protected String popStep(){

        return BaseActivity.this.stackStep.pop();

    }

    //===========================================================================================================//
    //  GESTURE FILTER                                                                              GESTURE FILTER
    //===========================================================================================================//

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){

        //init


        //before


        //main
        BaseActivity.this.detector.onTouchEvent(me);

        //after

        return BaseActivity.super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {

        //init


        //before
        if (BaseActivity.this.stackStep.isEmpty() == false){

            return;

        }

        //main
        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                BaseActivity.this.doInSwipeRight();
                break;

            case SimpleGestureFilter.SWIPE_LEFT :
                BaseActivity.this.doInSwipeLeft();
                break;

            case SimpleGestureFilter.SWIPE_UP :
                BaseActivity.this.doInSwipeUp();
                break;

            case SimpleGestureFilter.SWIPE_DOWN :
                BaseActivity.this.doInSwipeDown();
                break;

        }

        //after

    }

    @Override
    public void onDoubleTap() {

        //init


        //before
        if (BaseActivity.this.stackStep.isEmpty() == false){

            return;

        }

        //main
        BaseActivity.this.doInDoubleTap();

        //after

    }

    protected abstract void doInSwipeRight();

    protected abstract void doInSwipeLeft();

    protected abstract void doInSwipeUp();

    protected abstract void doInSwipeDown();

    protected abstract void doInDoubleTap();

    //===========================================================================================================//
    //  NOTE                                                                                        NOTE
    //===========================================================================================================//

}
