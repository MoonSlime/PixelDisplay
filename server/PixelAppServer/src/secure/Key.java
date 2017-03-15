package secure;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class Key {
    private static Key key = new Key();
    private PrivateKey privateKey;
    private KeyPair keyPair;
    private String publicKeyMoudulus;
    private String publicKeyExponent;
    private PublicKey publicKey;
    public Key() {
        try {
            KeyPair keyPair = RSAUtils.LoadKeyPair();
            KeyFactory keyFactory = KeyFactory.getInstance(RSAUtils.RSA);
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            RSAPublicKeySpec publicSpec = (RSAPublicKeySpec) keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            this.publicKeyMoudulus = publicSpec.getModulus().toString(16);
            this.publicKeyExponent = publicSpec.getPublicExponent().toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public static Key getInstance() {
        return key;
    }

    public String getPublicKeyExponent() {
        return publicKeyExponent;
    }

    public String getPublicKeyMoudulus() {
        return publicKeyMoudulus;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}