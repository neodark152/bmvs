package com.example.bc_final;

import android.util.Log;

import com.example.bc_final.bc_data_structure.Image;
import com.example.bc_final.tools.Crypto;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class CapturedImageQueue {
    private static Queue<Image> preImageQueue;

    public static void init() {
        if (preImageQueue == null) {
            preImageQueue = new ConcurrentLinkedQueue<>();

            Log.i("INIT", "CapturedImageQueue class initialize successfully.");
        }
    }

    public static void push(String imageHash, String timestamp) throws Exception {
        preImageQueue.offer(new Image(imageHash, timestamp, Own.getOwnAddress(), Crypto.encrypt(imageHash)));
        Log.d("CapturedImageQueue", "New image pushed, image hash is " + imageHash);
    }
    public static Image pop() {
        return preImageQueue.poll(); // Returns null if queue is empty
    }
}
