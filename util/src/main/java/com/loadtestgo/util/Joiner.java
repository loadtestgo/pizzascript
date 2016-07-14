package com.loadtestgo.util;

import java.util.Collection;
import java.util.Iterator;

public class Joiner {
    public static String join(Collection<?> values, String separator) {
        if (values == null || values.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<?> it = values.iterator();
        if (it.hasNext()) {
            sb.append(it.next().toString());
        }

        while (it.hasNext()) {
            sb.append(separator);
            sb.append(it.next().toString());
        }

        return sb.toString();
    }
}
