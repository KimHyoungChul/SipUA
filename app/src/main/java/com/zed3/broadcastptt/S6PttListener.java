package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class S6PttListener extends AbstractPttListenser {
	static final String ACTION_PTT_S6_DOWN = "com.chivin.action.MEDIA_PTT_DOWN";
	static final String ACTION_PTT_S6_UP = "com.chivin.action.MEDIA_PTT_UP";

	public S6PttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("com.chivin.action.MEDIA_PTT_DOWN");
		pttBroadcastReceiver.getActionSet().add("com.chivin.action.MEDIA_PTT_UP");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		KeyEvent keyEvent2;
		final KeyEvent keyEvent = keyEvent2 = null;
		if (extras != null) {
			final Object value = extras.get("android.intent.extra.KEY_EVENT");
			keyEvent2 = keyEvent;
			if (value instanceof KeyEvent) {
				keyEvent2 = (KeyEvent) value;
				Log.i("GUOK", "ptt KeyEvent:" + keyEvent2.toString());
			}
		}
		Log.i("GUOK", "S6PttListener Action " + intent.getAction());
		if (action.equals("com.chivin.action.MEDIA_PTT_DOWN") && ((keyEvent2 != null && keyEvent2.getRepeatCount() == 0) || keyEvent2 == null)) {
			if (TalkBackNew.checkHasCurrentGrp(context)) {
				GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
				return true;
			}
			MyToast.showToast(true, context, R.string.no_groups);
		} else {
			if (!action.equals("com.chivin.action.MEDIA_PTT_UP")) {
				return false;
			}
			if (TalkBackNew.checkHasCurrentGrp(context)) {
				PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
				return true;
			}
		}
		return true;
	}
}
