package com.pixeldp.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.UserModel;
import com.pixeldp.launcher.R;
import com.pixeldp.launcher.setting.SettingActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {
    @Bind(R.id.fragment_join_email)
    EditText join_email;
    @Bind(R.id.fragment_join_password)
    EditText join_password;
    @Bind(R.id.fragment_join_age)
    EditText join_age;
    @Bind(R.id.fragment_join_phone)
    EditText join_phone;
    @Bind(R.id.fragment_join_check_male)
    RadioButton check_male;
    @Bind(R.id.fragment_join_check_female)
    RadioButton check_female;
    @Bind(R.id.fragment_join_finish)
    Button join_finish;

    private UserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);

        userModel = new UserModel();

        join_email.setText("eastfar1324@naver.com");
        join_password.setText("0000");
        join_phone.setText("01039035201");
        join_age.setText("25");
    }

    @OnClick({R.id.fragment_join_finish})
    public void finish() {
        userModel.setEmailAddress(join_email.getText().toString());
        userModel.setPassword(join_password.getText().toString());
        userModel.setPhoneNum(join_phone.getText().toString());
        userModel.setAge(Integer.valueOf(join_age.getText().toString()));

        PixelAPI api = PixelService.getRetrofit(this);
        Call<UserModel> join = api.join(userModel.toString());
        join.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    UserModel userModel = response.body();
                    if (userModel.getCode() != 200) {
                        Log.d("debugging_http", userModel.toString());
                        Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(JoinActivity.this, SettingActivity.class);
                        startActivity(i);
                        JoinActivity.this.overridePendingTransition(0, 0);
                        JoinActivity.this.finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(JoinActivity.this, "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @OnCheckedChanged(R.id.fragment_join_check_female)
    public void onCheckedFemale(boolean checked) {
        if (checked) userModel.setSex(1);
    }

    @OnCheckedChanged(R.id.fragment_join_check_male)
    public void onCheckedMale(boolean checked) {
        if (checked) userModel.setSex(0);
    }
}
