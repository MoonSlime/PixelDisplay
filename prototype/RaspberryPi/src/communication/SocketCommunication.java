package communication;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;

class SocketCommunication {
    /*
    static final int CONTROL_PORT = 3903;
    static final int PICTURE_PORT = 5201;
    String raspberry_ip;
    String android_ip;
    private ServerSocket serverSocket_control;
    private ServerSocket serverSocket_picture;
    private ConnectionChecker connectionChecker;
    private communication.MessageReceiver messageReceiver;

    private OnMessageReceivedListener onMessageReceivedListener;
    private static communication.SocketCommunication instance;

    private communication.SocketCommunication(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;

        connectionChecker = new ConnectionChecker();
        connectionChecker.start();
    }

    public static communication.SocketCommunication getInstance(OnMessageReceivedListener onMessageReceivedListener) {
        if (instance == null) {
            instance = new communication.SocketCommunication(onMessageReceivedListener);
        }

        return instance;
    }

    public static communication.SocketCommunication getInstance() {
        return instance;
    }

    interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void sendImage(String path) {
        new Thread(() -> {
            Socket socket = null;
            try {
                byte container[] = new byte[2048];

                File imageFile = new File(path);
                int fileLength = (int) imageFile.length();

                byte[] size = ByteBuffer.allocate(4).putInt(fileLength).array();
                if ((socket = serverSocket_picture.accept()) != null) {
                    FileInputStream fis = new FileInputStream(imageFile);
                    OutputStream os = socket.getOutputStream();

                    os.write(size);
                    while (fis.available() > 0) {
                        int readSize = fis.read(container);
                        os.write(container, 0, readSize);
                    }
                    os.close();
                    fis.close();
                }

                imageFile.delete();
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }).start();
    }

    public ConnectionChecker getConnectionChecker() {
        return connectionChecker;
    }

    public void setMessageReceiver(communication.MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    private boolean isIPAddress(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            return !ip.endsWith(".");
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    class communication.MessageReceiver extends Thread {
        private boolean isRunning;

        communication.MessageReceiver() {
            setRunning(true);
        }

        @Override
        public void run() {
            super.run();
            try {
                serverSocket_control = new ServerSocket(CONTROL_PORT);
                serverSocket_picture = new ServerSocket(PICTURE_PORT);

                while (isRunning) {
                    Socket client = serverSocket_control.accept(); // wait for input

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String line = in.readLine();
                    if (isIPAddress(line)) {
                        android_ip = line;
                    }
                    onMessageReceivedListener.onMessageReceived(line);

                    client.close();
                }
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            } finally {
                try {
                    serverSocket_picture.close();
                    serverSocket_control.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        void setRunning(boolean running) {
            isRunning = running;
        }
    }

    class ConnectionChecker extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!isInterrupted()) {
                    sleep(7000);

                    InetAddress myAddress = getLocalHostLanAddress();

                    if (!isIPAddress(myAddress.getHostAddress())) {
                        System.out.println("Cannot connect to internet : " + myAddress.getHostAddress());

                        if (messageReceiver != null) {
                            messageReceiver.setRunning(false);
                            messageReceiver = null;
                            System.out.println("communication.MessageReceiver stopped");
                        }
                        continue;
                    }
                    if (!myAddress.isReachable(2000)) {
                        System.out.println("Cannot connect to internet : " + myAddress.getHostAddress());

                        if (messageReceiver != null) {
                            messageReceiver.setRunning(false);
                            messageReceiver = null;
                            System.out.println("communication.MessageReceiver stopped");
                        }
                        continue;
                    }

                    raspberry_ip = myAddress.getHostAddress();
                    String result = registerIPAddress(raspberry_ip);
                    System.out.println("internet connected, registering address " + result);
                    if (messageReceiver == null) {
                        messageReceiver = new communication.MessageReceiver();
                        messageReceiver.start();
                        System.out.println("communication.MessageReceiver started");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }

        private String registerIPAddress(String address) {
            HttpTransport httpTransport = null;
            HttpResponse response = null;
            String result = null;
            try {
                httpTransport = new NetHttpTransport();
                HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

                HashMap<String, String> map = new HashMap<>();
                map.put("ip", address);
                UrlEncodedContent content = new UrlEncodedContent(map);

                GenericUrl url = new GenericUrl("http://pixeldisplay.cafe24.com/iptable/update.jsp");
                HttpRequest request = requestFactory.buildPostRequest(url, content);

                response = request.execute();
                result = readContent(response.getContent());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    if (response != null) response.disconnect();
                    if (httpTransport != null) httpTransport.shutdown();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            return result;
        }

        private String readContent(InputStream inputstream) {

            BufferedReader in = null;
            StringBuffer result = null;
            try {
                in = new BufferedReader(new InputStreamReader(inputstream));
                result = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result.toString();
        }

        private InetAddress getLocalHostLanAddress() throws UnknownHostException {
            try {
                InetAddress candidateAddress = null;
                for (Enumeration interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                    NetworkInterface iface = (NetworkInterface) interfaces.nextElement();
                    for (Enumeration inetAddresses = iface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                        InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            if (inetAddress.isSiteLocalAddress()) {
                                return inetAddress;
                            } else if (candidateAddress == null) {
                                candidateAddress = inetAddress;
                            }
                        }
                    }
                }
                if (candidateAddress != null) {
                    return candidateAddress;
                }
                InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                if (jdkSuppliedAddress == null) {
                    throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
                }
                return jdkSuppliedAddress;
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
                UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        }
    }
    */
}