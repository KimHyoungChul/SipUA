package com.zed3.sipua.phone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.sipua.R;

public class PhoneUtils {
	private static final boolean DBG = false;
	private static final String LOG_TAG = "PhoneUtils";
	private static final int QUERY_TOKEN = -1;
	static CallerInfoAsyncQuery.OnQueryCompleteListener sCallerInfoQueryListener;

	static {
		PhoneUtils.sCallerInfoQueryListener = new CallerInfoAsyncQuery.OnQueryCompleteListener() {
			@Override
			public void onQueryComplete(final int n, final Object o, final CallerInfo userData) {
				((Connection) o).setUserData(userData);
			}
		};
	}

	static String getCompactNameFromCallerInfo(final CallerInfo callerInfo, final Context context) {
		String s = null;
		if (callerInfo != null && (s = callerInfo.name) == null) {
			s = callerInfo.phoneNumber;
		}
		String string;
		if ((string = s) == null) {
			string = context.getString(R.string.unknown);
		}
		return string;
	}

	private static void log(final String s) {
		Log.d("PhoneUtils", "[PhoneUtils] " + s);
	}

	static void saveToContact(final Context context, final String s) {
		final Intent intent = new Intent("android.intent.action.INSERT", Contacts.People.CONTENT_URI);
		intent.putExtra("phone", s);
		context.startActivity(intent);
	}

	public static CallerInfoToken startGetCallerInfo(final Context context, final Call call, final CallerInfoAsyncQuery.OnQueryCompleteListener onQueryCompleteListener, final Object o) {
		return startGetCallerInfo(context, call.getEarliestConnection(), onQueryCompleteListener, o);
	}

	static CallerInfoToken startGetCallerInfo(final Context context, final Connection connection, final CallerInfoAsyncQuery.OnQueryCompleteListener onQueryCompleteListener, final Object o) {
		if (connection == null) {
			final CallerInfoToken callerInfoToken = new CallerInfoToken();
			callerInfoToken.asyncQuery = null;
			return callerInfoToken;
		}
		final Object userData = connection.getUserData();
		CallerInfoToken callerInfoToken2;
		if (userData instanceof Uri) {
			final CallerInfoToken userData2 = new CallerInfoToken();
			userData2.currentInfo = new CallerInfo();
			(userData2.asyncQuery = CallerInfoAsyncQuery.startQuery(-1, context, (Uri) userData, PhoneUtils.sCallerInfoQueryListener, connection)).addQueryListener(-1, onQueryCompleteListener, o);
			userData2.isFinal = false;
			connection.setUserData(userData2);
			callerInfoToken2 = userData2;
		} else if (userData == null) {
			final String address = connection.getAddress();
			final CallerInfoToken userData3 = new CallerInfoToken();
			userData3.currentInfo = new CallerInfo();
			if (!TextUtils.isEmpty((CharSequence) address)) {
				userData3.currentInfo.phoneNumber = address;
				(userData3.asyncQuery = CallerInfoAsyncQuery.startQuery(-1, context, address, connection.getAddress2(), PhoneUtils.sCallerInfoQueryListener, connection)).addQueryListener(-1, onQueryCompleteListener, o);
				userData3.isFinal = false;
			} else {
				userData3.isFinal = true;
			}
			connection.setUserData(userData3);
			callerInfoToken2 = userData3;
		} else if (userData instanceof CallerInfoToken) {
			callerInfoToken2 = (CallerInfoToken) userData;
			if (callerInfoToken2.asyncQuery != null) {
				callerInfoToken2.asyncQuery.addQueryListener(-1, onQueryCompleteListener, o);
			} else {
				if (callerInfoToken2.currentInfo == null) {
					callerInfoToken2.currentInfo = new CallerInfo();
				}
				callerInfoToken2.isFinal = true;
			}
		} else {
			callerInfoToken2 = new CallerInfoToken();
			callerInfoToken2.currentInfo = (CallerInfo) userData;
			callerInfoToken2.asyncQuery = null;
			callerInfoToken2.isFinal = true;
		}
		return callerInfoToken2;
	}

	public static class CallerInfoToken {
		public CallerInfoAsyncQuery asyncQuery;
		public CallerInfo currentInfo;
		public boolean isFinal;
	}
}
