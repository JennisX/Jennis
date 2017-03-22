package com.demo.myrecorder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 20170716 on 2017/3/22.
 */

public class MyTogetherHalfActivity extends AppCompatActivity {

    @BindView(R.id.mr_aht_lv)
    public ListView listView;

    @BindView(R.id.mr_aht_txt)
    public TextView textView;

    private int tvWidth, tvHeight, tvX, tvY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_half_together);
        ButterKnife.bind(this);

        tvWidth = textView.getWidth();
        tvHeight = textView.getHeight();
        tvX = (int) textView.getX();
        tvY = (int) textView.getY();

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //改变UI
                        textView.setText(R.string.mtf_touch);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //改变UI
                        int curX = (int) event.getX();
                        int curY = (int) event.getY();
                        if((curX > tvX && curX < tvX + tvWidth)
                                && (curY > tvY && curY < tvY + tvHeight)) {
                            textView.setText(R.string.mtf_touch);
                        } else {
                            textView.setText(R.string.mtf_outside);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //改变UI
                        textView.setText(R.string.mtf_normal);
                        break;
                    default:
                }
                return true;
            }
        });
    }
}
