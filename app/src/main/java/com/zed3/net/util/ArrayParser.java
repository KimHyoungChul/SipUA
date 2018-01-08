package com.zed3.net.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ArrayParser {
	public static short[] byteArray2ShortArray(final byte[] array) {
		final short[] array2 = new short[array.length / 2];
		ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(array2);
		return array2;
	}

	public static byte[] shortArray2ByteArray(final short[] array) {
		final byte[] array2 = new byte[array.length * 2];
		ByteBuffer.wrap(array2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array);
		return array2;
	}
}
