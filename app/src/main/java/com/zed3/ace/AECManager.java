package com.zed3.ace;

import android.media.audiofx.AcousticEchoCanceler;
import android.os.Build;

import com.zed3.sipua.ui.Receiver;

public class AECManager {
	private static AcousticEchoCanceler aecInstance;
	public static boolean recordReady = false;
	public static int recordSessionId = 0;
	private static final String tag = "AECManager";

	static {
		AECManager.recordSessionId = 0;
		AECManager.recordReady = false;
	}

	public static void createRecordAEC(final int i) {
		AECManager.recordSessionId = i;
		if (isDeviceSupportAec() && TestTools.isAECOPen(Receiver.mContext)) {
			if (AECManager.aecInstance != null) {
				releaseAEC();
			}
			while (i == 0) {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			while (AECManager.aecInstance == null) {
				AECManager.aecInstance = AcousticEchoCanceler.create(i);
				if (AECManager.aecInstance != null) {
					break;
				}
			}
			if (AECManager.aecInstance != null) {
				if (!AECManager.aecInstance.getEnabled()) {
					AECManager.aecInstance.setEnabled(true);
				}
				AECManager.recordReady = true;
			}
		}
	}

	public static void enable(final boolean enabled) {
		if (AECManager.aecInstance != null) {
			AECManager.aecInstance.setEnabled(enabled);
		}
	}

	private static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

	public static boolean isDeviceSupportAec() {
		return getApiLevel() >= 16 && AcousticEchoCanceler.isAvailable();
	}

	public static void releaseAEC() {
		if (AECManager.aecInstance != null) {
			AECManager.aecInstance.setEnabled(false);
			AECManager.aecInstance.release();
			AECManager.aecInstance = null;
			AECManager.recordReady = false;
		}
	}
}
