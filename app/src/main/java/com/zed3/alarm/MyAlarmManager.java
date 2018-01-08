package com.zed3.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.exception.MyUncaughtExceptionHandler;
import com.zed3.utils.LogUtil;

import java.util.Calendar;

public class MyAlarmManager {
	private static final String TAG = "MyAlarmManager";
	private static Context mContext;
	private AlarmManager mAlarmManager;
	private boolean mIsInited;

	public static MyAlarmManager getInstance() {
		if (MyAlarmManager.mContext == null) {
			MyAlarmManager.mContext = SipUAApp.getAppContext();
		}
		return InstanceCreater.sInstance;
	}

	private static boolean isXiaoMI() {
		return Build.MODEL.contains("MI 1S") || Build.MODEL.contains("MI 2S") || Build.MODEL.contains("HUAWEI G700-U00") || Build.MODEL.contains("HUAWEI P6-U06") || Build.MODEL.contains("HUAWEI MT1-U06") || Build.MODEL.contains("HUAWEI Y511-T00");
	}

	private void makeLog(final String s, final String s2) {
		LogUtil.makeLog(s, s2);
	}

	public void cancelAlarm(final Class<?> p0) {
		// TODO
	}

	public boolean exit(final Context context) {
		synchronized (this) {
			if (this.mIsInited) {
				this.mIsInited = false;
				this.makeLog("MyAlarmManager", "MyPowerManager.exit() begin");
				this.makeLog("MyAlarmManager", "MyPowerManager.exit() end");
			} else {
				this.makeLog("MyAlarmManager", "MyPowerManager.exit() mIsInited is false ignore");
			}
			return false;
		}
	}

	public boolean init(final Context mContext) {
		synchronized (this) {
			MyAlarmManager.mContext = mContext;
			if (!this.mIsInited) {
				this.mIsInited = true;
				this.makeLog("MyAlarmManager", "MyPowerManager.init() begin");
				this.mAlarmManager = (AlarmManager) MyAlarmManager.mContext.getSystemService(Context.ALARM_SERVICE);
				this.makeLog("MyAlarmManager", "MyPowerManager.init() end");
			} else {
				this.makeLog("MyAlarmManager", "MyPowerManager.init() mIsInited is true ignore");
			}
			return false;
		}
	}

	public void setAlarm(final int n, final Class<?> clazz) {
		// monitorenter(this)
		try {
			final StringBuilder sb = new StringBuilder("setAlarm(" + n + "," + clazz + ")");
			Label_0187:
			{
				try {
					final PendingIntent broadcast = PendingIntent.getBroadcast(MyAlarmManager.mContext, 0, new Intent(MyAlarmManager.mContext, (Class) clazz), 0);
					if (n > 0) {
						if (isXiaoMI()) {
							final Calendar instance = Calendar.getInstance();
							instance.setTimeInMillis(System.currentTimeMillis());
							instance.add(Calendar.SECOND, n);
							this.mAlarmManager.set(AlarmManager.RTC, instance.getTimeInMillis(), broadcast);
						} else {
							this.mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + n * 1000, broadcast);
						}
						LogUtil.makeLog("MyAlarmManager", sb.toString());
						return;
					}
				} catch (Exception ex) {
					sb.append(" Exception " + ex.getMessage());
					MyUncaughtExceptionHandler.saveExceptionLog(ex);
					LogUtil.makeLog("MyAlarmManager", sb.toString());
//                    final PendingIntent pendingIntent;
//                    this.mAlarmManager.cancel(pendingIntent);
					sb.append(" cancel alarm");
				} finally {
					LogUtil.makeLog("MyAlarmManager", sb.toString());
				}
			}
		} finally {
		}
	}

	private static final class InstanceCreater {
		public static MyAlarmManager sInstance;

		static {
			InstanceCreater.sInstance = new MyAlarmManager();
		}
	}
}
