package me.smartproxy.tunnel.shadowsocks;

import java.nio.ByteBuffer;
import java.util.Random;

import me.smartproxy.crypto.CryptoUtils;
import me.smartproxy.tunnel.IEncryptor;

public class ShadowsocksEncryptor implements IEncryptor {
    private static final String TAG = "ShadowsocksEncryptor";

    private long id;

    public ShadowsocksEncryptor(String password, String method) {
        id = new Random(System.currentTimeMillis()).nextLong();
        CryptoUtils.initEncryptor(password, method, id);
    }

    @Override
    public void encrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            int bufferSize = buffer.remaining();
            byte[] encryptionBuff = new byte[bufferSize];
            buffer.get(encryptionBuff, buffer.position(), buffer.remaining());
            byte[] cipher = CryptoUtils.encrypt(encryptionBuff, id);
            buffer.clear();
            buffer.put(cipher);
            buffer.flip();
        }
    }

    @Override
    public void decrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte[] decryptBuff = new byte[buffer.remaining()];
            buffer.get(decryptBuff, buffer.position(), buffer.remaining());
            byte[] result = CryptoUtils.decrypt(decryptBuff, id);
            buffer.clear();
            buffer.put(result);
            buffer.flip();
        }
    }

    @Override
    public void dispose() {
        CryptoUtils.releaseEncryptor(id);
    }
}
