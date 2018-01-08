package com.zed3.utils;

import android.os.Handler;

import com.zed3.media.RtpStreamSender_group;

public class MyHandler {
	private static Handler myHandler;

	public static void sendMessage(final int n) {
		if (!RtpStreamSender_group.mPTTPause && MyHandler.myHandler != null) {
			MyHandler.myHandler.sendMessage(MyHandler.myHandler.obtainMessage(1, n, 0));
		}
	}

	public static void sendReceiveMessage(final int n) {
		if (RtpStreamSender_group.mPTTPause && MyHandler.myHandler != null) {
			MyHandler.myHandler.sendMessage(MyHandler.myHandler.obtainMessage(2, n, 0));
		}
	}

	public static void setHandler(final Handler myHandler) {
		MyHandler.myHandler = myHandler;
	}
}
