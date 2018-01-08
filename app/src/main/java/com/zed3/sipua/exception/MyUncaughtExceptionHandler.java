package com.zed3.sipua.exception;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Tools;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private static UncaughtExceptionHandler mDefaultHandler;
	private static MyUncaughtExceptionHandler mMyHandler;

	private MyUncaughtExceptionHandler() {
	}

	public static synchronized MyUncaughtExceptionHandler getInstance(Context context) {
		MyUncaughtExceptionHandler myUncaughtExceptionHandler;
		synchronized (MyUncaughtExceptionHandler.class) {
			if (mMyHandler == null) {
				mMyHandler = new MyUncaughtExceptionHandler();
				mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
			}
			myUncaughtExceptionHandler = mMyHandler;
		}
		return myUncaughtExceptionHandler;
	}

	public static UncaughtExceptionHandler getDefault() {
		return mDefaultHandler;
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		Context context = SipUAApp.mContext;
		System.out.println("-----uncau excetpion");
		ex.printStackTrace();
		saveExceptionLog(ex);
		Tools.exitApp(SipUAApp.mContext);
		Process.killProcess(Process.myPid());
	}

	public static void saveExceptionLog(Throwable ex) {
		Writer wr = new StringWriter();
		ex.printStackTrace(new PrintWriter(wr));
		StringBuilder sb = new StringBuilder();
		try {
			String version = SipUAApp.mContext.getPackageManager().getPackageInfo(SipUAApp.mContext.getPackageName(), 0).versionName;
			sb.append("ExceptionMessages:\n");
			sb.append("VersionName:" + version + "\n");
			sb.append(wr.toString());
			sb.append("\n");
			for (Field field : Build.class.getDeclaredFields()) {
				field.setAccessible(true);
				sb.append(new StringBuilder(String.valueOf(field.getName())).append("=").append(field.get(null).toString()).append("\n").toString());
			}
			sb.append(new StringBuilder(String.valueOf("time:" + getTimeString())).append("\n").toString());
			sb.append("===============================\n");
			String log = sb.toString();
			if (Environment.getExternalStorageState().equals("mounted")) {
				File dir = new File(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("com.zed3.sipua").toString());
				if (!dir.exists()) {
					dir.mkdir();
				}
				File file = new File(dir, "exceptions.txt");
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fileWriter = new FileWriter(file, true);
				fileWriter.write(log);
				fileWriter.flush();
				fileWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getTimeString() {
		return new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(System.currentTimeMillis()));
	}
}
