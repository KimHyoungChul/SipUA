package com.zed3.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64 {
	private static final char[] legalChars;

	static {
		legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	}

	private static int decode(final char c) {
		if (c >= 'A' && c <= 'Z') {
			return c - 'A';
		}
		if (c >= 'a' && c <= 'z') {
			return c - 'a' + '\u001a';
		}
		if (c >= '0' && c <= '9') {
			return c - '0' + '\u001a' + '\u001a';
		}
		switch (c) {
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 0;
			default:
				throw new RuntimeException("unexpected code: " + c);
		}
	}

	private static void decode(final String s, final OutputStream outputStream) throws IOException {
		int n = 0;
		final int length = s.length();
		while (true) {
			if (n >= length || s.charAt(n) > ' ') {
				if (n == length) {
					break;
				}
				final int n2 = (decode(s.charAt(n)) << 18) + (decode(s.charAt(n + 1)) << 12) + (decode(s.charAt(n + 2)) << 6) + decode(s.charAt(n + 3));
				outputStream.write(n2 >> 16 & 0xFF);
				if (s.charAt(n + 2) == '=') {
					break;
				}
				outputStream.write(n2 >> 8 & 0xFF);
				if (s.charAt(n + 3) == '=') {
					break;
				}
				outputStream.write(n2 & 0xFF);
				n += 4;
			} else {
				++n;
			}
		}
	}

	public static byte[] decode(String byteArray) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			decode(byteArray, bos);
			byte[] decodedBytes = bos.toByteArray();
			try {
				bos.close();
			} catch (IOException ex) {
				System.err.println("Error while decoding BASE64: " + ex.toString());
			}
			return decodedBytes;
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	public static String encode(final byte[] array) {
		final int length = array.length;
		final StringBuffer sb = new StringBuffer(array.length * 3 / 2);
		int i = 0;
		int n = 0;
		while (i <= length - 3) {
			final int n2 = (array[i] & 0xFF) << 16 | (array[i + 1] & 0xFF) << 8 | (array[i + 2] & 0xFF);
			sb.append(Base64.legalChars[n2 >> 18 & 0x3F]);
			sb.append(Base64.legalChars[n2 >> 12 & 0x3F]);
			sb.append(Base64.legalChars[n2 >> 6 & 0x3F]);
			sb.append(Base64.legalChars[n2 & 0x3F]);
			final int n3 = i + 3;
			int n4 = n + 1;
			if (n >= 14) {
				n4 = 0;
				sb.append(" ");
			}
			n = n4;
			i = n3;
		}
		if (i == 0 + length - 2) {
			final int n5 = (array[i] & 0xFF) << 16 | (array[i + 1] & 0xFF) << 8;
			sb.append(Base64.legalChars[n5 >> 18 & 0x3F]);
			sb.append(Base64.legalChars[n5 >> 12 & 0x3F]);
			sb.append(Base64.legalChars[n5 >> 6 & 0x3F]);
			sb.append("=");
		} else if (i == 0 + length - 1) {
			final int n6 = (array[i] & 0xFF) << 16;
			sb.append(Base64.legalChars[n6 >> 18 & 0x3F]);
			sb.append(Base64.legalChars[n6 >> 12 & 0x3F]);
			sb.append("==");
		}
		return sb.toString();
	}
}
