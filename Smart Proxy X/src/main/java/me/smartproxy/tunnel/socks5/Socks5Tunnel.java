package me.smartproxy.tunnel.socks5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.Bidi;

import me.smartproxy.core.LocalVpnService;
import me.smartproxy.core.ProxyConfig;
import me.smartproxy.tunnel.Tunnel;

public class Socks5Tunnel extends Tunnel {

	private boolean m_TunnelEstablished;
	private Socks5Config m_Config;
	private int stage;

	public Socks5Tunnel(Socks5Config config,Selector selector) throws IOException {
		super(config.ServerAddress,selector);
		m_Config=config;
		LocalVpnService.Instance.writeLog("new socks5 tunnel start");
		stage = 1;
	}

	@Override
	protected void onConnected(ByteBuffer buffer) throws Exception {
		try{
			byte[] b = new byte[2];
			String request1 = "\005\001\000";
			//\x05 socks v5
			//\x01 nmethods 1
			//\x00 methods 00 (no auth)
			buffer.clear();
			buffer.put(request1.getBytes());
			buffer.flip();
			if (!this.write(buffer,true)){
				throw new Exception("Err socks5 connect 1st failed");
			}
			buffer.clear();
			this.beginReceive();
		} catch (Exception e) {
			throw new Exception(String.format("Err Exception while connect to socks5 server <%s>",e));
		}
	}


	@Override
	protected void beforeSend(ByteBuffer buffer) throws Exception {
	}

	void sendConnectCmd(ByteBuffer buffer) throws Exception {
		LocalVpnService.Instance.writeLog("get into sendConnectCmd()");
		LocalVpnService.Instance.writeLog("address is <%s>", m_DestAddress.toString());
		String request2 = "\005\001\000\003";
		//\x05 version 5
		//\x01 CMD CONNECT
		//\x00 RSV
		//\x03 FQDN
		buffer.clear();
		String hostname = m_DestAddress.getHostName();
		buffer.put(request2.getBytes());
		buffer.put((byte)hostname.length());
		buffer.put(hostname.getBytes());
		buffer.put((byte)(0xff00 & m_DestAddress.getPort()));//send 
		buffer.put((byte)(0x00ff & m_DestAddress.getPort()));//htons(port)
		buffer.flip();
		LocalVpnService.Instance.writeLog("buffer flip to send");
		if(!this.write(buffer,true)){
			throw new Exception("Err socks5 CMD connect failed to send");
		}
	}

	@Override
	protected void afterReceived(ByteBuffer buffer) throws Exception {
		LocalVpnService.Instance.writeLog(String.format("socks5 afterRecv stat %d stage %d", ( m_TunnelEstablished ? 1: 0), stage));
		if (!m_TunnelEstablished) {
			LocalVpnService.Instance.writeLog("socks5 afterRecv auth stage %d",stage);
			byte[] b = new byte[2];
			buffer.get(b);
			LocalVpnService.Instance.writeLog("socks5 handshake stage %d had <\\x%x\\x%x>", stage, b[0], b[1]);
			if (!((b[0]==5)&&(b[1]==0))) {
				throw new Exception(String.format("Err failed to auth on stage %d <\\x%x\\x%x>", stage, b[0], b[1]));
			}
			try {
				if (stage == 1) {
					LocalVpnService.Instance.writeLog("socks5 handshake stage 01 get into if()");
					this.sendConnectCmd(buffer);
					buffer.clear();
					stage = 2;
				} else if (stage == 2) {
					LocalVpnService.Instance.writeLog("socks5 handshake stage 02 get into if()");
					m_TunnelEstablished = true;
					super.onTunnelEstablished();
					stage = 0;
				}
			} catch (Exception e) {
				LocalVpnService.Instance.writeLog("Exception on handshake err <%s> in stage <%d>",e,stage);
			}
		}
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
