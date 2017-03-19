package com.demo.myimagebanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 20170716 on 2017/3/17.
 */

public class MyImageBannerViewGroup extends ViewGroup {

    private int childWidth;

    private int childHeight;

    private int childCount;

    private int startX;

    private int index;

    private Scroller scroller;

    private boolean isAuto = true;

    private Timer timer;

    private TimerTask timerTask;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123) {
                if(isAuto) {
                    if(++index > childCount - 1) {
                        index = 0;
                    }
                    scrollTo(index * childWidth, 0);
                    postInvalidate();
                }
            }
        }
    };

    private boolean isClick = false;

    private OnImageBannerViewGroupClick click;

    public interface OnImageBannerViewGroupClick{
        void onImageBannerViewGroupClick(int position);
    }

    public void setOnImageBannerViewGroupClick(OnImageBannerViewGroupClick click) {
        this.click = click;
    }

    private void startAuto() {
        isAuto = true;
    }

    private void stopAuto() {
        isAuto = false;
    }

    public MyImageBannerViewGroup(Context context) {
        super(context);
        initViews();
    }

    public MyImageBannerViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public MyImageBannerViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        scroller = new Scroller(getContext());
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        };
        timer.schedule(timerTask, 100, 2000);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childCount = getChildCount();
        if(childCount == 0) {
            setMeasuredDimension(0, 0);
        } else {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            View child = getChildAt(0);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            setMeasuredDimension(childWidth * childCount, childHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed) {
            int margin = 0;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.layout(left + margin, 0, right + margin, childHeight);
                margin += childWidth;
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
                stopAuto();
                startX = (int) event.getX();
                if(!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) event.getX();
                int deta = endX - startX;
                scrollBy(-deta, 0);
                startX = endX;
                isClick = false;
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                index = (scrollX + childWidth / 2) / childWidth;
                if(index < 0) index = 0;
                if(index > childCount - 1) index = childCount - 1;
//                scrollTo(index * childWidth, 0);
                if(isClick) {
                    click.onImageBannerViewGroupClick(index);
                } else {
                    int dx = index * childWidth - scrollX;
                    scroller.startScroll(scrollX, 0, dx, 0);
                    postInvalidate();
                }
                startAuto();
                break;
            default:
        }
        return true;
    }
}
