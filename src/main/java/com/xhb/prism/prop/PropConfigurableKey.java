package com.xhb.prism.prop;

/*
 * Configurable prop can be modify by user interface, it is also mutable.   
 */
public class PropConfigurableKey<E> extends PropMutableKey<E> {
	
    public PropConfigurableKey() {
    }
    
    public PropConfigurableKey(String title) {
        super(title);
    }
    
    public PropConfigurableKey(String title, String desc) {
        super(title, desc);
    }
    
    public PropConfigurableKey(String title, E[] values) {
        super(title, values);
    }
    
    public PropConfigurableKey(String title, String[] values, int...unused) {
        super(title, values);
    }

    public PropConfigurableKey(String title, String desc, E[] values) {
        super(title, desc, values);
    }
    
    public PropConfigurableKey(String title, String desc, String[] values, int...unused) {
        super(title, desc, values);
    }

    public PropConfigurableKey(String title, E[] values, String[] titles) {
        super(title, values, titles);
    }

    public PropConfigurableKey(String title, String[] values, String[] titles, int...unused) {
        super(title, values, titles);
    }

    public PropConfigurableKey(String title, String desc, E[] values,
            String[] titles) {
        super(title, desc, values, titles);
    }

    public PropConfigurableKey(String title, String desc, String[] values,
            String[] titles, int...unused) {
        super(title, desc, values, titles);
    }

}
