package edu.uci.ics.deid.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static byte[] iv;

    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(StandardCharsets.US_ASCII);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setIv(String myIv)
    {
        MessageDigest sha = null;
        try {
            iv = myIv.getBytes(StandardCharsets.US_ASCII);
            sha = MessageDigest.getInstance("SHA-1");
            iv = sha.digest(iv);
            iv = Arrays.copyOf(iv, 16);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret, String ivString)
    {
        try
        {
            setKey(secret);
            setIv(ivString);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(iv));
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret, String ivString)
    {
        try
        {
            setKey(secret);
            setIv(ivString);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    
}