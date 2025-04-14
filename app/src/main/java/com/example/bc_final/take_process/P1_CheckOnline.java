package com.example.bc_final.take_process;

import android.util.Log;

import com.example.bc_final.CORT;
import com.example.bc_final.Own;
import com.example.bc_final.bc_data_structure.Node;
import com.example.bc_final.udp_message.Receiver;
import com.example.bc_final.udp_message.Sender;

import org.json.JSONObject;

public class P1_CheckOnline {
    private final String TAG = "CheckOnline Process";
    private static P1_CheckOnline instance;
    private final Receiver receiver;
    private boolean isRunning;

    // Private constructor to enforce singleton pattern
    private P1_CheckOnline() {
        this.receiver = Own.getReceiver(0);
        this.isRunning = false;
    }

    // Public method to get the single instance
    public static synchronized P1_CheckOnline getInstance() {
        if (instance == null) {
            instance = new P1_CheckOnline();
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
