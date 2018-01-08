package com.zed3.codecs;

class Speex extends CodecBase implements Codec {
	private static final int DEFAULT_COMPRESSION = 6;

	Speex() {
		this.CODEC_NAME = "speex";
		this.CODEC_USER_NAME = "speex";
		this.CODEC_DESCRIPTION = "11kbit";
		this.CODEC_NUMBER = 97;
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
//			this.open(6);
		}
	}

	@Override
	void load() {
		try {
			System.loadLibrary("speex_jni");
			super.load();
		} catch (Throwable t) {
		}
	}
}
