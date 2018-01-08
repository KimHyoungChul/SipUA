package com.zed3.log;

public class GKLoggingEvent {
	public GKLevel level;
	private Object message;
	private String tag;
	private String threadName;
	public final long timeStamp;

	public GKLoggingEvent(GKLevel level, Object message) {
		this.level = level;
		this.message = message;
		this.timeStamp = System.currentTimeMillis();
		this.tag = null;
	}

	public GKLoggingEvent(String tag, GKLevel level, Object message) {
		this(level, message);
		this.tag = tag;
	}

	public String getThreadName() {
		if (this.threadName == null) {
			this.threadName = Thread.currentThread().getName();
		}
		return this.threadName;
	}

	public GKLevel getLevel() {
		return this.level;
	}

	public void setLevel(GKLevel level) {
		this.level = level;
	}

	public Object getMessage() {
		return this.message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public String getTag() {
		return this.tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
