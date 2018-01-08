package com.zed3.sipua;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.log.MyLog;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.video.VideoManagerService;

import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.ExtendedCall;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;

public final class CallManager {
	public static final String EMPTY = "";
	public static final String EXTRA_CALL_ID = "com.zed3.extra.CALL_ID";
	public static final String EXTRA_CALL_TYPE = "com.zed3.extra.CALL_TYPE";
	public static final String EXTRA_CALL_USERNUMBER = "com.zed3.extra.CALL_USERNUMBER";
	private static CallManager sMangaer;
	private LinkedHashMap<String, ExtendedCall> mAudioCalls = new LinkedHashMap();
	private ExtendedCall mBackupAudioCall;
	private ExtendedCall mBackupVideoCall;
	private OnEndAudioCallHandler mEndAudioCallHandler;
	private Handler mHandler = new Handler();
	private OnRejectCallCompletedListener mLis;
	private LinkedHashMap<String, OnRejectCallCompletedListener> mRejectCallListeners = new LinkedHashMap();
	private LinkedHashMap<String, ExtendedCall> mVideoCalls = new LinkedHashMap();

	class C10532 implements Runnable {
		C10532() {
		}

		public void run() {
			CallManager.this.mEndAudioCallHandler.handle();
		}
	}

	public static final class CallParams {
		private String mCallId;
		private CallType mCallType = CallType.UNKNOW;
		private String mUserName;

		private CallParams() {
		}

		public static CallParams obtain(String callId, CallType callType, String username) {
			CallParams callParams = new CallParams();
			callParams.mCallId = callId;
			callParams.mCallType = callType;
			callParams.mUserName = username;
			return callParams;
		}

		public String getCallId() {
			return this.mCallId;
		}

		public CallType getCallType() {
			return this.mCallType;
		}

		public String getUsername() {
			return this.mUserName;
		}

		public String toString() {
			return "call id = " + this.mCallId + " , call type = " + this.mCallType + " , user name = " + this.mUserName;
		}

		public boolean equals(CallParams callParams) {
			if (getCallType() == callParams.getCallType() && getCallId().equals(callParams.getCallId()) && getUsername().equals(callParams.getUsername())) {
				return true;
			}
			return false;
		}

		public void recycle() {
			this.mCallId = null;
			this.mUserName = null;
			this.mCallType = null;
		}
	}

	public enum CallState {
		IDLE(0),
		INCOMING(1),
		OUTGOING(2),
		INCALL(3),
		HOLD(4),
		UNKNOW(-1);

		private int mState;

		private CallState(int state) {
			this.mState = state;
		}

		public int convert() {
			return this.mState;
		}

		public static CallState toCallState(int state) {
			CallState[] callStates = values();
			for (CallState callState : callStates) {
				if (callState.convert() == state) {
					return callState;
				}
			}
			return UNKNOW;
		}
	}

	public enum CallType {
		VIDEO,
		AUDIO,
		UNKNOW
	}

	public interface OnEndAudioCallHandler {
		void handle();
	}

	public interface OnRejectCallCompletedListener {
		void onCompledted(Call call);
	}

	public static synchronized CallManager getManager() {
		CallManager callManager;
		synchronized (CallManager.class) {
			if (sMangaer == null) {
				sMangaer = new CallManager();
			}
			callManager = sMangaer;
		}
		return callManager;
	}

	public boolean manageCall(Call call) {
		if (call == null || !(call instanceof ExtendedCall)) {
			return false;
		}
		ExtendedCall ec = (ExtendedCall) call;
		String callId = ec.getExtCallId();
		boolean isAudioCall = isAudioCall(call);
		boolean isVideoCall = isVideoCall(call);
		if (isAudioCall) {
			ec.setCallType(CallType.AUDIO);
			this.mAudioCalls.put(callId, ec);
		} else if (isVideoCall) {
			ec.setCallType(CallType.VIDEO);
			this.mVideoCalls.put(callId, ec);
		}
		return true;
	}

	public ExtendedCall getCall(CallState callState, String callerUsername) {
		ExtendedCall result = getCall(this.mVideoCalls, callState, callerUsername);
		return result == null ? getCall(this.mAudioCalls, callState, callerUsername) : result;
	}

	public ExtendedCall getVideoCall(CallState callState, String callerUsername) {
		return getCall(this.mVideoCalls, callState, callerUsername);
	}

	public ExtendedCall getAudioCall(CallState callState, String callerUsername) {
		return getCall(this.mAudioCalls, callState, callerUsername);
	}

	private ExtendedCall getCall(LinkedHashMap<String, ExtendedCall> calls, CallState callState, String callerUsername) {
		if (!(calls == null || TextUtils.isEmpty(callerUsername))) {
			for (Entry<String, ExtendedCall> mapItem : calls.entrySet()) {
				String callId = (String) mapItem.getKey();
				ExtendedCall call = (ExtendedCall) mapItem.getValue();
				if (callState == call.getCallState() && call.getCallerNumber().equals(callerUsername)) {
					return call;
				}
			}
		}
		return null;
	}

	private ExtendedCall getCall(LinkedHashMap<String, ExtendedCall> calls, CallState callState) {
		if (calls != null) {
			for (Entry<String, ExtendedCall> mapItem : calls.entrySet()) {
				String callId = (String) mapItem.getKey();
				ExtendedCall call = (ExtendedCall) mapItem.getValue();
				if (callState == call.getCallState()) {
					return call;
				}
			}
		}
		return null;
	}

	public boolean acceptCall(CallType callType, String callId) {
		ExtendedCall call = getCall(callType, callId);
		if (call != null) {
			call.setCallState(CallState.INCALL);
		}
		return false;
	}

	public boolean rejectCall(CallType callType, String callId) {
		return false;
	}

	public ExtendedCall getCall(CallType callType, String callId) {
		if (CallType.AUDIO == callType) {
			return (ExtendedCall) this.mAudioCalls.get(callId);
		}
		if (CallType.VIDEO == callType) {
			return (ExtendedCall) this.mVideoCalls.get(callId);
		}
		return null;
	}

	public CallState getCallState(CallType callType, String callId) {
		ExtendedCall ec = getCall(callType, callId);
		if (ec != null) {
			return ec.getCallState();
		}
		return CallState.UNKNOW;
	}

	public static String getCallExtId(Call call) {
		if (call == null || !(call instanceof ExtendedCall)) {
			return "";
		}
		return ((ExtendedCall) call).getExtCallId();
	}

	public CallType getCallType(Call call) {
		if (call == null || !(call instanceof ExtendedCall)) {
			return CallType.UNKNOW;
		}
		return ((ExtendedCall) call).getCallType();
	}

	public boolean isAudioCall(Call call) {
		if (getCallType(call) == CallType.AUDIO) {
			return true;
		}
		boolean existAudio = existMediaDescriptor(call, UserMinuteActivity.USER_AUDIO);
		if (existMediaDescriptor(call, UserMinuteActivity.USER_VIDEO) || !existAudio) {
			return false;
		}
		return true;
	}

	public boolean isVideoCall(Call call) {
		if (getCallType(call) == CallType.VIDEO) {
			return true;
		}
		return existMediaDescriptor(call, UserMinuteActivity.USER_VIDEO);
	}

	public boolean isVideoCallWithAudio(Call call) {
		return isVideoCall(call) && existMediaDescriptor(call, UserMinuteActivity.USER_AUDIO);
	}

	public boolean isPttGroupCall(Call call) {
		if (call == null) {
			MyLog.e("videoTrace", "UserAgent#isPttGroupCall() enter call is null");
			return false;
		}
		String localDescriptor = call.getLocalSessionDescriptor();
		String remoteDescriptor = call.getRemoteSessionDescriptor();
		MyLog.d("videoTrace", "UserAgent#isPttGroupCall() localSessionDescriptor = " + (TextUtils.isEmpty(localDescriptor) ? "null" : "not null"));
		MyLog.d("videoTrace", "UserAgent#isPttGroupCall() removeSessionDescriptor = " + (TextUtils.isEmpty(remoteDescriptor) ? "null" : "not null"));
		ExtendedCall extendedCall = (ExtendedCall) call;
		if ((TextUtils.isEmpty(localDescriptor) && TextUtils.isEmpty(remoteDescriptor)) || extendedCall.isGroupCall) {
			return true;
		}
		return false;
	}

	private boolean existMediaDescriptor(Call call, String mediaType) {
		boolean z = false;
		if (call != null) {
			String sessionDescriptor = call.getLocalSessionDescriptor();
			if (TextUtils.isEmpty(sessionDescriptor)) {
				sessionDescriptor = call.getRemoteSessionDescriptor();
			}
			if (!TextUtils.isEmpty(sessionDescriptor)) {
				Vector<MediaDescriptor> list = new SessionDescriptor(sessionDescriptor).getMediaDescriptors();
				z = false;
				for (int i = 0; i < list.size(); i++) {
					if (((MediaDescriptor) list.elementAt(i)).getMedia().getMedia().equals(mediaType)) {
						z = true;
					}
				}
			}
		}
		return z;
	}

	public CallManager setCallState(CallState state, Call call) {
		if (call != null && (call instanceof ExtendedCall)) {
			((ExtendedCall) call).setCallState(state);
		}
		return this;
	}

	public CallManager setCallerNumber(String callerUsername, Call call) {
		if (call != null && (call instanceof ExtendedCall)) {
			((ExtendedCall) call).setCallerNumber(callerUsername);
		}
		return this;
	}

	public String getCallerUsername(String caller) {
		if (TextUtils.isEmpty(caller)) {
			return "";
		}
		return new NameAddress(caller).getAddress().getUserName();
	}

	public String getCallId(CallState incoming, String userNum) {
		return getCallExtId(getCall(CallState.INCOMING, userNum));
	}

	public static CallParams getCallParams(Intent intent) {
		String callId = intent.getStringExtra(EXTRA_CALL_ID);
		String callType = intent.getStringExtra(EXTRA_CALL_TYPE);
		return CallParams.obtain(callId, !TextUtils.isEmpty(callType) ? CallType.valueOf(callType) : CallType.UNKNOW, intent.getStringExtra(EXTRA_CALL_USERNUMBER));
	}

	public static CallParams getAudioCallParams() {
		return CallParams.obtain(getCallExtId(getManager().getAudioCall(CallState.toCallState(Receiver.call_state), CallUtil.mNumber)), CallType.AUDIO, CallUtil.mNumber);
	}

	public static CallParams getVideoCallParams() {
		return CallParams.obtain(getCallExtId(getManager().getVideoCall(CallState.toCallState(Receiver.call_state), CallUtil.mNumber)), CallType.VIDEO, CallUtil.mNumber);
	}

	public void setUserAgentAudioCall(ExtendedCall audioCall) {
		UserAgent ua = Receiver.engine(SipUAApp.getAppContext()).GetCurUA();
		this.mBackupAudioCall = ua.getAudioCall();
		ua.setAudioCall(audioCall);
	}

	public void setUserAgentVideoCall(ExtendedCall videoCall) {
		UserAgent ua = Receiver.engine(SipUAApp.getAppContext()).GetCurUA();
		this.mBackupVideoCall = ua.getVideoCall();
		ua.setVideoCall(videoCall);
	}

	public void recoverVideoCall() {
		Receiver.engine(SipUAApp.getAppContext()).GetCurUA().setVideoCall(this.mBackupVideoCall);
	}

	public void recoverAudioCall() {
		Receiver.engine(SipUAApp.getAppContext()).GetCurUA().setAudioCall(this.mBackupAudioCall);
	}

	public void removeCall(Call call) {
		String callId = getCallExtId(call);
		CallType callType = getCallType(call);
		if (CallType.AUDIO == callType) {
			removeAudioCall(callId);
		} else if (CallType.VIDEO == callType) {
			removeVideoCall(callId);
		} else if (CallType.UNKNOW != callType) {
		} else {
			if (isVideoCall(call)) {
				removeVideoCall(callId);
			} else if (isAudioCall(call)) {
				removeAudioCall(callId);
			}
		}
	}

	private void removeAudioCall(String callId) {
		this.mAudioCalls.remove(callId);
	}

	private void removeVideoCall(String callId) {
		this.mVideoCalls.remove(callId);
	}

	public ExtendedCall getAudioInCall() {
		return getCall(this.mAudioCalls, CallState.INCALL);
	}

	public boolean existInAudioCall() {
		return getCall(this.mAudioCalls, CallState.INCALL) != null;
	}

	public boolean existIncommingAudioCall() {
		return getCall(this.mAudioCalls, CallState.INCOMING) != null;
	}

	public boolean existAudioCall(CallState callState) {
		return getCall(this.mAudioCalls, callState) != null;
	}

	public boolean existVideoCall(CallState callState) {
		return getCall(this.mVideoCalls, callState) != null;
	}

	public boolean existCall(CallState callState) {
		return existAudioCall(callState) || existVideoCall(callState);
	}

	public boolean existInVideoCall() {
		return existVideoCall(CallState.INCALL);
	}

	public CallParams getAudioCallParams(CallState incall) {
		ExtendedCall ec = getCall(this.mAudioCalls, incall);
		if (ec != null) {
			return CallParams.obtain(getCallExtId(ec), ec.getCallType(), ec.getCallerNumber());
		}
		return null;
	}

	public String getAudioCallNum(CallState state) {
		ExtendedCall ec = getCall(this.mAudioCalls, state);
		if (ec != null) {
			return ec.getPeerNumber();
		}
		return null;
	}

	public String getVideoCallNum(CallState state) {
		ExtendedCall ec = getCall(this.mVideoCalls, state);
		if (ec != null) {
			return ec.getPeerNumber();
		}
		return null;
	}

	public CallParams getVideoCallParams(CallState incall) {
		ExtendedCall ec = getCall(this.mVideoCalls, incall);
		if (ec != null) {
			return CallParams.obtain(getCallExtId(ec), ec.getCallType(), ec.getCallerNumber());
		}
		return null;
	}

	public CallParams getCallParams(CallState callState) {
		ExtendedCall ec = getCall(this.mAudioCalls, callState);
		if (ec != null) {
			return CallParams.obtain(getCallExtId(ec), ec.getCallType(), ec.getCallerNumber());
		}
		ec = getCall(this.mVideoCalls, callState);
		if (ec != null) {
			return CallParams.obtain(getCallExtId(ec), ec.getCallType(), ec.getCallerNumber());
		}
		return null;
	}

	public static void putExtras(CallParams callParams, Intent intent) {
		if (callParams != null) {
			intent.putExtra(EXTRA_CALL_ID, callParams.getCallId());
			intent.putExtra(EXTRA_CALL_TYPE, callParams.getCallType().toString());
			intent.putExtra(EXTRA_CALL_USERNUMBER, callParams.getUsername());
		}
	}

	public ExtendedCall getCall(String callId) {
		ExtendedCall ec = (ExtendedCall) this.mAudioCalls.get(callId);
		if (ec == null) {
			return (ExtendedCall) this.mVideoCalls.get(callId);
		}
		return ec;
	}

	public CallState getCallState(CallParams callParams) {
		if (callParams != null) {
			ExtendedCall call = getCall(callParams.mCallId);
			if (call != null) {
				return call.getCallState();
			}
		}
		return CallState.UNKNOW;
	}

	public int getCallConvertState(CallParams callParams) {
		if (callParams != null) {
			ExtendedCall call = getCall(callParams.mCallId);
			if (call != null) {
				return call.getCallState().convert();
			}
		}
		return CallState.UNKNOW.convert();
	}

	public synchronized int getVideoCallsCount() {
		return this.mVideoCalls.size();
	}

	public synchronized int getAudioCallsCount() {
		return this.mAudioCalls.size();
	}

	public synchronized void addOnRejectCallCompletedListener(String callId, OnRejectCallCompletedListener lis) {
		this.mRejectCallListeners.put(callId, lis);
	}

	public synchronized void removeOnRejectCallCompletedListener(String callId) {
		this.mRejectCallListeners.remove(callId);
	}

	public synchronized void addOnRejectCallCompletedListener(OnRejectCallCompletedListener lis) {
		this.mLis = lis;
	}

	public synchronized void removeOnRejectCallCompletedListener(OnRejectCallCompletedListener lis) {
		this.mLis = null;
	}

	public synchronized void addEndAudioCallHandler(OnEndAudioCallHandler lis) {
		this.mEndAudioCallHandler = lis;
	}

	public synchronized void removeEndAudioCallHandler(OnEndAudioCallHandler lis) {
		this.mEndAudioCallHandler = null;
	}

	public synchronized void dispatchAbortCompleted(final Call call) {
		if (this.mLis != null) {
			this.mHandler.postDelayed(new Runnable() {
				public void run() {
					CallManager.this.mLis.onCompledted(call);
				}
			}, 1000);
		}
	}

	public synchronized void rejectAllIncommingOrOutgoingCalls() {
		for (Entry value : this.mRejectCallListeners.entrySet()) {
			try {
				((OnRejectCallCompletedListener) value.getValue()).onCompledted(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void rejectAllCallsOfAudioRelated() {
		for (Entry<String, ExtendedCall> mapItem : this.mAudioCalls.entrySet()) {
			Receiver.GetCurUA().setAbortCall((ExtendedCall) mapItem.getValue());
			Receiver.GetCurUA().hangupWithoutRejoin();
		}
		boolean bVideoHangup = false;
		VideoManagerService VMS = VideoManagerService.getDefault();
		for (Entry<String, ExtendedCall> mapItem2 : this.mVideoCalls.entrySet()) {
			if (VMS.isCurrentVideoCall()) {
				Receiver.GetCurUA().setAbortCall((ExtendedCall) mapItem2.getValue());
				Receiver.GetCurUA().hangupWithoutRejoin();
				bVideoHangup = true;
			}
		}
		if (bVideoHangup) {
			VMS.clearRemoteVideoParameter();
		}
	}

	public synchronized void dispatchEndCall() {
		if (this.mEndAudioCallHandler != null) {
			this.mHandler.postDelayed(new C10532(), 1000);
		}
	}

	public String getCallerUsername(ExtendedCall abortCall) {
		return abortCall.getCallerNumber();
	}

	public boolean isGroupCall(Call call) {
		if (call != null) {
			return ((ExtendedCall) call).isGroupCall;
		}
		return false;
	}

	public static void printCallState(int state) {
		CallState[] callStates = CallState.values();
		for (CallState callState : callStates) {
			if (callState.convert() == state) {
				MyLog.d("videoTrace", " target state = " + state);
				return;
			}
		}
	}

	public ExtendedCall getVideoInCall() {
		return getCall(this.mVideoCalls, CallState.INCALL);
	}

	public ExtendedCall getVideoOutGoingCall() {
		return getCall(this.mVideoCalls, CallState.OUTGOING);
	}

	public boolean hasInCommingCall() {
		return (getCall(this.mAudioCalls, CallState.INCOMING) == null && getCall(this.mVideoCalls, CallState.INCOMING) == null) ? false : true;
	}

	public boolean existVideoCall() {
		return this.mVideoCalls.size() > 0;
	}

	public boolean existAudioCall() {
		return this.mAudioCalls.size() > 0;
	}

	public void printCalls() {
		StringBuffer resultBuffer = new StringBuffer();
		if (existVideoCall()) {
			resultBuffer.append("VIDEO_OUTGOING:").append(getVideoCallParams(CallState.OUTGOING)).append("\n");
			resultBuffer.append("VIDEO_INCOMING:").append(getVideoCallParams(CallState.INCOMING)).append("\n");
			resultBuffer.append("AUDIO_INCALL:").append(getVideoCallParams(CallState.INCALL)).append("\n");
		}
		if (existAudioCall()) {
			resultBuffer.append("AUDIO_OUTGOING:").append(getAudioCallParams(CallState.OUTGOING)).append("\n");
			resultBuffer.append("AUDIO_INCOMING:").append(getAudioCallParams(CallState.INCOMING)).append("\n");
			resultBuffer.append("AUDIO_INCALL:").append(getAudioCallParams(CallState.INCALL)).append("\n");
		}
		MyLog.e("UserCallType-CallManager", resultBuffer.toString());
	}
}
