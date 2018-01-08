package org.zoolu.tools;

public abstract class MessageDigest {
	public static String asHex(final byte[] array) {
		String string = new String();
		for (int i = 0; i < array.length; ++i) {
			string = String.valueOf(new StringBuilder(String.valueOf(string)).append(Integer.toHexString(array[i] >>> 4 & 0xF)).toString()) + Integer.toHexString(array[i] & 0xF);
		}
		return string;
	}

	public String asHex() {
		return asHex(this.doFinal());
	}

	public abstract byte[] doFinal();

	public byte[] getDigest() {
		return this.doFinal();
	}

	public MessageDigest update(final String s) {
		final byte[] bytes = s.getBytes();
		return this.update(bytes, 0, bytes.length);
	}

	public MessageDigest update(final byte[] array) {
		return this.update(array, 0, array.length);
	}

	public abstract MessageDigest update(final byte[] p0, final int p1, final int p2);
}
