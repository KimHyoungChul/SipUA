package com.zed3.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothConnectStateChangedListener {
	void onDeviceConnected(final BluetoothDevice p0);

	void onDeviceConnecting(final BluetoothDevice p0);

	void onDeviceDisConnected(final BluetoothDevice p0);

	void onDeviceDisConnecting(final BluetoothDevice p0);
}
