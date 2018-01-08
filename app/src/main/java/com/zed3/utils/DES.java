package com.zed3.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DES {
	private static byte[] iv;

	static {
		DES.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
	}

	public static String decryptDES(final String s, final String s2) throws Exception {
		new Base64();
		final byte[] decode = Base64.decode(s);
		final SecretKeySpec secretKeySpec = new SecretKeySpec(s2.getBytes(), "DES");
		final Cipher instance = Cipher.getInstance("DES/ECB/PKCS5Padding");
		instance.init(2, secretKeySpec);
		return new String(instance.doFinal(decode));
	}

	public static String encryptDES(final String s, final String s2) throws Exception {
		final SecretKeySpec secretKeySpec = new SecretKeySpec(s2.getBytes(), "DES");
		final Cipher instance = Cipher.getInstance("DES/ECB/PKCS5Padding");
		instance.init(1, secretKeySpec);
		return Base64.encode(instance.doFinal(s.getBytes()));
	}
}
