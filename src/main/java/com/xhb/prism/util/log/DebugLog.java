package com.xhb.prism.util.log;

import com.xhb.prism.prop.PropConfigurableKey;
import com.xhb.prism.prop.PropValue;
import com.xhb.prism.prop.PropertySet;
import com.xhb.prism.util.system.Os;
import com.xhb.prism.util.system.SystemProperties;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DebugLog {
    
    public interface ILogger {
        void log(long time, int tid, int priority, String tag, String msg);
    }

    private static LogFile sLogFile = new LogFile();
    static String sHead;
    
    private static int sGlobalPriority = Log.DEBUG;
    private static boolean sDumpObject = true;
    private static int sDumpDetail = 3;
    private static boolean sShowToast = 
            SystemProperties.getBoolean("persist.wplayer.show_toast", false);
    private static Map<String, Integer> sTagPriorityMap = new TreeMap<String, Integer>();
    private static Map<String, Integer> sJavaLogs = new TreeMap<String, Integer>();
    private static Map<String, String[]> sTagTraceMap = new TreeMap<String, String[]>();
    private static List<ILogger> sLoggers = new CopyOnWriteArrayList<ILogger>();
    
    public static final Config sConfig = new Config();

    public static void setLogFileParams(File file, String head, long limit, int count) {
        sHead = head;
        sLogFile.setHead(head);
        sLogFile.setFile(file);
        sLogFile.setLimit(limit);
        sLogFile.setCount(count);
    }
    
    public static void addLogger(ILogger l) {
        synchronized (sLoggers) {
            if (!sLoggers.contains(l))
                sLoggers.add(l);
        }
    }
    
    public static void removeLogger(ILogger l) {
        synchronized (sLoggers) {
            sLoggers.remove(l);
        }
    }

    public static void addJavaLog(String name) {
        setJavaLog(name, null);
    }

    public static void setJavaLog(String name, Integer level) {
        synchronized (sJavaLogs) {
            if (sJavaLogs.isEmpty())
                AndroidLoggingHandler.reset(new AndroidLoggingHandler());
            Integer o = sJavaLogs.get(name);
            if (o == null) {
                if (level == null)
                    level = Log.DEBUG;
            }
            if (!level.equals(o)) {
                Logger.getLogger(name).setLevel(sJavaLevels[level]);
                sJavaLogs.put(name, level);
            }
        }
    }
    
    /*
     * assert
     */

    public static final void wtf(String tag, String msg) {
        wtf(tag, msg, null, null);
    }

    public static final void wtf(String tag, String msg, Throwable tr) {
        wtf(tag, msg, null, tr);
    }

    public static final void wtf(String tag, String msg, Object obj) {
        wtf(tag, msg, obj, null);
    }

    public static final void wtf(String tag, String msg, Object obj, Throwable tr) {
        println(Log.ASSERT, tag, msg, obj, tr);
    }

    /*
     * error
     */

    public static final void e(String tag, String msg) {
        e(tag, msg, null, null);
    }

    public static final void e(String tag, String msg, Throwable tr) {
        e(tag, msg, null, tr);
    }

    public static final void e(String tag, String msg, Object obj) {
        e(tag, msg, obj, null);
    }

    public static final void e(String tag, String msg, Object obj, Throwable tr) {
        println(Log.ERROR, tag, msg, obj, tr);
    }

    /*
     * waring
     */

    public static final void w(String tag, String msg) {
        w(tag, msg, null, null);
    }

    public static final void w(String tag, String msg, Throwable tr) {
        w(tag, msg, null, tr);
    }

    public static final void w(String tag, String msg, Object obj) {
        w(tag, msg, obj, null);
    }

    public static final void w(String tag, String msg, Object obj, Throwable tr) {
        println(Log.WARN, tag, msg, obj, tr);
    }

    /*
     * info
     */

    public static final void i(String tag, String msg) {
        i(tag, msg, null, null);
    }

    public static final void i(String tag, String msg, Throwable tr) {
        i(tag, msg, null, tr);
    }

    public static final void i(String tag, String msg, Object obj) {
        i(tag, msg, obj, null);
    }

    public static final void i(String tag, String msg, Object obj, Throwable tr) {
        println(Log.INFO, tag, msg, obj, tr);
    }

    /*
     * debug
     */

    public static final void d(String tag, String msg) {
        d(tag, msg, null, null);
    }

    public static final void d(String tag, String msg, Throwable tr) {
        d(tag, msg, null, tr);
    }

    public static final void d(String tag, String msg, Object obj) {
        d(tag, msg, obj, null);
    }

    public static final void d(String tag, String msg, Object obj, Throwable tr) {
        println(Log.DEBUG, tag, msg, obj, tr);
    }

    /*
     * verbose
     */

    public static final void v(String tag, String msg) {
        v(tag, msg, null, null);
    }

    public static final void v(String tag, String msg, Throwable tr) {
        v(tag, msg, null, tr);
    }

    public static final void v(String tag, String msg, Object obj) {
        v(tag, msg, obj, null);
    }

    public static final void v(String tag, String msg, Object obj, Throwable tr) {
        println(Log.VERBOSE, tag, msg, obj, tr);
    }

    public static final void println(int priority, final String tag, final String msg, Object obj, Throwable tr) {
        Integer prio = sTagPriorityMap.get(tag);
        if (prio == null)
            prio = sGlobalPriority;
        String[] traces = sTagTraceMap.get(tag);
        if (traces != null) {
            for (String t : traces) {
                if (msg.contains(t)) {
                    tr = new Throwable();
                    break;
                }
            }
        }
        String msg2 = tr == null ? msg 
                : msg + " [" + tr + "]\n" + Log.getStackTraceString(tr);
        if (prio <= priority) {
            sLogFile.log(priority, tag, msg2);
            //println(priority, tag, msg2);
            if (!sLoggers.isEmpty()) {
                synchronized (sLoggers) {
                    long time = System.currentTimeMillis();
                    int tid = Os.gettid();
                    int start = 0;
                    int finish = msg2.indexOf('\n', start);
                    while (finish > 0) {
                        String msg3 = msg2.substring(start, finish++);
                        for (ILogger l : sLoggers) {
                            l.log(time, tid, priority, tag, msg3);
                        }
                        start = finish;
                        finish = msg2.indexOf('\n', start);
                    }
                    if (start < msg2.length()) {
                        String msg3 = msg2.substring(start);
                        for (ILogger l : sLoggers) {
                            l.log(time, tid, priority, tag, msg3);
                        }
                    }
                }
            }
        }
        if (obj != null && sDumpObject) {

        }
    }
    
    public static class Config extends PropertySet {
        
        public static final PropConfigurableKey<Integer> PROP_GLOBAL_PRIORITY =
                new PropConfigurableKey<Integer>("全局等级") {
            public Integer valueFromString(String value) {
                return priorityFromString(value);
            };
            public String valueToString(Integer value) {
                return sPriorityNames[value];
            };
        };
        
        public static final PropConfigurableKey<Boolean> PROP_DUMP_OBJECT = 
                new PropConfigurableKey<Boolean>("输出上下文");
        
        public static final PropConfigurableKey<Integer> PROP_DUMP_DETAIL = 
                new PropConfigurableKey<Integer>("输出上下文细节程度");
        
        public static final PropConfigurableKey<Boolean> PROP_SHOW_TOAST = 
                new PropConfigurableKey<Boolean>("弹出Toast");
        
        public static final PropConfigurableKey<String[]> PROP_JAVA_LOGS = 
                new PropConfigurableKey<String[]>("JAVA日志");

        public Config() {
            setProp(PROP_GLOBAL_PRIORITY, sGlobalPriority);
            setProp(PROP_DUMP_OBJECT, sDumpObject);
            setProp(PROP_SHOW_TOAST, sShowToast);
            setProp(PROP_SHOW_TOAST, sShowToast);
        }
        
        @Override
        public <E> void setProp(PropConfigurableKey<E> key, E value) {
            super.setProp(key, value);
            if (key == PROP_GLOBAL_PRIORITY) {
                sGlobalPriority = (Integer) value;
            } else if (key == PROP_DUMP_OBJECT) {
                sDumpObject = (Boolean) value;
            } else if (key == PROP_DUMP_DETAIL) {
                sDumpDetail = (Integer) value;
            } else if (key == PROP_SHOW_TOAST) {
                sShowToast = (Boolean) value;
            } else if (key == PROP_JAVA_LOGS) {
                String[] logs = (String[]) value;
                for (String l : logs) {
                    Integer p = null;
                    int n = l.indexOf(':');
                    if (n > 0) {
                        p = priorityFromString(l.substring(n + 1));
                        l = l.substring(0, n);
                    }
                    setJavaLog(l, p);
                }
            }
        }

        @Override
        public void setProp(String key, String value) {
            if (hasKey(key)) {
                super.setProp(key, value);
                return;
            }
            if (key.startsWith("priority.")) {
                sTagPriorityMap.put(key.substring(9), 
                        priorityFromString(value));
            } else if (key.startsWith("trace.")) {
                sTagTraceMap.put(key.substring(6), 
                        PropValue.fromString(new String[0].getClass(), value));
            }
        }
        
        @Override
        public void assign(PropertySet other) {
            super.assign(other);
            sGlobalPriority = getProp(PROP_GLOBAL_PRIORITY, Log.DEBUG);
            sDumpObject = getProp(PROP_DUMP_OBJECT, false);
            sDumpDetail = getProp(PROP_DUMP_DETAIL, 3);
            sShowToast = getProp(PROP_SHOW_TOAST, false);
        }
        
        @Override
        public String getProp(String key) {
            if (hasKey(key))
                return super.getProp(key);
            if (key.startsWith("priority.")) {
                String tag = key.substring(9);
                Integer pri = sTagPriorityMap.get(tag);
                if (pri == null)
                    return null;
                return sPriorityNames[pri];
            } else if (key.startsWith("trace.")) {
                String tag = key.substring(6);
                String[] trace = sTagTraceMap.get(tag);
                if (trace == null)
                    return null;
                return PropValue.toString(trace);
            } else {
                return null;
            }
        }
    }

    private static final String[] sPriorityNames = new String[] {
        "suppress", "suppress", 
        "verbose", "debug", "info", "warn", "error", "assert"
    };

    private static final String TAG = "DebugLog";

    private static final Level[] sJavaLevels = new Level[] {
        Level.ALL, Level.ALL, 
        Level.FINEST, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF
    };

    private static int priorityFromString(String str) {
        if (str != null)
            str = str.toLowerCase();
        if (str.length() == 1)
            return priorityFromChar(str.charAt(0));
        int priority = Arrays.asList(sPriorityNames).indexOf(str);
        if (priority >= 0)
            return priority;
        else
            return Log.VERBOSE - 1;
    }

    private static int priorityFromChar(char c) {
        switch (c) {
        case 's':
            return Log.VERBOSE - 1;
        case 'v':
            return Log.VERBOSE;
        case 'd':
            return Log.DEBUG;
        case 'i':
            return Log.INFO;
        case 'w':
            return Log.WARN;
        case 'e':
            return Log.ERROR;
        case 'f':
            return Log.ASSERT;
        default:
            return Log.VERBOSE - 1;
        }
    }

    private static class AndroidLoggingHandler extends Handler {

        public static void reset(Handler rootHandler) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            rootLogger.addHandler(rootHandler);
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void publish(LogRecord record) {
            int level = getAndroidLevel(record.getLevel());
            String tag = loggerNameToTag(record.getLoggerName());
            try {
                println(level, tag, record.getMessage(), null, record.getThrown());
            } catch (RuntimeException e) {
                w(TAG, "AndroidLoggingHandler", e);
            }
        }

        public static final int SEVERE = Level.SEVERE.intValue();
        public static final int WARNING = Level.WARNING.intValue();
        public static final int INFO = Level.INFO.intValue();
        public static final int FINE = Level.FINE.intValue();
        
        static int getAndroidLevel(Level level) {
            int value = level.intValue();
            if (value >= SEVERE) {
                return Log.ERROR;
            } else if (value >= WARNING) {
                return Log.WARN;
            } else if (value >= INFO) {
                return Log.INFO;
            } else if (value >= FINE) {
                return Log.DEBUG;
            } else {
                return Log.VERBOSE;
            }
        }

        public static String loggerNameToTag(String loggerName) {
            // Anonymous logger.
            if (loggerName == null) {
                return "null";
            }

            int length = loggerName.length();
            if (length <= 23) {
                return loggerName;
            }

            int lastPeriod = loggerName.lastIndexOf(".");
            return length - (lastPeriod + 1) <= 23
                    ? loggerName.substring(lastPeriod + 1)
                    : loggerName.substring(loggerName.length() - 23);
        }
    
    }
    
}
