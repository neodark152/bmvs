package com.example.bmvs.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Reads the entire contents of a file into a byte array.
 */
public class ReadFile {

    public static byte[] readAllByteFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + file.getPath(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getPath(), e);
        }
    }
}
