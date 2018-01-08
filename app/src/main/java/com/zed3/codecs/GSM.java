package com.zed3.codecs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;

class GSM extends CodecBase implements Codec {
	GSM() {
		this.CODEC_NAME = "GSM";
		this.CODEC_USER_NAME = "GSM";
		this.CODEC_DESCRIPTION = "13kbit";
		this.CODEC_NUMBER = 3;
		this.CODEC_DEFAULT_SETTING = "never";
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		final String string = defaultSharedPreferences.getString("compression", Settings.DEFAULT_COMPRESSION);
		if (string != null) {
			final SharedPreferences.Editor edit = defaultSharedPreferences.edit();
			edit.remove("compression");
			edit.putString(this.CODEC_NAME, string);
			edit.commit();
		}
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
//			this.open();
		}
	}

	@Override
	void load() {
		try {
			System.loadLibrary("gsm_jni");
			super.load();
		} catch (Throwable t) {
		}
	}

}
