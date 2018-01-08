package com.zed3.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String md5(String hexString) {
		try {
			final MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(hexString.getBytes());
			hexString = toHexString(instance.digest());
			return hexString;
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return "";
		}
	}

	private static String toHexString(final byte[] array) {
		final StringBuilder sb = new StringBuilder();
		for (int length = array.length, i = 0; i < length; ++i) {
			final byte b = array[i];
			int n;
			if ((n = b) < 0) {
				n = b + 256;
			}
			if (n < 16) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(n));
		}
		return sb.toString();
	}

	public static String toMd5(String hexString) {
		try {
			final MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.reset();
			instance.update(hexString.getBytes("utf-8"));
			hexString = toHexString(instance.digest());
			return hexString;
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		} catch (UnsupportedEncodingException ex2) {
			ex2.printStackTrace();
			return "";
		}
	}
}
