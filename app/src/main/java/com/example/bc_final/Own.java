package com.example.bc_final;

import android.content.Context;
import android.util.Log;

import com.example.bc_final.handler.ImageHandler;
import com.example.bc_final.udp_message.Receiver;

public class Own {
    private static String ownAddress;
    private static String ownPrivateKey;
    private static String ownIPAddress;
    private static ImageHandler imageHandler;
    private static int trustedNodeNumber;
    private static final Receiver[] receivers = new Receiver[5];

    public static synchronized void init(
            String _ownAddress, String _ownIPAddress, String _ownPrivateKey, int _trustedNodeNumber) {
        ownAddress = _ownAddress;
        ownIPAddress = _ownIPAddress;
        ownPrivateKey = _ownPrivateKey;

        trustedNodeNumber = _trustedNodeNumber;

        receivers[0] = new Receiver(10011);
        receivers[1] = new Receiver(10012);
        receivers[2] = new Receiver(10013);
        receivers[3] = new Receiver(10014);
        receivers[4] = new Receiver(10015);

        imageHandler = new ImageHandler();

        Log.i("INIT", "Own class initialized successfully.");
    }

    public static Receiver getReceiver(int num) {
        return receivers[num];
    }

    public static String getOwnAddress() {
        return ownAddress;
    }

    public static String getOwnPrivateKey() {
        return ownPrivateKey;
    }

    public static String getOwnIPAddress() {
        return ownIPAddress;
    }

    public static int getTrustedNodeNumber() {
        return trustedNodeNumber;
    }

    public static ImageHandler getImageHandler() {
        return imageHandler;
    }
}
