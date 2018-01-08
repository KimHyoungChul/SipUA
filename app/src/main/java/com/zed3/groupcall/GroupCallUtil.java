package com.zed3.groupcall;

import android.os.Message;
import android.util.Log;

import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.log.MyLog;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.baiduMap.JsLocationOverlay;
import com.zed3.sipua.baiduMap.LocationOverlayDemo;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.ui.lowsdk.TempGrpCallActivity;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;
import com.zed3.video.VideoManagerService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GroupCallUtil {
	private static String ActionMode;
	public static final int STATE_IDLE = 0;
	public static final String STATE_IDLE_STR = "STATE_IDLE";
	public static final int STATE_INITIATING = 4;
	public static final int STATE_LISTENING = 1;
	public static final String STATE_LISTENING_STR = "STATE_LISTENING";
	public static final int STATE_QUEUE = 3;
	public static final String STATE_QUEUE_STR = "STATE_QUEUE";
	public static final int STATE_SHUTDOWN = 4;
	public static final String STATE_SHUTDOWN_STR = "STATE_SHUTDOWN";
	public static final int STATE_TALKING = 2;
	public static final String STATE_TALKING_STR = "STATE_TALKING";
	private static Lock lock;
	private static int mGroupCallState;
	public static boolean mIsPttDown;
	private static UserAgent mUserAgent;
	private static Object pttGrps;
	private static String tag;
	private static String talkGroup;

	static {
		GroupCallUtil.lock = new ReentrantLock();
		GroupCallUtil.tag = "GroupCallUtil";
		GroupCallUtil.mGroupCallState = 4;
	}

	public static void changeUI(boolean down) {
		int i = 1;
		TalkBackNew.isPttPressing = down;
		LocationOverlayDemo.isPttPressing = down;
//		GoogleLocationOverlay.isPttPressing = down;
		JsLocationOverlay.isPttPressing = down;
		Message msg;
		if (TalkBackNew.isResume) {
			TalkBackNew instance = TalkBackNew.getInstance();
			if (instance != null) {
				msg = instance.pttPressHandler.obtainMessage();
				if (!down) {
					i = 0;
				}
				msg.what = i;
				instance.pttPressHandler.sendMessage(msg);
				if (!TalkBackNew.checkHasCurrentGrp(SipUAApp.mContext)) {
				}
			}
		} else if (LocationOverlayDemo.isResume) {
			LocationOverlayDemo instance2 = LocationOverlayDemo.getInstance();
			if (instance2 != null) {
				msg = instance2.pttPressHandler.obtainMessage();
				if (!down) {
					i = 0;
				}
				msg.what = i;
				instance2.pttPressHandler.sendMessage(msg);
				if (!LocationOverlayDemo.checkHasCurrentGrp(SipUAApp.mContext)) {
				}
			}
//		} else if (GoogleLocationOverlay.isResume) {
//			GoogleLocationOverlay instance3 = GoogleLocationOverlay.getInstance();
//			if (instance3 != null) {
//				msg = instance3.gooPttPressHandler.obtainMessage();
//				if (!down) {
//					i = 0;
//				}
//				msg.what = i;
//				instance3.gooPttPressHandler.sendMessage(msg);
//				if (!GoogleLocationOverlay.checkHasCurrentGrp(SipUAApp.mContext)) {
//				}
//			}
		} else if (JsLocationOverlay.isResume) {
			JsLocationOverlay instance4 = JsLocationOverlay.getInstance();
			if (instance4 != null) {
				msg = instance4.JsPttPressHandler.obtainMessage();
				if (!down) {
					i = 0;
				}
				msg.what = i;
				instance4.JsPttPressHandler.sendMessage(msg);
				if (!JsLocationOverlay.checkHasCurrentGrp(SipUAApp.mContext)) {
				}
			}
		} else if (TempGrpCallActivity.isResume) {
			TempGrpCallActivity instance5 = TempGrpCallActivity.getInstance();
			if (instance5 != null) {
				msg = instance5.TepttPressHandler.obtainMessage();
				if (!down) {
					i = 0;
				}
				msg.what = i;
				instance5.TepttPressHandler.sendMessage(msg);
			}
			if (!TempGrpCallActivity.checkHasCurrentGrp(SipUAApp.mContext)) {
			}
		}
	}

	public static String getActionMode() {
		return GroupCallUtil.ActionMode;
	}

	public static int getGroupCallState() {
		return GroupCallUtil.mGroupCallState;
	}

	public static String getGroupCallStateStr(final int n) {
		switch (n) {
			default: {
				return "unkown state";
			}
			case 0: {
				return "STATE_IDLE";
			}
			case 1: {
				return "STATE_LISTENING";
			}
			case 3: {
				return "STATE_QUEUE";
			}
			case 4: {
				return "STATE_SHUTDOWN";
			}
			case 2: {
				return "STATE_TALKING";
			}
		}
	}

	public static String getTalkGrp() {
		return GroupCallUtil.talkGroup;
	}

	public static void makeGroupCall(final boolean mIsPttDown, final boolean b, final UserAgent.PttPRMode pttPRMode) {
		Label_0124:
		{
			try {
				GroupCallUtil.lock.lock();
				MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ")");
				final VideoManagerService default1 = VideoManagerService.getDefault();
				final CallManager manager = CallManager.getManager();
				if (!mIsPttDown || (!default1.isCurrentVideoCall() && !manager.existAudioCall())) {
					if (!mIsPttDown || !MyPhoneStateListener.getInstance().isInCall()) {
						break Label_0124;
					}
					MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
					Log.i(GroupCallUtil.tag, "MyPhoneStateListener.getInstance().isInCall() is true ignore ");
				}
			} catch (Exception ex) {
				// TODO
			}
			// iftrue(Label_0184:, !b)
			// iftrue(Label_0287:, check)
			// iftrue(Label_0321:, GroupCallUtil.mUserAgent == null)
			// iftrue(Label_0279:, !mIsPttDown)
			// iftrue(Label_0239:, !CallUtil.isInCall())
			// iftrue(Label_0176:, !mIsPttDown || check)
			finally {
				GroupCallUtil.lock.unlock();
			}
		}
	}

	public static void makeGroupCallNoTip(final boolean mIsPttDown, final boolean b, final UserAgent.PttPRMode pttPRMode) {
		try {
			GroupCallUtil.lock.lock();
			MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ")");
			if (mIsPttDown && MyPhoneStateListener.getInstance().isInCall()) {
				MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
				Log.i(GroupCallUtil.tag, "MyPhoneStateListener.getInstance().isInCall() is true ignore ");
				return;
			}
			final boolean connect = Tools.isConnect(SipUAApp.mContext);
			if (mIsPttDown && !connect) {
				MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ") NetChecker.check() false");
				return;
			}
			if (b) {
				changeUI(mIsPttDown);
			}
			if (CallUtil.isInCall()) {
				MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ") isInCall() true");
			}
			if (!connect) {
				MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ") NetChecker.check() false");
				return;
			}
			GroupCallUtil.mUserAgent = Receiver.GetCurUA();
			if (GroupCallUtil.mUserAgent != null) {
				GroupCallUtil.mUserAgent.OnPttKey(mIsPttDown, pttPRMode);
			} else {
				MyLog.e(GroupCallUtil.tag, "makeGroupCall(" + mIsPttDown + ") pressPTT(" + mIsPttDown + ") ,ua = null");
			}
			GroupCallUtil.mIsPttDown = mIsPttDown;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			GroupCallUtil.lock.unlock();
		}
	}

	public static void setActionMode(final String actionMode) {
		GroupCallUtil.ActionMode = actionMode;
	}

	public static void setGroupCallState(final int mGroupCallState) {
		GroupCallUtil.mGroupCallState = mGroupCallState;
	}

	public static void setTalkGrp(final String talkGroup) {
		GroupCallUtil.talkGroup = talkGroup;
	}
}
