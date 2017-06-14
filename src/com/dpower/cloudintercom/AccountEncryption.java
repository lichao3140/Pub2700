package com.dpower.cloudintercom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

/**
 * AES算法加密解密
 * @author LiChao
 *
 */
public class AccountEncryption {
	
	private static byte[] g_MsgAesIv ={
		(byte)0xdd,(byte)0x11,(byte)0xed,(byte)0xf8,(byte)0xd2,(byte)0xa1,(byte)0xa5,(byte)0x7d,
		(byte)0xd1,(byte)0xd8,(byte)0x02,(byte)0xe7,(byte)0x24,(byte)0x0a,(byte)0x0e,(byte)0x92,
	};
	
	private static byte[] g_MsgAesKey ={
		(byte)0x59,(byte)0xf9,(byte)0x93,(byte)0x83,(byte)0xe9,(byte)0xc7,(byte)0xee,(byte)0x61,(byte)0xc2,(byte)0x1d
		,(byte)0x83,(byte)0x1d,(byte)0x5d,(byte)0x27,(byte)0x1f,(byte)0x63,(byte)0x72,(byte)0x16,(byte)0x7a,(byte)0x18
		,(byte)0x51,(byte)0x18,(byte)0x5e,(byte)0xed,(byte)0x89,(byte)0x87,(byte)0x0b,(byte)0x35,(byte)0x05,(byte)0x60
		,(byte)0xdb,(byte)0xf4
	};
	
	/** 加密 */
	public static String getEncodeString(String input) throws Exception{
		return Base64.encodeToString(AesEncodeMsgToByte(input.getBytes("ISO-8859-1")), Base64.DEFAULT);
	}
	
	/** 解密 */
	public static String getDecodeString(String input) throws Exception{
		return new String(AesDecodeMsgToByte(Base64.decode(input.getBytes("ISO-8859-1"), Base64.DEFAULT)), "ISO-8859-1");
	}
	
	public static byte[] AesEncodeMsgToByte(byte[] input) throws Exception{
		SecretKeySpec key = new SecretKeySpec(g_MsgAesKey, "AES");
		IvParameterSpec iv = new IvParameterSpec(g_MsgAesIv);
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CTR/Nopadding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		return cipher.doFinal(input);
	}
	
	public static byte[] AesDecodeMsgToByte(byte[] input) throws Exception{
		SecretKeySpec key = new SecretKeySpec(g_MsgAesKey, "AES");
		IvParameterSpec iv = new IvParameterSpec(g_MsgAesIv);
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CTR/Nopadding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		return cipher.doFinal(input);
	}
	
	public static String SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("SHA-1");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String SHA(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("SHA");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
