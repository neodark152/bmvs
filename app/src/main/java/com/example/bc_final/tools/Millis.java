package com.example.bc_final.tools;

public class Millis {
    /**
     * Gets the number of milliseconds elapsed in the current minute.
     *
     * @return Milliseconds (0-59999) since the start of the current minute
     */
    public static long getMillisInCurrentMinute() {
        return System.currentTimeMillis() % 60000;
    }

    /**
     * Calculates milliseconds remaining until the start of the next minute.
     *
     * @return Milliseconds (1-60000) until next minute transition
     *         Note: Returns 60000 exactly at minute transitions (when current millis is 0)
     */
    public static long getMillisUntilNextMinute() {
        return 60000 - getMillisInCurrentMinute();
    }
}