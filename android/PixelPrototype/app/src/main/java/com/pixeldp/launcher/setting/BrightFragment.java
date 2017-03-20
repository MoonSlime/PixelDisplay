package com.pixeldp.launcher.setting;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.pixeldp.prototype.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.feeeei.circleseekbar.CircleSeekBar;

public class BrightFragment extends Fragment implements CircleSeekBar.OnSeekBarChangeListener {
    @Bind(R.id.fragment_bright_progress)
    TextView fragment_bright_progress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bright, container, false);
        ButterKnife.bind(this, view);

        CircleSeekBar seekBar = (CircleSeekBar) view.findViewById(R.id.fragment_bright_seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        try {
            int screen_bright = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            seekBar.setCurProcess(screen_bright);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        getSystemCanWrite();

        return view;
    }


    @Override
    public void onChanged(CircleSeekBar circleSeekBar, int progress) {
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progress);
        WindowManager.LayoutParams getAttribute = getActivity().getWindow().getAttributes();
        getAttribute.screenBrightness = (float) progress / 255;
        getActivity().getWindow().setAttributes(getAttribute);
        float bright = ((float) progress / 255) * 100;
        fragment_bright_progress.setText("밝기 : " + (int) bright);
    }

    private void getSystemCanWrite() {
        try {
            if (android.provider.Settings.System.getInt(getActivity().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                android.provider.Settings.System.putInt(getActivity().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }
}
