package com.xhb.prism.util.system;


import com.xhb.prism.util.log.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class Files {

    private static final String TAG = "Files";

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.getFD().sync();
        outStream.close();
        dst.setLastModified(src.lastModified());
    }
    
    public static boolean rmdirs(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null)
                return dir.delete();
            for (File f : files) {
                if (f.isDirectory()) {
                    rmdirs(f);
                } else {
                    f.delete();
                }
            }
        }
        return dir.delete();
    }
    
    /*
     * dir: root directory, on Android, it's always absolute
     */
    public static List<File> walk(File dir, List<String> pattens) {
        if (!dir.isDirectory()) {
            if (dir.isFile())
                return Arrays.asList(new File[] { dir });
            return null;
        }
        Log.d(TAG, "walk " + dir + " with pattens " + pattens);
        List<File> dirs = new ArrayList<File>();
        List<File> files = new ArrayList<File>();
        Pattern[] patterns1 = new Pattern[pattens.size()];
        for (int i = 0; i < patterns1.length; ++i) {
            try {
                patterns1[i] = Pattern.compile(pattens.get(i));
            } catch (Exception e) {
                Log.w(TAG, "walk", e);
                patterns1[i] = null;
            }
        }
        dirs.add(dir);
        while (!dirs.isEmpty()) {
            File d = dirs.remove(0);
            Log.d(TAG, "walk search " + d);
            File[] list = d.listFiles();
            if (list == null)
                continue;
            for (File f : list) {
                if (f.getName().startsWith(".")) // for example .git
                    continue;
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    for (Pattern p : patterns1) {
                        if (p != null && p.matcher(f.getName()).matches()) {
                            Log.d(TAG, "walk match " + p.toString() + " -> " + f);
                            files.add(f);
                            break;
                        }
                    }
                }
            }
        }
        return files;
    }

    public static List<File> walk(File dir, String pattern) {
        return walk(dir, Arrays.asList(new String[] { pattern }));
    }
    
    public static List<File> walk(File dir) {
        return walk(dir, ".*");
    }

    public static void clean(File dir, FileFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files != null) {
            for (File f : files) {
                Log.d(TAG, "clean " + f);
                rmdirs(f);
            }
        }
    }
    
    public static void clean(File dir, final FilenameFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files != null) {
            for (File f : files) {
                Log.d(TAG, "clean " + f);
                rmdirs(f);
            }
        }
    }
    
    public static void cleanOthers(File dir, String... keep) {
        cleanOthers(dir, Arrays.asList(keep));
    }
    
    public static void cleanOthers(File dir, final List<String> keep) {
        clean(dir, new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                return !keep.contains(arg1);
            }
        });
    }
    
    public static void rotateFiles(File file, int count) {
        rotateFiles(file.getParentFile(), file.getName(), count);
    }
    
    public static void rotateFiles(File path, String name, int count) {
        File src = null;
        File dst = new File(path, name + "." + count);
        for (int i = count; i > 0; --i) {
            if (i == 1)
                src = new File(path, name);
            else
                src = new File(path, name + "." + (i - 1));
            src.renameTo(dst);
            dst = src;
        }
    }

}
