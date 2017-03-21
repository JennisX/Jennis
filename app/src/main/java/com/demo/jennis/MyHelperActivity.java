package com.demo.jennis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 20170716 on 2017/3/21.
 */

public class MyHelperActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(null);
    }

    public void code_1() {
        Intent intent = new Intent(MyHelperActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
