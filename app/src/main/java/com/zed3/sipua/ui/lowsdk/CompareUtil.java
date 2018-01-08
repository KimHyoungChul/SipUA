package com.zed3.sipua.ui.lowsdk;

import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

public class CompareUtil implements Comparator<PttGrp> {
	private static CompareUtil instance;

	private int getIndexOf(final Vector<PttGrp> vector, final PttGrp pttGrp) {
		for (int i = 0; i < vector.size(); ++i) {
			final int size = i;
			if (this.compare((PttGrp) vector.elementAt(i), pttGrp) > 0) {
				return size;
			}
		}
		return vector.size();
	}

	public static CompareUtil getInstance() {
		if (CompareUtil.instance == null) {
			CompareUtil.instance = new CompareUtil();
		}
		return CompareUtil.instance;
	}

	@Override
	public int compare(final PttGrp pttGrp, final PttGrp pttGrp2) {
		int n = 0;
		if (Collator.getInstance(Locale.CHINA).compare(pttGrp.getGrpName(), pttGrp2.getGrpName()) < 0) {
			n = -1;
		} else if (Collator.getInstance(Locale.CHINA).compare(pttGrp.getGrpName(), pttGrp2.getGrpName()) > 0) {
			return 1;
		}
		return n;
	}

	public PttGrps sortByDefault(final PttGrps pttGrps) {
		final Vector<PttGrp> pttGrps2 = pttGrps.getPttGrps();
		if (pttGrps2 != null && pttGrps2.size() > 0) {
			for (int i = 0; i < pttGrps2.size(); ++i) {
				final PttGrp pttGrp = pttGrps2.elementAt(i);
				if (i == 0) {
					pttGrps2.add(pttGrp);
				} else {
					pttGrps2.add(this.getIndexOf(pttGrps2, pttGrp), pttGrp);
				}
			}
		}
		return pttGrps;
	}
}
