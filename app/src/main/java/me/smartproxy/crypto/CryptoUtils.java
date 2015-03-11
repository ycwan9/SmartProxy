package me.smartproxy.crypto;

public class CryptoUtils {

    public static native void exec(String cmd);

    public static native String getABI();

    static {
        System.loadLibrary("encryptor");
    }
}
