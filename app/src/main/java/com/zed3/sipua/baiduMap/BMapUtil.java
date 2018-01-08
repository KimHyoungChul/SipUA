package com.zed3.sipua.baiduMap;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.MeasureSpec;

public class BMapUtil {
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		return view.getDrawingCache(true);
	}
}
