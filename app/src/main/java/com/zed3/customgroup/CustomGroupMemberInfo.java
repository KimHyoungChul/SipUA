package com.zed3.customgroup;

import java.io.Serializable;

public class CustomGroupMemberInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String memberName;
	private String memberNum;
	private String memberStatus;

	public CustomGroupMemberInfo() {
		this.memberName = "";
		this.memberNum = "";
		this.memberStatus = "0";
	}

	public String getMemberName() {
		return this.memberName;
	}

	public String getMemberNum() {
		return this.memberNum;
	}

	public String getMemberStatus() {
		return this.memberStatus;
	}

	public void setMemberName(final String memberName) {
		this.memberName = memberName;
	}

	public void setMemberNum(final String memberNum) {
		this.memberNum = memberNum;
	}

	public void setMemberStatus(final String memberStatus) {
		this.memberStatus = memberStatus;
	}

	@Override
	public String toString() {
		return "CustomGroupMemberInfo [memberName=" + this.memberName + ", memberNum=" + this.memberNum + ", memberStatus=" + this.memberStatus + "]";
	}
}
