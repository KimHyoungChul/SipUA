package com.zed3.audio;

import android.app.Activity;

public interface AudioUitlInterface {
	boolean checkMode(final int p0);

	int getCurrentMode();

	int getCustomMode(final int p0);

	void setAudioConnectMode(final int p0);

	boolean setCustomMode(final int p0, final int p1);

	void setVolumeControlStream(final Activity p0);

	void startBluetoothSCO();

	void stopBluetoothSCO();
}
