package com.zed3.utils;

import android.text.TextUtils;

public class HeartBeatParser {
	private static final String CCC = "CCC";
	private static final String XXXXXX = "XXXXXX";

	public static HeartBeatGrpState parser(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final String[] split = s.split("\r\n");
			if (split != null && split.length > 0) {
				final boolean b = true;
				final HeartBeatGrpState heartBeatGrpState = new HeartBeatGrpState();
				while (true) {
					for (int length = split.length, i = 0; i < length; ++i) {
						final String s2 = split[i];
						if (!TextUtils.isEmpty((CharSequence) s2)) {
							if (s2.trim().startsWith("CCC")) {
								if (s.split(":").length != 3) {
									final int n = 0;
									if (n != 0) {
										return heartBeatGrpState;
									}
									return null;
								} else {
									final String[] split2 = s.split(":");
									heartBeatGrpState.setGrpName(split2[1]);
									heartBeatGrpState.setGrpState(split2[2].trim());
								}
							} else {
								s2.trim().startsWith("XXXXXX");
							}
						}
					}
					final int n = b ? 1 : 0;
					continue;
				}
			}
		}
		return null;
	}
}
