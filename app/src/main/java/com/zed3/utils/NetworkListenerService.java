package com.zed3.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class NetworkListenerService extends Service {
	private static final String TAG = "NetworkService";
	private IntentFilter filter;
	private NetworkListenerReceiver networkListenerReceiver;

	public IBinder onBind(final Intent intent) {
		return null;
	}

	public void onCreate() {
		LogUtil.makeLog("NetworkService", "--++>>onCreate()");
		super.onCreate();
		this.networkListenerReceiver = new NetworkListenerReceiver();
		(this.filter = new IntentFilter()).addAction("android.net.conn.CONNECTIVITY_CHANGE");
	}

	public void onDestroy() {
		LogUtil.makeLog("NetworkService", "--++>>onDestroy() unregisterReceiver");
		super.onDestroy();
		if (this.networkListenerReceiver != null && this.filter != null) {
			this.unregisterReceiver((BroadcastReceiver) this.networkListenerReceiver);
		}
	}

	public int onStartCommand(final Intent intent, final int n, final int n2) {
		LogUtil.makeLog("NetworkService", "--++>>onStartCommand() registerReceiver");
		this.registerReceiver((BroadcastReceiver) this.networkListenerReceiver, this.filter);
		return super.onStartCommand(intent, n, n2);
	}
}
