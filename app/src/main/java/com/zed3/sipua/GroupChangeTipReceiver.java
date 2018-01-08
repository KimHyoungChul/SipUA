package com.zed3.sipua;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.ui.ActvityNotify;
import com.zed3.sipua.ui.Receiver;

public class GroupChangeTipReceiver extends BroadcastReceiver {
	public void onReceive(final Context context, Intent intent) {
		if (intent.getAction().equals("com.zed3.sipua.ui_groupcall.group_2_group")) {
			final Bundle extras = intent.getExtras();
			GroupCallUtil.setTalkGrp(extras.getString("0"));
			GroupCallUtil.setActionMode("com.zed3.sipua.ui_groupcall.group_2_group");
			intent = new Intent();
			intent.putExtras(extras);
			intent.setClass(Receiver.mContext, (Class) ActvityNotify.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Receiver.mContext.startActivity(intent);
		} else if (intent.getAction().equals("com.zed3.sipua.ui_groupcall.single_2_group")) {
			final Bundle extras2 = intent.getExtras();
			GroupCallUtil.setTalkGrp(extras2.getString("0"));
			GroupCallUtil.setActionMode("com.zed3.sipua.ui_groupcall.single_2_group");
			intent = new Intent();
			intent.putExtras(extras2);
			intent.setClass(Receiver.mContext, (Class) ActvityNotify.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Receiver.mContext.startActivity(intent);
		}
	}
}
