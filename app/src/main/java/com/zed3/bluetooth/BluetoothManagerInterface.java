package com.zed3.bluetooth;

import android.bluetooth.BluetoothHeadset;
import android.content.Context;

public interface BluetoothManagerInterface {
	void registerReceivers(final Context p0);

	void removeSppConnectStateListener(final OnSppConnectStateChangedListener p0);

	boolean setHeadSetConnectStateListener(final HeadSetConnectStateListener p0);

	void setSppConnectStateListener(final OnSppConnectStateChangedListener p0);

	void startReConnectingSPP(final String p0, final long p1, final int p2);

	boolean stopReConnectingSPP();

	void unregisterReceivers(final Context p0);

	public interface HeadSetConnectStateListener {
		void onHeadSetServiceConnected(final BluetoothHeadset p0);

		void onHeadSetServiceDisConnected(final BluetoothHeadset p0);
	}
}
