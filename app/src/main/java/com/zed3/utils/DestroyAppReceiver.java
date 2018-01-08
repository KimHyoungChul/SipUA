package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DestroyAppReceiver extends BroadcastReceiver {
	public void onReceive(final Context context, final Intent intent) {
		Tools.exitApp(context);
	}
}
