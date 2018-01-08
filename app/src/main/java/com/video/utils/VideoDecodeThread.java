package com.video.utils;

import android.util.Log;

import java.util.Collection;

public class VideoDecodeThread extends Thread {
	private static int[] PRIORITY;
	final String Tag;
	final Collection collection;
	int curPriority;

	static {
		VideoDecodeThread.PRIORITY = new int[]{-8, -16, -19};
	}

	public VideoDecodeThread(final String tag, final Collection collection) {
		this.curPriority = 0;
		this.Tag = tag;
		this.collection = collection;
	}

	protected void adjustThread() {
		final int size = this.collection.size();
		Log.e("GUOK", String.valueOf(this.Tag) + " collection.size() " + size);
		if (this.curPriority == 0 && size > 100) {
			Log.e("GUOK", String.valueOf(this.Tag) + " collection.size() " + size + " THREAD_PRIORITY_AUDIO");
			final int[] priority = VideoDecodeThread.PRIORITY;
			final int curPriority = this.curPriority + 1;
			this.curPriority = curPriority;
			android.os.Process.setThreadPriority(priority[curPriority]);
		} else {
			if (this.curPriority == 1 && size > 300) {
				Log.e("GUOK", String.valueOf(this.Tag) + " collection.size() " + size + " THREAD_PRIORITY_URGENT_AUDIO");
				final int[] priority2 = VideoDecodeThread.PRIORITY;
				final int curPriority2 = this.curPriority + 1;
				this.curPriority = curPriority2;
				android.os.Process.setThreadPriority(priority2[curPriority2]);
				return;
			}
			if (this.curPriority >= 1 && size < 30) {
				Log.e("GUOK", String.valueOf(this.Tag) + " collection.size() " + size + " THREAD_PRIORITY_URGENT_DISPLAY");
				this.curPriority = 0;
				android.os.Process.setThreadPriority(VideoDecodeThread.PRIORITY[this.curPriority]);
			}
		}
	}
}
