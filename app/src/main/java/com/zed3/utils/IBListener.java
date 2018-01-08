package com.zed3.utils;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class IBListener {
	public static final float[] BT_NOT_SELECTED;
	public static final float[] BT_SELECTED;
	public static final View.OnFocusChangeListener buttonOnFocusChangeListener;
	public static final View.OnTouchListener buttonOnTouchListener;

	static {
		BT_SELECTED = new float[]{2.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
		BT_NOT_SELECTED = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
		buttonOnFocusChangeListener = (View.OnFocusChangeListener) new View.OnFocusChangeListener() {
			public void onFocusChange(final View view, final boolean b) {
				if (b) {
					view.getBackground().setColorFilter((ColorFilter) new ColorMatrixColorFilter(IBListener.BT_SELECTED));
					return;
				}
				view.getBackground().setColorFilter((ColorFilter) new ColorMatrixColorFilter(IBListener.BT_NOT_SELECTED));
			}
		};
		buttonOnTouchListener = (View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				if (motionEvent.getAction() == 0) {
					view.getBackground().setColorFilter((ColorFilter) new ColorMatrixColorFilter(IBListener.BT_SELECTED));
					view.setBackgroundDrawable(view.getBackground());
				} else if (motionEvent.getAction() == 1) {
					view.getBackground().setColorFilter((ColorFilter) new ColorMatrixColorFilter(IBListener.BT_NOT_SELECTED));
					view.setBackgroundDrawable(view.getBackground());
				}
				return false;
			}
		};
	}

	public static final void setButton(final ImageButton imageButton) {
		imageButton.setOnFocusChangeListener(IBListener.buttonOnFocusChangeListener);
		imageButton.setOnTouchListener(IBListener.buttonOnTouchListener);
	}
}
