package com.zed3.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetStateSingleton {
	private static NetStateSingleton instance;
	private volatile NetState mNetState;

	static {
		NetStateSingleton.instance = null;
	}

	private NetStateSingleton() {
		this.mNetState = NetState.NoNet;
	}

	public static NetStateSingleton getInstance() {
		if (NetStateSingleton.instance != null) {
			return NetStateSingleton.instance;
		}
		synchronized (NetStateSingleton.class) {
			if (NetStateSingleton.instance == null) {
				NetStateSingleton.instance = new NetStateSingleton();
			}
			return NetStateSingleton.instance;
		}
	}

	public static boolean isNetworkAvailable(final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
		}
		return false;
	}

	public NetState getmNetState() {
		return this.mNetState;
	}

	public void setmNetState(final NetState mNetState) {
		synchronized (this) {
			this.mNetState = mNetState;
		}
	}

	public enum NetState {
		MobileState("MobileState", 1),
		NoNet("NoNet", 2),
		WIFIState("WIFIState", 0);

		private NetState(final String s, final int n) {
		}
	}
}
