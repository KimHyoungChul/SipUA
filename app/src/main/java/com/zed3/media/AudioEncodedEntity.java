package com.zed3.media;

import com.zed3.net.RtpPacket;

public class AudioEncodedEntity {
	int m;
	RtpPacket rtp_packet;

	public AudioEncodedEntity(final int m, final RtpPacket rtp_packet) {
		this.m = m;
		this.rtp_packet = rtp_packet;
	}
}
