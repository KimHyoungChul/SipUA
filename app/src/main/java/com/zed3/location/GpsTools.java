package com.zed3.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zed3.log.MyLog;
import com.zed3.sipua.LocalConfigSettings;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import java.math.BigInteger;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class GpsTools {
	public static long D_UnixTime;
	private static long LIMIT_TIME_FOR_GPSRESTART;
	public static final int Port;
	public static long Previous_LocTime;
	public static long Previous_UnixTime;
	public static double Previous_gps_x;
	public static double Previous_gps_y;
	public static String ServerIP;
	private static final String TAG;
	static int countTS;
	static int countXY;
	private static GpsInfo gpsInfo;
	public static List<GpsInfo> gpsList;
	private static boolean needLog;
	private static long sGpsListEmtyTime;
	private static long sGpsStopUpdateTime;
	private static final ReentrantLock sLock;
	static BroadcastReceiver timeChangedReceiver;

	static {
		GpsTools.ServerIP = MemoryMg.getInstance().IPAddress;
		Port = MemoryMg.getInstance().IPPort;
		GpsTools.needLog = false;
		sLock = new ReentrantLock();
		TAG = GpsTools.class.getSimpleName();
		GpsTools.LIMIT_TIME_FOR_GPSRESTART = 60000L;
		GpsTools.Previous_gps_x = 0.0;
		GpsTools.Previous_gps_y = 0.0;
		GpsTools.Previous_UnixTime = 0L;
		GpsTools.Previous_LocTime = 0L;
		GpsTools.D_UnixTime = 0L;
		GpsTools.gpsInfo = null;
		GpsTools.timeChangedReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				MyLog.d("testgps", "GPSTools#timeChangedReceiver intent:" + intent.toString());
				reInitTime();
			}
		};
	}

	public static String BitToE_id(final byte[] array) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 4; i < array.length; ++i) {
			final int abs = Math.abs(array[i]);
			final char[] array2 = null;
			char[] chars = null;
			Label_0068:
			{
				if (abs < 48 || abs > 57) {
					chars = array2;
					if (abs < 97) {
						break Label_0068;
					}
					chars = array2;
					if (abs > 122) {
						break Label_0068;
					}
				}
				chars = Character.toChars(abs);
			}
			if (chars != null) {
				sb.append(chars);
			}
		}
		return sb.toString();
	}

	public static int BitToE_idCount(final byte[] array) {
		return (array[1] & 0x3) << 6 | (array[2] & 0xFC) >> 2;
	}

	public static String[] BitToE_ids(final byte[] array, final int n) {
		final String[] array2 = new String[n];
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; ++i) {
			if (sb.length() > 0) {
				sb.delete(0, sb.length());
			}
			final int n2 = i * 32 + 2;
			for (int j = 0; j < 32; ++j) {
				final int abs = Math.abs((array[n2 + j] & 0x3) << 6 | (array[n2 + j + 1] & 0xFC) >> 2);
				final char[] array3 = null;
				char[] chars = null;
				Label_0154:
				{
					if (abs < 48 || abs > 57) {
						chars = array3;
						if (abs < 97) {
							break Label_0154;
						}
						chars = array3;
						if (abs > 122) {
							break Label_0154;
						}
					}
					chars = Character.toChars(abs);
				}
				if (chars != null) {
					sb.append(chars);
				}
			}
			array2[i] = sb.toString();
		}
		return array2;
	}

	public static int BitToExtralType(final byte[] array) {
		return (array[1] & 0x3) << 3 | (array[2] & 0xE0) >> 5;
	}

	public static String BitToOpenCloseGPSID(final byte[] array) {
		final int n = (array[2] & 0x7) << 3 | (array[3] & 0xE0) >> 5;
		final int n2 = (n - 4) / 8;
		String string = "";
		for (int i = 0; i < n2; ++i) {
			string = String.valueOf(new StringBuilder(String.valueOf(string)).append((array[i + 3] & 0x1) << 3 | (array[i + 3 + 1] & 0xE0) >> 5).toString()) + ((array[i + 3 + 1] & 0x1E) >> 1);
		}
		String string2 = string;
		if ((n - 4) % 8 != 0) {
			string2 = String.valueOf(string) + ((array[n2 + 3] & 0x1) << 3 | (array[n2 + 3 + 1] & 0xE0) >> 5);
		}
		MyLog.i("openclosegpsID", String.valueOf(string2) + " len:" + n + " i:" + n2);
		return string2.trim();
	}

	public static int BitToPDUExtendType(final byte[] array) {
		return (array[0] & 0x3C) >> 2;
	}

	public static int BitToSuccess(final byte[] array) {
		return (array[0] & 0x3) << 6 | (array[1] & 0xFC) >> 2;
	}

	public static long BitToUnixTime(final byte[] array, int n) {
		final byte[] array2 = new byte[8];
		int i;
		for (i = array2.length - 1, n = 11; i >= 0; --i, --n) {
			if (n >= 4) {
				array2[i] = array[n];
			} else {
				array2[i] = 0;
			}
		}
		return ((array2[0] & 0xFF) << 56) + ((array2[1] & 0xFF) << 48) + ((array2[2] & 0xFF) << 40) + ((array2[3] & 0xFF) << 32) + ((array2[4] & 0xFF) << 24) + ((array2[5] & 0xFF) << 16) + ((array2[6] & 0xFF) << 8) + (array2[7] & 0xFF);
	}

	public static String BitToUploadCycleID(final byte[] array) {
		String string = "";
		final int n = (array[2] & 0x3) << 4 | (array[3] & 0xF0) >> 4;
		if (n == 0) {
			return getNumString(getNumIDBinary(byteArray2BinaryArray(array)), ((((array[3] & 0xF) << 4 | (array[4] & 0xE0) >> 5) + 7) * 8 - 4 - 4) / 4).trim();
		}
		final int n2 = (n - 4) / 8;
		for (int i = 0; i < n2; ++i) {
			string = String.valueOf(new StringBuilder(String.valueOf(string)).append((array[i + 4] & 0xF0) >> 4).toString()) + (array[i + 4] & 0xF);
		}
		String string2 = string;
		if ((n - 4) % 8 != 0) {
			string2 = String.valueOf(string) + ((array[n2 + 4] & 0xF0) >> 4);
		}
		return string2.trim();
	}

	public static short BytetoMsgTypeShort(final byte[] array) {
		int n;
		if (array[0] >= 0) {
			n = 0 + array[0];
		} else {
			n = array[0] + 256;
		}
		final int n2 = n * 256;
		int n3;
		if (array[1] >= 0) {
			n3 = n2 + array[1];
		} else {
			n3 = n2 + 256 + array[1];
		}
		return (short) n3;
	}

	public static byte BytetoResultbyte(final byte[] array) {
		byte b = 0;
		for (int i = 20; i < 21; ++i) {
			b = array[i];
		}
		return b;
	}

	public static String BytetoTerminalNumStr(final byte[] array) {
		final byte[] array2 = new byte[16];
		System.arraycopy(array, 4, array2, 0, array2.length);
		return new String(array2).trim();
	}

	public static boolean CheckNetWork(final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			final NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
			if (allNetworkInfo != null) {
				for (int i = 0; i < allNetworkInfo.length; ++i) {
					if (allNetworkInfo[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void CloseGPSByCode(final Context context) {
		if (!Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), "gps")) {
			return;
		}
		try {
			MyLog.i("ffff", "close gps");
			Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), "gps", false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void CloseGPSByMySelf(final double n, final double n2, final float n3, final float n4, final int n5, final long n6, final String s) {
		while (true) {
			try {
				final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
				sendThread.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 10, n, n2, n3, n4, n5, n6, s));
				sendThread.start();
				MyLog.i("terminal close gps", "\u7ec8\u7aef\u4e3b\u52a8\u5173\u95edGPS\u4e0a\u4f20  \u670d\u52a1\u5668\u56de\u5e94\u7ed3\u679c");
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	public static void CloseGPSByServer(final Context context) {
		Label_0061_Outer:
		while (true) {
			while (true) {
				while (true) {
					try {
						final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
						sendThread.SetContent(ReplyOpenCloseGPS(MemoryMg.getInstance().TerminalNum, 0));
						sendThread.start();
						MemoryMg.getInstance().GpsLockState = false;
						if (Tools.getCurrentGpsMode() == 0) {
							GpsManage.getInstance(context).CloseGPS();
							MyLog.i("CloseGPSByServer", "CloseGPSByServer");
							return;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						continue Label_0061_Outer;
					}
					break;
				}
				Receiver.GetCurUA().GPSCloseLock();
				continue;
			}
		}
	}

	public static double Distance(double sin, double n, double sin2, double n2) {
		n = 3.141592653589793 * n / 180.0;
		n2 = 3.141592653589793 * n2 / 180.0;
		sin2 = (sin - sin2) * 3.141592653589793 / 180.0;
		sin = Math.sin((n - n2) / 2.0);
		sin2 = Math.sin(sin2 / 2.0);
		return 2.0 * 6378137.0 * Math.asin(Math.sqrt(sin * sin + Math.cos(n) * Math.cos(n2) * sin2 * sin2));
	}

	public static byte[] FloatUses(final int n) {
		return new byte[]{(byte) (n & 0xFF), (byte) (n >> 8 & 0xFF), (byte) (n >> 16 & 0xFF), (byte) (n >> 24 & 0xFF)};
	}

	public static byte[] FloattoByte(final float n) {
		return FloatUses(Float.floatToRawIntBits(n));
	}

	public static int GetGPSSpeedLevel(final float n) {
		final int n2 = 1;
		int n3 = 1;
		int n4;
		if (n <= 0.0f) {
			n4 = 0;
		} else if (n <= 28.0f) {
			final int n5 = (int) n;
			if (n - (int) n < 0.5) {
				n3 = 0;
			}
			n4 = n5 + n3;
		} else {
			final double n6 = Math.log(n / 16.0f) / Math.log(1.038) + 13.0;
			final int n7 = (int) n6;
			int n8;
			if (n6 - (int) n6 >= 0.5) {
				n8 = n2;
			} else {
				n8 = 0;
			}
			n4 = n7 + n8;
		}
		int n9 = n4;
		if (n4 > 127) {
			n9 = 127;
		}
		MyLog.d("testgps", "GetGPSSpeedLevel(" + n + ") return " + n9);
		return n9;
	}

	public static double GetGPSSpeedValue(final int n) {
		float n2;
		if (n <= 0) {
			n2 = 0.0f;
		} else if (n <= 28) {
			n2 = n;
		} else {
			n2 = (float) (16.0 * Math.pow(1.038, n - 13));
		}
		MyLog.d("testgps", "GetGPSSpeedValue(" + n + ") return " + n2);
		return n2;
	}

	public static int GetGPSX(final double n) {
		final double n2 = 180.0 + n;
		final double n3 = Math.pow(2.0, 25.0) / 360.0;
		MyLog.d("testgps", "GetGPSX(double " + n + ") return " + (int) (n2 * n3));
		return (int) (n2 * n3);
	}

	public static int GetGPSY(final double n) {
		final double n2 = 90.0 + n;
		final double n3 = Math.pow(2.0, 24.0) / 180.0;
		MyLog.d("testgps", "GetGPSY(double " + n + ") return " + (int) (n2 * n3));
		return (int) (n2 * n3);
	}

	public static boolean GetGspLockState(final Context context) {
		return Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), "gps");
	}

	private static int GetHeight(final float n) {
		return getAltitudeLevle((int) n);
	}

	private static String GetIsPadding(final int n) {
		try {
			if (Integer.toBinaryString(n * 4 + 4).length() < 7) {
				return "";
			}
			if (n % 2 == 0) {
				return "1111";
			}
			return "";
		} catch (Exception ex) {
			MyLog.e("GpsTool GetIsPadding error", ex.toString());
			return "";
		}
	}

	public static int GetLocationTimeValByModel(final int n) {
		final int n2 = 15;
		int n3;
		if (n == 0) {
			n3 = 5;
		} else {
			n3 = n2;
			if (n != 1) {
				if (n == 2) {
					return 30;
				}
				n3 = n2;
				if (n == 3) {
					return 60;
				}
			}
		}
		return n3;
	}

	private static String GetNumLen(final int n) {
		String s;
		try {
			final String binaryString = Integer.toBinaryString(n);
			if (binaryString.length() <= 6) {
				s = String.format("%06d", Integer.parseInt(binaryString));
			} else {
				if (binaryString.length() != 7) {
					throw new RuntimeException("lip extension type error");
				}
				final String binaryString2 = Integer.toBinaryString((n + 4) / 8 - 7);
				Log.i(GpsTools.TAG, "GetNumLen:" + binaryString2);
				s = "000000" + String.format("%07d", Integer.parseInt(binaryString2));
			}
		} catch (Exception ex) {
			MyLog.e("GpsTool GetNumLen error", ex.toString());
			return "";
		}
		return s;
	}

	private static String GetVal(int i, final int n) {
		int n2 = i;
		if (i < 0) {
			n2 = 0;
		}
		try {
			String s = Integer.toBinaryString(n2);
			i = s.length();
			if (i > n) {
				return "";
			}
			String s2 = s;
			if (s.length() < n) {
				int length;
				for (length = s.length(), i = 0; i < n - length; ++i) {
					s = "0" + s;
				}
				s2 = s;
			}
			MyLog.d("testgps", "GetVal(int " + n2 + "," + n + ") return " + s2);
			return s2;
		} catch (Exception ex) {
			MyLog.e("GpsTool GetVal error", ex.toString());
			ex.printStackTrace();
			MyLog.e("testgps", "GetVal(int " + n2 + "," + n + ") return emty");
			return "";
		}
	}

	private static String GetVal(final long n, final int n2) {
		long n3 = n;
		if (n < 0L) {
			n3 = 0L;
		}
		try {
			String s = Long.toBinaryString(n3);
			if (s.length() > n2) {
				return "";
			}
			String s2 = s;
			if (s.length() < n2) {
				for (int length = s.length(), i = 0; i < n2 - length; ++i) {
					s = "0" + s;
				}
				s2 = s;
			}
			MyLog.d("testgps", "GetVal(long " + n3 + "," + n2 + ") return " + s2);
			return s2;
		} catch (Exception ex) {
			MyLog.e("GpsTool GetVal error", ex.toString());
			ex.printStackTrace();
			MyLog.e("testgps", "GetVal(long " + n3 + "," + n2 + ") return emty");
			return "";
		}
	}

	public static byte[] GpsByte(String s, int i, final double n, final double n2, final float n3, final float n4, int length, final long n5, String s2) {
		LogUtil.makeLog("testgps", "GpsByte(" + s + "," + i + "," + n + "," + n2 + "," + n3 + "," + n4 + "," + length + "," + n5 + "," + s2 + ")");
		final byte[] array = new byte[69];
		final String string = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("")).append(GetVal(1, 2)).toString())).append(GetVal(3, 4)).toString())).append("00").toString()) + GetVal(4, 4);
		String s3;
		if (n == 0.0) {
			s3 = String.valueOf(string) + "0000000000000000000000000";
		} else {
			final String getVal = GetVal(GetGPSX(n), 25);
			if (TextUtils.isEmpty((CharSequence) getVal)) {
				LogUtil.makeLog("testgps", "GpsByte() gpsx (" + n + ") switch error ignore");
				return null;
			}
			s3 = String.valueOf(string) + getVal;
		}
		String s4;
		if (n2 == 0.0) {
			s4 = String.valueOf(s3) + "000000000000000000000000";
		} else {
			final String getVal2 = GetVal(GetGPSY(n2), 24);
			if (TextUtils.isEmpty((CharSequence) getVal2)) {
				LogUtil.makeLog("testgps", "GpsByte() gpsy (" + n2 + ") switch error ignore");
				return null;
			}
			s4 = String.valueOf(s3) + getVal2;
		}
		String s5;
		if (n4 == 0.0f) {
			s5 = String.valueOf(s4) + "000000000000";
		} else {
			final String string2 = String.valueOf(s4) + "0";
			final String getVal3 = GetVal(GetHeight(n4), 11);
			if (TextUtils.isEmpty((CharSequence) getVal3)) {
				LogUtil.makeLog("testgps", "GpsByte() gpsheight (" + n4 + ") switch error ignore");
				return null;
			}
			s5 = String.valueOf(string2) + getVal3;
		}
		final String string3 = String.valueOf(s5) + GetVal(5, 3);
		final int getGPSSpeedLevel = GetGPSSpeedLevel(n3);
		final String getVal4 = GetVal(getGPSSpeedLevel, 7);
		byte[] array2;
		if (TextUtils.isEmpty((CharSequence) getVal4)) {
			LogUtil.makeLog("testgps", "GpsByte() speedLevel (" + getGPSSpeedLevel + ") switch error ignore");
			array2 = null;
		} else {
			final String string4 = String.valueOf(string3) + getVal4;
			final String getVal5 = GetVal(length, 8);
			if (TextUtils.isEmpty((CharSequence) getVal5)) {
				LogUtil.makeLog("testgps", "GpsByte() gpsdirection (" + length + ") switch error ignore");
				return null;
			}
			final String string5 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(string4)).append(getVal5).toString())).append("10").toString())).append(GetVal(i, 8)).toString()) + GetVal(17, 5);
			i = s.length();
			String s6 = String.valueOf(new StringBuilder(String.valueOf(string5)).append(GetNumLen(i * 4 + 4)).toString()) + "1000";
			final char[] charArray = s.toCharArray();
			String s8;
			String s7;
			for (length = charArray.length, i = 0; i < length; ++i) {
				s7 = (s8 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[i])).toString())));
				if (s7.length() != 4) {
					s8 = String.format("%04d", Integer.parseInt(s7));
				}
				s6 = String.valueOf(s6) + s8;
			}
			s = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(s6)).append(GetIsPadding(s.length())).toString())).append("10011").toString())).append("000000").toString()) + "0101000";
			final String getVal6 = GetVal(n5, 64);
			if (TextUtils.isEmpty((CharSequence) getVal6)) {
				LogUtil.makeLog("testgps", "GpsByte() UnixTime (" + n5 + ") switch error ignore");
				return null;
			}
			s2 = String.valueOf(new StringBuilder(String.valueOf(s)).append(getVal6).toString()) + strToE_id(s2);
			length = s2.length() % 8;
			s = s2;
			if (length != 0) {
				i = 0;
				s = s2;
				while (i < 8 - length) {
					s = String.valueOf(s) + "1";
					++i;
				}
			}
			i = 0;
			while (true) {
				array2 = array;
				if (i >= s.length() / 8) {
					break;
				}
				s2 = s.substring(i * 8, i * 8 + 8);
				length = array[i];
				array[i] = (byte) (convertBinaryToInt(s2.toCharArray()) | 0x0);
				++i;
			}
		}
		return array2;
	}

	public static byte[] GpsByte(String s, int n, List<GpsInfo> s2, int n2) {
		// TODO
		return null;
	}

	public static byte[] InttoByte(final int n) {
		return new byte[]{(byte) ((0xFF000000 & n) >> 24), (byte) ((0xFF0000 & n) >> 16), (byte) ((0xFF00 & n) >> 8), (byte) (n & 0xFF)};
	}

	public static void IsUploadGpsInfo(final BDLocation bdLocation, final MyHandlerThread myHandlerThread) {
		// TODO
	}

	public static byte[] LoginByte(String s, final double n, final double n2, final float n3) {
		final byte[] array = new byte[28];
		array[0] = (byte) ((array[0] & 0x3F) | 0x40);
		array[0] = (byte) ((array[0] & 0xC3) | 0xC);
		array[0] &= (byte) 252;
		array[1] = (byte) ((array[1] & 0xF) | 0x40);
		if (n == 0.0) {
			array[1] &= (byte) 240;
			final byte b = array[2];
			array[2] = 0;
			final byte b2 = array[3];
			array[3] = 0;
			final byte b3 = array[4];
			array[4] = 0;
		} else {
			String s3;
			final String s2 = s3 = Integer.toBinaryString(GetGPSX(n));
			if (s2.length() == 24) {
				s3 = "0" + s2;
			}
			if (s3.length() == 25) {
				array[1] = (byte) ((array[1] & 0xF0) | convertBinaryToInt(s3.substring(0, 4).toCharArray()));
				final int convertBinaryToInt = convertBinaryToInt(s3.substring(4, 12).toCharArray());
				final byte b4 = array[2];
				array[2] = (byte) (convertBinaryToInt | 0x0);
				final int convertBinaryToInt2 = convertBinaryToInt(s3.substring(12, 20).toCharArray());
				final byte b5 = array[3];
				array[3] = (byte) (convertBinaryToInt2 | 0x0);
				array[4] = (byte) ((array[4] & 0x7) | convertBinaryToInt(s3.substring(20, 25).toCharArray()) << 3);
			} else {
				MyLog.e("gpsLogin", "length is not enough");
			}
		}
		String s5;
		final String s4 = s5 = Integer.toBinaryString(GetGPSY(42.1));
		if (s4.length() == 23) {
			s5 = "0" + s4;
		}
		if (s5.length() == 24) {
			array[4] = (byte) ((array[4] & 0xF8) | convertBinaryToInt(s5.substring(0, 3).toCharArray()));
			final int convertBinaryToInt3 = convertBinaryToInt(s5.substring(3, 11).toCharArray());
			final byte b6 = array[5];
			array[5] = (byte) (convertBinaryToInt3 | 0x0);
			final int convertBinaryToInt4 = convertBinaryToInt(s5.substring(11, 19).toCharArray());
			final byte b7 = array[6];
			array[6] = (byte) (convertBinaryToInt4 | 0x0);
			array[7] = (byte) ((array[7] & 0x7) | convertBinaryToInt(s5.substring(19, 24).toCharArray()) << 3);
		} else {
			MyLog.e("gpsLogin", "length is not enough");
		}
		array[7] &= (byte) 248;
		final byte b8 = array[8];
		array[8] = 0;
		array[9] = (byte) ((array[9] & 0xF) | 0x30);
		String s7;
		final String s6 = s7 = Integer.toBinaryString(GetGPSSpeedLevel(n3));
		if (s6.length() != 8) {
			s7 = String.format("%08d", Integer.parseInt(s6));
		}
		array[9] = (byte) ((array[9] & 0xF0) | convertBinaryToInt(s7.substring(0, 4).toCharArray()));
		array[10] = (byte) ((array[10] & 0x1F) | convertBinaryToInt(s7.substring(4, 8).toCharArray()) << 5);
		array[10] &= (byte) 224;
		array[11] &= 0x1F;
		array[11] = (byte) ((array[11] & 0xE0) | 0x10);
		final int length = s.length();
		array[12] &= 0x7;
		array[12] = (byte) ((array[12] & 0xF8) | 0x4);
		array[13] = (byte) ((array[13] & 0x3F) | 0x40);
		String s9;
		final String s8 = s9 = Integer.toBinaryString(length * 4 + 4);
		if (s8.length() != 6) {
			s9 = String.format("%06d", Integer.parseInt(s8));
		}
		array[13] = (byte) ((array[13] & 0xC0) | convertBinaryToInt(s9.toCharArray()));
		array[14] = (byte) ((array[14] & 0xF) | 0x80);
		final char[] charArray = s.toCharArray();
		final int length2 = charArray.length;
		s = "";
		for (int i = 0; i < length2; ++i) {
			String s11;
			final String s10 = s11 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[i])).toString()));
			if (s10.length() != 4) {
				s11 = String.format("%04d", Integer.parseInt(s10));
			}
			s = String.valueOf(s) + s11;
		}
		array[14] = (byte) ((array[14] & 0xF0) | convertBinaryToInt(s.substring(0, 4).toCharArray()));
		final int n4 = (s.length() - 4) % 8;
		String s12 = s;
		if (n4 != 0) {
			for (int j = 0; j < 8 - n4; ++j) {
				s = String.valueOf(s) + "1";
			}
			s12 = s;
		}
		final int n5 = (s12.length() - 4) / 8;
		int n6 = 0;
		for (int k = 1; k <= n5; ++k) {
			s = s12.substring((n6 * 2 + 1) * 4, (n6 * 2 + 1) * 4 + 8);
			final byte b9 = array[k + 4];
			array[k + 14] = (byte) (convertBinaryToInt(s.toCharArray()) | 0x0);
			++n6;
		}
		return array;
	}

	public static byte[] LoginByte(String string, final double n, final double n2, final float n3, final float n4, int i) {
		final byte[] array = new byte[32];
		final String string2 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("")).append(GetVal(1, 2)).toString())).append(GetVal(3, 4)).toString())).append("00").toString()) + GetVal(4, 4);
		String s;
		if (n == 0.0) {
			s = String.valueOf(string2) + "0000000000000000000000000";
		} else {
			s = String.valueOf(string2) + GetVal(GetGPSX(n), 25);
		}
		String s2;
		if (n2 == 0.0) {
			s2 = String.valueOf(s) + "000000000000000000000000";
		} else {
			s2 = String.valueOf(s) + GetVal(GetGPSY(n2), 24);
		}
		String s3;
		if (n4 == 0.0f) {
			s3 = String.valueOf(s2) + "000000000000";
		} else {
			s3 = String.valueOf(new StringBuilder(String.valueOf(s2)).append("0").toString()) + GetVal(GetHeight(n4), 11);
		}
		final String string3 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(s3)).append(GetVal(5, 3)).toString())).append(GetVal(GetGPSSpeedLevel(n3), 7)).toString())).append(GetVal(i, 8)).toString())).append("10").toString())).append("00000000").toString()) + GetVal(17, 5);
		i = string.length();
		String s4 = String.valueOf(new StringBuilder(String.valueOf(string3)).append(GetNumLen(i * 4 + 4)).toString()) + "1000";
		final char[] charArray = string.toCharArray();
		int length;
		String s6;
		String s5;
		for (length = charArray.length, i = 0; i < length; ++i) {
			s5 = (s6 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[i])).toString())));
			if (s5.length() != 4) {
				s6 = String.format("%04d", Integer.parseInt(s5));
			}
			s4 = String.valueOf(s4) + s6;
		}
		final String string4 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(s4)).append(GetIsPadding(string.length())).toString())).append("10100").toString())).append("001000").toString()) + "00000001";
		final int n5 = string4.length() % 8;
		string = string4;
		if (n5 != 0) {
			i = 0;
			string = string4;
			while (i < 8 - n5) {
				string = String.valueOf(string) + "1";
				++i;
			}
		}
		String substring;
		byte b;
		for (i = 0; i < string.length() / 8; ++i) {
			substring = string.substring(i * 8, i * 8 + 8);
			b = array[i];
			array[i] = (byte) (convertBinaryToInt(substring.toCharArray()) | 0x0);
		}
		return array;
	}

	public static void OpenGPSByCode(final Context context) {
		if (Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), "gps")) {
			return;
		}
		try {
			MyLog.i("ffff", "open gps");
			Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), "gps", true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void OpenGPSByMySelf(final double n, final double n2, final float n3, final float n4, final int n5, final long n6, final String s) {
		while (true) {
			try {
				final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
				sendThread.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, 9, n, n2, n3, n4, n5, n6, s));
				sendThread.start();
				MyLog.i("terminal open gps", "\u7ec8\u7aef\u4e3b\u52a8\u5f00\u542fGPS\u4e0a\u4f20 \u670d\u52a1\u5668\u56de\u5e94\u7ed3\u679c");
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	public static void OpenGPSByServer(final Context context) {
		Label_0061_Outer:
		while (true) {
			while (true) {
				while (true) {
					try {
						final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
						sendThread.SetContent(ReplyOpenCloseGPS(MemoryMg.getInstance().TerminalNum, 1));
						sendThread.start();
						MemoryMg.getInstance().GpsLockState = true;
						if (Tools.getCurrentGpsMode() == 0) {
							GpsManage.getInstance(context).startGps();
							MyLog.i("opengpsbyserver", "opengpsbyserver");
							return;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						continue Label_0061_Outer;
					}
					break;
				}
				Receiver.GetCurUA().GPSOpenLock();
				continue;
			}
		}
	}

	public static byte[] ReplyOpenCloseGPS(String s, int i) {
		final byte[] array = new byte[14];
		final String s2 = "";
		array[0] = (byte) ((array[0] & 0x3F) | 0x40);
		array[0] = (byte) ((array[0] & 0xC3) | 0x28);
		array[0] = (byte) ((array[0] & 0xFC) | 0x2);
		array[1] = (byte) ((array[1] & 0x1) | i << 1);
		array[1] = (byte) ((array[1] & 0xFE) | 0x1);
		array[2] = (byte) ((array[2] & 0xF) | convertBinaryToInt(Integer.toBinaryString(17).substring(1).toCharArray()) << 4);
		String s4;
		final String s3 = s4 = Integer.toBinaryString(s.length() * 4 + 4);
		if (s3.length() != 6) {
			s4 = String.format("%06d", Integer.parseInt(s3));
		}
		i = convertBinaryToInt(s4.substring(0, 4).toCharArray());
		array[2] = (byte) ((array[2] & 0xF0) | i);
		i = convertBinaryToInt(s4.substring(4, 6).toCharArray());
		array[3] = (byte) ((array[3] & 0x3F) | i << 6);
		array[3] = (byte) ((array[3] & 0xC3) | 0x20);
		final char[] charArray = s.toCharArray();
		final int length = charArray.length;
		i = 0;
		s = s2;
		while (i < length) {
			String s6;
			final String s5 = s6 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[i])).toString()));
			if (s5.length() != 4) {
				s6 = String.format("%04d", Integer.parseInt(s5));
			}
			s = String.valueOf(s) + s6;
			++i;
		}
		i = convertBinaryToInt(s.substring(0, 2).toCharArray());
		array[3] = (byte) ((array[3] & 0xFC) | i);
		final int n = (s.length() - 2) % 8;
		String s7 = s;
		if (n != 0) {
			for (i = 0; i < 8 - n; ++i) {
				s = String.valueOf(s) + "1";
			}
			s7 = s;
		}
		int n2;
		byte b;
		for (n2 = (s7.length() - 2) / 8, i = 0; i < n2; ++i) {
			s = s7.substring(i * 8 + 2, i * 8 + 10);
			b = array[i + 4];
			array[i + 4] = (byte) (convertBinaryToInt(s.toCharArray()) | 0x0);
		}
		return array;
	}

	public static byte[] ReplyServerDelOccur(String s, int i, int n) {
		final String string = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("")).append(GetVal(1, 2)).toString())).append(GetVal(7, 4)).toString())).append("1").toString())).append(GetVal(0, 8)).toString())).append(GetVal(17, 5)).toString())).append(GetVal(s.length() * 4 + 4, 6)).toString()) + GetVal(8, 4);
		final char[] charArray = s.toCharArray();
		final int length = charArray.length;
		int j = 0;
		s = string;
		while (j < length) {
			String s3;
			final String s2 = s3 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[j])).toString()));
			if (s2.length() != 4) {
				s3 = String.format("%04d", Integer.parseInt(s2));
			}
			s = String.valueOf(s) + s3;
			++j;
		}
		final String string2 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(s)).append(GetVal(20, 5)).toString())).append(GetVal(9, 6)).toString())).append(i).toString()) + GetVal(n, 8);
		n = string2.length() % 8;
		s = string2;
		if (n != 0) {
			i = 0;
			s = string2;
			while (i < 8 - n) {
				s = String.valueOf(s) + "1";
				++i;
			}
		}
		final byte[] array = new byte[14];
		String substring;
		for (i = 0; i < s.length() / 8; ++i) {
			substring = s.substring(i * 8, i * 8 + 8);
			n = array[i];
			array[i] = (byte) (convertBinaryToInt(substring.toCharArray()) | 0x0);
		}
		return array;
	}

	public static byte[] ReplyServerSetOrUpdateOccur(String s, int i, int n) {
		String s3;
		final String s2 = s3 = Integer.toBinaryString(1);
		if (s2.length() != 2) {
			s3 = String.format("%02d", Integer.parseInt(s2));
		}
		final String string = String.valueOf("") + s3;
		String s5;
		final String s4 = s5 = Integer.toBinaryString(6);
		if (s4.length() != 4) {
			s5 = String.format("%04d", Integer.parseInt(s4));
		}
		final String string2 = String.valueOf(new StringBuilder(String.valueOf(string)).append(s5).toString()) + "1";
		String s7;
		final String s6 = s7 = Integer.toBinaryString(0);
		if (s6.length() != 8) {
			s7 = String.format("%08d", Integer.parseInt(s6));
		}
		String s8 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(string2)).append(s7).append("00").append("10001").toString())).append(GetNumLen(s.length() * 4 + 4)).toString()) + "1000";
		final char[] charArray = s.toCharArray();
		for (int length = charArray.length, j = 0; j < length; ++j) {
			String s10;
			final String s9 = s10 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[j])).toString()));
			if (s9.length() != 4) {
				s10 = String.format("%04d", Integer.parseInt(s9));
			}
			s8 = String.valueOf(s8) + s10;
		}
		final String string3 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(s8)).append(GetIsPadding(s.length())).toString())).append("10011").toString()) + "001001";
		final String s11 = s = Integer.toBinaryString(n);
		if (s11.length() != 8) {
			s = String.format("%08d", Integer.parseInt(s11));
		}
		final String string4 = String.valueOf(new StringBuilder(String.valueOf(string3)).append(s).toString()) + i;
		n = string4.length() % 8;
		s = string4;
		if (n != 0) {
			i = 0;
			s = string4;
			while (i < 8 - n) {
				s = String.valueOf(s) + "1";
				++i;
			}
		}
		final byte[] array = new byte[17];
		String substring;
		for (i = 0; i < s.length() / 8; ++i) {
			substring = s.substring(i * 8, i * 8 + 8);
			n = array[i];
			array[i] = (byte) (convertBinaryToInt(substring.toCharArray()) | 0x0);
		}
		return array;
	}

	public static byte[] ReplyUploadCycleByte(int i, String string, int length, final int n) {
		final byte[] array = new byte[18];
		final String string2 = String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("")).append(GetVal(1, 2)).toString())).append(GetVal(5, 4)).toString())).append("1").toString())).append(GetVal(length, 8)).toString())).append(GetVal(i, 8)).toString())).append(GetVal(n, 7)).toString())).append(GetVal(0, 2)).toString()) + GetVal(17, 5);
		i = string.length();
		String s = String.valueOf(new StringBuilder(String.valueOf(string2)).append(GetNumLen(i * 4 + 4)).toString()) + "1000";
		final char[] charArray = string.toCharArray();
		String s3;
		String s2;
		for (length = charArray.length, i = 0; i < length; ++i) {
			s2 = (s3 = Integer.toBinaryString(Integer.parseInt(new StringBuilder(String.valueOf(charArray[i])).toString())));
			if (s2.length() != 4) {
				s3 = String.format("%04d", Integer.parseInt(s2));
			}
			s = String.valueOf(s) + s3;
		}
		final String string3 = String.valueOf(s) + GetIsPadding(string.length());
		length = string3.length() % 8;
		string = string3;
		if (length != 0) {
			i = 0;
			string = string3;
			while (i < 8 - length) {
				string = String.valueOf(string) + "1";
				++i;
			}
		}
		String substring;
		for (i = 0; i < string.length() / 8; ++i) {
			substring = string.substring(i * 8, i * 8 + 8);
			length = array[i];
			array[i] = (byte) (convertBinaryToInt(substring.toCharArray()) | 0x0);
		}
		return array;
	}

	public static void SendLoginUdp() {
		MyLog.d("testgps", "GPSPacket#SendLoginUdp enter ");
		try {
			final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
			sendThread.SetContent(LoginByte(MemoryMg.getInstance().TerminalNum, 0.0, 0.0, 0.0f, 0.0f, 0));
			sendThread.start();
			MyLog.i("GPSPacket", "SendLoginUdp gps login...");
		} catch (Exception ex) {
			MyLog.e("testgps", "GPSPacket#SendLoginUdp exception =  " + ex.getMessage());
			MyLog.e("gpsPacket SendLoginUdp error:", ex.toString());
		}
	}

	public static byte[] ShorttoByte(final short n) {
		return new byte[]{(byte) (n >> 8 & 0xFF), (byte) (n & 0xFF)};
	}

	public static int TimeToUnix() {
		long time = 0L;
		try {
			time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).getTime();
			return (int) (time / 1000L);
		} catch (Exception ex) {
			ex.printStackTrace();
			return (int) (time / 1000L);
		}
	}

	public static String UnixTimeToNormal(final int n) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(n * 1000L));
	}

	public static void UploadGPSByTerminal(final int n, final double n2, final double n3, final float n4, final float n5, final int n6, final long n7, final String s) {
		Log.i("secondTrace", "UploadGPSByTerminal **");
		try {
			final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
			sendThread.SetContent(GpsByte(MemoryMg.getInstance().TerminalNum, n, n2, n3, n4, n5, n6, n7, s));
			sendThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void UploadGPSByTerminal(final int n, final List<GpsInfo> list) {
		Log.i("secondTrace", "UploadGPSByTerminal ##");
		try {
			new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port, n, list).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String byteArray2BinaryArray(final byte[] array) {
		return new BigInteger(1, array).toString(2);
	}

	public static String byteToBit(final byte b) {
		return new StringBuilder().append((byte) (b >> 7 & 0x1)).append((byte) (b >> 6 & 0x1)).append((byte) (b >> 5 & 0x1)).append((byte) (b >> 4 & 0x1)).append((byte) (b >> 3 & 0x1)).append((byte) (b >> 2 & 0x1)).append((byte) (b >> 1 & 0x1)).append((byte) (b >> 0 & 0x1)).toString();
	}

	private static int convertBinaryToInt(final char[] array) {
		int n = 0;
		int n2 = 0;
		for (int i = array.length - 1; i >= 0; --i) {
			int n3 = 2;
			int n4;
			if (n2 == 0) {
				n4 = 1;
			} else if (n2 == 1) {
				n4 = 2;
			} else {
				int n5 = 1;
				while (true) {
					n4 = n3;
					if (n5 >= n2) {
						break;
					}
					n3 *= 2;
					++n5;
				}
			}
			n += Integer.parseInt(new StringBuilder(String.valueOf(array[i])).toString()) * n4;
			++n2;
		}
		return n;
	}

	public static byte[] doubleToByte(final double n) {
		final byte[] array = new byte[8];
		long doubleToLongBits = Double.doubleToLongBits(n);
		for (int i = 0; i < array.length; ++i) {
			array[i] = (byte) (Object) new Long(doubleToLongBits);
			doubleToLongBits >>= 8;
		}
		return array;
	}

	private static long getAbsTime(final long n) {
		return Math.abs(n - getCurrentLocalTime1());
	}

	private static int getAltitudeLevle(final int n) {
		int n2;
		if (n < -200) {
			n2 = 0;
		} else if (n >= -200 && n <= 1000) {
			n2 = n + 200 + 1;
		} else if (n > 1000 && n <= 2450) {
			n2 = (n - 1000) / 2 + 1201;
		} else if (n > 2450 && n <= 11525) {
			n2 = (n - 2450) / 75 + 1926;
		} else {
			n2 = 2047;
		}
		Log.i("altitude", "getAltitudeLevle(" + n + ")" + "return " + n2);
		return n2;
	}

	private static int getAltitudeValue(final int n) {
		if (n < 0) {
			Log.e("altitude", "getAltitudeValue(" + n + ") error 'level < 0' " + "return " + -200);
			return -200;
		}
		int n2;
		if (n == 0) {
			n2 = -200;
		} else if (n > 0 && n <= 1201) {
			n2 = n - 200 - 1;
		} else if (n > 1201 && n <= 1926) {
			n2 = (n - 1201) * 2 + 1000;
		} else if (n > 1926 && n <= 2047) {
			n2 = (n - 1926) * 75 + 2450;
		} else {
			n2 = 11525;
		}
		Log.i("altitude", "getAltitudeValue(" + n + ")" + "return " + n2);
		return n2;
	}

	public static long getCurrentLocalTime1() {
		return System.currentTimeMillis() / 1000L;
	}

	public static long getCurrentRealTime() {
		return SystemClock.elapsedRealtime() / 1000L;
	}

	public static long getCurrentUnixTime(final long n, final long n2) {
		final long currentRealTime = getCurrentRealTime();
		if (currentRealTime > n) {
			return currentRealTime - n + n2;
		}
		return -1L;
	}

	public static String getE_id() {
		return UUID.randomUUID().toString().trim().replaceAll("-", "");
	}

	private static String getInfoData(final int n, final GpsInfo gpsInfo) {
		synchronized (GpsTools.class) {
			LogUtil.makeLog("testgps", "GpsByte() getInfoData(" + n + "," + gpsInfo.gps_x + "," + gpsInfo.gps_y + "," + gpsInfo.gps_speed + "," + gpsInfo.gps_height + "," + gpsInfo.gps_direction + "," + gpsInfo.UnixTime + "," + gpsInfo.E_id + ")");
			final String getVal = GetVal(4, 4);
			String s;
			if (gpsInfo.gps_x == 0.0) {
				s = String.valueOf(getVal) + "0000000000000000000000000";
			} else {
				s = String.valueOf(getVal) + GetVal(GetGPSX(gpsInfo.gps_x), 25);
			}
			String s2;
			if (gpsInfo.gps_y == 0.0) {
				s2 = String.valueOf(s) + "000000000000000000000000";
			} else {
				s2 = String.valueOf(s) + GetVal(GetGPSY(gpsInfo.gps_y), 24);
			}
			String s3;
			if (gpsInfo.gps_height == 0.0f) {
				s3 = String.valueOf(s2) + "000000000000";
			} else {
				s3 = String.valueOf(new StringBuilder(String.valueOf(s2)).append("0").toString()) + GetVal(GetHeight(gpsInfo.gps_height), 11);
			}
			final String string = String.valueOf(s3) + GetVal(5, 3);
			final int n2 = (int) gpsInfo.gps_speed;
			final StringBuilder sb = new StringBuilder(String.valueOf(string));
			int n3 = n2;
			if (n2 > 127) {
				n3 = 127;
			}
			return String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(sb.append(GetVal(n3, 7)).toString())).append(GetVal(gpsInfo.gps_direction, 8)).toString())).append(GetVal(n, 8)).toString())).append("10011").toString())).append("000000").toString())).append("0101000").toString())).append(GetVal(gpsInfo.UnixTime, 64)).toString()) + strToE_id(gpsInfo.E_id);
		}
	}

	public static String getNumIDBinary(String s) {
		final String s2 = s = s.substring(s.indexOf("000000") + 6 + 7);
		if (s2.startsWith("1000")) {
			s = s2.substring(4);
		}
		return s;
	}

	public static String getNumString(final String s, final int n) {
		String string = "";
		int n2 = 0;
		for (int i = 0; i < n; ++i) {
			string = String.valueOf(string) + Byte.parseByte(s.substring(n2, n2 + 4), 2);
			n2 += 4;
		}
		return string;
	}

	public static long getRealUnixTime() {
		while (true) {
			MyLog.d("testgps", "GPSTools#getUnixTime lock enter");
			while (true) {
				Label_0129:
				{
					try {
						try {
							final boolean tryLock = GpsTools.sLock.tryLock(1L, TimeUnit.SECONDS);
							MyLog.d("testgps", "GPSTools#getUnixTime result:" + tryLock);
							if (tryLock) {
								final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
								return getCurrentUnixTime(sharedPreferences.getLong("Realtime", -1L), sharedPreferences.getLong("UnixTime", -1L));
							}
							break Label_0129;
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
						MyLog.d("testgps", "GPSTools#savuUnixTime lock exit");
						return -1L;
					} finally {
						GpsTools.sLock.unlock();
					}
				}
				GpsTools.sLock.unlock();
				continue;
			}
		}
	}

	private static long getScanSpan() {
		return GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel) * 1000;
	}

	public static long getServerUnixTime() {
		return SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getLong("serverUnixTime", 0L);
	}

	public static double getSpeed(double distance, final double n, final long n2, final double n3, final double n4, final long n5) {
		final double n6 = 0.0;
		try {
			distance = Distance(distance, n, n3, n4);
			distance /= n5 - n2;
			return Math.abs(distance);
		} catch (Exception ex) {
			distance = n6;
			return Math.abs(distance);
		}
	}

	public static long getUnixTime(final long n) {
		while (true) {
			MyLog.d("testgps", "GPSTools#getUnixTime lock enter");
			while (true) {
				Label_0139:
				{
					try {
						try {
							final boolean tryLock = GpsTools.sLock.tryLock(1L, TimeUnit.SECONDS);
							MyLog.d("testgps", "GPSTools#getUnixTime result:" + tryLock);
							if (tryLock) {
								final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
								final long long1 = sharedPreferences.getLong("UnixTime", -1L);
								final long long2 = sharedPreferences.getLong("LocalTime", -1L);
								GpsTools.sLock.unlock();
								return n - long2 + long1;
							}
							break Label_0139;
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
						MyLog.d("testgps", "GPSTools#savuUnixTime lock exit");
						return 0L;
					} finally {
						GpsTools.sLock.unlock();
					}
				}
				GpsTools.sLock.unlock();
				continue;
			}
		}
	}

	public static double getX(final int n) {
		return n / 3.3554432E7 * 360.0 - 180.0;
	}

	public static double getY(final int n) {
		return n / 1.6777216E7 * 180.0 - 90.0;
	}

	public static boolean isDirtyData(final double n, final double n2, final long n3, final double n4, final double n5, final long n6) {
		return getSpeed(n, n2, n3, n4, n5, n6) > 42.0;
	}

	private static boolean isGetUnixTime() {
		return SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getLong("UnixTime", -1L) != -1L;
	}

	public static void onLocationChanged(final Location p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     2: monitorenter
		//     3: invokestatic    com/zed3/sipua/SipUAApp.getInstance:()Lcom/zed3/sipua/SipUAApp;
		//     6: invokevirtual   com/zed3/sipua/SipUAApp.getmHandlerThread:()Lcom/zed3/location/MyHandlerThread;
		//     9: astore          9
		//    11: lconst_0
		//    12: lstore_1
		//    13: aload_0
		//    14: ifnull          22
		//    17: aload           9
		//    19: ifnonnull       35
		//    22: ldc_w           "testgps"
		//    25: ldc_w           "GPSTools#registerLocationListener location == null"
		//    28: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//    31: ldc             Lcom/zed3/location/GpsTools;.class
		//    33: monitorexit
		//    34: return
		//    35: new             Lcom/zed3/location/GpsInfo;
		//    38: dup
		//    39: invokespecial   com/zed3/location/GpsInfo.<init>:()V
		//    42: putstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    45: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    48: aload_0
		//    49: invokevirtual   android/location/Location.getLongitude:()D
		//    52: putfield        com/zed3/location/GpsInfo.gps_x:D
		//    55: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    58: aload_0
		//    59: invokevirtual   android/location/Location.getLatitude:()D
		//    62: putfield        com/zed3/location/GpsInfo.gps_y:D
		//    65: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    68: aload_0
		//    69: invokevirtual   android/location/Location.getAltitude:()D
		//    72: d2f
		//    73: putfield        com/zed3/location/GpsInfo.gps_height:F
		//    76: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    79: aload_0
		//    80: invokevirtual   android/location/Location.getSpeed:()F
		//    83: putfield        com/zed3/location/GpsInfo.gps_speed:F
		//    86: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//    89: invokestatic    com/zed3/location/GpsTools.getE_id:()Ljava/lang/String;
		//    92: putfield        com/zed3/location/GpsInfo.E_id:Ljava/lang/String;
		//    95: ldc_w           "testgps"
		//    98: new             Ljava/lang/StringBuilder;
		//   101: dup
		//   102: ldc_w           "location["
		//   105: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   108: aload_0
		//   109: invokevirtual   android/location/Location.getLongitude:()D
		//   112: invokevirtual   java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		//   115: ldc_w           ","
		//   118: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   121: aload_0
		//   122: invokevirtual   android/location/Location.getLatitude:()D
		//   125: invokevirtual   java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		//   128: ldc_w           ","
		//   131: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   134: aload_0
		//   135: invokevirtual   android/location/Location.getTime:()J
		//   138: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   141: ldc_w           "]"
		//   144: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   147: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   150: invokestatic    com/zed3/utils/LogUtil.makeLog:(Ljava/lang/String;Ljava/lang/String;)V
		//   153: aload_0
		//   154: invokevirtual   android/location/Location.getTime:()J
		//   157: lstore          7
		//   159: lload           7
		//   161: lconst_0
		//   162: lcmp
		//   163: ifeq            31
		//   166: invokestatic    java/lang/System.currentTimeMillis:()J
		//   169: ldc2_w          1000
		//   172: ldiv
		//   173: lstore_3
		//   174: lload_3
		//   175: lstore_1
		//   176: lload_3
		//   177: getstatic       com/zed3/location/GpsTools.Previous_LocTime:J
		//   180: lcmp
		//   181: ifne            459
		//   184: lload_3
		//   185: lstore_1
		//   186: invokestatic    java/lang/System.currentTimeMillis:()J
		//   189: ldc2_w          1000
		//   192: ldiv
		//   193: lstore          5
		//   195: lload           5
		//   197: lstore_1
		//   198: getstatic       com/zed3/location/GpsTools.sGpsStopUpdateTime:J
		//   201: lconst_0
		//   202: lcmp
		//   203: ifne            409
		//   206: lload           5
		//   208: lstore_1
		//   209: invokestatic    java/lang/System.currentTimeMillis:()J
		//   212: putstatic       com/zed3/location/GpsTools.sGpsStopUpdateTime:J
		//   215: lload           5
		//   217: lstore_3
		//   218: lload_3
		//   219: lstore_1
		//   220: invokestatic    com/zed3/location/GpsTools.isGetUnixTime:()Z
		//   223: ifeq            31
		//   226: lload_3
		//   227: lstore_1
		//   228: ldc_w           "testgps"
		//   231: new             Ljava/lang/StringBuilder;
		//   234: dup
		//   235: ldc_w           "GPSTools#registerLocationListener isGetUnixTime ="
		//   238: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   241: invokestatic    com/zed3/location/GpsTools.isGetUnixTime:()Z
		//   244: invokevirtual   java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		//   247: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   250: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   253: lload_3
		//   254: lstore_1
		//   255: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   258: lload_3
		//   259: invokestatic    com/zed3/location/GpsTools.getUnixTime:(J)J
		//   262: putfield        com/zed3/location/GpsInfo.UnixTime:J
		//   265: lload_3
		//   266: lstore_1
		//   267: ldc_w           "testgps"
		//   270: new             Ljava/lang/StringBuilder;
		//   273: dup
		//   274: ldc_w           "GpsTools#registerLocationListener gpsInfo:gps_x: "
		//   277: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   280: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   283: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   286: invokevirtual   java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		//   289: ldc_w           ",gps_y:"
		//   292: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   295: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   298: getfield        com/zed3/location/GpsInfo.gps_y:D
		//   301: invokevirtual   java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		//   304: ldc_w           ",UnixTime:"
		//   307: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   310: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   313: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   316: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   319: ldc_w           ",getTime:"
		//   322: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   325: lload           7
		//   327: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   330: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   333: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   336: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   339: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   342: d2i
		//   343: ifeq            357
		//   346: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   349: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   352: lconst_0
		//   353: lcmp
		//   354: ifne            474
		//   357: ldc_w           "testgps"
		//   360: new             Ljava/lang/StringBuilder;
		//   363: dup
		//   364: ldc_w           "GPSTools#registerLocationListener gpsInfo.gps_x ="
		//   367: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   370: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   373: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   376: invokevirtual   java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		//   379: ldc_w           "gpsInfo.UnixTime"
		//   382: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   385: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   388: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   391: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   394: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   397: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   400: goto            31
		//   403: astore_0
		//   404: ldc             Lcom/zed3/location/GpsTools;.class
		//   406: monitorexit
		//   407: aload_0
		//   408: athrow
		//   409: lload           5
		//   411: lstore_3
		//   412: lload           5
		//   414: lstore_1
		//   415: invokestatic    java/lang/System.currentTimeMillis:()J
		//   418: getstatic       com/zed3/location/GpsTools.sGpsStopUpdateTime:J
		//   421: lsub
		//   422: getstatic       com/zed3/location/GpsTools.LIMIT_TIME_FOR_GPSRESTART:J
		//   425: lcmp
		//   426: iflt            218
		//   429: lload           5
		//   431: lstore_1
		//   432: invokestatic    com/zed3/sipua/ui/Receiver.GetCurUA:()Lcom/zed3/sipua/UserAgent;
		//   435: invokevirtual   com/zed3/sipua/UserAgent.restartGps:()V
		//   438: lload           5
		//   440: lstore_1
		//   441: lconst_0
		//   442: putstatic       com/zed3/location/GpsTools.sGpsStopUpdateTime:J
		//   445: lload           5
		//   447: lstore_3
		//   448: goto            218
		//   451: astore_0
		//   452: aload_0
		//   453: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   456: goto            267
		//   459: lload_3
		//   460: lstore_1
		//   461: lload_3
		//   462: putstatic       com/zed3/location/GpsTools.Previous_LocTime:J
		//   465: lload_3
		//   466: lstore_1
		//   467: lconst_0
		//   468: putstatic       com/zed3/location/GpsTools.sGpsStopUpdateTime:J
		//   471: goto            218
		//   474: lload_1
		//   475: invokestatic    com/zed3/location/GpsTools.getAbsTime:(J)J
		//   478: ldc2_w          2
		//   481: invokestatic    com/zed3/location/GpsTools.getScanSpan:()J
		//   484: lmul
		//   485: lcmp
		//   486: ifle            515
		//   489: ldc_w           "testgps"
		//   492: new             Ljava/lang/StringBuilder;
		//   495: dup
		//   496: ldc_w           "GPSTools#registerLocationListener getAbsTime(currentTime) > 2 * getScanSpan(),currentTime="
		//   499: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   502: lload_1
		//   503: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   506: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   509: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   512: goto            31
		//   515: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   518: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   521: invokestatic    com/zed3/location/GpsTools.saveServerUnixTime:(J)V
		//   524: getstatic       com/zed3/location/GpsTools.Previous_gps_x:D
		//   527: dconst_0
		//   528: dcmpl
		//   529: ifne            592
		//   532: ldc_w           "testgps"
		//   535: ldc_w           "GPSTools#registerLocationListener Previous_gps_x == 0"
		//   538: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   541: aload           9
		//   543: aload           9
		//   545: getfield        com/zed3/location/MyHandlerThread.mInnerHandler:Lcom/zed3/location/MyHandlerThread.InnerHandler;
		//   548: iconst_1
		//   549: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   552: invokestatic    android/os/Message.obtain:(Landroid/os/Handler;ILjava/lang/Object;)Landroid/os/Message;
		//   555: invokevirtual   com/zed3/location/MyHandlerThread.sendMessage:(Landroid/os/Message;)V
		//   558: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   561: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   564: putstatic       com/zed3/location/GpsTools.Previous_gps_x:D
		//   567: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   570: getfield        com/zed3/location/GpsInfo.gps_y:D
		//   573: putstatic       com/zed3/location/GpsTools.Previous_gps_y:D
		//   576: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   579: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   582: putstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   585: lconst_0
		//   586: putstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   589: goto            31
		//   592: getstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   595: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   598: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   601: lcmp
		//   602: iflt            648
		//   605: ldc_w           "testgps"
		//   608: new             Ljava/lang/StringBuilder;
		//   611: dup
		//   612: ldc_w           "GpsTools#registerLocationListener Previous_UnixTime >= gpsInfo.UnixTime ,time:"
		//   615: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   618: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   621: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   624: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   627: ldc_w           ",Previous_UnixTime"
		//   630: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   633: getstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   636: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   639: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   642: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   645: goto            31
		//   648: getstatic       com/zed3/location/GpsTools.Previous_gps_x:D
		//   651: getstatic       com/zed3/location/GpsTools.Previous_gps_y:D
		//   654: getstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   657: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   660: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   663: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   666: getfield        com/zed3/location/GpsInfo.gps_y:D
		//   669: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   672: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   675: invokestatic    com/zed3/location/GpsTools.isDirtyData:(DDJDDJ)Z
		//   678: ifeq            818
		//   681: ldc_w           "testgps"
		//   684: new             Ljava/lang/StringBuilder;
		//   687: dup
		//   688: ldc_w           "GpsTools#isDirtyData gpsInfo.UnixTime:"
		//   691: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   694: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   697: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   700: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   703: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   706: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   709: getstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   712: lconst_0
		//   713: lcmp
		//   714: ifne            738
		//   717: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   720: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   723: putstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   726: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   729: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   732: putstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   735: goto            31
		//   738: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   741: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   744: getstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   747: lsub
		//   748: ldc2_w          60
		//   751: lcmp
		//   752: ifle            806
		//   755: aload           9
		//   757: aload           9
		//   759: getfield        com/zed3/location/MyHandlerThread.mInnerHandler:Lcom/zed3/location/MyHandlerThread.InnerHandler;
		//   762: iconst_1
		//   763: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   766: invokestatic    android/os/Message.obtain:(Landroid/os/Handler;ILjava/lang/Object;)Landroid/os/Message;
		//   769: invokevirtual   com/zed3/location/MyHandlerThread.sendMessage:(Landroid/os/Message;)V
		//   772: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   775: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   778: putstatic       com/zed3/location/GpsTools.Previous_gps_x:D
		//   781: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   784: getfield        com/zed3/location/GpsInfo.gps_y:D
		//   787: putstatic       com/zed3/location/GpsTools.Previous_gps_y:D
		//   790: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   793: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   796: putstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   799: lconst_0
		//   800: putstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   803: goto            31
		//   806: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   809: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   812: putstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   815: goto            31
		//   818: aload           9
		//   820: aload           9
		//   822: getfield        com/zed3/location/MyHandlerThread.mInnerHandler:Lcom/zed3/location/MyHandlerThread.InnerHandler;
		//   825: iconst_1
		//   826: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   829: invokestatic    android/os/Message.obtain:(Landroid/os/Handler;ILjava/lang/Object;)Landroid/os/Message;
		//   832: invokevirtual   com/zed3/location/MyHandlerThread.sendMessage:(Landroid/os/Message;)V
		//   835: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   838: getfield        com/zed3/location/GpsInfo.gps_x:D
		//   841: putstatic       com/zed3/location/GpsTools.Previous_gps_x:D
		//   844: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   847: getfield        com/zed3/location/GpsInfo.gps_y:D
		//   850: putstatic       com/zed3/location/GpsTools.Previous_gps_y:D
		//   853: getstatic       com/zed3/location/GpsTools.gpsInfo:Lcom/zed3/location/GpsInfo;
		//   856: getfield        com/zed3/location/GpsInfo.UnixTime:J
		//   859: putstatic       com/zed3/location/GpsTools.Previous_UnixTime:J
		//   862: lconst_0
		//   863: putstatic       com/zed3/location/GpsTools.D_UnixTime:J
		//   866: goto            31
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  3      11     403    409    Any
		//  22     31     403    409    Any
		//  35     159    403    409    Any
		//  166    174    451    459    Ljava/lang/Exception;
		//  166    174    403    409    Any
		//  176    184    451    459    Ljava/lang/Exception;
		//  176    184    403    409    Any
		//  186    195    451    459    Ljava/lang/Exception;
		//  186    195    403    409    Any
		//  198    206    451    459    Ljava/lang/Exception;
		//  198    206    403    409    Any
		//  209    215    451    459    Ljava/lang/Exception;
		//  209    215    403    409    Any
		//  220    226    451    459    Ljava/lang/Exception;
		//  220    226    403    409    Any
		//  228    253    451    459    Ljava/lang/Exception;
		//  228    253    403    409    Any
		//  255    265    451    459    Ljava/lang/Exception;
		//  255    265    403    409    Any
		//  267    357    403    409    Any
		//  357    400    403    409    Any
		//  415    429    451    459    Ljava/lang/Exception;
		//  415    429    403    409    Any
		//  432    438    451    459    Ljava/lang/Exception;
		//  432    438    403    409    Any
		//  441    445    451    459    Ljava/lang/Exception;
		//  441    445    403    409    Any
		//  452    456    403    409    Any
		//  461    465    451    459    Ljava/lang/Exception;
		//  461    465    403    409    Any
		//  467    471    451    459    Ljava/lang/Exception;
		//  467    471    403    409    Any
		//  474    512    403    409    Any
		//  515    589    403    409    Any
		//  592    645    403    409    Any
		//  648    735    403    409    Any
		//  738    803    403    409    Any
		//  806    815    403    409    Any
		//  818    866    403    409    Any
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0218:
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

	private static void reInitTime() {
		try {
			GpsTools.sLock.lock();
			if (isGetUnixTime()) {
				final long currentLocalTime1 = getCurrentLocalTime1();
				final long currentRealTime = getCurrentRealTime();
				final long realUnixTime = getRealUnixTime();
				if (realUnixTime != -1L) {
					saveLocalTime(currentLocalTime1);
					saveRealTime(currentRealTime);
					saveUnixTime(realUnixTime);
				}
			}
		} finally {
			GpsTools.sLock.unlock();
		}
	}

	public static void registerTimeChangedReceiver() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.TIME_SET");
		intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
		intentFilter.addAction("android.intent.action.DATE_CHANGED");
		SipUAApp.getAppContext().registerReceiver(GpsTools.timeChangedReceiver, intentFilter);
	}

	public static void saveLocalTime(final long n) {
		MyLog.d("testgps", "GPSTools#saveLocalTime enter param localtime = " + n);
		final SharedPreferences.Editor edit = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putLong("LocalTime", n);
		edit.commit();
		MyLog.d("testgps", "GPSTools#saveLocalTime exit");
	}

	public static void saveRealTime(final long n) {
		MyLog.d("testgps", "GPSTools#saveRealTime enter param realtime = " + n);
		final SharedPreferences.Editor edit = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putLong("Realtime", n);
		edit.commit();
		MyLog.d("testgps", "GPSTools#saveLocalTime exit");
	}

	public static void saveServerUnixTime(final long n) {
		MyLog.d("testgps", "GPSTools#saveServerUnixTime enter param unixTime = " + n);
		final SharedPreferences.Editor edit = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putLong("serverUnixTime", n);
		edit.commit();
		MyLog.d("testgps", "GPSTools#saveServerUnixTime exit");
	}

	public static void saveTime(final long n, final long n2, final long n3) {
		LogUtil.makeLog("testgps", "GPSTools#saveTime (UnixTime " + n + ",LocalTime " + n2 + ",realTime " + n3 + ")");
		MyLog.d("testgps", "GPSTools#saveTime enter");
		try {
			GpsTools.sLock.lock();
			saveUnixTime(n);
			saveLocalTime(n2);
			saveRealTime(n3);
			GpsTools.sLock.unlock();
			MyLog.d("testgps", "GPSTools#saveTime exit");
		} finally {
			GpsTools.sLock.unlock();
		}
	}

	public static void saveUnixTime(final long n) {
		MyLog.d("testgps", "GPSTools#saveUnixTime enter param unixTime = " + n);
		final SharedPreferences.Editor edit = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putLong("UnixTime", n);
		edit.commit();
		MyLog.d("testgps", "GPSTools#saveUnixTime exit");
	}

	public static void setLocationOption(final LocationClient locationClient) {
		final LocationClientOption locOption = new LocationClientOption();
		locOption.setProdName("zed3app");
		locOption.setOpenGps(true);
		final int mGpsLocationTime = LocalConfigSettings.SdcardConfig.pool().mGpsLocationTime;
		if (mGpsLocationTime > 0) {
			Log.i("configTrace", "set gps location time = " + mGpsLocationTime);
			locOption.setScanSpan(mGpsLocationTime);
		} else {
			final int getLocationTimeValByModel = GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel);
			Log.i("configTrace", "set gps location time = " + getLocationTimeValByModel);
			locOption.setScanSpan(getLocationTimeValByModel * 1000);
		}
		locOption.setCoorType("bd09ll");
		if (MemoryMg.getInstance().GpsLocationModel == 1) {
			Log.i("secondTrace", "Hight_Accuracy location");
			locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
			GpsTools.LIMIT_TIME_FOR_GPSRESTART = 60000L;
		} else if (MemoryMg.getInstance().GpsLocationModel == 2) {
			Log.i("secondTrace", "Device_Sensors location");
			locOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
			GpsTools.LIMIT_TIME_FOR_GPSRESTART = 300000L;
		}
		locationClient.setLocOption(locOption);
		final StringBuilder sb = new StringBuilder("setLocationOption() ");
		String s;
		if (MemoryMg.getInstance().GpsLocationModel == 1) {
			s = "LocationMode.Hight_Accuracy";
		} else {
			s = "LocationMode.Device_Sensors";
		}
		LogUtil.makeLog("testgps", sb.append(s).toString());
	}

	public static void setServer(final String s) {
		MemoryMg.getInstance().IPAddress = s;
		GpsTools.ServerIP = s;
	}

	public static String strToE_id(final String s) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			String s3;
			final String s2 = s3 = Integer.toBinaryString(s.charAt(i));
			if (s2.length() != 8) {
				s3 = String.format("%08d", Integer.parseInt(s2));
			}
			sb.append(s3);
		}
		return sb.toString();
	}

	private static void testAltitudeConver() {
		final int n = 0;
		final int[] array2;
		final int[] array = array2 = new int[20];
		array2[0] = -201;
		array2[1] = -200;
		array2[2] = -199;
		array2[3] = -198;
		array2[4] = 999;
		array2[5] = 1000;
		array2[6] = 1002;
		array2[7] = 1003;
		array2[8] = 2449;
		array2[9] = 2450;
		array2[10] = 2524;
		array2[11] = 2525;
		array2[12] = 2526;
		array2[13] = 11374;
		array2[14] = 11375;
		array2[15] = 11376;
		array2[16] = 11450;
		array2[17] = 11451;
		array2[18] = 11525;
		array2[19] = 11526;
		final int[] array3 = {0, 1, 2, 3, 1200, 1201, 1202, 1926, 1927, 1928, 2044, 2045, 2046, 2047};
		for (int length = array.length, i = 0; i < length; ++i) {
			final int n2 = array[i];
			Log.i("testAltitude", "value/level:" + n2 + "/" + getAltitudeLevle(n2));
		}
		for (int length2 = array3.length, j = n; j < length2; ++j) {
			final int n3 = array3[j];
			Log.i("testAltitude", "value/level:" + getAltitudeValue(n3) + "/" + n3);
		}
	}

	private static void testSpeed(final GpsInfo gpsInfo) {
		switch (GpsTools.countTS) {
			case 0: {
				gpsInfo.gps_speed = 1.0f;
				break;
			}
			case 1: {
				gpsInfo.gps_speed = 30.0f;
				break;
			}
			case 2: {
				gpsInfo.gps_speed = 67.0f;
				break;
			}
			case 3: {
				gpsInfo.gps_speed = 125.0f;
				break;
			}
			case 5: {
				GpsTools.countTS = -1;
				break;
			}
		}
		++GpsTools.countTS;
	}

	private static void testXY(final GpsInfo gpsInfo) {
		switch (GpsTools.countXY) {
			case 0: {
				gpsInfo.gps_x = 10.1;
				gpsInfo.gps_y = 10.1;
				break;
			}
			case 1: {
				gpsInfo.gps_x = 10.1;
				gpsInfo.gps_y = -10.1;
				break;
			}
			case 2: {
				gpsInfo.gps_x = -10.1;
				gpsInfo.gps_y = 10.1;
				break;
			}
			case 3: {
				gpsInfo.gps_x = -10.1;
				gpsInfo.gps_y = -10.1;
				break;
			}
			case 4: {
				gpsInfo.gps_x = 160.0;
				gpsInfo.gps_y = 80.0;
				break;
			}
			case 5: {
				gpsInfo.gps_x = -160.0;
				gpsInfo.gps_y = -80.0;
				GpsTools.countXY = -1;
				break;
			}
		}
		++GpsTools.countXY;
	}

	public static void unRegisterTimeChangedReceiver() {
		SipUAApp.getAppContext().unregisterReceiver(GpsTools.timeChangedReceiver);
	}
}
