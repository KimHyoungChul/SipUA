package com.zed3.media.mediaButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.zed3.audio.AudioModeUtils;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

public class HeadsetPlugReceiver extends BroadcastReceiver {
	protected static final int CHECK_MUSIC_ACTIVE = 2;
	protected static final int HEADSET_PLUG_CONNECTED = 1;
	protected static final int HEADSET_PLUG_DISCONNECTED = 0;
	protected static final long REGISTER_AGAIN_DELAY = 5000L;
	protected static final int REGISTER_MEDIABUTTON_EVENT_RECEIVER = 3;
	private static final String TAG = "HeadsetPlugReceiver";
	private static IntentFilter intentFilter;
	private static boolean isStarted;
	private static AudioManager mAudioManager;
	private static HeadsetPlugReceiver mReceiver;
	private static boolean needRegisterMBERP;
	private static String[] needRegisterMBERPDevices;
	private static StateChangedHandler stateChangedHandler;
	private boolean isFirstChecking;
	protected boolean mIsMusicActive;
	private int mLastState;
	private int mStateChangeCount;

	static {
		HeadsetPlugReceiver.mReceiver = new HeadsetPlugReceiver();
		(HeadsetPlugReceiver.intentFilter = new IntentFilter()).addAction("android.intent.action.HEADSET_PLUG");
		HeadsetPlugReceiver.mAudioManager = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
		HeadsetPlugReceiver.needRegisterMBERPDevices = new String[]{"HUAWEI MT7", "DATANG T98"};
		HeadsetPlugReceiver.needRegisterMBERP = false;
	}

	public HeadsetPlugReceiver() {
		this.mLastState = 0;
		this.isFirstChecking = true;
	}

	private boolean needRegisterMediaButtonEventReceiverPeriodically(final StringBuilder sb) {
		final String[] needRegisterMBERPDevices = HeadsetPlugReceiver.needRegisterMBERPDevices;
		for (int length = needRegisterMBERPDevices.length, i = 0; i < length; ++i) {
			if (Build.MODEL.contains(needRegisterMBERPDevices[i])) {
				sb.append(" device " + Build.MODEL);
				sb.append(" needRegisterMBERP is true");
				return true;
			}
		}
		return false;
	}

	public static void onScreamStateChanged(final Boolean b) {
		synchronized (HeadsetPlugReceiver.class) {
			final StringBuilder sb = new StringBuilder("onScreamStateChanged(");
			String s;
			if (b) {
				s = "on";
			} else {
				s = "off";
			}
			final StringBuilder sb2 = new StringBuilder(sb.append(s).append(")").toString());
			if (!SipUAApp.isHeadsetConnected) {
				sb2.append(" SipUAApp.isHeadsetConnected is false ignore");
				LogUtil.makeLog("HeadsetPlugReceiver", sb2.toString());
			} else {
				final Message obtainMessage = HeadsetPlugReceiver.stateChangedHandler.obtainMessage();
				obtainMessage.what = 3;
				HeadsetPlugReceiver.stateChangedHandler.sendMessageDelayed(obtainMessage, 1000L);
				sb2.append(" sendMessage REGISTER_MEDIABUTTON_EVENT_RECEIVER Delayed 1000");
				LogUtil.makeLog("HeadsetPlugReceiver", sb2.toString());
			}
		}
	}

	private void onStateChanged(final int n) {
		synchronized (this) {
			this.mLastState = n;
			++this.mStateChangeCount;
			final Message obtainMessage = HeadsetPlugReceiver.stateChangedHandler.obtainMessage();
			obtainMessage.what = n;
			obtainMessage.arg1 = this.mStateChangeCount;
			HeadsetPlugReceiver.stateChangedHandler.sendMessageDelayed(obtainMessage, 1000L);
		}
	}

	@Deprecated
	public static void restartReceive(final Context context) {
		synchronized (HeadsetPlugReceiver.class) {
			LogUtil.makeLog("HeadsetPlugReceiver", "restartReceive()");
			HeadsetPlugReceiver.mReceiver.mLastState = 0;
			stopReceive(context);
			startReceive(context);
		}
	}

	public static void startReceive(final Context context) {
		synchronized (HeadsetPlugReceiver.class) {
			final StringBuilder sb = new StringBuilder("startReceive()");
			if (!HeadsetPlugReceiver.isStarted) {
				HeadsetPlugReceiver.isStarted = true;
				sb.append(" isStarted is false registerReceiver()");
				context.registerReceiver((BroadcastReceiver) HeadsetPlugReceiver.mReceiver, HeadsetPlugReceiver.intentFilter);
			} else {
				sb.append(" isStarted is true ignore");
			}
			LogUtil.makeLog("HeadsetPlugReceiver", sb.toString());
		}
	}

	public static void stopReceive(final Context context) {
		synchronized (HeadsetPlugReceiver.class) {
			final StringBuilder sb = new StringBuilder("stopReceive()");
			if (HeadsetPlugReceiver.isStarted) {
				HeadsetPlugReceiver.isStarted = false;
				sb.append(" isStarted is true unregisterReceiver()");
				context.unregisterReceiver((BroadcastReceiver) HeadsetPlugReceiver.mReceiver);
			} else {
				sb.append(" isStarted is false ignore");
			}
			LogUtil.makeLog("HeadsetPlugReceiver", sb.toString());
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		final StringBuilder sb = new StringBuilder("HeadsetPlugReceiver#onReceive()");
		if (intent.hasExtra("state")) {
			final int intExtra = intent.getIntExtra("state", 0);
			sb.append(" state = " + intExtra);
			if (intExtra == this.mLastState) {
				sb.append(" state == mLastState ignore");
				LogUtil.makeLog("HeadsetPlugReceiver", sb.toString());
				return;
			}
			if (HeadsetPlugReceiver.stateChangedHandler != null) {
				HeadsetPlugReceiver.stateChangedHandler.removeAllMessages();
			}
			HeadsetPlugReceiver.needRegisterMBERP = this.needRegisterMediaButtonEventReceiverPeriodically(sb);
			HeadsetPlugReceiver.stateChangedHandler = new StateChangedHandler();
			this.onStateChanged(intExtra);
			if (intent.getIntExtra("state", 0) == 0) {
				SipUAApp.lastHeadsetConnectTime = System.currentTimeMillis();
				SipUAApp.isHeadsetConnected = false;
				sb.append(" isHeadsetConnected is false");
				if (UserAgent.ua_ptt_mode) {
					if (GroupCallUtil.mIsPttDown) {
						sb.append(" GroupCallUtil.mIsPttDown is true, makeGroupCall(false, true)");
						GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
					}
					sb.append(" is ptt mode setMode AudioUtil.MODE_SPEAKER");
					AudioModeUtils.setAudioStyle(0, true);
				} else {
					sb.append(" is not ptt mode setMode AudioUtil.MODE_HOOK");
					if (CallUtil.isInCall()) {
						RtpStreamReceiver_signal.speakermode = 2;
					}
				}
			} else if (intent.getIntExtra("state", 0) == 1) {
				SipUAApp.isHeadsetConnected = true;
				sb.append(" isHeadsetConnected is true");
				if ((GroupCallUtil.getGroupCallState() != 4 || CallUtil.isInCall()) && UserAgent.ua_ptt_mode) {
					AudioModeUtils.setAudioStyle(0, false);
				}
				MediaButtonReceiver.stopReceive(SipUAApp.mContext);
				MediaButtonReceiver.startReceive(SipUAApp.mContext);
			}
			RtpStreamSenderUtil.reCheckNeedSendMuteData("HeadsetPlugReceiver#onReceive()");
		}
		LogUtil.makeLog("HeadsetPlugReceiver", sb.toString());
	}

	private class StateChangedHandler extends Handler {
		private String tag;

		public StateChangedHandler() {
			LogUtil.makeLog(this.tag = "StateChangedHandler", "new StateChangedHandler()");
		}

		public void handleMessage(final Message message) {
			final StringBuilder sb = new StringBuilder("handleMessage() what = " + message.what + ", arg1 = " + message.arg1);
			if (!HeadsetPlugReceiver.isStarted) {
				sb.append(" isStarted is false ignore");
				LogUtil.makeLog(this.tag, sb.toString());
				return;
			}
			if (!SipUAApp.isHeadsetConnected) {
				sb.append(" SipUAApp.isHeadsetConnected is flase ignore");
				LogUtil.makeLog(this.tag, sb.toString());
				return;
			}
			final Message obtainMessage = this.obtainMessage();
			switch (message.what) {
				default: {
					sb.append(" error message");
					break;
				}
				case 0: {
					sb.append(" HEADSET_PLUG_DISCONNECTED");
					if (message.arg1 != HeadsetPlugReceiver.this.mStateChangeCount) {
						sb.append(" msg.arg1 != mStateChangeCount ignore");
						break;
					}
					sb.append(" stop MediaButtonReceiver");
					MediaButtonReceiver.stopReceive(SipUAApp.mContext);
					break;
				}
				case 1: {
					sb.append(" HEADSET_PLUG_CONNECTED");
					if (message.arg1 != HeadsetPlugReceiver.this.mStateChangeCount) {
						sb.append(" msg.arg1 != mStateChangeCount ignore");
						break;
					}
					sb.append(" registerMediaButtonEventReceiver");
					MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
					this.removeMessages(3);
					if (HeadsetPlugReceiver.needRegisterMBERP) {
						sb.append(" send regiser again delay 5000");
						obtainMessage.what = 3;
						this.sendMessageDelayed(obtainMessage, 5000L);
						break;
					}
					break;
				}
				case 2: {
					sb.append(" CHECK_MUSIC_ACTIVE");
					final boolean musicActive = HeadsetPlugReceiver.mAudioManager.isMusicActive();
					if (musicActive != HeadsetPlugReceiver.this.mIsMusicActive) {
						HeadsetPlugReceiver.this.mIsMusicActive = musicActive;
						sb.append(" stop and start MediaButtonReceiver");
					}
					this.sendMessageDelayed(obtainMessage, 2000L);
					MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
					break;
				}
				case 3: {
					sb.append(" REGISTER_MEDIABUTTON_EVENT_RECEIVER");
					MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
					this.removeMessages(3);
					if (HeadsetPlugReceiver.needRegisterMBERP) {
						sb.append(" send regiser again delay 5000");
						obtainMessage.what = 3;
						this.sendMessageDelayed(obtainMessage, 5000L);
						break;
					}
					break;
				}
			}
			LogUtil.makeLog(this.tag, sb.toString());
		}

		public void removeAllMessages() {
			this.removeMessages(0);
			this.removeMessages(1);
			this.removeMessages(2);
			this.removeMessages(3);
			LogUtil.makeLog(this.tag, "removeAllMessages()");
		}
	}
}
