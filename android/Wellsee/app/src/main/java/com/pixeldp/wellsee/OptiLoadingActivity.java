package com.pixeldp.wellsee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;

import com.pixeldp.util.PreferenceUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OptiLoadingActivity extends PixelActivity {
    private final String accountName = PreferenceUtil.instance(OptiLoadingActivity.this).get("accountName", "사용자").split("@")[0];
    private final String sentence = "님의 눈 정보에 바탕한\n" + "최적화 화면을 구성중입니다.";

    @BindView(R.id.opti_textview_loading)
    TextView textview_loading;

    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(OptiLoadingActivity.this, OptiPreviewAcitivity.class));
            finish();
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opti_loading);
        ButterKnife.bind(this);
        analyticsStart(this);
        String text = accountName + "  " + sentence;
        Spannable spannable = new SpannableString(text);
        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, accountName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textview_loading.setText(spannable, TextView.BufferType.SPANNABLE);
        handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
    }
}
