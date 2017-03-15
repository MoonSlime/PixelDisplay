package com.pixeldp.nodi;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.util.GsonUtil;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestVisualAcuityActivity extends PixelActivity {
    @BindView(R.id.test_visual_acuity_textview_preview)
    TextView textView_preview;

    VisualAcuityTester tester;

    static {
        eyeModel = new EyeModel();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_visual_acuity);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("최적화를 위한 눈 검사");
        getSupportActionBar().setElevation(0);
        tester = new VisualAcuityTester(textView_preview);
    }

    @OnClick({R.id.test_visual_acuity_imagebutton_right, R.id.test_visual_acuity_imagebutton_cantsee})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_visual_acuity_imagebutton_cantsee:
                showResultPersonalDataDialog();
                break;
            case R.id.test_visual_acuity_imagebutton_right:
                tester.nextStep();
                break;
        }
    }

    public void onDialogFinished() {
        PixelAPI api = PixelService.getRetrofit(getApplicationContext());
        Call<ResponseModel> update = api.update(age, sex, GsonUtil.serialize(eyeModel));
        update.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    ResponseModel type = response.body();
                    if (type.getCode() != 200) {
                        Log.d("debugging_http", type.toString());
                        Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });

        Intent intent = new Intent(TestVisualAcuityActivity.this, MainActivity.class);
        intent.putExtra("eyeModel", eyeModel.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showResultPersonalDataDialog() {
        FragmentManager fm = getFragmentManager();
        ResultPersonalDataDialog dialogFragment = new ResultPersonalDataDialog();
        dialogFragment.show(fm, "ResultPersonalDataDialog");
    }

    class VisualAcuityTester {
        private TextView previewText;
        private float defaultTextSize_dp;
        private float ratio = 15.0f;
        private static final float REDUCTION_RATIO = 0.9f;
        private Random random;

        String[] words = {
                "닭", "달", "밤", "입",
                "손", "일", "인", "슭",
                "금", "문", "신", "층",
                "형", "원", "번", "한",
                "목", "월", "확", "악",
                "불", "엄", "본", "약",
                "물", "랑", "름", "움",
                "술", "불", "님", "연"
        };

        public VisualAcuityTester(TextView previewText) {
            float font_scale = -1.0f;
            try {
                font_scale = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                font_scale = 1.2f;
            }

            float currentTextSize_dp = previewText.getTextSize() / getResources().getDisplayMetrics().density;
            defaultTextSize_dp = currentTextSize_dp / font_scale;

            random = new Random();
            previewText.setText(words[random.nextInt(words.length)]);
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, defaultTextSize_dp * ratio);

            this.previewText = previewText;

            eyeModel.setLevel_visualAcuity(ratio);
        }

        public void nextStep() {
            ratio *= REDUCTION_RATIO;
            eyeModel.setLevel_visualAcuity(ratio);

            if (ratio < 1.0f) {
                showResultPersonalDataDialog();
            }

            previewText.setText(words[random.nextInt(words.length)]);
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, defaultTextSize_dp * ratio);
        }
    }
}