package com.zed3.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.List;

public interface HeadsetBluetoothUtilInterface {
	BluetoothDevice getCurrentHeadsetBluetooth(final Context p0);

	List<BluetoothDevice> getHeadsetBluetooths(final Context p0);
}
