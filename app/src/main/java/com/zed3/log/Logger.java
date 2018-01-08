package com.zed3.log;

import android.util.*;

public class Logger
{
	private static int DEBUG;
	private static int ERROR;
	private static int INFO;
	private static int LOGLEVEL;
	private static int VERBOSE;
	private static int WARN;
	public static boolean isDebug;

	static {
		Logger.LOGLEVEL = 7;
		Logger.VERBOSE = 1;
		Logger.DEBUG = 2;
		Logger.INFO = 3;
		Logger.WARN = 4;
		Logger.ERROR = 5;
		Logger.isDebug = true;
	}

	public static void d(final boolean b, final String s, final String s2) {
		if (b && Logger.LOGLEVEL > Logger.DEBUG) {
			Log.d(s, s2);
		}
	}

	public static void e(final String s, final String s2) {
		Log.e(s, s2);
	}

	public static void e(final boolean b, final String s, final String s2) {
		if (b && Logger.LOGLEVEL > Logger.ERROR) {
			Log.e(s, s2);
		}
	}

	public static void i(final String s, final String s2) {
		Log.e(s, s2);
	}

	public static void i(final boolean b, final String s, final String s2) {
		if (b && Logger.LOGLEVEL > Logger.INFO) {
			Log.i(s, s2);
		}
	}

	public static void v(final String s, final String s2) {
		Log.e(s, s2);
	}

	public static void v(final boolean b, final String s, final String s2) {
		if (b && Logger.LOGLEVEL > Logger.VERBOSE) {
			Log.v(s, s2);
		}
	}

	public static void w(final String s, final String s2) {
		Log.e(s, s2);
	}

	public static void w(final boolean b, final String s, final String s2) {
		if (b && Logger.LOGLEVEL > Logger.WARN) {
			Log.w(s, s2);
		}
	}
}
