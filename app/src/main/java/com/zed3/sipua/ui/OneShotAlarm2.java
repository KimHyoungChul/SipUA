package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class OneShotAlarm2 extends BroadcastReceiver {
	public void onReceive(final Context context, final Intent intent) {
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("wlan", true) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("3g", false) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("vpn", false) || PreferenceManager.getDefaultSharedPreferences(context).getBoolean("edge", false)) {
			if (Receiver.mSipdroidEngine != null) {
				context.startService(new Intent(context, (Class) RegisterService.class));
			}
		} else {
			context.stopService(new Intent(context, (Class) RegisterService.class));
		}
		Log.e("thread....", "thread....");
	}
}
