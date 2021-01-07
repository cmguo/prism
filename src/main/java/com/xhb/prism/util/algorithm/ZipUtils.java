package com.xhb.prism.util.algorithm;

import com.xhb.prism.util.log.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipUtils {

    private static final String TAG = "ZipUtils";
    private static final int BUFFER_SIZE = 4096;

    public static boolean zipFiles(File dir, List<File> files, File dest) {
        try {
            FileOutputStream out = new FileOutputStream(dest);
            try {
                return zipFiles(dir, files, out);
            } finally {
                out.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "zipFiles", e);
            return false;
        }
    }

    public static boolean zipFiles(File dir, List<File> files, 
            OutputStream os) {
        try {
            int dirLen = dir == null ? 0 : dir.getAbsolutePath().length() + 1;
            BufferedInputStream origin = null;
            ZipOutputStream out = new ZipOutputStream(os);
            byte data[] = new byte[BUFFER_SIZE];
            for (File f : files) {
                Log.v(TAG, "zipFiles: Adding " + f);
                FileInputStream fi = new FileInputStream(f);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                ZipEntry entry = new ZipEntry(dirLen == 0 ? f.getName() 
                        : f.getPath().substring(dirLen));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "zipFiles", e);
            return false;
        }
    }
    
    public static long getEntryLength(File file, String path) throws IOException {
        ZipFile zip = new ZipFile(file);
        try {
            return zip.getEntry(path).getSize();
        } finally {
            zip.close();
        }
    }
    
    public static InputStream getInputStream(File file, 
            String path) throws IOException {
        final ZipFile zip = new ZipFile(file);
        boolean ok = false;
        try {
            InputStream is = new BufferedInputStream(
                    zip.getInputStream(new ZipEntry(path))) {
                @Override
                public void close() throws IOException {
                    super.close();
                    zip.close();
                }
            };
            ok = true;
            return is;
        } finally {
            if (!ok)
                zip.close();
        }
    }
    
}
