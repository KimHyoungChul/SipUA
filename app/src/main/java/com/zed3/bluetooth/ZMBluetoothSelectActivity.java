package com.zed3.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zed3.dialog.DialogUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

import java.util.ArrayList;
import java.util.Set;

public class ZMBluetoothSelectActivity extends Activity {
	private static final int STATE_ENABLE_BLUETOOTH = 0;
	static ZMBluetoothSelectActivity mContext;
	protected Dialog alertDialog;
	private ArrayList<SiriListItem> list;
	ChatListAdapter mAdapter;
	private BluetoothAdapter mBtAdapter;
	private AdapterView.OnItemClickListener mDeviceClickListener;
	private ListView mListView;
	private final BroadcastReceiver mReceiver;
	private Button seachButton;
	private View.OnClickListener seachButtonClickListener;
	private Button serviceButton;

	public ZMBluetoothSelectActivity() {
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		this.seachButtonClickListener = (View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (!ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
					ZMBluetoothSelectActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE"), 3);
					return;
				}
				if (ZMBluetoothSelectActivity.this.mBtAdapter.isDiscovering()) {
					ZMBluetoothSelectActivity.this.mBtAdapter.cancelDiscovery();
					ZMBluetoothSelectActivity.this.seachButton.setText(R.string.re_search);
					return;
				}
				ZMBluetoothSelectActivity.this.list.clear();
				ZMBluetoothSelectActivity.this.mAdapter.notifyDataSetChanged();
				final Set<BluetoothDevice> bondedDevices = ZMBluetoothSelectActivity.this.mBtAdapter.getBondedDevices();
				if (bondedDevices.size() > 0) {
					for (final BluetoothDevice bluetoothDevice : bondedDevices) {
						if (ZMBluetoothSelectActivity.this.checkIsZM(bluetoothDevice.getName())) {
							ZMBluetoothSelectActivity.this.list.add(new SiriListItem(String.valueOf(bluetoothDevice.getName()) + "(" + ZMBluetoothSelectActivity.this.getStateStr(bluetoothDevice) + ")" + "\n" + bluetoothDevice.getAddress(), true));
							ZMBluetoothSelectActivity.this.mAdapter.notifyDataSetChanged();
							ZMBluetoothSelectActivity.this.mListView.setSelection(ZMBluetoothSelectActivity.this.list.size() - 1);
						}
					}
				} else {
					ZMBluetoothSelectActivity.this.list.add(new SiriListItem("No devices have been paired", true));
					ZMBluetoothSelectActivity.this.mAdapter.notifyDataSetChanged();
					ZMBluetoothSelectActivity.this.mListView.setSelection(ZMBluetoothSelectActivity.this.list.size() - 1);
				}
				ZMBluetoothSelectActivity.this.mBtAdapter.startDiscovery();
				ZMBluetoothSelectActivity.this.seachButton.setText(R.string.stop_search);
			}
		};
		this.mDeviceClickListener = (AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
				SiriListItem item = (SiriListItem) ZMBluetoothSelectActivity.this.list.get(arg2);
				String info = item.message;
				final String address = info.substring(info.length() - 17);
				Builder StopDialog = new Builder(ZMBluetoothSelectActivity.mContext);
				StopDialog.setTitle(R.string.connect);
				StopDialog.setMessage(item.message);
				StopDialog.setPositiveButton(ZMBluetoothSelectActivity.this.getResources().getString(R.string.connect), new DialogInterface.OnClickListener() {
					private BluetoothDevice device;

					public void onClick(DialogInterface dialog, int which) {
						ZMBluetoothSelectActivity.this.mBtAdapter.cancelDiscovery();
						ZMBluetoothSelectActivity.this.seachButton.setText(R.string.re_search);
						this.device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
						if (this.device != null) {
							ZMBluetoothManager.getInstance().connectSPP(this.device);
						} else {
							Toast.makeText(ZMBluetoothSelectActivity.this.getApplicationContext(), "device is null error", Toast.LENGTH_SHORT).show();
						}
					}
				});
				StopDialog.setNegativeButton(ZMBluetoothSelectActivity.this.getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialogInterface, final int n) {
					}
				});
				StopDialog.show();
			}
		};
		this.mReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final String action = intent.getAction();
				if ("android.bluetooth.device.action.FOUND".equals(action)) {
					final BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
					if (bluetoothDevice.getBondState() != 12 && ZMBluetoothSelectActivity.this.checkIsZM(bluetoothDevice.getName())) {
						ZMBluetoothSelectActivity.this.list.add(new SiriListItem(String.valueOf(bluetoothDevice.getName()) + "(" + ZMBluetoothSelectActivity.this.getStateStr(bluetoothDevice) + ")" + "\n" + bluetoothDevice.getAddress(), false));
						ZMBluetoothSelectActivity.this.mAdapter.notifyDataSetChanged();
						ZMBluetoothSelectActivity.this.mListView.setSelection(ZMBluetoothSelectActivity.this.list.size() - 1);
					}
				} else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
					ZMBluetoothSelectActivity.this.setProgressBarIndeterminateVisibility(false);
					if (ZMBluetoothSelectActivity.this.mListView.getCount() == 0) {
						ZMBluetoothSelectActivity.this.list.add(new SiriListItem(ZMBluetoothSelectActivity.this.getResources().getString(R.string.no_bl_notify), false));
						ZMBluetoothSelectActivity.this.mAdapter.notifyDataSetChanged();
						ZMBluetoothSelectActivity.this.mListView.setSelection(ZMBluetoothSelectActivity.this.list.size() - 1);
					}
					ZMBluetoothSelectActivity.this.seachButton.setText(R.string.re_search);
					ZMBluetoothSelectActivity.this.connectionCurrentDevice();
				}
			}
		};
	}

	public static void askUserToSelectBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) ZMBluetoothSelectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		SipUAApp.mContext.startActivity(intent);
	}

	private boolean checkIsZM(final String s) {
		return s != null && s.startsWith("ZM") && s.length() == 8;
	}

	private void dismissDialog(final AlertDialog alertDialog) {
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
		}
	}

	private void dismissDialogs() {
		this.dismissDialog((AlertDialog) this.alertDialog);
	}

	private String getConnectStateStr(final BluetoothDevice bluetoothDevice) {
		switch (bluetoothDevice.getBondState()) {
			default: {
				return " unknow";
			}
			case 10: {
				return " is not bonded";
			}
			case 11: {
				return " is bonding";
			}
			case 12: {
				return " is bonded ";
			}
		}
	}

	public static Activity getInstance() {
		return ZMBluetoothSelectActivity.mContext;
	}

	private String getStateStr(final BluetoothDevice bluetoothDevice) {
		switch (bluetoothDevice.getBondState()) {
			default: {
				return " unknow";
			}
			case 10: {
				return SipUAApp.mContext.getResources().getString(R.string.bl_hm);
			}
			case 11: {
				return SipUAApp.mContext.getResources().getString(R.string.bl_hm);
			}
			case 12: {
				return SipUAApp.mContext.getResources().getString(R.string.bl_hm);
			}
		}
	}

	private void init() {
		this.list = new ArrayList<SiriListItem>();
		this.mAdapter = new ChatListAdapter((Context) this, this.list);
		(this.mListView = (ListView) this.findViewById(R.id.list)).setAdapter((ListAdapter) this.mAdapter);
		this.mListView.setFastScrollEnabled(true);
		this.mListView.setOnItemClickListener(this.mDeviceClickListener);
		this.registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
		this.registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
		(this.seachButton = (Button) this.findViewById(R.id.start_seach)).setOnClickListener(this.seachButtonClickListener);
		(this.serviceButton = (Button) this.findViewById(R.id.start_service)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				ZMBluetoothSelectActivity.this.startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
				ZMBluetoothSelectActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE"), 3);
			}
		});
	}

	private void showDialog(Builder stopDialog, boolean cancelable) {
		this.alertDialog = stopDialog.show();
		this.alertDialog.setCancelable(cancelable);
	}

	protected void connectionCurrentDevice() {
		if (this.list.size() > 1) {
			DialogUtil.showCheckDialog((Context) this, this.getResources().getString(R.string.information), String.valueOf(this.getResources().getString(R.string.information_1)) + this.list.size() + this.getResources().getString(R.string.information_2), this.getResources().getString(R.string.ok_know));
		} else if (this.list.size() != 0) {
			final String message = this.list.get(0).message;
			if (!this.checkIsZM(message.substring(0, 8))) {
				DialogUtil.showCheckDialog((Context) this, this.getResources().getString(R.string.information), this.getResources().getString(R.string.no_paried), this.getResources().getString(R.string.ok_know));
				return;
			}
			message.substring(message.length() - 17);
		}
	}

	protected void onActivityResult(final int n, final int n2, final Intent intent) {
		super.onActivityResult(n, n2, intent);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);
		mContext = this;
		init();
		this.list.clear();
		this.mAdapter.notifyDataSetChanged();
		Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (checkIsZM(device.getName())) {
					this.list.add(new SiriListItem(device.getName() + "(" + getStateStr(device) + ")" + "\n" + device.getAddress(), true));
					this.mAdapter.notifyDataSetChanged();
					this.mListView.setSelection(this.list.size() - 1);
				}
			}
		} else {
			this.list.add(new SiriListItem("No devices have been paired", true));
			this.mAdapter.notifyDataSetChanged();
			this.mListView.setSelection(this.list.size() - 1);
		}
		this.mBtAdapter.startDiscovery();
		this.seachButton.setText(R.string.stop_search);
	}

	protected void onDestroy() {
		super.onDestroy();
		if (this.mBtAdapter != null) {
			this.mBtAdapter.cancelDiscovery();
		}
		this.unregisterReceiver(this.mReceiver);
	}

	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		switch (n) {
			default: {
				return super.onKeyDown(n, keyEvent);
			}
			case 4: {
				return super.onKeyDown(n, keyEvent);
			}
		}
	}

	protected void onResume() {
		Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
		this.list.clear();
		this.mAdapter.notifyDataSetChanged();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (checkIsZM(device.getName())) {
					this.list.add(new SiriListItem(device.getName() + "(" + getStateStr(device) + ")" + "\n" + device.getAddress(), true));
					this.mAdapter.notifyDataSetChanged();
					this.mListView.setSelection(this.list.size() - 1);
				}
			}
		} else {
			this.list.add(new SiriListItem(getResources().getString(R.string.no_eq_notify), true));
			this.mAdapter.notifyDataSetChanged();
			this.mListView.setSelection(this.list.size() - 1);
		}
		super.onResume();
	}

	public void onStart() {
		super.onStart();
		if (!this.mBtAdapter.isEnabled()) {
			DialogUtil.showSelectDialog((Context) this, this.getResources().getString(R.string.information), this.getResources().getString(R.string.bl_off_notify_message), this.getResources().getString(R.string.settings), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
				@Override
				public void onNegativeButtonClick() {
				}

				@Override
				public void onPositiveButtonClick() {
					ZMBluetoothSelectActivity.this.startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 4);
					ZMBluetoothSelectActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 3);
				}
			});
		}
		this.dismissDialogs();
	}

	public class SiriListItem {
		boolean isSiri;
		String message;

		public SiriListItem(final String message, final boolean isSiri) {
			this.message = message;
			this.isSiri = isSiri;
		}
	}
}
