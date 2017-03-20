package com.pixeldp.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.UserModel;
import com.pixeldp.prototype.R;
import com.pixeldp.launcher.setting.SettingActivity;
import com.pixeldp.util.GsonUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.login_textview_email)
    EditText login_textview_email;
    @Bind(R.id.login_textview_password)
    EditText login_textview_password;
    @Bind(R.id.login_imagebutton_login)
    ImageButton login_imagebutton_login;
    @Bind(R.id.login_imagebutton_join)
    ImageButton login_imagebutton_join;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setButtonVisibility(View.VISIBLE);

        login_textview_email.setText("eastfar1324@naver.com");
        login_textview_password.setText("0000");
    }

    private void setButtonVisibility(int value) {
        login_textview_email.setVisibility(value);
        login_textview_password.setVisibility(value);
        login_imagebutton_login.setVisibility(value);
        login_imagebutton_join.setVisibility(value);
    }

    @OnClick({R.id.login_imagebutton_login})
    void loginClick() {
        String emailAddress = login_textview_email.getText().toString();
        String password = login_textview_password.getText().toString();

        PixelAPI api = PixelService.getRetrofit(this);
        String serializedUserModel = GsonUtil.serialize(new UserModel(emailAddress, password));
        Call<UserModel> login = api.login(serializedUserModel);
        login.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    UserModel userModel = response.body();
                    if (userModel.getCode() != 200) {
                        Log.d("debugging_http", userModel.toString());
                        Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @OnClick({R.id.login_imagebutton_join})
    void joinClick() {
        startActivity(new Intent(LoginActivity.this, JoinActivity.class));
    }
}
