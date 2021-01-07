package com.xhb.prism.util.log;

public class Log extends DebugLog {

    public static final int ASSERT = 1;
    public static final int ERROR = 2;
    public static final int WARN = 3;
    public static final int INFO = 4;
    public static final int DEBUG = 5;
    public static final int VERBOSE = 6;

    public static String getStackTraceString(Throwable tr) {
        return null;
    }
}
