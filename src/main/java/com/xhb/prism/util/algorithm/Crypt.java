package com.xhb.prism.util.algorithm;

import com.xhb.prism.util.log.Log;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypt {

    // openssl enc -aes-128-cbc -kfile key -md md5 -nosalt
    
    private static final String TAG = "Crypt";

    public static String encrypt(String algorithm, String key2, String value) {
        try {
            Key key = generateKey(algorithm, key2);
            Cipher cipher = Cipher.getInstance(algorithm);
            AlgorithmParameterSpec iv = generateIV(key2);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            //Log.d(TAG, "Provider: " + cipher.getProvider());
            //Log.d(TAG, "Algorithm: " + cipher.getAlgorithm());
            //Log.d(TAG, "BlockSize:" + cipher.getBlockSize());
            //Log.d(TAG, "OutputSize: " + cipher.getOutputSize(value.length()));
            //Log.d(TAG, "Key: " + Arrays.toString(key.getEncoded()));
            //Log.d(TAG, "IV: " + Arrays.toString(((IvParameterSpec) iv).getIV()));
            byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
            String encryptedValue64 = "";//Base64.encodeToString(encryptedByteValue,
                    //Base64.NO_WRAP);
            return encryptedValue64;
        } catch (Exception e) {
            Log.w(TAG, "encrypt", e);
            return null;
        }
    }

    public static String decrypt(String algorithm, String key2, String value) {
        try {
            Key key = generateKey(algorithm, key2);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, generateIV(key2));
            byte[] decryptedValue64 = null;//Base64.decode(value, Base64.NO_WRAP);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            String decryptedValue = new String(decryptedByteValue, "utf-8");
            return decryptedValue;
        } catch (Exception e) {
            Log.w(TAG, "decrypt", e);
            return null;
        }
    }

    private static Key generateKey(String algorithm, String key) {
        return new SecretKeySpec(Digest.get("MD5", key), algorithm);
    }

    private static AlgorithmParameterSpec generateIV(String key) {
        byte[] md5 = Digest.get("MD5", key);
        md5 = Digest.get("MD5", md5, key);
        return new IvParameterSpec(md5);
    }

}
