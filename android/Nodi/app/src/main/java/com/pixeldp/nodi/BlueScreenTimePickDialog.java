package com.pixeldp.nodi;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TimePicker;


import com.pixeldp.util.DialogCloseListener;
import com.pixeldp.util.PreferenceUtil;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlueScreenTimePickDialog extends DialogFragment {

    @BindView(R.id.blue_screen_timepicker_start)
    TimePicker timePicker_start;
    @BindView(R.id.blue_screen_timepicker_end)
    TimePicker timePicker_end;

    private Calendar startTime;
    private Calendar endTime;

    public static final String START_HOUR = "START_HOUR";
    public static final String START_MIN = "START_MIN";
    public static final String END_HOUR = "END_HOUR";
    public static final String END_MIN = "END_MIN";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @OnClick({R.id.blue_screen_textview_ok, R.id.blue_screen_textview_cancel})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.blue_screen_textview_ok:
                startTime = adjustTime(startTime, timePicker_start.getCurrentHour(), timePicker_start.getCurrentMinute());
                endTime = adjustTime(endTime, timePicker_end.getCurrentHour(), timePicker_end.getCurrentMinute());
                PreferenceUtil.instance(getActivity()).put(START_HOUR, startTime.get(Calendar.HOUR_OF_DAY));
                PreferenceUtil.instance(getActivity()).put(START_MIN, startTime.get(Calendar.MINUTE));
                PreferenceUtil.instance(getActivity()).put(END_HOUR, endTime.get(Calendar.HOUR_OF_DAY));
                PreferenceUtil.instance(getActivity()).put(END_MIN, endTime.get(Calendar.MINUTE));

                dismiss();
                break;
            case R.id.blue_screen_textview_cancel:
                dismiss();
                break;
        }
    }

    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener)
            ((DialogCloseListener) activity).handleDialogClose(startTime, endTime);
    }

    private Calendar adjustTime(Calendar calendar, int hour, int min) {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getDialog().getWindow().setAttributes(lpWindow);
        DisplayMetrics metrics = new DisplayMetrics();
        getDialog().getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) ((float) metrics.widthPixels * 0.85f);
        int height = (int) ((float) metrics.heightPixels * 0.9f);
        getDialog().getWindow().setLayout(width, height);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_blue_screen_time_pick, container, false);
        ButterKnife.bind(this, view);
        int startHour = PreferenceUtil.instance(getActivity()).get(START_HOUR, 18);
        int startMin = PreferenceUtil.instance(getActivity()).get(START_MIN, 0);
        int endHour = PreferenceUtil.instance(getActivity()).get(END_HOUR, 0);
        int endMin = PreferenceUtil.instance(getActivity()).get(END_MIN, 0);

        timePicker_start.setCurrentHour(startHour);
        timePicker_start.setCurrentMinute(startMin);
        timePicker_end.setCurrentHour(endHour);
        timePicker_end.setCurrentMinute(endMin);
        return view;
    }
}

