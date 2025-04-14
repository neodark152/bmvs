package com.example.bc_final.tools;

import android.util.Log;

import com.example.bc_final.Own;

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

    /**
     * Converts Base64 encoded string to PublicKey object.
     *
     * @param keyString Base64 encoded public key (X.509 format)
     * @return Initialized PublicKey instance
     */
    private static PublicKey stringToPublicKey(String keyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    /**
     * Converts Base64 encoded string to PrivateKey object.
     *
     * @param keyString Base64 encoded private key (PKCS#8 format)
     * @return Initialized PrivateKey instance
     */
    private static PrivateKey stringToPrivateKey(String keyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }

    /**
     * Generates digital signature for a message.
     *
     * @param messageHash The content to be signed
     * @return Base64 encoded signature
     */
    public static String sign(String messageHash) throws Exception {
        Log.i("Crypto", "Sign function called.");
        return encryptWithKey(messageHash, stringToPrivateKey(Own.getOwnPrivateKey()));
    }

    /**
     * Verifies a message against its signature.
     *
     * @param messageHash Original content
     * @param signature Base64 encoded signature to verify
     * @param publicKey Signer's Base64 encoded public key
     * @return true if signature is valid
     */
    public static boolean verify(String messageHash, String signature, String publicKey) throws Exception {
        Log.i("Crypto", "Verify function called.");
        return Objects.equals(messageHash, decryptWithKey(signature, stringToPublicKey(publicKey)));
    }

    /**
     * Encrypts data with own private key (typically for signing purposes).
     *
     * @param plainText Data to encrypt
     * @return Base64 encoded ciphertext
     */
    public static String encrypt(String plainText) throws Exception {
        Log.i("Crypto", "Encrypt function called.");
        return encryptWithKey(plainText, stringToPrivateKey(Own.getOwnPrivateKey()));
    }

    /**
     * Decrypts data with provided public key.
     *
     * @param cryptoText Base64 encoded ciphertext
     * @param publicKey Base64 encoded public key for decryption
     * @return Original plaintext
     */
    public static String decrypt(String cryptoText, String publicKey) throws Exception {
        Log.i("Crypto", "Decrypt function called.");
        return decryptWithKey(cryptoText, stringToPublicKey(publicKey));
    }

    /**
     * Internal RSA encryption implementation.
     *
     * @param plainText Data to encrypt
     * @param privateKey PrivateKey for encryption
     * @return Base64 encoded ciphertext
     */
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

    /**
     * Internal RSA decryption implementation.
     *
     * @param cryptoText Base64 encoded ciphertext
     * @param publicKey PublicKey for decryption
     * @return Decrypted plaintext
     */
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
