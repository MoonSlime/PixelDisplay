package com.pixeldp.prototype;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import com.pixeldp.prototype.device_control.BluetoothControl;
import com.pixeldp.prototype.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class RaspberryPiActivity extends Activity {
    private BluetoothControl bluetoothControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raspberry_pi);
        ButterKnife.bind(this);

        try {
            if (bluetoothControl == null) {
                bluetoothControl = BluetoothControl.getInstance(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable ex) {
                        Toast.makeText(getApplicationContext(), "Cannot connect to device.", Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                        finish();
                    }
                });

                if (!bluetoothControl.isEnabled()) {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BluetoothControl.REQUEST_ENABLE_BLUETOOTH);
                }

                bluetoothControl.connectToPairedDevice();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Cannot connect to device.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

    @OnClick(R.id.button_capture)
    public void onClickButtonCapture() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        bluetoothControl.sendMessage("capture", null);
        Intent intent = new Intent(RaspberryPiActivity.this, FindingPupilActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothControl.close();
    }
}