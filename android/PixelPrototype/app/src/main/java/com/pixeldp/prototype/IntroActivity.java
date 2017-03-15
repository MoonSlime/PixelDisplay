package com.pixeldp.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.model.UserModel;
import com.pixeldp.launcher.R;
import com.pixeldp.launcher.setting.SettingActivity;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        PixelAPI api = PixelService.getRetrofit(this);
        Call<UserModel> login = api.login(null);
        login.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    ResponseModel type = response.body();
                    if (type.getCode() == 200) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(IntroActivity.this, SettingActivity.class));
                                finish();
                            }
                        }, 2000);
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                                IntroActivity.this.finish();
                            }
                        }, 2000);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}
