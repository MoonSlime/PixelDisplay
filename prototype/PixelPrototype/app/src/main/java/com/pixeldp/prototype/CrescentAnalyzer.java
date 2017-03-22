package com.pixeldp.prototype;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.prototype.device_control.FlashControl;
import com.pixeldp.util.GsonUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrescentAnalyzer {
    private float[] pupilDiameterLine;
    private boolean isIncreasing;
    private boolean isMyoptic;
    private static CrescentAnalyzer instance;

    private CrescentAnalyzer(Bitmap pupilBitmap) {
        pupilDiameterLine = collectPixelIntensities(pupilBitmap);

        isIncreasing = checkIncreaseAndDecrease();
        switch (FlashControl.getPostionFromCamera()) {
            case FlashControl.POSITION_LEFT:
                isMyoptic = isIncreasing;
                break;
            case FlashControl.POSITION_RIGHT:
                isMyoptic = !isIncreasing;
                break;
            case FlashControl.POSITION_TOP:
                isMyoptic = !isIncreasing;
                break;
            case FlashControl.POSITION_BOTTOM:
                isMyoptic = isIncreasing;
                break;
        }
    }

    public static CrescentAnalyzer getInstance(Bitmap pupilBitmap) {
        if (instance == null) {
            instance = new CrescentAnalyzer(pupilBitmap);
        }

        return instance;
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

    private float[] collectPixelIntensities(Bitmap pupilBitmap) {
        int lengthOfSidesOfSquare = pupilBitmap.getWidth(); // also can be pupilBitmap.getHeight()
        float[] pupilDiameterLine = new float[lengthOfSidesOfSquare];

        for (int i = 0; i < pupilDiameterLine.length; i++) {
            int color;

            if (FlashControl.isHorizontallyPositionedFromCamera()) {
                color = pupilBitmap.getPixel(i, lengthOfSidesOfSquare / 2);
            } else {
                color = pupilBitmap.getPixel(lengthOfSidesOfSquare / 2, i);
            }

            float[] hsl = new float[3];
            ColorUtils.colorToHSL(color, hsl);
            pupilDiameterLine[i] = hsl[2]; // lightness
        }

        return pupilDiameterLine;
    }

    private boolean checkIncreaseAndDecrease() {
        float lightnessTop = 0.0f;
        float lightnessBottom = 0.0f;

        for (int i = 0; i < pupilDiameterLine.length / 10; i++) {
            lightnessTop += pupilDiameterLine[i];
            lightnessBottom += pupilDiameterLine[pupilDiameterLine.length - 1 - i];
        }

        return (lightnessBottom > lightnessTop);
    }

    public Crescent getCrescent() {
        Crescent crescent = new Crescent();

        boolean isDecreasingStarted = false;
        float previousValue = (isIncreasing) ? pupilDiameterLine[pupilDiameterLine.length - 1] : pupilDiameterLine[0];
        int ignoreStep = (isIncreasing) ? -5 : 5;

        for (int i = (isIncreasing) ? pupilDiameterLine.length - 1 : 0; isAvailableIndex(i, pupilDiameterLine.length); i = (isIncreasing) ? i - 1 : i + 1) {
            if (!isDecreasingStarted && pupilDiameterLine[i] < previousValue) {
                isDecreasingStarted = true;
                if (isIncreasing) {
                    crescent.setEndIndex(i);
                } else {
                    crescent.setStartIndex(i);
                }
            }

            if (isDecreasingStarted) {
                if (pupilDiameterLine[i] >= previousValue) {
                    if (!isAvailableIndex(i + ignoreStep, pupilDiameterLine.length)) {
                        if (isIncreasing) {
                            crescent.setStartIndex(i);
                        } else {
                            crescent.setEndIndex(i);
                        }
                        break;
                    } else if (pupilDiameterLine[i + ignoreStep] >= pupilDiameterLine[i]) {
                        if (isIncreasing) {
                            crescent.setStartIndex(i);
                        } else {
                            crescent.setEndIndex(i);
                        }
                        break;
                    } else {
                        previousValue = pupilDiameterLine[i];
                        i += ignoreStep;
                        continue;
                    }
                }
            }

            previousValue = pupilDiameterLine[i];
        }

        return crescent;
    }

    private boolean isAvailableIndex(int index, int length) {
        return (index >= 0 && index < length);
    }

    public boolean isMyoptic() {
        return isMyoptic;
    }

    public class Crescent {
        private int startIndex;
        private int endIndex;

        Crescent() {
            startIndex = 0;
            endIndex = pupilDiameterLine.length - 1;
        }

        public int getBoundaryLineIndex() {
            float min = 1.0f;
            float max = 0.0f;

            ArrayList<Float> intensityProfile = new ArrayList<>();
            for (int i = startIndex; i < endIndex; i++) {
                intensityProfile.add(pupilDiameterLine[i]);
            }

            for (float value : intensityProfile) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }

            float middleValue = (min + max) / 2.0f;


            int boundaryLineIndex = 0;
            for (int i = 1; i < intensityProfile.size(); i++) {
                float previousValue = intensityProfile.get(i - 1);
                float currentValue = intensityProfile.get(i);

                if ((middleValue - previousValue) * (middleValue - currentValue) < 0.0) {
                    boundaryLineIndex = startIndex + i;
                    break;
                }
            }

            return boundaryLineIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }
        public int getEndIndex() {
            return endIndex;
        }

        void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }
        void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }
    }
}