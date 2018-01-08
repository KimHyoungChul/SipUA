package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

public class SosPttListener extends AbstractPttListenser {
	static final String ACTION_SOS_DOWN = "android.intent.action.SOS";
	private static final long SOS_LIMIT_TIME = 5000L;
	private long sLastSosCallTime;

	public SosPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
		this.sLastSosCallTime = 0L;
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("android.intent.action.SOS");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		Log.i("GUOK", "SosPttListener Action " + intent.getAction());
		final String action = intent.getAction();
		if (action.equals("android.intent.action.SOS")) {
			int n = 0;
			int repeatCount = 0;
			final Object value = intent.getExtras().get("android.intent.extra.KEY_EVENT");
			if (value instanceof Integer) {
				n = (int) value;
			} else if (value instanceof KeyEvent) {
				n = ((KeyEvent) intent.getExtras().get("android.intent.extra.KEY_EVENT")).getAction();
				repeatCount = ((KeyEvent) intent.getExtras().get("android.intent.extra.KEY_EVENT")).getRepeatCount();
			}
			Log.i("GUOK", "Action " + n);
			if (repeatCount == 0 && n == 0) {
				LogUtil.makeLog("dd", "PttBroadcastReceiver#onReceive action " + action);
				if (DeviceInfo.svpnumber.equals("")) {
					Toast.makeText(Receiver.mContext, R.string.unavailable_num, Toast.LENGTH_SHORT).show();
					return true;
				}
				final long currentTimeMillis = System.currentTimeMillis();
				final long sLastSosCallTime = this.sLastSosCallTime;
				if (this.sLastSosCallTime != 0L && Long.valueOf(currentTimeMillis - sLastSosCallTime) < 5000L) {
					return true;
				}
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						Looper.prepare();
						CallUtil.makeSOSCall(Receiver.mContext, DeviceInfo.svpnumber, null);
						LogUtil.makeLog(" PttBoradCastPTT", " CallUtil.makeAudioCall(Receiver.mContext,DeviceInfo.svpnumber, null)");
					}
				}, 2000);
				this.sLastSosCallTime = System.currentTimeMillis();
			}
			return true;
		}
		return false;
	}
}
