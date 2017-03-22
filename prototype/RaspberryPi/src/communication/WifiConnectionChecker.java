package communication;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;

public class WifiConnectionChecker extends Thread {
    private static WifiConnectionChecker instance;
    private boolean isRunning;

    private WifiConnectionChecker() {
        isRunning = true;
    }

    public static WifiConnectionChecker getInstance() {
        if (instance == null) {
            instance = new WifiConnectionChecker();
        }

        return instance;
    }

    @Override
    public void run() {
        super.run();

        while (!isInterrupted()  &&  isRunning) {
            try {
                sleep(10000);

                InetAddress myAddress = getLocalHostLanAddress();

                if (!isIPAddress(myAddress.getHostAddress())) {
                    System.out.println("Cannot connect to internet : " + myAddress.getHostAddress());
                    continue;
                }
                if (!myAddress.isReachable(2000)) {
                    System.out.println("Cannot connect to internet : " + myAddress.getHostAddress());
                    continue;
                }

                String raspberry_ip = myAddress.getHostAddress();
                registerIPAddress(raspberry_ip);
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (instance != null) {
            isRunning = false;
            instance = null;
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

            GenericUrl url = new GenericUrl("http://pixeldisplay.cafe24.com/prototype_setRaspberryIp");
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
}