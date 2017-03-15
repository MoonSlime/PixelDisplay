package com.pixeldp.nodi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class OptiExplainActivity extends PixelActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opti_explain);
        ButterKnife.bind(this);
        analyticsStart(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("화면 최적화");
        actionBar.setElevation(0);
    }

    @OnClick({R.id.opti_explain_imagebutton_next})
    void onClick() {
        startActivity(new Intent(this, OptiInformActivity.class));
        overridePendingTransition(R.anim.enter_from_right,  R.anim.exit_to_left);
    }
}