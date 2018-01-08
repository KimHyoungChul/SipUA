package com.zed3.h264_fu_process;

public class FU {
	public static final int PACKET_STATE_COMPLETE = 2;
	public static final int PACKET_STATE_CONTINUE = 1;
	public static final int PACKET_STATE_INIT = 0;
	byte[] data;
	public int firstSeqNum;
	long len;
	public int lostCount;
	int packetState;
	long seqNumOrig;
	long seqNumReconstruct;
	long timeStamp;
	public int totalCount;
	FuType type;

	public FU() {
		this.data = new byte[1843200];
		this.packetState = 0;
		this.type = FuType.FU_TYPE_INVALID;
		this.timeStamp = 0L;
		this.seqNumOrig = 0L;
		this.seqNumReconstruct = 0L;
		this.firstSeqNum = 0;
		this.totalCount = 0;
		this.lostCount = 0;
		this.len = 0L;
		this.packetState = 0;
		if (this.len > 0L) {
			for (int i = 0; i < this.data.length; ++i) {
				this.data[i] = 0;
			}
		}
	}

	public byte[] getData() {
		return this.data;
	}

	public long getDataLen() {
		return this.len;
	}

	public void init(int i) {
		this.type = FuType.FU_TYPE_INVALID;
		this.timeStamp = 0L;
		this.seqNumOrig = 0L;
		this.seqNumReconstruct = 0L;
		this.lostCount = 0;
		this.firstSeqNum = i;
		this.totalCount = 0;
		this.len = 0L;
		this.packetState = 0;
		if (this.len > 0L) {
			for (i = 0; i < this.data.length; ++i) {
				this.data[i] = 0;
			}
		}
	}
}
