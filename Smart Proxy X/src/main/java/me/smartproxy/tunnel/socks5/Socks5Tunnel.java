package me.smartproxy.tunnel.socks5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.Bidi;

import me.smartproxy.core.ProxyConfig;
import me.smartproxy.tunnel.Tunnel;

public class Socks5Tunnel extends Tunnel {

	private boolean m_TunnelEstablished;
	private Socks5Config m_Config;

	public Socks5Tunnel(Socks5Config config,Selector selector) throws IOException {
		super(config.ServerAddress,selector);
		m_Config=config;
	}

	@Override
	protected void onConnected(ByteBuffer buffer) throws Exception {
		try{
			byte[] b = new byte[2];
			String request1 = "\005\001\000";
			String request2 = "\005\001\000\001";
			//\x05 socks v5
			//\x01 nmethods 1
			//\x00 methods 00 (no auth)
			buffer.clear();
			buffer.put(request1.getBytes());
			buffer.flip();
			if (!this.write(buffer,true)){
				throw new Exception("socks5 connect 1st failed");
			}
			buffer.clear();
			m_InnerChannel.configureBlocking(true);
			m_InnerChannel.read(buffer);
			buffer.flip();
			buffer.get(b);
			if (!((b[0]==5)&&(b[1]==0))) {
				throw new Exception(new String.format("failed to auth <\\x%x\\x%x>",b[0],b[1]));
			}
			buffer.clear();
			buffer.put(request2.getBytes());
			buffer.put(m_DestAddress.getAddress().getAddress());
			buffer.put((byte)(0xff00 & m_DestAddress.getPort()));//send 
			buffer.put((byte)(0x00ff & m_DestAddress.getPort()));//htons(port)
			buffer.flip();
			if(!this.write(buffer,true)){
				throw new Exception("socks5 CMD connect failed to send");
			}
			buffer.clear();
			m_InnerChannel.configureBlocking(true);
			m_InnerChannel.read(buffer);
			buffer.flip();
			buffer.get(b);
			if (!((b[0]==5)&&(b[1]==0))) {
				throw new Exception(new String.format("failed to connect <\\x%x\\x%x>",b[0],b[1]));
			}
		} catch (Exception e) {
			throw new Exception(new String.format("Exception while connect to socks5 server <%s>",e));
		}
		this.beginReceive();
		m_TunnelEstablished = true;
		super.onTunnelEstablished();
	}


	@Override
	protected void beforeSend(ByteBuffer buffer) throws Exception {
	}

	@Override
	protected void afterReceived(ByteBuffer buffer) throws Exception {
	}

	@Override
	protected boolean isTunnelEstablished() {
		return m_TunnelEstablished;
	}

	@Override
	protected void onDispose() {
		m_Config=null;
	}


}
