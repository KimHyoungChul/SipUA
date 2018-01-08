package com.zed3.sipua;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.zed3.addressbook.DataBaseService;
import com.zed3.customgroup.CustomGroupManager;
import com.zed3.customgroup.CustomGroupMemberInfo;
import com.zed3.customgroup.CustomGroupParserListener;
import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.customgroup.PttCustomGrp;
import com.zed3.log.MyLog;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sort.CustomGroupMemberInfoCompare;
import com.zed3.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PttGrps {
	private static final String tag = "PttGrps";
	private volatile PttGrp currentGrp;
	private int customGroupLength;
	private Map<String, PttCustomGrp> customGrpMap;
	private Vector<PttGrp> grps;
	private Lock lock = new ReentrantLock();
	private CustomGroupParserListener mListener = null;
	private Map<String, String> map;
	public String signalMulticastIP;
	public int signalMulticastPort;

	class C10541 implements Runnable {
		C10541() {
		}

		public void run() {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			SipUAApp.getAppContext().sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED));
		}
	}

	public void setOnParseCompledtedListener(CustomGroupParserListener listener) {
		if (listener != null) {
			this.mListener = listener;
		}
	}

	public PttGrps() {
		LogUtil.makeLog(tag, "new PttGrps()");
		this.grps = new Vector();
		this.currentGrp = null;
		this.signalMulticastIP = new String();
		this.signalMulticastPort = 0;
		this.customGrpMap = new LinkedHashMap();
		this.map = new HashMap();
	}

	public void setCustomGroupLength(int customGroupLength) {
		this.customGroupLength = customGroupLength;
	}

	public int getCustomGroupLength() {
		return this.customGroupLength;
	}

	public Vector<PttGrp> getPttGrps() {
		return this.grps;
	}

	public synchronized void setPttGrps(Vector<PttGrp> grps) {
		this.grps = grps;
	}

	public Map<String, String> getMap() {
		return this.map;
	}

	public synchronized void setMap(Map<String, String> currentMap) {
		this.map = currentMap;
	}

	public Map<String, PttCustomGrp> getCustomGrpMap() {
		return this.customGrpMap;
	}

	public synchronized void setCustomGrpMap(Map<String, PttCustomGrp> map) {
		this.customGrpMap = map;
	}

	public synchronized boolean customGroupMemberNumberListParser(String body) {
		synchronized (this) {
			boolean z = false;
			int i = 0;
			LogUtil.makeLog(tag, "customGroupMemberNumberListParser() " + body);
			String[] category = body.split("\r\n");
			if (category.length >= 0) {
				if (category[0].equals("getGroup")) {
					String group_number_list = category[1];
					if (group_number_list.startsWith("groupList:")) {
						String[] numbers = group_number_list.replaceFirst("groupList:", "").split(";");
						int length = numbers.length;
						if (numbers != null && length > 0) {
							this.customGroupLength = length;
							int length2 = numbers.length;
							while (i < length2) {
								CustomGroupUtil.getInstance().getCurrentCustomGroupMemberInfo(numbers[i]);
								i++;
							}
						}
					}
					z = true;
				}
			}
			return z;
		}
	}

	public synchronized boolean currentCustomGroupInfoParser(String body) {
		boolean z = false;
		synchronized (this) {
			LogUtil.makeLog(tag, "customGroupMemberInfoParser() " + body);
			String[] category = body.split("\r\n");
			if (category.length >= 0) {
				String response_type = category[0];
				if (!response_type.equals("")) {
					if (response_type.equals("getMember")) {
						PttCustomGrp currentCustomGrp = parseInfo(category);
						String groupNum = currentCustomGrp.getGroupNum();
						String groupName = currentCustomGrp.getGroupName().trim();
						if (CustomGroupUtil.getInstance().isExistCustomGroup(groupNum, this.customGrpMap)) {
							CustomGroupUtil.getInstance().removeElementByKey(groupNum, this.customGrpMap);
							this.customGrpMap.put(groupNum, currentCustomGrp);
							CustomGroupUtil.getInstance().removeElementByKey2(groupName, this.map);
							this.map.put(groupName, groupNum);
						} else {
							this.customGrpMap.put(groupNum, currentCustomGrp);
							this.map.put(groupName, groupNum);
						}
						this.grps = CustomGroupUtil.getInstance().updatePermanentGroupInfos(this.grps, currentCustomGrp);
						CustomGroupUtil.getInstance().updatePermanentGroupMemberInfos(currentCustomGrp);
						if (this.customGroupLength == this.customGrpMap.size()) {
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							SipUAApp.getAppContext().sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO));
						}
					}
				}
				z = true;
			}
		}
		return z;
	}

	public synchronized PttCustomGrp parseInfo(String[] info) {
		PttCustomGrp customGrp;
		customGrp = new PttCustomGrp();
		for (int i = 1; i < info.length; i++) {
			String group = info[i];
			String[] groups;
			if (group.startsWith("group:")) {
				groups = group.replaceFirst("group:", "").split(",");
				customGrp.setGroupCreatorNum(groups[0]);
				customGrp.setGroupCreatorName(groups[1]);
				customGrp.setGroupNum(groups[2]);
				customGrp.setGroupName(groups[3]);
			} else if (group.startsWith("ptt:")) {
				groups = group.replaceFirst("ptt:", "").split(",");
				customGrp.setSpeakerIdle(groups[0]);
				customGrp.setSpeakerTotal(groups[1]);
				customGrp.setGroupIdleTotal(groups[2]);
				customGrp.setRecordmode(groups[3]);
				customGrp.setLevel(groups[4]);
				customGrp.setReport_heartbeat(groups[5]);
			} else if (group.startsWith("member:")) {
				String[] members = group.replaceFirst("member:", "").split(";");
				ArrayList<CustomGroupMemberInfo> member_list = new ArrayList();
				for (String member : members) {
					String[] member_item = member.split(",");
					CustomGroupMemberInfo groupMember = new CustomGroupMemberInfo();
					groupMember.setMemberNum(member_item[0]);
					String memberName = "";
					memberName = DataBaseService.getInstance().getMemberNameByNum(member_item[0]);
					if (TextUtils.isEmpty(memberName) && member_item[0].length() == 11) {
						MyLog.i("refreshLog", "refresh happened 1????");
						memberName = member_item[0].substring(6, 11);
					}
					groupMember.setMemberName(memberName);
					groupMember.setMemberStatus(member_item[1]);
					member_list.add(groupMember);
				}
				Collections.sort(member_list, new CustomGroupMemberInfoCompare());
				customGrp.setMember_list(CustomGroupMemberInfoCompare.getInstance().sortOnline(member_list));
			}
		}
		return customGrp;
	}

	public synchronized boolean parseCustomGroupInfo(String info) {
		boolean z = false;
		synchronized (this) {
			LogUtil.makeLog(tag, "parseCustomGroupInfo() " + info);
			String[] category = info.split("\r\n");
			if (category.length >= 0) {
				String response_type = category[0];
				if (!response_type.equals("")) {
					if (response_type.equals("update")) {
						updateCustomGroupMemberInfoParser(category);
					} else if (response_type.equals("del")) {
						deleteMemberNotificationParser(category);
					} else if (response_type.equals("destroy")) {
						destroyCustomGroupNotificationParser(category);
//					} else if (response_type.equals(ProductAction.ACTION_ADD)) {
//						addMemberNotificationParser(category);
					} else if (response_type.equals("leave")) {
						leaveCustomGroupNotificationParser(category);
					}
					z = true;
				}
			}
		}
		return z;
	}

	public void updateCustomGroupMemberInfoParser(String[] category) {
		this.lock.lock();
		int i = 1;
		while (i < category.length) {
			try {
				PttCustomGrp currentCustomGrp = parseInfo(category);
				String groupNum = currentCustomGrp.getGroupNum();
				String groupName = currentCustomGrp.getGroupName();
				if (CustomGroupUtil.getInstance().isExistCustomGroup(groupNum, this.customGrpMap)) {
					CustomGroupUtil.getInstance().removeElementByKey(groupNum, this.customGrpMap);
					this.customGrpMap.put(groupNum, currentCustomGrp);
					CustomGroupUtil.getInstance().removeElementByKey2(groupName, this.map);
					this.map.put(groupName, groupNum);
				} else {
					this.customGrpMap.put(groupNum, currentCustomGrp);
					this.map.put(groupName, groupNum);
				}
				this.grps = CustomGroupUtil.getInstance().updatePermanentGroupInfos(this.grps, currentCustomGrp);
				CustomGroupUtil.getInstance().updatePermanentGroupMemberInfos(currentCustomGrp);
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				this.lock.unlock();
			}
		}
		new Thread(new C10541()).start();
	}

	/* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
	private void deleteMemberNotificationParser(java.lang.String[] r13) {
		// TODO
	}

	/* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
	private void destroyCustomGroupNotificationParser(java.lang.String[] r10) {
		// TODO
	}

	/* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
	private void addMemberNotificationParser(java.lang.String[] r12) {
		// TODO
	}

	/* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
	private void leaveCustomGroupNotificationParser(java.lang.String[] r10) {
		// TODO
	}

	private PttGrp parseGrpAttributes(String imp_group) {
		String[] attributes = imp_group.split(",");
		if (attributes.length != 4) {
			return null;
		}
		PttGrp grp = new PttGrp();
		grp.grpName = attributes[0];
		grp.grpID = attributes[1];
		grp.level = Integer.valueOf(attributes[2]).intValue();
		grp.report_heartbeat = Integer.valueOf(attributes[3]).intValue();
		grp.setType(0);
		return grp;
	}

	public synchronized boolean ParseGrpInfo(String info) {
		boolean z;
		int i;
		LogUtil.makeLog(tag, "pttGroupParse(info)");
		List<PttGrp> delGrps = new ArrayList();
		for (i = 0; i < this.grps.size(); i++) {
			if (((PttGrp) this.grps.elementAt(i)).getType() == 0) {
				delGrps.add((PttGrp) this.grps.get(i));
			}
		}
		this.grps.removeAll(delGrps);
		String[] category = info.split("\r\n");
		if (category.length < 1) {
			z = false;
		} else {
			for (String str : category) {
				PttGrp grp;
				if (str.startsWith("group: ")) {
					String[] g3_groups = str.replaceFirst("group: ", "").split(";");
					int length = g3_groups.length;
					int i2 = 0;
					int i3 = 0;
					while (i2 < length) {
						grp = parseGrpAttributes(g3_groups[i2]);
						if (grp != null) {
							i = i3 + 1;
							this.grps.add(i3, grp);
						} else {
							i = i3;
						}
						i2++;
						i3 = i;
					}
				} else if (str.startsWith("emergency-call: ")) {
					grp = parseGrpAttributes(str.replaceFirst("emergency-call: ", ""));
					if (grp != null) {
						this.grps.add(grp);
					}
				} else if (str.startsWith("alarm-svpnumber")) {
					DeviceInfo.svpnumber = str.split("alarm-svpnumber:")[1].trim();
				} else if (str.startsWith("mms-defaultrecnum")) {
					DeviceInfo.defaultrecnum = str.split("mms-defaultrecnum:")[1].trim();
				} else if (str.startsWith("http-port")) {
					DeviceInfo.http_port = str.split("http-port:")[1].trim();
				} else if (str.startsWith("https-port")) {
					DeviceInfo.https_port = str.split("https-port:")[1].trim();
				}
				MyLog.i(" = ", DeviceInfo.svpnumber + "  " + DeviceInfo.defaultrecnum + "  " + DeviceInfo.http_port + "  " + DeviceInfo.https_port);
			}
			if (!(Build.MODEL.equals("DATANG T98") || Build.MODEL.equals("Z508") || !DeviceInfo.CONFIG_SUPPORT_EMERGENYCALL || DeviceInfo.svpnumber.equals("") || DeviceInfo.ISAlarmShowing)) {
				SipUAApp.mContext.startService(new Intent(SipUAApp.mContext, AlarmService.class));
				DeviceInfo.ISAlarmShowing = true;
			}
			z = true;
		}
		return z;
	}

	public PttGrp GetCurGrp() {
		return this.currentGrp;
	}

	public synchronized void SetCurGrp(PttGrp grp) {
		LogUtil.makeLog(tag, "SetCurGrp(" + (grp == null ? "null" : grp.toString()) + ")");
		this.currentGrp = grp;
	}

	public synchronized PttGrp FirstGrp() {
		PttGrp pttGrp;
		if (this.grps.size() > 0) {
			pttGrp = (PttGrp) this.grps.elementAt(0);
		} else {
			pttGrp = null;
		}
		return pttGrp;
	}

	public synchronized int GetCount() {
		return this.grps.size();
	}

	public int getFixedGrpCount() {
		int count = 0;
		Iterator it = this.grps.iterator();
		while (it.hasNext()) {
			if (((PttGrp) it.next()).getType() == 0) {
				count++;
			}
		}
		return count;
	}

	public synchronized int getCustomGrpCount() {
		int count;
		count = 0;
		Iterator it = this.grps.iterator();
		while (it.hasNext()) {
			if (((PttGrp) it.next()).getType() == 1) {
				count++;
			}
		}
		return count;
	}

	public synchronized PttGrp GetGrpByID(String ID) {
		PttGrp pttGrp;
		if (this.grps.size() <= 0) {
			pttGrp = null;
		} else {
			pttGrp = null;
			for (int i = 0; i < this.grps.size(); i++) {
				if (((PttGrp) this.grps.elementAt(i)).grpID.equalsIgnoreCase(ID)) {
					pttGrp = (PttGrp) this.grps.elementAt(i);
					break;
				}
			}
		}
		return pttGrp;
	}

	public PttGrp GetGrpByIndex(int index) {
		if (index < 0 || index >= GetCount()) {
			return null;
		}
		return (PttGrp) this.grps.elementAt(index);
	}

	public synchronized void removeElementById(String id) {
		if (this.grps != null && this.grps.size() > 0) {
			for (int i = 0; i < this.grps.size(); i++) {
				if (((PttGrp) this.grps.elementAt(i)).getGrpID().equals(id)) {
					this.grps.removeElementAt(i);
					break;
				}
			}
		}
	}

	public synchronized PttGrps copyGrps() {
		PttGrps dGrps;
		dGrps = new PttGrps();
		if (this.currentGrp != null) {
			dGrps.currentGrp = this.currentGrp.clone();
		}
		dGrps.customGroupLength = this.customGroupLength;
		Vector<PttGrp> d = new Vector();
		if (this.grps != null) {
			Iterator it = this.grps.iterator();
			while (it.hasNext()) {
				d.add(((PttGrp) it.next()).clone());
			}
		}
		dGrps.grps = d;
		dGrps.lock = this.lock;
		dGrps.map = new HashMap();
		if (this.map != null) {
			dGrps.map.putAll(this.map);
		}
		dGrps.customGrpMap = new HashMap();
		if (this.customGrpMap != null) {
			dGrps.customGrpMap.putAll(this.customGrpMap);
		}
		return dGrps;
	}

	public synchronized PttCustomGrp parseCustomGrp(String grpStr) {
		PttCustomGrp customGrp;
		if (!TextUtils.isEmpty(grpStr) && grpStr.length() > 3) {
			if (grpStr.startsWith("group")) {
				grpStr = grpStr.substring("group:".length());
			}
			String[] grpArray = grpStr.split(",");
			if (grpArray != null && grpArray.length == 4) {
				customGrp = new PttCustomGrp();
				customGrp.setGroupCreatorNum(grpArray[0]);
				customGrp.setGroupCreatorName(grpArray[1]);
				customGrp.setGroupNum(grpArray[2]);
				customGrp.setGroupName(grpArray[3]);
				CustomGroupUtil.getInstance().getCurrentCustomGroupMemberInfo(grpArray[2]);
			}
		}
		customGrp = null;
		return customGrp;
	}

	public synchronized void addCustomGroup(PttCustomGrp customGrp) {
		if (customGrp != null) {
			if (this.customGrpMap.size() > 0 && this.customGrpMap.containsKey(customGrp.getGroupNum())) {
				CustomGroupUtil.getInstance().removeElementByKey(customGrp.getGroupNum(), this.customGrpMap);
			}
			this.customGrpMap.put(customGrp.getGroupNum(), customGrp);
			this.map.put(customGrp.getGroupName(), customGrp.getGroupNum());
			this.grps = CustomGroupUtil.getInstance().updatePermanentGroupInfos(this.grps, customGrp);
		}
	}

	public synchronized void addCustomGroups(String response_body) {
		synchronized (this) {
			String[] category = response_body.split("\r\n");
			if (category.length == 2 && category[0].equals("getGroup")) {
				String custom_group_list = category[1];
				if (custom_group_list.startsWith("groupList:")) {
					clearCustomGroupSet();
					String[] groups = custom_group_list.substring("groupList:".length()).split(";");
					if (groups != null && groups.length > 0) {
						this.customGroupLength = groups.length;
						for (String grpStr : groups) {
							PttCustomGrp grp = parseCustomGrp(grpStr);
							if (grp != null) {
								addCustomGroup(grp);
							}
						}
					}
				}
			}
		}
	}

	private void clearCustomGroupSet() {
		if (this.customGrpMap.size() > 0) {
			this.customGrpMap.clear();
		}
		if (this.map.size() > 0) {
			this.map.clear();
		}
		if (this.grps.size() > 0) {
			Vector<PttGrp> customGrps = new Vector();
			Iterator it = this.grps.iterator();
			while (it.hasNext()) {
				PttGrp grp = (PttGrp) it.next();
				if (grp.getType() == 1) {
					customGrps.add(grp);
				}
			}
			if (customGrps.size() > 0) {
				this.grps.removeAll(customGrps);
			}
		}
	}

	public synchronized void updateCustomGroupMemberName() {
		if (this.customGrpMap != null && this.customGrpMap.size() > 0) {
			if (this.grps.size() > 0) {
				Iterator it = this.grps.iterator();
				while (it.hasNext()) {
					PttGrp grp = (PttGrp) it.next();
					if (grp.getType() == 1) {
						PttCustomGrp customGrp = (PttCustomGrp) this.customGrpMap.get(grp.getGrpID());
						if (!(customGrp == null || customGrp.getMember_list() == null || customGrp.getMember_list().size() <= 0)) {
							for (CustomGroupMemberInfo info : customGrp.getMember_list()) {
								String memberName = DataBaseService.getInstance().getMemberNameByNum(info.getMemberNum());
								if (TextUtils.isEmpty(memberName) && info.getMemberNum().length() == 11) {
									MyLog.e("refreshLog", "refresh happened ????2");
									memberName = info.getMemberNum().substring(6, 11);
								}
								info.setMemberName(memberName);
							}
						}
					}
				}
			}
			SipUAApp.getAppContext().sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO));
		}
	}
}
