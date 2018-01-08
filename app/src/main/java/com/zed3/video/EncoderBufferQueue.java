package com.zed3.video;

import com.zed3.log.MyLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EncoderBufferQueue {
	private BlockingQueue<YUVData> storage;
	YUVData val;

	public EncoderBufferQueue() {
		this.storage = new LinkedBlockingQueue<YUVData>();
		this.val = null;
	}

	public void clear() {
		this.storage.clear();
	}

	public YUVData pop() throws InterruptedException {
		MyLog.i("BlockingQueue", "pop called!!!" + this.storage.size());
		return this.val = this.storage.take();
	}

	public void push(final YUVData yuvData) throws InterruptedException {
		MyLog.i("BlockingQueue", "push called!!!" + this.storage.size());
		if (!this.storage.offer(yuvData)) {
			MyLog.i("SyncQueue", "push count is :" + this.storage.size());
		}
	}

	public int size() {
		return this.storage.size();
	}
}
