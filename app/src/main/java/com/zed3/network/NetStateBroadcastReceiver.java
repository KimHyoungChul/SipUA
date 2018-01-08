package com.zed3.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetStateBroadcastReceiver extends BroadcastReceiver {
	private NetworkInfo.State mobileState;
	public NetStateSingleton stateSingleton;
	private NetworkInfo.State wifiState;

	public NetStateBroadcastReceiver() {
		this.stateSingleton = NetStateSingleton.getInstance();
	}

	public void onReceive(final Context context, final Intent intent) {
		if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			this.wifiState = connectivityManager.getNetworkInfo(1).getState();
			this.mobileState = connectivityManager.getNetworkInfo(0).getState();
			if (this.wifiState == null || this.mobileState == null) {
				this.stateSingleton.setmNetState(NetStateSingleton.NetState.NoNet);
				return;
			}
			if (NetworkInfo.State.CONNECTED == this.wifiState && NetworkInfo.State.CONNECTED != this.mobileState) {
				this.stateSingleton.setmNetState(NetStateSingleton.NetState.WIFIState);
			} else {
				if (NetworkInfo.State.CONNECTED != this.wifiState && NetworkInfo.State.CONNECTED == this.mobileState) {
					this.stateSingleton.setmNetState(NetStateSingleton.NetState.MobileState);
					return;
				}
				if (NetworkInfo.State.CONNECTED != this.wifiState && NetworkInfo.State.CONNECTED != this.mobileState) {
					this.stateSingleton.setmNetState(NetStateSingleton.NetState.NoNet);
				}
			}
		}
	}
}
