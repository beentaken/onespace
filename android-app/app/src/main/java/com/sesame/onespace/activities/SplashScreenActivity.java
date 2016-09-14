package com.sesame.onespace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.sesame.onespace.fragments.SplashScreenFragment;
import com.sesame.onespace.managers.UserAccountManager;
import com.sesame.onespace.service.MessageService;

/**
 * Created by chongos on 7/31/15 AD.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private long delay_time;
    private long time = 1000L;

    private SplashScreenFragment splashScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashScreenFragment = new SplashScreenFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, splashScreenFragment);
        transaction.commit();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                checkLogin();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        delay_time = time;
        handler.postDelayed(runnable, delay_time);
        time = System.currentTimeMillis();
        splashScreenFragment.setProgress(30);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        time = delay_time - (System.currentTimeMillis() - time);

    }

    private void checkLogin() {
        splashScreenFragment.setProgress(100);
        UserAccountManager session = UserAccountManager.getInstance(getApplicationContext());
        if(session.isLoggedIn()) {
            startMainActivity();
            if(!MessageService.isRunning) {
                Intent intent = new Intent(getApplicationContext(), MessageService.class);
                intent.setAction(MessageService.ACTION_CONNECT);
                startService(intent);
            }
        } else {
            startLoginActivity();
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

    private void startLoginActivity() {
        Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
    }

}
