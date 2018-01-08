package com.zed3.utils;

import com.zed3.audio.AudioUtil;
import com.zed3.flow.FlowStatistics;
import com.zed3.media.RtpStreamReceiver_group;
import com.zed3.net.RtpPacket;

import java.util.ArrayList;
import java.util.List;

public class RtpStreamReceiverUtil {
	static final long SET_NEED_WRITE_AUDIO_DATA_TRUE_DELAY_TIME = 1000L;
	static final int STATE_INSTANCE_CREATED = 1;
	static final int STATE_INSTANCE_DESTROYED = 0;
	static final int STATE_RECEIVING_STARTED = 2;
	static final int STATE_RECEIVING_STOPED = 3;
	static boolean mNeedWriteAudioData = false;
	static final StringBuilder mStringBuilder;
	private static final String tag = "RtpStreamReceiverUtil";
	static List<RtpStreamReceiverType> types;

	static {
		RtpStreamReceiverUtil.mNeedWriteAudioData = true;
		(RtpStreamReceiverUtil.types = new ArrayList<RtpStreamReceiverType>()).add(RtpStreamReceiverType.GROUP_CALL_RECEIVER);
		RtpStreamReceiverUtil.types.add(RtpStreamReceiverType.SINGLE_CALL_RECEIVER);
		mStringBuilder = new StringBuilder();
	}

	private static StringBuilder getStringBuilder() {
		synchronized (RtpStreamReceiverUtil.class) {
			if (RtpStreamReceiverUtil.mStringBuilder.length() > 0) {
				RtpStreamReceiverUtil.mStringBuilder.delete(0, RtpStreamReceiverUtil.mStringBuilder.length());
			}
			return RtpStreamReceiverUtil.mStringBuilder;
		}
	}

	public static boolean needWriteAudioData() {
		synchronized (RtpStreamReceiverUtil.class) {
			return RtpStreamReceiverUtil.mNeedWriteAudioData;
		}
	}

	public static void onAudioModeChanged(final int n) {
		switch (n) {
			default: {
			}
			case 0: {
				RtpStreamReceiver_group.speakermode = 0;
			}
			case 3: {
				RtpStreamReceiver_group.speakermode = 2;
			}
			case 2: {
				RtpStreamReceiver_group.speakermode = 2;
			}
		}
	}

	public static void onReceive(final RtpStreamReceiverType rtpStreamReceiverType, final RtpPacket rtpPacket) {
		final StringBuilder sb = new StringBuilder();
		sb.append("onReceive(" + rtpStreamReceiverType + ",...)");
		rtpStreamReceiverType.setReceiveCount(rtpStreamReceiverType.getReceiveCount() + 1);
		int n;
		if ((n = rtpPacket.getLength() + 42) < 60) {
			n = 60;
		}
		rtpStreamReceiverType.setReceiveTotalLen(rtpStreamReceiverType.getReceiveTotalLen() + n);
		synchronized ("RtpStreamReceiverUtil") {
			FlowStatistics.Voice_Receive_Data += n;
			// monitorexit("RtpStreamReceiverUtil")
			final long currentTimeMillis = System.currentTimeMillis();
			if (currentTimeMillis - rtpStreamReceiverType.getLastWriteReceiveLogTime() > 5000L) {
				rtpStreamReceiverType.setLastWriteReceiveLogTime(currentTimeMillis);
				sb.append(" mReceiveCount " + rtpStreamReceiverType.getReceiveCount() + " mReceiveLen " + n + " mReceiveTotalLen " + rtpStreamReceiverType.getReceiveTotalLen());
				LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
			}
			final long currentTimeMillis2 = System.currentTimeMillis();
			final int sequenceNumber = rtpPacket.getSequenceNumber();
			if (rtpStreamReceiverType.getLastReceiveTime() != 0L) {
				final int n2 = (int) (currentTimeMillis2 - rtpStreamReceiverType.getLastReceiveTime());
				if (n2 > 1000) {
					sb.append(" receive sycle " + n2 + " >1000ms");
					LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
				}
				final int n3 = sequenceNumber - rtpStreamReceiverType.getLastSequenceNumber() - 1;
				if (n3 > 1) {
					rtpStreamReceiverType.setLostTotal(rtpStreamReceiverType.getLostTotal() + n3);
					sb.append(" lost rtpPackets :" + n3 + " mLostTotal " + rtpStreamReceiverType.getLostTotal());
					LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
				}
			}
			rtpStreamReceiverType.setLastReceiveTime(currentTimeMillis2);
			rtpStreamReceiverType.setLastSequenceNumber(sequenceNumber);
			LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
		}
	}

	public static void onStartReceiving(final RtpStreamReceiverType rtpStreamReceiverType) {
		final StringBuilder sb = new StringBuilder();
		sb.append("onStartReceiving(" + rtpStreamReceiverType + ") ");
		rtpStreamReceiverType.setLostTotal(0);
		rtpStreamReceiverType.setLateTotal(0);
		rtpStreamReceiverType.setReceiveCount(0);
		rtpStreamReceiverType.setReceiveTotalLen(0);
		sb.append(" mLostTotal " + rtpStreamReceiverType.getLostTotal());
		sb.append(" mLateTotal " + rtpStreamReceiverType.getLateTotal());
		sb.append(" mReceiveCount " + rtpStreamReceiverType.getReceiveCount());
		sb.append(" ReceiveTotalLen " + rtpStreamReceiverType.getReceiveTotalLen());
		rtpStreamReceiverType.setState(2);
		LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
	}

	public static void onStopReceiving(final RtpStreamReceiverType rtpStreamReceiverType) {
		final StringBuilder sb = new StringBuilder();
		sb.append("onStopReceiving(" + rtpStreamReceiverType + ") ");
		sb.append(" mLostTotal " + rtpStreamReceiverType.getLostTotal());
		sb.append(" mLateTotal " + rtpStreamReceiverType.getLateTotal());
		sb.append(" mReceiveCount " + rtpStreamReceiverType.getReceiveCount());
		sb.append(" ReceiveTotalLen " + rtpStreamReceiverType.getReceiveTotalLen());
		boolean b = true;
		for (final RtpStreamReceiverType rtpStreamReceiverType2 : RtpStreamReceiverUtil.types) {
			if (rtpStreamReceiverType2 != rtpStreamReceiverType && rtpStreamReceiverType2.getState() != 0) {
				b = false;
			}
		}
		if (b) {
			sb.append(" needSetNormal " + b);
			AudioUtil.getInstance().setMode(0);
		}
		rtpStreamReceiverType.setState(3);
		rtpStreamReceiverType.setState(0);
		LogUtil.makeLog("RtpStreamReceiverUtil", sb.toString());
	}

	public static void setNeedWriteAudioData(final boolean mNeedWriteAudioData) {
		synchronized (RtpStreamReceiverUtil.class) {
			RtpStreamReceiverUtil.mNeedWriteAudioData = mNeedWriteAudioData;
		}
	}

	public enum RtpStreamReceiverType {
		GROUP_CALL_RECEIVER("GROUP_CALL_RECEIVER", 0, 0),
		SINGLE_CALL_RECEIVER("SINGLE_CALL_RECEIVER", 1, 0);

		private long mLastReceiveTime;
		private int mLastSequenceNumber;
		private long mLastWriteReceiveLogTime;
		private long mLateTotal;
		private int mLostTotal;
		private int mReceiveCount;
		private int mReceiveTotalLen;
		private int mState;

		private RtpStreamReceiverType(final String s, final int n, final int mState) {
			this.mState = 0;
			this.mLostTotal = 0;
			this.mLateTotal = 0L;
			this.mReceiveCount = 0;
			this.mReceiveTotalLen = 0;
			this.mState = mState;
		}

		public long getLastReceiveTime() {
			return this.mLastReceiveTime;
		}

		public int getLastSequenceNumber() {
			return this.mLastSequenceNumber;
		}

		public long getLastWriteReceiveLogTime() {
			return this.mLastWriteReceiveLogTime;
		}

		public long getLateTotal() {
			return this.mLateTotal;
		}

		public int getLostTotal() {
			return this.mLostTotal;
		}

		public int getReceiveCount() {
			return this.mReceiveCount;
		}

		public int getReceiveTotalLen() {
			return this.mReceiveTotalLen;
		}

		public int getState() {
			return this.mState;
		}

		public void setLastReceiveTime(final long mLastReceiveTime) {
			this.mLastReceiveTime = mLastReceiveTime;
		}

		public void setLastSequenceNumber(final int mLastSequenceNumber) {
			this.mLastSequenceNumber = mLastSequenceNumber;
		}

		public void setLastWriteReceiveLogTime(final long mLastWriteReceiveLogTime) {
			this.mLastWriteReceiveLogTime = mLastWriteReceiveLogTime;
		}

		public void setLateTotal(final int n) {
			this.mLateTotal = this.mLostTotal;
		}

		public void setLostTotal(final int mLostTotal) {
			this.mLostTotal = mLostTotal;
		}

		public void setReceiveCount(final int mReceiveCount) {
			this.mReceiveCount = mReceiveCount;
		}

		public void setReceiveTotalLen(final int mReceiveTotalLen) {
			this.mReceiveTotalLen = mReceiveTotalLen;
		}

		public int setState(final int n) {
			return this.mState;
		}
	}
}
