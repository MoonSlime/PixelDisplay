package com.pixeldp.wellsee;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

import static android.app.Activity.RESULT_OK;

public class ResultPersonalDataDialog extends DialogFragment {

    @BindView(R.id.test_radiogroup_astigmatism)
    RadioGroup radioGroup_astigmatism;
    @BindView(R.id.test_radiogroup_colorblindness)
    RadioGroup radioGroup_colorBlindness;
    @BindView(R.id.test_editText_age)
    EditText editText_age;
    @BindView(R.id.spinner_sex)
    Spinner spinner_sex;

    public enum USER_INPUT_ASTIGMATISM {
        YES, NO, DONTKNOW
    }

    public enum USER_INPUT_COLOR_BLINDNESS {
        YES, NO, DONTKNOW
    }

    public static USER_INPUT_ASTIGMATISM user_input_astigmatism;
    public static USER_INPUT_COLOR_BLINDNESS user_input_color_blindness;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
        int width = (int) ((float) metrics.widthPixels * 0.76f);
        int height = (int) ((float) metrics.heightPixels * 0.64f);
        getDialog().getWindow().setLayout(width, height);
    }

    @OnTextChanged(R.id.test_editText_age)
    public void onTextChanged(CharSequence text) {
        if (text.toString().length() > 0) {
            TestVisualAcuityActivity.age = Integer.valueOf(text.toString());
        }
    }

    @OnItemSelected(R.id.spinner_sex)
    public void onItemSelected(int position) {
        TestVisualAcuityActivity.sex = position - 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_result_presonalinfo, container, false);
        ButterKnife.bind(this, view);

        radioGroup_astigmatism.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch (id) {
                    case R.id.test_buttongroup_astigmatism_yes:
                        user_input_astigmatism = USER_INPUT_ASTIGMATISM.YES;
                        TestVisualAcuityActivity.eyeModel.setLevel_astigmatism(3);
                        break;
                    case R.id.test_buttongroup_astigmatism_no:
                        user_input_astigmatism = USER_INPUT_ASTIGMATISM.NO;
                        TestVisualAcuityActivity.eyeModel.setLevel_astigmatism(0);
                        break;
                    case R.id.test_buttongroup_astigmatism_dontknow:
                        user_input_astigmatism = USER_INPUT_ASTIGMATISM.DONTKNOW;
                        break;
                }
            }
        });

        radioGroup_colorBlindness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {
                switch (id) {
                    case R.id.test_buttongroup_colorblindness_yes:
                        user_input_color_blindness = USER_INPUT_COLOR_BLINDNESS.YES;
                        TestVisualAcuityActivity.eyeModel.setLevel_colorBlindness(3);
                        break;
                    case R.id.test_buttongroup_colorblindness_no:
                        user_input_color_blindness = USER_INPUT_COLOR_BLINDNESS.NO;
                        TestVisualAcuityActivity.eyeModel.setLevel_colorBlindness(0);
                        break;
                    case R.id.test_buttongroup_colorblindness_dontknow:
                        user_input_color_blindness = USER_INPUT_COLOR_BLINDNESS.DONTKNOW;
                        break;
                }
            }
        });

        return view;
    }

    @OnClick(R.id.test_imagebutton_confirm)
    void confirm() {
        if (isInputCorrect()) {
            if (user_input_astigmatism == USER_INPUT_ASTIGMATISM.DONTKNOW) {
                startActivityForResult(new Intent(getActivity(), TestAstigmatismActivity.class), 0);
                getActivity().overridePendingTransition(R.anim.enter_from_right,  R.anim.exit_to_left);
            } else if (user_input_color_blindness == USER_INPUT_COLOR_BLINDNESS.DONTKNOW) {
                startActivityForResult(new Intent(getActivity(), TestColorBlindnessActivity.class), 1);
                getActivity().overridePendingTransition(R.anim.enter_from_right,  R.anim.exit_to_left);
            } else {
                ((TestVisualAcuityActivity) getActivity()).onDialogFinished();
                dismiss();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            ((TestVisualAcuityActivity) getActivity()).onDialogFinished();
            dismiss();
        }
    }


    private boolean isInputCorrect() {
        boolean isIncorrectAge = editText_age.getText().toString().equals("");
        boolean isIncorrectSex = spinner_sex.getSelectedItemPosition() <= 0;
        boolean isIncorrectAstigmatism = radioGroup_astigmatism.getCheckedRadioButtonId() == -1;
        boolean isIncorrectColorBlindness = radioGroup_colorBlindness.getCheckedRadioButtonId() == -1;

        if (isIncorrectAge) {
            Toast.makeText(getActivity(), "나이를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else if (isIncorrectSex) {
            Toast.makeText(getActivity(), "성별을 선택해 주세요.", Toast.LENGTH_SHORT).show();
        } else if (isIncorrectAstigmatism) {
            Toast.makeText(getActivity(), "난시 여부를 선택해 주세요.", Toast.LENGTH_SHORT).show();
        } else if (isIncorrectColorBlindness) {
            Toast.makeText(getActivity(), "색약 여부를 선택해 주세요.", Toast.LENGTH_SHORT).show();
        }

        return !isIncorrectAge && !isIncorrectSex && !isIncorrectAstigmatism && !isIncorrectColorBlindness;
    }
}