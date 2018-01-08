package com.zed3.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenWakeupActionReceiver extends BroadcastReceiver {
	private static final String tag = "ScreenWakeupActionReceiver";

	public void onReceive(final Context context, final Intent intent) {
		MyPowerManager.getInstance().wakeupScreen("ScreenWakeupActionReceiver", 5000);
	}
}
