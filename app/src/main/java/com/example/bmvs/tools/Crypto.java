package com.example.bmvs.tools;

import android.util.Log;

import com.example.bmvs.buffer.Own;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;


/**
 * Cryptographic operations utility class using RSA algorithm.
 */
public class Crypto {
    private static PublicKey stringToPublicKey(String keyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    private static PrivateKey stringToPrivateKey(String keyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }

    public static String sign(String messageHash) throws Exception {
        Log.i("Crypto", "Sign function called.");
        return encryptWithKey(messageHash, stringToPrivateKey(Own.getOwnPrivateKey()));
    }

    public static boolean verify(String messageHash, String signature, String publicKey) throws Exception {
        Log.i("Crypto", "Verify function called.");
        return Objects.equals(messageHash, decryptWithKey(signature, stringToPublicKey(publicKey)));
    }

    public static String encrypt(String plainText) throws Exception {
        Log.i("Crypto", "Encrypt function called.");
        return encryptWithKey(plainText, stringToPrivateKey(Own.getOwnPrivateKey()));
    }

    public static String decrypt(String cryptoText, String publicKey) throws Exception {
        Log.i("Crypto", "Decrypt function called.");
        return decryptWithKey(cryptoText, stringToPublicKey(publicKey));
    }

    private static String encryptWithKey(String plainText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            Log.e("Crypto", "Encryption failed: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private static String decryptWithKey(String cryptoText, PublicKey publicKey) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(cryptoText);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage());
        }
    }
}
