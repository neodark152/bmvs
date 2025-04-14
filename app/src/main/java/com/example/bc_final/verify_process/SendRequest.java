package com.example.bc_final.verify_process;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bc_final.Own;
import com.example.bc_final.tools.ULog;
import com.example.bc_final.udp_message.Receiver;
import com.example.bc_final.udp_message.Sender;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class SendRequest {
    private static final String TAG = "VerifyPhotoThreadManager";
    private static Handler handler;
    private static Receiver receiveResponse;

    public static void init() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        if (receiveResponse == null) {
            receiveResponse = Own.getReceiver(4);
        }
    }

    public static void startSearch(int blockNo, List<String> IPList, String imHash) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("image_hash", imHash);
            requestJson.put("block_num", blockNo);
            requestJson.put("ip", Own.getOwnIPAddress());
            new Sender(10014).send2MultiIP(requestJson, IPList);
            handleResponse(imHash);
        } catch (Exception e) {
            throw new RuntimeException("Error when send request data: " + e.getMessage());
        }
    }

    private static void handleResponse(String imHash) {

        if (handler == null || receiveResponse == null) {
            throw new RuntimeException("SendRequest class didn't initialize.");
        }

        receiveResponse.receiveData(data -> {
            try {
                JSONObject responseJson = new JSONObject(data);
                if (Objects.equals(imHash, responseJson.getString("im_hash"))) {
                    ULog.add("Image(" + imHash.substring(0,5) + ") confirmation from " + responseJson.getString("address"));
                } else {
                    Log.w(TAG, "Different image, passed.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Invalid data, passed.");
            }
        });

        Runnable pauseReceive = () -> {
            Log.i(TAG, "10s passed, stop receive response.");
            receiveResponse.stopReceiving();
        };
        handler.postDelayed(pauseReceive, 10000);
    }
}
