package com.zed3.customgroup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;

public class CustomGroupInfoReceiver extends BroadcastReceiver {
	private static final String TAG = "CustomGroupInfoReceiver";
	private static CustomGroupInfoReceiver mCustomGroupInfoReceiver;
	private static IntentFilter mFilter;

	static {
		CustomGroupInfoReceiver.mCustomGroupInfoReceiver = null;
		CustomGroupInfoReceiver.mFilter = null;
	}

	public static void register(final Context context) {
		LogUtil.makeLog("CustomGroupInfoReceiver", "register()");
		CustomGroupInfoReceiver.mCustomGroupInfoReceiver = new CustomGroupInfoReceiver();
		(CustomGroupInfoReceiver.mFilter = new IntentFilter()).addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_NUMBER_LIST_TIME_OUT);
		CustomGroupInfoReceiver.mFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_MEMBER_INFO_TIME_OUT);
		context.registerReceiver((BroadcastReceiver) CustomGroupInfoReceiver.mCustomGroupInfoReceiver, CustomGroupInfoReceiver.mFilter);
	}

	public static void unRegister(final Context context) {
		LogUtil.makeLog("CustomGroupInfoReceiver", "unRegister()");
		if (CustomGroupInfoReceiver.mFilter != null) {
			context.unregisterReceiver((BroadcastReceiver) CustomGroupInfoReceiver.mCustomGroupInfoReceiver);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		LogUtil.makeLog("CustomGroupInfoReceiver", "onReceive()#action = " + action);
		if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_NUMBER_LIST_TIME_OUT)) {
			final UserAgent getCurUA = Receiver.GetCurUA();
			if (getCurUA != null) {
				getCurUA.SendCustomGroupMessage(6, null, null, null, null, null);
			}
		} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_GET_GROUP_MEMBER_INFO_TIME_OUT)) {
			CustomGroupUtil.getInstance().getCurrentCustomGroupMemberInfo(intent.getStringExtra("groupNumber"));
		}
	}
}
