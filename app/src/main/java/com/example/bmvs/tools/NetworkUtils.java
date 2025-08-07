package com.example.bmvs.tools;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.bmvs.MainActivity;

import java.util.Locale;

/**
 * Network utility class providing IP address and connectivity related functions.
 */
public class NetworkUtils {

    public static String getCurrentIP(MainActivity activity) {
        try {
            WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting current IP address: " + e.getMessage());
        }
        return null;
    }

    public static boolean isHostReachable(String ip) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + ip);
            int exitValue = process.waitFor();

            return (exitValue == 0);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}