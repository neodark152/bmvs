package com.example.bc_final.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.bc_final.R;

import java.util.Objects;

/**
 * Android utility class for real-time log display in UI.
 */
public class ULog {
    private static Handler handler;

    public static synchronized void init() {
        handler = new Handler(Looper.getMainLooper());
        Log.i("INIT", "ULog class initialize successfully.");
    }

    /**
     * Add log function.
     * @param logs message need to show in main UI.
     */
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
