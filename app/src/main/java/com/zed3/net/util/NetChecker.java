package com.zed3.net.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.R;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

public class NetChecker {
	private static long lastTime;

	/**
	 * 检查网络
	 * @param mContext
	 * @param needToast
	 * @return true:网络连接正常  false:网络连接异常
	 */
	public static boolean check(Context mContext, boolean needToast) {
		if (Tools.isConnect(mContext)) {
			return true;
		}
		if (checkTime()) {
			MyToast.showToast(needToast, mContext, (int) R.string.network_exception);
			lastTime = System.currentTimeMillis();
		}
		GroupCallUtil.changeUI(false);
		return false;
	}

	private static boolean checkTime() {
		long thisTime = System.currentTimeMillis();
		if (lastTime == 0) {
			lastTime = thisTime;
			return true;
		} else if (thisTime - lastTime <= 3000) {
			return false;
		} else {
			lastTime = thisTime;
			return true;
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity == null) {
				return false;
			}
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected() && info.getState() == State.CONNECTED) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isWifiEnabled(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().getState() == State.CONNECTED)
				|| telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS;
	}

	public static boolean isWifi(Context context) {
		NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (activeNetInfo == null || activeNetInfo.getType() != 1) {
			return false;
		}
		return true;
	}

	public static boolean is3G(Context context) {
		NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (activeNetInfo == null || activeNetInfo.getType() != 0) {
			return false;
		}
		return true;
	}
}
