package com.zed3.sipua;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.net.KeepAliveSip;
import com.zed3.sipua.ui.LoopAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SipdroidEngine implements RegisterAgentListener {
	public static final int INITIALIZED = 2;
	public static final int LINES = 1;
	public static final int UNINITIALIZED = 0;
	static long lasthalt = 0L;
	static long lastpwl = 0L;
	public static PowerManager.WakeLock[] pwl;
	public static long serverTimeVal = 0L;
	private static final String tag = "SipdroidEngine";
	public static PowerManager.WakeLock[] wl;
	public int isMakeVideoTRANSCRIBE;
	private KeepAliveSip[] kas;
	String[] lastmsgs;
	private Object mLock;
	public int pref;
	public RegisterAgent[] ras;
	public SipProvider[] sip_providers;
	public UserAgent ua;
	public UserAgent[] uas;
	public UserAgentProfile[] user_profiles;

	static {
		SipdroidEngine.serverTimeVal = 0L;
	}

	public SipdroidEngine() {
		this.isMakeVideoTRANSCRIBE = -1;
		this.mLock = new Object();
	}

	private String getContactURL(String string, final SipProvider sipProvider) {
		final int index = string.indexOf("@");
		String substring = string;
		if (index != -1) {
			substring = string.substring(0, index);
		}
		final StringBuilder append = new StringBuilder(String.valueOf(substring)).append("@").append(IpAddress.localIpAddress);
		if (sipProvider.getPort() != 0) {
			string = ":" + sipProvider.getPort();
		} else {
			string = "";
		}
		return append.append(string).append(";transport=").append(sipProvider.getDefaultTransport()).toString();
	}

	private UserAgent.GrpCallSetupType getPriority(final int n) {
		if (n == 0) {
			return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		}
		if (n == 1) {
			return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT;
		}
		return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_REJECT;
	}

	private void innnerHalt(final boolean b) {
		final long elapsedRealtime = SystemClock.elapsedRealtime();
		int n = 0;
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			this.unregister(n);
			while (registerAgent != null && registerAgent.CurrentState != 1 && SystemClock.elapsedRealtime() - elapsedRealtime < 2000L) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ex) {
				}
			}
			if (SipdroidEngine.wl[n].isHeld()) {
				SipdroidEngine.wl[n].release();
				if (SipdroidEngine.pwl[n] != null && SipdroidEngine.pwl[n].isHeld()) {
					SipdroidEngine.pwl[n].release();
				}
			}
			if (this.kas[n] != null) {
				Receiver.alarm(0, LoopAlarm.class);
				this.kas[n].halt();
			}
			Receiver.onText(n + 5, null, 0, 0L);
			if (registerAgent != null) {
				registerAgent.halt();
			}
			if (this.uas[n] != null) {
				this.uas[n].hangup();
				this.uas[n].HaltGroupCall();
				if (b) {
					this.uas[n].haltListen();
				} else {
					this.uas[n].haltListenNotCloseGps();
				}
			}
			SystemClock.sleep(500L);
			if (this.sip_providers[n] != null) {
				this.sip_providers[n].halt();
			}
			++n;
		}
	}

	private long timeConvert(final String s) {
		// TODO
		long time = -1L;
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			time = simpleDateFormat.parse(s.trim()).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MyLog.i("SipdroidEngine", "realtime = " + simpleDateFormat2.format(new Date(time)) + "\n" + simpleDateFormat2.format(new Date()));
		return time;
	}

	public void CheckEngine() {
		int n = 0;
		final SipProvider[] sip_providers = this.sip_providers;
		for (int length = sip_providers.length, i = 0; i < length; ++i) {
			final SipProvider sipProvider = sip_providers[i];
			if (sipProvider != null && !sipProvider.hasOutboundProxy()) {
				this.setOutboundProxy(sipProvider, n);
			}
			++n;
		}
	}

	public UserAgent GetCurUA() {
		return this.ua;
	}

	public boolean StartEngine() {
		LogUtil.makeLog("SipdroidEngine", "StartEngine()");
		// TODO
		return true;
	}

	public void answercall() {
		synchronized (this.mLock) {
			this.ua.accept();
		}
	}

	public boolean antaCall1(final String s, final String s2, final boolean b, final boolean b2) {
		if (Receiver.mSipdroidEngine.isRegistered()) {
			int n = this.pref;
			boolean b3 = false;
			boolean b4 = false;
			Label_0040:
			{
				if (this.isRegistered(n) && Receiver.isFast(n)) {
					b4 = true;
				} else {
					int i;
					for (i = 0; i < 1; ++i) {
						if (this.isRegistered(i) && Receiver.isFast(i)) {
							b3 = true;
							break;
						}
					}
					b4 = b3;
					n = i;
					if (!b3) {
						b4 = b3;
						n = i;
						if (b) {
							n = this.pref;
							if (Receiver.isFast(n)) {
								b4 = true;
							} else {
								int n2 = 0;
								while (true) {
									b4 = b3;
									n = n2;
									if (n2 >= 1) {
										break Label_0040;
									}
									if (Receiver.isFast(n2)) {
										break;
									}
									++n2;
								}
								b4 = true;
								n = n2;
							}
						}
					}
				}
			}
			if (b4 && (this.ua = this.uas[n]) != null) {
				this.ua.printLog("UAC: CALLING " + s);
				this.ua.user_profile.video = false;
				return this.ua.antaCall2(s, s2, false, b2);
			}
			if (PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getBoolean("callback", false) && PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("posurl", "").length() > 0) {
				Receiver.url("n=" + Uri.decode(s));
				return true;
			}
		}
		return false;
	}

	public boolean call(final String s, final boolean b, final boolean b2) {
		return this.call(s, b, b2, null);
	}

	public boolean call(final String s, final boolean b, final boolean b2, final String s2) {
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			return false;
		}
		int n = this.pref;
		boolean b3 = false;
		boolean b4 = false;
		Label_0040:
		{
			if (this.isRegistered(n) && Receiver.isFast(n)) {
				b4 = true;
			} else {
				int i;
				for (i = 0; i < 1; ++i) {
					if (this.isRegistered(i) && Receiver.isFast(i)) {
						b3 = true;
						break;
					}
				}
				b4 = b3;
				n = i;
				if (!b3) {
					b4 = b3;
					n = i;
					if (b) {
						n = this.pref;
						if (Receiver.isFast(n)) {
							b4 = true;
						} else {
							int n2 = 0;
							while (true) {
								b4 = b3;
								n = n2;
								if (n2 >= 1) {
									break Label_0040;
								}
								if (Receiver.isFast(n2)) {
									break;
								}
								++n2;
							}
							b4 = true;
							n = n2;
						}
					}
				}
			}
		}
		if (b4 && (this.ua = this.uas[n]) != null) {
			this.ua.printLog("UAC: CALLING " + s);
			if (!this.ua.user_profile.audio && !this.ua.user_profile.video) {
				this.ua.printLog("ONLY SIGNALING, NO MEDIA");
			}
			final StringBuilder sb = new StringBuilder("isMakeVideoCall is:");
			String s3;
			if (b2) {
				s3 = "true";
			} else {
				s3 = "false";
			}
			MyLog.i("sipdroidEngine", sb.append(s3).toString());
			return this.ua.call(s, false, b2, s2);
		}
		if (PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getBoolean("callback", false) && PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("posurl", "").length() > 0) {
			Receiver.url("n=" + Uri.decode(s));
			return true;
		}
		return false;
	}

	public void expire() {
		Receiver.expire_time = 0L;
		int n = 0;
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			if (registerAgent != null && registerAgent.CurrentState == 3) {
				registerAgent.CurrentState = 1;
				Receiver.onText(n + 5, null, 0, 0L);
			}
			++n;
		}
		this.register(false);
	}

	public void expire(final int n) {
		if (n == -1) {
			GroupCallUtil.changeUI(false);
			SipUAApp.isfirst_login = true;
			SipUAApp.isone_hour = false;
		}
		if (Receiver.call_state != 0) {
			Receiver.engine(Receiver.mContext).rejectcall();
		}
		Receiver.expire_time = 0L;
		int n2 = 0;
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			if (registerAgent != null && registerAgent.CurrentState == 3) {
				registerAgent.register(n);
				registerAgent.CurrentState = 1;
				Receiver.onText(n2 + 5, null, 0, 0L);
			}
			++n2;
		}
	}

	public int getLocalVideo() {
		return this.ua.local_video_port;
	}

	public String getRemoteAddr() {
		return this.ua.remote_media_address;
	}

	public int getRemoteVideo() {
		return this.ua.remote_video_port;
	}

	public Context getUIContext() {
		return Receiver.mContext;
	}

	UserAgentProfile getUserAgentProfile(final String s) {
		boolean pub = false;
		final UserAgentProfile userAgentProfile = new UserAgentProfile(null);
		userAgentProfile.username = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("username" + s, "");
		userAgentProfile.passwd = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("password" + s, "");
		if (PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("domain" + s, "").length() == 0) {
			userAgentProfile.realm = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("server" + s, "");
		} else {
			userAgentProfile.realm = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("domain" + s, "");
		}
		userAgentProfile.realm_orig = userAgentProfile.realm;
		if (PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("fromuser" + s, "").length() == 0) {
			userAgentProfile.from_url = userAgentProfile.username;
		} else {
			userAgentProfile.from_url = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("fromuser" + s, "");
		}
		userAgentProfile.qvalue = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getString("mmtel_qvalue", "1.00");
		userAgentProfile.mmtel = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getBoolean("mmtel", false);
		if (PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getBoolean("edge" + s, false) || PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).getBoolean("3g" + s, false)) {
			pub = true;
		}
		userAgentProfile.pub = pub;
		return userAgentProfile;
	}

	public void halt() {
		LogUtil.makeLog("SipdroidEngine", "halt()");
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stackTrace.length; ++i) {
			final StackTraceElement stackTraceElement = stackTrace[i];
			MyLog.d("test", "SipdroidEngine#halt called by:" + stackTraceElement.getClassName() + " , " + stackTraceElement.getMethodName());
		}
		this.innnerHalt(true);
	}

	public void haltNotCloseGps() {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stackTrace.length; ++i) {
			final StackTraceElement stackTraceElement = stackTrace[i];
			MyLog.d("test", "SipdroidEngine#haltNotCloseGps called by:" + stackTraceElement.getClassName() + " , " + stackTraceElement.getMethodName());
		}
		this.innnerHalt(false);
	}

	public void info(final char c, final int n) {
		this.ua.info(c, n);
	}

	public boolean isRegistered() {
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			if (registerAgent != null && registerAgent.isRegistered()) {
				return true;
			}
		}
		return false;
	}

	public boolean isRegistered(final int n) {
		return n < this.ras.length && this.ras[n] != null && this.ras[n].isRegistered();
	}

	public boolean isRegistered(final boolean b) {
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			if (registerAgent != null && registerAgent.isRegistered(b)) {
				return true;
			}
		}
		return false;
	}

	public void keepAlive() {
		// TODO
	}

	public void listen() {
		final UserAgent[] uas = this.uas;
		for (int length = uas.length, i = 0; i < length; ++i) {
			final UserAgent userAgent = uas[i];
			if (userAgent != null) {
				userAgent.printLog("UAS: WAITING FOR INCOMING CALL");
				if (!userAgent.user_profile.audio && !userAgent.user_profile.video) {
					userAgent.printLog("ONLY SIGNALING, NO MEDIA");
				}
				userAgent.listen();
			}
		}
	}

	@Override
	public void onMWIUpdate(final RegisterAgent registerAgent, final boolean b, final int n, final String mwi_account) {
		int n2 = 0;
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, n3 = 0; n3 < length && ras[n3] != registerAgent; ++n3) {
			++n2;
		}
		if (n2 == this.pref) {
			this.uas[n2].OnRegisterFailure();
			this.ras[n2].CurrentState = 1;
			if (!b) {
				Receiver.onText(1, null, 0, 0L);
				this.lastmsgs[n2] = null;
				return;
			}
			String s2;
			final String s = s2 = this.getUIContext().getString(R.string.voicemail);
			if (n != 0) {
				s2 = String.valueOf(s) + ": " + n;
			}
			Receiver.MWI_account = mwi_account;
			if (this.lastmsgs[n2] == null || !s2.equals(this.lastmsgs[n2])) {
				Receiver.onText(1, s2, 17301630, 0L);
				this.lastmsgs[n2] = s2;
			}
		}
	}

	@Override
	public void onUaRegistrationFailure(final RegisterAgent registerAgent, final NameAddress nameAddress, final NameAddress nameAddress2, final String s) {
		MyLog.i("Register", "Register failed reason:" + s);
		final Intent intent = new Intent("com.zed3.sipua.login");
		intent.putExtra("result", s);
		Receiver.mContext.sendBroadcast(intent);
		final boolean b = false;
		final RegisterAgent[] ras = this.ras;
		for (int length = ras.length, n = 0; n < length && ras[n] != registerAgent; ++n) {
		}
		boolean b2;
		if (this.isRegistered(0)) {
			registerAgent.CurrentState = 1;
			Receiver.onText(5, null, 0, 0L);
			b2 = b;
		} else {
			b2 = true;
			Receiver.onText(5, this.getUIContext().getString(R.string.regfailed), R.drawable.sym_presence_away, 0L);
		}
		if (b2 && SystemClock.uptimeMillis() > SipdroidEngine.lastpwl + 45000L && SipdroidEngine.pwl[0] != null && !SipdroidEngine.pwl[0].isHeld() && Receiver.on_wlan) {
			SipdroidEngine.lastpwl = SystemClock.uptimeMillis();
			if (SipdroidEngine.wl[0].isHeld()) {
				SipdroidEngine.wl[0].release();
			}
			SipdroidEngine.pwl[0].acquire();
			this.register(true);
			if (!SipdroidEngine.wl[0].isHeld() && SipdroidEngine.pwl[0].isHeld()) {
				SipdroidEngine.pwl[0].release();
			}
		} else if (SipdroidEngine.wl[0].isHeld()) {
			SipdroidEngine.wl[0].release();
			if (SipdroidEngine.pwl[0] != null && SipdroidEngine.pwl[0].isHeld()) {
				SipdroidEngine.pwl[0].release();
			}
		}
		if (SystemClock.uptimeMillis() > SipdroidEngine.lasthalt + 45000L) {
			SipdroidEngine.lasthalt = SystemClock.uptimeMillis();
			this.sip_providers[0].haltConnections();
		}
		if (!Thread.currentThread().getName().equals("main")) {
			this.updateDNS();
		}
		registerAgent.stopMWI();
		((WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE)).startScan();
	}

	@Override
	public void onUaRegistrationSuccess(final RegisterAgent p0, final NameAddress p1, final NameAddress p2, final String p3, final Message p4) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: getfield        com/zed3/sipua/SipdroidEngine.ras:[Lcom/zed3/sipua/RegisterAgent;
		//     4: astore_2
		//     5: aload_2
		//     6: arraylength
		//     7: istore          7
		//     9: iconst_0
		//    10: istore          6
		//    12: iload           6
		//    14: iload           7
		//    16: if_icmplt       362
		//    19: aload_0
		//    20: iconst_0
		//    21: invokevirtual   com/zed3/sipua/SipdroidEngine.isRegistered:(I)Z
		//    24: ifeq            395
		//    27: ldc_w           "register"
		//    30: new             Ljava/lang/StringBuilder;
		//    33: dup
		//    34: ldc_w           "success"
		//    37: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    40: invokestatic    java/lang/Thread.currentThread:()Ljava/lang/Thread;
		//    43: invokevirtual   java/lang/Thread.getId:()J
		//    46: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//    49: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    52: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//    55: pop
		//    56: getstatic       com/zed3/sipua/ui/Receiver.on_wlan:Z
		//    59: ifeq            79
		//    62: bipush          60
		//    64: ldc             Lcom/zed3/sipua/ui/LoopAlarm;.class
		//    66: invokestatic    com/zed3/sipua/ui/Receiver.alarm:(ILjava/lang/Class;)V
		//    69: ldc_w           "2==============================="
		//    72: ldc_w           "2==============================="
		//    75: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//    78: pop
		//    79: aload_0
		//    80: invokevirtual   com/zed3/sipua/SipdroidEngine.getUIContext:()Landroid/content/Context;
		//    83: astore_2
		//    84: aload_0
		//    85: getfield        com/zed3/sipua/SipdroidEngine.pref:I
		//    88: ifne            379
		//    91: ldc_w           R.string.regok
		//    94: istore          6
		//    96: iconst_5
		//    97: aload_2
		//    98: iload           6
		//   100: invokevirtual   android/content/Context.getString:(I)Ljava/lang/String;
		//   103: ldc_w           R.drawable.icon64
		//   106: lconst_0
		//   107: invokestatic    com/zed3/sipua/ui/Receiver.onText:(ILjava/lang/String;IJ)V
		//   110: aload_1
		//   111: iconst_0
		//   112: putfield        com/zed3/sipua/RegisterAgent.subattempts:I
		//   115: invokestatic    com/zed3/sipua/ui/Receiver.registered:()V
		//   118: aload           5
		//   120: ldc_w           "Date"
		//   123: invokevirtual   org/zoolu/sip/message/Message.getHeader:(Ljava/lang/String;)Lorg/zoolu/sip/header/Header;
		//   126: astore_1
		//   127: aload_1
		//   128: ifnull          281
		//   131: aload_1
		//   132: invokevirtual   org/zoolu/sip/header/Header.getValue:()Ljava/lang/String;
		//   135: astore_1
		//   136: ldc             "SipdroidEngine"
		//   138: new             Ljava/lang/StringBuilder;
		//   141: dup
		//   142: ldc_w           "dateheader value = "
		//   145: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   148: aload_1
		//   149: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   152: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   155: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   158: aload_1
		//   159: invokestatic    android/text/TextUtils.isEmpty:(Ljava/lang/CharSequence;)Z
		//   162: ifne            281
		//   165: invokestatic    java/lang/System.currentTimeMillis:()J
		//   168: aload_0
		//   169: aload_1
		//   170: invokespecial   com/zed3/sipua/SipdroidEngine.timeConvert:(Ljava/lang/String;)J
		//   173: lsub
		//   174: putstatic       com/zed3/sipua/SipdroidEngine.serverTimeVal:J
		//   177: ldc             "SipdroidEngine"
		//   179: new             Ljava/lang/StringBuilder;
		//   182: dup
		//   183: ldc_w           "serverTime value = "
		//   186: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   189: getstatic       com/zed3/sipua/SipdroidEngine.serverTimeVal:J
		//   192: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   195: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   198: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   201: getstatic       android/os/Build.MODEL:Ljava/lang/String;
		//   204: invokevirtual   java/lang/String.toLowerCase:()Ljava/lang/String;
		//   207: ldc_w           "fh688"
		//   210: invokevirtual   java/lang/String.contains:(Ljava/lang/CharSequence;)Z
		//   213: ifeq            281
		//   216: new             Ljava/text/SimpleDateFormat;
		//   219: dup
		//   220: ldc             "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
		//   222: getstatic       java/util/Locale.ENGLISH:Ljava/util/Locale;
		//   225: invokespecial   java/text/SimpleDateFormat.<init>:(Ljava/lang/String;Ljava/util/Locale;)V
		//   228: astore_2
		//   229: aload_2
		//   230: ldc             "GMT"
		//   232: invokestatic    java/util/TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
		//   235: invokevirtual   java/text/SimpleDateFormat.setTimeZone:(Ljava/util/TimeZone;)V
		//   238: aload_2
		//   239: aload_1
		//   240: invokevirtual   java/lang/String.trim:()Ljava/lang/String;
		//   243: invokevirtual   java/text/SimpleDateFormat.parse:(Ljava/lang/String;)Ljava/util/Date;
		//   246: invokevirtual   java/util/Date.getTime:()J
		//   249: lstore          8
		//   251: lload           8
		//   253: ldc2_w          1000
		//   256: ldiv
		//   257: ldc2_w          2147483647
		//   260: lcmp
		//   261: ifge            281
		//   264: getstatic       com/zed3/sipua/SipUAApp.mContext:Landroid/content/Context;
		//   267: ldc_w           "alarm"
		//   270: invokevirtual   android/content/Context.getSystemService:(Ljava/lang/String;)Ljava/lang/Object;
		//   273: checkcast       Landroid/app/AlarmManager;
		//   276: lload           8
		//   278: invokevirtual   android/app/AlarmManager.setTime:(J)V
		//   281: aload_0
		//   282: getfield        com/zed3/sipua/SipdroidEngine.uas:[Lcom/zed3/sipua/UserAgent;
		//   285: iconst_0
		//   286: aaload
		//   287: aload           4
		//   289: invokevirtual   com/zed3/sipua/UserAgent.OnRegisterSuccess:(Ljava/lang/String;)V
		//   292: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//   295: new             Landroid/content/Intent;
		//   298: dup
		//   299: ldc_w           "com.zed3.sipua.login"
		//   302: invokespecial   android/content/Intent.<init>:(Ljava/lang/String;)V
		//   305: ldc_w           "loginstatus"
		//   308: iconst_1
		//   309: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Z)Landroid/content/Intent;
		//   312: invokevirtual   android/content/Context.sendBroadcast:(Landroid/content/Intent;)V
		//   315: getstatic       com/zed3/sipua/SipdroidEngine.wl:[Landroid/os/PowerManager.WakeLock;
		//   318: iconst_0
		//   319: aaload
		//   320: invokevirtual   android/os/PowerManager.WakeLock.isHeld:()Z
		//   323: ifeq            361
		//   326: getstatic       com/zed3/sipua/SipdroidEngine.wl:[Landroid/os/PowerManager.WakeLock;
		//   329: iconst_0
		//   330: aaload
		//   331: invokevirtual   android/os/PowerManager.WakeLock.release:()V
		//   334: getstatic       com/zed3/sipua/SipdroidEngine.pwl:[Landroid/os/PowerManager.WakeLock;
		//   337: iconst_0
		//   338: aaload
		//   339: ifnull          361
		//   342: getstatic       com/zed3/sipua/SipdroidEngine.pwl:[Landroid/os/PowerManager.WakeLock;
		//   345: iconst_0
		//   346: aaload
		//   347: invokevirtual   android/os/PowerManager.WakeLock.isHeld:()Z
		//   350: ifeq            361
		//   353: getstatic       com/zed3/sipua/SipdroidEngine.pwl:[Landroid/os/PowerManager.WakeLock;
		//   356: iconst_0
		//   357: aaload
		//   358: invokevirtual   android/os/PowerManager.WakeLock.release:()V
		//   361: return
		//   362: aload_2
		//   363: iload           6
		//   365: aaload
		//   366: aload_1
		//   367: if_acmpeq       19
		//   370: iload           6
		//   372: iconst_1
		//   373: iadd
		//   374: istore          6
		//   376: goto            12
		//   379: ldc_w           R.string.regfailed
		//   382: istore          6
		//   384: goto            96
		//   387: astore_1
		//   388: aload_1
		//   389: invokevirtual   java/text/ParseException.printStackTrace:()V
		//   392: goto            281
		//   395: ldc_w           "register"
		//   398: new             Ljava/lang/StringBuilder;
		//   401: dup
		//   402: ldc_w           "fail"
		//   405: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   408: invokestatic    java/lang/Thread.currentThread:()Ljava/lang/Thread;
		//   411: invokevirtual   java/lang/Thread.getId:()J
		//   414: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   417: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   420: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//   423: pop
		//   424: iconst_5
		//   425: aconst_null
		//   426: iconst_0
		//   427: lconst_0
		//   428: invokestatic    com/zed3/sipua/ui/Receiver.onText:(ILjava/lang/String;IJ)V
		//   431: goto            315
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  --------------------------
		//  238    281    387    395    Ljava/text/ParseException;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: cmpeq:boolean(loadelement:RegisterAgent(var_2_04:RegisterAgent[], var_6_0A:int), p0:RegisterAgent)
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

	public void register(final boolean b) {
		IpAddress.setLocalIpAddress();
		int n = 0;
		final RegisterAgent[] ras = this.ras;
		final int length = ras.length;
		int i = 0;
		Label_0177_Outer:
		while (i < length) {
			final RegisterAgent registerAgent = ras[i];
			while (true) {
				Label_0184:
				{
					Label_0173:
					{
						try {
							if (this.user_profiles[n] == null || this.user_profiles[n].username.equals("") || this.user_profiles[n].realm.equals("")) {
								break Label_0173;
							}
							this.user_profiles[n].contact_url = this.getContactURL(this.user_profiles[n].from_url, this.sip_providers[n]);
							if (!Receiver.isFast(n)) {
								this.unregister(n);
							} else if (registerAgent != null && registerAgent.register()) {
								if (b) {
									Receiver.onText(n + 5, this.getUIContext().getString(R.string.reg), R.drawable.icon64, 0L);
								}
								SipdroidEngine.wl[n].acquire();
							}
						} catch (Exception ex) {
						}
						break Label_0184;
					}
					++n;
					++i;
					continue Label_0177_Outer;
				}
				++n;
				continue;
			}
		}
	}

	public void registerMore() {
		IpAddress.setLocalIpAddress();
		int n = 0;
		final RegisterAgent[] ras = this.ras;
		Label_0175:
		for (int length = ras.length, i = 0; i < length; ++i) {
			final RegisterAgent registerAgent = ras[i];
			while (true) {
				try {
					if (this.user_profiles[n] != null && !this.user_profiles[n].username.equals("")) {
						if (!this.user_profiles[n].realm.equals("")) {
							this.user_profiles[n].contact_url = this.getContactURL(this.user_profiles[n].from_url, this.sip_providers[n]);
							if (registerAgent != null && !registerAgent.isRegistered() && Receiver.isFast(n) && registerAgent.register()) {
								Receiver.onText(n + 5, this.getUIContext().getString(R.string.reg), R.drawable.icon64, 0L);
								SipdroidEngine.wl[n].acquire();
							}
							++n;
							continue Label_0175;
						}
					}
				} catch (Exception ex) {
					continue;
				}
				break;
			}
			++n;
		}
	}

	public void registerUdp() {
		IpAddress.setLocalIpAddress();
		int n = 0;
		final RegisterAgent[] ras = this.ras;
		final int length = ras.length;
		int i = 0;
		Label_0210_Outer:
		while (i < length) {
			final RegisterAgent registerAgent = ras[i];
			while (true) {
				Label_0217:
				{
					Label_0206:
					{
						try {
							if (this.user_profiles[n] == null || this.user_profiles[n].username.equals("") || this.user_profiles[n].realm.equals("") || this.sip_providers[n] == null || this.sip_providers[n].getDefaultTransport() == null || this.sip_providers[n].getDefaultTransport().equals("tcp")) {
								break Label_0206;
							}
							this.user_profiles[n].contact_url = this.getContactURL(this.user_profiles[n].from_url, this.sip_providers[n]);
							if (!Receiver.isFast(n)) {
								this.unregister(n);
							} else if (registerAgent != null && registerAgent.register()) {
								Receiver.onText(n + 5, this.getUIContext().getString(R.string.reg), R.drawable.icon64, 0L);
								SipdroidEngine.wl[n].acquire();
							}
						} catch (Exception ex) {
						}
						break Label_0217;
					}
					++n;
					++i;
					continue Label_0210_Outer;
				}
				++n;
				continue;
			}
		}
	}

	public void rejectCall() {
		synchronized (this.mLock) {
			Receiver.mIsRejectedByUser = true;
			this.ua.printLog("UA: HANGUP");
			this.ua.hangup();
		}
	}

	public void rejectcall() {
		Receiver.mIsRejectedByUser = true;
		CallUtil.rejectAudioCall();
		CallUtil.rejectVideoCall();
	}

	void setOutboundProxy(final SipProvider sipProvider, final int n) {
		if (sipProvider != null) {
			try {
				final IpAddress byName = IpAddress.getByName(this.getUIContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("server", ""));
				final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getUIContext());
				final StringBuilder sb = new StringBuilder("port");
				Serializable value;
				if (n != 0) {
					value = n;
				} else {
					value = "";
				}
				sipProvider.setOutboundProxy(new SocketAddress(byName, Integer.valueOf(defaultSharedPreferences.getString(sb.append(value).toString(), Settings.DEFAULT_PORT))));
			} catch (Exception ex) {
			}
		}
	}

	public void speaker(final int n) {
		this.ua.speakerMediaApplication(n);
		Receiver.progress();
	}

	public void togglebluetooth() {
		this.ua.bluetoothMediaApplication();
		Receiver.progress();
	}

	public void togglehold() {
		this.ua.reInvite(null, 0);
	}

	public void togglemute() {
		if (!this.ua.muteMediaApplication()) {
			Receiver.progress();
		}
	}

	public void transfer(final String s) {
		this.ua.callTransfer(s, 0);
	}

	public void unregister(final int n) {
	}

	public void updateDNS() {
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this.getUIContext()).edit();
		int n = 0;
		final SipProvider[] sip_providers = this.sip_providers;
		final int length = sip_providers.length;
		int i = 0;
		while (i < length) {
			final SipProvider sipProvider = sip_providers[i];
			while (true) {
				try {
					final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getUIContext());
					final StringBuilder sb = new StringBuilder("server");
					Serializable value;
					if (n != 0) {
						value = n;
					} else {
						value = "";
					}
					edit.putString("dns" + n, IpAddress.getByName(defaultSharedPreferences.getString(sb.append(value).toString(), "")).toString());
					edit.commit();
					this.setOutboundProxy(sipProvider, n);
					++n;
					++i;
				} catch (UnknownHostException ex) {
					++n;
					continue;
				}
				break;
			}
		}
	}
}
