package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zed3.sipua.ui.splash.SplashActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
	private final String sharedPrefsFile;

	public BootBroadcastReceiver() {
		this.sharedPrefsFile = "com.zed3.sipua_preferences";
	}

	private String IsAutoRunConfig(final Context context) {
		return context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("autorunkey", "0");
	}

	public void onReceive(final Context context, Intent intent) {
		LogUtil.makeLog("BootBroadcastReceiver", "onReceiver() onReceiver() intent.getAction() = " + intent.getAction());
		if (intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED") && this.IsAutoRunConfig(context).equals("1")) {
			intent = new Intent(context, (Class) SplashActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
}
