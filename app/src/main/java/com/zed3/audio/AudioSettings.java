package com.zed3.audio;

public class AudioSettings {
	public static volatile boolean isAECOpen;
	public static boolean isAGCOpen;
	public static long startTempStamp;

	static {
		AudioSettings.isAECOpen = true;
		AudioSettings.isAGCOpen = false;
		AudioSettings.startTempStamp = 0L;
	}
}
