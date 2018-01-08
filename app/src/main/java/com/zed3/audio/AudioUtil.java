package com.zed3.audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;

import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.LogUtil;

public class AudioUtil implements AudioUitlInterface {
	public static final String ACTION_SPEAKERPHONE_STATE_CHANGED = "speakerphone changed";
	public static final String ACTION_STREAM_CHANGED = "stream changed";
	private static final String KEY_ANTOCALL_AUDIO_CONNECT_MODE = "KEY_ANTOCALL_AUDIO_CONNECT_MODE";
	private static final String KEY_AUDIOCALL_AUDIO_CONNECT_MODE = "KEY_AUDIOCALL_AUDIO_CONNECT_MODE";
	private static final String KEY_AUDIO_CONNECT_MODE = "KEY_AUDIO_CONNECT_MODE";
	private static final String KEY_GROUPCALL_AUDIO_CONNECT_MODE = "KEY_GROUPCALL_AUDIO_CONNECT_MODE";
	public static final String KEY_STREAM_INT = "key stream int";
	private static final String KEY_VIDEOCALL_AUDIO_CONNECT_MODE = "KEY_VIDEOCALL_AUDIO_CONNECT_MODE";
	public static final int MODE_BLUETOOTH = 4;
	public static final int MODE_HOOK = 2;
	private static int MODE_IN_COMMUNICATION = getBestMode();
	public static final int MODE_RINGTONE = 1;
	public static final int MODE_SPEAKER = 3;
	public static final int TYPE_ANTOCALL = 13;
	public static final int TYPE_AUDIOCALL = 12;
	public static final int TYPE_GROUPCALL = 10;
	public static final int TYPE_VIDEOCALL = 11;
	public static AudioManager mAudioManager = ((AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE));
	public static AudioUtil mInstance = new AudioUtil();
	private static boolean needSetSpeakerphoneOnFalse = false;
	private static String[] needSetSpeakerphoneOnFalseDevices = new String[0];
	public static final String tag = "AudioUtil";
	private boolean isFirstChecking = true;
	private int mMode = 0;
	private int mStream = 0;

	public static AudioUtil getInstance() {
		return mInstance;
	}

	public synchronized int getMode() {
		int mode;
		mode = mAudioManager.getMode();
		LogUtil.makeLog(tag, "getMode()" + getModeStr(mode));
		return mode;
	}

	public synchronized void setMode(int mode) {
		mAudioManager.setMode(mode);
		LogUtil.makeLog(tag, "setMode(" + getModeStr(mode) + ")");
	}

	private AudioUtil() {
	}

	/* JADX WARNING: inconsistent code. */
	/* Code decompiled incorrectly, please refer to instructions dump. */
	public synchronized void setAudioConnectMode(int r9) {
		// TODO
	}

	private boolean checkDevice() {
		if (Build.MODEL.equals("XT885")) {
			return false;
		}
		return true;
	}

	public int getCurrentMode() {
		return this.mMode;
	}

	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences("com.zed3.app", 0);
	}

	public void startBluetoothSCO() {
		if (!mAudioManager.isBluetoothScoOn()) {
			MyLog.i(tag, "startConnectSco() startBluetoothSco(),setBluetoothScoOn(true)");
			mAudioManager.startBluetoothSco();
			mAudioManager.setBluetoothScoOn(true);
		}
	}

	public void stopBluetoothSCO() {
		if (mAudioManager.isBluetoothScoOn()) {
			MyLog.i(tag, "stopConnectSco() stopBluetoothSco(),setBluetoothScoOn(false)");
			mAudioManager.setBluetoothScoOn(false);
			mAudioManager.stopBluetoothSco();
		}
	}

	public boolean checkMode(int mode) {
		switch (mode) {
			case 2:
				MyLog.i(tag, "checkMode(" + mode + "),is MODE_HOOK");
				return true;
			case 3:
				MyLog.i(tag, "checkMode(" + mode + "),is MODE_SPEAKER");
				return true;
			case 4:
				MyLog.i(tag, "checkMode(" + mode + "),is MODE_BLUETOOTH");
				return true;
			default:
				MyLog.e(tag, "checkMode(" + mode + "),unkown mode error");
				return false;
		}
	}

	public boolean setCustomMode(int type, int mode) {
		MyLog.i(tag, "saveMode(" + mode + ")");
		if (checkMode(mode)) {
			Editor editor = getSharedPreferences(SipUAApp.mContext).edit();
			switch (type) {
				case 10:
					editor.putInt(KEY_GROUPCALL_AUDIO_CONNECT_MODE, mode);
					break;
				case 11:
					editor.putInt(KEY_VIDEOCALL_AUDIO_CONNECT_MODE, mode);
					break;
				case 12:
					editor.putInt(KEY_AUDIOCALL_AUDIO_CONNECT_MODE, mode);
					break;
				case 13:
					editor.putInt(KEY_ANTOCALL_AUDIO_CONNECT_MODE, mode);
					break;
			}
			editor.commit();
			MyLog.i(tag, "saveMode(" + mode + ")");
			return true;
		}
		MyLog.e(tag, "saveMode(" + mode + "),bad mode error");
		return false;
	}

	public int getCustomMode(int type) {
		SharedPreferences sharedPreferences = getSharedPreferences(SipUAApp.mContext);
		switch (type) {
			case 10:
				this.mMode = sharedPreferences.getInt(KEY_GROUPCALL_AUDIO_CONNECT_MODE, 3);
				break;
			case 11:
				this.mMode = sharedPreferences.getInt(KEY_VIDEOCALL_AUDIO_CONNECT_MODE, 2);
				break;
			case 12:
				this.mMode = sharedPreferences.getInt(KEY_AUDIOCALL_AUDIO_CONNECT_MODE, 2);
				break;
			case 13:
				this.mMode = sharedPreferences.getInt(KEY_ANTOCALL_AUDIO_CONNECT_MODE, 2);
				break;
			default:
				this.mMode = 3;
				break;
		}
		return this.mMode;
	}

	public void setVolumeControlStream(Activity activity) {
	}

	public void setStream(int streamType) {
		this.mStream = streamType;
		Intent intent = new Intent(ACTION_STREAM_CHANGED);
		Bundle extras = new Bundle();
		extras.putInt(KEY_STREAM_INT, streamType);
		intent.putExtras(extras);
		SipUAApp.getAppContext().sendBroadcast(intent);
	}

	public synchronized void setSpeakerphoneOn(Boolean on) {
		mAudioManager.setSpeakerphoneOn(on.booleanValue());
		SipUAApp.getAppContext().sendBroadcast(new Intent(ACTION_SPEAKERPHONE_STATE_CHANGED));
		LogUtil.makeLog(tag, "setSpeakerphoneOn(" + on + ")");
	}

	public synchronized Boolean isSpeakerphoneOn() {
		Boolean on;
		on = Boolean.valueOf(mAudioManager.isSpeakerphoneOn());
		LogUtil.makeLog(tag, "isSpeakerphoneOn() " + on);
		return on;
	}

	private boolean checkMate7(StringBuilder builder) {
		if (!Build.MODEL.contains("HUAWEI MT7")) {
			return false;
		}
		builder.append(" Build.MODEL.contains(HUAWEI MT7) " + Build.MODEL);
		return true;
	}

	public void exit() {
		StringBuilder builder = new StringBuilder("exit()");
		if (mAudioManager.getMode() != 0) {
			builder.append(" setMode(AudioManager.MODE_NORMAL)");
			setMode(0);
		}
		if (mAudioManager.isSpeakerphoneOn()) {
			builder.append(" setSpeakerphoneOn(false)");
			setSpeakerphoneOn(Boolean.valueOf(false));
		}
		LogUtil.makeLog(tag, builder.toString());
	}

	private boolean needSetSpeakerphoneOnFalse(StringBuilder builder) {
		int i = 0;
		boolean result = needSetSpeakerphoneOnFalse;
		if (!this.isFirstChecking) {
			return result;
		}
		this.isFirstChecking = false;
		String[] strArr = needSetSpeakerphoneOnFalseDevices;
		int length = strArr.length;
		while (i < length) {
			if (Build.MODEL.contains(strArr[i])) {
				builder.append(" device " + Build.MODEL);
				builder.append(" needSetSpeakerphoneOnFalse is true");
				return true;
			}
			i++;
		}
		return result;
	}

	public static synchronized int getBestMode() {
		int mode;
		synchronized (AudioUtil.class) {
			if (VERSION.SDK_INT > 10) {
				mode = 3;
			} else {
				mode = 2;
			}
		}
		return mode;
	}

	public String getModeStr(int mode) {
		switch (mode) {
			case -2:
				return "AudioManager.MODE_INVALID";
			case -1:
				return "AudioManager.MODE_CURRENT";
			case 0:
				return "AudioManager.MODE_NORMAL";
			case 1:
				return "AudioManager.MODE_RINGTONE";
			case 2:
				return "AudioManager.MODE_IN_CALL";
			case 3:
				return "AudioManager.MODE_IN_COMMUNICATION";
			default:
				return "mode(" + mode + ") MODE_???????";
		}
	}
}
