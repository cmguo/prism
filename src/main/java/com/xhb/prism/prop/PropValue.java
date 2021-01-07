package com.xhb.prism.prop;

import com.xhb.prism.util.log.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class PropValue {
	
    public static final int FLAG_EXTERNAL_CLASS = 1 << 8;
    
    private static final String TAG = "PropValue";

    public static <E> PropValue wrap(E value) {
        return wrap(value, 0);
    }
    
    public static <E> PropValue wrap(E value, int flags) {
        return value == null ? null : new PropValue(value, flags);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> E unwrap(PropValue value) {
        return value == null ? null : (E) value.getPropValue();
    }
    
	private Object mPropValue;
	private int mFlags;
	
    public PropValue(Object o, int f) {
        mPropValue = o;
        mFlags = f;
    }

	public void setFlags(int flags) {
	    mFlags = flags;
	}

	@Override
	public String toString() {
		return toString(mPropValue);
	}

	private Object getPropValue() {
		return mPropValue;
	}
    
    private static ClassLoader sClassLoader = PropValue.class.getClassLoader();

    private static Map<String, ClassLoader> sClassLoaders = new TreeMap<String, ClassLoader>(); 

    public static void setClassLoaders(Map<String, ClassLoader> loaders) {
        sClassLoaders = loaders;
    }

    private static Object[] copyArray(String type, Object value) {
        try {
            Class<?> componentType = Class.forName(type, 
                    true, sClassLoader);
            Object[] bad = (Object[]) value;
            Object[] good = (Object[]) Array.newInstance(componentType, bad.length);
            for (int j = 0; j < bad.length; ++j)
                good[j] = bad[j];
            return good;
        } catch (Exception e) {
            Log.w(TAG, "readValueFromParcel", e);
        }
        return null;
    }

    public static String toString(Object value) {
        return toString(value, false);
    }

    public static String toString(Object value, boolean title) {
        if (value == null) {
            return "<null>";
        } else if (value instanceof Enum) {
            return title ? String.valueOf(value) 
                    : ((Enum<?>) value).name();
        } else if (value instanceof Object[]) {
            return Arrays.deepToString((Object[]) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof boolean [])
                return Arrays.toString((boolean []) value);
            else if (value instanceof char [])
                return Arrays.toString((char []) value);
            else if (value instanceof byte [])
                return Arrays.toString((byte []) value);
            else if (value instanceof short [])
                return Arrays.toString((short []) value);
            else if (value instanceof int [])
                return Arrays.toString((int []) value);
            else if (value instanceof long [])
                return Arrays.toString((long []) value);
            else if (value instanceof float [])
                return Arrays.toString((float []) value);
            else if (value instanceof double [])
                return Arrays.toString((double []) value);
            else
                return null;
        } else {
            return String.valueOf(value);
        }
    }

    public static <E> E fromString(Class<E> type, String value) {
        return fromString((Type) type, value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <E> E fromString(Type type, String value) {
        // Log.v(TAG, "fromString type=" + type + ", value=" + value);
        try {
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isPrimitive()) {
                    // Log.v(TAG, "fromString isPrimitive");
                    return (E) fromStringPrimitive(clazz, value);
                } else if (clazz.isEnum()) {
                    // Log.v(TAG, "fromString isEnum");
                    return (E) Enum.valueOf((Class<Enum>)clazz, value);
                } else if (clazz == String.class) {
                    // Log.v(TAG, "fromString string");
                    return (E) value;
                } else if (clazz.isArray()) {
                    // Log.v(TAG, "fromString isArray");
                    return (E) fromStringArray(clazz.getComponentType(), value);
                } else {
                    // Log.v(TAG, "fromString other");
                    Method m = null;
                    try {
                        m = clazz.getMethod("valueOf", String.class);
                    } catch (Exception e) {
                        // Log.w(TAG, "fromString", e);
                    }
                    if (m != null && Modifier.isStatic(m.getModifiers())) {
                        // Log.v(TAG, "fromString valueOf");
                        return (E) m.invoke(null, value);
                    }
                    Constructor c = null;
                    try {
                        c = clazz.getConstructor(String.class);
                    } catch (Exception e) {
                        // Log.w(TAG, "fromString", e);
                    }
                    if (c != null) {
                        // Log.v(TAG, "fromString constructor");
                        return (E) c.newInstance(value);
                    }
                    Exception e = new InvalidParameterException(
                            "No fromString method for type " + type);;
                    Log.w(TAG, "fromString", e);
                    throw e;
                }
            } else if (type instanceof GenericArrayType) {
                // Log.v(TAG, "fromString GenericArrayType");
                return (E) fromStringArray(((GenericArrayType) type).getGenericComponentType(), value);
            } else if (type instanceof ParameterizedType) {
                // Log.v(TAG, "fromString ParameterizedType");
                Type clazz = ((ParameterizedType) type).getRawType();
                if (clazz instanceof Class && Map.class.isAssignableFrom((Class<?>) clazz)) {
                    Log.v(TAG, "fromString isMap");
                    Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                    return (E) fromStringMap(clazz, types[0], types[1], value);
                }
                return fromString(clazz, value);
            }
        } catch (Exception e) {
            Log.w(TAG, "fromString", e);
        }
        return null;
    }

    public static Object fromStringPrimitive(Class<?> clazz, String value) {
        // Log.v(TAG, "fromStringPrimitive clazz=" + clazz + ", value=" + value);
        if (clazz == boolean.class)
            return Boolean.valueOf(value);
        else if (clazz == char.class)
            if (value.length() == 1)
                return value.charAt(0);
            else 
                throw new NumberFormatException("Invalid char: \"" + value + "\""); 
        else if (clazz == byte.class)
            return Byte.valueOf(value);
        else if (clazz == short.class)
            return Short.valueOf(value);
        else if (clazz == int.class)
            return Integer.valueOf(value);
        else if (clazz == long.class)
            return Long.valueOf(value);
        else if (clazz == float.class)
            return Float.valueOf(value);
        else if (clazz == double.class)
            return Double.valueOf(value);
        else
            return null;
    }

    public static Object fromStringArray(Type componentType, String value) {
        // Log.v(TAG, "fromStringArray componentType=" + componentType + ", value=" + value);
        if (!value.startsWith("[") || !value.endsWith("]"))
            return null;
        Class<?> clazz = null;
        if ((componentType instanceof Class))
            clazz = (Class<?>) componentType;
        else
            clazz = (Class<?>) ((ParameterizedType) componentType).getRawType();
        value = value.substring(1, value.length() - 1);
        String[] values = value.isEmpty() ? new String[0] : value.split(", ?", -1);
        if (componentType == String.class)
            return values;
        Object pValues = Array.newInstance(clazz, values.length);
        for (int i = 0; i < values.length; ++i) {
            Array.set(pValues, i, fromString(componentType, values[i]));
        }
        return pValues;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object fromStringMap(Type mapType, Type keyType, Type valueType, 
            String value) throws Exception {
        Log.v(TAG, "fromStringMap mapType=" + mapType + ", keyType=" + keyType 
                + ", valueType=" + valueType + ", value=" + value);
        if (!value.startsWith("{") || !value.endsWith("}"))
            return null;
        Class<?> clazz = (Class<?>) mapType;
        Map map = (Map) clazz.newInstance();
        value = value.substring(1, value.length() - 1);
        String[] values = value.isEmpty() ? new String[0] : value.split(", ?", -1);
        for (String kv : values) {
            String[] parts = kv.split("=", 2);
            if (parts.length == 0)
                continue;
            Object k = fromString(keyType, parts[0]);
            Object v = null;
            if (parts.length > 1)
                v = fromString(valueType, parts[1]);
            map.put(k, v);
        }
        return map;
    }
    
    public static boolean isInstance(Type type, Object value) {
        if (value == null) {
            return false;
        } else if (type instanceof Class) {
            return ((Class<?>) type).isInstance(value);
        } else if (type instanceof GenericArrayType) {
            return isAssignableFrom(type, value.getClass());
        } else if (type instanceof ParameterizedType) {
            return isAssignableFrom(((ParameterizedType) type).getRawType(), value.getClass());
        } else {
            return false;
        }
    }
    
    public static boolean isAssignableFrom(Type type, Class<?> clazz) {
        if (type instanceof Class) {
            return ((Class<?>) type).isAssignableFrom(clazz);
        } else if (type instanceof GenericArrayType) {
            return clazz.isArray() && isAssignableFrom(
                    ((GenericArrayType) type).getGenericComponentType(), clazz.getComponentType());
        } else if (type instanceof ParameterizedType) {
            return isAssignableFrom(((ParameterizedType) type).getRawType(), clazz);
        } else {
            return false;
        }
    }

    @SuppressWarnings({ "unchecked" })
    public static <E> E[] getValues(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isEnum()) {
                return (E[]) clazz.getEnumConstants();
            } else if (clazz == Boolean.class) {
                return (E[]) new Boolean[] { false, true };
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
