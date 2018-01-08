package com.zed3.codecs;

import android.preference.ListPreference;

import org.audio.audioEngine.SlientCheck;

public interface Codec {
	void close();

	int decode(final byte[] p0, final short[] p1, final int p2);

	int decode(final byte[] p0, final short[] p1, final int p2, final int p3);

	int encode(final short[] p0, final int p1, final byte[] p2, final int p3);

	void fail();

	int frame_size();

	String getTitle();

	String getValue();

	void init();

	boolean isEnabled();

	boolean isFailed();

	boolean isLoaded();

	boolean isValid();

	String key();

	String name();

	int number();

	int samp_rate();

	void setListPreference(final ListPreference p0);

	void setVad(final SlientCheck p0);

	void update();

	String userName();
}
