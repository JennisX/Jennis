package com.demo.myrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @OnClick(R.id.mr_ac_btn_1)
    public void fileMode() {
        Intent intent = new Intent(MainActivity.this, MyFileModeActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.mr_ac_btn_2)
    public void bytesMode() {
        Intent intent = new Intent(MainActivity.this, MyBytesModeActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.mr_ac_btn_3)
    public void togetherMode() {
        Intent intent = new Intent(MainActivity.this, MyTogetherHalfActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
