package com.zed3.video;

import java.util.Comparator;

public class ReceivePacketCompare implements Comparator<ReceivePacketInfo> {
	@Override
	public int compare(final ReceivePacketInfo receivePacketInfo, final ReceivePacketInfo receivePacketInfo2) {
		if (receivePacketInfo.getSeqNum() > receivePacketInfo2.getSeqNum()) {
			if (receivePacketInfo.getSeqNum() - receivePacketInfo2.getSeqNum() >= 32767) {
				return -1;
			}
		} else {
			if (receivePacketInfo.getSeqNum() >= receivePacketInfo2.getSeqNum()) {
				return 0;
			}
			if (receivePacketInfo2.getSeqNum() - receivePacketInfo.getSeqNum() < 32767) {
				return -1;
			}
		}
		return 1;
	}
}
