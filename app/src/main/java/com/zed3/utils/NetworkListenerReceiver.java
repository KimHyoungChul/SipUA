package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zed3.net.util.NetChecker;
import com.zed3.sipua.ui.splash.SplashActivity;

public class NetworkListenerReceiver extends BroadcastReceiver {
	public void onReceive(final Context context, Intent intent) {
		LogUtil.makeLog("NetworkListenerReceiver", "onReceiver() intent.getAction() = " + intent.getAction());
		if (intent.getAction() != null && intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE") && NetChecker.isNetworkAvailable(context) && context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getBoolean("NetworkListenerService", false)) {
			intent = new Intent(context, (Class) SplashActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
}
