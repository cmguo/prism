package com.xhb.prism.prop;

public class Property<E> {

    private PropertySet mOwner;
    private PropKey<E> mKey;
    
    public Property(PropertySet owner, PropKey<E> key) {
        mOwner = owner;
        mKey = key;
    }
    
    public E get() {
        return mOwner.getProp(mKey);
    }
    
    public void set(E v) {
        if (mKey instanceof PropConfigurableKey<?>)
            mOwner.setProp((PropConfigurableKey<E>) mKey, v);
        else if (mKey instanceof PropMutableKey<?>)
            mOwner.setProp((PropMutableKey<E>) mKey, v);
        else
            mOwner.setProp(mKey, v);
    }
    
    public String getString() {
        E v = get();
        return v == null ? null : mOwner.valueToString(mKey, v);
    }
    
    public void setString(String str) {
        E v = str == null ? null : mOwner.valueFromString(mKey, str);
        set(v);
    }

    @Override
    public String toString() {
        return mKey.getTitle();
    }

    public PropKey<E> getKey() {
        return mKey;
    }

    public int getKeyIndex() {
        return mOwner.myClass().getKeyIndex(mKey);
    }

    public E[] getValues() {
        return mOwner.getPropValues(mKey);
    }

    public String getValueTitle(E value) {
        return mOwner.getValueTitle(mKey, value);
    }
    
    public E valueFromString(String value) {
        return mOwner.valueFromString(mKey, value);
    }

    public String valueToString(E value) {
        return mOwner.valueToString(mKey, value);
    }
    
    @Deprecated
    public static <E> E[] getValues(PropertySet owner, PropKey<E> key) {
        return owner.getPropValues(key);
    }
    
    @Deprecated
    public static <E> String getValueTitle(PropertySet owner, PropKey<E> key, E value) {
        return owner.getValueTitle(key, value);
    }

    @Deprecated
    public static <E> E valueFromString(PropertySet owner, PropKey<E> key, String value) {
        return owner.valueFromString(key, value);
    }

    @Deprecated
    public static <E> E valueFromString(PropertySet owner, String key, String value) {
        return owner.valueFromString(key, value);
    }

    @Deprecated
    public static <E> String valueToString(PropertySet owner, PropKey<E> key, E value) {
        return owner.valueToString(key, value);
    }

    @Deprecated
    public static <E> String valueToString(PropertySet owner, String key, E value) {
        return owner.valueToString(key, value);
    }

}
