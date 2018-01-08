package com.zed3.bluetooth;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;

public interface OnSppConnectStateChangedListener {
	void onDeviceConnectFailed(final BluetoothIBridgeDevice p0);

	void onDeviceConnected(final BluetoothIBridgeDevice p0);

	void onDeviceConnectting(final BluetoothIBridgeDevice p0);

	void onDeviceDisconnected(final BluetoothIBridgeDevice p0);

	void onDeviceDisconnectting(final BluetoothIBridgeDevice p0);

	void onDeviceFound(final BluetoothIBridgeDevice p0);
}
