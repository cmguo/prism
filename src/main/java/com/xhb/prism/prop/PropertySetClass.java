package com.xhb.prism.prop;

import com.xhb.prism.util.data.Collections;
import com.xhb.prism.util.log.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class PropertySetClass {

    protected static final String TAG = "PropertySetClass";

    private static Map<Class<? extends PropertySet>, PropertySetClass> sClassMap =
            new HashMap<Class<? extends PropertySet>, PropertySetClass>();

    static {
        sClassMap.put(PropertySet.class, new RootPropertySetClass());
    }

    private Class<? extends PropertySet> mClass;
    private PropertySetClass mParent;
    private PropertySetClass mCreatorClass;
    private int mParentKeyCount;
    private List<PropKey<?>> mKeyList;
    private Map<String, PropKey<?>> mNameKeyMap;

    @SuppressWarnings({"unchecked"})
    PropertySetClass(Class<? extends PropertySet> clazz) {
        mClass = clazz;
        Class<?> superCls = clazz.getSuperclass();
        mParent = get((Class<? extends PropertySet>) superCls);
        mCreatorClass = mParent.mCreatorClass;
        mParentKeyCount = mParent.getKeyCount();
        mKeyList = new ArrayList<PropKey<?>>();
        mNameKeyMap = new TreeMap<String, PropKey<?>>();
        // check if has CREATOR
        // extract all PROP_* members
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers()))
                continue;
            if (!f.getName().startsWith("PROP_"))
                continue;
            Type type = f.getGenericType();
            Type type2 = null;
            if (type instanceof ParameterizedType) {
                type2 = ((ParameterizedType) type).getActualTypeArguments()[0];
            }
            String name = f.getName().substring(5);
            //Log.d(TAG, "<init> - " + name);
            PropKey<?> key = null;
            try {
                key = (PropKey<?>) f.get(null);
            } catch (Exception e) {
                Log.d(TAG, "<init> " + clazz.getName(), e);
            }
            if (key != null) {
                key.setClass(clazz);
                key.setName(name);
                if (type2 != null)
                    key.setType(type2);
                else if (key.getType() == null)
                    throw new IllegalArgumentException(f.getName()
                            + ": must set Type for non Parameterized key");
                mKeyList.add(key);
                mNameKeyMap.put(name, key);
            }
        }
    }

    PropertySetClass(Class<PropertySet> clazz, int unused) {
        mClass = clazz;
        mParent = null;
        mCreatorClass = null;
    }

    protected PropertySetClass(PropertySetClass parent) {
        mClass = parent.mClass;
        mParent = parent;
        mCreatorClass = mParent.mCreatorClass;
        mParentKeyCount = mParent.getKeyCount();
        mKeyList = new ArrayList<PropKey<?>>();
        mNameKeyMap = new TreeMap<String, PropKey<?>>();
    }

    protected void addKey(PropKey<?> key) {
        mKeyList.add(key);
        mNameKeyMap.put(key.getName(), key);
    }

    public <E> PropKey<E> findKey(String key) {
        key = key.toUpperCase();
        return _findKey(key, this);
    }

    public <E> PropKey<E> findKey(String key, boolean force) {
        key = key.toUpperCase();
        return _findKey(key, force ? this : null);
    }

    protected <E> PropKey<E> _findKey(String key) {
        return _findKey(key, this);
    }

    @SuppressWarnings("unchecked")
    protected <E> PropKey<E> _findKey(String key, PropertySetClass from) {
        PropKey<?> pKey = mNameKeyMap.get(key);
        if (pKey == null)
            pKey = mParent._findKey(key, from);
        return (PropKey<E>) pKey;
    }

    public int getKeyIndex(PropKey<?> key) {
        int index = mKeyList.indexOf(key);
        if (index == -1)
            index = mParent.getKeyIndex(key);
        else
            index += mParentKeyCount;
        return index;
    }

    public int getKeyCount() {
        return mParentKeyCount + mKeyList.size();
    }

    @SuppressWarnings("unchecked")
    public <E> PropKey<E> getKeyAt(int index) {
        if (index < mParentKeyCount)
            return mParent.getKeyAt(index);
        index -= mParentKeyCount;
        return index < mKeyList.size() ? (PropKey<E>) mKeyList.get(index) : null;
    }

    public boolean hasKey(PropKey<?> key) {
        return mKeyList.contains(key) || mParent.hasKey(key);
    }

    public boolean hasKey(String key) {
        key = key.toUpperCase();
        return _hasKey(key);
    }

    protected boolean _hasKey(String key) {
        return mNameKeyMap.containsKey(key) || mParent._hasKey(key);
    }

    public Collection<PropKey<?>> allKeys() {
        return Collections.join(mParent.allKeys(), mKeyList);
    }

    public Class<? extends PropertySet> getJavaClass() {
        return mClass;
    }

    public PropertySetClass getParentClass() {
        return mParent;
    }

    public PropertySetClass getCreatorClass() {
        return mCreatorClass;
    }

    public static synchronized PropertySetClass get(Class<? extends PropertySet> clazz) {
        PropertySetClass pclazz = sClassMap.get(clazz);
        if (pclazz == null) {
            pclazz = new PropertySetClass(clazz);
            sClassMap.put(clazz, pclazz);
        }
        return pclazz;
    }
}

class RootPropertySetClass extends PropertySetClass {

    private List<PropKey<?>> mKeyList = new ArrayList<PropKey<?>>();
    
    RootPropertySetClass() {
        super(PropertySet.class, 0);
    }

    @Override
    protected <E> PropKey<E> _findKey(String key, PropertySetClass from) {
        if (from != null) {
            Log.e(TAG, "_findKey missing " + key + " from " + from.getJavaClass(), 
                    new Throwable());
        }
        return null;
    }

    @Override
    public int getKeyIndex(PropKey<?> key) {
        return -1;
    }

    @Override
    public int getKeyCount() {
        return 0;
    }

    @Override
    public <E> PropKey<E> getKeyAt(int index) {
        return null;
    }

    @Override
    public boolean hasKey(PropKey<?> key) {
        return false;
    }

    @Override
    protected boolean _hasKey(String key) {
        return false;
    }

    @Override
    public Collection<PropKey<?>> allKeys() {
        return mKeyList;
    }
    
}

