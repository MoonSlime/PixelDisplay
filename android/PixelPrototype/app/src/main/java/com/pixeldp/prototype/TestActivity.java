package com.pixeldp.prototype;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pixeldp.prototype.device_control.CameraControl;
import com.pixeldp.prototype.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TestActivity extends Activity {
    @Bind(R.id.button_test)
    Button buttonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        final CameraControl cameraControl = CameraControl.getInstance(getApplicationContext());
        synchronized (cameraControl) {
            cameraControl.initiateCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        if (cameraControl.getCamera() == null) {
            Log.d("debugging_camera", "cannot initiate camera");
            Toast.makeText(getApplicationContext(), "cannot initiate camera", Toast.LENGTH_LONG).show();
            finish();
        }

        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float viewAngle = cameraControl.getCamera().getParameters().getVerticalViewAngle();
                double width = Math.tan(Math.toRadians(viewAngle/2.0f)) * 2 * 500;
            }
        });
    }
}

