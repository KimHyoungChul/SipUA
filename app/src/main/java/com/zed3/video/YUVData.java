package com.zed3.video;

public class YUVData {
	private byte[] data;
	private long timeStamp;

	public YUVData(final byte[] data, final long timeStamp) {
		this.setData(data);
		this.setTimeStamp(timeStamp);
	}

	public byte[] getData() {
		return this.data;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public void setData(final byte[] data) {
		this.data = data;
	}

	public void setTimeStamp(final long timeStamp) {
		this.timeStamp = timeStamp;
	}
}
