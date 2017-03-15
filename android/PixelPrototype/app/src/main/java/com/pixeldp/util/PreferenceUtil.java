package com.pixeldp.util;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class PreferenceUtil extends BasePreferenceUtil {
    private static PreferenceUtil _instance = null;

    public static synchronized PreferenceUtil instance(Context $context) {
        if (_instance == null)
            _instance = new PreferenceUtil($context);
        return _instance;
    }

    public PreferenceUtil(Context $context) {
        super($context);
    }

    public void put(String $key, HashSet<String> $value) {
        super.put($key, $value);
    }

    public Set<String> get(String $key, Set<String> $value) {
        return super.get($key, $value);
    }

    public void put(String $key, int $value) {
        super.put($key, $value);
    }
    public int get(String $key, int $value) {
        return super.get($key, $value);
    }

}