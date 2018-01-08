package com.zed3.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.zed3.log.MyLog;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;

public class GroupCallStateReceiver extends BroadcastReceiver {
	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	public static final int STATE_IDLE = 0;
	public static final int STATE_INITIATING = 4;
	public static final int STATE_LISTENING = 1;
	public static final int STATE_QUEUE = 3;
	public static final int STATE_TALKING = 2;
	private static IntentFilter intentFilter;
	private static boolean isStarted;
	public static int mLastState;
	private static GroupCallStateReceiver mReceiver;
	private ZMBluetoothManager mInstance;
	private String tag;

	static {
		GroupCallStateReceiver.mLastState = 0;
		GroupCallStateReceiver.mReceiver = new GroupCallStateReceiver();
		(GroupCallStateReceiver.intentFilter = new IntentFilter()).addAction("com.zed3.sipua.ui_groupcall.group_status");
	}

	public GroupCallStateReceiver() {
		this.tag = "GroupCallStateReceiver";
		this.mInstance = ZMBluetoothManager.getInstance();
	}

	private void processState(final PttGrp pttGrp) {
		// TODO
	}

	private void sleep(final int n) {
		// monitorenter(this)
		final long n2 = n;
		try {
			Thread.sleep(n2);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
		}
		// monitorexit(this)
	}

	public static void startReceive(final Context context) {
		if (!GroupCallStateReceiver.isStarted) {
			context.registerReceiver((BroadcastReceiver) GroupCallStateReceiver.mReceiver, GroupCallStateReceiver.intentFilter);
		}
	}

	public static void stopReceive(final Context context) {
		if (GroupCallStateReceiver.isStarted) {
			context.unregisterReceiver((BroadcastReceiver) GroupCallStateReceiver.mReceiver);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.group_status")) {
			final Bundle extras = intent.getExtras();
			String trim;
			if (extras.getString("1") != null) {
				trim = extras.getString("1").trim();
			} else {
				trim = null;
			}
			String s = null;
			String s2 = trim;
			if (trim != null) {
				final String[] split = trim.split(" ");
				if (split.length == 1) {
					s = split[0];
					s2 = trim;
				} else {
					s = split[0];
					s2 = split[1];
				}
			}
			final UserAgent getCurUA = Receiver.mSipdroidEngine.GetCurUA();
			if (getCurUA != null) {
				final PttGrp getCurGrp = getCurUA.GetCurGrp();
				MyLog.i(this.tag, "speaker:" + s2 + ",userNum:" + s);
				if (getCurGrp != null) {
					this.processState(getCurGrp);
					return;
				}
				MyLog.i(this.tag, "pttGrp = null unprocess");
			}
		}
	}
}
