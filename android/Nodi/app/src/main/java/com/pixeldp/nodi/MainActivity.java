package com.pixeldp.nodi;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.RecordModel;
import com.pixeldp.view.widgets.HoloCircleSeekBar;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends PixelActivity {

    @BindView(R.id.main_seekbar_opti_result)
    HoloCircleSeekBar seekbar_opti_result;
    @BindView(R.id.main_seekbar_colorblind_probability)
    HoloCircleSeekBar seekbar_colorblind_probability;
    @BindView(R.id.main_seekbar_astigmatism_probability)
    HoloCircleSeekBar seekbar_astigmatism_probability;

    ValueAnimator animator_visualAcuity;
    ValueAnimator animator_astigmatism;
    ValueAnimator animator_colorBlindness;

    @BindView(R.id.main_textview_opti_result_value)
    TextView textView_levelForOpti;
    @BindView(R.id.main_textview_opti_result)
    TextView textView_opti_result;

    @BindView(R.id.main_textview_astigmatism_probability)
    TextView textView_astigmatism_probability;

    @BindView(R.id.main_textview_colorblind_probability)
    TextView textView_colorBlindness_probability;

    final String[] text_levelForOpti = {"매우 낮음", "낮음", "낮음", "낮은 편", "낮은 편", "보통", "보통", "좋은 편", "좋은 편", "매우 좋음"};
    final String[] text_probability = {"없음", "낮음", "높음", "있음"};

    private boolean isReadyToFinish = false;

    @BindView(R.id.pager)
    ViewPager pager;

    PageFragment.MainPagesAdapter mainPagesAdapter;

    @BindView(R.id.pager_indicator)
    LinearLayout pager_indicator;
    Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            isReadyToFinish = false;
        }
    };
    Handler handler;

    @Override
    public void finish() {
        if (isReadyToFinish) {
            super.finish();
        } else {
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG).show();
            isReadyToFinish = true;
            handler.postDelayed(finishRunnable, 3000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        analyticsStart(this);
        handler = new Handler();
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0.0f);

        mainPagesAdapter = new PageFragment.MainPagesAdapter(MainActivity.this);
        pager.setAdapter(mainPagesAdapter);
        PageIndicator pageIndicator = new PageIndicator();
        pager.addOnPageChangeListener(pageIndicator.onPageChangeListener);

        Thread askHaveEyeInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                PixelAPI api = PixelService.getRetrofit(MainActivity.this);
                Call<EyeModel> getLastEyeInfo = api.getLastEyeInfo();
                Response<EyeModel> response = null;
                boolean isSuccessful = false;
                try {
                    response = getLastEyeInfo.execute();
                    isSuccessful = (response != null && response.isSuccessful() && response.body() != null);
                } catch (IOException e) {
                    isSuccessful = false;
                    e.printStackTrace();
                }

                if (isSuccessful && response.body().getCode() == 202) {
                    MainActivity.super.finish();
                    startActivity(new Intent(MainActivity.this, TestExplainActivity.class));
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                }
            }
        });
        askHaveEyeInfoThread.start();
        try {
            askHaveEyeInfoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getStringExtra("eyeModel") == null) {
            EyeModelReceiver eyeModelReceiver = new EyeModelReceiver();
            eyeModelReceiver.execute();
        } else {
            drawPanel(PixelActivity.eyeModel);
        }
    }

    private class EyeModelReceiver extends AsyncTask<Void, Void, EyeModel> {
        @Override
        protected EyeModel doInBackground(Void... voids) {
            PixelAPI api = PixelService.getRetrofit(getApplicationContext());
            Call<EyeModel> getEye = api.getLastEyeInfo();

            Response<EyeModel> response = null;
            boolean isSuccessful = false;
            try {
                response = getEye.execute();
                isSuccessful = (response != null && response.isSuccessful() && response.body() != null && response.body().getCode() == 200);
            } catch (IOException e) {
                isSuccessful = false;
                e.printStackTrace();
            }

            return (isSuccessful) ? response.body() : null;
        }

        @Override
        protected void onPostExecute(EyeModel eyeModel) {
            super.onPostExecute(eyeModel);

            drawPanel(eyeModel);
        }
    }

    private void drawPanel(EyeModel eyeModel) {
        if (eyeModel == null) {
            seekbar_opti_result.setValue(0.0f);
            seekbar_astigmatism_probability.setValue(0.0f);
            seekbar_colorblind_probability.setValue(0.0f);

            textView_levelForOpti.setText("0");
            textView_opti_result.setText("미확인");
            textView_astigmatism_probability.setText("미확인");
            textView_colorBlindness_probability.setText("미확인");
        } else {
            RecordModel recordModel = new RecordModel(eyeModel);

            textView_levelForOpti.setText(Integer.valueOf(recordModel.getLevel_visualAcuity() + 1).toString());
            textView_opti_result.setText(text_levelForOpti[recordModel.getLevel_visualAcuity()]);
            textView_astigmatism_probability.setText(text_probability[recordModel.getLevel_astigmatism()]);
            textView_colorBlindness_probability.setText(text_probability[recordModel.getLevel_colorBlindness()]);

            animator_visualAcuity = ValueAnimator.ofFloat(0.0f, recordModel.getLevel_visualAcuity() * (100.0f / 9.0f));
            animator_astigmatism = ValueAnimator.ofFloat(0.0f, recordModel.getLevel_astigmatism() * (100.0f / 3.0f));
            animator_colorBlindness = ValueAnimator.ofFloat(0.0f, recordModel.getLevel_colorBlindness() * (100.0f / 3.0f));

            animator_visualAcuity.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_opti_result.setValue((Float) animation.getAnimatedValue());
                }
            });
            animator_astigmatism.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_astigmatism_probability.setValue((Float) animation.getAnimatedValue());
                }
            });
            animator_colorBlindness.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_colorblind_probability.setValue((Float) animation.getAnimatedValue());
                }
            });

            animator_visualAcuity.setDuration(700);
            animator_astigmatism.setDuration(700);
            animator_colorBlindness.setDuration(700);

            animator_visualAcuity.start();
            animator_astigmatism.start();
            animator_colorBlindness.start();
        }
    }

    class PageIndicator {
        ImageView[] dots;
        ViewPager.OnPageChangeListener onPageChangeListener;

        PageIndicator() {
            final int dotsCount = mainPagesAdapter.getCount();
            dots = new ImageView[dotsCount];

            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(getApplicationContext());
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.pager_nonselected_dot));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(12, 0, 12, 0);

                pager_indicator.addView(dots[i], params);
            }

            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.pager_selected_dot));

            onPageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < dotsCount; i++) {
                        dots[i].setImageDrawable(getResources().getDrawable(R.drawable.pager_nonselected_dot));
                    }

                    dots[position].setImageDrawable(getResources().getDrawable(R.drawable.pager_selected_dot));
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            };
        }
    }
}