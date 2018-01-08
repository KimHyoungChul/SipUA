package com.zed3.codecs;

class G722 extends CodecBase implements Codec {
	private static final int DEFAULT_BITRATE = 64000;

	G722() {
		this.CODEC_NAME = "G722 HD Voice";
		this.CODEC_USER_NAME = "G722";
		this.CODEC_DESCRIPTION = "64kbit";
		this.CODEC_NUMBER = 9;
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
			// TODO
//			this.open(64000);
		}
	}

	@Override
	void load() {
		try {
			System.loadLibrary("g722_jni");
			super.load();
		} catch (Throwable t) {
		}
	}
}
