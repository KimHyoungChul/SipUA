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

public class NormalPttListener extends AbstractPttListenser {
	public static final String ACTION_PTT_DOWN = "android.intent.action.PTT.down";
	public static final String ACTION_PTT_UP = "android.intent.action.PTT.up";

	public NormalPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("android.intent.action.PTT.down");
		pttBroadcastReceiver.getActionSet().add("android.intent.action.PTT.up");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		KeyEvent keyEvent2;
		final KeyEvent keyEvent = keyEvent2 = null;
		Label_0081:
		{
			if (extras == null) {
				break Label_0081;
			}
			final Object value = extras.get("android.intent.extra.KEY_EVENT");
			if (value != null) {
				keyEvent2 = keyEvent;
				if (value instanceof KeyEvent) {
					keyEvent2 = (KeyEvent) value;
					Log.i("GUOK", "ptt KeyEvent:" + keyEvent2.toString());
				}
				break Label_0081;
			}
			return false;
		}
		Log.i("GUOK", "NormalPttListener Action " + intent.getAction());
		if (action.equals("android.intent.action.PTT.down") && ((keyEvent2 != null && keyEvent2.getRepeatCount() == 0) || keyEvent2 == null)) {
			if (!TalkBackNew.checkHasCurrentGrp(context)) {
				MyToast.showToast(true, context, R.string.no_groups);
			} else {
				GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
			}
			return true;
		}
		if (action.equals("android.intent.action.PTT.up")) {
			if (TalkBackNew.checkHasCurrentGrp(context)) {
				PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
			}
			return true;
		}
		return false;
	}
}
