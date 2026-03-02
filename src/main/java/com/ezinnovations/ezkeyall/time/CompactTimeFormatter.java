package com.ezinnovations.ezkeyall.time;

public final class CompactTimeFormatter {
    private CompactTimeFormatter() {
    }

    public static String format(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);

        if (safeSeconds >= 60) {
            int totalMinutes = safeSeconds / 60;
            return totalMinutes + "m";
        }

        return safeSeconds + "s";
    }
}
