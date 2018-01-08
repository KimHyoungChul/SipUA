package com.zed3.video;

public final class VideoParameterTester {
	private static VideoParameterTester sDefault;

	static {
		VideoParameterTester.sDefault = new VideoParameterTester();
	}

	public static VideoParameterTester getDefault() {
		return VideoParameterTester.sDefault;
	}

	public static void main(final String[] array) {
		System.out.println("00001 : " + Integer.parseInt("00001", 2));
		System.out.println("00101 : " + Integer.parseInt("00101", 2));
		System.out.println("00010 : " + Integer.parseInt("00010", 2));
		System.out.println("00110 : " + Integer.parseInt("00110", 2));
		testHandeParameter();
	}

	private static void testBuildParamster() {
		System.out.println("================");
		System.out.println("00001(\u503c\u4e3a1)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u4e0d\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u524d\u7f6e\u6444\u50cf\u5934");
		final VideoParamter setUseFrontCamera = VideoParamter.obtain().setVideoMonitor(true).setDeviceUserConfrim(false).setUseFrontCamera(true);
		System.out.println("paramINT = " + setUseFrontCamera.build());
		System.out.println("paramBinary = " + setUseFrontCamera.buildBinary());
		System.out.println("================");
		System.out.println("00101(\u503c\u4e3a5)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u524d\u7f6e\u6444\u50cf\u5934");
		final VideoParamter setUseFrontCamera2 = VideoParamter.obtain().setVideoMonitor(true).setDeviceUserConfrim(true).setUseFrontCamera(true);
		System.out.println("paramINT = " + setUseFrontCamera2.build());
		System.out.println("paramBinary = " + setUseFrontCamera2.buildBinary());
		System.out.println("================");
		System.out.println("00010(\u503c\u4e3a2)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u4e0d\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u540e\u7f6e\u6444\u50cf\u5934");
		final VideoParamter setUsePostPosCamera = VideoParamter.obtain().setVideoMonitor(true).setDeviceUserConfrim(false).setUseFrontCamera(false).setUsePostPosCamera(true);
		System.out.println("paramINT = " + setUsePostPosCamera.build());
		System.out.println("paramBinary = " + setUsePostPosCamera.buildBinary());
		System.out.println("================");
		System.out.println("00110(\u503c\u4e3a6)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u540e\u7f6e\u6444\u50cf\u5934");
		final VideoParamter setUsePostPosCamera2 = VideoParamter.obtain().setVideoMonitor(true).setDeviceUserConfrim(true).setUseFrontCamera(false).setUsePostPosCamera(true);
		System.out.println("paramINT = " + setUsePostPosCamera2.build());
		System.out.println("paramBinary = " + setUsePostPosCamera2.buildBinary());
	}

	private static void testHandeParameter() {
		System.out.println("================");
		System.out.println("00001(\u503c\u4e3a1)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u4e0d\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u524d\u7f6e\u6444\u50cf\u5934");
		System.out.println(VideoParamter.handle(1).getParameterString());
		System.out.println("================");
		System.out.println("00101(\u503c\u4e3a5)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u524d\u7f6e\u6444\u50cf\u5934");
		System.out.println(VideoParamter.handle(5).getParameterString());
		System.out.println("================");
		System.out.println("00010(\u503c\u4e3a2)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u4e0d\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u540e\u7f6e\u6444\u50cf\u5934");
		System.out.println(VideoParamter.handle(2).getParameterString());
		System.out.println("================");
		System.out.println("00110(\u503c\u4e3a6)\uff0c\u89c6\u9891\u76d1\u63a7\uff0c\u9700\u8981\u7528\u6237\u786e\u8ba4\uff0c\u540e\u7f6e\u6444\u50cf\u5934");
		System.out.println(VideoParamter.handle(6).getParameterString());
	}
}
