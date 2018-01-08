package com.zed3.sipua.baiduMap;

import java.util.ArrayList;
import java.util.Map;

public class GisQuestStateInfo {
	private String GroupNumber;
	public Map<String, GroupMember> mGroupMemberMap;
	ArrayList<GroupMember> members;
	int reqestState;
	long successTime;

	public GisQuestStateInfo() {
		this.reqestState = 1;
		this.successTime = -1L;
	}

	public String getGroupNumber() {
		return this.GroupNumber;
	}

	public ArrayList<GroupMember> getMembers() {
		return this.members;
	}

	public int getReqestState() {
		return this.reqestState;
	}

	public long getSuccessTime() {
		return this.successTime;
	}

	public Map<String, GroupMember> getmGroupMemberMap() {
		return this.mGroupMemberMap;
	}

	public void setGroupNumber(final String groupNumber) {
		this.GroupNumber = groupNumber;
	}

	public void setMembers(final ArrayList<GroupMember> members) {
		this.members = members;
	}

	public void setReqestState(final int reqestState) {
		this.reqestState = reqestState;
	}

	public void setSuccessTime(final long successTime) {
		this.successTime = successTime;
	}

	public void setmGroupMemberMap(final Map<String, GroupMember> mGroupMemberMap) {
		this.mGroupMemberMap = mGroupMemberMap;
	}
}
