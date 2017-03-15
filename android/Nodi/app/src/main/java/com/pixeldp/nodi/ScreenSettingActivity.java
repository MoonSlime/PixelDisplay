package com.pixeldp.nodi;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.pixeldp.util.DialogCloseListener;
import com.pixeldp.util.FilterService;
import com.pixeldp.util.FontSizeChanger;
import com.pixeldp.util.PreferenceUtil;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScreenSettingActivity extends PixelActivity implements DialogCloseListener {
    @BindView(R.id.screen_adjust_imagebutton_bltime)
    ImageButton imagebutton_bltime;

    @BindView(R.id.screen_adjust_seekbar_fontsize)
    SeekBar seekbar_font;

    @BindView(R.id.screen_adjust_seekbar_filter)
    SeekBar seekbar_filter;

    @BindView(R.id.screen_adjust_switch_bl)
    Switch switch_bl;

    @BindView(R.id.opti_detailsetting_preview)
    ImageView imageview_preview;

    @BindView(R.id.opti_detailsetting_filter)
    RelativeLayout opti_detailsetting_filter;

    private int[] preview_image_arr = {
            R.drawable.screen_preview_1,
            R.drawable.screen_preview_2,
            R.drawable.screen_preview_3,
            R.drawable.screen_preview_4,
            R.drawable.screen_preview_5,
            R.drawable.screen_preview_6,
            R.drawable.screen_preview_7,
            R.drawable.screen_preview_8,
            R.drawable.screen_preview_9,
            R.drawable.screen_preview_10};

    private float size;
    private boolean check_switch_bl;
    public static final String CHECK_FILTER_APPLY = "CHECK_FILTER_APPLY";
    public static final String START_MILLI = "START_MILLI ";
    public static final String END_MILLI = "END_MILLI ";

    public static final String COLOR_A = "COLOR_A";
    public static final String COLOR_R = "COLOR_R";
    public static final String COLOR_G = "COLOR_G";
    public static final String COLOR_B = "COLOR_B";
    public static final String COLOR_PROGRESS = "COLOR_PROGRESS";

    private Calendar startTime;
    private Calendar endTime;

    public int colorA;
    public int colorR;
    public int colorG;
    public int colorB;
    public int colorProgressValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_setting);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("개별 화면 설정");
        getSupportActionBar().setElevation(0);

        check_switch_bl = PreferenceUtil.instance(this).get(CHECK_FILTER_APPLY, false);
        seekbar_filter.setOnSeekBarChangeListener(seekbar_filter_listener);

        colorProgressValue = PreferenceUtil.instance(this).get(COLOR_PROGRESS, 0);
        seekbar_filter.setProgress(colorProgressValue);

        seekbar_font.setOnSeekBarChangeListener(seekbar_font_listener);
        size = FontSizeChanger.loadFontSize(this);
        int progress = (int) ((size - 1) * 9);
        seekbar_font.setProgress(progress);
        imageview_preview.setBackgroundResource(preview_image_arr[progress]);
        switch_bl.setOnCheckedChangeListener(switch_bl_listener);
        switch_bl.setChecked(check_switch_bl);
    }

    @OnClick({R.id.screen_adjust_imagebutton_bltime})
    void onClickBLtime() {
        FragmentManager fm = getSupportFragmentManager();
        BlueScreenTimePickDialog dialogFragment = new BlueScreenTimePickDialog();
        dialogFragment.show(fm, "picker");
    }

    @OnClick({R.id.screen_adjust_imagebutton_ok})
    void onClickOk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScreenSettingActivity.this);
        builder.setMessage("글씨 크기는 핸드폰 종료 후 재부팅 하면 초기화 됩니다. 초기화 하시겠습니까?")
                .setPositiveButton(Html.fromHtml("<font color='#009688'>네</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (checkFontSize()) {
                            FontSizeChanger.set(size, ScreenSettingActivity.this);
                            FontSizeChanger.saveFontSize(ScreenSettingActivity.this, size);
                        }
                        PreferenceUtil.instance(ScreenSettingActivity.this).put(CHECK_FILTER_APPLY, check_switch_bl);
                        if (check_switch_bl) {
                            if (isMyServiceRunning(FilterService.class))
                                stopService(new Intent(ScreenSettingActivity.this, FilterService.class));

                            if (startTime != null && endTime != null) {
                                PreferenceUtil.instance(ScreenSettingActivity.this).put(START_MILLI, startTime.getTimeInMillis());
                                PreferenceUtil.instance(ScreenSettingActivity.this).put(END_MILLI, endTime.getTimeInMillis());
                            }

                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_A, colorA);
                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_R, colorR);
                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_G, colorG);
                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_B, colorB);
                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_PROGRESS, colorProgressValue);

                            Intent intent = new Intent(ScreenSettingActivity.this, FilterService.class);
                            startService(intent);

                        } else {
                            PreferenceUtil.instance(ScreenSettingActivity.this).put(COLOR_PROGRESS, 0);
                            if (isMyServiceRunning(FilterService.class))
                                stopService(new Intent(ScreenSettingActivity.this, FilterService.class));
                        }

                        Intent intent = new Intent(ScreenSettingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                    }
                })
                .setNegativeButton(Html.fromHtml("<font color='#009688'>아니오</font>"), null)
                .show();
    }

    public boolean checkFontSize() {
        float pref_font = FontSizeChanger.loadFontSize(ScreenSettingActivity.this);
        if (size > 1f && pref_font != size) {
            return true;
        }
        return false;
    }

    private SeekBar.OnSeekBarChangeListener seekbar_font_listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            size = (float) progress / 9 + 1;
            seekBar.setProgress(progress);
            imageview_preview.setBackgroundResource(preview_image_arr[progress]);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    private SeekBar.OnSeekBarChangeListener seekbar_filter_listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            colorProgressValue = progress;
            colorB = 0;
            colorG = (int) (progress * 0.8);
            colorR = (int) (progress * 1.7);
            colorA = (int) (progress * 0.6);
            if (check_switch_bl)
                opti_detailsetting_filter.setBackgroundColor(Color.argb(colorA, colorR, colorG, colorB));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private CompoundButton.OnCheckedChangeListener switch_bl_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            check_switch_bl = isChecked;
            if (isChecked) {
                opti_detailsetting_filter.setBackgroundColor(Color.argb(colorA, colorR, colorG, colorB));
            } else {
                seekbar_filter.setProgress(0);
                opti_detailsetting_filter.setBackgroundColor(Color.argb(0, 255, 255, 255));
            }
        }
    };

    @Override
    public void handleDialogClose(Calendar startTime, Calendar endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void finish() {
        if (isTaskRoot()) {
            startActivity(new Intent(ScreenSettingActivity.this, MainActivity.class));
        }
        super.finish();
    }
}
