package com.xhb.prism.util.data;

import java.lang.reflect.Type;

public class HumanNumber {

    public static final String BYTES_FORMAT = "GMKB";
    public static final int[] BYTES_SCALE = new int[] {
        1048576 * 1024, 1048576, 1024, 1
    };
    
    public static final String LONG_BYTES_FORMAT = "TGMKB";
    public static final long[] LONG_BYTES_SCALE = new long[] {
        1048576L * 1048576L, 1048576 * 1024, 1048576, 1024, 1
    };
    
    public static final String INTEVAL_FORMAT = "hms";
    public static final int[] INTEVAL_SCALE = new int[] {
        3600 * 1000, 60 * 1000, 1000
    };
    
    public static final String LONG_INTEVAL_FORMAT = "yMwdhms";
    public static final long[] LONG_INTEVAL_SCALE = new long[] {
        365L * 24 * 3600 * 1000, 30L * 24 * 3600 * 1000, 
        7 * 24 * 3600 * 1000, 24 * 3600 * 1000, 
        3600 * 1000, 60 * 1000, 1000
    };
    
    public static final class Template {
        
        Type mType;
        String mFormat;
        Object mScale; // array
        
        public Template(Class<?> type, String format, Object scale) {
            if (!scale.getClass().isArray())
                throw new IllegalArgumentException("Scale is not array");
            mType = type;
            mFormat = format;
            mScale = scale;
        }
    }
    
    public static final Template BYTES = 
            new Template(int.class, BYTES_FORMAT, BYTES_SCALE);
    public static final Template LONG_BYTES = 
            new Template(long.class, LONG_BYTES_FORMAT, LONG_BYTES_SCALE);
    public static final Template INTEVAL = 
            new Template(int.class, INTEVAL_FORMAT, INTEVAL_SCALE);
    public static final Template LONG_INTEVAL = 
            new Template(long.class, LONG_INTEVAL_FORMAT, LONG_INTEVAL_SCALE);
    
    public static int parseBytes(String str) {
        return parseInt(str, BYTES_FORMAT, BYTES_SCALE);
    }

    public static long parseLongBytes(String str) {
        return parseLong(str, LONG_BYTES_FORMAT, LONG_BYTES_SCALE);
    }

    public static long parseInteval(String str) {
        return parseInt(str, INTEVAL_FORMAT, INTEVAL_SCALE);
    }

    public static long parseLongInteval(String str) {
        return parseLong(str, LONG_INTEVAL_FORMAT, LONG_INTEVAL_SCALE);
    }

    public static int parseInt(
            String str, String format, final int[] scale) {
        return parse(str, format, new IntegerScaler(scale));
    }

    public static long parseLong(
            String str, String format, final long[] scale) {
        return parse(str, format, new LongScaler(scale));
    }

    public static float parseFloat(
            String str, String format, final float[] scale) {
        return parse(str, format, new FloatScaler(scale));
    }
    
    @SuppressWarnings("unchecked")
    public static <E extends Number> E parse(String str, Template tpl) {
        if (tpl.mType == int.class)
            return (E) parse(str, tpl.mFormat, new IntegerScaler((int[]) tpl.mScale));
        else if (tpl.mType == long.class)
            return (E) parse(str, tpl.mFormat, new LongScaler((long[]) tpl.mScale));
        else if (tpl.mType == float.class)
            return (E) parse(str, tpl.mFormat, new FloatScaler((float[]) tpl.mScale));
        else
            throw new IllegalArgumentException("Unknown type " + tpl.mType);
    }

    public static <E extends Number> E parse(
            String str, String format, Scaler<E> scaler) {
        int s = 0;
        int p = 0;
        boolean neg = false;
        if (str.startsWith("-")) {
            ++s;
            ++p;
            neg = true;
        }
        while (p < str.length()) {
            int f = format.indexOf(str.charAt(p));
            if (f < 0) {
                ++p;
                continue;
            }
            if (p == s) {
                scaler.add(f);
            } else {
                scaler.add(f, str.substring(s, p));
            }
            s = ++p;
        }
        if (s < p)
            scaler.add(str.substring(s, p));
        if (neg)
            scaler.negtive();
        return scaler.get();
    }
    
    public static String formatBytes(int val) {
        return formatInt(val, BYTES_FORMAT, BYTES_SCALE);
    }

    public static String formatLongBytes(long val) {
        return formatLong(val, LONG_BYTES_FORMAT, LONG_BYTES_SCALE);
    }

    public static String formatInteval(int val) {
        return formatInt(val, INTEVAL_FORMAT, INTEVAL_SCALE);
    }

    public static String formatLongInteval(long val) {
        return formatLong(val, LONG_INTEVAL_FORMAT, LONG_INTEVAL_SCALE);
    }

    public static String formatInt(
           int val, String format, final int[] scale) {
        return format(val, format, new IntegerScaler(scale));
    }

    public static String formatLong(
            long val, String format, final long[] scale) {
        return format(val, format, new LongScaler(scale));
    }

    public static String formatFloat(
            float val, String format, final float[] scale) {
        return format(val, format, new FloatScaler(scale));
    }
    
    public static <E extends Number> String format(E val, Template tpl) {
        if (tpl.mType == int.class)
            return format((Integer) val, tpl.mFormat, new IntegerScaler((int[]) tpl.mScale));
        else if (tpl.mType == long.class)
            return format((Long) val, tpl.mFormat, new LongScaler((long[]) tpl.mScale));
        else if (tpl.mType == float.class)
            return format((Float) val, tpl.mFormat, new FloatScaler((float[]) tpl.mScale));
        else
            throw new IllegalArgumentException("Unknown type " + tpl.mType);
    }

    public static <E extends Number> String format(E val, String format, 
            Scaler<E> scaler) {
        StringBuilder sb = new StringBuilder();
        scaler.set(val);
        if (scaler.isNegtive()) {
            sb.append('-');
            scaler.negtive();
        }
        for (int i = 0; i < format.length(); ++i) {
            char f = format.charAt(i);
            String s = scaler.dec(i);
            if (s == null)
                break;
            if (s.length() > 0) {
                sb.append(s);
                sb.append(f);
            }
        }
        if (sb.length() == 0) {
            sb.append('0');
            sb.append(format.charAt(format.length() - 1));
        }
        return sb.toString();
    }
    
    public interface Scaler<E> {
        void add(int s);
        void add(int s, String v);
        void add(String v);
        void negtive();
        E get();
        void set(E v);
        boolean isNegtive();
        String dec(int s);
    }

    private static final class IntegerScaler implements Scaler<Integer> {
        
        private final int[] mScale;
        private int mResult = 0;

        private IntegerScaler(int[] scale) {
            this.mScale = scale;
        }

        @Override
        public void add(int s) {
            mResult += mScale[s];
        }

        @Override
        public void add(int s, String v) {
            mResult += mScale[s] * Integer.parseInt(v);
        }

        @Override
        public void add(String v) {
            mResult += Integer.parseInt(v);
        }
        
        @Override
        public void negtive() {
            mResult = -mResult;
        }
        
        @Override
        public Integer get() {
            return mResult;
        }
        
        @Override
        public void set(Integer v) {
            mResult = v;
        }
        
        @Override
        public boolean isNegtive() {
            return mResult < 0;
        }
        
        @Override
        public String dec(int s) {
            if (mResult == 0)
                return null;
            int i = mResult / mScale[s];
            mResult -= i * mScale[s];
            return i == 0 ? "" : String.valueOf(i);
        }

    }

    private static final class LongScaler implements Scaler<Long> {
        
        private final long[] mScale;
        private long mResult = 0;

        private LongScaler(long[] scale) {
            this.mScale = scale;
        }

        @Override
        public void add(int s) {
            mResult += mScale[s];
        }

        @Override
        public void add(int s, String v) {
            mResult += mScale[s] * Long.parseLong(v);
        }

        @Override
        public void add(String v) {
            mResult += Long.parseLong(v);
        }
        
        @Override
        public void negtive() {
            mResult = -mResult;
        }
        
        @Override
        public Long get() {
            return mResult;
        }
        
        @Override
        public void set(Long v) {
            mResult = v;
        }

        @Override
        public boolean isNegtive() {
            return mResult < 0;
        }
        
        @Override
        public String dec(int s) {
            if (mResult == 0)
                return null;
            long l = mResult / mScale[s];
            mResult -= l * mScale[s];
            return l == 0L ? "" : String.valueOf(l);
        }

    }

    private static final class FloatScaler implements Scaler<Float> {
        
        private final float[] mScale;
        private float mResult = 0;

        private FloatScaler(float[] scale) {
            this.mScale = scale;
        }

        @Override
        public void add(int s) {
            mResult += mScale[s];
        }

        @Override
        public void add(int s, String v) {
            mResult += mScale[s] * Float.parseFloat(v);
        }

        @Override
        public void add(String v) {
            mResult += Float.parseFloat(v);
        }
        
        @Override
        public void negtive() {
            mResult = -mResult;
        }
        
        @Override
        public Float get() {
            return mResult;
        }
        
        @Override
        public void set(Float v) {
            mResult = v;
        }
       
        @Override
        public boolean isNegtive() {
            return mResult < 0;
        }
        
        @Override
        public String dec(int s) {
            if (mResult == 0)
                return null;
            if (mResult < mScale[s] && s < mScale.length - 1)
                return "";
            float f = mResult / mScale[s];
            mResult = 0;
            return String.valueOf(f);
        }

    }

}
