package com.example.bmvs.photographing;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bmvs.buffer.CORT;
import com.example.bmvs.buffer.Own;
import com.example.bmvs.tools.Millis;
import com.example.bmvs.tools.ULog;

/**
 * TimeController manages the workflow of the BMVS system based on the current time within each minute.
 * It uses a Handler and Runnable to repeatedly check the elapsed milliseconds in the current minute,
 * and triggers different processes accordingly:
 * - 0-15 seconds: Start CheckOnline process; stop VRFCompete and GenerateBlock processes.
 * - 15-16 seconds: Check if enough nodes are online; pause if not.
 * - 16-35 seconds: Start VRFCompete process; stop others.
 * - 35-58 seconds: Start GenerateBlock process; stop others.
 * - After 58 seconds: Clear the CORT status.
 */
public class TimeController {
    private static final String TAG = "TimeController";
    private static boolean isPaused;
    private static Handler handler;
    private static Runnable timeCheckRunnable;

    private static void init() {
        handler = new Handler(Looper.getMainLooper());
        isPaused = false;

        timeCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    Log.i(TAG, "Paused - skipping time check.");
                    return;
                }

                long millis = Millis.getMillisInCurrentMinute();

                if (millis < 15000) {
                    CheckOnline.getInstance().start();
                    VRFCompete.getInstance().stop();
                    GenerateBlock.getInstance().stop();
                } else if (millis < 16000) {
                    if (CORT.getCurrentOnlineNodeNum() < 1 + (Own.getTrustedNodeNumber() / 2)) {
                        Log.i(TAG, "No enough online nodes, wait to the next round.");
                        ULog.add("No enough online nodes, wait to the next round.");
                        pause();
                    } else {
                        Log.i(TAG, "Enough online nodes, go to the next process.");
                    }
                } else if (millis < 35000) {
                    CheckOnline.getInstance().stop();
                    VRFCompete.getInstance().start();
                    GenerateBlock.getInstance().stop();
                } else if (millis < 58000) {
                    CheckOnline.getInstance().stop();
                    VRFCompete.getInstance().stop();
                    GenerateBlock.getInstance().start();
                } else {
                    CORT.ClearCORTStatus();
                    Log.i(TAG, "Cleared current ORT status.");
                }

                // Schedule the next execution of this task after 1 second
                handler.postDelayed(this, 1000);
            }
        };
    }

    public static void startTimeCheck() {
        init();
        Log.i(TAG, "Starting time check loop.");
        handler.post(timeCheckRunnable);
    }

    public static void pause() {
        if (!isPaused) {
            Log.i(TAG, "Pausing time check loop.");
            isPaused = true;
            handler.removeCallbacks(timeCheckRunnable);
            handler.postDelayed(TimeController::resume, Millis.getMillisUntilNextMinute());
            Log.i(TAG, "Scheduled to resume at the start of the next minute in " + Millis.getMillisUntilNextMinute() + " ms");
        }
    }

    private static void resume() {
        if (isPaused) {
            Log.i(TAG, "Resuming time check loop.");
            isPaused = false;
            handler.post(timeCheckRunnable);
        }
    }
}
