package com.zed3.location;

import java.net.DatagramSocket;

public class MemoryMg {
	public static final String TABLE_NAME = "gps_info";
	public static final String TABLE_NAME_COPY = "gps_info_copy";
	public static float Voice;
	private static MemoryMg instance;
	public String CallNum;
	public boolean GPSSatelliteFailureTip;
	public int GpsLocationModel;
	public int GpsLocationModel_EN;
	public boolean GpsLockState;
	public int GpsSetTimeModel;
	public int GpsUploadTimeModel;
	public String GvsTransSize;
	public String IPAddress;
	public int IPPort;
	public boolean IsChangeListener;
	public String IsLock;
	public String LastSeq;
	public String Password;
	public int PhoneType;
	public String SupportVideoSizeStr;
	public String TerminalNum;
	public String User_3GDBLocalTime;
	public double User_3GDBLocalTotal;
	public double User_3GDBLocalTotalPTT;
	public double User_3GDBLocalTotalVideo;
	public double User_3GFlowOut;
	public String User_3GLocalTime;
	public double User_3GLocalTotal;
	public double User_3GLocalTotalPTT;
	public double User_3GLocalTotalVideo;
	public double User_3GRelTotal;
	public double User_3GRelTotalPTT;
	public double User_3GRelTotalVideo;
	public double User_3GTotal;
	public double User_3GTotalPTT;
	public double User_3GTotalVideo;
	public int cycle;
	public boolean isAudioVAD;
	public boolean isGpsLocation;
	public boolean isMicWakeUp;
	public boolean isProgressBarTip;
	public boolean isReceiverOnly;
	public boolean isSendOnly;
	private DatagramSocket socket;

	static {
		MemoryMg.Voice = 0.0f;
	}

	public MemoryMg() {
		this.TerminalNum = "";
		this.Password = "";
		this.IsLock = "";
		this.cycle = 0;
		this.IPAddress = "";
		this.IPPort = 5070;
		this.CallNum = "";
		this.LastSeq = "";
		this.IsChangeListener = true;
		this.isGpsLocation = false;
		this.GpsLockState = false;
		this.GpsLocationModel = 3;
		this.GpsLocationModel_EN = 3;
		this.GpsSetTimeModel = 1;
		this.GpsUploadTimeModel = 1;
		this.GPSSatelliteFailureTip = false;
		this.GvsTransSize = "";
		this.SupportVideoSizeStr = "";
		this.isSendOnly = false;
		this.isReceiverOnly = false;
		this.isAudioVAD = false;
		this.isMicWakeUp = false;
		this.PhoneType = 1;
		this.User_3GTotal = 0.0;
		this.User_3GTotalPTT = 0.0;
		this.User_3GTotalVideo = 0.0;
		this.User_3GLocalTotal = 0.0;
		this.User_3GLocalTotalPTT = 0.0;
		this.User_3GLocalTotalVideo = 0.0;
		this.User_3GLocalTime = "";
		this.User_3GDBLocalTotal = 0.0;
		this.User_3GDBLocalTotalPTT = 0.0;
		this.User_3GDBLocalTotalVideo = 0.0;
		this.User_3GDBLocalTime = "";
		this.User_3GRelTotal = 0.0;
		this.User_3GRelTotalPTT = 0.0;
		this.User_3GRelTotalVideo = 0.0;
		this.User_3GFlowOut = 0.0;
		this.isProgressBarTip = false;
	}

	public static MemoryMg getInstance() {
		if (MemoryMg.instance == null) {
			MemoryMg.instance = new MemoryMg();
		}
		return MemoryMg.instance;
	}

	public DatagramSocket getSocket() {
		// TODO
		return null;
	}
}
