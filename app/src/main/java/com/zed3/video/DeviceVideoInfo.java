package com.zed3.video;

public class DeviceVideoInfo {
	public static final String ACTION_RESTART_CAMERA = "com.zed3.siupa.ui.restartcamera";
	public static final String AUDIO_AEC_SWITCH = "AEC_SWITCH";
	public static final String AUDIO_AGC_SWITCH = "AGC_SWITCH";
	public static String CAMERA_FRONT_RESOLUTION;
	public static String CAMERA_POSTPOSTION_RESOLUTIN;
	public static String CAMERA_TYPE_FRONT_OR_POSTPOS;
	public static final boolean DEFAULT_AUDIO_AEC_SWITCH = true;
	public static final boolean DEFAULT_AUDIO_AGC_SWITCH = false;
	public static final int DEFAULT_PACKET_LOST_LEVEL = 1;
	public static final String DEFAULT_SCREEN_TYPE = "hor";
	public static final boolean DEFAULT_VIDEO_COLOR_CORRECT = false;
	public static final boolean DEFAULT_VIDEO_SUPPORT_FULLSCREEN = false;
	public static final boolean DEFAULT_VIDEO_SUPPORT_LAND = false;
	public static final boolean DEFAULT_VIDEO_SUPPORT_ROTATE = false;
	public static int MaxIFrameLostLimited = 0;
	public static int MaxPFrameLostLimited = 0;
	public static int MaxVideoJitterbufferDelay = 0;
	public static int MidVideoJitterbufferDelay = 0;
	public static int MinVideoJitterbufferDelay = 0;
	public static String PACKET_LOST_LEVEL;
	public static String SCREEN_TYPE;
	public static String VIDEO_COLOR_CORRECT;
	public static final String VIDEO_SUPPORT_FULLSCREEN = "full_screen";
	public static final String VIDEO_SUPPORT_LAND = "support_land";
	public static final String VIDEO_SUPPORT_ROTATE = "rotate";
	public static int allow_audio_MaxDelay;
	public static boolean color_correct;
	public static int curAngle;
	public static boolean isCodecK3;
	public static boolean isConsole;
	public static boolean isHorizontal;
	public static int lostLevel;
	public static boolean onlyCameraRotate;
	public static String screen_type;
	public static int supportColor;
	public static boolean supportFullScreen;
	public static boolean supportRotate;

	static {
		DeviceVideoInfo.curAngle = 0;
		DeviceVideoInfo.supportColor = -1;
		DeviceVideoInfo.supportRotate = false;
		DeviceVideoInfo.supportFullScreen = false;
		DeviceVideoInfo.color_correct = false;
		DeviceVideoInfo.isHorizontal = false;
		DeviceVideoInfo.VIDEO_COLOR_CORRECT = "color_correct";
		DeviceVideoInfo.SCREEN_TYPE = "screen_type";
		DeviceVideoInfo.screen_type = "hor";
		DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS = "usevideokey";
		DeviceVideoInfo.CAMERA_FRONT_RESOLUTION = "videoresolutionkey";
		DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN = "videoresolutionkey0";
		DeviceVideoInfo.isConsole = true;
		DeviceVideoInfo.onlyCameraRotate = true;
		DeviceVideoInfo.MaxIFrameLostLimited = 0;
		DeviceVideoInfo.MaxPFrameLostLimited = 0;
		DeviceVideoInfo.PACKET_LOST_LEVEL = "lost_level";
		DeviceVideoInfo.lostLevel = 1;
		DeviceVideoInfo.allow_audio_MaxDelay = 200;
		DeviceVideoInfo.MaxVideoJitterbufferDelay = 2000;
		DeviceVideoInfo.MinVideoJitterbufferDelay = 500;
		DeviceVideoInfo.MidVideoJitterbufferDelay = 1000;
		DeviceVideoInfo.isCodecK3 = false;
	}
}
