package me.smartproxy.tunnel.socks5;

import java.net.InetSocketAddress;

import android.net.Uri;
import me.smartproxy.tunnel.Config;

public class Socks5Config extends Config {
	public String UserName;
	public String Password;

	public static Socks5Config parse(String proxyInfo){
		Socks5Config config=new Socks5Config();
		Uri uri=Uri.parse(proxyInfo);
		String userInfoString=uri.getUserInfo();
		if(userInfoString!=null){
			String[] userStrings=userInfoString.split(":");
			config.UserName=userStrings[0];
			if(userStrings.length>=2){
				config.Password=userStrings[1];
			}
		}
		config.ServerAddress=new InetSocketAddress(uri.getHost(), uri.getPort());
		return config;
	}

	@Override
	public boolean equals(Object o) {
		if(o==null)
			return false;
		return this.toString().equals(o.toString());
	}

	@Override
	public String toString() {
		return String.format("socks5://%s:%s@%s", UserName,Password,ServerAddress);
	}
}
