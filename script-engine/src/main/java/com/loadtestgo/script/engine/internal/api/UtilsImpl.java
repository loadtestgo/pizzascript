package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.Util;
import org.mozilla.javascript.NativeArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.stream.IntStream;

public class UtilsImpl implements Util {
    private Random random = new Random();
    private String alphaNumeric = "abcdefghijklmnopqrstuvwxyz0123456789";

    public UtilsImpl() {
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
        return randomString(alphaNumeric, 10);
    }

    @Override
    public String randomString(String chars) {
        return randomString(chars, 10);
    }

    @Override
    public String randomString(String chars, int len) {
        String str = "";
        for (int i = 0; i < len; ++i) {
            str += chars.charAt(random.nextInt(chars.length()));
        }
        return str;
    }

    @Override
    public String randomString(int len) {
        return randomString(alphaNumeric, len);
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
