package com.zed3.h264_fu_process;

public class FUIndicator {
	private byte f846F;
	private byte NRI;
	private byte TYPE;
	private byte f847b;

	FUIndicator() {
		this.f847b = (byte) 0;
	}

	FUIndicator(byte b) {
		this.f847b = b;
	}

	public byte getTYPE() {
		return FUUtils.getType(this.f847b);
	}

	public void setTYPE(byte tYPE) {
		this.f847b = FUUtils.setType(this.f847b, tYPE);
	}

	public byte getNRI() {
		return FUUtils.getNri(this.f847b);
	}

	public void setNRI(byte nRI) {
		this.f847b = FUUtils.setNri(this.f847b, nRI);
	}

	public byte getF() {
		byte f = FUUtils.getF(this.f847b);
		this.f846F = f;
		return f;
	}

	public void setF(byte f) {
		this.f846F = f;
		this.f847b = FUUtils.setF(this.f847b, f);
	}

	public byte getByte() {
		return this.f847b;
	}
}
