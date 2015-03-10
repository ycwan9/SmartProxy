package me.smartproxy.tunnel.shadowsocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import me.smartproxy.tunnel.IEncryptor;

public class Aes256cfbEncryptor implements IEncryptor {

    private static final int KEY_LEN = 32;

    private static final int IV_LEN = 16;

    private byte[] _key = null;

    private byte[] _encryptIv = null;

    private byte[] _dectyptIv = null;

    public Aes256cfbEncryptor(String password) {
        try {
//            _key = EVP_BytesToKey(KEY_LEN, IV_LEN, MessageDigest.getInstance("MD5"), null,
//                    password.getBytes(), 1);
            initKey(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thanks go to Ola Bini for releasing this source on his blog.
     * The source was obtained from <a href="http://olabini.com/blog/tag/evp_bytestokey/">here</a>
     * .
     */
    public static byte[] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md,
            byte[] salt, byte[] data, int count) {
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
            return key;
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
        return key;
    }

    private void initKey(byte[] password) throws NoSuchAlgorithmException {
        byte[] result = new byte[password.length + 16];
        int i = 0;
        byte[] md5sum = null;
        _key = new byte[KEY_LEN];
        while (i < KEY_LEN) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            if (i == 0) {
                md5sum = digest.digest(password);
            } else {
                System.arraycopy(md5sum, 0, result, 0, md5sum.length);
                System.arraycopy(password, 0, result, md5sum.length, password.length);
                md5sum = digest.digest(result);
            }
            System.arraycopy(md5sum, 0, _key, i, md5sum.length);
            i += md5sum.length;
        }
        secretKeySpec = new SecretKeySpec(_key, "AES");
    }

    private SecretKeySpec secretKeySpec;

//    private IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    public byte[] encrypt(byte[] text) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (_encryptIv == null) {
            _encryptIv = randBytes();
            stream.write(_encryptIv);
        }

        // Encrypt text
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKeySpec,
                new IvParameterSpec(_encryptIv));
        byte[] encryptText = cipher.doFinal(text);
        // Hash text
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        digest.update(encryptText);
        stream.write(encryptText);

        byte[] bytes = stream.toByteArray();
        stream.close();
        return bytes;
    }

    private byte[] randBytes() {
        byte[] newIv = new byte[IV_LEN];
        new Random().nextBytes(newIv);
        return newIv;
    }

    public byte[] decrypt(byte[] bytes) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, InvalidHashException, InvalidHeaderException {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        if (_dectyptIv == null) {
            _dectyptIv = new byte[IV_LEN];
            buf.get(_dectyptIv);
        }
//        if (!Arrays.equals(header, Aes256cfbEncryptor.header)) {
//            throw new InvalidHeaderException(
//                    "Header is not valid. Decryption aborted.");
//        }

        int aeslen = bytes.length - IV_LEN;
        byte[] aes = new byte[aeslen];
        buf.get(aes);

        // Decrypt text
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, this.secretKeySpec,
                new IvParameterSpec(_dectyptIv));
        byte[] text = cipher.doFinal(aes);

        // Compute hash
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        digest.update(text);
//        byte[] hash = digest.digest();
//
//        byte[] hash2 = new byte[shalen];
//        buf.get(hash2);
//
//        if (!Arrays.equals(hash, hash2)) {
//            throw new InvalidHashException(
//                    "Verification failed. Decryption aborted.");
//        }

        return text;
    }

    @Override
    public ByteBuffer encrypt(ByteBuffer buffer) {
        try {
            byte[] data = encrypt(buffer.array());
            return ByteBuffer.wrap(data);
        } catch (IOException e) {
            e.printStackTrace();
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
            return ByteBuffer.wrap(decrypt(buffer.array()));
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
        } catch (InvalidHashException e) {
            e.printStackTrace();
        } catch (InvalidHeaderException e) {
            e.printStackTrace();
        }
        return null;
    }

    class InvalidHeaderException extends Exception {

        private static final long serialVersionUID = 1L;

        public InvalidHeaderException(String string) {
            super(string);
        }
    }

    class InvalidHashException extends Exception {

        private static final long serialVersionUID = 1L;

        public InvalidHashException(String string) {
            super(string);
        }
    }

//    public static void main(String[] args) throws Exception {
//        Aes256cfbEncryptor c = new Aes256cfbEncryptor(null);
//        System.out.println(c.decrypt(c.encrypt(
//                "String encryption/decryption with integrity check. In a real world example, the key should be kept secret and the IV should be unique.")));
//
//    }
}
