package com.zed3.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {
	private static DisplayMetrics displayMetrics;

	public static int dip2px(final Context context, final float n) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (n * DensityUtil.displayMetrics.density + 0.5f);
	}

	public static int getDipHeight(final Context context) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return px2dip(context, DensityUtil.displayMetrics.heightPixels);
	}

	public static int getDipWidth(final Context context) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return px2dip(context, DensityUtil.displayMetrics.widthPixels);
	}

	public static int getPxHeight(final Context context) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return DensityUtil.displayMetrics.heightPixels;
	}

	public static int getPxWidth(final Context context) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return DensityUtil.displayMetrics.widthPixels;
	}

	public static int px2dip(final Context context, final float n) {
		if (DensityUtil.displayMetrics == null) {
			DensityUtil.displayMetrics = context.getResources().getDisplayMetrics();
		}
		return (int) (n / DensityUtil.displayMetrics.density + 0.5f);
	}
}
