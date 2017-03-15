package com.pixeldp.util;

import android.content.Context;
import android.provider.Settings;

public class FontSizeChanger {
    private static final String SYSTEM_FONT_SIZE = "SYSTEM_FONT_SIZE";

    public static void set(float size, Context context) {
        Settings.System.putFloat(context.getContentResolver(), Settings.System.FONT_SCALE, size);
    }

    public static float loadFontSize(Context context) {
        return PreferenceUtil.instance(context).get(SYSTEM_FONT_SIZE, 1.0f);
    }

    public static void saveFontSize(Context context, float size) {
        PreferenceUtil.instance(context).put(SYSTEM_FONT_SIZE, size);
    }
}
