package com.zed3.utils;

import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;

public final class LoadingAnimation extends Handler implements Runnable {
	static final int DEFAULT_MAX_APPEND_COUNT = 3;
	static final int LOADING = 1;
	private int mAppendCount;
	private int mMaxAppendCount;
	private TextView mTarget;
	private String mTextSource;
	private String mTextTarget;

	public LoadingAnimation() {
		this.mMaxAppendCount = 3;
	}

	private String appendLoadingSymbol(final String s) {
		this.mTextTarget = String.valueOf(s) + ".";
		++this.mAppendCount;
		return this.mTextTarget;
	}

	private void appliyTargetText(final String text) {
		final TextView mTarget = this.mTarget;
		if (mTarget != null && !TextUtils.isEmpty((CharSequence) text)) {
			mTarget.setText((CharSequence) text);
		}
	}

	private void initParams(final TextView mTarget) {
		this.mTarget = mTarget;
		final String string = mTarget.getText().toString();
		this.mTextTarget = string;
		this.mTextSource = string;
	}

	private void postAnimation() {
		this.postDelayed((Runnable) this, 500L);
	}

	private void resetAppendCount() {
		this.mAppendCount = 0;
	}

	public boolean isStartAnimation() {
		return this.mTarget != null;
	}

	public void run() {
		if (this.mAppendCount >= this.mMaxAppendCount) {
			this.mAppendCount = 0;
			this.mTextTarget = this.mTextSource;
			this.appliyTargetText(this.mTextSource);
		} else {
			this.appliyTargetText(this.appendLoadingSymbol(this.mTextTarget));
		}
		this.postDelayed((Runnable) this, 500L);
	}

	public LoadingAnimation setAppendCount(final int mMaxAppendCount) {
		this.mMaxAppendCount = mMaxAppendCount;
		return this;
	}

	public void startAnimation(final TextView textView) {
		if (textView != null) {
			this.initParams(textView);
			this.postAnimation();
		}
	}

	public void stopAnimation() {
		this.removeCallbacks((Runnable) this);
		this.mTarget = null;
		this.resetAppendCount();
		this.mTextSource = null;
		this.mTextTarget = null;
	}
}
