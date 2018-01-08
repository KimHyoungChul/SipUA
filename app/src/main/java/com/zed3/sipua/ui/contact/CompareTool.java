package com.zed3.sipua.ui.contact;

import com.zed3.sipua.ui.lowsdk.ContactPerson;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CompareTool implements Comparator<ContactPerson> {
	private static CompareTool ct;

	static {
		CompareTool.ct = null;
	}

	private int findPos(final List<ContactPerson> list, final ContactPerson contactPerson) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare((ContactPerson) list.get(i), contactPerson) > 0) {
				return size;
			}
		}
		return list.size();
	}

	public static CompareTool getInstance() {
		if (CompareTool.ct == null) {
			CompareTool.ct = new CompareTool();
		}
		return CompareTool.ct;
	}

	@Override
	public int compare(final ContactPerson contactPerson, final ContactPerson contactPerson2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(contactPerson.getContact_name(), contactPerson2.getContact_name()) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(contactPerson.getContact_name(), contactPerson2.getContact_name()) > 0) {
			return 1;
		}
		return n;
	}

	public List<ContactPerson> sortByDefault(final List<ContactPerson> list) {
		final ArrayList<ContactPerson> list2 = new ArrayList<ContactPerson>();
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
