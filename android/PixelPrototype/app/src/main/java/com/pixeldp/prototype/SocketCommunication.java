package com.pixeldp.prototype;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

class SocketCommunication {
    private static final int CONTROL_PORT = 3903;
    private static final int PICTURE_PORT = 5201;
    private static String raspberry_ip;
    private OnProgressListener onProgressListener;
    private Thread.UncaughtExceptionHandler exceptionHandler;
    private boolean isReceivingImage;
    private static SocketCommunication instance;

    private SocketCommunication(String _raspberry_ip, Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        raspberry_ip = _raspberry_ip;
    }

    public static SocketCommunication getInstance(String raspberry_ip, Thread.UncaughtExceptionHandler exceptionHandler) {
        if (instance == null) {
            instance = new SocketCommunication(raspberry_ip, exceptionHandler);
        }

        return instance;
    }

    public void sendMessage(final String message) {
        Thread messageThread = new Thread(new Runnable() {
            public void run() {
                Socket socket_control = null;
                try {
                    socket_control = new Socket(raspberry_ip, CONTROL_PORT);
                    BufferedWriter networkWriter = new BufferedWriter(new OutputStreamWriter(socket_control.getOutputStream()));

                    PrintWriter printWriter = new PrintWriter(networkWriter, true);
                    printWriter.println(message);
                } catch (Exception e) {
                    if (e instanceof ConnectException) {
                        throw new RuntimeException();
                    }
                    if (e instanceof UnknownHostException) {
                        Log.d("debugging_socket", "Cannot connect to " + raspberry_ip);
                        throw new RuntimeException();
                    }
                    Log.d("debugging_socket", e.getClass().getSimpleName());
                    e.printStackTrace();
                } finally {
                    try {
                        if (socket_control != null) socket_control.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        });
        messageThread.setUncaughtExceptionHandler(exceptionHandler);
        messageThread.start();
    }

    public Bitmap receiveImage() {

        isReceivingImage = true;

        Socket socket = null;
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            socket = new Socket(raspberry_ip, PICTURE_PORT);
            is = socket.getInputStream();

            byte[] size = new byte[4];
            is.read(size);

            int maxSize = ByteBuffer.wrap(size).asIntBuffer().get();
            byte imageByteArray[] = new byte[maxSize];
            ByteBuffer imageByteBuffer = ByteBuffer.wrap(imageByteArray);

            byte[] container = new byte[2048];
            int readPosition = 0;
            while (  isReceivingImage  &&  readPosition < imageByteBuffer.limit()  ) {
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
            if (e instanceof ConnectException) {
                throw new RuntimeException();
            }
            Log.d("debugging_socket", e.getClass().getSimpleName());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
                if (is != null) is.close();
                isReceivingImage = false;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return bitmap;
    }

    public void setReceivingImage(boolean receivingImage) {
        isReceivingImage = receivingImage;
    }

    public void close() {
        if (instance != null) {
            isReceivingImage = false;
            instance = null;
        }
    }

    interface OnProgressListener {
        void onProgress(int progress);
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }
}


