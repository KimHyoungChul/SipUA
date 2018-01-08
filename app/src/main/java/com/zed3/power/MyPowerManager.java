package com.zed3.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.alarm.MyAlarmManager;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.utils.LogUtil;

import java.text.SimpleDateFormat;

public class MyPowerManager {
	public static final String KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX = "screen_wakeup_period_index";
	public static final int SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX = 0;
	private static final String TAG = "MyPowerManager";
	private static Context mContext;
	private PowerManager.WakeLock cpuWakeLock;
	private boolean isStarted;
	private boolean mIsInited;
	private PowerManager mPowerMananger;
	private String mScreamWakeLockAcquireTag;
	public long mScreenOffTime;
	ScreenOnOffStateReceiver mScreenOnOffStateReceiver;
	private int mScreenWakeupCount;
	private int mScreenWakeupPeriod;
	private SharedPreferences mSharedPreferences;
	private PowerManager.WakeLock mWakeLock;
	int[] screenWakeupPeriods;
	private SimpleDateFormat simpleDateFormat;

	private MyPowerManager() {
		this.mScreenWakeupPeriod = 0;
		this.mScreenWakeupCount = 0;
		this.simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
		this.screenWakeupPeriods = new int[]{0, 1, 5, 10, 20, 30};
		this.mScreenOnOffStateReceiver = new ScreenOnOffStateReceiver();
	}

	public static MyPowerManager getInstance() {
		return InstanceCreater.sInstance;
	}

	private void makeLog(final String s, final String s2) {
		LogUtil.makeLog(s, s2);
	}

	private void startReceive(final Context context) {
		if (!this.isStarted) {
			this.isStarted = true;
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("android.intent.action.SCREEN_ON");
			intentFilter.addAction("android.intent.action.SCREEN_OFF");
			context.registerReceiver((BroadcastReceiver) this.mScreenOnOffStateReceiver, intentFilter);
		}
	}

	private void stopReceive(final Context context) {
		if (this.isStarted) {
			this.isStarted = false;
			context.unregisterReceiver((BroadcastReceiver) this.mScreenOnOffStateReceiver);
		}
	}

	public void acquireCpuWakeLock(String s) {
		// TODO
//		s = (String) new StringBuilder("acquireCpuWakeLock(" + s + ")");
//		try {
//			if (!this.cpuWakeLock.isHeld()) {
//				this.cpuWakeLock.acquire();
//			} else {
//				((StringBuilder) s).append(" cpuWakeLock.isHeld() is true ignore");
//			}
//		} catch (Exception ex) {
//			((StringBuilder) s).append(" Exception " + ex.getMessage());
//			MyUncaughtExceptionHandler.saveExceptionLog(ex);
//		} finally {
//			LogUtil.makeLog("MyPowerManager", ((StringBuilder) s).toString());
//		}
	}

	public boolean exit(final Context context) {
		if (this.mIsInited) {
			this.mIsInited = false;
			this.makeLog("MyPowerManager", "MyPowerManager.exit() begin");
			this.stopReceive(context);
			this.mScreenWakeupCount = 0;
			this.mScreenWakeupPeriod = 0;
			this.releaseCpuWakeLock("MyPowerManager");
			this.makeLog("MyPowerManager", "MyPowerManager.exit() end");
			return false;
		}
		this.makeLog("MyPowerManager", "MyPowerManager.exit() mIsInited is false ignore");
		return false;
	}

	public int getScreenWakeupPeriodFromArray(final int n) {
		return this.screenWakeupPeriods[n];
	}

	public boolean init(final Context mContext) {
		MyPowerManager.mContext = mContext;
		if (!this.mIsInited) {
			this.mIsInited = true;
			this.makeLog("MyPowerManager", "MyPowerManager.init() begin");
			this.mSharedPreferences = MyPowerManager.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
			this.setScreenWakeupPeriod(this.getScreenWakeupPeriodFromArray(this.mSharedPreferences.getInt("screen_wakeup_period_index", 0)));
			this.startReceive(MyPowerManager.mContext);
			this.mPowerMananger = (PowerManager) MyPowerManager.mContext.getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = this.mPowerMananger.newWakeLock(268435482, "MyPowerManager");
			this.cpuWakeLock = this.mPowerMananger.newWakeLock(1, "MyPowerManager");
			this.mPowerMananger.isScreenOn();
			Log.e("TANGJIAN", "\u5524\u9192\u5c4f\u5e55\uff01MyPowerManager");
			this.acquireCpuWakeLock("MyPowerManager");
			this.makeLog("MyPowerManager", "MyPowerManager.init() end");
			return false;
		}
		this.makeLog("MyPowerManager", "MyPowerManager.init() mIsInited is true ignore");
		return false;
	}

	public boolean isScreenOn() {
		boolean screenOn = false;
		if (this.mPowerMananger != null) {
			screenOn = this.mPowerMananger.isScreenOn();
		}
		return screenOn;
	}

	public void releaseCpuWakeLock(String s) {
		// TODO
//		s = (String) new StringBuilder("releaseCpuWakeLock(" + s + ")");
//		try {
//			if (this.cpuWakeLock.isHeld()) {
//				this.cpuWakeLock.release();
//			} else {
//				((StringBuilder) s).append(" cpuWakeLock.isHeld() is false ignore");
//			}
//		} catch (Exception ex) {
//			((StringBuilder) s).append(" Exception " + ex.getMessage());
//			MyUncaughtExceptionHandler.saveExceptionLog(ex);
//		} finally {
//			LogUtil.makeLog("MyPowerManager", ((StringBuilder) s).toString());
//		}
	}

	public void releaseScreenWakeLock(final String s) {
		// monitorenter(this)
		try {
			final StringBuilder sb = new StringBuilder("releaseScreenWakeLock(" + s + ")");
			sb.append(" mScreamWakeLockAcquireTag " + this.mScreamWakeLockAcquireTag);
			Label_0184:
			{
				try {
					if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
						if (!TextUtils.isEmpty((CharSequence) this.mScreamWakeLockAcquireTag) && !TextUtils.isEmpty((CharSequence) s) && this.mScreamWakeLockAcquireTag.equals(s)) {
							this.mWakeLock.release();
							sb.append(" release ");
						} else {
							sb.append(" ignore ");
						}
						LogUtil.makeLog("MyPowerManager", sb.toString());
						return;
					}
					break Label_0184;
				} catch (Exception ex) {
					sb.append(" Exception " + ex.getMessage());
					MyUncaughtExceptionHandler.saveExceptionLog(ex);
					LogUtil.makeLog("MyPowerManager", sb.toString());
					sb.append(" ignore ");
				} finally {
					LogUtil.makeLog("MyPowerManager", sb.toString());
				}
			}
		} finally {
		}
	}

	public void setScreenWakeupPeriod(final int mScreenWakeupPeriod) {
		final StringBuilder sb = new StringBuilder("setScreenWakeupPeriod(" + mScreenWakeupPeriod + ")");
		this.mScreenWakeupPeriod = mScreenWakeupPeriod;
		if (this.mScreenWakeupPeriod > 0) {
			sb.append(" ");
			this.mScreenWakeupCount = 0;
		}
		LogUtil.makeLog("TANGJIAN", "setScreenWakeupPeriod(" + mScreenWakeupPeriod + ")");
	}

	public String wakeupScreen(String wakeupScreen) {
		synchronized (this) {
			wakeupScreen = this.wakeupScreen(wakeupScreen, -1);
			return wakeupScreen;
		}
	}

	public String wakeupScreen(final String p0, final int p1) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: monitorenter
		//     2: new             Ljava/lang/StringBuilder;
		//     5: dup
		//     6: new             Ljava/lang/StringBuilder;
		//     9: dup
		//    10: ldc_w           "wakeupScreen("
		//    13: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    16: aload_1
		//    17: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    20: ldc_w           ","
		//    23: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    26: iload_2
		//    27: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//    30: ldc             ")"
		//    32: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    35: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    38: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    41: astore          5
		//    43: aload_0
		//    44: aload_0
		//    45: getfield        com/zed3/power/MyPowerManager.mScreamWakeLockAcquireTag:Ljava/lang/String;
		//    48: invokevirtual   com/zed3/power/MyPowerManager.releaseScreenWakeLock:(Ljava/lang/String;)V
		//    51: iload_2
		//    52: ifle            223
		//    55: aload_0
		//    56: getfield        com/zed3/power/MyPowerManager.mWakeLock:Landroid/os/PowerManager.WakeLock;
		//    59: iload_2
		//    60: i2l
		//    61: invokevirtual   android/os/PowerManager.WakeLock.acquire:(J)V
		//    64: aload_0
		//    65: aload_0
		//    66: getfield        com/zed3/power/MyPowerManager.mScreenWakeupCount:I
		//    69: iconst_1
		//    70: iadd
		//    71: putfield        com/zed3/power/MyPowerManager.mScreenWakeupCount:I
		//    74: invokestatic    java/lang/System.currentTimeMillis:()J
		//    77: lstore_3
		//    78: aload           5
		//    80: new             Ljava/lang/StringBuilder;
		//    83: dup
		//    84: ldc_w           " count "
		//    87: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    90: aload_0
		//    91: getfield        com/zed3/power/MyPowerManager.mScreenWakeupCount:I
		//    94: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//    97: ldc_w           " time "
		//   100: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   103: aload_0
		//   104: getfield        com/zed3/power/MyPowerManager.simpleDateFormat:Ljava/text/SimpleDateFormat;
		//   107: new             Ljava/util/Date;
		//   110: dup
		//   111: lload_3
		//   112: invokespecial   java/util/Date.<init>:(J)V
		//   115: invokevirtual   java/text/SimpleDateFormat.format:(Ljava/util/Date;)Ljava/lang/String;
		//   118: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   121: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   124: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   127: pop
		//   128: aload_0
		//   129: getfield        com/zed3/power/MyPowerManager.mScreenOffTime:J
		//   132: lconst_0
		//   133: lcmp
		//   134: ifle            177
		//   137: aload           5
		//   139: new             Ljava/lang/StringBuilder;
		//   142: dup
		//   143: ldc_w           " screenOffTime "
		//   146: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   149: aload_0
		//   150: getfield        com/zed3/power/MyPowerManager.simpleDateFormat:Ljava/text/SimpleDateFormat;
		//   153: new             Ljava/util/Date;
		//   156: dup
		//   157: aload_0
		//   158: getfield        com/zed3/power/MyPowerManager.mScreenOffTime:J
		//   161: invokespecial   java/util/Date.<init>:(J)V
		//   164: invokevirtual   java/text/SimpleDateFormat.format:(Ljava/util/Date;)Ljava/lang/String;
		//   167: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   170: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   173: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   176: pop
		//   177: aload           5
		//   179: new             Ljava/lang/StringBuilder;
		//   182: dup
		//   183: ldc_w           " mScreenWakeupPeriod "
		//   186: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   189: aload_0
		//   190: getfield        com/zed3/power/MyPowerManager.mScreenWakeupPeriod:I
		//   193: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   196: ldc_w           " m"
		//   199: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   202: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   205: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   208: pop
		//   209: ldc             "MyPowerManager"
		//   211: aload           5
		//   213: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   216: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//   219: aload_0
		//   220: monitorexit
		//   221: aload_1
		//   222: areturn
		//   223: aload_0
		//   224: getfield        com/zed3/power/MyPowerManager.mWakeLock:Landroid/os/PowerManager.WakeLock;
		//   227: invokevirtual   android/os/PowerManager.WakeLock.acquire:()V
		//   230: aload_0
		//   231: aload_1
		//   232: putfield        com/zed3/power/MyPowerManager.mScreamWakeLockAcquireTag:Ljava/lang/String;
		//   235: goto            64
		//   238: astore          6
		//   240: aload           5
		//   242: new             Ljava/lang/StringBuilder;
		//   245: dup
		//   246: ldc             " Exception "
		//   248: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   251: aload           6
		//   253: invokevirtual   java/lang/Exception.getMessage:()Ljava/lang/String;
		//   256: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   259: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   262: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   265: pop
		//   266: aload           6
		//   268: invokestatic    com/zed3/sipua/exception/MyUncaughtExceptionHandler.saveExceptionLog:(Ljava/lang/Throwable;)V
		//   271: ldc             "MyPowerManager"
		//   273: aload           5
		//   275: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   278: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//   281: goto            219
		//   284: astore_1
		//   285: aload_0
		//   286: monitorexit
		//   287: aload_1
		//   288: athrow
		//   289: astore_1
		//   290: ldc             "MyPowerManager"
		//   292: aload           5
		//   294: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   297: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//   300: aload_1
		//   301: athrow
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  2      43     284    289    Any
		//  43     51     238    284    Ljava/lang/Exception;
		//  43     51     289    302    Any
		//  55     64     238    284    Ljava/lang/Exception;
		//  55     64     289    302    Any
		//  64     177    238    284    Ljava/lang/Exception;
		//  64     177    289    302    Any
		//  177    209    238    284    Ljava/lang/Exception;
		//  177    209    289    302    Any
		//  209    219    284    289    Any
		//  223    235    238    284    Ljava/lang/Exception;
		//  223    235    289    302    Any
		//  240    271    289    302    Any
		//  271    281    284    289    Any
		//  290    302    284    289    Any
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0064:
		//     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
		//     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2596)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		//     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		//     at us.deathmarine.luyten.FileSaver.access.300(FileSaver.java:45)
		//     at us.deathmarine.luyten.FileSaver.4.run(FileSaver.java:112)
		//     at java.lang.Thread.run(Thread.java:745)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
	}

	private static final class InstanceCreater {
		public static MyPowerManager sInstance;

		static {
			InstanceCreater.sInstance = new MyPowerManager();
		}
	}

	class ScreenOnOffStateReceiver extends BroadcastReceiver {
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			Log.e("TANGJIAN", "ACTION:" + action);
			final StringBuilder sb = new StringBuilder("ScreenOnOffStateReceiver#onReceive() " + action);
			if (action.equals("android.intent.action.SCREEN_ON")) {
				MyHeartBeatReceiver.process();
				Log.e("TANGJIAN", "\u5fc3\u8df3\u5305\u5df2\u53d1\uff01\uff01");
				MyAlarmManager.getInstance().cancelAlarm(ScreenWakeupActionReceiver.class);
			} else if (action.equals("android.intent.action.SCREEN_OFF")) {
				MyPowerManager.this.mScreenOffTime = System.currentTimeMillis();
				if (MyPowerManager.this.mScreenWakeupPeriod > 0) {
					MyAlarmManager.getInstance().setAlarm(MyPowerManager.this.mScreenWakeupPeriod * 60, ScreenWakeupActionReceiver.class);
				}
			}
			MyPowerManager.this.makeLog("MyPowerManager", sb.toString());
		}
	}
}
