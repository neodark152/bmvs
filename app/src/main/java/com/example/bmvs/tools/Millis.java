package com.example.bmvs.tools;

public class Millis {
    // Gets the number of milliseconds elapsed in the current minute.
    public static long getMillisInCurrentMinute() {
        return System.currentTimeMillis() % 60000;
    }

    // Calculates milliseconds remaining until the start of the next minute.
    public static long getMillisUntilNextMinute() {
        return 60000 - getMillisInCurrentMinute();
    }
}