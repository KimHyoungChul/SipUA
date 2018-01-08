package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

public class AlarmPttListener extends AbstractPttListenser {
	public static final String ACTION_PTT_DOWN = "com.zed3.action.ALARM_EMERGENCY_DOWN";

	public AlarmPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("com.zed3.action.ALARM_EMERGENCY_DOWN");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		Log.i("GUOK", "AlarmPttListener Action " + intent.getAction());
		if (!"com.zed3.action.ALARM_EMERGENCY_DOWN".equals(action)) {
			return false;
		}
		Log.i("GUOK", "AlarmPttListener ptt down");
		if (DeviceInfo.svpnumber.equals("")) {
			MyToast.showToast(true, Receiver.mContext, Receiver.mContext.getString(R.string.unavailable_cno));
			return true;
		}
		DeviceInfo.isEmergency = true;
		CallUtil.makeSOSCall(Receiver.mContext, DeviceInfo.svpnumber, null);
		return true;
	}
}
