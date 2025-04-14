package com.example.bc_final.handler;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.bc_final.CORT;
import com.example.bc_final.CapturedImageQueue;
import com.example.bc_final.MainActivity;
import com.example.bc_final.bc_data_structure.Block;
import com.example.bc_final.tools.Hash;
import com.example.bc_final.tools.MainActivityRef;
import com.example.bc_final.tools.ReadFile;
import com.example.bc_final.tools.ULog;
import com.example.bc_final.verify_process.SendRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ImageHandler {
    private final String TAG = "ImageHandler";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private File outputImage;
    private final MainActivity activity;

    public ImageHandler() {
        this.activity = MainActivityRef.getMainActivity();
        registerLaunchers();
        Log.i("INIT", "ImageHandler class initialize successfully.");
    }

    // Register image picker and camera launchers
    private void registerLaunchers() {
        imagePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Log.i(TAG, "Selected image URI: " + selectedImageUri);

                            new Thread(() -> {
                                try (InputStream inputStream = activity.getContentResolver().openInputStream(selectedImageUri)) {
                                    unzipAndStartSearch(inputStream);
                                } catch (IOException e) {
                                    Log.e(TAG, "Error opening image URI", e);
                                }
                            }).start();

                        } else {
                            Log.e(TAG, "Failed to get selected image URI.");
                        }
                    } else {
                        Log.e(TAG, "Image picker activity failed or was canceled.");
                    }
                }
        );

        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && outputImage != null && outputImage.exists()) {
                        new Thread(() -> {
                            try {
                                String imageHash = Hash.calculateHash(outputImage);
                                CapturedImageQueue.push(imageHash, String.valueOf(System.currentTimeMillis()));
                                Log.d(TAG, "Image saved to: " + outputImage.getAbsolutePath());
                                ULog.add("Captured Image Hash: " + imageHash);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to calculate hash and queue image.", e);
                                throw new RuntimeException("Original image captured, but can't generate signature.");
                            }
                        }).start();
                    } else {
                        Log.e(TAG, "Captured image file does not exist or camera activity failed.");
                    }
                }
        );
    }

    // Start file picker to select ZIP file
    public void startFilePicker() {
        if (imagePickerLauncher != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip"); // Limit to ZIP files
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(intent);
        } else {
            Log.e(TAG, "File picker launcher is not registered.");
        }
    }

    // Start the camera to capture an image
    public void startCamera() {
        executor.execute(() -> {
            File appExternalDir = new File(activity.getExternalFilesDir(null), "original_picture");
            if (!appExternalDir.exists() && !appExternalDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + appExternalDir.getAbsolutePath());
                return;
            }

            File imageFile = new File(appExternalDir, "IMG_" + UUID.randomUUID().toString() + ".jpg");
            try {
                if (!imageFile.createNewFile()) {
                    Log.e(TAG, "Failed to create image file.");
                    return;
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to create image file", e);
                return;
            }

            outputImage = imageFile;

            activity.runOnUiThread(() -> {
                Uri imageUri = FileProvider.getUriForFile(activity, "com.example.bc_final.fileprovider", outputImage);
                if (imageUri == null) {
                    Log.e(TAG, "Failed to create image URI.");
                    return;
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraLauncher.launch(intent);
            });
        });
    }

    // Package image and metadata into a ZIP file
    public void packageImage(Block newBlock) {
        if (outputImage == null || !outputImage.exists()) {
            Log.e(TAG, "No valid image to package.");
            return;
        }

        String fileName = outputImage.getName();
        File appExternalDir = new File(activity.getExternalFilesDir(null), "packaged_picture");
        if (!appExternalDir.exists() && !appExternalDir.mkdirs()) {
            Log.e(TAG, "Failed to create directory: " + appExternalDir.getAbsolutePath());
            return;
        }

        File jsonFile = new File(appExternalDir, fileName + ".json");
        try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
            fos.write(newBlock.getString().getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error writing JSON file", e);
            return;
        }

        File zipFile = new File(appExternalDir, fileName + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            try (FileInputStream fis = new FileInputStream(outputImage)) {
                zos.putNextEntry(new ZipEntry(outputImage.getName()));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }

            try (FileInputStream fis = new FileInputStream(jsonFile)) {
                zos.putNextEntry(new ZipEntry(jsonFile.getName()));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating ZIP file", e);
        }
        Log.d(TAG, "ZIP file created successfully: " + zipFile.getAbsolutePath());
    }

    // Unzip the selected file and start verification
    private void unzipAndStartSearch(InputStream inputStream) {
        File extractDir = new File(activity.getExternalFilesDir(null), "unzipped_picture");
        if (!extractDir.exists() && !extractDir.mkdirs()) {
            Log.e(TAG, "Failed to create extraction directory: " + extractDir.getAbsolutePath());
            return;
        }

        File extractedImage = null;
        File extractedJson = null;

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outputFile = new File(extractDir, entry.getName());
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                if (entry.getName().endsWith(".jpg")) {
                    extractedImage = outputFile;
                } else if (entry.getName().endsWith(".json")) {
                    extractedJson = outputFile;
                }
            }

            if (extractedImage == null || extractedJson == null) {
                Log.e(TAG, "Required files (.jpg and .json) are missing in the ZIP archive.");
                return;
            }

            String imageHash = Hash.calculateHash(extractedImage);
            Block newBlock = new Block(new String(ReadFile.readAllByteFromFile(extractedJson)));

            // Validate the image against the blockchain data (to be implemented)
            List<String> ipList = new ArrayList<>();
            for (String address : newBlock.getNodeAddresses()) {
                String ip = CORT.getNodeIPbyAddress(address);
                if (ip != null && !ip.isEmpty()) {
                    ipList.add(ip);
                } else {
                    throw new RuntimeException("Can't get node ip by node address!");
                }
            }

            SendRequest.startSearch(newBlock.getBlockNum(), ipList, imageHash);
            Log.i(TAG, "Verification started with block: " + newBlock.getBlockNum());

        } catch (IOException e) {
            Log.e(TAG, "Error occurred while unzipping and processing files", e);
        } finally {
            // Delete extracted files
            deleteFile(extractedImage);
            deleteFile(extractedJson);
        }
    }

    // Helper method to delete files
    private void deleteFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            Log.e(TAG, "Failed to delete extracted file: " + file.getAbsolutePath());
        }
    }
}
