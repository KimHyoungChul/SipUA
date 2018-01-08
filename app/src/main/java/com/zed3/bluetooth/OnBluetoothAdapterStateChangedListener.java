package com.zed3.bluetooth;

public interface OnBluetoothAdapterStateChangedListener {
	void onStateOff();

	void onStateOn();

	void onStateTurnningOff();

	void onStateTurnningOn();
}
