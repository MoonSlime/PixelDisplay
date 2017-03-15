package com.pixeldp.nodi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestExplainActivity extends PixelActivity {
    @BindView(R.id.test_explain_imagebutton_next)
    ImageButton next;

    private boolean finishCondition = false;
    private Handler handler;
    private Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            finishCondition = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_explain);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("최적화를 위한 검사");
        getSupportActionBar().setElevation(0);
        handler = new Handler();
    }

    @OnClick({R.id.test_explain_imagebutton_next})
    void nextClick() {
        startActivity(new Intent(getApplicationContext(), TestVisualAcuityActivity.class));
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void finish() {
        if (!finishCondition && isTaskRoot()) {
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG).show();
            finishCondition = true;
            handler.postDelayed(finishRunnable, 3000);
        } else {
            super.finish();
        }
    }
}
