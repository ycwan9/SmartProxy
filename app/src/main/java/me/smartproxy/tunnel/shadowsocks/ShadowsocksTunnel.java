package me.smartproxy.tunnel.shadowsocks;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import me.smartproxy.tunnel.IEncryptor;
import me.smartproxy.tunnel.Tunnel;

public class ShadowsocksTunnel extends Tunnel {

    private IEncryptor m_Encryptor;

    private ShadowsocksConfig m_Config;

    private boolean m_TunnelEstablished;

    public ShadowsocksTunnel(ShadowsocksConfig config, Selector selector) throws Exception {
        super(config.ServerAddress, selector);
        if (config.Encryptor == null) {
            throw new Exception("Error: The Encryptor for ShadowsocksTunnel is null.");
        }
        m_Config = config;
        m_Encryptor = config.Encryptor;
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        m_Encryptor = EncryptorFactory.createEncryptorByConfig(m_Config);

        //构造socks5请求（跳过前3个字节）
        buffer.clear();
        buffer.put((byte) 0x03);//domain
        byte[] domainBytes = m_DestAddress.getHostName().getBytes();
        buffer.put((byte) domainBytes.length);//domain length;
        buffer.put(domainBytes);
        buffer.putShort((short) m_DestAddress.getPort());
        buffer.flip();

        buffer = m_Encryptor.encrypt(buffer);
        if (write(buffer, true)) {
            m_TunnelEstablished = true;
            onTunnelEstablished();
        } else {
            m_TunnelEstablished = true;
            this.beginReceive();
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return m_TunnelEstablished;
    }

    @Override
    protected ByteBuffer beforeSend(ByteBuffer buffer) throws Exception {
        int start = buffer.arrayOffset() + buffer.position();
        int end = buffer.limit();
        int size = end - start;
        byte[] data = new byte[size];
        buffer.get(data, start, size);
        String dataString = new String(data);
        String afterDataString;
        Log.e("llllll", "before :" + dataString);

        buffer = m_Encryptor.encrypt(buffer);

        byte[] data2 = new byte[size];
        buffer.get(data2, start, size);
        afterDataString = new String(data2);
        Log.e("llllll", afterDataString);
        if (dataString.equalsIgnoreCase(afterDataString)) {
            Log.e("llllll", "what the fuck");
        }

        Log.e("llllll", "after :" + dataString);
        return buffer;
    }

    @Override
    protected ByteBuffer afterReceived(ByteBuffer buffer) throws Exception {
        return m_Encryptor.decrypt(buffer);
    }

    @Override
    protected void onDispose() {
        m_Config = null;
        m_Encryptor = null;
    }

}
