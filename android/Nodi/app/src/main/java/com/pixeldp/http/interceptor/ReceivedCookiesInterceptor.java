package com.pixeldp.http.interceptor;

import android.content.Context;


import com.pixeldp.http.PixelService;
import com.pixeldp.util.PreferenceUtil;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {
    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();
            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }
            PreferenceUtil.instance(context).put(PixelService.SHARED_PREFERENCE_NAME_COOKIE, cookies);
        }
        return originalResponse;
    }
}