package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.Util;
import org.mozilla.javascript.NativeArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UtilsImpl implements Util {
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
    public Object randomElement(NativeArray array) {
        if (array.size() == 0) {
            return null;
        }
        int i = random(array.size());
        return array.get(i);
    }
}
