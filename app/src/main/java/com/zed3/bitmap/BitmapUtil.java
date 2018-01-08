package com.zed3.bitmap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.zed3.utils.LogUtil;

public class BitmapUtil {
	private static final String TAG = "BitmapUtil";

	public Bitmap loadBitMap(int n, int inSampleSize, String decodeFile) {
		// TODO
		return null;
	}

	public boolean loadImage(final ImageView imageView, final int n, final int n2, final String s) {
		final StringBuilder sb = new StringBuilder("loadBitMap()");
		boolean b = false;
		boolean b2 = false;
		boolean b3 = false;
		try {
			final Bitmap loadBitMap = this.loadBitMap(n, n2, s);
			if (loadBitMap != null) {
				b3 = true;
			}
			if (imageView != null) {
				b = b3;
				b2 = b3;
				imageView.setImageBitmap(loadBitMap);
			}
			return b3;
		} catch (Exception ex) {
			b2 = b;
			ex.printStackTrace();
			b2 = b;
			sb.append(" Exception " + ex.getMessage());
			return b;
		} finally {
			sb.append(" return " + b2);
			LogUtil.makeLog("BitmapUtil", sb.toString());
		}
	}
}
