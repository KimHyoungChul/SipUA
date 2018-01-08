package com.zed3.customgroup;

import java.util.List;

public interface CustomGroupParserListener {
	void parseAddMemberInfoCompleted(final String groupCreatorName, final String groupName, final List<String> memberList);

	void parseDeleteMemberInfoCompleted(final String groupCreatorName, final String groupNum, final String groupName, final List<String> memberList);

	void parseDestroyCustomGroupInfoCompleted(final String groupCreatorName, final String groupNum, final String groupName);

	void parseLeaveCustomGroupInfoCompleted(final String groupCreatorName, final String groupName, final String leaveNumber);
}
