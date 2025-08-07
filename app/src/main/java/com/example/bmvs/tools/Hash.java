package com.example.bmvs.tools;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Computes SHA-256 hash of provided data.
 */
public class Hash {

    private static String calculateHash(byte[] inputData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(inputData);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String calculateHash(File file) {
        return calculateHash(Arrays.toString(ReadFile.readAllByteFromFile(file)));
    }

    public static String calculateHash(String input) {
        String cleanedInput = input.replaceAll("\\s+", "");
        return calculateHash(cleanedInput.getBytes());
    }
}
