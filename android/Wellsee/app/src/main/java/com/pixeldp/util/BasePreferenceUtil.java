package com.pixeldp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class BasePreferenceUtil {
    private SharedPreferences _sharedPreferences;

    protected BasePreferenceUtil(Context $context) {
        super();
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences($context);
    }

    protected void put(String $key, HashSet<String> $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putStringSet($key, $value);
        editor.commit();
    }

    protected void put(String $key, String $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putString($key, $value);
        editor.commit();
    }

    protected void put(String $key, int $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt($key, $value);
        editor.commit();
    }

    protected void put(String $key, boolean $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putBoolean($key, $value);
        editor.commit();
    }

    protected void put(String $key, float $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putFloat($key, $value);
        editor.commit();
    }

    protected void put(String $key, long $value) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putLong($key, $value);
        editor.commit();
    }

    protected Set<String> get(String $key, Set<String> $default) {  return _sharedPreferences.getStringSet($key, $default); }
    protected String get(String $key, String $default) { return _sharedPreferences.getString($key, $default);  }
    protected int get(String $key, int $default) { return _sharedPreferences.getInt($key, $default);  }
    protected float get(String $key, float $default) {  return _sharedPreferences.getFloat($key, $default);  }
    protected boolean get(String $key, boolean $default) {  return _sharedPreferences.getBoolean($key, $default);   }
    protected long get(String $key, long $default) {  return _sharedPreferences.getLong($key, $default);   }

}