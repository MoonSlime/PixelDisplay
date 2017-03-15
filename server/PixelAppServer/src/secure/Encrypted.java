package secure;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.PublicKey;

public class Encrypted {
    public static String encryptRsa(PublicKey key, String original)  {
        byte[] conv = null;
        try  {
            conv = original.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            conv = cipher.doFinal(conv);
        }
        catch(Exception e)  {
        }
        return byteArrayToHex(conv);
    }

    public static String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

}