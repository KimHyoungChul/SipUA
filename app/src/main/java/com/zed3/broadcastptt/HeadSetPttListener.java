package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;

import com.zed3.ptt.PttEventDispatcher;

public class HeadSetPttListener extends AbstractPttListenser {
	private static final String ACTION_PTT_DOWN = "android.intent.action.PTTHEAD.down";
	private static final String ACTION_PTT_UP = "android.intent.action.PTTHEAD.up";

	public HeadSetPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("android.intent.action.PTTHEAD.down");
		pttBroadcastReceiver.getActionSet().add("android.intent.action.PTTHEAD.up");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		if ("android.intent.action.PTTHEAD.down".equals(action)) {
			PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_DOWN);
			return true;
		}
		if ("android.intent.action.PTTHEAD.up".equals(action)) {
			PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
		}
		return false;
	}
}
