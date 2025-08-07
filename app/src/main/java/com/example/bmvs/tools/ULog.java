package com.example.bmvs.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.bmvs.R;

import java.util.Objects;

/**
 * Real-time log display in UI (Not in Android Studio Logcat).
 */
public class ULog {
    private static Handler handler;

    public static synchronized void init() {
        handler = new Handler(Looper.getMainLooper());
        Log.i("INIT", "ULog class initialize successfully.");
    }

    public static void add(String logs) {
        if (handler == null) throw new RuntimeException("Didn't initialized ULog.");
        handler.post(() -> {
            TextView textView = Objects.requireNonNull(MainActivityRef.getMainActivity()).findViewById(R.id.log_output);
            ScrollView scrollView = Objects.requireNonNull(MainActivityRef.getMainActivity()).findViewById(R.id.scroll_view);

            textView.append("\n" + logs);
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }
}
