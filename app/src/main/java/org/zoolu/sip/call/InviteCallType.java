package org.zoolu.sip.call;

public class InviteCallType {
	public static final int AUDIO_CALL = 0;
	public static final int BROADCAST_CALL = 6;
	public static final int CONFERENCE_CALL = 5;
	public static final int GROUP_CALL = 2;
	public static final int TEMPORARY_CALL = 3;
	public static final int UNKNOWN = -1;
	public static final int UPLOAD_MONITOR_CALL = 4;
	public static final int VIDEO_CALL = 1;

	public static String getCallTypeString(final int n) {
		switch (n) {
			default: {
				return "unknown";
			}
			case 0: {
				return "audio call";
			}
			case 1: {
				return "video call";
			}
			case 2: {
				return "group call";
			}
			case 3: {
				return "temporary call";
			}
			case 4: {
				return "upload or monitor video call";
			}
			case 5: {
				return "conference call";
			}
			case 6: {
				return "broadcast call";
			}
		}
	}
}
