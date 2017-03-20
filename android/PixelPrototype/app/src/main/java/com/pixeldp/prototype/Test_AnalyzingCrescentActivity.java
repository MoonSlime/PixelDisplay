package com.pixeldp.prototype;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.prototype.device_control.FlashControl;
import com.pixeldp.util.GsonUtil;
import com.pixeldp.util.ImageUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Test_AnalyzingCrescentActivity extends Activity {
    @Bind(R.id.imageView_test_crescent)
    ImageView imageView_crescent;
    private float[] pupilDiameterLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_analyzing_crescent);
        ButterKnife.bind(this);

        Bitmap pupilBitmap = getIntent().getParcelableExtra("pupilBitmap");

        MediaStore.Images.Media.insertImage(getContentResolver(), pupilBitmap, "test", "test");

        pupilDiameterLine = collectPixelIntensities(pupilBitmap);
        sendIntensityProfileToServer(Test_AnalyzingCrescentActivity.this);

        imageView_crescent.setImageBitmap(pupilBitmap);
    }

    private float[] collectPixelIntensities(Bitmap pupilBitmap) {
        int lengthOfSidesOfSquare = pupilBitmap.getWidth(); // also can be pupilBitmap.getHeight()
        float[] pupilDiameterLine = new float[lengthOfSidesOfSquare];

        for (int i = 0; i < pupilDiameterLine.length; i++) {
            int color = pupilBitmap.getPixel(lengthOfSidesOfSquare / 2, i);
            pupilDiameterLine[i] = ImageUtil.getBrightness(color);
        }

        return pupilDiameterLine;
    }

    public void sendIntensityProfileToServer(final Activity activity) {
        ArrayList<Float> intensityProfile = new ArrayList<>();
        for (float aPupilDiameterLine : pupilDiameterLine) {
            intensityProfile.add(aPupilDiameterLine);
        }

        String json = GsonUtil.serialize(intensityProfile);

        PixelAPI api = PixelService.getRetrofit(activity);
        Call<ResponseModel> saveIntensityProfile = api.saveIntensityProfile(json);
        saveIntensityProfile.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    ResponseModel responseModel = response.body();

                    if (responseModel.getCode() != 200) {
                        Log.d("debugging_http", responseModel.toString());
                        Toast.makeText(activity, "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(activity, "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}