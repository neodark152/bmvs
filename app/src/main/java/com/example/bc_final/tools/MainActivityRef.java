package com.example.bc_final.tools;

import com.example.bc_final.MainActivity;

import java.lang.ref.WeakReference;

public class MainActivityRef {
    private static WeakReference<MainActivity> activityRef;

    // Initialize the WeakReference to MainActivity
    public static void init(MainActivity activity) {
        activityRef = new WeakReference<>(activity);
    }

    // Get the reference to MainActivity with a null check
    public static MainActivity getMainActivity() {
        if (activityRef != null) {
            return activityRef.get();
        }
        // Return null or handle the case where MainActivity is not available
        return null;
    }
}
