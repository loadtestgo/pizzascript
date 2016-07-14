package com.loadtestgo.script.engine.internal.rhino;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Undefined;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RhinoUtils {
    /**
     * Convert a JSONObject to native Rhino Scriptable object.
     * @param data
     * @return
     */
    static public Object serialize(Object data) {
        if (data instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray)data;
            NativeArray array = new NativeArray(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i) {
                try {
                    array.put(i, array, serialize(jsonArray.get(i)));
                } catch (JSONException e) {
                    Logger.info("Unable to serialize to rhino object: ", e);
                }
            }
            return array;
        } else if (data instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)data;
            NativeObject object = new NativeObject();
            Iterator keys = jsonObject.keys();
            while (keys.hasNext()) {
                Object o = keys.next();
                String name = (String)o;
                try {
                    object.put(name, object, serialize(jsonObject.get(name)));
                } catch (JSONException e) {
                    Logger.info("Unable to serialize to rhino object: ", e);
                }
            }
            return object;
        } else {
            return data;
        }
    }

    static public Map<String,Object> deserialize(NativeObject object) {
        HashMap<String,Object> map = new HashMap<>();
        for (Object key : object.keySet()) {
            Object value = object.get(key);
            if (value == null) {
                map.put(key.toString(), null);
            } else if (value instanceof Number) {
                map.put(key.toString(), value);
            } else if (value instanceof Boolean) {
                map.put(key.toString(), value);
            } else if (value instanceof NativeObject) {
                map.put(key.toString(), deserialize((NativeObject)value));
            } else if (value instanceof NativeArray) {
                NativeArray array = (NativeArray)value;
                Object[] a = new Object[(int)array.getLength()];
                for (int i = 0; i < array.getLength(); ++i) {
                    Object o = array.get(i);
                    a[i] = deserialize(o);
                }
                map.put(key.toString(), a);
            } else {
                map.put(key.toString(), value.toString());
            }
        }
        return map;
    }

    static Object deserialize(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return value;
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof NativeObject) {
            return deserialize((NativeObject)value);
        } else if (value instanceof NativeArray) {
            NativeArray array = (NativeArray)value;
            Object[] a = new Object[(int)array.getLength()];
            for (int i = 0; i < array.getLength(); ++i) {
                Object o = array.get(i);
                a[i] = deserialize(o);
            }
            return a;
        } else {
            return value.toString();
        }
    }

    public static boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Undefined) {
            return false;
        } else if (value instanceof Number) {
            return ((Number)value).intValue() != 0;
        } else if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else if (value instanceof NativeJavaObject) {
            return toBoolean(((NativeJavaObject) value).unwrap());
        } else {
            return true;
        }
    }

    public static String toString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String)value;
        } else {
            return value.toString();
        }
    }

    public static String[] toStringList(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof NativeArray) {
            NativeArray array = (NativeArray)value;
            String[] a = new String[(int)array.getLength()];
            for (int i = 0; i < array.getLength(); ++i) {
                Object o = array.get(i);
                a[i] = toString(o);
            }
            return a;
        } else {
            return null;
        }
    }
}
