package com.zed3.sipua;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
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
import com.zed3.utils.LanguageChange;
import com.zed3.utils.LogUtil;
import com.zed3.utils.NetChangedReceiver;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.PhoneSupportTest;
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
	static final String PROCESS_BAIDU_SERVICE = "com.zed3.sipua:remote";
	public static final int databases_Maximum = 2000;
	public static GPSInfoDataBase gpsDB;
	public static boolean isDepartmentActivity = false;
	public static volatile boolean isHeadsetConnected = false;
	public static boolean isfirst_login = false;
	public static boolean isone_hour = false;
	public static long lastHeadsetConnectTime = 0L;
	public static Context mContext;
	private static SipUAApp mInstance;
	public static final long one_hour = 3600000L;
	private static ThreadPoolExecutor pool;
	private static final Handler sMainThreadHandler;
	private static final String tag = "SipUAApp";
	public static boolean updateNextTime;
	AlarmManager alarmManager;
	SQLiteDatabase db;
	public BMapManager mBMapManager;
	private volatile MyHandlerThread mHandlerThread;
	PendingIntent pi;
	private PowerManager powerManager;
	private SharedPreferences settings;
	public final String strKey;
	private PowerManager.WakeLock wakeLock;

	static {
		SipUAApp.isDepartmentActivity = false;
		SipUAApp.updateNextTime = false;
		SipUAApp.isfirst_login = true;
		SipUAApp.isone_hour = false;
		sMainThreadHandler = new Handler();
		SipUAApp.mInstance = null;
	}

	public SipUAApp() {
		this.mBMapManager = null;
		this.strKey = "hQzXk2qgLE193GnFd1S5NQi7";
		this.alarmManager = null;
		this.pi = null;
	}

	public static void exit() {
		LogUtil.makeLog("SipUAApp", "exit()");
		TipSoundPlayer.getInstance().exit();
		MyAlarmManager.getInstance().exit(SipUAApp.mContext);
		MyPowerManager.getInstance().exit(SipUAApp.mContext);
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			ZMBluetoothManager.getInstance().exit(SipUAApp.mContext);
		}
		NetChangedReceiver.unregisterSelf();
		HeadsetPlugReceiver.stopReceive(SipUAApp.mContext);
		ZMBluetoothManager.getInstance().unregisterReceivers(SipUAApp.mContext);
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
		final SharedPreferences settings = getInstance().getSettings();
		return settings != null && settings.getBoolean("logOnOffKey", false);
	}

	public static double getLastLatitude() {
		return GpsTools.Previous_gps_y;
	}

	public static double getLastLongitude() {
		return GpsTools.Previous_gps_x;
	}

	public static Handler getMainThreadHandler() {
		return SipUAApp.sMainThreadHandler;
	}

	public static ThreadPoolExecutor getTHreadPool() {
		if (SipUAApp.pool == null) {
			SipUAApp.pool = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 500L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor threadPoolExecutor) {
					threadPoolExecutor.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize() + 2);
					threadPoolExecutor.execute(runnable);
				}
			});
		}
		return SipUAApp.pool;
	}

	public static String getVersion() {
		return getVersion(SipUAApp.mContext);
	}

	public static String getVersion(final Context context) {
		String versionName;
		if (context == null) {
			versionName = "Unknown";
		} else {
			try {
				final String s = versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				if (s.contains(" + ")) {
					return String.valueOf(s.substring(0, s.indexOf(" + "))) + "b";
				}
			} catch (PackageManager.NameNotFoundException ex) {
				return "Unknown";
			}
		}
		return versionName;
	}

	private void init() {
		ZMBluetoothManager.getInstance().registerReceivers(SipUAApp.mContext);
	}

	private boolean initBluetoothOnOff() {
		return Settings.mNeedBlueTooth = this.settings.getBoolean("bluetoothonoff", false);
	}

	private void initPTime() {
		final String string = this.settings.getString("ptime", "20");
		if (!TextUtils.isEmpty((CharSequence) string) && TextUtils.isDigitsOnly((CharSequence) string) && string.length() < 4) {
			SettingsInfo.ptime = Integer.parseInt(string);
		}
	}

	private boolean initVideoOnOff() {
		return !this.settings.getString("videoCallKey", "1").equals("0");
	}

	public static void on(final Context context, final boolean b) {
		Context mContext = context;
		if (context == null) {
			mContext = SipUAApp.mContext;
		}
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
		edit.putBoolean("on", b);
		edit.commit();
		if (b) {
			Receiver.engine(mContext).isRegistered();
		}
	}

	public static boolean on(final Context context) {
		Context mContext = context;
		if (context == null) {
			mContext = SipUAApp.mContext;
		}
		return Boolean.valueOf(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("on", false));
	}

	public static void restoreVoice() {
		AudioUtil.getInstance().exit();
	}

	private void setmHandlerThread(final MyHandlerThread mHandlerThread) {
		synchronized (this) {
			if (this.mHandlerThread == null) {
				(this.mHandlerThread = mHandlerThread).start();
			}
		}
	}

	public static void startHomeActivity(final Context context) {
		final Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public void appLogout() {
		final UserAgent getCurUA = Receiver.GetCurUA();
		if (getCurUA.GetCurGrp() != null) {
			getCurUA.grouphangup(getCurUA.GetCurGrp());
		}
		TalkBackNew.getInstance().unregisterPttGroupChangedReceiver();
		Tools.cleanGrpID();
		Tools.onPreLogOut();
		Receiver.engine(null).expire(-1);
		Receiver.onText(3, null, 0, 0L);
		SipUAApp.mContext.getSharedPreferences("notifyInfo", 0).edit().clear().commit();
		while (true) {
			try {
				Thread.sleep(800L);
				Receiver.engine(null).halt();
				DeviceInfo.ISAlarmShowing = false;
				Receiver.alarm(0, OneShotAlarm.class);
				Receiver.alarm(0, MyHeartBeatReceiver.class);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
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
		return this.settings;
	}

	public MyHandlerThread getmHandlerThread() {
		return this.mHandlerThread;
	}

	public void onCreate() {
		Log.e("Build.MODEL", Build.MODEL);
		String processName = getProcessName();
		mContext = (Context) this;

		if (!TextUtils.isEmpty(processName) && processName.equals(PROCESS_BAIDU_SERVICE)) {
			Log.i("testapp", "SipUAApp#onCreate enter process name = " + processName + " return");
			LogUtil.makeLog("SipUAApp", "onCreate() processName com.zed3.sipua:remote");
			return;
		}
		LogUtil.makeLog(tag, "onCreate()");
		Thread.setDefaultUncaughtExceptionHandler(MyUncaughtExceptionHandler.getInstance(getApplicationContext()));
		LanguageChange.upDateLanguage(mContext);
		MemoryMg.getInstance().GpsLocationModel = PreferenceManager.getDefaultSharedPreferences(this).getInt(Settings.PREF_LOCATEMODE, 3);
		MemoryMg.getInstance().GpsLocationModel_EN = PreferenceManager.getDefaultSharedPreferences(this).getInt(Settings.PREF_LOCATEMODE_EN, 3);
		try {
			LocalConfigSettings.loadSettings(this);
			AutoConfigManager.LoadSettings(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.settings = getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
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
		if (Build.VERSION.SDK_INT >= 16) {
			DeviceInfo.isSupportHWChange = new PhoneSupportTest().startTest();
		}
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
