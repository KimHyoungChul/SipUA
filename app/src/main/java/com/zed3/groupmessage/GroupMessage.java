package com.zed3.groupmessage;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

public class GroupMessage {
	private Set<String> numbers;
	private final int type;

	public GroupMessage(final int type) {
		this.type = type;
		this.numbers = new HashSet<String>();
	}

	public void addElement(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final String[] split = s.replaceAll("\uff0c", ",").split(",");
			for (int i = 0; i < split.length; ++i) {
				this.numbers.add(split[i]);
			}
		}
	}

	public Set<String> getNumbers() {
		return this.numbers;
	}

	public int getType() {
		return this.type;
	}
}
