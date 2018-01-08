package com.zed3.video;

public final class VideoParamter {
	public static final int MAX_PARAMETER_LENGHT = 5;
	private boolean isDeviceUserConfrim;
	private boolean isUseFrontCamera;
	private boolean isUsePostPosCamera;
	private boolean isVideoCall;
	private boolean isVideoDispatch;
	private boolean isVideoMonitor;
	private boolean isVideoUpload;

	private VideoParamter() {
		this.isUseFrontCamera = false;
		this.isUsePostPosCamera = false;
		this.isDeviceUserConfrim = false;
		this.isVideoCall = false;
		this.isVideoUpload = false;
		this.isVideoMonitor = false;
		this.isVideoDispatch = false;
	}

	public static int build(final VideoParamter videoParamter) {
		return toInt(buildBinaryParameter(videoParamter));
	}

	public static String buildBinaryParameter(final VideoParamter videoParamter) {
		final StringBuffer sb = new StringBuffer();
		if (videoParamter.isVideoCall()) {
			sb.append(1);
			sb.append(0);
		} else if (videoParamter.isVideoUpload()) {
			sb.append(0);
			sb.append(1);
		} else if (videoParamter.isVideoMonitor()) {
			sb.append(0);
			sb.append(0);
		}
		if (videoParamter.isVideoMonitor()) {
			sb.append(1);
		} else {
			sb.append(toInt(videoParamter.isDeviceUserConfrim()));
		}
		sb.append(toInt(videoParamter.isUsePostPosCamera()));
		sb.append(toInt(videoParamter.isUseFrontCamera()));
		return sb.toString();
	}

	public static VideoParamter handle(int char1) {
		final boolean b = true;
		final VideoParamter obtain = obtain();
		final String binary = toBinary(char1);
		char1 = binary.charAt(0);
		final char char2 = binary.charAt(1);
		final char char3 = binary.charAt(2);
		final char char4 = binary.charAt(3);
		final char char5 = binary.charAt(4);
		if (char1 == 48 && char2 == '0') {
			obtain.setVideoMonitor(true);
		} else if (char1 == 48 && char2 == '1') {
			obtain.setVideoUpload(true);
		} else if (char1 == 49 && char2 == '0') {
			obtain.setVideoDispatch(true);
		}
		obtain.setDeviceUserConfrim(char3 == '1');
		obtain.setUsePostPosCamera(char4 == '1');
		obtain.setUseFrontCamera(char5 == '1' && b);
		return obtain;
	}

	public static VideoParamter obtain() {
		return new VideoParamter();
	}

	public static String toBinary(int i) {
		final String binaryString = Integer.toBinaryString(i);
		final int length = binaryString.length();
		final StringBuffer sb = new StringBuffer();
		for (i = 0; i < 5 - length; ++i) {
			sb.append(0);
		}
		sb.append(binaryString);
		return sb.toString();
	}

	private static int toInt(final String s) {
		try {
			return Integer.parseInt(s, 2);
		} catch (Exception ex) {
			return -1;
		}
	}

	private static int toInt(final boolean b) {
		if (b) {
			return 1;
		}
		return 0;
	}

	public int build() {
		return build(this);
	}

	public String buildBinary() {
		return buildBinaryParameter(this);
	}

	public String getParameterString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("isUseFrontCamera:").append(this.isUseFrontCamera).append(" , ").append("isUsePostPosCamera:").append(this.isUsePostPosCamera).append(" , ").append("isDeviceUserConfrim:").append(this.isDeviceUserConfrim).append(" , ").append("isVideoCall:").append(this.isVideoCall).append(" , ").append("isVideoUpload:").append(this.isVideoUpload).append(" , ").append("isVideoMonitor:").append(this.isVideoMonitor);
		return sb.toString();
	}

	public boolean isDeviceUserConfrim() {
		return this.isDeviceUserConfrim;
	}

	public boolean isUseFrontCamera() {
		return this.isUseFrontCamera;
	}

	public boolean isUsePostPosCamera() {
		return this.isUsePostPosCamera;
	}

	public boolean isVideoCall() {
		return this.isVideoCall;
	}

	public boolean isVideoDispatch() {
		return this.isVideoDispatch;
	}

	public boolean isVideoMonitor() {
		return this.isVideoMonitor;
	}

	public boolean isVideoUpload() {
		return this.isVideoUpload;
	}

	public VideoParamter setDeviceUserConfrim(final boolean isDeviceUserConfrim) {
		this.isDeviceUserConfrim = isDeviceUserConfrim;
		return this;
	}

	public VideoParamter setUseFrontCamera(final boolean isUseFrontCamera) {
		this.isUseFrontCamera = isUseFrontCamera;
		return this;
	}

	public VideoParamter setUsePostPosCamera(final boolean isUsePostPosCamera) {
		this.isUsePostPosCamera = isUsePostPosCamera;
		return this;
	}

	public VideoParamter setVideoCall(final boolean isVideoCall) {
		this.isVideoCall = isVideoCall;
		return this;
	}

	public void setVideoDispatch(final boolean isVideoDispatch) {
		this.isVideoDispatch = isVideoDispatch;
	}

	public VideoParamter setVideoMonitor(final boolean isVideoMonitor) {
		this.isVideoMonitor = isVideoMonitor;
		return this;
	}

	public VideoParamter setVideoUpload(final boolean isVideoUpload) {
		this.isVideoUpload = isVideoUpload;
		return this;
	}

	@Override
	public String toString() {
		return this.getParameterString();
	}
}
