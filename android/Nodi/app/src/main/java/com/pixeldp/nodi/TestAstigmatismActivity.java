package com.pixeldp.nodi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestAstigmatismActivity extends PixelActivity {
    @BindView(R.id.test_astigmatism_imagebutton_yes)
    ImageButton yes;
    @BindView(R.id.test_astigmatism_imagebutton_no)
    ImageButton no;
    @BindView(R.id.test_astigmatism_text)
    TextView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_astigmatism);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("난시 테스트");
        getSupportActionBar().setElevation(0);
    }

    @OnClick(R.id.test_astigmatism_imagebutton_yes)
    void onClickYes() {
        eyeModel.setLevel_astigmatism(2);
        finishTest();
    }

    @OnClick(R.id.test_astigmatism_imagebutton_no)
    void onClickNo() {
        eyeModel.setLevel_astigmatism(1);
        finishTest();
    }

    private void finishTest() {
        if (ResultPersonalDataDialog.user_input_color_blindness == ResultPersonalDataDialog.USER_INPUT_COLOR_BLINDNESS.DONTKNOW) {
            startActivityForResult(new Intent(TestAstigmatismActivity.this, TestColorBlindnessActivity.class), 0);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}