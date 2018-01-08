package com.zed3.media;

import android.media.AudioRecord;

import com.zed3.log.Logger;

public class AudioRecordUtils2 {
	private static boolean needLog;
	private static AudioRecord record;
	private static String tag;
	private static int userNum;

	static {
		AudioRecordUtils2.userNum = 0;
		AudioRecordUtils2.tag = "AudioRecordUtils";
		AudioRecordUtils2.needLog = true;
	}

	public static AudioRecord getAudioRecord(final int n, final int n2, final int n3, final int n4, final int n5) {
		synchronized (AudioRecordUtils2.class) {
			if (AudioRecordUtils2.record == null) {
				Logger.i(AudioRecordUtils2.needLog, AudioRecordUtils2.tag, "AudioRecordUtils-getAudioRecord() userNum == 0 ");
				AudioRecordUtils2.record = new AudioRecord(n, n2, n3, n4, n5);
			} else {
				Logger.i(AudioRecordUtils2.needLog, AudioRecordUtils2.tag, "AudioRecordUtils-getAudioRecord() userNum == " + AudioRecordUtils2.userNum);
			}
			++AudioRecordUtils2.userNum;
			return AudioRecordUtils2.record;
		}
	}

	public static void releaseAudioRecord(final AudioRecord audioRecord) {
		// TODO
	}
}
