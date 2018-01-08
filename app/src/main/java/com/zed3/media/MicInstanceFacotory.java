package com.zed3.media;

import android.media.AudioRecord;
import android.media.MediaRecorder;

public final class MicInstanceFacotory {
	static final String TAG = "MicInstanceFacotory";
	static AudioRecord mAudioRecord;
	static MediaRecorderListener mListener;
	static MediaRecorder mMediaRecorder;
	private static int sampleRateInHz;

	static {
		MicInstanceFacotory.mAudioRecord = null;
		MicInstanceFacotory.mMediaRecorder = null;
		MicInstanceFacotory.mListener = null;
		MicInstanceFacotory.sampleRateInHz = 8000;
	}

	private static int SingleRecordMin() {
		int minBufferSize;
		if ((minBufferSize = AudioRecord.getMinBufferSize(MicInstanceFacotory.sampleRateInHz, 16, 2)) < 3360) {
			minBufferSize = 3360;
		}
		return minBufferSize;
	}

	public static int getAudioSessionId() {
		if (MicInstanceFacotory.mAudioRecord == null) {
			return 0;
		}
		return MicInstanceFacotory.mAudioRecord.getAudioSessionId();
	}

	public static MediaRecorder getMediaRecorder(final MediaRecorderListener mListener) {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mMediaRecorder == null && (MicInstanceFacotory.mAudioRecord == null || MicInstanceFacotory.mAudioRecord.getState() == 0 || MicInstanceFacotory.mAudioRecord.getRecordingState() != 3)) {
				MicInstanceFacotory.mMediaRecorder = new MediaRecorder();
				MicInstanceFacotory.mListener = mListener;
			}
			return MicInstanceFacotory.mMediaRecorder;
		}
	}

	public static void getRecord() {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mAudioRecord == null) {
				if (MicInstanceFacotory.mMediaRecorder != null) {
					releaseMediaRecorder();
					if (MicInstanceFacotory.mListener != null) {
						MicInstanceFacotory.mListener.onRecorderRelease();
						MicInstanceFacotory.mListener = null;
					}
				}
				MicInstanceFacotory.mAudioRecord = new AudioRecord(1, MicInstanceFacotory.sampleRateInHz, 16, 2, SingleRecordMin());
			}
		}
	}

	public static boolean isAudioRecorderEmpty() {
		return MicInstanceFacotory.mAudioRecord == null;
	}

	public static boolean isRecordInited() {
		return MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() == 1;
	}

	public static boolean isRecordStateRecording() {
		return MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() != 0 && MicInstanceFacotory.mAudioRecord.getRecordingState() == 3;
	}

	public static boolean isRecordStateStopped() {
		return MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() != 0 && MicInstanceFacotory.mAudioRecord.getRecordingState() == 1;
	}

	public static boolean isRecordUnInited() {
		return MicInstanceFacotory.mAudioRecord == null || MicInstanceFacotory.mAudioRecord.getState() == 0;
	}

	public static int read(final short[] array, int read, final int n) {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() == 1) {
				read = MicInstanceFacotory.mAudioRecord.read(array, read, n);
			} else {
				read = 0;
			}
			return read;
		}
	}

	public static void releaseAudioRecord() {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mAudioRecord != null) {
				if (MicInstanceFacotory.mAudioRecord.getState() == 3) {
					MicInstanceFacotory.mAudioRecord.stop();
				}
				MicInstanceFacotory.mAudioRecord.release();
				MicInstanceFacotory.mAudioRecord = null;
			}
		}
	}

	public static void releaseMediaRecorder() {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mMediaRecorder != null) {
				MicInstanceFacotory.mMediaRecorder.stop();
				MicInstanceFacotory.mMediaRecorder.release();
				MicInstanceFacotory.mMediaRecorder = null;
			}
		}
	}

	public static void startRecording() {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() == 1) {
				if (MicInstanceFacotory.mMediaRecorder != null) {
					releaseMediaRecorder();
					if (MicInstanceFacotory.mListener != null) {
						MicInstanceFacotory.mListener.onRecorderRelease();
						MicInstanceFacotory.mListener = null;
					}
				}
				if (MicInstanceFacotory.mAudioRecord.getRecordingState() != 3) {
					MicInstanceFacotory.mAudioRecord.startRecording();
				}
			}
		}
	}

	public static void stop() {
		synchronized (MicInstanceFacotory.class) {
			if (MicInstanceFacotory.mAudioRecord != null && MicInstanceFacotory.mAudioRecord.getState() == 1 && MicInstanceFacotory.mAudioRecord.getRecordingState() != 1) {
				MicInstanceFacotory.mAudioRecord.stop();
			}
		}
	}
}
