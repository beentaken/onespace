package com.sesame.onespace.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.sesame.onespace.R;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.ImageMessage;
import com.sesame.onespace.utils.DateTimeUtil;
import com.sesame.onespace.utils.DrawableUtil;
import com.sesame.onespace.views.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by chongos on 9/30/15 AD.
 */

public class FullScreenViewActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener {

    private static final int INITIAL_HIDE_DELAY = 300;

    private Handler mHideHandler;
    private View mDecorView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private ImageMessage imageMessage;
    private ProgressBar progressBar;
    private TouchImageView imageView;
    private GestureDetector clickDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        mHideHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideSystemUI();
            }
        };

        mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(this);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        imageView = (TouchImageView) findViewById(R.id.imageview);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return clickDetector.onTouchEvent(motionEvent);
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras != null)
            imageMessage = extras.getParcelable(ImageMessage.class.getName());

        if(imageMessage != null) {
            String sender = getSenderName(imageMessage);
            String time = DateTimeUtil.getDateByTimestamp(imageMessage.getTimestamp())
                    + " at " + DateTimeUtil.getTimeByTimestamp(imageMessage.getTimestamp());
            toolbar.setTitle(sender);
            toolbar.setSubtitle(time);
            toolbar.setLogo(DrawableUtil.getTextDrawable(getApplicationContext(), sender));
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_close_white);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            Picasso.with(getApplicationContext())
                    .load(imageMessage.getImageURL())
                    .error(R.drawable.ic_broken_image_72dp)
                    .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                    );
        } else {
            imageView.setImageResource(R.drawable.ic_broken_image_72dp);
            progressBar.setVisibility(View.GONE);
        }

        clickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        boolean visible = (mDecorView.getSystemUiVisibility()
                                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                        if (visible) {
                            hideSystemUI();
                        } else {
                            showSystemUI();
                        }
                        return true;
                    }
                });

    }

    @Override
    public void onSystemUiVisibilityChange(int flags) {
        boolean visible = (flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
        appBarLayout.animate()
                .alpha(visible ? 1 : 0)
                .translationY(visible ? 0 : -appBarLayout.getHeight());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            delayedHide(INITIAL_HIDE_DELAY);
        } else {
            mHideHandler.removeMessages(0);
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    private String getSenderName(ImageMessage imageMessage) {
        if(imageMessage.isFromMe())
            return SettingsManager.getSettingsManager(getApplicationContext()).getUserAccountManager().getUsername();
        else {
            try {
                String senderJID = imageMessage.getFromJID();
                if (senderJID.equals(imageMessage.getChatID()))
                    return senderJID.split("@")[0];
                else
                    return senderJID.split("/")[1];
            } catch (Exception e) {
                return " ";
            }
        }
    }

}
