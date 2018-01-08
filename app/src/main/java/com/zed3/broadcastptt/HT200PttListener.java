package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class HT200PttListener extends AbstractPttListenser {
	static final String ACTION_PTT_HT200_DOWN = "android.intent.action.PTT_KEY_DOWN";

	public HT200PttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("android.intent.action.PTT_KEY_DOWN");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		Log.i("GUOK", "HT200PttListener Action " + intent.getAction());
		if (action.equals("android.intent.action.PTT_KEY_DOWN")) {
			if (Boolean.valueOf(intent.getBooleanExtra("action", true))) {
				if (TalkBackNew.checkHasCurrentGrp(context)) {
					GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
					return true;
				}
				MyToast.showToast(true, context, R.string.no_groups);
			} else if (TalkBackNew.checkHasCurrentGrp(context)) {
				PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
				return true;
			}
			return true;
		}
		return false;
	}
}
