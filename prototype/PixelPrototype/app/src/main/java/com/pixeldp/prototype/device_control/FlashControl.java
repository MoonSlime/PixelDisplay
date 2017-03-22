package com.pixeldp.prototype.device_control;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class FlashControl extends Thread {

    private CameraControl cameraControl;
    private OnStateChangedListener onStateChangedListener;
    private String Flash_Mode_Previous = Camera.Parameters.FLASH_MODE_OFF;
    private static FlashControl instance;

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_TOP = 1;
    public static final int POSITION_RIGHT = 2;
    public static final int POSITION_BOTTOM = 3;

    private static int positionFromCamera = POSITION_BOTTOM;

    private FlashControl(CameraControl cameraControl) {
        this.cameraControl = cameraControl;
        positionFromCamera = POSITION_BOTTOM;
    }

    public static FlashControl getInstance(CameraControl cameraControl) {
        if (instance == null) {
            instance = new FlashControl(cameraControl);
        }
        return instance;
    }

    public static boolean isAvailable(CameraControl cameraControl) {
        if (cameraControl.getCamera() == null) {
            return false;
        }

        Camera.Parameters params = cameraControl.getCamera().getParameters();
        List<String> supportedFlashModes = params.getSupportedFlashModes();
        if (!cameraControl.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) || supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1) {
            Log.d("debugging_flash", "has no supported flash mode");
            return false;
        }

        for (String flashMode : supportedFlashModes) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
        }
        return false;
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.onStateChangedListener = onStateChangedListener;
    }

    public void turnOnFlash() {
        Camera.Parameters params = cameraControl.getCamera().getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cameraControl.getCamera().setParameters(params);
    }

    public void turnOffFlash() {
        Camera.Parameters params = cameraControl.getCamera().getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        cameraControl.getCamera().setParameters(params);
    }

    @Override
    public void run() {
        super.run();

        while (!isInterrupted()) {
            String flashMode = cameraControl.getCamera().getParameters().getFlashMode();

            if (!Flash_Mode_Previous.equals(flashMode) &&  onStateChangedListener != null ) {
                switch (flashMode) {
                    case Camera.Parameters.FLASH_MODE_TORCH:
                        onStateChangedListener.onStateChanged(true);
                        break;
                    case Camera.Parameters.FLASH_MODE_OFF:
                        onStateChangedListener.onStateChanged(false);
                        break;
                }
            }

            Flash_Mode_Previous = flashMode;
        }
    }

    public void shutdown() {
        if ( instance != null ) {
            interrupt();
            onStateChangedListener = null;
            instance = null;
        }
    }

    private interface OnStateChangedListener {
        void onStateChanged(boolean isOn);
    }

    public static int getPostionFromCamera() {
        return positionFromCamera;
    }

    public static boolean isHorizontallyPositionedFromCamera() {
        return (positionFromCamera % 2) == 0;
    }
}
