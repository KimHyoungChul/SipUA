package com.zed3.customgroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PttCustomGrp implements Serializable {
	private static final long serialVersionUID = 1L;
	private String groupCreatorName;
	private String groupCreatorNum;
	private String groupIdleTotal;
	private String groupName;
	private String groupNum;
	private String level;
	private List<CustomGroupMemberInfo> member_list;
	private String recordmode;
	private String report_heartbeat;
	private String speakerIdle;
	private String speakerTotal;

	public PttCustomGrp() {
		this.speakerIdle = "20";
		this.speakerTotal = "120";
		this.groupIdleTotal = "900";
		this.recordmode = "2";
		this.level = "7";
		this.report_heartbeat = "1800";
		this.member_list = new ArrayList<CustomGroupMemberInfo>();
	}

	public String getGroupCreatorName() {
		return this.groupCreatorName;
	}

	public String getGroupCreatorNum() {
		return this.groupCreatorNum;
	}

	public String getGroupIdleTotal() {
		return this.groupIdleTotal;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public String getGroupNum() {
		return this.groupNum;
	}

	public String getLevel() {
		return this.level;
	}

	public List<CustomGroupMemberInfo> getMember_list() {
		return this.member_list;
	}

	public String getRecordmode() {
		return this.recordmode;
	}

	public String getReport_heartbeat() {
		return this.report_heartbeat;
	}

	public String getSpeakerIdle() {
		return this.speakerIdle;
	}

	public String getSpeakerTotal() {
		return this.speakerTotal;
	}

	public void setGroupCreatorName(final String groupCreatorName) {
		this.groupCreatorName = groupCreatorName;
	}

	public void setGroupCreatorNum(final String groupCreatorNum) {
		this.groupCreatorNum = groupCreatorNum;
	}

	public void setGroupIdleTotal(final String groupIdleTotal) {
		this.groupIdleTotal = groupIdleTotal;
	}

	public void setGroupName(final String groupName) {
		this.groupName = groupName;
	}

	public void setGroupNum(final String groupNum) {
		this.groupNum = groupNum;
	}

	public void setLevel(final String level) {
		this.level = level;
	}

	public void setMember_list(final List<CustomGroupMemberInfo> list) {
		this.member_list = new ArrayList<CustomGroupMemberInfo>(list);
	}

	public void setRecordmode(final String recordmode) {
		this.recordmode = recordmode;
	}

	public void setReport_heartbeat(final String report_heartbeat) {
		this.report_heartbeat = report_heartbeat;
	}

	public void setSpeakerIdle(final String speakerIdle) {
		this.speakerIdle = speakerIdle;
	}

	public void setSpeakerTotal(final String speakerTotal) {
		this.speakerTotal = speakerTotal;
	}

	@Override
	public String toString() {
		return "PttCustomGrp [groupCreatorNum=" + this.groupCreatorNum + ", groupCreatorName=" + this.groupCreatorName + ", groupNum=" + this.groupNum + ", groupName=" + this.groupName + ", speakerIdle=" + this.speakerIdle + ", speakerTotal=" + this.speakerTotal + ", groupIdleTotal=" + this.groupIdleTotal + ", recordmode=" + this.recordmode + ", level=" + this.level + ", report_heartbeat=" + this.report_heartbeat + ", member_list=" + this.member_list + "]";
	}
}
