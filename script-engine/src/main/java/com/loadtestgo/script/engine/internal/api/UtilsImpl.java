package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.Util;
import org.mozilla.javascript.NativeArray;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class UtilsImpl implements Util {
    private Random random = new Random();
    private static String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";

    public UtilsImpl() {
    }

    @Override
    public String date(String format) {
        return date(format, 0);
    }

    @Override
    public String date(String format, int daysOffset) {
        Date date = new Date();
        SimpleDateFormat dt1 = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, daysOffset);
        return dt1.format(c.getTime());
    }

    @Override
    public String dateTime(String format) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }

    @Override
    public String dateTime(String format, Date date) {
        OffsetDateTime dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
            ZoneOffset.UTC.normalized());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return dateTime.format(formatter);
    }

    @Override
    public int random(int max) {
        if (max <= 0) {
            return 0;
        }
        int r = (int)Math.floor(Math.random() * max);
        if (r == max) {
            return max - 1;
        }
        return r;
    }

    @Override
    public String randomString() {
        return randomString(ALPHANUMERIC, 10);
    }

    @Override
    public String randomString(String chars) {
        return randomString(chars, 10);
    }

    @Override
    public String randomString(String chars, int len) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            str.append(chars.charAt(random.nextInt(chars.length())));
        }
        return str.toString();
    }

    @Override
    public String randomString(int len) {
        return randomString(ALPHANUMERIC, len);
    }

    @Override
    public Object randomElement(NativeArray array) {
        if (array.size() == 0) {
            return null;
        }
        int i = random(array.size());
        return array.get(i);
    }
}
