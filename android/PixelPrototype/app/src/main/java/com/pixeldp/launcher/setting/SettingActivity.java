package com.pixeldp.launcher.setting;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeModel;
import com.pixeldp.prototype.MainActivity;
import com.pixeldp.launcher.R;
import com.pixeldp.prototype.IntroActivity;
import com.pixeldp.util.PreferenceUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity {
    @Bind(R.id.acivity_main_drawerlayout)
    DrawerLayout main_drawerlayout;
    @Bind(R.id.acivity_main_list)
    ListView main_list;
    @Bind(R.id.acivity_main_frame)
    FrameLayout main_frame;
    @Bind(R.id.activity_main_diopter)
    TextView main_diopter;
    private ActionBarDrawerToggle main_toggle;
    public static final String CAPTURE_CODE = "CAPTURE_CODE";
    public static final int UPDATE_EYE_INFO = 1;
    public static final int NEW_EYE_INFO = 0;
    private final String[] main_nav = {"글씨크기", "밝기 조절", "보호필터 추가", "적외선 촬영"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, main_nav);
        main_list.setAdapter(arrayAdapter);
        main_list.setOnItemClickListener(new ListClickListener());
        main_toggle = new ActionBarDrawerToggle(this, main_drawerlayout, R.string.open_drawer, R.string.close_drawer);
        main_drawerlayout.setDrawerListener(main_toggle);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }

        PixelAPI api = PixelService.getRetrofit(this);
        Call<EyeModel> getEyeInfo = api.getEyeInfo();
        getEyeInfo.enqueue(new Callback<EyeModel>() {
            @Override
            public void onResponse(Call<EyeModel> call, Response<EyeModel> response) {
                if (response != null && response.isSuccessful() && response.body() != null) {
                    EyeModel type = response.body();
                    Intent intent;
                    switch (type.getCode()) {
                        case 201:
                            intent = new Intent(SettingActivity.this, MainActivity.class);
                            PreferenceUtil.instance(SettingActivity.this).put(CAPTURE_CODE, NEW_EYE_INFO);
                            startActivity(intent);
                            break;
                        case 101:
                            intent = new Intent(SettingActivity.this, IntroActivity.class);
                            startActivity(intent);
                            break;
                        case 200:
                            main_diopter.setText("시력 : " + type.getLeftDi() + " Diopter");
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<EyeModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        main_toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        main_toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return main_toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private class ListClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    Fragment fragment = new FontSizeFragment();
                    moveFragment(fragment);
                    break;
                case 1:
                    fragment = new BrightFragment();
                    moveFragment(fragment);
                    break;
                case 2:
                    fragment = new FilterFragment();
                    moveFragment(fragment);
                    break;
                case 3:
                    Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                    PreferenceUtil.instance(getApplicationContext()).put(CAPTURE_CODE, UPDATE_EYE_INFO);
                    startActivity(intent);
                    finish();
                    break;
            }
            main_drawerlayout.closeDrawer(main_list);
        }
    }


    private void moveFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.acivity_main_frame, fragment);
        fragmentTransaction.commit();
    }

}
