package com.zed3.media.mediaButton;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.zed3.ptt.PttEventDispatcher;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

import java.text.SimpleDateFormat;

public class MediaButtonReceiver extends BroadcastReceiver {
	protected static final int PressPttDown = 0;
	protected static final int PressPttUp = 1;
	private static String TAG;
	private static StringBuilder builder;
	private static String deviceInfoStr;
	protected static int downCount = 0;
	static int[] downEventTimeDiffs;
	static int[] downEventTimeDiffs4calc;
	private static SimpleDateFormat formatter;
	private static boolean isStarted = false;
	private static AudioManager mAudioManager;
	private static ComponentName mComponentName;
	private static int mDownCount = 0;
	protected static int mDownCountTotal = 0;
	public static long mDownEventTime = 0L;
	public static int mDownEventTimeDiff = 0;
	public static long mDownTime = 0L;
	private static int mDownTimeDiff = 0;
	private static SimpleDateFormat mFormatter;
	private static MediaButtonReceiver mInstance;
	public static boolean mIsHandFreeDevice = false;
	public static boolean mIsPttDowned = false;
	public static long mLastDownEventTime = 0L;
	public static long mLastDownTime = 0L;
	private static int mLostCount = 0;
	private static int mMinDownTimeDiff = 0;
	private static final int mMinDownTimeDiff1 = 1000;
	static int[] minDownTimeDiffs;
	private static boolean needWriteLog;
	protected static int upCount;
	private Context mContext;

	static {
		MediaButtonReceiver.TAG = "MediaButtonReceiver ";
		MediaButtonReceiver.deviceInfoStr = null;
		MediaButtonReceiver.builder = new StringBuilder();
		MediaButtonReceiver.mMinDownTimeDiff = 1000;
		MediaButtonReceiver.mFormatter = new SimpleDateFormat(" yyyy-MM-dd hh:mm:ss SSS ");
		MediaButtonReceiver.mComponentName = new ComponentName(SipUAApp.mContext.getPackageName(), MediaButtonReceiver.class.getName());
		MediaButtonReceiver.mAudioManager = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
		MediaButtonReceiver.mInstance = new MediaButtonReceiver();
		if (Build.MODEL.contains("HUAWEI MT7")) {
			MediaButtonReceiver.mMinDownTimeDiff = 650;
		}
		MediaButtonReceiver.downEventTimeDiffs = new int[4];
		MediaButtonReceiver.minDownTimeDiffs = new int[4];
		MediaButtonReceiver.downEventTimeDiffs4calc = new int[30];
		MediaButtonReceiver.needWriteLog = true;
	}

	private void calcMinDownTimeDiff(int i) {
		if (MediaButtonReceiver.downEventTimeDiffs4calc[MediaButtonReceiver.downEventTimeDiffs4calc.length - 1] != 0) {
			return;
		}
		final StringBuilder sb = new StringBuilder("calcMinDownTimeDiff(" + i + ")");
		for (int j = 0; j < MediaButtonReceiver.downEventTimeDiffs4calc.length; ++j) {
			if (MediaButtonReceiver.downEventTimeDiffs4calc[j] == 0) {
				MediaButtonReceiver.downEventTimeDiffs4calc[j] = i;
				break;
			}
		}
		sb.append(" downEventTimeDiffs4calc:");
		for (i = 0; i < MediaButtonReceiver.downEventTimeDiffs4calc.length; ++i) {
			if (i == 0) {
				sb.append("{" + MediaButtonReceiver.downEventTimeDiffs4calc[i]);
			} else if (i == MediaButtonReceiver.downEventTimeDiffs4calc.length - 1) {
				sb.append("{" + MediaButtonReceiver.downEventTimeDiffs4calc[i]);
			} else {
				sb.append("," + MediaButtonReceiver.downEventTimeDiffs4calc[i]);
			}
		}
		if (MediaButtonReceiver.downEventTimeDiffs4calc[MediaButtonReceiver.downEventTimeDiffs4calc.length - 1] == 0) {
			LogUtil.makeLog(MediaButtonReceiver.TAG, sb.toString());
			return;
		}
		final int[] array = new int[10];
		this.getMinValues(MediaButtonReceiver.downEventTimeDiffs4calc, array);
		int n = 0;
		int n2 = 0;
		for (i = 2; i < array.length - 2; ++i) {
			n += array[i];
			++n2;
		}
		sb.append(" mDownEventTimeDiff = " + MediaButtonReceiver.mDownEventTimeDiff);
		MediaButtonReceiver.mMinDownTimeDiff = n / n2 + 50;
		LogUtil.makeLog(MediaButtonReceiver.TAG, sb.toString());
	}

	private boolean checkCycle() {
		if (MediaButtonReceiver.downEventTimeDiffs[0] != 0) {
			if (MediaButtonReceiver.downEventTimeDiffs[0] < MediaButtonReceiver.mMinDownTimeDiff && MediaButtonReceiver.downEventTimeDiffs[1] > MediaButtonReceiver.mMinDownTimeDiff && MediaButtonReceiver.downEventTimeDiffs[2] > MediaButtonReceiver.mMinDownTimeDiff && MediaButtonReceiver.downEventTimeDiffs[3] < MediaButtonReceiver.mMinDownTimeDiff) {
				return true;
			}
			if (MediaButtonReceiver.downEventTimeDiffs[0] < MediaButtonReceiver.mMinDownTimeDiff && MediaButtonReceiver.downEventTimeDiffs[1] > MediaButtonReceiver.downEventTimeDiffs[0] && MediaButtonReceiver.downEventTimeDiffs[2] > MediaButtonReceiver.downEventTimeDiffs[3] && MediaButtonReceiver.downEventTimeDiffs[3] < MediaButtonReceiver.mMinDownTimeDiff) {
				return true;
			}
		}
		return false;
	}

	private boolean checkLost() {
		return MediaButtonReceiver.downEventTimeDiffs[0] != 0 && MediaButtonReceiver.mDownCount % 3 == 0 && MediaButtonReceiver.downEventTimeDiffs[3] > MediaButtonReceiver.mMinDownTimeDiff;
	}

	public static void downPTT(final boolean mIsPttDowned) {
		while (true) {
			while (true) {
				synchronized (MediaButtonReceiver.TAG) {
					if (MediaButtonReceiver.mIsPttDowned == mIsPttDowned) {
						return;
					}
					MediaButtonReceiver.mIsPttDowned = mIsPttDowned;
					final PttEventDispatcher instance = PttEventDispatcher.getInstance();
					if (mIsPttDowned) {
						final PttEventDispatcher.PttEvent pttEvent = PttEventDispatcher.PttEvent.PTT_DOWN;
						instance.dispatch(pttEvent);
						return;
					}
				}
				final PttEventDispatcher.PttEvent pttEvent = PttEventDispatcher.PttEvent.PTT_UP;
				continue;
			}
		}
	}

	public static MediaButtonReceiver getInstance() {
		return MediaButtonReceiver.mInstance;
	}

	private void getMaxValues(final int[] array, final int[] array2) {
		if (array.length > array2.length) {
			final int[] array3 = new int[array.length];
			System.arraycopy(array, 0, array3, 0, array.length);
			for (int i = 0; i < array2.length; ++i) {
				int n = Integer.MIN_VALUE;
				int n2;
				for (int j = 0; j < array3.length; ++j, n = n2) {
					if (array3[j] > (n2 = n)) {
						n2 = array3[j];
					}
				}
				for (int k = 0; k < array3.length; ++k) {
					if (array3[k] == n) {
						array3[k] = Integer.MIN_VALUE;
						break;
					}
				}
				array2[i] = n;
			}
		}
	}

	private void getMinValues(final int[] array, final int[] array2) {
		if (array.length > array2.length) {
			final int[] array3 = new int[array.length];
			System.arraycopy(array, 0, array3, 0, array.length);
			for (int i = 0; i < array2.length; ++i) {
				int n = Integer.MAX_VALUE;
				int n2;
				for (int j = 0; j < array3.length; ++j, n = n2) {
					if (array3[j] < (n2 = n)) {
						n2 = array3[j];
					}
				}
				for (int k = 0; k < array3.length; ++k) {
					if (array3[k] == n) {
						array3[k] = Integer.MAX_VALUE;
						break;
					}
				}
				array2[i] = n;
			}
		}
	}

	private void pressDown(final KeyEvent keyEvent) {
		keyEvent.getKeyCode();
		MediaButtonReceiver.mDownTime = System.currentTimeMillis();
		MediaButtonReceiver.mDownEventTime = keyEvent.getEventTime();
		if (MediaButtonReceiver.mLastDownEventTime != 0L) {
			MediaButtonReceiver.mDownEventTimeDiff = (int) (MediaButtonReceiver.mDownEventTime - MediaButtonReceiver.mLastDownEventTime);
		}
		if (MediaButtonReceiver.mLastDownTime != 0L) {
			MediaButtonReceiver.mDownTimeDiff = (int) (MediaButtonReceiver.mDownTime - MediaButtonReceiver.mLastDownTime);
		}
		SystemClock.uptimeMillis();
		++MediaButtonReceiver.mDownCount;
		++MediaButtonReceiver.mDownCountTotal;
		MediaButtonReceiver.builder.append(" rece delay = " + (SystemClock.uptimeMillis() - MediaButtonReceiver.mDownEventTime));
		MediaButtonReceiver.builder.append(" down count/total/lost:" + MediaButtonReceiver.mDownCount + "/" + MediaButtonReceiver.mDownCountTotal + "/" + MediaButtonReceiver.mLostCount);
		MediaButtonReceiver.builder.append(" ReceiveTimeDiff/EventTimeDiff:" + MediaButtonReceiver.mDownTimeDiff + "/" + MediaButtonReceiver.mDownEventTimeDiff);
		MediaButtonReceiver.builder.append(" mMinDownTimeDiff = " + MediaButtonReceiver.mMinDownTimeDiff);
		switch (MediaButtonReceiver.mDownCount % 3) {
			case 1: {
				downPTT(true);
				MediaButtonReceiver.builder.append(" mDownCount%3==1     \u6309\u4e0bPTT  ");
				break;
			}
			case 2: {
				MediaButtonReceiver.builder.append(" mDownCount%3==2     \u677e\u5f00PTT  ");
				downPTT(false);
				break;
			}
			case 0: {
				MediaButtonReceiver.builder.append(" mDownCount%3==0   \u4e0d\u5904\u7406  ");
				break;
			}
		}
		this.calcMinDownTimeDiff(MediaButtonReceiver.mDownEventTimeDiff);
		this.reSetDownTimeDiff(MediaButtonReceiver.mDownEventTimeDiff);
		MediaButtonReceiver.builder.append(" downEventTimeDiffs\uff1a");
		for (int i = 0; i < MediaButtonReceiver.downEventTimeDiffs.length; ++i) {
			if (i == 0) {
				MediaButtonReceiver.builder.append("[" + MediaButtonReceiver.downEventTimeDiffs[i]);
			} else if (i == MediaButtonReceiver.downEventTimeDiffs.length - 1) {
				MediaButtonReceiver.builder.append("," + MediaButtonReceiver.downEventTimeDiffs[i] + "]");
			} else {
				MediaButtonReceiver.builder.append("," + MediaButtonReceiver.downEventTimeDiffs[i]);
			}
		}
		if (MediaButtonReceiver.mDownTimeDiff < MediaButtonReceiver.mMinDownTimeDiff && this.checkCycle()) {
			MediaButtonReceiver.mIsHandFreeDevice = true;
			MediaButtonReceiver.mDownCount = 0;
			MediaButtonReceiver.builder.append(" mDownTimeDiff<mMinDownTimeDiff  checkCycle() is true \u677e\u5f00PTT \r\nreset mDownCount = 0 ");
			downPTT(false);
		} else if (MediaButtonReceiver.mDownTimeDiff > MediaButtonReceiver.mMinDownTimeDiff && this.checkLost()) {
			MediaButtonReceiver.mDownCount = 1;
			++MediaButtonReceiver.mLostCount;
			MediaButtonReceiver.builder.append(" mDownTimeDiff>mMinDownTimeDiff  checkLost() is true \u6309\u4e0bPTT \r\nreset mDownCount = 1 ");
			downPTT(true);
		}
		writeLog2File(MediaButtonReceiver.builder.toString());
		MediaButtonReceiver.mLastDownTime = MediaButtonReceiver.mDownTime;
		MediaButtonReceiver.mLastDownEventTime = MediaButtonReceiver.mDownEventTime;
	}

	private void pressUp(final KeyEvent keyEvent) {
	}

	public static void reInitFields() {
		synchronized (MediaButtonReceiver.class) {
			MediaButtonReceiver.downCount = 0;
			MediaButtonReceiver.upCount = 0;
			MediaButtonReceiver.mDownEventTime = 0L;
			MediaButtonReceiver.mDownTime = 0L;
			MediaButtonReceiver.mLastDownTime = 0L;
			MediaButtonReceiver.mDownCount = 0;
			MediaButtonReceiver.mDownTimeDiff = 0;
		}
	}

	private void reSetDownTimeDiff(final int n) {
		for (int i = 0; i < MediaButtonReceiver.downEventTimeDiffs.length - 1; ++i) {
			MediaButtonReceiver.downEventTimeDiffs[i] = MediaButtonReceiver.downEventTimeDiffs[i + 1];
		}
		MediaButtonReceiver.downEventTimeDiffs[3] = n;
	}

	public static void registerMediaButtonEventReceiver(final Context context) {
		synchronized (MediaButtonReceiver.class) {
			MediaButtonReceiver.mAudioManager.registerMediaButtonEventReceiver(MediaButtonReceiver.mComponentName);
		}
	}

	public static void startReceive(final Context context) {
		synchronized (MediaButtonReceiver.class) {
			if (!MediaButtonReceiver.isStarted) {
				MediaButtonReceiver.isStarted = true;
				MediaButtonReceiver.mIsHandFreeDevice = false;
				Log.i(MediaButtonReceiver.TAG, "startReceive() ");
				writeLog2File("startReceive() ");
				reInitFields();
				registerMediaButtonEventReceiver(context);
				RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonReceiver.startReceive");
			}
		}
	}

	public static void stopReceive(final Context context) {
		synchronized (MediaButtonReceiver.class) {
			if (MediaButtonReceiver.isStarted) {
				MediaButtonReceiver.isStarted = false;
				Log.i(MediaButtonReceiver.TAG, "stopReceive() ");
				writeLog2File("stopReceive() ");
				if (MediaButtonReceiver.mIsPttDowned) {
					downPTT(false);
				}
				RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonReceiver.stopReceive");
			}
		}
	}

	public static void unregisterMediaButtonEventReceiver(final Context context) {
		synchronized (MediaButtonReceiver.class) {
			MediaButtonReceiver.mAudioManager.unregisterMediaButtonEventReceiver(MediaButtonReceiver.mComponentName);
		}
	}

	public static void writeLog2File(final String s) {
		if (!MediaButtonReceiver.needWriteLog) {
			return;
		}
		LogUtil.makeLog(MediaButtonReceiver.TAG, s);
	}

	public void onReceive(final Context mContext, final Intent intent) {
		this.mContext = mContext;
		if (MediaButtonReceiver.builder.length() > 0) {
			MediaButtonReceiver.builder.delete(0, MediaButtonReceiver.builder.length());
		}
		if (SipUAApp.mContext == null) {
			MediaButtonReceiver.builder.append(" SipUAApp.mContext == null ignore");
			LogUtil.makeLog(MediaButtonReceiver.TAG, MediaButtonReceiver.builder.toString());
			return;
		}
		if (Receiver.mSipdroidEngine == null) {
			MediaButtonReceiver.builder.append(" Receiver.mSipdroidEngine == null ignore");
			LogUtil.makeLog(MediaButtonReceiver.TAG, MediaButtonReceiver.builder.toString());
			return;
		}
		System.currentTimeMillis();
		final long lastHeadsetConnectTime = SipUAApp.lastHeadsetConnectTime;
		final String action = intent.getAction();
		final KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
		if ("android.intent.action.MEDIA_BUTTON".equals(action)) {
			final int keyCode = keyEvent.getKeyCode();
			final int action2 = keyEvent.getAction();
			String s;
			if (action2 == 0) {
				s = "ACTION_DOWN";
			} else {
				s = "ACTION_UP ";
			}
			MediaButtonReceiver.builder.append(" " + s);
			keyEvent.getEventTime();
			if (87 == keyCode) {
				MediaButtonReceiver.builder.append(" KEYCODE_MEDIA_NEXT " + keyCode);
			}
			if (85 == keyCode) {
				MediaButtonReceiver.builder.append("KEYCODE_MEDIA_PLAY_PAUSE " + keyCode);
			}
			if (79 == keyCode) {
				MediaButtonReceiver.builder.append("KEYCODE_HEADSETHOOK " + keyCode);
			}
			if (88 == keyCode) {
				MediaButtonReceiver.builder.append("KEYCODE_MEDIA_PREVIOUS " + keyCode);
			}
			if (86 == keyCode) {
				MediaButtonReceiver.builder.append("KEYCODE_MEDIA_STOP" + keyCode);
			}
			if (keyCode == 226) {
				if (action2 == 0) {
					if (keyEvent.getRepeatCount() == 0) {
						downPTT(true);
					}
				} else {
					downPTT(false);
				}
			} else if (action2 == 0) {
				this.pressDown(keyEvent);
			} else {
				this.pressUp(keyEvent);
			}
		}
		this.abortBroadcast();
	}
}
