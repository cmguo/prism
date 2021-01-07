package com.xhb.prism.prop;

public class PropMutableKey<E> extends PropKey<E> {
	
    public PropMutableKey() {
    }
    
    public PropMutableKey(String title) {
        super(title);
    }
    
    public PropMutableKey(String title, String desc) {
        super(title, desc);
    }
    
    public PropMutableKey(String title, E[] values) {
        super(title, values);
    }
    
    public PropMutableKey(String title, String[] values, int...unused) {
        super(title, values);
    }

    public PropMutableKey(String title, String desc, E[] values) {
        super(title, desc, values);
    }
    
    public PropMutableKey(String title, String desc, String[] values, int...unused) {
        super(title, desc, values);
    }

    public PropMutableKey(String title, E[] values, String[] titles) {
        super(title, values, titles);
    }

    public PropMutableKey(String title, String[] values, String[] titles, int...unused) {
        super(title, values, titles);
    }

    public PropMutableKey(String title, String desc, E[] values, String[] titles) {
        super(title, desc, values, titles);
    }

    public PropMutableKey(String title, String desc, String[] values,
            String[] titles, int...unused) {
        super(title, desc, values, titles);
    }

}
