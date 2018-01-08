package com.zed3.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatReceiver extends BroadcastReceiver {
	private static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
	private static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
	private static final String PTT_PA_OFF = "PA_OFF";
	private static final String PTT_PA_ON = "PA_ON";
	private static final String TAG = "PhoneStatReceiver";
	private static boolean incomingFlag;
	private static String incoming_number;
	private static IntentFilter intentFilter;
	private static boolean isStarted;
	private static PhoneStatReceiver mReceiver;
	private ZMBluetoothManager mInstance;

	static {
		PhoneStatReceiver.incomingFlag = false;
		PhoneStatReceiver.incoming_number = null;
		PhoneStatReceiver.mReceiver = new PhoneStatReceiver();
		(PhoneStatReceiver.intentFilter = new IntentFilter()).addAction("android.intent.action.PHONE_STATE");
		PhoneStatReceiver.intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
	}

	public static void startReceive(final Context context) {
		if (!PhoneStatReceiver.isStarted) {
			context.registerReceiver((BroadcastReceiver) PhoneStatReceiver.mReceiver, PhoneStatReceiver.intentFilter);
		}
	}

	public static void stopReceive(final Context context) {
		if (PhoneStatReceiver.isStarted) {
			context.unregisterReceiver((BroadcastReceiver) PhoneStatReceiver.mReceiver);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		this.mInstance = ZMBluetoothManager.getInstance();
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
			PhoneStatReceiver.incomingFlag = false;
			MyPhoneStateListener.getInstance().onPhoneStateChanged(3);
			final String stringExtra = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
			Log.i("PhoneStatReceiver", "call OUT:" + stringExtra);
			if (this.mInstance != null) {
				this.mInstance.makeLog("PhoneStatReceiver", "call OUT:" + stringExtra);
			}
			if (this.mInstance != null && this.mInstance.isSPPConnected()) {
				this.mInstance.sendSPPMessage("PA_ON");
			}
		}
	}
}
