package com.zed3.log;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class GKWriterAppender implements IAppender {
	protected String encoding;
	protected boolean immediateFlush;
	private GKLayout layout;
	protected GKQuietWriter qw;
	protected final ReentrantReadWriteLock readWriteLock;
	protected volatile String tag;

	public GKWriterAppender() {
		this.immediateFlush = true;
		this.readWriteLock = new ReentrantReadWriteLock();
	}

	public GKWriterAppender(GKLayout layout, OutputStream outputStream) {
		this(layout, new GKOutputStreamWriter(outputStream));
	}

	public GKWriterAppender(GKLayout layout, Writer writer) {
		this.immediateFlush = true;
		this.readWriteLock = new ReentrantReadWriteLock();
		setLayout(layout);
		setWriter(writer);
	}

	public synchronized void close() {
		reset();
	}

	protected GKOutputStreamWriter createWriter(OutputStream outputStream) {
		GKOutputStreamWriter writer = null;
		String enc = getEncoding();
		if (enc != null) {
			try {
				writer = new GKOutputStreamWriter(outputStream, enc);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		if (writer == null) {
			return new GKOutputStreamWriter(outputStream);
		}
		return writer;
	}

	public synchronized void setWriter(Writer writer) {
		reset();
		this.qw = new GKQuietWriter(writer);
		writeHeader();
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	protected void writeHeader() {
	}

	private void closeWriter() {
		if (this.qw != null) {
			this.qw.close();
		}
	}

	protected void reset() {
		closeWriter();
		this.qw = null;
	}

	public void append(GKLoggingEvent event) {
		if (event.getTag() == null) {
			event.setTag(getTag());
		}
		this.readWriteLock.readLock().lock();
		try {
			String str = this.layout.format(event);
			synchronized (this) {
				this.qw.write(str);
				if (this.immediateFlush) {
					this.qw.flush();
				}
			}
		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

	public void setLayout(GKLayout layout) {
		this.readWriteLock.writeLock().lock();
		try {
			this.layout = layout;
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
	}

	public GKLayout getLayout() {
		return this.layout;
	}

	public boolean isImmediateFlush() {
		return this.immediateFlush;
	}

	public void setImmediateFlush(boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public String getTag() {
		this.readWriteLock.readLock().lock();
		try {
			String str = this.tag;
			return str;
		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

	public void setTag(String tag) {
		this.readWriteLock.writeLock().lock();
		try {
			this.tag = tag;
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
	}
}
