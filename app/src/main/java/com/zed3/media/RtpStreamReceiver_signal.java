package com.zed3.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.zed3.audio.AudioModeUtils;
import com.zed3.audio.AudioUtil;
import com.zed3.audio.TrackPlayQueue;
import com.zed3.codecs.Codecs;
import com.zed3.log.MyLog;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.video.DeviceVideoInfo;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class RtpStreamReceiver_signal extends Thread {
	public static final int BUFFER_SIZE = 1024;
	public static boolean DEBUG = false;
	static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
	public static final int SO_TIMEOUT = 1000;
	public static boolean bluetoothmode = false;
	static String codec;
	static long down_time = 0L;
	static float gain = 0.0f;
	public static float good = 0.0f;
	public static int jitter = 0;
	public static byte judged_cmr = 0;
	public static float late = 0.0f;
	public static float loss = 0.0f;
	public static float loss2 = 0.0f;
	public static float lost = 0.0f;
	public static int mu = 0;
	public static int nearend = 0;
	static float ogain = 0.0f;
	static int oldvol = 0;
	public static LinkedBlockingQueue<short[]> queue;
	static boolean restored = false;
	static ToneGenerator ringbackPlayer;
	static boolean samsung = false;
	public static int speakermode = 0;
	public static boolean state_flg = false;
	private static final String tag = "RtpStreamReceiver_signal";
	public static int timeout;
	static boolean was_enabled;
	float Dtr;
	private final int MIN_MEAN_VALUE;
	AudioManager am;
	float avg_delay;
	int avgcnt;
	double avgheadroom;
	byte[] buffer;
	float cal_delay;
	float cal_lost;
	CallRecorder call_recorder;
	int cnt;
	int cnt2;
	ContentResolver cr;
	private int currentSpeakermode;
	double devheadroom;
	private int discardCount;
	private int fillCount;
	private int flow;
	boolean isVideo;
	boolean keepon;
	private int lastContinueSilenceCount;
	int lastSeq;
	boolean lockFirst;
	boolean lockLast;
	int lserver;
	int luser;
	int luser2;
	int maxjitter;
	int minjitter;
	int minjitteradjust;
	int nowSeq;
	Codecs.Map p_type;
	TrackPlayQueue playQueue;
	Thread playThread;
	PowerManager.WakeLock pwl;
	PowerManager.WakeLock pwl2;
	RtpPacket rtp_packet;
	RtpSocket rtp_socket;
	boolean running;
	double s;
	int seq;
	double smin;
	int timeCount;
	AudioTrack track;
	boolean trackRunning;
	int user;
	WifiManager.WifiLock wwl;

	static {
		RtpStreamReceiver_signal.DEBUG = true;
		RtpStreamReceiver_signal.codec = "";
		RtpStreamReceiver_signal.judged_cmr = 15;
		RtpStreamReceiver_signal.speakermode = -1;
		RtpStreamReceiver_signal.oldvol = -1;
		RtpStreamReceiver_signal.queue = new LinkedBlockingQueue<short[]>();
	}

	public RtpStreamReceiver_signal(final SipdroidSocket sipdroidSocket, final Codecs.Map p_type, final CallRecorder call_recorder, final boolean isVideo) {
		this.rtp_socket = null;
		this.isVideo = false;
		this.call_recorder = null;
		this.timeCount = 0;
		this.Dtr = 78.0f;
		this.cal_delay = 0.0f;
		this.avg_delay = 0.0f;
		this.discardCount = 0;
		this.fillCount = 0;
		this.smin = 200.0;
		this.lastSeq = -1;
		this.flow = 0;
		this.MIN_MEAN_VALUE = 10000;
		this.lastContinueSilenceCount = 0;
		this.init(sipdroidSocket);
		this.isVideo = isVideo;
		this.p_type = p_type;
		this.call_recorder = call_recorder;
		this.playQueue = new TrackPlayQueue();
		this.buffer = new byte[1612];
		this.rtp_packet = new RtpPacket(this.buffer, 0, "0");
		(this.playThread = new Thread(new TrackPlayer(this.playQueue))).start();
	}

	private boolean IsNowSeqLarger(final long n, final long n2) {
		if (n > n2) {
			if (n - n2 >= 32767L) {
				return false;
			}
		} else {
			if (n >= n2) {
				return false;
			}
			if (n2 - n < 32767L) {
				return false;
			}
		}
		return true;
	}

	public static void adjust(int n, final boolean b) {
		boolean b2 = false;
		final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (RtpStreamReceiver_signal.speakermode == 0 && (audioManager.getStreamVolume(stream()) == 0 ^ b)) {
			audioManager.setStreamMute(stream(), b);
		}
		if (b && RtpStreamReceiver_signal.down_time == 0L) {
			RtpStreamReceiver_signal.down_time = SystemClock.elapsedRealtime();
		}
		final boolean b3 = !b;
		if (RtpStreamReceiver_signal.speakermode != 0) {
			b2 = true;
		}
		if ((b3 ^ b2) && SystemClock.elapsedRealtime() - RtpStreamReceiver_signal.down_time < 500L) {
			if (!b) {
				RtpStreamReceiver_signal.down_time = 0L;
			}
			if (RtpStreamReceiver_signal.ogain > 1.0f) {
				if (n == 24) {
					if (RtpStreamReceiver_signal.gain != RtpStreamReceiver_signal.ogain) {
						RtpStreamReceiver_signal.gain = RtpStreamReceiver_signal.ogain;
						return;
					}
					if (audioManager.getStreamVolume(stream()) == audioManager.getStreamMaxVolume(stream())) {
						return;
					}
					RtpStreamReceiver_signal.gain = RtpStreamReceiver_signal.ogain / 2.0f;
				} else {
					if (RtpStreamReceiver_signal.gain == RtpStreamReceiver_signal.ogain) {
						RtpStreamReceiver_signal.gain = RtpStreamReceiver_signal.ogain / 2.0f;
						return;
					}
					if (audioManager.getStreamVolume(stream()) == 0) {
						return;
					}
					RtpStreamReceiver_signal.gain = RtpStreamReceiver_signal.ogain;
				}
			}
			final int stream = stream();
			if (n == 24) {
				n = 1;
			} else {
				n = -1;
			}
			audioManager.adjustStreamVolume(stream, n, 1);
		}
		Label_0197:
		{
			break Label_0197;
		}
		if (!b) {
			RtpStreamReceiver_signal.down_time = 0L;
		}
	}

	public static int byte2int(final byte b) {
		return (b + 256) % 256;
	}

	public static int byte2int(final byte b, final byte b2) {
		return ((b + 256) % 256 << 8) + (b2 + 256) % 256;
	}

	static void enableBluetooth(final boolean bluetoothmode) {
		if (RtpStreamReceiver_signal.bluetoothmode != bluetoothmode && (!bluetoothmode || isBluetoothAvailable())) {
			if (bluetoothmode) {
				RtpStreamReceiver_signal.was_enabled = true;
			}
			Bluetooth.enable(RtpStreamReceiver_signal.bluetoothmode = bluetoothmode);
		}
	}

	public static int getMode() {
		final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (Integer.parseInt(Build.VERSION.SDK) < 5) {
			return audioManager.getMode();
		}
		if (audioManager.isSpeakerphoneOn()) {
			return 0;
		}
		return 2;
	}

	private void init(final SipdroidSocket sipdroidSocket) {
		if (sipdroidSocket != null) {
			this.rtp_socket = new RtpSocket(sipdroidSocket);
		}
	}

	public static boolean isBluetoothAvailable() {
		return Receiver.headset <= 0 && Receiver.docked <= 0 && isBluetoothSupported() && Bluetooth.isAvailable();
	}

	public static boolean isBluetoothSupported() {
		return Build.VERSION.SDK_INT >= 8 && Bluetooth.isSupported();
	}

	private boolean isDelayOverLimitTime(final long n, final long n2) {
		if (n >= n2) {
			if ((int) (n - n2) / 8 <= DeviceVideoInfo.allow_audio_MaxDelay) {
				return false;
			}
		} else if (n2 - n > 2147483647L && (int) ((4294967296L - n2 + n) / 8L) <= DeviceVideoInfo.allow_audio_MaxDelay) {
			return false;
		}
		return true;
	}

	private boolean isExpectSeqNum(final long n, final long n2) {
		return 1L + n2 == n || (n2 == 65535L && n == 0L);
	}

	private static void println(final String s) {
	}

	public static void restoreMode() {
		if (SipUAApp.isHeadsetConnected) {
			AudioModeUtils.setAudioStyle(0, false);
			return;
		}
		AudioModeUtils.setAudioStyle(0, true);
	}

	public static void restoreSettings() {
		if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("oldvalid", false)) {
			final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
			final ContentResolver contentResolver = Receiver.mContext.getContentResolver();
			final int int1 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldvibrate", 0);
			final int int2 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldvibrate2", 0);
			final int int3 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldpolicy", 0);
			audioManager.setVibrateSetting(0, int1);
			audioManager.setVibrateSetting(1, int2);
			Settings.System.putInt(contentResolver, "wifi_sleep_policy", int3);
			PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring", 0);
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putBoolean("oldvalid", false);
			edit.commit();
			((PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(268435466, "Sipdroid.RtpStreamReceiver").acquire(1000L);
		}
		restoreMode();
	}

	public static void ringback(final boolean b) {
		// monitorenter(RtpStreamReceiver_signal.class)
		Label_0088:
		{
			if (!b) {
				break Label_0088;
			}
			try {
				if (RtpStreamReceiver_signal.ringbackPlayer == null) {
					RtpStreamReceiver_signal.oldvol = ((AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(3);
					AudioModeUtils.setAudioStyle(3, false);
					enableBluetooth(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("bluetooth", false));
//					(RtpStreamReceiver_signal.ringbackPlayer = new ToneGenerator(stream(), (int) (200.0f * Settings.getEarGain()))).startTone(23);
				} else if (!b && RtpStreamReceiver_signal.ringbackPlayer != null) {
					RtpStreamReceiver_signal.ringbackPlayer.stopTone();
					RtpStreamReceiver_signal.ringbackPlayer.release();
					RtpStreamReceiver_signal.ringbackPlayer = null;
					if (Receiver.call_state == 0 || Receiver.call_state == 2) {
						final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
						restoreMode();
						enableBluetooth(false);
						RtpStreamReceiver_signal.oldvol = -1;
					}
				}
			} finally {
			}
			// monitorexit(RtpStreamReceiver_signal.class)
		}
	}

	private static void setMode(final int n) {
	}

	static void setStreamVolume(final int n, final int n2, final int n3) {
		new Thread() {
			@Override
			public void run() {
				final AudioManager audioManager = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
				if (n == RtpStreamReceiver_signal.stream()) {
					RtpStreamReceiver_signal.restored = true;
				}
			}
		}.start();
	}

	public static int stream() {
		if (RtpStreamReceiver_signal.speakermode == 2) {
			return 0;
		}
		return 3;
	}

	int IsActiveVoice(final short[] array, final int n) {
		long n2 = 0L;
		for (int i = 0; i < n; ++i) {
			n2 += array[i] * array[i];
		}
		if (n2 / n < 10000L) {
			++this.lastContinueSilenceCount;
			return 0;
		}
		this.lastContinueSilenceCount = 0;
		return 1;
	}

	void bluetooth() {
		this.speaker(2);
		enableBluetooth(!RtpStreamReceiver_signal.bluetoothmode);
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
				RtpStreamReceiver_signal.nearend = RtpStreamReceiver_signal.mu * 6000 / 5;
			} else if (RtpStreamReceiver_signal.nearend > 0) {
				--RtpStreamReceiver_signal.nearend;
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
		final double n5 = n2 / (100000 * RtpStreamReceiver_signal.mu);
		if (n3 > 2.0 * this.smin || n3 < this.smin / 2.0) {
			this.smin = n3 * n5 + this.smin * (1.0 - n5);
		}
	}

	void calc2(final short[] array, final int n, final int n2) {
		for (int i = 0; i < n2; ++i) {
			final short n3 = array[i + n];
			if (n3 > 16350) {
				array[i + n] = 32700;
			} else if (n3 < -16350) {
				array[i + n] = -32700;
			} else {
				array[i + n] = (short) (n3 << 1);
			}
		}
	}

	void cleanupBluetooth() {
		if (!RtpStreamReceiver_signal.was_enabled || Build.VERSION.SDK_INT != 8) {
			return;
		}
		enableBluetooth(true);
		while (true) {
			try {
				Thread.sleep(3000L);
				if (Receiver.call_state == 0) {
//					Process.killProcess(Process.myPid());
				}
			} catch (InterruptedException ex) {
				continue;
			}
			break;
		}
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
			this.rtp_socket.getDatagramSocket().setSoTimeout(1000);
			this.seq = 0;
			this.lastSeq = -1;
		} catch (SocketException ex3) {
		}
	}

	public void halt() {
		this.running = false;
		this.trackRunning = false;
	}

	void handleReceiveAudio(final byte[] array, int i) {
		final short[] array2 = new short[1600];
		final int decode = this.p_type.codec.decode(array, array2, i);
		System.arraycopy(array2, 0, new short[decode], 0, decode);
		if (decode % 160 == 0) {
			i = 0;
			while (i < decode / 160) {
				final short[] array3 = new short[160];
				System.arraycopy(array2, i * 160, array3, 0, 160);
				while (true) {
					try {
						this.playQueue.push(array3);
						++i;
					} catch (InterruptedException ex) {
						ex.printStackTrace();
						continue;
					}
					break;
				}
			}
		}
	}

	void initMode() {
		if (this.isVideo && !SipUAApp.isHeadsetConnected) {
			AudioModeUtils.setAudioStyle(3, true);
			return;
		}
		AudioModeUtils.setAudioStyle(3, false);
	}

	public boolean isRunning() {
		return this.running;
	}

	void lock(final boolean b) {
		// TODO
	}

	void newjitter(final boolean b) {
		if (RtpStreamReceiver_signal.good != 0.0f && RtpStreamReceiver_signal.lost / RtpStreamReceiver_signal.good <= 0.01 && this.call_recorder == null) {
			final int n = (int) Math.sqrt(this.devheadroom);
			int minjitteradjust;
			if (b) {
				minjitteradjust = this.minjitteradjust;
			} else {
				minjitteradjust = 0;
			}
			int minjitter;
			if ((minjitter = n * 5 + minjitteradjust) < this.minjitter) {
				minjitter = this.minjitter;
			}
			int maxjitter;
			if ((maxjitter = minjitter) > this.maxjitter) {
				maxjitter = this.maxjitter;
			}
			if ((b || (Math.abs(RtpStreamReceiver_signal.jitter - maxjitter) >= this.minjitteradjust && maxjitter < RtpStreamReceiver_signal.jitter)) && (!b || maxjitter > RtpStreamReceiver_signal.jitter)) {
				RtpStreamReceiver_signal.jitter = maxjitter;
				RtpStreamReceiver_signal.late = 0.0f;
				this.avgcnt = 0;
				this.luser2 = this.user;
			}
		}
	}

	void restoreVolume() {
		if (this.am == null) {
			this.am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		}
		switch (getMode()) {
			case 2: {
				if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getInt("oldring", 0) > 0) {
//					setStreamVolume(2, (int) (this.am.getStreamMaxVolume(2) * Settings.getEarGain() * 3.0f / 4.0f), 0);
				}
//				this.track.setStereoVolume(AudioTrack.getMaxVolume() * (RtpStreamReceiver_signal.ogain = Settings.getEarGain() * 2.0f), AudioTrack.getMaxVolume() * Settings.getEarGain() * 2.0f);
				if (RtpStreamReceiver_signal.gain == 0.0f || RtpStreamReceiver_signal.ogain <= 1.0f) {
					RtpStreamReceiver_signal.gain = RtpStreamReceiver_signal.ogain;
					break;
				}
				break;
			}
			case 0: {
				this.track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
				break;
			}
		}
		final int stream = stream();
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		final String string = "volume" + RtpStreamReceiver_signal.speakermode;
		final int streamMaxVolume = this.am.getStreamMaxVolume(stream());
		int n;
		if (RtpStreamReceiver_signal.speakermode == 0) {
			n = 4;
		} else {
			n = 3;
		}
		setStreamVolume(stream, defaultSharedPreferences.getInt(string, n * streamMaxVolume / 4), 0);
	}

	@Override
	public void run() {
		LogUtil.makeLog("RtpStreamReceiver_signal", "run begin");
		RtpStreamReceiverUtil.onStartReceiving(RtpStreamReceiverUtil.RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
		PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("nodata", false);
		this.keepon = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("keepon", false);
		if (this.rtp_socket == null) {
			if (RtpStreamReceiver_signal.DEBUG) {
				println("ERROR: RTP socket is null");
			}
			return;
		}
		if (RtpStreamReceiver_signal.DEBUG) {
			println("Reading blocks of max " + this.buffer.length + " bytes");
		}
		this.running = true;
		enableBluetooth(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("bluetooth", false));
		RtpStreamReceiver_signal.restored = false;
		this.am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
		this.cr = Receiver.mContext.getContentResolver();
		this.saveSettings();
		Settings.System.putInt(this.cr, "wifi_sleep_policy", 2);
		this.am.setVibrateSetting(0, 0);
		this.am.setVibrateSetting(1, 0);
		if (Build.MODEL.equalsIgnoreCase("BYRT")) {
			this.am.setParameters("SetBesLoudnessStatus=1");
		}
		if (RtpStreamReceiver_signal.oldvol == -1) {
			RtpStreamReceiver_signal.oldvol = this.am.getStreamVolume(3);
		}
		this.initMode();
		// TODO

		this.saveVolume();
		restoreSettings();
		enableBluetooth(false);
		if (Build.MODEL.equalsIgnoreCase("BYRT")) {
			this.am.setParameters("SetBesLoudnessStatus=0");
		}
		RtpStreamReceiver_signal.oldvol = -1;
		this.p_type.codec.close();
		this.rtp_socket.close();
		this.rtp_socket = null;
		this.cleanupBluetooth();
		RtpStreamReceiverUtil.onStopReceiving(RtpStreamReceiverUtil.RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
		LogUtil.makeLog("RtpStreamReceiver_signal", "run end");
	}

	void saveSettings() {
		if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("oldvalid", false)) {
			final int vibrateSetting = this.am.getVibrateSetting(0);
			int vibrateSetting2 = this.am.getVibrateSetting(1);
			if (!PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).contains("oldvibrate2")) {
				vibrateSetting2 = 1;
			}
			final int int1 = Settings.System.getInt(this.cr, "wifi_sleep_policy", 0);
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putInt("oldvibrate", vibrateSetting);
			edit.putInt("oldvibrate2", vibrateSetting2);
			edit.putInt("oldpolicy", int1);
			edit.putInt("oldring", this.am.getStreamVolume(2));
			edit.putBoolean("oldvalid", true);
			edit.commit();
		}
	}

	void saveVolume() {
		if (RtpStreamReceiver_signal.restored) {
			if (this.am == null) {
				this.am = (AudioManager) Receiver.mContext.getSystemService(Context.AUDIO_SERVICE);
			}
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			edit.putInt("volume" + RtpStreamReceiver_signal.speakermode, this.am.getStreamVolume(stream()));
			edit.commit();
		}
	}

	void setCodec() {
		synchronized (this) {
			this.p_type.codec.init();
			RtpStreamReceiver_signal.codec = this.p_type.codec.getTitle();
			RtpStreamReceiver_signal.mu = this.p_type.codec.samp_rate() / 8000;
			this.maxjitter = AudioTrack.getMinBufferSize(this.p_type.codec.samp_rate(), 2, 2);
			if (this.maxjitter < RtpStreamReceiver_signal.mu * 12288) {
				this.maxjitter = RtpStreamReceiver_signal.mu * 12288;
			}
			final AudioTrack track = this.track;
			AudioUtil.getInstance().setStream(stream());
			this.track = new AudioTrack(stream(), this.p_type.codec.samp_rate(), 2, 2, this.maxjitter, 1);
			this.maxjitter /= 4;
			final int n = RtpStreamReceiver_signal.mu * 500;
			this.minjitteradjust = n;
			this.minjitter = n;
			RtpStreamReceiver_signal.jitter = RtpStreamReceiver_signal.mu * 875;
			this.devheadroom = Math.pow(RtpStreamReceiver_signal.jitter / 5, 2.0);
			RtpStreamReceiver_signal.timeout = 1;
			this.playQueue.clear();
			final int n2 = RtpStreamReceiver_signal.mu * -8000;
			this.luser2 = n2;
			this.luser = n2;
			this.lserver = 0;
			this.user = 0;
			this.cnt2 = 0;
			this.cnt = 0;
			if (track != null) {
				track.stop();
				track.release();
			}
			this.currentSpeakermode = RtpStreamReceiver_signal.speakermode;
		}
	}

	public int speaker(final int speakermode) {
		final int speakermode2 = RtpStreamReceiver_signal.speakermode;
		if (((Receiver.headset <= 0 && Receiver.docked <= 0 && Receiver.bluetooth <= 0) || speakermode == Receiver.speakermode()) && speakermode != speakermode2) {
			enableBluetooth(false);
			this.saveVolume();
			setMode(RtpStreamReceiver_signal.speakermode = speakermode);
			this.setCodec();
			this.restoreVolume();
			return speakermode2;
		}
		return speakermode2;
	}

	void write(final short[] array, int write, final int n) {
		// TODO
	}

	void writeNoqueue(final short[] array, final int n, int n2) {
		synchronized (this) {
			n2 /= 160;
			this.user += this.track.write(array, n, n2 * 160);
		}
	}

	class TrackPlayer implements Runnable {
		short[] lin2;
		TrackPlayQueue mQueue;

		public TrackPlayer(final TrackPlayQueue mQueue) {
			this.lin2 = new short[160];
			RtpStreamReceiver_signal.this.trackRunning = true;
			this.mQueue = mQueue;
		}

		@Override
		public void run() {
//			Process.setThreadPriority(-19);
			MyLog.i("RtpStreamReceiver_signal", "track Thread begining .. trackRunning = " + RtpStreamReceiver_signal.this.trackRunning);
			// TODO
		}
	}
}
