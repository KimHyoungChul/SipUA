package com.zed3.sipua.ui.lowsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.zed3.log.Logger;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;

import org.zoolu.tools.GroupListInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GroupListUtil {
	private static final String TAG = "GroupListUtil";
	private static Handler groupListHandler;
	private static BroadcastReceiver groupListReceiver;
	private static IntentFilter intentfilter;
	protected static boolean isSingleGroup;
	public static Context mContext;
	private static HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	private static ArrayList<PttGrp> mGroups;
	private static ArrayList<PttGrp> mGroups_buffur;
	private static PttGrps mPttGrps;
	private static boolean needLog;
	protected static boolean needSendRegetMessage;
	private static UserAgent ua;

	static {
		GroupListUtil.mContext = SipUAApp.mContext;
		GroupListUtil.mGroupListsMap = new HashMap<PttGrp, ArrayList<GroupListInfo>>();
		GroupListUtil.mGroups = new ArrayList<PttGrp>();
		GroupListUtil.mGroups_buffur = new ArrayList<PttGrp>();
		GroupListUtil.needLog = false;
		GroupListUtil.groupListReceiver = new BroadcastReceiver() {
			String tag = "groupListReceiver";

			private PttGrp getGroup(String s) {
				final String[] split = s.split(" ");
				PttGrp getGrpByIndex;
				if (split.length < 3) {
					getGrpByIndex = null;
				} else {
					s = split[2];
					final String substring = s.substring(0, s.indexOf("("));
					if (GroupListUtil.mPttGrps == null) {
						return null;
					}
					for (int fixedGrpCount = GroupListUtil.mPttGrps.getFixedGrpCount(), i = 0; i < fixedGrpCount; ++i) {
						if (substring.equals((getGrpByIndex = GroupListUtil.mPttGrps.GetGrpByIndex(i)).grpID)) {
							return getGrpByIndex;
						}
					}
					return null;
				}
				return getGrpByIndex;
			}

			private int getGroupIndex(final PttGrp pttGrp) {
				if (GroupListUtil.mPttGrps != null) {
					for (int fixedGrpCount = GroupListUtil.mPttGrps.getFixedGrpCount(), i = 0; i < fixedGrpCount; ++i) {
						final PttGrp getGrpByIndex = GroupListUtil.mPttGrps.GetGrpByIndex(i);
						if (pttGrp != null && getGrpByIndex != null) {
							final int n = i;
							if (pttGrp.grpID.equals(getGrpByIndex.grpID)) {
								return n;
							}
						}
					}
				}
				return -1;
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
				Logger.i(GroupListUtil.needLog, this.tag, "member\uff1a" + groupListInfo.GrpNum + "--" + groupListInfo.GrpName + "--" + groupListInfo.GrpState);
				return groupListInfo;
			}

			public ArrayList<GroupListInfo> ParseListInfo(String substring) {
				// TODO
				return null;
			}

			PttGrp findGroupFromListMap(final HashMap<PttGrp, ArrayList<GroupListInfo>> hashMap, final PttGrp pttGrp) {
				if (hashMap == null || hashMap.size() == 0) {
					return null;
				}
				final Set<PttGrp> keySet = hashMap.keySet();
				if (keySet == null || keySet.size() == 0) {
					return null;
				}
				for (final PttGrp pttGrp2 : keySet) {
					if (pttGrp2 == null || pttGrp == null) {
						return null;
					}
					if (pttGrp2.grpID.equals(pttGrp.grpID)) {
						return pttGrp2;
					}
				}
				return null;
			}

			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupstatelist")) {
					final Bundle extras = intent.getExtras();
					final String string = extras.getString("statusbody");
					final ArrayList parcelableArrayList = extras.getParcelableArrayList("statusInfo");
					if (string != null && parcelableArrayList != null) {
						final PttGrp group = this.getGroup(string);
						final PttGrp groupFromListMap = this.findGroupFromListMap(GroupListUtil.mGroupListsMap, group);
						if (groupFromListMap != null) {
							GroupListUtil.updateElement(groupFromListMap, group, parcelableArrayList);
						} else {
							GroupListUtil.putElement(group, parcelableArrayList);
						}
						if (GroupListUtil.isSingleGroup) {
							GroupListUtil.isSingleGroup = false;
							GroupListUtil.groupListHandler.sendMessage(GroupListUtil.groupListHandler.obtainMessage(3, (Object) "ok"));
						} else {
							final int groupIndex = this.getGroupIndex(group);
							if (groupIndex != -1) {
								Label_0253:
								{
									if (groupIndex != GroupListUtil.mGroups_buffur.size()) {
										break Label_0253;
									}
									GroupListUtil.mGroups_buffur.add(groupIndex, group);
									LogUtil.makeLog("GroupListUtil", "mPttGrps.getFixedGrpCount() = " + GroupListUtil.mPttGrps.getFixedGrpCount() + ",mGroups_buffur.size() = " + GroupListUtil.mGroups_buffur.size());
									Label_0238:
									{
										if (GroupListUtil.mPttGrps.getFixedGrpCount() != GroupListUtil.mGroups_buffur.size()) {
											break Label_0238;
										}
										synchronized (GroupListUtil.class) {
											GroupListUtil.mGroups.clear();
											for (int i = 0; i < GroupListUtil.mGroups_buffur.size(); ++i) {
												GroupListUtil.mGroups.add(GroupListUtil.mGroups_buffur.get(i));
											}
											// monitorexit(GroupListUtil.class)
											GroupListUtil.mGroups_buffur.clear();
											GroupListUtil.groupListHandler.sendMessage(GroupListUtil.groupListHandler.obtainMessage(3, (Object) "ok"));
											GroupListUtil.groupListHandler.sendMessage(GroupListUtil.groupListHandler.obtainMessage(5));
											while (true) {
//												Logger.i(GroupListUtil.needLog, this.tag, "groupIndex != mGroups_buffur.size()," + groupIndex + "/" + GroupListUtil.mGroups_buffur.size());
												final Message obtainMessage = GroupListUtil.groupListHandler.obtainMessage();
												obtainMessage.what = 4;
												GroupListUtil.groupListHandler.sendMessageDelayed(obtainMessage, 3000L);
												GroupListUtil.needSendRegetMessage = false;
												continue;
											}
										}
										// iftrue(Label_0286:, !GroupListUtil.needSendRegetMessage)
									}
								}
							}
						}
					}
				}
			}
		};
		GroupListUtil.groupListHandler = new Handler() {
			public void handleMessage(final Message message) {
				if (message.what == 2) {
					Receiver.GetCurUA().PttGetGroupList((String) message.obj);
				} else {
					if (message.what == 3) {
						GroupListUtil.mContext.sendBroadcast(new Intent("com.zed3.sipua_grouplist_update_over"));
						return;
					}
					if (message.what == 4) {
						GroupListUtil.needSendRegetMessage = true;
						GroupListUtil.getData4GroupList();
						return;
					}
					if (message.what == 5) {
						GroupListUtil.mContext.sendBroadcast(new Intent("custom_group_action_UPDATE_PERMANENT_GROUP_INFO"));
					}
				}
			}
		};
	}

	public static void clearGroupListsMap() {
		synchronized (GroupListUtil.class) {
			if (GroupListUtil.mGroupListsMap != null) {
				GroupListUtil.mGroupListsMap.clear();
			}
		}
	}

	public static boolean getData4GroupList() {
		LogUtil.makeLog("GroupListUtil", "getData4GroupList()");
		NetChecker.check(SipUAApp.mContext, true);
		GroupListUtil.ua = Receiver.GetCurUA();
		if (GroupListUtil.ua == null) {
			removeDataOfGroupList();
		} else {
			GroupListUtil.mPttGrps = GroupListUtil.ua.GetAllGrps();
			if (GroupListUtil.mPttGrps.getFixedGrpCount() == 0) {
				removeDataOfGroupList();
				return false;
			}
			if (GroupListUtil.mPttGrps != null) {
				GroupListUtil.groupListHandler.removeMessages(2);
				for (int fixedGrpCount = GroupListUtil.mPttGrps.getFixedGrpCount(), i = 0; i < fixedGrpCount; ++i) {
					final PttGrp getGrpByIndex = GroupListUtil.mPttGrps.GetGrpByIndex(i);
					if (getGrpByIndex.getType() == 0) {
						GroupListUtil.groupListHandler.sendMessage(GroupListUtil.groupListHandler.obtainMessage(2, (Object) getGrpByIndex.getGrpID()));
					}
				}
				return true;
			}
		}
		return false;
	}

	public static boolean getDataCurrentGroupList() {
		NetChecker.check(SipUAApp.mContext, true);
		GroupListUtil.ua = Receiver.GetCurUA();
		if (GroupListUtil.ua == null) {
			removeDataOfGroupList();
		} else {
			GroupListUtil.mPttGrps = GroupListUtil.ua.GetAllGrps();
			if (GroupListUtil.mPttGrps.getFixedGrpCount() == 0) {
				removeDataOfGroupList();
				return false;
			}
			if (GroupListUtil.mPttGrps != null) {
				GroupListUtil.groupListHandler.removeMessages(2);
				final PttGrp getCurGrp = GroupListUtil.ua.GetCurGrp();
				GroupListUtil.isSingleGroup = true;
				if (getCurGrp != null) {
					GroupListUtil.groupListHandler.sendMessage(GroupListUtil.groupListHandler.obtainMessage(2, (Object) getCurGrp.grpID));
				}
				return true;
			}
		}
		return false;
	}

	public static HashMap<PttGrp, ArrayList<GroupListInfo>> getGroupListsMap() {
		if (GroupListUtil.mGroupListsMap.size() == 0) {
			getData4GroupList();
		}
		return GroupListUtil.mGroupListsMap;
	}

	public static List<PttGrp> getGroups() {
		return GroupListUtil.mGroups;
	}

	public static String getUserName(String grpName) {
		Label_0074_Outer:
		while (true) {
			while (true) {
				int size = 0;
				Label_0141:
				while (true) {
					int n = 0;
					Label_0134:
					{
						synchronized (GroupListUtil.class) {
							if (GroupListUtil.mGroups.size() != 0 && GroupListUtil.mGroupListsMap.size() != 0) {
								n = 0;
								size = GroupListUtil.mGroups.size();
								if (n < size) {
									Cloneable cloneable = GroupListUtil.mGroups.get(n);
									if (cloneable == null) {
										break Label_0134;
									}
									cloneable = GroupListUtil.mGroupListsMap.get(cloneable);
									if (cloneable == null) {
										break Label_0134;
									}
									size = 0;
									if (size >= ((ArrayList) cloneable).size()) {
										break Label_0134;
									}
									final GroupListInfo groupListInfo = ((ArrayList<GroupListInfo>) cloneable).get(size);
									if (groupListInfo != null && grpName.equals(groupListInfo.GrpNum)) {
										grpName = groupListInfo.GrpName;
										return grpName;
									}
									break Label_0141;
								}
							} else {
								getData4GroupList();
							}
							grpName = null;
							return grpName;
						}
					}
					++n;
					continue Label_0074_Outer;
				}
				++size;
				continue;
			}
		}
	}

	public static void putElement(final PttGrp pttGrp, final ArrayList<GroupListInfo> list) {
		// monitorenter(GroupListUtil.class)
		if (pttGrp == null) {
			return;
		}
		try {
			if (GroupListUtil.mGroupListsMap != null) {
				GroupListUtil.mGroupListsMap.put(pttGrp, list);
			}
		} finally {
		}
		// monitorexit(GroupListUtil.class)
	}

	public static void registerReceiver() {
		if (GroupListUtil.intentfilter == null) {
			(GroupListUtil.intentfilter = new IntentFilter()).addAction("com.zed3.sipua.ui_groupstatelist");
		}
		GroupListUtil.mContext.registerReceiver(GroupListUtil.groupListReceiver, GroupListUtil.intentfilter);
	}

	public static void removeDataOfGroupList() {
		synchronized (GroupListUtil.class) {
			GroupListUtil.mGroupListsMap.clear();
			GroupListUtil.mGroups.clear();
			GroupListUtil.mContext.sendBroadcast(new Intent("com.zed3.sipua.ui_groupcall.all_groups_clear_over"));
		}
	}

	public static void removeElementByKey(final PttGrp pttGrp) {
		// monitorenter(GroupListUtil.class)
		if (pttGrp == null) {
			return;
		}
		try {
			if (GroupListUtil.mGroupListsMap != null && GroupListUtil.mGroupListsMap.containsKey(pttGrp)) {
				GroupListUtil.mGroupListsMap.remove(pttGrp);
			}
		} finally {
		}
		// monitorexit(GroupListUtil.class)
	}

	public static void unRegisterReceiver() {
		GroupListUtil.mContext.unregisterReceiver(GroupListUtil.groupListReceiver);
	}

	public static void updateElement(final PttGrp pttGrp, final PttGrp pttGrp2, final ArrayList<GroupListInfo> list) {
		// monitorenter(GroupListUtil.class)
		if (pttGrp == null || pttGrp2 == null) {
			return;
		}
		try {
			if (GroupListUtil.mGroupListsMap != null) {
				GroupListUtil.mGroupListsMap.remove(pttGrp);
				GroupListUtil.mGroupListsMap.put(pttGrp2, list);
			}
		} finally {
		}
		// monitorexit(GroupListUtil.class)
	}
}
