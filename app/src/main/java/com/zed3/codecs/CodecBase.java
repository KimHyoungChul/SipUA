package com.zed3.codecs;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.zed3.log.Logger;
import com.zed3.sipua.ui.Receiver;

import org.audio.audioEngine.SlientCheck;

public class CodecBase implements Preference.OnPreferenceChangeListener {
	public String CODEC_DEFAULT_SETTING;
	protected String CODEC_DESCRIPTION;
	protected int CODEC_FRAME_SIZE;
	protected String CODEC_NAME;
	protected int CODEC_NUMBER;
	protected int CODEC_SAMPLE_RATE;
	protected String CODEC_USER_NAME;
	private boolean enabled;
	private boolean failed;
	private boolean loaded;
	private boolean needLog;
	private int nt;
	protected SlientCheck slientCheck;
	private String tag;
	TelephonyManager tm;
	private String value;
	private boolean wlanOnly;
	private boolean wlanOr3GOnly;

	public CodecBase() {
		this.CODEC_SAMPLE_RATE = 8000;
		this.CODEC_FRAME_SIZE = 160;
		this.CODEC_DEFAULT_SETTING = "never";
		this.loaded = false;
		this.failed = false;
		this.enabled = false;
		this.wlanOnly = false;
		this.wlanOr3GOnly = false;
		this.tag = "CodecBase";
		this.needLog = true;
		this.slientCheck = null;
	}

	private void updateFlags(final String s) {
		if (s.equals("never")) {
			this.enabled = false;
			return;
		}
		this.enabled = true;
		if (s.equals("wlan")) {
			this.wlanOnly = true;
		} else {
			this.wlanOnly = false;
		}
		if (s.equals("wlanor3g")) {
			this.wlanOr3GOnly = true;
			return;
		}
		this.wlanOr3GOnly = false;
	}

	private boolean wlanOnly() {
		return this.enabled && this.wlanOnly;
	}

	private boolean wlanOr3GOnly() {
		return this.enabled && this.wlanOr3GOnly;
	}

	public void enable(final boolean enabled) {
		this.enabled = enabled;
	}

	public void fail() {
		this.update();
		this.failed = true;
	}

	public int frame_size() {
		return this.CODEC_FRAME_SIZE;
	}

	public int getNt() {
		return this.nt;
	}

	public String getTitle() {
		return String.valueOf(this.CODEC_NAME) + " (" + this.CODEC_DESCRIPTION + ")";
	}

	public String getValue() {
		return this.value;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isFailed() {
		return this.failed;
	}

	public boolean isLoaded() {
		return this.loaded;
	}

	public boolean isValid() {
		if (this.isEnabled()) {
			if (Receiver.on_wlan) {
				return true;
			}
			if (!this.wlanOnly()) {
				if (this.tm == null) {
					this.tm = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
				}
				return true;
			}
		}
		return false;
	}

	public String key() {
		return String.valueOf(this.CODEC_NAME) + "_new";
	}

	void load() {
		this.update();
		this.loaded = true;
	}

	public String name() {
		return this.CODEC_NAME;
	}

	public int number() {
		return this.CODEC_NUMBER;
	}

	public boolean onPreferenceChange(final Preference preference, final Object o) {
		final ListPreference listPreference = (ListPreference) preference;
		this.updateFlags(this.value = (String) o);
		listPreference.setValue(this.value);
		listPreference.setSummary(listPreference.getEntry());
		return true;
	}

	public int samp_rate() {
		return this.CODEC_SAMPLE_RATE;
	}

	public void setListPreference(final ListPreference listPreference) {
		listPreference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) this);
		listPreference.setValue(this.value);
	}

	public void setNt(final int nt) {
		this.nt = nt;
	}

	public void setVad(final SlientCheck slientCheck) {
		this.slientCheck = slientCheck;
	}

	@Override
	public String toString() {
		return "CODEC{ " + this.CODEC_NUMBER + ": " + this.getTitle() + "}";
	}

	public void update() {
		if (Receiver.mContext != null) {
			this.value = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(this.key(), this.CODEC_DEFAULT_SETTING);
			Logger.i(this.needLog, this.tag, "key() = " + this.key() + ",value = " + this.value);
			this.updateFlags(this.value);
		}
	}

	public String userName() {
		return this.CODEC_USER_NAME;
	}
}
