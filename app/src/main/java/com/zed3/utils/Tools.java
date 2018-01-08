package com.zed3.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.zed3.flow.FlowRefreshService;
import com.zed3.location.MemoryMg;
import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.power.MyPowerManager;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Tools {
	private static final boolean GPSDebug = true;
	public static boolean isInBg = true;
	private static final String sendCompressFolerName = "CompressPic";

	class C13321 implements OnClickListener {
		private final /* synthetic */ AlertDialog val$dlg;

		C13321(AlertDialog alertDialog) {
			this.val$dlg = alertDialog;
		}

		public void onClick(View v) {
			this.val$dlg.cancel();
		}
	}

	public static boolean isInMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	public static String getRandomCharNum(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
			if ("char".equalsIgnoreCase(charOrNum)) {
				val = new StringBuilder(String.valueOf(val)).append((char) (random.nextInt(26) + (random.nextInt(2) % 2 == 0 ? 65 : 97))).toString();
			} else if ("num".equalsIgnoreCase(charOrNum)) {
				val = new StringBuilder(String.valueOf(val)).append(String.valueOf(random.nextInt(10))).toString();
			}
		}
		return val;
	}

	public static boolean isRunBackGroud(Context context) {
		List<RunningTaskInfo> tasks = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
		if (tasks.isEmpty() || ((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName().equals(context.getPackageName())) {
			return false;
		}
		return true;
	}

	public static String getVersionName(Context ctx) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo == null ? "" : packageInfo.versionName;
	}

	public static void FlowAlertDialog(Context context) {
		AlertDialog dlg = new Builder(context).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.flowalertalarm);
//		((ImageView) window.findViewById(R.id.flowtitle)).setOnClickListener(new C13321(dlg));
	}

	public static double calculateTotal(double db) {
		return ((double) Math.round(((db / 1024.0d) / 1024.0d) * 100.0d)) / 100.0d;
	}

	public static double calculatePercent(double a, double b) {
		return ((double) Math.round((a / b) * 100.0d)) / 100.0d;
	}

	public static void onPreLogOut() {
		NetChangedReceiver.unregisterSelf();
		HeadsetPlugReceiver.stopReceive(SipUAApp.getAppContext());
		MyPowerManager.getInstance().exit(SipUAApp.getAppContext());
	}

	public static void onRegisterSuccess() {
		NetChangedReceiver.registerSelf();
		HeadsetPlugReceiver.startReceive(SipUAApp.getAppContext());
		MyPowerManager.getInstance().init(SipUAApp.getAppContext());
	}

	public static void exitApp(Context context) {
		LogUtil.makeLog("GUOK", "exitApp()");
//		if (MainActivity.getInstance() != null) {
//			MainActivity.getInstance().finish();
//		}
		cleanGrpID();
		SipUAApp.exit();
		Receiver.getGDProcess().quit();
		context.stopService(new Intent(context, FlowRefreshService.class));
		context.stopService(new Intent(context, AlarmService.class));
		Receiver.engine(context).expire(-1);
		Receiver.onText(3, null, 0, 0);
		context.getSharedPreferences("notifyInfo", 0).edit().clear().commit();
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Receiver.engine(context).halt();
		context.stopService(new Intent(context, RegisterService.class));
		Receiver.alarm(0, OneShotAlarm.class);
		Receiver.alarm(0, MyHeartBeatReceiver.class);
		System.exit(0);
	}

	public static void exitApp2(Context context) {
//		if (MainActivity.getInstance() != null) {
//			MainActivity.getInstance().finish();
//		}
		Receiver.getGDProcess().quit();
		SipUAApp.exit();
		context.stopService(new Intent(context, FlowRefreshService.class));
		context.stopService(new Intent(context, AlarmService.class));
		Receiver.engine(context).expire(-1);
		Receiver.onText(3, null, 0, 0);
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Receiver.engine(context).halt();
		context.stopService(new Intent(context, RegisterService.class));
		Receiver.alarm(0, OneShotAlarm.class);
		Receiver.alarm(0, MyHeartBeatReceiver.class);
		System.exit(0);
	}

	public static void cleanGrpID() {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(SipUAApp.getAppContext()).edit();
		edit.remove("grpID");
		System.out.println("----- is commit = " + edit.commit());
	}

	public static void saveGrpID(String grpID) {
		System.out.println("----- saveGrpID = " + grpID);
		Editor it = PreferenceManager.getDefaultSharedPreferences(SipUAApp.getAppContext()).edit();
		it.putString("grpID", grpID);
		it.commit();
	}

	public static boolean isConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) SipUAApp.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected() && info.getState() == State.CONNECTED) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	public static void bringtoFront(Context ctx) {
		List<RunningTaskInfo> task_info = ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(20);
		String className = "";
		int i = 0;
		while (i < task_info.size()) {
			if ("com.zed3.sipua".equals(((RunningTaskInfo) task_info.get(i)).topActivity.getPackageName())) {
				className = ((RunningTaskInfo) task_info.get(i)).topActivity.getClassName();
				Intent intent = new Intent();
				intent.setAction("android.intent.action.MAIN");
				intent.addCategory("android.intent.category.LAUNCHER");
				intent.addCategory("android.intent.action.VIEW");
				intent.setComponent(new ComponentName(((RunningTaskInfo) task_info.get(i)).topActivity.getPackageName(), className));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				ctx.startActivity(intent);
				return;
			} else if (i >= task_info.size() - 1) {
				Intent inn = new Intent(ctx, SplashActivity.class);
//				inn.setFlags(DriveFile.MODE_READ_ONLY);
				ctx.startActivity(inn);
				return;
			} else {
				i++;
			}
		}
	}

	public static byte[] shortArray2ByteArray(short[] shorts) {
		byte[] bytes = new byte[(shorts.length * 2)];
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
		return bytes;
	}

	public static int[] getWidthHeight(Activity ctx) {
		int[] res = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeigh = dm.heightPixels;
		res[0] = screenWidth;
		res[1] = screenHeigh;
		return res;
	}

	public static double getWidthHeightRate(Activity ctx) {
		int[] res = getWidthHeight(ctx);
		return (((double) res[0]) * 1.0d) / (((double) res[1]) * 1.0d);
	}

	public static void writeFileToSD(String str) {
		if (!TextUtils.isEmpty(str)) {
			if (!str.endsWith("\n")) {
				str = new StringBuilder(String.valueOf(str)).append("\n").toString();
			}
			str = new StringBuilder(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())))).append(": ").append(str).toString();
			if (Environment.getExternalStorageState().equals("mounted")) {
				try {
					String pathName = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/gps/").toString();
					String fileName = Build.MODEL + ".txt";
					File path = new File(pathName);
					File file = new File(new StringBuilder(String.valueOf(pathName)).append(fileName).toString());
					if (!path.exists()) {
						Log.d("TestFile", "Create the path:" + pathName);
						path.mkdir();
					}
					if (!file.exists()) {
						Log.d("TestFile", "Create the file:" + fileName);
						file.createNewFile();
					}
					FileOutputStream stream = new FileOutputStream(file, true);
					stream.write(str.getBytes());
					stream.close();
					return;
				} catch (Exception e) {
					Log.e("TestFile", "Error on writeFilToSD.");
					e.printStackTrace();
					return;
				}
			}
			Log.d("TestFile", "SD card is not avaiable/writeable right now.");
		}
	}

	public static void writeFileToSD_GPS(String str) {
		if (!TextUtils.isEmpty(str)) {
			if (!str.endsWith("\n")) {
				str = new StringBuilder(String.valueOf(str)).append("\n").toString();
			}
			str = new StringBuilder(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())))).append(": ").append(str).toString();
			if (Environment.getExternalStorageState().equals("mounted")) {
				try {
					String pathName = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/gps/").toString();
					String fileName = Build.MODEL + ".txt";
					File path = new File(pathName);
					File file = new File(new StringBuilder(String.valueOf(pathName)).append(fileName).toString());
					if (!path.exists()) {
						Log.d("TestFile", "Create the path:" + pathName);
						path.mkdir();
					}
					if (!file.exists()) {
						Log.d("TestFile", "Create the file:" + fileName);
						file.createNewFile();
					}
					FileOutputStream stream = new FileOutputStream(file, true);
					stream.write(str.getBytes());
					stream.close();
					return;
				} catch (Exception e) {
					Log.e("TestFile", "Error on writeFilToSD.");
					e.printStackTrace();
					return;
				}
			}
			Log.d("TestFile", "SD card is not avaiable/writeable right now.");
		}
	}

	public static void setGpsMode(SharedPreferences mSharedPreferences, int mode) {
		if (DeviceInfo.CONFIG_MAP_TYPE == 0) {
			MemoryMg.getInstance().GpsLocationModel = mode;
		} else {
			MemoryMg.getInstance().GpsLocationModel_EN = mode;
		}
		saveGpsMode(mSharedPreferences, mode);
	}

	private static void saveGpsMode(SharedPreferences mSharedPreferences, int mode) {
		Editor edit = mSharedPreferences.edit();
		if (DeviceInfo.CONFIG_MAP_TYPE == 0) {
			edit.putInt(Settings.PREF_LOCATEMODE, mode);
		} else {
			edit.putInt(Settings.PREF_LOCATEMODE_EN, mode);
		}
		edit.commit();
	}

	public static int getCurrentGpsMode() {
		if (DeviceInfo.CONFIG_MAP_TYPE == 0) {
			return MemoryMg.getInstance().GpsLocationModel;
		}
		return MemoryMg.getInstance().GpsLocationModel_EN;
	}

	public static String saveBitmapFile(Bitmap bitmap) {
		if (!Environment.getExternalStorageState().equals("mounted")) {
			Log.d("TestFile", "SD card is not avaiable/writeable right now.");
			return "";
		} else if (bitmap == null) {
			return "";
		} else {
			StringBuffer folderPath = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
			folderPath.append(File.separator).append(sendCompressFolerName);
			StringBuffer outputPath = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
			outputPath.append(File.separator).append(sendCompressFolerName).append(File.separator).append(formUniqueFileName()).append(".JPG");
			File file = new File(outputPath.toString());
			try {
				File folder = new File(folderPath.toString());
				if (!folder.exists()) {
					folder.mkdir();
				}
				if (!file.exists()) {
					Log.d("TestFile", "Create the file:" + file.getPath());
					file.createNewFile();
				}
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(CompressFormat.JPEG, 100, bos);
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return file.getAbsolutePath();
		}
	}

	private static String formUniqueFileName() {
		return UUID.randomUUID().toString();
	}

	public static void deleteFileByE_id(String e_id) {
		File file_dir = new File(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath())).append("/smsmms").toString());
		if (!file_dir.exists()) {
			file_dir.mkdirs();
		}
		File file = new File(file_dir, "mms_" + e_id + ".txt");
		if (file.exists()) {
			file.delete();
		}
	}
}
