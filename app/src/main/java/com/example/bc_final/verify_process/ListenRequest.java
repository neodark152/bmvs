package com.example.bc_final.verify_process;

import android.util.Log;

import com.example.bc_final.Own;
import com.example.bc_final.handler.DBHandler;
import com.example.bc_final.udp_message.Receiver;
import com.example.bc_final.udp_message.Sender;

import org.json.JSONException;
import org.json.JSONObject;

public class ListenRequest {
    private static final String TAG = "ListenRequest";
    private static Receiver receiveRequest;

    public static void init() {
        if (receiveRequest == null) {
            receiveRequest = Own.getReceiver(3);
        }
    }

    public static void startListen() {
        receiveRequest.receiveData(data -> {
            try {
                JSONObject requestJson = new JSONObject(data);
                int blockNo = requestJson.getInt("block_num");
                String imageHash = requestJson.getString("image_hash");
                String senderIP = requestJson.getString("ip");

                if (DBHandler.isImageHashPresent(blockNo, imageHash)) {
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("address", Own.getOwnAddress());
                    responseJson.put("im_hash", imageHash);
                    new Sender(10015).sendData(responseJson, senderIP);
                } else {
                    Log.w(TAG, "No matching image was found, passed.");
                }
            } catch (JSONException e) {
                Log.w(TAG, "Got invalid data, passed.");
            } catch (Exception e) {
                throw new RuntimeException("Unhandled error during listen request process: " + e.getMessage());
            }
        });
    }
}
