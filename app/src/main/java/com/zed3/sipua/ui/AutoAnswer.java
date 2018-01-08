package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class AutoAnswer extends Activity {
	AudioManager am;

	boolean getMode() {
		return PreferenceManager.getDefaultSharedPreferences((Context) this).getBoolean("auto_demand", false);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences((Context) this).edit();
		this.am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		this.saveVolume();
		edit.putBoolean("auto_demand", !this.getMode());
		edit.commit();
		this.restoreVolume();
		Receiver.updateAutoAnswer();
		this.finish();
	}

	protected void onResume() {
		Receiver.engine((Context) this);
		super.onResume();
	}

	void restoreVolume() {
		this.am.setRingerMode(PreferenceManager.getDefaultSharedPreferences((Context) this).getInt("ringermode" + this.getMode(), 2));
	}

	void saveVolume() {
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences((Context) this).edit();
		edit.putInt("volume" + this.getMode(), this.am.getStreamVolume(2));
		edit.putInt("ringermode" + this.getMode(), this.am.getRingerMode());
		edit.commit();
	}
}
