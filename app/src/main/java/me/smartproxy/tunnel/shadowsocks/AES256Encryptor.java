package me.smartproxy.tunnel.shadowsocks;

import android.util.Log;

import java.nio.ByteBuffer;

import me.smartproxy.crypto.CryptoUtils;
import me.smartproxy.tunnel.IEncryptor;

public class AES256Encryptor implements IEncryptor {


    private static final String TAG = "AES256";

    private byte[] iv_send = new byte[16];

    private byte[] iv_recv = new byte[16];

    private boolean iv_send_get = false;

    private boolean iv_recv_get = false;

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            int bufferSize = buffer.remaining();
            byte[] encryptionBuff = new byte[bufferSize];
            buffer.get(encryptionBuff, buffer.position(), buffer.remaining());
            Log.e(TAG, "buff len is " + encryptionBuff.length);
            Log.e(TAG, "buffer want to send\n " + new String(encryptionBuff));
            byte[] cipher = CryptoUtils.encrypt(encryptionBuff, 0);
            if (!iv_send_get) {
                System.arraycopy(cipher, 0, iv_send, 0, 16);
                iv_send_get = true;
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
            byte[] result = CryptoUtils.decrypt(decryptBuff, 0);
            if (result != null) {
                return ByteBuffer.wrap(result);
            }
        }
        return buffer;
    }
}
