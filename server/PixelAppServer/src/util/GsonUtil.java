package util;

import com.google.gson.Gson;
import model.ResponseModel;

import java.lang.reflect.Type;

public class GsonUtil {
    private static Gson gson = new Gson();

    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    public static <T> T deserealize(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T deserealize(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}