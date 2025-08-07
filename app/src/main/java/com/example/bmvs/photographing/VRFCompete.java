package com.example.bmvs.photographing;

import android.util.Log;

import com.example.bmvs.buffer.CORT;
import com.example.bmvs.buffer.CapturedImageQueue;
import com.example.bmvs.buffer.Own;
import com.example.bmvs.dstruct.Image;
import com.example.bmvs.database.DBHandler;
import com.example.bmvs.tools.Crypto;
import com.example.bmvs.tools.Hash;
import com.example.bmvs.nodemsg.Receiver;
import com.example.bmvs.nodemsg.Sender;

import org.json.JSONObject;

import java.util.Objects;

/**
 * VRFCompete is the second process in photographing.
 * It performs:
 * - Broadcasting the encrypted proof ("pi") which is either the hash of the captured image
 *   or a timestamp if no image is available.
 * - Receiving and verifying VRF results broadcasted by other nodes.
 * - Updating the CORT class with received images and their VRF values.
 */
public class VRFCompete {
    private static final String TAG = "VRF Compete Process";
    private static VRFCompete instance;
    private final Receiver receiver;
    private boolean isRunning;

    private VRFCompete() {
        this.receiver = Own.getReceiver(1);
        this.isRunning = false;
    }

    public static synchronized VRFCompete getInstance() {
        if (instance == null) {
            instance = new VRFCompete();
        }
        return instance;
    }

    private void broadcastPI() {
        try {
            Image image = CapturedImageQueue.pop();
            JSONObject VRFJson = new JSONObject();

            if (image == null) {
                String currentTimestamp = String.valueOf(System.currentTimeMillis());
                VRFJson.put("pi", Crypto.encrypt(currentTimestamp));
                VRFJson.put("im_hash", "");
                VRFJson.put("address", Own.getOwnAddress());
                VRFJson.put("timestamp", currentTimestamp);
                Log.i(TAG, "No image will on chain in this ORT.");
            } else {
                VRFJson.put("pi", Crypto.encrypt(image.getImageHash()));
                VRFJson.put("im_hash", image.getImageHash());
                VRFJson.put("address", Own.getOwnAddress());
                VRFJson.put("timestamp", image.getTimestamp());
                CORT.haveImage();
                Log.i(TAG, "Image will on chain in this ORT.");
            }

            new Sender(10012).broadcastData(VRFJson);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during broadcastPI", e);
        }
    }

    private void updateOtherVRF() {
        receiver.receiveData(data -> {
            try {
                JSONObject nodeVRFJson = new JSONObject(data);
                String imHash = nodeVRFJson.getString("im_hash");
                String timestamp = nodeVRFJson.getString("timestamp");
                String pi = nodeVRFJson.getString("pi");
                String address = nodeVRFJson.getString("address");

                String alpha = imHash.isEmpty() ? timestamp : imHash;
                String vrfResult = pi2vrf(pi, address, alpha);

                CORT.addNewReceivedImage(new Image(imHash, timestamp, address, pi));

                if (vrfResult != null && CORT.updateNodeVRF(address, vrfResult)) {
                    Log.i(TAG, "Updated VRF for address: " + address);
                } else {
                    Log.w(TAG, "VRF verification failed for address: " + address);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing received data", e);
            }
        });
    }

    private String pi2vrf(String pi, String address, String alpha) throws Exception {
        String decryptedAlpha = Crypto.decrypt(pi, DBHandler.publicKeyByAddress(address));
        return Objects.equals(decryptedAlpha, alpha) ? Hash.calculateHash(pi) : null;
    }

    public void start() {
        if (!isRunning) {
            broadcastPI();
            updateOtherVRF();
            isRunning = true;
        } else {
            Log.i(TAG, "VRF Compete Process is Running.");
        }
    }
    public void stop() {
        if (isRunning) {
            receiver.stopReceiving();
            Log.i(TAG, "VRF Compete Process Stopped.");
            isRunning = false;
        }
    }
}