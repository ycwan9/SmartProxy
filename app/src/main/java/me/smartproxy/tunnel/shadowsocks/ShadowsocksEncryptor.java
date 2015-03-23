package me.smartproxy.tunnel.shadowsocks;

import java.nio.ByteBuffer;
import java.util.Random;

import me.smartproxy.crypto.CryptoUtils;
import me.smartproxy.tunnel.IEncryptor;

public class ShadowsocksEncryptor implements IEncryptor {

    private static final String TAG = "ShadowsocksEncryptor";

    private static Random _random;

    private long id;

    private volatile boolean isEncrypting, isDecrypting;

    public ShadowsocksEncryptor(String password, String method) {
        if (_random == null) {
            _random = new Random(System.currentTimeMillis());
        }
        id = _random.nextLong();
        //Log.e("encryptor id","" + id);
        CryptoUtils.initEncryptor(password, method, id);
        isEncrypting = false;
        isDecrypting = false;
    }

    @Override
    public void encrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            isEncrypting = true;
            int bufferSize = buffer.remaining();
            bufferSize = CryptoUtils.encrypt(buffer, bufferSize, id);
            buffer.limit(bufferSize);
            isEncrypting = false;
        }
    }

    @Override
    public void decrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            isDecrypting = true;
            int bufferSize = buffer.remaining();
            bufferSize = CryptoUtils.decrypt(buffer, bufferSize, id);
            buffer.limit(bufferSize);
            isDecrypting = false;
        }
    }

    @Override
    public void dispose() {
//        Log.e(TAG, "disposed id is " + id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isEncrypting || isDecrypting) {
                    try {
//                        Log.e(TAG, "id " + id + " still waiting for dispose");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CryptoUtils.releaseEncryptor(id);
            }
        }).start();
    }

    @Override
    protected void finalize() throws Throwable {
        //make sure memory in jni is released
        dispose();
        super.finalize();
    }
}
