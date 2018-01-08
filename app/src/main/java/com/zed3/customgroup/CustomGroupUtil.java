package com.zed3.customgroup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.utils.LogUtil;

import org.zoolu.tools.GroupListInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CustomGroupUtil implements Comparator<GroupInfoItem> {
	private static final String TAG = "CustomGroupUtil";
	private static CustomGroupUtil customGroupUtil;
	private ProgressDialog mProgressDialog;

	static {
		CustomGroupUtil.customGroupUtil = null;
	}

	private CustomGroupUtil() {
		this.mProgressDialog = null;
	}

	private PttGrp convertCustomToStdGrp(final PttCustomGrp pttCustomGrp) {
		final PttGrp pttGrp = new PttGrp();
		pttGrp.setGrpID(pttCustomGrp.getGroupNum());
		pttGrp.setGrpName(pttCustomGrp.getGroupName());
		pttGrp.setLevel(Integer.valueOf(pttCustomGrp.getLevel()));
		pttGrp.setReport_heartbeat(Integer.valueOf(pttCustomGrp.getReport_heartbeat()));
		pttGrp.setType(1);
		return pttGrp;
	}

	private int getIndexOf(final List<GroupInfoItem> list, final GroupInfoItem groupInfoItem) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare((GroupInfoItem) list.get(i), groupInfoItem) > 0) {
				return size;
			}
		}
		return list.size();
	}

	public static CustomGroupUtil getInstance() {
		if (CustomGroupUtil.customGroupUtil == null) {
			CustomGroupUtil.customGroupUtil = new CustomGroupUtil();
		}
		return CustomGroupUtil.customGroupUtil;
	}

	private ArrayList<GroupListInfo> getPermanentMemberInfosFromCustomGrp(final PttCustomGrp pttCustomGrp) {
		final ArrayList<GroupListInfo> list = new ArrayList<GroupListInfo>();
		final List<CustomGroupMemberInfo> member_list = pttCustomGrp.getMember_list();
		for (int i = 0; i < member_list.size(); ++i) {
			final CustomGroupMemberInfo customGroupMemberInfo = member_list.get(i);
			final GroupListInfo groupListInfo = new GroupListInfo();
			groupListInfo.setGrpNum(customGroupMemberInfo.getMemberNum());
			groupListInfo.setGrpName(customGroupMemberInfo.getMemberName());
			groupListInfo.setGrpState(customGroupMemberInfo.getMemberStatus());
			list.add(groupListInfo);
		}
		return list;
	}

	private SharedPreferences getSharedPreferences(final Context context) {
		return context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
	}

	public void clearPttGroupInfo() {
		LogUtil.makeLog("CustomGroupUtil", "clearPttGroupInfo()");
		final UserAgent getCurUA = Receiver.GetCurUA();
		if (getCurUA != null) {
			getCurUA.getPttGrps().clear();
			getCurUA.getAllCustomGroups().clear();
			getCurUA.getCustomGroupMap().clear();
		}
		GroupListUtil.clearGroupListsMap();
	}

	@Override
	public int compare(final GroupInfoItem groupInfoItem, final GroupInfoItem groupInfoItem2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(groupInfoItem.getGrp_uName(), groupInfoItem2.getGrp_uName()) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(groupInfoItem.getGrp_uName(), groupInfoItem2.getGrp_uName()) > 0) {
			return 1;
		}
		return n;
	}

	public void deleteElementFromCustomGrpMap(final String s, final String s2) {
		LogUtil.makeLog("CustomGroupUtil", "deleteElementFromCustomGrpMap() " + s);
		final UserAgent getCurUA = Receiver.GetCurUA();
		final Map<String, PttCustomGrp> allCustomGroups = getCurUA.getAllCustomGroups();
		final Map<String, String> customGroupMap = getCurUA.getCustomGroupMap();
		if (allCustomGroups.containsKey(s)) {
			this.removeElementByKey(s, allCustomGroups);
			this.removeElementByKey2(s2, customGroupMap);
			getCurUA.updateAllCustomGroups(allCustomGroups);
			getCurUA.updateCustomGroupMap(customGroupMap);
			getCurUA.setCustomGroupLength(allCustomGroups.size());
		}
	}

	public void deleteElementFromGroupListMap(final String s) {
		synchronized (this) {
			LogUtil.makeLog("CustomGroupUtil", "deleteElementFromGroupListMap() " + s);
			final UserAgent getCurUA = Receiver.GetCurUA();
			getCurUA.removeGrpById(s);
			GroupListUtil.removeElementByKey(getCurUA.GetGrpByID(s));
		}
	}

	public void dismissProgressDialog() {
		if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
			this.mProgressDialog.dismiss();
			this.mProgressDialog = null;
		}
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

	public void getCurrentCustomGroupMemberInfo(final String s) {
		LogUtil.makeLog("CustomGroupUtil", "getCurrentCustomGroupMemberInfo() " + s);
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final UserAgent getCurUA = Receiver.GetCurUA();
			if (getCurUA != null) {
				getCurUA.SendCustomGroupMessage(7, null, null, null, s, null);
			}
		}
	}

	public String getCurrentUserDisplayName(final Context context) {
		return this.getSharedPreferences(context).getString("displayname", "");
	}

	public String getCurrentUserNum(final Context context) {
		return this.getSharedPreferences(context).getString("username", "");
	}

	public String getMemberNameByNumber(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final List<GroupInfoItem> allMembers = DataBaseService.getInstance().getAllMembers();
			final String s2 = "";
			int n = 0;
			GroupInfoItem groupInfoItem;
			while (true) {
				final String s3 = s2;
				if (n >= allMembers.size()) {
					return s3;
				}
				groupInfoItem = allMembers.get(n);
				if (s.equals(groupInfoItem.getGrp_uNumber())) {
					break;
				}
				++n;
			}
			return groupInfoItem.getGrp_uName();
		}
		return null;
	}

	public List<CustomGroupMemberInfo> getMembersNameFromAddressbook(final List<CustomGroupMemberInfo> list) {
		List<CustomGroupMemberInfo> list2;
		if (list == null || list.size() <= 0) {
			list2 = null;
		} else {
			final List<GroupInfoItem> allMembers = DataBaseService.getInstance().getAllMembers();
			int n = 0;
			while (true) {
				list2 = list;
				if (n >= list.size()) {
					break;
				}
				final CustomGroupMemberInfo customGroupMemberInfo = list.get(n);
				for (int i = 0; i < allMembers.size(); ++i) {
					final GroupInfoItem groupInfoItem = allMembers.get(i);
					if (customGroupMemberInfo.getMemberNum().equals(groupInfoItem.getGrp_uNumber())) {
						customGroupMemberInfo.setMemberName(groupInfoItem.getGrp_uName());
						break;
					}
				}
				++n;
			}
		}
		return list2;
	}

	public List<String> getMembersNameFromDB(final List<String> list) {
		if (list != null && list.size() > 0) {
			final List<GroupInfoItem> allMembers = DataBaseService.getInstance().getAllMembers();
			final ArrayList<String> list2 = new ArrayList<String>();
			for (final String s : list) {
				for (int i = 0; i < allMembers.size(); ++i) {
					final GroupInfoItem groupInfoItem = allMembers.get(i);
					if (s.equals(groupInfoItem.getGrp_uNumber())) {
						list2.add(groupInfoItem.getGrp_uName());
						break;
					}
				}
			}
			if (list2 != null) {
				final List<String> list3 = list2;
				if (list2.size() != 0) {
					return list3;
				}
			}
			return list;
		}
		return null;
	}

	public String getStringByResId(final Context context, final int n) {
		return context.getResources().getString(n);
	}

	public boolean isConsole(final String s) {
		return !s.equals("") && Member.UserType.toUserType(s) == Member.UserType.SVP;
	}

	public boolean isCurrentLanguageChinese(final Context context) {
		final int int1 = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("languageId", 0);
		boolean b = true;
		switch (int1) {
			case 0: {
				final Locale default1 = Locale.getDefault();
				if (default1.equals(Locale.SIMPLIFIED_CHINESE)) {
					b = true;
					break;
				}
				if (default1.equals(Locale.ENGLISH)) {
					b = false;
					break;
				}
				break;
			}
			case 1: {
				b = true;
				break;
			}
			case 2: {
				b = false;
				break;
			}
		}
		LogUtil.makeLog("CustomGroupUtil", "isCurrentLanguageChinese() " + b);
		return b;
	}

	public boolean isCustomGroupCreator(final Context context, final String s) {
		return this.getCurrentUserNum(context).equals(s);
	}

	public boolean isExistCustomGroup(final String s, final Map<String, PttCustomGrp> map) {
		return map != null && !map.isEmpty() && map.containsKey(s);
	}

	public void removeElementByKey(final String s, final Map<String, PttCustomGrp> map) {
		final Iterator<Map.Entry<String, PttCustomGrp>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getKey().equals(s)) {
				iterator.remove();
			}
		}
	}

	public void removeElementByKey2(final String s, final Map<String, String> map) {
		final Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getKey().equals(s)) {
				iterator.remove();
			}
		}
	}

	public List<GroupInfoItem> searchListBykeyWord(final String s, final List<GroupInfoItem> list) {
		final ArrayList<GroupInfoItem> list2 = new ArrayList<GroupInfoItem>();
		for (int i = 0; i < list.size(); ++i) {
			final GroupInfoItem groupInfoItem = list.get(i);
			final String lowerCase = groupInfoItem.getGrp_uName().toLowerCase();
			final String grp_uNumber = groupInfoItem.getGrp_uNumber();
			final String lowerCase2 = groupInfoItem.getGrp_uDept().toLowerCase();
			final String lowerCase3 = s.toLowerCase();
			if (lowerCase.contains(lowerCase3) || grp_uNumber.contains(s) || lowerCase2.contains(lowerCase3)) {
				list2.add(groupInfoItem);
			}
		}
		return list2;
	}

	public void showFailureReason(final Context context, int n) {
		final int n2 = 0;
		switch (n) {
			default: {
				n = n2;
				break;
			}
			case 450: {
				n = R.string.group_already_exist;
				break;
			}
			case 451: {
				n = R.string.hasNoSelf;
				break;
			}
			case 452: {
				n = R.string.member_already_exist;
				break;
			}
			case 453: {
				n = R.string.not_creator;
				break;
			}
			case 454: {
				n = R.string.delete_creator_error;
				break;
			}
			case 455: {
				n = R.string.member_not_exist;
				break;
			}
			case 456: {
				n = R.string.leave_console_error;
				break;
			}
		}
		this.showToast(context, n);
	}

	public void showProgressDialog(final Context context, final String title, final String message) {
		if (this.mProgressDialog == null) {
			(this.mProgressDialog = new ProgressDialog(context)).setTitle((CharSequence) title);
			this.mProgressDialog.setMessage((CharSequence) message);
			this.mProgressDialog.setCancelable(false);
			this.mProgressDialog.show();
		}
	}

	public void showToast(final Context context, final int n) {
		Toast.makeText(context, n, Toast.LENGTH_SHORT).show();
	}

	public void showToast(final Context context, final String s) {
		Toast.makeText(context, (CharSequence) s, Toast.LENGTH_SHORT).show();
	}

	public List<GroupInfoItem> sortByDefault(final List<GroupInfoItem> list) {
		final ArrayList<GroupInfoItem> list2 = new ArrayList<GroupInfoItem>();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); ++i) {
				final GroupInfoItem groupInfoItem = list.get(i);
				if (i == 0) {
					list2.add(groupInfoItem);
				} else {
					list2.add(this.getIndexOf(list2, groupInfoItem), groupInfoItem);
				}
			}
		}
		return list2;
	}

	public Vector<PttGrp> updatePermanentGroupInfos(final Vector<PttGrp> vector, final PttCustomGrp pttCustomGrp) {
		// monitorenter(this)
		Label_0156:
		{
			if (vector == null) {
				break Label_0156;
			}
			while (true) {
				while (true) {
					int n3 = 0;
					Label_0169:
					{
						try {
							if (vector.size() > 0) {
								int n = 0;
								int n2 = 0;
								n3 = 0;
								if (n3 >= vector.size()) {
									n3 = n;
								} else {
									if (!vector.elementAt(n3).getGrpID().equals(pttCustomGrp.getGroupNum())) {
										break Label_0169;
									}
									n = 1;
									n2 = n3;
									n3 = n;
								}
								if (n3 != 0) {
									final PttGrp pttGrp = vector.elementAt(n2);
									pttGrp.setGrpID(pttCustomGrp.getGroupNum());
									pttGrp.setGrpName(pttCustomGrp.getGroupName());
									pttGrp.setLevel(Integer.valueOf(pttCustomGrp.getLevel()));
									pttGrp.setReport_heartbeat(Integer.valueOf(pttCustomGrp.getReport_heartbeat()));
									pttGrp.setType(1);
								} else {
									vector.add(this.convertCustomToStdGrp(pttCustomGrp));
								}
								return vector;
							}
						} finally {
						}
						// monitorexit(this)
						break;
					}
					++n3;
					continue;
				}
			}
		}
		vector.add(this.convertCustomToStdGrp(pttCustomGrp));
		return vector;
	}

	public void updatePermanentGroupMemberInfos(final PttCustomGrp pttCustomGrp) {
		while (true) {
			while (true) {
				int n3 = 0;
				Label_0238:
				{
					final HashMap<PttGrp, ArrayList<GroupListInfo>> groupListsMap;
					synchronized (this) {
						groupListsMap = GroupListUtil.getGroupListsMap();
						if (!groupListsMap.isEmpty()) {
							Cloneable cloneable = Receiver.GetCurUA().getPttGrps();
							int n = 0;
							int n2 = 0;
							n3 = 0;
							if (n3 >= ((Vector) cloneable).size()) {
								n3 = n;
							} else {
								if (!((Vector<PttGrp>) cloneable).elementAt(n3).getGrpID().equals(pttCustomGrp.getGroupNum())) {
									break Label_0238;
								}
								n = 1;
								n2 = n3;
								n3 = n;
							}
							if (n3 != 0) {
								cloneable = ((Vector<PttGrp>) cloneable).elementAt(n2);
								((PttGrp) cloneable).setGrpID(pttCustomGrp.getGroupNum());
								((PttGrp) cloneable).setGrpName(pttCustomGrp.getGroupName());
								((PttGrp) cloneable).setLevel(Integer.valueOf(pttCustomGrp.getLevel()));
								((PttGrp) cloneable).setReport_heartbeat(Integer.valueOf(pttCustomGrp.getReport_heartbeat()));
								((PttGrp) cloneable).setType(1);
								GroupListUtil.removeElementByKey(this.findGroupFromListMap(groupListsMap, (PttGrp) cloneable));
								GroupListUtil.putElement((PttGrp) cloneable, this.getPermanentMemberInfosFromCustomGrp(pttCustomGrp));
							} else {
								cloneable = this.convertCustomToStdGrp(pttCustomGrp);
								final PttGrp groupFromListMap = this.findGroupFromListMap(groupListsMap, (PttGrp) cloneable);
								if (groupFromListMap != null) {
									groupListsMap.remove(groupFromListMap);
								}
								groupListsMap.put((PttGrp) cloneable, this.getPermanentMemberInfosFromCustomGrp(pttCustomGrp));
							}
							return;
						}
					}
					groupListsMap.put(this.convertCustomToStdGrp(pttCustomGrp), this.getPermanentMemberInfosFromCustomGrp(pttCustomGrp));
					return;
				}
				++n3;
				continue;
			}
		}
	}

	public void updateTalkBack(final Context context) {
		synchronized (this) {
			LogUtil.makeLog("CustomGroupUtil", "updateTalkBack()");
			final UserAgent getCurUA = Receiver.GetCurUA();
			Vector<PttGrp> pttGrps = getCurUA.getPttGrps();
			final Iterator<Map.Entry<String, PttCustomGrp>> iterator = getCurUA.getAllCustomGroups().entrySet().iterator();
			while (iterator.hasNext()) {
				final PttCustomGrp pttCustomGrp = iterator.next().getValue();
				pttGrps = this.updatePermanentGroupInfos(pttGrps, pttCustomGrp);
				this.updatePermanentGroupMemberInfos(pttCustomGrp);
			}
			getCurUA.setPttGrps(pttGrps);
			context.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO));
		}
	}
}
