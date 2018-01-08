package com.zed3.h264_fu_process;

import com.zed3.log.MyLog;
import com.zed3.net.RtpPacket;

public class H264FUManager {
	private long endBit = 0;
	private int eyePayloadLen = 0;
	private int eyeSeqNum = 0;
	private long eyeTimeStamp = 0;
	private int fuPayloadLen = 0;
	private int fuSeqNum = 0;
	private long fuTimeStamp = 0;
	private FU sigFU2 = new FU();
	private FU sigFu = new FU();
	private FU sigFuEyebeam = new FU();
	private long startBit = 0;
	private String tag = "H264FUManager";

	public void clearFus() {
		if (this.sigFu != null) {
			this.sigFu.init(0);
		}
		if (this.sigFuEyebeam != null) {
			this.sigFuEyebeam.init(0);
		}
	}

	public FU processFU(RtpPacket rtppack) {
		MyLog.i("processFU", "begin:");
		try {
			byte[] pPayload = rtppack.getPayload();
			this.startBit = (long) (pPayload[1] & 128);
			this.endBit = (long) (pPayload[1] & 64);
			FU pfuGet = null;
			this.fuTimeStamp = rtppack.getTimestamp();
			this.fuSeqNum = rtppack.getSequenceNumber();
			this.fuPayloadLen = rtppack.getPayloadLength();
			FU fu;
			if (this.startBit != 0) {
				if (this.sigFu.packetState == 2) {
					this.sigFu.init(this.fuSeqNum);
				} else if (this.sigFu.packetState == 1 && this.sigFu.timeStamp < this.fuTimeStamp) {
					fuCopy(this.sigFu, this.sigFU2);
					fu = this.sigFU2;
					fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFU2.seqNumReconstruct + 2), this.fuSeqNum);
					this.sigFU2.totalCount = CalcDiffOfTwoSequence(this.sigFU2.firstSeqNum, this.fuSeqNum);
					pfuGet = this.sigFU2;
					if (pfuGet.getDataLen() == 0) {
						MyLog.i("fu_test", "p 1");
					}
					this.sigFu.init(this.fuSeqNum);
				}
				this.sigFu.timeStamp = this.fuTimeStamp;
				this.sigFu.seqNumOrig = (long) this.fuSeqNum;
				this.sigFu.seqNumReconstruct = this.sigFu.seqNumOrig;
				this.sigFu.data[0] = (byte) ((pPayload[0] & 224) + (pPayload[1] & 31));
				fu = this.sigFu;
				fu.len++;
				System.arraycopy(pPayload, 2, this.sigFu.data, (int) this.sigFu.len, rtppack.getPayloadLength() - 2);
				this.sigFu.packetState = 1;
				fu = this.sigFu;
				fu.len += (long) (this.fuPayloadLen - 2);
			} else if (this.sigFu.timeStamp == this.fuTimeStamp) {
				if (!(this.sigFu.seqNumReconstruct + 1 == ((long) this.fuSeqNum) || this.fuSeqNum == 0)) {
					fu = this.sigFu;
					fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFu.seqNumReconstruct + 1), this.fuSeqNum);
				}
				System.arraycopy(pPayload, 2, this.sigFu.data, (int) this.sigFu.len, this.fuPayloadLen - 2);
				this.sigFu.seqNumReconstruct = (long) this.fuSeqNum;
				fu = this.sigFu;
				fu.len += (long) (this.fuPayloadLen - 2);
				if (this.endBit != 0 || rtppack.hasMarker()) {
					this.sigFu.packetState = 2;
					this.sigFu.totalCount = CalcDiffOfTwoSequence(this.sigFu.firstSeqNum, this.fuSeqNum + 1);
					pfuGet = this.sigFu;
					if (pfuGet.getDataLen() == 0) {
						MyLog.i("fu_test", "p 2");
					}
				}
			} else {
				if (this.sigFu.packetState == 1) {
					fuCopy(this.sigFu, this.sigFU2);
					fu = this.sigFU2;
					fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFU2.seqNumReconstruct + 2), this.fuSeqNum);
					this.sigFU2.totalCount = CalcDiffOfTwoSequence(this.sigFU2.firstSeqNum, this.fuSeqNum);
					pfuGet = this.sigFU2;
					if (pfuGet.getDataLen() == 0) {
						MyLog.i("fu_test", "p 3");
					}
				}
				if (this.fuSeqNum > 0) {
					this.sigFu.init(this.fuSeqNum - 1);
				} else {
					this.sigFu.init(this.fuSeqNum);
				}
				this.sigFu.timeStamp = this.fuTimeStamp;
				this.sigFu.seqNumOrig = (long) this.fuSeqNum;
				this.sigFu.seqNumReconstruct = this.sigFu.seqNumOrig;
				this.sigFu.lostCount = 1;
				this.sigFu.data[0] = (byte) ((pPayload[0] & 224) + (pPayload[1] & 31));
				fu = this.sigFu;
				fu.len++;
				System.arraycopy(pPayload, 2, this.sigFu.data, (int) this.sigFu.len, rtppack.getPayloadLength() - 2);
				this.sigFu.packetState = 1;
				fu = this.sigFu;
				fu.len += (long) (this.fuPayloadLen - 2);
			}
			if (!rtppack.hasMarker()) {
				return pfuGet;
			}
			this.sigFu.packetState = 2;
			this.sigFu.totalCount = CalcDiffOfTwoSequence(this.sigFu.firstSeqNum, this.fuSeqNum + 1);
			pfuGet = this.sigFu;
			if (pfuGet.getDataLen() != 0) {
				return pfuGet;
			}
			MyLog.i("fu_test", "p 4");
			return pfuGet;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public FU processFU4Eyebeam(RtpPacket rtppack) {
		try {
			byte[] pPayload = rtppack.getPayload();
			long startBit = (long) (pPayload[1] & 128);
			boolean endBit = rtppack.hasMarker();
			this.eyeTimeStamp = rtppack.getTimestamp();
			this.eyeSeqNum = rtppack.getSequenceNumber();
			this.eyePayloadLen = rtppack.getPayloadLength();
			FU pfuGet = null;
			byte[] eyeBeamhead = new byte[4];
			eyeBeamhead[3] = (byte) 1;
			FU fu;
			if (startBit != 0) {
				if (this.sigFuEyebeam.packetState == 2) {
					this.sigFuEyebeam.init(this.eyeSeqNum);
				} else if (this.sigFuEyebeam.timeStamp < this.eyeTimeStamp) {
					fuCopy(this.sigFuEyebeam, this.sigFU2);
					fu = this.sigFU2;
					fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFU2.seqNumReconstruct + 2), this.eyeSeqNum);
					this.sigFU2.totalCount = CalcDiffOfTwoSequence(this.sigFU2.firstSeqNum, this.eyeSeqNum);
					pfuGet = this.sigFU2;
					this.sigFuEyebeam.init(this.eyeSeqNum);
				}
				this.sigFuEyebeam.timeStamp = this.eyeTimeStamp;
				this.sigFuEyebeam.seqNumOrig = (long) this.eyeSeqNum;
				this.sigFuEyebeam.seqNumReconstruct = this.sigFuEyebeam.seqNumOrig;
				System.arraycopy(pPayload, 0, this.sigFuEyebeam.data, (int) this.sigFuEyebeam.len, this.eyePayloadLen);
				fu = this.sigFuEyebeam;
				fu.len += (long) this.eyePayloadLen;
				this.sigFuEyebeam.packetState = 1;
				MyLog.i(this.tag, "startBit != 0 pfu len:" + this.sigFuEyebeam.len + " seqnum:" + this.sigFuEyebeam.seqNumOrig);
			} else {
				MyLog.i(this.tag, "it.timeStamp--> " + this.sigFuEyebeam.timeStamp + " it.seqnum--->" + this.sigFuEyebeam.seqNumOrig);
				if (this.sigFuEyebeam.timeStamp != this.eyeTimeStamp) {
					if (this.sigFuEyebeam.packetState == 1) {
						fuCopy(this.sigFuEyebeam, this.sigFU2);
						fu = this.sigFU2;
						fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFU2.seqNumReconstruct + 2), this.eyeSeqNum);
						this.sigFU2.totalCount = CalcDiffOfTwoSequence(this.sigFU2.firstSeqNum, this.eyeSeqNum);
						pfuGet = this.sigFU2;
					}
					if (this.eyeSeqNum > 0) {
						this.sigFuEyebeam.init(this.eyeSeqNum - 1);
					} else {
						this.sigFuEyebeam.init(this.eyeSeqNum);
					}
					this.sigFuEyebeam.lostCount = 1;
					this.sigFuEyebeam.timeStamp = this.eyeTimeStamp;
					this.sigFuEyebeam.seqNumOrig = (long) this.eyeSeqNum;
					this.sigFuEyebeam.seqNumReconstruct = this.sigFuEyebeam.seqNumOrig;
					System.arraycopy(pPayload, 0, this.sigFuEyebeam.data, (int) this.sigFuEyebeam.len, this.eyePayloadLen);
					this.sigFuEyebeam.packetState = 1;
					fu = this.sigFuEyebeam;
					fu.len += (long) this.eyePayloadLen;
					MyLog.i(this.tag, "startBit != 0 pfu len:" + this.sigFuEyebeam.len + " seqnum:" + this.sigFuEyebeam.seqNumOrig);
				} else if (this.sigFuEyebeam.timeStamp == this.eyeTimeStamp) {
					MyLog.i(this.tag, "it.seqNumReconstruct --->" + this.sigFuEyebeam.seqNumReconstruct + " rtppack.getSequenceNumber()--->" + this.eyeSeqNum);
					if (!(this.sigFuEyebeam.seqNumReconstruct + 1 == ((long) this.eyeSeqNum) || this.eyeSeqNum == 0)) {
						fu = this.sigFuEyebeam;
						fu.lostCount += CalcDiffOfTwoSequence((int) (this.sigFuEyebeam.seqNumReconstruct + 1), this.eyeSeqNum);
					}
					System.arraycopy(eyeBeamhead, 0, this.sigFuEyebeam.data, (int) this.sigFuEyebeam.len, 4);
					fu = this.sigFuEyebeam;
					fu.len += 4;
					System.arraycopy(pPayload, 0, this.sigFuEyebeam.data, (int) this.sigFuEyebeam.len, this.eyePayloadLen);
					this.sigFuEyebeam.seqNumReconstruct = (long) this.eyeSeqNum;
					fu = this.sigFuEyebeam;
					fu.len += (long) this.eyePayloadLen;
					if (endBit) {
						this.sigFuEyebeam.packetState = 2;
						this.sigFuEyebeam.totalCount = CalcDiffOfTwoSequence(this.sigFuEyebeam.firstSeqNum, this.eyeSeqNum + 1);
					}
				}
			}
			if (!rtppack.hasMarker()) {
				return pfuGet;
			}
			this.sigFuEyebeam.packetState = 2;
			this.sigFuEyebeam.totalCount = CalcDiffOfTwoSequence(this.sigFuEyebeam.firstSeqNum, this.eyeSeqNum + 1);
			return this.sigFuEyebeam;
		} catch (Exception e) {
			MyLog.e("processFU4Eyebeam error", e.toString());
			e.printStackTrace();
			return null;
		}
	}

	void fuCopy(FU f1, FU f2) {
		if (f1 != null && f2 != null) {
			f2.packetState = f1.packetState;
			f2.len = f1.len;
			f2.lostCount = f1.lostCount;
			f2.totalCount = f1.totalCount;
			f2.firstSeqNum = f1.firstSeqNum;
			f2.seqNumOrig = f1.seqNumOrig;
			f2.seqNumReconstruct = f1.seqNumReconstruct;
			f2.timeStamp = f1.timeStamp;
			f2.type = f1.type;
			System.arraycopy(f1.data, 0, f2.data, 0, f1.data.length);
		}
	}

	int CalcDiffOfTwoSequence(int FirstSeq, int LastSeq) {
		if (FirstSeq < 0 || FirstSeq > 65535) {
			FirstSeq = 65535;
		}
		if (LastSeq < 0 || LastSeq > 65535) {
			LastSeq = 65535;
		}
		if (LastSeq >= FirstSeq) {
			return LastSeq - FirstSeq;
		}
		return (65535 - FirstSeq) + LastSeq;
	}
}
