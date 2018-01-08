package org.zoolu.tools;

public class Assert {
	public static final void isFalse(final boolean b) {
		if (b) {
			onError("Assertion failed");
		}
	}

	public static final void isFalse(final boolean b, final String s) {
		if (b) {
			onError("Assertion failed: " + s);
		}
	}

	public static final void isTrue(final boolean b) {
		if (!b) {
			onError("Assertion failed");
		}
	}

	public static final void isTrue(final boolean b, final String s) {
		if (!b) {
			onError("Assertion failed: " + s);
		}
	}

	private static void onError(final String s) {
		throw new AssertException(s);
	}
}
