package me.smartproxy.tunnel;

import java.nio.ByteBuffer;

public interface IEncryptor {

    ByteBuffer encrypt(ByteBuffer buffer);

    ByteBuffer decrypt(ByteBuffer buffer);
}
