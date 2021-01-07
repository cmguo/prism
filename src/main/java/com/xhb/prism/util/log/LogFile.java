package com.xhb.prism.util.log;

import com.xhb.prism.util.system.Files;
import com.xhb.prism.util.system.Os;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.text.SimpleDateFormat;

public class LogFile {
    
    private static final String TAG = "LogFile";
    
    private static final char[] sPriorityChar = new char[Log.VERBOSE + 1];

    static {
        sPriorityChar[Log.VERBOSE] = 'V';
        sPriorityChar[Log.DEBUG] = 'D';
        sPriorityChar[Log.INFO] = 'I';
        sPriorityChar[Log.WARN] = 'W';
        sPriorityChar[Log.ERROR] = 'E';
        sPriorityChar[Log.ASSERT] = 'A';
    }
    
    private File mFile = new File("/dev/null");
    private long mLimit = 0;
    private int mCount = 0;
    private String mHead;
    private int mPriority = Log.DEBUG;
    
    private RandomAccessFile mOut = null;
    private ThreadLocal<MessageFormattor> mFormattor; // 用于组合待写入内容
    
    LogFile() {
        mFormattor = new ThreadLocal<MessageFormattor>() {
            @Override
            protected MessageFormattor initialValue() {
                return new MessageFormattor();
            }
        };
        try {
            mOut = new RandomAccessFile(mFile, "rw");
        } catch (Exception e) {
            Log.e(TAG, "<init>", e);
        }
    }

    synchronized void setFile(File file) {
        if (file.equals(mOut))
            return;
        try {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.seek(f.length());
            mFormattor.get().writeTo(f, Log.INFO, TAG, mHead);
            mOut = f;
            mFile = file;
        } catch (IOException e) {
            Log.e(TAG, "setFile", e);
        }
    }
    
    synchronized void setLimit(long limit) {
        mLimit = limit;
    }

    synchronized void setCount(int count) {
        mCount = count;
    }

    synchronized void setHead(String head) {
        mHead = head;
    }

    synchronized void setPriority(int prio) {
        mPriority = prio;
    }

    public void log(int priority, String tag, String msg) {
        if (priority > mPriority)
            return;
        try {
            if (mLimit > 0 && mOut.getFilePointer() > mLimit)
                rotate();
            mFormattor.get().writeTo(mOut, priority, tag, msg);
        } catch (Throwable e) {
            Log.e(TAG, "log", e);
        }
    }

    public synchronized void rotate() {
        try {
            long pos = mOut.getFilePointer();
            if (pos < mLimit)
                return;
            Log.d(TAG, "rotate at " + pos);
            RandomAccessFile f = mOut;
            if (mCount == 0) {
                pos = 0;
                f.seek(0);
            } else {
                Files.rotateFiles(mFile, mCount);
                f = new RandomAccessFile(mFile, "rw");
            }
            mFormattor.get().writeTo(f, Log.INFO, TAG, mHead);
            RandomAccessFile f1 = mOut;
            mOut = f;
            if (f1 != f)
                f1.close();
        } catch (Throwable e) {
            Log.e(TAG, "rotate", e);
        }
    }

    /**
     * 按规定格式拼接 log日志 格式类似 日期 时间 进程ID 线程ID 日志等级 TAG， 消息
     * @throws IOException 
     */
    
    private static class MessageFormattor {
        
        private SimpleDateFormat mSdf = // 格式化日期时间对象
                new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        private StringBuilder mSb = new StringBuilder();
        private char[] mPtp; // pid, tid, prio
        private ByteBuffer mBytes = ByteBuffer.allocate(1024);
        private CharsetEncoder mEncoder = Charset.forName("UTF-8").newEncoder();
        
        MessageFormattor() {
            String ptp = String.format("% 6d% 6d P ", 
                    Os.getpid(),
                    Os.gettid());
            mPtp = new char[ptp.length()];
            ptp.getChars(0, ptp.length(), mPtp, 0);
        }
        
        public void writeTo(DataOutput out, int priority, String tag, 
                String msg) throws IOException {
            String content = format(System.currentTimeMillis(), priority, tag, msg);
            CharBuffer chars = CharBuffer.wrap(content);
            CoderResult result = mEncoder.encode(chars, mBytes, true);
            while (result == CoderResult.OVERFLOW) {
                ByteBuffer bytes = ByteBuffer.allocate(mBytes.capacity() * 2);
                mBytes.flip();
                bytes.put(mBytes);
                mBytes = bytes;
                result = mEncoder.encode(chars, mBytes, true);
            }
            if (result.isMalformed() || result.isUnmappable()) {
                result.throwException();
            }
            out.write(mBytes.array(), 0, mBytes.position());
            mBytes.position(0);
        }

        protected String format(long time, int priority, 
                String tag, String msg) {
            mSb.delete(0, mSb.length());
            String timeStr = mSdf.format(time);
            mPtp[mPtp.length - 2] = sPriorityChar[priority];
            tag += ": ";
            int start = 0;
            int finish = msg.indexOf('\n', start);
            while (finish > 0) {
                mSb.append(timeStr);
                mSb.append(mPtp);
                mSb.append(tag);
                mSb.append(msg, start, ++finish);
                start = finish;
                finish = msg.indexOf('\n', start);
            }
            if (start < msg.length()) {
                mSb.append(timeStr);
                mSb.append(mPtp);
                mSb.append(tag);
                mSb.append(msg, start, msg.length());
                mSb.append("\n");
            }
            return mSb.toString();
        }
    }
    
    String format(long time, int priority, 
            String tag, String msg) {
        return mFormattor.get().format(time, priority, tag, msg);
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (mOut != null)
            mOut.close();
        super.finalize();
    }

}
