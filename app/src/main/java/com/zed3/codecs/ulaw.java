package com.zed3.codecs;

class ulaw extends CodecBase implements Codec {
	ulaw() {
		this.CODEC_NAME = "PCMU";
		this.CODEC_USER_NAME = "PCMU";
		this.CODEC_DESCRIPTION = "64kbit";
		this.CODEC_NUMBER = 0;
		this.CODEC_DEFAULT_SETTING = "never";
		this.load();
	}

	@Override
	public void close() {
	}

	@Override
	public int decode(final byte[] array, final short[] array2, final int n) {
		G711.ulaw2linear(array, array2, n);
		return n;
	}

	@Override
	public int decode(final byte[] array, final short[] array2, final int n, final int n2) {
		throw new RuntimeException("do not use this method\uff01");
	}

	@Override
	public int encode(final short[] array, final int n, final byte[] array2, final int n2) {
		G711.linear2ulaw(array, n, array2, n2);
		return n2;
	}

	@Override
	public void init() {
		G711.init();
	}
}
