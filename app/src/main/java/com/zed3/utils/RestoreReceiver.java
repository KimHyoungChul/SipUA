package com.zed3.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestoreReceiver extends BroadcastReceiver {
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equals("com.zed3.restore") && Tools.isInBg) {
			Tools.bringtoFront(context);
		}
	}
}
