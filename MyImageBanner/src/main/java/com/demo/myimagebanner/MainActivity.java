package com.demo.myimagebanner;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MyImageBannerViewGroup banner;

    private int pics[] = {
            R.mipmap.pic_1,
            R.mipmap.pic_2,
            R.mipmap.pic_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        banner = (MyImageBannerViewGroup) findViewById(R.id.banner);
        for (int i = 0; i < pics.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setBackgroundResource(pics[i]);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            banner.addView(iv);
        }
        banner.setOnImageBannerViewGroupClick(new MyImageBannerViewGroup
                .OnImageBannerViewGroupClick() {
            @Override
            public void onImageBannerViewGroupClick(int position) {
                Toast.makeText(MainActivity.this, "position = " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
