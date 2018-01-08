package com.zed3.codecs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkType {
	public static final int NETWORKTYPE_2G = 1;
	public static final int NETWORKTYPE_3G = 2;
	public static final int NETWORKTYPE_INVALID = 0;
	public static final int NETWORKTYPE_WIFI = 3;

	public static EncodeRate.Mode getNetWorkType(final Context context) {
		final NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		EncodeRate.Mode mode = EncodeRate.Mode.MR475;
		if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
			final String typeName = activeNetworkInfo.getTypeName();
			if (typeName.equalsIgnoreCase("WIFI")) {
				mode = EncodeRate.Mode.MR122;
			} else if (typeName.equalsIgnoreCase("MOBILE")) {
				int n;
				if (isFastMobileNetwork(context)) {
					n = 2;
				} else {
					n = 1;
				}
				if (n == 1) {
					return EncodeRate.Mode.MR475;
				}
				return EncodeRate.Mode.MR122;
			}
			return mode;
		}
		return mode;
	}

	// TODO
	private static boolean isFastMobileNetwork(final Context context) {
		switch (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType()) {
			default: {
				return false;
			}
			case 5: {
				return true;
			}
			case 6: {
				return true;
			}
			case 8: {
				return true;
			}
			case 10: {
				return true;
			}
			case 9: {
				return true;
			}
			case 3: {
				return true;
			}
			case 14: {
				return true;
			}
			case 12: {
				return true;
			}
			case 15: {
				return true;
			}
			case 13: {
				return true;
			}
		}
	}
}
