package com.pixeldp.http;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.pixeldp.http.interceptor.AddCookiesInterceptor;
import com.pixeldp.http.interceptor.ReceivedCookiesInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PixelService {
    public static final String BASE_URL = "http://pixeldisplay.cafe24.com/";
    public static final String SHARED_PREFERENCE_NAME_COOKIE = "COOKIE";

    public static PixelAPI getRetrofit(Context context) {
        return (PixelAPI) retrofit(context, PixelAPI.class);
    }

    public static Object retrofit(Context context, Class<?> serviceName) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ReceivedCookiesInterceptor(context))
                .addNetworkInterceptor(new AddCookiesInterceptor(context))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(okHttpClient)
                .build();

        return retrofit.create(serviceName);
    }
}
