package com.zed3.sipua.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ButtonGridLayout extends ViewGroup {
	private final int mColumns;
	private int mPaddingBottom;
	private int mPaddingLeft;
	private int mPaddingRight;
	private int mPaddingTop;

	public ButtonGridLayout(final Context context) {
		super(context);
		this.mColumns = 3;
		this.mPaddingBottom = 0;
		this.mPaddingLeft = 0;
		this.mPaddingRight = 0;
		this.mPaddingTop = 0;
	}

	public ButtonGridLayout(final Context context, final AttributeSet set) {
		super(context, set);
		this.mColumns = 3;
		this.mPaddingBottom = 0;
		this.mPaddingLeft = 0;
		this.mPaddingRight = 0;
		this.mPaddingTop = 0;
	}

	public ButtonGridLayout(final Context context, final AttributeSet set, final int n) {
		super(context, set, n);
		this.mColumns = 3;
		this.mPaddingBottom = 0;
		this.mPaddingLeft = 0;
		this.mPaddingRight = 0;
		this.mPaddingTop = 0;
	}

	private int getRows() {
		return (this.getChildCount() + 3 - 1) / 3;
	}

	protected void onLayout(final boolean b, int i, int mPaddingTop, int j, int mPaddingLeft) {
		mPaddingTop = this.mPaddingTop;
		final int rows = this.getRows();
		final View child = this.getChildAt(0);
		final int n = (this.getHeight() - this.mPaddingTop - this.mPaddingBottom) / rows;
		final int n2 = (this.getWidth() - this.mPaddingLeft - this.mPaddingRight) / 3;
		final int measuredWidth = child.getMeasuredWidth();
		final int measuredHeight = child.getMeasuredHeight();
		final int n3 = (n2 - measuredWidth) / 2;
		final int n4 = (n - measuredHeight) / 2;
		int n5;
		for (i = 0; i < rows; ++i) {
			mPaddingLeft = this.mPaddingLeft;
			for (j = 0; j < 3; ++j) {
				n5 = i * 3 + j;
				if (n5 >= this.getChildCount()) {
					break;
				}
				this.getChildAt(n5).layout(mPaddingLeft + n3, mPaddingTop + n4, mPaddingLeft + n3 + measuredWidth, mPaddingTop + n4 + measuredHeight);
				mPaddingLeft += n2;
			}
			mPaddingTop += n;
		}
	}

	protected void onMeasure(final int n, final int n2) {
		final int mPaddingLeft = this.mPaddingLeft;
		final int mPaddingRight = this.mPaddingRight;
		final int mPaddingTop = this.mPaddingTop;
		final int mPaddingBottom = this.mPaddingBottom;
		final View child = this.getChildAt(0);
		child.measure(0, 0);
		final int measuredWidth = child.getMeasuredWidth();
		final int measuredHeight = child.getMeasuredHeight();
		for (int i = 1; i < this.getChildCount(); ++i) {
			this.getChildAt(0).measure(0, 0);
		}
		this.setMeasuredDimension(resolveSize(mPaddingLeft + mPaddingRight + measuredWidth * 3, n), resolveSize(mPaddingTop + mPaddingBottom + this.getRows() * measuredHeight, n2));
	}
}
