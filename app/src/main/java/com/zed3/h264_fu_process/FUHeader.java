package com.zed3.h264_fu_process;

public class FUHeader {
	private byte f843E;
	private byte f844R;
	private byte f845S;
	private byte TYPE;
	private byte byteValue;

	FUHeader() {
		this.byteValue = (byte) 0;
	}

	FUHeader(byte byteValue) {
		this.byteValue = byteValue;
	}

	public byte getTYPE() {
		this.TYPE = this.byteValue;
		this.TYPE = FUUtils.getType(this.TYPE);
		return this.TYPE;
	}

	public void setTYPE(byte tYPE) {
		this.byteValue = FUUtils.setType(this.byteValue, tYPE);
	}

	public byte getR() {
		this.f844R = this.byteValue;
		this.f844R = FUUtils.getR(this.f844R);
		return this.f844R;
	}

	public void setR(byte r) {
		this.byteValue = FUUtils.setR(this.byteValue, r);
	}

	public byte getE() {
		this.f843E = this.byteValue;
		this.f843E = FUUtils.getE(this.f843E);
		return this.f843E;
	}

	public void setE(byte e) {
		this.byteValue = FUUtils.setE(this.byteValue, e);
	}

	public byte getS() {
		this.f845S = this.byteValue;
		this.f845S = FUUtils.getS(this.f845S);
		return this.f845S;
	}

	public void setS(byte s) {
		this.byteValue = FUUtils.setS(this.byteValue, s);
	}

	public byte getByte() {
		return this.byteValue;
	}
}
