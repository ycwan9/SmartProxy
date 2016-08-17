package me.smartproxy.tunnel.shadowsocks;

import android.text.TextUtils;

import java.util.HashMap;

import me.smartproxy.tunnel.IEncryptor;

public class EncryptorFactory {

    public static IEncryptor createEncryptorByConfig(ShadowsocksConfig config) throws Exception {
        if(!TextUtils.isEmpty(config.EncryptMethod)) {
            IEncryptor encryptor = new ShadowsocksEncryptor(config.Password, config.EncryptMethod);
            return encryptor;
        }
        throw new Exception(String.format(
                "Does not support the '%s' method. Only 'table' encrypt method was supported.",
                config.EncryptMethod));
    }
}
