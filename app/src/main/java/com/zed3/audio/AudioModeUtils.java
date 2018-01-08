package com.zed3.audio;

import android.content.Context;
import android.media.AudioManager;

import com.zed3.sipua.SipUAApp;

public final class AudioModeUtils {
	private static final String TAG = "AudioModeUtils";
	static AudioManager mAudioManager;
	private static SpeakerphoneChangeListener mListener;

	static {
		AudioModeUtils.mListener = null;
		AudioModeUtils.mAudioManager = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	public static String formatMode(final int n) {
		switch (n) {
			default: {
				return "UNKNOWN-" + n;
			}
			case -1: {
				return "AudioManager.MODE_CURRENT";
			}
			case 2: {
				return "AudioManager.MODE_IN_CALL";
			}
			case 3: {
				return "AudioManager.MODE_IN_COMMUNICATION";
			}
			case -2: {
				return "AudioManager.MODE_INVALID";
			}
			case 0: {
				return "AudioManager.MODE_NORMAL";
			}
			case 1: {
				return "AudioManager.MODE_RINGTONE";
			}
		}
	}

	public static boolean isModeRingTone() {
		return AudioModeUtils.mAudioManager.getMode() == 1;
	}

	public static boolean isSpeakerPhoneOn() {
		return AudioModeUtils.mAudioManager.isSpeakerphoneOn();
	}

	public static void setAudioStyle(final int mode, final boolean speakerphoneOn) {
		synchronized (AudioModeUtils.class) {
			if (mode != AudioModeUtils.mAudioManager.getMode()) {
				AudioModeUtils.mAudioManager.setMode(mode);
			}
			if (speakerphoneOn != AudioModeUtils.mAudioManager.isSpeakerphoneOn()) {
				AudioModeUtils.mAudioManager.setSpeakerphoneOn(speakerphoneOn);
				if (AudioModeUtils.mListener != null) {
					AudioModeUtils.mListener.onSpeakerphoneOnChanged(speakerphoneOn);
				}
			}
		}
	}

	public static void setSpeakerphoneChangeLister(final SpeakerphoneChangeListener mListener) {
		AudioModeUtils.mListener = mListener;
		if (AudioModeUtils.mListener != null) {
			AudioModeUtils.mListener.onSpeakerphoneOnChanged(AudioModeUtils.mAudioManager.isSpeakerphoneOn());
		}
	}
}
