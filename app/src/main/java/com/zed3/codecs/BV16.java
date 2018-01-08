package com.zed3.codecs;

class BV16 extends CodecBase implements Codec {
	BV16() {
		this.CODEC_NAME = "BV16";
		this.CODEC_USER_NAME = "BV16";
		this.CODEC_DESCRIPTION = "16kbit";
		this.CODEC_NUMBER = 106;
		this.CODEC_DEFAULT_SETTING = "always";
		super.update();
	}

	@Override
	public void close() {

	}

	@Override
	public int decode(byte[] p0, short[] p1, int p2) {
		return 0;
	}

	@Override
	public int decode(byte[] p0, short[] p1, int p2, int p3) {
		return 0;
	}

	@Override
	public int encode(short[] p0, int p1, byte[] p2, int p3) {
		return 0;
	}

	@Override
	public void init() {
		this.load();
		if (this.isLoaded()) {
			this.open();
		}
	}

	@Override
	void load() {
		try {
			System.loadLibrary("bv16_jni");
			super.load();
		} catch (Throwable t) {
		}
	}

	//  TODO
	public int open() {
		return 0;
	}
}
