package org.zoolu.tools;

public class Base64 {
	private static int[] aux;
	private static final String base64codes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	static {
		Base64.aux = new int[4];
	}

	public static byte[] decode(final String s) {
		final int index = s.indexOf("=");
		String substring = s;
		if (index != -1) {
			substring = s.substring(0, index);
		}
		final int[] array = new int[3];
		final int n = substring.length() / 4;
		final int n2 = substring.length() % 4;
		int n3 = 0;
		if (n2 != 0) {
			n3 = 1;
		}
		final byte[] array2 = new byte[n * 3 + (n2 - 1) * n3];
		int i;
		for (i = 0; i < n; ++i) {
			Base64.aux[0] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4));
			Base64.aux[1] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 1));
			Base64.aux[2] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 2));
			Base64.aux[3] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 3));
			array[0] = (Base64.aux[0] << 2) + (Base64.aux[1] >>> 4);
			array[1] = (Base64.aux[1] % 16 << 4) + (Base64.aux[2] >>> 2);
			array[2] = (Base64.aux[2] % 4 << 6) + Base64.aux[3];
			array2[i * 3] = (byte) array[0];
			array2[i * 3 + 1] = (byte) array[1];
			array2[i * 3 + 2] = (byte) array[2];
		}
		if (i == n) {
			if (n2 == 2) {
				Base64.aux[0] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4));
				Base64.aux[1] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 1));
				array[0] = (Base64.aux[0] << 2) + (Base64.aux[1] >>> 4);
				array2[i * 3] = (byte) array[0];
			}
			if (n2 == 3) {
				Base64.aux[0] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4));
				Base64.aux[1] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 1));
				Base64.aux[2] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf(substring.charAt(i * 4 + 2));
				array[0] = (Base64.aux[0] << 2) + (Base64.aux[1] >>> 4);
				array[1] = (Base64.aux[1] % 16 << 4) + (Base64.aux[2] >>> 2);
				array2[i * 3] = (byte) array[0];
				array2[i * 3 + 1] = (byte) array[1];
			}
		}
		return array2;
	}

	public static String encode(final byte[] array) {
		String string = "";
		final byte[] array2 = new byte[3];
		final int n = array.length / 3;
		final int n2 = array.length % 3;
		int i;
		for (i = 0; i < n; ++i) {
			array2[0] = array[i * 3];
			array2[1] = array[i * 3 + 1];
			array2[2] = array[i * 3 + 2];
			Base64.aux[0] = (array2[0] >>> 2 & 0x3F);
			Base64.aux[1] = ((array2[0] & 0x3) << 4) + (array2[1] >>> 4 & 0xF);
			Base64.aux[2] = ((array2[1] & 0xF) << 2) + (array2[2] >>> 6 & 0x3);
			Base64.aux[3] = (array2[2] & 0x3F);
			string = String.valueOf(string) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[0]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[1]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[2]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[3]);
		}
		String string2 = string;
		if (i == n) {
			string2 = string;
			if (n2 != 0) {
				if (n2 == 1) {
					Base64.aux[0] = (array[n * 3] >>> 2 & 0x3F);
					Base64.aux[1] = (array[n * 3] & 0x3) << 4;
					string2 = String.valueOf(string) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[0]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[1]) + "==";
				} else {
					string2 = string;
					if (n2 == 2) {
						Base64.aux[0] = (array[n * 3] >>> 2 & 0x3F);
						Base64.aux[1] = ((array[n * 3] & 0x3) << 4) + (array[n * 3 + 1] >>> 4 & 0xF);
						Base64.aux[2] = (array[n * 3 + 1] & 0xF) << 2;
						return String.valueOf(string) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[0]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[1]) + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".charAt(Base64.aux[2]) + "=";
					}
				}
			}
		}
		return string2;
	}

	public static void main(String[] array) {
		final String encode = encode(array[0].getBytes());
		System.out.println("messaggio codificato: " + encode);
		final byte[] decode = decode(encode);
		array = (String[]) (Object) "";
		while (true) {
			try {
				array = (String[]) (Object) new String(decode, "ISO-8859-1");
				System.out.println("messaggio decodificato e: " + (String) (Object) array);
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}
}
