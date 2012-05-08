package com.southfreo.quota.utils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * Usage:
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 * @author ferenc.hechler
 */

public class Security {

	public final static String TAG = "Security";
	
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}
	
	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
	    kgen.init(128, sr); // 192 and 256 bits may not be available
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}
	
	private static byte[] iPhoneKey(String s) throws Exception {
		byte[] raw = s.getBytes("UTF8");
		byte[] mykey = {44,27,88,22,44,77,11,22,32,45,22,11,34,21,46,77};
		
		for (int i=0;i<raw.length && i<mykey.length;i++) {
			mykey[i] = raw[i];
		}
		return mykey;
	}
	

	public static String encryptBase64(String pwd, String strString) {
		try {
			byte[] raw = strString.getBytes("UTF8");
			SecretKeySpec skeySpec = new SecretKeySpec(iPhoneKey(pwd), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		    byte[] crypted = cipher.doFinal(raw);
		    return Base64.encodeBytes(crypted);
		} catch (Exception e) {
			Log.e(TAG,e.toString());
			return null;
		}
	}

	public  static String decryptBase64(String pwd, String b64String) {
		try {
			byte[] ea = Base64.decode(b64String);
			SecretKeySpec skeySpec = new SecretKeySpec(iPhoneKey(pwd), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
		    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		    byte[] decrypted = cipher.doFinal(ea);
		    return new String(decrypted,"UTF8");
		} catch (Exception e) {
			Log.e(TAG,e.toString());
			return null;
		}
	}
	
	
	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
	
}
