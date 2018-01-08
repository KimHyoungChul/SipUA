package com.zed3.utils;

public class RtpStreamSenderUtil {
	private static final String TAG = "RtpStreamSenderUtil";
	private static boolean sNeedSendMuteData;

	public static boolean needSendMuteData() {
		return RtpStreamSenderUtil.sNeedSendMuteData;
	}

	public static void reCheckNeedSendMuteData(final String s) {
		// TODO
	}

	public static void setNeedSendMuteData(final boolean sNeedSendMuteData, final String s) {
		RtpStreamSenderUtil.sNeedSendMuteData = sNeedSendMuteData;
		LogUtil.makeLog("RtpStreamSenderUtil", " setNeedSendMuteData(" + sNeedSendMuteData + "," + s + ")");
	}
}
