package com.zed3.log;

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GKFileUtils {

	class C09951 implements Runnable {
		private final /* synthetic */ Activity val$activity;
		private final /* synthetic */ String val$str;

		C09951(Activity activity, String str) {
			this.val$activity = activity;
			this.val$str = str;
		}

		public void run() {
			Toast.makeText(this.val$activity, this.val$str, Toast.LENGTH_LONG).show();
		}
	}

	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	public static void toastText(String str, Activity activity) {
//		activity.runOnUiThread(new C09951(activity, str));
	}

	protected static boolean isFileExits(String fileName) {
		return new File(fileName).exists();
	}

	public static String createFilename() {
		return "zed3-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date()) + ".log";
	}

	public static String makeFileDir() {
		String dirName = "";
		if (Environment.getExternalStorageState().equals("mounted")) {
			dirName = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append(File.separator).append("zed3").toString();
			synchronized (GKFileUtils.class) {
				File dir = new File(dirName);
				if (!dir.exists()) {
					dir.mkdir();
				}
			}
		}
		return dirName;
	}

	public static String fatchFullFileName() {
		return makeFileDir() + File.separator + createFilename();
	}

	public static void delete0Files(String dir) {
		if (!TextUtils.isEmpty(dir)) {
			File dirs = new File(dir);
			if (dirs.isDirectory()) {
				for (File file : dirs.listFiles()) {
					if (file.length() == 0) {
						file.delete();
					}
				}
			}
		}
	}
}
