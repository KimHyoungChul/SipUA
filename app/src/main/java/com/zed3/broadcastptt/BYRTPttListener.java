package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class BYRTPttListener extends AbstractPttListenser {
	static final String ACTION_PTT_DOWN = "com.runbo.camera.key.down";
	static final String ACTION_PTT_UP = "com.runbo.camera.key.up";

	public BYRTPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("com.runbo.camera.key.down");
		pttBroadcastReceiver.getActionSet().add("com.runbo.camera.key.up");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		final String action = intent.getAction();
		intent.getExtras();
		if (action.equals("com.runbo.camera.key.down")) {
			if (TalkBackNew.checkHasCurrentGrp(context)) {
				GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
				return true;
			}
			MyToast.showToast(true, context, R.string.no_groups);
		} else {
			if (!action.equals("com.runbo.camera.key.up")) {
				return false;
			}
			MyLog.e("dd", "ptt_up");
			if (TalkBackNew.checkHasCurrentGrp(context)) {
				PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
				return true;
			}
		}
		return true;
	}
}
