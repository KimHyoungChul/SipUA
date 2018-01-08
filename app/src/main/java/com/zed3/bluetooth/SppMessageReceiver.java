package com.zed3.bluetooth;

import android.util.Log;

public class SppMessageReceiver extends Thread {
	private boolean isRunning;
	private SppMessageStorage mStorage;
	private String tag;

	public SppMessageReceiver(final SppMessageStorage mStorage) {
		this.isRunning = true;
		this.tag = "SppMessageReceiver";
		this.mStorage = mStorage;
	}

	@Override
	public void run() {
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver   start receiving");
		Log.i(this.tag, "SppMessageReceiver   start receiving");
		while (this.isRunning) {
			final SppMessageStorage.SppMessage value = this.mStorage.get();
			if (value != null) {
				final String message = value.getMessage();
				if (!value.isAvailable()) {
					Log.i(this.tag, "message.isAvailable() is false   continue");
				} else {
					ZMBluetoothManager.getInstance().receive(message);
				}
			} else {
				ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver  mStorage.get() return null");
				Log.i(this.tag, "SppMessageReceiver  mStorage.get() return null");
			}
		}
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageReceiver   stop receiving");
		Log.i(this.tag, "SppMessageReceiver   stop receiving");
	}

	public void startReceiving() {
		this.isRunning = true;
	}

	public void stopReceiving() {
		this.isRunning = false;
	}
}
