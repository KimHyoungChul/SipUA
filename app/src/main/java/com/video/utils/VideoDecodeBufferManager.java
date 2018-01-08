package com.video.utils;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class VideoDecodeBufferManager {
	private Collection h264Queue;
	private final VideoDecodeThread h264Thread;
	private Future<?> h264ThreadFuture;
	private Collection packetQueue;
	private final VideoDecodeThread packetThread;
	private Future<?> packetThreadFuture;
	private final ExecutorService threadPool;

	public VideoDecodeBufferManager(final Collection packetQueue, final Collection h264Queue, final VideoDecodeThread packetThread, final VideoDecodeThread h264Thread) {
		this.threadPool = Executors.newCachedThreadPool();
		this.packetQueue = packetQueue;
		this.h264Queue = h264Queue;
		this.packetThread = packetThread;
		this.h264Thread = h264Thread;
	}

	public void startThreads() {
		this.packetThreadFuture = this.threadPool.submit(this.packetThread);
		this.h264ThreadFuture = this.threadPool.submit(this.h264Thread);
	}

	public void stopThreads() {
		this.threadPool.shutdownNow();
		try {
			this.threadPool.awaitTermination(50L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			this.packetThreadFuture.cancel(true);
			this.h264ThreadFuture.cancel(true);
		}
	}
}
