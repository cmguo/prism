package com.xhb.prism.util.stream;

import com.xhb.prism.util.data.ArraysEx;
import com.xhb.prism.util.data.Collections;
import com.xhb.prism.util.log.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Streams {

    private static final String TAG = "Streams";

    public static String toString(File file) {
        return toString(file, "UTF-8");
    }
    
    public static String toString(File file, String charset) {
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                return toString(fis, charset);
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "toString", e);
            return null;
        }
    }

    public static String toString(InputStream is) {
        return toString(is, "UTF-8");
    }
    
    public static String toString(InputStream is, String charset) {
        try {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(is, charset);
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        } catch (Exception e) {
            Log.w(TAG, "toString", e);
            return null;
        }
    }

    public static void transfer(InputStream in, OutputStream out) {
        try {
            final int bufferSize = 1024;
            final byte[] buffer = new byte[bufferSize];
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.write(buffer, 0, rsz);
            }
        } catch (Exception e) {
            Log.w(TAG, "transfer", e);
        }
    }

    public static void transfer(File file, OutputStream out) {
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                transfer(fis, out);
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "transfer", e);
        }
    }
    
    public static void transfer(InputStream in, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                transfer(in, fos);
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "transfer", e);
        }
    }
    
    public static String[] toLines(InputStream in) {
        return toLines(in, "UTF-8", (Collections.Filter<String>) null);
    }
    
    public static String[] toLines(InputStream in, Collections.Filter<String> filter) {
        return toLines(in, "UTF-8", filter);
    }
    
    public static String[] toLines(InputStream in, LineFilter filter) {
        return toLines(in, "UTF-8", filter);
    }
    
    public interface LineFilter {
        String invoke(String line);
    }
    
    public static String[] toLines(InputStream in, String charset, 
            final Collections.Filter<String> filter) {
        return toLines(in, charset, filter == null ? null : new LineFilter() {
            @Override
            public String invoke(String line) {
                return filter.invoke(line) ? line : null;
            }
        });
    }
    
    public static String[] toLines(InputStream in, String charset, 
            LineFilter filter) {
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        try {
            reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                if (filter != null)
                    line = filter.invoke(line);
                if (line != null)
                    lines.add(line);
            }
        } catch (Exception e) {
            Log.w(TAG, "toLines", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        return ArraysEx.fromList(lines, String.class);
    }
    
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

}
