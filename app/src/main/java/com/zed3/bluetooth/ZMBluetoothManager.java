package com.zed3.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeAdapter;
import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.audio.AudioUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ZMBluetoothManager implements BluetoothManagerInterface, OnBluetoothConnectStateChangedListener {
	public static final String ACTION_BLUETOOTH_CONTROL = "com.zed3.sipua_bluetooth";
	public static final String ACTION_BLUETOOTH_RESPOND = "com.zed3.sipua_bluetooth_respond";
	public static final String CONTROL_ACTION = "control_action";
	public static final String CONTROL_ACTION_FUNCTION = "control_action_function";
	public static final String CONTROL_ACTION_PTT_DOWN = "control_action_ptt_down";
	public static final String CONTROL_ACTION_PTT_UP = "control_action_ptt_up";
	public static final String CONTROL_ACTION_VOL_LONG_DOWN = "control_action_vol_long_down";
	public static final String CONTROL_ACTION_VOL_LONG_UP = "control_action_vol_long_up";
	public static final String CONTROL_ACTION_VOL_SHORT_DOWN = "control_action_vol_short_down";
	public static final String CONTROL_ACTION_VOL_SHORT_UP = "control_action_vol_short_up";
	public static final String CONTROL_TYPE = "control_type";
	public static final String CONTROL_TYPE_CALL = "control_type_call";
	public static final String CONTROL_TYPE_FUNCTION = "control_type_function";
	public static final String CONTROL_TYPE_PTT = "control_type_ptt";
	public static final String CONTROL_TYPE_VOL = "control_type_vol";
	private static final String FUNCTION = "FUNCTION";
	private static final String PTT = "PTT";
	public static final String PTT_DOWN = "PTT_DOWN";
	public static final String PTT_PA_OFF = "PA_OFF";
	public static final String PTT_PA_ON = "PA_ON";
	public static final String PTT_START = "R_START";
	public static final String PTT_STOP = "R_STOP";
	public static final String PTT_SUCCESS = "PTT_SUCC";
	public static final String PTT_UP = "PTT_UP";
	public static final String PTT_WAITING = "PTT_WAIT";
	public static final String REQUEST_ADDRESS = "get addr";
	public static final String REQUEST_DEVICE_NAME = "request device name";
	public static final String RESPOND_ACTION = "respond_action";
	public static final String RESPOND_ACTION_FUNCTION_RECEIVED = "respond_action_function_received";
	public static final String RESPOND_ACTION_PTT_DOWN = "respond_action_ptt_down";
	public static final String RESPOND_ACTION_PTT_DOWN_RECEIVED = "respond_action_ptt_down_received";
	public static final String RESPOND_ACTION_PTT_UP = "respond_action_ptt_up";
	public static final String RESPOND_ACTION_PTT_UP_RECEIVED = "respond_action_ptt_up_received";
	public static final String RESPOND_ACTION_VOL_LONG_DOWN_RECEIVED = "respond_action_vol_long_down_received";
	public static final String RESPOND_ACTION_VOL_LONG_UP_RECEIVED = "respond_action_vol_long_up_received";
	public static final String RESPOND_ACTION_VOL_SHORT_DOWN_RECEIVED = "respond_action_vol_short_down_received";
	public static final String RESPOND_ACTION_VOL_SHORT_UP_RECEIVED = "respond_action_vol_short_up_received";
	public static final String RESPOND_ADDRESS_HEAD = "addr:";
	public static final String RESPOND_DEVICE_NAME_HEAD = "device name:";
	public static final String RESPOND_PTT_HEART = "HEART";
	public static final String RESPOND_PTT_PA_OFF = "PA_OFF_OK";
	public static final String RESPOND_PTT_PA_ON = "PA_ON_OK";
	public static final String RESPOND_PTT_START = "R_START_OK";
	public static final String RESPOND_PTT_STOP = "R_STOP_OK";
	public static final String RESPOND_PTT_SUCCESS = "PTT_SUCC_OK";
	public static final String RESPOND_PTT_WAITING = "PTT_WAIT_OK";
	public static final String RESPOND_TYPE = "respond_type";
	public static final String RESPOND_TYPE_CALL = "respond_type_call";
	public static final String RESPOND_TYPE_FHP_STATE = "respond_type_hfp_state";
	public static final String RESPOND_TYPE_FUNCTION = "respond_type_function";
	public static final String RESPOND_TYPE_HEART = "respond_type_heart";
	public static final String RESPOND_TYPE_PA_CONTROL = "respond_type_pa_control";
	public static final String RESPOND_TYPE_PTT = "respond_type_ptt";
	public static final String RESPOND_TYPE_VOL = "respond_type_vol";
	private static final String STATE_CODE_SCO_CONNECTED = "4";
	private static final String STATE_CODE_SCO_DISCONNECTED = "5";
	private static final String VOL = "VOL";
	public static final String VOL_LONG_DOWN = "VOL_LONG_DOWN";
	public static final String VOL_LONG_UP = "VOL_LONG_UP";
	public static final String VOL_SHORT_DOWN = "VOL_SHORT_DOWN";
	public static final String VOL_SHORT_UP = "VOL_SHORT_UP";
	private static SimpleDateFormat formatter;
	private static ZMBluetoothManager instance;
	private static String mLastSendMsg;
	public static boolean mNeedBroadCast;
	static SppMessageStorage sppMessageStorage4Receive;
	static SppMessageStorage sppMessageStorage4Send;
	private final byte[] block4GetDeviceModel;
	private final byte[] block4MakeLog;
	private String deviceInfoStr;
	private String deviceModelStr;
	private FileWriter fileWriter;
	boolean flag;
	private Queue<String> inMsgStorage;
	private long lastTime;
	private boolean mAntoConnectSCO;
	private AudioManager mAudioManager;
	BluetoothAdapter mBluetoothAdapter;
	protected BluetoothHeadset mBluetoothHeadset;
	private BluetoothAdapter mBtAdapter;
	public Context mContext;
	public BluetoothIBridgeDevice mCurrentIBridgeDevice;
	private MyDataReceiver mDataReceiver;
	private MyEventReceiver mEventReceiver;
	private FileWriter mFileWriter;
	private boolean mGetProfileProxy;
	private HeadSetConnectStateListener mHeadSetConnectStateListener;
	private BluetoothIBridgeAdapter mIBridgeAdapter;
	private BluetoothIBridgeDevice mIBridgeDevice;
	Runnable mInMsgProcessThread;
	public boolean mIsAllSPPDisConnected;
	private boolean mIsSPPConnected;
	BluetoothDevice mLastSPPConnectDevice;
	private long mLastSendTime;
	private File mLogFile;
	public boolean mNeedAskUserToReconnectSpp;
	public boolean mNeedCheckVersion;
	private boolean mNeedControlVol;
	protected boolean mNeedReConnectSPP;
	private OnSppConnectStateChangedListener mOnSppConnectStateChangedListener;
	Runnable mOutMsgProcessThread;
	private ProgressDialog mProgressDialog;
	private BluetoothDevice mSCOConnectDevice;
	private HashMap<String, BluetoothIBridgeDevice> mSPPConnectedDevices;
	private String mState;
	private Queue<String> outMsgStorage;
	private boolean profileProxy;
	private StringBuilder sb;
	public Thread sender;
	ArrayList<OnSppConnectStateChangedListener> sppConnectStateChangedListeners;
	public SppMessageReceiver sppMessageReceiver;
	public SppMessageSender sppMessageSender;
	private String tag;

	static {
		ZMBluetoothManager.sppMessageStorage4Send = new SppMessageStorage();
		ZMBluetoothManager.sppMessageStorage4Receive = new SppMessageStorage();
		ZMBluetoothManager.instance = new ZMBluetoothManager();
		ZMBluetoothManager.mNeedBroadCast = true;
		ZMBluetoothManager.mLastSendMsg = "";
	}

	private ZMBluetoothManager() {
		this.sppConnectStateChangedListeners = new ArrayList<OnSppConnectStateChangedListener>();
		this.mNeedControlVol = false;
		this.mEventReceiver = new MyEventReceiver();
		this.mDataReceiver = new MyDataReceiver();
		this.mSPPConnectedDevices = new HashMap<String, BluetoothIBridgeDevice>();
		this.tag = "ZMBluetoothManager";
		this.mNeedCheckVersion = true;
		this.flag = true;
		this.mContext = SipUAApp.mContext;
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		this.mAudioManager = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.inMsgStorage = new LinkedList<String>();
		this.outMsgStorage = new LinkedList<String>();
		this.mOutMsgProcessThread = new Runnable() {
			String msg;

			@Override
			public void run() {
				while (!ZMBluetoothManager.this.mIsAllSPPDisConnected) {
					synchronized (ZMBluetoothManager.this.outMsgStorage) {
						if (ZMBluetoothManager.this.outMsgStorage.size() <= 0) {
							continue;
						}
						this.msg = ZMBluetoothManager.this.outMsgStorage.poll();
						if (this.msg == null) {
							continue;
						}
						ZMBluetoothManager.this.sendSPPMessage(this.msg);
					}
				}
			}
		};
		this.mInMsgProcessThread = new Runnable() {
			String msg;

			@Override
			public void run() {
				while (!ZMBluetoothManager.this.mIsAllSPPDisConnected) {
					synchronized (ZMBluetoothManager.this.inMsgStorage) {
						if (ZMBluetoothManager.this.inMsgStorage.size() <= 0) {
							continue;
						}
						this.msg = ZMBluetoothManager.this.inMsgStorage.poll();
						if (this.msg == null) {
							continue;
						}
						ZMBluetoothManager.this.checkMessageAndSendBroadcast(this.msg);
					}
				}
			}
		};
		this.mNeedReConnectSPP = true;
		this.block4MakeLog = new byte[0];
		this.block4GetDeviceModel = new byte[0];
	}

	private void changeUIStateAndAudioMode(final boolean b) {
	}

	private boolean checkCallState() {
		switch (Receiver.call_state) {
			default: {
				return false;
			}
			case 4: {
				Log.i(this.tag, "checkCallState(),UA_STATE_HOLD unprocess");
				return true;
			}
			case 0: {
				return false;
			}
			case 3: {
				Log.i(this.tag, "checkCallState(),UA_STATE_INCALL unprocess");
				return true;
			}
			case 1: {
				Log.i(this.tag, "checkCallState(),UA_STATE_INCOMING_CALL unprocess");
				return true;
			}
			case 2: {
				Log.i(this.tag, "checkCallState(),UA_STATE_OUTGOING_CALL unprocess");
				return true;
			}
		}
	}

	private boolean checkTime(final int n) {
		final long currentTimeMillis = System.currentTimeMillis();
		if (this.lastTime == 0L) {
			this.lastTime = currentTimeMillis;
			return true;
		}
		if (currentTimeMillis - this.lastTime < n) {
			return false;
		}
		this.lastTime = currentTimeMillis;
		return true;
	}

	private void disConnectAllSPP() {
		if (this.mIBridgeAdapter != null) {
			Log.i(this.tag, "disConnectAllSPP() mIBridgeAdapter != null,has (" + this.mSPPConnectedDevices.size() + ")device to disconnect");
			this.writeLog2File("disConnectAllSPP() mIBridgeAdapter != null,has (" + this.mSPPConnectedDevices.size() + ")device to disconnect");
			for (final BluetoothDevice bluetoothDevice : this.getBondedZMDevices()) {
				final BluetoothIBridgeDevice bluetoothIBridgeDevice = this.mSPPConnectedDevices.get(bluetoothDevice.getAddress());
				if (bluetoothIBridgeDevice != null) {
					if (!bluetoothIBridgeDevice.isConnected()) {
						this.writeLog2File("disConnectAllSPP() disconnectDevice()  device " + bluetoothDevice.getName() + ",device is not connected ");
						Log.i(this.tag, "disConnectAllSPP() disconnectDevice()  device " + bluetoothDevice.getName() + ",device is not connected ");
					} else {
						final Iterator<OnSppConnectStateChangedListener> iterator2 = this.sppConnectStateChangedListeners.iterator();
						while (iterator2.hasNext()) {
							iterator2.next().onDeviceDisconnectting(bluetoothIBridgeDevice);
						}
						this.mIBridgeAdapter.disconnectDevice(bluetoothIBridgeDevice);
						Log.i(this.tag, "disConnectAllSPP() disconnectDevice()  device " + bluetoothDevice.getName());
						this.writeLog2File("disConnectAllSPP() disconnectDevice()  device " + bluetoothDevice.getName());
					}
				} else {
					Log.i(this.tag, "disConnectAllSPP() disconnectDevice() is not connected device " + bluetoothDevice.getName());
					this.writeLog2File("disConnectAllSPP() disconnectDevice() is not connected device " + bluetoothDevice.getName());
				}
			}
			this.mSPPConnectedDevices.clear();
		} else {
			this.writeLog2File("disConnectAllSPP() mIBridgeAdapter == null");
			Log.i(this.tag, "disConnectAllSPP() mIBridgeAdapter == null  ");
		}
		this.stopThreads();
	}

	private void disconnectBluetoothSco(final Context context) {
		Log.i(this.tag, "disconnectBluetoothSco() beging");
		final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setBluetoothScoOn(false);
		audioManager.stopBluetoothSco();
		if (!audioManager.isBluetoothA2dpOn()) {
			audioManager.setBluetoothA2dpOn(true);
		}
		Log.i(this.tag, "disconnectBluetoothSco() end");
	}

	private void dismissProgressDialog() {
		if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
			this.mProgressDialog.dismiss();
			this.mProgressDialog = null;
		}
	}

	private void exitSPP(final Context context) {
		Log.i(this.tag, "exitSPP()");
		if (this.mIBridgeAdapter != null) {
			this.writeLog2File("exitSPP() ...");
			this.disConnectAllSPP();
			this.mIBridgeAdapter.unregisterDataReceiver((BluetoothIBridgeAdapter.DataReceiver) this.mDataReceiver);
			this.mIBridgeAdapter.unregisterEventReceiver((BluetoothIBridgeAdapter.EventReceiver) this.mEventReceiver);
			this.mIBridgeAdapter = null;
			return;
		}
		Log.i(this.tag, "exitSPP() mIBridgeAdapter == null  ");
		this.writeLog2File("exitSPP() mIBridgeAdapter == null");
	}

	private boolean findSPPConnectedDevice(final BluetoothDevice bluetoothDevice) {
		return this.mSPPConnectedDevices.get(bluetoothDevice.getAddress()) != null;
	}

	private String getDeviceInfo() {
		// TODO
		return "";
	}

	private String getDeviceModel() {
		// TODO
		return "";
	}

	public static ZMBluetoothManager getInstance() {
		return ZMBluetoothManager.instance;
	}

	private SharedPreferences getSharedPreferences(final Context context) {
		return context.getSharedPreferences("com.zed3.app", 0);
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

	private static String getTimeString() {
		if (ZMBluetoothManager.formatter == null) {
			ZMBluetoothManager.formatter = new SimpleDateFormat(" yyyy-MM-dd hh:mm:ss SSS ");
		}
		return ZMBluetoothManager.formatter.format(new Date(System.currentTimeMillis()));
	}

	private void initFile() {
		final String lastLogFileName = this.getLastLogFileName(SipUAApp.getAppContext());
		final File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + File.separator + "com.zed3.sipua");
		if (!file.exists()) {
			file.mkdir();
		}
		final File[] listFiles = file.listFiles();
		boolean b = false;
		for (int i = 0; i < listFiles.length; ++i) {
			if (listFiles[i].getName().equals(lastLogFileName)) {
				b = true;
			}
		}
		if (TextUtils.isEmpty((CharSequence) lastLogFileName) || !b) {
			final String string = "ZMBluetoothMsgLog-" + this.getDeviceModel() + "-" + new SimpleDateFormat("MMdd-hhmmss").format(new Date(System.currentTimeMillis())) + ".txt";
			this.mLogFile = new File(file, string);
			this.saveLastLogFileName(SipUAApp.getAppContext(), string);
			return;
		}
		this.mLogFile = new File(file, lastLogFileName);
	}

	private void initHFP(final Context context) {
		Log.i(this.tag, "initHFP()");
		Log.i(this.tag, "getProfileProxy success is " + this.mGetProfileProxy + " waitting for headset serviceconnect");
		this.writeLog2File("initHFP()getProfileProxy success is " + this.mGetProfileProxy + " waitting for headset serviceconnect");
	}

	private void initSPP(final Context context) {
		Log.i(this.tag, "initSPP()");
		if (this.mIBridgeAdapter == null) {
			Log.i(this.tag, "initSPP() init...");
			this.writeLog2File("initSPP() init...");
			(this.mIBridgeAdapter = new BluetoothIBridgeAdapter(context)).registerEventReceiver((BluetoothIBridgeAdapter.EventReceiver) this.mEventReceiver);
			this.mIBridgeAdapter.registerDataReceiver((BluetoothIBridgeAdapter.DataReceiver) this.mDataReceiver);
			return;
		}
		Log.i(this.tag, "initSPP() need not init again");
		this.writeLog2File("initSPP() need not init again");
	}

	public void askUserToCheckZMBluetooth() {
		Log.i(this.tag, "askUserToCheckZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToCheckZMBluetooth();
	}

	public void askUserToConnectBluetooth() {
		Log.i(this.tag, "askUserToConnectBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToConnectBluetooth();
	}

	public void askUserToConnectZMBluetooth(final BluetoothDevice bluetoothDevice) {
		Log.i(this.tag, "askUserToConnectZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToConnectZMBluetooth(bluetoothDevice);
	}

	public void askUserToDisableBluetooth() {
		Log.i(this.tag, "askUserToCheckZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToDisableBluetooth();
	}

	public void askUserToEnableBluetooth() {
		Log.i(this.tag, "askUserToCheckZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToEnableBluetooth();
	}

	public void askUserToReConnectZMBluetooth() {
		Log.i(this.tag, "askUserToConnectZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToReConnectZMBluetooth();
	}

	public void askUserToSelectBluetooth() {
		Log.i(this.tag, "askUserToConnectZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothSelectActivity.askUserToSelectBluetooth();
	}

	public void askUserToSelectHeadSetBluetooth() {
		Log.i(this.tag, "askUserToConnectZMBluetooth()");
		this.changeUIStateAndAudioMode(false);
		ZMBluetoothControlActivity.askUserToSelectHeadSetBluetooth();
	}

	boolean checkIsZM(final String s) {
		return s != null && s.startsWith("ZM") && s.length() == 8;
	}

	public void checkMessageAndSendBroadcast(final String s) {
		if (s.contains("PTT")) {
			this.sendPTTBroadcast(s);
		} else {
			if (s.contains("VOL") && this.mNeedControlVol) {
				this.sendVolumeBroadcast(s);
				return;
			}
			if (s.contains("FUNCTION")) {
				this.sendFunctionBroadcast(s);
			}
		}
	}

	public void checkVersion() {
	}

	public void connectSCO(final Context context) {
		Log.i(this.tag, "connectSCO()");
		if (this.isSPPConnected()) {
			AudioUtil.getInstance().setAudioConnectMode(4);
			return;
		}
		AudioUtil.getInstance().startBluetoothSCO();
	}

	public void connectSPP(final BluetoothDevice mLastSPPConnectDevice) {
		this.mNeedAskUserToReconnectSpp = true;
		this.mLastSPPConnectDevice = mLastSPPConnectDevice;
		Log.i(this.tag, "connectSPP() device\uff1a" + mLastSPPConnectDevice.getName());
		if (this.mIBridgeAdapter == null) {
			Log.i(this.tag, "mIBridgeAdapter() == null need initSPP");
			this.writeLog2File("mIBridgeAdapter() == null need initSPP");
			this.initSPP(SipUAApp.mContext);
		}
		if (this.findSPPConnectedDevice(mLastSPPConnectDevice)) {
			Log.i(this.tag, "mIBridgeAdapter() == null need initSPP");
			this.writeLog2File("findSPPConnectedDevice(device) " + mLastSPPConnectDevice.getName() + "is true need not connect again ");
			return;
		}
		final BluetoothIBridgeDevice bluetoothIBridgeDevice = BluetoothIBridgeDevice.createBluetoothIBridgeDevice(mLastSPPConnectDevice.getAddress());
		final Iterator<OnSppConnectStateChangedListener> iterator = this.sppConnectStateChangedListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onDeviceConnectting(bluetoothIBridgeDevice);
		}
		Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.spp_connecting)) + bluetoothIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
		this.mIsSPPConnected = this.mIBridgeAdapter.connectDevice(bluetoothIBridgeDevice);
		if (this.mIsSPPConnected) {
			Log.i(this.tag, "SPP connect device\uff1a" + bluetoothIBridgeDevice.getDeviceName() + " success");
			return;
		}
		Log.i(this.tag, "SPP connect device\uff1a" + bluetoothIBridgeDevice.getDeviceName() + "faile");
		Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.hm_connect_failed)) + bluetoothIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
	}

	public boolean connectZMBluetooth(final Context context) {
		this.writeLog2File("SPP connect ,connectZMBluetooth()");
		if (!this.isDeviceSupportBluetooth()) {
			Toast.makeText(context, (CharSequence) context.getResources().getString(R.string.mobile_notify), Toast.LENGTH_SHORT).show();
			this.writeLog2File("SPP connect ,connectZMBluetooth()  " + context.getResources().getString(R.string.mobile_notify));
			Log.i(this.tag, "\u624b\u673a\u4e0d\u652f\u6301\u84dd\u7259");
			return false;
		}
		if (!this.isBluetoothAdapterEnabled()) {
			this.enableAdapter();
		}
		BluetoothSCOStateReceiver.setOnBluetoothConnectStateChangedListener(this);
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			HeadsetBluetoothUtil.getInstance().connectZMBluetooth(this.mContext);
		} else {
			this.askUserToSelectBluetooth();
		}
		return true;
	}

	public void disConnect(final Context context, final BluetoothDevice bluetoothDevice) {
		if (this.mIBridgeDevice != null) {
			this.mIBridgeAdapter.disconnectDevice(this.mIBridgeDevice);
			Log.i(this.tag, "SPP disconnect device\uff1a" + bluetoothDevice.getName());
			this.mIBridgeDevice = null;
			return;
		}
		Toast.makeText(context, (CharSequence) context.getResources().getString(R.string.no_notify), Toast.LENGTH_SHORT).show();
	}

	public void disConnectSCO(final Context context) {
		if (this.isSPPConnected()) {
			AudioUtil.getInstance().setAudioConnectMode(3);
			return;
		}
		AudioUtil.getInstance().stopBluetoothSCO();
	}

	public void disConnectSPP(final Context context, final BluetoothDevice bluetoothDevice) {
		this.disConnect(context, bluetoothDevice);
	}

	public void disConnectZMBluetooth(final Context context) {
		this.disConnectAllSPP();
		AudioUtil.getInstance().setAudioConnectMode(3);
		BluetoothSCOStateReceiver.reMoveOnBluetoothConnectStateChangedListener(this);
	}

	public void disableAdapter() {
		if (!this.isBluetoothAdapterEnabled()) {
			return;
		}
		this.mBluetoothAdapter.disable();
		try {
			Thread.sleep(100L);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void enableAdapter() {
		if (this.isBluetoothAdapterEnabled()) {
			return;
		}
		this.mBluetoothAdapter.enable();
		try {
			Thread.sleep(100L);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void exit(final Context context) {
		Log.i(this.tag, "exit() begin");
		this.exitSPP(context);
		this.exitHFP(context);
		Log.i(this.tag, "exit() end");
		this.writeLog2File("exit()");
		AudioUtil.getInstance().setAudioConnectMode(3);
	}

	public void exitHFP(final Context context) {
		Log.i(this.tag, "exitHFP()");
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			this.disConnectSCO(context);
			HeadsetBluetoothUtil.closeProfileProxy();
			return;
		}
		this.askUserToSelectBluetooth();
	}

	public BluetoothDevice findBondedDevice(final Context context, final String s) {
		if (this.mBtAdapter == null) {
			return null;
		}
		for (final BluetoothDevice bluetoothDevice : this.mBtAdapter.getBondedDevices()) {
			if (bluetoothDevice.getAddress().equals(s)) {
				return bluetoothDevice;
			}
		}
		return null;
	}

	public List<BluetoothDevice> getBondedZMDevices() {
		final ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
		if (this.mBtAdapter != null) {
			for (final BluetoothDevice bluetoothDevice : this.mBtAdapter.getBondedDevices()) {
				if (this.checkIsZM(bluetoothDevice.getName())) {
					list.add(bluetoothDevice);
				}
			}
		}
		return list;
	}

	public String getLastConnectDevice(final Context context) {
		return this.getSharedPreferences(context).getString("last_spp_ZMBluetoothAddress", "");
	}

	public String getLastLogFileName(final Context context) {
		return this.getSharedPreferences(context).getString("last_spp_ZMBluetooth_logfile_name", "");
	}

	public BluetoothIBridgeDevice getSPPConnectedDevice() {
		if (this.mIBridgeAdapter != null) {
			return this.mIBridgeAdapter.getLastConnectedDevice();
		}
		return null;
	}

	public HashMap<String, BluetoothIBridgeDevice> getSPPConnectedDevices() {
		return this.mSPPConnectedDevices;
	}

	public boolean getZMBluetoothOnOffState(final Context context) {
		return this.getSharedPreferences(context).getBoolean("last_spp_ZMBluetooth_spp_onoff_state", false);
	}

	public void init(final Context context) {
		Log.i(this.tag, "init() begin");
		this.initSPP(context);
		this.initHFP(context);
		Log.i(this.tag, "init() end");
	}

	public boolean isBluetoothAdapterEnabled() {
		return this.mBtAdapter != null && this.mBtAdapter.isEnabled();
	}

	public boolean isDeviceSupportBluetooth() {
		return this.mBtAdapter != null;
	}

	public boolean isHeadSetEnabled() {
		if (this.isSPPConnected()) {
			return true;
		}
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			HeadsetBluetoothUtil.getInstance();
			this.mBluetoothHeadset = HeadsetBluetoothUtil.mBluetoothHeadset;
			if (this.mBluetoothHeadset != null && this.mBluetoothHeadset.getConnectedDevices().size() > 0) {
				Log.i(this.tag, "isHeadSetEnabled() = true");
				return true;
			}
		}
		Log.i(this.tag, "isHeadSetEnabled() = false");
		return false;
	}

	public boolean isSCOConnected() {
		return false;
	}

	public boolean isSPPConnected() {
		return this.mSPPConnectedDevices.size() != 0;
	}

	public void makeLog(final String s, final String s2) {
		String s3 = s;
		if (TextUtils.isEmpty((CharSequence) s)) {
			s3 = "--";
		}
		LogUtil.makeLog(s3, s2);
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice bluetoothDevice) {
		if (bluetoothDevice != null) {
			if (!getInstance().checkIsZM(bluetoothDevice.getName())) {
				Log.i(this.tag, "onDeviceConnected(),device:" + bluetoothDevice.getName() + " askUserToConnectZMBluetooth");
				this.writeLog2File("onDeviceConnected(),device:" + bluetoothDevice.getName() + " askUserToConnectZMBluetooth");
				getInstance().askUserToConnectZMBluetooth(bluetoothDevice);
				return;
			}
			Log.i(this.tag, "onDeviceConnected(),device:" + bluetoothDevice.getName() + " connectSPP");
			this.writeLog2File("onDeviceConnected(),device:" + bluetoothDevice.getName() + " connectSPP");
			getInstance().connectSPP(bluetoothDevice);
		}
	}

	@Override
	public void onDeviceConnecting(final BluetoothDevice bluetoothDevice) {
	}

	@Override
	public void onDeviceDisConnected(final BluetoothDevice bluetoothDevice) {
	}

	@Override
	public void onDeviceDisConnecting(final BluetoothDevice bluetoothDevice) {
	}

	public void processMsg(final String s) {
		Log.i(this.tag, "processMsg() ,msg:" + s);
		if (!s.equals("R_START_OK") && !s.equals("R_STOP_OK") && !s.equals("PTT_SUCC_OK") && !s.equals("PTT_WAIT_OK") && !s.equals("PA_ON_OK") && !s.equals("PA_OFF_OK")) {
			if (s.equals("PTT_DOWN")) {
				if (!this.checkCallState()) {
					Log.i(this.tag, "processMsg(" + s + ") ,PTTHandler.pressPTT(true)");
					Log.i(this.tag, "processMsg() ,GroupCallUtil.makeGroupCall(true)");
					GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
					return;
				}
				Log.i(this.tag, "processMsg(" + s + ") ,checkCallState() is true unprocess");
			} else {
				if (s.equals("PTT_UP")) {
					Log.i(this.tag, "processMsg() ,GroupCallUtil.makeGroupCall(false)");
					GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
					return;
				}
				if (!s.equals("VOL_LONG_DOWN") && !s.equals("VOL_LONG_UP") && !s.equals("FUNCTION") && !s.equals("4")) {
					s.equals("5");
				}
			}
		}
	}

	public void receive(final String s) {
		this.processMsg(s);
	}

	@Override
	public void registerReceivers(final Context context) {
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			GroupCallStateReceiver.startReceive(this.mContext);
			PhoneStatReceiver.startReceive(this.mContext);
			BluetoothSCOStateReceiver.startReceive(this.mContext);
			((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).listen((PhoneStateListener) MyPhoneStateListener.getInstance(), 32);
		}
	}

	@Override
	public void removeSppConnectStateListener(final OnSppConnectStateChangedListener onSppConnectStateChangedListener) {
		this.sppConnectStateChangedListeners.remove(onSppConnectStateChangedListener);
	}

	public void saveDevice() {
		final SharedPreferences.Editor edit = this.getSharedPreferences(SipUAApp.mContext).edit();
		edit.putString("last_spp_ZMBluetoothAddress", this.mIBridgeDevice.getDeviceAddress());
		edit.commit();
	}

	public void saveLastLogFileName(final Context context, final String s) {
		final SharedPreferences.Editor edit = this.getSharedPreferences(context).edit();
		edit.putString("last_spp_ZMBluetooth_logfile_name", s);
		edit.commit();
	}

	public void saveZMBluetoothOnOffState(final boolean b) {
		final SharedPreferences.Editor edit = this.getSharedPreferences(SipUAApp.mContext).edit();
		edit.putBoolean("last_spp_ZMBluetooth_spp_onoff_state", b);
		edit.commit();
	}

	public void send(final String mLastSendMsg) {
		while (true) {
			Label_0082:
			{
				synchronized (this) {
					synchronized (ZMBluetoothManager.class) {
						ZMBluetoothManager.mLastSendMsg = mLastSendMsg;
						if (this.mIBridgeAdapter == null) {
							this.writeLog2File("error   mIBridgeAdapter == null");
							Log.i(this.tag, "error   mIBridgeAdapter == null");
						} else {
							if (this.mCurrentIBridgeDevice != null) {
								break Label_0082;
							}
							this.writeLog2File("error   mCurrentIBridgeDevice == null,no device connect spp");
							Log.i(this.tag, "error   mCurrentIBridgeDevice == null,no device connect spp");
						}
						return;
					}
				}
			}
			final String s;
//            final byte[] bytes = s.getBytes();
			final long currentTimeMillis = System.currentTimeMillis();
			if (this.mLastSendTime == 0L) {
				this.mLastSendTime = currentTimeMillis;
			}
			final long n = currentTimeMillis - this.mLastSendTime;
//            Log.i(this.tag, "SPP out ,device:" + this.mCurrentIBridgeDevice.getDeviceName() + " msg:" + s + "cycle = " + n);
//            this.writeLog2File("SPP out ,device:" + this.mCurrentIBridgeDevice.getDeviceName() + " msg:" + s + "cycle = " + n);
//            this.mIBridgeAdapter.send(this.mCurrentIBridgeDevice, bytes, bytes.length);
			this.mLastSendTime = currentTimeMillis;
		}
		// monitorexit(ZMBluetoothManager.class)
	}

	public void sendFunctionBroadcast(final String s) {
		final Intent intent = new Intent("com.zed3.sipua_bluetooth");
		intent.putExtra("control_type", "control_type_function");
		if (s.equals("FUNCTION")) {
			intent.putExtra("control_action", "control_action_function");
			SipUAApp.mContext.sendBroadcast(intent);
		}
	}

	public void sendPTTBroadcast(final String s) {
		final Intent intent = new Intent("com.zed3.sipua_bluetooth");
		intent.putExtra("control_type", "control_type_ptt");
		if (s.equals("PTT_DOWN")) {
			intent.putExtra("control_action", "control_action_ptt_down");
		} else {
			if (!s.equals("PTT_UP")) {
				return;
			}
			intent.putExtra("control_action", "control_action_ptt_up");
		}
		SipUAApp.mContext.sendBroadcast(intent);
	}

	@Deprecated
	public void sendSPPMessage(final String s) {
	}

	public void sendVolumeBroadcast(final String s) {
		final Intent intent = new Intent("com.zed3.sipua_bluetooth");
		intent.putExtra("control_type", "control_type_vol");
		if (s.equals("VOL_SHORT_DOWN")) {
			intent.putExtra("control_action", "control_action_vol_short_down");
		} else {
			if (s.equals("VOL_SHORT_UP")) {
				intent.putExtra("control_action", "control_action_vol_short_up");
				return;
			}
			if (s.equals("VOL_LONG_DOWN")) {
				intent.putExtra("control_action", "control_action_vol_long_down");
				return;
			}
			if (s.equals("VOL_LONG_UP")) {
				intent.putExtra("control_action", "control_action_vol_long_up");
			}
		}
	}

	@Override
	public boolean setHeadSetConnectStateListener(final HeadSetConnectStateListener mHeadSetConnectStateListener) {
		if (Integer.parseInt(Build.VERSION.SDK) >= 15) {
			HeadsetBluetoothUtil.getInstance().mHeadSetConnectStateListener = mHeadSetConnectStateListener;
			HeadsetBluetoothUtil.getInstance().getHeadsetBluetooths(this.mContext);
			return true;
		}
		this.askUserToSelectBluetooth();
		return false;
	}

	@Override
	public void setSppConnectStateListener(final OnSppConnectStateChangedListener onSppConnectStateChangedListener) {
		this.sppConnectStateChangedListeners.add(onSppConnectStateChangedListener);
	}

	@Override
	public void startReConnectingSPP(final String s, final long n, final int n2) {
	}

	public void startThreads() {
		this.stopThreads();
		(this.sppMessageSender = new SppMessageSender(ZMBluetoothManager.sppMessageStorage4Send)).startSending();
		this.sppMessageSender.start();
		this.writeLog2File("SPP state onDeviceConnected() sppMessageSender.startSending();");
		(this.sppMessageReceiver = new SppMessageReceiver(ZMBluetoothManager.sppMessageStorage4Receive)).startReceiving();
		this.sppMessageReceiver.start();
		this.writeLog2File("SPP state onDeviceConnected() sppMessageSender.startReceiving();");
	}

	@Override
	public boolean stopReConnectingSPP() {
		return false;
	}

	public void stopThreads() {
		// TODO
	}

	@Override
	public void unregisterReceivers(final Context context) {
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			GroupCallStateReceiver.stopReceive(this.mContext);
			PhoneStatReceiver.stopReceive(this.mContext);
			BluetoothSCOStateReceiver.stopReceive(this.mContext);
		}
	}

	public void writeLog2File(final String s) {
		this.makeLog("--", s);
	}

	public void writeLog2File(String string, final String s) {
		try {
			if (Environment.getExternalStorageState().equals("mounted")) {
				if (this.sb == null) {
					this.sb = new StringBuilder();
				}
				this.sb.append("\r\n" + this.getDeviceModel());
				this.sb.append(" " + ("TIME:" + getTimeString()));
				this.sb.append(" Thread:" + Thread.currentThread().getName());
				this.sb.append(" " + string);
				this.sb.append(" " + s);
				string = this.sb.toString();
				if (this.mFileWriter == null) {
					this.initFile();
					this.mFileWriter = new FileWriter(this.mLogFile, true);
				}
				if (!this.mLogFile.exists()) {
					this.initFile();
					this.mFileWriter = new FileWriter(this.mLogFile, true);
				}
				if (this.mLogFile.length() > 52428800L) {
					this.mFileWriter.close();
					this.mLogFile.delete();
					this.saveLastLogFileName(SipUAApp.getAppContext(), "");
					this.initFile();
					this.mFileWriter = new FileWriter(this.mLogFile, true);
				}
				this.mFileWriter.write(string);
				this.mFileWriter.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (this.sb != null) {
				this.sb.delete(0, this.sb.length());
			}
		}
	}

	class MyDataReceiver implements BluetoothIBridgeAdapter.DataReceiver {
		private String msg;

		@Override
		public void onDataReceived(final BluetoothIBridgeDevice mCurrentIBridgeDevice, final byte[] array, int i) {
			// TODO
		}
	}

	class MyEventReceiver implements BluetoothIBridgeAdapter.EventReceiver {
		public void onDeviceConnectFailed(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
			ZMBluetoothManager.this.dismissProgressDialog();
			Log.i(ZMBluetoothManager.this.tag, "SPP 连接失败  " + bluetoothIBridgeDevice.getDeviceName() + ":" + bluetoothIBridgeDevice.getDeviceAddress() + " onDeviceConnectFailed()");
			Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.spp_failed)) + bluetoothIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
			ZMBluetoothManager.this.changeUIStateAndAudioMode(false);
			ZMBluetoothManager.this.askUserToCheckZMBluetooth();
			ZMBluetoothManager.this.writeLog2File("SPP state onDeviceConnectFailed() device " + bluetoothIBridgeDevice.getDeviceName());
			AudioUtil.getInstance().setAudioConnectMode(3);
			final Iterator<OnSppConnectStateChangedListener> iterator = ZMBluetoothManager.this.sppConnectStateChangedListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().onDeviceConnectFailed(bluetoothIBridgeDevice);
			}
		}

		@Override
		public void onDeviceConnected(final BluetoothIBridgeDevice mCurrentIBridgeDevice) {
			Tools.bringtoFront(SipUAApp.mContext);
			ZMBluetoothManager.this.dismissProgressDialog();
			Log.i(ZMBluetoothManager.this.tag, "SPP \u8fde\u63a5\u6210\u529f  " + mCurrentIBridgeDevice.getDeviceName() + "," + mCurrentIBridgeDevice.getDeviceAddress() + " onDeviceConnected()");
			Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.spp_success)) + mCurrentIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
			ZMBluetoothManager.this.mSPPConnectedDevices.put(mCurrentIBridgeDevice.getDeviceAddress(), mCurrentIBridgeDevice);
			ZMBluetoothManager.this.mCurrentIBridgeDevice = mCurrentIBridgeDevice;
			ZMBluetoothManager.this.writeLog2File("SPP state onDeviceConnected() device " + mCurrentIBridgeDevice.getDeviceName());
			ZMBluetoothManager.this.startThreads();
			if (ZMBluetoothManager.mLastSendMsg != null && !ZMBluetoothManager.mLastSendMsg.equals("")) {
				ZMBluetoothManager.this.sendSPPMessage(ZMBluetoothManager.mLastSendMsg);
			}
			final Activity instance = ZMBluetoothSelectActivity.getInstance();
			if (instance != null) {
				instance.finish();
			}
			final Activity instance2 = ZMBluetoothControlActivity.getInstance();
			if (instance2 != null) {
				instance2.finish();
			}
			ZMBluetoothManager.this.changeUIStateAndAudioMode(true);
			final Iterator<OnSppConnectStateChangedListener> iterator = ZMBluetoothManager.this.sppConnectStateChangedListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().onDeviceConnected(mCurrentIBridgeDevice);
			}
		}

		@Override
		public void onDeviceDisconnected(BluetoothIBridgeDevice bluetoothIBridgeDevice, String s) {

		}

		@Override
		public void onDeviceConnectFailed(BluetoothIBridgeDevice bluetoothIBridgeDevice, String s) {

		}

		@Override
		public void onWriteFailed(BluetoothIBridgeDevice bluetoothIBridgeDevice, String s) {

		}

		public void onDeviceDisconnected(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
			Log.i(ZMBluetoothManager.this.tag, "SPP \u8fde\u63a5\u4e2d\u65ad  " + bluetoothIBridgeDevice.getDeviceName() + "," + bluetoothIBridgeDevice.getDeviceAddress() + " onDeviceDisconnected()");
			Toast.makeText(SipUAApp.mContext, (CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.spp_dis)) + bluetoothIBridgeDevice.getDeviceName()), Toast.LENGTH_SHORT).show();
			final BluetoothIBridgeDevice bluetoothIBridgeDevice2 = ZMBluetoothManager.this.mSPPConnectedDevices.remove(bluetoothIBridgeDevice.getDeviceAddress());
			if (ZMBluetoothManager.this.mSPPConnectedDevices.size() == 0) {
				ZMBluetoothManager.this.mIsAllSPPDisConnected = true;
				Log.i(ZMBluetoothManager.this.tag, "SPP \u6240\u6709\u8fde\u63a5\u8bbe\u5907\u5df2\u65ad\u5f00  \u505c\u6b62\u901a\u4fe1  ");
				Toast.makeText(SipUAApp.mContext, (CharSequence) SipUAApp.mContext.getResources().getString(R.string.hm_dis), Toast.LENGTH_SHORT).show();
			}
			if (ZMBluetoothManager.this.mNeedAskUserToReconnectSpp) {
				ZMBluetoothManager.this.askUserToReConnectZMBluetooth();
			}
			ZMBluetoothManager.this.writeLog2File("SPP state onDeviceDisconnected() device " + bluetoothIBridgeDevice.getDeviceName());
			ZMBluetoothManager.this.changeUIStateAndAudioMode(false);
			final Iterator<OnSppConnectStateChangedListener> iterator = ZMBluetoothManager.this.sppConnectStateChangedListeners.iterator();
			while (true) {
				Label_0262:
				{
					if (iterator.hasNext()) {
						break Label_0262;
					}
					synchronized (ZMBluetoothManager.class) {
						ZMBluetoothManager.this.mCurrentIBridgeDevice = null;
						// monitorexit(ZMBluetoothManager.class)
						GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
						ZMBluetoothManager.this.stopThreads();
						iterator.next().onDeviceDisconnected(bluetoothIBridgeDevice);
					}
				}
			}
		}

		@Override
		public void onDeviceFound(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
			Log.i(ZMBluetoothManager.this.tag, String.valueOf(bluetoothIBridgeDevice.getDeviceName()) + "," + bluetoothIBridgeDevice.getDeviceAddress() + " onDeviceFound()");
			final Iterator<OnSppConnectStateChangedListener> iterator = ZMBluetoothManager.this.sppConnectStateChangedListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().onDeviceFound(bluetoothIBridgeDevice);
			}
		}

		@Override
		public void onDiscoveryFinished() {
			Log.i(ZMBluetoothManager.this.tag, "onDiscoveryFinished()");
		}
	}
}
