package com.zed3.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.zed3.audio.AudioModeUtils;
import com.zed3.audio.AudioUtil;
import com.zed3.codecs.Codecs;
import com.zed3.log.MyLog;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamReceiverUtil;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class RtpStreamReceiver_group extends Thread {
	public static final int BUFFER_SIZE = 1600;
	public static boolean DEBUG = false;
	static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
	public static final int SO_TIMEOUT = 1000;
	static long down_time = 0L;
	public static float good = 0.0f;
	public static float late = 0.0f;
	public static float loss = 0.0f;
	public static float lost = 0.0f;
	private static Context mContext;
	public static int nearend = 0;
	static int oldvol = 0;
	static boolean restored = false;
	static ToneGenerator ringbackPlayer;
	public static Queue<RtpPacket> rtpPacketQueue;
	static boolean samsung = false;
	public static int speakermode = 0;
	private static final String tag = "RtpStreamReceiver_group";
	public static int timeout;
	static boolean was_enabled;
	AudioManager am;
	double avgheadroom;
	private boolean bluetoothmode;
	CallRecorder call_recorder;
	int callptime;
	int cnt;
	int cnt2;
	private String codec;
	ContentResolver cr;
	private int currentSpeakermode;
	private long end_receive;
	private int errorCount;
	long firstPacketTime;
	int flow;
	private boolean isStartAudioPlay;
	public int jitter;
	boolean keepon;
	private int lastTime;
	boolean lockFirst;
	boolean lockLast;
	int lserver;
	int luser;
	int luser2;
	int maxjitter;
	int minheadroom;
	int minjitter;
	int minjitteradjust;
	public int mu;
	private boolean needLog;
	private Codecs.Map p_type;
	PowerManager.WakeLock pwl;
	PowerManager.WakeLock pwl2;
	private boolean rcvSuspend;
	private int receiveCount;
	RtpPacket rtp_packet;
	RtpSocket rtp_socket;
	boolean running;
	double s;
	int seq;
	double smin;
	int sortTime;
	List<RtpPacket> tempList;
	private int timeCount;
	private int times;
	private AudioTrack track;
	int user;
	TimerTask writeDataTask;
	private Timer writeDataTimer;

	static {
		RtpStreamReceiver_group.DEBUG = true;
		RtpStreamReceiver_group.rtpPacketQueue = new LinkedList<RtpPacket>();
		RtpStreamReceiver_group.speakermode = -1;
		RtpStreamReceiver_group.mContext = SipUAApp.mContext;
		RtpStreamReceiver_group.oldvol = -1;
	}

	public RtpStreamReceiver_group(final SipdroidSocket sipdroidSocket, final Codecs.Map p_type, final CallRecorder call_recorder, final int callptime) {
		this.flow = 0;
		this.sortTime = 20;
		this.tempList = new ArrayList<RtpPacket>();
		this.codec = "";
		this.rtp_socket = null;
		this.firstPacketTime = 0L;
		this.call_recorder = null;
		this.callptime = 100;
		this.rcvSuspend = false;
		this.writeDataTask = new TimerTask() {
			private byte[] audioData = new byte[1600];
			private int count;
			private boolean isFirst = true;

			@Override
			public void run() {
				if (RtpStreamReceiver_group.this.rcvSuspend) {
					if (this.isFirst) {
						this.isFirst = false;
						for (int i = 0; i < this.audioData.length; ++i) {
							this.audioData[i] = 0;
						}
					}
					if (RtpStreamReceiver_group.this.track != null) {
						RtpStreamReceiver_group.this.track.write(this.audioData, 0, 1600);
						++this.count;
						if (this.count > 4) {
							this.count = 0;
							Log.i("RtpStreamReceiver_group", "writeDataTask write()");
						}
					}
				}
			}
		};
		this.smin = 200.0;
		this.needLog = false;
		this.init(sipdroidSocket);
		this.p_type = p_type;
		this.call_recorder = call_recorder;
		this.callptime = callptime;
		if (callptime == 20) {
			this.sortTime = 100;
		}
	}

	public static void adjust(int n, final boolean b) {
		boolean b2 = false;
		final AudioManager audioManager = (AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (RtpStreamReceiver_group.speakermode == 0 && (audioManager.getStreamVolume(stream()) == 0 ^ b)) {
			audioManager.setStreamMute(stream(), b);
		}
		if (b && RtpStreamReceiver_group.down_time == 0L) {
			RtpStreamReceiver_group.down_time = SystemClock.elapsedRealtime();
		}
		final boolean b3 = !b;
		if (RtpStreamReceiver_group.speakermode != 0) {
			b2 = true;
		}
		if ((b3 ^ b2) && SystemClock.elapsedRealtime() - RtpStreamReceiver_group.down_time < 500L) {
			final int stream = stream();
			if (n == 24) {
				n = 1;
			} else {
				n = -1;
			}
			audioManager.adjustStreamVolume(stream, n, 1);
		}
		if (!b) {
			RtpStreamReceiver_group.down_time = 0L;
		}
	}

	public static int byte2int(final byte b) {
		return (b + 256) % 256;
	}

	public static int byte2int(final byte b, final byte b2) {
		return ((b + 256) % 256 << 8) + (b2 + 256) % 256;
	}

	private int getFrameNum(final byte[] array) {
		// monitorenter(this)
		final byte b = array[0];
		int n = 0;
		try {
			final int length = array.length;
			switch (b >> 4) {
				case 7: {
					n = (length - 1) / 32;
					break;
				}
				case 6: {
					n = (length - 1) / 27;
					break;
				}
				case 5: {
					n = (length - 1) / 21;
					break;
				}
				case 4: {
					n = (length - 1) / 20;
					break;
				}
				case 3: {
					n = (length - 1) / 18;
					break;
				}
				case 2: {
					n = (length - 1) / 16;
					break;
				}
				case 1: {
					n = (length - 1) / 14;
					break;
				}
				case 0: {
					n = (length - 1) / 13;
					break;
				}
			}
			return n;
		} finally {
		}
		// monitorexit(this)
	}

	public static int getMode() {
		final AudioManager audioManager = (AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (Integer.parseInt(Build.VERSION.SDK) < 5) {
			return audioManager.getMode();
		}
		if (audioManager.isSpeakerphoneOn()) {
			return 0;
		}
		return 2;
	}

	private void init(final SipdroidSocket sipdroidSocket) {
		if (sipdroidSocket == null) {
			return;
		}
		this.rtp_socket = new RtpSocket(sipdroidSocket);
		try {
			MyLog.e("receivebuffer_tt", "receiveBuffer before set = " + sipdroidSocket.getReceiveBufferSize());
			sipdroidSocket.setReceiveBufferSize(524288);
			MyLog.e("receivebuffer_tt", "receiveBuffer after set = " + sipdroidSocket.getReceiveBufferSize());
			sipdroidSocket.setSoTimeout(100);
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isBluetoothAvailable() {
		return Receiver.headset <= 0 && Receiver.docked <= 0 && isBluetoothSupported() && Bluetooth.isAvailable();
	}

	public static boolean isBluetoothSupported() {
		return Integer.parseInt(Build.VERSION.SDK) >= 8 && Bluetooth.isSupported();
	}

	protected static void println(final String s) {
	}

	public static void restoreMode() {
	}

	public static void restoreSettings() {
		if (PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getBoolean("oldvalid", false)) {
			final AudioManager audioManager = (AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE);
			final ContentResolver contentResolver = RtpStreamReceiver_group.mContext.getContentResolver();
			final int int1 = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getInt("oldvibrate", 0);
			final int int2 = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getInt("oldvibrate2", 0);
			final int int3 = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getInt("oldpolicy", 0);
			audioManager.setVibrateSetting(0, int1);
			audioManager.setVibrateSetting(1, int2);
			Settings.System.putInt(contentResolver, "wifi_sleep_policy", int3);
			PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getInt("oldring", 0);
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).edit();
			edit.putBoolean("oldvalid", false);
			edit.commit();
		}
		restoreMode();
	}

	public static void ringback(final boolean b) {
		// monitorenter(RtpStreamReceiver_group.class)
		Label_0107:
		{
			if (!b) {
				break Label_0107;
			}
			try {
				if (RtpStreamReceiver_group.ringbackPlayer == null) {
					RtpStreamReceiver_group.oldvol = ((AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(3);
					setMode(RtpStreamReceiver_group.speakermode);
					RtpStreamReceiver_group.ringbackPlayer = new ToneGenerator(0, 100);
//					if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
//						AudioUtil.getInstance().setAudioConnectMode(4);
//					} else {
//						AudioUtil.getInstance().setAudioConnectMode(2);
//					}
					RtpStreamReceiver_group.ringbackPlayer.startTone(23);
					return;
				}
			} finally {
			}
			// monitorexit(RtpStreamReceiver_group.class)
		}
		if (b || RtpStreamReceiver_group.ringbackPlayer == null) {
			return;
		}
		RtpStreamReceiver_group.ringbackPlayer.stopTone();
		RtpStreamReceiver_group.ringbackPlayer.release();
		RtpStreamReceiver_group.ringbackPlayer = null;
		if (Receiver.call_state == 0) {
			final AudioManager audioManager = (AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE);
			restoreMode();
			RtpStreamReceiver_group.oldvol = -1;
		}
	}

	public static void setMode(final int n) {
	}

	static void setStreamVolume(final int n, final int n2, final int n3) {
		new Thread() {
			@Override
			public void run() {
				if (n == RtpStreamReceiver_group.stream()) {
					RtpStreamReceiver_group.restored = true;
				}
			}
		}.start();
	}

	public static int stream() {
		final StringBuilder sb = new StringBuilder("stream(),return ");
		String s;
		if (RtpStreamReceiver_group.speakermode == 2) {
			s = "MODE_IN_CALL";
		} else {
			s = "STREAM_VOICE_CALL";
		}
		Log.i("Bluetooth_control", sb.append(s).toString());
		if (RtpStreamReceiver_group.speakermode == 2) {
			return 0;
		}
		return 3;
	}

	public void RcvResume() {
		this.rcvSuspend = false;
	}

	public void RcvSuspend() {
		this.rcvSuspend = true;
	}

	void bluetooth() {
		this.speaker(2);
		this.enableBluetooth(!this.bluetoothmode);
	}

	void calc(final short[] array, final int n, final int n2) {
		double n3 = 30000.0;
		double s;
		for (int i = 0; i < n2; i += 5, n3 = s) {
			this.s = 0.03 * Math.abs(array[i + n]) + 0.97 * this.s;
			s = n3;
			if (this.s < n3) {
				s = this.s;
			}
			if (this.s > this.smin) {
				RtpStreamReceiver_group.nearend = this.mu * 6000 / 5;
			} else if (RtpStreamReceiver_group.nearend > 0) {
				--RtpStreamReceiver_group.nearend;
			}
		}
		for (int j = 0; j < n2; ++j) {
			final short n4 = array[j + n];
			if (n4 > 6550) {
				array[j + n] = 32750;
			} else if (n4 < -6550) {
				array[j + n] = -32750;
			} else {
				array[j + n] = (short) (n4 * 5);
			}
		}
		final double n5 = n2 / (100000 * this.mu);
		if (n3 > 2.0 * this.smin || n3 < this.smin / 2.0) {
			this.smin = n3 * n5 + this.smin * (1.0 - n5);
		}
	}

	void cleanupBluetooth() {
		if (!RtpStreamReceiver_group.was_enabled || Integer.parseInt(Build.VERSION.SDK) != 8) {
			return;
		}
		this.enableBluetooth(true);
//		while (true) {
//			try {
//				Thread.sleep(3000L);
//				if (Receiver.call_state == 0) {
//					Process.killProcess(Process.myPid());
//				}
//			} catch (InterruptedException ex) {
//				continue;
//			}
//			break;
//		}
	}

	void empty() {
		try {
			this.rtp_socket.getDatagramSocket().setSoTimeout(1);
			while (true) {
				this.rtp_socket.receive(this.rtp_packet);
			}
		} catch (SocketException ex) {
		} catch (IOException ex2) {
		}
		try {
			this.rtp_socket.getDatagramSocket().setSoTimeout(100);
			this.seq = 0;
		} catch (SocketException ex3) {
		}
	}

	void enableBluetooth(final boolean bluetoothmode) {
		if (this.bluetoothmode != bluetoothmode && (!bluetoothmode || isBluetoothAvailable())) {
			if (bluetoothmode) {
				RtpStreamReceiver_group.was_enabled = true;
			}
			Bluetooth.enable(this.bluetoothmode = bluetoothmode);
		}
	}

	public AudioTrack getAudioTrack() {
		return this.track;
	}

	public String getCodec() {
		return this.codec;
	}

	public void halt() {
		this.running = false;
	}

	void initMode() {
		if (SipUAApp.isHeadsetConnected) {
			AudioModeUtils.setAudioStyle(0, false);
			return;
		}
		AudioModeUtils.setAudioStyle(0, true);
	}

	public boolean isRunning() {
		return this.running;
	}

	void reinitAudioTrack() {
		// TODO
	}

	void restoreVolume() {
		switch (getMode()) {
			case 2: {
				if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring", 0) > 0) {
//					setStreamVolume(2, (int) (this.am.getStreamMaxVolume(2) * Settings.getEarGain() * 3.0f / 4.0f), 0);
				}
//				this.track.setStereoVolume(AudioTrack.getMaxVolume() * Settings.getEarGain(), AudioTrack.getMaxVolume() * Settings.getEarGain());
				break;
			}
			case 0: {
				this.track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
				break;
			}
		}
		final int stream = stream();
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		final String string = "volume" + RtpStreamReceiver_group.speakermode;
		final int streamMaxVolume = this.am.getStreamMaxVolume(stream());
		int n;
		if (RtpStreamReceiver_group.speakermode == 0) {
			n = 4;
		} else {
			n = 3;
		}
		setStreamVolume(stream, defaultSharedPreferences.getInt(string, n * streamMaxVolume / 4), 0);
	}

	@Override
	public void run() {
		LogUtil.makeLog("RtpStreamReceiver_group", "run begin");
		RtpStreamReceiverUtil.onStartReceiving(RtpStreamReceiverUtil.RtpStreamReceiverType.GROUP_CALL_RECEIVER);
		PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getBoolean("nodata", false);
		this.keepon = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getBoolean("keepon", false);
		if (this.rtp_socket == null) {
			if (RtpStreamReceiver_group.DEBUG) {
				println("ERROR: RTP socket is null");
			}
			return;
		}
		final byte[] array = new byte[1612];
		this.rtp_packet = new RtpPacket(array, 0, "0");
		if (RtpStreamReceiver_group.DEBUG) {
			println("Reading blocks of max " + array.length + " bytes");
		}
		this.running = true;
		RtpStreamReceiver_group.restored = false;
//		Process.setThreadPriority(-19);
		this.am = (AudioManager) RtpStreamReceiver_group.mContext.getSystemService(Context.AUDIO_SERVICE);
		this.cr = RtpStreamReceiver_group.mContext.getContentResolver();
		this.saveSettings();
		Settings.System.putInt(this.cr, "wifi_sleep_policy", 2);
		this.am.setVibrateSetting(0, 0);
		this.am.setVibrateSetting(1, 0);
		if (Build.MODEL.equalsIgnoreCase("BYRT")) {
			this.am.setParameters("SetBesLoudnessStatus=1");
		}
		if (RtpStreamReceiver_group.oldvol == -1) {
			RtpStreamReceiver_group.oldvol = this.am.getStreamVolume(3);
		}
		this.initMode();
		this.setCodec();
		final short[] array2 = new short[1600];
		final short[] array3 = new short[1600];
		final short[] array4 = new short[160];
		this.track.play();
		System.gc();
		this.lockFirst = true;
		// TODO
	}

	void saveSettings() {
		if (!PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).getBoolean("oldvalid", false)) {
			final int vibrateSetting = this.am.getVibrateSetting(0);
			int vibrateSetting2 = this.am.getVibrateSetting(1);
			if (!PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).contains("oldvibrate2")) {
				vibrateSetting2 = 1;
			}
			final int int1 = Settings.System.getInt(this.cr, "wifi_sleep_policy", 0);
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).edit();
			edit.putInt("oldvibrate", vibrateSetting);
			edit.putInt("oldvibrate2", vibrateSetting2);
			edit.putInt("oldpolicy", int1);
			edit.putInt("oldring", this.am.getStreamVolume(2));
			edit.putBoolean("oldvalid", true);
			edit.commit();
		}
	}

	void saveVolume() {
		if (RtpStreamReceiver_group.restored) {
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(RtpStreamReceiver_group.mContext).edit();
			edit.putInt("volume" + RtpStreamReceiver_group.speakermode, this.am.getStreamVolume(stream()));
			edit.commit();
		}
	}

	void setCodec() {
		synchronized (this) {
			this.p_type.codec.init();
			this.codec = this.p_type.codec.getTitle();
			this.reinitAudioTrack();
		}
	}

	public int speaker(final int speakermode) {
		MyLog.i("SPEAKER", "group called mode = " + speakermode);
		final int speakermode2 = RtpStreamReceiver_group.speakermode;
		if (((Receiver.headset <= 0 && Receiver.docked <= 0 && Receiver.bluetooth <= 0) || speakermode == Receiver.speakermode()) && speakermode != speakermode2) {
			setMode(RtpStreamReceiver_group.speakermode = speakermode);
			return speakermode2;
		}
		return speakermode2;
	}

	public void startBackgroudAfterThreadStarting() {
//		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
//			AudioUtil.getInstance().setAudioConnectMode(4);
//			if (UserAgent.ua_ptt_mode && this.writeDataTimer == null) {
//				(this.writeDataTimer = new Timer()).schedule(this.writeDataTask, 0L, 100L);
//				MyLog.i("RtpStreamReceiver_group", "startBackgroud() writeDataTimer.schedule(writeDataTask, 0, 100)");
//			}
//			return;
//		}
		if (UserAgent.ua_ptt_mode) {
			MyLog.i("RtpStreamReceiver_group", "startBackgroud() setAudioConnectMode() setAudioConnectMode(AudioUtil.MODE_SPEAKER)");
			AudioUtil.getInstance().setAudioConnectMode(3);
			return;
		}
		MyLog.i("RtpStreamReceiver_group", "startBackgroud() setAudioConnectMode() setAudioConnectMode(AudioUtil.MODE_HOOK)");
		AudioUtil.getInstance().setAudioConnectMode(2);
	}

	public void stopBackgroudBeforeThreadStopping() {
		if (this.writeDataTimer != null) {
			MyLog.i("RtpStreamReceiver_group", "stopBackgroud() writeDataTimer.cancel()");
			this.writeDataTimer.cancel();
		}
		MyLog.i("RtpStreamReceiver_group", "stopBackgroud() setAudioConnectMode(TalkBackNew.mAudioMode)");
//		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null && ZMBluetoothManager.getInstance().isHeadSetEnabled()) {
//			AudioUtil.getInstance().setAudioConnectMode(4);
//			return;
//		}
		if (SipUAApp.isHeadsetConnected) {
			AudioUtil.getInstance().setAudioConnectMode(2);
			return;
		}
		AudioUtil.getInstance().setAudioConnectMode(3);
	}

	public void write(final short[] array, final int n, final int n2) {
		synchronized (this) {
			this.user += this.track.write(array, n, n2);
		}
	}
}
