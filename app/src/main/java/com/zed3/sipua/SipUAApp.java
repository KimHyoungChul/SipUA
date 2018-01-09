package com.zed3.sipua;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.zed3.alarm.MyAlarmManager;
import com.zed3.audio.AudioSettings;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.customgroup.CustomGroupInfoReceiver;
import com.zed3.location.GPSInfoDataBase;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.location.MyHandlerThread;
import com.zed3.log.MyLog;
import com.zed3.media.TipSoundPlayer;
import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.power.MyPowerManager;
import com.zed3.settings.SettingsInfo;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetChangedReceiver;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.VideoManagerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SipUAApp extends Application {
	public static final String PROCESS_BAIDU_SERVICE = "com.zed3.sipua:remote";
	public static final int databases_Maximum = 2000;
	public static GPSInfoDataBase gpsDB = null;
	public static boolean isDepartmentActivity = false;
	public static volatile boolean isHeadsetConnected = false;
	public static boolean isfirst_login = true;
	public static boolean isone_hour = false;
	public static long lastHeadsetConnectTime = 0;
	public static Context mContext = null;
	public static final long one_hour = 3600000;
	public static boolean updateNextTime = false;

	private static SipUAApp mInstance = null;
	private static ThreadPoolExecutor pool = null;
	private static final Handler sMainThreadHandler = new Handler();
	private static final String tag = "SipUAApp";

	AlarmManager alarmManager = null;
	SQLiteDatabase db;
	public BMapManager mBMapManager = null;
	private volatile MyHandlerThread mHandlerThread;
	PendingIntent pi = null;
	private PowerManager powerManager;
	private SharedPreferences settings;
	public final String strKey = "hQzXk2qgLE193GnFd1S5NQi7";
	private WakeLock wakeLock;

	public static void exit() {
		LogUtil.makeLog("SipUAApp", "exit()");
		TipSoundPlayer.getInstance().exit();
		MyAlarmManager.getInstance().exit(mContext);
		MyPowerManager.getInstance().exit(mContext);
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			ZMBluetoothManager.getInstance().exit(mContext);
		}
		NetChangedReceiver.unregisterSelf();
		HeadsetPlugReceiver.stopReceive(mContext);
		ZMBluetoothManager.getInstance().unregisterReceivers(mContext);
		AudioUtil.getInstance().exit();
	}

	public static Context getAppContext() {
		return SipUAApp.mContext;
	}

	public static SipUAApp getInstance() {
		if (SipUAApp.mInstance == null) {
			SipUAApp.mInstance = new SipUAApp();
		}
		return SipUAApp.mInstance;
	}

	public static boolean getIsClosed() {
		SharedPreferences settings = getInstance().getSettings();
		return settings != null && settings.getBoolean("logOnOffKey", false);
	}

	public static double getLastLatitude() {
		return GpsTools.Previous_gps_y;
	}

	public static double getLastLongitude() {
		return GpsTools.Previous_gps_x;
	}

	public static Handler getMainThreadHandler() {
		return sMainThreadHandler;
	}

	public static String getVersion() {
		return getVersion(mContext);
	}

	public static String getVersion(final Context context) {
		String unknown = "Unknown";
		if (context == null) {
			return "Unknown";
		}
		try {
			String ret = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			if (ret.contains(" + ")) {
				return ret.substring(0, ret.indexOf(" + ")) + "b";
			}
			return ret;
		} catch (NameNotFoundException e) {
			return "Unknown";
		}
	}

	private void init() {
		ZMBluetoothManager.getInstance().registerReceivers(mContext);
	}

	private boolean initBluetoothOnOff() {
		return Settings.mNeedBlueTooth = settings.getBoolean(Settings.PREF_BLUETOOTH_ONOFF, false);
	}

	private void initPTime() {
		String ptime = settings.getString(Settings.PTIME_MODE, Settings.DEFAULT_PTIME_MODE);
		if (!TextUtils.isEmpty(ptime) && TextUtils.isDigitsOnly(ptime) && ptime.length() < 4) {
			SettingsInfo.ptime = Integer.parseInt(ptime);
		}
	}

	private boolean initVideoOnOff() {
		return !settings.getString(Settings.PREF_VIDEOCALL_ONOFF, "1").equals("0");
	}

	public static ThreadPoolExecutor getTHreadPool() {
		if (pool == null) {
			pool = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 500L, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
					threadPoolExecutor.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize() + 2);
					threadPoolExecutor.execute(runnable);
				}
			});
		}
		return pool;
	}

	public static void on(Context context, boolean on) {
		if (context == null) {
			context = mContext;
		}
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
		if (on) {
			Receiver.engine(mContext).isRegistered();
		}
	}

	public static boolean on(Context context) {
		if (context == null) {
			context = mContext;
		}
		return Boolean.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_ON, false)).booleanValue();
	}

	public static void restoreVoice() {
		AudioUtil.getInstance().exit();
	}

	private void setmHandlerThread(MyHandlerThread mHandlerThread) {
		synchronized (this) {
			if (this.mHandlerThread == null) {
				(this.mHandlerThread = mHandlerThread).start();
			}
		}
	}

	public static void startHomeActivity(Context context) {
		final Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public void appLogout() {
		UserAgent user = Receiver.GetCurUA();
		if (user.GetCurGrp() != null) {
			user.grouphangup(user.GetCurGrp());
		}
		TalkBackNew.getInstance().unregisterPttGroupChangedReceiver();
		Tools.cleanGrpID();
		Tools.onPreLogOut();
		Receiver.engine(null).expire(-1);
		Receiver.onText(3, null, 0, 0L);
		mContext.getSharedPreferences("notifyInfo", 0).edit().clear().commit();
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Receiver.engine(null).halt();
		DeviceInfo.ISAlarmShowing = false;
		Receiver.alarm(0, OneShotAlarm.class);
		Receiver.alarm(0, MyHeartBeatReceiver.class);
	}

	private String getProcessName() {
		try {
			File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
			BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
			String processName = mBufferedReader.readLine().trim();
			mBufferedReader.close();
			return processName;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public SharedPreferences getSettings() {
		return settings;
	}

	public MyHandlerThread getmHandlerThread() {
		return mHandlerThread;
	}

	public void onCreate() {
		Log.e("Build.MODEL", Build.MODEL);
		String processName = getProcessName();
		mContext = this;

		if (!TextUtils.isEmpty(processName) && processName.equals(PROCESS_BAIDU_SERVICE)) {
			Log.i("testapp", "SipUAApp#onCreate enter process name = " + processName + " return");
			LogUtil.makeLog("SipUAApp", "onCreate() processName " + PROCESS_BAIDU_SERVICE);
			return;
		}

		LogUtil.makeLog(tag, "onCreate()");
		Thread.setDefaultUncaughtExceptionHandler(MyUncaughtExceptionHandler.getInstance(getApplicationContext()));
		// 更新语言
//		LanguageChange.upDateLanguage(mContext);

		// GPS信息
		MemoryMg.getInstance().GpsLocationModel = PreferenceManager.getDefaultSharedPreferences(this).getInt(Settings.PREF_LOCATEMODE, 3);
		MemoryMg.getInstance().GpsLocationModel_EN = PreferenceManager.getDefaultSharedPreferences(this).getInt(Settings.PREF_LOCATEMODE_EN, 3);
		try {
			LocalConfigSettings.loadSettings(this);
			AutoConfigManager.LoadSettings(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		settings = getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		DeviceVideoInfo.supportRotate = this.settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_ROTATE, false);
		DeviceVideoInfo.supportFullScreen = this.settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_FULLSCREEN, false);
		DeviceVideoInfo.isHorizontal = this.settings.getBoolean(DeviceVideoInfo.VIDEO_SUPPORT_LAND, false);
		DeviceVideoInfo.color_correct = this.settings.getBoolean(DeviceVideoInfo.VIDEO_COLOR_CORRECT, false);
		DeviceVideoInfo.screen_type = this.settings.getString(DeviceVideoInfo.SCREEN_TYPE, DeviceVideoInfo.DEFAULT_SCREEN_TYPE);
		if (DeviceVideoInfo.screen_type.equals("ver")) {
			DeviceVideoInfo.isHorizontal = false;
			DeviceVideoInfo.supportRotate = false;
			DeviceVideoInfo.onlyCameraRotate = true;
		} else if (DeviceVideoInfo.screen_type.equals(DeviceVideoInfo.DEFAULT_SCREEN_TYPE)) {
			DeviceVideoInfo.isHorizontal = true;
			DeviceVideoInfo.supportRotate = false;
			DeviceVideoInfo.onlyCameraRotate = true;
		} else {
			DeviceVideoInfo.isHorizontal = false;
			DeviceVideoInfo.supportRotate = true;
			DeviceVideoInfo.onlyCameraRotate = false;
		}
		AudioSettings.isAECOpen = this.settings.getBoolean(DeviceVideoInfo.AUDIO_AEC_SWITCH, true);
		AudioSettings.isAGCOpen = this.settings.getBoolean(DeviceVideoInfo.AUDIO_AGC_SWITCH, false);
		DeviceVideoInfo.lostLevel = this.settings.getInt(DeviceVideoInfo.PACKET_LOST_LEVEL, 1);
		mContext = getApplicationContext();
		initPTime();
		Settings.needVideoCall = initVideoOnOff();
		Settings.mNeedBlueTooth = initBluetoothOnOff();
		super.onCreate();
		TipSoundPlayer.getInstance().init(getApplicationContext());
		MyAlarmManager.getInstance().init(getApplicationContext());
		MyPowerManager.getInstance().init(getApplicationContext());
		VideoManagerService.getDefault().init(this);
		CallManager.getManager();
		// 启动GPS
//		resumeGpsThread();

		// 注册组列表接收服务器
		GroupListUtil.registerReceiver();

		// 注册定制组接收服务器
		CustomGroupInfoReceiver.register(mContext);

		// 注册GPS时间变化服务
//		GpsTools.registerTimeChangedReceiver();

		// 初始化蓝牙
		init();

		// 百度地图
//		if (this.mBMapManager == null) {
//			this.mBMapManager = new BMapManager(this);
//		}
//		this.mBMapManager.init(new MyGeneralListener());
		mInstance = this;

		// 测试
//		if (Build.VERSION.SDK_INT >= 16) {
//			DeviceInfo.isSupportHWChange = new PhoneSupportTest().startTest();
//		}

		MyHeartBeatReceiver.MyHeartBeatMessageHandler.createInstance();
		this.powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(26, "WAKE_LOCK");
	}

	public void onLowMemory() {
		LogUtil.makeLog("SipUAApp", "SipUAApp#onLowMemory()");
		super.onLowMemory();
	}

	public void onTerminate() {
		LogUtil.makeLog("SipUAApp", "SipUAApp#onTerminate()");
		final Intent intent = new Intent();
		intent.setAction("com.zed3.flow.FlowRefreshService");
		this.stopService(intent);
		GroupListUtil.unRegisterReceiver();
		CustomGroupInfoReceiver.unRegister(SipUAApp.mContext);
		GpsTools.unRegisterTimeChangedReceiver();
		super.onTerminate();
	}

	public void resumeGpsThread() {
		if (this.mHandlerThread == null) {
			this.setmHandlerThread(new MyHandlerThread("GpsThread"));
		}
	}

	public void setSettings(final SharedPreferences settings) {
		this.settings = settings;
	}

	public void stopGpsThread() {
		if (this.mHandlerThread != null) {
			this.mHandlerThread.stopSelf();
			this.mHandlerThread = null;
		}
	}

	public void wakeLock(final boolean b) {
		if (b && !this.wakeLock.isHeld()) {
			this.wakeLock.acquire();
		} else if (!b && this.wakeLock.isHeld()) {
			this.wakeLock.release();
		}
	}

	class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(final int n) {
			if (n == 2) {
				MyLog.e("SIPUAAPP", "baidu map network error");
			} else if (n == 3) {
				MyLog.e("SIPUAAPP", "baidu map network data error");
			}
		}

		@Override
		public void onGetPermissionState(final int n) {
			if (n != 0) {
				MyLog.e("SIPUAAPP", "baidu map key error");
			}
		}
	}
}
