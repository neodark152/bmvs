package com.example.bmvs.buffer;

import android.util.Log;

import com.example.bmvs.dstruct.Image;
import com.example.bmvs.tools.Crypto;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * CapturedImageQueue is a thread-safe queue that temporarily stores
 * metadata of captured images (e.g., hash, timestamp).
 * It supports initialization, pushing new images, and popping them for further processing.
 */
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
