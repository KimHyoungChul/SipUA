package com.zed3.sipua.ui;

import android.content.Context;
import android.util.AttributeSet;

public class InstantAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {
	public InstantAutoCompleteTextView(final Context context, final AttributeSet set) {
		super(context, set);
	}

	public boolean enoughToFilter() {
		return true;
	}

	public void onWindowFocusChanged(final boolean b) {
		super.onWindowFocusChanged(b);
	}
}
