package me.smartproxy.crypto;

public class CryptoUtils {

    public static native void exec(String cmd);

    public static native String getABI();

    public static native String testEncrypt();

    public static native void initEncryptor(String password, String method);

    public static native byte[] encrypt(byte[] array);

    public static native byte[] decrypt(byte[] array);

    public static native byte[] encryptAll(byte[] array, String password, String method);

    public static native byte[] decryptAll(byte[] array, String password, String method);

    static {
        System.loadLibrary("encryptor");
    }
}
