package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class MessageReceiver extends Thread {
    private BluetoothCommunication.OnMessageReceivedListener onMessageReceivedListener;
    private BufferedReader reader;
    private Boolean isReceiving;
    private static MessageReceiver instance;

    private MessageReceiver(InputStream is, BluetoothCommunication.OnMessageReceivedListener onMessageReceivedListener) {
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())));
        this.onMessageReceivedListener = onMessageReceivedListener;
        isReceiving = true;
    }

    static MessageReceiver getInstance(InputStream is, BluetoothCommunication.OnMessageReceivedListener onMessageReceivedListener) {
        if (instance == null) {
            instance = new MessageReceiver(is, onMessageReceivedListener);
        }

        return instance;
    }

    @Override
    public void run() {
        super.run();

        try {
            while (!isInterrupted() && isReceiving) {
                String line = reader.readLine();

                if (line != null) {
                    onMessageReceivedListener.onMessageReceived(line.trim());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("New connection is opened.");
        }
    }

    void close() {
        if (instance != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isReceiving = false;
                instance = null;
            }
        }
    }
}