package com.zed3.sipua;

import com.zed3.log.MyLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SyncBufferQueue {
	private BlockingQueue<byte[]> storage;

	public SyncBufferQueue() {
		this.storage = new LinkedBlockingQueue<byte[]>(1024);
	}

	public void clear() {
		this.storage.clear();
	}

	public byte[] pop() throws InterruptedException {
		MyLog.i("BlockingQueue", "thread3 pop called,size = " + this.storage.size());
		return this.storage.take();
	}

	public byte[] pop(final long n) throws InterruptedException {
		MyLog.i("BlockingQueue", "thread3 pop called,size = " + this.storage.size());
		return this.storage.poll(n, TimeUnit.MILLISECONDS);
	}

	public void push(final byte[] array) throws InterruptedException {
		MyLog.i("BlockingQueue", "thread2 called,size =" + this.storage.size());
		if (!this.storage.offer(array)) {
			MyLog.i("BlockingQueue", "thread2 push faild");
		}
	}

	public int size() {
		return this.storage.size();
	}
}
