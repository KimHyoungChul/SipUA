package com.zed3.sipua.ui.lowsdk;

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
import android.media.MediaFormat;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.constant.GroupConstant;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.h264_fu_process.RtpStack;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.CallManager.CallParams;
import com.zed3.sipua.CallManager.CallType;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.TimeOutSyncBufferQueue;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.phone.CallerInfo;
import com.zed3.sipua.phone.CallerInfoAsyncQuery.OnQueryCompleteListener;
import com.zed3.sipua.phone.ContactsAsyncHelper.OnImageLoadCompleteListener;
import com.zed3.sipua.ui.GrpCallNotify;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.Tools;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.EncoderBufferQueue;
import com.zed3.video.PhoneSupportTest;
import com.zed3.video.SensorCheckService;
import com.zed3.video.VideoManagerService;
import com.zed3.video.VideoManagerService.EndVideoCallHandler;
import com.zed3.video.VideoParamter;
import com.zed3.video.VideoUtils;
import com.zed3.video.YUVData;
import com.zed3.window.MyWindowManager;

import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.tools.Parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TranscribeActivity extends BaseActivity implements OnClickListener, OnQueryCompleteListener, OnImageLoadCompleteListener, EndVideoCallHandler {
	private static final String TAG = "CameraCall";
	private static boolean backflag;
	public static TranscribeActivity transcribeActivity;
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	private final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	private final String ACTION_SINGLE_2_GROUP = GroupConstant.ACTION_SINGLE_2_GROUP;
	private AudioManager AM;
	private VideoManagerService VMS;
	Button back;
	byte[] byteBuffer1;
	byte[] byteBuffer2;
	PreviewCallBack callback;
	private int cameraCurrLock = -1;
	private String cameraval = "";
	private int color_fmt = -1;
	int curAngle = 0;
	boolean eflag = false;
	TimeOutSyncBufferQueue<YUVData> encodeataQueue = null;
	boolean encoderChanging = false;
	EncoderBufferQueue equeue = null;
	private boolean flowflag = false;
	int frame = 0;
	Button giveup;
	Handler hd = new C13002();
	int height = 0;
	private int heightPix = 0;
	int iframe = 0;
	private boolean isChgVideo = false;
	boolean isFrontCamera = false;
	boolean isLocalRemoteChanged = false;
	boolean isMonitor = false;
	private boolean isMute = false;
	boolean isOutGoing = false;
	boolean isPaused = false;
	private boolean isShowViewFlag = false;
	private boolean isSpeakLoud = false;
	boolean isSurfaceDestroyed = false;
	boolean isUpload = true;
	boolean isVideoCall = false;
	long lastTimeStamp = -1;
	RelativeLayout layout;
	LayoutParams localLp;
	Parameters localParameters;
	private SurfaceHolder localSurfaceHolder;
	private SurfaceView localview;
	private CallParams mCallParams;
	private Chronometer mCallTime;
	private Camera mCameraDevice;
	Context mContext = null;
	Thread mEncodeOutThread = null;
	Thread mEncodeSendThread = null;
	private IntentFilter mFilter;
	MediaCodec mMediaCodec;
	private String mScreanWakeLockKey = TAG;
	private int mStreamMusicVolumn = 0;
	private int mStreamVoiceCallVolumn = 0;
	boolean needChangeUV = false;
	int netrate = 0;
	boolean onlyBigViewClick = false;
	private String pixTag = "";
	private boolean prewRunning = false;
	private double progressval = 0.0d;
	boolean pttIdle = true;
	private BroadcastReceiver quitRecv2 = new C12991();
	private int rag = 0;
	private boolean recTcpFlag = false;
	private RtpStack rtpStack = null;
	encodeOutSendRunnable runable = null;
	sendRunnable sendRunnableInstance = null;
	boolean sendThreadFlag = false;
	Handler sizeChangeHandler = new C13013();
	Timer timer;
	long timestamp;
	ToneGenerator toneGenerator = null;
	private boolean videoFlag = false;
	private String videocode = "";
	private int videoport = 0;
	private String videourl = "";
	ImageView voide;
	private boolean whichCameraFlag = true;
	int width = 0;
	private int widthPix = 0;

	class C12991 extends BroadcastReceiver {
		C12991() {
		}

		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			MyLog.d("videoTrace", "CameraCall#onReceive action = " + intent.getAction());
			if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_callscreen_finish")) {
				CallParams callParams = CallManager.getCallParams(intent);
				MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END username = " + TranscribeActivity.this.mCallParams.getUsername() + " , param user name = " + callParams.getUsername());
				if (callParams == null || !callParams.equals(TranscribeActivity.this.mCallParams)) {
					MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END not eques");
					return;
				}
				CallManager callManager = CallManager.getManager();
				if (TranscribeActivity.this.mCallParams != null && TranscribeActivity.this.mCallParams.getCallType() == CallType.VIDEO) {
					callManager.setUserAgentVideoCall(callManager.getCall(TranscribeActivity.this.mCallParams.getCallType(), TranscribeActivity.this.mCallParams.getCallId()));
				}
				TranscribeActivity.this.reject();
				TranscribeActivity.this.endCameraCall();
			} else if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
				Tools.FlowAlertDialog(TranscribeActivity.this);
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_SINGLE_2_GROUP)) {
				GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
				GroupCallUtil.setActionMode(GroupConstant.ACTION_SINGLE_2_GROUP);
				GrpCallNotify.startSelf(intent);
			} else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
				switch (extras.getInt(AudioUtil.KEY_STREAM_INT)) {
					case 0:
						TranscribeActivity.this.setVolumeControlStream(0);
						return;
					case 3:
						TranscribeActivity.this.setVolumeControlStream(3);
						return;
					default:
						return;
				}
			} else if (intent.getAction().equals(AudioUtil.ACTION_SPEAKERPHONE_STATE_CHANGED)) {
//				TranscribeActivity.this.isSpeakLoud = AudioUtil.getInstance().isSpeakerphoneOn().booleanValue();
			} else if (!intent.getAction().equalsIgnoreCase(DeviceVideoInfo.ACTION_RESTART_CAMERA)) {
			} else {
				if (!DeviceVideoInfo.onlyCameraRotate) {
					TranscribeActivity.this.encoderChanging = true;
					TranscribeActivity.this.releaseEncoder();
					TranscribeActivity.this.curAngle = DeviceVideoInfo.curAngle;
					TranscribeActivity.this.initMediaCodec();
					TranscribeActivity.this.encoderChanging = false;
				} else if (DeviceVideoInfo.isHorizontal) {
					if (TranscribeActivity.this.mCameraDevice == null) {
						return;
					}
					if (TranscribeActivity.this.isFrontCamera) {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
						if (DeviceVideoInfo.curAngle == 0) {
							TranscribeActivity.this.curAngle = 90;
						} else {
							TranscribeActivity.this.curAngle = 270;
						}
					} else if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
					} else {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					}
				} else if (TranscribeActivity.this.mCameraDevice == null) {
				} else {
					if (TranscribeActivity.this.isFrontCamera) {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					} else if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
					} else {
						TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
					}
				}
			}
		}
	}

	class C13002 extends Handler {
		C13002() {
		}

		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				TranscribeActivity.this.finish();
			} else if (msg.what == 1) {
//				TranscribeActivity.this.progressval = Tools.calculatePercent(MemoryMg.getInstance().User_3GRelTotal, MemoryMg.getInstance().User_3GTotal);
				if ((TranscribeActivity.this.progressval >= 0.6d || TranscribeActivity.this.progressval < 0.0d) && ((TranscribeActivity.this.progressval < 0.9d && TranscribeActivity.this.progressval >= 0.6d) || TranscribeActivity.this.progressval < 0.9d)) {
					TranscribeActivity.this.hd.sendMessageDelayed(TranscribeActivity.this.hd.obtainMessage(1), 8000);
				} else {
					TranscribeActivity.this.hd.sendMessageDelayed(TranscribeActivity.this.hd.obtainMessage(1), 8000);
				}
			} else if (msg.what == 2) {
				TranscribeActivity.this.hd.sendMessageDelayed(TranscribeActivity.this.hd.obtainMessage(2), 1000);
			}
		}
	}

	class C13013 extends Handler {
		C13013() {
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
//			switch (msg.what) {
//				case 1:
//					if (TranscribeActivity.this.isShowViewFlag) {
//						TranscribeActivity.this.isShowViewFlag = false;
//					}
//					TranscribeActivity.this.giveup.setVisibility(View.GONE);
//					TranscribeActivity.this.layout.setVisibility(View.GONE);
//					return;
//				default:
//					return;
//			}
		}
	}

	class C13024 implements Runnable {
		C13024() {
		}

		public void run() {
			while (TranscribeActivity.this.prewRunning) {
				TranscribeActivity.this.rtpStack.SendEmptyPacket();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class C13035 implements PreviewCallback {
		C13035() {
		}

		public void onPreviewFrame(byte[] data, Camera camera) {
			if (data != null && !TranscribeActivity.this.encoderChanging) {
				long timestamp;
				MyLog.i("previewData", "preview called");
				if (TranscribeActivity.this.lastTimeStamp == -1) {
					TranscribeActivity.this.lastTimeStamp = SystemClock.elapsedRealtime();
					timestamp = 0;
				} else {
					timestamp = (SystemClock.elapsedRealtime() - TranscribeActivity.this.lastTimeStamp) * 1000;
				}
				byte[] dst = new byte[data.length];
				switch (TranscribeActivity.this.color_fmt) {
					case 0:
						if (!TranscribeActivity.this.isFrontCamera) {
							switch (TranscribeActivity.this.curAngle) {
								case 0:
									if (!TranscribeActivity.this.needChangeUV) {
										VideoUtils.NV21Rotate90DegreeRightwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
										break;
									} else {
										VideoUtils.NV21Rotate90DegreeRightwiseMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
										break;
									}
//								case AdSize.LARGE_AD_HEIGHT /*90*/:
//									if (!TranscribeActivity.this.needChangeUV) {
//										VideoUtils.NV21Rotate180Degree(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//										break;
//									} else {
//										VideoUtils.NV21Rotate180DegreeMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//										break;
//									}
								case 180:
									if (!TranscribeActivity.this.needChangeUV) {
										VideoUtils.NV21Rotate90DegreeLeftwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
										break;
									} else {
										VideoUtils.NV21Rotate90DegreeLeftwiseMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
										break;
									}
								case 270:
									if (TranscribeActivity.this.needChangeUV) {
										VideoUtils.changeUV(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data);
									}
									dst = (byte[]) data.clone();
									break;
								default:
									break;
							}
						}
						switch (TranscribeActivity.this.curAngle) {
							case 0:
								if (!TranscribeActivity.this.needChangeUV) {
									VideoUtils.NV21Rotate90DegreeLeftwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								} else {
									VideoUtils.NV21Rotate90DegreeLeftwiseMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								}
//							case AdSize.LARGE_AD_HEIGHT /*90*/:
//								if (!TranscribeActivity.this.needChangeUV) {
//									VideoUtils.NV21Rotate180Degree(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//									break;
//								} else {
//									VideoUtils.NV21Rotate180DegreeMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//									break;
//								}
							case 180:
								if (!TranscribeActivity.this.needChangeUV) {
									VideoUtils.NV21Rotate90DegreeRightwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								} else {
									VideoUtils.NV21Rotate90DegreeRightwiseMi(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								}
							case 270:
								if (TranscribeActivity.this.needChangeUV) {
									VideoUtils.changeUV(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data);
								}
								dst = (byte[]) data.clone();
								break;
							default:
								break;
						}
					case 1:
						if (!TranscribeActivity.this.isFrontCamera) {
							switch (TranscribeActivity.this.curAngle) {
								case 0:
									VideoUtils.NV21ToI420pWithRotate90DegreeRightwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
//								case AdSize.LARGE_AD_HEIGHT /*90*/:
//									VideoUtils.NV21ToI420pWithRotate180Degree(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//									break;
								case 180:
									VideoUtils.NV21ToI420pWithRotate90DegreeLeftwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								case 270:
									VideoUtils.NV21ToI420p(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
									break;
								default:
									break;
							}
						}
						switch (TranscribeActivity.this.curAngle) {
							case 0:
								VideoUtils.NV21ToI420pWithRotate90DegreeLeftwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
								break;
//							case AdSize.LARGE_AD_HEIGHT /*90*/:
//								VideoUtils.NV21ToI420pWithRotate180Degree(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
//								break;
							case 180:
								VideoUtils.NV21ToI420pWithRotate90DegreeRightwise(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
								break;
							case 270:
								VideoUtils.NV21ToI420p(TranscribeActivity.this.getCameraWidth(), TranscribeActivity.this.getCameraHeight(), data, dst);
								break;
							default:
								break;
						}
				}
				camera.addCallbackBuffer(data);
				try {
					TranscribeActivity.this.equeue.push(new YUVData(dst, timestamp));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class C13046 implements AutoFocusMoveCallback {
		C13046() {
		}

		public void onAutoFocusMoving(boolean start, Camera camera) {
		}
	}

	class C13079 extends TimerTask {
		C13079() {
		}

		public void run() {
			if (TranscribeActivity.this.sizeChangeHandler != null) {
				TranscribeActivity.this.sizeChangeHandler.sendEmptyMessage(1);
			}
		}
	}

	public TranscribeActivity() {
		this.quitRecv2 = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final Bundle extras = intent.getExtras();
				MyLog.d("videoTrace", "CameraCall#onReceive action = " + intent.getAction());
				if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_callscreen_finish")) {
					final CallManager.CallParams callParams = CallManager.getCallParams(intent);
					MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END username = " + TranscribeActivity.this.mCallParams.getUsername() + " , param user name = " + callParams.getUsername());
					if (callParams != null && callParams.equals(TranscribeActivity.this.mCallParams)) {
						final CallManager manager = CallManager.getManager();
						if (TranscribeActivity.this.mCallParams != null && TranscribeActivity.this.mCallParams.getCallType() == CallManager.CallType.VIDEO) {
							manager.setUserAgentVideoCall(manager.getCall(TranscribeActivity.this.mCallParams.getCallType(), TranscribeActivity.this.mCallParams.getCallId()));
						}
						TranscribeActivity.this.reject();
						TranscribeActivity.this.endCameraCall();
						return;
					}
					MyLog.d("videoTrace", "CameraCall#onReceive action = ACTION_CALL_END not eques");
				} else {
					if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
						Tools.FlowAlertDialog((Context) TranscribeActivity.this);
						return;
					}
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.single_2_group")) {
						GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
						GroupCallUtil.setActionMode("com.zed3.sipua.ui_groupcall.single_2_group");
						GrpCallNotify.startSelf(intent);
						return;
					}
					if (intent.getAction().equals("stream changed")) {
						switch (extras.getInt("key stream int")) {
							default: {
							}
							case 0: {
								TranscribeActivity.this.setVolumeControlStream(0);
							}
							case 3: {
								TranscribeActivity.this.setVolumeControlStream(3);
							}
						}
					} else {
						if (intent.getAction().equals("speakerphone changed")) {
//							TranscribeActivity.access .1
//							(TranscribeActivity.this, AudioUtil.getInstance().isSpeakerphoneOn());
							return;
						}
						if (intent.getAction().equalsIgnoreCase("com.zed3.siupa.ui.restartcamera")) {
							if (!DeviceVideoInfo.onlyCameraRotate) {
								TranscribeActivity.this.encoderChanging = true;
								TranscribeActivity.this.releaseEncoder();
								TranscribeActivity.this.curAngle = DeviceVideoInfo.curAngle;
								TranscribeActivity.this.initMediaCodec();
								TranscribeActivity.this.encoderChanging = false;
								return;
							}
							if (DeviceVideoInfo.isHorizontal) {
								if (TranscribeActivity.this.mCameraDevice != null) {
									if (TranscribeActivity.this.isFrontCamera) {
										TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
										if (DeviceVideoInfo.curAngle == 0) {
											TranscribeActivity.this.curAngle = 90;
											return;
										}
										TranscribeActivity.this.curAngle = 270;
									} else {
										if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
											TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
											return;
										}
										TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
									}
								}
							} else if (TranscribeActivity.this.mCameraDevice != null) {
								if (TranscribeActivity.this.isFrontCamera) {
									TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
									return;
								}
								if (DeviceVideoInfo.curAngle == 0 || DeviceVideoInfo.curAngle == 180) {
									TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 90) % 360);
									return;
								}
								TranscribeActivity.this.mCameraDevice.setDisplayOrientation((DeviceVideoInfo.curAngle + 270) % 360);
							}
						}
					}
				}
			}
		};
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

	private void exitDialog(final Context context, final String s, final String text) {
		final AlertDialog create = new AlertDialog.Builder(context).create();
		create.show();
		final Window window = create.getWindow();
		window.setContentView(R.layout.shrew_exit_dialog);
		final TextView textView = (TextView) window.findViewById(R.id.btn_ok);
		((TextView) window.findViewById(R.id.msg_tv)).setText((CharSequence) text);
		textView.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.dismiss();
				MyLog.d("videoTrace", "Cameracall#exitDialog() enter");
				TranscribeActivity.this.reject();
				TranscribeActivity.this.endCameraCall();
			}
		});
		((TextView) window.findViewById(R.id.btn_cancel)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.cancel();
			}
		});
	}

	public static TranscribeActivity getTranscribeActivityObject() {
		return TranscribeActivity.transcribeActivity;
	}

	private boolean onAlwaysVolumeDown() {
		this.AM.setStreamVolume(3, this.mStreamMusicVolumn + 1, 8);
		this.AM.setStreamVolume(0, this.mStreamVoiceCallVolumn + 1, 8);
		return true;
	}

	private void onFrameInThread() {
		while (this.sendThreadFlag) {
			byte[] data = null;
			long timeStamp = 0L;
			Label_0212:
			{
				byte[] array = null;
				Label_0196:
				{
					try {
						final YUVData yuvData = this.encodeataQueue.pop();
						data = yuvData.getData();
						timeStamp = yuvData.getTimeStamp();
						if (data.length <= 0) {
							continue;
						}
						if (data.length <= 3 || data[0] != 0 || data[1] != 0 || data[2] != 0 || data[3] != 1) {
							break Label_0212;
						}
						array = new byte[data.length - 4];
						System.arraycopy(data, 4, array, 0, array.length);
						if ((array[0] & 0x1F) != 0x7) {
							break Label_0196;
						}
						final byte[] sps = this.findSPS(array);
						final byte[] array2 = new byte[array.length - sps.length];
						System.arraycopy(array, sps.length, array2, 0, array2.length);
						this.rtpStack.transmitH264FU(sps, sps.length, timeStamp);
						final byte[] pps = this.findPPS(array2);
						final byte[] array3 = new byte[array2.length - pps.length];
						System.arraycopy(array2, pps.length, array3, 0, array3.length);
						this.rtpStack.transmitH264FU(pps, pps.length, timeStamp);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					continue;
				}
				this.rtpStack.transmitH264FU(array, array.length, timeStamp);
				continue;
			}
			if ((data[0] & 0x1F) == 0x7) {
				final byte[] sps2 = this.findSPS(data);
				final byte[] array4 = new byte[data.length - sps2.length];
				System.arraycopy(data, sps2.length, array4, 0, array4.length);
				this.rtpStack.transmitH264FU(sps2, sps2.length, timeStamp);
				final byte[] pps2 = this.findPPS(array4);
				final byte[] array5 = new byte[array4.length - pps2.length];
				System.arraycopy(array4, pps2.length, array5, 0, array5.length);
				this.rtpStack.transmitH264FU(pps2, pps2.length, timeStamp);
			} else {
				this.rtpStack.transmitH264FU(data, data.length, timeStamp);
			}
		}
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

	private void setAntibanding(final Camera.Parameters param) {
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

	private void startPreview(final boolean p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//     4: ifnull          23
		//     7: aload_0
		//     8: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isPaused:Z
		//    11: ifeq            23
		//    14: aload_0
		//    15: invokespecial   com/zed3/sipua/ui/lowsdk/TranscribeActivity.closeCamera:()V
		//    18: aload_0
		//    19: iconst_0
		//    20: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isPaused:Z
		//    23: aload_0
		//    24: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//    27: ifnonnull       681
		//    30: ldc             "CameraCall"
		//    32: ldc_w           "startPreview"
		//    35: invokestatic    com/zed3/log/MyLog.v:(Ljava/lang/String;Ljava/lang/String;)V
		//    38: invokestatic    android/hardware/Camera.getNumberOfCameras:()I
		//    41: iconst_2
		//    42: if_icmpne       578
		//    45: iload_1
		//    46: ifeq            537
		//    49: aload_0
		//    50: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraval:Ljava/lang/String;
		//    53: ldc_w           "0"
		//    56: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//    59: ifeq            488
		//    62: aload_0
		//    63: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isChgVideo:Z
		//    66: ifne            438
		//    69: aload_0
		//    70: iconst_0
		//    71: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//    74: aload_0
		//    75: iconst_0
		//    76: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//    79: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//    82: aload_0
		//    83: iconst_0
		//    84: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//    87: getstatic       com/zed3/video/DeviceVideoInfo.isHorizontal:Z
		//    90: ifeq            594
		//    93: aload_0
		//    94: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//    97: iconst_0
		//    98: invokevirtual   android/hardware/Camera.setDisplayOrientation:(I)V
		//   101: aload_0
		//   102: aload_0
		//   103: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   106: invokevirtual   android/hardware/Camera.getParameters:()Landroid/hardware/Camera.Parameters;
		//   109: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   112: aload_0
		//   113: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   116: bipush          17
		//   118: invokevirtual   android/hardware/Camera.Parameters.setPreviewFormat:(I)V
		//   121: aload_0
		//   122: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   125: invokevirtual   android/hardware/Camera.Parameters.getSupportedPreviewFpsRange:()Ljava/util/List;
		//   128: invokeinterface java/util/List.iterator:()Ljava/util/Iterator;
		//   133: astore_3
		//   134: aload_3
		//   135: invokeinterface java/util/Iterator.hasNext:()Z
		//   140: ifeq            207
		//   143: aload_3
		//   144: invokeinterface java/util/Iterator.next:()Ljava/lang/Object;
		//   149: checkcast       [I
		//   152: astore_3
		//   153: aload_3
		//   154: iconst_1
		//   155: iaload
		//   156: aload_0
		//   157: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.frame:I
		//   160: sipush          1000
		//   163: imul
		//   164: if_icmplt       181
		//   167: aload_3
		//   168: iconst_0
		//   169: iaload
		//   170: aload_0
		//   171: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.frame:I
		//   174: sipush          1000
		//   177: imul
		//   178: if_icmple       207
		//   181: aload_0
		//   182: aload_3
		//   183: iconst_1
		//   184: iaload
		//   185: sipush          1000
		//   188: idiv
		//   189: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.frame:I
		//   192: aload_0
		//   193: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.frame:I
		//   196: bipush          30
		//   198: if_icmple       207
		//   201: aload_0
		//   202: bipush          30
		//   204: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.frame:I
		//   207: aload_0
		//   208: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   211: invokevirtual   android/hardware/Camera.Parameters.getSupportedFocusModes:()Ljava/util/List;
		//   214: astore_3
		//   215: aload_3
		//   216: ldc_w           "continuous-video"
		//   219: invokeinterface java/util/List.contains:(Ljava/lang/Object;)Z
		//   224: ifeq            606
		//   227: aload_0
		//   228: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   231: ldc_w           "continuous-video"
		//   234: invokevirtual   android/hardware/Camera.Parameters.setFocusMode:(Ljava/lang/String;)V
		//   237: aload_0
		//   238: iconst_2
		//   239: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.iframe:I
		//   242: ldc_w           "pixTest"
		//   245: new             Ljava/lang/StringBuilder;
		//   248: dup
		//   249: ldc_w           "setPreviewSize width : "
		//   252: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   255: aload_0
		//   256: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getCameraWidth:()I
		//   259: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   262: ldc_w           "height:"
		//   265: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   268: aload_0
		//   269: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getCameraHeight:()I
		//   272: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   275: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   278: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   281: aload_0
		//   282: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   285: aload_0
		//   286: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getCameraWidth:()I
		//   289: aload_0
		//   290: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getCameraHeight:()I
		//   293: invokevirtual   android/hardware/Camera.Parameters.setPreviewSize:(II)V
		//   296: getstatic       android/os/Build.MODEL:Ljava/lang/String;
		//   299: invokevirtual   java/lang/String.toLowerCase:()Ljava/lang/String;
		//   302: ldc_w           "datang"
		//   305: invokevirtual   java/lang/String.contains:(Ljava/lang/CharSequence;)Z
		//   308: ifeq            319
		//   311: aload_0
		//   312: aload_0
		//   313: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   316: invokespecial   com/zed3/sipua/ui/lowsdk/TranscribeActivity.setAntibanding:(Landroid/hardware/Camera.Parameters;)V
		//   319: aload_0
		//   320: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   323: aload_0
		//   324: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   327: invokevirtual   android/hardware/Camera.setParameters:(Landroid/hardware/Camera.Parameters;)V
		//   330: aload_0
		//   331: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   334: aconst_null
		//   335: invokevirtual   android/hardware/Camera.setPreviewCallbackWithBuffer:(Landroid/hardware/Camera.PreviewCallback;)V
		//   338: aload_0
		//   339: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   342: aload_0
		//   343: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.byteBuffer1:[B
		//   346: invokevirtual   android/hardware/Camera.addCallbackBuffer:([B)V
		//   349: aload_0
		//   350: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   353: aload_0
		//   354: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.byteBuffer2:[B
		//   357: invokevirtual   android/hardware/Camera.addCallbackBuffer:([B)V
		//   360: aload_0
		//   361: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   364: new             Lcom/zed3/sipua/ui/lowsdk/TranscribeActivity.5;
		//   367: dup
		//   368: aload_0
		//   369: invokespecial   com/zed3/sipua/ui/lowsdk/TranscribeActivity.5.<init>:(Lcom/zed3/sipua/ui/lowsdk/TranscribeActivity;)V
		//   372: invokevirtual   android/hardware/Camera.setPreviewCallbackWithBuffer:(Landroid/hardware/Camera.PreviewCallback;)V
		//   375: aload_0
		//   376: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   379: aload_0
		//   380: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localSurfaceHolder:Landroid/view/SurfaceHolder;
		//   383: invokevirtual   android/hardware/Camera.setPreviewDisplay:(Landroid/view/SurfaceHolder;)V
		//   386: aload_0
		//   387: iconst_1
		//   388: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.prewRunning:Z
		//   391: aload_0
		//   392: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   395: invokevirtual   android/hardware/Camera.startPreview:()V
		//   398: aload_0
		//   399: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   402: new             Lcom/zed3/sipua/ui/lowsdk/TranscribeActivity.6;
		//   405: dup
		//   406: aload_0
		//   407: invokespecial   com/zed3/sipua/ui/lowsdk/TranscribeActivity.6.<init>:(Lcom/zed3/sipua/ui/lowsdk/TranscribeActivity;)V
		//   410: invokevirtual   android/hardware/Camera.setAutoFocusMoveCallback:(Landroid/hardware/Camera.AutoFocusMoveCallback;)V
		//   413: ldc             "CameraCall"
		//   415: new             Ljava/lang/StringBuilder;
		//   418: dup
		//   419: ldc_w           "startpreview end:"
		//   422: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   425: invokestatic    java/lang/System.currentTimeMillis:()J
		//   428: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   431: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   434: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   437: return
		//   438: aload_0
		//   439: iconst_1
		//   440: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   443: aload_0
		//   444: iconst_1
		//   445: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//   448: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   451: aload_0
		//   452: iconst_1
		//   453: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//   456: goto            87
		//   459: astore_3
		//   460: aload_3
		//   461: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   464: iconst_1
		//   465: aload_0
		//   466: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getApplicationContext:()Landroid/content/Context;
		//   469: ldc_w           R.string.cameracall_camerarestart
		//   472: invokestatic    com/zed3/toast/MyToast.showToastInBg:(ZLandroid/content/Context;I)V
		//   475: aload_0
		//   476: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.reject:()V
		//   479: aload_0
		//   480: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.endCameraCall:()V
		//   483: aload_0
		//   484: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.finish:()V
		//   487: return
		//   488: aload_0
		//   489: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isChgVideo:Z
		//   492: ifne            516
		//   495: aload_0
		//   496: iconst_1
		//   497: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   500: aload_0
		//   501: iconst_1
		//   502: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//   505: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   508: aload_0
		//   509: iconst_1
		//   510: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//   513: goto            87
		//   516: aload_0
		//   517: iconst_0
		//   518: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   521: aload_0
		//   522: iconst_0
		//   523: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//   526: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   529: aload_0
		//   530: iconst_0
		//   531: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//   534: goto            87
		//   537: aload_0
		//   538: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   541: iconst_1
		//   542: iadd
		//   543: iconst_2
		//   544: irem
		//   545: istore_2
		//   546: aload_0
		//   547: iload_2
		//   548: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//   551: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   554: iload_2
		//   555: ifne            690
		//   558: iconst_0
		//   559: istore_1
		//   560: aload_0
		//   561: iload_1
		//   562: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//   565: aload_0
		//   566: aload_0
		//   567: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   570: iconst_1
		//   571: iadd
		//   572: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.cameraCurrLock:I
		//   575: goto            87
		//   578: aload_0
		//   579: iconst_0
		//   580: invokestatic    android/hardware/Camera.open:(I)Landroid/hardware/Camera;
		//   583: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   586: aload_0
		//   587: iconst_0
		//   588: putfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.isFrontCamera:Z
		//   591: goto            87
		//   594: aload_0
		//   595: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.mCameraDevice:Landroid/hardware/Camera;
		//   598: bipush          90
		//   600: invokevirtual   android/hardware/Camera.setDisplayOrientation:(I)V
		//   603: goto            101
		//   606: aload_3
		//   607: ldc_w           "continuous-picture"
		//   610: invokeinterface java/util/List.contains:(Ljava/lang/Object;)Z
		//   615: ifeq            237
		//   618: aload_0
		//   619: getfield        com/zed3/sipua/ui/lowsdk/TranscribeActivity.localParameters:Landroid/hardware/Camera.Parameters;
		//   622: ldc_w           "continuous-picture"
		//   625: invokevirtual   android/hardware/Camera.Parameters.setFocusMode:(Ljava/lang/String;)V
		//   628: goto            237
		//   631: astore_3
		//   632: ldc             "CameraCall"
		//   634: new             Ljava/lang/StringBuilder;
		//   637: dup
		//   638: ldc_w           "startPreview failed"
		//   641: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   644: aload_3
		//   645: invokevirtual   java/lang/Exception.toString:()Ljava/lang/String;
		//   648: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   651: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   654: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   657: aload_3
		//   658: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   661: iconst_1
		//   662: aload_0
		//   663: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.getApplicationContext:()Landroid/content/Context;
		//   666: ldc_w           R.string.cameracall_pixsupport
		//   669: invokestatic    com/zed3/toast/MyToast.showToastInBg:(ZLandroid/content/Context;I)V
		//   672: aload_0
		//   673: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.reject:()V
		//   676: aload_0
		//   677: invokevirtual   com/zed3/sipua/ui/lowsdk/TranscribeActivity.endCameraCall:()V
		//   680: return
		//   681: ldc             "CameraCall"
		//   683: ldc_w           "startpreview mCameraDevice is not null"
		//   686: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   689: return
		//   690: iconst_1
		//   691: istore_1
		//   692: goto            560
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  38     45     459    488    Ljava/lang/Exception;
		//  49     87     459    488    Ljava/lang/Exception;
		//  101    181    631    681    Ljava/lang/Exception;
		//  181    207    631    681    Ljava/lang/Exception;
		//  207    237    631    681    Ljava/lang/Exception;
		//  237    319    631    681    Ljava/lang/Exception;
		//  319    437    631    681    Ljava/lang/Exception;
		//  438    456    459    488    Ljava/lang/Exception;
		//  488    513    459    488    Ljava/lang/Exception;
		//  516    534    459    488    Ljava/lang/Exception;
		//  537    554    459    488    Ljava/lang/Exception;
		//  560    575    459    488    Ljava/lang/Exception;
		//  578    591    459    488    Ljava/lang/Exception;
		//  606    628    631    681    Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0101:
		//     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
		//     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2596)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		//     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		//     at us.deathmarine.luyten.FileSaver.access.300(FileSaver.java:45)
		//     at us.deathmarine.luyten.FileSaver.4.run(FileSaver.java:112)
		//     at java.lang.Thread.run(Thread.java:745)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
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

	public void endCameraCall() {
		MyLog.d("videoTrace", "Cameracall#endCameraCall() enter");
		if (!this.recTcpFlag) {
			return;
		}
		this.recTcpFlag = false;
		MyLog.i("endCameraCall", "endCameraCall");
		MyLog.i("CameraCall", "endcameracall begin:" + System.currentTimeMillis());
		this.prewRunning = false;
		MemoryMg.getInstance().isSendOnly = false;
		if (this.runable != null) {
			this.runable.stop();
		}
		if (this.mEncodeOutThread != null) {
			this.mEncodeOutThread.interrupt();
		}
		if (this.mEncodeSendThread != null) {
			this.mEncodeSendThread.interrupt();
		}
		if (this.rtpStack != null) {
			this.rtpStack.CloseUdpSocket();
		}
		this.closeCamera();
		this.releaseEncoder();
		MyLog.i("CameraCall", "endcameracall end:" + System.currentTimeMillis());
		this.VMS.clearRemoteVideoParameter();
		this.hd.sendEmptyMessageDelayed(0, 500L);
		MyLog.i("endCameraCall", "endCameraCall");
	}

	byte[] findI(final byte[] array) {
		final int n = -1;
		while (true) {
			for (int i = 0; i < array.length; ++i) {
				if (i < array.length - 3 && array[i] == 0 && array[i + 1] == 0 && array[i + 2] == 0 && array[i + 3] == 1 && (array[i + 4] & 0x1F) == 0x5) {
					final int n2 = i + 4;
					final byte[] array2 = new byte[array.length - n2];
					System.arraycopy(array, n2, array2, 0, array2.length);
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
		System.arraycopy(array, 4, array2, 0, array2.length);
		return array2;
	}

	byte[] findSPS(final byte[] array) {
		final int n = -1;
		while (true) {
			for (int i = 0; i < array.length; ++i) {
				if (i < array.length - 3 && array[i] == 0 && array[i + 1] == 0 && array[i + 2] == 0 && array[i + 3] == 1 && (array[i + 4] & 0x1F) == 0x8) {
					final byte[] array2 = new byte[i];
					System.arraycopy(array, 0, array2, 0, i);
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

	String getCurVideoKey() {
		if (this.cameraval.equals("0")) {
			return PreferenceManager.getDefaultSharedPreferences((Context) this).getString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
		}
		return PreferenceManager.getDefaultSharedPreferences((Context) this).getString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
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

	void initMediaCodec() {
		MyLog.d("videoTrace", "CameraCall#initMediaCodec() enter sdk version = " + Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT < 16) {
			MyToast.showToast(true, this.mContext, this.mContext.getString(R.string.version_unsupported));
			this.reject();
			this.endCameraCall();
			return;
		}
		Label_0178_Outer:
		while (true) {
			while (true) {
				while (true) {
					try {
						this.mMediaCodec = MediaCodec.createEncoderByType("video/avc");
						MyLog.i("pixTest", "encode width = " + this.getEncodeWidth() + " height = " + this.getEncodeHeight());
						final String[] split = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(SettingVideoSize.getCurVideoSize(this.pixTag, "com.zed3.action.VIDEO_UPLOAD"), "1,10,300").split(",");
						if (split.length == 3) {
							this.iframe = Integer.parseInt(split[0]);
							this.frame = Integer.parseInt(split[1]);
							this.netrate = Integer.parseInt(split[2]) * 1000;
							if (this.iframe == 0 || this.frame == 0 || this.netrate == 0) {
								this.iframe = 1;
								this.frame = 10;
								this.netrate = 300000;
							}
							final MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", this.getEncodeWidth(), this.getEncodeHeight());
							videoFormat.setInteger("bitrate", this.netrate);
							videoFormat.setInteger("frame-rate", this.frame);
							videoFormat.setInteger("i-frame-interval", this.iframe);
							this.color_fmt = DeviceVideoInfo.supportColor;
							videoFormat.setInteger("color-format", PhoneSupportTest.ColorFormatList[DeviceVideoInfo.supportColor][1]);
//							this.mMediaCodec.configure(videoFormat, (Surface) null, (MediaCrypto) null, 1);
							this.mMediaCodec.start();
							this.runable = new encodeOutSendRunnable();
							this.sendThreadFlag = true;
							this.sendRunnableInstance = new sendRunnable("encodeataQueue", this.encodeataQueue);
							(this.mEncodeSendThread = new Thread(this.sendRunnableInstance)).start();
							(this.mEncodeOutThread = new Thread(this.runable)).start();
							return;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						continue Label_0178_Outer;
					}
					break;
				}
				this.iframe = 1;
				this.frame = 10;
				this.netrate = 300000;
				continue;
			}
		}
	}

	void initVideoView() {
		this.back = (Button) this.findViewById(R.id.Buttonback);
		this.voide = (ImageView) this.findViewById(R.id.buttonvoide);
		this.giveup = (Button) this.findViewById(R.id.closebtn);
		(this.mCallTime = (Chronometer) this.findViewById(R.id.call_time)).start();
		this.layout = (RelativeLayout) this.findViewById(R.id.RelativeLayout);
		this.localview = (SurfaceView) this.findViewById(R.id.localvideoView);
		this.giveup.setOnClickListener((View.OnClickListener) this);
		this.localview.setOnClickListener((View.OnClickListener) this);
		this.back.setOnClickListener((View.OnClickListener) this);
		this.voide.setOnClickListener((View.OnClickListener) this);
		this.onBigWindowClickedEvent();
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
			this.giveup.setVisibility(View.GONE);
			this.layout.setVisibility(View.GONE);
			return;
		}
		this.isShowViewFlag = true;
		this.giveup.setVisibility(View.VISIBLE);
		this.layout.setVisibility(View.VISIBLE);
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		(this.timer = new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				if (TranscribeActivity.this.sizeChangeHandler == null) {
					return;
				}
				TranscribeActivity.this.sizeChangeHandler.sendEmptyMessage(1);
			}
		}, 3000L);
	}

	public void onClick(final View view) {
		boolean isLocalRemoteChanged = false;
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		MyLog.d("videoTrace", "Cameracall#onClick() check state = " + callState);
		if (Receiver.isCallNotificationNeedClose() && callState == CallManager.CallState.IDLE) {
			MyLog.d("videoTrace", "Cameracall#onClick() enter finish");
			this.finish();
		}
		switch (view.getId()) {
			default: {
			}
			case R.id.closebtn: {
				this.exitDialog((Context) this, this.getResources().getString(R.string.information), this.getResources().getString(R.string.end_vedio_transcribe));
			}
			case R.id.buttonvoide: {
				final ImageView imageView = (ImageView) view;
				if (this.isChgVideo) {
					this.isChgVideo = false;
					imageView.setImageResource(R.drawable.call_chgcamera0);
				} else {
					this.isChgVideo = true;
					imageView.setImageResource(R.drawable.call_chgcamera);
				}
				MyLog.d("videoTrace", "CameraCall#onClick() enter");
				this.closeCamera();
				this.setCameraDataParas(true);
				this.startPreview(false);
			}
			case R.id.localvideoView: {
				if (!this.isLocalRemoteChanged && !this.onlyBigViewClick) {
					this.viewResize(this.localview);
					this.localview.getBackground().setAlpha(0);
					if (!this.isLocalRemoteChanged) {
						isLocalRemoteChanged = true;
					}
					this.isLocalRemoteChanged = isLocalRemoteChanged;
					return;
				}
				this.onBigWindowClickedEvent();
			}
			case R.id.Buttonback: {
				TranscribeActivity.backflag = false;
				final Intent intent = new Intent((Context) this, (Class) MainActivity.class);
				intent.putExtra("backtotranscribe", true);
				this.startActivity(intent);
			}
		}
	}

	public void onConfigurationChanged(final Configuration configuration) {
		LanguageChange.upDateLanguage((Context) this);
		super.onConfigurationChanged(configuration);
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		MyLog.d("videoTrace", "TranscribeActivity#onCreate() enter");
		super.onCreate(bundle);
		TranscribeActivity.transcribeActivity = this;
		TranscribeActivity.backflag = true;
		(this.VMS = VideoManagerService.getDefault()).setCurrentAction("com.zed3.action.VIDEO_TRANSCRIBE");
		Log.d("getTaskId", new StringBuilder(String.valueOf(this.getTaskId())).toString());
		SettingVideoSize.setDefaultValue("com.zed3.action.VIDEO_UPLOAD", (Context) this);
		this.needChangeUV = this.needChangeUVinNV21();
		this.VMS.initVideoSettingColumns("com.zed3.action.VIDEO_UPLOAD");
		this.VMS.initSettingValue((Context) this, "com.zed3.action.VIDEO_UPLOAD");
		this.VMS.registerEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
		this.mCallParams = CallManager.getCallParams(this.getIntent());
		this.AM = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		this.mStreamMusicVolumn = this.AM.getStreamVolume(3);
		this.mStreamVoiceCallVolumn = this.AM.getStreamVolume(0);
		if (DeviceVideoInfo.isHorizontal) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			this.curAngle = 270;
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			this.curAngle = 0;
		}
		MyLog.i("CameraCall", "create begin" + System.currentTimeMillis());
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
		}
		this.recTcpFlag = true;
		if (VideoManagerService.bye) {
			VideoManagerService.bye = false;
			MyToast.showToast(true, (Context) this, R.string.error_transcribe);
			this.reject();
			this.endCameraCall();
		} else {
			if ("".equals(this.videourl) || this.videoport == 0) {
				MyLog.i("CameraCall", "AudioPort Camera_URL VideoPort  null");
				MyToast.showToastInBg(true, this.getApplicationContext(), R.string.cameracall_startfail);
				((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(2);
				this.finish();
				return;
			}
			if (Build.VERSION.SDK_INT >= 16) {
				DeviceVideoInfo.supportColor = PhoneSupportTest.getEncodeSupportColor();
			}
			this.videoFlag = false;
			this.isShowViewFlag = false;
			this.isSpeakLoud = false;
			this.isMute = false;
			UserAgent.isCamerPttDialog = true;
			this.whichCameraFlag = true;
			this.mScreanWakeLockKey = MyPowerManager.getInstance().wakeupScreen("CameraCall");
			MyWindowManager.getInstance().disableKeyguard(this);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(1024, 1024);
			this.setContentView(R.layout.cameratranscribe);
			this.initVideoView();
			this.startService(new Intent((Context) this, (Class) SensorCheckService.class));
			this.mContext = (Context) this;
			this.timer = new Timer();
			final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences((Context) this);
			final String camera_TYPE_FRONT_OR_POSTPOS = DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS;
			String s;
			if (this.VMS.isCurrentVideoTRANSCRIBE() || this.VMS.isCurrentVideoUpload() || this.VMS.isCurrentVideoMonitor()) {
				s = "0";
			} else {
				s = "1";
			}
			this.cameraval = defaultSharedPreferences.getString(camera_TYPE_FRONT_OR_POSTPOS, s);
			this.videocode = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("videocode", "0");
			this.pixTag = this.getCurVideoKey();
			if (NetChecker.is3G((Context) this)) {
			}
			final String[] split = PreferenceManager.getDefaultSharedPreferences((Context) this).getString(SettingVideoSize.getCurVideoSize(this.pixTag, "com.zed3.action.VIDEO_UPLOAD"), "1,10,300").split(",");
			if (split.length == 3) {
				this.iframe = Integer.parseInt(split[0]);
				this.frame = Integer.parseInt(split[1]);
				this.netrate = Integer.parseInt(split[2]) * 1000;
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
			this.isVideoCall = this.VMS.isCurrentVideoCall();
			this.isMonitor = this.VMS.isCurrentVideoMonitor();
			this.isOutGoing = this.VMS.isVideoOutgoingCall();
			if (this.videoport != 0) {
				if (this.rtpStack == null && Receiver.GetCurUA().getVedioSocket() != null) {
					this.rtpStack = new RtpStack(this.localview, null, (Context) this, this.videourl, this.videoport, Receiver.GetCurUA().getVedioSocket());
					this.InitH264Encoder();
				}
				this.InitH264Encoder();
			}
			final boolean sendVideoData = this.VMS.isSendVideoData();
			final VideoParamter remoteVideoControlParamter = this.VMS.getRemoteVideoControlParamter();
			if (MemoryMg.getInstance().isSendOnly && remoteVideoControlParamter != null && !remoteVideoControlParamter.isVideoDispatch()) {
				this.VMS.resumeSendVideoData();
			}
			if (sendVideoData) {
				this.localLp = (RelativeLayout.LayoutParams) this.localview.getLayoutParams();
				if (this.isUpload) {
					this.localLp.leftMargin = 0;
					this.localLp.topMargin = 0;
					this.localLp.width = this.widthPix;
					this.localLp.height = this.heightPix;
					this.localview.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
					this.localview.getBackground().setAlpha(0);
					this.onlyBigViewClick = true;
				}
				this.localSurfaceHolder = this.localview.getHolder();
				this.callback = new PreviewCallBack();
				this.localSurfaceHolder.addCallback((SurfaceHolder.Callback) this.callback);
			} else {
				this.localview.setVisibility(View.GONE);
				this.localLp = (RelativeLayout.LayoutParams) this.localview.getLayoutParams();
				if (this.rtpStack != null) {
					this.prewRunning = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							while (TranscribeActivity.this.prewRunning) {
								TranscribeActivity.this.rtpStack.SendEmptyPacket();
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
			(this.mFilter = new IntentFilter()).addAction("com.zed3.sipua.ui_groupcall.single_2_group");
			this.mFilter.addAction("com.zed3.siupa.ui.restartcamera");
			this.mFilter.addAction("com.zed3.sipua.ui_callscreen_finish");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.single_2_group");
			this.mFilter.addAction("stream changed");
			this.mFilter.addAction("speakerphone changed");
			this.registerReceiver(this.quitRecv2, this.mFilter);
			if (MemoryMg.getInstance().isProgressBarTip) {
				if (MemoryMg.getInstance().User_3GTotal != 0.0) {
					if (this.hd.hasMessages(1)) {
						this.hd.removeMessages(1);
					}
					this.hd.sendEmptyMessage(1);
				}
			} else {
				MyLog.i("CameraCall", "create end:" + System.currentTimeMillis());
			}
			MyLog.i("CameraCall", "oncreate");
			this.equeue = new EncoderBufferQueue();
			this.encodeataQueue = new TimeOutSyncBufferQueue<YUVData>();
			this.initMediaCodec();
			this.isSpeakLoud = true;
			this.VMS.registerEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
			MyLog.d("videoTrace", "Cameracall#onCreate() exit");
			if (VideoManagerService.bye) {
				VideoManagerService.bye = false;
				MyToast.showToast(true, (Context) this, R.string.error_transcribe);
				this.reject();
				this.endCameraCall();
			}
		}
	}

	protected void onDestroy() {
		MyLog.d("videoTrace", "Cameracall#onDestroy() enter");
		this.VMS.clearRemoteVideoParameter();
		this.stopService(new Intent((Context) this, (Class) SensorCheckService.class));
		if (this.runable != null) {
			this.runable.stop();
		}
		if (this.mEncodeOutThread != null) {
			this.mEncodeOutThread.interrupt();
		}
		if (this.mEncodeSendThread != null) {
			this.mEncodeSendThread.interrupt();
		}
		if (!this.pttIdle) {
			this.pttIdle = true;
			this.onVolumeUp();
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
		if (this.hd.hasMessages(1)) {
			this.hd.removeMessages(1);
		}
		if (this.hd.hasMessages(2)) {
			this.hd.removeMessages(2);
		}
		if (this.toneGenerator != null) {
			this.toneGenerator.stopTone();
			this.toneGenerator.release();
			this.toneGenerator = null;
		}
		if (this.VMS != null) {
			this.VMS.unregisterEndVideoCallHandler((VideoManagerService.EndVideoCallHandler) this);
		}
		VideoManagerService.bye = false;
		this.sendBroadcast(new Intent("android.action.closeDemoCallScreen"));
		LanguageChange.upDateLanguage(SipUAApp.mContext);
		MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
		MyWindowManager.getInstance().reenableKeyguard(this);
		if (this.mFilter != null) {
			this.unregisterReceiver(this.quitRecv2);
		}
		super.onDestroy();
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
		if (n == 25 && (this.VMS.isCurrentVideoTRANSCRIBE() || this.VMS.isCurrentVideoMonitor() || this.VMS.isCurrentVideoUpload() || MemoryMg.getInstance().isReceiverOnly)) {
			if (Build.MODEL.contains("Z508") || Build.MODEL.contains("Z506") || Build.MODEL.contains("FH688") || Build.MODEL.contains("DATANG T98") || Build.MODEL.contains("Z306W") || Build.MODEL.toLowerCase().contains("lter")) {
				Log.e("TANGJIAN", "MODEL:" + Build.MODEL);
				Log.e("TANGJIAN", "Count:" + keyEvent.getRepeatCount());
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

	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		Log.d("onNewIntent", "onNewIntentonNewIntentonNewIntent");
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
		MyLog.i("CameraCall", "onResume  begin " + System.currentTimeMillis());
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			ZMBluetoothManager.getInstance().isHeadSetEnabled();
		} else {
			MyLog.i("CameraCall", "onResume end" + System.currentTimeMillis());
		}
		final CallManager.CallState callState = CallManager.getManager().getCallState(this.mCallParams);
		MyLog.d("videoTrace", "Cameracall#onResume() check state");
		if (Receiver.isCallNotificationNeedClose() && callState == CallManager.CallState.IDLE) {
			MyLog.d("videoTrace", "Cameracall#onResume() enter finish");
			this.finish();
		}
		MyLog.d("videoTrace", "Cameracall#onResume() exit");
	}

	protected void onStop() {
		MyLog.d("videoTrace", "Cameracall#onStop() enter");
		super.onStop();
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
			if (this.isLocalRemoteChanged) {
				surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
			}
		} else if (!this.isLocalRemoteChanged) {
			surfaceView.setLayoutParams((ViewGroup.LayoutParams) this.localLp);
		}
	}

	class FIFO {
		long curERTime;
		long timeStamp;

		public FIFO() {
		}

		public FIFO(final long curERTime, final long timeStamp) {
			this.curERTime = curERTime;
			this.timeStamp = timeStamp;
		}
	}

	class PreviewCallBack implements SurfaceHolder.Callback {
		public void surfaceChanged(final SurfaceHolder surfaceHolder, final int n, final int n2, final int n3) {
			MyLog.i("surface", "preview changed!!!");
		}

		public void surfaceCreated(final SurfaceHolder surfaceHolder) {
			MyLog.i("surface", "preview create!!!");
			TranscribeActivity.this.closeCamera();
			TranscribeActivity.this.startPreview(TranscribeActivity.this.whichCameraFlag);
		}

		public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
			MyLog.i("surface", "preview destroyed!!!");
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
			this.fifolist = new ArrayList<FIFO>();
			this.lastFIFO = new FIFO();
			this.yuvData = null;
			this.flag = true;
			this.dst = null;
			this.oBufs = TranscribeActivity.this.mMediaCodec.getOutputBuffers();
			this.bufInfo = new MediaCodec.BufferInfo();
		}

		@Override
		public void run() {
			// TODO
		}

		public void stop() {
			this.flag = false;
			TranscribeActivity.this.sendThreadFlag = false;
		}
	}

	class sendRunnable implements Runnable {
		public sendRunnable(final String s, final Collection collection) {
		}

		private void onFrameInThread() {
			while (TranscribeActivity.this.sendThreadFlag) {
				byte[] data = null;
				long timeStamp = 0L;
				Label_0264:
				{
					byte[] array = null;
					Label_0245:
					{
						try {
							final YUVData yuvData = TranscribeActivity.this.encodeataQueue.pop();
							Log.w("GUOK", "I frame " + TranscribeActivity.this.encodeataQueue.size());
							data = yuvData.getData();
							timeStamp = yuvData.getTimeStamp();
							if (data.length <= 0) {
								continue;
							}
							if (data.length <= 3 || data[0] != 0 || data[1] != 0 || data[2] != 0 || data[3] != 1) {
								break Label_0264;
							}
							array = new byte[data.length - 4];
							System.arraycopy(data, 4, array, 0, array.length);
							if ((array[0] & 0x1F) != 0x7) {
								break Label_0245;
							}
							final byte[] sps = TranscribeActivity.this.findSPS(array);
							final byte[] array2 = new byte[array.length - sps.length];
							System.arraycopy(array, sps.length, array2, 0, array2.length);
							TranscribeActivity.this.rtpStack.transmitH264FU(sps, sps.length, timeStamp);
							final byte[] pps = TranscribeActivity.this.findPPS(array2);
							final byte[] array3 = new byte[array2.length - pps.length];
							System.arraycopy(array2, pps.length, array3, 0, array3.length);
							TranscribeActivity.this.rtpStack.transmitH264FU(pps, pps.length, timeStamp);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
						continue;
					}
					TranscribeActivity.this.rtpStack.transmitH264FU(array, array.length, timeStamp);
					continue;
				}
				if ((data[0] & 0x1F) == 0x7) {
					final byte[] sps2 = TranscribeActivity.this.findSPS(data);
					final byte[] array4 = new byte[data.length - sps2.length];
					System.arraycopy(data, sps2.length, array4, 0, array4.length);
					TranscribeActivity.this.rtpStack.transmitH264FU(sps2, sps2.length, timeStamp);
					final byte[] pps2 = TranscribeActivity.this.findPPS(array4);
					final byte[] array5 = new byte[array4.length - pps2.length];
					System.arraycopy(array4, pps2.length, array5, 0, array5.length);
					TranscribeActivity.this.rtpStack.transmitH264FU(pps2, pps2.length, timeStamp);
				} else {
					TranscribeActivity.this.rtpStack.transmitH264FU(data, data.length, timeStamp);
				}
			}
		}

		@Override
		public void run() {
			Process.setThreadPriority(-19);
			this.onFrameInThread();
		}
	}
}
