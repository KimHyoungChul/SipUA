package com.zed3.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;

public class GKBufferAutoFlushWriter extends GKFileAppender {
	protected static final int TIME_DELAY = 500;
	private Handler handler;
	private HandlerThread handlerThread;
	protected Runnable runnable = new C09941();

	class C09941 implements Runnable {
		C09941() {
		}

		public void run() {
			GKBufferAutoFlushWriter.this.qw.flush();
			Log.i("GUOK", "thread name:" + Thread.currentThread().getName());
		}
	}

	public GKBufferAutoFlushWriter() throws IOException {
		initHandler();
	}

	public GKBufferAutoFlushWriter(String tag) throws IOException {
		super(tag);
		initHandler();
	}

	public GKBufferAutoFlushWriter(GKLayout layout, String filename, boolean isAppend) throws IOException {
		super(layout, filename, isAppend);
		initHandler();
	}

	public GKBufferAutoFlushWriter(String tag, GKLayout layout, String filename, boolean isAppend) throws IOException {
		super(tag, layout, filename, isAppend);
		initHandler();
	}

	public GKBufferAutoFlushWriter(GKLayout layout, String filename) throws IOException {
		super(layout, filename);
		initHandler();
	}

	public GKBufferAutoFlushWriter(String tag, GKLayout layout, String filename) throws IOException {
		super(tag, layout, filename);
		initHandler();
	}

	public GKBufferAutoFlushWriter(GKLayout layout) throws IOException {
		super(layout);
		initHandler();
	}

	public GKBufferAutoFlushWriter(String tag, GKLayout layout) throws IOException {
		super(tag, layout);
		initHandler();
	}

	private void initHandler() {
		this.handlerThread = new HandlerThread("GKBufferAutoFlushWriter");
		this.handlerThread.start();
		this.handler = new Handler(this.handlerThread.getLooper());
	}

	public void append(GKLoggingEvent event) {
		super.append(event);
		if (this.bufferedIO) {
			this.handler.removeCallbacks(this.runnable);
			this.handler.postAtTime(this.runnable, 500);
		}
	}

	public HandlerThread getHandlerThread() {
		return this.handlerThread;
	}

	public Handler getHandler() {
		return this.handler;
	}

	public void setNull() {
		this.handlerThread.quit();
		this.handler = null;
	}
}
