package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoopLocation extends BroadcastReceiver {
	public void onReceive(final Context context, final Intent intent) {
		OneShotLocation.receive(context, intent);
	}
}
