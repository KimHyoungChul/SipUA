package com.zed3.sipua;

import com.zed3.log.MyLog;

import java.util.LinkedList;
import java.util.Queue;

public class SyncBufferSendQueue {
	private Queue<byte[]> storage;
	byte[] val;

	public SyncBufferSendQueue() {
		this.storage = new LinkedList<byte[]>();
		this.val = null;
	}

	public byte[] pop() throws InterruptedException {
		synchronized (this) {
			while (this.storage.size() == 0) {
				try {
					this.wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			this.notify();
			this.val = this.storage.poll();
			if (this.val == null) {
				MyLog.e("SyncBufferSendQueue", "buffer is null");
			}
			MyLog.i("SyncBufferSendQueue", "pop count is:" + this.storage.size());
			return this.val;
		}
	}

	public void push(final byte[] array) throws InterruptedException {
		synchronized (this) {
			if (this.storage.size() == 200) {
				this.storage.clear();
			}
			this.storage.offer(array);
			MyLog.i("SyncBufferSendQueue", "push count is :" + this.storage.size());
			this.notify();
		}
	}
}
