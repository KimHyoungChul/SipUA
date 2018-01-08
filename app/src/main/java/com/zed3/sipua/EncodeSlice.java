package com.zed3.sipua;

public class EncodeSlice {
	private byte[] buffer;
	private int len;
	private int marker;

	public EncodeSlice(final byte[] array, final int len, final int marker) {
		this.len = len;
		this.buffer = array.clone();
		this.marker = marker;
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public int getLen() {
		return this.len;
	}

	public int getMarker() {
		return this.marker;
	}

	public void setBuffer(final byte[] buffer) {
		this.buffer = buffer;
	}

	public void setLen(final int len) {
		this.len = len;
	}

	public void setMarker(final int marker) {
		this.marker = marker;
	}
}
