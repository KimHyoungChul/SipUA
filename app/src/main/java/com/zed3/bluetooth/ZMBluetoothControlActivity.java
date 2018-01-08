package com.zed3.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

public class ZMBluetoothControlActivity extends Activity implements View.OnClickListener, OnSppConnectStateChangedListener, OnBluetoothAdapterStateChangedListener, OnBluetoothConnectStateChangedListener {
	private static final String CONTROL_DEVICE_NAME = "control_device_name";
	private static final String CONTROL_STATE = "control_state";
	private static final int STATE_CHECK_ZM_BLUETOOTH = 4;
	private static final int STATE_CONNECT_AUDIO = 1;
	private static final int STATE_CONNECT_SPP = 3;
	private static final int STATE_CONNECT_ZM_AUDIO = 2;
	private static final int STATE_DISABLE_BLUETOOTH = 6;
	private static final int STATE_ENABLE_BLUETOOTH = 0;
	private static final int STATE_RECONNECT_ZM_BLUETOOTH = 5;
	private static final int STATE_SELECT_HEADSET_BLUETOOTH = 7;
	private static Activity mContext;
	public static int mState = 0;
	private static final String tag = "ZMBlueControlActivity";
	private TextView mCancelTV;
	private TextView mCommitTV;
	private String mDeviceName;
	private TextView mMsgTV;
	private TextView mTitleTV;

	static {
		ZMBluetoothControlActivity.mState = -1;
	}

	public ZMBluetoothControlActivity() {
		this.mDeviceName = "";
	}

	public static void askUserToCheckZMBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 4);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToConnectBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 1);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToConnectZMBluetooth(final BluetoothDevice bluetoothDevice) {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 2);
		intent.putExtra("control_device_name", bluetoothDevice.getName());
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToDisableBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 6);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToEnableBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 0);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToReConnectZMBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 5);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToSelectHeadSetBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothControlActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 7);
		SipUAApp.mContext.startActivity(intent);
	}

	public static Activity getInstance() {
		return ZMBluetoothControlActivity.mContext;
	}

	private void initTextViews(final String text, final String text2, final String text3, final String text4) {
		this.mTitleTV.setText((CharSequence) text);
		this.mMsgTV.setText((CharSequence) text2);
		this.mCancelTV.setText((CharSequence) text3);
		this.mCommitTV.setText((CharSequence) text4);
	}

	protected void onActivityResult(final int n, final int n2, final Intent intent) {
		switch (n) {
			default: {
				Log.e("ZMBlueControlActivity", "unknow state error");
				this.finish();
				break;
			}
			case 0: {
				this.finish();
				if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
					Log.i("ZMBlueControlActivity", "\u7cfb\u7edf\u84dd\u7259\u672a\u6253\u5f00");
					askUserToEnableBluetooth();
					break;
				}
				Log.i("ZMBlueControlActivity", "\u7cfb\u7edf\u84dd\u7259\u5df2\u6253\u5f00");
				ZMBluetoothManager.getInstance().connectZMBluetooth(this.getApplicationContext());
				break;
			}
			case 1: {
				this.finish();
				ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				break;
			}
			case 2: {
				this.finish();
				ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				break;
			}
			case 3: {
				ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				this.finish();
				break;
			}
			case 4: {
				ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
				this.finish();
				break;
			}
			case 6: {
				if (ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
					askUserToDisableBluetooth();
					break;
				}
				this.finish();
				break;
			}
			case 7: {
				this.finish();
				break;
			}
		}
		super.onActivityResult(n, n2, intent);
	}

	public void onClick(final View view) {
		// TODO
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		ZMBluetoothControlActivity.mContext = this;
		ZMBluetoothManager.getInstance().setSppConnectStateListener(this);
		BluetoothSCOStateReceiver.setOnBluetoothAdapterStateChangedListener(this);
		BluetoothSCOStateReceiver.setOnBluetoothConnectStateChangedListener(this);
		this.setContentView(R.layout.zmbluetooth_control_dialog);
		this.mTitleTV = (TextView) this.findViewById(R.id.title_tv);
		this.mMsgTV = (TextView) this.findViewById(R.id.msg_tv);
		this.mCancelTV = (TextView) this.findViewById(R.id.cancel_tv);
		this.mCommitTV = (TextView) this.findViewById(R.id.ok_tv);
		this.mCancelTV.setOnClickListener((View.OnClickListener) this);
		this.mCommitTV.setOnClickListener((View.OnClickListener) this);
		ZMBluetoothManager.getInstance().isDeviceSupportBluetooth();
		ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled();
		final Intent intent = this.getIntent();
		ZMBluetoothControlActivity.mState = intent.getIntExtra("control_state", -1);
		this.mDeviceName = intent.getStringExtra("control_device_name");
//        switch (ZMBluetoothControlActivity.mState) {
//            default: {}
//            case 0: {
//                this.initTextViews(this.getResources().getString(R.string.bl_status_1), this.getResources().getString(R.string.bl_off_notfiy), this.getResources().getString(R.string.dis_hm), this.getResources().getString(R.string.open_bl));
//            }
//            case 1: {
//                this.initTextViews(this.getResources().getString(R.string.dis_bl_hm), this.getResources().getString(R.string.bl_notify_2), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.bl_notify_ok));
//            }
//            case 2: {
//                if (this.mDeviceName != null && !this.mDeviceName.equals("")) {
//                    this.initTextViews(this.getResources().getString(R.string.dis_bl_hm), String.valueOf(this.getResources().getString(R.string.blueTooth_1)) + this.mDeviceName + this.getResources().getString(R.string.blueTooth_2), this.getResources().getString(R.string.keep), this.getResources().getString(R.string.settings));
//                    return;
//                }
//                this.initTextViews(this.getResources().getString(R.string.dis_bl_hm), this.getResources().getString(R.string.blueTooth_3), this.getResources().getString(R.string.keep), this.getResources().getString(R.string.settings));
//            }
//            case 4: {
//                this.initTextViews(this.getResources().getString(R.string.bl_port_notify), this.getResources().getString(R.string.hm_notify), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.try_again));
//            }
//            case 5: {
//                this.initTextViews(this.getResources().getString(R.string.dising_hm), this.getResources().getString(R.string.dis_notify), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.try_again));
//            }
//            case 6: {
//                this.initTextViews(this.getResources().getString(R.string.turn_off_bl), "", this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.ok));
//            }
//            case 7: {
//                this.initTextViews(this.getResources().getString(R.string.turn_on_bl), this.getResources().getString(R.string.bl_off_notify), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.ok));
//            }
//        }
	}

	protected void onDestroy() {
		ZMBluetoothManager.getInstance().removeSppConnectStateListener(this);
		BluetoothSCOStateReceiver.reMoveOnBluetoothAdapterStateChangedListener(this);
		super.onDestroy();
	}

	public void onDeviceConnectFailed(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	public void onDeviceConnected(final BluetoothDevice bluetoothDevice) {
	}

	public void onDeviceConnected(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		Toast.makeText(this.getApplicationContext(), (CharSequence) (String.valueOf(this.getResources().getString(R.string.bl_hm_connected)) + bluetoothIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
		this.finish();
	}

	public void onDeviceConnecting(final BluetoothDevice bluetoothDevice) {
	}

	public void onDeviceConnectting(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	public void onDeviceDisConnected(final BluetoothDevice bluetoothDevice) {
		this.finish();
	}

	public void onDeviceDisConnecting(final BluetoothDevice bluetoothDevice) {
	}

	public void onDeviceDisconnected(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	public void onDeviceDisconnectting(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	public void onDeviceFound(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
		if (ZMBluetoothControlActivity.mState == -1) {
			Log.e("ZMBlueControlActivity", "unknow state error");
			this.finish();
		}
	}

	public void onStateOff() {
	}

	public void onStateOn() {
		if (ZMBluetoothControlActivity.mState == 0) {
			this.finish();
		}
	}

	public void onStateTurnningOff() {
	}

	public void onStateTurnningOn() {
	}
}
