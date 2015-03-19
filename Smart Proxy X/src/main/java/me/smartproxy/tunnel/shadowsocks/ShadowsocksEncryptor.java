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
            byte[] encryptionBuff = new byte[bufferSize];
            buffer.get(encryptionBuff, buffer.position(), buffer.remaining());
            byte[] cipher = CryptoUtils.encrypt(encryptionBuff, id);
            buffer.clear();
            buffer.put(cipher);
            buffer.flip();
            isEncrypting = false;
        }
    }

    @Override
    public void decrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            isDecrypting = true;
            byte[] decryptBuff = new byte[buffer.remaining()];
            buffer.get(decryptBuff, buffer.position(), buffer.remaining());
            byte[] result = CryptoUtils.decrypt(decryptBuff, id);
            buffer.clear();
            buffer.put(result);
            buffer.flip();
            isDecrypting = false;
        }
    }

    @Override
    public void dispose() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isEncrypting || isDecrypting) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CryptoUtils.releaseEncryptor(id);
            }
        }).start();
    }
}
