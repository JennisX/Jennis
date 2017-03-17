package com.demo.myimagebanner;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 20170716 on 2017/3/17.
 */

public class MyImageBannerViewGroup extends ViewGroup {

    private View child;

    private int width;

    private int height;

    private int childCount;

    public MyImageBannerViewGroup(Context context) {
        super(context);
    }

    public MyImageBannerViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageBannerViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childCount = getChildCount();
        if(childCount == 0) {
            setMeasuredDimension(0, 0);
        } else {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            child = getChildAt(0);
            width = child.getMeasuredWidth();
            height = child.getMeasuredHeight();
            setMeasuredDimension(width * childCount, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed) {
            int margin = 0;
            for (int i = 0; i < childCount; i++) {
                child = getChildAt(i);
                child.layout(left + margin, 0, right + margin, height);
                margin += width;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
        }
        return true;
    }
}
