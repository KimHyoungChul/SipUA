package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.groupmessage.GroupMessage;
import com.zed3.log.MyLog;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.phone.Call;
import com.zed3.sipua.phone.Phone;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.MessageListAdapter;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;
import com.zed3.video.VideoManagerService;
import com.zed3.window.MyWindowManager;

import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.tools.InCallInfo;

import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DemoCallScreen extends BaseActivity implements CallManager.OnRejectCallCompletedListener {
	private static final int ANSWER_CALL = 1;
	static final float PROXIMITY_THRESHOLD = 5.0f;
	private static final int REJECT_CALL = 0;
	private static final String TAG = "DemoCallScreen";
	public static DemoCallScreen mContext;
	public static String mName;
	public static String mNumber;
	public static boolean pactive;
	public static boolean started;
	private final String ACTION_CALL_END;
	ArrayList<GroupMessage> ListGroupMessage;
	private VideoManagerService VMS;
	ImageView btnSMS;
	ImageView btnno;
	ImageView btnok;
	ImageView btnoutend;
	Handler buttonHandler;
	TextView callName;
	TextView callNum;
	TextView calltip;
	Phone ccPhone;
	private Handler handler;
	private ImageView image1;
	private ImageView image2;
	private ImageView image3;
	private InCallInfo info;
	private boolean isRinging;
	boolean isshowing;
	LinearLayout line_incall;
	LinearLayout line_outcall;
	private String mCallId;
	private CallManager.CallParams mCallParams;
	private CallManager.CallType mCallType;
	Chronometer mElapsedTime;
	private IntentFilter mFilter;
	private String mScreanWakeLockKey;
	private PowerManager.WakeLock mWakelock;
	private DatagramSocket mdsSocket;
	private ListView messageList;
	private View messageView;
	private FrameLayout photo;
	private PopupWindow popview;
	private BroadcastReceiver quitRecv;
	private boolean rejFlag;
	private FrameLayout ringA;
	private View screenOffView;
	List<String> strings;

	public DemoCallScreen() {
		this.ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
		this.isRinging = true;
		this.handler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.arg1) {
					default: {
					}
					case 0: {
						DemoCallScreen.this.image1.setAlpha(0);
						DemoCallScreen.this.image2.setAlpha(250);
						DemoCallScreen.this.image3.setAlpha(250);
					}
					case 1: {
						DemoCallScreen.this.image1.setAlpha(250);
						DemoCallScreen.this.image2.setAlpha(0);
						DemoCallScreen.this.image3.setAlpha(250);
					}
					case 2: {
						DemoCallScreen.this.image1.setAlpha(250);
						DemoCallScreen.this.image2.setAlpha(250);
						DemoCallScreen.this.image3.setAlpha(0);
					}
				}
			}
		};
		this.quitRecv = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_callscreen_finish")) {
					if (Receiver.call_state == 0) {
						Log.e("DemoCallScreen", "ACTION_CALL_END onstop");
						DemoCallScreen.this.finish();
					} else {
						MyLog.i("DemoCallScreen", "0000");
					}
				}
				if (intent.getAction().equalsIgnoreCase("android.action.closeDemoCallScreen")) {
					DemoCallScreen.this.finish();
					MyLog.i("DemoCallScreen", "finish broadcas");
				}
			}
		};
		this.mdsSocket = null;
		this.calltip = null;
		this.callNum = null;
		this.callName = null;
		this.btnok = null;
		this.btnno = null;
		this.btnSMS = null;
		this.btnoutend = null;
		this.mWakelock = null;
		this.info = null;
		this.line_outcall = null;
		this.line_incall = null;
		this.rejFlag = false;
		this.mScreanWakeLockKey = "DemoCallScreen";
		this.isshowing = false;
		this.strings = null;
		this.buttonHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					case 0: {
						if (DemoCallScreen.this.ringA != null) {
//							DemoCallScreen.access .4 (DemoCallScreen.this, false);
						}
						if (!DemoCallScreen.this.rejFlag) {
//							DemoCallScreen.access .6 (DemoCallScreen.this, true);
							DemoCallScreen.this.reject();
						}
						DemoCallScreen.this.finish();
					}
					case 1: {
						if (DemoCallScreen.this.ringA != null) {
//							DemoCallScreen.access .4 (DemoCallScreen.this, false);
						}
						if (Receiver.call_state == 1) {
							DemoCallScreen.this.answer();
							DemoCallScreen.this.finish();
							return;
						}
						break;
					}
				}
			}
		};
	}

	private void GetCallNum() {
		this.callName.setText((CharSequence) CallUtil.mName);
		if (!CallUtil.mName.equals(CallUtil.mNumber)) {
			this.callNum.setText((CharSequence) CallUtil.mNumber);
		}
	}

	private String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(System.currentTimeMillis() - SipdroidEngine.serverTimeVal);
		} catch (Exception ex) {
			return null;
		}
	}

	public static DemoCallScreen getInstance() {
		return DemoCallScreen.mContext;
	}

	private String getMsgId(final Context context) {
		final StringBuilder sb = new StringBuilder();
		sb.append("00000000");
		sb.append(String.valueOf((System.currentTimeMillis() - SipdroidEngine.serverTimeVal) / 1000L));
		sb.append(Tools.getRandomCharNum(14));
		return sb.toString();
	}

	private int getStringId(final boolean b, final String s) {
		if (!b) {
			if ("com.zed3.action.VIDEO_CALL".equals(s)) {
				return R.string.vedio_calloutgoing;
			}
			if ("com.zed3.action.VIDEO_MONITOR".equals(s)) {
				return R.string.vedio_monitor_outgoing;
			}
			return R.string.vedio_upload_outgoing;
		} else {
			if ("com.zed3.action.VIDEO_CALL".equals(s)) {
				return R.string.vedio_callincoming;
			}
			if ("com.zed3.action.VIDEO_MONITOR".equals(s)) {
				return R.string.vedio_monitor_incoming;
			}
			return R.string.vedio_upload_incoming;
		}
	}

	private void sendMessage(final String s, final String s2) {
		final String sendTextMessage = Receiver.GetCurUA().SendTextMessage(s, s2, this.getMsgId((Context) DemoCallScreen.mContext));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase((Context) DemoCallScreen.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("body", s2);
		contentValues.put("mark", 1);
		contentValues.put("address", s);
		contentValues.put("status", 1);
		contentValues.put("date", this.getCurrentTime());
		contentValues.put("E_id", sendTextMessage);
		contentValues.put("send", 2);
		contentValues.put("type", "sms");
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		contentValues.put("server_ip", autoConfigManager.fetchLocalServer());
		contentValues.put("local_number", autoConfigManager.fetchLocalUserName());
		smsMmsDatabase.insert("message_talk", contentValues);
		LogUtil.makeLog("DemoCallScreen", "--++>>sendMessage()->body:" + s2);
	}

	public void answer() {
		final String currentAction = this.VMS.getCurrentAction();
		if (UserAgent.isTempGrpCallMode && currentAction != null && currentAction.contains("com.zed3.action.VIDEO_CALL")) {
			Receiver.ua.hangupTmpGrpCall(true);
			Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
		}
		CallUtil.answerCall();
		if (Receiver.ccCall != null) {
			Receiver.ccCall.setState(Call.State.ACTIVE);
			Receiver.ccCall.base = SystemClock.elapsedRealtime();
		}
	}

	public void answerCall() {
		final Message obtainMessage = this.buttonHandler.obtainMessage();
		obtainMessage.what = 1;
		this.buttonHandler.sendMessage(obtainMessage);
	}

	void dismissPop() {
		if (this.popview == null || !this.popview.isShowing()) {
			return;
		}
		while (true) {
			try {
				this.popview.dismiss();
				this.isshowing = false;
			} catch (Exception ex) {
				continue;
			}
			break;
		}
	}

	void moveBack() {
		if (Receiver.ccConn != null) {
			Receiver.ccConn.isIncoming();
		}
		this.onStop();
	}

	@Override
	public void onCompledted(final org.zoolu.sip.call.Call call) {
		if (!this.rejFlag) {
			this.rejFlag = true;
			CallManager.getManager().setUserAgentVideoCall(CallManager.getManager().getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
			this.reject();
			VideoManagerService.getDefault().clearRemoteVideoParameter();
		}
		this.finish();
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		DemoCallScreen.mContext = this;
		this.getWindow().setType(2003);
		super.onCreate(bundle);
		this.VMS = VideoManagerService.getDefault();
		final String currentAction = this.VMS.getCurrentAction();
		boolean booleanExtra = this.getIntent().getBooleanExtra("IsCallIn", false);
		this.mCallParams = CallManager.getCallParams(this.getIntent());
		if (TextUtils.isEmpty((CharSequence) this.mCallParams.getCallId())) {
			this.mCallParams = CallManager.getVideoCallParams();
			final ExtendedCall call = CallManager.getManager().getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId());
			if (call == null) {
				this.finish();
				return;
			}
			booleanExtra = (call.getCallerState() == 1);
		}
		if (Receiver.call_state == 3) {
			this.finish();
			return;
		}
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen("DemoCallScreen");
		MyWindowManager.getInstance().disableKeyguard(this);
		this.setContentView(R.layout.callscreen);
		this.screenOffView = this.findViewById(R.id.screen_off_view);
		(this.calltip = (TextView) this.findViewById(R.id.calltip)).setText(this.getStringId(booleanExtra, currentAction));
		this.callNum = (TextView) this.findViewById(R.id.callnum);
		this.callName = (TextView) this.findViewById(R.id.callname);
		this.mElapsedTime = (Chronometer) this.findViewById(R.id.elapsedTime);
		this.image1 = (ImageView) this.findViewById(R.id.image1);
		this.image2 = (ImageView) this.findViewById(R.id.image2);
		this.image3 = (ImageView) this.findViewById(R.id.image3);
		this.photo = (FrameLayout) this.findViewById(R.id.photoUser);
		this.ringA = (FrameLayout) this.findViewById(R.id.callOut);
		this.line_incall = (LinearLayout) this.findViewById(R.id.line_incall);
		this.line_outcall = (LinearLayout) this.findViewById(R.id.line_outcall);
		(this.mFilter = new IntentFilter()).addAction("com.zed3.sipua.ui_callscreen_finish");
		this.mFilter.addAction("android.action.closeDemoCallScreen");
		this.registerReceiver(this.quitRecv, this.mFilter);
		if (this.mElapsedTime != null) {
			this.mElapsedTime.setBase(SystemClock.elapsedRealtime());
			this.mElapsedTime.start();
		}
		MyLog.i("DemoCallScreen", "oncreate");
	}

	protected void onDestroy() {
		super.onDestroy();
		MyLog.i("DemoCallScreen", "democallscreen ondestory");
		while (true) {
			try {
				if (this.mElapsedTime != null) {
					this.mElapsedTime.stop();
				}
				MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
				MyWindowManager.getInstance().reenableKeyguard(this);
				if (this.mFilter != null) {
					this.unregisterReceiver(this.quitRecv);
				}
				DemoCallScreen.mContext = null;
			} catch (Exception ex) {
				MyLog.e("DemoCallScreen", "democallscreen ondestory error:" + ex.toString());
				continue;
			}
			break;
		}
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		boolean onKeyDown = true;
		switch (n) {
			default: {
				onKeyDown = super.onKeyDown(n, keyEvent);
				return onKeyDown;
			}
			case 4: {
				return onKeyDown;
			}
			case 5: {
				onKeyDown = onKeyDown;
				if (Receiver.call_state == 1) {
					this.answer();
					this.finish();
					return true;
				}
				return onKeyDown;
			}
			case 6: {
				this.reject();
				return true;
			}
		}
	}

	protected void onPause() {
		this.isRinging = false;
		super.onPause();
	}

	protected void onResume() {
		// TODO
		super.onResume();
	}

	protected void onStart() {
		super.onStart();
		if (Receiver.call_state == 0) {
			DemoCallScreen.started = true;
		}
	}

	protected void onStop() {
		super.onStop();
		DemoCallScreen.started = false;
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		callState.convert();
		if (callState == CallManager.CallState.INCOMING || callState == CallManager.CallState.OUTGOING) {
			CallManager.getManager().addOnRejectCallCompletedListener(this.mCallParams.getCallId(), (CallManager.OnRejectCallCompletedListener) this);
		}
	}

	public void reject() {
		if (Receiver.ccCall != null) {
			Receiver.ccCall.setState(Call.State.DISCONNECTED);
		}
		new Thread() {
			@Override
			public void run() {
				CallUtil.rejectVideoCall();
//				DemoCallScreen.access .6 (DemoCallScreen.this, false);
			}
		}.start();
	}

	public void rejectCall() {
		final Message obtainMessage = this.buttonHandler.obtainMessage();
		obtainMessage.what = 0;
		this.buttonHandler.sendMessage(obtainMessage);
	}

	void showMessagePopWindow(final View view) {
		if (this.popview == null) {
			this.messageView = View.inflate((Context) DemoCallScreen.mContext, R.layout.message_list, (ViewGroup) null);
			this.messageList = (ListView) this.messageView.findViewById(R.id.messagelistview);
			this.strings = DataBaseService.getInstance().getAllMessages();
			this.messageList.setAdapter((ListAdapter) new MessageListAdapter((Context) DemoCallScreen.mContext, this.strings));
			this.popview = new PopupWindow(this.messageView, -1, 500);
		}
		this.popview.setFocusable(true);
		this.popview.setOutsideTouchable(true);
		this.popview.setBackgroundDrawable((Drawable) new BitmapDrawable());
		final int n = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() / 2 - this.popview.getWidth() / 2;
		Log.i("coder", "xPos:" + n);
		view.getLocationOnScreen(new int[2]);
		this.popview.showAsDropDown(view, n, 0);
		this.messageList.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				Receiver.isCallNotificationNeedClose();
				if (DemoCallScreen.this.ringA != null) {
//					DemoCallScreen.access .4 (DemoCallScreen.this, false);
				}
				if (!DemoCallScreen.this.rejFlag) {
//					DemoCallScreen.access .6 (DemoCallScreen.this, true);
					CallManager.getManager().setUserAgentVideoCall(CallManager.getManager().getCall(DemoCallScreen.this.mCallParams.getCallType(), DemoCallScreen.this.mCallParams.getCallId()));
					DemoCallScreen.this.reject();
					new Thread() {
						@Override
						public void run() {
							DemoCallScreen.this.sendMessage(CallUtil.mNumber, DemoCallScreen.this.strings.get(n));
						}
					}.start();
				}
				DemoCallScreen.this.dismissPop();
				DemoCallScreen.this.finish();
			}
		});
		this.isshowing = true;
	}
}
