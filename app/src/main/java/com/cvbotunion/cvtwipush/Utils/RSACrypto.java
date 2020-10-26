package com.cvbotunion.cvtwipush.Utils;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public final class RSACrypto {
    private static RSACrypto instance = null;
    private Cipher cipher;
    private RSAPublicKey publicKey;

    private RSACrypto() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("RSA");
    }

    public static synchronized  void init() {
        try {
            instance = new RSACrypto();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            Log.e("RSACrypto","get Cipher RSA instance failed");
        }
    }

    public static synchronized RSACrypto getInstance() {
        if(instance==null) init();
        return instance;
    }

    private void setPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.decode(publicKeyString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    /**
     * Encrypt with publicKey.
     */
    public String encrypt(String str, String publicKeyString) {
        try {
            if(publicKeyString!=null) {
                setPublicKey(publicKeyString);
            } else {
                return null;
            }
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return new String(Base64.encode(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP), StandardCharsets.UTF_8);
        } catch(Exception e) {
            Log.e("RSACrypto", e.toString());
            return null;
        }
    }
}
