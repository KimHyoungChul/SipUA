package com.zed3.ace;

import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;

import com.zed3.sipua.ui.Receiver;

public class NSManager {
	private static NoiseSuppressor nsInstance;
	public static boolean recordReady = false;
	public static int recordSessionId = 0;
	private static final String tag = "NSManager";

	static {
		NSManager.recordSessionId = 0;
		NSManager.recordReady = false;
	}

	public static void createRecordNS(final int i) {
		NSManager.recordSessionId = i;
		if (isDeviceSupportNS() && TestTools.isAECOPen(Receiver.mContext)) {
			if (NSManager.nsInstance != null) {
				releaseNS();
			}
			while (i == 0) {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			while (NSManager.nsInstance == null) {
				NSManager.nsInstance = NoiseSuppressor.create(i);
				if (NSManager.nsInstance != null) {
					break;
				}
			}
			if (NSManager.nsInstance != null) {
				if (!NSManager.nsInstance.getEnabled()) {
					NSManager.nsInstance.setEnabled(true);
				}
				NSManager.recordReady = true;
			}
		}
	}

	public static void enable(final boolean enabled) {
		if (NSManager.nsInstance != null) {
			NSManager.nsInstance.setEnabled(enabled);
		}
	}

	private static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

	public static boolean isDeviceSupportNS() {
		return getApiLevel() >= 16 && AcousticEchoCanceler.isAvailable();
	}

	public static void releaseNS() {
		if (NSManager.nsInstance != null) {
			NSManager.nsInstance.setEnabled(false);
			NSManager.nsInstance.release();
			NSManager.nsInstance = null;
			NSManager.recordReady = false;
		}
	}
}
