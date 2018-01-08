package com.zed3.sipua;

import com.zed3.log.MyLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataSliceQueue {
	private BlockingQueue<EncodeSlice> storage;

	public DataSliceQueue() {
		this.storage = new LinkedBlockingQueue<EncodeSlice>(2500);
	}

	public EncodeSlice pop() throws InterruptedException {
		MyLog.i("BlockingQueueEncoderPop", "DataSliceQueue pop called,size = " + this.storage.size());
		return this.storage.take();
	}

	public void push(final EncodeSlice encodeSlice) throws InterruptedException {
		MyLog.i("BlockingQueueEncoder", "DataSliceQueue called,size =" + this.storage.size());
		if (!this.storage.offer(encodeSlice)) {
			MyLog.i("BlockingQueueEncoder", "DataSliceQueue push faild");
		}
	}
}
