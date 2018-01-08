package com.zed3.sipua.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.MessageDialogueActivity;

public class RegisterService extends Service {
	private Context mContext;
	Caller m_caller;
	Receiver m_receiver;

	private boolean existUnixTime() {
		boolean b = false;
		final long long1 = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getLong("serverUnixTime", -1L);
		MyLog.d("testgps", "RegisterService#existUnixTime() unixTime = " + long1);
		if (long1 > 0L) {
			b = true;
		}
		return b;
	}

	private boolean isEnableGps() {
		boolean b = false;
		final int int1 = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("locateModle", 3);
		MyLog.d("testgps", "RegisterService#isEnableGps mode = " + int1);
		if (int1 != 3) {
			b = true;
		}
		return b;
	}

	public IBinder onBind(final Intent intent) {
		return null;
	}

	public void onCreate() {
		MyLog.d("testgps", "RegisterService#onCreate enter");
		super.onCreate();
		this.mContext = this.getApplicationContext();
		if (this.m_receiver == null) {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
			intentFilter.addAction("android.intent.action.PHONE_STATE");
			intentFilter.addAction("android.intent.action.DOCK_EVENT");
			intentFilter.addAction("android.intent.action.HEADSET_PLUG");
			intentFilter.addAction("android.intent.action.USER_PRESENT");
			intentFilter.addAction("vpn.connectivity");
			intentFilter.addAction("android.media.SCO_AUDIO_STATE_CHANGED");
			intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
			intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
			intentFilter.addAction("android.intent.action.PTT.down");
			intentFilter.addAction("android.intent.action.PTT.up");
			intentFilter.addAction(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
			intentFilter.addAction(MessageDialogueActivity.SEND_TEXT_FAIL);
			intentFilter.addAction(MessageDialogueActivity.SEND_TEXT_SUCCEED);
			intentFilter.addAction(MessageDialogueActivity.SEND_TEXT_TIMEOUT);
			this.registerReceiver((BroadcastReceiver) (this.m_receiver = new Receiver()), intentFilter);
		}
		if (this.m_caller == null) {
			final IntentFilter intentFilter2 = new IntentFilter();
			intentFilter2.addAction("android.intent.action.NEW_OUTGOING_CALL");
			this.registerReceiver((BroadcastReceiver) (this.m_caller = new Caller()), intentFilter2);
		}
		Receiver.engine((Context) this).isRegistered();
		final UserAgent getCurUA = Receiver.engine((Context) this).GetCurUA();
		MyLog.d("testgps", "RegisterService#onCreate currentUserAgent = " + getCurUA);
		if (getCurUA != null) {
			final boolean enableGps = this.isEnableGps();
			MyLog.d("testgps", "RegisterService#onCreate enableGps = " + enableGps);
			final boolean existUnixTime = this.existUnixTime();
			MyLog.d("testgps", "RegisterService#onCreate existUnixTime = " + existUnixTime);
			if (enableGps && existUnixTime) {
				MyLog.d("testgps", "RegisterService#onCreate is open gps = " + getCurUA.isOpenGps());
				if (!getCurUA.isOpenGps()) {
					MyLog.d("testgps", "RegisterService#onCreate prepare open gps");
					getCurUA.GPSOpenLock();
				}
			}
		}
		MyLog.d("testgps", "RegisterService#onCreate exit");
	}

	public void onDestroy() {
		super.onDestroy();
		if (this.m_receiver != null) {
			this.unregisterReceiver((BroadcastReceiver) this.m_receiver);
			this.m_receiver = null;
		}
		if (this.m_caller != null) {
			this.unregisterReceiver((BroadcastReceiver) this.m_caller);
			this.m_caller = null;
		}
		Receiver.alarm(0, OneShotAlarm2.class);
	}

	public void onStart(final Intent intent, final int n) {
		super.onStart(intent, n);
	}

	public int onStartCommand(final Intent intent, final int n, final int n2) {
		if (intent != null && intent.getBooleanExtra("hasgps", false)) {
			if (intent.getBooleanExtra("gpsopen", false)) {
				Receiver.GetCurUA().GPSOpenLock();
			} else {
				Receiver.GetCurUA().GPSCloseLock();
			}
		}
		return super.onStartCommand(intent, n, n2);
	}
}
