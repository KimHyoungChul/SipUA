package com.zed3.sipua;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.zed3.addressbook.AddressBookUtils;
import com.zed3.addressbook.DataBaseService;
import com.zed3.audio.AudioModeUtils;
import com.zed3.audio.AudioSettings;
import com.zed3.bluetooth.MyPhoneStateListener;
import com.zed3.codecs.AmrNB;
import com.zed3.codecs.Codec;
import com.zed3.codecs.Codecs;
import com.zed3.customgroup.CustomGroupManager;
import com.zed3.customgroup.PttCustomGrp;
import com.zed3.flow.TotalFlowThread;
import com.zed3.groupmessage.GroupMessage;
import com.zed3.location.GPSPacket;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.location.MyHandlerThread;
import com.zed3.log.CrashHandler;
import com.zed3.log.MyLog;
import com.zed3.media.JAudioLauncher;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.media.TipSoundPlayer;
import com.zed3.net.SipdroidSocket;
import com.zed3.settings.SettingsInfo;
import com.zed3.sipua.message.CommonUtil;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.message.MessageSender;
import com.zed3.sipua.message.MmsMessageService;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.ui.MyHeartBeatReceiver;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.TempGroupCallUtil;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.HeartBeatGrpState;
import com.zed3.utils.HeartBeatPacket;
import com.zed3.utils.HeartBeatParser;
import com.zed3.utils.IHeartBeatListener;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Systems;
import com.zed3.utils.Tools;
import com.zed3.video.VideoManagerService;

import org.zoolu.net.IpAddress;
import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.ConnectionField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sdp.TimeField;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.InviteCallType;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.dialog.InviteDialog;
import org.zoolu.sip.header.ContentLengthHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;
import org.zoolu.tools.Log;
import org.zoolu.tools.Parser;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class UserAgent extends CallListenerAdapter implements TransactionClientListener, SipProviderListener, Runnable, IHeartBeatListener {
	private static final int BLUETOOTH_MODE = 2;
	public static final int CALL_LINES = 4;
	private static final int MUTE_MODE = 3;
	private static final int PTT_CLICK_INTERVAL_LIMIT = 500;
	public static final int PTT_IDLE = 5;
	private static final int PTT_LISTENING = 7;
	private static final int PTT_QUEUE = 8;
	public static final int PTT_TALKING = 6;
	private static final int PTT_UNREG = 9;
	private static final int SPEAK_MODE = 1;
	public static final int UA_STATE_HOLD = 4;
	public static final int UA_STATE_IDLE = 0;
	public static final int UA_STATE_INCALL = 3;
	public static final int UA_STATE_INCOMING_CALL = 1;
	public static final int UA_STATE_OUTGOING_CALL = 2;
	static byte[] buffer;
	public static String camera_PayLoadType;
	private static GPSPacket gpsPacket;
	public static boolean isCamerPttDialog;
	public static boolean isTempGrpCallMode;
	public static long timeOfpttAcceptTipSoundEnd;
	public static volatile boolean ua_ptt_mode;
	public final String ACTION_3GFlow_ALARM;
	public String Camera_AudioPort;
	public String Camera_URL;
	public String Camera_VideoPort;
	GroupChangeTipReceiver SetGrpRecv;
	AudioTrack at;
	protected ExtendedCall audioCall;
	int audioFormat;
	public JAudioLauncher audio_app;
	private boolean automaticAnswer;
	private Handler beatHandler;
	int call_state;
	protected ExtendedCall call_transfer;
	protected Vector<ExtendedCall> calls;
	int channel;
	private Handler cmdHandler;
	private Thread cmdProcThread;
	private PttGrp curTmpGrp;
	private TotalFlowThread flowThread;
	private GrpCallSetupType grpCallSetupHigh;
	private GrpCallSetupType grpCallSetupLow;
	private GrpCallSetupType grpCallSetupSame;
	private PttGrp.E_Grp_State grpStateBeforeQueue;
	private long intervalDown;
	private boolean isInitGroupData;
	private boolean isStartedGPS;
	private String lastIMContent;
	private String lastIMSeq;
	List<String> listsmsid;
	protected String local_session;
	int local_video_port;
	Log log;
	private ExtendedCall mAbortCall;
	private IntentFilter mGrpFilter;
	private String mLastGrpIDBeforeMessageGroupChange;
	private SipdroidSocket mSocket;
	private SipdroidSocket mVedioSocket;
	int pcmlen;
	private String preGroup;
	private String preGrpBeforeEmergencyCall;
	private String preGrpBeforeTmpGrpCall;
	private PttGrps pttGrps;
	private PttPRMode pttPressReleaseMode;
	private boolean ptt_key_down;
	String realm;
	String remote_media_address;
	int remote_video_port;
	int sampleRateInHz;
	protected SipProvider sip_provider;
	private int statusBeforeQueue;
	private String tag;
	private int ua_ptt_state;
	public UserAgentProfile user_profile;
	protected ExtendedCall videoCall;

	static {
		UserAgent.camera_PayLoadType = "";
		UserAgent.ua_ptt_mode = true;
		UserAgent.isCamerPttDialog = false;
		UserAgent.gpsPacket = null;
		UserAgent.isTempGrpCallMode = false;
		UserAgent.buffer = null;
	}

	public UserAgent(final SipProvider sip_provider, final UserAgentProfile user_profile) {
		this.SetGrpRecv = new GroupChangeTipReceiver();
		this.ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
		this.listsmsid = new ArrayList<String>();
		this.pttPressReleaseMode = PttPRMode.Idle;
		this.videoCall = null;
		this.audioCall = null;
		this.calls = null;
		this.mSocket = null;
		this.mVedioSocket = null;
		this.audio_app = null;
		this.local_session = null;
		this.call_state = 0;
		this.isInitGroupData = false;
		this.Camera_URL = "";
		this.Camera_AudioPort = "";
		this.Camera_VideoPort = "";
		this.ptt_key_down = false;
		this.ua_ptt_state = 9;
		this.preGrpBeforeEmergencyCall = "";
		this.preGrpBeforeTmpGrpCall = "";
		this.preGroup = "";
		this.pttGrps = new PttGrps();
		this.cmdHandler = null;
		this.beatHandler = null;
		this.grpCallSetupHigh = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		this.grpCallSetupSame = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		this.grpCallSetupLow = GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		this.cmdProcThread = null;
		this.lastIMContent = "";
		this.lastIMSeq = "";
		this.isStartedGPS = false;
		this.flowThread = null;
		this.mGrpFilter = null;
		this.tag = "UserAgent";
		this.curTmpGrp = null;
		this.intervalDown = 0L;
		this.at = null;
		this.pcmlen = 0;
		this.sampleRateInHz = 0;
		this.channel = 0;
		this.audioFormat = 0;
		this.statusBeforeQueue = 5;
		this.grpStateBeforeQueue = PttGrp.E_Grp_State.GRP_STATE_IDLE;
		this.automaticAnswer = false;
		this.logfunc("UserAgent");
		this.sip_provider = sip_provider;
		this.log = sip_provider.getLog();
		this.user_profile = user_profile;
		this.realm = user_profile.realm;
		user_profile.initContactAddress(sip_provider);
		LogUtil.makeLog(this.tag, "new UserAgent()");
	}

	private void GPSCloseLock(final boolean b) {
		synchronized (this) {
			if (UserAgent.gpsPacket != null) {
				UserAgent.gpsPacket.loginFlag = false;
				if (b) {
					UserAgent.gpsPacket.ExitGPS(true);
					UserAgent.gpsPacket = null;
				} else {
					UserAgent.gpsPacket.ExitGPS(false);
				}
			}
		}
	}

	private void Get3GNetWorkType() {
		String s2;
		final String s = s2 = this.user_profile.username;
		if (s.indexOf("@") < 0) {
			String string = s;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s;
			}
			s2 = String.valueOf(string) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s2), new NameAddress(this.user_profile.from_url), null);
		request.setHeader(new Header("Ptt-Extension", "3ghandset getdatatotal"));
		new TransactionClient(this.sip_provider, request, this).request();
	}

	private boolean GetPttMode() {
		return UserAgent.ua_ptt_mode;
	}

	private int GetPttStatus() {
		return this.ua_ptt_state;
	}

	private void JoinTmpGrpCallinner(final PttGrp pttGrp, final String s) {
		if (pttGrp != null) {
			final String username = this.user_profile.username;
			if (username.indexOf("@") < 0) {
				String string = username;
				if (this.user_profile.realm.equals("")) {
					string = "&" + username;
				}
				new StringBuilder(String.valueOf(string)).append("@").append(this.realm).toString();
			}
			final ExtendedCall extendedCall = (ExtendedCall) pttGrp.oVoid;
			if (extendedCall != null && extendedCall.getCallTypeEx() == 3) {
				final Message request = BaseMessageFactory.createRequest(extendedCall.getDialog(), "INFO", null);
				final Vector<Header> vector = new Vector<Header>();
				vector.add(new Header("Ptt-Extension", "3ghandset tmpgrpadd " + pttGrp.grpName));
				vector.add(new Header("Ptt-Member", s));
				request.addHeaders(vector, false);
				new TransactionClient(this.sip_provider, request, this).request();
			}
		}
	}

	private void OnPttKey2(final boolean b) {
		this.logfunc("OnPttKey2");
		MyLog.d("videoTrace", "UserAgent#onPttKey2() enter keydown = " + b + " , ptt state = " + this.ua_ptt_state);
		if (b) {
			if (5 == this.ua_ptt_state) {
				final PttGrp getCurGrp = this.GetCurGrp();
				if (getCurGrp.oVoid != null) {
					if (getCurGrp.isCreateSession && ((Call) getCurGrp.oVoid).isOnCall()) {
						this.PttGroupRequestSpeak();
						this.onPttKeyDown();
					}
				} else if (!UserAgent.isTempGrpCallMode) {
					this.onPttKeyDown();
					this.pttGroupCall();
				}
			} else if (7 == this.ua_ptt_state) {
				this.onPttKeyDown();
				this.PttGroupRequestSpeak();
			}
		} else {
			if (6 == this.ua_ptt_state) {
				this.stopMediaApplication();
				MyLog.i("hst", "onpttkey2");
				return;
			}
			if (8 == this.ua_ptt_state) {
				this.PttGroupReleaseQueue();
			}
		}
	}

	private void PttGroupReleaseQueue() {
		this.logfunc("PttGroupReleaseQueue");
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp == null) {
			return;
		}
		String s2;
		final String s = s2 = getCurGrp.grpID;
		if (s.indexOf("@") < 0) {
			String string = s;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s;
			}
			s2 = String.valueOf(string) + "@" + this.realm;
		}
		final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
		Message message;
		if (getCurGrp.isCreateSession && extendedCall != null) {
			message = BaseMessageFactory.createRequest(extendedCall.getDialog(), "INFO", null);
		} else {
			message = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s2), new NameAddress(this.user_profile.from_url), null);
		}
		message.setHeader(new Header("Ptt-Extension", "3ghandset cancelwaiting"));
		new TransactionClient(this.sip_provider, message, this).request();
		this.process_TYPE_REQUEST_CANCEL_WAITING_OK_PHONE_or_TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE_inner();
	}

	private void PttGroupReleaseSpeak() {
		this.logfunc("PttGroupReleaseSpeak");
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp == null) {
			return;
		}
		String s2;
		final String s = s2 = getCurGrp.grpID;
		if (s.indexOf("@") < 0) {
			String string = s;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s;
			}
			s2 = String.valueOf(string) + "@" + this.realm;
		}
		final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
		Message message;
		if (getCurGrp.isCreateSession && extendedCall != null) {
			message = BaseMessageFactory.createRequest(extendedCall.getDialog(), "INFO", null);
		} else {
			message = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s2), new NameAddress(this.user_profile.from_url), null);
		}
		message.setHeader(new Header("Ptt-Extension", "3ghandset cancel"));
		new TransactionClient(this.sip_provider, message, this).request();
		this.process_TYPE_REQUEST_CANCEL_OK_PHONE_or_TYPE_SERVER_FORCECANCEL_PHONE_inner(false);
	}

	private void PttGroupRequestSpeak() {
		this.logfunc("PttGroupRequestSpeak");
		MyLog.d("pttreqeustTrace", "UserAgent#PttGroupRequestSpeak() enter");
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null && (UserAgent.isTempGrpCallMode || getCurGrp.level != 0)) {
			String s2;
			final String s = s2 = getCurGrp.grpID;
			if (s.indexOf("@") < 0) {
				String string = s;
				if (this.user_profile.realm.equals("")) {
					string = "&" + s;
				}
				s2 = String.valueOf(string) + "@" + this.realm;
			}
			final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
			Message message;
			if (getCurGrp.isCreateSession && extendedCall != null) {
				message = BaseMessageFactory.createRequest(extendedCall.getDialog(), "INFO", null);
			} else {
				message = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s2), new NameAddress(this.user_profile.from_url), null);
			}
			message.setHeader(new Header("Ptt-Extension", "3ghandset request"));
			new TransactionClient(this.sip_provider, message, this).request();
		}
	}

	private void SetNullGrp() {
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null && getCurGrp.isCreateSession) {
			final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
			if (extendedCall != null && (6 == this.GetPttStatus() || 7 == this.GetPttStatus())) {
				this.pttGroupRelease(false, extendedCall);
			}
			getCurGrp.speakerN = "";
			getCurGrp.speaker = "";
			getCurGrp.isCreateSession = false;
			getCurGrp.oVoid = null;
		}
		this.pttGrps.SetCurGrp(null);
		final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
		intent.putExtra("0", "");
		intent.putExtra("1", "");
		Receiver.mContext.sendBroadcast(intent);
		Receiver.onText(5, SipUAApp.mContext.getResources().getString(R.string.regok), R.drawable.icon64, 0L);
	}

	private void SetPttMode(final boolean ua_ptt_mode) {
		UserAgent.ua_ptt_mode = ua_ptt_mode;
	}

	private void SetPttStatus(final int ua_ptt_state) {
		if (ua_ptt_state != this.ua_ptt_state) {
			this.ua_ptt_state = ua_ptt_state;
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null && 9 != this.GetPttStatus()) {
				final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
				intent.putExtra("0", getCurGrp.grpID);
				intent.putExtra("1", String.valueOf(getCurGrp.speakerN) + " " + getCurGrp.speaker);
				Receiver.mContext.sendBroadcast(intent);
			}
		}
	}

	private void StartHeartbeat(final int n) {
	}

	private void abortCallCompleted(final Call call) {
		this.logcall("abortCallCompleted", (ExtendedCall) call);
		final boolean audioCall = this.isAudioCall(call);
		MyLog.d("videoTrace", "UserAgent#abortCallCompleted() isAudioCall = " + audioCall);
		CallManager.getManager().removeCall(call);
		if (audioCall) {
			this.audioCall = null;
		} else {
			this.videoCall = null;
		}
		if (VideoManagerService.getDefault().existRemoteVideoControl() || !VideoManagerService.getDefault().isEmptyVideoAction()) {
			VideoManagerService.getDefault().clearRemoteVideoParameter();
		}
	}

	private boolean acceptInner() {
		this.logfunc("acceptInner()");
		final VideoManagerService default1 = VideoManagerService.getDefault();
		boolean b = false;
		Label_0038:
		{
			if (default1.existRemoteVideoControl()) {
				if (this.videoCall != null) {
					break Label_0038;
				}
			} else {
				b = true;
				if (this.audioCall != null) {
					break Label_0038;
				}
			}
			return false;
		}
		MyLog.d("videoTrace", "UserAgent#acceptInner() enter audio app = " + this.audio_app);
		if (default1.isEmptyVideoAction() || default1.isCurrentVideoCall() || b) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null && getCurGrp.isCreateSession) {
				final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
				if (extendedCall != null) {
					this.pttGroupRelease(false, extendedCall);
					getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
					getCurGrp.speaker = "";
					getCurGrp.speakerN = "";
					final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
					intent.putExtra("0", getCurGrp.grpID);
					intent.putExtra("1", getCurGrp.speaker);
					Receiver.mContext.sendBroadcast(intent);
				}
				getCurGrp.speakerN = "";
				getCurGrp.speaker = "";
				getCurGrp.isCreateSession = false;
				getCurGrp.oVoid = null;
			}
			MyLog.d("pttTrace", "UsrAgent#acceptInner() enter SetPttMode(@param false)");
			this.SetPttMode(false);
		} else {
			this.SetPttMode(true);
		}
		this.printLog("ACCEPT");
		ExtendedCall extendedCall2;
		if (default1.existRemoteVideoControl()) {
			extendedCall2 = this.videoCall;
		} else {
			extendedCall2 = this.audioCall;
		}
		this.setMediaProt(extendedCall2, false, true);
		extendedCall2.setCallBeginTime(System.currentTimeMillis());
		CallManager.getManager().setCallState(CallManager.CallState.INCALL, extendedCall2);
		this.changeStatus(3);
		extendedCall2.accept(extendedCall2.getLocalSessionDescriptor());
		this.startMediaApplication(extendedCall2, 0);
		return true;
	}

	private void addCall() {
		synchronized (this) {
			this.logfunc("addCall()");
			final ExtendedCall extendedCall = new ExtendedCall(this.sip_provider, this.user_profile.from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this);
			extendedCall.listen();
			this.calls.add(extendedCall);
		}
	}

	private void addCall(final ExtendedCall extendedCall) {
		synchronized (this) {
			this.logcall("addCall(ExtendedCall)", extendedCall);
			this.calls.add(extendedCall);
		}
	}

	private void addMediaDescriptor(final String s, final int n, final int n2, final String s2, final int n3) {
		final SessionDescriptor sessionDescriptor = new SessionDescriptor(this.local_session);
		String s3 = String.valueOf(n2);
		if (s2 != null) {
			s3 = String.valueOf(s3) + " " + s2 + "/" + n3;
		}
		final Vector<AttributeField> vector = new Vector<AttributeField>();
		vector.add(new AttributeField("rtpmap", s3));
		if (MemoryMg.getInstance().GvsTransSize.equals("3")) {
			vector.add(new AttributeField("fmtp", String.valueOf(String.valueOf(n2)) + " " + "profile-level-id=42e00a;qcif=1;fps=10"));
		} else if (MemoryMg.getInstance().GvsTransSize.equals("4")) {
			vector.add(new AttributeField("fmtp", String.valueOf(String.valueOf(n2)) + " " + "profile-level-id=42e016;4cif=1;fps=10"));
		} else if (MemoryMg.getInstance().GvsTransSize.equals("5")) {
			vector.add(new AttributeField("fmtp", String.valueOf(String.valueOf(n2)) + " " + "profile-level-id=42e00b;cif=1;fps=10"));
		} else {
			vector.add(new AttributeField("fmtp", String.valueOf(String.valueOf(n2)) + " " + "profile-level-id=42e01f;720p=1;fps=6"));
		}
		final VideoManagerService default1 = VideoManagerService.getDefault();
		if (default1.isCurrentVideoUpload()) {
			vector.add(new AttributeField("sendonly"));
		}
		if (default1.isCurrentVideoMonitor()) {
			vector.add(new AttributeField("recvonly"));
		}
		sessionDescriptor.addMedia(new MediaField(s, n, 0, "RTP/AVP", String.valueOf(n2)), vector);
		this.local_session = sessionDescriptor.toString();
	}

	private void addMediaDescriptor(final String s, final int n, final Codecs.Map map) {
		final SessionDescriptor sessionDescriptor = new SessionDescriptor(this.local_session);
		final Vector<String> vector = new Vector<String>();
		final Vector<AttributeField> vector2 = new Vector<AttributeField>();
		if (map == null) {
			final int[] codecs = Codecs.getCodecs();
			for (int length = codecs.length, i = 0; i < length; ++i) {
				final int n2 = codecs[i];
				final Codec value = Codecs.get(n2);
				if (n2 == 0) {
					value.init();
				}
				vector.add(String.valueOf(n2));
				if (value.number() == 9) {
					vector2.add(new AttributeField("rtpmap", String.format("%d %s/%d", n2, value.userName(), 8000)));
				} else {
					vector2.add(new AttributeField("rtpmap", String.format("%d %s/%d", n2, value.userName(), value.samp_rate())));
					if (value.number() == 114) {
						final AmrNB amrNB = (AmrNB) value;
						for (final AttributeField attributeField : vector2) {
							if (attributeField.getValue().contains("mode-set=")) {
								vector2.remove(attributeField);
								break;
							}
						}
						vector2.add(new AttributeField("fmtp", String.format("%d mode-set=%d", amrNB.number(), amrNB.getMode())));
					}
				}
			}
		} else {
			map.codec.init();
			vector.add(String.valueOf(map.number));
			if (map.codec.number() == 9) {
				vector2.add(new AttributeField("rtpmap", String.format("%d %s/%d", map.number, map.codec.userName(), 8000)));
			} else {
				vector2.add(new AttributeField("rtpmap", String.format("%d %s/%d", map.number, map.codec.userName(), map.codec.samp_rate())));
				if (map.codec.number() == 114) {
					final AmrNB amrNB2 = (AmrNB) map.codec;
					for (final AttributeField attributeField2 : vector2) {
						if (attributeField2.getValue().contains("mode-set=")) {
							vector2.remove(attributeField2);
							break;
						}
					}
					vector2.add(new AttributeField("fmtp", String.format("%d mode-set=%d", amrNB2.number(), amrNB2.getMode())));
				}
			}
		}
		if (this.user_profile.dtmf_avp != 0) {
			vector.add(String.valueOf(this.user_profile.dtmf_avp));
			vector2.add(new AttributeField("rtpmap", String.format("%d telephone-event/%d", this.user_profile.dtmf_avp, this.user_profile.audio_sample_rate)));
			vector2.add(new AttributeField("fmtp", String.format("%d 0-15", this.user_profile.dtmf_avp)));
		}
		sessionDescriptor.addMedia(new MediaField(s, n, 0, "RTP/AVP", vector), vector2);
		final MediaDescriptor mediaDescriptor = sessionDescriptor.getMediaDescriptor("audio");
		if (mediaDescriptor != null) {
			for (final AttributeField attributeField3 : vector2) {
				if (attributeField3.getValue().contains("ptime")) {
					vector2.remove(attributeField3);
					break;
				}
			}
			mediaDescriptor.addAttribute(new AttributeField("ptime", String.valueOf(SettingsInfo.ptime)));
		}
		this.local_session = sessionDescriptor.toString();
	}

	private void answerGroupCallinner(final PttGrp pttGrp) {
		if (pttGrp == null || pttGrp.oVoid == null || !((ExtendedCall) pttGrp.oVoid).isTmpCall() || UserAgent.isTempGrpCallMode) {
			if (!this.IsPttMode()) {
				if (pttGrp.level == 0) {
					final VideoManagerService default1 = VideoManagerService.getDefault();
					if (!CallManager.getManager().existCall(CallManager.CallState.INCOMING) && !default1.existVideoUploadOrMonitor()) {
						this.hangupinnerWithoutRejoin();
					}
				} else if (!UserAgent.isTempGrpCallMode) {
					final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.single_2_group");
					intent.putExtra("0", pttGrp.grpID);
					Receiver.mContext.sendBroadcast(intent);
					return;
				}
			}
			final VideoManagerService default2 = VideoManagerService.getDefault();
			if (!CallManager.getManager().existCall(CallManager.CallState.INCOMING) && !default2.existVideoUploadOrMonitor()) {
				this.hangupinnerWithoutRejoin();
			}
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null && !getCurGrp.grpID.equalsIgnoreCase(pttGrp.grpID)) {
				this.pttGroupRelease(false, null);
				getCurGrp.speakerN = "";
				getCurGrp.speaker = "";
				getCurGrp.isCreateSession = false;
				getCurGrp.oVoid = null;
			}
			MyLog.i(this.tag, "SetPttStatus  PTT_LISTENING answerGroupCall.");
			this.SetPttStatus(7);
			this.SetPttMode(true);
			if (getCurGrp == null) {
				this.setCurGrpinner(pttGrp, false);
				if (UserAgent.isTempGrpCallMode) {
					TempGroupCallUtil.mCall = (ExtendedCall) pttGrp.oVoid;
				}
			}
			if (getCurGrp != null && !getCurGrp.grpID.equalsIgnoreCase(pttGrp.grpID)) {
				if (pttGrp.level == 0) {
					this.preGrpBeforeEmergencyCall = getCurGrp.grpID;
				}
				if (pttGrp.level == -1) {
					android.util.Log.i("zdx", "-------answerGroupCallInner------TmpGrpCall---------");
					this.preGrpBeforeTmpGrpCall = getCurGrp.grpID;
					new Exception("--TmpGrpCall valued  test----").printStackTrace();
				}
				if (this.automaticAnswer && "".equals(this.preGroup)) {
					this.preGroup = getCurGrp.grpID;
				}
				this.setCurGrpinner(pttGrp, false);
				if (UserAgent.isTempGrpCallMode) {
					TempGroupCallUtil.mCall = (ExtendedCall) pttGrp.oVoid;
				}
				Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua_currentgroup_changed"));
			}
			if (pttGrp.oVoid != null) {
				this.groupAccept((ExtendedCall) pttGrp.oVoid);
				pttGrp.isCreateSession = true;
			}
		}
	}

	private boolean antaCall3(String s, final String s2, final boolean b, final boolean b2, final ExtendedCall extendedCall) {
		this.logfunc("antaCall3");
		if (Receiver.call_state != 0) {
			this.printLog("Call attempted in state" + this.getSessionDescriptor() + " : Failing Request", 1);
			return false;
		}
		String from_url;
		if (!b) {
			from_url = this.user_profile.from_url;
		} else {
			from_url = "sip:anonymous@anonymous.com";
		}
		this.SetPttMode(false);
		this.createOffer(false);
		if (this.audioCall == null) {
			this.audioCall = this.getIdlePttLine();
		}
		if (this.audioCall != null) {
			this.audioCall.hangup();
			this.removeCall(this.audioCall);
		}
		this.addCall(this.audioCall = new ExtendedCall(this.sip_provider, from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this));
		this.audioCall.setCallState(CallManager.CallState.OUTGOING);
		this.audioCall.setCallType(CallManager.CallType.AUDIO);
		CallManager.getManager().setCallerNumber(s, this.audioCall).manageCall(this.audioCall);
		this.changeStatus(2, s);
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		s = null;
		if (this.user_profile.mmtel) {
			s = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}
		final String string3 = this.sip_provider.completeNameAddress(string).toString();
		if (this.user_profile.no_offer) {
			this.audioCall.antaCall4(string3, s2, null, null, null, null, b2);
		} else {
			this.audioCall.antaCall4(string3, s2, null, null, this.local_session, s, b2);
		}
		return true;
	}

	private void bluetoothMediaApplicationinner() {
		if (this.audio_app != null) {
			this.audio_app.bluetoothMedia();
		}
	}

	private void busyNotifier(final Call call) {
		this.logcall("busyNotifier", (ExtendedCall) call);
		call.busy();
		new ExtendedCall(this.sip_provider, this.user_profile.from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this).listen();
	}

	private boolean call(final String s, final boolean b, final boolean b2, final ExtendedCall extendedCall, final boolean b3, final String s2, final String s3, final boolean b4) {
		return this.call(s, b, b2, extendedCall, b3, s2, s3, b4, null);
	}

	private boolean call(String s, final boolean b, final boolean isGroupCall, ExtendedCall extendedCall, final boolean b2, final String s2, final String s3, final boolean b3, final String s4) {
		this.logcall("call(String, boolean,boolean, ExtendedCall, boolean, String, String , boolean)", extendedCall);
		final VideoManagerService default1 = VideoManagerService.getDefault();
		if (Receiver.call_state != 0) {
			final boolean pttGroupCall = this.isPttGroupCall(extendedCall);
			final boolean existVideoUploadOrMonitor = VideoManagerService.getDefault().existVideoUploadOrMonitor();
			MyLog.d("videoTrace", "UserAgent#call(..)  isPttGroupCall = " + pttGroupCall + " , existVideoUploadOrMonitor = " + existVideoUploadOrMonitor);
			if (!pttGroupCall || (!existVideoUploadOrMonitor && !default1.isCurrentVideoTRANSCRIBE())) {
				this.printLog("Call attempted in state" + this.getSessionDescriptor() + " : Failing Request", 1);
				final PttGrp getCurGrp = this.GetCurGrp();
				if (getCurGrp != null && getCurGrp.oVoid != null) {
					getCurGrp.oVoid = null;
					getCurGrp.isCreateSession = false;
				}
				return false;
			}
		}
		MemoryMg.getInstance().isSendOnly = false;
		String from_url;
		if (!b) {
			from_url = this.user_profile.from_url;
		} else {
			from_url = "sip:anonymous@anonymous.com";
		}
		final VideoManagerService default2 = VideoManagerService.getDefault();
		if (default2.isCurrentVideoMonitor() || default2.isCurrentVideoUpload() || default2.isCurrentVideoTRANSCRIBE()) {
			this.SetPttMode(true);
		} else {
			this.SetPttMode(isGroupCall);
		}
		this.setMediaProt(null, isGroupCall, false);
		if (isGroupCall) {
			this.createOfferForGroupCall();
		} else {
			this.createOffer(b3);
		}
		if (!isGroupCall) {
			if (b3) {
				(this.videoCall = new ExtendedCall(this.sip_provider, from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this)).setCallType(CallManager.CallType.VIDEO);
				this.videoCall.isGroupCall = isGroupCall;
				this.addCall(this.videoCall);
				this.videoCall.setCallState(CallManager.CallState.OUTGOING);
				CallManager.getManager().setCallerNumber(s, this.videoCall).manageCall(this.videoCall);
			} else {
				(this.audioCall = new ExtendedCall(this.sip_provider, from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this)).setCallType(CallManager.CallType.AUDIO);
				this.audioCall.isGroupCall = isGroupCall;
				this.addCall(this.audioCall);
				this.audioCall.setCallState(CallManager.CallState.OUTGOING);
				CallManager.getManager().setCallerNumber(s, this.audioCall).manageCall(this.audioCall);
			}
			this.changeStatus(2, s);
		} else {
			final ExtendedCall oVoid = new ExtendedCall(this.sip_provider, from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this);
			oVoid.isGroupCall = isGroupCall;
			this.addCall(oVoid);
			extendedCall = oVoid;
			if (this.GetCurGrp() != null) {
				this.GetCurGrp().oVoid = oVoid;
				extendedCall = oVoid;
			}
		}
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		s = null;
		if (this.user_profile.mmtel) {
			s = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}
		final String string3 = this.sip_provider.completeNameAddress(string).toString();
		if (this.user_profile.no_offer) {
			if (!isGroupCall) {
				if (b3) {
					this.videoCall.call(string3, null, null, null, null);
				} else {
					this.audioCall.call(string3, null, null, null, null, s4);
				}
			} else if (s2 == null) {
				extendedCall.groupcall(string3, null, null, null, null, b2);
			} else {
				extendedCall.tempGroupcall(string3, null, null, null, null, b2, s2, s3);
			}
		} else if (!isGroupCall) {
			if (b3) {
				this.videoCall.call(string3, null, null, this.local_session, s);
			} else {
				this.audioCall.call(string3, null, null, this.local_session, s, s4);
			}
		} else if (s2 == null) {
			extendedCall.groupcall(string3, null, null, this.local_session, s, b2);
		} else {
			extendedCall.tempGroupcall(string3, null, null, this.local_session, s, b2, s2, s3);
		}
		this.logcall("call(String, boolean,boolean, ExtendedCall, boolean, String, String , boolean)", extendedCall);
		return true;
	}

	private boolean callinner(final String s, final boolean b, final boolean b2, final String s2) {
		this.logfunc("callinner(String,boolean,boolean)");
		if (this.GetPttStatus() == 9) {
			return false;
		}
		if (!TextUtils.isEmpty((CharSequence) s2) && s2.contains("Emergency")) {
			this.hangupinner();
		}
		if (!this.isIdleOfPttLines() && !VideoManagerService.getDefault().existVideoUploadOrMonitor()) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null) {
				if (!UserAgent.isTempGrpCallMode) {
					this.grouphangupinner(getCurGrp);
				} else {
					this.hangupTmpGrpCallinner(false);
					Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
				}
				MyLog.i(this.tag, "SetPttStatus  PTT_IDLE call(String target_url, boolean send_anonymous).");
				this.SetPttStatus(5);
				getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
				getCurGrp.speaker = "";
				getCurGrp.speakerN = "";
				final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
				intent.putExtra("0", getCurGrp.grpID);
				intent.putExtra("1", getCurGrp.speaker);
				Receiver.mContext.sendBroadcast(intent);
			}
		}
		return this.call(s, b, false, null, false, null, null, b2, s2);
	}

	private void createAnswer(final SessionDescriptor sessionDescriptor, final boolean b, final Call call, final boolean b2) {
		this.logcall("createAnswer", (ExtendedCall) call);
		this.initSessionDescriptor(Codecs.getCodec(sessionDescriptor), b2);
		this.sessionProduct(sessionDescriptor, b, call);
	}

	private void createOffer(final boolean b) {
		this.initSessionDescriptor(null, b);
	}

	private void createOfferForGroupCall() {
		this.initSessionDescriptorForGroupCall(null);
	}

	private void customGroupParser(final String s) {
		LogUtil.makeLog(this.tag, "customGroupParse() " + s);
		this.pttGrps.parseCustomGroupInfo(s);
	}

	private void dispatchCallStatus(final CallStatusPara callStatusPara) {
		if (callStatusPara == null) {
			this.logfunc("processCallMessage ,callStatus = null");
		} else {
			// TODO
		}
	}

	private void dispatchPttGroupState(final PttGrp.E_Grp_State state) {
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp == null || getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_LISTENING) {
			return;
		}
		getCurGrp.state = state;
		final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
		intent.putExtra("0", getCurGrp.grpID);
		intent.putExtra("1", "");
		Receiver.mContext.sendBroadcast(intent);
	}

	private void extendedSipProcess(final ExtendedSipCallbackPara extendedSipCallbackPara) {
		if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (this.GetPttStatus() != 6) {
				if (!this.isPttKeyDown()) {
					this.startMediaApplication((ExtendedCall) getCurGrp.oVoid, -1);
					this.PttGroupReleaseSpeak();
					MyLog.i("hst", "extendsSipProcess");
				} else if (6 != this.GetPttStatus()) {
					final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
					this.SetPttStatus(6);
					MyLog.i(this.tag, "SetPttStatus  PTT_TALKING TYPE_REQUEST_ACCEPT_PHONE.");
					this.startMediaApplication(extendedCall, 1);
				}
			}
		} else if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_REJECT_PHONE) {
			final PttGrp getCurGrp2 = this.GetCurGrp();
			if (getCurGrp2 != null && !getCurGrp2.isCreateSession) {
				getCurGrp2.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
				getCurGrp2.speaker = "";
				getCurGrp2.speakerN = "";
				final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
				intent.putExtra("0", getCurGrp2.grpID);
				intent.putExtra("1", getCurGrp2.speaker);
				Receiver.mContext.sendBroadcast(intent);
			}
		} else if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_LINE || extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_PHONE) {
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_WAITING_LINE && this.GetCurGrp().oVoid != extendedSipCallbackPara.para2) {
				this.pttGroupRelease(false, (ExtendedCall) extendedSipCallbackPara.para2);
				return;
			}
			if (!this.isPttKeyDown()) {
				this.PttGroupReleaseQueue();
				return;
			}
			if (8 != this.GetPttStatus()) {
				this.statusBeforeQueue = this.GetPttStatus();
				final PttGrp getCurGrp3 = this.GetCurGrp();
				this.grpStateBeforeQueue = getCurGrp3.state;
				getCurGrp3.state = PttGrp.E_Grp_State.GRP_STATE_QUEUE;
				final Intent intent2 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
				intent2.putExtra("0", getCurGrp3.grpID);
				new StringBuilder(String.valueOf(getCurGrp3.speakerN)).append(" ").append(getCurGrp3.speaker).toString();
				Receiver.mContext.sendBroadcast(intent2);
				MyLog.i(this.tag, "SetPttStatus  PTT_QUEUE TYPE_REQUEST_WAITING_LINE.");
				this.SetPttStatus(8);
			}
		} else {
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_WAITING_OK_PHONE || extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE) {
				this.process_TYPE_REQUEST_CANCEL_WAITING_OK_PHONE_or_TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE_inner();
				return;
			}
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_SERVER_FORCECANCEL_PHONE) {
				MyLog.e("huangfujian", "TYPE_REQUEST_CANCEL_OK_PHONE");
				this.process_TYPE_REQUEST_CANCEL_OK_PHONE_or_TYPE_SERVER_FORCECANCEL_PHONE_inner(true);
				return;
			}
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_RECEIVE_TEXT_MESSAGE_PHONE) {
				final TextMessage textMessage = (TextMessage) extendedSipCallbackPara.para2;
				final Intent intent3 = new Intent("com.zed3.sipua.ui_receive_text_message");
				intent3.putExtra("2", textMessage.seq);
				final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(Receiver.mContext);
				final ContentValues contentValues = new ContentValues();
				contentValues.put("body", textMessage.content);
				contentValues.put("mark", 0);
				contentValues.put("address", textMessage.from);
				contentValues.put("status", 0);
				contentValues.put("sip_name", textMessage.sipName);
				contentValues.put("type", "sms");
				if (textMessage.from.equals(textMessage.to)) {
					contentValues.put("status", 1);
				}
				contentValues.put("date", CommonUtil.getCurrentTime());
				final int nextInt = new Random().nextInt(9999999);
				MyLog.i("guojunfeng-random-E_id", new StringBuilder(String.valueOf(nextInt)).toString());
				contentValues.put("E_id", nextInt);
				final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
				final String fetchLocalServer = autoConfigManager.fetchLocalServer();
				final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
				contentValues.put("server_ip", fetchLocalServer);
				contentValues.put("local_number", fetchLocalUserName);
				smsMmsDatabase.insert("message_talk", contentValues);
				MyLog.i("message-->", textMessage.content);
				Receiver.mContext.sendBroadcast(intent3);
				this.pttTextMessageTipSound();
				return;
			}
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE) {
				final Intent intent4 = new Intent("com.zed3.sipua.ui_send_text_message_fail");
				intent4.putExtra("0", extendedSipCallbackPara.para1);
				Receiver.mContext.sendBroadcast(intent4);
				return;
			}
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE) {
				final Intent intent5 = new Intent("com.zed3.sipua.ui_send_text_message_succeed");
				intent5.putExtra("0", extendedSipCallbackPara.para1);
				Receiver.mContext.sendBroadcast(intent5);
				return;
			}
			if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_PTT_STATUS_PHONE) {
				final PttGrp getCurGrp4 = this.GetCurGrp();
				if (getCurGrp4 != null) {
					getCurGrp4.lastRcvTime = SystemClock.currentThreadTimeMillis();
					final String string = extendedSipCallbackPara.para1.toString();
					if (string.length() == 0) {
						if (this.GetCurGrp().state == PttGrp.E_Grp_State.GRP_STATE_QUEUE) {
							getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_QUEUE;
						} else {
							getCurGrp4.speaker = "";
							getCurGrp4.speakerN = "";
							getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_IDLE;
						}
					} else {
						final String[] split = string.split(" ");
						getCurGrp4.speakerN = split[0];
						if (split.length > 1) {
							getCurGrp4.speaker = split[1];
						}
						if (!getCurGrp4.speakerN.equalsIgnoreCase(this.user_profile.username)) {
							if (getCurGrp4.state != PttGrp.E_Grp_State.GRP_STATE_QUEUE) {
								getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_LISTENING;
							} else {
								getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_QUEUE;
							}
						} else {
							getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_TALKING;
						}
					}
					if (!this.IsPttMode()) {
						getCurGrp4.state = PttGrp.E_Grp_State.GRP_STATE_IDLE;
					} else if (SipUAApp.isHeadsetConnected) {
						AudioModeUtils.setAudioStyle(0, false);
					} else {
						AudioModeUtils.setAudioStyle(0, true);
					}
					final Intent intent6 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
					intent6.putExtra("0", getCurGrp4.grpID);
					intent6.putExtra("1", String.valueOf(getCurGrp4.speakerN) + " " + getCurGrp4.speaker);
					Receiver.mContext.sendBroadcast(intent6);
				}
			} else if (extendedSipCallbackPara.type != ExtendedSipCallbackType.TYPE_REQUEST_LISTEN_LINE && extendedSipCallbackPara.type != ExtendedSipCallbackType.TYPE_REQUEST_REJECT_LINE && extendedSipCallbackPara.type != ExtendedSipCallbackType.TYPE_LOCAL_HANGUP_LINE && extendedSipCallbackPara.type != ExtendedSipCallbackType.TYPE_PEER_HANGUP_LINE && extendedSipCallbackPara.type != ExtendedSipCallbackType.TYPE_REQUEST_GETSTATUS_PHONE) {
				if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REGISTER_SUCCESS) {
					if (!this.isInitGroupData) {
						GroupListUtil.getData4GroupList();
						this.isInitGroupData = true;
					}
					if (this.user_profile.gps && !this.isStartedGPS) {
						while (true) {
							this.isStartedGPS = true;
							while (true) {
								Label_1540:
								{
									try {
										Tools.onRegisterSuccess();
										if (DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS) {
											this.Get3GTotalFromServer();
											this.Get3GNetWorkType();
										}
										if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("logOnOffKey", false)) {
											CrashHandler.getInstance().init(Receiver.mContext, true);
										}
										android.util.Log.e("configTrace", "open gps model = " + Tools.getCurrentGpsMode());
										if (Tools.getCurrentGpsMode() != 3) {
											if (!LocalConfigSettings.SdcardConfig.pool().mLoadGps) {
												break Label_1540;
											}
											android.util.Log.e("configTrace", "open gps model");
											this.GPSOpenLock();
										}
										MemoryMg.getInstance().User_3GDBLocalTotal = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("User_3GDBLocalTotal", "0"));
										MemoryMg.getInstance().User_3GDBLocalTotalPTT = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("User_3GDBLocalTotalPTT", "0"));
										MemoryMg.getInstance().User_3GDBLocalTotalVideo = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("User_3GDBLocalTotalVideo", "0"));
										MemoryMg.getInstance().User_3GDBLocalTime = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("User_3GDBLocalTime", this.GetCurrentMouth(false));
										MemoryMg.getInstance().User_3GFlowOut = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("3gflowoutval", "0"));
										MemoryMg.getInstance().isProgressBarTip = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("flowtooltip", true);
										MemoryMg.getInstance().TerminalNum = Settings.getUserName();
										(this.mGrpFilter = new IntentFilter()).addAction("com.zed3.sipua.ui_groupcall.group_2_group");
										this.mGrpFilter.addAction("com.zed3.sipua.ui_groupcall.single_2_group");
										SipUAApp.mContext.registerReceiver((BroadcastReceiver) this.SetGrpRecv, this.mGrpFilter);
										return;
									} catch (Exception ex) {
										MyLog.e(this.tag, new StringBuilder().append(ex.toString()).toString());
										ex.printStackTrace();
										return;
									}
								}
								android.util.Log.e("configTrace", "don't open gps model");
								continue;
							}
						}
					}
				} else if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_REQUEST_403) {
					final String para1 = extendedSipCallbackPara.para1;
					final String currentGroupCallId = this.getCurrentGroupCallId();
					int n2;
					final int n = n2 = 0;
					if (!TextUtils.isEmpty((CharSequence) para1)) {
						n2 = n;
						if (para1.equals(currentGroupCallId)) {
							final PttGrp getCurGrp5 = this.GetCurGrp();
							boolean b;
							if (UserAgent.isTempGrpCallMode && TempGroupCallUtil.mCall != null && getCurGrp5 != null && ((ExtendedCall) getCurGrp5.oVoid).getCallTypeEx() == 3) {
								this.hangupTmpGrpCallinner(true);
								Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
								b = true;
							} else {
								this.pttGroupRelease(false, null);
								b = false;
							}
							n2 = (b ? 1 : 0);
							if (getCurGrp5 != null) {
								n2 = (b ? 1 : 0);
								if (getCurGrp5.isCreateSession) {
									n2 = (b ? 1 : 0);
									if (getCurGrp5.oVoid != null) {
										this.grouphangupinner(getCurGrp5);
										n2 = (b ? 1 : 0);
									}
								}
							}
						}
					}
					if (!UserAgent.isTempGrpCallMode && n2 == 0) {
						this.pttGroupCall();
					}
				} else if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_FLOWVIEWSCANNER_START) {
					if (!TextUtils.isEmpty((CharSequence) MemoryMg.getInstance().User_3GDBLocalTime) && !this.GetCurrentMouth(true).equals(MemoryMg.getInstance().User_3GDBLocalTime.substring(0, 7))) {
						MemoryMg.getInstance().User_3GDBLocalTotal = 0.0;
						MemoryMg.getInstance().User_3GDBLocalTotalPTT = 0.0;
						MemoryMg.getInstance().User_3GDBLocalTotalVideo = 0.0;
						MemoryMg.getInstance().User_3GDBLocalTime = this.GetCurrentMouth(false);
						this.NetFlowPreferenceEdit("0", "0", "0", this.GetCurrentMouth(false));
					}
					final String string2 = String.valueOf(MemoryMg.getInstance().User_3GLocalTime) + "|" + MemoryMg.getInstance().User_3GLocalTotal + "|" + MemoryMg.getInstance().User_3GLocalTotalPTT + "|" + MemoryMg.getInstance().User_3GLocalTotalVideo;
					if (!TextUtils.isEmpty((CharSequence) MemoryMg.getInstance().User_3GLocalTime)) {
						if (this.GetCurrentMouth(true).equals(MemoryMg.getInstance().User_3GLocalTime.substring(0, 7))) {
							if (MemoryMg.getInstance().User_3GLocalTotal > MemoryMg.getInstance().User_3GDBLocalTotal) {
								MemoryMg.getInstance().User_3GRelTotal = MemoryMg.getInstance().User_3GLocalTotal;
								MemoryMg.getInstance().User_3GRelTotalPTT = MemoryMg.getInstance().User_3GLocalTotalPTT;
								MemoryMg.getInstance().User_3GRelTotalVideo = MemoryMg.getInstance().User_3GLocalTotalVideo;
								this.NetFlowPreferenceEdit(new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GLocalTotal)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GLocalTotalPTT)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GLocalTotalVideo)).toString(), this.GetCurrentMouth(false));
							} else {
								MemoryMg.getInstance().User_3GRelTotal = MemoryMg.getInstance().User_3GDBLocalTotal;
								MemoryMg.getInstance().User_3GRelTotalPTT = MemoryMg.getInstance().User_3GDBLocalTotalPTT;
								MemoryMg.getInstance().User_3GRelTotalVideo = MemoryMg.getInstance().User_3GDBLocalTotalVideo;
								this.Upload3GTotal(new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GDBLocalTotal)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GDBLocalTotalPTT)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GDBLocalTotalVideo)).toString());
							}
						} else {
							MemoryMg.getInstance().User_3GRelTotal = 0.0;
							MemoryMg.getInstance().User_3GRelTotalPTT = 0.0;
							MemoryMg.getInstance().User_3GRelTotalVideo = 0.0;
							this.Upload3GTotal("0", "0", "0");
							MemoryMg.getInstance().User_3GLocalTotal = 0.0;
							MemoryMg.getInstance().User_3GLocalTotalPTT = 0.0;
							MemoryMg.getInstance().User_3GLocalTotalVideo = 0.0;
							MemoryMg.getInstance().User_3GLocalTime = this.GetCurrentMouth(false);
						}
					}
					MyLog.i(this.tag, "test1test2test3" + string2 + " " + (String.valueOf(MemoryMg.getInstance().User_3GRelTotal) + "|" + MemoryMg.getInstance().User_3GRelTotalPTT + "|" + MemoryMg.getInstance().User_3GRelTotalVideo) + " " + (String.valueOf(MemoryMg.getInstance().User_3GDBLocalTotal) + "|" + MemoryMg.getInstance().User_3GDBLocalTotalPTT + "|" + MemoryMg.getInstance().User_3GDBLocalTotalVideo));
					if (MemoryMg.getInstance().User_3GTotal > 0.0 && MemoryMg.getInstance().User_3GTotal != -1.0) {
						(this.flowThread = new TotalFlowThread(Receiver.mContext)).start();
					}
				} else if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_UNIONPASSWORDLOGIN_STATE) {
					if (extendedSipCallbackPara.para1.equals("fail")) {
						SipUAApp.mContext.sendBroadcast(new Intent("android.intent.action.RestartUnionLogin"));
					}
				} else {
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_TEMPGROUP_ADD_MEMBER) {
						final Intent intent7 = new Intent();
						intent7.setAction("com.zed3.sipua.tmpgrp.invite");
						final String[] split2 = extendedSipCallbackPara.para1.split(",");
						final ArrayList<String> list = new ArrayList<String>();
						if (split2 != null) {
							for (int i = 0; i < split2.length; ++i) {
								list.add(split2[i]);
							}
						}
						intent7.putStringArrayListExtra("inviteMembers", (ArrayList) list);
						SipUAApp.mContext.sendBroadcast(intent7);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_TMPGRP_HANGUP_LINE) {
						this.process_TYPE_TMPGRP_HANGUP_LINE_inner(extendedSipCallbackPara);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLINCOMING) {
						this.onCallIncominginner((Call) extendedSipCallbackPara.para2, (NameAddress) extendedSipCallbackPara.para3, (NameAddress) extendedSipCallbackPara.para4, (String) extendedSipCallbackPara.para5, (Message) extendedSipCallbackPara.para6);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLRINGING) {
						this.onCallRinginginner((Call) extendedSipCallbackPara.para2, (Message) extendedSipCallbackPara.para3);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLACCEPTED) {
						this.onCallAcceptedinner((Call) extendedSipCallbackPara.para2, (String) extendedSipCallbackPara.para3, (Message) extendedSipCallbackPara.para4);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLREFUSED) {
						this.onCallRefusedinner((Call) extendedSipCallbackPara.para2, (String) extendedSipCallbackPara.para3, (Message) extendedSipCallbackPara.para4);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLCLOSING) {
						this.onCallClosinginner((Call) extendedSipCallbackPara.para2, (Message) extendedSipCallbackPara.para3);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLCLOSED) {
						this.onCallClosedinner((Call) extendedSipCallbackPara.para2, (Message) extendedSipCallbackPara.para3);
						return;
					}
					if (extendedSipCallbackPara.type == ExtendedSipCallbackType.TYPE_ONCALLTIMEOUT) {
						this.onCallTimeoutinner((Call) extendedSipCallbackPara.para2);
					}
				}
			}
		}
	}

	private String getCurrentGroupCallId() {
		final String s = "";
		final PttGrp getCurGrp = this.GetCurGrp();
		String callID = s;
		if (getCurGrp != null) {
			final Object oVoid = getCurGrp.oVoid;
			callID = s;
			if (oVoid != null) {
				final ExtendedCall extendedCall = (ExtendedCall) oVoid;
				callID = s;
				if (extendedCall != null) {
					if (extendedCall.getDialog() == null) {
						return "";
					}
					callID = extendedCall.getDialog().getCallID();
				}
			}
		}
		return callID;
	}

	private ExtendedCall getIdlePttLine() {
		for (int i = 0; i < this.calls.size(); ++i) {
			final ExtendedCall extendedCall = this.calls.get(i);
			final CallManager.CallState callState = extendedCall.getCallState();
			int n;
			if (extendedCall.isOnCall()) {
				n = 0;
			} else {
				n = 1;
			}
			if (n != 0) {
				ExtendedCall extendedCall2 = extendedCall;
				if (CallManager.CallState.IDLE != callState) {
					extendedCall2 = extendedCall;
					if (CallManager.CallState.UNKNOW != callState) {
						continue;
					}
				}
				return extendedCall2;
			}
		}
		return null;
	}

	private String getSessionDescriptor() {
		return this.local_session;
	}

	private boolean groupAccept(final ExtendedCall extendedCall) {
		boolean b = true;
		this.logcall("groupAccept(ExtendedCall)", extendedCall);
		if (extendedCall == null) {
			b = false;
		} else {
			this.setMediaProt(extendedCall, true, true);
			extendedCall.accept(this.local_session);
			if (7 == this.GetPttStatus()) {
				this.startMediaApplication(extendedCall, -1);
				return true;
			}
		}
		return b;
	}

	private boolean groupcall(final String s, final boolean b, final ExtendedCall extendedCall, final boolean b2) {
		this.logcall("groupcall", extendedCall);
		return this.GetPttStatus() != 9 && this.call(s, b, true, extendedCall, b2, null, null, false);
	}

	private void grouphangup(final ExtendedCall extendedCall) {
		this.logcall("grouphangup(ExtendedCall)", extendedCall);
		this.hangup(true, extendedCall);
	}

	private void grouphangupinner(final PttGrp pttGrp) {
		this.logfunc("grouphangupinner(PttGrp)");
		if (pttGrp != null && pttGrp.oVoid != null) {
			this.grouphangup((ExtendedCall) pttGrp.oVoid);
			pttGrp.isCreateSession = false;
			pttGrp.oVoid = null;
		}
	}

	private void haltGroupCallinner() {
		MyLog.i("UserAgent", "HaltGroupCall inner");
		this.pttGroupRelease(false, null);
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
			getCurGrp.speaker = "";
			getCurGrp.speakerN = "";
			final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
			intent.putExtra("0", getCurGrp.grpID);
			intent.putExtra("1", getCurGrp.speaker);
			Receiver.mContext.sendBroadcast(intent);
			if (UserAgent.isTempGrpCallMode) {
				this.hangupTmpGrpCallinner(false);
				Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
			}
		}
	}

	private void haltListen(final boolean b) {
		this.logfunc("haltListen(boolean)");
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_HALT_LISTEN);
		callStatusPara.setPara1(b);
		this.handleCallStatus(callStatusPara);
	}

	private void handleAntaMessage(final Message message) {
		final String value = message.getHeader("Anta-Extension").getValue();
		System.out.println("xxxxxx UserAgent handleAntaMessage pttExtensionValue" + value);
		if (value.substring(value.length() - 3, value.length()).equals("msg")) {
			if (!value.substring(10, 20).equals(DataBaseService.getInstance().getMsgVersion())) {
				AddressBookUtils.getMsgList();
			}
			return;
		}
		AddressBookUtils.updateAlVersion(value.substring(18));
	}

	private void handleCallStatus(final CallStatusPara obj) {
		this.logfunc("handleCallStatus name =" + obj.getCallStatus().name());
		MyLog.i("dd", "callStatus=" + obj);
		if (this.cmdHandler != null) {
			final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
			obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_CALL_CMD.ordinal();
			obtainMessage.arg2 = 4;
			obtainMessage.obj = obj;
			this.cmdHandler.sendMessage(obtainMessage);
		}
		MyLog.i("test", "cmdHandler = " + this.cmdHandler);
	}

	private void hangup(final boolean b, final ExtendedCall para2) {
		this.logcall("hangup(boolean,ec)", para2);
		if (b == this.IsPttMode()) {
			final String tag = this.tag;
			final StringBuilder sb = new StringBuilder("closeMediaApplication hangup ");
			String s;
			if (b) {
				s = "goupcall";
			} else {
				s = "single call";
			}
			MyLog.i(tag, sb.append(s).toString());
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null && para2 == getCurGrp.oVoid) {
				this.closeMediaApplication(para2.getExtCallId(), false, this.isMonitorOnline());
			}
		}
		if (para2 != null) {
			this.removeCall(para2);
			if (!b) {
				para2.hangup();
			} else {
				para2.grouphangup();
			}
			this.addCall();
		}
		if (!this.IsPttMode() && !b) {
			RtpStreamReceiver_signal.ringback(false);
			this.prepareAbortCall(para2);
			this.changeStatus(0);
			this.abortCallCompleted(para2);
			this.SetPttMode(true);
		}
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_LOCAL_HANGUP_LINE;
		extendedSipCallbackPara.para1 = this.GetCurGrp().grpID;
		extendedSipCallbackPara.para2 = para2;
	}

	private void hangup(final boolean b, final boolean b2) {
		this.logfunc("hangup(boolean,rejoin)");
		final CallManager manager = CallManager.getManager();
		final VideoManagerService default1 = VideoManagerService.getDefault();
		MyLog.d("videoTrace", "UserAgent#hangup() enter getAbortCall() = " + this.getAbortCall());
		if (this.getAbortCall() == null) {
			MyLog.d("videoTrace", "UserAgent#hangup() enter reject call is null");
			return;
		}
		MyLog.d("videoTrace", "UserAgent#hangup() enter abort user name = " + manager.getCallerUsername(this.getAbortCall()));
		this.printLog("HANGUP");
		final boolean existInAudioCall = manager.existInAudioCall();
		final CallManager.CallState callState = this.getAbortCall().getCallState();
		MyLog.d("videoTrace", "UserAgent#hangup() enter existAudioCall = " + existInAudioCall);
		final boolean b3 = manager.isAudioCall(this.getAbortCall()) || manager.isVideoCall(this.getAbortCall());
		boolean b4 = false;
		if ((callState == CallManager.CallState.INCALL || callState == CallManager.CallState.OUTGOING) && default1.isCurrentVideoCall()) {
			b4 = true;
		} else if (b3) {
			b4 = true;
		}
		MyLog.d("videoTrace", "UserAgent#hangup() enter abort result = " + b4);
		if (!b) {
			final String tag = this.tag;
			final StringBuilder sb = new StringBuilder("closeMediaApplication hangup ");
			String s;
			if (b) {
				s = "goupcall";
			} else {
				s = "single call";
			}
			MyLog.i(tag, sb.append(s).toString());
			this.closeMediaApplication(this.getAbortCall().getExtCallId(), !b4, false);
		}
		final ExtendedCall abortCall = this.getAbortCall();
		if (abortCall != null) {
			this.removeCall(abortCall);
			if (!b) {
				abortCall.hangup();
			} else {
				abortCall.grouphangup();
			}
			this.addCall();
		}
		if (!b) {
			RtpStreamReceiver_signal.ringback(false);
			this.prepareAbortCall(abortCall);
			this.changeStatus(0);
			this.abortCallCompleted(abortCall);
			if (!this.IsPttMode()) {
				this.SetPttMode(true);
				if (b2) {
					this.pttGroupJoin();
				}
			}
		}
		manager.dispatchAbortCompleted(this.getAbortCall());
		default1.dispatchEndCallCompleted();
	}

	private void hangupTmpGrpCallinner(final boolean flag) {
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_TMPGRP_HANGUP_LINE;
		extendedSipCallbackPara.para2 = TempGroupCallUtil.mCall;
		extendedSipCallbackPara.flag = flag;
		this.process_TYPE_TMPGRP_HANGUP_LINE_inner(extendedSipCallbackPara);
	}

	private void hangupWithoutRejoinInner() {
		this.logfunc("hangupWithoutRejoinInner()");
		this.hangup(false, false);
	}

	private void hangupinner() {
		this.logfunc("hangupinner()");
		this.prepareRejectVideoCall();
		this.hangup(false, true);
		this.prepareRejectAudioCall();
		this.hangup(false, true);
	}

	private void hangupinnerWithoutRejoin() {
		this.logfunc("hangupinner()");
		this.prepareRejectVideoCall();
		this.hangup(false, false);
		this.prepareRejectAudioCall();
		this.hangup(false, false);
	}

	private void initSessionDescriptor(final Codecs.Map map, final boolean b) {
		this.local_session = new SessionDescriptor(this.user_profile.from_url, this.sip_provider.getViaAddress()).toString();
		if (this.user_profile.audio) {
			this.addMediaDescriptor("audio", this.user_profile.audio_port, map);
		}
		if (b) {
			this.initVideoSocket();
			if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("videocode", "0").equals("1")) {
				this.addMediaDescriptor("video", this.user_profile.video_port, this.user_profile.video_avps, "H264S", 90000);
			} else {
				this.addMediaDescriptor("video", this.user_profile.video_port, this.user_profile.video_avp, "H264", 90000);
			}
		}
		this.user_profile.video = true;
	}

	private void initSessionDescriptorForGroupCall(final Codecs.Map map) {
		this.local_session = new SessionDescriptor(this.user_profile.from_url, this.sip_provider.getViaAddress()).toString();
		if (this.user_profile.audio) {
			this.addMediaDescriptor("audio", this.user_profile.audio_port, map);
		}
	}

	private SipdroidSocket initVideoSocket() {
		if (this.mVedioSocket != null) {
			this.mVedioSocket.close();
		}
		while (true) {
			try {
				this.mVedioSocket = new SipdroidSocket(0);
				this.setVedioProt(this.mVedioSocket.getLocalPort());
				return this.mVedioSocket;
			} catch (SocketException ex) {
				ex.printStackTrace();
				continue;
			} catch (UnknownHostException ex2) {
				ex2.printStackTrace();
				continue;
			}
		}
	}

	private void innerHaltListen(final boolean b) {
		this.logfunc("innerHaltListen");
		if (this.cmdProcThread != null) {
			MyLog.i("UserAgent", "innerHaltListen .....  Exceptioin Log for test");
			new Exception("---print trace----").printStackTrace();
			this.cmdHandler.getLooper().quit();
			this.cmdHandler = null;
			this.cmdProcThread = null;
		}
		MyHeartBeatReceiver.stop("UserAgent#innerHaltListen");
		if (this.calls != null) {
			this.calls.clear();
		}
		if (this.user_profile.gps && this.isStartedGPS) {
			this.GPSCloseLock(b);
			this.isStartedGPS = false;
			if (b) {
				SipUAApp.getInstance().stopGpsThread();
			}
		}
		this.isInitGroupData = false;
		if (this.flowThread != null) {
			MyLog.i(this.tag, "haltListen StopFlow");
			this.flowThread.StopFlow();
			this.flowThread = null;
		}
		try {
			if (this.SetGrpRecv != null && this.mGrpFilter != null) {
				SipUAApp.mContext.unregisterReceiver((BroadcastReceiver) this.SetGrpRecv);
			}
		} catch (Exception ex) {
			MyLog.e("UserAgent batterylow", ex.toString());
		}
	}

	private boolean interceptStartMediaApplication(final ExtendedCall extendedCall) {
		this.logcall("interceptStartMediaApplication(ExtendedCall)", extendedCall);
		final CallManager manager = CallManager.getManager();
		final VideoManagerService default1 = VideoManagerService.getDefault();
		return manager.isVideoCall(extendedCall) && (default1.isCurrentVideoMonitor() || default1.isCurrentVideoUpload() || default1.isCurrentVideoTRANSCRIBE());
	}

	private boolean isAudioCall(final Call call) {
		return CallManager.getManager().isAudioCall(call);
	}

	private boolean isIdleOfPttLines() {
		this.logfunc("isIdleOfPttLines()");
		for (int i = 0; i < this.calls.size(); ++i) {
			if (this.calls.get(i).isOnCall()) {
				return false;
			}
		}
		return true;
	}

	private boolean isInCalls(final ExtendedCall extendedCall) {
		synchronized (this) {
			return this.calls.contains(extendedCall);
		}
	}

	private boolean isMonitorOnline() {
		final Iterator<ExtendedCall> iterator = this.calls.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getCallTypeEx() == 4) {
				return true;
			}
		}
		return false;
	}

	private boolean isPttGroupCall(final Call call) {
		return CallManager.getManager().isPttGroupCall(call);
	}

	private boolean isPttKeyDown() {
		return this.ptt_key_down;
	}

	private boolean isVideoCall(final Call call) {
		return CallManager.getManager().isVideoCall(call);
	}

	private void logcall(String s, final ExtendedCall extendedCall) {
		if (extendedCall != null && extendedCall.getDialog() != null && extendedCall.getDialog().getInviteMessage() != null) {
			final int callDirection = extendedCall.getCallDirection();
			final StringBuilder append = new StringBuilder("Func:").append(s).append(",").append("calltype:").append(InviteCallType.getCallTypeString(extendedCall.getCallTypeEx())).append(",").append("callid:").append(extendedCall.getDialog().getCallID()).append(",").append("calldirection:");
			if (callDirection == 0) {
				s = "caller";
			} else if (callDirection == 1) {
				s = "callee";
			} else {
				s = "invalid";
			}
			MyLog.i("UserAgentCall", append.append(s).append(",").append("peer number:").append(extendedCall.getPeerNumber()).toString());
			return;
		}
		this.logfunc(s);
	}

	private void logfunc(final String s) {
		MyLog.i("UserAgentCall", String.valueOf(s) + Thread.currentThread().getId());
	}

	private ExtendedCall makeExtendedCall() {
		this.logfunc("makeExtendedCall");
		return new ExtendedCall(this.sip_provider, this.user_profile.from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this);
	}

	private boolean makeTempGrpCallinner(final String s, final String grpName, final ArrayList<String> list) {
		if (!this.IsPttMode()) {
			return false;
		}
		final PttGrp pttGrp = new PttGrp();
		pttGrp.grpID = Settings.getUserName();
		pttGrp.grpName = grpName;
		pttGrp.level = -1;
		pttGrp.isCreateSession = true;
		final ExtendedCall extendedCall = this.makeExtendedCall();
		pttGrp.oVoid = extendedCall;
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			this.preGrpBeforeTmpGrpCall = getCurGrp.grpID;
			MyLog.i("zdx", "makeTempGrpCall#preGrpBeforeTmpGrpCall = " + this.preGrpBeforeTmpGrpCall);
		}
		this.setCurGrpinner(pttGrp, true);
		UserAgent.isTempGrpCallMode = true;
		return this.tempGroupCall(s, false, extendedCall, false, grpName, list);
	}

	private boolean muteMediaApplicationinner() {
		return this.audio_app != null && this.audio_app.muteMedia();
	}

	private void onCallAcceptedinner(final Call targetCall, String value, final Message message) {
		this.logcall("onCallAcceptedinner", (ExtendedCall) targetCall);
		MyLog.d("videoTrace", "UserAgent#onCallAccepted enter");
		this.printLog("onCallAccepted()", 5);
		if (!this.isInCalls((ExtendedCall) targetCall) && targetCall != this.call_transfer) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("ACCEPTED/CALL", 1);
			if (!this.IsPttMode()) {
				if (!this.statusIs(2)) {
					this.hangupinner();
					return;
				}
				if (message.hasPttExtensionHeader() || ((ExtendedCall) targetCall).getCallTypeEx() == 2) {
					this.hangup(true, false);
				}
			}
			if (message.getSupportedHeader() != null) {
				targetCall.startUpdateMessage();
			}
			final SessionDescriptor sessionDescriptor = new SessionDescriptor(value);
			final ExtendedCall extendedCall = null;
			ExtendedCall extendedCall2 = null;
			Label_0412:
			{
				if (message.hasPttExtensionHeader() || ((ExtendedCall) targetCall).getCallTypeEx() == 2) {
					if (!message.hasPttExtensionHeader()) {
						MyLog.i("error_tag", "resp.hasPttExtensionHeader() = false ,but group call");
					}
					this.sessionProduct(sessionDescriptor, true, targetCall);
					final Header header = message.getHeader("Ptt-Extension");
					if (header == null) {
						if (((ExtendedCall) targetCall).getDialog().getInviteMessage().getPttExtensionHeader().getValue().contains("rejoin")) {
							value = "3ghandset listen";
						} else {
							value = "3ghandset accept";
						}
					} else {
						value = header.getValue();
					}
					if (value.equalsIgnoreCase("3ghandset listen")) {
						final PttGrp getCurGrp = this.GetCurGrp();
						if (getCurGrp.oVoid != targetCall) {
							final PttGrps getAllGrps = this.GetAllGrps();
							if (getAllGrps != null) {
								for (int i = 0; i < getAllGrps.GetCount(); ++i) {
									final PttGrp getGrpByIndex = getAllGrps.GetGrpByIndex(i);
									if (getGrpByIndex.oVoid != null && ((ExtendedCall) targetCall).getDialog().getCallID().equalsIgnoreCase(((ExtendedCall) getGrpByIndex.oVoid).getDialog().getCallID())) {
										this.grouphangupinner(getGrpByIndex);
										return;
									}
								}
							}
							targetCall.grouphangup();
							return;
						}
						getCurGrp.isCreateSession = true;
						MyLog.i(this.tag, "TYPE_REQUEST_LISTEN_LINE 0.");
						extendedCall2 = extendedCall;
						if (7 != this.GetPttStatus()) {
							MyLog.i(this.tag, "TYPE_REQUEST_LISTEN_LINE 1.");
							this.startMediaApplication((ExtendedCall) getCurGrp.oVoid, -1);
							MyLog.i(this.tag, "SetPttStatus  PTT_LISTENING TYPE_REQUEST_LISTEN_LINE.");
							this.SetPttStatus(7);
							extendedCall2 = extendedCall;
						}
					} else {
						final PttGrp getCurGrp2 = this.GetCurGrp();
						if (getCurGrp2.oVoid != targetCall) {
							this.pttGroupRelease(false, (ExtendedCall) targetCall);
							return;
						}
						getCurGrp2.isCreateSession = true;
						extendedCall2 = extendedCall;
						if (this.GetPttStatus() != 6) {
							if (!this.isPttKeyDown()) {
								this.startMediaApplication((ExtendedCall) getCurGrp2.oVoid, -1);
								this.PttGroupReleaseSpeak();
								MyLog.i("hst", "extendsSipProcess");
								extendedCall2 = extendedCall;
							} else {
								extendedCall2 = extendedCall;
								if (6 != this.GetPttStatus()) {
									final ExtendedCall extendedCall3 = (ExtendedCall) getCurGrp2.oVoid;
									this.SetPttStatus(6);
									MyLog.i(this.tag, "SetPttStatus  PTT_TALKING TYPE_REQUEST_ACCEPT_PHONE.");
									this.startMediaApplication(extendedCall3, 1);
									extendedCall2 = extendedCall;
								}
							}
						}
					}
				} else {
					RtpStreamReceiver_signal.ringback(false);
					this.sessionProduct(sessionDescriptor, false, targetCall);
					if (!UserAgent.isTempGrpCallMode) {
						final ExtendedCall setTargetCall = this.setTargetCall(targetCall);
						RtpStreamReceiver_signal.ringback(false);
						setTargetCall.setLocalSessionDescriptor(this.local_session);
						final boolean startMediaApplication = this.startMediaApplication(setTargetCall, 0);
						if (!startMediaApplication) {
							extendedCall2 = setTargetCall;
							if (startMediaApplication) {
								break Label_0412;
							}
							if (!VideoManagerService.getDefault().isCurrentVideoMonitor() && !VideoManagerService.getDefault().isCurrentVideoUpload()) {
								extendedCall2 = setTargetCall;
								if (!VideoManagerService.getDefault().isCurrentVideoTRANSCRIBE()) {
									break Label_0412;
								}
							}
						}
						CallManager.getManager().getCall(CallManager.getCallExtId(setTargetCall)).setCallState(CallManager.CallState.INCALL);
						this.changeStatus(3);
						extendedCall2 = setTargetCall;
					} else if (TempGroupCallUtil.mCall == null || (TempGroupCallUtil.mCall != null && ((ExtendedCall) TempGroupCallUtil.mCall).getExtCallId().equals(((ExtendedCall) targetCall).getExtCallId()))) {
						RtpStreamReceiver_signal.ringback(false);
						extendedCall2 = extendedCall;
						if (((ExtendedCall) targetCall).getDialog().getCallID().equals(((ExtendedCall) this.GetCurGrp().oVoid).getDialog().getCallID())) {
							this.startMediaApplication((ExtendedCall) targetCall, -1);
							TempGroupCallUtil.mCall = targetCall;
							this.SetPttStatus(7);
							Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.create_success"));
							extendedCall2 = extendedCall;
						}
					} else {
						this.sessionProduct(sessionDescriptor, false, targetCall);
						final ExtendedCall setTargetCall2 = this.setTargetCall(targetCall);
						setTargetCall2.setLocalSessionDescriptor(this.local_session);
						final boolean startMediaApplication2 = this.startMediaApplication(setTargetCall2, 0);
						if (!startMediaApplication2) {
							extendedCall2 = setTargetCall2;
							if (startMediaApplication2) {
								break Label_0412;
							}
							if (!VideoManagerService.getDefault().isCurrentVideoMonitor()) {
								extendedCall2 = setTargetCall2;
								if (!VideoManagerService.getDefault().isCurrentVideoUpload()) {
									break Label_0412;
								}
							}
						}
						CallManager.getManager().getCall(CallManager.getCallExtId(setTargetCall2)).setCallState(CallManager.CallState.INCALL);
						this.changeStatus(3);
						extendedCall2 = setTargetCall2;
					}
				}
			}
			if (targetCall == this.call_transfer) {
				final StatusLine statusLine = message.getStatusLine();
				extendedCall2.notify(statusLine.getCode(), statusLine.getReason());
			}
		}
	}

	private void onCallCancelingInner(final Call call, final Message message) {
		this.logcall("onCallCancelingInner", (ExtendedCall) call);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("CANCEL", 1);
			MyLog.d("pttTrace", "UserAgent#onCallCanceling() enter IsPttMode() = " + this.IsPttMode());
			final PttGrps getAllGrps = this.GetAllGrps();
			if (getAllGrps != null) {
				for (int i = 0; i < getAllGrps.GetCount(); ++i) {
					final PttGrp getGrpByIndex = getAllGrps.GetGrpByIndex(i);
					if (getGrpByIndex.oVoid != null && message.getCallIdHeader().getCallId().equalsIgnoreCase(((ExtendedCall) getGrpByIndex.oVoid).getDialog().getCallID())) {
						getGrpByIndex.oVoid = null;
						getGrpByIndex.isCreateSession = false;
						this.removeCall((ExtendedCall) call);
						this.addCall();
						return;
					}
				}
			}
			if (!this.IsPttMode() || !message.hasPttExtensionHeader()) {
				if (CallUtil.isInCallState() && !UserAgent.isTempGrpCallMode) {
					MyLog.d("pttTrace", "UserAgent#onCallCanceling() enter IsPttMode() = " + this.IsPttMode());
					this.hangupinner();
				} else if (UserAgent.isTempGrpCallMode) {
					ExtendedCall extendedCall = null;
					if (TempGroupCallUtil.mCall != null) {
						extendedCall = (ExtendedCall) TempGroupCallUtil.mCall;
					}
					if (extendedCall == null || (extendedCall != null && !message.getCallIdHeader().getCallId().equalsIgnoreCase(extendedCall.getDialog().getCallID()))) {
						this.prepareAbortCall(call);
						this.changeStatus(0);
						this.abortCallCompleted(call);
					} else {
						UserAgent.isTempGrpCallMode = false;
						TempGroupCallUtil.mCall = null;
						SipUAApp.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
					}
				} else {
					this.prepareAbortCall(call);
					this.changeStatus(0);
					this.abortCallCompleted(call);
				}
			}
			MyLog.d("pttTrace", "UserAgent#onCallCanceling() enter set ptt mode true ");
			this.SetPttMode(true);
			this.removeCall((ExtendedCall) call);
			this.addCall();
			if (((ExtendedCall) call).getCallTypeEx() == 4 || ((ExtendedCall) call).getCallTypeEx() == 1) {
				final VideoManagerService default1 = VideoManagerService.getDefault();
				if (default1.existRemoteVideoControl()) {
					default1.clearRemoteVideoParameter();
				}
			}
		}
	}

	private void onCallClosedinner(final Call call, final Message message) {
		this.logcall("onCallClosedinner", (ExtendedCall) call);
		MyLog.d("videoTrace", "UserAgent#onCallClosed enter");
		this.printLog("onCallClosed()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("CLOSE/OK", 1);
			final CallManager manager = CallManager.getManager();
			MyLog.d("videoTrace", "UserAgent#onCallClosed isgroup call = " + manager.isGroupCall(call));
			if (!this.IsPttMode() && !manager.isGroupCall(call)) {
				MyLog.d("videoTrace", "UserAgent#onCallClosed change status idle");
				this.prepareAbortCall(call);
				this.changeStatus(0);
				this.abortCallCompleted(call);
			}
			if (!this.IsPttMode() && !manager.isGroupCall(call)) {
				MyLog.d("videoTrace", "UserAgent#onCallClosed pttGroupJoin");
				this.SetPttMode(true);
				this.pttGroupJoin();
			}
		}
	}

	private void onCallClosinginner(final Call para2, final Message message) {
		this.logcall("onCallClosinginner", (ExtendedCall) para2);
		MyLog.d("videoTrace", "UserAgent#onCallClosing() enter");
		final CallManager manager = CallManager.getManager();
		final VideoManagerService default1 = VideoManagerService.getDefault();
		this.printLog("onCallClosing()", 5);
		if (!this.isInCalls((ExtendedCall) para2) && para2 != this.call_transfer) {
			this.printLog("NOT the current call", 5);
		} else {
			if (para2 != this.call_transfer && this.call_transfer != null) {
				this.printLog("CLOSE PREVIOUS CALL", 1);
				this.videoCall = this.call_transfer;
				this.call_transfer = null;
				return;
			}
			this.printLog("CLOSE", 1);
			if (!((ExtendedCall) para2).isGroupCall) {
				final boolean videoCall = manager.isVideoCall(para2);
				final boolean audioCall = manager.isAudioCall(para2);
				boolean b = false;
				if (videoCall && default1.isCurrentVideoCall()) {
					b = true;
				} else if (audioCall) {
					b = true;
				}
				if (default1.isCurrentVideoTRANSCRIBE()) {
					return;
				}
				MyLog.d("videoTrace", "UserAgent#onCallClosing() closeMediaApplication result = " + b);
				final String tag = this.tag;
				final StringBuilder sb = new StringBuilder("closeMediaApplication onCallClosing ");
				String s;
				if (((ExtendedCall) para2).isGroupCall) {
					s = "goupcall";
				} else {
					s = "single call";
				}
				MyLog.i(tag, sb.append(s).toString());
				this.closeMediaApplication(((ExtendedCall) para2).getExtCallId(), !b, false);
				this.prepareAbortCall(para2);
				this.changeStatus(0);
				para2.stopUpdateMessage();
				this.abortCallCompleted(para2);
			}
			this.removeCall((ExtendedCall) para2);
			this.addCall();
			if (UserAgent.isTempGrpCallMode && TempGroupCallUtil.mCall != null && ((ExtendedCall) para2).getDialog().getCallID().equals(((ExtendedCall) TempGroupCallUtil.mCall).getDialog().getCallID())) {
				this.hangupTmpGrpCallinner(true);
				Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.tmpgrp.closing"));
				return;
			}
			final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
			extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_PEER_HANGUP_LINE;
			extendedSipCallbackPara.para2 = para2;
			this.process_TYPE_LOCAL_HANGUP_LINE_or_TYPE_PEER_HANGUP_LINEinner(extendedSipCallbackPara);
		}
	}

	private void onCallIncominginner(final Call call, final NameAddress nameAddress, final NameAddress nameAddress2, String userName, final Message message) {
		this.logcall("onCallIncominginner", (ExtendedCall) call);
		if (MyPhoneStateListener.getInstance().isInCall()) {
			this.busyNotifier(call);
		} else {
			// TODO
		}
	}

	private void onCallRinginginner(final Call call, final Message message) {
		this.logcall("onCallRinginginner", (ExtendedCall) call);
		MyLog.d("videoTrace", "UserAgent#onCallRing() enter");
		if (!this.isInCalls((ExtendedCall) call) && call != this.call_transfer) {
			this.printLog("NOT the current call", 5);
		} else {
			final ExtendedCall extendedCall = (ExtendedCall) call;
			if (!extendedCall.getDialog().getInviteMessage().hasPttExtensionHeader() || extendedCall.getDialog().getInviteMessage().getPttExtensionHeader().getValue().contains("3ghandset tmp")) {
				final String remoteSessionDescriptor = call.getRemoteSessionDescriptor();
				if ((remoteSessionDescriptor == null || remoteSessionDescriptor.length() == 0) && message.getStatusLine().getCode() == 180) {
					if (extendedCall.getDialog().isInInviting() || extendedCall.getDialog().isWaitingOrReWaiting()) {
						this.printLog("RINGING", 1);
						RtpStreamReceiver_signal.ringback(true);
					}
				} else {
					this.printLog("RINGING(with SDP)", 1);
					if (!this.user_profile.no_offer) {
						RtpStreamReceiver_signal.ringback(false);
						this.sessionProduct(new SessionDescriptor(remoteSessionDescriptor), false, call);
						if (message.getStatusLine().getCode() == 183 && (!UserAgent.isTempGrpCallMode || TempGroupCallUtil.mCall == null || !TempGroupCallUtil.mCall.isOnCall())) {
							this.startMediaApplication((ExtendedCall) call, 0);
						}
					}
				}
			}
		}
	}

	private void onCallTimeoutinner(final Call call) {
		this.logcall("onCallTimeoutinner", (ExtendedCall) call);
		this.printLog("onCallTimeout()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("NOT FOUND/TIMEOUT", 1);
		switch (((ExtendedCall) call).getCallTypeEx()) {
			default: {
				this.prepareAbortCall(call);
				RtpStreamReceiver_signal.ringback(false);
				this.changeStatus(0);
				this.abortCallCompleted(call);
				this.SetPttMode(true);
				this.pttGroupJoin();
				break;
			}
			case 3: {
				if (TempGroupCallUtil.mCall == null || (TempGroupCallUtil.mCall != null && ((ExtendedCall) call).getDialog().getCallID().equals(((ExtendedCall) TempGroupCallUtil.mCall).getDialog().getCallID()))) {
					this.hangupTmpGrpCallinner(true);
					final Intent intent = new Intent("com.zed3.sipua.tmpgrp.closing");
					intent.putExtra("isTimeout", true);
					Receiver.mContext.sendBroadcast(intent);
					RtpStreamReceiver_signal.ringback(false);
					break;
				}
				break;
			}
			case 2: {
				if (call != null) {
					for (final PttGrp pttGrp : this.pttGrps.getPttGrps()) {
						if (pttGrp != null && pttGrp.oVoid != null && pttGrp.oVoid == call) {
							pttGrp.speakerN = "";
							pttGrp.speaker = "";
							pttGrp.oVoid = null;
							pttGrp.isCreateSession = false;
							this.SetPttStatus(5);
							pttGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
							final Intent intent2 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
							intent2.putExtra("0", pttGrp.grpID);
							intent2.putExtra("1", "");
							pttGrp.speaker = "";
							Receiver.mContext.sendBroadcast(intent2);
						}
					}
					break;
				}
				break;
			}
		}
		if (call == this.call_transfer) {
			this.videoCall.notify(408, "Request Timeout");
			this.call_transfer = null;
		}
		this.removeCall((ExtendedCall) call);
		this.addCall();
	}

	private void onPttGroupRequestTimeout(final Message message) {
		SipUAApp.getMainThreadHandler().post((Runnable) new Runnable() {
			@Override
			public void run() {
				Toast.makeText(SipUAApp.getAppContext(), R.string.ptt_group_request_timeout, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void onPttKeyDown() {
		this.dispatchPttGroupState(PttGrp.E_Grp_State.GRP_STATE_INITIATING);
	}

	private void openGps() {
		while (true) {
			synchronized (this) {
				final MyHandlerThread getmHandlerThread = SipUAApp.getInstance().getmHandlerThread();
				if (getmHandlerThread != null && !getmHandlerThread.isAlive()) {
					getmHandlerThread.start();
				}
				MyLog.d("testgps", "UserAgent#openGps gpsPacket = " + UserAgent.gpsPacket + " , user_profile.realm_orig = " + this.user_profile.realm_orig);
				if (Tools.isInMainThread()) {
					if (UserAgent.gpsPacket == null) {
						UserAgent.gpsPacket = new GPSPacket(Receiver.mContext, this.user_profile.username, this.user_profile.passwd, this.user_profile.realm_orig);
						GpsTools.setServer(SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("server", ""));
						Receiver.engine(Receiver.mContext).updateDNS();
						UserAgent.gpsPacket.StartGPS(true);
						MyLog.i("GPSSend", "userAgent  new gpspacket  " + this.user_profile.username + this.user_profile.realm_orig);
					} else {
						UserAgent.gpsPacket.StartGPS(false);
						MyLog.i("GPSSend", "userAgent  gpspacket  != null " + this.user_profile.username + this.user_profile.realm_orig);
					}
					return;
				}
			}
			final Intent intent = new Intent(SipUAApp.mContext, (Class) RegisterService.class);
			intent.putExtra("hasgps", true);
			intent.putExtra("gpsopen", true);
			SipUAApp.mContext.startService(intent);
		}
	}

	private void prepareAbortCall(final Call call) {
		this.logcall("prepareAbortCall", (ExtendedCall) call);
		final boolean audioCall = this.isAudioCall(call);
		final boolean videoCall = this.isVideoCall(call);
		MyLog.d("videoTrace", "UserAgent#prepareAbortCall() isAudioCall = " + audioCall + " , isVideoCal = " + videoCall);
		final CallManager manager = CallManager.getManager();
		this.mAbortCall = manager.getCall(CallManager.getCallExtId(call));
		if (audioCall && this.mAbortCall == null) {
			this.prepareRejectAudioCall();
		} else if (videoCall && this.mAbortCall == null) {
			this.prepareRejectVideoCall();
		}
		manager.setCallState(CallManager.CallState.IDLE, this.mAbortCall);
	}

	private void prepareRejectAudioCall() {
		MyLog.d("videoTrace", "UserAgent#setRejectAudioCall() enter");
		this.mAbortCall = this.audioCall;
	}

	private void prepareRejectVideoCall() {
		MyLog.d("videoTrace", "UserAgent#setRejectVideoCall() enter");
		this.mAbortCall = this.videoCall;
	}

	private void processHeadBeatMessage(final android.os.Message message) {
		MyLog.d("pttTrace", "UserAgent#processHeadBeatMessage() enter");
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp == null) {
			MyLog.d("pttTrace", "UserAgent#processHeadBeatMessage() current group is null");
			return;
		}
		final Object obj = message.obj;
		if (obj == null) {
			MyLog.d("pttTrace", "UserAgent#processHeadBeatMessage() message.obj is null");
			return;
		}
		final HeartBeatGrpState heartBeatGrpState = (HeartBeatGrpState) obj;
		if (heartBeatGrpState.getGrpName().equals(getCurGrp.grpID) && !UserAgent.isTempGrpCallMode && !this.getCurGrpState().equalsIgnoreCase(heartBeatGrpState.getGrpState())) {
			if (this.getCurGrpState().equalsIgnoreCase("ON") && heartBeatGrpState.getGrpState().equalsIgnoreCase("OFF")) {
				MyLog.i(this.tag, "do ...pttGroupRelease");
				if (getCurGrp.oVoid != null) {
					final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
					if (extendedCall != null) {
						MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
						this.pttGroupRelease(false, extendedCall);
						getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
						getCurGrp.speaker = "";
						getCurGrp.speakerN = "";
						final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
						intent.putExtra("0", getCurGrp.grpID);
						intent.putExtra("1", getCurGrp.speaker);
						Receiver.mContext.sendBroadcast(intent);
					}
					getCurGrp.speakerN = "";
					getCurGrp.speaker = "";
					getCurGrp.isCreateSession = false;
					getCurGrp.oVoid = null;
				}
			} else if (this.getCurGrpState().equalsIgnoreCase("ON") && heartBeatGrpState.getGrpState().equalsIgnoreCase("OUT")) {
				MyLog.i(this.tag, "do ...pttGroupRelease  then rejoin");
				if (getCurGrp.oVoid != null) {
					final ExtendedCall extendedCall2 = (ExtendedCall) getCurGrp.oVoid;
					if (extendedCall2 != null) {
						MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
						this.pttGroupRelease(false, extendedCall2);
						getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
						getCurGrp.speaker = "";
						getCurGrp.speakerN = "";
						final Intent intent2 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
						intent2.putExtra("0", getCurGrp.grpID);
						intent2.putExtra("1", getCurGrp.speaker);
						Receiver.mContext.sendBroadcast(intent2);
					}
					getCurGrp.speakerN = "";
					getCurGrp.speaker = "";
					getCurGrp.isCreateSession = false;
					getCurGrp.oVoid = null;
					this.pttGroupJoin();
				}
			} else if (this.getCurGrpState().equalsIgnoreCase("OFF") && (heartBeatGrpState.getGrpState().equalsIgnoreCase("ON") || heartBeatGrpState.getGrpState().equalsIgnoreCase("OUT"))) {
				if (getCurGrp.oVoid != null && this.audio_app == null) {
					final ExtendedCall extendedCall3 = (ExtendedCall) getCurGrp.oVoid;
					if (extendedCall3 != null) {
						MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
						this.pttGroupRelease(false, extendedCall3);
						getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
						getCurGrp.speaker = "";
						getCurGrp.speakerN = "";
						final Intent intent3 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
						intent3.putExtra("0", getCurGrp.grpID);
						intent3.putExtra("1", getCurGrp.speaker);
						Receiver.mContext.sendBroadcast(intent3);
					}
					getCurGrp.speakerN = "";
					getCurGrp.speaker = "";
					getCurGrp.isCreateSession = false;
					getCurGrp.oVoid = null;
					this.pttGroupJoin();
				} else {
					this.pttGroupJoin();
				}
			} else if (this.getCurGrpState().equalsIgnoreCase("ON") && heartBeatGrpState.getGrpState().equalsIgnoreCase("ERR")) {
				MyLog.i(this.tag, "do ...pttGroupRelease  then rejoin");
				if (getCurGrp.oVoid != null) {
					final ExtendedCall extendedCall4 = (ExtendedCall) getCurGrp.oVoid;
					if (extendedCall4 != null) {
						MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
						this.pttGroupRelease(false, extendedCall4);
						getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
						getCurGrp.speaker = "";
						getCurGrp.speakerN = "";
						final Intent intent4 = new Intent("com.zed3.sipua.ui_groupcall.group_status");
						intent4.putExtra("0", getCurGrp.grpID);
						intent4.putExtra("1", getCurGrp.speaker);
						Receiver.mContext.sendBroadcast(intent4);
					}
					getCurGrp.speakerN = "";
					getCurGrp.speaker = "";
					getCurGrp.isCreateSession = false;
					getCurGrp.oVoid = null;
					this.pttGroupJoin();
				}
			} else if (this.getCurGrpState().equalsIgnoreCase("OFF") && heartBeatGrpState.getGrpState().equalsIgnoreCase("ERR")) {
				MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg pttGroupRelease");
				this.pttGroupJoin();
			}
		}
		MyLog.d("pttTrace", "UserAgent#processHeadBeatMessage() exit");
	}

	private void process_TYPE_LOCAL_HANGUP_LINE_or_TYPE_PEER_HANGUP_LINEinner(final ExtendedSipCallbackPara extendedSipCallbackPara) {
		final ExtendedCall extendedCall = (ExtendedCall) extendedSipCallbackPara.para2;
		if (!extendedCall.isGroupCall && !this.IsPttMode() && Receiver.call_state == 0) {
			this.SetPttMode(true);
			if (7 != this.GetPttStatus() && 6 != this.GetPttStatus()) {
				MyLog.i(this.tag, "SetPttStatus  PTT_IDLE TYPE_LOCAL_HANGUP_LINE.");
				this.SetPttStatus(5);
				this.pttGroupJoin();
			}
		} else if (this.IsPttMode() && extendedCall.isGroupCall) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp.oVoid == extendedSipCallbackPara.para2) {
				boolean b = false;
				final Iterator<ExtendedCall> iterator = this.calls.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getCallTypeEx() == 4) {
						b = true;
						break;
					}
				}
				android.util.Log.i("GUOK", "ec.isGroupCall:" + b);
				MyLog.i(this.tag, "closeMediaApplication TYPE_LOCAL_HANGUP_LINE");
				this.closeMediaApplication(((ExtendedCall) getCurGrp.oVoid).getExtCallId(), false, b);
				if (6 == this.GetPttStatus()) {
					this.pttReleaseTipSound();
				}
				getCurGrp.speakerN = "";
				getCurGrp.speaker = "";
				getCurGrp.isCreateSession = false;
				getCurGrp.oVoid = null;
				MyLog.i(this.tag, "SetPttStatus  PTT_IDLE TYPE_LOCAL_HANGUP_LINE.");
				this.SetPttStatus(5);
				getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
				final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
				intent.putExtra("0", getCurGrp.grpID);
				intent.putExtra("1", "");
				getCurGrp.speaker = "";
				Receiver.mContext.sendBroadcast(intent);
				this.restorePreGrp();
			}
		}
	}

	private void process_TYPE_PEER_INVITE_LINE_inner(final ExtendedSipCallbackPara extendedSipCallbackPara) {
		final String string = extendedSipCallbackPara.para1.toString();
		String string2;
		if (extendedSipCallbackPara.para3 == null) {
			string2 = null;
		} else {
			string2 = extendedSipCallbackPara.para3.toString();
		}
		final PttGrp getCurGrp = this.GetCurGrp();
		final PttGrp getGrpByID = this.GetGrpByID(string);
		if (getGrpByID != null) {
			final int getPttStatus = this.GetPttStatus();
			if (getCurGrp.grpID.equalsIgnoreCase(string)) {
				if (getCurGrp.isCreateSession) {
					this.grouphangupinner(getCurGrp);
				}
				getGrpByID.oVoid = extendedSipCallbackPara.para2;
				if (getPttStatus != 8 && !getCurGrp.isCreateSession) {
					this.answerGroupCallinner(getGrpByID);
					if (string2 != null && string2.contains("3ghandset open")) {
						getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_LISTENING;
						final String[] split = string2.replace("3ghandset open", "").trim().split(" ");
						if (split.length == 2) {
							getCurGrp.speaker = split[1];
							getCurGrp.speakerN = split[0];
							final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
							intent.putExtra("0", getCurGrp.grpID);
							intent.putExtra("1", getCurGrp.speaker);
							Receiver.mContext.sendBroadcast(intent);
						}
					}
				}
			} else {
				final PttGrp getGrpByID2 = this.GetGrpByID(string);
				if (getGrpByID2 != null) {
					getGrpByID.oVoid = extendedSipCallbackPara.para2;
					if (extendedSipCallbackPara.para2 == getCurGrp.oVoid) {
						getCurGrp.oVoid = null;
					}
					if (getGrpByID.level == 0) {
						this.answerGroupCall(getGrpByID);
						return;
					}
					if (UserAgent.isTempGrpCallMode) {
						this.grouphangupinner(getGrpByID);
						return;
					}
					if (getCurGrp.level != 0 || getPttStatus == 5) {
						if ((getCurGrp.level > getGrpByID2.level && this.grpCallSetupHigh == GrpCallSetupType.GRPCALLSETUPTYPE_TIP) || (getCurGrp.level == getGrpByID2.level && this.grpCallSetupSame == GrpCallSetupType.GRPCALLSETUPTYPE_TIP) || (getCurGrp.level < getGrpByID2.level && this.grpCallSetupLow == GrpCallSetupType.GRPCALLSETUPTYPE_TIP)) {
							this.sendGroupTipBroadcast(this.IsPttMode(), getGrpByID.grpID);
							return;
						}
						if ((getCurGrp.level > getGrpByID2.level && this.grpCallSetupHigh == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT) || (getCurGrp.level == getGrpByID2.level && this.grpCallSetupSame == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT) || (getCurGrp.level < getGrpByID2.level && this.grpCallSetupLow == GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT)) {
							if (Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getBoolean("restoreAfterOtherGrp", true)) {
								this.automaticAnswer = true;
							}
							this.answerGroupCall(getGrpByID);
							return;
						}
						this.grouphangupinner(getGrpByID);
					}
				}
			}
		}
	}

	private void process_TYPE_REQUEST_CANCEL_OK_PHONE_or_TYPE_SERVER_FORCECANCEL_PHONE_inner(final boolean b) {
		if (6 == this.GetPttStatus()) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (b) {
				this.startMediaApplication((ExtendedCall) getCurGrp.oVoid, -1);
			}
			if (this.IsPttMode() && 6 == this.GetPttStatus()) {
				this.pttReleaseTipSound();
			}
			MyLog.i(this.tag, "SetPttStatus  PTT_LISTENING TYPE_REQUEST_CANCEL_WAITING_OK_PHONE.");
			this.SetPttStatus(7);
			getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_IDLE;
			final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
			intent.putExtra("0", getCurGrp.grpID);
			intent.putExtra("1", "");
			getCurGrp.speaker = "";
			Receiver.mContext.sendBroadcast(intent);
		}
	}

	private void process_TYPE_REQUEST_CANCEL_WAITING_OK_PHONE_or_TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE_inner() {
		if (8 == this.GetPttStatus()) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (this.statusBeforeQueue == 5) {
				this.statusBeforeQueue = 7;
				this.grpStateBeforeQueue = PttGrp.E_Grp_State.GRP_STATE_LISTENING;
				this.startMediaApplication((ExtendedCall) getCurGrp.oVoid, -1);
			}
			MyLog.i(this.tag, "SetPttStatus  " + this.statusBeforeQueue + " TYPE_REQUEST_CANCEL_WAITING_OK_PHONE.");
			this.SetPttStatus(this.statusBeforeQueue);
			getCurGrp.state = this.grpStateBeforeQueue;
			final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
			intent.putExtra("0", getCurGrp.grpID);
			intent.putExtra("1", String.valueOf(getCurGrp.speakerN) + " " + getCurGrp.speaker);
			Receiver.mContext.sendBroadcast(intent);
		}
	}

	private void process_TYPE_REQUEST_REJECT_LINE_inner(final ExtendedSipCallbackPara extendedSipCallbackPara) {
		final ExtendedCall extendedCall = (ExtendedCall) extendedSipCallbackPara.para2;
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null && getCurGrp.oVoid != null && extendedCall.getDialog().getCallID().equalsIgnoreCase(((ExtendedCall) getCurGrp.oVoid).getDialog().getCallID())) {
			MyLog.i(this.tag, "SetPttStatus  PTT_IDLE TYPE_REQUEST_REJECT_LINE.");
			this.SetPttStatus(5);
			getCurGrp.state = PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN;
			getCurGrp.speaker = "";
			getCurGrp.speakerN = "";
			final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
			intent.putExtra("0", getCurGrp.grpID);
			intent.putExtra("1", getCurGrp.speaker);
			Receiver.mContext.sendBroadcast(intent);
		}
		final PttGrps getAllGrps = this.GetAllGrps();
		if (getAllGrps != null) {
			for (int i = 0; i < getAllGrps.GetCount(); ++i) {
				final PttGrp getGrpByIndex = getAllGrps.GetGrpByIndex(i);
				if (getGrpByIndex.oVoid != null && extendedCall.getDialog().getCallID().equalsIgnoreCase(((ExtendedCall) getGrpByIndex.oVoid).getDialog().getCallID())) {
					getGrpByIndex.oVoid = null;
					getGrpByIndex.isCreateSession = false;
					this.removeCall(extendedCall);
					this.addCall();
					return;
				}
			}
		}
	}

	private void process_TYPE_TMPGRP_HANGUP_LINE_inner(final ExtendedSipCallbackPara extendedSipCallbackPara) {
		if (UserAgent.isTempGrpCallMode) {
			TempGroupCallUtil.arrayListMembers.clear();
			RtpStreamReceiver_signal.ringback(false);
			if (this.GetPttStatus() != 7 && this.GetCurGrp() != null && this.GetCurGrp().oVoid != null) {
				final InviteDialog dialog = ((ExtendedCall) this.GetCurGrp().oVoid).getDialog();
				if (dialog != null) {
					dialog.cancel();
				}
			}
			MyLog.i("zdx", "hangupTmpGrpCall#preGrpBeforeTmpGrpCall = " + this.preGrpBeforeTmpGrpCall);
			UserAgent.isTempGrpCallMode = false;
			final PttGrp getGrpByID = this.GetGrpByID(this.preGrpBeforeTmpGrpCall);
			if (!TextUtils.isEmpty((CharSequence) this.preGrpBeforeTmpGrpCall) && getGrpByID != null) {
				this.setCurGrpinner(getGrpByID, extendedSipCallbackPara.flag);
			} else {
				MyLog.i("zdx", "Err-hangupTmpGrpCall#preGrpBeforeTmpGrpCall = [ " + this.preGrpBeforeTmpGrpCall + " ]");
				MyLog.i("zdx", "Err-hangupTmpGrpCall#pttGrps.FirstGrp() = " + this.pttGrps.FirstGrp());
				this.setCurGrpinner(this.pttGrps.FirstGrp(), extendedSipCallbackPara.flag);
			}
			this.preGrpBeforeTmpGrpCall = "";
			TempGroupCallUtil.mCall = null;
		}
	}

	private void pttAcceptTipSound() {
		if (this.IsPttMode()) {
			TipSoundPlayer.getInstance().play(TipSoundPlayer.Sound.PTT_ACCEPT);
			SystemClock.sleep(500L);
		}
	}

	private boolean pttGroupCall() {
		this.logfunc("pttGroupCall");
		if (this.IsPttMode()) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null) {
				ExtendedCall idlePttLine;
				if ((idlePttLine = (ExtendedCall) getCurGrp.oVoid) == null) {
					idlePttLine = this.getIdlePttLine();
				}
				if (idlePttLine != null) {
					getCurGrp.oVoid = idlePttLine;
					this.SetPttMode(getCurGrp.isCreateSession = true);
					return this.groupcall(getCurGrp.grpID, false, idlePttLine, false);
				}
			}
		}
		return false;
	}

	private boolean pttGroupJoin() {
		if (!CallUtil.checkGsmCallInCall()) {
			this.logfunc("pttGroupJoin");
			if (this.IsPttMode()) {
				final PttGrp getCurGrp = this.GetCurGrp();
				if (getCurGrp != null) {
					if (getCurGrp.oVoid != null) {
						if (((ExtendedCall) getCurGrp.oVoid).isGroupCall) {
							MyLog.i(this.tag, "zzhan-debugrejoin-already rejoin.");
							return true;
						}
					} else if (!UserAgent.isTempGrpCallMode) {
						final ExtendedCall idlePttLine = this.getIdlePttLine();
						if (idlePttLine != null) {
							getCurGrp.oVoid = idlePttLine;
							this.SetPttMode(true);
							return this.groupcall(getCurGrp.grpID, false, idlePttLine, true);
						}
					}
				}
			}
		}
		return false;
	}

	private void pttGroupParse(final String s) {
		LogUtil.makeLog(this.tag, "pttGroupParse? pttGroupParse(info)");
		this.pttGrps.ParseGrpInfo(s);
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			if (this.pttGrps.getPttGrps() != null && this.pttGrps.getPttGrps().size() > 0) {
				int n = 0;
				for (final PttGrp pttGrp : this.pttGrps.getPttGrps()) {
					if (pttGrp.getGrpID().equalsIgnoreCase(getCurGrp.getGrpID())) {
						pttGrp.oVoid = getCurGrp.oVoid;
						pttGrp.isCreateSession = getCurGrp.isCreateSession;
						this.pttGrps.SetCurGrp(pttGrp);
						n = 1;
					}
				}
				if (n == 0) {
					if (6 == this.GetPttStatus() || 7 == this.GetPttStatus()) {
						this.pttGroupRelease(true, null);
					}
					this.SetCurGrp(this.pttGrps.FirstGrp(), true);
				}
			} else {
				if (6 == this.GetPttStatus() || 7 == this.GetPttStatus()) {
					this.pttGroupRelease(true, null);
				}
				this.SetCurGrp(this.pttGrps.FirstGrp(), true);
			}
		} else {
			this.SetCurGrp(this.pttGrps.FirstGrp(), true);
		}
		this.mLastGrpIDBeforeMessageGroupChange = "";
		Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua.ui_groupcall.all_groups_change"));
	}

	private boolean pttGroupRelease(final boolean b, ExtendedCall extendedCall) {
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			if (extendedCall == null) {
				extendedCall = (ExtendedCall) getCurGrp.oVoid;
			}
			if (extendedCall != null) {
				this.grouphangupinner(getCurGrp);
			}
			getCurGrp.speakerN = "";
			getCurGrp.speaker = "";
			getCurGrp.isCreateSession = false;
			getCurGrp.oVoid = null;
			MyLog.i(this.tag, "SetPttStatus  PTT_IDLE pttGroupRelease.");
			this.SetPttStatus(5);
			if (b) {
				this.pttReleaseTipSound();
			}
			return true;
		}
		return false;
	}

	private void pttReleaseTipSound() {
		if (this.IsPttMode()) {
			TipSoundPlayer.getInstance().play(TipSoundPlayer.Sound.PTT_RELEASE);
		}
	}

	private void pttTextMessageTipSound() {
		TipSoundPlayer.getInstance().play(TipSoundPlayer.Sound.MESSAGE_ACCEPT);
	}

	private void rejectTmpGrpCallinner() {
		MyLog.i("zdx", "--------rejectTmpGrpCallinner--------");
		this.grouphangupinner(this.curTmpGrp);
		this.curTmpGrp = null;
		UserAgent.isTempGrpCallMode = false;
		TempGroupCallUtil.mCall = null;
	}

	private void removeCall(final ExtendedCall extendedCall) {
		synchronized (this) {
			this.logcall("removeCall", extendedCall);
			this.calls.remove(extendedCall);
		}
	}

	private void restorePreGrp() {
		if (this.preGrpBeforeEmergencyCall.length() <= 0 && this.preGroup.length() <= 0 && this.preGrpBeforeTmpGrpCall.length() <= 0) {
			MyLog.i(this.tag, "restorePreGrp() return ");
			return;
		}
		final PttGrp getCurGrp = this.GetCurGrp();
		MyLog.i(this.tag, "curGrp level = " + getCurGrp.level + "  PRE =" + this.preGrpBeforeEmergencyCall);
		if (getCurGrp != null) {
			if (getCurGrp.level == 0) {
				LogUtil.makeLog(this.tag, "restorePreGrp() 0 == curGrp.level  SetCurGrp()");
				this.setCurGrpinner(this.GetGrpByID(this.preGrpBeforeEmergencyCall), true);
				this.preGrpBeforeEmergencyCall = "";
			}
			if (getCurGrp.level == -1) {
				final PttGrp getGrpByID = this.GetGrpByID(this.preGrpBeforeTmpGrpCall);
				if (!TextUtils.isEmpty((CharSequence) this.preGrpBeforeTmpGrpCall) && getGrpByID != null) {
					this.setCurGrpinner(getGrpByID, true);
				} else {
					MyLog.i("zdx", "Err-restorePreGrp#preGrpBeforeTmpGrpCall = [ " + this.preGrpBeforeTmpGrpCall + " ]");
					MyLog.i("zdx", "Err-restorePreGrp#pttGrps.FirstGrp() = " + this.pttGrps.FirstGrp());
					this.setCurGrpinner(this.pttGrps.FirstGrp(), true);
				}
				this.preGrpBeforeTmpGrpCall = "";
			}
		}
		if (Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getBoolean("restoreAfterOtherGrp", true) && this.automaticAnswer) {
			LogUtil.makeLog(this.tag, "restorePreGrp() isRestore && automaticAnswer SetCurGrp()");
			this.setCurGrpinner(this.GetGrpByID(this.preGroup), true);
			this.preGroup = "";
			this.automaticAnswer = false;
		}
		Receiver.mContext.sendBroadcast(new Intent("com.zed3.sipua_currentgroup_changed"));
		new Exception("--TmpGrpCall unvalued  test----").printStackTrace();
		MyLog.i(this.tag, "SetPttStatus  PTT_IDLE restorePreGrp.");
		this.SetPttStatus(5);
	}

	private void runAutomaticHangup(int delay_time) {
		if (delay_time > 0) {
			try {
				Thread.sleep((long) (delay_time * 1000));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (this.videoCall != null && this.videoCall.isOnCall()) {
			printLog("AUTOMATIC-HANGUP");
			hangupinner();
		}
	}

	private void runCallTransfer(String transfer_to, int delay_time) {
		if (delay_time > 0) {
			try {
				Thread.sleep((long) (delay_time * 1000));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (this.videoCall != null && this.videoCall.isOnCall()) {
			printLog("REFER/TRANSFER");
			this.videoCall.transfer(transfer_to);
		}
	}

	private void runReInvite(String contact, String body, int delay_time) {
		if (delay_time > 0) {
			try {
				Thread.sleep((long) (delay_time * 1000));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		printLog("RE-INVITING/MODIFYING");
		if (this.videoCall != null && this.videoCall.isOnCall()) {
			printLog("REFER/TRANSFER");
			this.videoCall.modify(contact, body);
		}
	}

	private void sendGroupTipBroadcast(final boolean b, final String s) {
		Intent intent;
		if (b) {
			intent = new Intent("com.zed3.sipua.ui_groupcall.group_2_group");
		} else {
			intent = new Intent("com.zed3.sipua.ui_groupcall.single_2_group");
		}
		intent.putExtra("0", s);
		SipUAApp.getAppContext().sendBroadcast(intent);
	}

	private void sendSipCmdMessage(final Object obj) {
		final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
		obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
		obtainMessage.obj = obj;
		this.cmdHandler.sendMessage(obtainMessage);
	}

	private void sessionProduct(SessionDescriptor sdpMediaProduct, final boolean b, final Call call) {
		this.logcall("sessionProduct", (ExtendedCall) call);
		final SessionDescriptor sessionDescriptor = new SessionDescriptor(this.local_session);
		final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(sessionDescriptor.getOrigin(), sessionDescriptor.getSessionName(), sessionDescriptor.getConnection(), sessionDescriptor.getTime());
		sessionDescriptor2.addMediaDescriptors(sessionDescriptor.getMediaDescriptors());
		MyLog.i("hdf336", sdpMediaProduct.toString());
		if (sdpMediaProduct.toString().contains("video")) {
			this.initVideoSocket();
		}
		sdpMediaProduct = SdpTools.sdpMediaProduct(sessionDescriptor2, sdpMediaProduct.getMediaDescriptors(), (ExtendedCall) call);
		if (((ExtendedCall) call).getCallDirection() == 1) {
			final MediaDescriptor mediaDescriptor = sdpMediaProduct.getMediaDescriptor("audio");
			if (mediaDescriptor != null) {
				final String value = String.valueOf(SettingsInfo.ptime);
				MyLog.i("sdk_sdpinfo", "audio callee ptime = " + value);
				mediaDescriptor.addAttribute(new AttributeField("ptime", value));
			}
		}
		MyLog.i("useragent", "sessionProduct ptime:" + sdpMediaProduct);
		this.local_session = sdpMediaProduct.toString();
		if (!b) {
			call.setLocalSessionDescriptor(this.local_session);
		} else {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null && getCurGrp.oVoid != null) {
				((ExtendedCall) getCurGrp.oVoid).setLocalSessionDescriptor(this.local_session);
			}
		}
	}

	private void setCurGrpinner(final PttGrp pttGrp, final boolean b) {
		this.logfunc("setCurGrpinner");
		final String tag = this.tag;
		final StringBuilder sb = new StringBuilder("SetCurGrp(");
		String string;
		if (pttGrp == null) {
			string = "null";
		} else {
			string = pttGrp.toString();
		}
		LogUtil.makeLog(tag, sb.append(string).append(")").toString());
		if (this.pttGrps == null || pttGrp == null) {
			this.SetNullGrp();
			return;
		}
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
			if (extendedCall != null) {
				this.pttGroupRelease(false, extendedCall);
			}
			getCurGrp.speakerN = "";
			getCurGrp.speaker = "";
			getCurGrp.isCreateSession = false;
			getCurGrp.oVoid = null;
		}
		this.pttGrps.SetCurGrp(pttGrp);
		final PttGrp getCurGrp2 = this.GetCurGrp();
		if (b && getCurGrp2.oVoid == null && !getCurGrp2.isCreateSession) {
			if (!UserAgent.isTempGrpCallMode) {
				this.pttGroupJoin();
			}
			if (getCurGrp2.report_heartbeat > 0) {
				this.StartHeartbeat(getCurGrp2.report_heartbeat);
			}
		}
		final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
		intent.putExtra("0", pttGrp.grpID);
		intent.putExtra("1", "");
		pttGrp.speaker = "";
		Receiver.mContext.sendBroadcast(intent);
		Receiver.onText(5, SipUAApp.mContext.getResources().getString(R.string.regok), R.drawable.icon64, 0L);
	}

	private void setMediaProt(final ExtendedCall p0, final boolean p1, final boolean p2) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: getfield        com/zed3/sipua/UserAgent.mSocket:Lcom/zed3/net/SipdroidSocket;
		//     4: ifnonnull       19
		//     7: aload_0
		//     8: new             Lcom/zed3/net/SipdroidSocket;
		//    11: dup
		//    12: iconst_0
		//    13: invokespecial   com/zed3/net/SipdroidSocket.<init>:(I)V
		//    16: putfield        com/zed3/sipua/UserAgent.mSocket:Lcom/zed3/net/SipdroidSocket;
		//    19: iload_2
		//    20: ifeq            81
		//    23: aload_0
		//    24: aload_0
		//    25: getfield        com/zed3/sipua/UserAgent.mSocket:Lcom/zed3/net/SipdroidSocket;
		//    28: invokevirtual   com/zed3/net/SipdroidSocket.getLocalPort:()I
		//    31: invokevirtual   com/zed3/sipua/UserAgent.setAudioProt:(I)V
		//    34: iload_3
		//    35: ifeq            60
		//    38: new             Lorg/zoolu/sdp/SessionDescriptor;
		//    41: dup
		//    42: aload_1
		//    43: invokevirtual   org/zoolu/sip/call/ExtendedCall.getLocalSessionDescriptor:()Ljava/lang/String;
		//    46: invokespecial   org/zoolu/sdp/SessionDescriptor.<init>:(Ljava/lang/String;)V
		//    49: astore          4
		//    51: aload_0
		//    52: aload           4
		//    54: iconst_1
		//    55: aload_1
		//    56: iconst_0
		//    57: invokespecial   com/zed3/sipua/UserAgent.createAnswer:(Lorg/zoolu/sdp/SessionDescriptor;ZLorg/zoolu/sip/call/Call;Z)V
		//    60: return
		//    61: astore          4
		//    63: aload           4
		//    65: invokevirtual   java/net/SocketException.printStackTrace:()V
		//    68: goto            19
		//    71: astore          4
		//    73: aload           4
		//    75: invokevirtual   java/net/UnknownHostException.printStackTrace:()V
		//    78: goto            19
		//    81: aload_0
		//    82: aload_0
		//    83: getfield        com/zed3/sipua/UserAgent.mSocket:Lcom/zed3/net/SipdroidSocket;
		//    86: invokevirtual   com/zed3/net/SipdroidSocket.getLocalPort:()I
		//    89: invokevirtual   com/zed3/sipua/UserAgent.setAudioProt:(I)V
		//    92: iload_3
		//    93: ifeq            60
		//    96: new             Lorg/zoolu/sdp/SessionDescriptor;
		//    99: dup
		//   100: aload_1
		//   101: invokevirtual   org/zoolu/sip/call/ExtendedCall.getLocalSessionDescriptor:()Ljava/lang/String;
		//   104: invokespecial   org/zoolu/sdp/SessionDescriptor.<init>:(Ljava/lang/String;)V
		//   107: astore          4
		//   109: aload_0
		//   110: aload           4
		//   112: iconst_0
		//   113: aload_1
		//   114: aload           4
		//   116: invokevirtual   org/zoolu/sdp/SessionDescriptor.toString:()Ljava/lang/String;
		//   119: ldc_w           "video"
		//   122: invokevirtual   java/lang/String.contains:(Ljava/lang/CharSequence;)Z
		//   125: invokespecial   com/zed3/sipua/UserAgent.createAnswer:(Lorg/zoolu/sdp/SessionDescriptor;ZLorg/zoolu/sip/call/Call;Z)V
		//   128: return
		//   129: astore_1
		//   130: return
		//   131: astore_1
		//   132: return
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  -------------------------------
		//  0      19     61     71     Ljava/net/SocketException;
		//  0      19     71     81     Ljava/net/UnknownHostException;
		//  51     60     131    133    Ljava/lang/Exception;
		//  109    128    129    131    Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0060:
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

	private ExtendedCall setTargetCall(final Call call) {
		this.logcall("setTargetCall", (ExtendedCall) call);
		if (this.isVideoCall(call)) {
			return this.videoCall = (ExtendedCall) call;
		}
		return this.audioCall = (ExtendedCall) call;
	}

	private int speakerMediaApplicationinner(final int n) {
		if (this.audio_app != null) {
			return this.audio_app.speakerMedia(n);
		}
		if (Receiver.GetCurUA().IsPttMode()) {
			final int speakermode = RtpStreamReceiver_group.speakermode;
			RtpStreamReceiver_group.speakermode = n;
			return speakermode;
		}
		final int speakermode2 = RtpStreamReceiver_signal.speakermode;
		RtpStreamReceiver_signal.speakermode = n;
		return speakermode2;
	}

	private boolean startMediaApplication(final ExtendedCall extendedCall, final int n) {
		boolean b = true;
		this.logcall("startMediaApplication(ExtendedCall,int)", extendedCall);
		MyLog.d("videoTrace", "UserAgent#startMediaApplication() enter audio_app = " + this.audio_app);
		final boolean launchMediaApplication = this.launchMediaApplication(extendedCall, n);
		MyLog.d("videoTrace", "UserAgent#startMediaApplication() enter launch media reuslt = " + launchMediaApplication);
		if (!launchMediaApplication) {
			b = false;
		} else if (this.audio_app != null) {
			if (n == -1 || n == 1 || n == -2) {
				this.pttSpeakerControl();
			} else if (!Build.MODEL.toLowerCase().contains("g716-l070") && extendedCall.getCallTypeEx() != 1) {
				this.audio_app.speakerMedia(2);
			}
			this.audio_app.startMedia(extendedCall, n);
			return true;
		}
		return b;
	}

	private void stopMediaApplication() {
		if (6 == this.GetPttStatus()) {
			this.startMediaApplication((ExtendedCall) this.GetCurGrp().oVoid, -2);
		}
	}

	private boolean tempGroupCall(final String s, final boolean b, final ExtendedCall extendedCall, final boolean b2, final String s2, final ArrayList<String> list) {
		this.logcall("tempGroupCall", extendedCall);
		if (this.GetPttStatus() == 9) {
			return false;
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); ++i) {
			sb.append(list.get(i));
			if (i != list.size() - 1) {
				sb.append(",");
			}
		}
		return this.call(s, b, true, extendedCall, b2, s2, sb.toString(), false);
	}

	public void GPSCloseLock() {
		synchronized (this) {
			MyLog.d("testgps", "UserAgent#GPSCloseLock enter");
			if (Tools.isInMainThread()) {
				if (UserAgent.gpsPacket != null) {
					MyLog.i("GPSSend", "userAgent GPSCloseLock ");
					MyLog.d("testgps", "UserAgent#GPSCloseLock enter gpsPacket:" + UserAgent.gpsPacket);
					UserAgent.gpsPacket.ExitGPS(true);
					UserAgent.gpsPacket = null;
				}
			} else {
				final Intent intent = new Intent(SipUAApp.mContext, (Class) RegisterService.class);
				intent.putExtra("hasgps", true);
				intent.putExtra("gpsopen", false);
				SipUAApp.mContext.startService(intent);
			}
		}
	}

	public void GPSOpenLock() {
		MyLog.d("testgps", "UserAgent#GPSOpenLock is openGps = " + this.isOpenGps());
		this.openGps();
	}

	public void Get3GTotalFromServer() {
		String s2;
		final String s = s2 = this.user_profile.username;
		if (s.indexOf("@") < 0) {
			String string = s;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s;
			}
			s2 = String.valueOf(string) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s2), new NameAddress(this.user_profile.from_url), null);
		request.setHeader(new Header("Ptt-Extension", "3ghandset getdatastatistics"));
		new TransactionClient(this.sip_provider, request, this).request();
	}

	public PttGrps GetAllGrps() {
		return this.pttGrps;
	}

	public GrpCallSetupType GetCrpCallConfigOfHigh() {
		return this.grpCallSetupHigh;
	}

	public GrpCallSetupType GetCrpCallConfigOfLow() {
		return this.grpCallSetupLow;
	}

	public GrpCallSetupType GetCrpCallConfigOfSame() {
		return this.grpCallSetupSame;
	}

	public PttGrp GetCurGrp() {
		return this.pttGrps.GetCurGrp();
	}

	public String GetCurrentMouth(final boolean b) {
		final Date date = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (b) {
			return simpleDateFormat.format(date).substring(0, 7);
		}
		return simpleDateFormat.format(date);
	}

	public PttGrp GetGrpByID(final String s) {
		return this.pttGrps.GetGrpByID(s);
	}

	public int GetPttStatusForLine() {
		return this.ua_ptt_state;
	}

	public void HaltGroupCall() {
		this.handleCallStatus(new CallStatusPara(CallStatus.CALL_HALT_GROUP));
	}

	public boolean IsPttMode() {
		return UserAgent.ua_ptt_mode;
	}

	public void JoinTmpGrpCall(final PttGrp para2, final String para3) {
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_JOIN_TMP_CALL);
		callStatusPara.setPara1(para3);
		callStatusPara.setPara2(para2);
		this.handleCallStatus(callStatusPara);
	}

	public void NetFlowPreferenceEdit(final String s, final String s2, final String s3, final String s4) {
		final SharedPreferences.Editor edit = Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putString("User_3GDBLocalTotal", s);
		edit.putString("User_3GDBLocalTotalPTT", s2);
		edit.putString("User_3GDBLocalTotalVideo", s3);
		edit.putString("User_3GDBLocalTime", s4);
		edit.commit();
	}

	public boolean OnPttKey(final boolean b, final PttPRMode pttPressReleaseMode) {
		this.logfunc("OnPttKey");
		this.pttPressReleaseMode = pttPressReleaseMode;
		if (this.GetPttMode() && this.ptt_key_down != b && this.GetCurGrp() != null) {
			final boolean b2 = false;
			if (b) {
				boolean b3 = b2;
				if (SystemClock.uptimeMillis() - this.intervalDown >= 500L) {
					if (this.cmdHandler != null) {
						this.ptt_key_down = b;
						final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
						obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD.ordinal();
						obtainMessage.arg2 = 1;
						this.cmdHandler.sendMessage(obtainMessage);
						b3 = true;
					} else {
						MyLog.e(this.tag, "OnPttKey() cmdHandler is null");
						b3 = b2;
					}
				}
				this.intervalDown = SystemClock.uptimeMillis();
				return b3;
			}
			if (this.ptt_key_down) {
				if (this.cmdHandler != null) {
					this.ptt_key_down = b;
					final android.os.Message obtainMessage2 = this.cmdHandler.obtainMessage();
					obtainMessage2.arg1 = ProcessCmdType.PROCESS_TYPE_PTT_KEY_CMD.ordinal();
					obtainMessage2.arg2 = 0;
					this.cmdHandler.sendMessage(obtainMessage2);
					return true;
				}
				MyLog.e(this.tag, "OnPttKey() cmdHandler is null");
				return false;
			}
		}
		return false;
	}

	public void OnRegisterFailure() {
		if (9 != this.GetPttStatus()) {
			MyLog.e(this.tag, "SetPttStatus  PTT_UNREG OnRegisterFailure.");
			this.SetPttStatus(9);
		}
	}

	public void OnRegisterSuccess(final String para1) {
		LogUtil.makeLog(this.tag, "pttGroupParse? OnRegisterSuccess()");
		if (9 == this.GetPttStatus()) {
			MyLog.i("UserAgent", "OnRegisterSuccess run");
			MyLog.i(this.tag, "SetPttStatus  PTT_IDLE OnRegisterSuccess.");
			this.SetPttStatus(5);
			this.pttGroupParse(para1);
			CustomGroupManager.getInstance().sendCustomGroupMessage(this.sip_provider, this.user_profile, null, null, 6, null, null, null);
			DataBaseService.getInstance().getAlversion();
			AddressBookUtils.ISREQUEST = false;
			AddressBookUtils.getNewAddressBook2();
			AddressBookUtils.getMsgList();
			final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
			extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_REGISTER_SUCCESS;
			extendedSipCallbackPara.para1 = para1;
			this.sendSipCmdMessage(extendedSipCallbackPara);
		}
		if (this.sip_provider != null && Receiver.mSipdroidEngine.isRegistered()) {
			this.sip_provider.setHeartBeatListner(this);
		}
		MyHeartBeatReceiver.start("UserAgent#OnRegisterSuccess");
	}

	public void PttGetGroupList(final String s) {
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(string), new NameAddress(this.user_profile.from_url), null);
		request.setHeader(new Header("Ptt-Extension", "3ghandset getstatus"));
		new TransactionClient(this.sip_provider, request, this).request();
	}

	public String SendCustomGroupMessage(final int n, final String s, final String s2, final String s3, final String s4, final String s5) {
		LogUtil.makeLog(this.tag, "SendCustomGroupMessage()#type = " + n);
		return CustomGroupManager.getInstance().sendCustomGroupMessage(this.sip_provider, this.user_profile, s3, s4, n, s, s2, s5);
	}

	public String SendGroupTextMessage(final ArrayList<GroupMessage> list, final String s, final String s2) {
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "MESSAGE", new NameAddress(this.user_profile.from_url), new NameAddress(this.user_profile.from_url), s);
		request.setHeader(new Header("Content-Type", "text/plain"));
		request.setHeader(new Header("Anta-Extension", this.getGroupMessageFormat(list)));
		request.setHeader(new Header("EnhanceSMS-ID", s2));
		request.setHeader(new Header("EnhanceSMS-Attribute", "65532"));
		request.setHeader(new Header("EnhanceSMS-Type", "Normal"));
		final String value = String.valueOf(request.getCallIdHeader().getCallId());
		new TransactionClient(this.sip_provider, request, this).request();
		return value;
	}

	public String SendTextMessage(final String s, String value) {
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "MESSAGE", new NameAddress(string), new NameAddress(this.user_profile.from_url), value);
		request.setHeader(new Header("Content-Type", "text/plain"));
		value = String.valueOf(request.getCallIdHeader().getCallId());
		new TransactionClient(this.sip_provider, request, this).request();
		return value;
	}

	public String SendTextMessage(final String s, String value, final String s2) {
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "MESSAGE", new NameAddress(string), new NameAddress(this.user_profile.from_url), value);
		request.setHeader(new Header("Content-Type", "text/plain"));
		request.setHeader(new Header("EnhanceSMS-ID", s2));
		request.setHeader(new Header("EnhanceSMS-Attribute", "65532"));
		request.setHeader(new Header("EnhanceSMS-Type", "Normal"));
		value = String.valueOf(request.getCallIdHeader().getCallId());
		new TransactionClient(this.sip_provider, request, this).request();
		return value;
	}

	public void SetCurGrp(final PttGrp pttGrp) {
		final String tag = this.tag;
		final StringBuilder sb = new StringBuilder("SetCurGrp(");
		String string;
		if (pttGrp == null) {
			string = "null";
		} else {
			string = pttGrp.toString();
		}
		LogUtil.makeLog(tag, sb.append(string).append(")").toString());
		if (this.pttGrps == null || pttGrp == null) {
			return;
		}
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null && getCurGrp.isCreateSession) {
			final ExtendedCall extendedCall = (ExtendedCall) getCurGrp.oVoid;
			if (extendedCall != null && (6 == this.GetPttStatus() || 7 == this.GetPttStatus())) {
				this.pttGroupRelease(false, extendedCall);
			}
			getCurGrp.speakerN = "";
			getCurGrp.speaker = "";
			getCurGrp.isCreateSession = false;
			getCurGrp.oVoid = null;
		}
		this.pttGrps.SetCurGrp(pttGrp);
		final PttGrp getCurGrp2 = this.GetCurGrp();
		if (getCurGrp2.oVoid == null && !getCurGrp2.isCreateSession) {
			this.pttGroupJoin();
			if (getCurGrp2.report_heartbeat > 0) {
				this.StartHeartbeat(getCurGrp2.report_heartbeat);
			}
		}
		final Intent intent = new Intent("com.zed3.sipua.ui_groupcall.group_status");
		intent.putExtra("0", pttGrp.grpID);
		intent.putExtra("1", "");
		pttGrp.speaker = "";
		Receiver.mContext.sendBroadcast(intent);
		Receiver.onText(5, SipUAApp.mContext.getResources().getString(R.string.regok), R.drawable.icon64, 0L);
	}

	public void SetCurGrp(final PttGrp para1, final boolean b) {
		if (para1 != null) {
			this.logfunc("SetCurGrp " + para1.grpName + " rejoin :" + b);
		} else {
			this.logfunc("SetCurGrp null grp rejoin :" + b);
		}
		new Exception("---print trace----").printStackTrace();
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_SET_GROUP);
		callStatusPara.setPara1(para1);
		callStatusPara.setPara2(b);
		this.handleCallStatus(callStatusPara);
	}

	public void SetGrpCallConfig(final GrpCallSetupType grpCallSetupHigh, final GrpCallSetupType grpCallSetupSame, final GrpCallSetupType grpCallSetupLow) {
		this.grpCallSetupHigh = grpCallSetupHigh;
		this.grpCallSetupSame = grpCallSetupSame;
		this.grpCallSetupLow = grpCallSetupLow;
	}

	public void Upload3GTotal(final String s, final String s2, final String s3) {
		String s5;
		final String s4 = s5 = this.user_profile.username;
		if (s4.indexOf("@") < 0) {
			String string = s4;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s4;
			}
			s5 = String.valueOf(string) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s5), new NameAddress(this.user_profile.from_url), null);
		request.setHeader(new Header("Ptt-Extension", "3ghandset reportdatastatistics"));
		request.setBody("datastatistics:" + s + "\r\n" + "pttstatistics:" + s2 + "\r\n" + "videostatistics:" + s3 + "\r\n" + "time:" + this.GetCurrentMouth(false) + "\r\n");
		new TransactionClient(this.sip_provider, request, this).request();
	}

	public void UploadUnionPwd(final String s) {
		String s3;
		final String s2 = s3 = this.user_profile.username;
		if (s2.indexOf("@") < 0) {
			String string = s2;
			if (this.user_profile.realm.equals("")) {
				string = "&" + s2;
			}
			s3 = String.valueOf(string) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(s3), new NameAddress(this.user_profile.from_url), null);
		request.setHeader(new Header("Ptt-Extension", "3ghandset auth " + s));
		new TransactionClient(this.sip_provider, request, this).request();
	}

	public boolean accept() {
		this.logfunc("accept()");
		this.handleCallStatus(new CallStatusPara(CallStatus.CALL_ACCEPT));
		return true;
	}

	public void addCustomGroupLength() {
		this.pttGrps.setCustomGroupLength(this.pttGrps.getCustomGroupLength() + 1);
	}

	public void answerGroupCall(final PttGrp para1) {
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.ACCEPT_GROUP_CALL);
		callStatusPara.setPara1(para1);
		this.handleCallStatus(callStatusPara);
	}

	public void answerTmpGrpCall() {
		MyLog.i("zdx", "--------answerTmpGrpCall--------");
		this.answerGroupCall(this.curTmpGrp);
	}

	public boolean antaCall2(final String s, final String s2, final boolean b, final boolean b2) {
		this.logfunc("antaCall2(String,String,boolean, boolean)");
		if (this.GetPttStatus() == 9) {
			return false;
		}
		MemoryMg.getInstance().isSendOnly = false;
		if (!this.isIdleOfPttLines()) {
			final PttGrp getCurGrp = this.GetCurGrp();
			if (getCurGrp != null) {
				this.grouphangupinner(getCurGrp);
			}
		}
		return this.antaCall3(s, s2, b, b2, null);
	}

	void automaticHangup(final int n) {
		new Thread() {
			@Override
			public void run() {
				UserAgent.this.runAutomaticHangup(n);
			}
		}.start();
	}

	public void bluetoothMediaApplication() {
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_MEDIA);
		callStatusPara.setPara1(Integer.getInteger("2"));
		this.handleCallStatus(callStatusPara);
	}

	public boolean call(final String s, final boolean b, final boolean b2) {
		return this.call(s, b, b2, null);
	}

	public boolean call(final String para3, final boolean b, final boolean b2, final String para4) {
		this.logfunc("call(String,boolean,boolean)");
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_SINGLE_CALL);
		callStatusPara.setPara1(b);
		callStatusPara.setPara2(b2);
		callStatusPara.setPara3(para3);
		callStatusPara.setPara4(para4);
		this.handleCallStatus(callStatusPara);
		return true;
	}

	void callTransfer(String string, final int n) {
		if (string.indexOf("@") < 0) {
			string = String.valueOf(string) + "@" + this.realm;
		}
		final String finalString = string;
		new Thread() {
			@Override
			public void run() {
//				UserAgent.this.runCallTransfer(finalString, n);
			}
		}.start();
	}

	protected void changeStatus(final int n) {
		this.changeStatus(n, null);
	}

	protected void changeStatus(final int call_state, final String s) {
		synchronized (this) {
			Receiver.onState(this.call_state = call_state, s);
		}
	}

	protected void closeMediaApplication(final String s, final boolean b, final boolean b2) {
		synchronized (this) {
			this.logfunc("closeMediaApplication(String)");
			MyLog.d("videoTrace", "UserAgent#closeMediaApplication() enter audio_app = " + this.audio_app);
			if (!TextUtils.isEmpty((CharSequence) s)) {
				if (this.audio_app != null && s.equals(this.audio_app.callId) && !b) {
					this.audio_app.stopMedia();
					this.audio_app = null;
					MyLog.i("UserAgent", "closeMediaApplication.");
					if (this.mSocket != null) {
						this.mSocket.close();
						this.mSocket = null;
					}
				}
				if (!b2 && this.mVedioSocket != null) {
					android.util.Log.i("GUOK", "closeVideo:" + b2);
					this.mVedioSocket.close();
					this.mVedioSocket = null;
				}
			}
		}
	}

	public ExtendedCall getAbortCall() {
		return this.mAbortCall;
	}

	public Map<String, PttCustomGrp> getAllCustomGroups() {
		return this.pttGrps.getCustomGrpMap();
	}

	public ExtendedCall getAudioCall() {
		return this.audioCall;
	}

	String getCurGrpState() {
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp == null) {
			return "";
		}
		if (getCurGrp.oVoid == null || this.audio_app == null) {
			return "OFF";
		}
		return "ON";
	}

	public PttPRMode getCurPttPRMode() {
		return this.pttPressReleaseMode;
	}

	public Map<String, String> getCustomGroupMap() {
		return this.pttGrps.getMap();
	}

	public int getCustomGrpBySelfNum() {
		int n = 0;
		final Iterator<Map.Entry<String, PttCustomGrp>> iterator = this.getAllCustomGroups().entrySet().iterator();
		while (iterator.hasNext()) {
			final PttCustomGrp pttCustomGrp = iterator.next().getValue();
			if (pttCustomGrp != null && pttCustomGrp.getGroupCreatorNum().equals(Settings.getUserName())) {
				++n;
			}
		}
		return n;
	}

	public GPSPacket getGpsPacket() {
		return UserAgent.gpsPacket;
	}

	public String getGroupMessageFormat(final ArrayList<GroupMessage> list) {
		if (list != null && list.size() > 0) {
			final StringBuffer sb = new StringBuffer();
			sb.append("groupSMS;");
			for (int i = 0; i < list.size(); ++i) {
				final GroupMessage groupMessage = list.get(i);
				switch (groupMessage.getType()) {
					case 0: {
						sb.append("members=");
						final Iterator<String> iterator = groupMessage.getNumbers().iterator();
						while (iterator.hasNext()) {
							sb.append((Object) iterator.next());
							sb.append(",");
						}
						if (sb.lastIndexOf(",") > 0) {
							sb.delete(sb.lastIndexOf(","), sb.length());
							sb.append(";");
							break;
						}
						break;
					}
				}
			}
			return sb.toString();
		}
		return "";
	}

	public PttGrp getGrpById(final String s) {
		if (TextUtils.isEmpty((CharSequence) s)) {
			return null;
		}
		if (this.pttGrps != null) {
			for (final PttGrp pttGrp : this.pttGrps.getPttGrps()) {
				if (s.equalsIgnoreCase(pttGrp.grpID)) {
					return pttGrp;
				}
			}
		}
		return null;
	}

	public Vector<PttGrp> getPttGrps() {
		return this.pttGrps.getPttGrps();
	}

	public SipdroidSocket getVedioSocket() {
		if (this.mVedioSocket != null) {
			return this.mVedioSocket;
		}
		return null;
	}

	public ExtendedCall getVideoCall() {
		return this.videoCall;
	}

	public void grouphangup(final PttGrp para1) {
		this.logfunc("grouphangup(PttGrp)");
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_HANGUP_GROUP);
		callStatusPara.setPara1(para1);
		this.handleCallStatus(callStatusPara);
	}

	public void haltListen() {
		this.logfunc("haltListen");
		this.haltListen(true);
	}

	public void haltListenNotCloseGps() {
		this.logfunc("haltListenNotCloseGps");
		this.haltListen(false);
	}

	public void hangup() {
		this.logfunc("hangup()");
		this.handleCallStatus(new CallStatusPara(CallStatus.CALL_HANGUP));
	}

	public void hangupTmpGrpCall(final boolean flag) {
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_TMPGRP_HANGUP_LINE;
		extendedSipCallbackPara.para2 = TempGroupCallUtil.mCall;
		extendedSipCallbackPara.flag = flag;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	public void hangupWithoutRejoin() {
		this.logfunc("hangupWithoutRejoin()");
		this.handleCallStatus(new CallStatusPara(CallStatus.CALL_HANGUP_WITHOUT_REJOIN));
	}

	public void info(final char c, final int n) {
		boolean b;
		if (this.audio_app != null && this.audio_app.sendDTMF(c)) {
			b = true;
		} else {
			b = false;
		}
		if (!b && this.videoCall != null) {
			this.videoCall.info(c, n);
		}
	}

	public boolean isOpenGps() {
		return UserAgent.gpsPacket != null;
	}

	protected boolean launchMediaApplication(final ExtendedCall extendedCall, final int n) {
		this.logcall("launchMediaApplication(ExtendedCall,int)", extendedCall);
		MyLog.d("videoTrace", "UserAgent#launchMediaApplication() enter audio_app = " + this.audio_app);
		final VideoManagerService default1 = VideoManagerService.getDefault();
		if (this.audio_app != null && this.audio_app.callId.equals(extendedCall.getExtCallId())) {
			this.printLog("DEBUG: media application is already running", 1);
			MyLog.d("videoTrace", "UserAgent#launchMediaApplication() return");
			return true;
		}
		if (this.audio_app != null && !this.audio_app.callId.equals(extendedCall.getExtCallId()) && (default1.isEmptyVideoAction() || default1.isCurrentVideoCall())) {
			this.closeMediaApplication(this.audio_app.callId, false, false);
		}
		final SessionDescriptor sessionDescriptor = new SessionDescriptor(extendedCall.getLocalSessionDescriptor());
		int n2 = 0;
		this.local_video_port = 0;
		final boolean b = false;
		final Codecs.Map codec = Codecs.getCodec(sessionDescriptor);
		AudioSettings.startTempStamp = 0L;
		boolean b2 = false;
		final MediaDescriptor mediaDescriptor = sessionDescriptor.getMediaDescriptor("video");
		if (mediaDescriptor != null) {
			b2 = true;
			this.local_video_port = mediaDescriptor.getMedia().getPort();
			final Iterator<AttributeField> iterator = mediaDescriptor.getAttributes().iterator();
			while (iterator.hasNext()) {
				final String value = iterator.next().getValue();
				if (value.contains("rtpmap")) {
					MyLog.i("camera_PayLoadType", UserAgent.camera_PayLoadType = value.substring(7, value.lastIndexOf(" ")));
				}
			}
		}
		final MediaDescriptor mediaDescriptor2 = sessionDescriptor.getMediaDescriptor("audio");
		int dtmf_avp = b ? 1 : 0;
		if (mediaDescriptor2 != null) {
			final int port = mediaDescriptor2.getMedia().getPort();
			dtmf_avp = (b ? 1 : 0);
			n2 = port;
			if (mediaDescriptor2.getMedia().getFormatList().contains(String.valueOf(this.user_profile.dtmf_avp))) {
				dtmf_avp = this.user_profile.dtmf_avp;
				n2 = port;
			}
		}
		final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(extendedCall.getRemoteSessionDescriptor());
		this.remote_media_address = new Parser(sessionDescriptor2.getConnection().toString()).skipString().skipString().getString();
		int n3 = 0;
		this.remote_video_port = 0;
		final Enumeration<MediaDescriptor> elements = sessionDescriptor2.getMediaDescriptors().elements();
		while (elements.hasMoreElements()) {
			final MediaField media = elements.nextElement().getMedia();
			int port2 = n3;
			if (media.getMedia().equals("audio")) {
				port2 = media.getPort();
			}
			n3 = port2;
			if (media.getMedia().equals("video")) {
				this.remote_video_port = media.getPort();
				n3 = port2;
			}
		}
		if (b2) {
			this.Camera_URL = this.remote_media_address;
			this.Camera_AudioPort = new StringBuilder(String.valueOf(n3)).toString();
			this.Camera_VideoPort = new StringBuilder(String.valueOf(this.remote_video_port)).toString();
			AudioSettings.startTempStamp = System.currentTimeMillis();
		}
		MyLog.d("videoTrace", "UserAgent#launchMediaApplication() camera video port = " + this.Camera_VideoPort);
		MyLog.d("videoTrace", "UserAgent#launchMediaApplication() camera video url = " + this.Camera_URL);
		MyLog.d("pttTrace", "UserAgent#launchMediaApplication() init prepare ");
		if (this.user_profile.audio && n2 != 0) {
			MyLog.d("pttTrace", "UserAgent#launchMediaApplication() init start ");
			if (this.audio_app == null) {
				String send_file = null;
				if (this.user_profile.send_tone) {
					send_file = "TONE";
				} else if (this.user_profile.send_file != null) {
					send_file = this.user_profile.send_file;
				}
				String recv_file = null;
				if (this.user_profile.recv_file != null) {
					recv_file = this.user_profile.recv_file;
				}
				MyLog.i("88888888", "url:" + this.Camera_URL + "audio:" + this.Camera_AudioPort + "video:" + this.Camera_VideoPort);
				final boolean interceptStartMediaApplication = this.interceptStartMediaApplication(extendedCall);
				MyLog.d("videoTrace", "UserAgent#launchMediaApplication() intercept result = " + interceptStartMediaApplication);
				if (interceptStartMediaApplication) {
					return false;
				}
				if (n != 0) {
					MyLog.d("videoTrace", "UserAgent#launchMediaApplication() create JAudioLauncher");
					(this.audio_app = new JAudioLauncher(this.mSocket, this.remote_media_address, n3, n, send_file, recv_file, codec.codec.samp_rate(), this.user_profile.audio_sample_size, extendedCall.getCallPtime() / 20 * codec.codec.frame_size(), this.log, codec, dtmf_avp, extendedCall.getExtCallId(), extendedCall.getCallPtime(), false)).setUserAgentHandler(this.cmdHandler);
				} else {
					MyLog.d("videoTrace", "UserAgent#launchMediaApplication() create JAudioLauncher");
					this.audio_app = new JAudioLauncher(this.mSocket, this.remote_media_address, n3, n, send_file, recv_file, codec.codec.samp_rate(), this.user_profile.audio_sample_size, extendedCall.getCallPtime() / 20 * codec.codec.frame_size(), this.log, codec, dtmf_avp, extendedCall.getExtCallId(), extendedCall.getCallPtime(), b2);
				}
			}
		}
		return true;
	}

	public boolean listen() {
		this.logfunc("listen");
		if (this.calls == null) {
			this.calls = new Vector<ExtendedCall>();
			for (int i = 0; i < 4; ++i) {
				final ExtendedCall extendedCall = new ExtendedCall(this.sip_provider, this.user_profile.from_url, this.user_profile.contact_url, this.user_profile.username, this.user_profile.realm, this.user_profile.passwd, this);
				extendedCall.listen();
				this.calls.add(extendedCall);
			}
			if (this.cmdProcThread == null) {
				this.sip_provider.addSipProviderListener(new TransactionIdentifier("MESSAGE"), this);
				this.sip_provider.addSipProviderListener(new TransactionIdentifier("INFO"), this);
				this.sip_provider.addSipProviderListener(new TransactionIdentifier("NOTIFY"), this);
				this.sip_provider.addSipProviderListener(new TransactionIdentifier("OPTIONS"), this);
				(this.cmdProcThread = new Thread(this)).setName("cmdProcThread");
				this.cmdProcThread.start();
				return true;
			}
		}
		return true;
	}

	public void makeTempGrpCall(final String para3, final String para4, final ArrayList<String> list) {
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_TMP_CALL);
		callStatusPara.setPara1(para4);
		callStatusPara.setPara2(list.clone());
		callStatusPara.setPara3(para3);
		this.handleCallStatus(callStatusPara);
	}

	public boolean muteMediaApplication() {
		return this.muteMediaApplicationinner();
	}

	@Override
	public void onCallAccepted(final Call para2, final String para3, final Message para4) {
		this.logcall("onCallAccepted", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLACCEPTED;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		extendedSipCallbackPara.para4 = para4;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallCanceling(final Call call, final Message message) {
		this.logcall("onCallCanceling", (ExtendedCall) call);
		MyLog.d("videoTrace", "UserAgent#onCallCanceling() enter");
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_CANCELING);
		callStatusPara.setCall(call);
		callStatusPara.setMessage(message);
		this.handleCallStatus(callStatusPara);
	}

	@Override
	public void onCallClosed(final Call para2, final Message para3) {
		this.logcall("onCallClosed", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLCLOSED;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallClosing(final Call para2, final Message para3) {
		this.logcall("onCallClosing", (ExtendedCall) para2);
		if (VideoManagerService.getDefault().isCurrentVideoTRANSCRIBE() && ((ExtendedCall) para2).callType.equals(CallManager.CallType.VIDEO)) {
			VideoManagerService.bye = true;
		}
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLCLOSING;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallConfirmed(final Call call, final String s, final Message message) {
		this.printLog("onCallConfirmed()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("CONFIRMED/CALL", 1);
			if (!this.IsPttMode() && this.user_profile.hangup_time > 0) {
				this.automaticHangup(this.user_profile.hangup_time);
			}
		}
	}

	@Override
	public void onCallIncoming(final Call para2, final NameAddress para3, final NameAddress para4, final String para5, final Message para6) {
		this.logcall("onCallIncoming", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLINCOMING;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		extendedSipCallbackPara.para4 = para4;
		extendedSipCallbackPara.para5 = para5;
		extendedSipCallbackPara.para6 = para6;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallModifying(final Call call, final String s, final Message message) {
		this.printLog("onCallModifying()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("RE-INVITE/MODIFY", 1);
		super.onCallModifying(call, s, message);
	}

	@Override
	public void onCallReInviteAccepted(final Call call, final String s, final Message message) {
		this.printLog("onCallReInviteAccepted()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("RE-INVITE-ACCEPTED/CALL", 1);
			if (!this.IsPttMode()) {
				if (this.statusIs(4)) {
					this.changeStatus(3);
					return;
				}
				this.changeStatus(4);
			}
		}
	}

	@Override
	public void onCallReInviteRefused(final Call call, final String s, final Message message) {
		this.printLog("onCallReInviteRefused()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("RE-INVITE-REFUSED (" + s + ")/CALL", 1);
	}

	@Override
	public void onCallRedirection(final Call call, final String s, final Vector<String> vector, final Message message) {
		this.printLog("onCallRedirection()", 5);
		if (!this.isInCalls((ExtendedCall) call)) {
			this.printLog("NOT the current call", 5);
		} else {
			this.printLog("REDIRECTION (" + s + ")", 1);
			if (!this.IsPttMode()) {
				call.call(vector.elementAt(0));
			}
		}
	}

	@Override
	public void onCallRefused(final Call para2, final String para3, final Message para4) {
		this.logcall("onCallRefused", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLREFUSED;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		extendedSipCallbackPara.para4 = para4;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	public void onCallRefusedinner(final Call para2, final String s, final Message message) {
		this.logcall("onCallRefusedinner", (ExtendedCall) para2);
		MyLog.d("videoTrace", "UserAgent#onCallRefused() enter");
		this.printLog("onCallRefused()", 5);
		if (!this.isInCalls((ExtendedCall) para2)) {
			this.printLog("NOT the current call", 5);
		} else if (message.getStatusLine().getCode() != 487) {
			this.printLog("REFUSED (" + s + ")", 1);
			if (s.equalsIgnoreCase("not acceptable here")) {
				Receiver.call_end_reason = R.string.card_title_ended_no_codec;
			}
			if (message.hasPttExtensionHeader() || ((ExtendedCall) para2).getCallTypeEx() == 2) {
				this.cmdHandler.obtainMessage().arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
				final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
				extendedSipCallbackPara.para2 = para2;
				if (message.getStatusLine().getCode() == 403) {
					this.process_TYPE_LOCAL_HANGUP_LINE_or_TYPE_PEER_HANGUP_LINEinner(extendedSipCallbackPara);
				} else if (message.getStatusLine().getCode() >= 400 && message.getStatusLine().getCode() < 500) {
					this.process_TYPE_REQUEST_REJECT_LINE_inner(extendedSipCallbackPara);
				}
			} else {
				if (!this.statusIs(0)) {
					if (message.toString().contains("403 Forbidden")) {
						MyLog.i(this.tag, "No videoCall 403 Forbidden");
						this.beatHandler.sendEmptyMessage(2);
					}
					MyLog.i(this.tag, "closeMediaApplication onCallRefused." + message.getStatusLine().getCode());
					final CallManager manager = CallManager.getManager();
					final VideoManagerService default1 = VideoManagerService.getDefault();
					final boolean videoCall = manager.isVideoCall(para2);
					final boolean audioCall = manager.isAudioCall(para2);
					boolean b = false;
					if (videoCall && default1.isCurrentVideoCall()) {
						b = true;
					} else if (audioCall) {
						b = true;
					}
					if (message.getStatusLine().getCode() == 480) {
						SipUAApp.mContext.sendBroadcast(new Intent("com.zed3.flow.yaobi_prompt"));
					}
					MyLog.d("videoTrace", "UserAgent#onCallRefused() closeMediaApplication result = " + b);
					this.closeMediaApplication(((ExtendedCall) para2).getExtCallId(), !b, false);
					CallManager.getManager().setCallState(CallManager.CallState.IDLE, para2);
					this.prepareAbortCall(para2);
					this.changeStatus(0);
					this.abortCallCompleted(para2);
					if (para2 == this.call_transfer) {
						final int code = message.getStatusLine().getCode();
						if (this.isVideoCall(para2)) {
							this.videoCall.notify(code, s);
						} else {
							this.audioCall.notify(code, s);
						}
						this.call_transfer = null;
					}
				}
				if (!this.IsPttMode()) {
					MyLog.d("pttTrace", "UsrAgent#onCallRefused() enter SetPttMode(@param true) ");
					this.SetPttMode(true);
					MyLog.i("cysx_test", "call refused set TRUE");
					this.pttGroupJoin();
				} else if (UserAgent.isTempGrpCallMode && ((ExtendedCall) para2).getDialog().getInviteMessage().hasPttExtensionHeader() && ((ExtendedCall) para2).getDialog().getInviteMessage().getPttExtensionHeader().getValue().contains("3ghandset tmp")) {
					RtpStreamReceiver_signal.ringback(false);
					this.hangupTmpGrpCallinner(true);
					final Intent intent = new Intent("com.zed3.sipua.tmpgrp.closing");
					intent.putExtra("resused", true);
					SipUAApp.mContext.sendBroadcast(intent);
				}
			}
			MyLog.d("pttTrace", "UsrAgent#onCallRefused() enter IsPttMode() = " + this.IsPttMode());
			RtpStreamReceiver_signal.ringback(false);
			if (!this.IsPttMode()) {
				MyLog.d("pttTrace", "UsrAgent#onCallRefused() enter SetPttMode(@param true) ");
				this.SetPttMode(true);
				this.pttGroupJoin();
			}
			this.removeCall((ExtendedCall) para2);
			this.addCall();
		}
	}

	@Override
	public void onCallRinging(final Call para2, final Message para3) {
		this.logcall("onCallRinging", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLRINGING;
		extendedSipCallbackPara.para2 = para2;
		extendedSipCallbackPara.para3 = para3;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallTimeout(final Call para2) {
		this.logcall("onCallTimeout", (ExtendedCall) para2);
		final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
		extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_ONCALLTIMEOUT;
		extendedSipCallbackPara.para2 = para2;
		this.sendSipCmdMessage(extendedSipCallbackPara);
	}

	@Override
	public void onCallTransfer(final ExtendedCall extendedCall, final NameAddress nameAddress, final NameAddress nameAddress2, final Message message) {
		this.printLog("onCallTransfer()", 5);
		if (!this.isInCalls(extendedCall) || this.IsPttMode()) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("Transfer to " + nameAddress.toString(), 1);
		extendedCall.acceptTransfer();
		(this.call_transfer = new ExtendedCall(this.sip_provider, this.user_profile.from_url, this.user_profile.contact_url, this)).call(nameAddress.toString(), this.local_session, null);
	}

	@Override
	public void onCallTransferAccepted(final ExtendedCall extendedCall, final Message message) {
		this.printLog("onCallTransferAccepted()", 5);
		if (!this.isInCalls(extendedCall) || this.IsPttMode()) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("Transfer accepted", 1);
	}

	@Override
	public void onCallTransferFailure(final ExtendedCall extendedCall, final String s, final Message message) {
		this.printLog("onCallTransferFailure()", 5);
		if (!this.isInCalls(extendedCall) || this.IsPttMode()) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("Transfer failed", 1);
	}

	@Override
	public void onCallTransferRefused(final ExtendedCall extendedCall, final String s, final Message message) {
		this.printLog("onCallTransferRefused()", 5);
		if (!this.isInCalls(extendedCall) || this.IsPttMode()) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("Transfer refused", 1);
	}

	@Override
	public void onCallTransferSuccess(final ExtendedCall extendedCall, final Message message) {
		this.printLog("onCallTransferSuccess()", 5);
		if (!this.isInCalls(extendedCall) || this.IsPttMode()) {
			this.printLog("NOT the current call", 5);
			return;
		}
		this.printLog("Transfer successed", 1);
		extendedCall.hangup();
	}

	@Override
	public void onReceiveHeatBeatMsg(final String s) {
		MyLog.i(this.tag, "receive:--" + s);
		final HeartBeatGrpState parser = HeartBeatParser.parser(s);
		if (parser != null && this.cmdHandler != null) {
			MyLog.d("pttTrace", "UserAgent#onReceiveHeatBeatMsg() cmd handler send message");
			final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
			obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_HEATBEAT_MESSAGE_CMD.ordinal();
			obtainMessage.arg2 = 3;
			obtainMessage.obj = parser;
			this.cmdHandler.sendMessage(obtainMessage);
		}
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		LogUtil.makeLog(this.tag, "pttGroupParse? onReceivedMessage()");
		if (message.isRequest()) {
			final TransactionServer transactionServer = new TransactionServer(sipProvider, message, null);
			final Message response = BaseMessageFactory.createResponse(message, 200, SipResponses.reasonOf(200), null);
			final Header header = message.getHeader("Ptt-Extension");
			if (header != null && header.getValue().equalsIgnoreCase("3ghandset OfflineDataSend")) {
				final Header header2 = message.getHeader("OfflineData-ID");
				final Header header3 = message.getHeader("OfflineData-Client-CheckID");
				response.setHeader(new Header("Ptt-Extension", "3ghandset OfflineDataSend"));
				response.setHeader(new Header("OfflineData-ID", header2.getValue()));
				response.setHeader(new Header("OfflineData-Client-CheckID", header3.getValue()));
			}
			transactionServer.respondWith(response);
			if (message.isRequest("NOTIFY")) {
				final Header header4 = message.getHeader("Anta-Extension");
				if (header4 != null) {
					header4.getValue().equalsIgnoreCase("destory");
				}
				final Header header5 = message.getHeader("OfflineData-ID");
				final String userName = message.getFromHeader().getNameAddress().getAddress().getUserName();
				if (header5 != null) {
					final Header header6 = message.getHeader("OfflineData-Reply");
					if (header6 == null) {
						return;
					}
					final String value = header6.getValue();
					final Intent intent = new Intent();
					intent.setAction("com.zed3.sipua.delivery_report");
					intent.putExtra("E_id", header5.getValue());
					intent.putExtra("type", "mms");
					intent.putExtra("reply", value);
					intent.putExtra("recipient_num", userName);
					Receiver.mContext.sendBroadcast(intent);
				}
			}
			message.isRequest("OPTIONS");
			if (message.isRequest("MESSAGE")) {
				final ContentLengthHeader contentLengthHeader = message.getContentLengthHeader();
				if (contentLengthHeader != null && Integer.valueOf(contentLengthHeader.getContentLength()) > 0) {
					final String body = message.getBody();
					final String value2 = message.getContentTypeHeader().getValue();
					if (value2.equalsIgnoreCase("text/3ghandset")) {
						if (this.GetCurGrp() != null) {
							this.mLastGrpIDBeforeMessageGroupChange = this.GetCurGrp().getGrpID();
						}
						this.pttGroupParse(body);
						return;
					}
					if (value2.contains("text/plain")) {
						String substring = body;
						if (body.endsWith("\r\n")) {
							substring = body.substring(0, body.length() - "\r\n".length());
						}
						String substring2 = substring;
						if (substring.endsWith("\r\n")) {
							substring2 = substring.substring(0, substring.length() - "\r\n".length());
						}
						final Header header7 = message.getHeader("EnhanceSMS-ID");
						String format;
						final String s = format = "";
						if (header7 != null) {
							final String value3 = header7.getValue();
							if (this.listsmsid != null && this.listsmsid.size() > 0) {
								MyLog.v("huang123", "listsmsid != null@@@");
								if (this.listsmsid.contains(value3)) {
									MyLog.v("huang123", "receivertime!!!" + this.listsmsid);
									return;
								}
							}
							if (this.listsmsid.size() > 19) {
								MyLog.v("huang123", "remove(listsmsid.get(0)" + this.listsmsid.get(0));
								this.listsmsid.remove(this.listsmsid.get(0));
							}
							this.listsmsid.add(value3);
							MyLog.v("huang123", "listsmsid!!!" + this.listsmsid);
							MyLog.v("huang123", "listsmsid.size!!!" + this.listsmsid.size());
							MyLog.i("receivertime", "receivertime ==" + value3.substring(8, 18));
							format = s;
							if (!TextUtils.isEmpty((CharSequence) value3)) {
								format = new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(new Date(1000L * Long.parseLong(value3.substring(8, 18))));
								MyLog.i("receivertime", "rmsgtime ==" + format);
							}
						}
						final TextMessage para2 = new TextMessage();
						para2.from = message.getFromHeader().getNameAddress().getAddress().getUserName();
						para2.to = this.user_profile.username;
						para2.content = substring2;
						para2.seq = String.valueOf(message.getCSeqHeader().getSequenceNumber());
						if (message.getFromHeader().getNameAddress().getDisplayName() != null) {
							para2.sipName = message.getFromHeader().getNameAddress().getDisplayName();
							MyLog.i("sipname", "sipname=" + para2.sipName);
						}
						MyLog.i("MESSAGE==>", substring2);
						if (!this.lastIMContent.equalsIgnoreCase(para2.content) || !this.lastIMSeq.equalsIgnoreCase(para2.seq)) {
							MyLog.i("MESSAGE==><", substring2);
							final ExtendedSipCallbackPara obj = new ExtendedSipCallbackPara();
							obj.type = ExtendedSipCallbackType.TYPE_RECEIVE_TEXT_MESSAGE_PHONE;
							obj.para2 = para2;
							obj.para3 = format;
							final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
							obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
							obtainMessage.obj = obj;
							this.cmdHandler.sendMessageDelayed(obtainMessage, 1500L);
							this.lastIMContent = para2.content;
							this.lastIMSeq = para2.seq;
						}
					} else if (value2.equalsIgnoreCase("text/customGroup")) {
						this.customGroupParser(body);
					}
				}
			} else if (message.isRequest("INFO")) {
				if (message.hasAntaExtensionHeader()) {
					this.handleAntaMessage(message);
				}
				final Header header8 = message.getHeader("Ptt-Extension");
				if (header8 != null) {
					final String value4 = header8.getValue();
					final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
					if (value4.equalsIgnoreCase("3ghandset accept")) {
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE;
					} else if (value4.equalsIgnoreCase("3ghandset forcecancel")) {
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_SERVER_FORCECANCEL_PHONE;
					} else if (value4.equalsIgnoreCase("3ghandset forcecancelwaiting")) {
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE;
					} else if (value4.equalsIgnoreCase("3ghandset OfflineDataSend")) {
						if (message.getHeader("Ptt-Extension") == null) {
							return;
						}
						final Header header9 = message.getHeader("OfflineData-ID");
						final Header header10 = message.getHeader("OfflineData-Client-CheckID");
						final Header header11 = message.getHeader("OfflineData-Connection");
						final Header header12 = message.getHeader("OfflineData-Size");
						final String value5 = header12.getValue();
						if (header9 == null || header12 == null || header10 == null || header11 == null) {
							return;
						}
						final String userName2 = message.getFromHeader().getNameAddress().getAddress().getUserName();
						final String value6 = header9.getValue();
						final String value7 = header10.getValue();
						int int1;
						if (!TextUtils.isEmpty((CharSequence) value5)) {
							int1 = Integer.parseInt(value5);
						} else {
							int1 = 0;
						}
						new MmsMessageService(header11.getValue(), 1, int1, value6, value7, userName2, null).initSocket();
					} else if (value4.startsWith("3ghandset status")) {
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_PTT_STATUS_PHONE;
						final String replace = value4.replace("3ghandset status ", "");
						if (replace.startsWith("1")) {
							extendedSipCallbackPara.para1 = replace.substring(2);
						} else {
							extendedSipCallbackPara.para1 = "";
						}
						final PttGrp getCurGrp = this.GetCurGrp();
						final String value8 = message.getHeader("From").getValue();
						if (!value8.equals("")) {
							final String trim = value8.substring(value8.indexOf(":") + 1, value8.indexOf("@")).trim();
							MyLog.i(this.tag, "sip from mds groupID:" + trim + " curGrpID:" + getCurGrp.grpID);
							if (getCurGrp == null) {
								return;
							}
							if (getCurGrp != null && !getCurGrp.grpID.equalsIgnoreCase(trim)) {
								return;
							}
						}
					} else if (value4.contains("3ghandset auth")) {
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_UNIONPASSWORDLOGIN_STATE;
						MyLog.i(this.tag, "3ghandset auth " + value4);
						if (value4.contains("fail")) {
							extendedSipCallbackPara.para1 = "fail";
						} else {
							extendedSipCallbackPara.para1 = "ok";
						}
					} else {
						if (!value4.contains("3ghandset tmpgrpadd")) {
							return;
						}
						extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_TEMPGROUP_ADD_MEMBER;
						if (value4.contains(this.GetCurGrp().getGrpName())) {
							final Header header13 = message.getHeader("Ptt-Member");
							if (header13 != null) {
								extendedSipCallbackPara.para1 = header13.getValue();
							}
						}
					}
					this.sendSipCmdMessage(extendedSipCallbackPara);
				}
			}
		}
	}

	public void onRtpStreamSenderException() {
		MyLog.e("testptt", "UserAgent#onRtpStreamSenderException enter");
		final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
		obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD.ordinal();
		obtainMessage.arg2 = 2;
		this.cmdHandler.sendMessage(obtainMessage);
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		if (transactionClient.getTransactionMethod().equals("MESSAGE")) {
			if (message.getStatusLine().getCode() == 404) {
				final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
				extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE;
				extendedSipCallbackPara.para1 = String.valueOf(message.getCallIdHeader().getCallId());
				this.sendSipCmdMessage(extendedSipCallbackPara);
			}
		} else if (transactionClient.getTransactionMethod().equals("INFO")) {
			final int code = message.getStatusLine().getCode();
			if (code == 403 && transactionClient.getRequestMessage().hasPttExtensionHeader() && transactionClient.getRequestMessage().getPttExtensionHeader().getValue().contains("3ghandset request")) {
				final ExtendedSipCallbackPara extendedSipCallbackPara2 = new ExtendedSipCallbackPara();
				extendedSipCallbackPara2.type = ExtendedSipCallbackType.TYPE_REQUEST_403;
				extendedSipCallbackPara2.para1 = String.valueOf(message.getCallIdHeader().getCallId());
				this.sendSipCmdMessage(extendedSipCallbackPara2);
			}
			message.getHeader("Ptt-Extension");
			if (transactionClient.getRequestMessage().hasHeader("OfflineData-Type")) {
				final Header header = transactionClient.getRequestMessage().getHeader("OfflineData-ID");
				final Header header2 = transactionClient.getRequestMessage().getHeader("OfflineData-Type");
				if (header != null) {
					final String value = header.getValue();
					if (header2.getValue().contains("mms")) {
						if (code == 405) {
							MessageSender.updateMmsState(value, 4);
							final String userName = message.getToHeader().getNameAddress().getAddress().getUserName();
							final Intent intent = new Intent();
							intent.setAction("com.zed3.sipua.mms_offline_space_full");
							intent.putExtra("recipient_num", userName);
							Receiver.mContext.sendBroadcast(intent);
						}
						final Intent intent2 = new Intent(MessageDialogueActivity.SEND_TEXT_FAIL);
						intent2.putExtra("0", value);
						Receiver.mContext.sendBroadcast(intent2);
					}
				}
			}
		}
	}

	@Override
	public void onTransProvisionalResponse(final TransactionClient transactionClient, final Message message) {
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		if (transactionClient.getTransactionMethod().equals("INFO")) {
			final int code = message.getStatusLine().getCode();
			if ((code == 200 || code == 403) && message.getTransactionMethod().equals("INFO")) {
				final Header header = message.getHeader("Ptt-Extension");
				if (header != null) {
					final android.os.Message obtainMessage = this.cmdHandler.obtainMessage();
					obtainMessage.arg1 = ProcessCmdType.PROCESS_TYPE_SIP_CMD.ordinal();
					final ExtendedSipCallbackPara obj = new ExtendedSipCallbackPara();
					if (code == 200) {
						final String value = header.getValue();
						if (value.equalsIgnoreCase("3ghandset accept")) {
							this.printLog("3ghandset accept");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_ACCEPT_PHONE;
						} else if (value.equalsIgnoreCase("3ghandset waiting")) {
							this.printLog("3ghandset waiting");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_WAITING_PHONE;
						} else if (value.equalsIgnoreCase("3ghandset reject")) {
							this.printLog("3ghandset reject");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_REJECT_PHONE;
						} else if (value.equalsIgnoreCase("3ghandset cancel")) {
							this.printLog("3ghandset cancel");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_OK_PHONE;
						} else if (value.equalsIgnoreCase("3ghandset cancelwaiting")) {
							this.printLog("3ghandset cancelwaiting");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_CANCEL_WAITING_OK_PHONE;
						} else if (value.equalsIgnoreCase("3ghandset getdatatotal")) {
							final String replace = message.getBody().trim().replace("\r\n", ":");
							if (replace != null && replace.contains(":")) {
								final String[] split = replace.split(":");
								MemoryMg.getInstance().User_3GTotal = Double.parseDouble(split[1]);
								MemoryMg.getInstance().User_3GTotalPTT = Double.parseDouble(split[3]);
								MemoryMg.getInstance().User_3GTotalVideo = Double.parseDouble(split[5]);
							} else {
								MemoryMg.getInstance().User_3GTotal = -1.0;
							}
							MyLog.i(this.tag, "3ghandset getdatatotal recv");
							obj.type = ExtendedSipCallbackType.TYPE_FLOWVIEWSCANNER_START;
						} else if (value.equalsIgnoreCase("3ghandset getdatastatistics")) {
							MyLog.i(this.tag, "3ghandset getdatastatistics recv");
							final String replace2 = message.getBody().trim().replace("\r\n", ",");
							if (replace2 != null && replace2.contains(",")) {
								final String[] split2 = replace2.split(",");
								if (!TextUtils.isEmpty((CharSequence) split2[0])) {
									MemoryMg.getInstance().User_3GLocalTotal = Double.parseDouble(split2[0].split(":")[1]);
									MemoryMg.getInstance().User_3GLocalTotalPTT = Double.parseDouble(split2[1].split(":")[1]);
									MemoryMg.getInstance().User_3GLocalTotalVideo = Double.parseDouble(split2[2].split(":")[1]);
								}
								final String substring = split2[3].substring(5);
								if (!TextUtils.isEmpty((CharSequence) substring)) {
									MemoryMg.getInstance().User_3GLocalTime = substring;
								} else {
									MemoryMg.getInstance().User_3GLocalTime = this.GetCurrentMouth(false);
								}
							}
						} else if (value.equalsIgnoreCase("3ghandset reportdatastatistics")) {
							MyLog.i(this.tag, "reportdatastatistics " + message.getBody());
						} else if (value.equalsIgnoreCase("3ghandset OfflineDataSend")) {
							if (message.getHeader("Ptt-Extension") == null) {
								return;
							}
							final Header header2 = message.getHeader("OfflineData-ID");
							final Header header3 = message.getHeader("OfflineData-NumType");
							final Header header4 = message.getHeader("OfflineData-Client-CheckID");
							final Header header5 = message.getHeader("OfflineData-Connection");
							if (header2 == null || header3 == null || header4 == null || header5 == null) {
								return;
							}
							new MmsMessageService(header5.getValue(), 0, 0, header2.getValue(), header4.getValue(), null, null).initSocket();
						} else {
							if (!value.equalsIgnoreCase("3ghandset getstatus")) {
								return;
							}
							this.printLog("3ghandset getstatus");
							obj.type = ExtendedSipCallbackType.TYPE_REQUEST_GETSTATUS_PHONE;
							Receiver.getGDProcess().fixgrpUpdate(message.getBody());
						}
						obtainMessage.obj = obj;
						this.cmdHandler.sendMessage(obtainMessage);
					}
				}
			}
		} else if (transactionClient.getTransactionMethod().equals("MESSAGE") && message.getStatusLine().getCode() == 200) {
			final ExtendedSipCallbackPara extendedSipCallbackPara = new ExtendedSipCallbackPara();
			extendedSipCallbackPara.type = ExtendedSipCallbackType.TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE;
			extendedSipCallbackPara.para1 = String.valueOf(message.getCallIdHeader().getCallId());
			this.sendSipCmdMessage(extendedSipCallbackPara);
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		if (transactionClient != null) {
			MyLog.i("guojunfengtimeout", "\u670d\u52a1\u5668\u8fde\u63a5\u8d85!..==>" + transactionClient.getTransactionMethod() + "...E_id = " + transactionClient.getRequestMessage().getCallIdHeader().getCallId());
		}
		if (transactionClient != null) {
			if (transactionClient != null) {
				MyLog.i("guojunfengtimeout", "\u670d\u52a1\u5668\u8fde\u63a5\u8d85!..==>" + transactionClient.getTransactionMethod() + "...E_id = " + transactionClient.getRequestMessage().getCallIdHeader().getCallId());
				Systems.log.print("testptt", "UserAgent#onTransTimeout method = " + transactionClient.getTransactionMethod() + ", callId = " + transactionClient.getRequestMessage().getCallIdHeader().getCallId() + " *********** request message = " + transactionClient.getRequestMessage().toString());
				final Message requestMessage = transactionClient.getRequestMessage();
				final Header header = requestMessage.getHeader("Ptt-Extension");
				if (header != null) {
					final String value = header.getValue();
					Systems.log.print("testptt", "UserAgent#onTransTimeout request message header = " + value);
					if ("3ghandset request".equals(value)) {
						final PttGrp getCurGrp = this.GetCurGrp();
						if (getCurGrp != null) {
							Systems.log.print("testptt", "UserAgent#onTransTimeout curGrp.state = " + getCurGrp.state);
							MyLog.d("pttreqeustTrace", "UserAgent#onTransTimeout() enter curGrp.state = " + getCurGrp.state);
							if (getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_IDLE || getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_LISTENING || getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN) {
								if (this.isPttKeyDown()) {
									this.onPttGroupRequestTimeout(requestMessage);
								}
							} else {
								Systems.log.print("testptt", "UserAgent#onTransTimeout ptt group state = " + getCurGrp.state);
							}
						}
					} else if ("3ghandset OfflineDataSend".equalsIgnoreCase(value)) {
						final Header header2 = requestMessage.getHeader("OfflineData-ID");
						if (header2 == null) {
							return;
						}
						final String value2 = header2.getValue();
						final Intent intent = new Intent(MessageDialogueActivity.SEND_TEXT_FAIL);
						intent.putExtra("0", value2);
						Receiver.mContext.sendBroadcast(intent);
					} else {
						Systems.log.print("testptt", "UserAgent#onTransTimeout request message header = " + value);
					}
				} else {
					Systems.log.print("testptt", "UserAgent#onTransTimeout ptt extension header is null ");
				}
			}
			if (transactionClient.getTransactionMethod().equals("MESSAGE")) {
				final String callId = transactionClient.getRequestMessage().getCallIdHeader().getCallId();
				final Intent intent2 = new Intent("com.zed3.sipua.ui_send_text_message_timeout");
				intent2.putExtra("E_id", callId);
				Receiver.mContext.sendBroadcast(intent2);
			}
		}
	}

	void printException(final Exception ex, final int n) {
	}

	void printLog(final String s) {
		this.printLog(s, 1);
	}

	void printLog(final String s, final int n) {
	}

	protected void processCallMessage(final android.os.Message message) {
		this.logfunc("processCallMessage");
		final Object obj = message.obj;
		if (obj != null) {
			this.dispatchCallStatus((CallStatusPara) obj);
		}
	}

	protected void processRtpSenderExceptionInner() {
		MyLog.d("testptt", "UserAgent#processRtpSenderException enter");
		final PttGrp getCurGrp = this.GetCurGrp();
		if (getCurGrp != null) {
			this.closeMediaApplication(((ExtendedCall) getCurGrp.oVoid).getExtCallId(), false, false);
		}
		MyLog.d("testptt", "UserAgent#processRtpSenderException currentPttGroup = " + getCurGrp);
		if (getCurGrp != null) {
			final Object oVoid = getCurGrp.oVoid;
			MyLog.d("testptt", "UserAgent#processRtpSenderException oVoid = " + oVoid);
			if (oVoid != null) {
				final ExtendedCall extendedCall = (ExtendedCall) oVoid;
				MyLog.d("testptt", "UserAgent#processRtpSenderException oVoid = " + oVoid);
				this.startMediaApplication(extendedCall, -1);
			}
		}
	}

	public void pttSpeakerControl() {
		if (this.audio_app == null) {
			return;
		}
		if (SipUAApp.isHeadsetConnected) {
			AudioModeUtils.setAudioStyle(0, false);
			return;
		}
		AudioModeUtils.setAudioStyle(0, true);
	}

	void reInvite(final String s, final int n) {
		final SessionDescriptor sessionDescriptor = new SessionDescriptor(this.local_session);
		sessionDescriptor.IncrementOLine();
		SessionDescriptor sessionDescriptor2;
		if (this.statusIs(3)) {
			sessionDescriptor2 = new SessionDescriptor(sessionDescriptor.getOrigin(), sessionDescriptor.getSessionName(), new ConnectionField("IP4", "0.0.0.0"), new TimeField());
		} else {
			sessionDescriptor2 = new SessionDescriptor(sessionDescriptor.getOrigin(), sessionDescriptor.getSessionName(), new ConnectionField("IP4", IpAddress.localIpAddress), new TimeField());
		}
		sessionDescriptor2.addMediaDescriptors(sessionDescriptor.getMediaDescriptors());
		this.local_session = sessionDescriptor.toString();
		new Thread() {
			@Override
			public void run() {
//				UserAgent.this.runReInvite(s, sessionDescriptor2.toString(), n);
			}
		}.start();
	}

	public void rejectTmpGrpCall() {
		this.logfunc("rejectTmpGrpCall");
		this.handleCallStatus(new CallStatusPara(CallStatus.CALL_REJECT_TMPCALL));
	}

	public void removeGrpById(final String s) {
		this.pttGrps.removeElementById(s);
	}

	public void restartGps() {
		synchronized (this) {
			if (UserAgent.gpsPacket != null) {
				UserAgent.gpsPacket.restartGPS();
				LogUtil.makeLog("testgps", "GPSPacket#restartGps(1) BDLocation is ");
			}
		}
	}

	@Override
	public void run() {
		// TODO
	}

	public void sendHeartBeat() {
		if (this.sip_provider != null && Receiver.mSipdroidEngine.isRegistered()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final PttGrp getCurGrp = UserAgent.this.GetCurGrp();
					if (getCurGrp != null && getCurGrp.oVoid != null && !getCurGrp.isCreateSession) {
						MyLog.d(UserAgent.this.tag, "UserAgent#sendHeartBeat() break send empty pkg");
						UserAgent.this.sip_provider.sendMessage("0");
						return;
					}
					final String s = "";
					if (UserAgent.this.GetCurGrp() != null) {
						final String grpID = UserAgent.this.GetCurGrp().grpID;
						final ExtendedCall extendedCall = (ExtendedCall) UserAgent.this.GetCurGrp().oVoid;
						String s2 = s;
						if (extendedCall != null) {
							s2 = s;
							if (extendedCall.getDialog() != null) {
								final String s3 = s2 = extendedCall.getDialog().getCallID();
								if (!TextUtils.isEmpty((CharSequence) s3)) {
									s2 = s3;
									if (s3.length() > 24) {
										s2 = s3.substring(s3.length() - 24, s3.length());
									}
								}
							}
						}
						android.util.Log.i("xxxx", "UserAgent#sendHeartBeat send message");
						UserAgent.this.sip_provider.sendMessage(new HeartBeatPacket(Settings.getUserName(), Settings.getPassword(), grpID, UserAgent.this.getCurGrpState(), s2).toString());
						return;
					}
					UserAgent.this.sip_provider.sendMessage(new HeartBeatPacket(Settings.getUserName(), Settings.getPassword(), "", "OFF", "").toString());
				}
			}).start();
		}
	}

	public String sendMultiMessage(final String s, final String s2, final String s3, final String s4, final String s5, final int n) {
		String string = s;
		if (s.indexOf("@") < 0) {
			String string2 = s;
			if (this.user_profile.realm.equals("")) {
				string2 = "&" + s;
			}
			string = String.valueOf(string2) + "@" + this.realm;
		}
		final Message request = BaseMessageFactory.createRequest(this.sip_provider, "INFO", new NameAddress(string), new NameAddress(this.user_profile.from_url), s2);
		request.setHeader(new Header("Ptt-Extension", "3ghandset OfflineDataSend"));
		request.setHeader(new Header("OfflineData-ID", s3));
		request.setHeader(new Header("OfflineData-Attribute", s4));
		request.setHeader(new Header("OfflineData-Type", s5));
		request.setHeader(new Header("OfflineData-Size", new StringBuilder(String.valueOf(n)).toString()));
		final long sequenceNumber = request.getCSeqHeader().getSequenceNumber();
		new TransactionClient(this.sip_provider, request, this).request();
		return String.valueOf(sequenceNumber);
	}

	public void setAbortCall(final ExtendedCall mAbortCall) {
		this.mAbortCall = mAbortCall;
	}

	public void setAcceptTime(final int accept_time) {
		this.user_profile.accept_time = accept_time;
	}

	public void setAudio(final boolean audio) {
		this.user_profile.audio = audio;
	}

	public void setAudioCall(final ExtendedCall audioCall) {
		this.audioCall = audioCall;
	}

	public void setAudioProt(final int audio_port) {
		this.user_profile.audio_port = audio_port;
	}

	public void setCustomGroupLength(final int customGroupLength) {
		this.pttGrps.setCustomGroupLength(customGroupLength);
	}

	public void setHangupTime(final int hangup_time) {
		this.user_profile.hangup_time = hangup_time;
	}

	public void setNoOfferMode(final boolean no_offer) {
		this.user_profile.no_offer = no_offer;
	}

	public void setPttGrps(final Vector<PttGrp> pttGrps) {
		this.pttGrps.setPttGrps(pttGrps);
	}

	public void setReceiveOnlyMode(final boolean recv_only) {
		this.user_profile.recv_only = recv_only;
	}

	public void setRecvFile(final String recv_file) {
		this.user_profile.recv_file = recv_file;
	}

	public void setRedirection(final String redirect_to) {
		this.user_profile.redirect_to = redirect_to;
	}

	public void setSendFile(final String send_file) {
		this.user_profile.send_file = send_file;
	}

	public void setSendOnlyMode(final boolean send_only) {
		this.user_profile.send_only = send_only;
	}

	public void setSendToneMode(final boolean send_tone) {
		this.user_profile.send_tone = send_tone;
	}

	public void setVedioProt(final int video_port) {
		this.user_profile.video_port = video_port;
	}

	public void setVideoCall(final ExtendedCall videoCall) {
		this.videoCall = videoCall;
	}

	void sleep(final int n) {
		final long n2 = n;
		try {
			Thread.sleep(n2);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void speakerMediaApplication(final int n) {
		final CallStatusPara callStatusPara = new CallStatusPara(CallStatus.CALL_MEDIA);
		callStatusPara.setPara1(Integer.parseInt("1"));
		callStatusPara.setPara2(Integer.parseInt(new StringBuilder(String.valueOf(n)).toString()));
		this.handleCallStatus(callStatusPara);
	}

	protected boolean statusIs(final int n) {
		return this.call_state == n;
	}

	public void updateAllCustomGroups(final Map<String, PttCustomGrp> customGrpMap) {
		this.pttGrps.setCustomGrpMap(customGrpMap);
	}

	public void updateCustomGroupMap(final Map<String, String> map) {
		this.pttGrps.setMap(map);
	}

	enum CallStatus {
		ACCEPT_GROUP_CALL("ACCEPT_GROUP_CALL", 2),
		CALL_ACCEPT("CALL_ACCEPT", 0),
		CALL_CANCELING("CALL_CANCELING", 1),
		CALL_HALT_GROUP("CALL_HALT_GROUP", 5),
		CALL_HALT_LISTEN("CALL_HALT_LISTEN", 12),
		CALL_HANGUP("CALL_HANGUP", 4),
		CALL_HANGUP_GROUP("CALL_HANGUP_GROUP", 6),
		CALL_HANGUP_WITHOUT_REJOIN("CALL_HANGUP_WITHOUT_REJOIN", 3),
		CALL_JOIN_TMP_CALL("CALL_JOIN_TMP_CALL", 9),
		CALL_MEDIA("CALL_MEDIA", 11),
		CALL_REJECT_TMPCALL("CALL_REJECT_TMPCALL", 13),
		CALL_SET_GROUP("CALL_SET_GROUP", 10),
		CALL_SINGLE_CALL("CALL_SINGLE_CALL", 7),
		CALL_TMP_CALL("CALL_TMP_CALL", 8);

		private CallStatus(final String s, final int n) {
		}
	}

	class CallStatusPara {
		private CallStatus callStatus;
		private Call mCall;
		private Message mMessage;
		private Object para1;
		private Object para2;
		private Object para3;
		private Object para4;

		public CallStatusPara(final CallStatus callStatus) {
			this.para1 = null;
			this.para2 = null;
			this.para3 = null;
			this.para4 = null;
			this.callStatus = callStatus;
		}

		public Call getCall() {
			return this.mCall;
		}

		public CallStatus getCallStatus() {
			return this.callStatus;
		}

		public Message getMessage() {
			return this.mMessage;
		}

		public Object getPara1() {
			return this.para1;
		}

		public Object getPara2() {
			return this.para2;
		}

		public Object getPara3() {
			return this.para3;
		}

		public Object getPara4() {
			return this.para4;
		}

		public void setCall(final Call mCall) {
			this.mCall = mCall;
		}

		public void setMessage(final Message mMessage) {
			this.mMessage = mMessage;
		}

		public void setPara1(final Object para1) {
			this.para1 = para1;
		}

		public void setPara2(final Object para2) {
			this.para2 = para2;
		}

		public void setPara3(final Object para3) {
			this.para3 = para3;
		}

		public void setPara4(final Object para4) {
			this.para4 = para4;
		}
	}

	public class ExtendedSipCallbackPara {
		boolean flag;
		String para1;
		Object para2;
		Object para3;
		Object para4;
		Object para5;
		Object para6;
		ExtendedSipCallbackType type;
	}

	enum ExtendedSipCallbackType {
		TYPE_FLOWVIEWSCANNER_START("TYPE_FLOWVIEWSCANNER_START", 20),
		TYPE_LOCAL_HANGUP_LINE("TYPE_LOCAL_HANGUP_LINE", 15),
		TYPE_ONCALLACCEPTED("TYPE_ONCALLACCEPTED", 26),
		TYPE_ONCALLCLOSED("TYPE_ONCALLCLOSED", 29),
		TYPE_ONCALLCLOSING("TYPE_ONCALLCLOSING", 28),
		TYPE_ONCALLINCOMING("TYPE_ONCALLINCOMING", 24),
		TYPE_ONCALLREFUSED("TYPE_ONCALLREFUSED", 27),
		TYPE_ONCALLRINGING("TYPE_ONCALLRINGING", 25),
		TYPE_ONCALLTIMEOUT("TYPE_ONCALLTIMEOUT", 30),
		TYPE_PEER_HANGUP_LINE("TYPE_PEER_HANGUP_LINE", 16),
		TYPE_PEER_INVITE_LINE("TYPE_PEER_INVITE_LINE", 12),
		TYPE_PTT_STATUS_PHONE("TYPE_PTT_STATUS_PHONE", 11),
		TYPE_RECEIVE_TEXT_MESSAGE_PHONE("TYPE_RECEIVE_TEXT_MESSAGE_PHONE", 8),
		TYPE_REGISTER_SUCCESS("TYPE_REGISTER_SUCCESS", 18),
		TYPE_REQUEST_403("TYPE_REQUEST_403", 19),
		TYPE_REQUEST_ACCEPT_PHONE("TYPE_REQUEST_ACCEPT_PHONE", 0),
		TYPE_REQUEST_CANCEL_OK_PHONE("TYPE_REQUEST_CANCEL_OK_PHONE", 7),
		TYPE_REQUEST_CANCEL_WAITING_OK_PHONE("TYPE_REQUEST_CANCEL_WAITING_OK_PHONE", 4),
		TYPE_REQUEST_GETSTATUS_PHONE("TYPE_REQUEST_GETSTATUS_PHONE", 17),
		TYPE_REQUEST_LISTEN_LINE("TYPE_REQUEST_LISTEN_LINE", 13),
		TYPE_REQUEST_REJECT_LINE("TYPE_REQUEST_REJECT_LINE", 14),
		TYPE_REQUEST_REJECT_PHONE("TYPE_REQUEST_REJECT_PHONE", 1),
		TYPE_REQUEST_WAITING_LINE("TYPE_REQUEST_WAITING_LINE", 3),
		TYPE_REQUEST_WAITING_PHONE("TYPE_REQUEST_WAITING_PHONE", 2),
		TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE("TYPE_SEND_TEXT_MESSAGE_FAIL_PHONE", 9),
		TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE("TYPE_SEND_TEXT_MESSAGE_SUCCEED_PHONE", 10),
		TYPE_SERVER_FORCECANCEL_PHONE("TYPE_SERVER_FORCECANCEL_PHONE", 6),
		TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE("TYPE_SERVER_FORCE_CANCEL_WAITING_PHONE", 5),
		TYPE_TEMPGROUP_ADD_MEMBER("TYPE_TEMPGROUP_ADD_MEMBER", 22),
		TYPE_TMPGRP_HANGUP_LINE("TYPE_TMPGRP_HANGUP_LINE", 23),
		TYPE_UNIONPASSWORDLOGIN_STATE("TYPE_UNIONPASSWORDLOGIN_STATE", 21);

		private ExtendedSipCallbackType(final String s, final int n) {
		}
	}

	public enum GrpCallSetupType {
		GRPCALLSETUPTYPE_ACCEPT("GRPCALLSETUPTYPE_ACCEPT", 1),
		GRPCALLSETUPTYPE_REJECT("GRPCALLSETUPTYPE_REJECT", 2),
		GRPCALLSETUPTYPE_TIP("GRPCALLSETUPTYPE_TIP", 0);

		private GrpCallSetupType(final String s, final int n) {
		}
	}

	enum ProcessCmdType {
		PROCESS_TYPE_CALL_CMD("PROCESS_TYPE_CALL_CMD", 4),
		PROCESS_TYPE_HEATBEAT_MESSAGE_CMD("PROCESS_TYPE_HEATBEAT_MESSAGE_CMD", 3),
		PROCESS_TYPE_PTT_KEY_CMD("PROCESS_TYPE_PTT_KEY_CMD", 1),
		PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD("PROCESS_TYPE_RTP_SENDER_EXCEPTION_CMD", 2),
		PROCESS_TYPE_SIP_CMD("PROCESS_TYPE_SIP_CMD", 0);

		private ProcessCmdType(final String s, final int n) {
		}
	}

	public enum PttPRMode {
		Idle("Idle", 2),
		ScreenPress("ScreenPress", 0),
		SideKeyPress("SideKeyPress", 1);

		private PttPRMode(final String s, final int n) {
		}
	}

	public class TextMessage {
		String content;
		String from;
		String seq;
		String sipName;
		String to;
	}
}
