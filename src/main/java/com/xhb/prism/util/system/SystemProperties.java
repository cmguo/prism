package com.xhb.prism.util.system;


import com.xhb.prism.util.reflect.ClassWrapper;

public class SystemProperties {

    private static ClassWrapper<?> sSysProp =
            ClassWrapper.wrap("android.os.SystemProperties");
    
    public static String get(String key) {
        return sSysProp.invoke("get", key);
    }
    
    public static String get(String key, String def) {
        return sSysProp.invoke("get", key, def);
    }
    
    public static int getInt(String key, int def) {
        return sSysProp.invoke("getInt", key, def);
    }
    
    public static long getLong(String key, long def) {
        return sSysProp.invoke("getLong", key, def);
    }
    
    public static boolean getBoolean(String key, boolean def) {
        return sSysProp.invoke("getBoolean", key, def);
    }
    
    public static void set(String key, String val) {
        sSysProp.invoke("set", key, val);
    }
    
}
