package com.zed3.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.zed3.audio.AudioUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

import java.util.Iterator;
import java.util.List;

public class HeadsetBluetoothUtil implements HeadsetBluetoothUtilInterface {
	protected static BluetoothHeadset mBluetoothHeadset;
	private static boolean mGetProfileProxy;
	private static HeadsetBluetoothUtil mInstance;
	protected static String tag;
	public BluetoothManagerInterface.HeadSetConnectStateListener mHeadSetConnectStateListener;
	private BluetoothProfile.ServiceListener mProfileListener;
	protected BluetoothDevice mSCOConnectDevice;

	static {
		HeadsetBluetoothUtil.tag = "HeadsetBluetoothUtil";
		HeadsetBluetoothUtil.mInstance = new HeadsetBluetoothUtil();
	}

	private HeadsetBluetoothUtil() {
		this.mProfileListener = (BluetoothProfile.ServiceListener) new BluetoothProfile.ServiceListener() {
			public void onServiceConnected(final int n, final BluetoothProfile bluetoothProfile) {
				switch (n) {
					default: {
						final String string = String.valueOf("") + "BluetoothProfile.???";
						Log.i(HeadsetBluetoothUtil.tag, string);
						Toast.makeText(SipUAApp.mContext, (CharSequence) string, Toast.LENGTH_SHORT).show();
						break;
					}
					case 2: {
						Log.i(HeadsetBluetoothUtil.tag, "BluetoothProfile.A2DP onServiceConnected()");
						Toast.makeText(SipUAApp.mContext, (CharSequence) "BluetoothProfile.A2DP connected", Toast.LENGTH_SHORT).show();
						break;
					}
					case 1: {
						Log.i(HeadsetBluetoothUtil.tag, "BluetoothProfile.HEADSET onServiceConnected()");
						HeadsetBluetoothUtil.mBluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
						if (HeadsetBluetoothUtil.this.mHeadSetConnectStateListener != null) {
							HeadsetBluetoothUtil.this.mHeadSetConnectStateListener.onHeadSetServiceConnected(HeadsetBluetoothUtil.mBluetoothHeadset);
							HeadsetBluetoothUtil.this.mHeadSetConnectStateListener = null;
						}
						final List<BluetoothDevice> connectedDevices = HeadsetBluetoothUtil.mBluetoothHeadset.getConnectedDevices();
						if (connectedDevices.size() == 0) {
							final String string2 = SipUAApp.mContext.getResources().getString(R.string.blv_notify);
							Log.i(HeadsetBluetoothUtil.tag, string2);
							Toast.makeText(SipUAApp.mContext, (CharSequence) string2, Toast.LENGTH_SHORT).show();
							ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 0 " + string2);
							ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
							break;
						}
						if (connectedDevices.size() == 1) {
							final BluetoothDevice mscoConnectDevice = connectedDevices.get(0);
							final String string3 = "BluetoothProfile.HEADSET connected device:" + mscoConnectDevice.getName();
							Log.i(HeadsetBluetoothUtil.tag, string3);
							ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 " + string3);
							final String name = mscoConnectDevice.getName();
							if (ZMBluetoothManager.getInstance().checkIsZM(mscoConnectDevice.getName())) {
								ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 " + string3);
								ZMBluetoothManager.getInstance().connectSPP(mscoConnectDevice);
							} else {
								Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)) + name), Toast.LENGTH_SHORT).show();
								ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() == 1 \u5f53\u524d\u8bbe\u5907\u4e3a\u975e\u84dd\u7259\u624b\u54aa\uff01   " + name);
								ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(mscoConnectDevice);
							}
							HeadsetBluetoothUtil.this.mSCOConnectDevice = mscoConnectDevice;
							break;
						}
						String string4 = "BluetoothProfile.HEADSET connected device:";
						final Iterator<BluetoothDevice> iterator = connectedDevices.iterator();
						while (iterator.hasNext()) {
							string4 = String.valueOf(string4) + iterator.next().getName() + ",";
						}
						Log.i(HeadsetBluetoothUtil.tag, string4);
						final BluetoothDevice mscoConnectDevice2 = connectedDevices.get(0);
						final String string5 = "BluetoothProfile.HEADSET connected device:" + mscoConnectDevice2.getName();
						Log.i(HeadsetBluetoothUtil.tag, string5);
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + "," + string5);
						final String name2 = mscoConnectDevice2.getName();
						if (ZMBluetoothManager.getInstance().checkIsZM(mscoConnectDevice2.getName())) {
							ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + "," + string5);
							ZMBluetoothManager.getInstance().connectSPP(mscoConnectDevice2);
						} else {
							Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.dev_nofity_1)) + name2), Toast.LENGTH_SHORT).show();
							ZMBluetoothManager.getInstance().writeLog2File("SPP connect , onServiceConnected() connectedDevices.size() = " + connectedDevices.size() + ", \u5f53\u524d\u8bbe\u5907\u4e3a\u975e\u84dd\u7259\u624b\u54aa\uff01   " + name2);
							ZMBluetoothManager.getInstance().askUserToConnectZMBluetooth(mscoConnectDevice2);
						}
						HeadsetBluetoothUtil.this.mSCOConnectDevice = mscoConnectDevice2;
						break;
					}
					case 3: {
						final String string6 = String.valueOf("") + "BluetoothProfile.HEALTH";
						Log.i(HeadsetBluetoothUtil.tag, string6);
						Toast.makeText(SipUAApp.mContext, (CharSequence) string6, Toast.LENGTH_SHORT).show();
						break;
					}
				}
				HeadsetBluetoothUtil.closeProfileProxy();
			}

			public void onServiceDisconnected(final int n) {
				String s = null;
				switch (n) {
					default: {
						s = String.valueOf("") + "BluetoothProfile.???";
						break;
					}
					case 2: {
						s = String.valueOf("") + "BluetoothProfile.A2DP";
						break;
					}
					case 1: {
						final String string = String.valueOf("") + "BluetoothProfile.HEADSET";
						HeadsetBluetoothUtil.mBluetoothHeadset = null;
						ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,mProfileListenerForSPP  BluetoothProfile.HEADSET onServiceConnected()  askUserToConnectBluetooth ");
						if (SipUAApp.isHeadsetConnected) {
							AudioUtil.getInstance().setAudioConnectMode(2);
						} else {
							AudioUtil.getInstance().setAudioConnectMode(3);
						}
						s = string;
						if (ZMBluetoothManager.getInstance().mNeedAskUserToReconnectSpp) {
							ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
							s = string;
							break;
						}
						break;
					}
					case 3: {
						s = String.valueOf("") + "BluetoothProfile.HEALTH";
						break;
					}
				}
				Log.i(HeadsetBluetoothUtil.tag, String.valueOf(s) + "disconnected\uff01\uff01");
				HeadsetBluetoothUtil.closeProfileProxy();
			}
		};
	}

	public static void closeProfileProxy() {
		if (HeadsetBluetoothUtil.mGetProfileProxy) {
			Log.i(HeadsetBluetoothUtil.tag, "closeProfileProxy() closeProfileProxy ...");
			ZMBluetoothManager.getInstance().mBluetoothAdapter.closeProfileProxy(1, (BluetoothProfile) HeadsetBluetoothUtil.mBluetoothHeadset);
			HeadsetBluetoothUtil.mBluetoothHeadset = null;
			HeadsetBluetoothUtil.mGetProfileProxy = false;
			return;
		}
		Log.i(HeadsetBluetoothUtil.tag, "closeProfileProxy() need not closeProfileProxy");
	}

	public static HeadsetBluetoothUtil getInstance() {
		return HeadsetBluetoothUtil.mInstance;
	}

	public boolean connectZMBluetooth(final Context context) {
		ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()");
		if (!ZMBluetoothManager.getInstance().isDeviceSupportBluetooth()) {
			Toast.makeText(context, (CharSequence) context.getResources().getString(R.string.dev_nofity_2), Toast.LENGTH_SHORT).show();
			ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()  " + context.getResources().getString(R.string.dev_nofity_2));
			Log.i(HeadsetBluetoothUtil.tag, "\u624b\u673a\u4e0d\u652f\u6301\u84dd\u7259");
			return false;
		}
		if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
			ZMBluetoothManager.getInstance().enableAdapter();
		}
		Log.i(HeadsetBluetoothUtil.tag, "connectZMBluetooth()  get HeadSet connected devices");
		final boolean profileProxy = ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, this.mProfileListener, 1);
		Log.i(HeadsetBluetoothUtil.tag, "connectZMBluetooth()  get HeadSet connected devices  success? " + profileProxy);
		if (!profileProxy) {
			Log.i(HeadsetBluetoothUtil.tag, "connectZMBluetooth()  askUserToConnectBluetooth");
			ZMBluetoothManager.getInstance().askUserToConnectBluetooth();
			ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth()  askUserToConnectBluetooth");
			return false;
		}
		ZMBluetoothManager.getInstance().writeLog2File("SPP connect ,connectZMBluetooth() wait for mBluetoothHeadset get service");
		return true;
	}

	public void disConnectZMBluetooth(final Context context) {
	}

	@Override
	public BluetoothDevice getCurrentHeadsetBluetooth(final Context context) {
		return null;
	}

	@Override
	public List<BluetoothDevice> getHeadsetBluetooths(final Context context) {
		ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, this.mProfileListener, 1);
		return null;
	}

	public void initHeadSet() {
		ZMBluetoothManager.getInstance().mBluetoothAdapter.getProfileProxy(SipUAApp.mContext, this.mProfileListener, 1);
	}
}
