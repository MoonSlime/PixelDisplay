package com.pixeldp.wellsee;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.util.FilterService;
import com.pixeldp.util.FontSizeChanger;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageFragment extends Fragment {
    private int position;

    @Nullable
    @BindView(R.id.main_menu_sub_listview)
    ListView listView;

    public static PageFragment create(int position) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putInt("page", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = null;
        switch (position) {
            case 0:
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_pager_child_main, container, false);
                ButterKnife.bind(this, rootView);
                break;
            case 1:
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_pager_child_sub, container, false);
                ButterKnife.bind(this, rootView);

                ArrayList<String> list = new ArrayList<>();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_menu_sub, R.id.item_menu_sub_textview, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        String menuName = ((TextView) view.findViewById(R.id.item_menu_sub_textview)).getText().toString();

                        switch (menuName) {
                            case "화면 초기화":
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("화면 초기화")
                                        .setMessage("디바이스를 종료 후 재부팅 시키면 화면이 초기화 됩니다. 초기화 하시겠습니까?")
                                        .setPositiveButton(Html.fromHtml("<font color='#009688'>네</font>"), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                FontSizeChanger.set(1.0f, getActivity());
                                                FontSizeChanger.saveFontSize(getActivity(), 1.0f);

                                                if (isMyServiceRunning(FilterService.class))
                                                    getActivity().stopService(new Intent(getActivity(), FilterService.class));
                                            }
                                        })
                                        .setNegativeButton(Html.fromHtml("<font color='#009688'>아니오</font>"), null)
                                        .show();
                                break;
                            case "로그 아웃":
                                PixelAPI api = PixelService.getRetrofit(getActivity());
                                Call<ResponseModel> logout = api.logout();
                                logout.enqueue(new Callback<ResponseModel>() {
                                    @Override
                                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                        if (response != null && response.isSuccessful() && response.body() != null) {
                                            final ResponseModel eyeModel = response.body();
                                            if (eyeModel.getCode() != 200) {
                                                Log.d("debugging_http", eyeModel.toString());
                                                Toast.makeText(getActivity(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                                        Toast.makeText(getActivity(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                        t.printStackTrace();
                                    }
                                });
                                break;
                            case "회사 정보":
                                startActivity(new Intent(getActivity(), CompanyInformationActivity.class));
                                break;
                        }
                    }
                });

                list.add("화면 초기화");
                list.add("로그 아웃");
                list.add("회사 정보");
                break;
        }

        return rootView;
    }

    @Optional
    @OnClick({R.id.main_imagebutton_opti, R.id.main_imagebutton_record, R.id.main_imagebutton_screeninit, R.id.main_imagebutton_test})
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.main_imagebutton_opti:
                intent = new Intent(getActivity(), OptiExplainActivity.class);
                break;
            case R.id.main_imagebutton_test:
                intent = new Intent(getActivity(), TestExplainActivity.class);
                break;
            case R.id.main_imagebutton_record:
                intent = new Intent(getActivity(), RecordListActivity.class);
                break;
            case R.id.main_imagebutton_screeninit:
                intent = new Intent(getActivity(), ScreenSettingActivity.class);
                break;
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    static class MainPagesAdapter extends FragmentPagerAdapter {

        MainPagesAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return create(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}