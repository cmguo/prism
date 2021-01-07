package com.xhb.prism.util.algorithm;

import com.xhb.prism.util.log.Log;
import com.xhb.prism.util.stream.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Created by alinapei on 2017/10/12.
 */

public class MD5 {

    private static String TAG = "MD5";

    private static String byteToHex(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 0x80 : 0);
        return (value < 0x10 ? "0" : "") + Integer.toHexString(value).toLowerCase();
    }

    /**
     * 校验32位的md5值
     * @param url 需要校验的url
     * @return
     */
    public static String MD5URL_32(String url) {                        // 播放素材文件名校验
        String md5Url = null;
        if (url == null) {
            return null;
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");       // 创建数据摘要算法实例

            byte[] urlBytes = url.getBytes("utf-8");                    // 把字符串转换为字节数组，并且计算数据摘要
            md5.update(urlBytes);
            byte[] digest = md5.digest(urlBytes);

            StringBuffer strbuf = new StringBuffer();                   // 数据摘要转换为字符串
            for (int i = 0; i < digest.length; i++) {
                strbuf.append(byteToHex(digest[i]));
            }
            md5Url = strbuf.toString();

        } catch (Exception e) {
            throw new RuntimeException("无法把字符串转换为MD5数据摘要");
        }
        return md5Url;
    }

    /**
     * MD5_16取了MD5_32的中间16位
     * @param url
     * @return
     */
    public static String MD5URL_16(String url) {
        return MD5URL_32(url).subSequence(8, 24).toString();
    }


    /**
     * 对文件进行校验（32位）
     * @param file
     * @return
     */
    public static String MD5File_32(File file) {                                   // 缓存文件校验
        int bufferSize = 10 * 1024;
        FileInputStream in = null;
        DigestInputStream digestInputStream = null;
        try {
            if (file == null) {
                return null;
            }

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");         // 创建数据摘要算法实例
            in = new FileInputStream(file);
            digestInputStream = new DigestInputStream(in, messageDigest);
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) {                            // 文件读取过程中进行MD5校验

            }
            messageDigest = digestInputStream.getMessageDigest();
            StringBuffer strbuf = new StringBuffer();
            byte[] digest = messageDigest.digest();

            for (int i = 0; i < digest.length; i++) {
                strbuf.append(byteToHex(digest[i]));
            }
            return strbuf.toString();

        } catch (Exception e) {
            Log.d(TAG, "文件校验失败!");
        } finally {
            Streams.closeQuietly(in);
            Streams.closeQuietly(digestInputStream);
        }
        return null;
    }

    /**
     * 对文件进行校验（16位）
     * @param file
     * @return
     */
    public static String MD5File_16(File file) {
        return MD5File_32(file).subSequence(8, 24).toString();
    }
}