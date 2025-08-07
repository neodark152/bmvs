package com.example.bmvs.nodemsg;

import android.util.Log;

import com.example.bmvs.buffer.Own;
import com.example.bmvs.tools.Crypto;
import com.example.bmvs.tools.Hash;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

/**
 * Sender is a utility class responsible for sending UDP messages to one or more nodes
 * It supports:
 * - Broadcasting messages to all nodes
 * - Sending to multiple specified IP addresses
 * - Signing messages to ensure authenticity and integrity
 */
public class Sender {
    private final String TAG = "Sender";
    private final int port;
    private DatagramSocket sendSocket;

    public Sender(int port) {
        this.port = port;
        initializeSockets();
    }

    private void initializeSockets() {
        try {
            if (sendSocket == null || sendSocket.isClosed()) {
                sendSocket = new DatagramSocket();
                Log.i(TAG, "SendSocket initialized on port: " + sendSocket.getLocalPort());
            }
        } catch (SocketException e) {
            Log.e(TAG, "Failed to initialize Sender on port " + port, e);
            throw new RuntimeException("Unable to initialize Sender on port " + port, e);
        }
    }

    public void broadcastData(JSONObject message) {
        this.sendData(message, "255.255.255.255");
    }

    public void send2MultiIP(JSONObject message, List<String> IPList) {
        if (message == null || IPList == null || IPList.isEmpty()) {
            throw new RuntimeException("Invalid input, message must be non-null and IPList must be non-empty.");
        }

        for (String ipAddress : IPList) {
            sendData(message, ipAddress);
        }
    }

    public void sendData(JSONObject message, String ipv4Address) {
        initializeSockets();
        String signedMessageString = this.addSign(message).toString();

        new Thread(() -> {
            try {
                if (sendSocket == null || sendSocket.isClosed()) {
                    throw new RuntimeException("Cannot send data, sendSocket is closed.");
                }
                byte[] buffer = signedMessageString.getBytes();
                InetAddress address = InetAddress.getByName(ipv4Address);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                sendSocket.send(packet);
                Log.i(TAG, "Sent data to " + ipv4Address + " on port " + port + ": " + message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send UDP data: " + e.getMessage());
            }
        }).start();
    }

    private JSONObject addSign(JSONObject originalMessage) {
        try {
            String messageHash = Hash.calculateHash(originalMessage.toString());

            JSONObject newMessageJson = new JSONObject();

            newMessageJson.put("message", originalMessage.toString());
            newMessageJson.put("sender_address", Own.getOwnAddress());
            newMessageJson.put("message_hash", messageHash);
            newMessageJson.put("signature", Crypto.sign(messageHash));

            return newMessageJson;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign message: " + e.getMessage());
        }
    }
}