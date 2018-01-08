package com.zed3.utils;

import android.text.TextUtils;

public class HeartBeatPacket {
	private static String mNonce;
	private static String prePassword;
	private static String preUserName;
	private static String sCallId;
	String grpNum;
	String grpState;
	String md5value;
	String nonce;
	String num;
	String pwd;

	static {
		HeartBeatPacket.preUserName = "";
		HeartBeatPacket.prePassword = "";
	}

	public HeartBeatPacket(final String s, final String s2, final String s3, final String s4) {
		this(s, s2, s3, s4, null);
	}

	public HeartBeatPacket(final String s, final String s2, final String grpNum, final String grpState, final String sCallId) {
		if (!HeartBeatPacket.preUserName.equals(s) || !HeartBeatPacket.prePassword.equals(s2)) {
			this.nonce = Tools.getRandomCharNum(8);
			HeartBeatPacket.mNonce = new String(this.nonce);
		}
		HeartBeatPacket.preUserName = s;
		HeartBeatPacket.prePassword = s2;
		this.md5value = this.packetMd5value(HeartBeatPacket.preUserName, HeartBeatPacket.prePassword, HeartBeatPacket.mNonce);
		this.num = s;
		this.grpNum = grpNum;
		this.pwd = s2;
		this.grpState = grpState;
		HeartBeatPacket.sCallId = sCallId;
	}

	private String packetMd5value(final String s, final String s2, final String s3) {
		final StringBuffer sb = new StringBuffer();
		sb.append(s).append(":").append(HeartBeatPacket.mNonce).append(":").append(s2);
		return MD5.toMd5(sb.toString());
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("RRR:").append(this.num).append(":").append(HeartBeatPacket.mNonce).append(":").append(this.md5value).append("\r\n");
		if (this.grpNum.length() > 0) {
			sb.append("CCC:").append(this.grpNum).append(":").append(this.grpState).append("\r\n");
		}
		if (!TextUtils.isEmpty((CharSequence) HeartBeatPacket.sCallId)) {
			sb.append("CID:").append(HeartBeatPacket.sCallId).append("\r\n");
		}
		return sb.toString();
	}
}
