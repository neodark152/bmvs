package com.example.bmvs.buffer;

import android.util.Log;

import com.example.bmvs.image.ImageHandler;
import com.example.bmvs.nodemsg.Receiver;


/**
 * The Own class represents the local node's identity and configuration in the blockchain system.
 * It stores key information such as address, IP, private key, and manages receivers and image processing.
 */
public class Own {
    private static String ownAddress;
    private static String ownPrivateKey;
    private static String ownIPAddress;
    private static ImageHandler imageHandler;
    private static int trustedNodeNumber;

    // Pre-initialized message receivers bound to specific ports.
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
