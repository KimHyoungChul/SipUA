package com.zed3.customgroup;

import android.text.TextUtils;

import com.zed3.sipua.PttGrp;
import com.zed3.sipua.ui.Receiver;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

public class CompareUtil implements Comparator<Map<String, String>> {
	private static CompareUtil instance;

	private Map<String, String> getGrpMemberMapById(final String s, final List<Map<String, String>> list) {
		HashMap<String, String> hashMap2;
		HashMap<String, String> hashMap = hashMap2 = new HashMap<String, String>();
		if (!TextUtils.isEmpty((CharSequence) s)) {
			hashMap2 = hashMap;
			if (list != null) {
				hashMap2 = hashMap;
				if (list.size() > 0) {
					HashMap<String, String> hashMap3;
					for (int i = 0; i < list.size(); ++i, hashMap = hashMap3) {
						final Map<String, String> map = list.get(i);
						final String s2 = map.get("curGrp_ID");
						hashMap3 = hashMap;
						if (s2 != null) {
							hashMap3 = hashMap;
							if (s2.equalsIgnoreCase(s)) {
								hashMap3 = (HashMap<String, String>) map;
							}
						}
					}
					hashMap2 = hashMap;
				}
			}
		}
		return hashMap2;
	}

	private int getIndexOf(final List<Map<String, String>> list, final Map<String, String> map) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare((Map<String, String>) list.get(i), map) > 0) {
				return size;
			}
		}
		return list.size();
	}

	public static CompareUtil getInstance() {
		if (CompareUtil.instance == null) {
			CompareUtil.instance = new CompareUtil();
		}
		return CompareUtil.instance;
	}

	@Override
	public int compare(final Map<String, String> map, final Map<String, String> map2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(map.get("curGrp_name"), map2.get("curGrp_name")) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(map.get("curGrp_name"), map2.get("curGrp_name")) > 0) {
			return 1;
		}
		return n;
	}

	public List<Map<String, String>> sortByDefault(final List<Map<String, String>> list) {
		final ArrayList<Map<String, String>> list2 = new ArrayList<Map<String, String>>();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); ++i) {
				final Map<String, String> map = list.get(i);
				if (i == 0) {
					list2.add(map);
				} else {
					list2.add(this.getIndexOf(list2, map), map);
				}
			}
		}
		return list2;
	}

	public List<Map<String, String>> sortByGrpOrder(final List<Map<String, String>> list, final boolean b) {
		final ArrayList<Map<String, String>> list2 = new ArrayList<Map<String, String>>();
		final Vector<PttGrp> pttGrps = Receiver.GetCurUA().GetAllGrps().getPttGrps();
		final Vector<PttGrp> vector = new Vector<PttGrp>();
		if (b) {
			for (final PttGrp pttGrp : pttGrps) {
				if (pttGrp.getType() == 0) {
					vector.add(pttGrp);
				}
			}
		} else {
			for (final PttGrp pttGrp2 : pttGrps) {
				if (pttGrp2.getType() != 0) {
					vector.add(pttGrp2);
				}
			}
		}
		if (vector != null && vector.size() > 0) {
			for (final PttGrp pttGrp3 : vector) {
				if (!TextUtils.isEmpty((CharSequence) pttGrp3.getGrpID())) {
					list2.add(this.getGrpMemberMapById(pttGrp3.getGrpID(), list));
				}
			}
		}
		return list2;
	}
}
