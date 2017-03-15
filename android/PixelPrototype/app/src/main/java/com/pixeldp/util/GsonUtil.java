package com.pixeldp.util;

import com.google.gson.Gson;

public class GsonUtil {
    private static Gson gson = new Gson();

    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    public static <T> T deserealize(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
