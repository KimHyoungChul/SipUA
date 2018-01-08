package com.zed3.jni;

import android.util.Log;

import com.zed3.log.MyLog;

public class VideoUtils {
	private static String tag;

	static {
		VideoUtils.tag = "VideoUtils";
		loadLibrary();
	}

	public static int[] getWithAndHight(final byte[] array) {
		synchronized (VideoUtils.class) {
			final int[] withAndHightFromC = null;
			// TODO getWithAndHightFromC(array, new int[2]);
			Log.i(VideoUtils.tag, "BYTE = " + withAndHightFromC[0] + ":" + withAndHightFromC[1]);
			return withAndHightFromC;
		}
	}

//	static native int[] getWithAndHightFromC(final byte[] p0, final int[] p1);

	private static void loadLibrary() {
		try {
			MyLog.i(VideoUtils.tag, "ready load h264_wh.so");
			System.loadLibrary("H264_WH");
			MyLog.i(VideoUtils.tag, "load h264_wh.so success");
		} catch (Exception ex) {
			MyLog.e(VideoUtils.tag, "loadLibrary error!" + ex.toString());
		}
	}
}
