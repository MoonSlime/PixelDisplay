package com.pixeldp.prototype;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.prototype.R;
import com.pixeldp.launcher.setting.SettingActivity;
import com.pixeldp.util.GsonUtil;
import com.pixeldp.util.PreferenceUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.pixeldp.launcher.setting.SettingActivity.CAPTURE_CODE;
import static com.pixeldp.launcher.setting.SettingActivity.NEW_EYE_INFO;

public class AnalyzingCrescentActivity extends Activity {
    @Bind(R.id.imageView_crescent)
    ImageView imageView_crescent;
    @Bind(R.id.darkRatio)
    TextView textView_darkRatio;
    @Bind(R.id.eye_camera_distance)
    TextView textView_distance;
    @Bind(R.id.diameter)
    TextView textView_diameter;
    @Bind(R.id.diopter)
    TextView textView_diopter;

    private int captureMode;
    private float diopter;
    private float pupilDiameter;
    private float eye_camera_distance; // meter
    CrescentAnalyzer crescentAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyzing_crescent);
        ButterKnife.bind(this);

        Bitmap pupilBitmap = getIntent().getParcelableExtra("pupilBitmap");
        pupilDiameter = getIntent().getFloatExtra("pupilDiameter", 0.0f);
        eye_camera_distance = getIntent().getFloatExtra("eye_camera_distance", 0.6f);

        crescentAnalyzer = CrescentAnalyzer.getInstance(pupilBitmap);
        crescentAnalyzer.sendIntensityProfileToServer(this);

        CrescentAnalyzer.Crescent crescent = crescentAnalyzer.getCrescent();
        showResult(pupilBitmap, crescent.getBoundaryLineIndex());

        captureMode = PreferenceUtil.instance(this).get(CAPTURE_CODE, NEW_EYE_INFO);
    }

    @OnClick(R.id.analyzing_crescent_button_register)
    public void onButtonRegister(View v) {
        EyeModel eyeModel = new EyeModel(null, null, null, null, diopter, diopter, null);

        PixelAPI api = PixelService.getRetrofit(this);
        Call<ResponseModel> eyeInfo = (captureMode == SettingActivity.NEW_EYE_INFO) ? api.insertEyeInfo(GsonUtil.serialize(eyeModel)) : api.updateEyeInfo(GsonUtil.serialize(eyeModel));
        eyeInfo.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    ResponseModel responseModel = response.body();

                    if (responseModel.getCode() != 200) {
                        Log.d("debugging_http", responseModel.toString());
                        Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        int value = getMyopticOptimization();
                        PreferenceUtil.instance(AnalyzingCrescentActivity.this).put("FONT_SIZE", value);

                        Intent intent = new Intent(AnalyzingCrescentActivity.this, SettingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void showResult(Bitmap pupilBitmap, int boundaryLineIndex) {

        for (int i = 0; i < pupilBitmap.getHeight(); i++) {
            pupilBitmap.setPixel(pupilBitmap.getWidth() / 2, i, (i < boundaryLineIndex) ? Color.RED : Color.BLUE);
        }

        imageView_crescent.setImageBitmap(pupilBitmap);

        float flash_camera_distance = 6.0f; // mm
        float darkRatio = (boundaryLineIndex + 1) / (float) pupilBitmap.getHeight();
        diopter = (flash_camera_distance / (darkRatio * eye_camera_distance * pupilDiameter)) * (crescentAnalyzer.isMyoptic() ? -1.0f : 1.0f);

        DecimalFormat formatter = new DecimalFormat("0.###");

        textView_darkRatio.setText(formatter.format(darkRatio));
        textView_distance.setText(formatter.format(eye_camera_distance) + "m");
        textView_diameter.setText(formatter.format(pupilDiameter) + "mm");
        textView_diopter.setText(formatter.format(diopter));
    }

    private void systemCanWrite() {
        try {
            if (android.provider.Settings.System.getInt(this.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                android.provider.Settings.System.putInt(this.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setBrightness(int value) {
        Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        WindowManager.LayoutParams getAttribute = this.getWindow().getAttributes();
        getAttribute.screenBrightness = (float) value / 255;
        this.getWindow().setAttributes(getAttribute);
    }

    public int getMyopticOptimization() {
        systemCanWrite();
        // private int columnSize[] = {5, 4, 3, 3, 2, 2};
        // 128, 192, 255
        int value = 0;
        if (-3.75f <= diopter && diopter < -1.5f) {
            value = 2;
            setBrightness(128);
        } else if (-6.0f <= diopter && diopter < -3.75f) {
            value = 3;
            setBrightness(192);
        } else if (-6.0f > diopter) {
            value = 4;
            setBrightness(255
            );
        }
        return value;
    }
}