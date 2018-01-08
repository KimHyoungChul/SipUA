package com.zed3.sipua.ui.contact;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MeetingCompareTool implements Comparator<Map<String, Object>> {
	private static MeetingCompareTool ct;

	static {
		MeetingCompareTool.ct = null;
	}

	private int findPos(final List<Map<String, Object>> list, final Map<String, Object> map) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare((Map<String, Object>) list.get(i), map) > 0) {
				return size;
			}
		}
		return list.size();
	}

	public static MeetingCompareTool getInstance() {
		if (MeetingCompareTool.ct == null) {
			MeetingCompareTool.ct = new MeetingCompareTool();
		}
		return MeetingCompareTool.ct;
	}

	@Override
	public int compare(final Map<String, Object> map, final Map<String, Object> map2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(map.get("title"), map2.get("title")) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(map.get("title"), map2.get("title")) > 0) {
			return 1;
		}
		return n;
	}

	public List<Map<String, Object>> sortByDefault(final List<Map<String, Object>> list) {
		final ArrayList<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		if (list != null && list.size() >= 1) {
			for (int i = 0; i < list.size(); ++i) {
				if (i == 0) {
					list2.add(list.get(i));
				} else {
					list2.add(this.findPos(list2, list.get(i)), list.get(i));
				}
			}
		}
		return list2;
	}
}
