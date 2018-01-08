package org.zoolu.tools;

public class Mangle {
	public static byte[] addressToBytes(final String s) {
		int n = 0;
		final byte[] array = new byte[4];
		for (int i = 0; i < 4; ++i) {
			if (i < 3) {
				final int index = s.indexOf(46, n);
				array[i] = (byte) Integer.parseInt(s.substring(n, index));
				n = index + 1;
			} else {
				array[3] = (byte) Integer.parseInt(s.substring(n));
			}
		}
		return array;
	}

	public static String bytesToAddress(final byte[] array) {
		return String.valueOf(Integer.toString(uByte(array[0]))) + "." + Integer.toString(uByte(array[1])) + "." + Integer.toString(uByte(array[2])) + "." + Integer.toString(uByte(array[3]));
	}

	public static String bytesToHexString(final byte[] array) {
		return bytesToHexString(array, array.length);
	}

	public static String bytesToHexString(final byte[] array, final int n) {
		String string = new String();
		for (int i = 0; i < n; ++i) {
			string = String.valueOf(new StringBuilder(String.valueOf(string)).append(Integer.toHexString((array[i] + 256) % 256 / 16 % 16)).toString()) + Integer.toHexString((array[i] + 256) % 256 % 16);
		}
		return string;
	}

	public static long bytesToInt(final byte[] array) {
		return (((uByte(array[0]) << 8) + uByte(array[1]) << 8) + uByte(array[2]) << 8) + uByte(array[3]);
	}

	public static long bytesToWord(final byte[] array) {
		return (((uByte(array[3]) << 8) + uByte(array[2]) << 8) + uByte(array[1]) << 8) + uByte(array[0]);
	}

	public static long bytesToWord(final byte[] array, final int n) {
		return (((uByte(array[n + 3]) << 8) + uByte(array[n + 2]) << 8) + uByte(array[n + 1]) << 8) + uByte(array[n + 0]);
	}

	public static byte[] clone(final byte[] array) {
		return getBytes(array, 0, array.length);
	}

	public static boolean compare(final byte[] array, final byte[] array2) {
		if (array.length == array2.length) {
			for (int i = 0; i < array.length; ++i) {
				if (array[i] != array2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static void copyBytes(final byte[] array, final byte[] array2, final int n) {
		for (int i = 0; i < array.length; ++i) {
			array2[n + i] = array[i];
		}
	}

	public static void copyBytes(final byte[] array, final byte[] array2, final int n, final int n2) {
		for (int i = 0; i < n2; ++i) {
			array2[n + i] = array[i];
		}
	}

	public static void copyFourBytes(final byte[] array, final byte[] array2, final int n) {
		copyBytes(array, array2, n, 4);
	}

	public static void copyTwoBytes(final byte[] array, final byte[] array2, final int n) {
		copyBytes(array, array2, n, 2);
	}

	private static void decode(final byte[] array, final int[] array2) {
		int i = 0;
		int n = 0;
		while (i < 64) {
			array2[n] = ((array[i] & 0xFF) | (array[i + 1] & 0xFF) << 8 | (array[i + 2] & 0xFF) << 16 | array[i + 3] << 24);
			++n;
			i += 4;
		}
	}

	public static byte[] fourBytes(final byte[] array, final int n) {
		return getBytes(array, n, 4);
	}

	public static byte[] getBytes(final byte[] array, final int n, final int n2) {
		final byte[] array2 = new byte[n2];
		for (int i = 0; i < n2; ++i) {
			array2[i] = array[n + i];
		}
		return array2;
	}

	public static byte[] hexStringToBytes(final String s) {
		return hexStringToBytes(s, -1);
	}

	public static byte[] hexStringToBytes(final String s, int i) {
		String s2 = s;
		if (s.indexOf(":") >= 0) {
			s2 = "";
			String string;
			for (int j = 0; j < s.length(); ++j, s2 = string) {
				final char char1 = s.charAt(j);
				string = s2;
				if (char1 != ':') {
					string = String.valueOf(s2) + char1;
				}
			}
		}
		int n;
		if ((n = i) < 0) {
			n = s2.length() / 2;
		}
		final byte[] array = new byte[n];
		for (i = 0; i < n; ++i) {
			if (n < s2.length() / 2) {
				array[i] = (byte) Integer.parseInt(s2.substring(i * 2, i * 2 + 2), 16);
			} else {
				array[i] = 0;
			}
		}
		return array;
	}

	public static byte[] initBytes(final byte[] array, final int n) {
		for (int i = 0; i < array.length; ++i) {
			array[i] = (byte) n;
		}
		return array;
	}

	public static byte[] intToBytes(final long n) {
		return new byte[]{(byte) (n >> 24), (byte) ((n >> 16) % 256L), (byte) ((n >> 8) % 256L), (byte) (n % 256L)};
	}

	public static void main(final String[] array) {
		final byte[] array2 = new byte[64];
		for (int i = 0; i < 64; ++i) {
			array2[i] = (byte) i;
		}
		final int[] array3 = new int[16];
		for (int j = 0; j < 16; ++j) {
			array3[j] = (int) bytesToWord(array2, j * 4);
		}
		for (int k = 0; k < 16; ++k) {
			print("x[" + k + "]: " + bytesToHexString(wordToBytes(array3[k])));
		}
		decode(array2, array3);
		for (int l = 0; l < 16; ++l) {
			print("x[" + l + "]: " + bytesToHexString(wordToBytes(array3[l])));
		}
	}

	private static void print(final String s) {
		System.out.println(s);
	}

	private static int rotateLeft(final int n, final int n2) {
		return n << n2 | n >>> 32 - n2;
	}

	private static byte[] rotateLeft(final byte[] array) {
		final int length = array.length;
		final byte b = array[length - 1];
		for (int i = length - 1; i > 1; --i) {
			array[i] = array[i - 1];
		}
		array[0] = b;
		return array;
	}

	private static int[] rotateLeft(final int[] array) {
		final int length = array.length;
		final int n = array[length - 1];
		for (int i = length - 1; i > 1; --i) {
			array[i] = array[i - 1];
		}
		array[0] = n;
		return array;
	}

	private static int rotateRight(final int n, final int n2) {
		return n >>> n2 | n << 32 - n2;
	}

	private static byte[] rotateRight(final byte[] array) {
		final int length = array.length;
		final byte b = array[0];
		for (int i = 1; i < length; ++i) {
			array[i - 1] = array[i];
		}
		array[length - 1] = b;
		return array;
	}

	private static int[] rotateRight(final int[] array) {
		final int length = array.length;
		final int n = array[0];
		for (int i = 1; i < length; ++i) {
			array[i - 1] = array[i];
		}
		array[length - 1] = n;
		return array;
	}

	public static byte[] twoBytes(final byte[] array, final int n) {
		return getBytes(array, n, 2);
	}

	public static short uByte(final byte b) {
		return (short) ((b + 256) % 256);
	}

	public static long uWord(final int n) {
		final long n2 = 65536L * 65536L;
		return (n + n2) % n2;
	}

	public static byte[] wordToBytes(final long n) {
		return new byte[]{(byte) (n % 256L), (byte) ((n >> 8) % 256L), (byte) ((n >> 16) % 256L), (byte) (n >> 24)};
	}
}
