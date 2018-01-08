package com.zed3.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.net.util.NetChecker;

public final class Systems {
	public static final String EMPTY = "";
	public static final Logger log = new Logger();

	public static final class Logger {
		private static final boolean DEBUG = true;

		public void print(String tag, String log) {
			Log.i(tag, log);
		}
	}

	public static boolean isConnectedNetwork(Context context) {
		return NetChecker.isNetworkAvailable(context);
	}

	public static int parseInt(String value) {
		int defaultValue = -1;
		if (!TextUtils.isEmpty(value)) {
			try {
				defaultValue = Integer.parseInt(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	public static String getLocalMacAddress(Context context) {
		if (context == null) {
			return "";
		}
		return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
	}

	public static String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			return tm.getDeviceId();
		}catch (SecurityException e){
			//权限失败android.permission.READ_PHONE_STATE
			return "";
		}
	}

	public static String getThid(Context context) {
		String Imei = getImei(context);
		return Imei != null ? Imei : getLocalMacAddress(context);
	}

	public static String getVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
