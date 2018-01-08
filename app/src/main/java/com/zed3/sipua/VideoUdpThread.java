package com.zed3.sipua;

import android.view.SurfaceView;

import com.video.utils.H264Dec;
import com.video.utils.H264Frame;
import com.video.utils.VideoDecodeBufferManager;
import com.video.utils.VideoDecodeThread;
import com.zed3.audio.AudioSettings;
import com.zed3.flow.FlowStatistics;
import com.zed3.log.MyLog;
import com.zed3.net.RtpPacket;
import com.zed3.net.SipdroidSocket;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.ReceivePacketInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class VideoUdpThread {
	public static final int MAX_HANDLE_VIDEO_PLAY_INTERVAL = 20;
	public static final int SO_TIMEOUT = 1000;
	public static int VideoPlaySpeedFactor;
	private String TAG;
	private VideoDecodeBufferManager bufferManager;
	int count;
	private DialogListener dialogListener;
	private final CountDownLatch downLatch;
	private DatagramPacket dpPacket;
	private int fuByteLen;
	H264Dec h264Dec;
	boolean isIFrameDiscard;
	boolean isPFrameDiscard;
	int[] lastArray;
	long lastSystemTime;
	long lastTokenFrameTS;
	long lastTokenTimeMS;
	List<ReceivePacketInfo> mList;
	int mVideoport;
	String mVideourl;
	private boolean markflag;
	private InetAddress mdsIP;
	private DatagramSocket mdsSocket;
	boolean needCallTryConfig;
	private byte[] newbuf;
	private int parVal;
	private byte[] parambyte;
	private byte[] picbyte;
	private int pixHeight;
	private int pixWidth;
	private byte[] ppsFrame;
	int rFlow;
	private int recCount;
	private int recLost;
	private byte[] recbuffer;
	RtpPacket rtpPacket;
	private byte[] rtpbuffer;
	int sFlow;
	private long seqFirstTime;
	private short seqNum;
	private long seqTime;
	private SocketAddress socketAddress;
	private byte[] spsFrame;
	Thread t2;
	private byte[] tempb;
	private volatile boolean threadFlag;
	private byte[] timeSpanByte;
	DatagramChannel udpSendChannel;
	SurfaceView videoView;
	private int[] withAndHight;

	static {
		VideoUdpThread.VideoPlaySpeedFactor = 1;
	}

	public VideoUdpThread(final SurfaceView surfaceView, final String mVideourl, final int mVideoport, final SipdroidSocket mdsSocket) {
		this.lastTokenTimeMS = -1L;
		this.lastTokenFrameTS = -1L;
		this.lastSystemTime = 0L;
		this.count = 0;
		this.needCallTryConfig = false;
		this.isIFrameDiscard = false;
		this.isPFrameDiscard = false;
		this.sFlow = 0;
		this.rFlow = 0;
		this.TAG = "VideoUdpThread";
		this.mdsSocket = null;
		this.spsFrame = null;
		this.ppsFrame = null;
		this.threadFlag = false;
		this.markflag = false;
		this.recCount = 0;
		this.fuByteLen = 0;
		this.pixWidth = 0;
		this.pixHeight = 0;
		this.recLost = 0;
		this.parambyte = null;
		this.picbyte = null;
		this.rtpbuffer = null;
		this.newbuf = null;
		this.recbuffer = null;
		this.seqNum = 0;
		this.parVal = 0;
		this.seqTime = 3600L;
		this.seqFirstTime = 3600L;
		this.lastArray = new int[2];
		this.downLatch = new CountDownLatch(1);
		this.rtpPacket = null;
		this.h264Dec = null;
		this.mVideourl = "";
		this.mVideoport = 0;
		this.h264Dec = new H264Dec(surfaceView);
		this.threadFlag = true;
		this.mVideourl = mVideourl;
		this.mVideoport = mVideoport;
		if (AudioSettings.startTempStamp != 0L) {
			this.seqTime = Math.abs(System.currentTimeMillis() - AudioSettings.startTempStamp);
		}
		while (true) {
			try {
				this.mdsIP = InetAddress.getByName(mVideourl);
				this.socketAddress = new InetSocketAddress(this.mVideourl, this.mVideoport);
				(this.udpSendChannel = DatagramChannel.open()).configureBlocking(false);
				(this.mdsSocket = mdsSocket).setSoTimeout(1000);
				this.mdsSocket.setReceiveBufferSize(524288);
				this.mdsSocket.setSendBufferSize(524288);
				this.mList = new ArrayList<ReceivePacketInfo>();
				final TimeOutSyncBufferQueue<byte[]> timeOutSyncBufferQueue = new TimeOutSyncBufferQueue<byte[]>();
				final TimeOutSyncBufferQueue<H264Frame> timeOutSyncBufferQueue2 = new TimeOutSyncBufferQueue<H264Frame>();
				new Thread(new Producer(timeOutSyncBufferQueue)).start();
				(this.bufferManager = new VideoDecodeBufferManager(timeOutSyncBufferQueue, timeOutSyncBufferQueue2, new VideoProcess(timeOutSyncBufferQueue2), new Consumer(timeOutSyncBufferQueue2, timeOutSyncBufferQueue))).startThreads();
				this.lastSystemTime = System.currentTimeMillis();
			} catch (Exception ex) {
				MyLog.e(String.valueOf(this.TAG) + "mdsSocket time out", ex.toString());
				continue;
			}
			break;
		}
	}

	private byte[] ShorttoByte(final byte[] array, final short n) {
		array[3] = (byte) (n & 0xFF);
		array[2] = (byte) (n >> 8 & 0xFF);
		return array;
	}

	private static boolean getBit(final byte b, final int n) {
		return (b & 0x80) == 0x80;
	}

	private static int getInt(final byte[] array, final int n, final int n2) {
		return (int) getLong(array, n, n2);
	}

	private static long getLong(final byte[] array, int i, final int n) {
		long n2 = 0L;
		while (i < n) {
			n2 = (n2 << 8) + (array[i] & 0xFF);
			++i;
		}
		return n2;
	}

	private int getSequenceNumber(final byte[] array, final int n) {
		if (n >= 12) {
			return getInt(array, 2, 4);
		}
		return 0;
	}

	private boolean hasMarker(final byte[] array, final int n) {
		return n >= 12 && getBit(array[1], 7);
	}

	public void CloseUdpSocket() {
		this.threadFlag = false;
		this.bufferManager.stopThreads();
		MyLog.i("dcdc", "closeUdpSocket callded  flag = " + this.threadFlag);
		MyLog.i(this.TAG, "recv lost all packet:" + this.recLost);
		FlowStatistics.Video_Packet_Lost = 0;
		while (true) {
			try {
				if (this.udpSendChannel != null) {
					this.udpSendChannel.close();
				}
				if (this.mdsSocket != null) {
					MyLog.i(this.TAG, "mdsSocket Socket Close");
					this.mdsSocket.close();
					this.mdsSocket = null;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	public boolean IsNowSeqLarger(final long n, final long n2) {
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

	public void VideoPacketSend(final byte[] array, final int payloadLength, final int n, final boolean b) {
		if (payloadLength > 0) {
			try {
				this.markflag = false;
				if (n == 1) {
					this.markflag = true;
				}
				System.arraycopy(array, 0, this.newbuf = new byte[payloadLength + 12], 12, payloadLength);
				final RtpPacket rtpPacket = new RtpPacket(this.newbuf, 0, "1");
				if (UserAgent.camera_PayLoadType.length() > 0) {
					rtpPacket.setPayloadType(Integer.parseInt(UserAgent.camera_PayLoadType));
				}
				rtpPacket.setPayloadLength(payloadLength);
				rtpPacket.setTimestamp(this.seqTime);
				rtpPacket.setMarker(this.markflag);
				this.rtpbuffer = rtpPacket.getPacket();
				++this.seqNum;
				this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength + 12);
				if (this.markflag) {
					this.seqTime += 3600L;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void VideoPacketToH264(byte[] array, int payloadLength, final int n, final long n2) {
		if (payloadLength <= 0) {
			return;
		}
		boolean marker = false;
		try {
			if (this.seqFirstTime == 3600L) {
				this.lastSystemTime = System.currentTimeMillis();
			}
			if (AudioSettings.startTempStamp != 0L && this.seqFirstTime == 3600L) {
				this.seqFirstTime = Math.abs(System.currentTimeMillis() - AudioSettings.startTempStamp) * 90L;
			}
			this.seqTime = this.seqFirstTime + n2 / 1000L * 90L;
			if (n == 1) {
				marker = true;
				MyLog.i("encodeTimeStamp", "mark = 1,timestamp = " + this.seqTime);
			}
			System.arraycopy(array, 0, this.newbuf = new byte[payloadLength + 12], 12, payloadLength);
			final RtpPacket rtpPacket = new RtpPacket(this.newbuf, 0, "1");
			if (UserAgent.camera_PayLoadType.length() > 0) {
				rtpPacket.setPayloadType(Integer.parseInt(UserAgent.camera_PayLoadType));
			}
			rtpPacket.setPayloadLength(payloadLength);
			rtpPacket.setTimestamp(this.seqTime);
			MyLog.i("rtpTimeTe0401", "seqTime  = " + this.seqTime);
			rtpPacket.setMarker(marker);
			this.rtpbuffer = rtpPacket.getPacket();
			payloadLength += 12;
			this.parVal = (this.rtpbuffer[12] & 0x1F);
			if (this.parVal == 7) {
				this.parambyte = new byte[payloadLength];
				System.arraycopy(this.rtpbuffer, 0, this.parambyte, 0, payloadLength);
				MyLog.i("test_local_saveparameter", "save sps parameter frame" + this.seqNum);
				return;
			}
		} catch (Exception ex) {
			MyLog.e(this.TAG, "VideoPacketToH264 " + ex.toString());
			return;
		}
		if (this.parVal == 8) {
			this.picbyte = new byte[payloadLength];
			System.arraycopy(this.rtpbuffer, 0, this.picbyte, 0, payloadLength);
			MyLog.i("test_local_saveparameter", "save pps parameter frame" + this.seqNum);
			return;
		}
		++this.seqNum;
		if ((this.rtpbuffer[12] & 0x1F) == 0x1C) {
			if ((this.rtpbuffer[13] & 0x1F) != 0x5) {
				this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength);
				return;
			}
			this.timeSpanByte = new byte[4];
			System.arraycopy(this.rtpbuffer, 4, this.timeSpanByte, 0, 4);
			if ((this.rtpbuffer[13] >> 7 & 0x1) == 0x1) {
				if (this.parambyte != null) {
					MyLog.i("test_local_sendparameter", "send parameter frame" + this.seqNum);
					System.arraycopy(this.timeSpanByte, 0, this.parambyte, 4, 4);
					array = this.parambyte;
					final short seqNum = this.seqNum;
					this.seqNum = (short) (seqNum + 1);
					this.sendNewByte(this.ShorttoByte(array, seqNum), this.parambyte.length);
				}
				if (this.picbyte != null) {
					System.arraycopy(this.timeSpanByte, 0, this.picbyte, 4, 4);
					array = this.picbyte;
					final short seqNum2 = this.seqNum;
					this.seqNum = (short) (seqNum2 + 1);
					this.sendNewByte(this.ShorttoByte(array, seqNum2), this.picbyte.length);
				}
				this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength);
				return;
			}
			this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength);
		} else {
			if ((this.rtpbuffer[12] & 0x1F) == 0x5) {
				this.timeSpanByte = new byte[4];
				System.arraycopy(this.rtpbuffer, 4, this.timeSpanByte, 0, 4);
				if (this.parambyte != null) {
					System.arraycopy(this.timeSpanByte, 0, this.parambyte, 4, 4);
					array = this.parambyte;
					final short seqNum3 = this.seqNum;
					this.seqNum = (short) (seqNum3 + 1);
					this.sendNewByte(this.ShorttoByte(array, seqNum3), this.parambyte.length);
				}
				if (this.picbyte != null) {
					System.arraycopy(this.timeSpanByte, 0, this.picbyte, 4, 4);
					array = this.picbyte;
					final short seqNum4 = this.seqNum;
					this.seqNum = (short) (seqNum4 + 1);
					this.sendNewByte(this.ShorttoByte(array, seqNum4), this.picbyte.length);
				}
				this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength);
				return;
			}
			this.sendNewByte(this.ShorttoByte(this.rtpbuffer, this.seqNum), payloadLength);
		}
	}

	long calcIntervalBetweenTwoTS(final long n, final long n2) {
		if (n >= n2) {
			return (n - n2) / 90L;
		}
		if (n2 - n > 2147483647L) {
			return (4294967296L - n2 + n) / 90L;
		}
		MyLog.e(this.TAG, "error happened ! error value");
		return 0L;
	}

	public boolean doesSetSfview() {
		return this.h264Dec.getSfview() != null;
	}

	public long getTimestamp(final byte[] array, final int n) {
		if (n >= 12) {
			return getLong(array, 4, 8);
		}
		return 0L;
	}

	boolean isDelayBeyondMinTime(final long n, final long n2) {
		if (n >= n2) {
			if ((int) (n - n2) / 90 <= DeviceVideoInfo.MinVideoJitterbufferDelay) {
				return false;
			}
		} else {
			if (n2 - n <= 2147483647L) {
				MyLog.e(this.TAG, "error happened ! error value");
				return true;
			}
			if ((int) ((4294967296L - n2 + n) / 90L) <= DeviceVideoInfo.MinVideoJitterbufferDelay) {
				return false;
			}
		}
		return true;
	}

	boolean isDelayOverLimitTime(final long n, final long n2) {
		if (n >= n2) {
			if ((int) (n - n2) / 90 <= DeviceVideoInfo.MaxVideoJitterbufferDelay) {
				return false;
			}
		} else {
			if (n2 - n <= 2147483647L) {
				MyLog.e(this.TAG, "error happened ! error value");
				return true;
			}
			if ((int) ((4294967296L - n2 + n) / 90L) <= DeviceVideoInfo.MaxVideoJitterbufferDelay) {
				return false;
			}
		}
		return true;
	}

	boolean isExpectSeqNum(final long n, final long n2) {
		return 1L + n2 == n || (n2 == 65535L && n == 0L);
	}

	boolean isNeedDiscard(final boolean b, final int n, final int n2) {
		if (n > 0) {
			MyLog.i(String.valueOf(this.TAG) + "1", "total:" + n2 + ",lost:" + n);
			if (this.withAndHight != null) {
				final int length = this.withAndHight.length;
			}
		}
		// TODO
		return false;
	}

	boolean isThisFramePlayTime(long calcIntervalBetweenTwoTS, final long n) {
		if (n != -1L) {
			final long currentTimeMillis = System.currentTimeMillis();
			calcIntervalBetweenTwoTS = this.calcIntervalBetweenTwoTS(calcIntervalBetweenTwoTS, n);
			if (currentTimeMillis - this.lastTokenTimeMS < calcIntervalBetweenTwoTS / VideoUdpThread.VideoPlaySpeedFactor) {
				return false;
			}
		}
		return true;
	}

	public void resetDecode() {
		this.needCallTryConfig = true;
	}

	public void rotateDecoder() {
		this.h264Dec.addmRotation();
	}

	public void sendNewByte(final byte[] array, final int n) {
		try {
			if (!this.mVideourl.equals("") && this.mVideoport != 0) {
				if (this.mdsIP == null) {
					this.mdsIP = InetAddress.getByName(this.mVideourl);
				}
				this.dpPacket = new DatagramPacket(array, n, this.mdsIP, this.mVideoport);
				if (this.mdsSocket != null) {
					this.mdsSocket.send(this.dpPacket);
				}
				FlowStatistics.Video_Send_Data += n;
				return;
			}
			MyLog.e(this.TAG, "\u89c6\u9891\u7aef\u53e3\u5730\u5740\u4e3a\u7a7a");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void setAndAdjustLastTokenTimeMS(long calcIntervalBetweenTwoTS) {
		if (this.lastTokenTimeMS == -1L) {
			this.lastTokenTimeMS = System.currentTimeMillis();
		} else {
			final long currentTimeMillis = System.currentTimeMillis();
			calcIntervalBetweenTwoTS = this.calcIntervalBetweenTwoTS(calcIntervalBetweenTwoTS, this.lastTokenFrameTS);
			final long n = currentTimeMillis - this.lastTokenTimeMS;
			if (n >= calcIntervalBetweenTwoTS / VideoUdpThread.VideoPlaySpeedFactor) {
				this.lastTokenTimeMS = currentTimeMillis - (n - calcIntervalBetweenTwoTS / VideoUdpThread.VideoPlaySpeedFactor);
			}
		}
	}

	public void setDialogListener(final DialogListener dialogListener) {
		this.dialogListener = dialogListener;
	}

	public void setSfview(final SurfaceView sfview) {
		this.h264Dec.setSfview(sfview);
	}

	public void setSurfaceAviable(final boolean surfaceAviable) {
		this.h264Dec.setSurfaceAviable(surfaceAviable);
	}

	class Consumer extends VideoDecodeThread {
		long a;
		long b;
		int conCount;
		TimeOutSyncBufferQueue<H264Frame> decodeQueue;
		int frameType;
		int h;
		byte[] head;
		int k;
		int len;
		TimeOutSyncBufferQueue<byte[]> packetQueue;
		int seqNumLog;
		int temp;
		int w;

		public Consumer(final TimeOutSyncBufferQueue<H264Frame> decodeQueue, final TimeOutSyncBufferQueue<byte[]> packetQueue) {
			super("packet", packetQueue);
			this.frameType = 0;
			this.w = 0;
			this.h = 0;
			this.k = 0;
			this.len = 0;
			this.conCount = 0;
			this.seqNumLog = 0;
			this.head = new byte[]{0, 0, 0, 1};
			this.a = 0L;
			this.b = 0L;
			this.temp = 0;
			this.decodeQueue = decodeQueue;
			this.packetQueue = packetQueue;
			MyLog.i(VideoUdpThread.this.TAG, "producer Consumer");
		}

		@Override
		public void run() {
			//
			// This method could not be decompiled.
			//
			// Original Bytecode:
			//
			//     2: invokestatic    android/os/Process.setThreadPriority:(I)V
			//     5: new             Lcom/zed3/h264_fu_process/H264FUManager;
			//     8: dup
			//     9: invokespecial   com/zed3/h264_fu_process/H264FUManager.<init>:()V
			//    12: astore          7
			//    14: aconst_null
			//    15: astore_2
			//    16: aload_0
			//    17: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    20: invokestatic    com/zed3/sipua/VideoUdpThread.access.1:(Lcom/zed3/sipua/VideoUdpThread;)Z
			//    23: ifne            47
			//    26: aload_0
			//    27: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    30: invokestatic    com/zed3/sipua/VideoUdpThread.access.3:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/util/concurrent/CountDownLatch;
			//    33: invokevirtual   java/util/concurrent/CountDownLatch.await:()V
			//    36: aload_0
			//    37: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    40: getfield        com/zed3/sipua/VideoUdpThread.h264Dec:Lcom/video/utils/H264Dec;
			//    43: invokevirtual   com/video/utils/H264Dec.releaseCodec:()V
			//    46: return
			//    47: aload_2
			//    48: astore_3
			//    49: aload_0
			//    50: invokevirtual   com/zed3/sipua/VideoUdpThread.Consumer.adjustThread:()V
			//    53: aload_2
			//    54: astore_3
			//    55: aload_0
			//    56: getfield        com/zed3/sipua/VideoUdpThread.Consumer.packetQueue:Lcom/zed3/sipua/TimeOutSyncBufferQueue;
			//    59: invokevirtual   com/zed3/sipua/TimeOutSyncBufferQueue.pop:()Ljava/lang/Object;
			//    62: checkcast       [B
			//    65: astore          8
			//    67: aload_2
			//    68: astore_3
			//    69: aload_0
			//    70: aload           8
			//    72: arraylength
			//    73: putfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//    76: aload_2
			//    77: astore_3
			//    78: aload_0
			//    79: aload_0
			//    80: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    83: aload           8
			//    85: aload_0
			//    86: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//    89: invokestatic    com/zed3/sipua/VideoUdpThread.access.4:(Lcom/zed3/sipua/VideoUdpThread;[BI)I
			//    92: putfield        com/zed3/sipua/VideoUdpThread.Consumer.seqNumLog:I
			//    95: aload_2
			//    96: astore_3
			//    97: aload_0
			//    98: invokestatic    java/lang/System.currentTimeMillis:()J
			//   101: putfield        com/zed3/sipua/VideoUdpThread.Consumer.a:J
			//   104: aload           8
			//   106: bipush          12
			//   108: baload
			//   109: bipush          31
			//   111: iand
			//   112: bipush          28
			//   114: if_icmpeq       662
			//   117: aload           8
			//   119: bipush          12
			//   121: baload
			//   122: bipush          31
			//   124: iand
			//   125: bipush          7
			//   127: if_icmpeq       156
			//   130: aload           8
			//   132: bipush          12
			//   134: baload
			//   135: bipush          31
			//   137: iand
			//   138: bipush          8
			//   140: if_icmpeq       156
			//   143: aload           8
			//   145: bipush          12
			//   147: baload
			//   148: bipush          31
			//   150: iand
			//   151: bipush          6
			//   153: if_icmpne       461
			//   156: aload_2
			//   157: astore_3
			//   158: aload_0
			//   159: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   162: bipush          12
			//   164: isub
			//   165: newarray        B
			//   167: astore          4
			//   169: aload_2
			//   170: astore_3
			//   171: aload           8
			//   173: bipush          12
			//   175: aload           4
			//   177: iconst_0
			//   178: aload_0
			//   179: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   182: bipush          12
			//   184: isub
			//   185: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   188: aload_2
			//   189: astore_3
			//   190: aload_0
			//   191: invokestatic    java/lang/System.currentTimeMillis:()J
			//   194: putfield        com/zed3/sipua/VideoUdpThread.Consumer.a:J
			//   197: aload           4
			//   199: astore          5
			//   201: aload_2
			//   202: astore          4
			//   204: aload           4
			//   206: astore_3
			//   207: aload_0
			//   208: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   211: invokestatic    com/zed3/sipua/VideoUdpThread.access.6:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   214: ifne            1066
			//   217: aload           4
			//   219: astore_2
			//   220: aload           8
			//   222: bipush          12
			//   224: baload
			//   225: bipush          31
			//   227: iand
			//   228: bipush          7
			//   230: if_icmpne       16
			//   233: aload           4
			//   235: astore_3
			//   236: aload_0
			//   237: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   240: iconst_1
			//   241: invokestatic    com/zed3/sipua/VideoUdpThread.access.7:(Lcom/zed3/sipua/VideoUdpThread;I)V
			//   244: aload           4
			//   246: astore_3
			//   247: aload_0
			//   248: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   251: bipush          13
			//   253: isub
			//   254: newarray        B
			//   256: astore_2
			//   257: aload           4
			//   259: astore_3
			//   260: aload           5
			//   262: iconst_1
			//   263: aload_2
			//   264: iconst_0
			//   265: aload_0
			//   266: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   269: bipush          13
			//   271: isub
			//   272: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   275: aload           4
			//   277: astore_3
			//   278: aload_0
			//   279: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   282: aload_2
			//   283: invokestatic    com/zed3/jni/VideoUtils.getWithAndHight:([B)[I
			//   286: invokestatic    com/zed3/sipua/VideoUdpThread.access.8:(Lcom/zed3/sipua/VideoUdpThread;[I)V
			//   289: aload           4
			//   291: astore_3
			//   292: aload_0
			//   293: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   296: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//   299: ifnull          1047
			//   302: aload           4
			//   304: astore_3
			//   305: aload_0
			//   306: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   309: aload_0
			//   310: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   313: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//   316: iconst_0
			//   317: iaload
			//   318: invokestatic    com/zed3/sipua/VideoUdpThread.access.10:(Lcom/zed3/sipua/VideoUdpThread;I)V
			//   321: aload           4
			//   323: astore_3
			//   324: aload_0
			//   325: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   328: aload_0
			//   329: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   332: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//   335: iconst_1
			//   336: iaload
			//   337: invokestatic    com/zed3/sipua/VideoUdpThread.access.11:(Lcom/zed3/sipua/VideoUdpThread;I)V
			//   340: aload           4
			//   342: astore_3
			//   343: aload_0
			//   344: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   347: invokestatic    com/zed3/sipua/VideoUdpThread.access.12:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   350: sipush          720
			//   353: if_icmple       844
			//   356: aload           4
			//   358: astore_3
			//   359: aload_0
			//   360: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   363: invokestatic    com/zed3/sipua/VideoUdpThread.access.13:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   366: sipush          720
			//   369: if_icmple       844
			//   372: aload           4
			//   374: astore_3
			//   375: aload_0
			//   376: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   379: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//   382: ldc             "this resolution not support"
			//   384: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//   387: aload           4
			//   389: astore_3
			//   390: aload_0
			//   391: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   394: iconst_0
			//   395: invokestatic    com/zed3/sipua/VideoUdpThread.access.14:(Lcom/zed3/sipua/VideoUdpThread;Z)V
			//   398: aload           4
			//   400: astore_3
			//   401: aload_0
			//   402: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   405: invokestatic    com/zed3/sipua/VideoUdpThread.access.15:(Lcom/zed3/sipua/VideoUdpThread;)Lcom/zed3/sipua/DialogListener;
			//   408: iconst_0
			//   409: invokeinterface com/zed3/sipua/DialogListener.DialogCancel:(I)V
			//   414: return
			//   415: astore          4
			//   417: aload_3
			//   418: astore_2
			//   419: aload           4
			//   421: astore_3
			//   422: new             Ljava/lang/StringBuilder;
			//   425: dup
			//   426: aload_0
			//   427: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   430: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//   433: invokestatic    java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
			//   436: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
			//   439: ldc             "3"
			//   441: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
			//   444: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
			//   447: aload_3
			//   448: invokevirtual   java/lang/Exception.toString:()Ljava/lang/String;
			//   451: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
			//   454: aload_3
			//   455: invokevirtual   java/lang/Exception.printStackTrace:()V
			//   458: goto            16
			//   461: aload_2
			//   462: astore_3
			//   463: aload_0
			//   464: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   467: aload           8
			//   469: aload_0
			//   470: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   473: invokestatic    com/zed3/sipua/VideoUdpThread.access.5:(Lcom/zed3/sipua/VideoUdpThread;[BI)Z
			//   476: ifeq            529
			//   479: aload           8
			//   481: bipush          13
			//   483: baload
			//   484: sipush          128
			//   487: iand
			//   488: sipush          128
			//   491: if_icmpne       529
			//   494: aload_2
			//   495: astore_3
			//   496: aload_0
			//   497: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   500: bipush          12
			//   502: isub
			//   503: newarray        B
			//   505: astore          4
			//   507: aload_2
			//   508: astore_3
			//   509: aload           8
			//   511: bipush          12
			//   513: aload           4
			//   515: iconst_0
			//   516: aload_0
			//   517: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   520: bipush          12
			//   522: isub
			//   523: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   526: goto            188
			//   529: aload_2
			//   530: astore_3
			//   531: new             Lcom/zed3/net/RtpPacket;
			//   534: dup
			//   535: aload           8
			//   537: aload_0
			//   538: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   541: iconst_0
			//   542: invokespecial   com/zed3/net/RtpPacket.<init>:([BII)V
			//   545: astore          4
			//   547: aload           4
			//   549: astore          5
			//   551: aload           7
			//   553: aload           4
			//   555: invokevirtual   com/zed3/h264_fu_process/H264FUManager.processFU4Eyebeam:(Lcom/zed3/net/RtpPacket;)Lcom/zed3/h264_fu_process/FU;
			//   558: astore_3
			//   559: aload_3
			//   560: ifnonnull       586
			//   563: aload_3
			//   564: astore_2
			//   565: aload           4
			//   567: astore          5
			//   569: aload_0
			//   570: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   573: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//   576: ldc             "not 28 eyebeam fuByte == null"
			//   578: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//   581: aload_3
			//   582: astore_2
			//   583: goto            16
			//   586: aload_3
			//   587: astore_2
			//   588: aload           4
			//   590: astore          5
			//   592: aload_3
			//   593: invokevirtual   com/zed3/h264_fu_process/FU.getDataLen:()J
			//   596: l2i
			//   597: istore_1
			//   598: aload_3
			//   599: astore_2
			//   600: aload           4
			//   602: astore          5
			//   604: iload_1
			//   605: newarray        B
			//   607: astore          6
			//   609: aload_3
			//   610: astore_2
			//   611: aload           4
			//   613: astore          5
			//   615: aload_3
			//   616: invokevirtual   com/zed3/h264_fu_process/FU.getData:()[B
			//   619: iconst_0
			//   620: aload           6
			//   622: iconst_0
			//   623: iload_1
			//   624: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   627: aload_3
			//   628: astore_2
			//   629: aload           4
			//   631: astore          5
			//   633: aload_3
			//   634: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//   637: ifle            653
			//   640: aload           6
			//   642: iconst_0
			//   643: aload           6
			//   645: iconst_0
			//   646: baload
			//   647: sipush          128
			//   650: ior
			//   651: i2b
			//   652: bastore
			//   653: aload_3
			//   654: astore_2
			//   655: aload           6
			//   657: astore          4
			//   659: goto            188
			//   662: aload_2
			//   663: astore_3
			//   664: aload_0
			//   665: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   668: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//   671: new             Ljava/lang/StringBuilder;
			//   674: dup
			//   675: ldc             "Rtp is 28 seqNum:"
			//   677: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
			//   680: aload_0
			//   681: getfield        com/zed3/sipua/VideoUdpThread.Consumer.seqNumLog:I
			//   684: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
			//   687: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
			//   690: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//   693: aload_2
			//   694: astore_3
			//   695: new             Lcom/zed3/net/RtpPacket;
			//   698: dup
			//   699: aload           8
			//   701: aload_0
			//   702: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//   705: iconst_0
			//   706: invokespecial   com/zed3/net/RtpPacket.<init>:([BII)V
			//   709: astore          6
			//   711: aload           6
			//   713: astore          5
			//   715: aload           7
			//   717: aload           6
			//   719: invokevirtual   com/zed3/h264_fu_process/H264FUManager.processFU:(Lcom/zed3/net/RtpPacket;)Lcom/zed3/h264_fu_process/FU;
			//   722: astore          4
			//   724: aload           4
			//   726: ifnonnull       754
			//   729: aload           4
			//   731: astore_2
			//   732: aload           6
			//   734: astore          5
			//   736: aload_0
			//   737: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   740: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//   743: ldc             "28 fuByte == null"
			//   745: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//   748: aload           4
			//   750: astore_2
			//   751: goto            16
			//   754: aload           4
			//   756: astore_2
			//   757: aload           6
			//   759: astore          5
			//   761: aload           4
			//   763: invokevirtual   com/zed3/h264_fu_process/FU.getDataLen:()J
			//   766: l2i
			//   767: istore_1
			//   768: aload           4
			//   770: astore_2
			//   771: aload           6
			//   773: astore          5
			//   775: iload_1
			//   776: newarray        B
			//   778: astore_3
			//   779: aload           4
			//   781: astore_2
			//   782: aload           6
			//   784: astore          5
			//   786: aload           4
			//   788: invokevirtual   com/zed3/h264_fu_process/FU.getData:()[B
			//   791: iconst_0
			//   792: aload_3
			//   793: iconst_0
			//   794: iload_1
			//   795: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   798: aload           4
			//   800: astore_2
			//   801: aload           6
			//   803: astore          5
			//   805: aload           4
			//   807: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//   810: ifle            824
			//   813: aload_3
			//   814: iconst_0
			//   815: aload_3
			//   816: iconst_0
			//   817: baload
			//   818: sipush          128
			//   821: ior
			//   822: i2b
			//   823: bastore
			//   824: aload           4
			//   826: astore_2
			//   827: aload           6
			//   829: astore          5
			//   831: aload_0
			//   832: invokestatic    java/lang/System.currentTimeMillis:()J
			//   835: putfield        com/zed3/sipua/VideoUdpThread.Consumer.a:J
			//   838: aload_3
			//   839: astore          5
			//   841: goto            204
			//   844: aload           4
			//   846: astore_3
			//   847: aload_0
			//   848: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   851: getfield        com/zed3/sipua/VideoUdpThread.h264Dec:Lcom/video/utils/H264Dec;
			//   854: invokevirtual   com/video/utils/H264Dec.createCodec:()V
			//   857: aload           4
			//   859: astore_3
			//   860: aload_0
			//   861: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   864: getfield        com/zed3/sipua/VideoUdpThread.h264Dec:Lcom/video/utils/H264Dec;
			//   867: aload_0
			//   868: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   871: invokestatic    com/zed3/sipua/VideoUdpThread.access.13:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   874: aload_0
			//   875: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   878: invokestatic    com/zed3/sipua/VideoUdpThread.access.12:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   881: aconst_null
			//   882: aconst_null
			//   883: invokevirtual   com/video/utils/H264Dec.tryConfig:(II[B[B)V
			//   886: aload           4
			//   888: astore_2
			//   889: aload           4
			//   891: astore_3
			//   892: aload_0
			//   893: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   896: invokestatic    com/zed3/sipua/VideoUdpThread.access.13:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   899: ifeq            16
			//   902: aload           4
			//   904: astore_2
			//   905: aload           4
			//   907: astore_3
			//   908: aload_0
			//   909: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//   912: invokestatic    com/zed3/sipua/VideoUdpThread.access.12:(Lcom/zed3/sipua/VideoUdpThread;)I
			//   915: ifeq            16
			//   918: aload           4
			//   920: astore_3
			//   921: aload           5
			//   923: arraylength
			//   924: iconst_4
			//   925: iadd
			//   926: newarray        B
			//   928: astore_2
			//   929: aload           4
			//   931: astore_3
			//   932: aload           5
			//   934: iconst_0
			//   935: aload_2
			//   936: iconst_4
			//   937: aload           5
			//   939: arraylength
			//   940: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   943: aload           4
			//   945: astore_3
			//   946: aload_0
			//   947: getfield        com/zed3/sipua/VideoUdpThread.Consumer.head:[B
			//   950: iconst_0
			//   951: aload_2
			//   952: iconst_0
			//   953: iconst_4
			//   954: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   957: aload           4
			//   959: astore_3
			//   960: aload_2
			//   961: astore          5
			//   963: aload_2
			//   964: arraylength
			//   965: iconst_4
			//   966: if_icmple       1484
			//   969: aload_2
			//   970: iconst_4
			//   971: baload
			//   972: bipush          31
			//   974: iand
			//   975: bipush          7
			//   977: if_icmpeq       991
			//   980: aload_2
			//   981: iconst_4
			//   982: baload
			//   983: bipush          31
			//   985: iand
			//   986: bipush          8
			//   988: if_icmpne       1665
			//   991: aload_2
			//   992: iconst_4
			//   993: baload
			//   994: bipush          31
			//   996: iand
			//   997: bipush          7
			//   999: if_icmpne       1127
			//  1002: aload           4
			//  1004: astore_3
			//  1005: aload_0
			//  1006: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1009: aload_2
			//  1010: invokevirtual   [B.clone:()Ljava/lang/Object;
			//  1013: checkcast       [B
			//  1016: invokestatic    com/zed3/sipua/VideoUdpThread.access.16:(Lcom/zed3/sipua/VideoUdpThread;[B)V
			//  1019: aload           4
			//  1021: astore_3
			//  1022: aload_0
			//  1023: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1026: iconst_0
			//  1027: putfield        com/zed3/sipua/VideoUdpThread.isIFrameDiscard:Z
			//  1030: aload           4
			//  1032: astore_3
			//  1033: aload_0
			//  1034: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1037: iconst_0
			//  1038: putfield        com/zed3/sipua/VideoUdpThread.isPFrameDiscard:Z
			//  1041: aload           4
			//  1043: astore_2
			//  1044: goto            16
			//  1047: aload           4
			//  1049: astore_3
			//  1050: aload_0
			//  1051: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1054: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//  1057: ldc_w           "get width height from jni is null"
			//  1060: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//  1063: goto            886
			//  1066: aload           8
			//  1068: bipush          12
			//  1070: baload
			//  1071: bipush          31
			//  1073: iand
			//  1074: bipush          7
			//  1076: if_icmpne       886
			//  1079: aload           4
			//  1081: astore_3
			//  1082: aload_0
			//  1083: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//  1086: bipush          13
			//  1088: isub
			//  1089: newarray        B
			//  1091: astore_2
			//  1092: aload           4
			//  1094: astore_3
			//  1095: aload           5
			//  1097: iconst_1
			//  1098: aload_2
			//  1099: iconst_0
			//  1100: aload_0
			//  1101: getfield        com/zed3/sipua/VideoUdpThread.Consumer.len:I
			//  1104: bipush          13
			//  1106: isub
			//  1107: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//  1110: aload           4
			//  1112: astore_3
			//  1113: aload_0
			//  1114: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1117: aload_2
			//  1118: invokestatic    com/zed3/jni/VideoUtils.getWithAndHight:([B)[I
			//  1121: invokestatic    com/zed3/sipua/VideoUdpThread.access.8:(Lcom/zed3/sipua/VideoUdpThread;[I)V
			//  1124: goto            886
			//  1127: aload_2
			//  1128: astore          5
			//  1130: aload_2
			//  1131: iconst_4
			//  1132: baload
			//  1133: bipush          31
			//  1135: iand
			//  1136: bipush          8
			//  1138: if_icmpne       1484
			//  1141: aload           4
			//  1143: astore_3
			//  1144: aload_0
			//  1145: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1148: aload_2
			//  1149: invokevirtual   [B.clone:()Ljava/lang/Object;
			//  1152: checkcast       [B
			//  1155: invokestatic    com/zed3/sipua/VideoUdpThread.access.17:(Lcom/zed3/sipua/VideoUdpThread;[B)V
			//  1158: aload           4
			//  1160: astore_3
			//  1161: aload_0
			//  1162: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1165: iconst_0
			//  1166: putfield        com/zed3/sipua/VideoUdpThread.isIFrameDiscard:Z
			//  1169: aload           4
			//  1171: astore_3
			//  1172: aload_0
			//  1173: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1176: iconst_0
			//  1177: putfield        com/zed3/sipua/VideoUdpThread.isPFrameDiscard:Z
			//  1180: aload           4
			//  1182: astore_3
			//  1183: aload_0
			//  1184: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1187: invokestatic    com/zed3/sipua/VideoUdpThread.access.18:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1190: arraylength
			//  1191: aload_0
			//  1192: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1195: invokestatic    com/zed3/sipua/VideoUdpThread.access.19:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1198: arraylength
			//  1199: iadd
			//  1200: newarray        B
			//  1202: astore          5
			//  1204: aload           4
			//  1206: astore_3
			//  1207: aload_0
			//  1208: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1211: invokestatic    com/zed3/sipua/VideoUdpThread.access.18:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1214: iconst_0
			//  1215: aload           5
			//  1217: iconst_0
			//  1218: aload_0
			//  1219: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1222: invokestatic    com/zed3/sipua/VideoUdpThread.access.18:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1225: arraylength
			//  1226: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//  1229: aload           4
			//  1231: astore_3
			//  1232: aload_0
			//  1233: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1236: invokestatic    com/zed3/sipua/VideoUdpThread.access.19:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1239: iconst_0
			//  1240: aload           5
			//  1242: aload_0
			//  1243: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1246: invokestatic    com/zed3/sipua/VideoUdpThread.access.18:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1249: arraylength
			//  1250: aload_0
			//  1251: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1254: invokestatic    com/zed3/sipua/VideoUdpThread.access.19:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1257: arraylength
			//  1258: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//  1261: aload           4
			//  1263: astore_3
			//  1264: aload_0
			//  1265: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1268: getfield        com/zed3/sipua/VideoUdpThread.needCallTryConfig:Z
			//  1271: ifne            1348
			//  1274: aload           4
			//  1276: astore_3
			//  1277: aload_0
			//  1278: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1281: getfield        com/zed3/sipua/VideoUdpThread.lastArray:[I
			//  1284: ifnull          1476
			//  1287: aload           4
			//  1289: astore_3
			//  1290: aload_0
			//  1291: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1294: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//  1297: ifnull          1476
			//  1300: aload           4
			//  1302: astore_3
			//  1303: aload_0
			//  1304: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1307: getfield        com/zed3/sipua/VideoUdpThread.lastArray:[I
			//  1310: iconst_0
			//  1311: iaload
			//  1312: aload_0
			//  1313: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1316: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//  1319: iconst_0
			//  1320: iaload
			//  1321: if_icmpne       1348
			//  1324: aload           4
			//  1326: astore_3
			//  1327: aload_0
			//  1328: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1331: getfield        com/zed3/sipua/VideoUdpThread.lastArray:[I
			//  1334: iconst_1
			//  1335: iaload
			//  1336: aload_0
			//  1337: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1340: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//  1343: iconst_1
			//  1344: iaload
			//  1345: if_icmpeq       1476
			//  1348: aload           4
			//  1350: astore_3
			//  1351: aload_0
			//  1352: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1355: aload_0
			//  1356: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1359: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//  1362: iconst_0
			//  1363: iaload
			//  1364: invokestatic    com/zed3/sipua/VideoUdpThread.access.10:(Lcom/zed3/sipua/VideoUdpThread;I)V
			//  1367: aload           4
			//  1369: astore_3
			//  1370: aload_0
			//  1371: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1374: aload_0
			//  1375: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1378: invokestatic    com/zed3/sipua/VideoUdpThread.access.9:(Lcom/zed3/sipua/VideoUdpThread;)[I
			//  1381: iconst_1
			//  1382: iaload
			//  1383: invokestatic    com/zed3/sipua/VideoUdpThread.access.11:(Lcom/zed3/sipua/VideoUdpThread;I)V
			//  1386: aload           4
			//  1388: astore_3
			//  1389: aload_0
			//  1390: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1393: getfield        com/zed3/sipua/VideoUdpThread.lastArray:[I
			//  1396: iconst_0
			//  1397: aload_0
			//  1398: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1401: invokestatic    com/zed3/sipua/VideoUdpThread.access.13:(Lcom/zed3/sipua/VideoUdpThread;)I
			//  1404: iastore
			//  1405: aload           4
			//  1407: astore_3
			//  1408: aload_0
			//  1409: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1412: getfield        com/zed3/sipua/VideoUdpThread.lastArray:[I
			//  1415: iconst_1
			//  1416: aload_0
			//  1417: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1420: invokestatic    com/zed3/sipua/VideoUdpThread.access.12:(Lcom/zed3/sipua/VideoUdpThread;)I
			//  1423: iastore
			//  1424: aload           4
			//  1426: astore_3
			//  1427: aload_0
			//  1428: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1431: getfield        com/zed3/sipua/VideoUdpThread.h264Dec:Lcom/video/utils/H264Dec;
			//  1434: aload_0
			//  1435: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1438: invokestatic    com/zed3/sipua/VideoUdpThread.access.13:(Lcom/zed3/sipua/VideoUdpThread;)I
			//  1441: aload_0
			//  1442: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1445: invokestatic    com/zed3/sipua/VideoUdpThread.access.12:(Lcom/zed3/sipua/VideoUdpThread;)I
			//  1448: aload_0
			//  1449: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1452: invokestatic    com/zed3/sipua/VideoUdpThread.access.18:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1455: aload_0
			//  1456: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1459: invokestatic    com/zed3/sipua/VideoUdpThread.access.19:(Lcom/zed3/sipua/VideoUdpThread;)[B
			//  1462: invokevirtual   com/video/utils/H264Dec.reConfig:(II[B[B)V
			//  1465: aload           4
			//  1467: astore_3
			//  1468: aload_0
			//  1469: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1472: iconst_0
			//  1473: putfield        com/zed3/sipua/VideoUdpThread.needCallTryConfig:Z
			//  1476: aload           4
			//  1478: astore_3
			//  1479: aload_0
			//  1480: iconst_2
			//  1481: putfield        com/zed3/sipua/VideoUdpThread.Consumer.frameType:I
			//  1484: aload           4
			//  1486: astore_2
			//  1487: aload           5
			//  1489: ifnull          16
			//  1492: aload           4
			//  1494: astore_2
			//  1495: aload           4
			//  1497: astore_3
			//  1498: aload           5
			//  1500: arraylength
			//  1501: ifeq            16
			//  1504: aload           4
			//  1506: ifnull          1592
			//  1509: aload           4
			//  1511: astore_3
			//  1512: aload           4
			//  1514: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//  1517: aload           4
			//  1519: getfield        com/zed3/h264_fu_process/FU.totalCount:I
			//  1522: if_icmplt       1592
			//  1525: aload           4
			//  1527: astore_3
			//  1528: new             Ljava/lang/StringBuilder;
			//  1531: dup
			//  1532: aload_0
			//  1533: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1536: invokestatic    com/zed3/sipua/VideoUdpThread.access.0:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/lang/String;
			//  1539: invokestatic    java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
			//  1542: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
			//  1545: ldc_w           "1"
			//  1548: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
			//  1551: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
			//  1554: new             Ljava/lang/StringBuilder;
			//  1557: dup
			//  1558: ldc_w           "isNeedDiscard():total:"
			//  1561: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
			//  1564: aload           4
			//  1566: getfield        com/zed3/h264_fu_process/FU.totalCount:I
			//  1569: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
			//  1572: ldc_w           ",lost:"
			//  1575: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
			//  1578: aload           4
			//  1580: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//  1583: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
			//  1586: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
			//  1589: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//  1592: aload           5
			//  1594: iconst_4
			//  1595: baload
			//  1596: bipush          31
			//  1598: iand
			//  1599: iconst_5
			//  1600: if_icmpne       1704
			//  1603: aload           4
			//  1605: ifnull          1635
			//  1608: aload           4
			//  1610: astore_2
			//  1611: aload           4
			//  1613: astore_3
			//  1614: aload_0
			//  1615: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1618: iconst_1
			//  1619: aload           4
			//  1621: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//  1624: aload           4
			//  1626: getfield        com/zed3/h264_fu_process/FU.totalCount:I
			//  1629: invokevirtual   com/zed3/sipua/VideoUdpThread.isNeedDiscard:(ZII)Z
			//  1632: ifne            16
			//  1635: aload           4
			//  1637: astore_3
			//  1638: aload_0
			//  1639: getfield        com/zed3/sipua/VideoUdpThread.Consumer.decodeQueue:Lcom/zed3/sipua/TimeOutSyncBufferQueue;
			//  1642: new             Lcom/video/utils/H264Frame;
			//  1645: dup
			//  1646: aload           5
			//  1648: aload_0
			//  1649: getfield        com/zed3/sipua/VideoUdpThread.Consumer.frameType:I
			//  1652: invokespecial   com/video/utils/H264Frame.<init>:([BI)V
			//  1655: invokevirtual   com/zed3/sipua/TimeOutSyncBufferQueue.push:(Ljava/lang/Object;)Z
			//  1658: pop
			//  1659: aload           4
			//  1661: astore_2
			//  1662: goto            16
			//  1665: aload_2
			//  1666: iconst_4
			//  1667: baload
			//  1668: bipush          31
			//  1670: iand
			//  1671: bipush          6
			//  1673: if_icmpne       1690
			//  1676: aload           4
			//  1678: astore_3
			//  1679: aload_0
			//  1680: iconst_2
			//  1681: putfield        com/zed3/sipua/VideoUdpThread.Consumer.frameType:I
			//  1684: aload_2
			//  1685: astore          5
			//  1687: goto            1484
			//  1690: aload           4
			//  1692: astore_3
			//  1693: aload_0
			//  1694: iconst_0
			//  1695: putfield        com/zed3/sipua/VideoUdpThread.Consumer.frameType:I
			//  1698: aload_2
			//  1699: astore          5
			//  1701: goto            1484
			//  1704: aload           5
			//  1706: iconst_4
			//  1707: baload
			//  1708: bipush          31
			//  1710: iand
			//  1711: iconst_1
			//  1712: if_icmpne       1635
			//  1715: aload           4
			//  1717: ifnull          1750
			//  1720: aload           4
			//  1722: astore_3
			//  1723: aload_0
			//  1724: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1727: iconst_0
			//  1728: aload           4
			//  1730: getfield        com/zed3/h264_fu_process/FU.lostCount:I
			//  1733: aload           4
			//  1735: getfield        com/zed3/h264_fu_process/FU.totalCount:I
			//  1738: invokevirtual   com/zed3/sipua/VideoUdpThread.isNeedDiscard:(ZII)Z
			//  1741: ifeq            1635
			//  1744: aload           4
			//  1746: astore_2
			//  1747: goto            16
			//  1750: aload           4
			//  1752: astore_3
			//  1753: aload_0
			//  1754: getfield        com/zed3/sipua/VideoUdpThread.Consumer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//  1757: iconst_0
			//  1758: iconst_0
			//  1759: bipush          10
			//  1761: invokevirtual   com/zed3/sipua/VideoUdpThread.isNeedDiscard:(ZII)Z
			//  1764: ifeq            1635
			//  1767: aload           4
			//  1769: astore_3
			//  1770: ldc_w           "discard"
			//  1773: ldc_w           "called"
			//  1776: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
			//  1779: aload           4
			//  1781: astore_2
			//  1782: goto            16
			//  1785: astore_2
			//  1786: aload_2
			//  1787: invokevirtual   java/lang/InterruptedException.printStackTrace:()V
			//  1790: goto            36
			//  1793: astore_3
			//  1794: goto            422
			//    Exceptions:
			//  Try           Handler
			//  Start  End    Start  End    Type
			//  -----  -----  -----  -----  --------------------------------
			//  26     36     1785   1793   Ljava/lang/InterruptedException;
			//  49     53     415    422    Ljava/lang/Exception;
			//  55     67     415    422    Ljava/lang/Exception;
			//  69     76     415    422    Ljava/lang/Exception;
			//  78     95     415    422    Ljava/lang/Exception;
			//  97     104    415    422    Ljava/lang/Exception;
			//  158    169    415    422    Ljava/lang/Exception;
			//  171    188    415    422    Ljava/lang/Exception;
			//  190    197    415    422    Ljava/lang/Exception;
			//  207    217    415    422    Ljava/lang/Exception;
			//  236    244    415    422    Ljava/lang/Exception;
			//  247    257    415    422    Ljava/lang/Exception;
			//  260    275    415    422    Ljava/lang/Exception;
			//  278    289    415    422    Ljava/lang/Exception;
			//  292    302    415    422    Ljava/lang/Exception;
			//  305    321    415    422    Ljava/lang/Exception;
			//  324    340    415    422    Ljava/lang/Exception;
			//  343    356    415    422    Ljava/lang/Exception;
			//  359    372    415    422    Ljava/lang/Exception;
			//  375    387    415    422    Ljava/lang/Exception;
			//  390    398    415    422    Ljava/lang/Exception;
			//  401    414    415    422    Ljava/lang/Exception;
			//  463    479    415    422    Ljava/lang/Exception;
			//  496    507    415    422    Ljava/lang/Exception;
			//  509    526    415    422    Ljava/lang/Exception;
			//  531    547    415    422    Ljava/lang/Exception;
			//  551    559    1793   1797   Ljava/lang/Exception;
			//  569    581    1793   1797   Ljava/lang/Exception;
			//  592    598    1793   1797   Ljava/lang/Exception;
			//  604    609    1793   1797   Ljava/lang/Exception;
			//  615    627    1793   1797   Ljava/lang/Exception;
			//  633    640    1793   1797   Ljava/lang/Exception;
			//  664    693    415    422    Ljava/lang/Exception;
			//  695    711    415    422    Ljava/lang/Exception;
			//  715    724    1793   1797   Ljava/lang/Exception;
			//  736    748    1793   1797   Ljava/lang/Exception;
			//  761    768    1793   1797   Ljava/lang/Exception;
			//  775    779    1793   1797   Ljava/lang/Exception;
			//  786    798    1793   1797   Ljava/lang/Exception;
			//  805    813    1793   1797   Ljava/lang/Exception;
			//  831    838    1793   1797   Ljava/lang/Exception;
			//  847    857    415    422    Ljava/lang/Exception;
			//  860    886    415    422    Ljava/lang/Exception;
			//  892    902    415    422    Ljava/lang/Exception;
			//  908    918    415    422    Ljava/lang/Exception;
			//  921    929    415    422    Ljava/lang/Exception;
			//  932    943    415    422    Ljava/lang/Exception;
			//  946    957    415    422    Ljava/lang/Exception;
			//  963    969    415    422    Ljava/lang/Exception;
			//  1005   1019   415    422    Ljava/lang/Exception;
			//  1022   1030   415    422    Ljava/lang/Exception;
			//  1033   1041   415    422    Ljava/lang/Exception;
			//  1050   1063   415    422    Ljava/lang/Exception;
			//  1082   1092   415    422    Ljava/lang/Exception;
			//  1095   1110   415    422    Ljava/lang/Exception;
			//  1113   1124   415    422    Ljava/lang/Exception;
			//  1144   1158   415    422    Ljava/lang/Exception;
			//  1161   1169   415    422    Ljava/lang/Exception;
			//  1172   1180   415    422    Ljava/lang/Exception;
			//  1183   1204   415    422    Ljava/lang/Exception;
			//  1207   1229   415    422    Ljava/lang/Exception;
			//  1232   1261   415    422    Ljava/lang/Exception;
			//  1264   1274   415    422    Ljava/lang/Exception;
			//  1277   1287   415    422    Ljava/lang/Exception;
			//  1290   1300   415    422    Ljava/lang/Exception;
			//  1303   1324   415    422    Ljava/lang/Exception;
			//  1327   1348   415    422    Ljava/lang/Exception;
			//  1351   1367   415    422    Ljava/lang/Exception;
			//  1370   1386   415    422    Ljava/lang/Exception;
			//  1389   1405   415    422    Ljava/lang/Exception;
			//  1408   1424   415    422    Ljava/lang/Exception;
			//  1427   1465   415    422    Ljava/lang/Exception;
			//  1468   1476   415    422    Ljava/lang/Exception;
			//  1479   1484   415    422    Ljava/lang/Exception;
			//  1498   1504   415    422    Ljava/lang/Exception;
			//  1512   1525   415    422    Ljava/lang/Exception;
			//  1528   1592   415    422    Ljava/lang/Exception;
			//  1614   1635   415    422    Ljava/lang/Exception;
			//  1638   1659   415    422    Ljava/lang/Exception;
			//  1679   1684   415    422    Ljava/lang/Exception;
			//  1693   1698   415    422    Ljava/lang/Exception;
			//  1723   1744   415    422    Ljava/lang/Exception;
			//  1753   1767   415    422    Ljava/lang/Exception;
			//  1770   1779   415    422    Ljava/lang/Exception;
			//
			// The error that occurred was:
			//
			// java.lang.IllegalStateException: Expression is linked from several locations: Label_0586:
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
			//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:556)
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

	class Producer implements Runnable {
		long lastTimeStamp;
		int len;
		byte[] pBuffer;
		TimeOutSyncBufferQueue<byte[]> tSync;
		long tempStamp;
		int timeOutCount;

		public Producer(final TimeOutSyncBufferQueue<byte[]> tSync) {
			this.tSync = null;
			this.len = 0;
			this.tempStamp = 0L;
			this.lastTimeStamp = -1L;
			this.pBuffer = null;
			this.timeOutCount = 0;
			this.tSync = tSync;
			MyLog.i(VideoUdpThread.this.TAG, "producer runnable");
		}

		@Override
		public void run() {
			//
			// This method could not be decompiled.
			//
			// Original Bytecode:
			//
			//     2: invokestatic    android/os/Process.setThreadPriority:(I)V
			//     5: sipush          10240
			//     8: newarray        B
			//    10: astore_2
			//    11: aload_0
			//    12: getfield        com/zed3/sipua/VideoUdpThread.Producer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    15: invokestatic    com/zed3/sipua/VideoUdpThread.access.1:(Lcom/zed3/sipua/VideoUdpThread;)Z
			//    18: ifne            22
			//    21: return
			//    22: new             Ljava/net/DatagramPacket;
			//    25: dup
			//    26: aload_2
			//    27: aload_2
			//    28: arraylength
			//    29: invokespecial   java/net/DatagramPacket.<init>:([BI)V
			//    32: astore_1
			//    33: aload_0
			//    34: getfield        com/zed3/sipua/VideoUdpThread.Producer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    37: invokestatic    com/zed3/sipua/VideoUdpThread.access.2:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/net/DatagramSocket;
			//    40: aload_1
			//    41: invokevirtual   java/net/DatagramSocket.receive:(Ljava/net/DatagramPacket;)V
			//    44: aload_0
			//    45: aload_1
			//    46: invokevirtual   java/net/DatagramPacket.getLength:()I
			//    49: putfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//    52: ldc             "GUOK"
			//    54: new             Ljava/lang/StringBuilder;
			//    57: dup
			//    58: ldc             "mdsSocket.receive "
			//    60: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
			//    63: aload_0
			//    64: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//    67: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
			//    70: ldc             " "
			//    72: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
			//    75: aload_0
			//    76: getfield        com/zed3/sipua/VideoUdpThread.Producer.this.0:Lcom/zed3/sipua/VideoUdpThread;
			//    79: invokestatic    com/zed3/sipua/VideoUdpThread.access.2:(Lcom/zed3/sipua/VideoUdpThread;)Ljava/net/DatagramSocket;
			//    82: invokevirtual   java/net/DatagramSocket.getLocalPort:()I
			//    85: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
			//    88: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
			//    91: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
			//    94: pop
			//    95: aload_0
			//    96: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//    99: ifle            107
			//   102: aload_0
			//   103: iconst_0
			//   104: putfield        com/zed3/sipua/VideoUdpThread.Producer.timeOutCount:I
			//   107: getstatic       com/zed3/flow/FlowStatistics.Video_Receive_Data:I
			//   110: aload_0
			//   111: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//   114: iadd
			//   115: putstatic       com/zed3/flow/FlowStatistics.Video_Receive_Data:I
			//   118: aload_0
			//   119: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//   122: bipush          12
			//   124: if_icmple       178
			//   127: aload_0
			//   128: aload_0
			//   129: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//   132: newarray        B
			//   134: putfield        com/zed3/sipua/VideoUdpThread.Producer.pBuffer:[B
			//   137: aload_2
			//   138: iconst_0
			//   139: aload_0
			//   140: getfield        com/zed3/sipua/VideoUdpThread.Producer.pBuffer:[B
			//   143: iconst_0
			//   144: aload_0
			//   145: getfield        com/zed3/sipua/VideoUdpThread.Producer.len:I
			//   148: invokestatic    java/lang/System.arraycopy:(Ljava/lang/Object;ILjava/lang/Object;II)V
			//   151: aload_0
			//   152: getfield        com/zed3/sipua/VideoUdpThread.Producer.tSync:Lcom/zed3/sipua/TimeOutSyncBufferQueue;
			//   155: aload_0
			//   156: getfield        com/zed3/sipua/VideoUdpThread.Producer.pBuffer:[B
			//   159: invokevirtual   com/zed3/sipua/TimeOutSyncBufferQueue.push:(Ljava/lang/Object;)Z
			//   162: pop
			//   163: goto            11
			//   166: astore_1
			//   167: aload_1
			//   168: invokevirtual   java/lang/Exception.printStackTrace:()V
			//   171: goto            11
			//   174: astore_1
			//   175: goto            167
			//   178: goto            11
			//    Exceptions:
			//  Try           Handler
			//  Start  End    Start  End    Type
			//  -----  -----  -----  -----  ---------------------
			//  22     33     166    167    Ljava/lang/Exception;
			//  33     107    174    178    Ljava/lang/Exception;
			//  107    163    174    178    Ljava/lang/Exception;
			//
			// The error that occurred was:
			//
			// java.lang.IllegalStateException: Expression is linked from several locations: Label_0107:
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
			//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:556)
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

	class VideoProcess extends VideoDecodeThread {
		int DelaySum;
		int count;
		boolean firstTime;
		long firstTimeMS;
		long firstTimeStamp;
		int lastTokenPackageSeq;
		List<ReceivePacketInfo> receivePackageList;
		TimeOutSyncBufferQueue<H264Frame> sync;

		public VideoProcess(final TimeOutSyncBufferQueue<H264Frame> sync) {
			super("decode", sync);
			this.firstTime = true;
			this.lastTokenPackageSeq = -1;
			this.receivePackageList = null;
			this.count = 0;
			this.DelaySum = 0;
			this.sync = sync;
			this.receivePackageList = new ArrayList<ReceivePacketInfo>();
		}

		@Override
		public void run() {
//			Process.setThreadPriority(-8);
			while (VideoUdpThread.this.threadFlag) {
				try {
					this.adjustThread();
					final H264Frame h264Frame = this.sync.pop();
					final byte[] data = h264Frame.getData();
					if (data == null) {
						continue;
					}
					VideoUdpThread.this.h264Dec.PlayDecode(data, h264Frame.getFrameType());
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			VideoUdpThread.this.downLatch.countDown();
		}
	}
}
