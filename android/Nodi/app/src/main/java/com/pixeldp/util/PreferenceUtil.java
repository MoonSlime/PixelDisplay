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

    private PreferenceUtil(Context $context) {
        super($context);
    }

    public void put(String $key, HashSet<String> $value) {
        super.put($key, $value);
    }
    public void put(String $key, String $value) {
        super.put($key, $value);
    }
    public void put(String $key, int $value) { super.put($key, $value); }
    public void put(String $key, boolean $value) {
        super.put($key, $value);
    }
    public void put(String $key, float $value) { super.put($key, $value);  }
    public void put(String $key, long $value) { super.put($key, $value);  }


    public Set<String> get(String $key, Set<String> $value) {
        return super.get($key, $value);
    }
    public String get(String $key, String $default) {  return super.get($key, $default); }
    public int get(String $key, int $value) { return super.get($key, $value); }
    public boolean get(String $key, boolean $default) {   return super.get($key, $default);  }
    public float get(String $key, float $value) { return super.get($key, $value); }
    public long get(String $key, long $value) { return super.get($key, $value); }


}