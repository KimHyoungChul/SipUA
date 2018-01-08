package com.zed3.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TrackPlayQueue {
	private BlockingQueue<byte[]> storage;

	public TrackPlayQueue() {
		this.storage = new LinkedBlockingQueue<byte[]>(500);
	}

	public static byte[] shortArray2ByteArray(final short[] array) {
		final byte[] array2 = new byte[array.length * 2];
		ByteBuffer.wrap(array2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(array);
		return array2;
	}

	public short[] byteArray2ShortArray(final byte[] array) {
		final short[] array2 = new short[array.length / 2];
		ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(array2);
		return array2;
	}

	public void clear() {
		if (!this.storage.isEmpty()) {
			this.storage.clear();
		}
	}

	public short[] pop() throws InterruptedException {
		final byte[] array = this.storage.poll(20L, TimeUnit.MILLISECONDS);
		if (array == null) {
			return null;
		}
		return this.byteArray2ShortArray(array);
	}

	public void push(final short[] array) throws InterruptedException {
		this.storage.offer(shortArray2ByteArray(array));
	}
}
