package com.xhb.prism.prop;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;

public class PropKey<E> implements Comparable<PropKey<E>> {
	
    protected static final String TAG = "PropKey";
    
    private Class<? extends PropertySet> mClass;
    private String mName;
    private String mTitle;
    private String mDesc;
    private Type mType;
    private E[] mValues;
    private String[] mStringValues;
    private String[] mValueTitles;

    public PropKey() {
    }
    
    public PropKey(String title) {
        mTitle = title;
    }
    
    public PropKey(String title, String desc) {
        mTitle = title;
        mDesc = desc;
    }
    
    public PropKey(String title, E[] values) {
        mTitle = title;
        mValues = values;
    }
    
    /*
     * @param unused: use to avoid generic method overloading ambiguity
     */
    public PropKey(String title, String[] values, int...unused) {
        mTitle = title;
        mStringValues = values;
        mValueTitles = values;
    }
    
    public PropKey(String title, String desc, E[] values) {
        mTitle = title;
        mDesc = desc;
        mValues = values;
    }
    
    public PropKey(String title, String desc, String[] values, int...unused) {
        mTitle = title;
        mDesc = desc;
        mStringValues = values;
        mValueTitles = values;
    }
    
    public PropKey(String title, E[] values, String[] titles) {
        mTitle = title;
        mValues = values;
        mValueTitles = titles;
    }
    
    public PropKey(String title, String[] values, String[] titles, int...unused) {
        mTitle = title;
        mStringValues = values;
        mValueTitles = titles;
    }

    public PropKey(String title, String desc, E[] values, String[] titles) {
        mTitle = title;
        mDesc = desc;
        mValues = values;
        mValueTitles = titles;
    }
    
    public PropKey(String title, String desc, String[] values, 
            String[] titles, int...unused) {
        mTitle = title;
        mDesc = desc;
        mStringValues = values;
        mValueTitles = titles;
    }
    
    public Class<? extends PropertySet> getClazz() {
        return mClass;
    }
    
    public String getName() {
        return mName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDesc() {
        return mDesc == null ? mTitle : mDesc;
    }

    public Type getType() {
        return mType;
    }

    public E valueFromString(String value) {
        return PropValue.fromString(mType, value);
    }

    public String valueToString(E value) {
        return PropValue.toString(value);
    }

    @SuppressWarnings("unchecked")
    public E[] getValues() {
        if (mStringValues != null && mValues == null) {
            mValues = (E[]) Array.newInstance((Class<?>) mType, 
                    mStringValues.length);
            for (int i = 0; i < mStringValues.length; ++i)
                mValues[i] = valueFromString(mStringValues[i]);
        }
        return mValues == null 
                ? (E[]) PropValue.getValues(mType) : mValues;
    }
    
    public String getValueTitle(E value) {
        if (mValues == null || mValueTitles == null)
            return PropValue.toString(value, true);
        int index = Arrays.asList(mValues).indexOf(value);
        if (index < 0)
            return PropValue.toString(value, true);
        return mValueTitles[index];
    }
    
    @Override
    public int compareTo(PropKey<E> another) {
        return mClass == another.mClass
                ? mName.compareTo(another.mName)
                : mClass.getName().compareTo(another.mClass.getName());
    }
    
    protected void setClass(Class<? extends PropertySet> clazz) {
        mClass = clazz;
    }

    protected void setName(String name) {
        mName = name;
        if (mTitle == null)
            mTitle = name;
    }

    protected void setType(Type type) {
        mType = type;
    }

    @Override
    public String toString() {
        return mTitle;
    }

}
