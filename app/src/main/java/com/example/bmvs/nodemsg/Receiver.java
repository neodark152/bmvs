package com.example.bmvs.nodemsg;

import android.util.Log;

import com.example.bmvs.database.DBHandler;
import com.example.bmvs.tools.Crypto;
import com.example.bmvs.tools.Hash;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receiver is a UDP message listener for inter-node communication.
 * It provides:
 * - Continuous receiving of UDP messages in a background thread
 * - One-time UDP packet receiving for initialization or specific events
 * - Signature verification for message integrity and authenticity
 */
public class Receiver {
    private static final String TAG = "Receiver";
    private DatagramSocket receiveSocket;
    private final int receivePort;
    private ExecutorService executor;

    public Receiver(int receivePort) {
        this.receivePort = receivePort;
        this.executor = Executors.newSingleThreadExecutor();
        initializeSockets();
    }

    private void initializeSockets() {
        try {
            if (receiveSocket == null || receiveSocket.isClosed()) {
                receiveSocket = new DatagramSocket(receivePort);
                Log.i(TAG, "ReceiveSocket initialized on port: " + receiveSocket.getLocalPort());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize sockets", e);
            throw new RuntimeException("Unable to initialize UDPHandler", e);
        }
    }

    public interface DataReceivedCallback {
        void onDataReceived(String data);
    }

    public void receiveData(DataReceivedCallback callback) {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.submit(() -> {
            byte[] buffer = new byte[40960];

            try {
                while (!Thread.currentThread().isInterrupted() && !receiveSocket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    receiveSocket.receive(packet);

                    byte[] receivedBytes = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), receivedBytes, 0, packet.getLength());
                    String receivedData = new String(receivedBytes);

                    String verifiedMessage = verifyMessageSignature(receivedData);

                    if (verifiedMessage != null) {
                        Log.i(TAG, receiveSocket.getLocalPort() + " received data: " + verifiedMessage);
                        if (callback != null) callback.onDataReceived(verifiedMessage);
                    } else {
                        Log.w(TAG, "Skip invalid data.");
                    }
                }
            } catch (SocketException e) {
                Log.w(TAG, "Receive socket stopped.");
            } catch (Exception e) {
                Log.e(TAG, "Error receiving data", e);
            }
        });
    }

    public void stopReceiving() {
        Log.i(TAG, "Stopping receiving.");
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public void receiveOnePacket(DataReceivedCallback callback) {
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.submit(() -> {
            byte[] buffer = new byte[40960];
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(packet);

                byte[] receivedBytes = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), receivedBytes, 0, packet.getLength());
                String receivedData = new String(receivedBytes);

                String verifiedMessage = verifyMessageSignature(receivedData);

                if (verifiedMessage != null) {
                    Log.i(TAG, receiveSocket.getLocalPort() + " received one-time data: " + verifiedMessage);
                    if (callback != null) callback.onDataReceived(verifiedMessage);
                } else {
                    Log.w(TAG, "Skip invalid one-time data.");
                }

            } catch (SocketException e) {
                Log.w(TAG, "One-time receive socket stopped.");
            } catch (Exception e) {
                Log.e(TAG, "Error receiving one-time data", e);
            } finally {
                receiveSocket.close();
                singleExecutor.shutdownNow();
                Log.i(TAG, "Receiver stopped after one packet.");
            }
        });
    }


    private String verifyMessageSignature(String messageString) {
        try {
            JSONObject json = new JSONObject(messageString);
            String originalMessage = json.getString("message");
            String messageHash = json.getString("message_hash");
            String signature = json.getString("signature");
            String address = json.getString("sender_address");

            if (!Hash.calculateHash(originalMessage).equals(messageHash)) return null;

            String publicKey = DBHandler.publicKeyByAddress(address);
            if (publicKey == null) return null;

            if (!Crypto.verify(messageHash, signature, publicKey)) return null;

            return originalMessage;
        } catch (Exception e) {
            Log.w(TAG, "Invalid message format or verification failed: " + e.getMessage());
            return null;
        }
    }
}