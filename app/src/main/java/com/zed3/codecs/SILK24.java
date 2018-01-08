package com.zed3.codecs;

class SILK24 extends CodecBase implements Codec {
	private static final int DEFAULT_COMPLEXITY = 0;

	SILK24() {
		this.CODEC_USER_NAME = "SILK";
		this.CODEC_NAME = "silk24";
		this.CODEC_DESCRIPTION = "12-40kbit";
		this.CODEC_NUMBER = 120;
		this.CODEC_DEFAULT_SETTING = "never";
		this.CODEC_SAMPLE_RATE = 24000;
		this.CODEC_FRAME_SIZE = 480;
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
			System.loadLibrary("silk24_jni");
			super.load();
		} catch (Throwable t) {
		}
	}
}
