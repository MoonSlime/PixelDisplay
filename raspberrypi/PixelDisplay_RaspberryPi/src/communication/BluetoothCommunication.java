package communication;

import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BluetoothCommunication extends Thread {
    private static final String SERIAL_UUID = "0000110100001000800000805f9b34fb";

    private StreamConnectionNotifier server;
    private StreamConnection channel;
    private InputStream is;
    private OutputStream os;
    private MessageReceiver messageReceiver;
    private OnMessageReceivedListener onMessageReceivedListener;

    private boolean isSendingImage;

    private static BluetoothCommunication instance;

    private BluetoothCommunication() {
        try {
            server = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + SERIAL_UUID, Connector.READ_WRITE, true);
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            localDevice.updateRecord(localDevice.getRecord(server));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        setSendingImage(false);
    }

    public static BluetoothCommunication getInstance() {
        if (instance == null) {
            instance = new BluetoothCommunication();
        }
        return instance;
    }

    @Override
    public void run() {
        super.run();

        try {
            while (!isInterrupted()) {
                channel = server.acceptAndOpen(); // android 에서 connectToPairedDevice 메소드에 의해 unlock

                if (messageReceiver != null) messageReceiver.close(); // 이미 연결된 상태일 때 resetting
                if (os != null) os.close();
                if (is != null) is.close();

                is = channel.openInputStream();
                os = channel.openDataOutputStream();
                messageReceiver = MessageReceiver.getInstance(is, onMessageReceivedListener);
                messageReceiver.start();
            }
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendImage(File image, float distance) {
        setSendingImage(true);

        new Thread(() -> {
            FileInputStream fis = null;
            try {
                byte container[] = new byte[8192];

                int fileLength = (int) image.length();

                os.write(ByteBuffer.allocate(4).putFloat(distance).array());
                os.write(ByteBuffer.allocate(4).putInt(fileLength).array());

                fis = new FileInputStream(image);
                while (isSendingImage && fis.available() > 0) {
                    int readSize = fis.read(container);

                    os.write(container, 0, readSize);
                }

                os.flush();
                image.delete();
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                isSendingImage = false;
            }
        }).start();
    }

    public void setSendingImage(boolean sendingImage) {
        isSendingImage = sendingImage;
    }

    public void close() {
        try {
            if (instance != null) {
                if (os != null) os.close();
                if (is != null) is.close();
                if (channel != null) channel.close();
                if (server != null) server.close();

                isSendingImage = false;
                instance = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;
    }
}