package com.zed3.media;

import com.zed3.net.RtpPacket;

import java.util.Comparator;

public class RtpPacketCompare implements Comparator<RtpPacket> {
	@Override
	public int compare(final RtpPacket rtpPacket, final RtpPacket rtpPacket2) {
		if (rtpPacket.getSequenceNumber() > rtpPacket2.getSequenceNumber()) {
			if (rtpPacket.getSequenceNumber() - rtpPacket2.getSequenceNumber() >= 32767) {
				return -1;
			}
		} else {
			if (rtpPacket.getSequenceNumber() >= rtpPacket2.getSequenceNumber()) {
				return 0;
			}
			if (rtpPacket2.getSequenceNumber() - rtpPacket.getSequenceNumber() < 32767) {
				return -1;
			}
		}
		return 1;
	}
}
