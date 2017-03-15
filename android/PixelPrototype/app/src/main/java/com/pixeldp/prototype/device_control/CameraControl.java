package com.pixeldp.prototype.device_control;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import static android.content.Context.WINDOW_SERVICE;

public class CameraControl extends HandlerThread {

    private AtomicReference<Camera> camera;
    private Context context;
    private int cameraID;
    private static CameraControl instance;

    private CameraControl(Context context) {
        super(CameraControl.class.getSimpleName());
        this.context = context;
        camera = new AtomicReference<Camera>();
    }

    public static CameraControl getInstance(Context context) {
        if (instance == null) {
            instance = new CameraControl(context);
            instance.start(); // this should be here, not constructor
        }

        return instance;
    }

    public Camera getCamera() {
        return camera.get();
    }

    public Context getContext() {
        return context;
    }

    public void initiateCamera(final int cameraID) {
        Handler handler = new Handler(getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Log.d("debugging_camera", "No camera on this device");
                    camera.set(null);
                    notifyCameraOpened();
                    return;
                }

                try {
                    CameraControl.this.cameraID = cameraID;
                    camera.set(Camera.open(cameraID));
                } catch (Exception e) {
                    Log.d("debugging_camera", e.getClass().getSimpleName());
                    e.printStackTrace();

                    camera.set(null);
                    notifyCameraOpened();
                    return;
                }

                Camera.Parameters parameters = camera.get().getParameters();

                Camera.Size biggestPreviewSize = parameters.getSupportedPreviewSizes().get(0);
                parameters.setPreviewSize(biggestPreviewSize.width, biggestPreviewSize.height);

                Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        camera.get().setDisplayOrientation(90);
                        parameters.setRotation(90);
                        break;
                    case Surface.ROTATION_180:
                        camera.get().setDisplayOrientation(270);
                        parameters.setRotation(270);
                        break;
                    case Surface.ROTATION_270:
                        camera.get().setDisplayOrientation(180);
                        parameters.setRotation(180);
                        break;
                    case Surface.ROTATION_90:
                        break;
                }

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

                camera.get().setParameters(parameters);

                notifyCameraOpened();
            }
        });
        try {
            wait();
        } catch (InterruptedException e) {
            Log.d("debugging_camera", "wait was interrupted");
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.get().setPreviewCallback(null);
            camera.get().stopPreview();
            camera.get().release();
            camera.set(null);

            quit();
            instance = null;
        }
    }

    public Bitmap getBitmapFromOnPreviewFrame(byte[] data) {
        Camera.Parameters parameters = camera.get().getParameters();

        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

        byte[] bytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private synchronized void notifyCameraOpened() {
        notify();
    }

    public int getCameraID() {
        return cameraID;
    }

    /*
    private ArrayList<Camera.Size> getSupportedSizes() {
        if (camera == null) {
            return null;
        }

        List<Camera.Size> supportedPictureSizes = camera.get().getParameters().getSupportedPictureSizes();
        List<Camera.Size> supportedPreviewSizes = camera.get().getParameters().getSupportedPreviewSizes();

        ArrayList<Camera.Size> supportedSizes = new ArrayList<Camera.Size>();

        for (Camera.Size pictureSize : supportedPictureSizes) {
            for (Camera.Size previewSize : supportedPreviewSizes) {
                if (pictureSize.equals(previewSize)) {
                    supportedSizes.add(pictureSize);
                }
            }
        }

        return supportedSizes;
    }

    private void showSizeInfo() {
        Camera.Parameters parameters = camera.get().getParameters();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size pictureS : supportedPictureSizes) {
            Log.d("debugging_camera", "supportedPictureSizes : (" + pictureS.width + ", " + pictureS.height + ")");
        }
        for (Camera.Size previewS : supportedPreviewSizes) {
            Log.d("debugging_camera", "supportedPreviewSizes : (" + previewS.width + ", " + previewS.height + ")");
        }
    }
    */
}
