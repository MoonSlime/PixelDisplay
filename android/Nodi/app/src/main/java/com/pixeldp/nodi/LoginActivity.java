package com.pixeldp.nodi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pixeldp.http.LoginHandler;
import com.pixeldp.util.PreferenceUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class LoginActivity extends PixelActivity {
    @BindView(R.id.login_imagebutton_google_login)
    ImageButton imageButton_login;
    @BindView(R.id.login_linearlayout_area)
    LinearLayout linearLayout;
    @BindView(R.id.login_logo_wellsee)
    ImageView logo;

    private List<Account> google_accounts;
    AccountAdapter adapter;
    private static final String REAL_WIDTH = "REAL_WIDTH";
    private static final String REAL_HEIGHT = "REAL_HEIGHT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        analyticsStart(this);
        imageButton_login.setClickable(false);
        getDisplaySize();
         LogoAnimation animation = new LogoAnimation(0, 0, 0, -400, 1000, linearLayout);
        logo.setAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        google_accounts = getDeviceAccount();
        if (google_accounts.size() > 1) {
            adapter = new AccountAdapter(LoginActivity.this, R.layout.font_list, google_accounts);
            adapter.add(new Account("계정 추가", "com.pixel"));
        }
    }

    private List<Account> getDeviceAccount() {
        List<Account> google_accounts;
        try {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccounts();

            google_accounts = new ArrayList<>();
            for (Account account : accounts) {
                if (account.type.equals("com.google")) { //구글 계정 구분
                    google_accounts.add(account);
                }
            }
        } catch (SecurityException e) {
            google_accounts = null;
            e.printStackTrace();
        }

        return google_accounts;
    }

    @OnCheckedChanged(R.id.login_checkbox_agree)
    public void checkboxToggled(boolean isChecked) {
        if (isChecked) {
            imageButton_login.setClickable(isChecked);
            imageButton_login.setBackgroundResource(R.drawable.login_imagebutton_google_login_checked);
        } else {
            imageButton_login.setClickable(isChecked);
            imageButton_login.setBackgroundResource(R.drawable.login_imagebutton_google_login);
        }
    }

    @OnClick({R.id.login_imagebutton_google_login, R.id.login_text_agree})
    public void onClickLogin(View v) {
        switch (v.getId()) {
            case R.id.login_imagebutton_google_login:
                switch (google_accounts.size()) {
                    case 0:
                        requestNewAccount();
                        break;
                    case 1:
                        login(google_accounts.get(0).name);
                        break;
                    default: // 계정이 2개 이상일 때
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Choose account.")
                                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String accountName = adapter.getItem(id).name;
                                        dialog.dismiss();

                                        if (accountName.equals("계정 추가")) {
                                            requestNewAccount();
                                        } else {
                                            login(accountName);
                                        }
                                    }
                                }).show();
                        break;
                }
                break;
            case R.id.login_text_agree:
                startActivity(new Intent(LoginActivity.this, AccessTermsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);

                break;
        }
    }

    private void requestNewAccount() {
        Intent intent_newAccount = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent_newAccount.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
        startActivity(intent_newAccount);
    }

    private void login(String accountName) {
        if (LoginHandler.login(accountName, LoginActivity.this)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private class AccountAdapter extends ArrayAdapter<Account> {
        private Context context;
        private int resourceID;
        private List<Account> items;

        AccountAdapter(@NonNull Context context, @LayoutRes int resourceID, @NonNull List<Account> items) {
            super(context, resourceID, items);

            this.context = context;
            this.resourceID = resourceID;
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, View v, @NonNull ViewGroup parent) {
            CheckedTextView mView = (CheckedTextView) v;
            if (mView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = (CheckedTextView) vi.inflate(resourceID, null);
            }

            if (items.get(position) != null) {
                mView.setText(items.get(position).name);
            }

            return mView;
        }
    }

    private void getDisplaySize() {
        int realWidth = PreferenceUtil.instance(this).get(REAL_WIDTH, 0);
        int realHeight = PreferenceUtil.instance(this).get(REAL_HEIGHT, 0);
        if (realWidth != 0 && realHeight != 0) return;

        Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 17) {
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            realWidth = realMetrics.widthPixels;
            realHeight = realMetrics.heightPixels;
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                realWidth = display.getWidth();
                realHeight = display.getHeight();
            }
        } else {
            realWidth = display.getWidth();
            realHeight = display.getHeight();
        }
        PreferenceUtil.instance(this).put(REAL_WIDTH, realWidth);
        PreferenceUtil.instance(this).put(REAL_HEIGHT, realHeight);
    }


    class LogoAnimation extends TranslateAnimation implements Animation.AnimationListener {
        LinearLayout linearLayout;

        public LogoAnimation(int x, int tx, int y, int ty, int duration, LinearLayout layout) {
            super(x, tx, y, ty);
            setFillAfter(true);
            setInterpolator(new AccelerateDecelerateInterpolator());
            setRepeatCount(0);
            setDuration(duration);
            setAnimationListener(this);
            this.linearLayout = layout;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            boolean isSuccessful = LoginHandler.login(null, LoginActivity.this);
            if (isSuccessful) {
                startActivity(new Intent(getApplication(), MainActivity.class));
                finish();
                overridePendingTransition(R.anim.enter_from_right,  R.anim.exit_to_left);
            } else {
                linearLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }
}
