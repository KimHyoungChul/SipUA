package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class ZdzlPttListener extends AbstractPttListenser {
	static final String ACTION_PTT_ZDZL_DOWN = "com.ceiw.keyevent";

	public ZdzlPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("com.ceiw.keyevent");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		if ("com.ceiw.keyevent".equals(intent.getAction()) && intent.getStringExtra("key_code").equals("voice")) {
			final String stringExtra = intent.getStringExtra("key_action");
			if (stringExtra.equals("down")) {
				if (TalkBackNew.checkHasCurrentGrp(context)) {
					GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
					return true;
				}
				MyToast.showToast(true, context, R.string.no_groups);
			} else if ((stringExtra.equals("up") || stringExtra.equals("short_press")) && TalkBackNew.checkHasCurrentGrp(context)) {
				PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
				return true;
			}
			return true;
		}
		return false;
	}
}
