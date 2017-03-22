package com.pixeldp.prototype.device_control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.pixeldp.prototype.FindingPupilActivity;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class BluetoothControl {

    public static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // bluetooth serial port service
    //private static final UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

    private final AtomicReference<BluetoothSocket> bluetoothSocket = new AtomicReference<>();
    private BluetoothAdapter bluetoothAdapter;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    private OnProgressListener onProgressListener;
    private boolean isReceivingImage;

    private static BluetoothControl instance;

    private BluetoothControl(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isReceivingImage = false;

        if (bluetoothAdapter == null) {
            Log.d("debugging_bluetooth", "Device not support bluetooth.");
            throw new RuntimeException();
        }
    }

    public static BluetoothControl getInstance(Thread.UncaughtExceptionHandler exceptionHandler) {
        if (instance == null) {
            instance = new BluetoothControl(exceptionHandler);
        }

        return instance;
    }

    public static boolean hasInstance() {
        return (instance != null);
    }

    public void connectToPairedDevice() throws IOException {
        final Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

        if (devices.size() == 0) {
            Log.d("debugging_bluetooth", "There is no paired device.");
            throw new RuntimeException();
        }

        for (BluetoothDevice device : devices) {
            if (device.getName().equals("raspberrypi")) {
                try {
                    bluetoothSocket.set(device.createRfcommSocketToServiceRecord(SERIAL_UUID));
                    bluetoothSocket.get().connect();
                } catch (Exception e) {
                    try {
                        //http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                        bluetoothSocket.set((BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1));
                        bluetoothSocket.get().connect();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }

                return;
            }
        }

        throw new RuntimeException();
    }

    public void sendMessage(final String message, final OnMessageSendListener onMessageSendListener) {
        Thread controlThread = new Thread(new Runnable() {
            public void run() {
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(bluetoothSocket.get().getOutputStream()));
                    PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
                    printWriter.println(message);
                } catch (Exception e) {
                    Log.d("debugging_bluetooth", e.getClass().getSimpleName());
                    e.printStackTrace();
                } finally {
                    if (onMessageSendListener != null) {
                        onMessageSendListener.onSent();
                    }
                }
            }
        });
        controlThread.setUncaughtExceptionHandler(exceptionHandler);
        controlThread.start();
    }

    public Bitmap receiveImage() {
        isReceivingImage = true;

        byte[] imageByteArray;
        Bitmap bitmap = null;
        try {
            DataInputStream is = new DataInputStream(bluetoothSocket.get().getInputStream());
            flushInputStream(is);

            byte[] bytes_distance = new byte[4];
            is.read(bytes_distance);
            FindingPupilActivity.eye_camera_distance = ByteBuffer.wrap(bytes_distance).asFloatBuffer().get();

            byte[] bytes_size = new byte[4];
            is.read(bytes_size);
            int maxSize = ByteBuffer.wrap(bytes_size).asIntBuffer().get();

            imageByteArray = new byte[maxSize];
            ByteBuffer imageByteBuffer = ByteBuffer.wrap(imageByteArray);

            byte[] container = new byte[2048];
            int readPosition = 0;
            while (isReceivingImage && readPosition < imageByteBuffer.limit()) {
                int readSize;

                if ((readSize = is.read(container)) > 0) {
                    imageByteBuffer.put(container, 0, readSize);

                    readPosition += readSize;
                }

                if (onProgressListener != null) {
                    int percentage = (int) (((double) readPosition / (double) maxSize) * 100.0);
                    onProgressListener.onProgress(percentage);
                }
            }

            bitmap = (isReceivingImage) ? BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length) : null;
        } catch (Exception e) {
            Log.d("debugging_socket", e.getClass().getSimpleName());
            e.printStackTrace();
        } finally {
            isReceivingImage = false;
        }

        return bitmap;
    }

    public boolean isEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public void setReceivingImage(boolean receivingImage) {
        isReceivingImage = receivingImage;
    }

    public void close() {
        if (instance != null) {
            isReceivingImage = false;
            try {
                if (bluetoothSocket.get() != null && bluetoothSocket.get().isConnected()) {
                    bluetoothSocket.get().close();
                    bluetoothSocket.set(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                instance = null;
            }
        }
    }

    interface OnMessageSendListener {
        void onSent();
    }

    public interface OnProgressListener {
        void onProgress(int progress);
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    private void flushInputStream(InputStream is) {
        try {
            int available = is.available();

            for (int skippedByte = 0; skippedByte < available; ) {
                skippedByte += is.skip(2048);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}