package me.smartproxy.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CryptoUtils {

    public static native void initEncryptor(String password, String method, long id);

    public static native byte[] encrypt(byte[] array, long id);

    public static native byte[] decrypt(byte[] array, long id);

    public static native byte[] encryptAll(byte[] array, String password, String method);

    public static native byte[] decryptAll(byte[] array, String password, String method);

    public static native void releaseEncryptor(long id);

    static {
        System.loadLibrary("encryptor");
    }
}
