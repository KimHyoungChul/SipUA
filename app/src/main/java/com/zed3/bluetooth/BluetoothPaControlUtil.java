package com.zed3.bluetooth;

import android.content.Context;
import android.content.Intent;

public class BluetoothPaControlUtil {
	public static final String ACTION_BLUETOOTH_RESPOND = "com.zed3.sipua_bluetooth_respond";
	public static final String PTT_PA_OFF = "PA_OFF";
	public static final String PTT_PA_ON = "PA_ON";
	public static final String PTT_START = "R_START";
	public static final String PTT_STOP = "R_STOP";
	public static final String PTT_SUCCESS = "PTT_SUCC";
	public static final String PTT_WAITING = "PTT_WAIT";
	public static final String RESPOND_ACTION = "respond_action";
	public static final String RESPOND_ACTION_FUNCTION_RECEIVED = "respond_action_function_received";
	public static final String RESPOND_ACTION_PTT_DOWN = "respond_action_ptt_down";
	public static final String RESPOND_ACTION_PTT_DOWN_RECEIVED = "respond_action_ptt_down_received";
	public static final String RESPOND_ACTION_PTT_IDLE = "respond_action_ptt_idle";
	public static final String RESPOND_ACTION_PTT_LISTENING = "respond_action_ptt_listening";
	public static final String RESPOND_ACTION_PTT_SUCCESS = "respond_action_ptt_success";
	public static final String RESPOND_ACTION_PTT_UP = "respond_action_ptt_up";
	public static final String RESPOND_ACTION_PTT_UP_RECEIVED = "respond_action_ptt_up_received";
	public static final String RESPOND_ACTION_PTT_WAITTING = "respond_action_ptt_waitting";
	public static final String RESPOND_ACTION_VOL_LONG_DOWN_RECEIVED = "respond_action_vol_long_down_received";
	public static final String RESPOND_ACTION_VOL_LONG_UP_RECEIVED = "respond_action_vol_long_up_received";
	public static final String RESPOND_ACTION_VOL_SHORT_DOWN_RECEIVED = "respond_action_vol_short_down_received";
	public static final String RESPOND_ACTION_VOL_SHORT_UP_RECEIVED = "respond_action_vol_short_up_received";
	public static final String RESPOND_TYPE = "respond_type";
	public static final String RESPOND_TYPE_CALL = "respond_type_call";
	public static final String RESPOND_TYPE_FUNCTION = "respond_type_function";
	public static final String RESPOND_TYPE_PA_CONTROL = "respond_type_pa_control";
	public static final String RESPOND_TYPE_PTT = "respond_type_ptt";
	public static final String RESPOND_TYPE_VOL = "respond_type_vol";
	public static boolean mNeedOff;
	protected static String tag;

	static {
		BluetoothPaControlUtil.tag = "BluetoothPaControlUtil";
	}

	private static void sendRespondBroadcast(final Context context, final String action, final String s, final String s2) {
		final Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra("respond_type", s);
		intent.putExtra("respond_action", s2);
		context.sendBroadcast(intent);
	}

	public static void setPaOn(final Context context, final boolean b) {
		// TODO
	}
}
