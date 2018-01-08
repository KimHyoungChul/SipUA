package com.zed3.media;

public interface MediaLauncher {
	void bluetoothMedia();

	boolean muteMedia();

	boolean sendDTMF(final char p0);

	int speakerMedia(final int p0);

	boolean startMedia();

	boolean stopMedia();
}
