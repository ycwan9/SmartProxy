package me.smartproxy.tunnel.shadowsocks;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Random;

import me.smartproxy.crypto.CryptoUtils;
import me.smartproxy.tunnel.IEncryptor;

public class Aes256cfbEncryptor implements IEncryptor {

    private static final int KEY_SIZE = 32;

    private static final int IV_SIZE = 16;

    private byte[] key, iv_send, iv_recv;

    private static final String TAG = "Aes256cfbEncryptor";

    public Aes256cfbEncryptor(String password) {
        Log.e(TAG, "new encryptor created" + this);
        byte[][] keyAndIV = CryptoUtils.EVP_BytesToKey(KEY_SIZE, IV_SIZE, password.getBytes(), 1);
        if (keyAndIV == null) {
            throw new IllegalArgumentException("no md5 digest support");
        }
        key = keyAndIV[0];
        CryptoUtils.initEncryptor(password, "aes-256-cfb");
//        iv_send = iv_recv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    private byte[] randBytes() {
        byte[] newIv = new byte[IV_SIZE];
        new Random().nextBytes(newIv);
        return newIv;
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte[] encryptionBuff = new byte[buffer.remaining()];
            buffer.get(encryptionBuff, buffer.position(), buffer.remaining());
            boolean sendIV = false;
            if (iv_send == null) {
                sendIV = true;
                iv_send = randBytes();
            }
            byte[] cipher = CryptoUtils.encrypt(encryptionBuff, iv_send);
            if (sendIV) {
                Log.e(TAG, "iv sent" + this);
                byte[] result = new byte[IV_SIZE + cipher.length];
                System.arraycopy(iv_send, 0, result, 0, IV_SIZE);
                System.arraycopy(cipher, 0, result, IV_SIZE, cipher.length);
                cipher = result;
            }
            return ByteBuffer.wrap(cipher);
        }
        return buffer;
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte[] decryptBuff = new byte[buffer.remaining()];
            buffer.get(decryptBuff, buffer.position(), buffer.remaining());
            if (iv_recv == null) {
                if (decryptBuff.length < IV_SIZE) {
                    return buffer;
                }
                iv_recv = new byte[IV_SIZE];
                byte[] realData = new byte[decryptBuff.length - IV_SIZE];
                System.arraycopy(decryptBuff, 0, iv_recv, 0, IV_SIZE);
                System.arraycopy(decryptBuff, IV_SIZE, realData, 0,
                        decryptBuff.length - IV_SIZE);
                decryptBuff = realData;
            }
            byte[] result = CryptoUtils.decrypt(decryptBuff, iv_recv);
            if (result != null) {
                return ByteBuffer.wrap(result);
            }
        }
        return buffer;
    }
}
