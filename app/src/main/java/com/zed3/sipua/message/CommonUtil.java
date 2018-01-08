package com.zed3.sipua.message;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {
	public static Boolean isLog = Boolean.valueOf(true);

	public static void Log(String tag, String funcName, String msg, char type) {
		if (isLog.booleanValue()) {
			switch (type) {
				case 'd':
					Log.d(tag, new StringBuilder(String.valueOf(funcName)).append("===>").append(msg).toString());
					return;
				case 'e':
					Log.e(tag, new StringBuilder(String.valueOf(funcName)).append("===>").append(msg).toString());
					return;
				case 'i':
					Log.i(tag, new StringBuilder(String.valueOf(funcName)).append("===>").append(msg).toString());
					return;
				case 'v':
					Log.v(tag, new StringBuilder(String.valueOf(funcName)).append("===>").append(msg).toString());
					return;
				default:
					Log.d(tag, new StringBuilder(String.valueOf(funcName)).append("===>").append(msg).toString());
					return;
			}
		}
	}

	public static String getNowDate() {
		String date;
		Calendar calendar = Calendar.getInstance();
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mMonth++;
		if (mMonth < 10) {
			date = new StringBuilder(String.valueOf(mYear)).append("-0").append(mMonth).toString();
		} else {
			date = new StringBuilder(String.valueOf(mYear)).append("-").append(mMonth).toString();
		}
		if (mDay < 10) {
			return new StringBuilder(String.valueOf(date)).append("-0").append(mDay).toString();
		}
		return new StringBuilder(String.valueOf(date)).append("-").append(mDay).toString();
	}

	public static String getNowTime() {
		String time;
		Calendar calendar = Calendar.getInstance();
		int mHour = calendar.get(Calendar.HOUR_OF_DAY);
		int mMinute = calendar.get(Calendar.MINUTE);
		int mSecond = calendar.get(Calendar.SECOND);
		if (mHour < 10) {
			time = "0" + mHour;
		} else {
			time = new StringBuilder(String.valueOf(mHour)).toString();
		}
		if (mMinute < 10) {
			time = new StringBuilder(String.valueOf(time)).append(":0").append(mMinute).toString();
		} else {
			time = new StringBuilder(String.valueOf(time)).append(":").append(mMinute).toString();
		}
		if (mSecond < 10) {
			return new StringBuilder(String.valueOf(time)).append(":0").append(mSecond).toString();
		}
		return new StringBuilder(String.valueOf(time)).append(":").append(mSecond).toString();
	}

	public static String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			return null;
		}
	}
}
