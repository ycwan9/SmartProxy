package me.smartproxy.tunnel.shadowsocks;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import me.smartproxy.tunnel.IEncryptor;

public class Aes256cfbEncryptor implements IEncryptor {

    private static final int INDEX_KEY = 0;

    private static final int INDEX_IV = 1;

    private static final int ITERATIONS = 1;

    private static final int KEY_SIZE_BITS = 32;

    private static final int KEY_LEN = 16;

    private static final int IV_LEN = 16;

    private String password;

    private byte[] encryptIv;

    private byte[] decryptIv;

    public Aes256cfbEncryptor(String password) {
        this.password = password;
    }

    /**
     * Thanks go to Ola Bini for releasing this source on his blog. The source was obtained from <a
     * href="http://olabini.com/blog/tag/evp_bytestokey/">here</a> .
     */
    public byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt,
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

    public byte[] encrypt(byte[] contents, String pw)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher aesCFB = Cipher.getInstance("AES/CFB/NoPadding");
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        final byte[][] keyAndIV = EVP_BytesToKey(KEY_LEN, IV_LEN,
                md5, null, pw.getBytes(), ITERATIONS);

        if (encryptIv == null) {
            encryptIv = keyAndIV[INDEX_IV];
        }

        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
        IvParameterSpec iv = new IvParameterSpec(encryptIv);

        aesCFB.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] result = aesCFB.doFinal(contents);
        return concat(encryptIv, result);
    }

    public byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public byte[] decrypt(byte[] encrypted, String pw)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher aesCBC = Cipher.getInstance("AES/CFB/NoPadding");
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        byte[] realData = null;
        if (decryptIv == null) {
            decryptIv = new byte[IV_LEN];
            realData = new byte[encrypted.length - IV_LEN];
            System.arraycopy(encrypted, 0, decryptIv, 0, IV_LEN);
            System.arraycopy(encrypted, IV_LEN, realData, 0, realData.length);
        }

        if (realData == null) {
            realData = encrypted;
        }
        final byte[][] keyAndIV = EVP_BytesToKey(KEY_LEN, IV_LEN,
                md5, null, pw.getBytes(), ITERATIONS);
        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
        IvParameterSpec iv = new IvParameterSpec(decryptIv);

        aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
        return aesCBC.doFinal(realData);
    }

    public void encryptTest() throws Exception {
        String pw = "AKeyForAES128CBC";

        byte[] encrypted = "Secret message! Oh, My GOD!".getBytes();

        byte[] decrypted = encrypt(encrypted, pw);
        String str = new String(decrypted, "UTF-8");
        System.out.println(str);
        String answer = new String(decrypt(decrypted, pw));
        System.out.println(answer);
//        answer = new String(decrypt(str.getBytes(), pw));
//        System.out.println(answer);
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        try {
            return ByteBuffer.wrap(encrypt(buffer.array(), password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ByteBuffer decrypt(ByteBuffer buffer) {
        try {
            return ByteBuffer.wrap(decrypt(buffer.array(), password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
