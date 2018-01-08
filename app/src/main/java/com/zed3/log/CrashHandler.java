package com.zed3.log;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	private static SimpleDateFormat formatx = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static CrashHandler mInstance = null;
	private final Vector<String> fileNameList = new Vector();
	private UncaughtExceptionHandler mDefaultHandler;
	public volatile RandomAccessFile raf = null;

	class C09931 extends Thread {
		C09931() {
		}

		public void run() {
			Looper.prepare();
			Looper.loop();
		}
	}

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		if (mInstance == null) {
			synchronized (CrashHandler.class) {
				if (mInstance == null) {
					mInstance = new CrashHandler();
				}
			}
		}
		return mInstance;
	}

	public void init(Context context, boolean flag) {
		Log.e(TAG, "init");
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	private void InitFile() {
		if (this.raf == null) {
			try {
				String fileName = "zed3-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".log";
				String dirName = "";
				String delName = "";
				if (Environment.getExternalStorageState().equals("mounted")) {
					File dir;
					dirName = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("zed3").toString();
					synchronized (this) {
						dir = new File(dirName);
						if (!dir.exists()) {
							dir.mkdir();
						}
					}
					this.fileNameList.add(fileName);
					Log.e(TAG, new StringBuilder(String.valueOf(this.fileNameList.size())).append(" file count").toString());
					if (this.fileNameList.size() >= 5) {
						delName = (String) this.fileNameList.get(0);
						File delFile = new File(new StringBuilder(String.valueOf(dirName)).append(File.separator).append(delName).toString());
						Log.e(TAG, new StringBuilder(String.valueOf(dirName)).append(File.separator).append(delName).toString());
						if (delFile.exists()) {
							if (delFile.delete()) {
								this.fileNameList.remove(0);
							} else {
								Log.e(TAG, "delete file " + delName + " failed");
							}
						}
					}
					synchronized (this) {
						this.raf = new RandomAccessFile(new File(dir, fileName), "rw");
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(TAG, ex.toString());
			}
		}
	}

	public static void SaveLog(String tag, String log) {
		getInstance().saveLogInstance(tag, log);
	}

	public static void EndLog() {
		getInstance().endLogInstance();
	}

	public void saveLogInstance(String tag, String log) {
		try {
			InitFile();
			if (this.raf != null) {
				String str = new StringBuilder(formatx.format(new Date())).append(" ").append(tag).append(" ").append(log).append("\r\n").toString();
				synchronized (str) {
					this.raf.writeUTF(str);
				}
				fileDivision();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fileDivision() throws IOException {
		if (this.raf != null && this.raf.length() > 3145728) {
			synchronized (this) {
				if (this.raf != null) {
					try {
						this.raf.close();
						this.raf = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			InitFile();
		}
	}

	public void endLogInstance() {
		this.fileNameList.clear();
		if (this.raf != null) {
			synchronized (TAG) {
				try {
					this.raf.close();
					this.raf = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void uncaughtException(Thread thread, Throwable ex) {
		if (handleException(ex) || this.mDefaultHandler == null) {
			Log.e(TAG, "killProcess");
			Process.killProcess(Process.myPid());
			System.exit(1);
			return;
		}
		Log.e(TAG, "uncaughtexception");
		this.mDefaultHandler.uncaughtException(thread, ex);
	}

	public boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		new C09931().start();
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
			cause.printStackTrace(pw);
		}
		pw.close();
		SaveLog("system.err", writer.toString());
		return true;
	}
}
