package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.log.MyLog;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.CallManager.CallParams;
import com.zed3.sipua.CallManager.CallState;
import com.zed3.sipua.CallManager.CallType;
import com.zed3.sipua.CallManager.OnEndAudioCallHandler;
import com.zed3.sipua.CallManager.OnRejectCallCompletedListener;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.MessageListAdapter;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;
import com.zed3.video.VideoManagerService;
import com.zed3.window.MyWindowManager;

import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.ExtendedCall;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;


public class CallActivity extends BaseActivity implements OnClickListener, OnLongClickListener, OnEndAudioCallHandler, OnRejectCallCompletedListener {
	public static String ACTION_AMR_RATE_CHANGE;
	public static String ACTION_CHANGE_CALL_STATE;
	protected static final int HIDECONNECTVIEW = 0;
	public static String NEWSTATE;
	static final float PROXIMITY_THRESHOLD = 5.0f;
	private static final String TAG = "CallActivity";
	private static final int TONE_LENGTH_MS = 150;
	static boolean isKeyBoardShow = false;
	static boolean isLoudspeakerOn = false;
	static boolean isMuteOn = false;
	private static RelativeLayout loudspeakerOffBT;
	private static RelativeLayout loudspeakerOnBT;
	private static Context mContext;
	public static int mState = 0;
	public static final HashMap<Character, Integer> mToneMap;
	private static final int maxInputNum = 1000;
	public static boolean pactive;
	public static String userName;
	private static TextView userNameTV;
	public static String userNum;
	private static TextView userNumberTV;
	private final String ACTION_CALL_END;
	private String CALL_STATE;
	final int SCREEN_OFF_TIMEOUT;
	private ImageView acceptIncomingCall;
	private LinearLayout bg;
	private ImageButton btn0;
	private ImageButton btndel;
	private ImageButton btnenight;
	private ImageButton btnfive;
	private ImageButton btnfour;
	private ImageButton btnjing;
	private ImageButton btnmi;
	private ImageButton btnnine;
	private ImageButton btnone;
	private ImageButton btnseven;
	private ImageButton btnsix;
	private ImageButton btnthree;
	private ImageButton btntwo;
	public Chronometer callTime;
	public TextView callTime2;
	private TextView connectStateTV;
	private View controlOverLayView;
	private ImageView endCallBT;
	private ImageView endCallBT2;
	private TextView endCallTT;
	private RelativeLayout forbidSoundOutOffBT;
	private RelativeLayout forbidSoundOutOnBT;
	Handler handler;
	private Handler handler2;
	private ImageView image1;
	private ImageView image2;
	private ImageView image3;
	private View incomeControlView;
	private boolean isAcceptSelf;
	boolean isIncomingcall;
	private boolean isPause;
	private boolean isRinging;
	boolean isshowing;
	private View keyBoard;
	private View keyBoard2;
	private ImageView keyboardHideBT;
	private ImageView keyboardShowBT;
	private int len;
	private View lineForbidSoundOut;
	private View lineKeyboard;
	private View lineLoudspeaker;
	private CallParams mCallParams;
	private String mCallername;
	private boolean mDTMFToneEnabled;
	private View mRootView;
	private String mScreanWakeLockKey;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock;
	private ListView messageList;
	private View messageView;
	private ImageView newCallBT;
	private int num;
	private EditText numTxt;
	private boolean numTxtCursor;
	int oldtimeout;
	private FrameLayout photo;
	private PopupWindow popview;
	private BroadcastReceiver receiver;
	private boolean rejFlag;
	private ImageView rejectIncomingCall;
	private FrameLayout ringA;
	boolean running;
	private View screenOffView;
	private ImageView smsAnswer;
	List<String> strings;
	Thread t;
	private LinearLayout tableShow;
	private ImageView userPhotoIV;
	private View yincang;

	static {
		CallActivity.userNum = "--";
		CallActivity.userName = "--";
		CallActivity.ACTION_CHANGE_CALL_STATE = "com.zed3.sipua.ui.CallActivity.CALL_STATE";
		CallActivity.NEWSTATE = "callState";
		CallActivity.ACTION_AMR_RATE_CHANGE = "com.zed3.sipua.ui.AMR_RATE_CHANGE";
		mToneMap = new HashMap<Character, Integer>();
	}

	public CallActivity() {
		this.numTxt = null;
		this.btnone = null;
		this.btntwo = null;
		this.btnthree = null;
		this.btnfour = null;
		this.btnfive = null;
		this.btnsix = null;
		this.btnseven = null;
		this.btnenight = null;
		this.btnnine = null;
		this.btn0 = null;
		this.btnmi = null;
		this.btnjing = null;
		this.btndel = null;
		this.isRinging = true;
		this.isAcceptSelf = false;
		this.handler2 = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.arg1) {
					default: {
					}
					case 0: {
						CallActivity.this.image1.setAlpha(0);
						CallActivity.this.image2.setAlpha(250);
						CallActivity.this.image3.setAlpha(250);
					}
					case 1: {
						CallActivity.this.image1.setAlpha(250);
						CallActivity.this.image2.setAlpha(0);
						CallActivity.this.image3.setAlpha(250);
					}
					case 2: {
						CallActivity.this.image1.setAlpha(250);
						CallActivity.this.image2.setAlpha(250);
						CallActivity.this.image3.setAlpha(0);
					}
				}
			}
		};
		this.ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
		this.mCallername = "";
		this.receiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final Bundle extras = intent.getExtras();
				MyLog.d("videoTrace", "CallActivity#onReceive action = " + intent.getAction());
				if (intent.getAction().equals(CallActivity.ACTION_CHANGE_CALL_STATE)) {
					final CallManager.CallParams callParams = CallManager.getCallParams(intent);
					MyLog.d("videoTrace", "CallActivity#onReceive action = ACTION_CALL_END username = " + CallActivity.this.mCallParams.getUsername() + " , param user name = " + callParams.getUsername());
					if (callParams == null || !callParams.equals(CallActivity.this.mCallParams)) {
						MyLog.d("videoTrace", "CallActivity#onReceive action = ACTION_CALL_END  no eques");
					} else {
						if (CallManager.getManager().getCallState(callParams).convert() == -1) {
							extras.getInt(CallActivity.NEWSTATE);
						}
						AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
						final int int1 = extras.getInt(CallActivity.NEWSTATE);
						switch (int1) {
							case 1:
							case 2:
							case 4: {
								break;
							}
							default: {
							}
							case 0: {
								CallActivity.this.keyBoard2.setVisibility(View.INVISIBLE);
								CallActivity.this.callTime2.setVisibility(View.INVISIBLE);
								if (CallActivity.this.callTime != null) {
									CallActivity.this.callTime.stop();
									CallActivity.this.callTime = null;
								}
								CallActivity.this.setText4ConnectStateView(int1);
								CallActivity.reSetControlStates();
								CallActivity.this.finish();
							}
							case 3: {
//								CallActivity.access .4 (CallActivity.this, 0);
//								if (CallActivity.this.callTime != null) {
//									CallActivity.this.callTime.setBase(SystemClock.elapsedRealtime());
//								}
								CallUtil.mCallBeginTime = java.lang.System.currentTimeMillis();
								CallUtil.mCallBeginTime2 = java.lang.System.currentTimeMillis();
								if (RtpStreamReceiver_signal.speakermode == 2) {
									CallActivity.this.setVolumeControlStream(0);
								}
								CallActivity.this.setText4ConnectStateView(int1);
								CallActivity.this.hideConnectStateView(2000);
								CallActivity.this.setControlViewsVisible();
								final Message obtainMessage = CallActivity.this.handler.obtainMessage();
								obtainMessage.what = 0;
								CallActivity.this.handler.sendMessage(obtainMessage);
							}
						}
					}
				} else if (intent.getAction().equals("com.zed3.sipua.ui_callscreen_finish") && Receiver.call_state == 0) {
					final CallManager.CallParams callParams2 = CallManager.getCallParams(intent);
					MyLog.d("videoTrace", "CallActivity#onReceive() enter call action action_call_end , call params = " + callParams2 + " , hold call params = " + CallActivity.this.mCallParams);
					if (callParams2 != null && CallActivity.this.mCallParams != null && CallActivity.this.mCallParams.equals(callParams2)) {
						if (CallActivity.this.callTime != null) {
							CallActivity.this.callTime.stop();
							CallActivity.this.callTime = null;
						}
						CallActivity.this.setText4ConnectStateView(CallActivity.mState);
						CallActivity.reSetControlStates();
						CallActivity.this.finish();
					}
				}
			}
		};
		this.handler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						CallActivity.this.hideConnectStateView(0);
					}
				}
			}
		};
		this.num = 0;
		this.mScreanWakeLockKey = "CallActivity";
		this.isshowing = false;
		this.strings = null;
		this.running = false;
		this.mToneGeneratorLock = new Object();
		this.len = 0;
		this.isPause = false;
		this.SCREEN_OFF_TIMEOUT = 12000;
		this.CALL_STATE = "callstate";
		this.rejFlag = false;
	}

	private void InitTones() {
		boolean mdtmfToneEnabled = true;
		CallActivity.mToneMap.put('1', 1);
		CallActivity.mToneMap.put('2', 2);
		CallActivity.mToneMap.put('3', 3);
		CallActivity.mToneMap.put('4', 4);
		CallActivity.mToneMap.put('5', 5);
		CallActivity.mToneMap.put('6', 6);
		CallActivity.mToneMap.put('7', 7);
		CallActivity.mToneMap.put('8', 8);
		CallActivity.mToneMap.put('9', 9);
		CallActivity.mToneMap.put('0', 0);
		CallActivity.mToneMap.put('#', 11);
		CallActivity.mToneMap.put('*', 10);
		CallActivity.mToneMap.put('d', 12);

//		if (Settings.System.getInt(mContext.getContentResolver(), "dtmf_tone", 1) != 1)
//			mdtmfToneEnabled = false;
		this.mDTMFToneEnabled = mdtmfToneEnabled;
		synchronized (this.mToneGeneratorLock) {
			if (this.mToneGenerator == null) {
				try {
					this.mToneGenerator = new ToneGenerator(3, 80);
					((Activity) mContext).setVolumeControlStream(3);
				} catch (RuntimeException e) {
					Log.w("tag", "Exception caught while creating local tone generator: " + e);
					this.mToneGenerator = null;
				}
			}
		}
	}

	private void closeRinging() {
		this.isRinging = false;
		this.ringA.setVisibility(View.GONE);
		this.photo.setVisibility(View.VISIBLE);
	}

	private void delete() {
		final StringBuffer sb = new StringBuffer(this.numTxt.getText().toString().trim());
		int n2;
		StringBuffer sb2;
		if (this.numTxtCursor) {
			final int n = n2 = this.numTxt.getSelectionStart();
			sb2 = sb;
			if (n > 0) {
				sb2 = sb.delete(n - 1, n);
				n2 = n;
			}
		} else {
			final int n3 = n2 = this.numTxt.length();
			sb2 = sb;
			if (n3 > 0) {
				sb2 = sb.delete(n3 - 1, n3);
				n2 = n3;
			}
		}
		this.numTxt.setText((CharSequence) sb2.toString());
		if (n2 > 0) {
			Selection.setSelection((Spannable) this.numTxt.getText(), n2 - 1);
		}
		this.len = this.numTxt.getText().toString().trim().length();
		if (this.numTxt.getText().toString().trim().length() <= 0) {
			this.numTxt.setCursorVisible(false);
			this.numTxtCursor = false;
			this.numTxt.setGravity(19);
		}
	}

	private void endCall() {
		final CallManager manager = CallManager.getManager();
		if (this.callTime != null) {
			this.callTime.stop();
			this.callTime = null;
		}
		final int callConvertState = manager.getCallConvertState(this.mCallParams);
		if (callConvertState == 3) {
			this.callTime2.setVisibility(View.INVISIBLE);
			this.connectStateTV.setText(R.string.audio_ending);
			this.showConnectStateView();
		}
		if ((callConvertState == 3 || callConvertState == 2 || callConvertState == 1) && !this.rejFlag) {
			this.rejFlag = true;
			if (this.mCallParams != null && this.mCallParams.getCallType() == CallType.AUDIO) {
				manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
			}
			CallUtil.rejectAudioCall();
		}
		AudioUtil.getInstance().setVolumeControlStream(this);
		if (!this.isIncomingcall) {
			this.setVolumeControlStream(0);
		} else {
			this.setVolumeControlStream(2);
		}
		reSetControlStates();
		this.finish();
	}

	private void findViewsAndSetListener(final View view) {
		CallActivity.userNameTV = (TextView) view.findViewById(R.id.user_name);
		CallActivity.userNumberTV = (TextView) view.findViewById(R.id.user_number);
		if (AntaCallUtil.isAntaCall) {
			if (Receiver.call_state == 2) {
				CallActivity.userNumberTV.setVisibility(View.INVISIBLE);
				String userName;
				if (AntaCallUtil.isIsGroupBroadcast()) {
					userName = this.getResources().getString(R.string.broadcast);
				} else {
					userName = this.getResources().getString(R.string.conference);
				}
				CallActivity.userName = userName;
			} else {
				final StringBuilder sb = new StringBuilder(String.valueOf(CallActivity.userName));
				String string;
				if (AntaCallUtil.isAntaCall) {
					string = this.getResources().getString(R.string.con_bro);
				} else {
					string = "";
				}
				CallActivity.userName = sb.append(string).toString();
			}
			((ImageView) this.findViewById(R.id.user_photo)).setImageResource(R.drawable.picture_unknown_anta);
		}
		if (TextUtils.isEmpty((CharSequence) CallActivity.userNum)) {
			CallActivity.userNameTV.setText((CharSequence) "");
			CallActivity.userNumberTV.setText((CharSequence) "");
		} else {
			CallActivity.userNameTV.setText((CharSequence) CallActivity.userName);
			CallActivity.userNumberTV.setText((CharSequence) CallActivity.userNum);
			if (CallActivity.userNum != null && CallActivity.userNum.equals(CallActivity.userName)) {
				CallActivity.userNumberTV.setVisibility(View.INVISIBLE);
			}
		}
		(this.userPhotoIV = (ImageView) view.findViewById(R.id.user_photo)).setOnClickListener((View.OnClickListener) this);
		this.connectStateTV = (TextView) view.findViewById(R.id.connect_state);
		this.setText4ConnectStateView(Receiver.call_state);
		(this.endCallBT = (ImageView) this.findViewById(R.id.end_call)).setOnClickListener((View.OnClickListener) this);
		(this.endCallBT2 = (ImageView) this.findViewById(R.id.end_call2)).setOnClickListener((View.OnClickListener) this);
		this.endCallTT = (TextView) this.findViewById(R.id.end_call2_text);
		(this.keyboardShowBT = (ImageView) this.findViewById(R.id.keyboard_show)).setOnClickListener((View.OnClickListener) this);
		(this.keyboardHideBT = (ImageView) this.findViewById(R.id.keyboard_hide)).setOnClickListener((View.OnClickListener) this);
		(CallActivity.loudspeakerOnBT = (RelativeLayout) this.findViewById(R.id.loudspeaker_on)).setOnClickListener((View.OnClickListener) this);
		(CallActivity.loudspeakerOffBT = (RelativeLayout) this.findViewById(R.id.loudspeaker_off)).setOnClickListener((View.OnClickListener) this);
		(this.forbidSoundOutOnBT = (RelativeLayout) this.findViewById(R.id.forbid_sound_out_on)).setOnClickListener((View.OnClickListener) this);
		(this.forbidSoundOutOffBT = (RelativeLayout) this.findViewById(R.id.forbid_sound_out_off)).setOnClickListener((View.OnClickListener) this);
		this.incomeControlView = this.findViewById(R.id.income_control_layout);
		if (Receiver.call_state == 1) {
			this.acceptIncomingCall = (ImageView) this.findViewById(R.id.accept_call);
			this.incomeControlView.setVisibility(View.VISIBLE);
			this.acceptIncomingCall.setOnClickListener((View.OnClickListener) this);
		}
		(this.keyBoard = this.findViewById(R.id.keyboard_layout)).setVisibility(View.INVISIBLE);
		this.keyBoard2 = this.findViewById(R.id.keyboard_layout_1);
		this.keyBoard.setVisibility(View.INVISIBLE);
		(this.lineKeyboard = this.findViewById(R.id.line_keyboard)).setVisibility(View.INVISIBLE);
		(this.lineLoudspeaker = this.findViewById(R.id.line_loudspeaker)).setVisibility(View.INVISIBLE);
		(this.lineForbidSoundOut = this.findViewById(R.id.line_forbid_sound_out)).setVisibility(View.INVISIBLE);
		this.initKeyBoard();
		(this.screenOffView = this.findViewById(R.id.screen_off_view)).setOnClickListener((View.OnClickListener) this);
		this.incomeControlView = this.findViewById(R.id.income_control_layout);
		this.acceptIncomingCall = (ImageView) this.findViewById(R.id.accept_call);
		this.setControlViewsVisible();
	}

	private String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(java.lang.System.currentTimeMillis() - SipdroidEngine.serverTimeVal);
		} catch (Exception ex) {
			return null;
		}
	}

	private String getMsgId(final Context context) {
		final StringBuilder sb = new StringBuilder();
		sb.append("00000000");
		sb.append(String.valueOf((java.lang.System.currentTimeMillis() - SipdroidEngine.serverTimeVal) / 1000L));
		sb.append(Tools.getRandomCharNum(14));
		return sb.toString();
	}

	private void initKeyBoard() {
		(this.numTxt = (EditText) this.findViewById(R.id.p_digits)).setText((CharSequence) "");
		this.numTxt.setCursorVisible(false);
		this.numTxtCursor = false;
		this.numTxt.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CallActivity.this.numTxt.setGravity(17);
				CallActivity.this.numTxt.setCursorVisible(true);
//				CallActivity.access .16 (CallActivity.this, true);
			}
		});
		this.numTxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
		this.numTxt.setDrawingCacheEnabled(true);
		(this.btnjing = (ImageButton) this.mRootView.findViewById(R.id.pjing)).setOnClickListener((View.OnClickListener) this);
		(this.btnone = (ImageButton) this.mRootView.findViewById(R.id.pone)).setOnClickListener((View.OnClickListener) this);
		(this.btntwo = (ImageButton) this.mRootView.findViewById(R.id.ptwo)).setOnClickListener((View.OnClickListener) this);
		(this.btnthree = (ImageButton) this.mRootView.findViewById(R.id.pthree)).setOnClickListener((View.OnClickListener) this);
		(this.btnfour = (ImageButton) this.mRootView.findViewById(R.id.pfour)).setOnClickListener((View.OnClickListener) this);
		(this.btnfive = (ImageButton) this.mRootView.findViewById(R.id.pfive)).setOnClickListener((View.OnClickListener) this);
		(this.btnsix = (ImageButton) this.mRootView.findViewById(R.id.psix)).setOnClickListener((View.OnClickListener) this);
		(this.btnseven = (ImageButton) this.mRootView.findViewById(R.id.pseven)).setOnClickListener((View.OnClickListener) this);
		(this.btnenight = (ImageButton) this.mRootView.findViewById(R.id.penight)).setOnClickListener((View.OnClickListener) this);
		(this.btnnine = (ImageButton) this.mRootView.findViewById(R.id.pnine)).setOnClickListener((View.OnClickListener) this);
		(this.btn0 = (ImageButton) this.mRootView.findViewById(R.id.p0)).setOnClickListener((View.OnClickListener) this);
		(this.btnmi = (ImageButton) this.mRootView.findViewById(R.id.pmi)).setOnClickListener((View.OnClickListener) this);
		(this.btndel = (ImageButton) this.mRootView.findViewById(R.id.pdel)).setOnClickListener((View.OnClickListener) this);
		this.btndel.setOnLongClickListener((View.OnLongClickListener) this);
		this.InitTones();
	}

	protected static void reSetControlStates() {
		CallActivity.isKeyBoardShow = false;
		CallActivity.isLoudspeakerOn = false;
		CallActivity.isMuteOn = false;
		AntaCallUtil.reInit();
		CallUtil.mCallBeginTime = 0L;
	}

	private void releaseToneGenerator() {
		if (this.mToneGenerator == null) {
			return;
		}
		try {
			this.mToneGenerator.release();
		} catch (Exception ex) {
			if (ex != null) {
				ex.printStackTrace();
			}
		} finally {
			this.mToneGenerator = null;
		}
	}

	public static void resetCallParams() {
		reSetControlStates();
	}

	private void sendMessage(final String s, final String s2) {
		final String sendTextMessage = Receiver.GetCurUA().SendTextMessage(s, s2, this.getMsgId(CallActivity.mContext));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(CallActivity.mContext);
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
		smsMmsDatabase.insert("new_message_talk", contentValues);
		LogUtil.makeLog("CallActivity", "--++>>sendMessage()->body:" + s2);
	}

	private void setControlViewsVisible() {
		switch (CallManager.getManager().getCallState(this.mCallParams).convert()) {
			default: {
				this.incomeControlView.setVisibility(View.GONE);
				final ImageView keyboardShowBT = this.keyboardShowBT;
				int visibility;
				if (CallActivity.isKeyBoardShow) {
					visibility = 4;
				} else {
					visibility = 0;
				}
				keyboardShowBT.setVisibility(visibility);
				final ImageView keyboardHideBT = this.keyboardHideBT;
				int visibility2;
				if (CallActivity.isKeyBoardShow) {
					visibility2 = 0;
				} else {
					visibility2 = 4;
				}
				keyboardHideBT.setVisibility(visibility2);
				final RelativeLayout loudspeakerOnBT = CallActivity.loudspeakerOnBT;
				int visibility3;
				if (CallActivity.isLoudspeakerOn) {
					visibility3 = 0;
				} else {
					visibility3 = 4;
				}
				loudspeakerOnBT.setVisibility(visibility3);
				final RelativeLayout loudspeakerOffBT = CallActivity.loudspeakerOffBT;
				int visibility4;
				if (CallActivity.isLoudspeakerOn) {
					visibility4 = 4;
				} else {
					visibility4 = 0;
				}
				loudspeakerOffBT.setVisibility(visibility4);
				final RelativeLayout forbidSoundOutOnBT = this.forbidSoundOutOnBT;
				int visibility5;
				if (CallActivity.isMuteOn) {
					visibility5 = 0;
				} else {
					visibility5 = 4;
				}
				forbidSoundOutOnBT.setVisibility(visibility5);
				final RelativeLayout forbidSoundOutOffBT = this.forbidSoundOutOffBT;
				int visibility6;
				if (CallActivity.isMuteOn) {
					visibility6 = 4;
				} else {
					visibility6 = 0;
				}
				forbidSoundOutOffBT.setVisibility(visibility6);
				final View keyBoard = this.keyBoard;
				int visibility7;
				if (CallActivity.isKeyBoardShow) {
					visibility7 = 0;
				} else {
					visibility7 = 4;
				}
				keyBoard.setVisibility(visibility7);
				if (!this.isAcceptSelf) {
					this.closeRinging();
				}
				this.keyBoard2.setVisibility(View.VISIBLE);
				this.endCallTT.setText(R.string.declineCall);
				if (this.tableShow != null) {
					this.tableShow.setVisibility(View.VISIBLE);
				}
				this.callTime2.setVisibility(View.VISIBLE);
				if (this.callTime != null) {
					MyLog.d("videoTrace", "CallActivity#onResume() enter gone call time view");
					this.callTime.setVisibility(View.GONE);
					break;
				}
				break;
			}
			case 1: {
				this.incomeControlView.setVisibility(View.VISIBLE);
				(this.acceptIncomingCall = (ImageView) this.findViewById(R.id.accept_call)).setOnClickListener((View.OnClickListener) this);
				this.ringA.setVisibility(View.VISIBLE);
				this.photo.setVisibility(View.INVISIBLE);
				new Thread(new Runnable() {
					@Override
					public void run() {
						int arg1 = 0;
						while (CallActivity.this.isRinging) {
							Label_0037_Outer:
							while (true) {
								while (true) {
									while (true) {
										try {
											Thread.sleep(200L);
											final Message obtain = Message.obtain();
											obtain.arg1 = arg1;
											if (arg1 < 2) {
												++arg1;
												CallActivity.this.handler2.sendMessage(obtain);
												break;
											}
										} catch (InterruptedException ex) {
											ex.printStackTrace();
											continue Label_0037_Outer;
										}
										break;
									}
									arg1 = 0;
									continue;
								}
							}
						}
					}
				}).start();
				break;
			}
			case 2: {
				(this.tableShow = (LinearLayout) this.findViewById(R.id.table_show)).setVisibility(View.GONE);
				this.incomeControlView.setVisibility(View.GONE);
				this.keyboardShowBT.setVisibility(View.INVISIBLE);
				this.endCallTT.setText(R.string.cancel);
				this.keyboardHideBT.setVisibility(View.INVISIBLE);
				CallActivity.loudspeakerOnBT.setVisibility(View.INVISIBLE);
				CallActivity.loudspeakerOffBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
				break;
			}
		}
		if (AntaCallUtil.isAntaCall) {
			this.keyboardShowBT.setVisibility(View.INVISIBLE);
			this.keyboardHideBT.setVisibility(View.INVISIBLE);
		}
		if (ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			CallActivity.loudspeakerOnBT.setVisibility(View.GONE);
			CallActivity.loudspeakerOffBT.setVisibility(View.GONE);
		}
	}

	public static void setSpeakerPhoneOFF() {
		if (CallUtil.isInCall() && CallActivity.isLoudspeakerOn) {
			Context context;
			if (CallActivity.mContext == null) {
				context = SipUAApp.mContext;
			} else {
				context = CallActivity.mContext;
			}
			Receiver.engine(context);
			((AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(false);
			CallActivity.isLoudspeakerOn = false;
			if (CallActivity.mContext != null) {
				CallActivity.loudspeakerOffBT.setVisibility(View.VISIBLE);
				CallActivity.loudspeakerOnBT.setVisibility(View.INVISIBLE);
			}
		}
	}

	public static void setSpeakerPhoneON() {
		CallActivity.isLoudspeakerOn = false;
		if (CallActivity.mContext != null) {
			CallActivity.loudspeakerOffBT.setVisibility(View.VISIBLE);
			CallActivity.loudspeakerOnBT.setVisibility(View.INVISIBLE);
		}
	}

	private void setText4ConnectStateView(final int n) {
		this.hideConnectStateView(0);
		switch (n) {
			default: {
			}
			case 4: {
				this.connectStateTV.setText(R.string.audio_hold);
				this.showConnectStateView();
			}
			case 0: {
				this.connectStateTV.setText(R.string.audio_ending);
				this.showConnectStateView();
			}
			case 3: {
				this.connectStateTV.setText(R.string.audio_incom);
				this.showConnectStateView();
				this.hideConnectStateView(2000);
			}
			case 1: {
				this.connectStateTV.setText(R.string.audio_incoming);
				this.showConnectStateView();
			}
			case 2: {
				this.connectStateTV.setText(R.string.audio_calling);
				this.showConnectStateView();
			}
		}
	}

	private void toVibrate() {
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

	public void downKey(final String s) {
		this.numTxt.setGravity(17);
		if (this.numTxtCursor) {
			final int selectionStart = this.numTxt.getSelectionStart();
			if (selectionStart > 999) {
				return;
			}
			this.numTxt.setText((CharSequence) new StringBuffer(this.numTxt.getText().toString().trim()).insert(selectionStart, s).toString());
			Selection.setSelection((Spannable) this.numTxt.getText(), selectionStart + 1);
		} else {
			this.numTxt.setText((CharSequence) (String.valueOf(this.numTxt.getText().toString().trim()) + s));
		}
		this.toVibrate();
	}

	public void handle() {
		this.endCall();
	}

	void hideConnectStateView(final int n) {
		if (n != 0) {
			final Message obtainMessage = this.handler.obtainMessage();
			obtainMessage.what = 0;
			this.handler.sendMessageDelayed(obtainMessage, (long) n);
			return;
		}
		if (Build.MODEL.toLowerCase().contains("fh688")) {
			this.connectStateTV.setVisibility(View.GONE);
			return;
		}
		this.connectStateTV.setVisibility(View.INVISIBLE);
	}

	protected void hideControlDisplayView() {
		this.controlOverLayView.setVisibility(View.GONE);
	}

	public void onClick(final View view) {
		MyLog.d("videoTrace", "CallActivity#onClick() enter");
		final CallManager manager = CallManager.getManager();
		final VideoManagerService default1 = VideoManagerService.getDefault();
		if (Receiver.isCallNotificationNeedClose()) {
			reSetControlStates();
			this.finish();
		}
		switch (view.getId()) {
			case R.id.end_call: {
				if (this.callTime != null) {
					this.callTime.stop();
					this.callTime = null;
				}
				final int callConvertState = manager.getCallConvertState(this.mCallParams);
				if (callConvertState == 3) {
					this.callTime2.setVisibility(View.INVISIBLE);
					this.connectStateTV.setText(R.string.audio_ending);
					this.showConnectStateView();
				}
				if ((callConvertState == 3 || callConvertState == 2 || callConvertState == 1) && !this.rejFlag) {
					this.rejFlag = true;
					if (this.mCallParams != null && this.mCallParams.getCallType() == CallType.AUDIO) {
						manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
					}
					CallUtil.rejectAudioCall();
				}
				AudioUtil.getInstance().setVolumeControlStream(this);
				if (!this.isIncomingcall) {
					this.setVolumeControlStream(0);
				} else {
					this.setVolumeControlStream(2);
				}
				reSetControlStates();
				this.finish();
			}
			case R.id.end_call2: {
				if (this.callTime != null) {
					this.callTime.stop();
					this.callTime = null;
				}
				final int callConvertState2 = manager.getCallConvertState(this.mCallParams);
				if (callConvertState2 == 3) {
					this.callTime2.setVisibility(View.INVISIBLE);
					this.connectStateTV.setText(R.string.audio_ending);
					this.showConnectStateView();
				}
				if ((callConvertState2 == 3 || callConvertState2 == 2 || callConvertState2 == 1) && !this.rejFlag) {
					this.rejFlag = true;
					if (this.mCallParams != null && this.mCallParams.getCallType() == CallType.AUDIO) {
						manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
					}
					CallUtil.rejectAudioCall();
				}
				AudioUtil.getInstance().setVolumeControlStream(this);
				if (!this.isIncomingcall) {
					this.setVolumeControlStream(0);
				} else {
					this.setVolumeControlStream(2);
				}
				reSetControlStates();
				this.finish();
			}
			case R.id.accept_call: {
				this.isAcceptSelf = true;
				this.closeRinging();
				this.keyBoard2.setVisibility(View.VISIBLE);
				final ExtendedCall audioInCall = manager.getAudioInCall();
				int n = 0;
				ExtendedCall videoInCall = audioInCall;
				if (default1.existRemoteVideoControl()) {
					final boolean videoCall = default1.getRemoteVideoControlParamter().isVideoCall();
					videoInCall = audioInCall;
					if ((n = (videoCall ? 1 : 0)) != 0) {
						videoInCall = manager.getVideoInCall();
						n = (videoCall ? 1 : 0);
					}
				}
				if (UserAgent.isTempGrpCallMode) {
					Receiver.GetCurUA().hangupTmpGrpCall(true);
					Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
				}
				if (videoInCall != null || n != 0) {
					manager.setUserAgentAudioCall(videoInCall);
					manager.addOnRejectCallCompletedListener((CallManager.OnRejectCallCompletedListener) new OnRejectCallCompletedListener() {
						@Override
						public void onCompledted(final Call call) {
							if (CallActivity.this.mCallParams != null && CallActivity.this.mCallParams.getCallType() == CallType.AUDIO) {
								manager.setUserAgentAudioCall(manager.getCall(CallActivity.this.mCallParams.getCallType(), CallActivity.this.mCallParams.getCallId()));
							}
							CallUtil.answerCall();
							AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
							if (!CallActivity.this.isIncomingcall) {
								CallActivity.this.setVolumeControlStream(0);
							} else {
								CallActivity.this.setVolumeControlStream(2);
							}
							CallActivity.this.incomeControlView.setVisibility(View.GONE);
							manager.removeOnRejectCallCompletedListener((CallManager.OnRejectCallCompletedListener) this);
							CallActivity.this.setText4ConnectStateView(CallState.INCALL.convert());
						}
					});
					CallUtil.rejectAudioCall();
					return;
				}
				if (this.mCallParams != null && this.mCallParams.getCallType() == CallType.AUDIO) {
					manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
				}
				CallUtil.answerCall();
				AudioUtil.getInstance().setVolumeControlStream(this);
				if (!this.isIncomingcall) {
					this.setVolumeControlStream(0);
				} else {
					this.setVolumeControlStream(2);
				}
				this.incomeControlView.setVisibility(View.GONE);
				this.setText4ConnectStateView(CallState.INCALL.convert());
			}
			case R.id.sms_answer: {
				this.showMessagePopWindow(view);
			}
			case R.id.keyboard_show: {
				this.keyBoard.setVisibility(View.VISIBLE);
				this.keyBoard2.setVisibility(View.INVISIBLE);
				this.keyboardShowBT.setVisibility(View.INVISIBLE);
				this.keyboardHideBT.setVisibility(View.VISIBLE);
				CallActivity.isKeyBoardShow = true;
			}
			case R.id.keyboard_hide: {
				this.keyBoard.setVisibility(View.INVISIBLE);
				this.keyBoard2.setVisibility(View.VISIBLE);
				this.keyboardHideBT.setVisibility(View.INVISIBLE);
				this.keyboardShowBT.setVisibility(View.VISIBLE);
				CallActivity.isKeyBoardShow = false;
			}
			case R.id.loudspeaker_on: {
				if (Receiver.call_state == 3) {
					CallActivity.loudspeakerOnBT.setVisibility(View.INVISIBLE);
					CallActivity.loudspeakerOffBT.setVisibility(View.VISIBLE);
					((AudioManager) this.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(false);
					CallActivity.isLoudspeakerOn = false;
					AudioUtil.getInstance().setVolumeControlStream(this);
					return;
				}
				break;
			}
			case R.id.loudspeaker_off: {
				if (Receiver.call_state == 3) {
					CallActivity.loudspeakerOffBT.setVisibility(View.INVISIBLE);
					CallActivity.loudspeakerOnBT.setVisibility(View.VISIBLE);
					final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
					audioManager.setSpeakerphoneOn(true);
					if (Build.MODEL.toLowerCase().contains("lter")) {
						audioManager.setMode(0);
					}
					CallActivity.isLoudspeakerOn = true;
					AudioUtil.getInstance().setVolumeControlStream(this);
					return;
				}
				break;
			}
			case R.id.forbid_sound_out_on: {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Receiver.engine(CallActivity.mContext).togglemute();
					}
				}).start();
				this.forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOffBT.setVisibility(View.VISIBLE);
				CallActivity.isMuteOn = false;
			}
			case R.id.forbid_sound_out_off: {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Receiver.engine(CallActivity.mContext).togglemute();
					}
				}).start();
				this.forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOnBT.setVisibility(View.VISIBLE);
				CallActivity.isMuteOn = true;
			}
			case R.id.pone: {
				this.downKey("1");
				this.playTone('1');
			}
			case R.id.ptwo: {
				this.downKey("2");
				this.playTone('2');
			}
			case R.id.pthree: {
				this.downKey("3");
				this.playTone('3');
			}
			case R.id.pfour: {
				this.downKey("4");
				this.playTone('4');
			}
			case R.id.pfive: {
				this.downKey("5");
				this.playTone('5');
			}
			case R.id.psix: {
				this.downKey("6");
				this.playTone('6');
			}
			case R.id.pseven: {
				this.downKey("7");
				this.playTone('7');
			}
			case R.id.penight: {
				this.downKey("8");
				this.playTone('8');
			}
			case R.id.pnine: {
				this.downKey("9");
				this.playTone('9');
			}
			case R.id.p0: {
				this.downKey("0");
				this.playTone('0');
			}
			case R.id.pmi: {
				this.downKey("*");
				this.playTone('*');
			}
			case R.id.pjing: {
				this.downKey("#");
				this.playTone('#');
			}
			case R.id.pdel: {
				this.delete();
				this.playTone('d');
			}
		}
	}

	public void onCompledted(final Call call) {
		this.endCall();
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		MyLog.d("testcrash", "CallActivity#onCreate() enter");
		super.onCreate(bundle);
		((CallActivity) (CallActivity.mContext = (Context) this)).requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen("CallActivity");
		MyWindowManager.getInstance().disableKeyguard(this);
		final Intent intent = this.getIntent();
		this.mCallParams = CallManager.getCallParams(intent);
		this.mCallername = this.mCallParams.getUsername();
		if (TextUtils.isEmpty((CharSequence) this.mCallParams.getCallId())) {
			this.mCallParams = CallManager.getAudioCallParams();
		}
		MyLog.d("videoTrace", "CallActivity#onCreate() enter caller = " + this.mCallername);
		if (!(this.isIncomingcall = intent.getBooleanExtra("isCallingIn", true))) {
			this.setVolumeControlStream(0);
		} else {
			this.setVolumeControlStream(2);
		}
		CallActivity.userNum = CallUtil.mNumber;
		CallActivity.userName = CallUtil.mName;
		this.setContentView(this.mRootView = this.getLayoutInflater().inflate(R.layout.call_out_ui, (ViewGroup) null));
		this.image1 = (ImageView) this.findViewById(R.id.image1);
		this.image2 = (ImageView) this.findViewById(R.id.image2);
		this.image3 = (ImageView) this.findViewById(R.id.image3);
		this.photo = (FrameLayout) this.findViewById(R.id.photoUser);
		this.ringA = (FrameLayout) this.findViewById(R.id.callOut);
		this.callTime = (Chronometer) this.findViewById(R.id.call_time);
		this.callTime2 = (TextView) this.findViewById(R.id.call_time2);
		if (Build.MODEL.toLowerCase().contains("fh688")) {
			this.callTime2.setTextSize(30.0f);
		}
		this.findViewsAndSetListener(this.mRootView);
		(this.yincang = this.mRootView.findViewById(R.id.yincang)).setVisibility(View.GONE);
		(this.smsAnswer = (ImageView) this.findViewById(R.id.sms_answer)).setOnClickListener((View.OnClickListener) this);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CallActivity.ACTION_CHANGE_CALL_STATE);
		intentFilter.addAction("com.zed3.sipua.ui_callscreen_finish");
		intentFilter.addAction("stream changed");
		intentFilter.addAction("speakerphone changed");
		this.registerReceiver(this.receiver, intentFilter);
		this.callTime.setOnChronometerTickListener((Chronometer.OnChronometerTickListener) new Chronometer.OnChronometerTickListener() {
			public void onChronometerTick(final Chronometer chronometer) {
				final int n = 0 + CallActivity.this.num % 60;
				final int n2 = 0 + (CallActivity.this.num - n) / 60 % 60;
				final int n3 = 0 + (CallActivity.this.num - n - n2 * 60) / 3600;
				String s;
				if (n < 10) {
					s = "0" + n;
				} else {
					s = new StringBuilder().append(n).toString();
				}
				String s2;
				if (n2 < 10) {
					s2 = "0" + n2;
				} else {
					s2 = new StringBuilder().append(n2).toString();
				}
				String s3;
				if (n3 < 10) {
					s3 = "0" + n3;
				} else {
					s3 = new StringBuilder().append(n3).toString();
				}
				CallActivity.this.callTime2.setText((CharSequence) (String.valueOf(s3) + ":" + s2 + ":" + s));
//				final CallActivity this .0 = CallActivity.this;
//				CallActivity.access .4 (this .0, this .0.num + 1);
			}
		});
		this.callTime.start();
		long n = 0L;
		final long currentTimeMillis = java.lang.System.currentTimeMillis();
		if (CallUtil.mCallBeginTime == 0L) {
			CallUtil.mCallBeginTime = currentTimeMillis;
		} else {
			n = currentTimeMillis - CallUtil.mCallBeginTime;
		}
		this.callTime.setBase(SystemClock.elapsedRealtime() - n);
		MyLog.d("testcrash", "CallActivity#onCreate() exit");
	}

	protected void onDestroy() {
		MyLog.d("testcrash", "CallActivity#onDestroy() enter");
		this.unregisterReceiver(this.receiver);
		MyLog.d("videoTrace", "CallActivity#onDestroy()  caller = " + this.mCallername);
		if (this.mCallParams != null) {
			CallManager.getManager().removeOnRejectCallCompletedListener(this.mCallParams.getCallId());
			this.mCallParams.recycle();
			this.mCallParams = null;
		}
		CallUtil.isDestory = true;
		this.running = false;
		this.releaseToneGenerator();
		MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		CallActivity.mContext = null;
		super.onDestroy();
		MyLog.d("testcrash", "CallActivity#onDestroy() exit");
	}

	public boolean onLongClick(final View view) {
		switch (view.getId()) {
			case R.id.pdel: {
				this.numTxt.setText((CharSequence) "");
				break;
			}
		}
		return false;
	}

	protected void onPause() {
		LogUtil.makeLog(" CallActivity ", " onPause() ");
		this.isRinging = false;
		this.isPause = true;
		super.onPause();
	}

	protected void onResume() {
		MyLog.d("videoTrace", "CallActivity#onResume() enter caller = " + this.mCallername);
		if (this.isPause || CallUtil.isDestory) {
			this.num = (int) (java.lang.System.currentTimeMillis() - CallUtil.mCallBeginTime2) / 1000 + 1;
			this.isPause = false;
			CallUtil.isDestory = false;
		}
		this.isRinging = true;
		this.screenOffView.setVisibility(View.GONE);
		Receiver.engine((Context) this);
		super.onResume();
		this.setControlViewsVisible();
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			CallActivity.loudspeakerOnBT.setVisibility(View.GONE);
			CallActivity.loudspeakerOffBT.setVisibility(View.GONE);
		}
		AudioUtil.getInstance().setVolumeControlStream(this);
		if (!this.isIncomingcall) {
			this.setVolumeControlStream(0);
		} else {
			this.setVolumeControlStream(2);
		}
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams.getCallType(), this.mCallParams.getCallId());
		if (this.t == null && callState.convert() != 0) {
			this.numTxt.setText((CharSequence) "");
			this.running = true;
			(this.t = new Thread() {
				@Override
				public void run() {
				}
			}).start();
		}
		MyLog.d("videoTrace", "CallActivity#onResume() exit");
		if (Receiver.isCallNotificationNeedClose()) {
			MyLog.d("videoTrace", "CallActivity#onResume() finsh");
			reSetControlStates();
			this.finish();
		}
	}

	protected void onSaveInstanceState(final Bundle bundle) {
		bundle.putInt(this.CALL_STATE, CallActivity.mState);
		super.onSaveInstanceState(bundle);
	}

	public void onStart() {
		super.onStart();
		CallActivity.pactive = false;
	}

	public void onStop() {
		super.onStop();
		MyLog.d("videoTrace", "CallActivity#onStop() enter caller = " + this.mCallername);
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		if (callState.convert() == 0) {
			MyLog.d("videoTrace", "CallActivity#onStop() enter finish call state = IDLE , username = " + this.mCallername);
			this.finish();
		}
		if (callState == CallState.INCOMING || callState == CallState.OUTGOING) {
			CallManager.getManager().addOnRejectCallCompletedListener(this.mCallParams.getCallId(), (CallManager.OnRejectCallCompletedListener) this);
		}
	}

	void playTone(final Character c) {
		if (this.mDTMFToneEnabled) {
			final int ringerMode = ((AudioManager) this.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
			if (ringerMode != 0 && ringerMode != 1) {
				synchronized (this.mToneGeneratorLock) {
					if (this.mToneGenerator == null) {
						Log.w("tagdd", "playTone: mToneGenerator == null, tone: " + c);
						return;
					}
				}
//				final Throwable t;
//				this.mToneGenerator.startTone((int) CallActivity.mToneMap.get(t), 150);
			}
		}
	}

	protected void retstartCallTime() {
		this.callTime.stop();
		this.callTime = null;
		this.callTime = (Chronometer) this.findViewById(R.id.call_time);
		if (this.callTime != null) {
			this.callTime.start();
		}
	}

	void screenOff(final boolean b) {
		final ContentResolver contentResolver = this.getContentResolver();
//		if (b) {
//			if (this.oldtimeout == 0) {
//				this.oldtimeout = Settings.System.getInt(contentResolver, "screen_off_timeout", 60000);
//				Settings.System.putInt(contentResolver, "screen_off_timeout", 12000);
//			}
//		} else {
//			if (this.oldtimeout == 0 && Settings.System.getInt(contentResolver, "screen_off_timeout", 60000) == 12000) {
//				this.oldtimeout = 60000;
//			}
//			if (this.oldtimeout != 0) {
//				Settings.System.putInt(contentResolver, "screen_off_timeout", this.oldtimeout);
//				this.oldtimeout = 0;
//			}
//		}
	}

	void setScreenBacklight(final float screenBrightness) {
		final WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
		attributes.screenBrightness = screenBrightness;
		this.getWindow().setAttributes(attributes);
	}

	void showConnectStateView() {
		this.connectStateTV.setVisibility(View.VISIBLE);
	}

	void showMessagePopWindow(final View view) {
		final CallManager manager = CallManager.getManager();
		VideoManagerService.getDefault();
		if (this.popview == null) {
			this.messageView = View.inflate(CallActivity.mContext, R.layout.message_list, (ViewGroup) null);
			this.messageList = (ListView) this.messageView.findViewById(R.id.messagelistview);
			this.strings = DataBaseService.getInstance().getAllMessages();
			this.messageList.setAdapter((ListAdapter) new MessageListAdapter(CallActivity.mContext, this.strings));
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
				if (CallActivity.this.callTime != null) {
					CallActivity.this.callTime.stop();
					CallActivity.this.callTime = null;
				}
				final int callConvertState = manager.getCallConvertState(CallActivity.this.mCallParams);
				if (callConvertState == 3) {
					CallActivity.this.callTime2.setVisibility(View.INVISIBLE);
					CallActivity.this.connectStateTV.setText(R.string.audio_ending);
					CallActivity.this.showConnectStateView();
				}
				if ((callConvertState == 3 || callConvertState == 2 || callConvertState == 1) && !CallActivity.this.rejFlag) {
//					CallActivity.access .11 (CallActivity.this, true);
					if (CallActivity.this.mCallParams != null && CallActivity.this.mCallParams.getCallType() == CallType.AUDIO) {
						manager.setUserAgentAudioCall(manager.getCall(CallActivity.this.mCallParams.getCallType(), CallActivity.this.mCallParams.getCallId()));
					}
					CallUtil.rejectAudioCall();
				}
				AudioUtil.getInstance().setVolumeControlStream(CallActivity.this);
				if (!CallActivity.this.isIncomingcall) {
					CallActivity.this.setVolumeControlStream(0);
				} else {
					CallActivity.this.setVolumeControlStream(2);
				}
				new Thread() {
					@Override
					public void run() {
						CallActivity.this.sendMessage(CallUtil.mNumber, CallActivity.this.strings.get(n));
					}
				}.start();
				CallActivity.reSetControlStates();
				CallActivity.this.dismissPop();
				CallActivity.this.finish();
			}
		});
		this.isshowing = true;
	}
}
