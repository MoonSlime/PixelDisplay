package com.pixeldp.wellsee;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestColorBlindnessActivity extends PixelActivity {
    @BindView(R.id.test_colorblind_imageview)
    ImageView imageColorblind;
    @BindView(R.id.test_colorblind_edittext)
    EditText insertImageNumber;
    private int[] imageSet = {R.drawable.test_blindness_image_1, R.drawable.test_blindness_image_2, R.drawable.test_blindness_image_3, R.drawable.test_blindness_image_4, R.drawable.test_blindness_image_5};
    private int answer = 0;
    String[] imageAnswer = {"21", "9", "14", "83", "50"};
    private int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_colorblindness);
        ButterKnife.bind(this);
        analyticsStart(this);
        imageColorblind.setBackgroundResource(imageSet[count]);
        getSupportActionBar().setTitle("색맹 테스트");
    }

    @OnClick({R.id.test_colorblind_button_see, R.id.test_colorblind_button_notsee})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_colorblind_button_see:
                if (insertImageNumber.getText().toString().equals(imageAnswer[count])) {
                    answer++;
                }
                break;
            case R.id.test_colorblind_button_notsee:
                break;
        }
        if (count < 4) {
            count++;
            insertImageNumber.setText("");
            imageColorblind.setBackgroundResource(imageSet[count]);
        } else if (count == 4) {

            if (answer == 5) {
                eyeModel.setLevel_colorBlindness(1);
            } else {
                eyeModel.setLevel_colorBlindness(2);
            }

            setResult(RESULT_OK);
            finish();
        }
    }
}
