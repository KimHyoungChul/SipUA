package com.zed3.video;

public class ReceivePacketInfo {
	private byte[] data;
	private int length;
	private int seqNum;
	private long timeStamp;

	public ReceivePacketInfo(final int seqNum, final byte[] data, final long timeStamp) {
		this.seqNum = seqNum;
		this.data = data;
		this.setTimeStamp(timeStamp);
	}

	public ReceivePacketInfo(final int seqNum, final byte[] array, final long timeStamp, final int length) {
		this.seqNum = seqNum;
		this.data = array.clone();
		this.setTimeStamp(timeStamp);
		this.setLength(length);
	}

	public byte[] getData() {
		return this.data;
	}

	public int getLength() {
		return this.length;
	}

	public int getSeqNum() {
		return this.seqNum;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public void setData(final byte[] data) {
		this.data = data;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public void setSeqNum(final int seqNum) {
		this.seqNum = seqNum;
	}

	public void setTimeStamp(final long timeStamp) {
		this.timeStamp = timeStamp;
	}
}
