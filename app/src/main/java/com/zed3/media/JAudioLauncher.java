package com.zed3.media;

import android.os.Handler;
import android.preference.PreferenceManager;

import com.zed3.codecs.Codecs;
import com.zed3.log.MyLog;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.video.VideoManagerService;

import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;

public class JAudioLauncher implements MediaLauncher {
	public static final String TONE = "TONE";
	public static double tone_amp;
	public static int tone_freq;
	boolean big_endian;
	public String callId;
	int dir;
	int frame_rate;
	int frame_size;
	boolean isVideo;
	Log log;
	Process media_process;
	private RtpStreamReceiver_group receiver_group;
	private RtpStreamReceiver_signal receiver_signal;
	int sample_rate;
	int sample_size;
	private RtpStreamSender_group sender_group;
	private RtpStreamSender_signal sender_signal;
	boolean signed;
	SipdroidSocket socket;
	private String tag;
	boolean useDTMF;

	static {
		JAudioLauncher.tone_freq = 100;
		JAudioLauncher.tone_amp = 1.0;
	}

	public JAudioLauncher(final SipdroidSocket socket, final String s, final int n, final int dir, final String s2, final String s3, final int n2, final int n3, final int n4, final Log log, final Codecs.Map map, final int n5, final String callId, final int n6, final boolean isVideo) {
		this.isVideo = false;
		this.log = null;
		this.sample_rate = 8000;
		this.sample_size = 1;
		this.frame_size = 160;
		this.frame_rate = 50;
		this.signed = false;
		this.big_endian = false;
		this.callId = "";
		this.media_process = null;
		this.socket = null;
		this.sender_group = null;
		this.sender_signal = null;
		this.receiver_group = null;
		this.receiver_signal = null;
		this.useDTMF = false;
		this.tag = "JAudioLauncher";
		this.log = log;
		this.frame_rate = n2 / n4;
		this.callId = callId;
		MyLog.i(this.tag, "frame_rate = " + n4);
		this.isVideo = isVideo;
		while (true) {
			Label_0414:
			{
				if (n5 == 0) {
					break Label_0414;
				}
				final boolean useDTMF = true;
				this.useDTMF = useDTMF;
				CallRecorder callRecorder = null;
				try {
					if (PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getBoolean("callrecord", false)) {
						callRecorder = new CallRecorder(null, map.codec.samp_rate());
					}
					this.socket = socket;
					android.util.Log.i("jiangkai", "Prot   " + socket.getLocalPort());
					this.dir = dir;
					if (this.dir == 0) {
						this.printLog("new audio sender to " + s + ":" + n, 3);
						(this.sender_signal = new RtpStreamSender_signal(true, map, this.frame_rate, n4, socket, s, n, callRecorder, isVideo)).setSyncAdj(2);
						this.sender_signal.setDTMFpayloadType(n5);
						this.receiver_signal = new RtpStreamReceiver_signal(socket, map, callRecorder, isVideo);
						return;
					}
					(this.sender_group = new RtpStreamSender_group(true, map, this.frame_rate, n4, socket, s, n, callRecorder, n6)).setSyncAdj(2);
					this.sender_group.setDTMFpayloadType(n5);
					this.receiver_group = new RtpStreamReceiver_group(socket, map, callRecorder, n6);
					return;
				} catch (Exception ex) {
					this.printException(ex, 1);
					ex.printStackTrace();
					return;
				}
			}
			final boolean useDTMF = false;
			continue;
		}
	}

	private void printLog(final String s) {
		this.printLog(s, 1);
	}

	private void printLog(final String s, final int n) {
		if (SipUAApp.getIsClosed()) {
			if (this.log != null) {
				this.log.println("AudioLauncher: " + s, SipStack.LOG_LEVEL_UA + n);
			}
			if (n <= 1) {
				System.out.println("AudioLauncher: " + s);
			}
		}
	}

	@Override
	public void bluetoothMedia() {
		if (this.receiver_group != null) {
			this.receiver_group.bluetooth();
		}
		if (this.receiver_signal != null) {
			this.receiver_signal.bluetooth();
		}
	}

	@Override
	public boolean muteMedia() {
		if (Receiver.GetCurUA().IsPttMode()) {
			if (this.sender_group != null) {
				return this.sender_group.mute();
			}
		} else if (this.sender_signal != null) {
			return this.sender_signal.mute();
		}
		return false;
	}

	void printException(final Exception ex, final int n) {
	}

	@Override
	public boolean sendDTMF(final char c) {
		if (!this.useDTMF) {
			return false;
		}
		if (this.sender_signal != null) {
			this.sender_signal.sendDTMF(c);
		}
		return true;
	}

	public void setUserAgentHandler(final Handler userAgentHandler) {
		if (userAgentHandler != null && this.sender_group != null) {
			this.sender_group.setUserAgentHandler(userAgentHandler);
		}
	}

	@Override
	public int speakerMedia(final int n) {
		if (Receiver.GetCurUA().IsPttMode()) {
			if (this.receiver_group != null) {
				return this.receiver_group.speaker(n);
			}
		} else if (this.receiver_signal != null) {
			return this.receiver_signal.speaker(n);
		}
		return 0;
	}

	@Override
	public boolean startMedia() {
		return false;
	}

	public boolean startMedia(final ExtendedCall extendedCall, final int n) {
		if (n != 0 && (this.sender_group == null || this.receiver_group == null)) {
			return false;
		}
		MyLog.i(this.tag, "starting java audio.." + n);
		switch (n) {
			case -1: {
				this.sender_group.SndSuspend();
				this.receiver_group.RcvResume();
				break;
			}
			case 1: {
				this.sender_group.SndResume();
				this.receiver_group.RcvSuspend();
				break;
			}
			case -2: {
				this.sender_group.SndSuspend();
				this.sender_group.PttRelecase();
				this.receiver_group.RcvResume();
				break;
			}
		}
		final CallManager manager = CallManager.getManager();
		boolean b = manager.isAudioCall(extendedCall);
		final boolean videoCall = manager.isVideoCall(extendedCall);
		final boolean videoCallWithAudio = manager.isVideoCallWithAudio(extendedCall);
		if (VideoManagerService.getDefault().existRemoteVideoControl()) {
			b = VideoManagerService.getDefault().getRemoteVideoControlParamter().isVideoCall();
		}
		MyLog.d("videoTrace", "JAudioLauncher#startMedia() enter isAudioCall = " + b);
		MyLog.d("videoTrace", "JAudioLauncher#startMedia() enter isVideoCall = " + videoCall);
		MyLog.d("videoTrace", "JAudioLauncher#startMedia() enter isVideoCallWithAudio = " + videoCallWithAudio);
		if (this.receiver_signal != null && !this.receiver_signal.isRunning() && n == 0 && (b || videoCallWithAudio)) {
			MyLog.i(this.tag, "start receiving");
			MyLog.d("videoTrace", "JAudioLauncher#startMedia() enter receiver_signal start");
			this.receiver_signal.start();
		}
		if (this.sender_signal != null && !this.sender_signal.isRunning() && n == 0 && (b || videoCallWithAudio)) {
			MyLog.i(this.tag, "start sending");
			MyLog.d("videoTrace", "JAudioLauncher#startMedia() enter sender_signal start");
			this.sender_signal.start();
		}
		if (this.receiver_group != null && !this.receiver_group.isRunning() && n != 0) {
			this.receiver_group.start();
		}
		if (this.sender_group != null && !this.sender_group.isRunning() && n != 0) {
			this.sender_group.start();
		}
		return true;
	}

	@Override
	public boolean stopMedia() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: ldc_w           "halting java audio.."
		//     4: iconst_1
		//     5: invokespecial   com/zed3/media/JAudioLauncher.printLog:(Ljava/lang/String;I)V
		//     8: aload_0
		//     9: getfield        com/zed3/media/JAudioLauncher.sender_group:Lcom/zed3/media/RtpStreamSender_group;
		//    12: ifnull          54
		//    15: aload_0
		//    16: getfield        com/zed3/media/JAudioLauncher.sender_group:Lcom/zed3/media/RtpStreamSender_group;
		//    19: invokevirtual   com/zed3/media/RtpStreamSender_group.halt:()V
		//    22: aload_0
		//    23: getfield        com/zed3/media/JAudioLauncher.sender_group:Lcom/zed3/media/RtpStreamSender_group;
		//    26: invokevirtual   com/zed3/media/RtpStreamSender_group.interrupt:()V
		//    29: aload_0
		//    30: getfield        com/zed3/media/JAudioLauncher.sender_group:Lcom/zed3/media/RtpStreamSender_group;
		//    33: ldc2_w          500
		//    36: invokevirtual   com/zed3/media/RtpStreamSender_group.join:(J)V
		//    39: aload_0
		//    40: aconst_null
		//    41: putfield        com/zed3/media/JAudioLauncher.sender_group:Lcom/zed3/media/RtpStreamSender_group;
		//    44: aload_0
		//    45: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//    48: ldc_w           "sender halted"
		//    51: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//    54: aload_0
		//    55: getfield        com/zed3/media/JAudioLauncher.receiver_group:Lcom/zed3/media/RtpStreamReceiver_group;
		//    58: ifnull          100
		//    61: aload_0
		//    62: getfield        com/zed3/media/JAudioLauncher.receiver_group:Lcom/zed3/media/RtpStreamReceiver_group;
		//    65: invokevirtual   com/zed3/media/RtpStreamReceiver_group.halt:()V
		//    68: aload_0
		//    69: getfield        com/zed3/media/JAudioLauncher.receiver_group:Lcom/zed3/media/RtpStreamReceiver_group;
		//    72: invokevirtual   com/zed3/media/RtpStreamReceiver_group.interrupt:()V
		//    75: aload_0
		//    76: getfield        com/zed3/media/JAudioLauncher.receiver_group:Lcom/zed3/media/RtpStreamReceiver_group;
		//    79: ldc2_w          500
		//    82: invokevirtual   com/zed3/media/RtpStreamReceiver_group.join:(J)V
		//    85: aload_0
		//    86: aconst_null
		//    87: putfield        com/zed3/media/JAudioLauncher.receiver_group:Lcom/zed3/media/RtpStreamReceiver_group;
		//    90: aload_0
		//    91: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//    94: ldc_w           "receiver halted"
		//    97: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   100: aload_0
		//   101: getfield        com/zed3/media/JAudioLauncher.sender_signal:Lcom/zed3/media/RtpStreamSender_signal;
		//   104: ifnull          146
		//   107: aload_0
		//   108: getfield        com/zed3/media/JAudioLauncher.sender_signal:Lcom/zed3/media/RtpStreamSender_signal;
		//   111: invokevirtual   com/zed3/media/RtpStreamSender_signal.halt:()V
		//   114: aload_0
		//   115: getfield        com/zed3/media/JAudioLauncher.sender_signal:Lcom/zed3/media/RtpStreamSender_signal;
		//   118: invokevirtual   com/zed3/media/RtpStreamSender_signal.interrupt:()V
		//   121: aload_0
		//   122: getfield        com/zed3/media/JAudioLauncher.sender_signal:Lcom/zed3/media/RtpStreamSender_signal;
		//   125: ldc2_w          500
		//   128: invokevirtual   com/zed3/media/RtpStreamSender_signal.join:(J)V
		//   131: aload_0
		//   132: aconst_null
		//   133: putfield        com/zed3/media/JAudioLauncher.sender_signal:Lcom/zed3/media/RtpStreamSender_signal;
		//   136: aload_0
		//   137: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   140: ldc_w           "sender halted"
		//   143: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   146: aload_0
		//   147: getfield        com/zed3/media/JAudioLauncher.receiver_signal:Lcom/zed3/media/RtpStreamReceiver_signal;
		//   150: ifnull          192
		//   153: aload_0
		//   154: getfield        com/zed3/media/JAudioLauncher.receiver_signal:Lcom/zed3/media/RtpStreamReceiver_signal;
		//   157: invokevirtual   com/zed3/media/RtpStreamReceiver_signal.halt:()V
		//   160: aload_0
		//   161: getfield        com/zed3/media/JAudioLauncher.receiver_signal:Lcom/zed3/media/RtpStreamReceiver_signal;
		//   164: invokevirtual   com/zed3/media/RtpStreamReceiver_signal.interrupt:()V
		//   167: aload_0
		//   168: getfield        com/zed3/media/JAudioLauncher.receiver_signal:Lcom/zed3/media/RtpStreamReceiver_signal;
		//   171: ldc2_w          500
		//   174: invokevirtual   com/zed3/media/RtpStreamReceiver_signal.join:(J)V
		//   177: aload_0
		//   178: aconst_null
		//   179: putfield        com/zed3/media/JAudioLauncher.receiver_signal:Lcom/zed3/media/RtpStreamReceiver_signal;
		//   182: aload_0
		//   183: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   186: ldc_w           "receiver halted"
		//   189: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   192: aload_0
		//   193: getfield        com/zed3/media/JAudioLauncher.socket:Lcom/zed3/net/SipdroidSocket;
		//   196: ifnull          206
		//   199: aload_0
		//   200: getfield        com/zed3/media/JAudioLauncher.socket:Lcom/zed3/net/SipdroidSocket;
		//   203: invokevirtual   com/zed3/net/SipdroidSocket.close:()V
		//   206: iconst_1
		//   207: ireturn
		//   208: astore_1
		//   209: aload_1
		//   210: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//   213: aload_0
		//   214: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   217: new             Ljava/lang/StringBuilder;
		//   220: dup
		//   221: ldc_w           "sender join exception : "
		//   224: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   227: aload_1
		//   228: invokevirtual   java/lang/InterruptedException.toString:()Ljava/lang/String;
		//   231: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   234: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   237: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   240: goto            39
		//   243: astore_1
		//   244: aload_1
		//   245: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//   248: aload_0
		//   249: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   252: new             Ljava/lang/StringBuilder;
		//   255: dup
		//   256: ldc_w           "receiver join exception : "
		//   259: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   262: aload_1
		//   263: invokevirtual   java/lang/InterruptedException.toString:()Ljava/lang/String;
		//   266: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   269: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   272: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   275: goto            85
		//   278: astore_1
		//   279: aload_1
		//   280: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//   283: aload_0
		//   284: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   287: new             Ljava/lang/StringBuilder;
		//   290: dup
		//   291: ldc_w           "sender join exception : "
		//   294: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   297: aload_1
		//   298: invokevirtual   java/lang/InterruptedException.toString:()Ljava/lang/String;
		//   301: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   304: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   307: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   310: goto            131
		//   313: astore_1
		//   314: aload_1
		//   315: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
		//   318: aload_0
		//   319: getfield        com/zed3/media/JAudioLauncher.tag:Ljava/lang/String;
		//   322: new             Ljava/lang/StringBuilder;
		//   325: dup
		//   326: ldc_w           "receiver join exception : "
		//   329: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   332: aload_1
		//   333: invokevirtual   java/lang/InterruptedException.toString:()Ljava/lang/String;
		//   336: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   339: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   342: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   345: goto            177
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  --------------------------------
		//  22     39     208    243    Ljava/lang/InterruptedException;
		//  68     85     243    278    Ljava/lang/InterruptedException;
		//  114    131    278    313    Ljava/lang/InterruptedException;
		//  160    177    313    348    Ljava/lang/InterruptedException;
		//
		// The error that occurred was:
		//
		// java.lang.IndexOutOfBoundsException: Index: 152, Size: 152
		//     at java.util.ArrayList.rangeCheck(ArrayList.java:653)
		//     at java.util.ArrayList.get(ArrayList.java:429)
		//     at com.strobel.decompiler.ast.AstBuilder.convertToAst(AstBuilder.java:3321)
		//     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:113)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
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
}
