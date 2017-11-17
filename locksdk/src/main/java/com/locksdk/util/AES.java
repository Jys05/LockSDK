package com.locksdk.util;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Sujiayong on 2017/10/25.
 * AES加密解密工具
 */

public class AES {

    // 加密
    public static byte[] Encrypt(byte[] sData, String sKey) throws Exception {
        byte[] raw = Base64Util.decode(sKey);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sData);

//        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        return encrypted;
    }

    public static byte[] Encrypt(byte[] sData, byte[] sKey) throws Exception {
        byte[] raw = sKey;
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sData);

//        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        return encrypted;
    }


    // 解密
    public static byte[] Decrypt(byte[] sData, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            byte[] raw = Base64Util.decode(sKey);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//            byte[] encrypted1 = new Base64.decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(sData);
//                String originalString = new String(original,"utf-8");
                return original;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }


// 解密(用于通讯秘钥)
    public static byte[] Decrypt(byte[] sData, byte[] sKey) throws Exception {
        try {
            // 判断Key是否正确
            byte[] raw = sKey;
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//            byte[] encrypted1 = new Base64.decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(sData);
//                String originalString = new String(original,"utf-8");
                return original;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

}
