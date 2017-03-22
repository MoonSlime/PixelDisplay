package com.pixeldp.prototype;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pixeldp.prototype.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.main_button_bluetooth)
    public void onClickBluetooth(View v) {
        startActivity(new Intent(MainActivity.this, RaspberryPiActivity.class));
    }

    @OnClick(R.id.main_button_test)
    public void onClickTest(View v) {
        startActivity(new Intent(MainActivity.this, Test_FindingPupilActivity.class));
    }
}