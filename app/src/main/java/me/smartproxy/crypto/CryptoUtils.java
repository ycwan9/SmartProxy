package me.smartproxy.crypto;

import java.nio.ByteBuffer;

public class CryptoUtils {

    public static native void initEncryptor(String password, String method, long id);

    public static native int encrypt(ByteBuffer array, int size, long id);

    public static native int decrypt(ByteBuffer array, int size, long id);

    public static native byte[] encryptAll(byte[] array, String password, String method);

    public static native byte[] decryptAll(byte[] array, String password, String method);

    public static native void releaseEncryptor(long id);

    static {
        System.loadLibrary("encryptor");
    }
}
