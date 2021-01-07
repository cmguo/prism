package com.xhb.prism.util.algorithm;

import com.xhb.prism.util.log.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {

    private static final String TAG = "Digest";

    public static byte[] get(String algorithm, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(content.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "get", e);
            return null;
        }
    }

    public static byte[] get(String algorithm, byte[] content1, String content2) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(content1);
            md.update(content2.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "get", e);
            return null;
        }
    }

}
