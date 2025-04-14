package com.example.bc_final.take_process;

import android.util.Log;

import com.example.bc_final.CORT;
import com.example.bc_final.CapturedImageQueue;
import com.example.bc_final.Own;
import com.example.bc_final.bc_data_structure.Image;
import com.example.bc_final.handler.DBHandler;
import com.example.bc_final.tools.Crypto;
import com.example.bc_final.tools.Hash;
import com.example.bc_final.udp_message.Receiver;
import com.example.bc_final.udp_message.Sender;

import org.json.JSONObject;

import java.util.Objects;

public class P2_VRFCompete {
    private static final String TAG = "VRF Compete Process";
    private static P2_VRFCompete instance;
    private final Receiver receiver;
    private boolean isRunning;

    private P2_VRFCompete() {
        this.receiver = Own.getReceiver(1);
        this.isRunning = false;
    }

    public static synchronized P2_VRFCompete getInstance() {
        if (instance == null) {
            instance = new P2_VRFCompete();
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