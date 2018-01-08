package com.zed3.sort;

import com.zed3.customgroup.CustomGroupMemberInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class CustomGroupMemberInfoCompare implements Comparator<CustomGroupMemberInfo> {
	private static CustomGroupMemberInfoCompare ct;

	static {
		CustomGroupMemberInfoCompare.ct = null;
	}

	public static CustomGroupMemberInfoCompare getInstance() {
		if (CustomGroupMemberInfoCompare.ct == null) {
			CustomGroupMemberInfoCompare.ct = new CustomGroupMemberInfoCompare();
		}
		return CustomGroupMemberInfoCompare.ct;
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

	public ArrayList<CustomGroupMemberInfo> sortOnline(final ArrayList<CustomGroupMemberInfo> list) {
		new ArrayList();
		if (list != null && list.size() > 1) {
			final ArrayList<CustomGroupMemberInfo> list2 = new ArrayList<CustomGroupMemberInfo>();
			final ArrayList<CustomGroupMemberInfo> list3 = new ArrayList<CustomGroupMemberInfo>();
			for (int i = 0; i < list.size(); ++i) {
				final CustomGroupMemberInfo customGroupMemberInfo = list.get(i);
				if (customGroupMemberInfo.getMemberStatus().equals("0")) {
					list3.add(customGroupMemberInfo);
				} else {
					list2.add(customGroupMemberInfo);
				}
			}
			if (list3.size() > 0) {
				list2.addAll(list3);
			}
			return new ArrayList<CustomGroupMemberInfo>(list2);
		}
		return list;
	}
}
