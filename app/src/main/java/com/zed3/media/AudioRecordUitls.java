package com.zed3.media;

import android.media.AudioRecord;

import com.zed3.log.Logger;

public class AudioRecordUitls {
	private static boolean needLog;
	public static AudioRecord record;
	private static String tag;

	static {
		AudioRecordUitls.tag = "AudioRecordUitls";
		AudioRecordUitls.needLog = true;
	}

	public static AudioRecord getRecord(final int n, final int n2, final int n3, final int n4, final int n5) {
		synchronized (AudioRecordUitls.class) {
			if (AudioRecordUitls.record != null) {
				Logger.i(AudioRecordUitls.needLog, AudioRecordUitls.tag, "AudioRecordFactory-new AudioRecord record != null");
				releaseRecord(AudioRecordUitls.record);
			} else {
				Logger.i(AudioRecordUitls.needLog, AudioRecordUitls.tag, "AudioRecordFactory-new AudioRecord record == null");
			}
			return AudioRecordUitls.record = new AudioRecord(n, n2, n3, n4, n5);
		}
	}

	public static void releaseRecord(final AudioRecord audioRecord) {
		// TODO
	}
}
