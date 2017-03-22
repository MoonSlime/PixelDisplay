package com.pixeldp.prototype.device_control;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FocusControl extends Thread {
    private CameraControl cameraControl;
    private OnFocusedListener onFocusedListener;
    private Rect focusArea;
    private Point viewSize;
    private boolean isFocusing;
    private static FocusControl instance;

    private FocusControl(CameraControl cameraControl) {
        this.cameraControl = cameraControl;
        isFocusing = false;
    }

    public static FocusControl getInstance(CameraControl cameraControl) {
        if (instance == null) {
            instance = new FocusControl(cameraControl);
        }

        return instance;
    }

    @Override
    public void run() {
        super.run();

        while (!isInterrupted()) {
            try {
                sleep(300);
            } catch (InterruptedException ie) {
                Log.d("debugging_focus", "FocusControl Interrupted");
                break;
            }

            if (focusArea == null || isFocusing) {
                continue;
            }

            setFocusArea(focusArea);
        }
    }

    @Override
    public synchronized void start() {
        if (isAvailable(cameraControl)) {
            super.start();
        }
    }

    public void shutdown() {
        if (isAvailable(cameraControl) && instance != null) {
            interrupt();
            onFocusedListener = null;
            instance = null;
        }
    }

    private static boolean isAvailable(CameraControl cameraControl) {
        if (cameraControl.getCamera() == null) {
            return false;
        }

        Camera.Parameters parameters = cameraControl.getCamera().getParameters();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (!cameraControl.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) || supportedFocusModes == null || supportedFocusModes.isEmpty() || supportedFocusModes.size() == 1) {
            Log.d("debugging_focus", "has no supported focus mode");
            return false;
        }

        if (parameters.getMaxNumFocusAreas() < 1) {
            Log.d("debugging_focus", "Focus not available.");
            return false;
        }

        return true;
    }

    public void setOnFocusedListener(OnFocusedListener onFocusedListener) {
        this.onFocusedListener = onFocusedListener;
    }

    public void registerFocusArea(FaceDetector.Face face, Point viewSize, Point previewSize) {
        this.viewSize = viewSize;
        Rect newFocusArea = calculateFocusArea(face, viewSize, previewSize);
        if (newFocusArea == null) {
            return;
        }
        focusArea = newFocusArea;
    }

    private void setFocusArea(Rect focusArea) {
        isFocusing = true;

        // Convert from View's width and height to +/- 1000
        Rect focusRect = new Rect();
        focusRect.set(focusArea.left * 2000 / viewSize.x - 1000,
                focusArea.top * 2000 / viewSize.y - 1000,
                focusArea.right * 2000 / viewSize.x - 1000,
                focusArea.bottom * 2000 / viewSize.y - 1000);
        clampRect(focusRect);

        try {
            List<Camera.Area> focusAreas = cameraControl.getCamera().getParameters().getFocusAreas();

            if (focusAreas != null && focusRect.equals(focusAreas.get(0).rect)) {
                isFocusing = false;
                return;
            }
        } catch (NumberFormatException nfe) {
            // setfocusAreas 가 한번도 호출되지 않았다면 getFocusAreas 에서 NumberFormatException 발생 / android bug
        }

        // Submit focus area to camera
        ArrayList<Camera.Area> focusAreas = new ArrayList<>();
        focusAreas.add(new Camera.Area(focusRect, 1000));

        Camera.Parameters parameters = cameraControl.getCamera().getParameters();
        parameters.setFocusAreas(focusAreas);
        cameraControl.getCamera().setParameters(parameters);

        cameraControl.getCamera().autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean focused, Camera camera) {
                if (focused && onFocusedListener != null) {
                    onFocusedListener.onFocused();
                    isFocusing = false;
                }
            }
        });
    }

    private static Rect calculateFocusArea(FaceDetector.Face face, Point viewSize, Point previewSize) {
        PointF glabellaPoint = new PointF();
        face.getMidPoint(glabellaPoint);

        PointF ratio = new PointF(viewSize.x / (float) previewSize.y, viewSize.y / (float) previewSize.x); // be careful
        float widthRatio = ratio.x;
        float heightRatio = ratio.y;

        int realX = (int) (glabellaPoint.x * widthRatio);
        int realY = (int) (glabellaPoint.y * heightRatio);

        int halfEyeDist = (int) (widthRatio * face.eyesDistance() / 2.0f);
        int RECTANGLE_SIZE = 50;

        int left = realX - halfEyeDist - RECTANGLE_SIZE;
        int top = realY - RECTANGLE_SIZE - 20;
        int right = realX + halfEyeDist + RECTANGLE_SIZE;
        int bottom = realY + RECTANGLE_SIZE + 20;

        if (left < 0 || top < 0 || right < 0 || bottom < 0) {
            return null;
        }
        if (left >= right || top >= bottom) {
            return null;
        }

        return new Rect(left, top, right, bottom);
    }

    private static void clampRect(Rect rect) {
        rect.left = Math.min(Math.max(rect.left, -1000), 1000);
        rect.top = Math.min(Math.max(rect.top, -1000), 1000);
        rect.right = Math.min(Math.max(rect.right, -1000), 1000);
        rect.bottom = Math.min(Math.max(rect.bottom, -1000), 1000);
    }

    public Rect getFocusArea() {
        return focusArea;
    }

    interface OnFocusedListener {
        void onFocused();
    }
}
