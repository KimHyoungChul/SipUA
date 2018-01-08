package org.zoolu.tools;

public class SimpleDigest extends MessageDigest {
	byte add_term;
	int index;
	boolean is_done;
	byte[] message_digest;

	public SimpleDigest(final int n) {
		this.init(n);
	}

	public SimpleDigest(final int n, final String s) {
		this.init(n);
		this.update(s);
	}

	public SimpleDigest(final int n, final byte[] array) {
		this.init(n);
		this.update(array);
	}

	public SimpleDigest(final int n, final byte[] array, final int n2, final int n3) {
		this.init(n);
		this.update(array, n2, n3);
	}

	public static byte[] digest(final int n, final String s) {
		return new SimpleDigest(n, s).doFinal();
	}

	public static byte[] digest(final int n, final byte[] array) {
		return digest(n, array, 0, array.length);
	}

	public static byte[] digest(final int n, final byte[] array, final int n2, final int n3) {
		return new SimpleDigest(n, array, n2, n3).doFinal();
	}

	private void init(final int n) {
		this.is_done = false;
		this.message_digest = new byte[n];
		for (int i = 0; i < n; ++i) {
			this.message_digest[i] = (byte) i;
		}
		this.index = 0;
		this.add_term = 0;
	}

	@Override
	public byte[] doFinal() {
		if (this.is_done) {
			return this.message_digest;
		}
		int n = this.message_digest.length - this.index;
		while (this.index < this.message_digest.length) {
			this.message_digest[this.index] ^= (byte) n;
			++this.index;
			++n;
		}
		for (int i = 0; i < this.message_digest.length; ++i) {
			this.message_digest[i] ^= this.add_term;
		}
		return this.message_digest;
	}

	@Override
	public MessageDigest update(final byte[] array, final int n, final int n2) {
		if (!this.is_done) {
			for (int i = 0; i < n2; ++i) {
				if (this.index == this.message_digest.length) {
					this.index = 0;
				}
				this.add_term += array[n + i];
				this.message_digest[this.index] ^= this.add_term;
				++this.index;
			}
		}
		return this;
	}
}
