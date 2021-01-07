package com.xhb.prism.prop;

import com.xhb.prism.util.algorithm.Crypt;

public class CryptPropKey extends PropConfigurableKey<String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "--CryptPropKey--";
    
    private String mAlgorithm;
    private String mKey;

    public CryptPropKey(String title) {
        this(title, KEY);
    }
    
    public CryptPropKey(String title, String key) {
        this(title, ALGORITHM, key);
    }
    
    public CryptPropKey(String title, String algorithm, String key) {
        super(title);
        setType(String.class);
        mAlgorithm = algorithm;
        mKey = key;
    }

    @Override
    public String valueFromString(String value) {
        if (value.startsWith("[") && value.endsWith("]"))
            return value;
        String encrypt = Crypt.encrypt(mAlgorithm, mKey, value);
        return encrypt == null ? null : "[" + encrypt + "]";
    }
    
    @Override
    public String valueToString(String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
            return Crypt.decrypt(mAlgorithm, mKey, value);
        }
        return value;
    }
    
}
