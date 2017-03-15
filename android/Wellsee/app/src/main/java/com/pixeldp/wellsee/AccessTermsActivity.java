package com.pixeldp.wellsee;

import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccessTermsActivity extends PixelActivity {
    @BindView(R.id.access_terms_textView)
    TextView textView_accessTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_terms);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("이용약관");
        textView_accessTerms.setText(R.string.access_terms);
    }
}