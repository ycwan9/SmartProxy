package me.smartproxy.tunnel.shadowsocks;

import android.text.TextUtils;

import java.util.HashMap;

import me.smartproxy.tunnel.IEncryptor;

public class EncryptorFactory {

    private static HashMap<String, IEncryptor> EncryptorCache = new HashMap<String, IEncryptor>();

    public static IEncryptor createEncryptorByConfig(ShadowsocksConfig config) throws Exception {
        if(!TextUtils.isEmpty(config.EncryptMethod)) {
            //TODO add supported method check
            IEncryptor encryptor = new ShadowsocksEncryptor(config.Password, config.EncryptMethod);
            return encryptor;
        }
//        if ("table".equals(config.EncryptMethod)) {
//            IEncryptor tableEncryptor = EncryptorCache.get(config.toString());
//            if (tableEncryptor == null) {
//                tableEncryptor = new TableEncryptor(config.Password);
//                if (EncryptorCache.size() > 2) {
//                    EncryptorCache.clear();
//                }
//                EncryptorCache.put(config.toString(), tableEncryptor);
//            }
//            return tableEncryptor;
//        } else if ("aes-256-cfb".equalsIgnoreCase(config.EncryptMethod)) {
//            return new Aes256cfbEncryptor(config.Password);
//        } else if ("rc4-md5".equalsIgnoreCase(config.EncryptMethod)) {
//            IEncryptor rc4md5Encryptor = EncryptorCache.get(config.toString());
//            if (rc4md5Encryptor == null) {
//                rc4md5Encryptor = new RC4MD5Encryptor(config.Password);
//                if (EncryptorCache.size() > 2) {
//                    EncryptorCache.clear();
//                }
//                EncryptorCache.put(config.toString(), rc4md5Encryptor);
//            }
//            return rc4md5Encryptor;
//        }
        throw new Exception(String.format(
                "Does not support the '%s' method. Only 'table' encrypt method was supported.",
                config.EncryptMethod));
    }
}
