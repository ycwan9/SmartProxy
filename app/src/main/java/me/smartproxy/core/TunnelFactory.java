package me.smartproxy.core;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import me.smartproxy.tunnel.Config;
import me.smartproxy.tunnel.RawTunnel;
import me.smartproxy.tunnel.Tunnel;
import me.smartproxy.tunnel.httpconnect.HttpConnectConfig;
import me.smartproxy.tunnel.httpconnect.HttpConnectTunnel;
import me.smartproxy.tunnel.shadowsocks.ShadowsocksConfig;
import me.smartproxy.tunnel.shadowsocks.ShadowsocksTunnel;
import me.smartproxy.tunnel.socks5.Socks5Config;
import me.smartproxy.tunnel.socks5.Socks5Tunnel;

public class TunnelFactory {

	public static Tunnel wrap(SocketChannel channel,Selector selector){
		return new RawTunnel(channel, selector);
	}

	public static Tunnel createTunnelByConfig(InetSocketAddress destAddress,Selector selector) throws Exception {
		if(destAddress.isUnresolved()){
			Config config=ProxyConfig.getInstance().getDefaultTunnelConfig(destAddress);
			if(config instanceof HttpConnectConfig){
				return new HttpConnectTunnel((HttpConnectConfig)config,selector);
			}else if(config instanceof ShadowsocksConfig){
				return new ShadowsocksTunnel((ShadowsocksConfig)config,selector);
			}else if(config instanceof Socks5Config){
				return new Socks5Tunnel((Socks5Config)config,selector);
			}
			throw new Exception("The config is unknow.");
		}else {
			return new RawTunnel(destAddress, selector);
		}
	}

}
