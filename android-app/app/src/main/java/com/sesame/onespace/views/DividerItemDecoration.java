package com.sesame.onespace.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.sesame.onespace.R;
import com.sesame.onespace.utils.ScreenUtil;

/**
 * Created by chongos on 12/7/15 AD.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    private Context context;
    private Drawable mDivider;
    private int marginLeft;
    private int marginRight;

    public DividerItemDecoration(Context context, int marginLeft, int marginRight) {
        this.context = context;
        this.mDivider = context.getDrawable(R.drawable.line_divider);
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = (int) (parent.getPaddingLeft() + ScreenUtil.dpToPx(context, marginLeft));
        int right = (int) (parent.getWidth() - parent.getPaddingRight() - ScreenUtil.dpToPx(context, marginRight));

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            if(i == childCount-1) {
                left = parent.getPaddingLeft();
                right = parent.getWidth() - parent.getPaddingRight();
            }

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

//    @Override
//    public void onDraw(Canvas c, RecyclerView parent) {
//        drawVertical(c, parent);
//    }
//
//    public void drawVertical(Canvas c, RecyclerView parent) {
//        final int left = (int) (parent.getPaddingLeft() + ScreenUtil.dpToPx(context, marginLeft));
//        final int right = parent.getWidth() - parent.getPaddingRight();
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
//                    .getLayoutParams();
//            final int top = child.getBottom() + params.bottomMargin;
//            final int bottom = top + mDivider.getIntrinsicHeight();
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
//        }
//    }
//
//
//    @Override
//    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
//        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
//    }

}