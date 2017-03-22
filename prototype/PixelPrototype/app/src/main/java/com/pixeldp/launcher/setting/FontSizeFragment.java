package com.pixeldp.launcher.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pixeldp.prototype.R;
import com.pixeldp.util.PreferenceUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FontSizeFragment extends Fragment {
    @Bind(R.id.fragment_fontsize_radio)
    RadioGroup radioGroup;
    @Bind(R.id.fragment_fontsize_apply)
    TextView dialog_font_apply;

    private int groupArr[] = {R.id.fragment_fontsize_font1, R.id.fragment_fontsize_font2, R.id.fragment_fontsize_font3, R.id.fragment_fontsize_font4, R.id.fragment_fontsize_font5, R.id.fragment_fontsize_font6};
    private int checkedValue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fontsize, container, false);
        ButterKnife.bind(this, view);
        checkedValue = PreferenceUtil.instance(getActivity()).get("FONT_SIZE", 0);
        RadioButton button = (RadioButton)view.findViewById(groupArr[checkedValue + 1]);
        button.setChecked(true);
        return view;
    }

    @OnClick({R.id.fragment_fontsize_apply})
    public void apply() {
        PreferenceUtil.instance(getContext()).put("FONT_SIZE", checkedValue);
        //Settings.System.putFloat(context.getContentResolver(), Settings.System.FONT_SCALE, systemFontSize);*/
    }

    @OnClick({R.id.fragment_fontsize_font1, R.id.fragment_fontsize_font2, R.id.fragment_fontsize_font3, R.id.fragment_fontsize_font4, R.id.fragment_fontsize_font5, R.id.fragment_fontsize_font6})
    public void onRadioButtonClicked(RadioButton radioButton) {
        boolean checked = radioButton.isChecked();
        if (checked) {
            String size = radioButton.getTag().toString();
            switch (size) {
                case "14":
                    checkedValue = -1;
                    break;
                case "16":
                    checkedValue = 0;
                    break;
                case "18":
                    checkedValue = 1;
                    break;
                case "20":
                    checkedValue = 2;
                    break;
                case "24":
                    checkedValue = 3;
                    break;
                case "28":
                    checkedValue = 4;
                    break;
            }
        }
    }
}
