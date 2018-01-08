package com.zed3.zhejiang;

import java.util.List;

public class GroupInfo {
	public List<GroupMember> groupMember;
	public String groupName;
	public String groupNumber;

	public GroupInfo() {
		this.groupNumber = "";
		this.groupName = "";
		this.groupMember = null;
	}

	public static class GroupMember {
		public String memberName;
		public String memberNumber;

		public GroupMember() {
			this.memberNumber = "";
			this.memberName = "";
		}
	}
}
