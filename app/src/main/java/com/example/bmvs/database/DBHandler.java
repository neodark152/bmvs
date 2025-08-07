package com.example.bmvs.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bmvs.dstruct.Block;
import com.example.bmvs.tools.MainActivityRef;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import kotlin.Triple;

/**
 * Database handler class for managing SQLite database operations.
 */
public class DBHandler {
    private static final String TAG = "DBHandler";
    private static final String DATABASE_NAME = "blockchain_data.db";
    private static SQLiteDatabase database;

    public static synchronized void init() {
        initializeDatabase();
        openDatabase();
        Log.i("INIT", "DBHandler class initialize successfully.");
    }


    // Copies the database from assets to the app's database directory if it doesn't already exist.
    private static void initializeDatabase() {
        File dbFile = new File(Objects.requireNonNull(MainActivityRef.getMainActivity()).getDatabasePath(DATABASE_NAME).getPath());
        if (!dbFile.exists()) {
            try (InputStream inputStream = MainActivityRef.getMainActivity().getAssets().open(DATABASE_NAME);
                 OutputStream outputStream = new FileOutputStream(dbFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                Log.d(TAG, "Database copied successfully to " + dbFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error copying database", e);
                throw new RuntimeException("Failed to initialize database", e);
            }
        } else {
            Log.d(TAG, "Database already exists at " + dbFile.getPath());
        }
    }

    // Opens the database in read-write mode.
    private static void openDatabase() {
        database = SQLiteDatabase.openDatabase(
                Objects.requireNonNull(MainActivityRef.getMainActivity()).getDatabasePath(DATABASE_NAME).getPath(),
                null,
                SQLiteDatabase.OPEN_READWRITE
        );
    }

    public static int getTrustedNodeNumber() {
        int count = 0;
        try (Cursor cursor = database.rawQuery(
                "SELECT COUNT(address) FROM node_info WHERE address IS NOT NULL", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get trusted node number: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return count;
    }

    public static String publicKeyByAddress(String address) {
        try (Cursor cursor = database.rawQuery(
                "SELECT public_key FROM node_info WHERE address = ?", new String[]{address})) {
            if (cursor.moveToFirst()) {
                String publicKey = cursor.getString(cursor.getColumnIndexOrThrow("public_key"));
                Log.i(TAG, "Public key queried.");
                return publicKey;
            } else {
                Log.w(TAG, "No address match, passed.");
                return null;
            }
        }
    }

    public static Triple<String, String, String> latestBlockInfo() {
        try (Cursor cursor = database.rawQuery(
                "SELECT * FROM blocks ORDER BY block_no DESC LIMIT 1;", null)) {
            if (cursor.moveToFirst()) {
                String preHash = cursor.getString(cursor.getColumnIndexOrThrow("pre_hash"));
                String tailHash = cursor.getString(cursor.getColumnIndexOrThrow("tail_hash"));
                String blockNo = cursor.getString(cursor.getColumnIndexOrThrow("block_no"));
                Log.i(TAG, String.format("%s: %s", "Latest block Info", "Pre Hash=" + preHash + ", Tail Hash=" + tailHash + ", BlockNo=" + blockNo));
                return new Triple<>(preHash, tailHash, blockNo);
            } else {
                Log.e(TAG, "Error when query latest block information.");
                return null;
            }
        }
    }

    public static boolean isImageHashPresent(int blockNo, String imageHash) {
        try (Cursor cursor = database.rawQuery(
                "SELECT 1 FROM image_list WHERE block_no = ? AND im_hash = ? LIMIT 1", new String[]{String.valueOf(blockNo), imageHash})) {
            if (cursor.moveToFirst()) {
                Log.i(TAG, "A corresponding image was detected.");
                return true;
            } else {
                Log.i(TAG, "No corresponding image was detected.");
                return false;
            }
        }
    }

    public static void insertNewBlock(Block block) {
        // Insert block information
        ContentValues blockInfoValues = new ContentValues();
        blockInfoValues.put("pre_hash", block.getPreHash());
        blockInfoValues.put("tail_hash", block.getTailHash());
        blockInfoValues.put("block_no", block.getBlockNum());
        blockInfoValues.put("timestamp", block.getTimestamp());
        blockInfoValues.put("winner_address", block.getVrfWinnerAddress());

        Log.i(TAG, "Block info: " + blockInfoValues);

        try (Cursor cursor = database.rawQuery(
                "SELECT * FROM store_node_list WHERE block_no = ?",
                new String[]{String.valueOf(block.getBlockNum())})) {
            if (cursor.getCount() > 0) {
                Log.w("DBHandler", "Block number already exists. Skipping insertion.");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for existing block number", e);
        }

        try {
            database.insert("blocks", null, blockInfoValues);
        } catch (Exception e) {
            throw new RuntimeException("Error inserting block information", e);
        }

        // Insert images associated with the block
        try {
            JSONArray imageList = block.getImageList();
            for (int i = 0; i < imageList.length(); i++) {
                JSONObject image = imageList.getJSONObject(i);
                String imageHash = image.getString("im_hash");

                if (imageHash.trim().isEmpty()) {
                    Log.w(TAG, "Image hash is empty.");
                    continue;
                }

                ContentValues imageValues = new ContentValues();
                imageValues.put("block_no", block.getBlockNum());
                imageValues.put("im_hash", imageHash);
                imageValues.put("address", image.getString("address"));
                imageValues.put("signature", image.getString("signature"));

                try {
                    database.insertOrThrow("image_list", null, imageValues);
                    Log.i(TAG, "Inserted image: " + image);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to insert image (may be duplicate): " + image, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image list", e);
        }

        // Insert nodes associated with the block
        try {
            List<String> nodeAddresses = block.getNodeAddresses();

            for (String nodeAddress : nodeAddresses) {
                ContentValues nodeInfo = new ContentValues();
                nodeInfo.put("block_no", block.getBlockNum());
                nodeInfo.put("address", nodeAddress);

                Log.i(TAG, "Node info: " + nodeInfo);

                database.insert("store_node_list", null, nodeInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting node information", e);
        }

        Log.d(TAG, "Block inserted successfully with block_no: " + block.getBlockNum());
    }
}
