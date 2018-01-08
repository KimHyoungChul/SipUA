package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;

public class S6SOSListener implements IpttKeyListener {
	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		return false;
	}
}
