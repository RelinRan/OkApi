
package androidx.api;

import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密解密
 */
public class AES {

    /**
     * 密钥KEY
     */
    public static String SECRET_KEY = "1111wwww2222uuuu";

    /**
     * 加密
     *
     * @param content 内容
     * @return
     */
    public static String encrypt(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        try {
            byte[] result = encrypt(SECRET_KEY, content);
            return new String(Base64.encode(result, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content 内容
     * @return
     */
    public static String decrypt(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        try {
            byte[] value = decrypt(SECRET_KEY, content.getBytes("UTF-8"));
            return new String(Base64.encode(value, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密
     *
     * @param key     key值
     * @param content 内容
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String key, String content) throws Exception {
        //创建AES秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES/ECB/PKCS5Padding");
        //创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        //初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        //加密
        return cipher.doFinal(content.getBytes("UTF-8"));
    }

    /**
     * 解密
     *
     * @param key     key值
     * @param content 内容
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(String key, byte[] content) throws Exception {
        // 创建AES秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES/ECB/PKCS5Padding");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        // 初始化解密器
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        // 解密
        return cipher.doFinal(content);
    }

}