package com.pixeldp.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageUtil {
    public static Bitmap getRotatedBitmap(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
}
