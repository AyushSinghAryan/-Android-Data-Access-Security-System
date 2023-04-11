package com.example.securefileaccess.helper;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Authentication {

    private static byte[] SecretKey = {0x0f};
    String key = "1234567890123456";

    Context context;

    public Authentication(Context con) {
        context = con;
    }

    //Excrypt
    public byte[] encrypt(String password, String key)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        byte[] textBytes = password.getBytes("UTF-8");
        byte[] keybytes = key.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(keybytes);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    //Decrypt
    public byte[] decrypt(String password, String key)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {


        byte[] textBytes = Base64.decode(password, Base64.DEFAULT);
        byte[] keybytes = key.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(keybytes);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

//    public String encryptFile(String path, File file) {
//        String ans = path;
//        byte[] txt0;
//        try {
//            BufferedOutputStream bos = new BufferedOutputStream(
//                    new FileOutputStream(file));
//            txt0 = encrypt(ans, key);
//            bos.write(txt0);
//            bos.flush();
//            bos.close();
//            ans = Base64.encodeToString(txt0, Base64.DEFAULT);
//            return ans;
//
//        } catch (Exception e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//        return ans;
//    }

    public String EncyptMesg(String mesg) {
        String ans = mesg;
        byte[] txt0 = new byte[0];
        try {
            txt0 = encrypt(ans, key);
            ans = Base64.encodeToString(txt0, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return ans;
    }

    public String DecryptMesg(String mesg) {
        byte[] txt1 = new byte[0];
        String ans = "";
        try {
            txt1 = decrypt(mesg, key);
            ans = new String(txt1, "UTF-8");
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return ans;
    }

}
