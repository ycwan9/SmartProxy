package me.smartproxy.tunnel.shadowsocks;

import java.nio.ByteBuffer;

import me.smartproxy.crypto.CryptoUtils;
import me.smartproxy.tunnel.IEncryptor;

public class Aes256cfbEncryptor implements IEncryptor {

    public Aes256cfbEncryptor(String password) {
        CryptoUtils.initEncryptor(password, "aes-256-cfb");
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        int start = buffer.arrayOffset() + buffer.position();
        int end = buffer.limit();
        int size = end - start;
        if (size <= 0) {
            return buffer;
        }
        byte[] encryptionBuff = new byte[size];
        int j = 0;
        for (int i = start; i < end; i++, j++) {
            encryptionBuff[j] = buffer.array()[i];
        }

        byte[] result = CryptoUtils.encrypt(encryptionBuff);

        j = 0;
        for (int i = start; i < end; i++, j++) {
            buffer.array()[i] = result[j];
        }

        return buffer;
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer buffer) {
        int buffSize = buffer.limit() - buffer.arrayOffset() - buffer.position();
        if (buffSize == 0) {
            return buffer;
        }
        byte[] encryptionBuff = new byte[buffSize];

        int j = 0;
        for (int i = buffer.arrayOffset() + buffer.position(); i < buffer.limit(); i++, j++) {
            encryptionBuff[j] = buffer.array()[i];
        }
        byte[] result = CryptoUtils.decrypt(encryptionBuff);

        j = 0;
        for (int i = buffer.arrayOffset() + buffer.position(); i < buffer.limit(); i++, j++) {
            buffer.array()[i] = result[j];
        }

        return buffer;
    }
}
