package com.zed3.media;

import android.media.AudioRecord;
import android.os.Handler;
import android.text.TextUtils;

import com.zed3.codecs.Codecs;
import com.zed3.log.Logger;
import com.zed3.log.MyLog;
import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;

import org.audio.audioEngine.SlientCheck;

import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class RtpStreamSender_group extends Thread {
	public static boolean DEBUG;
	public static boolean changed;
	public static String codecName;
	public static int delay;
	public static boolean mPTTPause;
	private static HashMap<Character, Byte> rtpEventMap;
	public static long time;
	String HTag;
	private final int INTERVAL_RTP_SEND_3G;
	int IP_UDP_VALUE;
	int MINIMUM_VALUE;
	private long SuspendTime;
	private byte[] bLock;
	CallRecorder call_recorder;
	int callptime;
	private Handler cmdhHandler;
	final int discardNum;
	boolean do_sync;
	String dtmf;
	int dtmf_payload_type;
	int flow;
	int frame_rate;
	int frame_size;
	private long intervalSendOfSuspend;
	int length;
	private int mframeNumber;
	int mu;
	boolean muted;
	private long mutedTimeMillion;
	int nearend;
	private boolean needLog;
	Codecs.Map p_type;
	private boolean pttRelease;
	Random random;
	AudioRecord record;
	RtpSocket rtp_socket;
	private boolean running;
	double s;
	int sendCount;
	private int seqn;
	SlientCheck slientChk;
	double smin;
	private boolean sndSuspend;
	private Queue<RtpPacket> storage;
	int sync_adj;
	private String tag;

	static {
		RtpStreamSender_group.DEBUG = true;
		RtpStreamSender_group.rtpEventMap = new HashMap<Character, Byte>() {
			{
				this.put('0', (byte) 0);
				this.put('1', (byte) 1);
				this.put('2', (byte) 2);
				this.put('3', (byte) 3);
				this.put('4', (byte) 4);
				this.put('5', (byte) 5);
				this.put('6', (byte) 6);
				this.put('7', (byte) 7);
				this.put('8', (byte) 8);
				this.put('9', (byte) 9);
				this.put('*', (byte) 10);
				this.put('#', (byte) 11);
				this.put('A', (byte) 12);
				this.put('B', (byte) 13);
				this.put('C', (byte) 14);
				this.put('D', (byte) 15);
			}
		};
		RtpStreamSender_group.delay = 0;
		RtpStreamSender_group.time = 0L;
	}

	public RtpStreamSender_group(final boolean b, final Codecs.Map map, final long n, final int n2, final SipdroidSocket sipdroidSocket, final String s, final int n3, final CallRecorder call_recorder, final int callptime) {
		this.flow = 0;
		this.callptime = 100;
		this.length = 0;
		this.HTag = "htag";
		this.bLock = new byte[0];
		this.discardNum = 2;
		this.sendCount = 0;
		this.storage = new LinkedList<RtpPacket>();
		this.MINIMUM_VALUE = 60;
		this.IP_UDP_VALUE = 42;
		this.rtp_socket = null;
		this.slientChk = null;
		this.do_sync = true;
		this.sync_adj = 0;
		this.running = false;
		this.muted = false;
		this.dtmf = "";
		this.dtmf_payload_type = 101;
		this.sndSuspend = false;
		this.pttRelease = false;
		this.INTERVAL_RTP_SEND_3G = 20000;
		this.intervalSendOfSuspend = 0L;
		this.SuspendTime = 0L;
		this.call_recorder = null;
		this.smin = 200.0;
		this.tag = "RtpStreamSender_group";
		this.needLog = false;
		this.seqn = 0;
		this.mutedTimeMillion = 0L;
		this.record = null;
		this.cmdhHandler = null;
		this.slientChk = new SlientCheck();
		this.init(b, map, n, n2, sipdroidSocket, s, n3);
		this.call_recorder = call_recorder;
		this.callptime = callptime;
		Logger.i(this.needLog, this.tag, String.valueOf(System.currentTimeMillis()) + "AudioRecord   new RtpStreamSender()");
	}

	private void init(final boolean do_sync, final Codecs.Map p_type, final long n, final int n2, final SipdroidSocket sipdroidSocket, final String s, final int n3) {
		this.p_type = p_type;
		this.frame_rate = (int) n;
		// TODO
	}

	private boolean isSocketInvalidArgmentException(final Exception ex) {
		if (ex != null && ex instanceof SocketException) {
			MyLog.e("testptt", "RtpStreamSender#onRtpStreamSenderException is socket exception");
			final String message = ((SocketException) ex).getMessage();
			if (!TextUtils.isEmpty((CharSequence) message) && message.contains("Invalid argument")) {
				return true;
			}
		}
		return false;
	}

	private void onRtpStreamSenderException(final Exception ex) {
		MyLog.e("testptt", "RtpStreamSender#onRtpStreamSenderException enter exception object = " + ex);
		Receiver.engine(SipUAApp.mContext).GetCurUA().onRtpStreamSenderException();
	}

	private static void println(final String s) {
	}

	public void PttRelecase() {
		this.pttRelease = true;
	}

	public void SndResume() {
		RtpStreamSender_group.mPTTPause = false;
		this.sndSuspend = false;
		synchronized (this.bLock) {
			if (!this.storage.isEmpty()) {
				this.storage.clear();
			}
		}
	}

	public void SndSuspend() {
		RtpStreamSender_group.mPTTPause = true;
		this.sndSuspend = true;
		this.SuspendTime = System.currentTimeMillis();
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
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     2: new             Ljava/lang/StringBuilder;
		//     5: dup
		//     6: ldc_w           "group- run begin ,time = "
		//     9: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    12: invokestatic    java/lang/System.currentTimeMillis:()J
		//    15: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//    18: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    21: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//    24: aload_0
		//    25: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//    28: ldc_w           "run begin"
		//    31: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//    34: iconst_0
		//    35: istore_3
		//    36: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//    39: ldc_w           "wifi"
		//    42: invokevirtual   android/content/Context.getSystemService:(Ljava/lang/String;)Ljava/lang/Object;
		//    45: checkcast       Landroid/net/wifi/WifiManager;
		//    48: astore          20
		//    50: lconst_0
		//    51: lstore          10
		//    53: aload_0
		//    54: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//    57: ifnonnull       61
		//    60: return
		//    61: bipush          -19
		//    63: invokestatic    android/os/Process.setThreadPriority:(I)V
		//    66: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//    69: invokestatic    android/preference/PreferenceManager.getDefaultSharedPreferences:(Landroid/content/Context;)Landroid/content/SharedPreferences;
		//    72: ldc_w           "improve"
		//    75: iconst_0
		//    76: invokeinterface android/content/SharedPreferences.getBoolean:(Ljava/lang/String;Z)Z
		//    81: istore          16
		//    83: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//    86: invokestatic    android/preference/PreferenceManager.getDefaultSharedPreferences:(Landroid/content/Context;)Landroid/content/SharedPreferences;
		//    89: ldc_w           "selectwifi"
		//    92: iconst_0
		//    93: invokeinterface android/content/SharedPreferences.getBoolean:(Ljava/lang/String;Z)Z
		//    98: istore          17
		//   100: aload_0
		//   101: iconst_1
		//   102: putfield        com/zed3/media/RtpStreamSender_group.running:Z
		//   105: iconst_1
		//   106: istore_1
		//   107: aload_0
		//   108: aload_0
		//   109: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   112: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   115: invokeinterface com/zed3/codecs/Codec.samp_rate:()I
		//   120: sipush          8000
		//   123: idiv
		//   124: putfield        com/zed3/media/RtpStreamSender_group.mu:I
		//   127: aload_0
		//   128: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   131: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   134: invokeinterface com/zed3/codecs/Codec.samp_rate:()I
		//   139: iconst_2
		//   140: iconst_2
		//   141: invokestatic    android/media/AudioRecord.getMinBufferSize:(III)I
		//   144: istore          9
		//   146: aload_0
		//   147: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//   150: new             Ljava/lang/StringBuilder;
		//   153: dup
		//   154: ldc_w           "getMinBufferSize() min = "
		//   157: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   160: iload           9
		//   162: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   165: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   168: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   171: aload_0
		//   172: aload_0
		//   173: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   176: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   179: invokeinterface com/zed3/codecs/Codec.samp_rate:()I
		//   184: aload_0
		//   185: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   188: idiv
		//   189: putfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//   192: sipush          1000
		//   195: aload_0
		//   196: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//   199: idiv
		//   200: i2l
		//   201: lstore          14
		//   203: aload_0
		//   204: aload_0
		//   205: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//   208: i2d
		//   209: ldc2_w          1.5
		//   212: dmul
		//   213: d2i
		//   214: putfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//   217: aload_0
		//   218: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   221: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   224: invokeinterface com/zed3/codecs/Codec.init:()V
		//   229: aload_0
		//   230: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   233: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   236: invokeinterface com/zed3/codecs/Codec.name:()Ljava/lang/String;
		//   241: putstatic       com/zed3/media/RtpStreamSender_group.codecName:Ljava/lang/String;
		//   244: aload_0
		//   245: aload_0
		//   246: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   249: iconst_2
		//   250: imul
		//   251: sipush          320
		//   254: idiv
		//   255: putfield        com/zed3/media/RtpStreamSender_group.mframeNumber:I
		//   258: new             Ljava/lang/StringBuilder;
		//   261: dup
		//   262: ldc_w           "Sample rate  = "
		//   265: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   268: aload_0
		//   269: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   272: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   275: invokeinterface com/zed3/codecs/Codec.samp_rate:()I
		//   280: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   283: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   286: invokestatic    com/zed3/media/RtpStreamSender_group.println:(Ljava/lang/String;)V
		//   289: new             Ljava/lang/StringBuilder;
		//   292: dup
		//   293: ldc_w           "Buffer size = "
		//   296: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   299: iload           9
		//   301: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   304: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   307: invokestatic    com/zed3/media/RtpStreamSender_group.println:(Ljava/lang/String;)V
		//   310: aload_0
		//   311: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   314: aload_0
		//   315: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//   318: iconst_1
		//   319: iadd
		//   320: imul
		//   321: newarray        S
		//   323: astore          21
		//   325: iconst_0
		//   326: istore          5
		//   328: aload_0
		//   329: new             Ljava/util/Random;
		//   332: dup
		//   333: invokespecial   java/util/Random.<init>:()V
		//   336: putfield        com/zed3/media/RtpStreamSender_group.random:Ljava/util/Random;
		//   339: aconst_null
		//   340: astore          18
		//   342: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//   345: invokevirtual   android/content/Context.getAssets:()Landroid/content/res/AssetManager;
		//   348: ldc_w           "alerting"
		//   351: invokevirtual   android/content/res/AssetManager.open:(Ljava/lang/String;)Ljava/io/InputStream;
		//   354: astore          19
		//   356: aload           19
		//   358: astore          18
		//   360: iconst_0
		//   361: istore_2
		//   362: aload_0
		//   363: getfield        com/zed3/media/RtpStreamSender_group.running:Z
		//   366: ifne            456
		//   369: aload_0
		//   370: getfield        com/zed3/media/RtpStreamSender_group.slientChk:Lorg/audio/audioEngine/SlientCheck;
		//   373: ifnull          396
		//   376: aload_0
		//   377: getfield        com/zed3/media/RtpStreamSender_group.slientChk:Lorg/audio/audioEngine/SlientCheck;
		//   380: invokevirtual   org/audio/audioEngine/SlientCheck.WebRtcVadFree:()I
		//   383: ifne            3277
		//   386: aload_0
		//   387: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//   390: ldc_w           "free ok"
		//   393: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   396: invokestatic    com/zed3/media/MicInstanceFacotory.releaseAudioRecord:()V
		//   399: aload_0
		//   400: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   403: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   406: invokeinterface com/zed3/codecs/Codec.close:()V
		//   411: aload_0
		//   412: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//   415: invokevirtual   com/zed3/net/RtpSocket.close:()V
		//   418: aload_0
		//   419: aconst_null
		//   420: putfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//   423: getstatic       com/zed3/media/RtpStreamSender_group.DEBUG:Z
		//   426: ifeq            435
		//   429: ldc_w           "rtp sender terminated"
		//   432: invokestatic    com/zed3/media/RtpStreamSender_group.println:(Ljava/lang/String;)V
		//   435: aload_0
		//   436: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//   439: ldc_w           "run terminated."
		//   442: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   445: aload_0
		//   446: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//   449: ldc_w           "run end"
		//   452: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//   455: return
		//   456: aload_0
		//   457: getfield        com/zed3/media/RtpStreamSender_group.sndSuspend:Z
		//   460: ifeq            1203
		//   463: invokestatic    com/zed3/media/MicInstanceFacotory.isRecordStateRecording:()Z
		//   466: ifeq            518
		//   469: invokestatic    com/zed3/media/MicInstanceFacotory.stop:()V
		//   472: aload_0
		//   473: iconst_0
		//   474: putfield        com/zed3/media/RtpStreamSender_group.sendCount:I
		//   477: aload_0
		//   478: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   481: newarray        S
		//   483: astore          19
		//   485: aload           19
		//   487: iconst_0
		//   488: invokestatic    java/util/Arrays.fill:([SS)V
		//   491: aload           19
		//   493: iconst_0
		//   494: aload_0
		//   495: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   498: invokestatic    com/zed3/media/MicInstanceFacotory.read:([SII)I
		//   501: istore          4
		//   503: iload           4
		//   505: ifgt            721
		//   508: aload_0
		//   509: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//   512: ldc_w           "--------stop called"
		//   515: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   518: aload_0
		//   519: getfield        com/zed3/media/RtpStreamSender_group.cmdhHandler:Landroid/os/Handler;
		//   522: ifnull          615
		//   525: aload_0
		//   526: getfield        com/zed3/media/RtpStreamSender_group.pttRelease:Z
		//   529: ifeq            615
		//   532: ldc_w           "huangfujian"
		//   535: new             Ljava/lang/StringBuilder;
		//   538: dup
		//   539: ldc_w           "pttRelease:::"
		//   542: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   545: aload_0
		//   546: getfield        com/zed3/media/RtpStreamSender_group.pttRelease:Z
		//   549: invokevirtual   java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		//   552: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   555: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   558: aload_0
		//   559: getfield        com/zed3/media/RtpStreamSender_group.cmdhHandler:Landroid/os/Handler;
		//   562: invokevirtual   android/os/Handler.obtainMessage:()Landroid/os/Message;
		//   565: astore          19
		//   567: aload           19
		//   569: bipush          9
		//   571: putfield        android/os/Message.what:I
		//   574: aload_0
		//   575: getfield        com/zed3/media/RtpStreamSender_group.cmdhHandler:Landroid/os/Handler;
		//   578: aload           19
		//   580: invokevirtual   android/os/Handler.sendMessage:(Landroid/os/Message;)Z
		//   583: pop
		//   584: aload_0
		//   585: iconst_0
		//   586: putfield        com/zed3/media/RtpStreamSender_group.pttRelease:Z
		//   589: ldc_w           "huangfujian"
		//   592: new             Ljava/lang/StringBuilder;
		//   595: dup
		//   596: ldc_w           "pttRelease1:::"
		//   599: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   602: aload_0
		//   603: getfield        com/zed3/media/RtpStreamSender_group.pttRelease:Z
		//   606: invokevirtual   java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		//   609: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   612: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   615: iload_2
		//   616: istore          4
		//   618: invokestatic    android/os/SystemClock.uptimeMillis:()J
		//   621: aload_0
		//   622: getfield        com/zed3/media/RtpStreamSender_group.intervalSendOfSuspend:J
		//   625: lsub
		//   626: ldc2_w          20000
		//   629: lcmp
		//   630: ifle            662
		//   633: aload_0
		//   634: invokestatic    android/os/SystemClock.uptimeMillis:()J
		//   637: putfield        com/zed3/media/RtpStreamSender_group.intervalSendOfSuspend:J
		//   640: aload_0
		//   641: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//   644: ldc_w           "NAT process..."
		//   647: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   650: iconst_0
		//   651: istore          4
		//   653: iload           4
		//   655: iconst_3
		//   656: if_icmplt       1031
		//   659: iload_2
		//   660: istore          4
		//   662: iconst_0
		//   663: istore          6
		//   665: iload           4
		//   667: istore_2
		//   668: aload_0
		//   669: getfield        com/zed3/media/RtpStreamSender_group.sndSuspend:Z
		//   672: ifeq            362
		//   675: iload           4
		//   677: istore_2
		//   678: iload           6
		//   680: aload_0
		//   681: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   684: iconst_2
		//   685: imul
		//   686: sipush          320
		//   689: idiv
		//   690: if_icmpge       362
		//   693: iload           6
		//   695: iconst_1
		//   696: iadd
		//   697: istore          6
		//   699: ldc2_w          20
		//   702: invokestatic    com/zed3/media/RtpStreamSender_group.sleep:(J)V
		//   705: goto            665
		//   708: astore          19
		//   710: aload           19
		//   712: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//   715: iload           4
		//   717: istore_2
		//   718: goto            362
		//   721: aload_0
		//   722: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   725: bipush          12
		//   727: iadd
		//   728: newarray        B
		//   730: astore          22
		//   732: aload_0
		//   733: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   736: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   739: aload           19
		//   741: iconst_0
		//   742: aload           22
		//   744: aload_0
		//   745: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   748: invokeinterface com/zed3/codecs/Codec.encode:([SI[BI)I
		//   753: istore          6
		//   755: iload           6
		//   757: ifne            779
		//   760: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//   763: aload_0
		//   764: getfield        com/zed3/media/RtpStreamSender_group.mframeNumber:I
		//   767: sipush          160
		//   770: imul
		//   771: i2l
		//   772: ladd
		//   773: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//   776: goto            477
		//   779: new             Lcom/zed3/net/RtpPacket;
		//   782: dup
		//   783: aload           22
		//   785: iconst_0
		//   786: ldc_w           "0"
		//   789: invokespecial   com/zed3/net/RtpPacket.<init>:([BILjava/lang/String;)V
		//   792: astore          19
		//   794: aload           19
		//   796: invokestatic    java/lang/System.currentTimeMillis:()J
		//   799: invokevirtual   com/zed3/net/RtpPacket.setPackedTime:(J)V
		//   802: aload           19
		//   804: aload_0
		//   805: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   808: getfield        com/zed3/codecs/Codecs.Map.number:I
		//   811: invokevirtual   com/zed3/net/RtpPacket.setPayloadType:(I)V
		//   814: ldc_w           "huangfujian"
		//   817: new             Ljava/lang/StringBuilder;
		//   820: dup
		//   821: ldc_w           "setSequenceNumber(seqn++)::"
		//   824: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   827: aload_0
		//   828: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//   831: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   834: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   837: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   840: aload_0
		//   841: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//   844: istore          7
		//   846: aload_0
		//   847: iload           7
		//   849: iconst_1
		//   850: iadd
		//   851: putfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//   854: aload           19
		//   856: iload           7
		//   858: invokevirtual   com/zed3/net/RtpPacket.setSequenceNumber:(I)V
		//   861: aload           19
		//   863: iload           6
		//   865: invokevirtual   com/zed3/net/RtpPacket.setPayloadLength:(I)V
		//   868: aload           19
		//   870: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//   873: invokevirtual   com/zed3/net/RtpPacket.setTimestamp:(J)V
		//   876: aload_0
		//   877: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//   880: ifne            914
		//   883: aload_0
		//   884: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//   887: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//   890: invokeinterface com/zed3/codecs/Codec.number:()I
		//   895: bipush          9
		//   897: if_icmpne       1002
		//   900: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//   903: aload_0
		//   904: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   907: iconst_2
		//   908: idiv
		//   909: i2l
		//   910: ladd
		//   911: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//   914: aload           19
		//   916: ifnull          966
		//   919: aload_0
		//   920: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//   923: new             Ljava/lang/StringBuilder;
		//   926: dup
		//   927: ldc_w           "send pp 1 seqNum = "
		//   930: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   933: aload           19
		//   935: invokevirtual   com/zed3/net/RtpPacket.getSequenceNumber:()I
		//   938: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   941: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   944: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   947: aload_0
		//   948: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//   951: aload           19
		//   953: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//   956: pop
		//   957: ldc_w           "huangfujian"
		//   960: ldc_w           "stop\u4e4b\u540e\u53d1\u9001\u7f13\u5b58"
		//   963: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   966: iload           4
		//   968: aload_0
		//   969: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//   972: if_icmpge       477
		//   975: ldc_w           "huangfujian"
		//   978: new             Ljava/lang/StringBuilder;
		//   981: dup
		//   982: ldc_w           "stop\u4e4b\u540e\u53d1\u9001\u7f13\u5b58\u7684\u5927\u5c0f"
		//   985: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   988: iload           4
		//   990: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   993: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   996: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   999: goto            508
		//  1002: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  1005: aload_0
		//  1006: getfield        com/zed3/media/RtpStreamSender_group.mframeNumber:I
		//  1009: sipush          160
		//  1012: imul
		//  1013: i2l
		//  1014: ladd
		//  1015: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  1018: goto            914
		//  1021: astore          19
		//  1023: aload           19
		//  1025: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1028: goto            966
		//  1031: new             Ljava/net/DatagramPacket;
		//  1034: dup
		//  1035: iconst_1
		//  1036: newarray        B
		//  1038: iconst_1
		//  1039: invokespecial   java/net/DatagramPacket.<init>:([BI)V
		//  1042: astore          19
		//  1044: aload           19
		//  1046: aload_0
		//  1047: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  1050: invokevirtual   com/zed3/net/RtpSocket.GetAddress:()Ljava/net/InetAddress;
		//  1053: invokevirtual   java/net/DatagramPacket.setAddress:(Ljava/net/InetAddress;)V
		//  1056: aload           19
		//  1058: aload_0
		//  1059: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  1062: invokevirtual   com/zed3/net/RtpSocket.GetPort:()I
		//  1065: invokevirtual   java/net/DatagramPacket.setPort:(I)V
		//  1068: aload_0
		//  1069: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  1072: invokevirtual   com/zed3/net/RtpSocket.GetSocket:()Lcom/zed3/net/SipdroidSocket;
		//  1075: aload           19
		//  1077: invokevirtual   com/zed3/net/SipdroidSocket.send:(Ljava/net/DatagramPacket;)V
		//  1080: ldc2_w          10
		//  1083: invokestatic    com/zed3/media/RtpStreamSender_group.sleep:(J)V
		//  1086: aload_0
		//  1087: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1090: new             Ljava/lang/StringBuilder;
		//  1093: dup
		//  1094: ldc_w           "NAT send "
		//  1097: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1100: iload           4
		//  1102: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  1105: ldc_w           " port:"
		//  1108: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//  1111: aload_0
		//  1112: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  1115: invokevirtual   com/zed3/net/RtpSocket.GetPort:()I
		//  1118: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  1121: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1124: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1127: iload_2
		//  1128: istore          6
		//  1130: iload           4
		//  1132: iconst_1
		//  1133: iadd
		//  1134: istore          4
		//  1136: iload           6
		//  1138: istore_2
		//  1139: goto            653
		//  1142: astore          19
		//  1144: aload_0
		//  1145: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1148: new             Ljava/lang/StringBuilder;
		//  1151: dup
		//  1152: ldc_w           "NAT exception"
		//  1155: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1158: aload           19
		//  1160: invokevirtual   java/lang/Exception.toString:()Ljava/lang/String;
		//  1163: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//  1166: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1169: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  1172: iload_2
		//  1173: istore          6
		//  1175: aload_0
		//  1176: aload           19
		//  1178: invokespecial   com/zed3/media/RtpStreamSender_group.isSocketInvalidArgmentException:(Ljava/lang/Exception;)Z
		//  1181: ifeq            1130
		//  1184: iload_2
		//  1185: istore          6
		//  1187: iload_2
		//  1188: ifne            1130
		//  1191: iconst_1
		//  1192: istore          6
		//  1194: aload_0
		//  1195: aload           19
		//  1197: invokespecial   com/zed3/media/RtpStreamSender_group.onRtpStreamSenderException:(Ljava/lang/Exception;)V
		//  1200: goto            1130
		//  1203: invokestatic    com/zed3/media/MicInstanceFacotory.isRecordStateStopped:()Z
		//  1206: ifeq            1556
		//  1209: invokestatic    com/zed3/media/TipSoundPlayer.getInstance:()Lcom/zed3/media/TipSoundPlayer;
		//  1212: getstatic       com/zed3/media/TipSoundPlayer.Sound.PTT_ACCEPT:Lcom/zed3/media/TipSoundPlayer.Sound;
		//  1215: invokevirtual   com/zed3/media/TipSoundPlayer.play:(Lcom/zed3/media/TipSoundPlayer.Sound;)V
		//  1218: ldc_w           "huangfujian"
		//  1221: new             Ljava/lang/StringBuilder;
		//  1224: dup
		//  1225: ldc_w           "TipSoundPlayer PTT_ACCEPT::"
		//  1228: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1231: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  1234: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1237: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1240: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  1243: ldc2_w          280
		//  1246: invokestatic    android/os/SystemClock.sleep:(J)V
		//  1249: invokestatic    com/zed3/media/MicInstanceFacotory.startRecording:()V
		//  1252: aload_0
		//  1253: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  1256: ldc_w           "--------startRecording called"
		//  1259: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1262: ldc_w           "huangfujian"
		//  1265: new             Ljava/lang/StringBuilder;
		//  1268: dup
		//  1269: ldc_w           "startRecording()::"
		//  1272: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1275: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  1278: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1281: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1284: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  1287: aload_0
		//  1288: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  1291: bipush          12
		//  1293: iadd
		//  1294: newarray        B
		//  1296: astore          22
		//  1298: new             Lcom/zed3/net/RtpPacket;
		//  1301: dup
		//  1302: aload           22
		//  1304: iconst_0
		//  1305: ldc_w           "0"
		//  1308: invokespecial   com/zed3/net/RtpPacket.<init>:([BILjava/lang/String;)V
		//  1311: astore          19
		//  1313: aload           19
		//  1315: invokestatic    java/lang/System.currentTimeMillis:()J
		//  1318: invokevirtual   com/zed3/net/RtpPacket.setPackedTime:(J)V
		//  1321: aload           19
		//  1323: aload_0
		//  1324: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  1327: getfield        com/zed3/codecs/Codecs.Map.number:I
		//  1330: invokevirtual   com/zed3/net/RtpPacket.setPayloadType:(I)V
		//  1333: getstatic       com/zed3/media/RtpStreamSender_group.changed:Z
		//  1336: ifne            1345
		//  1339: invokestatic    com/zed3/media/MicInstanceFacotory.isAudioRecorderEmpty:()Z
		//  1342: ifeq            1642
		//  1345: invokestatic    com/zed3/media/MicInstanceFacotory.isAudioRecorderEmpty:()Z
		//  1348: ifne            1386
		//  1351: invokestatic    com/zed3/media/MicInstanceFacotory.releaseAudioRecord:()V
		//  1354: getstatic       com/zed3/media/RtpStreamReceiver_group.samsung:Z
		//  1357: ifeq            1386
		//  1360: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//  1363: ldc_w           "audio"
		//  1366: invokevirtual   android/content/Context.getSystemService:(Ljava/lang/String;)Ljava/lang/Object;
		//  1369: checkcast       Landroid/media/AudioManager;
		//  1372: astore          23
		//  1374: aload           23
		//  1376: iconst_2
		//  1377: invokevirtual   android/media/AudioManager.setMode:(I)V
		//  1380: aload           23
		//  1382: iconst_0
		//  1383: invokevirtual   android/media/AudioManager.setMode:(I)V
		//  1386: iconst_0
		//  1387: putstatic       com/zed3/media/RtpStreamSender_group.changed:Z
		//  1390: aload_0
		//  1391: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  1394: new             Ljava/lang/StringBuilder;
		//  1397: dup
		//  1398: ldc_w           "PTIME = "
		//  1401: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1404: lload           14
		//  1406: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1409: ldc_w           ",MIN = "
		//  1412: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//  1415: iload           9
		//  1417: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  1420: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1423: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1426: invokestatic    com/zed3/media/MicInstanceFacotory.getRecord:()V
		//  1429: ldc_w           "huangfujian"
		//  1432: new             Ljava/lang/StringBuilder;
		//  1435: dup
		//  1436: ldc_w           "AudioRecordUitls.getRecord::"
		//  1439: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1442: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  1445: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1448: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1451: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1454: ldc             "htag"
		//  1456: new             Ljava/lang/StringBuilder;
		//  1459: dup
		//  1460: ldc_w           "group- record wanna init ,time = "
		//  1463: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1466: invokestatic    java/lang/System.currentTimeMillis:()J
		//  1469: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1472: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1475: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1478: aload_0
		//  1479: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1482: new             Ljava/lang/StringBuilder;
		//  1485: dup
		//  1486: invokestatic    java/lang/System.currentTimeMillis:()J
		//  1489: invokestatic    java/lang/String.valueOf:(J)Ljava/lang/String;
		//  1492: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1495: ldc_w           "AudioRecord   new AudioRecord() min = "
		//  1498: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//  1501: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1504: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1507: invokestatic    com/zed3/media/MicInstanceFacotory.isRecordUnInited:()Z
		//  1510: ifeq            1605
		//  1513: aload_0
		//  1514: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  1517: aload_0
		//  1518: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1521: ldc_w           "AudioRecord  fail \u5f55\u97f3\u5668\u521d\u59cb\u5316\u5931\u8d25 "
		//  1524: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  1527: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//  1530: invokestatic    com/zed3/sipua/ui/Receiver.engine:(Landroid/content/Context;)Lcom/zed3/sipua/SipdroidEngine;
		//  1533: invokevirtual   com/zed3/sipua/SipdroidEngine.rejectcall:()V
		//  1536: aload_0
		//  1537: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  1540: aload_0
		//  1541: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1544: ldc_w           "AudioRecord  rejectcall \u62d2\u7edd\u901a\u8bdd "
		//  1547: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  1550: invokestatic    com/zed3/media/MicInstanceFacotory.releaseAudioRecord:()V
		//  1553: goto            369
		//  1556: invokestatic    com/zed3/media/MicInstanceFacotory.isAudioRecorderEmpty:()Z
		//  1559: ifeq            1287
		//  1562: invokestatic    com/zed3/media/TipSoundPlayer.getInstance:()Lcom/zed3/media/TipSoundPlayer;
		//  1565: getstatic       com/zed3/media/TipSoundPlayer.Sound.PTT_ACCEPT:Lcom/zed3/media/TipSoundPlayer.Sound;
		//  1568: invokevirtual   com/zed3/media/TipSoundPlayer.play:(Lcom/zed3/media/TipSoundPlayer.Sound;)V
		//  1571: ldc_w           "huangfujian"
		//  1574: new             Ljava/lang/StringBuilder;
		//  1577: dup
		//  1578: ldc_w           "TipSoundPlayer2 PTT_ACCEPT::"
		//  1581: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1584: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  1587: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1590: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1593: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  1596: ldc2_w          280
		//  1599: invokestatic    android/os/SystemClock.sleep:(J)V
		//  1602: goto            1287
		//  1605: invokestatic    com/zed3/media/MicInstanceFacotory.startRecording:()V
		//  1608: ldc             "htag"
		//  1610: new             Ljava/lang/StringBuilder;
		//  1613: dup
		//  1614: ldc_w           "group- record startRecording ,time = "
		//  1617: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1620: invokestatic    java/lang/System.currentTimeMillis:()J
		//  1623: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  1626: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1629: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1632: invokestatic    com/zed3/sipua/ui/Settings.getMicGain:()F
		//  1635: ldc_w           10.0
		//  1638: fmul
		//  1639: f2i
		//  1640: istore          4
		//  1642: aload_0
		//  1643: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//  1646: ifne            1656
		//  1649: getstatic       com/zed3/sipua/ui/Receiver.call_state:I
		//  1652: iconst_4
		//  1653: if_icmpne       1693
		//  1656: getstatic       com/zed3/sipua/ui/Receiver.call_state:I
		//  1659: iconst_4
		//  1660: if_icmpne       1666
		//  1663: invokestatic    com/zed3/media/RtpStreamReceiver_group.restoreMode:()V
		//  1666: invokestatic    com/zed3/media/MicInstanceFacotory.stop:()V
		//  1669: aload_0
		//  1670: getfield        com/zed3/media/RtpStreamSender_group.running:Z
		//  1673: ifeq            1690
		//  1676: aload_0
		//  1677: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//  1680: ifne            2189
		//  1683: getstatic       com/zed3/sipua/ui/Receiver.call_state:I
		//  1686: iconst_4
		//  1687: if_icmpeq       2189
		//  1690: invokestatic    com/zed3/media/MicInstanceFacotory.startRecording:()V
		//  1693: aload_0
		//  1694: getfield        com/zed3/media/RtpStreamSender_group.dtmf:Ljava/lang/String;
		//  1697: invokevirtual   java/lang/String.length:()I
		//  1700: ifeq            1805
		//  1703: bipush          16
		//  1705: newarray        B
		//  1707: astore          23
		//  1709: new             Lcom/zed3/net/RtpPacket;
		//  1712: dup
		//  1713: aload           23
		//  1715: iconst_0
		//  1716: ldc_w           "0"
		//  1719: invokespecial   com/zed3/net/RtpPacket.<init>:([BILjava/lang/String;)V
		//  1722: astore          24
		//  1724: aload           24
		//  1726: aload_0
		//  1727: getfield        com/zed3/media/RtpStreamSender_group.dtmf_payload_type:I
		//  1730: invokevirtual   com/zed3/net/RtpPacket.setPayloadType:(I)V
		//  1733: aload           24
		//  1735: iconst_4
		//  1736: invokevirtual   com/zed3/net/RtpPacket.setPayloadLength:(I)V
		//  1739: aload           24
		//  1741: aload           19
		//  1743: invokevirtual   com/zed3/net/RtpPacket.getSscr:()J
		//  1746: invokevirtual   com/zed3/net/RtpPacket.setSscr:(J)V
		//  1749: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  1752: lstore          12
		//  1754: iconst_0
		//  1755: istore          4
		//  1757: iload           4
		//  1759: bipush          6
		//  1761: if_icmplt       2208
		//  1764: iconst_0
		//  1765: istore          4
		//  1767: iload           4
		//  1769: iconst_3
		//  1770: if_icmplt       2370
		//  1773: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  1776: ldc2_w          160
		//  1779: ladd
		//  1780: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  1783: aload_0
		//  1784: aload_0
		//  1785: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  1788: iconst_1
		//  1789: iadd
		//  1790: putfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  1793: aload_0
		//  1794: aload_0
		//  1795: getfield        com/zed3/media/RtpStreamSender_group.dtmf:Ljava/lang/String;
		//  1798: iconst_1
		//  1799: invokevirtual   java/lang/String.substring:(I)Ljava/lang/String;
		//  1802: putfield        com/zed3/media/RtpStreamSender_group.dtmf:Ljava/lang/String;
		//  1805: getstatic       android/os/Build.VERSION.SDK_INT:I
		//  1808: bipush          20
		//  1810: if_icmple       2468
		//  1813: iconst_0
		//  1814: istore          4
		//  1816: aload           21
		//  1818: iload           4
		//  1820: aload_0
		//  1821: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  1824: invokestatic    com/zed3/media/MicInstanceFacotory.read:([SII)I
		//  1827: istore          7
		//  1829: aload_0
		//  1830: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  1833: new             Ljava/lang/StringBuilder;
		//  1836: dup
		//  1837: ldc_w           "readNum = "
		//  1840: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  1843: iload           7
		//  1845: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  1848: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  1851: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  1854: iload           7
		//  1856: ifle            362
		//  1859: aload_0
		//  1860: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  1863: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  1866: invokeinterface com/zed3/codecs/Codec.isValid:()Z
		//  1871: ifeq            362
		//  1874: iload_3
		//  1875: iconst_1
		//  1876: iadd
		//  1877: istore          6
		//  1879: iload           6
		//  1881: istore_3
		//  1882: invokestatic    com/zed3/sipua/ui/Receiver.GetCurUA:()Lcom/zed3/sipua/UserAgent;
		//  1885: invokevirtual   com/zed3/sipua/UserAgent.IsPttMode:()Z
		//  1888: ifeq            1972
		//  1891: iload           6
		//  1893: istore_3
		//  1894: iload           6
		//  1896: sipush          300
		//  1899: aload_0
		//  1900: getfield        com/zed3/media/RtpStreamSender_group.callptime:I
		//  1903: idiv
		//  1904: if_icmpne       1972
		//  1907: iconst_0
		//  1908: istore          8
		//  1910: aload_0
		//  1911: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  1914: newarray        S
		//  1916: astore          23
		//  1918: aload           21
		//  1920: iload           4
		//  1922: aload           23
		//  1924: iconst_0
		//  1925: aload_0
		//  1926: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  1929: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
		//  1932: aload           23
		//  1934: invokestatic    com/zed3/utils/Tools.shortArray2ByteArray:([S)[B
		//  1937: astore          23
		//  1939: iload           8
		//  1941: istore_3
		//  1942: aload           23
		//  1944: arraylength
		//  1945: ifle            1972
		//  1948: iconst_0
		//  1949: istore          6
		//  1951: iconst_0
		//  1952: istore_3
		//  1953: iload_3
		//  1954: aload           23
		//  1956: arraylength
		//  1957: if_icmplt       2501
		//  1960: iload           6
		//  1962: aload           23
		//  1964: arraylength
		//  1965: idiv
		//  1966: invokestatic    com/zed3/utils/MyHandler.sendMessage:(I)V
		//  1969: iload           8
		//  1971: istore_3
		//  1972: aload_0
		//  1973: getfield        com/zed3/media/RtpStreamSender_group.call_recorder:Lcom/zed3/media/CallRecorder;
		//  1976: ifnull          2006
		//  1979: aload_0
		//  1980: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  1983: aload_0
		//  1984: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  1987: ldc_w           "debug 707"
		//  1990: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  1993: aload_0
		//  1994: getfield        com/zed3/media/RtpStreamSender_group.call_recorder:Lcom/zed3/media/CallRecorder;
		//  1997: aload           21
		//  1999: iload           4
		//  2001: iload           7
		//  2003: invokevirtual   com/zed3/media/CallRecorder.writeOutgoing:([SII)V
		//  2006: aload_0
		//  2007: aload           21
		//  2009: iload           4
		//  2011: iload           7
		//  2013: getstatic       com/zed3/location/MemoryMg.Voice:F
		//  2016: invokevirtual   com/zed3/media/RtpStreamSender_group.micGain:([SIIF)V
		//  2019: getstatic       com/zed3/sipua/ui/Receiver.call_state:I
		//  2022: iconst_3
		//  2023: if_icmpeq       2520
		//  2026: getstatic       com/zed3/sipua/ui/Receiver.call_state:I
		//  2029: iconst_2
		//  2030: if_icmpeq       2520
		//  2033: aload           18
		//  2035: ifnull          2520
		//  2038: getstatic       com/zed3/sipua/UserAgent.ua_ptt_mode:Z
		//  2041: ifne            2520
		//  2044: aload_0
		//  2045: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  2048: aload_0
		//  2049: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  2052: ldc_w           "debug 744"
		//  2055: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  2058: aload           18
		//  2060: invokevirtual   java/io/InputStream.available:()I
		//  2063: iload           7
		//  2065: aload_0
		//  2066: getfield        com/zed3/media/RtpStreamSender_group.mu:I
		//  2069: idiv
		//  2070: if_icmpge       2078
		//  2073: aload           18
		//  2075: invokevirtual   java/io/InputStream.reset:()V
		//  2078: aload           18
		//  2080: aload           22
		//  2082: bipush          12
		//  2084: iload           7
		//  2086: aload_0
		//  2087: getfield        com/zed3/media/RtpStreamSender_group.mu:I
		//  2090: idiv
		//  2091: invokevirtual   java/io/InputStream.read:([BII)I
		//  2094: pop
		//  2095: iload           7
		//  2097: istore          4
		//  2099: aload_0
		//  2100: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  2103: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  2106: invokeinterface com/zed3/codecs/Codec.number:()I
		//  2111: bipush          8
		//  2113: if_icmpeq       2150
		//  2116: aload           22
		//  2118: aload           21
		//  2120: iload           7
		//  2122: aload_0
		//  2123: getfield        com/zed3/media/RtpStreamSender_group.mu:I
		//  2126: invokestatic    com/zed3/codecs/G711.alaw2linear:([B[SII)V
		//  2129: aload_0
		//  2130: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  2133: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  2136: aload           21
		//  2138: iconst_0
		//  2139: aload           22
		//  2141: iload           7
		//  2143: invokeinterface com/zed3/codecs/Codec.encode:([SI[BI)I
		//  2148: istore          4
		//  2150: iload           4
		//  2152: ifne            2594
		//  2155: aload_0
		//  2156: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  2159: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  2162: invokeinterface com/zed3/codecs/Codec.number:()I
		//  2167: bipush          9
		//  2169: if_icmpne       2575
		//  2172: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2175: aload_0
		//  2176: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2179: iconst_2
		//  2180: idiv
		//  2181: i2l
		//  2182: ladd
		//  2183: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2186: goto            362
		//  2189: ldc2_w          1000
		//  2192: invokestatic    com/zed3/media/RtpStreamSender_group.sleep:(J)V
		//  2195: goto            1669
		//  2198: astore          23
		//  2200: aload           23
		//  2202: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//  2205: goto            1690
		//  2208: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2211: ldc2_w          160
		//  2214: ladd
		//  2215: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2218: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2221: lload           12
		//  2223: lsub
		//  2224: l2i
		//  2225: istore          6
		//  2227: aload_0
		//  2228: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  2231: istore          7
		//  2233: aload_0
		//  2234: iload           7
		//  2236: iconst_1
		//  2237: iadd
		//  2238: putfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  2241: aload           24
		//  2243: iload           7
		//  2245: invokevirtual   com/zed3/net/RtpPacket.setSequenceNumber:(I)V
		//  2248: aload           24
		//  2250: lload           12
		//  2252: invokevirtual   com/zed3/net/RtpPacket.setTimestamp:(J)V
		//  2255: aload           23
		//  2257: bipush          12
		//  2259: getstatic       com/zed3/media/RtpStreamSender_group.rtpEventMap:Ljava/util/HashMap;
		//  2262: aload_0
		//  2263: getfield        com/zed3/media/RtpStreamSender_group.dtmf:Ljava/lang/String;
		//  2266: iconst_0
		//  2267: invokevirtual   java/lang/String.charAt:(I)C
		//  2270: invokestatic    java/lang/Character.valueOf:(C)Ljava/lang/Character;
		//  2273: invokevirtual   java/util/HashMap.get:(Ljava/lang/Object;)Ljava/lang/Object;
		//  2276: checkcast       Ljava/lang/Byte;
		//  2279: invokevirtual   java/lang/Byte.byteValue:()B
		//  2282: bastore
		//  2283: aload           23
		//  2285: bipush          13
		//  2287: bipush          10
		//  2289: bastore
		//  2290: aload           23
		//  2292: bipush          14
		//  2294: iload           6
		//  2296: bipush          8
		//  2298: ishr
		//  2299: i2b
		//  2300: bastore
		//  2301: aload           23
		//  2303: bipush          15
		//  2305: iload           6
		//  2307: i2b
		//  2308: bastore
		//  2309: aload_0
		//  2310: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  2313: aload           24
		//  2315: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//  2318: pop
		//  2319: aload_0
		//  2320: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  2323: ldc_w           "RtpStreamSender send"
		//  2326: ldc_w           "rtp_socket.send(dt_packet)"
		//  2329: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  2332: ldc2_w          20
		//  2335: invokestatic    com/zed3/media/RtpStreamSender_group.sleep:(J)V
		//  2338: aload_0
		//  2339: getfield        com/zed3/media/RtpStreamSender_group.needLog:Z
		//  2342: ldc_w           "RtpStreamSender send"
		//  2345: ldc_w           "rtp_socket.send(dt_packet)"
		//  2348: invokestatic    com/zed3/log/Logger.i:(ZLjava/lang/String;Ljava/lang/String;)V
		//  2351: iload           4
		//  2353: iconst_1
		//  2354: iadd
		//  2355: istore          4
		//  2357: goto            1757
		//  2360: astore          25
		//  2362: aload           25
		//  2364: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  2367: goto            2351
		//  2370: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2373: lload           12
		//  2375: lsub
		//  2376: l2i
		//  2377: istore          6
		//  2379: aload           24
		//  2381: aload_0
		//  2382: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  2385: invokevirtual   com/zed3/net/RtpPacket.setSequenceNumber:(I)V
		//  2388: aload           24
		//  2390: lload           12
		//  2392: invokevirtual   com/zed3/net/RtpPacket.setTimestamp:(J)V
		//  2395: aload           23
		//  2397: bipush          12
		//  2399: getstatic       com/zed3/media/RtpStreamSender_group.rtpEventMap:Ljava/util/HashMap;
		//  2402: aload_0
		//  2403: getfield        com/zed3/media/RtpStreamSender_group.dtmf:Ljava/lang/String;
		//  2406: iconst_0
		//  2407: invokevirtual   java/lang/String.charAt:(I)C
		//  2410: invokestatic    java/lang/Character.valueOf:(C)Ljava/lang/Character;
		//  2413: invokevirtual   java/util/HashMap.get:(Ljava/lang/Object;)Ljava/lang/Object;
		//  2416: checkcast       Ljava/lang/Byte;
		//  2419: invokevirtual   java/lang/Byte.byteValue:()B
		//  2422: bastore
		//  2423: aload           23
		//  2425: bipush          13
		//  2427: bipush          -118
		//  2429: bastore
		//  2430: aload           23
		//  2432: bipush          14
		//  2434: iload           6
		//  2436: bipush          8
		//  2438: ishr
		//  2439: i2b
		//  2440: bastore
		//  2441: aload           23
		//  2443: bipush          15
		//  2445: iload           6
		//  2447: i2b
		//  2448: bastore
		//  2449: aload_0
		//  2450: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  2453: aload           24
		//  2455: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//  2458: pop
		//  2459: iload           4
		//  2461: iconst_1
		//  2462: iadd
		//  2463: istore          4
		//  2465: goto            1767
		//  2468: getstatic       com/zed3/media/RtpStreamSender_group.delay:I
		//  2471: aload_0
		//  2472: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//  2475: imul
		//  2476: aload_0
		//  2477: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2480: imul
		//  2481: iload           5
		//  2483: iadd
		//  2484: aload_0
		//  2485: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2488: aload_0
		//  2489: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//  2492: iconst_1
		//  2493: iadd
		//  2494: imul
		//  2495: irem
		//  2496: istore          4
		//  2498: goto            1816
		//  2501: iload           6
		//  2503: aload           23
		//  2505: iload_3
		//  2506: baload
		//  2507: invokestatic    java/lang/Math.abs:(I)I
		//  2510: iadd
		//  2511: istore          6
		//  2513: iload_3
		//  2514: iconst_1
		//  2515: iadd
		//  2516: istore_3
		//  2517: goto            1953
		//  2520: getstatic       android/os/Build.VERSION.SDK_INT:I
		//  2523: bipush          20
		//  2525: if_icmple       2556
		//  2528: iconst_0
		//  2529: istore          4
		//  2531: aload_0
		//  2532: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  2535: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  2538: aload           21
		//  2540: iload           4
		//  2542: aload           22
		//  2544: iload           7
		//  2546: invokeinterface com/zed3/codecs/Codec.encode:([SI[BI)I
		//  2551: istore          4
		//  2553: goto            2150
		//  2556: iload           5
		//  2558: aload_0
		//  2559: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2562: aload_0
		//  2563: getfield        com/zed3/media/RtpStreamSender_group.frame_rate:I
		//  2566: iconst_1
		//  2567: iadd
		//  2568: imul
		//  2569: irem
		//  2570: istore          4
		//  2572: goto            2531
		//  2575: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2578: aload_0
		//  2579: getfield        com/zed3/media/RtpStreamSender_group.mframeNumber:I
		//  2582: sipush          160
		//  2585: imul
		//  2586: i2l
		//  2587: ladd
		//  2588: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2591: goto            362
		//  2594: iload           5
		//  2596: aload_0
		//  2597: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2600: iadd
		//  2601: istore          5
		//  2603: aload_0
		//  2604: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//  2607: ifne            2631
		//  2610: aload_0
		//  2611: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  2614: istore          6
		//  2616: aload_0
		//  2617: iload           6
		//  2619: iconst_1
		//  2620: iadd
		//  2621: putfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  2624: aload           19
		//  2626: iload           6
		//  2628: invokevirtual   com/zed3/net/RtpPacket.setSequenceNumber:(I)V
		//  2631: aload           19
		//  2633: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2636: invokevirtual   com/zed3/net/RtpPacket.setTimestamp:(J)V
		//  2639: aload           19
		//  2641: iload           4
		//  2643: invokevirtual   com/zed3/net/RtpPacket.setPayloadLength:(I)V
		//  2646: aload_0
		//  2647: getfield        com/zed3/media/RtpStreamSender_group.sndSuspend:Z
		//  2650: ifne            2660
		//  2653: aload_0
		//  2654: invokestatic    java/lang/System.currentTimeMillis:()J
		//  2657: putfield        com/zed3/media/RtpStreamSender_group.SuspendTime:J
		//  2660: aload           19
		//  2662: invokevirtual   com/zed3/net/RtpPacket.getPackedTime:()J
		//  2665: aload_0
		//  2666: getfield        com/zed3/media/RtpStreamSender_group.SuspendTime:J
		//  2669: lcmp
		//  2670: ifgt            3231
		//  2673: aload_0
		//  2674: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//  2677: ifne            3065
		//  2680: aload_0
		//  2681: getfield        com/zed3/media/RtpStreamSender_group.bLock:[B
		//  2684: astore          22
		//  2686: aload           22
		//  2688: monitorenter
		//  2689: aload_0
		//  2690: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  2693: new             Ljava/lang/StringBuilder;
		//  2696: dup
		//  2697: ldc_w           "to offer , rtp_packet.seq = "
		//  2700: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  2703: aload           19
		//  2705: invokevirtual   com/zed3/net/RtpPacket.getSequenceNumber:()I
		//  2708: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  2711: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  2714: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2717: aload           19
		//  2719: ifnull          2760
		//  2722: aload_0
		//  2723: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  2726: new             Ljava/lang/StringBuilder;
		//  2729: dup
		//  2730: ldc_w           "send pp 1 seqNum = "
		//  2733: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  2736: aload           19
		//  2738: invokevirtual   com/zed3/net/RtpPacket.getSequenceNumber:()I
		//  2741: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  2744: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  2747: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2750: aload_0
		//  2751: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  2754: aload           19
		//  2756: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//  2759: pop
		//  2760: ldc             "htag"
		//  2762: new             Ljava/lang/StringBuilder;
		//  2765: dup
		//  2766: ldc_w           "group- record offer packet ,time = "
		//  2769: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  2772: invokestatic    java/lang/System.currentTimeMillis:()J
		//  2775: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  2778: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  2781: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2784: invokestatic    java/lang/System.currentTimeMillis:()J
		//  2787: lstore          12
		//  2789: aload_0
		//  2790: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  2793: new             Ljava/lang/StringBuilder;
		//  2796: dup
		//  2797: ldc_w           "offer  curTime = "
		//  2800: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  2803: lload           12
		//  2805: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  2808: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  2811: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2814: iload_1
		//  2815: iconst_2
		//  2816: if_icmpne       2872
		//  2819: aload_0
		//  2820: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  2823: ldc_w           "m = 2,offer same package"
		//  2826: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2829: aload           19
		//  2831: ifnull          2872
		//  2834: aload_0
		//  2835: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  2838: new             Ljava/lang/StringBuilder;
		//  2841: dup
		//  2842: ldc_w           "send pp 2 seqNum = "
		//  2845: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  2848: aload           19
		//  2850: invokevirtual   com/zed3/net/RtpPacket.getSequenceNumber:()I
		//  2853: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//  2856: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  2859: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  2862: aload_0
		//  2863: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  2866: aload           19
		//  2868: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//  2871: pop
		//  2872: aload           22
		//  2874: monitorexit
		//  2875: aload_0
		//  2876: lconst_0
		//  2877: putfield        com/zed3/media/RtpStreamSender_group.mutedTimeMillion:J
		//  2880: aload_0
		//  2881: getfield        com/zed3/media/RtpStreamSender_group.muted:Z
		//  2884: ifne            2918
		//  2887: aload_0
		//  2888: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  2891: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  2894: invokeinterface com/zed3/codecs/Codec.number:()I
		//  2899: bipush          9
		//  2901: if_icmpne       3244
		//  2904: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2907: aload_0
		//  2908: getfield        com/zed3/media/RtpStreamSender_group.frame_size:I
		//  2911: iconst_2
		//  2912: idiv
		//  2913: i2l
		//  2914: ladd
		//  2915: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  2918: getstatic       com/zed3/media/RtpStreamReceiver_group.good:F
		//  2921: fconst_0
		//  2922: fcmpl
		//  2923: ifeq            3272
		//  2926: getstatic       com/zed3/media/RtpStreamReceiver_group.loss:F
		//  2929: getstatic       com/zed3/media/RtpStreamReceiver_group.good:F
		//  2932: fdiv
		//  2933: f2d
		//  2934: ldc2_w          0.01
		//  2937: dcmpl
		//  2938: ifle            3272
		//  2941: lload           10
		//  2943: lstore          12
		//  2945: iload           17
		//  2947: ifeq            2988
		//  2950: lload           10
		//  2952: lstore          12
		//  2954: getstatic       com/zed3/sipua/ui/Receiver.on_wlan:Z
		//  2957: ifeq            2988
		//  2960: lload           10
		//  2962: lstore          12
		//  2964: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  2967: lload           10
		//  2969: lsub
		//  2970: ldc2_w          10000
		//  2973: lcmp
		//  2974: ifle            2988
		//  2977: aload           20
		//  2979: invokevirtual   android/net/wifi/WifiManager.startScan:()Z
		//  2982: pop
		//  2983: invokestatic    android/os/SystemClock.elapsedRealtime:()J
		//  2986: lstore          12
		//  2988: iload           16
		//  2990: ifeq            3263
		//  2993: getstatic       com/zed3/media/RtpStreamSender_group.delay:I
		//  2996: ifne            3263
		//  2999: aload_0
		//  3000: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  3003: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  3006: invokeinterface com/zed3/codecs/Codec.number:()I
		//  3011: ifeq            3048
		//  3014: aload_0
		//  3015: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  3018: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  3021: invokeinterface com/zed3/codecs/Codec.number:()I
		//  3026: bipush          8
		//  3028: if_icmpeq       3048
		//  3031: aload_0
		//  3032: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  3035: getfield        com/zed3/codecs/Codecs.Map.codec:Lcom/zed3/codecs/Codec;
		//  3038: invokeinterface com/zed3/codecs/Codec.number:()I
		//  3043: bipush          9
		//  3045: if_icmpne       3263
		//  3048: iconst_2
		//  3049: istore_1
		//  3050: lload           12
		//  3052: lstore          10
		//  3054: goto            362
		//  3057: astore          19
		//  3059: aload           22
		//  3061: monitorexit
		//  3062: aload           19
		//  3064: athrow
		//  3065: aload_0
		//  3066: getfield        com/zed3/media/RtpStreamSender_group.mutedTimeMillion:J
		//  3069: lconst_0
		//  3070: lcmp
		//  3071: ifne            3081
		//  3074: aload_0
		//  3075: invokestatic    java/lang/System.currentTimeMillis:()J
		//  3078: putfield        com/zed3/media/RtpStreamSender_group.mutedTimeMillion:J
		//  3081: invokestatic    java/lang/System.currentTimeMillis:()J
		//  3084: aload_0
		//  3085: getfield        com/zed3/media/RtpStreamSender_group.mutedTimeMillion:J
		//  3088: lsub
		//  3089: ldc2_w          20000
		//  3092: lcmp
		//  3093: ifle            2880
		//  3096: aload_0
		//  3097: lconst_0
		//  3098: putfield        com/zed3/media/RtpStreamSender_group.mutedTimeMillion:J
		//  3101: aload_0
		//  3102: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  3105: new             Ljava/lang/StringBuilder;
		//  3108: dup
		//  3109: ldc_w           "send audio null packet:"
		//  3112: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//  3115: invokestatic    java/lang/System.currentTimeMillis:()J
		//  3118: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//  3121: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//  3124: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//  3127: ldc_w           "0"
		//  3130: invokevirtual   java/lang/String.getBytes:()[B
		//  3133: astore          19
		//  3135: bipush          13
		//  3137: newarray        B
		//  3139: astore          22
		//  3141: aload           19
		//  3143: iconst_0
		//  3144: aload           22
		//  3146: bipush          12
		//  3148: iconst_1
		//  3149: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
		//  3152: new             Lcom/zed3/net/RtpPacket;
		//  3155: dup
		//  3156: aload           22
		//  3158: iconst_0
		//  3159: ldc_w           "0"
		//  3162: invokespecial   com/zed3/net/RtpPacket.<init>:([BILjava/lang/String;)V
		//  3165: astore          19
		//  3167: aload           19
		//  3169: aload_0
		//  3170: getfield        com/zed3/media/RtpStreamSender_group.p_type:Lcom/zed3/codecs/Codecs.Map;
		//  3173: getfield        com/zed3/codecs/Codecs.Map.number:I
		//  3176: invokevirtual   com/zed3/net/RtpPacket.setPayloadType:(I)V
		//  3179: aload           19
		//  3181: iconst_1
		//  3182: invokevirtual   com/zed3/net/RtpPacket.setPayloadLength:(I)V
		//  3185: aload           19
		//  3187: aload_0
		//  3188: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  3191: invokevirtual   com/zed3/net/RtpPacket.setSequenceNumber:(I)V
		//  3194: aload           19
		//  3196: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  3199: invokevirtual   com/zed3/net/RtpPacket.setTimestamp:(J)V
		//  3202: aload_0
		//  3203: aload_0
		//  3204: getfield        com/zed3/media/RtpStreamSender_group.rtp_socket:Lcom/zed3/net/RtpSocket;
		//  3207: aload           19
		//  3209: invokevirtual   com/zed3/net/RtpSocket.send:(Lcom/zed3/net/RtpPacket;)I
		//  3212: bipush          42
		//  3214: iadd
		//  3215: putfield        com/zed3/media/RtpStreamSender_group.length:I
		//  3218: aload_0
		//  3219: aload_0
		//  3220: getfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  3223: iconst_1
		//  3224: iadd
		//  3225: putfield        com/zed3/media/RtpStreamSender_group.seqn:I
		//  3228: goto            2880
		//  3231: aload_0
		//  3232: getfield        com/zed3/media/RtpStreamSender_group.HTag:Ljava/lang/String;
		//  3235: ldc_w           "never called!!!!!!!!!!"
		//  3238: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  3241: goto            2880
		//  3244: getstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  3247: aload_0
		//  3248: getfield        com/zed3/media/RtpStreamSender_group.mframeNumber:I
		//  3251: sipush          160
		//  3254: imul
		//  3255: i2l
		//  3256: ladd
		//  3257: putstatic       com/zed3/media/RtpStreamSender_group.time:J
		//  3260: goto            2918
		//  3263: iconst_1
		//  3264: istore_1
		//  3265: lload           12
		//  3267: lstore          10
		//  3269: goto            362
		//  3272: iconst_1
		//  3273: istore_1
		//  3274: goto            362
		//  3277: aload_0
		//  3278: getfield        com/zed3/media/RtpStreamSender_group.tag:Ljava/lang/String;
		//  3281: ldc_w           "free error"
		//  3284: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//  3287: goto            396
		//  3290: astore          23
		//  3292: goto            2095
		//  3295: astore          25
		//  3297: goto            2459
		//  3300: astore          19
		//  3302: goto            360
		//  3305: astore          19
		//  3307: goto            2880
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  --------------------------------
		//  342    356    3300   3305   Ljava/io/IOException;
		//  699    705    708    721    Ljava/lang/InterruptedException;
		//  947    966    1021   1031   Ljava/io/IOException;
		//  1068   1127   1142   1203   Ljava/lang/Exception;
		//  2058   2078   3290   3295   Ljava/io/IOException;
		//  2078   2095   3290   3295   Ljava/io/IOException;
		//  2189   2195   2198   2208   Ljava/lang/InterruptedException;
		//  2309   2351   2360   2370   Ljava/lang/Exception;
		//  2449   2459   3295   3300   Ljava/lang/Exception;
		//  2646   2660   3305   3310   Ljava/lang/Exception;
		//  2660   2689   3305   3310   Ljava/lang/Exception;
		//  2689   2717   3057   3065   Any
		//  2722   2760   3057   3065   Any
		//  2760   2814   3057   3065   Any
		//  2819   2829   3057   3065   Any
		//  2834   2872   3057   3065   Any
		//  2872   2875   3057   3065   Any
		//  2875   2880   3305   3310   Ljava/lang/Exception;
		//  3059   3062   3057   3065   Any
		//  3062   3065   3305   3310   Ljava/lang/Exception;
		//  3065   3081   3305   3310   Ljava/lang/Exception;
		//  3081   3228   3305   3310   Ljava/lang/Exception;
		//  3231   3241   3305   3310   Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_2459:
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

	public void sendDTMF(final char c) {
		this.dtmf = String.valueOf(this.dtmf) + c;
	}

	public void setDTMFpayloadType(final int dtmf_payload_type) {
		this.dtmf_payload_type = dtmf_payload_type;
	}

	public void setSyncAdj(final int sync_adj) {
		this.sync_adj = sync_adj;
	}

	public void setUserAgentHandler(final Handler cmdhHandler) {
		this.cmdhHandler = cmdhHandler;
	}
}
