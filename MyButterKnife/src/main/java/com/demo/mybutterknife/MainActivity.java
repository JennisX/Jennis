package com.demo.mybutterknife;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MainActivity extends AppCompatActivity {

    private boolean flag = false;

    @BindView(R.id.mbk_ma_btn)
    public Button btn;

    @BindView(R.id.mbk_ma_txt)
    public TextView text;

    @BindString(R.string.mbk_ma_name)
    public String name;

    @OnClick(R.id.mbk_ma_btn)
    public void click() {
        text.setText(name);
        flag = true;
    }

    @OnLongClick(R.id.mbk_ma_ll)
    public boolean longClick() {
        if(flag) {
            text.setText(getResources().getString(R.string.mbk_ma_default_text));
            flag = false;
            Toast.makeText(this, "The text is changed !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please click the button !", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }
}
