package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ChangeAccount extends Activity {
	public static int getPref(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt("account", 0);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences((Context) this).edit();
		edit.putInt("account", Receiver.engine((Context) this).pref = 1 - getPref((Context) this));
		edit.commit();
		Receiver.engine((Context) this).register(true);
		this.finish();
	}
}
