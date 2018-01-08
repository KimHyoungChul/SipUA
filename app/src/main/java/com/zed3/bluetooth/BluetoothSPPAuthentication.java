package com.zed3.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.zed3.utils.Tea;

import java.util.Random;

public class BluetoothSPPAuthentication implements Runnable {
	private static final String TAG = "BluetoothSPPAuthentication";
	Random mRandom;
	private int radom;

	public BluetoothSPPAuthentication() {
		this.mRandom = new Random();
	}

	private int[] byteToInt(final byte[] array) {
		return new Tea().byteToInt(array, 0);
	}

	private byte[] intToByte(final int[] array) {
		return new Tea().intToByte(array, 0);
	}

	private void testTea() {
		final BluetoothSPPAuthentication bluetoothSPPAuthentication = new BluetoothSPPAuthentication();
		Log.i("BlueSPPAuthentication", "testTea() radom = 0x10203040");
		Log.i("BlueSPPAuthentication", "testTea() bluetoothAddress = " + "77:88:99:aa:bb:cc");
		bluetoothSPPAuthentication.getSecretKey("77:88:99:aa:bb:cc", 1, 270544960);
		Log.i("BlueSPPAuthentication", "testTea() text1 = " + new String(bluetoothSPPAuthentication.getSecretText("77:88:99:aa:bb:cc", 1, 270544960)));
	}

	public int[] getSecretKey(final String s, int i, final int n) {
		if (i != 1 && i != 2) {
			throw new RuntimeException("bad type error " + i + "type should be 1 or 2");
		}
		if (s.length() != 17) {
			throw new RuntimeException("bad bluetoothAddress error " + s);
		}
		final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		if (defaultAdapter == null) {
			throw new RuntimeException(" unsupport bluetooth error ");
		}
		defaultAdapter.getAddress();
		final byte[] array = new byte[16];
		switch (i) {
			case 1: {
				i = (char) this.mRandom.nextInt();
				final String[] split = "11:22:33:44:55:66".split(":");
				final byte[] array2 = new byte[6];
				for (i = 0; i < 6; ++i) {
					array2[i] = (byte) Integer.parseInt(split[i], 16);
				}
				final String[] split2 = s.split(":");
				final byte[] array3 = new byte[6];
				for (i = 0; i < 6; ++i) {
					array3[i] = (byte) Integer.parseInt(split2[i], 16);
				}
				for (i = 0; i < split2.length; ++i) {
					array[i] = (byte) (array2[i] ^ array3[i]);
				}
				for (i = 0; i < split2.length; ++i) {
					array[i + 6] = (byte) (array2[5 - i] ^ array3[i]);
				}
				for (i = 0; i < 4; ++i) {
					array[i + 12] = array3[i + 2];
				}
				break;
			}
			case 2: {
				final String[] split3 = "11:22:33:44:55:66".split(":");
				final byte[] array4 = new byte[6];
				for (i = 0; i < 6; ++i) {
					array4[i] = (byte) Integer.parseInt(split3[i], 16);
				}
				final String[] split4 = s.split(":");
				final byte[] array5 = new byte[6];
				for (i = 0; i < 6; ++i) {
					array5[i] = (byte) Integer.parseInt(split4[i], 16);
				}
				for (i = 0; i < split4.length; ++i) {
					array[i] = (byte) (array4[i] ^ array5[i]);
				}
				for (i = 0; i < split4.length; ++i) {
					array[i + 6] = (byte) (array4[5 - i] ^ array5[i]);
				}
				for (i = 0; i < 4; ++i) {
					array[i + 12] = (byte) (n >> 24 - i * 8);
				}
				break;
			}
		}
		return this.byteToInt(array);
	}

	public byte[] getSecretText(String string, int i, final int n) {
		if (i != 1 && i != 2) {
			throw new RuntimeException("bad type error " + i + "type should be 1 or 2");
		}
		final byte[] array = new byte[8];
		switch (i) {
			case 1: {
				String s = string;
				if (string.length() < 4) {
					for (i = 0; i < 4 - string.length(); string = String.valueOf(string) + "0", ++i) {
					}
					s = string;
				}
				final byte[] bytes = s.getBytes();
				for (i = 0; i < 4; ++i) {
					array[i * 2] = bytes[i];
					array[i * 2 + 1] = (byte) (n >> 24 - i * 8);
				}
				break;
			}
		}
		return array;
	}

	@Override
	public void run() {
		this.getSecretKey("zm04", 1, this.radom = 0);
		this.getSecretKey("zm04", 2, this.radom);
		this.getSecretText("zm04", 1, this.radom);
		this.getSecretText("zm04", 2, this.radom);
	}
}
