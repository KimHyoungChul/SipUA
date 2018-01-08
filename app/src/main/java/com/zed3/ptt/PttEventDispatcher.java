package com.zed3.ptt;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.DemoCallScreen;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;
import com.zed3.video.VideoManagerService;

public final class PttEventDispatcher {
	private static final String TAG = "PttEventDispatcher";
	public static PttEvent sPttEvent;
	private StringBuilder builder;

	static {
		PttEventDispatcher.sPttEvent = PttEvent.PTT_UP;
	}

	private PttEventDispatcher() {
		this.builder = new StringBuilder();
	}

	private void clearBuilder() {
		if (this.builder.length() > 0) {
			this.builder.delete(0, this.builder.length());
		}
	}

	public static PttEventDispatcher getInstance() {
		return InstanceCreater.sInstance;
	}

	private void interceptEvent(final PttEvent pttEvent) {
	}

	private void onPttDown(final PttEvent pttEvent) {
		this.clearBuilder();
		this.builder.append(" onPttDown()");
		if (Receiver.call_state == 1) {
			this.builder.append(" UserAgent.UA_STATE_INCOMING_CALL answercall()");
			if (DemoCallScreen.getInstance() != null) {
				this.builder.append(" DemoCallScreen.getInstance() != null  DemoCallScreen.answerCall()");
				DemoCallScreen.getInstance().answerCall();
			} else {
				this.builder.append(" CallUtil.answerCall()");
				CallUtil.answerCall();
			}
		} else if (UserAgent.ua_ptt_mode) {
			this.builder.append(" GroupCallUtil.makeGroupCall(true, true)");
			GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
		} else if (Receiver.call_state == 3) {
			this.builder.append(" UserAgent.UA_STATE_INCALL setNeedWriteAudioData(false)");
			RtpStreamSenderUtil.reCheckNeedSendMuteData("PttEventDispatcher");
		}
		LogUtil.makeLog("PttEventDispatcher", this.builder.toString());
	}

	private void onPttUp(final PttEvent pttEvent) {
		this.clearBuilder();
		if (Receiver.call_state == 1) {
			this.builder.append(" UA_STATE_INCOMING_CALL GroupCallUtil.makeGroupCall(false, true);");
			GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
		} else if (UserAgent.ua_ptt_mode) {
			this.builder.append(" GroupCallUtil.makeGroupCall(false, true);");
			GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
		} else if (Receiver.call_state == 3) {
			this.builder.append(" UserAgent.UA_STATE_INCALL setNeedWriteAudioData(true)");
			GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
		}
		LogUtil.makeLog("PttEventDispatcher", this.builder.toString());
	}

	public boolean dispatch(final PttEvent sPttEvent) {
		while (true) {
			synchronized (this) {
				final VideoManagerService default1 = VideoManagerService.getDefault();
				final CallManager manager = CallManager.getManager();
				if (default1.isCurrentVideoCall() || manager.existAudioCall()) {
					return false;
				}
				this.interceptEvent(PttEventDispatcher.sPttEvent = sPttEvent);
				if (PttEvent.PTT_DOWN == sPttEvent) {
					this.onPttDown(sPttEvent);
				} else if (PttEvent.PTT_UP == sPttEvent) {
					this.onPttUp(sPttEvent);
				}
			}
			return true;
		}
	}

	private static final class InstanceCreater {
		public static PttEventDispatcher sInstance;

		static {
			InstanceCreater.sInstance = new PttEventDispatcher();
		}
	}

	public enum PttEvent {
		PTT_DOWN("PTT_DOWN", 0),
		PTT_UP("PTT_UP", 1);

		private PttEvent(final String s, final int n) {
		}
	}
}
