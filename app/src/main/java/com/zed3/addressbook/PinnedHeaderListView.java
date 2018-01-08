package com.zed3.addressbook;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PinnedHeaderListView extends ListView {
	private boolean mDrawFlag;
	private View mHeaderView;
	private int mMeasuredHeight;
	private int mMeasuredWidth;
	private PinnedHeaderAdapter mPinnedHeaderAdapter;

	public PinnedHeaderListView(final Context context) {
		super(context);
		this.mDrawFlag = true;
	}

	public PinnedHeaderListView(final Context context, final AttributeSet set) {
		super(context, set);
		this.mDrawFlag = true;
	}

	public PinnedHeaderListView(final Context context, final AttributeSet set, final int n) {
		super(context, set, n);
		this.mDrawFlag = true;
	}

	public void controlPinnedHeader(int bottom) {
		if (this.mHeaderView != null) {
			switch (this.mPinnedHeaderAdapter.getPinnedHeaderState(bottom)) {
				default: {
				}
				case 0: {
					this.mDrawFlag = false;
				}
				case 1: {
					this.mPinnedHeaderAdapter.configurePinnedHeader(this.mHeaderView, bottom, 0);
					this.mDrawFlag = true;
					this.mHeaderView.layout(0, 0, this.mMeasuredWidth, this.mMeasuredHeight);
				}
				case 2: {
					this.mPinnedHeaderAdapter.configurePinnedHeader(this.mHeaderView, bottom, 0);
					this.mDrawFlag = true;
					final View child = this.getChildAt(0);
					if (child == null) {
						break;
					}
					bottom = child.getBottom();
					final int height = this.mHeaderView.getHeight();
					if (bottom < height) {
						bottom -= height;
					} else {
						bottom = 0;
					}
					if (this.mHeaderView.getTop() != bottom) {
						this.mHeaderView.layout(0, bottom, this.mMeasuredWidth, this.mMeasuredHeight + bottom);
						return;
					}
					break;
				}
			}
		}
	}

	protected void dispatchDraw(final Canvas canvas) {
		super.dispatchDraw(canvas);
		if (this.mHeaderView != null && this.mDrawFlag) {
			this.drawChild(canvas, this.mHeaderView, this.getDrawingTime());
		}
	}

	protected void onLayout(final boolean b, final int n, final int n2, final int n3, final int n4) {
		super.onLayout(b, n, n2, n3, n4);
		if (this.mHeaderView != null) {
			this.mHeaderView.layout(0, 0, this.mMeasuredWidth, this.mMeasuredHeight);
			this.controlPinnedHeader(this.getFirstVisiblePosition());
		}
	}

	protected void onMeasure(final int n, final int n2) {
		super.onMeasure(n, n2);
		if (this.mHeaderView != null) {
			this.measureChild(this.mHeaderView, n, n2);
			this.mMeasuredWidth = this.mHeaderView.getMeasuredWidth();
			this.mMeasuredHeight = this.mHeaderView.getMeasuredHeight();
		}
	}

	public void setAdapter(final ListAdapter adapter) {
		super.setAdapter(adapter);
		this.mPinnedHeaderAdapter = (PinnedHeaderAdapter) adapter;
	}

	public void setPinnedHeader(final View mHeaderView) {
		this.mHeaderView = mHeaderView;
		this.requestLayout();
	}

	public interface PinnedHeaderAdapter {
		public static final int PINNED_HEADER_GONE = 0;
		public static final int PINNED_HEADER_PUSHED_UP = 2;
		public static final int PINNED_HEADER_VISIBLE = 1;

		void configurePinnedHeader(final View p0, final int p1, final int p2);

		int getPinnedHeaderState(final int p0);
	}
}
