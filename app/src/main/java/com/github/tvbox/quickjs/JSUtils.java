package com.github.tvbox.quickjs;

import com.whl.quickjs.wrapper.JSArray;
import com.whl.quickjs.wrapper.JSFunction;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSUtils {

    public static Object toJavaObject(Object obj) {
        if (obj instanceof JSArray) {
            return toList((JSArray) obj);
        } else if (obj instanceof JSObject) {
            return toMap((JSObject) obj);
        }
        return obj;
    }

    public static Map<String, Object> toMap(JSObject jsObject) {
        if (jsObject == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        try {
            java.lang.reflect.Method getNames = jsObject.getClass().getMethod("getNames");
            String[] names = (String[]) getNames.invoke(jsObject);
            if (names != null) {
                for (String name : names) {
                    map.put(name, toJavaObject(jsObject.get(name)));
                }
                return map;
            }
        } catch (Throwable ignored) {
        }
        return map;
    }

    public static List<Object> toList(JSArray jsArray) {
        if (jsArray == null) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>();
        try {
            java.lang.reflect.Method lengthMethod = jsArray.getClass().getMethod("length");
            int len = (int) lengthMethod.invoke(jsArray);
            for (int i = 0; i < len; i++) {
                list.add(toJavaObject(jsArray.get(i)));
            }
        } catch (Throwable ignored) {
        }
        return list;
    }

    public static JSObject toJSObject(QuickJSContext context, Map<?, ?> map) {
        if (map == null || context == null) {
            return null;
        }
        JSObject jsObject = context.createNewJSObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                jsObject.set(key, toJSObject(context, (Map<?, ?>) value));
            } else if (value instanceof List) {
                jsObject.set(key, toJSArray(context, (List<?>) value));
            } else if (value instanceof Integer) {
                jsObject.set(key, (Integer) value);
            } else if (value instanceof Double) {
                jsObject.set(key, (Double) value);
            } else if (value instanceof Float) {
                jsObject.set(key, ((Float) value).doubleValue());
            } else if (value instanceof Long) {
                jsObject.set(key, ((Long) value).doubleValue());
            } else if (value instanceof Boolean) {
                jsObject.set(key, (Boolean) value);
            } else if (value instanceof String) {
                jsObject.set(key, (String) value);
            } else if (value == null) {
                jsObject.set(key, (String) null);
            } else {
                jsObject.set(key, value.toString());
            }
        }
        return jsObject;
    }

    public static JSArray toJSArray(QuickJSContext context, List<?> list) {
        if (list == null || context == null) {
            return null;
        }
        JSArray jsArray = context.createNewJSArray();
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            if (value instanceof Map) {
                jsArray.set(toJSObject(context, (Map<?, ?>) value), i);
            } else if (value instanceof List) {
                jsArray.set(toJSArray(context, (List<?>) value), i);
            } else if (value instanceof Integer) {
                jsArray.set((Integer) value, i);
            } else if (value instanceof Double) {
                jsArray.set((Double) value, i);
            } else if (value instanceof Float) {
                jsArray.set(((Float) value).doubleValue(), i);
            } else if (value instanceof Long) {
                jsArray.set(((Long) value).doubleValue(), i);
            } else if (value instanceof Boolean) {
                jsArray.set((Boolean) value, i);
            } else if (value instanceof String) {
                jsArray.set((String) value, i);
            } else if (value == null) {
                jsArray.set((String) null, i);
            } else {
                jsArray.set(value.toString(), i);
            }
        }
        return jsArray;
    }

    public static String stringify(QuickJSContext context, Object obj) {
        if (context == null || obj == null) {
            return "";
        }
        try {
            JSObject json = (JSObject) context.getGlobalObject().get("JSON");
            if (json != null) {
                JSFunction stringify = (JSFunction) json.get("stringify");
                if (stringify != null) {
                    return (String) stringify.call(obj);
                }
            }
        } catch (Throwable ignored) {
        }
        return String.valueOf(obj);
    }

    public static Object parse(QuickJSContext context, String jsonStr) {
        if (context == null || jsonStr == null) {
            return null;
        }
        try {
            JSObject json = (JSObject) context.getGlobalObject().get("JSON");
            if (json != null) {
                JSFunction parse = (JSFunction) json.get("parse");
                if (parse != null) {
                    return parse.call(jsonStr);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String readStream(InputStream is) {
        if (is == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String optString(JSObject obj, String key, String def) {
        if (obj == null || key == null) {
            return def;
        }
        try {
            Object val = obj.get(key);
            return val != null ? val.toString() : def;
        } catch (Exception e) {
            return def;
        }
    }

    public static int optInt(JSObject obj, String key, int def) {
        if (obj == null || key == null) {
            return def;
        }
        try {
            Object val = obj.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            if (val instanceof String) {
                return Integer.parseInt((String) val);
            }
        } catch (Exception ignored) {
        }
        return def;
    }

    public static boolean optBoolean(JSObject obj, String key, boolean def) {
        if (obj == null || key == null) {
            return def;
        }
        try {
            Object val = obj.get(key);
            if (val instanceof Boolean) {
                return (Boolean) val;
            }
            if (val instanceof String) {
                return Boolean.parseBoolean((String) val);
            }
        } catch (Exception ignored) {
        }
        return def;
    }
}
