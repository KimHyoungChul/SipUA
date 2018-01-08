package com.zed3.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.constant.Contants;
import com.zed3.sipua.SipUAApp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	private static boolean DEBUG = false;
	private static final byte[] block4MakeLog = new byte[0];
	private static SimpleDateFormat formatter;
	private final byte[] block4GetDeviceModel;
	private String deviceInfoStr;
	private String deviceModelStr;
	private volatile FileWriter mFileWriter;
	private volatile File mLogFile;
	private StringBuilder sb;

	private static final class InstanceCreater {
		public static LogUtil sInstance = new LogUtil();

		private InstanceCreater() {
		}
	}

	private void writeLog2File(String s) {
		throw new UnsupportedOperationException("Method not decompiled: com.zed3.utils.LogUtil.writeLog2File(java.lang.String):void");
	}

	private LogUtil() {
		this.block4GetDeviceModel = new byte[0];
	}

	public static LogUtil getInstance() {
		return InstanceCreater.sInstance;
	}

	public static void makeLog(String tag, String logMsg) {
		try {
			Log.i(tag, logMsg);
			if (DEBUG && SipUAApp.getIsClosed()) {
				getInstance().writeLog2File(new StringBuilder(String.valueOf(tag)).append(" ").append(logMsg).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void newLogFile() throws IOException {
		this.mFileWriter = new FileWriter(this.mLogFile, true);
	}

	private void initFile() {
		File dir;
		String fileName = getLastLogFileName(SipUAApp.getAppContext());
		synchronized (this) {
			dir = new File(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("com.zed3.sipua").toString());
			if (!dir.exists()) {
				dir.mkdir();
			}
		}
		File[] listFiles = dir.listFiles();
		boolean isExistsed = false;
		for (File file : listFiles) {
			if (file.getName().equals(fileName)) {
				isExistsed = true;
			}
		}
		if (TextUtils.isEmpty(fileName) || !isExistsed) {
			String filename = "GQT-Log-" + getDeviceModel() + "-" + new SimpleDateFormat("MMdd-hhmm-ss").format(new Date(System.currentTimeMillis())) + ".txt";
			synchronized (this) {
				this.mLogFile = new File(dir, filename);
				saveLastLogFileName(SipUAApp.getAppContext(), filename);
			}
			return;
		}
		synchronized (this) {
			this.mLogFile = new File(dir, fileName);
		}
	}

	private static String getTimeString() {
		if (formatter == null) {
			formatter = new SimpleDateFormat(" yyyy-MM-dd hh:mm:ss SSS ");
		}
		return formatter.format(new Date(System.currentTimeMillis()));
	}

	private String getDeviceInfo() {
		// TODO
		return "TODO";
	}

	private String getDeviceModel() {
		// TODO
		return "TODO";
	}

	private String getLastLogFileName(Context context) {
		return getSharedPreferences(context).getString(Contants.KEY_LAST_GQT_MAIN_LOGFILE_NAME, "");
	}

	private void saveLastLogFileName(Context context, String fileName) {
		Editor edit = getSharedPreferences(context).edit();
		edit.putString(Contants.KEY_LAST_GQT_MAIN_LOGFILE_NAME, fileName);
		edit.commit();
	}

	private SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences("com.zed3.app", 0);
	}
}
