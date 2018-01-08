package com.zed3.codecs;

import android.content.Intent;
import android.preference.PreferenceManager;

import com.zed3.log.MyLog;
import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.CallActivity;

public class AmrNB extends CodecBase implements Codec {
	private static String amrModeString;
	public static EncodeRate.Mode amrRate;
	public static EncodeRate.Mode requestAmrRate;
	private final int LEN_MODE0;
	private final int LEN_MODE1;
	private final int LEN_MODE2;
	private final int LEN_MODE3;
	private final int LEN_MODE4;
	private final int LEN_MODE5;
	private final int LEN_MODE6;
	private final int LEN_MODE7;
	int[] sizes;
	private final String tag;

	static {
		AmrNB.amrRate = EncodeRate.Mode.MR475;
		AmrNB.requestAmrRate = EncodeRate.Mode.MR475;
	}

	AmrNB() {
		this.tag = "AmrNB";
		this.LEN_MODE0 = 13;
		this.LEN_MODE1 = 14;
		this.LEN_MODE2 = 16;
		this.LEN_MODE3 = 18;
		this.LEN_MODE4 = 20;
		this.LEN_MODE5 = 21;
		this.LEN_MODE6 = 27;
		this.LEN_MODE7 = 32;
		this.sizes = new int[]{12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0};
		this.CODEC_NAME = "AMR";
		this.CODEC_USER_NAME = "AMR";
		this.CODEC_DESCRIPTION = "4.75-12.2kbit";
		this.CODEC_NUMBER = 114;
		this.CODEC_DEFAULT_SETTING = "always";
		AmrNB.amrModeString = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getString("amrMode", "4.75");
		if ("Auto".equals(AmrNB.amrModeString)) {
//			AmrNB.amrRate = MainActivity.mode;
		} else {
			this.setRate(getModeFromString(AmrNB.amrModeString));
		}
		super.update();
	}

	public static EncodeRate.Mode getModeFromString(final String s) {
		if (s.equals("12.2")) {
			return EncodeRate.Mode.MR122;
		}
		if (s.equals("10.2")) {
			return EncodeRate.Mode.MR102;
		}
		if (s.equals("7.95")) {
			return EncodeRate.Mode.MR795;
		}
		if (s.equals("7.4")) {
			return EncodeRate.Mode.MR74;
		}
		if (s.equals("6.7")) {
			return EncodeRate.Mode.MR67;
		}
		if (s.equals("5.9")) {
			return EncodeRate.Mode.MR59;
		}
		if (s.equals("5.15")) {
			return EncodeRate.Mode.MR515;
		}
		if (s.equals("4.75")) {
			return EncodeRate.Mode.MR475;
		}
		return EncodeRate.Mode.N_MODES;
	}

//	public native int amrDecode(final byte[] p0, final short[] p1, final int p2);
//
//	public native int amrEncode(final short[] p0, final int p1, final byte[] p2, final int p3, final EncodeRate.Mode p4);

	public void close() {
		// TODO
	}

	@Override
	public int decode(final byte[] array, final short[] array2, int i) {
		AmrNB.requestAmrRate = ModeChange.getmode((byte) (array[12] >> 4 & 0xF));
		int n = 0;
		for (i = 0; (array[i + 13] & 0x80) != 0x0; ++i) {
		}
		final int n2 = i + 1;
		final byte[] array3 = new byte[160];
		final short[] array4 = new short[160];
		int n3;
		for (i = 0; i < n2; ++i) {
			if (n2 > 10) {
				MyLog.e("EEEEE-DECODE", "frameCount = " + n2);
				MyLog.e("EEEEE-DECODE", "data = " + new String(array));
				return n;
			}
			array3[13] = array[i + 13];
			if (i < n2 - 1) {
				array3[0] &= 0x7F;
			}
			n3 = this.sizes[(byte) (array3[13] >> 3 & 0xF)];
			System.arraycopy(array, n2 + 13 + i * n3, array3, 14, n3);
			// TODO
			//n += this.amrDecode(array3, array4, n3 + 1);
			System.arraycopy(array4, 0, array2, i * 160, 160);
		}
		return n;
	}

	@Override
	public int decode(final byte[] array, final short[] array2, final int n, final int n2) {
		return -1;
	}

	@Override
	public int encode(final short[] array, final int n, final byte[] array2, int i) {
		if ("Auto".equals(AmrNB.amrModeString)) {
			AmrNB.amrRate = AmrNB.requestAmrRate;
		}
		int n2 = 1;
		final int bufferSize = this.getBufferSize(AmrNB.amrRate);
		final int n3 = i / 160;
		final short[] array3 = new short[160];
		final byte[] array4 = new byte[50];
		for (i = 0; i < n3; ++i) {
			System.arraycopy(array, n + i * 160, array3, 0, array3.length);
			if (this.slientCheck != null) {
				// TODO
//				if (this.slientCheck.WebRtcVadProcess(8000, ArrayParser.shortArray2ByteArray(array3), 160) == 1) {
//					break;
//				}
				if (i == n3 - 1) {
					return 0;
				}
			}
		}
		for (i = 0; i < n3; ++i) {
			System.arraycopy(array, n + i * 160, array3, 0, array3.length);
			// TODO
//			n2 += this.amrEncode(array3, 0, array4, bufferSize, AmrNB.amrRate);
			if (i < n3 - 1) {
				array2[i + 12 + 1] = (byte) (array4[13] | 0x80);
			} else {
				array2[i + 12 + 1] = array4[13];
			}
			System.arraycopy(array4, 14, array2, (bufferSize - 1) * i + 13 + n3, bufferSize - 1);
			array2[12] = (byte) (RtpStreamReceiver_signal.judged_cmr & 0x7F);
			array2[12] = (byte) (array2[12] << 4 & 0xF0);
		}
		return n2;
	}

	// TODO
	int getBufferSize(final EncodeRate.Mode mode) {
		return 13;
//		switch (mode) {
//			case 1: {
//				return 13;
//			}
//			case 2: {
//				return 14;
//			}
//			case 3: {
//				return 16;
//			}
//			case 4: {
//				return 18;
//			}
//			case 5: {
//				return 20;
//			}
//			case 6: {
//				return 21;
//			}
//			case 7: {
//				return 27;
//			}
//			case 8: {
//				return 32;
//			}
//			default: {
//				return 13;
//			}
//		}
	}

	public int getMode() {
		return AmrNB.amrRate.ordinal();
	}

	@Override
	public void init() {
		this.load();
		if (this.isLoaded()) {
			// TODO
//			this.open();
		}
	}

	@Override
	void load() {
//		try {
//			System.loadLibrary("amrnb_jni");
//			super.load();
//		} catch (Throwable t) {
//		}
	}

//	public native int open();

	public void setRate(final EncodeRate.Mode amrRate) {
		if (amrRate != EncodeRate.Mode.N_MODES && amrRate != AmrNB.amrRate) {
			AmrNB.amrRate = amrRate;
			SipUAApp.mContext.sendBroadcast(new Intent(CallActivity.ACTION_AMR_RATE_CHANGE));
		}
	}
}
