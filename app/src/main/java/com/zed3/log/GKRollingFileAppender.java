package com.zed3.log;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class GKRollingFileAppender extends GKBufferAutoFlushWriter {
	protected long maxFileSize = 3145728;

	public GKRollingFileAppender(String tag) throws IOException {
		super(tag);
	}

	public GKRollingFileAppender(GKLayout layout, String filename, boolean isAppend) throws IOException {
		super(layout, filename, isAppend);
	}

	public GKRollingFileAppender(String tag, GKLayout layout, String filename, boolean isAppend) throws IOException {
		super(tag, layout, filename, isAppend);
	}

	public GKRollingFileAppender(GKLayout layout, String filename) throws IOException {
		super(layout, filename);
	}

	public GKRollingFileAppender(String tag, GKLayout layout, String filename) throws IOException {
		super(tag, layout, filename);
	}

	public GKRollingFileAppender(GKLayout layout) throws IOException {
		super(layout);
	}

	public GKRollingFileAppender(String tag, GKLayout layout) throws IOException {
		super(tag, layout);
	}

	public synchronized void rollOver() {
		String filename = GKFileUtils.fatchFullFileName();
		try {
			reset();
			setFile(filename, true, this.bufferedIO, this.bufferSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void setQWForFiles(Writer writer) {
		this.qw = new GKCountingQuietWriter(writer);
	}

	public synchronized void setFile(String filename, boolean isAppend, boolean bufferedIO, int bufferSize) throws IOException {
		super.setFile(filename, isAppend, bufferedIO, bufferSize);
		if (isAppend) {
			((GKCountingQuietWriter) this.qw).setCount(new File(filename).length());
		}
	}

	public void append(GKLoggingEvent event) {
		super.append(event);
		if (this.fileName != null && this.qw != null && ((GKCountingQuietWriter) this.qw).getCount() >= this.maxFileSize) {
			rollOver();
		}
	}
}
