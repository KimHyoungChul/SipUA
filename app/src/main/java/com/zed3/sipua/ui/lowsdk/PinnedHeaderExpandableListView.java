package com.zed3.sipua.ui.lowsdk;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class PinnedHeaderExpandableListView extends ExpandableListView implements AbsListView.OnScrollListener, ExpandableListView.OnGroupClickListener {
	private static final int MAX_ALPHA = 255;
	private HeaderAdapter mAdapter;
	private float mDownX;
	private float mDownY;
	private View mHeaderView;
	private int mHeaderViewHeight;
	private boolean mHeaderViewVisible;
	private int mHeaderViewWidth;
	private int mOldState;
	private float xDistance;
	private float xLast;
	private float yDistance;
	private float yLast;

	public PinnedHeaderExpandableListView(final Context context) {
		super(context);
		this.mOldState = -1;
		this.registerListener();
	}

	public PinnedHeaderExpandableListView(final Context context, final AttributeSet set) {
		super(context, set);
		this.mOldState = -1;
		this.registerListener();
	}

	public PinnedHeaderExpandableListView(final Context context, final AttributeSet set, final int n) {
		super(context, set, n);
		this.mOldState = -1;
		this.registerListener();
	}

	private void headerViewClick() {
		final long expandableListPosition = this.getExpandableListPosition(this.getFirstVisiblePosition());
		final int packedPositionGroup = ExpandableListView.getPackedPositionGroup(expandableListPosition);
		Log.i("jiangkai", "groupPosition  " + packedPositionGroup + "  packedPosition " + expandableListPosition);
		if (this.isGroupExpanded(packedPositionGroup)) {
			this.collapseGroup(packedPositionGroup);
		} else {
			this.expandGroup(packedPositionGroup, false);
		}
		this.setSelectedGroup(packedPositionGroup);
	}

	private void registerListener() {
		this.setOnScrollListener((AbsListView.OnScrollListener) this);
		this.setOnGroupClickListener((ExpandableListView.OnGroupClickListener) this);
	}

	public void configureHeaderView(final int n, final int n2) {
		if (this.mHeaderView == null || this.mAdapter == null || ((ExpandableListAdapter) this.mAdapter).getGroupCount() == 0) {
			return;
		}
		switch (this.mAdapter.getHeaderState(n, n2)) {
			default: {
			}
			case 0: {
				this.mHeaderViewVisible = false;
			}
			case 1: {
				this.mAdapter.configureHeader(this.mHeaderView, n, n2, 255);
				if (this.mHeaderView.getTop() != 0) {
					this.mHeaderView.layout(0, 0, this.mHeaderViewWidth, this.mHeaderViewHeight);
				}
				this.mHeaderViewVisible = true;
			}
			case 2: {
				final int bottom = this.getChildAt(0).getBottom();
				final int height = this.mHeaderView.getHeight();
				int n3;
				int n4;
				if (bottom < height) {
					n3 = bottom - height;
					n4 = (height + n3) * 255 / height;
				} else {
					n3 = 0;
					n4 = 255;
				}
				this.mAdapter.configureHeader(this.mHeaderView, n, n2, n4);
				if (this.mHeaderView.getTop() != n3) {
					this.mHeaderView.layout(0, n3, this.mHeaderViewWidth, this.mHeaderViewHeight + n3);
				}
				this.mHeaderViewVisible = true;
			}
		}
	}

	protected void dispatchDraw(final Canvas canvas) {
		super.dispatchDraw(canvas);
		if (this.mHeaderViewVisible) {
			this.drawChild(canvas, this.mHeaderView, this.getDrawingTime());
		}
	}

	public boolean dispatchTouchEvent(final MotionEvent motionEvent) {
		final boolean b = false;
		final boolean b2 = true;
		Log.i("jiangkai", "mHeaderViewVisible  " + this.mHeaderViewVisible);
		if (this.mHeaderViewVisible) {
			switch (motionEvent.getAction()) {
				case 0: {
					this.mDownX = motionEvent.getX();
					this.mDownY = motionEvent.getY();
					if (this.mDownX <= this.mHeaderViewWidth && this.mDownY <= this.mHeaderViewHeight) {
						return true;
					}
					break;
				}
				case 1: {
					final float x = motionEvent.getX();
					final float y = motionEvent.getY();
					final float abs = Math.abs(x - this.mDownX);
					final float abs2 = Math.abs(y - this.mDownY);
					final StringBuilder append = new StringBuilder("mHeaderView  ").append(this.mHeaderView != null).append("  ").append(x <= this.mHeaderViewWidth).append("  ").append(y <= this.mHeaderViewHeight).append("  ").append(abs <= this.mHeaderViewWidth).append("  ");
					boolean b3 = b;
					if (abs2 <= this.mHeaderViewHeight) {
						b3 = true;
					}
					Log.i("jiangkai", append.append(b3).toString());
					if (x > this.mHeaderViewWidth || y > this.mHeaderViewHeight || abs > this.mHeaderViewWidth || abs2 > this.mHeaderViewHeight) {
						break;
					}
					final boolean dispatchTouchEvent = b2;
					if (this.mHeaderView != null) {
						this.headerViewClick();
						return true;
					}
					return dispatchTouchEvent;
				}
			}
		} else {
			switch (motionEvent.getAction()) {
				case 0: {
					this.yDistance = 0.0f;
					this.xDistance = 0.0f;
					this.xLast = motionEvent.getX();
					this.yLast = motionEvent.getY();
					break;
				}
				case 2: {
					final float x2 = motionEvent.getX();
					final float y2 = motionEvent.getY();
					this.xDistance += Math.abs(x2 - this.xLast);
					this.yDistance += Math.abs(y2 - this.yLast);
					this.xLast = x2;
					this.yLast = y2;
					if (this.xDistance > this.yDistance) {
						return false;
					}
					break;
				}
			}
		}
		return super.dispatchTouchEvent(motionEvent);
	}

	public View getHeaderView() {
		return this.mHeaderView;
	}

	public boolean onGroupClick(final ExpandableListView expandableListView, final View view, final int n, final long n2) {
		if (expandableListView.isGroupExpanded(n)) {
			expandableListView.collapseGroup(n);
		} else {
			expandableListView.expandGroup(n, false);
		}
		return true;
	}

	protected void onLayout(final boolean b, int packedPositionGroup, int packedPositionChild, int headerState, final int n) {
		super.onLayout(b, packedPositionGroup, packedPositionChild, headerState, n);
		final long expandableListPosition = this.getExpandableListPosition(this.getFirstVisiblePosition());
		packedPositionGroup = ExpandableListView.getPackedPositionGroup(expandableListPosition);
		packedPositionChild = ExpandableListView.getPackedPositionChild(expandableListPosition);
		headerState = this.mAdapter.getHeaderState(packedPositionGroup, packedPositionChild);
		if (this.mHeaderView != null && this.mAdapter != null && headerState != this.mOldState) {
			this.mOldState = headerState;
			this.mHeaderView.layout(0, 0, this.mHeaderViewWidth, this.mHeaderViewHeight);
		}
		this.configureHeaderView(packedPositionGroup, packedPositionChild);
	}

	protected void onMeasure(final int n, final int n2) {
		super.onMeasure(n, n2);
		if (this.mHeaderView != null) {
			this.measureChild(this.mHeaderView, n, n2);
			this.mHeaderViewWidth = this.mHeaderView.getMeasuredWidth();
			this.mHeaderViewHeight = this.mHeaderView.getMeasuredHeight();
		}
	}

	public void onScroll(final AbsListView absListView, final int n, final int n2, final int n3) {
		final long expandableListPosition = this.getExpandableListPosition(n);
		this.configureHeaderView(ExpandableListView.getPackedPositionGroup(expandableListPosition), ExpandableListView.getPackedPositionChild(expandableListPosition));
	}

	public void onScrollStateChanged(final AbsListView absListView, final int n) {
	}

	public void setAdapter(final ExpandableListAdapter adapter) {
		super.setAdapter(adapter);
		this.mAdapter = (HeaderAdapter) adapter;
	}

	public void setHeaderView(final View mHeaderView) {
		(this.mHeaderView = mHeaderView).setLayoutParams((ViewGroup.LayoutParams) new AbsListView.LayoutParams(-1, -2));
		if (this.mHeaderView != null) {
			this.setFadingEdgeLength(0);
		}
		this.requestLayout();
	}

	public interface HeaderAdapter {
		public static final int PINNED_HEADER_GONE = 0;
		public static final int PINNED_HEADER_PUSHED_UP = 2;
		public static final int PINNED_HEADER_VISIBLE = 1;

		void configureHeader(final View p0, final int p1, final int p2, final int p3);

		int getGroupClickStatus(final int p0);

		int getHeaderState(final int p0, final int p1);

		void setGroupClickStatus(final int p0, final int p1);
	}
}
