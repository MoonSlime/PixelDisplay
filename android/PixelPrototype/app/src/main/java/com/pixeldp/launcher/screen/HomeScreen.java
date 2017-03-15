package com.pixeldp.launcher.screen;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.UserModel;
import com.pixeldp.launcher.R;
import com.pixeldp.prototype.IntroActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_homescreen);

        Fragment fragment = new AppGridFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homscreen_fragment_place, fragment, "APPGRID");
        fragmentTransaction.commit();

        PixelAPI api = PixelService.getRetrofit(this);
        Call<UserModel> login = api.login(null);
        login.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    UserModel userModel = response.body();
                    if (userModel.getCode() != 200) {
                        Intent intent = new Intent(HomeScreen.this, IntroActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Toast.makeText(HomeScreen.this, "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

}
