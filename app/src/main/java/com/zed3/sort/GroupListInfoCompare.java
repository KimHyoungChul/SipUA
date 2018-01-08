package com.zed3.sort;

import org.zoolu.tools.GroupListInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class GroupListInfoCompare implements Comparator<GroupListInfo> {
	private static GroupListInfoCompare ct;

	static {
		GroupListInfoCompare.ct = null;
	}

	public static GroupListInfoCompare getInstance() {
		if (GroupListInfoCompare.ct == null) {
			GroupListInfoCompare.ct = new GroupListInfoCompare();
		}
		return GroupListInfoCompare.ct;
	}

	@Override
	public int compare(final GroupListInfo groupListInfo, final GroupListInfo groupListInfo2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(groupListInfo.getGrpName(), groupListInfo2.getGrpName()) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(groupListInfo.getGrpName(), groupListInfo2.getGrpName()) > 0) {
			return 1;
		}
		return n;
	}

	public ArrayList<GroupListInfo> sortOnline(final ArrayList<GroupListInfo> list) {
		new ArrayList();
		if (list != null && list.size() > 1) {
			final ArrayList<GroupListInfo> list2 = new ArrayList<GroupListInfo>();
			final ArrayList<GroupListInfo> list3 = new ArrayList<GroupListInfo>();
			for (int i = 0; i < list.size(); ++i) {
				final GroupListInfo groupListInfo = list.get(i);
				if (groupListInfo.getGrpState().equals("0")) {
					list3.add(groupListInfo);
				} else {
					list2.add(groupListInfo);
				}
			}
			if (list3.size() > 0) {
				for (int j = 0; j < list3.size(); ++j) {
					list2.add(list3.get(j));
				}
			}
			return list2;
		}
		return list;
	}
}
