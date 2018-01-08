package com.zed3.bluetooth;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.zed3.audio.AudioUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.media.mediaButton.MediaButtonReceiver;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;

public class MyPhoneStateListener extends PhoneStateListener {
	public static final int CALL_STATE_IDLE = 0;
	public static final int CALL_STATE_OFFHOOK = 2;
	public static final int CALL_STATE_OUTGONING = 3;
	public static final int CALL_STATE_RINGING = 1;
	public static boolean systemCallMode;
	private ZMBluetoothManager mInstance;
	private int mLastMode;
	private int mPhoneState;
	private final String tag;

	static {
		MyPhoneStateListener.systemCallMode = false;
	}

	private MyPhoneStateListener() {
		this.mPhoneState = 0;
		this.tag = "MyPhoneStateListener";
	}

	public static MyPhoneStateListener getInstance() {
		return InstanceCreater.sInstance;
	}

	private void makeLog(final String s, final String s2) {
		ZMBluetoothManager.getInstance().makeLog(s, s2);
	}

	public String getPhoneStateStr(final int n) {
		switch (n) {
			default: {
				return "unkown state";
			}
			case 0: {
				return "CALL_STATE_IDLE";
			}
			case 1: {
				return "CALL_STATE_RINGING";
			}
			case 2: {
				return "CALL_STATE_OFFHOOK";
			}
			case 3: {
				return "CALL_STATE_OUTGONING";
			}
		}
	}

	void hangupGQTphone() {
		MyPhoneStateListener.systemCallMode = true;
		if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
			final UserAgent getCurUA = Receiver.GetCurUA();
			if (getCurUA != null) {
				getCurUA.hangup();
				getCurUA.HaltGroupCall();
			}
		}
	}

	public boolean isInCall() {
		final TelephonyManager telephonyManager = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getCallState() != 0 && telephonyManager.getSimState() == 5;
	}

	public void onCallStateChanged(final int n, final String s) {
		this.mInstance = ZMBluetoothManager.getInstance();
		final String string = "MyPhoneStateListener.onCallStateChanged(" + this.getPhoneStateStr(n) + ")";
		if (n == this.mPhoneState) {
			this.makeLog("MyPhoneStateListener", String.valueOf(string) + "state == mPhoneState ignore");
		} else {
			switch (n) {
				case 0: {
					this.makeLog("MyPhoneStateListener", "CALL_STATE_IDLE");
					this.rejoinCurGrp();
					break;
				}
				case 1: {
					this.hangupGQTphone();
					this.mLastMode = AudioUtil.getInstance().getMode();
					this.makeLog("MyPhoneStateListener", "CALL_STATE_RINGING RINGING :" + s);
					break;
				}
				case 2: {
					this.hangupGQTphone();
					this.mLastMode = AudioUtil.getInstance().getMode();
					this.makeLog("MyPhoneStateListener", "CALL_STATE_OFFHOOK OFFHOOK :" + s);
					break;
				}
			}
			this.onPhoneStateChanged(n);
			if (MyPhoneStateListener.systemCallMode) {
				SipUAApp.restoreVoice();
			}
		}
	}

	public void onPhoneStateChanged(final int mPhoneState) {
		final StringBuilder sb = new StringBuilder("onPhoneStateChanged(" + this.getPhoneStateStr(mPhoneState) + ")");
		final TelephonyManager telephonyManager = (TelephonyManager) SipUAApp.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		sb.append(" TelephonyManager.getSimState() is" + telephonyManager.getSimState());
		LogUtil.makeLog(" MyPhoneStateListener ", sb.toString());
		if (telephonyManager.getSimState() != 5) {
			return;
		}
		if (mPhoneState == this.mPhoneState) {
			sb.append(" state == mPhoneState ignore");
			this.makeLog("MyPhoneStateListener", sb.toString());
			return;
		}
		switch (this.mPhoneState = mPhoneState) {
			default: {
				sb.append(" unkown state");
				break;
			}
			case 0: {
				if (this.mInstance != null && this.mInstance.isSPPConnected()) {
					sb.append(" send PTT_PA_OFF");
					this.mInstance.sendSPPMessage("PA_OFF");
				}
				if (UserAgent.ua_ptt_mode) {
					sb.append(" set MODE_SPEAKER");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(2000L);
								if (SipUAApp.isHeadsetConnected) {
									AudioUtil.getInstance().setAudioConnectMode(2);
									return;
								}
							} catch (InterruptedException ex) {
								ex.printStackTrace();
								if (SipUAApp.isHeadsetConnected) {
									AudioUtil.getInstance().setAudioConnectMode(2);
									return;
								}
								AudioUtil.getInstance().setAudioConnectMode(3);
								return;
							} finally {
								while (true) {
									if (SipUAApp.isHeadsetConnected) {
										AudioUtil.getInstance().setAudioConnectMode(2);
									}
									AudioUtil.getInstance().setAudioConnectMode(3);
									continue;
								}
							}
//							AudioUtil.getInstance().setAudioConnectMode(3);
						}
					}).start();
				} else {
					sb.append(" set mLastMode");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(2000L);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							} finally {
								AudioUtil.getInstance().setAudioConnectMode(MyPhoneStateListener.this.mLastMode);
							}
						}
					}).start();
				}
				if (SipUAApp.isHeadsetConnected) {
					MediaButtonReceiver.startReceive(SipUAApp.mContext);
					sb.append(" startReceive MediaButtonReceiver");
					break;
				}
				break;
			}
			case 3: {
				if (this.mInstance != null && this.mInstance.isSPPConnected()) {
					sb.append(" send PTT_PA_ON");
					this.mInstance.sendSPPMessage("PA_ON");
				}
				if (SipUAApp.isHeadsetConnected) {
					MediaButtonReceiver.stopReceive(SipUAApp.mContext);
					sb.append(" stopReceive MediaButtonReceiver");
				}
				if (GroupCallUtil.mIsPttDown) {
					sb.append(" makeGroupCall(false, true)");
					GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
				}
				if (CallUtil.isInCall()) {
					sb.append(" rejectCall");
					CallUtil.rejectCall();
					break;
				}
				break;
			}
			case 1: {
				if (this.mInstance != null && this.mInstance.isSPPConnected()) {
					sb.append(" send PTT_PA_ON");
					this.mInstance.sendSPPMessage("PA_ON");
				}
				if (SipUAApp.isHeadsetConnected) {
					MediaButtonReceiver.stopReceive(SipUAApp.mContext);
					sb.append(" stopReceive MediaButtonReceiver");
				}
				if (GroupCallUtil.mIsPttDown) {
					sb.append(" makeGroupCall(false, true)");
					GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
				}
				if (CallUtil.isInCall()) {
					sb.append(" rejectCall");
					CallUtil.rejectCall();
					break;
				}
				break;
			}
			case 2: {
				AudioUtil.getInstance().setAudioConnectMode(2);
				if (GroupCallUtil.mIsPttDown) {
					sb.append(" makeGroupCall(false, true)");
					GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
				}
				if (CallUtil.isInCall()) {
					sb.append(" rejectCall");
					CallUtil.rejectCall();
					break;
				}
				break;
			}
		}
		this.makeLog("MyPhoneStateListener", sb.toString());
	}

	void rejoinCurGrp() {
		MyPhoneStateListener.systemCallMode = false;
		if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
			final UserAgent getCurUA = Receiver.GetCurUA();
			if (getCurUA != null) {
				getCurUA.SetCurGrp(getCurUA.GetCurGrp(), true);
			}
		}
	}

	private static final class InstanceCreater {
		public static MyPhoneStateListener sInstance;

		static {
			InstanceCreater.sInstance = new MyPhoneStateListener();
		}
	}
}
