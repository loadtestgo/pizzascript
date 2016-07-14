package com.loadtestgo.util;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String removeStart(String str, String start) {
        if (str.startsWith(start)) {
            return str.substring(start.length());
        }
        return str;
    }

    public static String addForwardSlash(String str) {
        if (str.endsWith("/")) {
            return str;
        } else {
            return str + "/";
        }
    }

    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        return str.isEmpty();
    }

    public static boolean isSet(String str) {
        return !isEmpty(str);
    }

    public static String join(String joinString, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i){
            sb.append(strings.get(i));
            if (i + 1 < strings.size()) {
                sb.append(joinString);
            }
        }
        return sb.toString();
    }

    public static String join(String joinString, String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; ++i){
            sb.append(strings[i]);
            if (i + 1 < strings.length) {
                sb.append(joinString);
            }
        }
        return sb.toString();
    }

    public static String joinAndQuote(char joinChar, char quoteChar, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i) {
            sb.append(quoteChar);
            sb.append(strings.get(i));
            if (i + 1 < strings.size()) {
                sb.append(joinChar);
            }
            sb.append(quoteChar);
        }
        return sb.toString();
    }

    public static String joinAndQuote(char joinChar, char quoteChar, String[] strings) {
        if (strings == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; ++i) {
            sb.append(quoteChar);
            sb.append(strings[i]);
            if (i + 1 < strings.length) {
                sb.append(joinChar);
            }
            sb.append(quoteChar);
        }
        return sb.toString();
    }

    public static String join(char joinChar, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i){
            sb.append(strings.get(i));
            if (i + 1 < strings.size()) {
                sb.append(joinChar);
            }
        }
        return sb.toString();
    }

    public static Object toAscii(byte[] bytes) {
        try {
            return new String(bytes, "ASCII");
        } catch (UnsupportedEncodingException e) {
            // Yeah right
            return "";
        }
    }

    // Quote strings with commas, double up existing quotes
    public static String escapeCsv(String s) {
        if (s == null) {
            return "";
        }

        int comma = s.indexOf(',');
        if (comma >= 0) {
            s = s.replaceAll("\"", "\"\"");
            s = "\"" + s + "\"";
        }

        return s;
    }

    public static String formatIp(byte[] ip) {
        if (ip == null) {
            return "";
        }
        try {
            InetAddress inetAddress = InetAddress.getByAddress(ip);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String formatTimeSpanSeconds(long span) {
        final int minute = 60;
        final int hour = 60 * 60;
        final int day = 24 * hour;

        if (span < minute) {
            return span + "s";
        } else if (span < hour) {
            return (span / 60) + "m " + (span % 60) + "s";
        } else if (span < day) {
            span = span / 60;
            return (span / 60) + "h " + (span % 60) + "m";
        } else {
            long days = span / day;
            long hours = (span % day) / hour;
            long minutes = (span % hour) / minute;
            return days + "d " + hours + "h " + minutes + "m";
        }
    }

    /**
     * Parse a time span that's in minutes by defualt.
     *
     * Return number of seconds.
     */
    public static long parseTimeSpan(String timeSpan) {
        Pattern hourRegex = Pattern.compile("(\\d+)h");
        Pattern minRegex = Pattern.compile("(\\d+)m");
        Pattern secondRegex = Pattern.compile("(\\d+)s");
        Matcher hourMatch = hourRegex.matcher(timeSpan);
        Matcher minMatch = minRegex.matcher(timeSpan);
        Matcher secMatch = secondRegex.matcher(timeSpan);

        boolean hourMatched = hourMatch.find();
        boolean minMatched = minMatch.find();
        boolean secMatched = secMatch.find();

        if (hourMatched || minMatched || secMatched) {
            long hour = 0;
            long min = 0;
            long sec = 0;

            if (hourMatched) {
                hour = Long.parseLong(hourMatch.group(1));
            }

            if (minMatched) {
                min = Long.parseLong(minMatch.group(1));
            }

            if (secMatched) {
                sec = Long.parseLong(secMatch.group(1));
            }

            return (hour * 60 * 60) + (min * 60) + (sec);
        }

        return Long.parseLong(timeSpan) * 60;
    }


    public static int parseInteger(String val, int defaultValue) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long parseLong(String val, long defaultValue) {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float parseFloat(String val, float defaultValue) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDouble(String val, double defaultValue) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer parseInteger(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseLong(String val) {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Float parseFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double parseDouble(String val) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
