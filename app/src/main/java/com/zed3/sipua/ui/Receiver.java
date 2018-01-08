package com.zed3.sipua.ui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.zed3.audio.AudioModeUtils;
import com.zed3.flow.FlowRefreshService;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.media.Bluetooth;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.media.RtpStreamSender_group;
import com.zed3.media.RtpStreamSender_signal;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.phone.Call;
import com.zed3.sipua.phone.Connection;
import com.zed3.sipua.ui.anta.AntaCallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.TranscribeActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sort.GroupDateProcess;
import com.zed3.utils.LanguageChange;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RestoreReceiver;
import com.zed3.video.VideoManagerService;

import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.provider.SipProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Receiver extends BroadcastReceiver {
	static final String ACTION_CALL_END = "com.zed3.sipua.ui_callscreen_finish";
	static final String ACTION_DATA_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
	static final String ACTION_DEVICE_IDLE = "com.android.server.WifiManager.action.DEVICE_IDLE";
	static final String ACTION_DOCK_EVENT = "android.intent.action.DOCK_EVENT";
	static final String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
	static final String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
	static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
	static final String ACTION_PTT_DOWN = "android.intent.action.PTT.down";
	static final String ACTION_PTT_UP = "android.intent.action.PTT.up";
	static final String ACTION_SCO_AUDIO_STATE_CHANGED = "android.media.SCO_AUDIO_STATE_CHANGED";
	static final String ACTION_SIGNAL_STRENGTH_CHANGED = "android.intent.action.SIG_STR";
	static final String ACTION_VPN_CONNECTIVITY = "vpn.connectivity";
	public static final int AUTO_ANSWER_NOTIFICATION = 4;
	public static final int CALL_NOTIFICATION = 2;
	static final String CATEGORY_CAR_DOCK = "android.intent.category.CAR_DOCK";
	static final String CATEGORY_DESK_DOCK = "android.intent.category.DESK_DOCK";
	static final String EXTRA_DOCK_STATE = "android.intent.extra.DOCK_STATE";
	static final int EXTRA_DOCK_STATE_CAR = 2;
	static final int EXTRA_DOCK_STATE_DESK = 1;
	static final String EXTRA_SCO_AUDIO_STATE = "android.media.extra.SCO_AUDIO_STATE";
	static final int GPS_UPDATES = 4000000;
	public static final boolean IN = true;
	public static final String INOROUT = "isCallingIn";
	static final String METADATA_DOCK_HOME = "android.dock_home";
	public static final int MISSED_CALL_NOTIFICATION = 3;
	public static final int MWI_NOTIFICATION = 1;
	public static String MWI_account;
	static final int NET_UPDATES = 600000;
	public static final String NULLSTR = "--";
	public static final boolean OUT = false;
	static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	public static final int REGISTER_NOTIFICATION = 5;
	static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static String USERNAME;
	public static String USERNUMBER;
	static AlarmManager am;
	public static int bluetooth;
	static int cache_res;
	static String cache_text;
	private static long callBeginTime;
	public static int call_end_reason;
	private static int call_inState;
	private static String call_incoming;
	public static int call_state;
	public static Call ccCall;
	public static Connection ccConn;
	public static int docked;
	public static long expire_time;
	static PendingIntent gps_sender;
	static GroupDateProcess grpDateProcess;
	public static boolean hasIntent;
	public static int headset;
	private static String lastnumber;
	private static String laststate;
	static LocationManager lm;
	public static int mCallState;
	public static final Context mContext;
	private static boolean mHasInCalled;
	private static boolean mIsCallIn;
	public static boolean mIsRejectedByUser;
	public static MediaPlayer mMediaPlayer;
	public static SipdroidEngine mSipdroidEngine;
	static boolean net_enabled;
	static PendingIntent net_sender;
	public static boolean on_wlan;
	public static String pstn_state;
	public static long pstn_time;
	private static String tag;
	static UserAgent ua;
	static Vibrator v;
	static final long[] vibratePattern;
	public static boolean viewInvisible;
	static boolean was_playing;
	static PowerManager.WakeLock wl;
	final int MSG_ENABLE;
	final int MSG_SCAN;
	Handler mHandler;
	private SmsMmsDatabase mSmsMmsDatabase;

	static {
		Receiver.hasIntent = false;
		Receiver.viewInvisible = false;
		vibratePattern = new long[]{0L, 1000L, 1000L};
		Receiver.docked = -1;
		Receiver.headset = -1;
		Receiver.bluetooth = -1;
		Receiver.call_end_reason = -1;
		mContext = SipUAApp.getAppContext();
		Receiver.USERNUMBER = "userNumber";
		Receiver.USERNAME = "userName";
		Receiver.mCallState = -1;
		Receiver.tag = "Receiver";
	}

	public Receiver() {
		this.MSG_SCAN = 1;
		this.MSG_ENABLE = 2;
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 1: {
						((WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE)).startScan();
					}
					case 2: {
						Receiver.enable_wifi(true);
					}
				}
			}
		};
	}

	public static UserAgent GetCurUA() {
		return Receiver.ua = engine(Receiver.mContext).GetCurUA();
	}

	public static void alarm(final int n, final Class<?> clazz) {
		LogUtil.makeLog(Receiver.tag, "alarm(" + n + "," + clazz + ")");
		final PendingIntent broadcast = PendingIntent.getBroadcast(Receiver.mContext, 0, new Intent(Receiver.mContext, (Class) clazz), 0);
		final AlarmManager alarmManager = (AlarmManager) Receiver.mContext.getSystemService(Context.ALARM_SERVICE);
		if (n <= 0) {
			alarmManager.cancel(broadcast);
			return;
		}
//		if (isXiaoMI()) {
//			final Calendar instance = Calendar.getInstance();
//			instance.setTimeInMillis(System.currentTimeMillis());
//			instance.add(13, n);
//			alarmManager.set(1, instance.getTimeInMillis(), broadcast);
//			return;
//		}
//		alarmManager.set(2, SystemClock.elapsedRealtime() + n * 1000, broadcast);
	}

	private static void appendCallParams(final Intent intent, final CallManager.CallState callState, final String s, final String callerName) {
		final CallManager manager = CallManager.getManager();
		ExtendedCall extendedCall;
		if (callState == CallManager.CallState.INCOMING) {
			extendedCall = manager.getAudioCall(CallManager.CallState.INCOMING, s);
		} else {
			extendedCall = manager.getAudioCall(CallManager.CallState.OUTGOING, s);
		}
		if (extendedCall != null) {
			extendedCall.setCallerName(callerName);
		}
		MyLog.d("videoTrace", "Receiver#moveTop() enter username = " + s + " , call = " + extendedCall);
		intent.putExtra("com.zed3.extra.CALL_ID", CallManager.getCallExtId(extendedCall));
		intent.putExtra("com.zed3.extra.CALL_TYPE", extendedCall.getCallType().toString());
		intent.putExtra("com.zed3.extra.CALL_USERNUMBER", s);
	}

	static void broadcastCallStateChanged(final String s, String lastnumber) {
		String laststate = s;
		if (s == null) {
			laststate = Receiver.laststate;
			lastnumber = Receiver.lastnumber;
		}
		if (laststate.equals("IDLE")) {
			if (Receiver.was_playing) {
				if (Receiver.pstn_state == null || Receiver.pstn_state.equals("IDLE")) {
					Receiver.mContext.sendBroadcast(new Intent("com.android.music.musicservicecommand.togglepause"));
				}
				Receiver.was_playing = false;
			}
		} else {
			final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
			if ((Receiver.laststate == null || Receiver.laststate.equals("IDLE")) && (Receiver.was_playing = audioManager.isMusicActive())) {
				Receiver.mContext.sendBroadcast(new Intent("com.android.music.musicservicecommand.pause"));
			}
		}
		Receiver.laststate = laststate;
		Receiver.lastnumber = lastnumber;
	}

	public static Intent createCallLogIntent() {
		final Intent intent = new Intent(Receiver.mContext, (Class) MainActivity.class);
		intent.putExtra("other_intent", "SipdroidActivity");
		return intent;
	}

	static Intent createHomeDockIntent() {
		final Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
		if (Receiver.docked == 2) {
			intent.addCategory("android.intent.category.CAR_DOCK");
		} else {
			if (Receiver.docked != 1) {
				return null;
			}
			intent.addCategory("android.intent.category.DESK_DOCK");
		}
//		final ActivityInfo resolveActivityInfo = intent.resolveActivityInfo(Receiver.mContext.getPackageManager(), 128);
//		if (resolveActivityInfo == null) {
//			return null;
//		}
//		if (resolveActivityInfo.metaData != null && resolveActivityInfo.metaData.getBoolean("android.dock_home")) {
//			intent.setClassName(resolveActivityInfo.packageName, resolveActivityInfo.name);
//			return intent;
//		}
		return null;
	}

	public static Intent createHomeIntent() {
		final Intent homeDockIntent = createHomeDockIntent();
		if (homeDockIntent != null) {
			return homeDockIntent;
		}
		final Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
		intent.addCategory("android.intent.category.HOME");
		return intent;
	}

	static Intent createIntent(final Class<?> clazz) {
		final Intent intent = new Intent();
		intent.setClass(Receiver.mContext, (Class) clazz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	static Intent createMWIIntent() {
		if (Receiver.MWI_account != null) {
			return new Intent("android.intent.action.CALL", Uri.parse(Receiver.MWI_account.replaceFirst("sip:", "sipdroid:")));
		}
		return new Intent("android.intent.action.DIAL");
	}

	static void enable_wifi(final boolean wifiEnabled) {
		boolean b = false;
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("ownwifi", false) && (!wifiEnabled || PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("wifi_disabled", false))) {
			final WifiManager wifiManager = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
			final ContentResolver contentResolver = Receiver.mContext.getContentResolver();
//			if (wifiEnabled || Settings.Secure.getInt(contentResolver, "wifi_on", 0) != 0) {
//				final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
//				if (!wifiEnabled) {
//					b = true;
//				}
//				edit.putBoolean("wifi_disabled", b);
//				edit.commit();
//				if (wifiEnabled) {
//					final Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
//					intent.putExtra("newState", wifiManager.getWifiState());
//					Receiver.mContext.sendBroadcast(intent);
//				}
//				wifiManager.setWifiEnabled(wifiEnabled);
//			}
		}
	}

	public static SipdroidEngine engine(final Context context) {
		synchronized (Receiver.class) {
			if (Receiver.mSipdroidEngine == null) {
				(Receiver.mSipdroidEngine = new SipdroidEngine()).StartEngine();
				if (Build.VERSION.SDK_INT >= 8) {
					Bluetooth.init();
				}
				Receiver.mContext.startService(new Intent(Receiver.mContext, (Class) RegisterService.class));
				new Exception("---RegisterService start----").printStackTrace();
			} else {
				Receiver.mSipdroidEngine.CheckEngine();
			}
			return Receiver.mSipdroidEngine;
		}
	}

	public static GroupDateProcess getGDProcess() {
		synchronized (Receiver.class) {
			if (Receiver.grpDateProcess == null) {
				Receiver.grpDateProcess = new GroupDateProcess();
				new Thread(Receiver.grpDateProcess).start();
			}
			return Receiver.grpDateProcess;
		}
	}

	public static boolean isCallNotificationNeedClose() {
		boolean b = false;
		if (Receiver.call_state == 0) {
			onText(2, null, 0, 0L);
			b = true;
		}
		return b;
	}

	public static boolean isFast(final int n) {
		return true;
	}

	static boolean isFastGSM(final int n) {
		final TelephonyManager telephonyManager = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		if (on_vpn() && telephonyManager.getNetworkType() >= 2) {
			final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
			final StringBuilder sb = new StringBuilder("vpn");
			Serializable value;
			if (n != 0) {
				value = n;
			} else {
				value = "";
			}
			return defaultSharedPreferences.getBoolean(sb.append(value).toString(), false);
		}
		if (telephonyManager.getNetworkType() >= 3) {
			final SharedPreferences defaultSharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
			final StringBuilder sb2 = new StringBuilder("3g");
			Serializable value2;
			if (n != 0) {
				value2 = n;
			} else {
				value2 = "";
			}
			return defaultSharedPreferences2.getBoolean(sb2.append(value2).toString(), false);
		}
		if (telephonyManager.getNetworkType() == 2) {
			final SharedPreferences defaultSharedPreferences3 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
			final StringBuilder sb3 = new StringBuilder("edge");
			Serializable value3;
			if (n != 0) {
				value3 = n;
			} else {
				value3 = "";
			}
			return defaultSharedPreferences3.getBoolean(sb3.append(value3).toString(), false);
		}
		return false;
	}

	public static boolean isXiaoMI() {
		return Build.MODEL.contains("MI 1S") || Build.MODEL.contains("MI 2S") || Build.MODEL.contains("HUAWEI G700-U00") || Build.MODEL.contains("HUAWEI P6-U06") || Build.MODEL.contains("HUAWEI MT1-U06") || Build.MODEL.contains("HUAWEI Y511-T00");
	}

	private static void moveTop(final Boolean b, final String s, final String s2) {
		Receiver.ua = engine(Receiver.mContext).GetCurUA();
		progress();
		MyLog.d("testcrash", "Receiver#moveTop() enter AntaCallUtil#isAntaCall = " + AntaCallUtil.isAntaCall);
		Intent intent2;
		if (AntaCallUtil.isAntaCall) {
			PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit().putString("AntaCallCreateTime", AntaCallUtil.mCreateTime()).commit();
			final Intent intent = new Intent(Receiver.mContext, (Class) CallActivity2.class);
			CallManager.CallState callState;
			if (b) {
				callState = CallManager.CallState.INCOMING;
			} else {
				callState = CallManager.CallState.OUTGOING;
			}
			appendCallParams(intent, callState, s, s2);
			intent2 = intent;
		} else if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("autoAnswerKey", false)) {
			final Intent intent3 = new Intent(Receiver.mContext, (Class) CallActivity.class);
			CallManager.CallState callState2;
			if (b) {
				callState2 = CallManager.CallState.INCOMING;
			} else {
				callState2 = CallManager.CallState.OUTGOING;
			}
			appendCallParams(intent3, callState2, s, s2);
			sendBroadcast4Cal(3, CallManager.getCallParams(intent3));
			intent2 = intent3;
		} else {
			final Intent intent4 = new Intent(Receiver.mContext, (Class) CallActivity.class);
			CallManager.CallState callState3;
			if (b) {
				callState3 = CallManager.CallState.INCOMING;
			} else {
				callState3 = CallManager.CallState.OUTGOING;
			}
			appendCallParams(intent4, callState3, s, s2);
			intent2 = intent4;
		}
		final Bundle bundle = new Bundle();
		bundle.putBoolean("isCallingIn", (boolean) b);
		bundle.putString(Receiver.USERNUMBER, s);
		bundle.putString(Receiver.USERNAME, s2);
		intent2.putExtras(bundle);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		SipUAApp.mContext.startActivity(intent2);
	}

	public static void onState(final int p0, final String p1) {
	}

	public static void onText(final int n, String cache_text, final int n2, final long n3) {
		if (Receiver.mSipdroidEngine != null && n == Receiver.mSipdroidEngine.pref + 5) {
			Receiver.cache_text = cache_text;
			Receiver.cache_res = n2;
		}
		String s = cache_text;
		if (n >= 5) {
			s = cache_text;
			if (n2 == R.drawable.icon64) {
				s = cache_text;
				if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("registration", true)) {
					s = null;
				}
			}
		}
		final NotificationManager notificationManager = (NotificationManager) Receiver.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		final VideoManagerService default1 = VideoManagerService.getDefault();
		if (s != null) {
			final Notification notification = new Notification();
			notification.icon = n2;
			if (n == 3) {
				notification.flags |= 0x10;
				notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createCallLogIntent(), 0);
				final RemoteViews contentView = new RemoteViews(Receiver.mContext.getPackageName(), R.layout.ongoing_call_notification);
				contentView.setImageViewResource(R.id.icon, notification.icon);
				contentView.setTextViewText(R.id.text1, (CharSequence) s);
				contentView.setTextViewText(R.id.text2, (CharSequence) Receiver.mContext.getString(R.string.app_name));
				notification.contentView = contentView;
				if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("notify", false)) {
					notification.flags |= 0x1;
					notification.ledARGB = -16776961;
					notification.ledOnMS = 125;
					notification.ledOffMS = 2875;
				}
			} else {
				switch (n) {
					default: {
						if (n >= 5 && Receiver.mSipdroidEngine != null && n != Receiver.mSipdroidEngine.pref + 5 && n2 == R.drawable.icon64) {
							notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createIntent(ChangeAccount.class), 0);
						} else if (n == 2) {
							if (Receiver.call_state == 3) {
								if (!default1.existVideoRelatedCall()) {
									final Context mContext = Receiver.mContext;
									Serializable s2;
									if (AntaCallUtil.isAntaCall) {
										s2 = CallActivity2.class;
									} else {
										s2 = CallActivity.class;
									}
									notification.contentIntent = PendingIntent.getActivity(mContext, 0, createIntent((Class<?>) s2), 0);
									MyLog.i("Receiver", "\u901a\u8bdd\u4e2d AudioCall");
								} else {
									if (default1.isCurrentVideoTRANSCRIBE()) {
										notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createIntent(TranscribeActivity.class), 0);
									} else {
										notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createIntent(CameraCall.class), 0);
									}
									MyLog.i("Receiver", "\u901a\u8bdd\u4e2dcameracall");
								}
							} else {
								if (Receiver.call_state == 2 && !default1.existVideoRelatedCall()) {
									final Context mContext2 = Receiver.mContext;
									Serializable s3;
									if (AntaCallUtil.isAntaCall) {
										s3 = CallActivity2.class;
									} else {
										s3 = CallActivity.class;
									}
									notification.contentIntent = PendingIntent.getActivity(mContext2, 0, createIntent((Class<?>) s3), 0);
								} else if (!default1.existVideoRelatedCall()) {
									final Context mContext3 = Receiver.mContext;
									Serializable s4;
									if (AntaCallUtil.isAntaCall) {
										s4 = CallActivity2.class;
									} else {
										s4 = CallActivity.class;
									}
									notification.contentIntent = PendingIntent.getActivity(mContext3, 0, createIntent((Class<?>) s4), 0);
								} else {
									notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createIntent(DemoCallScreen.class), 0);
								}
								MyLog.i("Receiver", "\u547c\u53eb\u6216\u6765\u7535\u4e2d\u3002\u3002\u3002");
							}
						} else {
							final Intent intent = new Intent(SipUAApp.mContext, (Class) RestoreReceiver.class);
							intent.setAction("com.zed3.restore");
							notification.contentIntent = PendingIntent.getBroadcast(SipUAApp.mContext, 0, intent, 0);
						}
						if (n2 == R.drawable.sym_presence_away) {
							notification.flags |= 0x1;
							notification.ledARGB = -65536;
							notification.ledOnMS = 125;
							notification.ledOffMS = 2875;
							break;
						}
						break;
					}
					case 1: {
						notification.flags |= 0x10;
						notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createMWIIntent(), 0);
						notification.flags |= 0x1;
						notification.ledARGB = -16711936;
						notification.ledOnMS = 125;
						notification.ledOffMS = 2875;
						break;
					}
					case 4: {
						notification.contentIntent = PendingIntent.getActivity(Receiver.mContext, 0, createIntent(AutoAnswer.class), 0);
						break;
					}
				}
				notification.flags |= 0x2;
				final RemoteViews contentView2 = new RemoteViews(Receiver.mContext.getPackageName(), R.layout.ongoing_call_notification);
				contentView2.setImageViewResource(R.id.icon, notification.icon);
				if (n3 != 0L) {
					contentView2.setChronometer(R.id.text1, n3, String.valueOf(s) + " (%s)", true);
				} else if (n >= 5) {
					Receiver.mContext.getSharedPreferences("notifyInfo", 0).edit().putInt("type", n).putString("text", s).putInt("mInCallResId", n2).putLong("base", n3).commit();
					if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("pos", false)) {
						contentView2.setTextViewText(R.id.text2, (CharSequence) (String.valueOf(s) + "/" + Receiver.mContext.getString(R.string.settings_pos3)));
					} else {
						contentView2.setTextViewText(R.id.text2, (CharSequence) s);
					}
					SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
					contentView2.setTextViewText(R.id.text1, (CharSequence) Settings.getUserName());
					if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
						cache_text = DeviceInfo.AutoVNoName;
						cache_text = "";
						Receiver.ua = engine(Receiver.mContext).GetCurUA();
						String grpName = cache_text;
						if (Receiver.ua != null) {
							final PttGrp getCurGrp = Receiver.ua.GetCurGrp();
							grpName = cache_text;
							if (getCurGrp != null) {
								grpName = cache_text;
								if (!TextUtils.isEmpty((CharSequence) getCurGrp.grpName)) {
									grpName = getCurGrp.grpName;
								}
							}
						}
						final SharedPreferences sharedPreferences = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
						cache_text = sharedPreferences.getString("displayname", "");
						if (cache_text == null || cache_text.equals("") || cache_text.equalsIgnoreCase("null")) {
							cache_text = sharedPreferences.getString("userName", "");
						}
						String string = cache_text;
						if (!grpName.equals("")) {
							string = String.valueOf(cache_text) + "@" + grpName;
						}
						contentView2.setTextViewText(R.id.text1, (CharSequence) string);
					}
				} else {
					contentView2.setTextViewText(R.id.text1, (CharSequence) s);
					contentView2.setTextViewText(R.id.text2, (CharSequence) Receiver.mContext.getResources().getString(R.string.app_name));
				}
				notification.contentView = contentView2;
			}
			notificationManager.notify(n, notification);
		} else {
			notificationManager.cancel(n);
		}
		if (n != 4) {
			updateAutoAnswer();
		}
		if (Receiver.mSipdroidEngine != null && n >= 5 && n != Receiver.mSipdroidEngine.pref + 5) {
			onText(Receiver.mSipdroidEngine.pref + 5, Receiver.cache_text, Receiver.cache_res, 0L);
		}
	}

	static void on_vpn(final boolean b) {
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
		edit.putBoolean("on_vpn", b);
		edit.commit();
	}

	static boolean on_vpn() {
		return PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("on_vpn", false);
	}

	public static void pos(final boolean b) {
		if (Receiver.lm == null) {
			Receiver.lm = (LocationManager) Receiver.mContext.getSystemService(Context.LOCATION_SERVICE);
		}
		if (Receiver.am == null) {
			Receiver.am = (AlarmManager) Receiver.mContext.getSystemService(Context.ALARM_SERVICE);
		}
		pos_gps(false);
		if (b) {
			if (Receiver.call_state != 0 || !SipUAApp.on(Receiver.mContext) || !PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("pos", false) || PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("posurl", "").length() <= 0) {
				pos_net(false);
				return;
			}
//			final Location lastKnownLocation = Receiver.lm.getLastKnownLocation("network");
//			if (lastKnownLocation == null || System.currentTimeMillis() - lastKnownLocation.getTime() > 4000000L) {
//				pos_gps(true);
//				pos_net(false);
//			}
			pos_net(true);
		}
	}

	static void pos_gps(final boolean b) {
		if (Receiver.gps_sender == null) {
			Receiver.gps_sender = PendingIntent.getBroadcast(Receiver.mContext, 0, new Intent(Receiver.mContext, (Class) OneShotLocation.class), 0);
		}
		if (!b) {
			Receiver.am.cancel(Receiver.gps_sender);
			Receiver.lm.removeUpdates(Receiver.gps_sender);
			return;
		}
//		Receiver.lm.requestLocationUpdates("gps", 4000000L, 3000.0f, Receiver.gps_sender);
//		if (isXiaoMI()) {
//			final Calendar instance = Calendar.getInstance();
//			instance.setTimeInMillis(System.currentTimeMillis());
//			instance.add(13, 10);
//			Receiver.am.set(1, instance.getTimeInMillis(), Receiver.gps_sender);
//			return;
//		}
//		Receiver.am.set(2, SystemClock.elapsedRealtime() + 10000L, Receiver.gps_sender);
	}

	static void pos_net(final boolean net_enabled) {
		if (Receiver.net_sender == null) {
			Receiver.net_sender = PendingIntent.getBroadcast(Receiver.mContext, 0, new Intent(Receiver.mContext, (Class) LoopLocation.class), 0);
		}
		if (Receiver.net_enabled != net_enabled) {
			if (net_enabled) {
//				Receiver.lm.requestLocationUpdates("network", 600000L, 3000.0f, Receiver.net_sender);
			} else {
				Receiver.lm.removeUpdates(Receiver.net_sender);
			}
			Receiver.net_enabled = net_enabled;
		}
	}

	public static void progress() {
		Receiver.ua = engine(Receiver.mContext).GetCurUA();
		if (Receiver.call_state != 0) {
			int n;
			if (GetCurUA().IsPttMode()) {
				n = RtpStreamReceiver_group.speakermode;
			} else {
				n = RtpStreamReceiver_signal.speakermode;
			}
			int speakermode = n;
			if (n == -1) {
				speakermode = speakermode();
			}
			if (speakermode != 0) {
				if (Receiver.bluetooth > 0) {
					onText(2, Receiver.mContext.getString(R.string.menu_bluetooth), R.drawable.stat_sys_phone_call_bluetooth, Receiver.ccCall.base);
					return;
				}
				onText(2, Receiver.mContext.getString(R.string.card_title_in_progress), R.drawable.stat_sys_phone_call, Receiver.ccCall.base);
			}
		}
	}

	public static void reRegister(final int n) {
		// TODO
	}

	public static void registered() {
		pos(true);
	}

	public static void saveCallTimeBegin(final long n) {
		if (n != 0L) {
			final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
			final String string = "begin= " + n;
			final ContentValues contentValues = new ContentValues();
			contentValues.put("begin", System.currentTimeMillis());
			instance.update(CallHistoryDatabase.Table_Name, string, contentValues);
			Receiver.callBeginTime = 0L;
		}
	}

	public static void saveCallTimeEnd(final long n) {
		if (!AntaCallUtil.isAntaCall && n != 0L) {
			final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
			final String string = "begin= " + n;
			final ContentValues contentValues = new ContentValues();
			contentValues.put("end", System.currentTimeMillis());
			instance.update(CallHistoryDatabase.Table_Name, string, contentValues);
			Receiver.callBeginTime = 0L;
		}
	}

	private static void savePrtCallIn(final String s, final String s2, final long n) {
		if (AntaCallUtil.isAntaCall) {
			return;
		}
		final String format = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(n));
		final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("name", s2);
		contentValues.put("number", s);
		contentValues.put("begin", n);
		contentValues.put("begin_str", format);
		contentValues.put("type", "CallIn");
		instance.insert(CallHistoryDatabase.Table_Name, contentValues);
	}

	public static void savePrtCallOut(final String s, final String s2, final long n) {
		if (AntaCallUtil.isAntaCall) {
			return;
		}
		final String format = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(n));
		final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("name", s2);
		contentValues.put("number", s);
		contentValues.put("begin", n);
		contentValues.put("begin_str", format);
		contentValues.put("type", "CallOut");
		instance.insert(CallHistoryDatabase.Table_Name, contentValues);
	}

	public static void savePrtCallUnAc(final String s, final String s2, final long n) {
		if (AntaCallUtil.isAntaCall) {
			return;
		}
		final String format = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(n));
		final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("name", s2);
		contentValues.put("number", s);
		contentValues.put("begin", n);
		contentValues.put("begin_str", format);
		contentValues.put("type", "CallUnak");
		instance.insert(CallHistoryDatabase.Table_Name, contentValues);
	}

	public static void savePrtCallUnOut(final String s, final String s2, final long n) {
		if (AntaCallUtil.isAntaCall) {
			return;
		}
		final String format = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(n));
		final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("name", s2);
		contentValues.put("number", s);
		contentValues.put("begin", n);
		contentValues.put("begin_str", format);
		contentValues.put("type", "CallUnout");
		instance.insert(CallHistoryDatabase.Table_Name, contentValues);
	}

	public static void saveTmpGrpCallHistory(final String s, final ArrayList<String> list) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ", Locale.CHINA);
		final long currentTimeMillis = System.currentTimeMillis();
		final String format = simpleDateFormat.format(new Date(currentTimeMillis));
		final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); ++i) {
			sb.append(list.get(i));
			if (i != list.size() - 1) {
				sb.append(",");
			}
		}
		final ContentValues contentValues = new ContentValues();
		contentValues.put("name", String.valueOf(s) + "(" + list.size() + ")");
		contentValues.put("number", sb.toString());
		contentValues.put("begin", currentTimeMillis);
		contentValues.put("begin_str", format);
		contentValues.put("type", "TempGrpCall");
		instance.insert(CallHistoryDatabase.Table_Name, contentValues);
		Receiver.callBeginTime = currentTimeMillis;
		Log.i("zdx", "---------------saveTmpGrpCallHistory-------" + s + "-----------");
	}

	public static void saveUnAcceptCall(final long n) {
		if (n != 0L) {
			final CallHistoryDatabase instance = CallHistoryDatabase.getInstance(Receiver.mContext);
			final String string = "begin= " + n;
			final ContentValues contentValues = new ContentValues();
			contentValues.put("type", "CallUnout");
			instance.update(CallHistoryDatabase.Table_Name, string, contentValues);
			Receiver.callBeginTime = 0L;
		}
	}

	private static void sendBroadcast4Cal(final int n, final CallManager.CallParams callParams) {
		Intent intent;
		if (AntaCallUtil.isAntaCall) {
			intent = new Intent(CallActivity2.ACTION_CHANGE_CALL_STATE);
			final Bundle bundle = new Bundle();
			bundle.putInt(CallActivity2.NEWSTATE, n);
			intent.putExtras(bundle);
		} else {
			final Intent intent2 = new Intent(CallActivity.ACTION_CHANGE_CALL_STATE);
			CallManager.putExtras(callParams, intent2);
			final Bundle bundle2 = new Bundle();
			bundle2.putInt(CallActivity.NEWSTATE, n);
			intent2.putExtras(bundle2);
			intent = intent2;
		}
		SipUAApp.mContext.sendBroadcast(intent);
	}

	public static void showCameraActivity(final boolean b, final String s, final String callerName) {
		Receiver.ua = engine(Receiver.mContext).GetCurUA();
		progress();
		final Intent intent = new Intent(Receiver.mContext, (Class) DemoCallScreen.class);
		intent.putExtra("IsCallIn", b);
		final CallManager manager = CallManager.getManager();
		ExtendedCall extendedCall;
		if (b) {
			extendedCall = manager.getVideoCall(CallManager.CallState.INCOMING, s);
		} else {
			extendedCall = manager.getVideoCall(CallManager.CallState.OUTGOING, s);
		}
		if (extendedCall != null) {
			extendedCall.setCallerName(callerName);
		}
		intent.putExtra("com.zed3.extra.CALL_ID", CallManager.getCallExtId(extendedCall));
		intent.putExtra("com.zed3.extra.CALL_TYPE", extendedCall.getCallType().toString());
		intent.putExtra("com.zed3.extra.CALL_USERNUMBER", s);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Receiver.mContext.startActivity(intent);
	}

	public static int speakermode() {
		if (Receiver.docked > 0 && Receiver.headset <= 0) {
			return 0;
		}
		return 2;
	}

	public static void stopRingtone() {
		if (Receiver.v != null) {
			Receiver.v.cancel();
			Receiver.v = null;
		}
		if (Receiver.mMediaPlayer == null) {
			return;
		}
		while (true) {
			try {
				Receiver.mMediaPlayer.stop();
				Receiver.mMediaPlayer.release();
				Receiver.mMediaPlayer = null;
				if (AudioModeUtils.isModeRingTone() && GetCurUA().IsPttMode()) {
					if (!SipUAApp.isHeadsetConnected) {
						AudioModeUtils.setAudioStyle(0, true);
						return;
					}
					AudioModeUtils.setAudioStyle(0, false);
				}
			} catch (Exception ex) {
				continue;
			}
			break;
		}
	}

	static void updateAutoAnswer() {
		if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("auto_on_demand", false) || !SipUAApp.on(Receiver.mContext)) {
			updateAutoAnswer(-1);
			return;
		}
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("auto_demand", false)) {
			updateAutoAnswer(1);
			return;
		}
		updateAutoAnswer(0);
	}

	static void updateAutoAnswer(final int n) {
	}

	private void updateUI() {
		SipUAApp.mContext.sendBroadcast(new Intent(MessageDialogueActivity.REFRESH_UI));
	}

	public static void url(final String s) {
		new Thread() {
			@Override
			public void run() {
				try {
					new BufferedReader(new InputStreamReader(new URL(String.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("posurl", "")) + "?" + s).openStream())).close();
				} catch (IOException ex) {
				}
			}
		}.start();
	}

	int asu(final ScanResult scanResult) {
		if (scanResult == null) {
			return 0;
		}
		return Math.round((scanResult.level + 113.0f) / 2.0f);
	}

	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		Label_0292:
		{
			if (action.equals("android.intent.action.LOCALE_CHANGED")) {
				if (Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("languageId", 0) != 0) {
					LanguageChange.upDateLanguage(Receiver.mContext);
				} else {
					LanguageChange.upDateLanguage(Receiver.mContext);
					GroupListUtil.removeDataOfGroupList();
					final SharedPreferences sharedPreferences = context.getSharedPreferences("notifyInfo", 0);
					final int int1 = sharedPreferences.getInt("type", -1);
					if (int1 != -1) {
						final String string = sharedPreferences.getString("text", (String) null);
						final int int2 = sharedPreferences.getInt("mInCallResId", 0);
						final long long1 = sharedPreferences.getLong("base", 0L);
						String s = null;
						Label_0157:
						{
							if ("\u767b\u5f55\u6210\u529f".equals(string) || "Login successfully!".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.regok);
							} else if ("\u767b\u5f55\u5931\u8d25".equals(string) || "Login failed".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.regfailed);
							} else if ("\u6b63\u5728\u767b\u5f55...".equals(string) || "Loginning".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.reg);
							} else if ("\u901a\u8bdd\u4e2d".equals(string) || "In-Call".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.card_title_in_progress);
							} else if ("\u6765\u7535".equals(string) || "Incoming".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.card_title_incoming_call);
							} else if ("\u901a\u8bdd\u7ed3\u675f".equals(string) || "Call End".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.card_title_call_ended);
							} else if ("\u901a\u8bdd\u4fdd\u6301\u4e2d".equals(string) || "In the to keep".equals(string)) {
								s = Receiver.mContext.getResources().getString(R.string.card_title_on_hold);
							} else {
								if (!"\u62e8\u53f7\u4e2d".equals(string)) {
									s = string;
									if (!"Dialing".equals(string)) {
										break Label_0157;
									}
								}
								s = Receiver.mContext.getResources().getString(R.string.card_title_dialing);
							}
						}
						onText(int1, s, int2, long1);
						final SharedPreferences sharedPreferences2 = Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
						final boolean boolean1 = sharedPreferences2.getBoolean("flowOnOffKey", false);
						if (boolean1) {
							final Intent intent2 = new Intent(Receiver.mContext, (Class) FlowRefreshService.class);
							Receiver.mContext.stopService(intent2);
							Receiver.mContext.startService(intent2);
							sharedPreferences2.edit().putBoolean("flowOnOffKey", boolean1).commit();
						}
						if (DeviceInfo.ISAlarmShowing) {
							final Intent intent3 = new Intent(Receiver.mContext, (Class) AlarmService.class);
							Receiver.mContext.stopService(intent3);
							Receiver.mContext.startService(intent3);
						}
						break Label_0292;
					}
				}
			} else {
				if (action.equals(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE)) {
					this.updateUI();
					break Label_0292;
				}
				if (action.equals(MessageDialogueActivity.SEND_TEXT_FAIL)) {
					final String stringExtra = intent.getStringExtra("0");
					this.mSmsMmsDatabase = new SmsMmsDatabase(context);
					final ContentValues contentValues = new ContentValues();
					contentValues.put("send", 1);
					this.mSmsMmsDatabase.update("message_talk", "E_id = '" + stringExtra + "'", contentValues);
					this.updateUI();
					break Label_0292;
				}
				if (action.equals(MessageDialogueActivity.SEND_TEXT_SUCCEED)) {
					final String stringExtra2 = intent.getStringExtra("0");
					this.mSmsMmsDatabase = new SmsMmsDatabase(context);
					final ContentValues contentValues2 = new ContentValues();
					contentValues2.put("send", 0);
					this.mSmsMmsDatabase.update("message_talk", "E_id = '" + stringExtra2 + "'", contentValues2);
					this.updateUI();
					break Label_0292;
				}
				if (action.equals(MessageDialogueActivity.SEND_TEXT_TIMEOUT)) {
					final String stringExtra3 = intent.getStringExtra("0");
					this.mSmsMmsDatabase = new SmsMmsDatabase(context);
					final ContentValues contentValues3 = new ContentValues();
					contentValues3.put("send", 1);
					this.mSmsMmsDatabase.update("message_talk", "E_id = '" + stringExtra3 + "'", contentValues3);
					this.updateUI();
				}
				break Label_0292;
			}
			return;
		}
		if (action.equals("SettingLanguage")) {
			GroupListUtil.removeDataOfGroupList();
			final SharedPreferences sharedPreferences3 = context.getSharedPreferences("notifyInfo", 0);
			final int int3 = sharedPreferences3.getInt("type", 0);
			final String string2 = sharedPreferences3.getString("text", (String) null);
			final int int4 = sharedPreferences3.getInt("mInCallResId", 0);
			final long long2 = sharedPreferences3.getLong("base", 0L);
			String s2 = null;
			Label_0403:
			{
				if ("\u767b\u5f55\u6210\u529f".equals(string2) || "Login successfully!".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.regok);
				} else if ("\u767b\u5f55\u5931\u8d25".equals(string2) || "Login failed".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.regfailed);
				} else if ("\u6b63\u5728\u767b\u5f55...".equals(string2) || "Loginning".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.reg);
				} else if ("\u901a\u8bdd\u4e2d".equals(string2) || "In-Call".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.card_title_in_progress);
				} else if ("\u6765\u7535".equals(string2) || "Incoming".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.card_title_incoming_call);
				} else if ("\u901a\u8bdd\u7ed3\u675f".equals(string2) || "Call End".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.card_title_call_ended);
				} else if ("\u901a\u8bdd\u4fdd\u6301\u4e2d".equals(string2) || "In the to keep".equals(string2)) {
					s2 = Receiver.mContext.getResources().getString(R.string.card_title_on_hold);
				} else {
					if (!"\u62e8\u53f7\u4e2d".equals(string2)) {
						s2 = string2;
						if (!"Dialing".equals(string2)) {
							break Label_0403;
						}
					}
					s2 = Receiver.mContext.getResources().getString(R.string.card_title_dialing);
				}
			}
			onText(int3, s2, int4, long2);
			final SharedPreferences sharedPreferences4 = Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
			final boolean boolean2 = sharedPreferences4.getBoolean("flowOnOffKey", false);
			if (boolean2) {
				final Intent intent4 = new Intent(Receiver.mContext, (Class) FlowRefreshService.class);
				Receiver.mContext.stopService(intent4);
				Receiver.mContext.startService(intent4);
				sharedPreferences4.edit().putBoolean("flowOnOffKey", boolean2).commit();
			}
			if (DeviceInfo.ISAlarmShowing) {
				final Intent intent5 = new Intent(Receiver.mContext, (Class) AlarmService.class);
				Receiver.mContext.stopService(intent5);
				Receiver.mContext.startService(intent5);
			}
		}
		if (!SipUAApp.on(context) || action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE") || action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE") || action.equals("android.intent.action.PACKAGE_REPLACED")) {
			return;
		}
		if (action.equals("vpn.connectivity") && intent.hasExtra("connection_state")) {
			final String string3 = intent.getSerializableExtra("connection_state").toString();
			if (string3 == null || on_vpn() == string3.equals("CONNECTED")) {
				return;
			}
			on_vpn(string3.equals("CONNECTED"));
			if (Receiver.mSipdroidEngine == null) {
				return;
			}
			final SipProvider[] sip_providers = engine(context).sip_providers;
			for (int length = sip_providers.length, i = 0; i < length; ++i) {
				final SipProvider sipProvider = sip_providers[i];
				if (sipProvider != null) {
					sipProvider.haltConnections();
				}
			}
			if (Receiver.mSipdroidEngine != null) {
				engine(context).register(true);
			}
		} else if (action.equals("android.intent.action.PHONE_STATE") && !intent.getBooleanExtra(context.getString(R.string.app_name), false)) {
			stopRingtone();
			Receiver.pstn_state = intent.getStringExtra("state");
			Receiver.pstn_time = SystemClock.elapsedRealtime();
			if (Receiver.pstn_state.equals("IDLE") && Receiver.call_state != 0) {
				broadcastCallStateChanged(null, null);
			}
			if (((Receiver.pstn_state.equals("OFFHOOK") && Receiver.call_state == 3) || (Receiver.pstn_state.equals("IDLE") && Receiver.call_state == 4)) && Receiver.mSipdroidEngine != null) {
				engine(context).togglehold();
			}
		} else if (action.equals("android.intent.action.DOCK_EVENT")) {
			Receiver.docked = intent.getIntExtra("android.intent.extra.DOCK_STATE", -1);
			if (Receiver.call_state == 3 && Receiver.mSipdroidEngine != null) {
				engine(Receiver.mContext).speaker(speakermode());
			}
		} else if (action.equals("android.media.SCO_AUDIO_STATE_CHANGED")) {
			Receiver.bluetooth = intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1);
			if (Receiver.mSipdroidEngine == null) {
				return;
			}
			progress();
			if (GetCurUA().IsPttMode()) {
				RtpStreamSender_group.changed = true;
				return;
			}
			RtpStreamSender_signal.changed = true;
		} else if (action.equals("android.intent.action.HEADSET_PLUG")) {
			Receiver.headset = intent.getIntExtra("state", -1);
			if (Receiver.mSipdroidEngine == null) {
				return;
			}
			if (Receiver.call_state == 3) {
				engine(Receiver.mContext).speaker(speakermode());
				return;
			}
			if (engine(Receiver.mContext).GetCurUA().IsPttMode()) {
				engine(Receiver.mContext).GetCurUA().pttSpeakerControl();
			}
		} else {
			if (action.equals("android.intent.action.SCREEN_ON")) {
				MyLog.i(Receiver.tag, "ACTION_SCREEN_ON");
				return;
			}
			if (action.equals("android.intent.action.USER_PRESENT")) {
				this.mHandler.sendEmptyMessageDelayed(2, 3000L);
				return;
			}
			if (action.equals("android.intent.action.SCREEN_OFF")) {
				MyLog.i(Receiver.tag, "ACTION_SCREEN_OFF");
				return;
			}
			if (action.equals("android.intent.action.PTT.down")) {
				GroupCallUtil.makeGroupCall(true, true, UserAgent.PttPRMode.SideKeyPress);
				return;
			}
			if (action.equals("android.intent.action.PTT.up")) {
				GroupCallUtil.makeGroupCall(false, true, UserAgent.PttPRMode.Idle);
				return;
			}
			if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
				this.mHandler.sendEmptyMessageDelayed(1, 3000L);
				return;
			}
			if (!action.equals("android.net.wifi.SCAN_RESULTS") || !PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("selectwifi", false)) {
				return;
			}
			final WifiManager wifiManager = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			String ssid = null;
			int n = 0;
			final boolean b = false;
			if (connectionInfo != null) {
				ssid = connectionInfo.getSSID();
			}
			final List scanResults = wifiManager.getScanResults();
			final List configuredNetworks = wifiManager.getConfiguredNetworks();
			if (configuredNetworks == null) {
				return;
			}
			WifiConfiguration wifiConfiguration = null;
			final WifiConfiguration wifiConfiguration2 = null;
			WifiConfiguration wifiConfiguration3 = null;
//			for (final WifiConfiguration wifiConfiguration4 : configuredNetworks) {
//				if (wifiConfiguration3 == null || wifiConfiguration4.priority > wifiConfiguration3.priority) {
//					wifiConfiguration3 = wifiConfiguration4;
//				}
//			}
			ScanResult scanResult = null;
			final ScanResult scanResult2 = null;
			ScanResult scanResult3 = null;
			final ScanResult scanResult4 = null;
			if (scanResults != null) {
				final Iterator<ScanResult> iterator2 = scanResults.iterator();
				scanResult3 = scanResult4;
				scanResult = scanResult2;
				wifiConfiguration = wifiConfiguration2;
				n = (b ? 1 : 0);
				while (iterator2.hasNext()) {
					final ScanResult scanResult5 = iterator2.next();
					int n2 = n;
					if (ssid != null) {
						n2 = n;
						if (ssid.equals(scanResult5.SSID)) {
							n2 = 1;
						}
					}
					final Iterator<WifiConfiguration> iterator3 = configuredNetworks.iterator();
					ScanResult scanResult6 = scanResult3;
					ScanResult scanResult7 = scanResult;
					WifiConfiguration wifiConfiguration5 = wifiConfiguration;
					while (true) {
						n = n2;
						wifiConfiguration = wifiConfiguration5;
						scanResult = scanResult7;
						scanResult3 = scanResult6;
						if (!iterator3.hasNext()) {
							break;
						}
						final WifiConfiguration wifiConfiguration6 = iterator3.next();
						if (wifiConfiguration6.SSID == null || !wifiConfiguration6.SSID.equals("\"" + scanResult5.SSID + "\"")) {
							continue;
						}
						WifiConfiguration wifiConfiguration7 = null;
						ScanResult scanResult8 = null;
						Label_2477:
						{
							if (scanResult7 != null) {
								wifiConfiguration7 = wifiConfiguration5;
								scanResult8 = scanResult7;
								if (scanResult5.level <= scanResult7.level) {
									break Label_2477;
								}
							}
							scanResult8 = scanResult5;
							wifiConfiguration7 = wifiConfiguration6;
						}
						wifiConfiguration5 = wifiConfiguration7;
						scanResult7 = scanResult8;
						if (wifiConfiguration6 != wifiConfiguration3) {
							continue;
						}
						scanResult6 = scanResult5;
						wifiConfiguration5 = wifiConfiguration7;
						scanResult7 = scanResult8;
					}
				}
			}
			if (wifiConfiguration == null || wifiConfiguration.priority == wifiConfiguration3.priority || this.asu(scanResult) <= this.asu(scanResult3) * 1.5 || (ssid != null && n == 0)) {
				return;
			}
			if (ssid == null || !ssid.equals(scanResult.SSID)) {
				wifiManager.disconnect();
			}
			wifiConfiguration.priority = wifiConfiguration3.priority + 1;
			wifiManager.updateNetwork(wifiConfiguration);
			wifiManager.enableNetwork(wifiConfiguration.networkId, true);
			wifiManager.saveConfiguration();
			if (ssid == null || !ssid.equals(scanResult.SSID)) {
				wifiManager.reconnect();
			}
		}
	}
}
