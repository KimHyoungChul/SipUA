package com.zed3.media;

import android.content.Context;
import android.media.AudioRecord;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;

import com.zed3.ace.NSManager;
import com.zed3.audio.AudioSettings;
import com.zed3.codecs.Codecs;
import com.zed3.codecs.EncodeRate;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamSenderUtil;

import org.audio.audioEngine.SlientCheck;

import java.util.Random;

import WebRtc.aecm;

public class RtpStreamSender_signal extends Thread {
	public static boolean DEBUG = false;
	public static boolean changed = false;
	public static int delay = 0;
	public static int m = 0;
	public static EncodeRate.Mode mode;
	private static final String tag = "RtpStreamSender_signal";
	CallRecorder call_recorder;
	private long currentTime;
	boolean do_sync;
	String dtmf;
	int dtmf_payload_type;
	private int encodeCount;
	private int flow;
	int frame_rate;
	int frame_size;
	boolean isVideo;
	private long mLastSendMuteDataTime;
	private long mLastWriteLogTime;
	private int mMuteCount;
	private int mframeNumber;
	int mu;
	boolean muted;
	aecm myaecm;
	int nearend;
	Codecs.Map p_type;
	Random random;
	RtpSocket rtp_socket;
	boolean running;
	double s;
	SlientCheck slientChk;
	double smin;
	int sync_adj;

	static {
		RtpStreamSender_signal.DEBUG = false;
		RtpStreamSender_signal.delay = 0;
	}

	public RtpStreamSender_signal(final boolean b, final Codecs.Map map, final long n, final int n2, final SipdroidSocket sipdroidSocket, final String s, final int n3, final CallRecorder call_recorder, final boolean isVideo) {
		this.myaecm = new aecm();
		this.rtp_socket = null;
		this.slientChk = null;
		this.mframeNumber = 1;
		this.do_sync = true;
		this.sync_adj = 0;
		this.running = false;
		this.muted = false;
		this.dtmf = "";
		this.dtmf_payload_type = 101;
		this.call_recorder = null;
		this.isVideo = false;
		this.smin = 200.0;
		this.flow = 0;
		this.slientChk = new SlientCheck();
		this.init(b, map, n, n2, sipdroidSocket, s, n3);
		this.call_recorder = call_recorder;
		this.mframeNumber = n2 * 2 / 320;
		this.isVideo = isVideo;
	}

	private void closeAudioThread(final AudioRecord audioRecord, final AudioSendThread audioSendThread) {
		NSManager.releaseNS();
		audioSendThread.getLooper().quit();
		if (Integer.parseInt(Build.VERSION.SDK) < 5) {
			while (RtpStreamReceiver_signal.getMode() == 2) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException ex) {
				}
			}
		}
		if (this.slientChk != null) {
//			if (this.slientChk.WebRtcVadFree() == 0) {
//				MyLog.i("RtpStreamSender_signal", "free ok");
//			} else {
//				MyLog.e("RtpStreamSender_signal", "free error");
//			}
		}
		MicInstanceFacotory.releaseAudioRecord();
		RtpStreamSender_signal.m = 0;
		this.p_type.codec.close();
		this.rtp_socket.close();
		this.rtp_socket = null;
		if (this.call_recorder != null) {
			this.call_recorder.stopOutgoing();
			this.call_recorder = null;
		}
		LogUtil.makeLog("RtpStreamSender_signal", "run end");
	}

	private void init(final boolean do_sync, final Codecs.Map p_type, final long n, final int n2, final SipdroidSocket sipdroidSocket, final String s, final int n3) {
		this.p_type = p_type;
		this.frame_rate = (int) n;
		// TODO
	}

	private static void println(final String s) {
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
				this.nearend = this.mu * 3000 / 5;
			} else if (this.nearend > 0) {
				--this.nearend;
			}
		}
		final double n4 = n2 / (100000 * this.mu);
		if (n3 > 2.0 * this.smin || n3 < this.smin / 2.0) {
			this.smin = n3 * n4 + this.smin * (1.0 - n4);
		}
	}

	void calc1(final short[] array, final int n, final int n2) {
		for (int i = 0; i < n2; ++i) {
			array[i + n] >>= 2;
		}
	}

	void calc10(final short[] array, final int n, final int n2) {
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

	void calc2(final short[] array, final int n, final int n2) {
		for (int i = 0; i < n2; ++i) {
			array[i + n] >>= 1;
		}
	}

	public void halt() {
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	boolean isSupportNSReal() {
		return !Build.MODEL.equalsIgnoreCase("SM-G9008W");
	}

	void micGain(final short[] array, final int n, final int n2, final float n3) {
		if (n3 > 1.0f && n3 <= 2.0f) {
			for (int i = 0; i < n2; ++i) {
				final short n4 = array[i + n];
				if (n4 > 16350) {
					array[i + n] = (short) (16350.0f * n3);
				} else if (n4 < -16350) {
					array[i + n] = (short) (-16350.0f * n3);
				} else {
					array[i + n] = (short) (n4 * n3);
				}
			}
		}
	}

	public boolean mute() {
		return this.muted = !this.muted;
	}

	void noise(final short[] array, final int n, final int n2, final double n3) {
		int n4;
		if ((n4 = (int) (2.0 * n3)) == 0) {
			n4 = 1;
		}
		for (int i = 0; i < n2; i += 4) {
			final short n5 = (short) (this.random.nextInt(n4 * 2) - n4);
			array[i + n + 1] = (array[i + n] = n5);
			array[i + n + 3] = (array[i + n + 2] = n5);
		}
	}

	@Override
	public void run() {
		LogUtil.makeLog("RtpStreamSender_signal", "run begin");
		RtpStreamSenderUtil.reCheckNeedSendMuteData("RtpStreamSender_signal#run()");
		final WifiManager wifiManager = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
		long n = 0L;
		if (this.rtp_socket == null) {
			return;
		}
//		Process.setThreadPriority(-19);
		int n2 = 0;
		long abs = 0L;
		if (AudioSettings.startTempStamp != 0L) {
			abs = Math.abs(System.currentTimeMillis() - AudioSettings.startTempStamp);
		}
		final boolean boolean1 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("improve", false);
		final boolean boolean2 = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("selectwifi", false);
		this.running = true;
		RtpStreamSender_signal.m = 1;
		this.mu = this.p_type.codec.samp_rate() / 8000;
		int minBufferSize = AudioRecord.getMinBufferSize(this.p_type.codec.samp_rate(), 2, 2);
		// TODO
	}

	public void sendDTMF(final char c) {
		this.dtmf = String.valueOf(this.dtmf) + c;
	}

	public void setDTMFpayloadType(final int dtmf_payload_type) {
		this.dtmf_payload_type = dtmf_payload_type;
	}

	public void setSyncAdj(final int sync_adj) {
		this.sync_adj = sync_adj;
	}
}
