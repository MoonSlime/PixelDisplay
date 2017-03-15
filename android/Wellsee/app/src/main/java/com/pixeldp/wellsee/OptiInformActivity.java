package com.pixeldp.wellsee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class OptiInformActivity extends PixelActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opti_inform);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("화면 최적화");
    }

    @OnClick({R.id.opti_inform_imagebutton_retest, R.id.opti_inform_imagebutton_imm})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.opti_inform_imagebutton_imm:
                startActivity(new Intent(this, OptiLoadingActivity.class));
                break;
            case R.id.opti_inform_imagebutton_retest:
                startActivity(new Intent(this, TestExplainActivity.class));
                break;
        }
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}