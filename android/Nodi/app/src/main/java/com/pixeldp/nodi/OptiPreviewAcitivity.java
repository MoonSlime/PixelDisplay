package com.pixeldp.nodi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.RecordModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.util.FontSizeChanger;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OptiPreviewAcitivity extends PixelActivity {
    @BindView(R.id.opti_preview_imageview)
    ImageView previewImage;

    int optiLevel;

    int[] previewDrawableImage = {
            R.drawable.opti_preview_01,
            R.drawable.opti_preview_02,
            R.drawable.opti_preview_03,
            R.drawable.opti_preview_04,
            R.drawable.opti_preview_05,
            R.drawable.opti_preview_06,
            R.drawable.opti_preview_07,
            R.drawable.opti_preview_08,
            R.drawable.opti_preview_09,
            R.drawable.opti_preview_10,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opti_preview);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("화면 최적화");
        getSupportActionBar().setElevation(0);

        Thread getEyeModelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                PixelAPI api = PixelService.getRetrofit(getApplicationContext());
                Call<EyeModel> optimize = api.getLastEyeInfo();
                Response<EyeModel> response = null;
                boolean isSuccessful = false;
                try {
                    response = optimize.execute();
                    isSuccessful = (response != null && response.isSuccessful() && response.body() != null && response.body().getCode() == 200);
                } catch (IOException e) {
                    isSuccessful = false;
                    e.printStackTrace();
                }

                if (isSuccessful) {
                    RecordModel recordModel = new RecordModel(response.body());
                    optiLevel = recordModel.getLevel_visualAcuity();
                } else {
                    Toast.makeText(OptiPreviewAcitivity.this, "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getEyeModelThread.start();
        try {
            getEyeModelThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        previewImage.setBackgroundResource(previewDrawableImage[optiLevel]);
    }

    @OnClick({R.id.opti_preview_imagebutton_confirm, R.id.opti_preview_imagebutton_self_adjust})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.opti_preview_imagebutton_confirm:
                float textScaleRatio = 1.1f + optiLevel * 0.1f;
                FontSizeChanger.set(textScaleRatio, getApplicationContext());
                FontSizeChanger.saveFontSize(getApplicationContext(), textScaleRatio);

                PixelAPI api = PixelService.getRetrofit(getApplicationContext());
                Call<ResponseModel> optimize = api.optimize();
                optimize.enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        if (response != null && response.isSuccessful() && response.body() != null) {
                            final ResponseModel eyeModel = response.body();
                            if (eyeModel.getCode() != 200) {
                                Log.d("debugging_http", eyeModel.toString());
                                Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(OptiPreviewAcitivity.this);
                                builder.setMessage("핸드폰 종료 후 재부팅 하면 초기화 됩니다.")
                                        .setPositiveButton(Html.fromHtml("<font color='#009688'>네</font>"), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(OptiPreviewAcitivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
                break;
            case R.id.opti_preview_imagebutton_self_adjust:
                Intent intent = new Intent(OptiPreviewAcitivity.this, ScreenSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}
