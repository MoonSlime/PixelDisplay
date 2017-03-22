package com.pixeldp.http;

import android.app.Activity;
import android.util.Log;

import com.pixeldp.model.ResponseModel;
import com.pixeldp.util.PreferenceUtil;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class LoginHandler {
    private static boolean isSuccessful;

    public static boolean login(final String accountName, final Activity activity) {

        Thread loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                PixelAPI api = PixelService.getRetrofit(activity);
                Call<ResponseModel> login = api.login(accountName);

                Response<ResponseModel> response = null;
                try {
                    response = login.execute();
                    isSuccessful = (response != null && response.isSuccessful() && response.body() != null && response.body().getCode()==200);
                    if(isSuccessful) {
                        PreferenceUtil.instance(activity).put("accountName", accountName);
                    }
                } catch (IOException e) {
                    isSuccessful = false;
                    e.printStackTrace();
                }
            }
        });
        loginThread.start();
        try {
            loginThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isSuccessful;
    }
}