package com.zed3.codecs;

class SILK8 extends CodecBase implements Codec {
	private static final int DEFAULT_COMPLEXITY = 0;

	SILK8() {
		this.CODEC_USER_NAME = "SILK";
		this.CODEC_NAME = "silk8";
		this.CODEC_DESCRIPTION = "6-20kbit";
		this.CODEC_NUMBER = 117;
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
	public int decode(final byte[] array, final short[] array2, final int n, final int n2) {
		throw new RuntimeException("do not use this method\uff01");
	}

	@Override
	public int encode(short[] p0, int p1, byte[] p2, int p3) {
		return 0;
	}

	@Override
	public void init() {
		this.load();
		if (this.isLoaded()) {
//			this.open(0);
		}
	}

	@Override
	void load() {
		try {
			System.loadLibrary("silk8_jni");
			super.load();
		} catch (Throwable t) {
		}
	}
}
