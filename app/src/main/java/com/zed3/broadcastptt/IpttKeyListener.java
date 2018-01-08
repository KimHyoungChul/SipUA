package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;

public interface IpttKeyListener {
	boolean pttKeyClick(final Context p0, final Intent p1, final PttBroadcastReceiver p2);
}
