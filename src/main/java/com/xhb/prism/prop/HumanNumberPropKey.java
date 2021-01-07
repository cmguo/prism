package com.xhb.prism.prop;


import com.xhb.prism.util.data.HumanNumber;
import com.xhb.prism.util.data.HumanNumber.Template;

public class HumanNumberPropKey<E extends Number> extends PropConfigurableKey<E> {

    private HumanNumber.Template mTemplate;

    public HumanNumberPropKey(String title, Template tpl) {
        super(title);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, String desc) {
        super(title, desc);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, E[] values) {
        super(title, values);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, String[] values, int...unused) {
        super(title, values);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, String desc, E[] values) {
        super(title, desc, values);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, String desc, 
            String[] values, int...unused) {
        super(title, desc, values);
        mTemplate = tpl;
    }
    
    public HumanNumberPropKey(String title, Template tpl, E[] values, String[] titles) {
        super(title, values, titles);
        mTemplate = tpl;
    }

    public HumanNumberPropKey(String title, Template tpl, String[] values, 
            String[] titles, int...unused) {
        super(title, values, titles);
        mTemplate = tpl;
    }

    public HumanNumberPropKey(String title, Template tpl, String desc, E[] values,
            String[] titles) {
        super(title, desc, values, titles);
        mTemplate = tpl;
    }

    public HumanNumberPropKey(String title, Template tpl, String desc, String[] values,
            String[] titles, int...unused) {
        super(title, desc, values, titles);
        mTemplate = tpl;
    }

    @Override
    public E valueFromString(String value) {
        if (value == null)
            return null;
        return HumanNumber.parse(value, mTemplate);
    }
    
    @Override
    public String valueToString(E value) {
        if (value == null)
            return null;
        return HumanNumber.format(value, mTemplate);
    }
    
}
