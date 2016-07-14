package com.loadtestgo.util;

public class TimeFormat {
    static public String secondsToShortTime(long seconds) {
        long minute = 60;
        long hour = 60 * 60;
        long day = 24 * hour;
        if (seconds < minute) {
            return String.format("%02ds", seconds);
        } else if (seconds < hour) {
            return String.format("%02dm %02ds", (long)Math.floor(seconds / 60), seconds % 60);
        } else if (seconds < day) {
            long minutes = (long)Math.floor(seconds/60);
            return String.format("%dh %02dm %02ds", (long)Math.floor(minutes / 60), minutes % 60, seconds % 60);
        } else {
            long days = (long)Math.floor(seconds / day);
            long hours = (long)Math.floor((seconds % day) / hour);
            long minutes = (long)Math.floor((seconds % hour) / minute);
            return String.format("%dd %dh %02dm", days, hours, minutes);
        }
    }
}
