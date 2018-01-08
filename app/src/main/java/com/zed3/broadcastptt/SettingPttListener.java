package com.zed3.broadcastptt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.toast.MyToast;

import java.util.Set;

public class SettingPttListener extends AbstractPttListenser {
	private String actionDown;
	private String actionUp;
	private String keyCode;

	public SettingPttListener(final PttBroadcastReceiver pttBroadcastReceiver) {
		super(pttBroadcastReceiver);
	}

	private boolean getBroadcastSettings(final Context context) {
		final SharedPreferences settings = ((SipUAApp) context.getApplicationContext()).getSettings();
		this.actionDown = settings.getString("broadcast_action_down", "");
		this.actionUp = settings.getString("broadcast_action_up", "");
		this.keyCode = settings.getString("broadcast_keycode", "");
		return !TextUtils.isEmpty((CharSequence) this.actionDown) || !TextUtils.isEmpty((CharSequence) this.actionUp);
	}

	@Override
	public void addAction(final PttBroadcastReceiver pttBroadcastReceiver) {
		if (!TextUtils.isEmpty((CharSequence) this.actionDown) || this.getBroadcastSettings(SipUAApp.mContext)) {
			final Set<String> actionSet = pttBroadcastReceiver.getActionSet();
			actionSet.add(this.actionDown);
			actionSet.add(this.actionUp);
		}
	}

	@Override
	public boolean pttKeyClick(final Context context, final Intent intent, final PttBroadcastReceiver pttBroadcastReceiver) {
		Log.i("GUOK", "SettingPttListener Action " + intent.getAction());
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		KeyEvent keyEvent2;
		final KeyEvent keyEvent = keyEvent2 = null;
		if (extras != null) {
			final Object value = extras.get("android.intent.extra.KEY_EVENT");
			keyEvent2 = keyEvent;
			if (value instanceof KeyEvent) {
				keyEvent2 = (KeyEvent) value;
				Log.i("GUOK", "SettingPttListener ptt KeyEvent:" + keyEvent2.toString());
			}
		}
		if (!TextUtils.isEmpty((CharSequence) this.actionDown) && this.getBroadcastSettings(context)) {
			if (this.actionDown.trim().equals(action)) {
				if (keyEvent2 != null && keyEvent2.getRepeatCount() != 0) {
					return false;
				}
				if (!TalkBackNew.checkHasCurrentGrp(context)) {
					MyToast.showToast(true, context, R.string.no_groups);
				} else {
					GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
				}
				Log.i("GUOK", "SettingPttListener ptt KeyEvent:" + keyEvent2);
			} else {
				if (!this.actionUp.trim().equals(action)) {
					return false;
				}
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
