package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

public class EarinPttListener extends AbstractPttListenser {
	public static final String ACTION_PTT_DOWN = "com.earintent.ptt";

	public EarinPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		pttBroadcastReceiver.getActionSet().add("com.earintent.ptt");
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		Log.i("GUOK", "EarinPttListener Action " + intent.getAction());
		final String action = intent.getAction();
		final int intExtra = intent.getIntExtra("PTT_STATUS", 0);
		if ("com.earintent.ptt".equals(action)) {
			if (intExtra == 1) {
				MyLog.i("hTag", "receive down pttkey");
				if (TalkBackNew.checkHasCurrentGrp(context)) {
					GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
					return true;
				}
				MyToast.showToast(true, context, R.string.no_groups);
			} else if (intExtra == 0) {
				MyLog.i("hTag", "receive up pttkey");
				if (TalkBackNew.checkHasCurrentGrp(context)) {
					PttEventDispatcher.getInstance().dispatch(PttEventDispatcher.PttEvent.PTT_UP);
					return true;
				}
			}
			return true;
		}
		return false;
	}
}
