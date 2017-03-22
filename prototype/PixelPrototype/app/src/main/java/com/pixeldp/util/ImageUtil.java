package com.pixeldp.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

public class ImageUtil {
    public static Bitmap getRotatedBitmap(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static float getBrightness(int color) {
        float[] hsb = new float[3];
        Color.RGBToHSV((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, hsb);
        return hsb[2] * 255;
    }
}
