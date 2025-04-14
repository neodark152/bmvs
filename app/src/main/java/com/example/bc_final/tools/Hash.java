package com.example.bc_final.tools;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Hash {
    /**
     * Computes SHA-256 hash of raw byte data.
     *
     * @param inputData The byte array to hash
     * @return Hexadecimal string representation of the hash (lowercase),
     *         or null if hashing algorithm is unavailable
     */
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

    /**
     * Computes SHA-256 hash of a file's contents.
     *
     * @param file The file to hash
     * @return Hexadecimal string hash of the file contents,
     *         or null if the file cannot be read or hashing fails
     */
    public static String calculateHash(File file) {
        return calculateHash(Arrays.toString(ReadFile.readAllByteFromFile(file)));
    }

    /**
     * Computes SHA-256 hash of a string (using UTF-8 encoding).
     *
     * @param input The string to hash
     * @return Hexadecimal string representation of the hash,
     *         or null if hashing fails
     */
    public static String calculateHash(String input) {
        String cleanedInput = input.replaceAll("\\s+", "");
        return calculateHash(cleanedInput.getBytes());
    }
}
