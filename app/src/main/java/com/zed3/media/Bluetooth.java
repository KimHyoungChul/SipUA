package com.zed3.media;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;

import com.zed3.sipua.SipUAApp;

import org.ksoap2.transport.ServiceConnection;

public class Bluetooth {
	static AudioManager am;
	static BluetoothAdapter ba;

	public static void init() {
		if (ba == null) {
			ba = BluetoothAdapter.getDefaultAdapter();
			am = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
		}
	}

	public static void enable(boolean mode) {
		if (mode) {
			am.startBluetoothSco();
		} else {
			am.stopBluetoothSco();
		}
	}

	public static boolean isAvailable() {
		if (ba == null || !ba.isEnabled()) {
			return false;
		}
		for (BluetoothDevice dev : ba.getBondedDevices()) {
			BluetoothClass cl = dev.getBluetoothClass();
			if (cl != null && (cl.hasService(ServiceConnection.DEFAULT_BUFFER_SIZE) || cl.getDeviceClass() == 1032 || cl.getDeviceClass() == 1056 || cl.getDeviceClass() == 1028)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSupported() {
		init();
		return am.isBluetoothScoAvailableOffCall();
	}
}
