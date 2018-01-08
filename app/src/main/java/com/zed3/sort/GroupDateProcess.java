package com.zed3.sort;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;

import org.zoolu.tools.GroupListInfo;

import java.util.ArrayList;

public class GroupDateProcess implements Runnable {
	private static final int ADDRESSBOOK_UPDATE = 2;
	private static final int CUSTOMGRP_MEMBER_UPDATE = 1;
	private static final int FIXGRP_MEMBER_UPDATE = 0;
	private Handler GrpOPHandler;
	String tag;

	public GroupDateProcess() {
		this.GrpOPHandler = null;
		this.tag = "GroupDateProcess";
	}

	private ArrayList<GroupListInfo> ParseListInfo(String substring) {
		// TODO
		return null;
	}

	private GroupListInfo parseGrpAttributes(final String s) {
		final String[] split = s.split(",");
		if (split.length != 3) {
			return null;
		}
		final GroupListInfo groupListInfo = new GroupListInfo();
		groupListInfo.GrpNum = split[0];
		groupListInfo.GrpName = split[1];
		groupListInfo.GrpState = split[2];
		MyLog.i(this.tag, "member\uff1a" + groupListInfo.GrpNum + "--" + groupListInfo.GrpName + "--" + groupListInfo.GrpState);
		return groupListInfo;
	}

	private void processAddressBookUpdate() {
		Receiver.GetCurUA().GetAllGrps().updateCustomGroupMemberName();
	}

	private void processCustomMemberUpdate(final String s) {
		Receiver.GetCurUA().GetAllGrps().currentCustomGroupInfoParser(s);
	}

	private void processFixGrpMemberUpdate(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s.replace("3ghandset: getstatus", "").trim())) {
			final ArrayList<GroupListInfo> parseListInfo = this.ParseListInfo(s);
			if (parseListInfo != null && parseListInfo.size() != 0) {
//				Collections.sort((List<Object>) parseListInfo, (Comparator<? super Object>) new GroupListInfoCompare());
				final ArrayList<GroupListInfo> sortOnline = GroupListInfoCompare.getInstance().sortOnline(parseListInfo);
				final Intent intent = new Intent("com.zed3.sipua.ui_groupstatelist");
				final Bundle bundle = new Bundle();
				intent.putExtra("statusbody", s);
				bundle.putParcelableArrayList("statusInfo", (ArrayList) sortOnline);
				intent.putExtras(bundle);
				SipUAApp.mContext.sendBroadcast(intent);
			}
		}
	}

	public void addressBookUpdate() {
		if (this.GrpOPHandler != null) {
			this.GrpOPHandler.sendMessage(this.GrpOPHandler.obtainMessage(2));
		}
	}

	public void customGrpUpdate(final String s) {
		if (this.GrpOPHandler != null) {
			this.GrpOPHandler.sendMessage(this.GrpOPHandler.obtainMessage(1, (Object) s));
		}
	}

	public void fixgrpUpdate(final String s) {
		if (this.GrpOPHandler != null) {
			this.GrpOPHandler.sendMessage(this.GrpOPHandler.obtainMessage(0, (Object) s));
		}
	}

	public void quit() {
		if (this.GrpOPHandler != null) {
			this.GrpOPHandler.getLooper().quit();
		}
	}

	@Override
	public void run() {
		Looper.prepare();
		this.GrpOPHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						GroupDateProcess.this.processFixGrpMemberUpdate((String) message.obj);
					}
					case 1: {
						GroupDateProcess.this.processCustomMemberUpdate((String) message.obj);
					}
					case 2: {
						GroupDateProcess.this.processAddressBookUpdate();
					}
				}
			}
		};
		Looper.loop();
	}
}
