package com.zed3.codecs;

class SILK16 extends CodecBase implements Codec {
	private static final int DEFAULT_COMPLEXITY = 0;

	SILK16() {
		this.CODEC_USER_NAME = "SILK";
		this.CODEC_NAME = "silk16";
		this.CODEC_DESCRIPTION = "8-30kbit";
		this.CODEC_NUMBER = 119;
		this.CODEC_DEFAULT_SETTING = "wlanor3g";
		this.CODEC_SAMPLE_RATE = 16000;
		this.CODEC_FRAME_SIZE = 320;
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
			System.loadLibrary("silk16_jni");
			super.load();
		} catch (Throwable t) {
		}
	}
}
