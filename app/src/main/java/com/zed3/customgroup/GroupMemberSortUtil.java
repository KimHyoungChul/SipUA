package com.zed3.customgroup;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GroupMemberSortUtil implements Comparator<CustomGroupMemberInfo> {
	private static GroupMemberSortUtil instance;

	private int getIndexOf(final List<CustomGroupMemberInfo> list, final CustomGroupMemberInfo customGroupMemberInfo) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare((CustomGroupMemberInfo) list.get(i), customGroupMemberInfo) > 0) {
				return size;
			}
		}
		return list.size();
	}

	public static GroupMemberSortUtil getInstance() {
		if (GroupMemberSortUtil.instance == null) {
			GroupMemberSortUtil.instance = new GroupMemberSortUtil();
		}
		return GroupMemberSortUtil.instance;
	}

	@Override
	public int compare(final CustomGroupMemberInfo customGroupMemberInfo, final CustomGroupMemberInfo customGroupMemberInfo2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(customGroupMemberInfo.getMemberName(), customGroupMemberInfo2.getMemberName()) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(customGroupMemberInfo.getMemberName(), customGroupMemberInfo2.getMemberName()) > 0) {
			return 1;
		}
		return n;
	}

	public List<CustomGroupMemberInfo> sortByDefault(final List<CustomGroupMemberInfo> list) {
		final ArrayList<CustomGroupMemberInfo> list2 = new ArrayList<CustomGroupMemberInfo>();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); ++i) {
				final CustomGroupMemberInfo customGroupMemberInfo = list.get(i);
				if (i == 0) {
					list2.add(customGroupMemberInfo);
				} else {
					list2.add(this.getIndexOf(list2, customGroupMemberInfo), customGroupMemberInfo);
				}
			}
		}
		return list2;
	}
}
