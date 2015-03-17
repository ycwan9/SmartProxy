package me.smartproxy.tunnel.shadowsocks;

import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import me.smartproxy.tunnel.IEncryptor;

public class AESEncryptor implements IEncryptor {

    private static final int INDEX_KEY = 0;

    private static final int INDEX_IV = 1;

    private static final int ITERATIONS = 1;

    private static final int KEY_SIZE_BITS = 256;

    private static final int IV_SIZE = 16;

    private SecretKeySpec _keySpec;

    private IvParameterSpec _encryptIV, _decryptIV;

    private byte[] encryptIVBytes;

    public AESEncryptor(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = EVP_BytesToKey(KEY_SIZE_BITS / Byte.SIZE,
                    IV_SIZE, md5, null, password.getBytes(), ITERATIONS);
            _keySpec = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
            encryptIVBytes = keyAndIV[INDEX_IV];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thanks go to Ola Bini for releasing this source on his blog. The source was obtained from <a
     * href="http://olabini.com/blog/tag/evp_bytestokey/">here</a> .
     */
    public static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt,
            byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (; ; ) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (; ; ) {
                    if (nkey == 0) {
                        break;
                    }
                    if (i == md_buf.length) {
                        break;
                    }
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (; ; ) {
                    if (niv == 0) {
                        break;
                    }
                    if (i == md_buf.length) {
                        break;
                    }
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }

    private Lock encryptLock = new ReentrantLock();

    private Lock decryptLock = new ReentrantLock();

    public byte[] encrypt(byte[] contents) {
        byte[] data = Base64.encode(contents, Base64.DEFAULT);
        Log.e("TAG", "buff len is " + data.length);
        try {
            encryptLock.lock();
            Cipher aesCBC = Cipher.getInstance("AES/CFB/PKCS5Padding");

            if (_encryptIV == null) {
                _encryptIV = new IvParameterSpec(encryptIVBytes);
                aesCBC.init(Cipher.ENCRYPT_MODE, _keySpec, _encryptIV);
                byte[] cipher = aesCBC.doFinal(data);
                byte[] result = new byte[IV_SIZE + cipher.length];
                System.arraycopy(encryptIVBytes, 0, result, 0, IV_SIZE);
                System.arraycopy(cipher, 0, result, IV_SIZE, cipher.length);
                return result;
            }

            aesCBC.init(Cipher.ENCRYPT_MODE, _keySpec, _encryptIV);
            byte[] result = aesCBC.doFinal(data);
            Log.e("TAG", "result length is " + result.length);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        } finally {
            encryptLock.unlock();
        }
        return contents;
    }

    public byte[] decrypt(byte[] encrypted) {
        try {
            decryptLock.lock();
            Cipher aesCBC = Cipher.getInstance("AES/CFB/PKCS5Padding");
            byte[] realData;
            if (_decryptIV == null) {
                byte[] iv = new byte[IV_SIZE];
                realData = new byte[encrypted.length - IV_SIZE];
                System.arraycopy(encrypted, 0, iv, 0, IV_SIZE);
                System.arraycopy(encrypted, IV_SIZE, realData, 0, encrypted.length - IV_SIZE);
                _decryptIV = new IvParameterSpec(iv);
            } else {
                realData = encrypted;
            }
            byte[] data = Base64.decode(realData, Base64.DEFAULT);
            aesCBC.init(Cipher.DECRYPT_MODE, _keySpec, _decryptIV);
            return aesCBC.doFinal(data);
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } finally {
            decryptLock.unlock();
        }
        return encrypted;
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        byte[] result = encrypt(data);
        return ByteBuffer.wrap(result);
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        byte[] result = decrypt(data);
        return ByteBuffer.wrap(result);
    }

//    public static void main(String[] args) throws Exception {
//        String pw = "AKeyForAES128CBC";
//
//        byte[] salt = "1234567890".getBytes();
//        byte[] encrypted = "Secret message! Oh, My GOD!".getBytes();
//
//        byte[] decrypted = encrypt(salt, encrypted, pw);
//        String str = Base16.encode(decrypted);
//        System.out.println(str);
//        String answer = new String(decrypt(salt, decrypted, pw));
//        System.out.println(answer);
//        answer = new String(decrypt(salt, Base16.decode(str), pw));
//        System.out.println(answer);
//    }
}
