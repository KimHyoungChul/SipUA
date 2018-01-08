package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

public class OneShotLocation extends BroadcastReceiver {
	public static void receive(final Context context, final Intent intent) {
		final Location location = (Location) intent.getParcelableExtra("location");
		if (location != null) {
			Receiver.pos(false);
			Receiver.url("lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&rad=" + location.getAccuracy());
		} else if (intent.hasExtra("android.intent.extra.ALARM_COUNT")) {
			Receiver.pos(false);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		receive(context, intent);
	}
}
