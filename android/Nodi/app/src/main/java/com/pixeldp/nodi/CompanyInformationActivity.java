package com.pixeldp.nodi;

import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

    public class CompanyInformationActivity extends PixelActivity {
        @BindView(R.id.company_info_textview)
        TextView textView_companyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_information);
        ButterKnife.bind(this);
        analyticsStart(this);
        getSupportActionBar().setTitle("회사 정보");

        textView_companyInfo.setText(R.string.company_info);
    }
}
