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
        return ByteBuffer.wrap(CryptoUtils.encrypt(buffer.array()));
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer buffer) {
        return ByteBuffer.wrap(CryptoUtils.decrypt(buffer.array()));
    }
}
