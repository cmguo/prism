package com.xhb.prism.util.log;

import java.io.PrintWriter;
import java.io.Writer;

public class LogcatPrintWriter extends PrintWriter {

    private static class LogcatWriter extends Writer {
        
        private String TAG;
        private int PRIO;
        
        private StringBuilder sb = new StringBuilder();
        
        public LogcatWriter() {
        }

        public LogcatWriter(String tag, int priority) {
            TAG = tag;
            PRIO = priority;
        }

        public void setTagPriority(String tag, int priority) {
            TAG = tag;
            PRIO = priority;
        }

        @Override
        public void close() {
        }
        
        @Override
        public void flush() {
            Log.println(PRIO, TAG, sb.toString(), null, null);
            sb.setLength(0);
        }
        
        @Override
        public void write(char[] buf, int offset, int count) {
            sb.append(buf, offset, count);
        }

    }
    
    public LogcatPrintWriter(String tag, int priority) {
        super(new LogcatWriter(tag, priority), true);
    }
    
    public LogcatPrintWriter(String tag) {
        super(new LogcatWriter(tag, Log.DEBUG), true);
    }
    
    public LogcatPrintWriter() {
        super(new LogcatWriter(), true);
    }
    
    public void setTagPriority(String tag, int priority) {
        ((LogcatWriter) out).setTagPriority(tag, priority);
    }
    
    @Override
    public void println() {
        // eat line break
        super.flush();
    }

}

