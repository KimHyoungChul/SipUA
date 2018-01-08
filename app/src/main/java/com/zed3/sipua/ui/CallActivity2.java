package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.log.MyLog;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.CallManager.CallType;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.anta.AntaCallActivity2;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.anta.InviteContactActivity;
import com.zed3.sipua.ui.anta.Linkman;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.video.VideoManagerService;
import com.zed3.window.MyWindowManager;

import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.ExtendedCall;

import java.util.ArrayList;
import java.util.HashMap;

public class CallActivity2 extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, SensorEventListener {
	public static String ACTION_CHANGE_CALL_STATE;
	protected static final int HIDECONNECTVIEW = 0;
	public static String NEWSTATE;
	static final float PROXIMITY_THRESHOLD = 5.0f;
	private static final String TAG = "CallActivity2";
	private static final int TONE_LENGTH_MS = 150;
	private static TextView connectStateTV;
	static boolean isKeyBoardShow = false;
	static boolean isLoudspeakerOn = false;
	static boolean isMuteOn = false;
	private static ImageView loudspeakerOffBT;
	private static ImageView loudspeakerOnBT;
	private static Context mContext;
	public static boolean mIsIncomeCall = false;
	public static boolean mIsMemberFrameShowed = false;
	public static int mState = 0;
	public static final HashMap<Character, Integer> mToneMap;
	private static final int maxInputNum = 1000;
	public static boolean pactive;
	public static String userName;
	private static TextView userNameTV;
	public static String userNum;
	private static TextView userNumberTV;
	private String CALL_STATE;
	final int SCREEN_OFF_TIMEOUT;
	private ImageView acceptIncomingCall;
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
	public Chronometer callTimeBig;
	private View controlOverLayView;
	private ImageView endCallBT;
	private ImageView forbidSoundOutOffBT;
	private ImageView forbidSoundOutOnBT;
	Handler handler;
	private View incomeControlView;
	private View keyBoard;
	private ImageView keyboardHideBT;
	private ImageView keyboardShowBT;
	private View lineForbidSoundOut;
	private View lineKeyboard;
	private View lineLoudspeaker;
	private MyGridViewAdapter mAdapter_;
	private CallManager.CallParams mCallParams;
	private boolean mDTMFToneEnabled;
	private ImageView mFrameControlIV;
	private ArrayList<Linkman> mGridData;
	private GridView mGridView;
	View mMemberViews;
	private View mRootView;
	private String mScreanWakeLockKey;
	View mTimeViews;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock;
	private EditText numTxt;
	private boolean numTxtCursor;
	int oldtimeout;
	Sensor proximitySensor;
	private BroadcastReceiver receiver;
	boolean running;
	private View screenOffView;
	SensorManager sensorManager;
	Thread t;
	private LinearLayout txtClick;
	private ImageView userPhotoIV;

	static {
		CallActivity2.userNum = "--";
		CallActivity2.userName = "--";
		CallActivity2.ACTION_CHANGE_CALL_STATE = "com.zed3.sipua.ui.CallActivity2.CALL_STATE";
		CallActivity2.NEWSTATE = "callState";
		mToneMap = new HashMap<Character, Integer>();
	}

	public CallActivity2() {
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
		this.receiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				int i = 4;
				Bundle extras = intent.getExtras();
				if (intent.getAction().equals(CallActivity2.ACTION_CHANGE_CALL_STATE)) {
					CallActivity2.mState = extras.getInt(CallActivity2.NEWSTATE);
					switch (CallActivity2.mState) {
						case 0:
							MyLog.d("testcrash", "CallActivity2#onReceive() UA_STATE_IDLE 挂断 ");
							if (CallActivity2.this.callTime != null) {
								CallActivity2.this.callTime.stop();
								CallActivity2.this.callTime = null;
							}
							CallActivity2.this.setText4ConnectStateView(CallActivity2.mState);
							CallActivity2.reSetControlStates();
							CallActivity2.this.finish();
							return;
						case 3:
							if (CallActivity2.this.callTime == null) {
								CallActivity2.this.callTime = (Chronometer) CallActivity2.this.findViewById(R.id.call_time);
								if (CallActivity2.this.callTime != null) {
									CallActivity2.this.callTime.start();
								}
							}
							CallActivity2.this.callTime.setBase(SystemClock.elapsedRealtime());
							CallActivity2.this.callTimeBig.setBase(SystemClock.elapsedRealtime());
//							CallUtil.mCallBeginTime = System.currentTimeMillis();
							if (RtpStreamReceiver_signal.speakermode == 2) {
								CallActivity2.this.setVolumeControlStream(0);
							}
							CallActivity2.this.setText4ConnectStateView(CallActivity2.mState);
							CallActivity2.this.hideConnectStateView(2000);
							CallActivity2.this.setControlViewsVisible();
							Message obtainMessage = CallActivity2.this.handler.obtainMessage();
							obtainMessage.what = 0;
							CallActivity2.this.handler.sendMessageDelayed(obtainMessage, 2000);
							return;
					}
				} else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
					switch (extras.getInt(AudioUtil.KEY_STREAM_INT)) {
						case 0:
							CallActivity2.this.setVolumeControlStream(0);
							return;
						case 3:
							CallActivity2.this.setVolumeControlStream(3);
							return;
						default:
							return;
					}
				} else if (intent.getAction().equals(AudioUtil.ACTION_SPEAKERPHONE_STATE_CHANGED)) {
					int i2;
					CallActivity2.isLoudspeakerOn = AudioUtil.getInstance().isSpeakerphoneOn().booleanValue();
					ImageView access$2 = CallActivity2.loudspeakerOnBT;
					if (CallActivity2.isLoudspeakerOn) {
						i2 = 0;
					} else {
						i2 = 4;
					}
					access$2.setVisibility(i2);
					ImageView access$3 = CallActivity2.loudspeakerOffBT;
					if (!CallActivity2.isLoudspeakerOn) {
						i = 0;
					}
					access$3.setVisibility(i);
				}
			}
		};
		this.handler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						CallActivity2.this.hideConnectStateView(0);
					}
				}
			}
		};
		this.mScreanWakeLockKey = "CallActivity2";
		this.mToneGeneratorLock = new Object();
		this.SCREEN_OFF_TIMEOUT = 12000;
		this.CALL_STATE = "callstate";
	}

	private void InitTones() {
		boolean mdtmfToneEnabled = true;
		CallActivity2.mToneMap.put('1', 1);
		CallActivity2.mToneMap.put('2', 2);
		CallActivity2.mToneMap.put('3', 3);
		CallActivity2.mToneMap.put('4', 4);
		CallActivity2.mToneMap.put('5', 5);
		CallActivity2.mToneMap.put('6', 6);
		CallActivity2.mToneMap.put('7', 7);
		CallActivity2.mToneMap.put('8', 8);
		CallActivity2.mToneMap.put('9', 9);
		CallActivity2.mToneMap.put('0', 0);
		CallActivity2.mToneMap.put('#', 11);
		CallActivity2.mToneMap.put('*', 10);
		CallActivity2.mToneMap.put('d', 12);
		// TODO
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
		if (this.numTxt.getText().toString().trim().length() <= 0) {
			this.numTxt.setCursorVisible(false);
			this.numTxtCursor = false;
			this.numTxt.setGravity(19);
		}
	}

	private void findViewsAndSetListener(final View view) {
		final int convert = CallManager.getManager().getCallState(this.mCallParams).convert();
		CallActivity2.userNameTV = (TextView) view.findViewById(R.id.user_name);
		CallActivity2.userNumberTV = (TextView) view.findViewById(R.id.user_number);
		CallActivity2.userName = String.valueOf(this.getResources().getString(R.string.conferenc_host)) + CallActivity2.userName;
		CallActivity2.userNameTV.setText((CharSequence) CallActivity2.userName);
		CallActivity2.userNumberTV.setText((CharSequence) CallActivity2.userNum);
		if (CallActivity2.userNum != null && CallActivity2.userNum.equals(CallActivity2.userName)) {
			CallActivity2.userNumberTV.setVisibility(View.INVISIBLE);
		}
		(this.userPhotoIV = (ImageView) view.findViewById(R.id.user_photo)).setOnClickListener((View.OnClickListener) this);
		CallActivity2.connectStateTV = (TextView) view.findViewById(R.id.connect_state);
		this.setText4ConnectStateView(convert);
		(this.endCallBT = (ImageView) this.findViewById(R.id.end_call)).setOnClickListener((View.OnClickListener) this);
		(this.keyboardShowBT = (ImageView) this.findViewById(R.id.keyboard_show)).setOnClickListener((View.OnClickListener) this);
		(this.keyboardHideBT = (ImageView) this.findViewById(R.id.keyboard_hide)).setOnClickListener((View.OnClickListener) this);
		(CallActivity2.loudspeakerOnBT = (ImageView) this.findViewById(R.id.loudspeaker_on)).setOnClickListener((View.OnClickListener) this);
		(CallActivity2.loudspeakerOffBT = (ImageView) this.findViewById(R.id.loudspeaker_off)).setOnClickListener((View.OnClickListener) this);
		(this.forbidSoundOutOnBT = (ImageView) this.findViewById(R.id.forbid_sound_out_on)).setOnClickListener((View.OnClickListener) this);
		(this.forbidSoundOutOffBT = (ImageView) this.findViewById(R.id.forbid_sound_out_off)).setOnClickListener((View.OnClickListener) this);
		this.incomeControlView = this.findViewById(R.id.income_control_layout);
		if (convert == 1) {
			this.acceptIncomingCall = (ImageView) this.findViewById(R.id.accept_call);
			this.incomeControlView.setVisibility(View.VISIBLE);
			this.acceptIncomingCall.setOnClickListener((View.OnClickListener) this);
		}
		(this.keyBoard = this.findViewById(R.id.keyboard_layout)).setVisibility(View.INVISIBLE);
		(this.lineKeyboard = this.findViewById(R.id.line_keyboard)).setVisibility(View.INVISIBLE);
		(this.lineLoudspeaker = this.findViewById(R.id.line_loudspeaker)).setVisibility(View.INVISIBLE);
		(this.lineForbidSoundOut = this.findViewById(R.id.line_forbid_sound_out)).setVisibility(View.INVISIBLE);
		this.initKeyBoard();
		(this.screenOffView = this.findViewById(R.id.screen_off_view)).setOnClickListener((View.OnClickListener) this);
		this.setControlViewsVisible();
	}

	private void initKeyBoard() {
		(this.numTxt = (EditText) this.findViewById(R.id.p_digits)).setText((CharSequence) "");
		this.numTxt.setCursorVisible(false);
		this.numTxtCursor = false;
		this.numTxt.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CallActivity2.this.numTxt.setCursorVisible(true);
//				CallActivity2.access .10 (CallActivity2.this, true);
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

	private void initMemberFrameViews() {
		if (CallActivity2.mIsIncomeCall || Receiver.call_state == 1 || PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("autoAnswerKey", false)) {
			this.mGridData = new ArrayList<Linkman>();
			((LinearLayout) this.findViewById(R.id.message_show_control)).setVisibility(View.GONE);
			CallActivity2.mIsIncomeCall = true;
		} else {
			this.mGridData = AntaCallActivity2.mGridData;
		}
		this.mGridView = (GridView) this.findViewById(R.id.grid_selected_member);
		final MyHandler myHandler = new MyHandler();
		final Message obtain = Message.obtain();
		obtain.what = 1;
		myHandler.sendMessage(obtain);
	}

	private void initTimeFrameViews() {
		((TextView) this.findViewById(R.id.create_time_tv)).setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.start_time)) + this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("AntaCallCreateTime", "")));
	}

	protected static void reSetControlStates() {
		MyLog.d("testcrash", "CallActivity2#reSetControlStates() enter ");
		CallActivity2.isKeyBoardShow = false;
		CallActivity2.isLoudspeakerOn = false;
		CallActivity2.isMuteOn = false;
		AntaCallUtil.reInit();
		CallUtil.mCallBeginTime = 0L;
		CallActivity2.mIsIncomeCall = false;
		MyLog.d("testcrash", "CallActivity2#reSetControlStates() exit ");
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

	private void setControlViewsVisible() {
		final boolean b = false;
		switch (CallManager.getManager().getCallState(this.mCallParams).convert()) {
			default: {
				this.incomeControlView.setVisibility(View.INVISIBLE);
				final ImageView keyboardShowBT = this.keyboardShowBT;
				int visibility;
				if (CallActivity2.isKeyBoardShow) {
					visibility = 4;
				} else {
					visibility = 0;
				}
				keyboardShowBT.setVisibility(visibility);
				final ImageView keyboardHideBT = this.keyboardHideBT;
				int visibility2;
				if (CallActivity2.isKeyBoardShow) {
					visibility2 = 0;
				} else {
					visibility2 = 4;
				}
				keyboardHideBT.setVisibility(visibility2);
				final ImageView loudspeakerOnBT = CallActivity2.loudspeakerOnBT;
				int visibility3;
				if (CallActivity2.isLoudspeakerOn) {
					visibility3 = 0;
				} else {
					visibility3 = 4;
				}
				loudspeakerOnBT.setVisibility(visibility3);
				final ImageView loudspeakerOffBT = CallActivity2.loudspeakerOffBT;
				int visibility4;
				if (CallActivity2.isLoudspeakerOn) {
					visibility4 = 4;
				} else {
					visibility4 = 0;
				}
				loudspeakerOffBT.setVisibility(visibility4);
				final ImageView forbidSoundOutOnBT = this.forbidSoundOutOnBT;
				int visibility5;
				if (CallActivity2.isMuteOn) {
					visibility5 = 0;
				} else {
					visibility5 = 4;
				}
				forbidSoundOutOnBT.setVisibility(visibility5);
				final ImageView forbidSoundOutOffBT = this.forbidSoundOutOffBT;
				int visibility6;
				if (CallActivity2.isMuteOn) {
					visibility6 = 4;
				} else {
					visibility6 = 0;
				}
				forbidSoundOutOffBT.setVisibility(visibility6);
				final View keyBoard = this.keyBoard;
				int visibility7;
				if (CallActivity2.isKeyBoardShow) {
					visibility7 = (b ? 1 : 0);
				} else {
					visibility7 = 4;
				}
				keyBoard.setVisibility(visibility7);
				break;
			}
			case 1: {
				this.incomeControlView.setVisibility(View.VISIBLE);
				(this.acceptIncomingCall = (ImageView) this.findViewById(R.id.accept_call)).setOnClickListener((View.OnClickListener) this);
				break;
			}
			case 2: {
				this.keyboardShowBT.setVisibility(View.INVISIBLE);
				this.keyboardHideBT.setVisibility(View.INVISIBLE);
				CallActivity2.loudspeakerOnBT.setVisibility(View.INVISIBLE);
				CallActivity2.loudspeakerOffBT.setVisibility(View.INVISIBLE);
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
			CallActivity2.loudspeakerOnBT.setVisibility(View.GONE);
			CallActivity2.loudspeakerOffBT.setVisibility(View.GONE);
		}
	}

	public static void setSpeakerPhoneOFF() {
		if (CallUtil.isInCall() && CallActivity2.isLoudspeakerOn) {
			Context context;
			if (CallActivity2.mContext == null) {
				context = SipUAApp.mContext;
			} else {
				context = CallActivity2.mContext;
			}
			Receiver.engine(context);
			((AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(false);
			CallActivity2.isLoudspeakerOn = false;
			if (CallActivity2.mContext != null) {
				CallActivity2.loudspeakerOffBT.setVisibility(View.VISIBLE);
				CallActivity2.loudspeakerOnBT.setVisibility(View.INVISIBLE);
			}
		}
	}

	public static void setSpeakerPhoneON() {
		CallActivity2.isLoudspeakerOn = false;
		if (CallActivity2.mContext != null) {
			CallActivity2.loudspeakerOffBT.setVisibility(View.VISIBLE);
			CallActivity2.loudspeakerOnBT.setVisibility(View.INVISIBLE);
		}
	}

	private void setText4ConnectStateView(final int n) {
		this.hideConnectStateView(0);
		switch (n) {
			default: {
			}
			case 4: {
				CallActivity2.connectStateTV.setText(R.string.vedio_hold);
				this.showConnectStateView();
			}
			case 0: {
				CallActivity2.connectStateTV.setText(R.string.audio_ending);
				this.showConnectStateView();
			}
			case 3: {
				CallActivity2.connectStateTV.setText(R.string.audio_incom);
				this.showConnectStateView();
				this.hideConnectStateView(2000);
			}
			case 1: {
				CallActivity2.connectStateTV.setText(R.string.audio_incoming);
				this.showConnectStateView();
			}
			case 2: {
				CallActivity2.connectStateTV.setText(R.string.video_call);
				this.showConnectStateView();
			}
		}
	}

	private void showMemberFrame(final boolean mIsMemberFrameShowed) {
		final int n = 8;
		if (mIsMemberFrameShowed && this.mGridData.size() == 0) {
			return;
		}
		CallActivity2.mIsMemberFrameShowed = mIsMemberFrameShowed;
		final View mMemberViews = this.mMemberViews;
		int visibility;
		if (mIsMemberFrameShowed) {
			visibility = 0;
		} else {
			visibility = 8;
		}
		mMemberViews.setVisibility(visibility);
		final View mTimeViews = this.mTimeViews;
		int visibility2;
		if (mIsMemberFrameShowed) {
			visibility2 = n;
		} else {
			visibility2 = 0;
		}
		mTimeViews.setVisibility(visibility2);
		final ImageView mFrameControlIV = this.mFrameControlIV;
		int backgroundResource;
		if (mIsMemberFrameShowed) {
			backgroundResource = R.drawable.new_down;
		} else {
			backgroundResource = R.drawable.new_up;
		}
		mFrameControlIV.setBackgroundResource(backgroundResource);
	}

	private void toVibrate() {
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

	void hideConnectStateView(final int n) {
		if (n == 0) {
			CallActivity2.connectStateTV.setVisibility(View.GONE);
			return;
		}
		final Message obtainMessage = this.handler.obtainMessage();
		obtainMessage.what = 0;
		this.handler.sendMessageDelayed(obtainMessage, (long) n);
	}

	protected void hideControlDisplayView() {
		this.controlOverLayView.setVisibility(View.GONE);
	}

	public void onAccuracyChanged(final Sensor sensor, final int n) {
	}

	public void onClick(final View view) {
		final CallManager manager = CallManager.getManager();
		final VideoManagerService default1 = VideoManagerService.getDefault();
		final int convert = CallManager.getManager().getCallState(this.mCallParams).convert();
		if (Receiver.isCallNotificationNeedClose()) {
			reSetControlStates();
			this.finish();
		}
		switch (view.getId()) {
			default: {
			}
			case R.id.end_call: {
				if (this.callTime != null) {
					this.callTime.stop();
					this.callTime = null;
				}
				if (convert == 3) {
					CallActivity2.connectStateTV.setText(R.string.call_state_video_endcall);
					this.showConnectStateView();
				}
				if (convert == 3 || convert == 2 || convert == 1) {
					if (this.mCallParams != null && this.mCallParams.getCallType() == CallManager.CallType.AUDIO) {
						manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
					}
					CallUtil.rejectAudioCall();
				}
				this.setVolumeControlStream(0);
				reSetControlStates();
				this.finish();
			}
			case R.id.accept_call: {
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
				if (videoInCall != null || n != 0) {
					manager.setUserAgentAudioCall(videoInCall);
					manager.addOnRejectCallCompletedListener((CallManager.OnRejectCallCompletedListener) new CallManager.OnRejectCallCompletedListener() {
						@Override
						public void onCompledted(final Call call) {
							if (CallActivity2.this.mCallParams != null && CallActivity2.this.mCallParams.getCallType() == CallType.AUDIO) {
								manager.setUserAgentAudioCall(manager.getCall(CallActivity2.this.mCallParams.getCallType(), CallActivity2.this.mCallParams.getCallId()));
							}
							CallUtil.answerCall();
							CallActivity2.this.setVolumeControlStream(0);
							CallActivity2.this.incomeControlView.setVisibility(View.INVISIBLE);
							manager.removeOnRejectCallCompletedListener((CallManager.OnRejectCallCompletedListener) this);
						}
					});
					CallUtil.rejectAudioCall();
				} else {
					if (this.mCallParams != null && this.mCallParams.getCallType() == CallManager.CallType.AUDIO) {
						manager.setUserAgentAudioCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
					}
					CallUtil.answerCall();
					this.setVolumeControlStream(0);
					this.incomeControlView.setVisibility(View.INVISIBLE);
				}
				CallUtil.answerCall();
				this.setVolumeControlStream(0);
				this.incomeControlView.setVisibility(View.INVISIBLE);
			}
			case R.id.keyboard_show: {
				this.keyBoard.setVisibility(View.VISIBLE);
				this.keyboardShowBT.setVisibility(View.INVISIBLE);
				this.keyboardHideBT.setVisibility(View.VISIBLE);
				CallActivity2.isKeyBoardShow = true;
			}
			case R.id.keyboard_hide: {
				this.keyBoard.setVisibility(View.INVISIBLE);
				this.keyboardHideBT.setVisibility(View.INVISIBLE);
				this.keyboardShowBT.setVisibility(View.VISIBLE);
				CallActivity2.isKeyBoardShow = false;
			}
			case R.id.loudspeaker_on: {
				CallActivity2.loudspeakerOnBT.setVisibility(View.INVISIBLE);
				CallActivity2.loudspeakerOffBT.setVisibility(View.VISIBLE);
				((AudioManager) this.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(false);
				this.setVolumeControlStream(0);
				CallActivity2.isLoudspeakerOn = false;
			}
			case R.id.loudspeaker_off: {
				CallActivity2.loudspeakerOffBT.setVisibility(View.INVISIBLE);
				CallActivity2.loudspeakerOnBT.setVisibility(View.VISIBLE);
				((AudioManager) this.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(true);
				this.setVolumeControlStream(0);
				CallActivity2.isLoudspeakerOn = true;
			}
			case R.id.forbid_sound_out_on: {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Receiver.engine(CallActivity2.mContext).togglemute();
					}
				}).start();
				this.forbidSoundOutOnBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOffBT.setVisibility(View.VISIBLE);
				CallActivity2.isMuteOn = false;
			}
			case R.id.forbid_sound_out_off: {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Receiver.engine(CallActivity2.mContext).togglemute();
					}
				}).start();
				this.forbidSoundOutOffBT.setVisibility(View.INVISIBLE);
				this.forbidSoundOutOnBT.setVisibility(View.VISIBLE);
				CallActivity2.isMuteOn = true;
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
			case R.id.textclick: {
				this.showMemberFrame(!CallActivity2.mIsMemberFrameShowed);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		MyLog.d("testcrash", "CallActivity2#onCreate() enter");
		super.onCreate(bundle);
		((CallActivity2) (CallActivity2.mContext = (Context) this)).requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen("CallActivity2");
		MyWindowManager.getInstance().disableKeyguard(this);
		this.mCallParams = CallManager.getCallParams(this.getIntent());
		CallActivity2.userNum = CallUtil.mNumber;
		CallActivity2.userName = CallUtil.mName;
		if (TextUtils.isEmpty((CharSequence) this.mCallParams.getCallId())) {
			this.mCallParams = CallManager.getAudioCallParams();
		}
		this.setContentView(this.mRootView = this.getLayoutInflater().inflate(R.layout.call_out_ui2, (ViewGroup) null));
		this.findViewsAndSetListener(this.mRootView);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CallActivity2.ACTION_CHANGE_CALL_STATE);
		intentFilter.addAction("stream changed");
		intentFilter.addAction("speakerphone changed");
		this.registerReceiver(this.receiver, intentFilter);
		(this.callTime = (Chronometer) this.findViewById(R.id.call_time)).start();
		(this.callTimeBig = (Chronometer) this.findViewById(R.id.call_time_big)).start();
		long n = 0L;
//		final long currentTimeMillis = System.currentTimeMillis();
//		if (CallUtil.mCallBeginTime == 0L) {
//			CallUtil.mCallBeginTime = currentTimeMillis;
//		} else {
//			n = currentTimeMillis - CallUtil.mCallBeginTime;
//		}
		this.callTime.setBase(SystemClock.elapsedRealtime() - n);
		this.callTimeBig.setBase(SystemClock.elapsedRealtime() - n);
		this.mMemberViews = this.findViewById(R.id.membersFrameViews);
		this.mTimeViews = this.findViewById(R.id.TimeFrameViews);
		this.mFrameControlIV = (ImageView) this.findViewById(R.id.frame_control_iv);
		this.initMemberFrameViews();
		this.initTimeFrameViews();
		(this.txtClick = (LinearLayout) this.findViewById(R.id.textclick)).setOnClickListener((View.OnClickListener) this);
		this.showMemberFrame(CallActivity2.mIsMemberFrameShowed);
		MyLog.d("testcrash", "CallActivity2#onCreate() exit");
	}

	protected void onDestroy() {
		MyLog.d("testcrash", "CallActivity2#onDestroy() enter");
		this.releaseToneGenerator();
		MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		LogUtil.makeLog(" CallActivity2 ", " onDestory is ongoing... mContext = null !");
		this.showMemberFrame(false);
		CallActivity2.mContext = null;
		this.unregisterReceiver(this.receiver);
		super.onDestroy();
		MyLog.d("testcrash", "CallActivity2#onDestroy() exit");
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

	protected void onResume() {
		this.screenOffView.setVisibility(View.GONE);
		if (this.sensorManager == null) {
			this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
			this.proximitySensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			this.sensorManager.registerListener((SensorEventListener) this, this.proximitySensor, 3);
		}
		if (this.mGridData == null) {
			this.mGridData = AntaCallActivity2.mGridData;
		}
		if (this.mGridData != null && MeetingMem.inviteContact != null) {
			this.mGridData.addAll(MeetingMem.inviteContact);
			MeetingMem.inviteContact = null;
			final MyHandler myHandler = new MyHandler();
			final Message obtain = Message.obtain();
			obtain.what = 1;
			myHandler.sendMessage(obtain);
			AntaCallActivity2.mGridData = this.mGridData;
		}
		Receiver.engine((Context) this);
		super.onResume();
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		final int convert = callState.convert();
		this.setControlViewsVisible();
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
			CallActivity2.loudspeakerOnBT.setVisibility(View.GONE);
			CallActivity2.loudspeakerOffBT.setVisibility(View.GONE);
		}
		if (Receiver.isCallNotificationNeedClose() && callState == CallManager.CallState.IDLE) {
			reSetControlStates();
			this.finish();
		}
	}

	protected void onSaveInstanceState(final Bundle bundle) {
		bundle.putInt(this.CALL_STATE, CallActivity2.mState);
		super.onSaveInstanceState(bundle);
	}

	public void onSensorChanged(final SensorEvent sensorEvent) {
		int visibility = 0;
		final float n = sensorEvent.values[0];
		final boolean b = CallActivity2.pactive = (n >= 0.0 && n < 5.0f && n < sensorEvent.sensor.getMaximumRange());
		final View screenOffView = this.screenOffView;
		if (!b) {
			visibility = 8;
		}
		screenOffView.setVisibility(visibility);
	}

	public void onStart() {
		super.onStart();
		CallActivity2.pactive = false;
	}

	public void onStop() {
		super.onStop();
		this.running = false;
		if (CallManager.getManager().getCallState(this.mCallParams).convert() == 0) {
			this.finish();
		}
	}

	void playTone(final Character c) {
		// TODO
	}

	protected void retstartCallTime() {
		this.callTime.stop();
		this.callTime = null;
		this.callTime = (Chronometer) this.findViewById(R.id.call_time);
		if (this.callTime != null) {
			this.callTime.start();
		}
		this.callTimeBig.stop();
		this.callTimeBig = null;
		this.callTimeBig = (Chronometer) this.findViewById(R.id.call_time_big);
		if (this.callTimeBig != null) {
			this.callTimeBig.start();
		}
	}

	void screenOff(boolean off) {
		ContentResolver cr = getContentResolver();
		if (this.proximitySensor == null) {
			if (!off) {
				if (this.oldtimeout == 0 && System.getInt(cr, "screen_off_timeout", 60000) == 12000) {
					this.oldtimeout = 60000;
				}
				if (this.oldtimeout != 0) {
					System.putInt(cr, "screen_off_timeout", this.oldtimeout);
					this.oldtimeout = 0;
				}
			} else if (this.oldtimeout == 0) {
				this.oldtimeout = System.getInt(cr, "screen_off_timeout", 60000);
				System.putInt(cr, "screen_off_timeout", 12000);
			}
		}
	}


	void setScreenBacklight(final float screenBrightness) {
		final WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
		attributes.screenBrightness = screenBrightness;
		this.getWindow().setAttributes(attributes);
	}

	void showConnectStateView() {
		CallActivity2.connectStateTV.setVisibility(View.VISIBLE);
	}

	private class CellHolder {
		ImageView img;
		TextView name;
		TextView number;
	}

	private class MyGridViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyGridViewAdapter(final Context context) {
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			if (CallActivity2.this.mGridData == null) {
				return 1;
			}
			return CallActivity2.this.mGridData.size() + 1;
		}

		public Object getItem(final int n) {
			if (n == this.getCount() - 1) {
				final Linkman linkman = new Linkman();
				linkman.name = "";
				linkman.number = "";
				return linkman;
			}
			return CallActivity2.this.mGridData.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View view, final ViewGroup viewGroup) {
			if (n == this.getCount() - 1) {
				CellHolder tag;
				if (view == null) {
					tag = new CellHolder();
					view = this.mInflater.inflate(R.layout.custom_gridview_item, (ViewGroup) null);
					tag.img = (ImageView) view.findViewById(R.id.person_icon);
					tag.name = (TextView) view.findViewById(R.id.custom_name);
					tag.number = (TextView) view.findViewById(R.id.custom_number);
					view.setTag((Object) tag);
				} else {
					tag = (CellHolder) view.getTag();
				}
				tag.name.setText((CharSequence) "");
				tag.number.setText((CharSequence) "");
				tag.img.setImageResource(R.drawable.meeting_invite);
				view.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
					public void onClick(final View view) {
						final Intent intent = new Intent((Context) CallActivity2.this, (Class) InviteContactActivity.class);
						MeetingMem.selectContact = CallActivity2.this.mGridData;
						CallActivity2.this.startActivity(intent);
					}
				});
				return view;
			}
			final Linkman linkman = CallActivity2.this.mGridData.get(n);
			CellHolder tag2;
			if (view == null) {
				tag2 = new CellHolder();
				view = this.mInflater.inflate(R.layout.custom_gridview_item, (ViewGroup) null);
				tag2.img = (ImageView) view.findViewById(R.id.person_icon);
				tag2.name = (TextView) view.findViewById(R.id.custom_name);
				tag2.number = (TextView) view.findViewById(R.id.custom_number);
				view.setTag((Object) tag2);
			} else {
				tag2 = (CellHolder) view.getTag();
			}
			MyLog.i("hDebug", tag2 + "   item:" + linkman);
			tag2.name.setText((CharSequence) linkman.name);
			tag2.number.setText((CharSequence) linkman.number);
			tag2.img.setImageResource(R.drawable.person_icon);
			return view;
		}
	}

	private class MyHandler extends Handler {
		public void handleMessage(final Message message) {
			LogUtil.makeLog(" CallActivity2 ", " MyHandler is ongoning.. mContext = null? is " + (CallActivity2.mContext == null));
			if (CallActivity2.mContext == null) {
				return;
			}
		}
	}
}
