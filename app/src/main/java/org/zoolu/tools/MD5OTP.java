package org.zoolu.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MD5OTP {
	static int size;
	byte[] h;
	int index;
	byte[] skey;

	public MD5OTP(final byte[] array) {
		this.init(16, array, null);
	}

	public MD5OTP(final byte[] array, final byte[] array2) {
		this.init(16, array, array2);
	}

	private static String asHex(final byte[] array) {
		final StringBuffer sb = new StringBuffer(array.length * 2);
		for (int i = 0; i < array.length; ++i) {
			if ((array[i] & 0xFF) < 16) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(array[i] & 0xFF));
		}
		return sb.toString();
	}

	private static byte[] cat(final byte[] array, final byte[] array2) {
		final byte[] array3 = new byte[array.length + array2.length];
		for (int i = 0; i < array.length; ++i) {
			array3[i] = array[i];
		}
		for (int j = 0; j < array2.length; ++j) {
			array3[array.length + j] = array2[j];
		}
		return array3;
	}

	private static byte[] clone(final byte[] array) {
		final byte[] array2 = new byte[array.length];
		for (int i = 0; i < array.length; ++i) {
			array2[i] = array[i];
		}
		return array2;
	}

	public static byte[] decrypt(byte[] sub, final byte[] array) {
		final byte[] sub2 = sub(sub, 0, 16);
		sub = sub(sub, 16, sub.length);
		return new MD5OTP(array, sub2).update(sub);
	}

	public static byte[] encrypt(final byte[] array, final byte[] array2) {
		final byte[] digest = MD5.digest(Long.toString(new Random().nextLong()));
		return cat(digest, new MD5OTP(array2, digest).update(array));
	}

	private static byte[] hash(final byte[] array) {
		return MD5.digest(array);
	}

	private void init(final int size, final byte[] skey, final byte[] array) {
		MD5OTP.size = size;
		byte[] array2 = array;
		if (array == null) {
			array2 = new byte[size];
			for (int i = 0; i < size; ++i) {
				array2[i] = 0;
			}
		}
		this.skey = skey;
		this.h = clone(array2);
		this.index = size - 1;
	}

	public static void main(final String[] array) {
		if (array.length < 2) {
			System.out.println("Usage:\n\n   java MD5OTP <message> <pass_phrase> [<iv>]");
			System.exit(0);
		}
		final byte[] bytes = array[0].getBytes();
		final byte[] bytes2 = array[1].getBytes();
		byte[] bytes3 = null;
		if (array.length > 2) {
			bytes3 = array[2].getBytes();
		}
		System.out.println("m= " + asHex(bytes) + " (" + new String(bytes) + ")");
		final byte[] update = new MD5OTP(bytes2, bytes3).update(bytes);
		System.out.println("c= " + asHex(update));
		final byte[] update2 = new MD5OTP(bytes2, bytes3).update(update);
		System.out.println("m= " + asHex(update2) + " (" + new String(update2) + ")");
	}

	private static byte[] sub(final byte[] array, final int n, final int n2) {
		final byte[] array2 = new byte[n2 - n];
		for (int i = n; i < n2; ++i) {
			array2[i - n] = array[i];
		}
		return array2;
	}

	public void update(final InputStream inputStream, final OutputStream outputStream) {
		final byte[] array = new byte[2048];
		try {
			while (true) {
				final int read = inputStream.read(array);
				if (read <= 0) {
					break;
				}
				outputStream.write(this.update(sub(array, 0, read)));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public byte[] update(final byte[] array) {
		final byte[] array2 = new byte[array.length];
		for (int i = 0; i < array.length; ++i) {
			++this.index;
			if (this.index == MD5OTP.size) {
				this.h = hash(cat(this.skey, this.h));
				this.index = 0;
			}
			array2[i] = (byte) (array[i] ^ this.h[this.index]);
		}
		return array2;
	}
}
