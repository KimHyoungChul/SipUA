package com.zed3.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.zed3.log.MyLog;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ReceiveThread extends Thread {
	private static final int CYCLEUP = 2;
	private static final int EXITSYSTEM = 0;
	private static final int MESSAGE_CHECK_NETWORK = 1001;
	private static final int NORMALSTATE = 3;
	private static final int SAVEVALUE = 4;
	public static final String TAG = "ReceiveThread";
	public static boolean flag;
	public static List<GpsInfo> infoList;
	private long LocalTime;
	private long RealTime;
	private int TIMEOUT;
	private long UnixTime;
	Context context;
	int count;
	int cyclex;
	int delType;
	int flow;
	GPSPacket gpsPacket;
	Handler handler;
	private Looper myLooper;
	int occurCircle;
	int occurType;
	private GpsListener regListener;
	private boolean returnFlag;
	DatagramSocket socket;
	boolean tipFlag;

	static {
		ReceiveThread.flag = true;
	}

	public ReceiveThread(final DatagramSocket socket, final Context context, final GPSPacket gpsPacket) {
		this.TIMEOUT = 500;
		this.flow = 0;
		this.returnFlag = true;
		this.cyclex = 0;
		this.occurType = 0;
		this.occurCircle = -1;
		this.delType = -1;
		this.tipFlag = false;
		this.UnixTime = 0L;
		this.RealTime = 0L;
		this.LocalTime = 0L;
		this.regListener = null;
		this.count = 0;
		this.handler = new Handler() {
			public void handleMessage(final Message message) {
				MyLog.d("testgps", "ReceiverThread#handleMessage message what = " + message.what);
				Label_0222:
				{
					Label_0175:
					{
						try {
							switch (message.what) {
								case 2: {
									ReceiveThread.this.cyclex = (int) message.obj;
									if (ReceiveThread.this.cyclex != 0) {
										if (MemoryMg.getInstance().GpsLockState) {
											ReceiveThread.infoList = GPSInfoDataBase.getInfos();
											ReceiveThread.this.UploadGPSInfo(GPSInfoDataBase.getInfos());
										}
										MyLog.i("GPSCycle", "gps cycle is run");
										ReceiveThread.this.handler.sendMessageDelayed(ReceiveThread.this.handler.obtainMessage(2, (Object) ReceiveThread.this.cyclex), (long) ReceiveThread.this.cyclex);
										return;
									}
									break;
								}
								case 3: {
									break Label_0175;
								}
								case 4: {
									break Label_0222;
								}
								default: {
									return;
								}
							}
						} catch (Exception ex) {
							Log.e("", "", (Throwable) ex);
							return;
						}
						MyLog.i("gpsReceiveThread", "stop message");
						return;
					}
					MyLog.i("send null packet", "gps send null packet");
					ReceiveThread.this.SendGPS(ReceiveThread.this.GetEmptyByte());
					ReceiveThread.this.handler.sendMessageDelayed(ReceiveThread.this.handler.obtainMessage(3), 45000L);
					return;
				}
				final int intValue = (int) message.obj;
				int n;
				if (intValue == 5) {
					n = 0;
				} else if (intValue == 15) {
					n = 1;
				} else if (intValue == 30) {
					n = 2;
				} else {
					n = 3;
				}
				final SharedPreferences.Editor edit = ReceiveThread.this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
				edit.putInt("locateUploadTime", n);
				edit.commit();
			}
		};
		this.myLooper = null;
		this.socket = socket;
		this.context = context;
		this.gpsPacket = gpsPacket;
	}

	private int ChangeTimeToLip(final int n) {
		if (n == 1) {
			return 0;
		}
		if (n == 2) {
			return 1;
		}
		if (n > 2 && n <= 5) {
			return 2;
		}
		if (n > 5 && n <= 10) {
			return 3;
		}
		if (n > 10 && n <= 15) {
			return 4;
		}
		if (n > 15 && n <= 30) {
			return 5;
		}
		if (n > 30 && n <= 40) {
			return 6;
		}
		if (n > 40 && n <= 80) {
			return 7;
		}
		return 4;
	}

	private int CheckGpsEnabled() {
		if (Settings.Secure.isLocationProviderEnabled(this.context.getContentResolver(), "gps")) {
			return 1;
		}
		return 0;
	}

	private byte[] GetEmptyByte() {
		final byte[] array = new byte[4];
		final byte[] bytes = "\r\n\r\n".getBytes();
		System.arraycopy(bytes, 0, array, 0, bytes.length);
		return array;
	}

	private void SetGPSReCycle(int n, final boolean b) {
		Log.i("secondTrace", "SetGPSReCycle start");
		Log.i("secondTrace", "cycle = " + n);
		int n2 = 0;
		if (n >= 0) {
			if (n <= 6) {
				if (n == 0) {
					n2 = 1;
				} else if (n == 1) {
					n2 = 2;
				} else if (n == 2) {
					n2 = 5;
				} else if (n == 3) {
					n2 = 10;
				} else if (n == 4) {
					n2 = 15;
				} else if (n == 5) {
					n2 = 30;
				} else if (n == 6) {
					n2 = 40;
				}
			} else if (n > 6 && n <= 29) {
				n2 = (n + 1) * 10;
			} else if (n >= 30 && n <= 59) {
				n2 = (int) (5.5 + 0.5 * (n - 30)) * 60;
			} else if (n >= 60 && n <= 127) {
				n2 = ((n - 60) * 1 + 21) * 60;
			} else {
				n2 = 60;
			}
			if (b) {
				this.SendGPS(GpsTools.ReplyUploadCycleByte(this.CheckGpsEnabled(), MemoryMg.getInstance().TerminalNum, 0, n));
			}
			Log.i("secondTrace", "second = " + n2);
			n = n2 * 1000;
			this.tipFlag = true;
			if (this.handler.hasMessages(2)) {
				this.handler.removeMessages(2);
			}
			this.handler.sendMessage(this.handler.obtainMessage(2, (Object) n));
			this.handler.sendMessage(this.handler.obtainMessage(4, (Object) (n / 1000)));
		} else {
			this.SendGPS(GpsTools.ReplyUploadCycleByte(this.CheckGpsEnabled(), MemoryMg.getInstance().TerminalNum, 1, 0));
		}
		Log.i("secondTrace", "SetGPSReCycle end");
	}

	private void UploadGPSInfo(final List<GpsInfo> list) {
		if (list != null) {
			Log.i("secondTrace", "info list size = " + list.size());
		} else {
			Log.i("secondTrace", "info list is null");
		}
		if (list != null && list.size() > 0) {
			GpsTools.UploadGPSByTerminal(129, list);
			return;
		}
		if (!this.tipFlag) {
			MyLog.i("GPS upload by seconds: fail" + this.cyclex, "null");
			GpsTools.UploadGPSByTerminal(21, 0.0, 0.0, 0.0f, 0.0f, 0, 0L, "");
			return;
		}
		this.tipFlag = false;
	}

	private void onLooperPrepared() {
		this.startRun();
	}

	private void startRun() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     2: ldc_w           "ReceiverThread#startRun enter"
		//     5: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
		//     8: pop
		//     9: sipush          1024
		//    12: newarray        B
		//    14: astore          5
		//    16: aload_0
		//    17: invokevirtual   com/zed3/location/ReceiveThread.GetReturnFlag:()Z
		//    20: ifne            24
		//    23: return
		//    24: new             Ljava/net/DatagramPacket;
		//    27: dup
		//    28: aload           5
		//    30: aload           5
		//    32: arraylength
		//    33: invokespecial   java/net/DatagramPacket.<init>:([BI)V
		//    36: astore          4
		//    38: aload_0
		//    39: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//    42: invokevirtual   com/zed3/location/MemoryMg.getSocket:()Ljava/net/DatagramSocket;
		//    45: putfield        com/zed3/location/ReceiveThread.socket:Ljava/net/DatagramSocket;
		//    48: aload_0
		//    49: getfield        com/zed3/location/ReceiveThread.socket:Ljava/net/DatagramSocket;
		//    52: astore          6
		//    54: aload           4
		//    56: astore_3
		//    57: aload           6
		//    59: ifnull          85
		//    62: aload_0
		//    63: getfield        com/zed3/location/ReceiveThread.socket:Ljava/net/DatagramSocket;
		//    66: aload_0
		//    67: getfield        com/zed3/location/ReceiveThread.TIMEOUT:I
		//    70: invokevirtual   java/net/DatagramSocket.setSoTimeout:(I)V
		//    73: aload_0
		//    74: getfield        com/zed3/location/ReceiveThread.socket:Ljava/net/DatagramSocket;
		//    77: aload           4
		//    79: invokevirtual   java/net/DatagramSocket.receive:(Ljava/net/DatagramPacket;)V
		//    82: aload           4
		//    84: astore_3
		//    85: aload_3
		//    86: ifnull          16
		//    89: aload_3
		//    90: invokevirtual   java/net/DatagramPacket.getLength:()I
		//    93: bipush          42
		//    95: iadd
		//    96: bipush          60
		//    98: if_icmple       407
		//   101: aload_0
		//   102: aload_3
		//   103: invokevirtual   java/net/DatagramPacket.getLength:()I
		//   106: bipush          42
		//   108: iadd
		//   109: putfield        com/zed3/location/ReceiveThread.flow:I
		//   112: aload           5
		//   114: invokestatic    com/zed3/location/GpsTools.BitToPDUExtendType:([B)I
		//   117: istore_1
		//   118: ldc_w           "gpsReceiveThread"
		//   121: new             Ljava/lang/StringBuilder;
		//   124: dup
		//   125: ldc_w           "msgtype:"
		//   128: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   131: iload_1
		//   132: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   135: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   138: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   141: iload_1
		//   142: iconst_5
		//   143: if_icmpne       425
		//   146: ldc_w           "testgps"
		//   149: ldc_w           "ReceiveThread#startRun extType=5"
		//   152: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   155: ldc             "ReceiveThread"
		//   157: new             Ljava/lang/StringBuilder;
		//   160: dup
		//   161: ldc_w           "TerminalNum:"
		//   164: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   167: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//   170: getfield        com/zed3/location/MemoryMg.TerminalNum:Ljava/lang/String;
		//   173: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   176: ldc_w           "  buffer:"
		//   179: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   182: aload           5
		//   184: invokestatic    com/zed3/location/GpsTools.BitToUploadCycleID:([B)Ljava/lang/String;
		//   187: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   190: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   193: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
		//   196: pop
		//   197: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//   200: getfield        com/zed3/location/MemoryMg.TerminalNum:Ljava/lang/String;
		//   203: aload           5
		//   205: invokestatic    com/zed3/location/GpsTools.BitToUploadCycleID:([B)Ljava/lang/String;
		//   208: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   211: ifeq            16
		//   214: aload           5
		//   216: iconst_1
		//   217: baload
		//   218: sipush          254
		//   221: iand
		//   222: iconst_1
		//   223: ishr
		//   224: istore_1
		//   225: ldc_w           "gps cycle up cycle:"
		//   228: new             Ljava/lang/StringBuilder;
		//   231: dup
		//   232: iload_1
		//   233: invokestatic    java/lang/String.valueOf:(I)Ljava/lang/String;
		//   236: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   239: ldc_w           "**"
		//   242: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   245: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   248: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   251: iload_1
		//   252: bipush          127
		//   254: if_icmpne       416
		//   257: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//   260: getfield        com/zed3/location/MemoryMg.GpsUploadTimeModel:I
		//   263: invokestatic    com/zed3/location/GpsTools.GetLocationTimeValByModel:(I)I
		//   266: istore_1
		//   267: iload_1
		//   268: ifle            922
		//   271: aload_0
		//   272: iload_1
		//   273: invokespecial   com/zed3/location/ReceiveThread.ChangeTimeToLip:(I)I
		//   276: istore_1
		//   277: aload_0
		//   278: iload_1
		//   279: iconst_1
		//   280: invokespecial   com/zed3/location/ReceiveThread.SetGPSReCycle:(IZ)V
		//   283: goto            16
		//   286: astore_3
		//   287: aload_3
		//   288: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   291: ldc_w           "xxxx"
		//   294: new             Ljava/lang/StringBuilder;
		//   297: dup
		//   298: ldc_w           "ReceiverThread#run exception = "
		//   301: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   304: aload_3
		//   305: invokevirtual   java/lang/Exception.getMessage:()Ljava/lang/String;
		//   308: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   311: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   314: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
		//   317: pop
		//   318: return
		//   319: astore_3
		//   320: aload_3
		//   321: ifnull          335
		//   324: aload_3
		//   325: instanceof      Ljava/net/SocketTimeoutException;
		//   328: ifne            335
		//   331: aload_3
		//   332: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   335: aconst_null
		//   336: astore_3
		//   337: aload_0
		//   338: getfield        com/zed3/location/ReceiveThread.gpsPacket:Lcom/zed3/location/GPSPacket;
		//   341: getfield        com/zed3/location/GPSPacket.loginFlag:Z
		//   344: ifne            85
		//   347: aload_0
		//   348: aload_0
		//   349: getfield        com/zed3/location/ReceiveThread.count:I
		//   352: iconst_1
		//   353: iadd
		//   354: putfield        com/zed3/location/ReceiveThread.count:I
		//   357: aload_0
		//   358: getfield        com/zed3/location/ReceiveThread.count:I
		//   361: bipush          20
		//   363: if_icmple       16
		//   366: invokestatic    com/zed3/location/GpsTools.SendLoginUdp:()V
		//   369: ldc_w           "xxxx"
		//   372: new             Ljava/lang/StringBuilder;
		//   375: dup
		//   376: ldc_w           "ReceiveThread#GPSPacket.loginFlag"
		//   379: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   382: aload_0
		//   383: getfield        com/zed3/location/ReceiveThread.gpsPacket:Lcom/zed3/location/GPSPacket;
		//   386: getfield        com/zed3/location/GPSPacket.loginFlag:Z
		//   389: invokevirtual   java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		//   392: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   395: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
		//   398: pop
		//   399: aload_0
		//   400: iconst_0
		//   401: putfield        com/zed3/location/ReceiveThread.count:I
		//   404: goto            16
		//   407: aload_0
		//   408: bipush          60
		//   410: putfield        com/zed3/location/ReceiveThread.flow:I
		//   413: goto            112
		//   416: aload_0
		//   417: iload_1
		//   418: iconst_1
		//   419: invokespecial   com/zed3/location/ReceiveThread.SetGPSReCycle:(IZ)V
		//   422: goto            16
		//   425: iload_1
		//   426: bipush          6
		//   428: if_icmpne       440
		//   431: aload_0
		//   432: aload           5
		//   434: invokevirtual   com/zed3/location/ReceiveThread.BitToSetOrUpdateOccurID:([B)V
		//   437: goto            16
		//   440: iload_1
		//   441: bipush          7
		//   443: if_icmpne       455
		//   446: aload_0
		//   447: aload           5
		//   449: invokevirtual   com/zed3/location/ReceiveThread.BitToDelOccurID:([B)V
		//   452: goto            16
		//   455: iload_1
		//   456: bipush          10
		//   458: if_icmpne       506
		//   461: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//   464: getfield        com/zed3/location/MemoryMg.TerminalNum:Ljava/lang/String;
		//   467: aload           5
		//   469: invokestatic    com/zed3/location/GpsTools.BitToOpenCloseGPSID:([B)Ljava/lang/String;
		//   472: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   475: ifeq            16
		//   478: aload           5
		//   480: iconst_1
		//   481: baload
		//   482: iconst_1
		//   483: if_icmpne       496
		//   486: aload_0
		//   487: getfield        com/zed3/location/ReceiveThread.context:Landroid/content/Context;
		//   490: invokestatic    com/zed3/location/GpsTools.OpenGPSByServer:(Landroid/content/Context;)V
		//   493: goto            16
		//   496: aload_0
		//   497: getfield        com/zed3/location/ReceiveThread.context:Landroid/content/Context;
		//   500: invokestatic    com/zed3/location/GpsTools.CloseGPSByServer:(Landroid/content/Context;)V
		//   503: goto            16
		//   506: iload_1
		//   507: iconst_4
		//   508: if_icmpne       773
		//   511: aload           5
		//   513: invokestatic    com/zed3/location/GpsTools.BitToSuccess:([B)I
		//   516: istore_1
		//   517: aload           5
		//   519: invokestatic    com/zed3/location/GpsTools.BitToExtralType:([B)I
		//   522: istore_2
		//   523: aload           5
		//   525: invokestatic    com/zed3/location/GpsTools.BitToE_id:([B)Ljava/lang/String;
		//   528: astore_3
		//   529: iload_2
		//   530: bipush          18
		//   532: if_icmpne       652
		//   535: aload_0
		//   536: aload           5
		//   538: iconst_4
		//   539: invokestatic    com/zed3/location/GpsTools.BitToUnixTime:([BI)J
		//   542: putfield        com/zed3/location/ReceiveThread.UnixTime:J
		//   545: aload_0
		//   546: invokestatic    com/zed3/location/GpsTools.getCurrentRealTime:()J
		//   549: putfield        com/zed3/location/ReceiveThread.RealTime:J
		//   552: aload_0
		//   553: invokestatic    com/zed3/location/GpsTools.getCurrentLocalTime1:()J
		//   556: putfield        com/zed3/location/ReceiveThread.LocalTime:J
		//   559: aload_0
		//   560: getfield        com/zed3/location/ReceiveThread.UnixTime:J
		//   563: aload_0
		//   564: getfield        com/zed3/location/ReceiveThread.LocalTime:J
		//   567: aload_0
		//   568: getfield        com/zed3/location/ReceiveThread.RealTime:J
		//   571: invokestatic    com/zed3/location/GpsTools.saveTime:(JJJ)V
		//   574: getstatic       java/lang/System.out:Ljava/io/PrintStream;
		//   577: new             Ljava/lang/StringBuilder;
		//   580: dup
		//   581: ldc_w           "gps--ReceiverThread--unixtime--="
		//   584: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   587: aload_0
		//   588: getfield        com/zed3/location/ReceiveThread.UnixTime:J
		//   591: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   594: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   597: invokevirtual   java/io/PrintStream.println:(Ljava/lang/String;)V
		//   600: getstatic       java/lang/System.out:Ljava/io/PrintStream;
		//   603: new             Ljava/lang/StringBuilder;
		//   606: dup
		//   607: ldc_w           "gps--ReceiverThread--LocalTime--="
		//   610: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   613: aload_0
		//   614: getfield        com/zed3/location/ReceiveThread.LocalTime:J
		//   617: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   620: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   623: invokevirtual   java/io/PrintStream.println:(Ljava/lang/String;)V
		//   626: getstatic       java/lang/System.out:Ljava/io/PrintStream;
		//   629: new             Ljava/lang/StringBuilder;
		//   632: dup
		//   633: ldc_w           "gps--ReceiverThread---RealTime ="
		//   636: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   639: aload_0
		//   640: getfield        com/zed3/location/ReceiveThread.RealTime:J
		//   643: invokevirtual   java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		//   646: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   649: invokevirtual   java/io/PrintStream.println:(Ljava/lang/String;)V
		//   652: ldc_w           "GpsRecv"
		//   655: new             Ljava/lang/StringBuilder;
		//   658: dup
		//   659: ldc_w           "Recv Server Respond:"
		//   662: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   665: iload_1
		//   666: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   669: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   672: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   675: iload_1
		//   676: ifne            739
		//   679: iload_2
		//   680: bipush          18
		//   682: if_icmpne       724
		//   685: aload_0
		//   686: getfield        com/zed3/location/ReceiveThread.regListener:Lcom/zed3/location/GpsListener;
		//   689: iconst_0
		//   690: invokeinterface com/zed3/location/GpsListener.LoginResult:(I)V
		//   695: invokestatic    com/zed3/location/MemoryMg.getInstance:()Lcom/zed3/location/MemoryMg;
		//   698: getfield        com/zed3/location/MemoryMg.GpsUploadTimeModel:I
		//   701: invokestatic    com/zed3/location/GpsTools.GetLocationTimeValByModel:(I)I
		//   704: istore_1
		//   705: iload_1
		//   706: ifle            927
		//   709: aload_0
		//   710: iload_1
		//   711: invokespecial   com/zed3/location/ReceiveThread.ChangeTimeToLip:(I)I
		//   714: istore_1
		//   715: aload_0
		//   716: iload_1
		//   717: iconst_0
		//   718: invokespecial   com/zed3/location/ReceiveThread.SetGPSReCycle:(IZ)V
		//   721: goto            16
		//   724: aload_0
		//   725: getfield        com/zed3/location/ReceiveThread.regListener:Lcom/zed3/location/GpsListener;
		//   728: iconst_0
		//   729: iload_2
		//   730: aload_3
		//   731: invokeinterface com/zed3/location/GpsListener.UploadResult:(IILjava/lang/String;)V
		//   736: goto            16
		//   739: iload_2
		//   740: bipush          18
		//   742: if_icmpne       758
		//   745: aload_0
		//   746: getfield        com/zed3/location/ReceiveThread.regListener:Lcom/zed3/location/GpsListener;
		//   749: iconst_1
		//   750: invokeinterface com/zed3/location/GpsListener.LoginResult:(I)V
		//   755: goto            16
		//   758: aload_0
		//   759: getfield        com/zed3/location/ReceiveThread.regListener:Lcom/zed3/location/GpsListener;
		//   762: iconst_1
		//   763: iload_2
		//   764: aload_3
		//   765: invokeinterface com/zed3/location/GpsListener.UploadResult:(IILjava/lang/String;)V
		//   770: goto            16
		//   773: iload_1
		//   774: bipush          14
		//   776: if_icmpne       16
		//   779: aload           5
		//   781: invokestatic    com/zed3/location/GpsTools.BitToSuccess:([B)I
		//   784: ifne            16
		//   787: aload           5
		//   789: aload           5
		//   791: invokestatic    com/zed3/location/GpsTools.BitToE_idCount:([B)I
		//   794: invokestatic    com/zed3/location/GpsTools.BitToE_ids:([BI)[Ljava/lang/String;
		//   797: astore_3
		//   798: new             Ljava/lang/StringBuilder;
		//   801: dup
		//   802: new             Ljava/lang/StringBuilder;
		//   805: dup
		//   806: ldc_w           "receive eids ("
		//   809: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   812: aload_3
		//   813: arraylength
		//   814: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   817: ldc_w           ")"
		//   820: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   823: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   826: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   829: astore          4
		//   831: aload_3
		//   832: arraylength
		//   833: istore_2
		//   834: iconst_0
		//   835: istore_1
		//   836: iload_1
		//   837: iload_2
		//   838: if_icmplt       880
		//   841: ldc             "gps"
		//   843: new             Ljava/lang/StringBuilder;
		//   846: dup
		//   847: ldc             "gps"
		//   849: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   852: aload           4
		//   854: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   857: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   860: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   863: invokestatic    com/zed3/log/MyLog.d:(Ljava/lang/String;Ljava/lang/String;)V
		//   866: aload_0
		//   867: getfield        com/zed3/location/ReceiveThread.regListener:Lcom/zed3/location/GpsListener;
		//   870: iconst_0
		//   871: aload_3
		//   872: invokeinterface com/zed3/location/GpsListener.UploadResult:(I[Ljava/lang/String;)V
		//   877: goto            16
		//   880: aload_3
		//   881: iload_1
		//   882: aaload
		//   883: astore          6
		//   885: aload           4
		//   887: new             Ljava/lang/StringBuilder;
		//   890: dup
		//   891: ldc_w           "["
		//   894: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   897: aload           6
		//   899: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   902: ldc_w           "]"
		//   905: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   908: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   911: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   914: pop
		//   915: iload_1
		//   916: iconst_1
		//   917: iadd
		//   918: istore_1
		//   919: goto            836
		//   922: iconst_4
		//   923: istore_1
		//   924: goto            277
		//   927: iconst_4
		//   928: istore_1
		//   929: goto            715
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  9      16     286    319    Ljava/lang/Exception;
		//  16     23     286    319    Ljava/lang/Exception;
		//  24     54     286    319    Ljava/lang/Exception;
		//  62     82     319    407    Ljava/lang/Exception;
		//  89     112    286    319    Ljava/lang/Exception;
		//  112    141    286    319    Ljava/lang/Exception;
		//  146    214    286    319    Ljava/lang/Exception;
		//  225    251    286    319    Ljava/lang/Exception;
		//  257    267    286    319    Ljava/lang/Exception;
		//  271    277    286    319    Ljava/lang/Exception;
		//  277    283    286    319    Ljava/lang/Exception;
		//  324    335    286    319    Ljava/lang/Exception;
		//  337    404    286    319    Ljava/lang/Exception;
		//  407    413    286    319    Ljava/lang/Exception;
		//  416    422    286    319    Ljava/lang/Exception;
		//  431    437    286    319    Ljava/lang/Exception;
		//  446    452    286    319    Ljava/lang/Exception;
		//  461    478    286    319    Ljava/lang/Exception;
		//  486    493    286    319    Ljava/lang/Exception;
		//  496    503    286    319    Ljava/lang/Exception;
		//  511    529    286    319    Ljava/lang/Exception;
		//  535    652    286    319    Ljava/lang/Exception;
		//  652    675    286    319    Ljava/lang/Exception;
		//  685    705    286    319    Ljava/lang/Exception;
		//  709    715    286    319    Ljava/lang/Exception;
		//  715    721    286    319    Ljava/lang/Exception;
		//  724    736    286    319    Ljava/lang/Exception;
		//  745    755    286    319    Ljava/lang/Exception;
		//  758    770    286    319    Ljava/lang/Exception;
		//  779    834    286    319    Ljava/lang/Exception;
		//  841    877    286    319    Ljava/lang/Exception;
		//  885    915    286    319    Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0085:
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

	public void BitToDelOccurID(final byte[] array) {
		final int n = (array[1] & 0x7) << 3 | (array[2] & 0xE0) >> 5;
		final int n2 = (n - 4) / 8;
		String s = "";
		int i;
		for (i = 0; i < n2; ++i) {
			s = String.valueOf(new StringBuilder(String.valueOf(s)).append((array[i + 2] & 0x1) << 3 | (array[i + 2 + 1] & 0xE0) >> 5).toString()) + ((array[i + 2 + 1] & 0x1E) >> 1);
		}
		if ((n - 4) % 8 != 0) {
			s = String.valueOf(s) + ((array[n2 + 2] & 0x1) << 3 | (array[n2 + 2 + 1] & 0xE0) >> 5);
			this.occurType = ((array[i + 2 + 1] & 0x1) << 7 | (array[i + 2 + 2] & 0xFE) >> 1);
			this.delType = (array[i + 2 + 1] & 0x2) >> 2;
		} else {
			this.occurType = ((array[i + 2 + 2] & 0x1F) << 3 | (array[i + 2 + 3] & 0xE0) >> 5);
			this.delType = (array[i + 2 + 2] & 0x20) >> 5;
		}
		if (MemoryMg.getInstance().TerminalNum.equals(s)) {
			this.SendGPS(GpsTools.ReplyServerDelOccur(MemoryMg.getInstance().TerminalNum, this.delType, this.occurType));
		}
		MyLog.i(">>>>>DelOccur", String.valueOf(this.occurType) + " " + this.delType + "  name:" + s);
	}

	public void BitToSetOrUpdateOccurID(final byte[] array) {
		final int n = (array[1] & 0x1) << 7 | (array[2] & 0xF8) >> 3;
		final int n2 = (n - 4) / 8;
		String s = "";
		int i;
		for (i = 0; i < n2; ++i) {
			s = String.valueOf(new StringBuilder(String.valueOf(s)).append((array[i + 3] & 0x78) >> 3).toString()) + ((array[i + 3] & 0x7) << 1 | (array[i + 3 + 1] & 0x80) >> 7);
		}
		if ((n - 4) % 8 != 0) {
			s = String.valueOf(s) + ((array[n2 + 3] & 0x78) >> 3);
			this.occurType = (array[i + 3 + 2] & 0xFF);
			this.occurCircle = (array[i + 3 + 3] & 0x80) >> 7;
		} else {
			this.occurType = ((array[i + 3 + 1] & 0xF) << 4 | (array[i + 3 + 2] & 0xF0) >> 4);
			this.occurCircle = (array[i + 3 + 2] & 0x8) >> 3;
		}
		if (MemoryMg.getInstance().TerminalNum.equals(s)) {
			if (this.occurType == 12) {
				MyLog.i("GPS ", "\u8bbe\u7f6e\u7535\u6c60\u7535\u91cf\u4f4e");
			} else if (this.occurType == 21) {
				MyLog.i("GPS ", "\u8bbe\u7f6e \u8131\u7f51");
			} else if (this.occurType == 255) {
				MyLog.i("GPS ", "\u8bbe\u7f6e \u5012\u5730\u544a\u8b66");
			} else if (this.occurType == 2) {
				MyLog.i("GPS ", "\u7d27\u6025\u547c\u53eb");
			}
			this.SendGPS(GpsTools.ReplyServerSetOrUpdateOccur(MemoryMg.getInstance().TerminalNum, this.occurCircle, this.occurType));
		}
		MyLog.i(">>>>>SetOrUpdateOccur", String.valueOf(this.occurType) + " " + this.occurCircle + "  name:" + s);
	}

	public void DestoryAll(final boolean b) {
		System.out.println("--------停止线程------" + b);
		this.StopRunning();
		// TODO
	}

	public void ExitSys(final boolean b) {
		if (this.handler.hasMessages(3)) {
			this.handler.removeMessages(3);
		}
		if (this.handler.hasMessages(2)) {
			this.handler.removeMessages(2);
		}
		this.DestoryAll(b);
	}

	public boolean GetReturnFlag() {
		return this.returnFlag;
	}

	public void SendGPS(final byte[] array) {
		try {
			final SendThread sendThread = new SendThread(MemoryMg.getInstance().getSocket(), InetAddress.getByName(GpsTools.ServerIP), GpsTools.Port);
			sendThread.SetContent(array);
			sendThread.start();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
	}

	public void StartHandler() {
		if (this.handler.hasMessages(3)) {
			this.handler.removeMessages(3);
		}
		this.handler.sendMessage(this.handler.obtainMessage(3));
		Log.i("secondTrace", "login success send empty pkg start");
		Log.i("secondTrace", "login success send empty pkg end");
	}

	public void StopRunning() {
		Label_0019:
		{
			if (this.myLooper == null) {
				break Label_0019;
			}
			try {
				this.myLooper.quit();
				this.myLooper = null;
				this.returnFlag = false;
				this.handler = null;
			} finally {
				this.myLooper = null;
			}
		}
	}

	@Override
	public void run() {
		Looper.prepare();
		this.myLooper = Looper.myLooper();
		this.onLooperPrepared();
		Looper.loop();
	}

	public void setGpsListener(final GpsListener regListener) {
		this.regListener = regListener;
	}
}
