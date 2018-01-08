package com.zed3.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BluetoothSCOStateReceiver extends BroadcastReceiver {
	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	static List<OnBluetoothAdapterStateChangedListener> bluetoothAdapterListeners;
	static List<OnBluetoothConnectStateChangedListener> bluetoothConnectListeners;
	private static IntentFilter intentFilter;
	public static boolean isBluetoothAdapterEnabled;
	private static boolean isStarted;
	private static BluetoothSCOStateReceiver mReceiver;
	private boolean flag;
	protected BluetoothHeadset mBluetoothHeadset;
	private ZMBluetoothManager mInstance;
	protected BluetoothDevice mSppConnectDevice;
	private String tag;

	static {
		BluetoothSCOStateReceiver.bluetoothAdapterListeners = new ArrayList<OnBluetoothAdapterStateChangedListener>();
		BluetoothSCOStateReceiver.bluetoothConnectListeners = new ArrayList<OnBluetoothConnectStateChangedListener>();
		BluetoothSCOStateReceiver.mReceiver = new BluetoothSCOStateReceiver();
		(BluetoothSCOStateReceiver.intentFilter = new IntentFilter()).addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
		BluetoothSCOStateReceiver.intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
		BluetoothSCOStateReceiver.intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			BluetoothSCOStateReceiver.isBluetoothAdapterEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
		}
	}

	public BluetoothSCOStateReceiver() {
		this.tag = "BluetoothSCOStateReceiver";
		this.mInstance = ZMBluetoothManager.getInstance();
	}

	private String getConnectionStateString(final int n) {
		switch (n) {
			default: {
				return "";
			}
			case 1: {
				return "BluetoothAdapter.STATE_CONNECTING";
			}
			case 2: {
				return "BluetoothAdapter.STATE_CONNECTED";
			}
			case 3: {
				return "BluetoothAdapter.STATE_DISCONNECTING";
			}
			case 0: {
				return "BluetoothAdapter.STATE_DISCONNECTED";
			}
		}
	}

	private void getHeadsetDevice() {
		if (this.flag) {
			return;
		}
		ZMBluetoothManager.getInstance().setHeadSetConnectStateListener(new BluetoothManagerInterface.HeadSetConnectStateListener() {
			@Override
			public void onHeadSetServiceConnected(final BluetoothHeadset mBluetoothHeadset) {
				BluetoothSCOStateReceiver.this.mBluetoothHeadset = mBluetoothHeadset;
				List<BluetoothDevice> connectedDevices = BluetoothSCOStateReceiver.this.mBluetoothHeadset.getConnectedDevices();
				if (connectedDevices.size() == 0) {
					final String string = SipUAApp.mContext.getResources().getString(R.string.blv_notify);
					Log.i(BluetoothSCOStateReceiver.this.tag, string);
					Toast.makeText(SipUAApp.mContext, (CharSequence) string, Toast.LENGTH_SHORT).show();
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 0 " + string);
					ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
					return;
				}
				if (connectedDevices.size() == 1) {
					final BluetoothDevice bluetoothDevice = connectedDevices.get(0);
					final String string2 = "BluetoothProfile.HEADSET connected device:" + bluetoothDevice.getName();
					Log.i(BluetoothSCOStateReceiver.this.tag, string2);
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 " + string2);
					final String name = bluetoothDevice.getName();
					if (ZMBluetoothManager.getInstance().checkIsZM(bluetoothDevice.getName())) {
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 " + string2);
						ZMBluetoothManager.getInstance().connectSPP(bluetoothDevice);
						return;
					}
					Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)) + name), Toast.LENGTH_SHORT).show();
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 \u5f53\u524d\u8bbe\u5907\u4e3a\u975e\u84dd\u7259\u624b\u54aa\uff01   " + name);
					ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(bluetoothDevice);
				} else {
					String string3 = "BluetoothProfile.HEADSET connected device:";
					final Iterator<BluetoothDevice> iterator = connectedDevices.iterator();
					while (iterator.hasNext()) {
						string3 = String.valueOf(string3) + iterator.next().getName() + ",";
					}
					Log.i(BluetoothSCOStateReceiver.this.tag, string3);
					final BluetoothDevice mSppConnectDevice = connectedDevices.get(0);
					BluetoothSCOStateReceiver.this.mSppConnectDevice = mSppConnectDevice;
					final String string4 = "BluetoothProfile.HEADSET connected device:" + mSppConnectDevice.getName();
					Log.i(BluetoothSCOStateReceiver.this.tag, string4);
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + "," + string4);
					final String name2 = mSppConnectDevice.getName();
					if (ZMBluetoothManager.getInstance().checkIsZM(mSppConnectDevice.getName())) {
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + "," + string4);
						ZMBluetoothManager.getInstance().connectSPP(mSppConnectDevice);
						return;
					}
					Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)) + name2), Toast.LENGTH_SHORT).show();
					ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + ", \u5f53\u524d\u8bbe\u5907\u4e3a\u975e\u84dd\u7259\u624b\u54aa\uff01   " + name2);
					ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(mSppConnectDevice);
				}
			}

			@Override
			public void onHeadSetServiceDisConnected(final BluetoothHeadset bluetoothHeadset) {
			}
		});
		this.flag = true;
	}

	private String getScoStateString(final int n) {
		switch (n) {
			default: {
				return "";
			}
			case 2: {
				return "AudioManager.SCO_AUDIO_STATE_CONNECTING";
			}
			case 1: {
				return "AudioManager.SCO_AUDIO_STATE_CONNECTED";
			}
			case 0: {
				return "AudioManager.SCO_AUDIO_STATE_DISCONNECTED";
			}
			case -1: {
				return "AudioManager.SCO_AUDIO_STATE_ERROR";
			}
		}
	}

	public static void reMoveOnBluetoothAdapterStateChangedListener(final OnBluetoothAdapterStateChangedListener onBluetoothAdapterStateChangedListener) {
		BluetoothSCOStateReceiver.bluetoothAdapterListeners.add(onBluetoothAdapterStateChangedListener);
	}

	public static void reMoveOnBluetoothConnectStateChangedListener(final OnBluetoothConnectStateChangedListener onBluetoothConnectStateChangedListener) {
		BluetoothSCOStateReceiver.bluetoothConnectListeners.add(onBluetoothConnectStateChangedListener);
	}

	private void receiveBluetoothAdapterState(final Context context, final Intent intent) {
		final int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
		intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
		switch (intExtra) {
			case 11: {
				MyLog.i(this.tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_TURNING_ON");
				break;
			}
			case 12: {
				MyLog.i(this.tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_ON");
				BluetoothSCOStateReceiver.isBluetoothAdapterEnabled = true;
				break;
			}
			case 13: {
				MyLog.i(this.tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_TURNING_OFF");
				ZMBluetoothManager.getInstance().disConnectZMBluetooth(SipUAApp.mContext);
				break;
			}
			case 10: {
				BluetoothSCOStateReceiver.isBluetoothAdapterEnabled = false;
				MyLog.i(this.tag, "receiveBluetoothAdapterState() BluetoothAdapter.STATE_OFF");
				break;
			}
		}
		for (final OnBluetoothAdapterStateChangedListener onBluetoothAdapterStateChangedListener : BluetoothSCOStateReceiver.bluetoothAdapterListeners) {
			switch (intExtra) {
				default: {
					continue;
				}
				case 10: {
					onBluetoothAdapterStateChangedListener.onStateOff();
					continue;
				}
				case 11: {
					onBluetoothAdapterStateChangedListener.onStateTurnningOn();
					continue;
				}
				case 12: {
					onBluetoothAdapterStateChangedListener.onStateOn();
					continue;
				}
				case 13: {
					onBluetoothAdapterStateChangedListener.onStateTurnningOff();
					continue;
				}
			}
		}
	}

	private void receiveConnectionStateAudioState(final Context context, final Intent intent) {
		final int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
		final int intExtra2 = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE", -1);
		final BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
		final String connectionStateString = this.getConnectionStateString(intExtra);
		final String connectionStateString2 = this.getConnectionStateString(intExtra2);
		String name = "unkown device";
		if (bluetoothDevice != null) {
			name = bluetoothDevice.getName();
		}
		MyLog.i(this.tag, String.valueOf(connectionStateString2) + "-->" + (String.valueOf(connectionStateString) + "  " + name));
		switch (intExtra) {
			case 1: {
				Toast.makeText(context, (CharSequence) (String.valueOf(context.getResources().getString(R.string.status_1)) + name), Toast.LENGTH_SHORT).show();
				MyLog.i(this.tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_CONNECTING  " + name);
				break;
			}
			case 2: {
				Toast.makeText(context, (CharSequence) (String.valueOf(context.getResources().getString(R.string.status_2)) + name), Toast.LENGTH_SHORT).show();
				MyLog.i(this.tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_CONNECTED  " + name);
				break;
			}
			case 3: {
				Toast.makeText(context, (CharSequence) (String.valueOf(context.getResources().getString(R.string.status_3)) + name), Toast.LENGTH_SHORT).show();
				MyLog.i(this.tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_DISCONNECTING  " + name);
				break;
			}
			case 0: {
				Toast.makeText(context, (CharSequence) (String.valueOf(context.getResources().getString(R.string.status_4)) + name), Toast.LENGTH_SHORT).show();
				MyLog.i(this.tag, "receiveConnectionStateAudioState() BluetoothAdapter.STATE_DISCONNECTED  " + name);
				break;
			}
		}
		for (final OnBluetoothConnectStateChangedListener onBluetoothConnectStateChangedListener : BluetoothSCOStateReceiver.bluetoothConnectListeners) {
			switch (intExtra) {
				default: {
					continue;
				}
				case 0: {
					onBluetoothConnectStateChangedListener.onDeviceDisConnected(bluetoothDevice);
					continue;
				}
				case 1: {
					onBluetoothConnectStateChangedListener.onDeviceConnecting(bluetoothDevice);
					continue;
				}
				case 2: {
					onBluetoothConnectStateChangedListener.onDeviceConnected(bluetoothDevice);
					continue;
				}
				case 3: {
					onBluetoothConnectStateChangedListener.onDeviceDisConnecting(bluetoothDevice);
					continue;
				}
			}
		}
	}

	private void receiveScoAudioState(final Context context, final Intent intent) {
		final int intExtra = intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1);
		intent.getIntExtra("android.media.extra.SCO_AUDIO_PREVIOUS_STATE", -1);
		MyLog.i(this.tag, "receiveScoAudioState() previousState = " + this.getScoStateString(intExtra));
		switch (intExtra) {
			default: {
			}
			case 2: {
				MyLog.i(this.tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_CONNECTING");
			}
			case 1: {
				MyLog.i(this.tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_CONNECTED");
			}
			case 0: {
				MyLog.i(this.tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_DISCONNECTED");
			}
			case -1: {
				MyLog.i(this.tag, "receiveScoAudioState() AudioManager.SCO_AUDIO_STATE_ERROR");
			}
		}
	}

	public static void setOnBluetoothAdapterStateChangedListener(final OnBluetoothAdapterStateChangedListener onBluetoothAdapterStateChangedListener) {
		BluetoothSCOStateReceiver.bluetoothAdapterListeners.add(onBluetoothAdapterStateChangedListener);
	}

	public static void setOnBluetoothConnectStateChangedListener(final OnBluetoothConnectStateChangedListener onBluetoothConnectStateChangedListener) {
		BluetoothSCOStateReceiver.bluetoothConnectListeners.add(onBluetoothConnectStateChangedListener);
	}

	private void sleep(final int n) {
		// monitorenter(this)
		final long n2 = n;
		try {
			Thread.sleep(n2);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
		}
		// monitorexit(this)
	}

	public static void startReceive(final Context context) {
		if (!BluetoothSCOStateReceiver.isStarted) {
			context.registerReceiver((BroadcastReceiver) BluetoothSCOStateReceiver.mReceiver, BluetoothSCOStateReceiver.intentFilter);
		}
	}

	public static void stopReceive(final Context context) {
		if (BluetoothSCOStateReceiver.isStarted) {
			context.unregisterReceiver((BroadcastReceiver) BluetoothSCOStateReceiver.mReceiver);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		if (Settings.mNeedBlueTooth) {
			final String action = intent.getAction();
			if (action.equals("android.media.ACTION_SCO_AUDIO_STATE_UPDATED")) {
				this.receiveScoAudioState(context, intent);
				return;
			}
			if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
				this.receiveBluetoothAdapterState(context, intent);
				return;
			}
			if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
				this.receiveConnectionStateAudioState(context, intent);
			}
		}
	}
}
