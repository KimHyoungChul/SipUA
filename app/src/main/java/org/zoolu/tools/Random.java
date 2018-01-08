package org.zoolu.tools;

public class Random {
	static java.util.Random rand;
	static final long seed;

	static {
		seed = System.currentTimeMillis();
		Random.rand = new java.util.Random(Random.seed);
	}

	public static boolean nextBoolean() {
		return Random.rand.nextInt(2) == 1;
	}

	public static byte[] nextBytes(final int n) {
		final byte[] array = new byte[n];
		for (int i = 0; i < n; ++i) {
			array[i] = (byte) nextInt(256);
		}
		return array;
	}

	public static String nextHexString(final int n) {
		final byte[] array = new byte[n];
		for (int i = 0; i < n; ++i) {
			final int nextInt = nextInt(16);
			int n2;
			if (nextInt < 10) {
				n2 = nextInt + 48;
			} else {
				n2 = nextInt + 87;
			}
			array[i] = (byte) n2;
		}
		return new String(array);
	}

	public static int nextInt() {
		return Random.rand.nextInt();
	}

	public static int nextInt(final int n) {
		return Math.abs(Random.rand.nextInt()) % n;
	}

	public static long nextLong() {
		return Random.rand.nextLong();
	}

	public static String nextNumString(final int n) {
		final byte[] array = new byte[n];
		for (int i = 0; i < n; ++i) {
			array[i] = (byte) (nextInt(10) + 48);
		}
		return new String(array);
	}

	public static String nextString(final int n) {
		final byte[] array = new byte[n];
		for (int i = 0; i < n; ++i) {
			final int nextInt = nextInt(62);
			int n2;
			if (nextInt < 10) {
				n2 = nextInt + 48;
			} else if (nextInt < 36) {
				n2 = nextInt + 55;
			} else {
				n2 = nextInt + 61;
			}
			array[i] = (byte) n2;
		}
		return new String(array);
	}

	public static void setSeed(final long seed) {
		Random.rand.setSeed(seed);
	}
}
