package com.example.bmvs.tools;

import com.example.bmvs.MainActivity;

import java.lang.ref.WeakReference;

/**
 * Holds a weak reference to the MainActivity instance.
 */
public class MainActivityRef {
    private static WeakReference<MainActivity> activityRef;

    public static void init(MainActivity activity) {
        activityRef = new WeakReference<>(activity);
    }

    public static MainActivity getMainActivity() {
        if (activityRef != null) {
            return activityRef.get();
        }
        // Return null or handle the case where MainActivity is not available
        return null;
    }
}
