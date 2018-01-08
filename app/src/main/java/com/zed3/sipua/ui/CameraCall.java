package com.zed3.sipua.ui;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.video.utils.VideoDecodeThread;
import com.zed3.audio.AudioModeUtils;
import com.zed3.audio.AudioUtil;
import com.zed3.audio.SpeakerphoneChangeListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.constant.GroupConstant;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.h264_fu_process.RtpStack;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.CallManager.CallParams;
import com.zed3.sipua.CallManager.CallType;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.TimeOutSyncBufferQueue;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.phone.CallerInfo;
import com.zed3.sipua.phone.CallerInfoAsyncQuery.OnQueryCompleteListener;
import com.zed3.sipua.phone.ContactsAsyncHelper.OnImageLoadCompleteListener;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.TempGroupCallUtil;
import com.zed3.sipua.ui.lowsdk.TempGrpCallActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.PhoneSupportTest;
import com.zed3.video.SensorCheckService;
import com.zed3.video.VideoManagerService;
import com.zed3.video.VideoManagerService.EndVideoCallHandler;
import com.zed3.video.VideoParamter;
import com.zed3.video.YUVData;
import com.zed3.window.MyWindowManager;

import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.header.BaseSipHeaders;
import org.zoolu.tools.Parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraCall extends BaseActivity implements OnClickListener, OnQueryCompleteListener, OnImageLoadCompleteListener, EndVideoCallHandler, SpeakerphoneChangeListener {
	private static final String TAG = "CameraCall";
	private static final int TONE_LENGTH_MS = 150;
	private static String cameraval = "";
	public static final HashMap<Character, Integer> mToneMap = new HashMap();
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	private final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	private final String ACTION_SPEAK_ON = "com.ceiw.keyevent";
	private AudioManager AM;
	private VideoManagerService VMS;
	TextView alarmFlowNum = null;
	AudioManager audioManager = null;
	LinearLayout bottomBtnBar = null;
	private ImageButton btn0 = null;
	private ImageButton btndel = null;
	private ImageButton btnenight = null;
	private ImageButton btnfive = null;
	private ImageButton btnfour = null;
	private ImageButton btnjing = null;
	private ImageButton btnmi = null;
	private ImageButton btnnine = null;
	private ImageButton btnone = null;
	private ImageButton btnseven = null;
	private ImageButton btnsix = null;
	private ImageButton btnthree = null;
	private ImageButton btntwo = null;
	byte[] byteBuffer1;
	byte[] byteBuffer2;
	TextView callName = null;
	TextView callNum = null;
	PreviewCallBack callback;
	private int cameraCurrLock = -1;
	ImageView chgvideobtn = null;
	LinearLayout closelinear = null;
	private int color_fmt = -1;
	int curAngle = 0;
	boolean eflag = false;
	TimeOutSyncBufferQueue<YUVData> encodeataQueue = null;
	boolean encoderChanging = false;
	TimeOutSyncBufferQueue<YUVData> equeue = null;
	private boolean flowflag = false;
	ImageView flowlockbtn = null;
	int frame = 0;
	Handler handler = new C11542();
	Handler hangupHandler = new C11553();
	int height = 0;
	private int heightPix = 0;
	int iframe = 0;
	private boolean isChgVideo = false;
	boolean isFrontCamera = false;
	boolean isKeyboard = false;
	boolean isLocalRemoteChanged = false;
	boolean isMonitor = false;
	private boolean isMute = false;
	boolean isOutGoing = false;
	boolean isPaused = false;
	private boolean isShowViewFlag = false;
	private boolean isSpeakLoud = false;
	boolean isSurfaceDestroyed = false;
	boolean isUpload = false;
	boolean isVideoCall = false;
	private View keyboardView;
	private ImageView keyboard_img = null;
	long lastTimeStamp = -1;
	private int len = 0;
	LayoutParams localLp;
	Parameters localParameters;
	private SurfaceHolder localSurfaceHolder;
	private SurfaceView localview;
	private CallParams mCallParams;
	private Chronometer mCallTime;
	private Camera mCameraDevice;
	Context mContext = null;
	private boolean mDTMFToneEnabled;
	Thread mEncodeOutThread = null;
	Thread mEncodeSendThread = null;
	private IntentFilter mFilter;
	MediaCodec mMediaCodec;
	private String mScreanWakeLockKey = TAG;
	private int mStreamMusicVolumn = 0;
	private int mStreamVoiceCallVolumn = 0;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();
	ImageView mutebtn = null;
	boolean needChangeUV = false;
	int netrate = 0;
	private volatile EditText numTxt = null;
	private boolean numTxtCursor;
	boolean onlyBigViewClick = false;
	private String pixTag = "";
	private boolean prewRunning = false;
	ProgressBar proBar = null;
	private LinearLayout progressbarlinear = null;
	private double progressval = 0.0d;
	PttGrp pttGrp = null;
	boolean pttIdle = true;
	private BroadcastReceiver quitRecv2 = new C11531();
	RelativeLayout relatLayout = null;
	LayoutParams remoteLp;
	private SurfaceView remoteview;
	ImageView rotatebtn = null;
	private RtpStack rtpStack = null;
	encodeOutSendRunnable runable = null;
	boolean running = false;
	TextView selTxt = null;
	sendRunnable sendRunnableInstance = null;
	boolean sendThreadFlag = false;
	Handler sizeChangeHandler = new C11564();
	ImageView speakerbtn = null;
	ImageView stopvideobtn = null;
	Thread f1166t;
	Timer timer;
	long timestamp;
	ToneGenerator toneGenerator = null;
	LinearLayout topBoard;
	LinearLayout topLinearLayout = null;
	TextView tv_groupcall_status;
	private String videocode = "";
	private int videoport = 0;
	private String videourl = "";
	private boolean whichCameraFlag = true;
	int width = 0;
	private int widthPix = 0;

	class C11531 extends BroadcastReceiver {
		C11531() {
		}

		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			MyLog.d("viddeoTrace", "CameraCall#onReceive action = " + intent.getAction());
			if (intent.getAction().equals(GroupConstant.ACTION_GROUP_STATUS)) {
				PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
				if (pttGrp != null && CameraCall.this.tv_groupcall_status != null) {
					CameraCall.this.tv_groupcall_status.setText(CameraCall.this.ShowPttStatus(pttGrp.state));
				}
			} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_callscreen_finish")) {
				CallParams callParams = CallManager.getCallParams(intent);
				MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END username = " + CameraCall.this.mCallParams.getUsername() + " , param user name = " + callParams.getUsername());
				if (callParams == null || !callParams.equals(CameraCall.this.mCallParams)) {
					MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END not eques");
					return;
				}
				CallManager callManager = CallManager.getManager();
				if (CameraCall.this.mCallParams != null && CameraCall.this.mCallParams.getCallType() == CallType.VIDEO) {
					callManager.setUserAgentVideoCall(callManager.getCall(CameraCall.this.mCallParams.getCallType(), CameraCall.this.mCallParams.getCallId()));
				}
				CameraCall.this.reject();
				CameraCall.this.endCameraCall();
			} else if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
				Tools.FlowAlertDialog(CameraCall.this);
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_SINGLE_2_GROUP)) {
				GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
				GroupCallUtil.setActionMode(GroupConstant.ACTION_SINGLE_2_GROUP);
				GrpCallNotify.startSelf(intent);
			} else if (intent.getAction().equalsIgnoreCase(DeviceVideoInfo.ACTION_RESTART_CAMERA)) {
				if (!DeviceVideoInfo.onlyCameraRotate) {
					CameraCall.this.encoderChanging = true;
					CameraCall.this.releaseEncoder();
					CameraCall.this.curAngle = DeviceVideoInfo.curAngle;
					CameraCall.this.initMediaCodec();
					CameraCall.this.encoderChanging = false;
				} else if (DeviceVideoInfo.isHorizontal) {
					if (CameraCall.this.mCameraDevice == null) {
						return;
					}
					if (CameraCall.this.isFrontCamera) {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
						if (DeviceVideoInfo.curAngle == 0) {
							CameraCall.this.curAngle = 90;
						} else {
							CameraCall.this.curAngle = 270;
						}
					} else if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
					} else {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					}
				} else if (CameraCall.this.mCameraDevice == null) {
				} else {
					if (CameraCall.this.isFrontCamera) {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					} else if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					} else {
						CameraCall.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
					}
				}
			} else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
				switch (extras.getInt(AudioUtil.KEY_STREAM_INT)) {
					case 0:
						CameraCall.this.setVolumeControlStream(0);
						return;
					case 3:
						CameraCall.this.setVolumeControlStream(3);
						return;
					default:
						return;
				}
			} else if (intent.getAction().equalsIgnoreCase(TempGrpCallActivity.ACTION_TEMP_GRP_INVITE)) {
				ArrayList<String> inviteMembers = intent.getStringArrayListExtra("inviteMembers");
				if (inviteMembers != null && inviteMembers.size() > 0) {
					ArrayList<String> mMemberList = new ArrayList();
					for (int i = 0; i < inviteMembers.size(); i++) {
						String newMem = (String) inviteMembers.get(i);
						if (!TempGroupCallUtil.arrayListMembers.contains(newMem)) {
							mMemberList.add(newMem);
						}
					}
					TempGroupCallUtil.arrayListMembers.addAll(mMemberList);
				}
			} else if (intent.getAction().equalsIgnoreCase("com.ceiw.keyevent")) {
				if (intent.getStringExtra("key_code").equals(BaseSipHeaders.Contact_short) && CameraCall.this.VMS.isCurrentVideoCall()) {
					String down = intent.getStringExtra("key_action");
					AudioManager am = (AudioManager) CameraCall.this.getSystemService(Context.AUDIO_SERVICE);
					if (Build.MODEL.toLowerCase().contains("lter")) {
						am.setMode(AudioManager.MODE_NORMAL);
					}
					if (down.equals("long_press")) {
						if (!CameraCall.this.isMute) {
							Receiver.engine(CameraCall.this.mContext).togglemute();
							CameraCall.this.isMute = true;
						}
						CameraCall.this.mutebtn.setImageResource(R.drawable.call_unmute0);
						if (Build.MODEL.toLowerCase().contains("lter")) {
							assert am != null;
							am.setSpeakerphoneOn(false);
						} else {
							AudioModeUtils.setAudioStyle(3, false);
						}
					} else if (down.equals("up")) {
						if (CameraCall.this.isMute) {
							CameraCall.this.isMute = false;
							Receiver.engine(CameraCall.this.mContext).togglemute();
						}
						CameraCall.this.mutebtn.setImageResource(R.drawable.call_unmute);
						if (Build.MODEL.toLowerCase().contains("lter")) {
							assert am != null;
							am.setSpeakerphoneOn(true);
						} else {
							AudioModeUtils.setAudioStyle(3, true);
						}
					}
					if (CameraCall.this.getVolumeControlStream() != 0) {
						CameraCall.this.setVolumeControlStream(0);
					}
				}
			}
		}
	}

	class C11542 extends Handler {
		C11542() {
		}

		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				CameraCall.this.finish();
			} else if (msg.what == 1) {
				CameraCall.this.alarmFlowNum.setText(new StringBuilder(String.valueOf(Tools.calculateTotal(MemoryMg.getInstance().User_3GRelTotal))).append("M").toString());
				CameraCall.this.progressval = Tools.calculatePercent(MemoryMg.getInstance().User_3GRelTotal, MemoryMg.getInstance().User_3GTotal);
				if (CameraCall.this.progressval < 0.6d && CameraCall.this.progressval >= 0.0d) {
					CameraCall.this.proBar.setProgress((int) (100.0d - (CameraCall.this.progressval * 100.0d)));
					CameraCall.this.proBar.setProgressDrawable(CameraCall.this.getResources().getDrawable(R.drawable.progressblue));
				} else if (CameraCall.this.progressval < 0.9d && CameraCall.this.progressval >= 0.6d) {
					CameraCall.this.proBar.setProgress((int) (100.0d - (CameraCall.this.progressval * 100.0d)));
					CameraCall.this.proBar.setProgressDrawable(CameraCall.this.getResources().getDrawable(R.drawable.progressyellow));
				} else if (CameraCall.this.progressval >= 0.9d && CameraCall.this.progressval <= 1.0d) {
					CameraCall.this.proBar.setProgress((int) (100.0d - (CameraCall.this.progressval * 100.0d)));
					CameraCall.this.proBar.setProgressDrawable(CameraCall.this.getResources().getDrawable(R.drawable.progressred));
				}
				CameraCall.this.handler.sendMessageDelayed(CameraCall.this.handler.obtainMessage(1), 8000);
			} else if (msg.what == 2) {
				CameraCall.this.handler.sendMessageDelayed(CameraCall.this.handler.obtainMessage(2), 1000);
			}
		}
	}

	class C11553 extends Handler {
		C11553() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					CameraCall.this.reject();
					CameraCall.this.endCameraCall();
					return;
				default:
					return;
			}
		}
	}

	class C11564 extends Handler {
		C11564() {
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					if (CameraCall.this.isShowViewFlag) {
						CameraCall.this.isShowViewFlag = false;
						if (CameraCall.this.bottomBtnBar != null) {
							CameraCall.this.bottomBtnBar.setVisibility(View.GONE);
						}
						if (CameraCall.this.topBoard != null) {
							CameraCall.this.topBoard.setVisibility(View.GONE);
						}
						if (CameraCall.this.closelinear != null) {
							CameraCall.this.closelinear.setVisibility(View.GONE);
							return;
						}
						return;
					}
					return;
				default:
					return;
			}
		}
	}

	class C11575 implements Runnable {
		C11575() {
		}

		public void run() {
			while (CameraCall.this.prewRunning) {
				CameraCall.this.rtpStack.SendEmptyPacket();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class C11586 implements Callback {
		C11586() {
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			MyLog.e("remote surface", "show view destroyed!");
			CameraCall.this.isSurfaceDestroyed = true;
			if (CameraCall.this.rtpStack != null) {
				CameraCall.this.rtpStack.setSurfaceAviable(false);
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			MyLog.e("remote surface", "show view created!");
			if (CameraCall.this.rtpStack != null) {
				CameraCall.this.rtpStack.setSurfaceAviable(true);
			}
			if (CameraCall.this.rtpStack != null && CameraCall.this.isSurfaceDestroyed) {
				CameraCall.this.rtpStack.resetDecode();
				CameraCall.this.isSurfaceDestroyed = false;
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			MyLog.e("surface", "show view changed. 2222222222222" + width + " height = " + height);
			holder.setFixedSize(width, height);
		}
	}

	class C11597 extends Thread {
		C11597() {
		}

		public void run() {
			while (CameraCall.this.running) {
				if (CameraCall.this.numTxt == null || CameraCall.this.len == CameraCall.this.numTxt.getText().length()) {
					try {
						C11597.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					long time = SystemClock.elapsedRealtime();
					if (CameraCall.this.numTxt.getText().length() > CameraCall.this.len) {
						SipdroidEngine engine = Receiver.engine(Receiver.mContext);
						Editable text = CameraCall.this.numTxt.getText();
						CameraCall cameraCall = CameraCall.this;
						int access$18 = cameraCall.len;
						cameraCall.len = access$18 + 1;
						engine.info(text.charAt(access$18), 250);
					}
					time = 250 - (SystemClock.elapsedRealtime() - time);
					if (time > 0) {
						try {
							C11597.sleep(time);
						} catch (InterruptedException e2) {
						}
					}
					try {
						if (CameraCall.this.running) {
							C11597.sleep(250);
						}
					} catch (InterruptedException e3) {
					}
				}
			}
			CameraCall.this.f1166t = null;
		}
	}

	class C11608 implements PreviewCallback {
		C11608() {
		}

		public void onPreviewFrame(byte[] data, Camera camera) {
		}
	}

	class C11619 implements AutoFocusMoveCallback {
		C11619() {
		}

		public void onAutoFocusMoving(boolean start, Camera camera) {
		}
	}

	class FIFO {
		long curERTime;
		long timeStamp;

		public FIFO(long curERTime, long timeStamp) {
			this.curERTime = curERTime;
			this.timeStamp = timeStamp;
		}
	}

	class PreviewCallBack implements Callback {
		PreviewCallBack() {
		}

		public void surfaceCreated(SurfaceHolder holder) {
			MyLog.e("surface", "preview create!!!");
			Log.w("GUOK", "PreviewCallBack: surfaceCreated");
			MyLog.d("videoTrace", "CameraCall#surfaceCreated() enter");
			CameraCall.this.closeCamera();
			CameraCall.this.startPreview(CameraCall.this.whichCameraFlag);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			MyLog.e("surface", "preview changed!!!");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			MyLog.e("surface", "preview destroyed!!!");
		}
	}

	private void InitH264Encoder() {
		if (this.pixTag.equals("5")) {
			this.width = 320;
			this.height = 240;
		} else if (this.pixTag.equals("4")) {
			this.width = 352;
			this.height = 288;
		} else if (this.pixTag.equals("6")) {
			this.width = 640;
			this.height = 480;
		} else if (this.pixTag.equals("3")) {
			this.width = 720;
			this.height = 480;
		} else if (this.pixTag.equals("7")) {
			this.width = 176;
			this.height = 144;
		} else if (this.pixTag.equals("8")) {
			this.width = 384;
			this.height = 288;
		} else if (this.pixTag.equals("9")) {
			this.width = 480;
			this.height = 320;
		} else {
			this.width = 1280;
			this.height = 720;
		}
		this.byteBuffer1 = new byte[this.width * this.height * 3 / 2];
		this.byteBuffer2 = new byte[this.width * this.height * 3 / 2];
	}

	private void InitTones() {
		boolean mdtmfToneEnabled = true;
		CameraCall.mToneMap.put('1', 1);
		CameraCall.mToneMap.put('2', 2);
		CameraCall.mToneMap.put('3', 3);
		CameraCall.mToneMap.put('4', 4);
		CameraCall.mToneMap.put('5', 5);
		CameraCall.mToneMap.put('6', 6);
		CameraCall.mToneMap.put('7', 7);
		CameraCall.mToneMap.put('8', 8);
		CameraCall.mToneMap.put('9', 9);
		CameraCall.mToneMap.put('0', 0);
		CameraCall.mToneMap.put('#', 11);
		CameraCall.mToneMap.put('*', 10);
		CameraCall.mToneMap.put('d', 12);
	}

	private void ShowCallNumAndTime() {
		try {
			if (TextUtils.isEmpty((CharSequence) CallUtil.mName)) {
				this.callName.setText((CharSequence) CallUtil.mNumber);
			} else {
				this.callName.setText((CharSequence) CallUtil.mName);
			}
			this.selTxt.setText(R.string.vedio_incom);
		} catch (Exception ex) {
			MyLog.e("CameraCall ShowCallNumAndTime error", ex.toString());
		}
	}

	private void buildRtpStack(final SurfaceView surfaceView) {
		synchronized (this) {
			if (this.rtpStack == null && Receiver.GetCurUA().getVedioSocket() != null) {
				this.rtpStack = new RtpStack(surfaceView, null, (Context) this, this.videourl, this.videoport, Receiver.GetCurUA().getVedioSocket());
			}
			this.checkSurfaceInRtpStack(surfaceView);
		}
	}

	private void checkSurfaceInRtpStack(final SurfaceView sfview) {
		if (this.rtpStack != null && !this.rtpStack.doesSetSfview() && sfview != null) {
			this.rtpStack.setSfview(sfview);
		}
	}

	private void closeCamera() {
		MyLog.d("videoTrace", "Cameracall#closeCamera() enter");
		if (this.mCameraDevice == null) {
			return;
		}
		MyLog.v("CameraCall", "closeCamera");
		this.mCameraDevice.setPreviewCallback((Camera.PreviewCallback) null);
		this.mCameraDevice.stopPreview();
		this.mCameraDevice.release();
		this.mCameraDevice = null;
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

	private void endCameraCall() {
		MyLog.d("videoTrace", "Cameracall#endCameraCall() enter");
		MyLog.i("endCameraCall", "endCameraCall");
//		MyLog.i("CameraCall", "endcameracall begin:" + System.currentTimeMillis());
		this.prewRunning = false;
		MemoryMg.getInstance().isSendOnly = false;
		if (this.runable != null) {
			this.runable.stop();
		}
		if (this.mEncodeOutThread != null) {
			this.mEncodeOutThread.interrupt();
		}
		this.closeCamera();
		this.releaseEncoder();
		if (this.rtpStack != null) {
			this.rtpStack.CloseUdpSocket();
		}
//		MyLog.i("CameraCall", "endcameracall end:" + System.currentTimeMillis());
		this.handler.sendEmptyMessageDelayed(0, 500L);
	}

	private void exitDialog(final Context context, final String s, final String s2) {
		final AlertDialog create = new AlertDialog.Builder((Context) this).create();
		create.show();
		final Window window = create.getWindow();
		window.setContentView(R.layout.shrew_exit_dialog);
		((TextView) window.findViewById(R.id.btn_ok)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.dismiss();
				MyLog.d("videoTrace", "Cameracall#exitDialog() enter");
				CameraCall.this.reject();
				CameraCall.this.endCameraCall();
			}
		});
		((TextView) window.findViewById(R.id.btn_cancel)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.cancel();
			}
		});
	}

	public static String getCurVideoKey() {
		if (CameraCall.cameraval.equals("0")) {
			return PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
		}
		return PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
	}

	private void initKeyBoard() {
		this.keyboardView = this.findViewById(R.id.keyboard_layout);
		(this.keyboard_img = (ImageView) this.findViewById(R.id.keyboard_img)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final ImageView imageView = (ImageView) CameraCall.this.findViewById(R.id.keyboard_img);
				switch (motionEvent.getAction()) {
					case 0: {
						if (CameraCall.this.isKeyboard) {
							imageView.setImageResource(R.drawable.keyboarddown);
							break;
						}
						imageView.setImageResource(R.drawable.keyboardup);
						break;
					}
					case 1: {
						if (CameraCall.this.isKeyboard) {
							imageView.setImageResource(R.drawable.keyboarddown_release);
							break;
						}
						imageView.setImageResource(R.drawable.keyboardup_release);
						break;
					}
				}
				return false;
			}
		});
		this.keyboard_img.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final ImageView imageView = (ImageView) view;
				if (CameraCall.this.isKeyboard) {
					CameraCall.this.isKeyboard = false;
					imageView.setImageResource(R.drawable.keyboardup_release);
					CameraCall.this.keyboardView.setVisibility(View.GONE);
					return;
				}
				CameraCall.this.isKeyboard = true;
				imageView.setImageResource(R.drawable.keyboarddown_release);
				CameraCall.this.keyboardView.setVisibility(View.VISIBLE);
			}
		});
		this.findViewById(R.id.yincang).setVisibility(View.GONE);
		(this.numTxt = (EditText) this.findViewById(R.id.p_digits)).setText((CharSequence) "");
		this.numTxt.setFocusable(false);
		this.numTxt.setFocusableInTouchMode(false);
		this.numTxt.setClickable(false);
		this.numTxt.setEnabled(false);
		this.numTxt.setCursorVisible(false);
		this.numTxtCursor = false;
		this.numTxt.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CameraCall.this.numTxt.setGravity(17);
				CameraCall.this.numTxt.setCursorVisible(true);
//				CameraCall.access .23 (CameraCall.this, true);
			}
		});
		this.numTxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
		this.numTxt.setDrawingCacheEnabled(true);
		(this.btnjing = (ImageButton) this.findViewById(R.id.pjing)).setOnClickListener((View.OnClickListener) this);
		(this.btnone = (ImageButton) this.findViewById(R.id.pone)).setOnClickListener((View.OnClickListener) this);
		(this.btntwo = (ImageButton) this.findViewById(R.id.ptwo)).setOnClickListener((View.OnClickListener) this);
		(this.btnthree = (ImageButton) this.findViewById(R.id.pthree)).setOnClickListener((View.OnClickListener) this);
		(this.btnfour = (ImageButton) this.findViewById(R.id.pfour)).setOnClickListener((View.OnClickListener) this);
		(this.btnfive = (ImageButton) this.findViewById(R.id.pfive)).setOnClickListener((View.OnClickListener) this);
		(this.btnsix = (ImageButton) this.findViewById(R.id.psix)).setOnClickListener((View.OnClickListener) this);
		(this.btnseven = (ImageButton) this.findViewById(R.id.pseven)).setOnClickListener((View.OnClickListener) this);
		(this.btnenight = (ImageButton) this.findViewById(R.id.penight)).setOnClickListener((View.OnClickListener) this);
		(this.btnnine = (ImageButton) this.findViewById(R.id.pnine)).setOnClickListener((View.OnClickListener) this);
		(this.btn0 = (ImageButton) this.findViewById(R.id.p0)).setOnClickListener((View.OnClickListener) this);
		(this.btnmi = (ImageButton) this.findViewById(R.id.pmi)).setOnClickListener((View.OnClickListener) this);
		(this.btndel = (ImageButton) this.findViewById(R.id.pdel)).setOnClickListener((View.OnClickListener) this);
		this.btndel.setOnLongClickListener((View.OnLongClickListener) new View.OnLongClickListener() {
			public boolean onLongClick(final View view) {
				CameraCall.this.numTxt.setText((CharSequence) "");
				return false;
			}
		});
		this.InitTones();
	}

	private boolean initMediaCodec() {
		MyLog.d("videoTrace", "CameraCall#initMediaCodec() enter sdk version = " + Build.VERSION.SDK_INT);
		this.color_fmt = DeviceVideoInfo.supportColor;
		if (Build.VERSION.SDK_INT < 16) {
			MyToast.showToast(true, this.mContext, this.mContext.getString(R.string.version_unsupported));
			this.hangupHandler.sendEmptyMessageDelayed(0, 1500L);
			return false;
		}
		if (this.color_fmt < 0) {
			MyToast.showToastInBg(true, this.getApplicationContext(), R.string.cameracall_encode_error);
			this.hangupHandler.sendEmptyMessageDelayed(0, 1500L);
			return false;
		}
		return true;
	}

	private boolean onVolumeDown() {
		GroupCallUtil.makeGroupCall(true, false, UserAgent.PttPRMode.SideKeyPress);
		return true;
	}

	private boolean onVolumeUp() {
		MyLog.d("videoTrace", "volumn up");
		GroupCallUtil.makeGroupCall(false, false, UserAgent.PttPRMode.Idle);
		return true;
	}

	private void releaseEncoder() {
		if (this.runable != null) {
			this.runable.stop();
		}
		if (this.mEncodeOutThread != null && this.mEncodeOutThread.isAlive()) {
			this.mEncodeOutThread.interrupt();
		}
		if (this.mEncodeSendThread != null && this.mEncodeSendThread.isAlive()) {
			this.mEncodeSendThread.interrupt();
		}
		if (this.equeue != null) {
			this.equeue.clear();
		}
		if (this.encodeataQueue != null) {
			this.encodeataQueue.clear();
		}
		if (this.mMediaCodec != null) {
			this.mMediaCodec.stop();
			this.mMediaCodec.release();
			this.mMediaCodec = null;
		}
	}

	private void setAntibanding(Camera.Parameters param) {
//		final List supportedAntibanding = camera.Parameters.getSupportedAntibanding();
//		if (supportedAntibanding != null && supportedAntibanding.size() != 0 && supportedAntibanding.indexOf("50hz") >= 0) {
//			camera.Parameters.setAntibanding("50hz");
//		}
	}

	private void setCameraDataParas(final boolean b) {
		if (b && !DeviceVideoInfo.isCodecK3) {
			this.releaseEncoder();
		}
		if (this.isFrontCamera) {
			this.pixTag = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
		} else {
			this.pixTag = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
		}
		if (!DeviceVideoInfo.isCodecK3) {
			this.InitH264Encoder();
			this.initMediaCodec();
		}
	}

	private void setCurAngleAfterCameraOpen(final boolean b) {
		if (!DeviceVideoInfo.isHorizontal) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			this.curAngle = 0;
			return;
		}
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (b) {
			this.curAngle = 90;
			return;
		}
		this.curAngle = 270;
	}

	private void setRemoteSize() {
		this.remoteLp.leftMargin = 0;
		this.remoteLp.topMargin = 0;
		if (!DeviceVideoInfo.isHorizontal) {
			this.remoteLp.width = this.widthPix;
			this.remoteLp.height = this.localLp.topMargin;
			return;
		}
		if (this.heightPix > this.widthPix) {
			this.remoteLp.height = this.widthPix;
			this.remoteLp.width = this.heightPix - this.localLp.width - this.dip2px(20);
			return;
		}
		this.remoteLp.height = this.heightPix;
		this.remoteLp.width = this.localLp.leftMargin;
	}

	private void startPreview(final boolean p0) {
		//
	}

	String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
		switch (e_Grp_State) {
			case GRP_STATE_SHOUDOWN:
				return this.getResources().getString(R.string.close);
			case GRP_STATE_IDLE:
				return this.getResources().getString(R.string.idle);
			case GRP_STATE_INITIATING:
				return this.getResources().getString(R.string.ptt_requesting);
			case GRP_STATE_TALKING:
				return this.getResources().getString(R.string.talking);
			case GRP_STATE_LISTENING:
				return this.getResources().getString(R.string.listening);
			case GRP_STATE_QUEUE:
				return this.getResources().getString(R.string.queueing);
			default:
				return this.getResources().getString(R.string.error);
		}
	}

	public int dip2px(int n) {
		final float density = this.getResources().getDisplayMetrics().density;
		final float n2 = n;
		if (n >= 0) {
			n = 1;
		} else {
			n = -1;
		}
		return (int) (n * 0.5f + n2 * density);
	}

	public void downKey(final String s) {
		this.numTxt.setGravity(17);
		if (this.numTxtCursor) {
			final int selectionStart = this.numTxt.getSelectionStart();
			this.numTxt.setText((CharSequence) new StringBuffer(this.numTxt.getText().toString().trim()).insert(selectionStart, s).toString());
			Selection.setSelection((Spannable) this.numTxt.getText(), selectionStart + 1);
			return;
		}
		this.numTxt.setText((CharSequence) (String.valueOf(this.numTxt.getText().toString().trim()) + s));
	}

	byte[] findI(final byte[] array) {
		final int n = -1;
		while (true) {
			for (int i = 0; i < array.length; ++i) {
				if (i < array.length - 3 && array[i] == 0 && array[i + 1] == 0 && array[i + 2] == 0 && array[i + 3] == 1 && (array[i + 4] & 0x1F) == 0x5) {
					final int n2 = i + 4;
					final byte[] array2 = new byte[array.length - n2];
//					System.arraycopy(array, n2, array2, 0, array2.length);
					return array2;
				}
			}
			final int n2 = n;
			continue;
		}
	}

	byte[] findPPS(final byte[] array) {
		for (int n = 0; n < array.length && (n >= array.length - 3 || array[n] != 0 || array[n + 1] != 0 || array[n + 2] != 0 || array[n + 3] != 1); ++n) {
		}
		final byte[] array2 = new byte[array.length - 4];
//		System.arraycopy(array, 4, array2, 0, array2.length);
		return array2;
	}

	byte[] findSPS(final byte[] array) {
		final int n = -1;
		while (true) {
			for (int i = 0; i < array.length; ++i) {
				if (i < array.length - 3 && array[i] == 0 && array[i + 1] == 0 && array[i + 2] == 0 && array[i + 3] == 1 && (array[i + 4] & 0x1F) == 0x8) {
					final byte[] array2 = new byte[i];
//					System.arraycopy(array, 0, array2, 0, i);
					return array2;
				}
			}
			int i = n;
			continue;
		}
	}

	int getCameraHeight() {
		if (!DeviceInfo.isSupportHWChange) {
			return this.height;
		}
		if (DeviceVideoInfo.isHorizontal) {
			return this.height;
		}
		return this.width;
	}

	int getCameraWidth() {
		if (!DeviceInfo.isSupportHWChange) {
			return this.width;
		}
		if (DeviceVideoInfo.isHorizontal) {
			return this.width;
		}
		return this.height;
	}

	int getEncodeHeight() {
		if (DeviceVideoInfo.isHorizontal) {
			return this.height;
		}
		if (DeviceInfo.isSupportHWChange) {
			if (this.curAngle == 0 || this.curAngle == 180) {
				return this.height;
			}
			return this.width;
		} else {
			if (this.curAngle == 0 || this.curAngle == 180) {
				return this.width;
			}
			return this.height;
		}
	}

	int getEncodeWidth() {
		if (DeviceVideoInfo.isHorizontal) {
			return this.width;
		}
		if (DeviceInfo.isSupportHWChange) {
			if (this.curAngle == 0 || this.curAngle == 180) {
				return this.width;
			}
			return this.height;
		} else {
			if (this.curAngle == 0 || this.curAngle == 180) {
				return this.height;
			}
			return this.width;
		}
	}

	public void handle() {
		this.reject();
		this.endCameraCall();
	}

	void initVideoView() {
		this.callName = (TextView) this.findViewById(R.id.vcallname);
		this.callNum = (TextView) this.findViewById(R.id.vcallnum);
		this.selTxt = (TextView) this.findViewById(R.id.selecttxt);
		this.bottomBtnBar = (LinearLayout) this.findViewById(R.id.bottomBoard);
		this.topBoard = (LinearLayout) this.findViewById(R.id.topboard);
		(this.speakerbtn = (ImageView) this.findViewById(R.id.speakerbtn)).setOnClickListener((View.OnClickListener) this);
		(this.mutebtn = (ImageView) this.findViewById(R.id.mutebtn)).setOnClickListener((View.OnClickListener) this);
		(this.stopvideobtn = (ImageView) this.findViewById(R.id.stopvideobtn)).setOnClickListener((View.OnClickListener) this);
		(this.chgvideobtn = (ImageView) this.findViewById(R.id.chgvideobtn)).setOnClickListener((View.OnClickListener) this);
		(this.rotatebtn = (ImageView) this.findViewById(R.id.rotatebtn)).setVisibility(View.GONE);
		this.rotatebtn.setOnClickListener((View.OnClickListener) this);
		this.flowlockbtn = (ImageView) this.findViewById(R.id.flowlockbtn);
		if (!DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS) {
			this.flowlockbtn.setVisibility(View.GONE);
		}
		this.flowlockbtn.setOnClickListener((View.OnClickListener) this);
		(this.closelinear = (LinearLayout) this.findViewById(R.id.closelinear)).setOnClickListener((View.OnClickListener) this);
		(this.mCallTime = (Chronometer) this.findViewById(R.id.call_time)).start();
		this.progressbarlinear = (LinearLayout) this.findViewById(R.id.progressbarlinear);
		this.remoteview = (SurfaceView) this.findViewById(R.id.bigvideoView);
		(this.localview = (SurfaceView) this.findViewById(R.id.localvideoView)).setOnClickListener((View.OnClickListener) this);
		this.remoteview.setOnClickListener((View.OnClickListener) this);
		this.remoteview.getHolder().addCallback((SurfaceHolder.Callback) new SurfaceHolder.Callback() {
			public void surfaceChanged(final SurfaceHolder surfaceHolder, final int n, final int n2, final int n3) {
				MyLog.i("surface", "show view changed. 2222222222222" + n2 + " height = " + n3);
				surfaceHolder.setFixedSize(n2, n3);
			}

			public void surfaceCreated(final SurfaceHolder surfaceHolder) {
				MyLog.i("remote surface", "show view created!");
				if (CameraCall.this.rtpStack != null) {
					CameraCall.this.rtpStack.setSurfaceAviable(true);
				}
				if (CameraCall.this.rtpStack != null && CameraCall.this.isSurfaceDestroyed) {
					CameraCall.this.rtpStack.resetDecode();
					CameraCall.this.isSurfaceDestroyed = false;
				}
			}

			public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
				MyLog.i("remote surface", "show view destroyed!");
				CameraCall.this.isSurfaceDestroyed = true;
				if (CameraCall.this.rtpStack != null) {
					CameraCall.this.rtpStack.setSurfaceAviable(false);
				}
			}
		});
		this.alarmFlowNum = (TextView) this.findViewById(R.id.alarmnum);
		this.proBar = (ProgressBar) this.findViewById(R.id.probar);
		this.tv_groupcall_status = (TextView) this.findViewById(R.id.groupcall_status);
		if (this.VMS.isCurrentVideoMonitor() || this.VMS.isCurrentVideoUpload() || MemoryMg.getInstance().isReceiverOnly) {
			this.tv_groupcall_status.setVisibility(View.VISIBLE);
			this.tv_groupcall_status.setText(R.string.close);
			this.speakerbtn.setVisibility(View.GONE);
			this.mutebtn.setVisibility(View.GONE);
			if (this.VMS.isCurrentVideoMonitor() && this.VMS.isVideoOutgoingCall()) {
				this.chgvideobtn.setVisibility(View.GONE);
			}
			if (this.VMS.isCurrentVideoUpload() && !this.VMS.isVideoOutgoingCall()) {
				this.chgvideobtn.setVisibility(View.GONE);
			}
			final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
			if (getCurGrp != null && this.tv_groupcall_status != null) {
				this.tv_groupcall_status.setText((CharSequence) this.ShowPttStatus(getCurGrp.state));
			}
			return;
		}
		this.tv_groupcall_status.setVisibility(View.GONE);
	}

	public boolean needChangeUVinNV21() {
		final String lowerCase = Build.MODEL.toLowerCase();
		if (DeviceVideoInfo.isCodecK3 && !lowerCase.contains("honor")) {
			if (!DeviceVideoInfo.color_correct) {
				return false;
			}
		} else if (DeviceVideoInfo.color_correct) {
			return false;
		}
		return true;
	}

	public void onBigWindowClickedEvent() {
		if (this.isShowViewFlag) {
			this.isShowViewFlag = false;
			if (this.bottomBtnBar != null) {
				this.bottomBtnBar.setVisibility(View.GONE);
			}
			if (this.topBoard != null) {
				this.topBoard.setVisibility(View.GONE);
			}
			if (this.closelinear != null) {
				this.closelinear.setVisibility(View.GONE);
			}
			return;
		}
		this.bottomBtnBar.getParent().bringChildToFront((View) this.bottomBtnBar);
		this.topBoard.getParent().bringChildToFront((View) this.topBoard);
		this.closelinear.getParent().bringChildToFront((View) this.closelinear);
		this.isShowViewFlag = true;
		if (this.bottomBtnBar != null) {
			this.bottomBtnBar.setVisibility(View.VISIBLE);
		}
		if (this.topBoard != null) {
			this.topBoard.setVisibility(View.VISIBLE);
		}
		if (this.closelinear != null) {
			this.closelinear.setVisibility(View.VISIBLE);
		}
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		(this.timer = new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				if (CameraCall.this.sizeChangeHandler == null) {
					return;
				}
				CameraCall.this.sizeChangeHandler.sendEmptyMessage(1);
			}
		}, 3000L);
	}

	public void onClick(final View view) {
		final boolean b = false;
		boolean isLocalRemoteChanged = false;
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		MyLog.d("videoTrace", "Cameracall#onClick() check state = " + callState);
		if (Receiver.isCallNotificationNeedClose() && callState == CallManager.CallState.IDLE) {
			MyLog.d("videoTrace", "Cameracall#onClick() enter finish");
			this.finish();
		}
		switch (view.getId()) {
			case R.id.speakerbtn: {
				final ImageView imageView = (ImageView) view;
				if (this.isSpeakLoud) {
					AudioModeUtils.setAudioStyle(3, false);
				} else {
					AudioModeUtils.setAudioStyle(3, true);
				}
				if (this.getVolumeControlStream() != 0) {
					this.setVolumeControlStream(0);
					return;
				}
				break;
			}
			case R.id.mutebtn: {
				final ImageView imageView2 = (ImageView) view;
				if (!Build.MODEL.toLowerCase().contains("lter")) {
					Receiver.engine((Context) this).togglemute();
					if (this.isMute) {
						this.isMute = false;
						imageView2.setImageResource(R.drawable.call_unmute0);
						return;
					}
					this.isMute = true;
					imageView2.setImageResource(R.drawable.call_unmute);
					return;
				} else {
					if (!this.isMute) {
						Receiver.engine(this.mContext).togglemute();
						this.isMute = true;
						imageView2.setImageResource(R.drawable.call_unmute0);
						return;
					}
					Receiver.engine(this.mContext).togglemute();
					this.isMute = false;
					imageView2.setImageResource(R.drawable.call_unmute);
					return;
				}
			}
			case R.id.chgvideobtn: {
				final ImageView imageView3 = (ImageView) view;
				if (this.isChgVideo) {
					this.isChgVideo = false;
					imageView3.setImageResource(R.drawable.call_chgcamera0);
				} else {
					this.isChgVideo = true;
					imageView3.setImageResource(R.drawable.call_chgcamera);
				}
				MyLog.d("videoTrace", "CameraCall#onClick() enter");
				this.closeCamera();
				this.setCameraDataParas(true);
				this.startPreview(false);
			}
			case R.id.flowlockbtn: {
				if (!this.flowflag) {
					this.flowflag = true;
					return;
				}
				this.flowflag = false;
			}
			case R.id.closelinear: {
				this.exitDialog((Context) this, this.getResources().getString(R.string.information), this.getResources().getString(R.string.end_vedio_notify));
			}
			case R.id.localvideoView: {
				if (!this.isLocalRemoteChanged && !this.onlyBigViewClick) {
					this.viewResize(this.remoteview);
					this.viewResize(this.localview);
					this.localview.getBackground().setAlpha(0);
					this.remoteview.getParent().bringChildToFront((View) this.remoteview);
					if (!this.isLocalRemoteChanged) {
						isLocalRemoteChanged = true;
					}
					this.isLocalRemoteChanged = isLocalRemoteChanged;
					return;
				}
				this.onBigWindowClickedEvent();
			}
			case R.id.bigvideoView: {
				if (this.isLocalRemoteChanged && !MemoryMg.getInstance().isSendOnly) {
					this.viewResize(this.localview);
					this.viewResize(this.remoteview);
					this.localview.getBackground().setAlpha(100);
					this.remoteview.getBackground().setAlpha(0);
					this.localview.getParent().bringChildToFront((View) this.localview);
					this.isLocalRemoteChanged = (!this.isLocalRemoteChanged || b);
					return;
				}
				this.onBigWindowClickedEvent();
			}
			case R.id.rotatebtn: {
				if (this.rtpStack != null) {
					this.rtpStack.rotateRemoteView();
					return;
				}
				break;
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

	public void onConfigurationChanged(final Configuration configuration) {
		LanguageChange.upDateLanguage((Context) this);
		super.onConfigurationChanged(configuration);
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		MyLog.d("videoTrace", "Cameracall#onCreate() enter");
		Log.e("TANGJIAN", "\u5f53\u524d\u624b\u673a\u578b\u53f7\uff1a" + Build.MODEL);
		super.onCreate(bundle);
		this.VMS = VideoManagerService.getDefault();
		final String currentAction = this.VMS.getCurrentAction();
		SettingVideoSize.setDefaultValue(currentAction, (Context) this);
		this.needChangeUV = this.needChangeUVinNV21();
		this.VMS.initVideoSettingColumns(currentAction);
		this.VMS.initSettingValue((Context) this, currentAction);
		this.VMS.registerEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
		this.mCallParams = CallManager.getCallParams(this.getIntent());
		this.AM = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		if (DeviceVideoInfo.isHorizontal) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			this.curAngle = 270;
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			this.curAngle = 0;
		}
//		MyLog.i("CameraCall", "create begin" + System.currentTimeMillis());
		final ExtendedCall videoInCall = CallManager.getManager().getVideoInCall();
		if (videoInCall != null) {
			final SessionDescriptor sessionDescriptor = new SessionDescriptor(videoInCall.getRemoteSessionDescriptor());
			this.videourl = new Parser(sessionDescriptor.getConnection().toString()).skipString().skipString().getString();
			final Enumeration<MediaDescriptor> elements = sessionDescriptor.getMediaDescriptors().elements();
			while (elements.hasMoreElements()) {
				final MediaField media = elements.nextElement().getMedia();
				if (media.getMedia().equals("video")) {
					this.videoport = media.getPort();
				}
			}
			MyLog.i("CameraCall", String.valueOf(this.videourl) + "   " + this.videoport + "  " + videoInCall.toString());
		}
		MyLog.i("CameraCall", String.valueOf(this.videourl) + "   " + this.videoport + "  ");
		if ("".equals(this.videourl) || this.videoport == 0) {
			MyLog.i("CameraCall", "AudioPort Camera_URL VideoPort  null");
			MyToast.showToastInBg(true, this.getApplicationContext(), R.string.cameracall_startfail);
			((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2);
			this.finish();
		} else {
			if (Build.VERSION.SDK_INT >= 16 && DeviceVideoInfo.supportColor == -1) {
				DeviceVideoInfo.supportColor = PhoneSupportTest.getEncodeSupportColor();
			}
			this.isShowViewFlag = false;
			this.isMute = false;
			UserAgent.isCamerPttDialog = true;
			this.whichCameraFlag = true;
			this.mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen("CameraCall");
			MyWindowManager.getInstance().disableKeyguard(this);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(1024, 1024);
			this.setContentView(R.layout.cameracall_new);
			this.initVideoView();
			AudioModeUtils.setSpeakerphoneChangeLister(this);
			this.startService(new Intent((Context) this, (Class) SensorCheckService.class));
			this.mContext = (Context) this;
			this.timer = new Timer();
			final VideoManagerService default1 = VideoManagerService.getDefault();
			final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences((Context) this);
			final String camera_TYPE_FRONT_OR_POSTPOS = DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS;
			String s;
			if (default1.isCurrentVideoUpload() || default1.isCurrentVideoMonitor()) {
				s = "0";
			} else {
				s = "1";
			}
			CameraCall.cameraval = defaultSharedPreferences.getString(camera_TYPE_FRONT_OR_POSTPOS, s);
			this.videocode = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("videocode", "0");
			this.pixTag = getCurVideoKey();
			String[] array;
			if (this.pixTag.equals("5")) {
				array = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(SettingVideoSize.getCurVideoSize(this.pixTag, VideoManagerService.getDefault().getCurrentAction()), "2,20,1600").split(",");
			} else if (this.pixTag.equals("6")) {
				array = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(SettingVideoSize.getCurVideoSize(this.pixTag, VideoManagerService.getDefault().getCurrentAction()), "1,10,4000").split(",");
			} else {
				array = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(SettingVideoSize.getCurVideoSize(this.pixTag, VideoManagerService.getDefault().getCurrentAction()), "1,10,6400").split(",");
			}
			if (array.length == 3) {
				this.iframe = Integer.parseInt(array[0]);
				this.frame = Integer.parseInt(array[1]);
				this.netrate = Integer.parseInt(array[2]) / 8 * 1000;
				MyLog.e("TANGJIAN", "netrate000: " + this.netrate);
			} else {
				this.iframe = 1;
				this.frame = 10;
				this.netrate = 300000;
			}
			if (this.iframe == 0 || this.frame == 0 || this.netrate == 0) {
				this.iframe = 1;
				this.frame = 10;
				this.netrate = 300000;
			}
			MyLog.i("CameraCall", "consult pix:" + this.pixTag + " videocode:" + this.videocode);
			final DisplayMetrics displayMetrics = new DisplayMetrics();
			this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			this.widthPix = displayMetrics.widthPixels;
			this.heightPix = displayMetrics.heightPixels;
			this.isVideoCall = default1.isCurrentVideoCall();
			this.isMonitor = default1.isCurrentVideoMonitor();
			this.isUpload = default1.isCurrentVideoUpload();
			this.isOutGoing = default1.isVideoOutgoingCall();
			if (Build.MODEL.toLowerCase().contains("lter") && this.isVideoCall) {
				Receiver.engine(this.mContext).togglemute();
				this.mutebtn.setImageResource(R.drawable.call_unmute);
			}
			if (this.videoport != 0) {
				this.buildRtpStack(this.remoteview);
				this.InitH264Encoder();
			}
			final boolean sendVideoData = default1.isSendVideoData();
			final VideoParamter remoteVideoControlParamter = default1.getRemoteVideoControlParamter();
			if (MemoryMg.getInstance().isSendOnly && remoteVideoControlParamter != null && !remoteVideoControlParamter.isVideoDispatch()) {
				default1.resumeSendVideoData();
			}
			if (sendVideoData) {
				this.localLp = (RelativeLayout.LayoutParams) this.localview.getLayoutParams();
				this.remoteLp = (RelativeLayout.LayoutParams) this.remoteview.getLayoutParams();
				if ((this.isOutGoing && this.isUpload) || (!this.isOutGoing && this.isMonitor) || MemoryMg.getInstance().isReceiverOnly) {
					this.localLp.leftMargin = 0;
					this.localLp.topMargin = 0;
					this.localLp.width = this.widthPix;
					this.localLp.height = this.heightPix;
					this.localview.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
					this.localview.getBackground().setAlpha(0);
					this.remoteview.setVisibility(View.GONE);
					this.onlyBigViewClick = true;
				} else {
					int px2dip;
					int px2dip2;
					if (Build.MODEL.equals("HT200")) {
						px2dip = 320;
						px2dip2 = 240;
					} else {
						px2dip = this.px2dip(320);
						px2dip2 = this.px2dip(240);
					}
					int n = px2dip;
					int n2 = px2dip2;
					if (this.widthPix >= 1080) {
						n = px2dip;
						n2 = px2dip2;
						if (this.heightPix >= 1080) {
							n = 400;
							n2 = 300;
						}
					}
					final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.localview.getLayoutParams();
					if (DeviceVideoInfo.isHorizontal && this.heightPix > this.widthPix) {
						layoutParams.width = n;
						layoutParams.height = n2;
						layoutParams.leftMargin = this.heightPix - n - this.dip2px(20);
						layoutParams.topMargin = this.widthPix - n2 - this.dip2px(20);
					} else {
						layoutParams.width = n;
						layoutParams.height = n2;
						if (Build.MODEL.equals("HT200")) {
							layoutParams.leftMargin = this.widthPix - n;
							layoutParams.topMargin = this.heightPix - n2;
						} else {
							layoutParams.leftMargin = this.widthPix - n - this.dip2px(20);
							layoutParams.topMargin = this.heightPix - n2 - this.dip2px(20);
						}
					}
					this.localview.setLayoutParams((ViewGroup.LayoutParams) layoutParams);
					this.remoteview.getBackground().setAlpha(0);
					this.setRemoteSize();
				}
				this.localSurfaceHolder = this.localview.getHolder();
				this.callback = new PreviewCallBack();
				this.localSurfaceHolder.addCallback((SurfaceHolder.Callback) this.callback);
			} else {
				this.localview.setVisibility(View.GONE);
				this.localLp = (RelativeLayout.LayoutParams) this.localview.getLayoutParams();
				this.remoteLp = (RelativeLayout.LayoutParams) this.remoteview.getLayoutParams();
				this.remoteview.getBackground().setAlpha(0);
				if (this.rtpStack != null) {
					this.prewRunning = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							while (CameraCall.this.prewRunning) {
								CameraCall.this.rtpStack.SendEmptyPacket();
								try {
									Thread.sleep(2000L);
								} catch (InterruptedException ex) {
									ex.printStackTrace();
								}
							}
						}
					}).start();
				}
			}
			(this.mFilter = new IntentFilter()).addAction("com.zed3.sipua.ui_callscreen_finish");
			this.mFilter.addAction("com.ceiw.keyevent");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.single_2_group");
			this.mFilter.addAction("com.zed3.siupa.ui.restartcamera");
			this.mFilter.addAction("stream changed");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.group_status");
			this.mFilter.addAction("com.zed3.sipua.tmpgrp.invite");
			this.registerReceiver(this.quitRecv2, this.mFilter);
			if (MemoryMg.getInstance().isProgressBarTip) {
				this.progressbarlinear.setVisibility(View.GONE);
				if (MemoryMg.getInstance().User_3GTotal != 0.0) {
					if (this.handler.hasMessages(1)) {
						this.handler.removeMessages(1);
					}
					this.handler.sendEmptyMessage(1);
				}
			} else {
				this.flowlockbtn.setVisibility(View.GONE);
			}
			if (RtpStreamReceiver_signal.speakermode == 2) {
				this.setVolumeControlStream(0);
			}
//			MyLog.i("CameraCall", "create end:" + System.currentTimeMillis());
			MyLog.i("CameraCall", "oncreate");
			this.equeue = new TimeOutSyncBufferQueue<YUVData>();
			this.encodeataQueue = new TimeOutSyncBufferQueue<YUVData>();
			if (this.initMediaCodec()) {
				if (AudioModeUtils.isSpeakerPhoneOn()) {
					this.isSpeakLoud = true;
					this.speakerbtn.setImageResource(R.drawable.call_speaker_pressed);
				}
				if (!default1.isRecieveVideoData()) {
					this.remoteview.setVisibility(View.GONE);
					this.rotatebtn.setVisibility(View.GONE);
				}
				default1.registerEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
				MyLog.d("videoTrace", "Cameracall#onCreate() exit");
				this.initKeyBoard();
			}
		}
	}

	protected void onDestroy() {
		MyLog.d("videoTrace", "Cameracall#onDestroy() enter");
		super.onDestroy();
		this.running = false;
		AudioModeUtils.setSpeakerphoneChangeLister(null);
		if (this.mCallTime != null) {
			this.mCallTime.stop();
			this.mCallTime = null;
		}
		CallUtil.mCallBeginTime = 0L;
		CallUtil.mNumber = null;
		CallUtil.mName = null;
		if (UserAgent.isTempGrpCallMode && TempGroupCallUtil.mCall != null && TempGroupCallUtil.mCall.isOnCall() && !TextUtils.isEmpty((CharSequence) TempGroupCallUtil.tmpGrpName) && TempGroupCallUtil.arrayListMembers.size() > 0) {
			final Intent intent = new Intent();
			intent.setClass((Context) this, (Class) TempGrpCallActivity.class);
			intent.putStringArrayListExtra("groupMemberList", (ArrayList) TempGroupCallUtil.arrayListMembers);
			MyLog.i("tetete", "arrayListMembers size = " + TempGroupCallUtil.arrayListMembers.size());
			intent.putExtra("tempGroupName", TempGroupCallUtil.tmpGrpName);
			this.startActivity(intent);
		}
		if (!this.pttIdle) {
			this.pttIdle = true;
			this.onVolumeUp();
		}
		this.stopService(new Intent((Context) this, (Class) SensorCheckService.class));
		if (this.runable != null) {
			this.runable.stop();
		}
		if (this.mEncodeOutThread != null) {
			this.mEncodeOutThread.interrupt();
		}
		if (this.sizeChangeHandler.hasMessages(0)) {
			this.sizeChangeHandler.removeMessages(0);
		}
		if (this.sizeChangeHandler.hasMessages(1)) {
			this.sizeChangeHandler.removeMessages(1);
		}
		this.sizeChangeHandler = null;
		this.releaseEncoder();
		this.closeCamera();
		UserAgent.isCamerPttDialog = false;
		if (this.handler.hasMessages(1)) {
			this.handler.removeMessages(1);
		}
		if (this.handler.hasMessages(2)) {
			this.handler.removeMessages(2);
		}
		if (this.toneGenerator != null) {
			this.toneGenerator.stopTone();
			this.toneGenerator.release();
			this.toneGenerator = null;
		}
		if (this.VMS != null) {
			this.VMS.unregisterEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
		}
		this.sendBroadcast(new Intent("android.action.closeDemoCallScreen"));
		LanguageChange.upDateLanguage(SipUAApp.mContext);
		MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		if (this.mFilter != null) {
			this.unregisterReceiver(this.quitRecv2);
		}
	}

	void onFrame(final byte[] array, final long n) {
		try {
			this.encodeataQueue.push(new YUVData(array, n));
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void onImageLoadComplete(final int n, final Object o, final ImageView imageView, final boolean b) {
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n == 4) {
			return true;
		}
		if (n == 25 && (this.VMS.isCurrentVideoMonitor() || this.VMS.isCurrentVideoUpload() || MemoryMg.getInstance().isReceiverOnly)) {
			if (Build.MODEL.contains("Z508") || Build.MODEL.contains("Z506") || Build.MODEL.contains("FH688") || Build.MODEL.contains("DATANG T98") || Build.MODEL.contains("Z306W") || Build.MODEL.toLowerCase().contains("lter")) {
				if (keyEvent.getRepeatCount() == 0) {
					this.mStreamMusicVolumn = this.AM.getStreamVolume(3);
					this.mStreamVoiceCallVolumn = this.AM.getStreamVolume(0);
				} else {
					this.AM.setStreamVolume(3, this.mStreamMusicVolumn, 8);
					this.AM.setStreamVolume(0, this.mStreamVoiceCallVolumn, 8);
				}
			} else {
				if (this.pttIdle) {
					this.pttIdle = false;
					return this.onVolumeDown();
				}
				this.pttIdle = true;
				return this.onVolumeUp();
			}
		}
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
		return super.onKeyUp(n, keyEvent);
	}

	protected void onPause() {
		super.onPause();
		this.isPaused = true;
		MyLog.i("debug_h", "onPause called");
		MyLog.i("CameraCall", "onPaues()");
	}

	public void onQueryComplete(final int n, final Object o, final CallerInfo callerInfo) {
	}

	protected void onResume() {
		MyLog.d("videoTrace", "Cameracall#onResume() enter");
		super.onResume();
		MyLog.i("debug_h", "onresume called");
//		MyLog.i("CameraCall", "onResume  begin " + System.currentTimeMillis());
		this.ShowCallNumAndTime();
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			if (ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
				this.speakerbtn.setVisibility(View.GONE);
			}
		} else if (this.VMS.isCurrentVideoCall()) {
			this.speakerbtn.setVisibility(View.VISIBLE);
		}
//		MyLog.i("CameraCall", "onResume end" + System.currentTimeMillis());
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		MyLog.d("videoTrace", "Cameracall#onResume() check state");
		if (Receiver.isCallNotificationNeedClose() && callState == CallManager.CallState.IDLE) {
			MyLog.d("videoTrace", "Cameracall#onResume() enter finish");
			this.finish();
		}
//		MyLog.i("CameraCall", "onResume end" + System.currentTimeMillis());
//		MyLog.i("CameraCall", "onResume end" + System.currentTimeMillis());
//		if (this.t == null && Receiver.call_state != 0) {
//			if (this.numTxt != null) {
//				this.numTxt.setText((CharSequence) "");
//			}
//			this.running = true;
//			(this.t = new Thread() {
//				@Override
//				public void run() {
//					Label_0159_Outer:
//					while (CameraCall.this.running) {
//						Label_0183:
//						{
//							if (CameraCall.this.numTxt == null || CameraCall.this.len == CameraCall.this.numTxt.getText().length()) {
//								break Label_0183;
//							}
//							final long elapsedRealtime = SystemClock.elapsedRealtime();
//							if (CameraCall.this.numTxt.getText().length() > CameraCall.this.len) {
//								final SipdroidEngine engine = Receiver.engine(Receiver.mContext);
//								final Editable text = CameraCall.this.numTxt.getText();
//								final CameraCall this .0 = CameraCall.this;
//								final int access .18 = this .0.len;
//								CameraCall.access .19 (this .0, access .18 + 1);
//								engine.info(text.charAt(access .18),250);
//							}
//							final long n = 250L - (SystemClock.elapsedRealtime() - elapsedRealtime);
//							while (true) {
//								if (n <= 0L) {
//									break Label_0159;
//								}
//								try {
//									Thread.sleep(n);
//									try {
//										if (!CameraCall.this.running) {
//											continue Label_0159_Outer;
//										}
//										Thread.sleep(250L);
//									} catch (InterruptedException ex) {
//									}
//									continue Label_0159_Outer;
//									try {
//										Thread.sleep(1000L);
//									} catch (InterruptedException ex2) {
//									}
//								} catch (InterruptedException ex3) {
//									continue;
//								}
//								break;
//							}
//						}
//					}
//					CameraCall.this.t = null;
//				}
//			}).start();
//		}
		MyLog.d("videoTrace", "Cameracall#onResume() exit");
	}

	public void onSpeakerphoneOnChanged(final boolean isSpeakLoud) {
		this.isSpeakLoud = isSpeakLoud;
		this.runOnUiThread((Runnable) new Runnable() {
			@Override
			public void run() {
				if (CameraCall.this.speakerbtn != null) {
					final ImageView speakerbtn = CameraCall.this.speakerbtn;
					int imageResource;
					if (CameraCall.this.isSpeakLoud) {
						imageResource = R.drawable.call_speaker_pressed;
					} else {
						imageResource = R.drawable.call_speaker_pressed0;
					}
					speakerbtn.setImageResource(imageResource);
				}
			}
		});
	}

	protected void onStop() {
		MyLog.d("videoTrace", "Cameracall#onStop() enter");
		super.onStop();
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
//				this.mToneGenerator.startTone((int) CameraCall.mToneMap.get(t), 150);
			}
			// monitorexit(o)
		}
	}

	public int px2dip(int n) {
		final float n2 = n / this.getResources().getDisplayMetrics().density;
		if (n >= 0) {
			n = 1;
		} else {
			n = -1;
		}
		return (int) (n * 0.5f + n2);
	}

	public void reject() {
		final CallManager manager = CallManager.getManager();
		if (this.mCallParams != null && this.mCallParams.getCallType() == CallManager.CallType.VIDEO) {
			manager.setUserAgentVideoCall(manager.getCall(this.mCallParams.getCallType(), this.mCallParams.getCallId()));
		}
		CallUtil.rejectVideoCall();
	}

	public void viewResize(final SurfaceView surfaceView) {
		if (surfaceView == this.localview) {
			if (!this.isLocalRemoteChanged) {
				surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.remoteLp);
				return;
			}
			surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
		} else {
			if (!this.isLocalRemoteChanged) {
				surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
				return;
			}
			surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.remoteLp);
		}
	}

	class encodeOutSendRunnable implements Runnable {
		MediaCodec.BufferInfo bufInfo;
		byte[] dst;
		List<FIFO> fifolist;
		boolean flag;
		ByteBuffer[] iBufs;
		FIFO lastFIFO;
		ByteBuffer[] oBufs;
		long timestamp;
		YUVData yuvData;

		public encodeOutSendRunnable() {
//			this.fifolist = new ArrayList<FIFO>();
//			this.lastFIFO = new FIFO();
			this.yuvData = null;
			this.flag = true;
			this.dst = null;
			this.oBufs = CameraCall.this.mMediaCodec.getOutputBuffers();
			this.bufInfo = new MediaCodec.BufferInfo();
		}

		@Override
		public void run() {
			// TODO
		}

		public void stop() {
			this.flag = false;
			CameraCall.this.sendThreadFlag = false;
		}
	}

	class sendRunnable extends VideoDecodeThread {
		public sendRunnable(final String s, final Collection collection) {
			super(s, collection);
		}

		private void onFrameInThread() {
			// TODO
		}

		@Override
		public void run() {
			Process.setThreadPriority(-19);
			this.onFrameInThread();
		}
	}
}
