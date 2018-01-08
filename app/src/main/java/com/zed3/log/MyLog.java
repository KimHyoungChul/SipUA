package com.zed3.log;

import android.util.*;
import com.zed3.sipua.*;

public class MyLog
{
	private static boolean isClosed;

	static {
		MyLog.isClosed = false;
	}

	public static void d(final String s, final String s2) {
		Log.d(s, s2);
		if (SipUAApp.getIsClosed() && !MyLog.isClosed) {
			CrashHandler.SaveLog(s, s2);
		}
	}

	public static void e(final String s, final String s2) {
		Log.e(s, s2);
		if (SipUAApp.getIsClosed() && !MyLog.isClosed) {
			CrashHandler.SaveLog(s, s2);
		}
	}

	public static void i(final String s, final String s2) {
		Log.i(s, s2);
		if (SipUAApp.getIsClosed() && !MyLog.isClosed) {
			CrashHandler.SaveLog(s, s2);
		}
	}

	public static void v(final String s, final String s2) {
		Log.v(s, s2);
		if (SipUAApp.getIsClosed() && !MyLog.isClosed) {
			CrashHandler.SaveLog(s, s2);
		}
	}

	public static void w(final String s, final String s2) {
		Log.w(s, s2);
		CrashHandler.SaveLog(s, s2);
	}
}
