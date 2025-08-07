package com.example.bmvs.photographing;

import android.util.Log;

import com.example.bmvs.buffer.CORT;
import com.example.bmvs.buffer.Own;
import com.example.bmvs.dstruct.Node;
import com.example.bmvs.nodemsg.Receiver;
import com.example.bmvs.nodemsg.Sender;

import org.json.JSONObject;

/**
 * CheckOnline is the first process in photographing.
 * It performs:
 * - Broadcasting the current node's address and IP to the network
 * - Listening for and updating the status of other online nodes
 */
public class CheckOnline {
    private final String TAG = "CheckOnline Process";
    private static CheckOnline instance;
    private final Receiver receiver;
    private boolean isRunning;

    // Private constructor to enforce singleton pattern
    private CheckOnline() {
        this.receiver = Own.getReceiver(0);
        this.isRunning = false;
    }

    // Public method to get the single instance
    public static synchronized CheckOnline getInstance() {
        if (instance == null) {
            instance = new CheckOnline();
        }
        return instance;
    }

    // Broadcast node status
    private void broadcastStatus() {
        try {
            JSONObject onlineStatusJson = new JSONObject();
            onlineStatusJson.put("address", Own.getOwnAddress());
            onlineStatusJson.put("ip", Own.getOwnIPAddress());
            // onlineStatusJson.put("status", 1);  // 1 indicates online

            new Sender(10011).broadcastData(onlineStatusJson);
            Log.i(TAG, "Broadcast status: " + onlineStatusJson);
        } catch (Exception e) {
            Log.e(TAG, "Error during status broadcast.", e);
        }
    }

    // Update the status of other nodes
    private void updateOtherStatus() {
        receiver.receiveData(data -> {
            try {
                JSONObject nodeStatusJson = new JSONObject(data);
                String address = nodeStatusJson.getString("address");
                String ip = nodeStatusJson.getString("ip");

                CORT.addNewNode(new Node(address, ip));

                Log.i(TAG, "Updated node status for address: " + address);
            } catch (Exception e) {
                Log.e(TAG, "Error when updating node status.", e);
            }
        });
    }

    // Public method to start the process
    public void start() {
        if (!isRunning) {
            broadcastStatus();
            updateOtherStatus();
            isRunning = true;
        } else {
            Log.i(TAG, "Check Online Process is Running.");
        }
    }
    public void stop() {
        if (isRunning) {
            receiver.stopReceiving();
            Log.i(TAG, "Check Online Process Stopped.");
            isRunning = false;
        }
    }
}
