package com.zed3.sipua.ui.lowsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.toast.MyToast;
import com.zed3.video.VideoManagerService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CallUtil {
	private static final byte[] block4processCall;
	public static boolean isDestory;
	private static Lock lock;
	private static AlertDialog mAlertDlg;
	public static long mCallBeginTime;
	public static long mCallBeginTime2;
	public static String mName;
	public static String mNumber;
	private static String tag;
	private static ArrayList<String> tempGrpList;

	static {
		CallUtil.mName = "";
		CallUtil.mNumber = "";
		CallUtil.mCallBeginTime = 0L;
		CallUtil.mCallBeginTime2 = 0L;
		CallUtil.isDestory = false;
		CallUtil.tag = "CallUtil";
		CallUtil.tempGrpList = new ArrayList<String>();
		CallUtil.lock = new ReentrantLock();
		block4processCall = new byte[0];
	}

	public static void AudioCall(final Context context, final String s, final String s2, final String s3, final String s4) {
		if (!NetChecker.check(context, true)) {
			MyToast.showToast(true, context, R.string.notfast_1);
		} else {
			if (checkGsmCallInCall()) {
				MyToast.showToast(true, context, R.string.gsm_in_call);
				return;
			}
			if (!Receiver.mSipdroidEngine.isRegistered()) {
				MyToast.showToast(true, context, R.string.nologin);
				return;
			}
			if (s.length() != 0) {
				if (s.equals(MemoryMg.getInstance().TerminalNum)) {
					MyToast.showToast(true, context, R.string.call_notify);
					return;
				}
				if ((s4 != null && Member.UserType.toUserType(s4) == Member.UserType.SVP) || (s4 != null && Member.UserType.toUserType(s4) == Member.UserType.MOBILE_GQT && s3 != null && s3.equalsIgnoreCase("1"))) {
					new AlertDialog.Builder(context).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialogInterface, final int n) {
							switch (n) {
								default: {
								}
								case 0: {
									call_menu(context, s.trim(), s2, false);
								}
							}
						}
					}).show();
					return;
				}
				if ((s4 != null && Member.UserType.toUserType(s4) == Member.UserType.VIDEO_MONITOR_GVS) || Member.UserType.toUserType(s4) == Member.UserType.VIDEO_MONITOR_GB28181) {
					new AlertDialog.Builder(context).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialogInterface, final int n) {
							switch (n) {
								default: {
								}
								case 0: {
									call_menu(context, s.trim(), s2, false);
								}
							}
						}
					}).show();
					return;
				}
				new AlertDialog.Builder(context).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialogInterface, final int n) {
						switch (n) {
							default: {
							}
							case 0: {
								call_menu(context, s.trim(), s2, false);
							}
						}
					}
				}).show();
			}
		}
	}

	public static void answerCall() {
		synchronized (CallUtil.block4processCall) {
			final StringBuilder sb = new StringBuilder("answerCall()");
			if (Thread.currentThread().getName().equals("main")) {
				sb.append(" main thread, answerCall by new thread");
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						Receiver.engine(Receiver.mContext).answercall();
						Looper.loop();
					}
				}).start();
			} else {
				sb.append(" not main thread, answerCall current thread");
				Receiver.engine(Receiver.mContext).answercall();
			}
			makeLog(CallUtil.tag, sb.toString());
		}
	}

	private static void call_menu(final Context context, final String s, final String s2, final boolean b) {
		call_menu(context, s, s2, b, null);
	}

	private static void call_menu(final Context context, final String s, final String s2, final boolean b, final String s3) {
		if (checkGsmCallInCall()) {
			MyToast.showToast(true, context, R.string.gsm_in_call);
		} else {
			AntaCallUtil.reInit();
			initNameAndNumber(s, s2);
			if (NetChecker.check(context, true)) {
				if (CallUtil.mAlertDlg != null) {
					CallUtil.mAlertDlg.cancel();
				}
				if (CallUtil.mAlertDlg != null) {
					CallUtil.mAlertDlg.cancel();
				}
				if (s.length() == 0) {
					CallUtil.mAlertDlg = new AlertDialog.Builder(context).setMessage(R.string.empty).setTitle(R.string.information).setCancelable(true).show();
					return;
				}
				if (!Receiver.engine(context).call(s, true, b, s3)) {
					if (context instanceof Activity) {
						CallUtil.mAlertDlg = new AlertDialog.Builder(context).setMessage(R.string.notfast_1).setTitle(R.string.information).setCancelable(true).show();
						return;
					}
					MyToast.showToast(true, context, R.string.notfast_1);
				}
			}
		}
	}

	public static boolean checkGsmCallInCall() {
		return MyPhoneStateListener.getInstance().isInCall();
	}

	public static String getTimeHour() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void initNameAndNumber(String userName, final String mName) {
		CallUtil.mName = null;
		CallUtil.mNumber = null;
		CallUtil.mNumber = userName;
		final String userName2 = ContactUtil.getUserName(userName);
		if (userName2 != null) {
			CallUtil.mName = userName2;
			return;
		}
		userName = GroupListUtil.getUserName(userName);
		if (userName != null && !userName.equals("")) {
			CallUtil.mName = userName;
			return;
		}
		CallUtil.mName = mName;
	}

	public static boolean isInCall() {
		return Receiver.call_state != 0;
	}

	public static boolean isInCallState() {
		return Receiver.call_state == 3;
	}

	public static void makeAudioCall(final Context context, final String s, final String s2) {
		makeAudioCall(context, s, s2, null);
	}

	public static void makeAudioCall(final Context context, final String s, final String s2, final String s3) {
		if (!NetChecker.check(context, true)) {
			MyToast.showToast(true, context, R.string.notfast_1);
		} else {
			if (checkGsmCallInCall()) {
				MyToast.showToast(true, context, R.string.gsm_in_call);
				return;
			}
			if (!Receiver.mSipdroidEngine.isRegistered()) {
				MyToast.showToast(true, context, R.string.nologin);
				return;
			}
			if (isInCall()) {
				MyToast.showToast(true, context, R.string.vedio_calling_notify);
				Looper.loop();
				return;
			}
			if (s.length() != 0) {
				if (s.equals(MemoryMg.getInstance().TerminalNum)) {
					MyToast.showToast(true, context, R.string.call_notify);
					return;
				}
				call_menu(context, s.trim(), s2, false, s3);
			}
		}
	}

	public static void makeLog(final String s, final String s2) {
		ZMBluetoothManager.getInstance().makeLog(s, s2);
	}

	public static void makeSOSCall(final Context context, final String s, final String s2) {
		makeAudioCall(context, s, s2, "Emergency");
	}

	public static void makeVideoCall(final Context context, final String s, final String s2, final String s3) {
		if (!NetChecker.check(context, true)) {
			MyToast.showToast(true, context, R.string.notfast_1);
		} else {
			if (!Receiver.mSipdroidEngine.isRegistered()) {
				MyToast.showToast(true, context, R.string.nologin);
				return;
			}
			if (Build.VERSION.SDK_INT < 16) {
				MyToast.showToast(true, context, R.string.version_unsupported);
				return;
			}
			if (isInCall()) {
				MyToast.showToast(true, context, R.string.vedio_calling_notify);
				return;
			}
			if (s.length() != 0) {
				if (s.equals(MemoryMg.getInstance().TerminalNum)) {
					MyToast.showToast(true, context, R.string.call_notify);
					return;
				}
				MyLog.e("dd", "numberString==" + s);
				if (s.length() == 11) {
					if (!s.substring(0, 6).equals(DataBaseService.getInstance().getCompanyId()) && !DataBaseService.getInstance().sameCopmany(s)) {
						MyToast.showToast(true, context, R.string.number_no_sustain);
						return;
					}
					final Member stringbyItem = DataBaseService.getInstance().getStringbyItem(s);
					final String video = stringbyItem.getVideo();
					final String mtype = stringbyItem.getMtype();
					if (!TextUtils.isEmpty((CharSequence) video) && video.equals("0") && mtype != null && Member.UserType.toUserType(mtype) == Member.UserType.MOBILE_GQT) {
						MyToast.showToast(true, context, R.string.number_no_dredge);
						return;
					}
				}
				if (s != null && !s.equals("")) {
					VideoManagerService.getDefault().showVideoSelectDialog(context, (VideoManagerService.VideoSelectDialogBuilder.OnVideoSelectItemClickListener) new VideoManagerService.VideoSelectDialogBuilder.OnVideoSelectItemClickListener() {
						@Override
						public void onAudioConnectionClicked() {
							CallUtil.makeAudioCall(context, s, s2);
						}

						@Override
						public void onVideoConnectionClicked() {
							final Intent intent = new Intent("com.zed3.action.VIDEO_CALL");
							intent.putExtra("com.zed3.extra.VIDEO_NUMBER", s);
							VideoManagerService.getDefault().sendIntent(intent);
						}

						@Override
						public void onVideoMonitorClicked() {
							final Intent intent = new Intent("com.zed3.action.VIDEO_MONITOR");
							intent.putExtra("com.zed3.extra.VIDEO_NUMBER", s);
							VideoManagerService.getDefault().sendIntent(intent);
						}

						@Override
						public void onVideoUploadClicked() {
							final Intent intent = new Intent("com.zed3.action.VIDEO_UPLOAD");
							intent.putExtra("com.zed3.extra.VIDEO_NUMBER", s);
							VideoManagerService.getDefault().sendIntent(intent);
						}
					}, s, s3);
				}
			}
		}
	}

	private static void makeVideoCallInner(final Context context, final String s, final String s2) {
		call_menu(context, s, s2, true);
	}

	public static void reInit() {
		CallUtil.lock.lock();
		CallUtil.mName = null;
		CallUtil.mNumber = null;
		CallUtil.lock.unlock();
	}

	public static void rejectAudioCall() {
		rejectCall();
	}

	public static void rejectCall() {
		synchronized (CallUtil.block4processCall) {
			final StringBuilder sb = new StringBuilder("rejectcall()");
			if (Thread.currentThread().getName().equals("main")) {
				sb.append(" main thread, rejectcall by new thread");
				makeLog(CallUtil.tag, sb.toString());
				new Thread(new Runnable() {
					@Override
					public void run() {
						Receiver.engine(Receiver.mContext).rejectCall();
					}
				}).start();
			} else {
				sb.append(" not main thread, rejectcall current thread");
				makeLog(CallUtil.tag, sb.toString());
				Receiver.engine(Receiver.mContext).rejectCall();
			}
		}
	}

	public static void rejectVideoCall() {
		rejectCall();
	}

	public static void startVideoCall(final Context context, final String s, final String s2) {
		makeVideoCallInner(context, s, s2);
	}
}
