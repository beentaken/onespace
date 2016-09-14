package com.sesame.onespace.views;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.sesame.onespace.R;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by chongos on 11/18/15 AD.
 */
public class ConncetionStatusView {

    private Context context;
    private TextView view;

    public ConncetionStatusView(Context context, TextView view) {
        this.context = context;
        this.view = view;
    }

    public void setStatus(int status) {
        setStatus(0, status);
    }

    public void setStatus(int oldStatus, int newStatus) {
        if(newStatus < 1)
            return;
        view.setText(getStatusString(newStatus));
        view.setBackgroundColor(getStatusColor(newStatus));
        if(newStatus == 3) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    showStatusView(false);
                }
            }, 2000);
        }

        if(view.getVisibility() == View.GONE)
            showStatusView(true);
    }

    private void showStatusView(final boolean show) {
        view.post(new Runnable() {
            @Override
            public void run() {
                int cx = (view.getLeft() + view.getRight()) / 2;
                int cy = view.getTop();
                int radius = Math.max(view.getWidth(), view.getHeight());

                final SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, radius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(100);
                final SupportAnimator animatorReverse = animator.reverse();

                if (show) {
                    view.setVisibility(View.VISIBLE);
                    animator.start();
                } else {
                    animatorReverse.addListener(new SupportAnimator.AnimatorListener() {

                        @Override
                        public void onAnimationEnd() {
                            view.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart() {
                        }

                        @Override
                        public void onAnimationCancel() {
                        }

                        @Override
                        public void onAnimationRepeat() {
                        }
                    });
                    animatorReverse.start();
                }
            }
        });

    }

    private String getStatusString(int status) {
        String res = "";
        switch(status) {
            case 2:
            case 5:
                res = context.getString(R.string.status_connecting);
                break;
            case 3:
                res = context.getString(R.string.status_connected);
                break;
            case 1:
            case 4:
                res = context.getString(R.string.status_disconnected);
                break;
            case 6:
                res = context.getString(R.string.status_waiting_for_network);
                break;
        }
        return res;
    }

    private int getStatusColor(int status) {
        switch(status) {
            case 2:
            case 5:
                return context.getResources().getColor(R.color.amber_500);
            case 3:
                return context.getResources().getColor(R.color.light_green_500);
            case 1:
            case 4:
                return context.getResources().getColor(R.color.red_500);
            case 6:
                return context.getResources().getColor(R.color.orange_500);
        }
        return 0;
    }

}
