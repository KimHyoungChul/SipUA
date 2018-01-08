package com.zed3.video;

import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhoneSupportTest {
	public static int[][] ColorFormatList;
	public static final int ColorFormat_I420 = 1;
	public static final int ColorFormat_NV21 = 0;
	private static List<Camera.Size> listSize;
	private static final String tag = "PhoneSupportTest";

	static {
		PhoneSupportTest.listSize = null;
		PhoneSupportTest.ColorFormatList = new int[][]{{0, 21}, {1, 19}};
	}

	private void closeCamera(final Camera camera) {
		if (camera != null) {
			camera.release();
		}
	}

	public static int getEncodeSupportColor() {
		// TODO
		MyLog.i("codecInfo", "find  return  value = ," + -1);
		return -1;
	}

	private String[] getPixList() {
		final String lowerCase = Build.MODEL.toLowerCase();
		if (lowerCase.contains("mi") || lowerCase.contains("g716-l070")) {
			return new String[]{"384*288", "480*320", "640*480", "720*480", "1280*720"};
		}
		return new String[]{"320*240", "352*288", "640*480", "720*480", "1280*720"};
	}

	private void getSupportCodec() {
		String string = "";
		final ArrayList<CodecInfo> list = new ArrayList<CodecInfo>(CodecInfo.getSupportedCodecs());
		String s = string;
		if (list != null) {
			s = string;
			if (list.size() > 0) {
				final Iterator<CodecInfo> iterator = list.iterator();
				while (iterator.hasNext()) {
					final String string2 = iterator.next().toString();
					Log.e("Ht", "\n" + string2);
					string = String.valueOf(string) + string2;
				}
				s = string;
			}
		}
		if (!TextUtils.isEmpty((CharSequence) s)) {
			DeviceVideoInfo.isCodecK3 = s.toLowerCase().contains("k3");
		}
	}

	private List<Camera.Size> getSupportPreViewSize(final Camera camera) {
		if (camera == null) {
			MyLog.e("PhoneSupportTest", "camera is not open!");
			throw new NullPointerException();
		}
		return (List<Camera.Size>) camera.getParameters().getSupportedPreviewSizes();
	}

	public String getSupportSizeList() {
//		String s;
//		if (PhoneSupportTest.listSize == null || PhoneSupportTest.listSize.size() < 1) {
//			s = "";
//		} else {
//			String string = "";
//			final String[] pixList = this.getPixList();
//			for (int length = pixList.length, i = 0; i < length; ++i) {
//				final String s2 = pixList[i];
//				for (final Camera.Size camera.Size:
//				PhoneSupportTest.listSize){
//					if (s2.equalsIgnoreCase(String.valueOf(camera.Size.width) + "*" + camera.Size.height)) {
//						string = String.valueOf(string) + s2 + ",";
//					}
//				}
//			}
//			s = string;
//			if (string.length() > 0) {
//				return string.substring(0, string.length() - 1);
//			}
//		}
//		return s;
		return "";
	}

	public boolean startTest() {
		Camera open;
		Camera.Parameters parameters;
		int width;
		int height;
		while (true) {
			final boolean b = true;
			List<Camera.Size> supportPreViewSize;
			try {
				open = Camera.open(0);
				parameters = open.getParameters();
				supportPreViewSize = this.getSupportPreViewSize(open);
				this.getSupportCodec();
				PhoneSupportTest.listSize = supportPreViewSize;
				MemoryMg.getInstance().SupportVideoSizeStr = this.getSupportSizeList();
				if (supportPreViewSize == null || supportPreViewSize.size() < 1) {
					return false;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			final Iterator<Camera.Size> iterator = supportPreViewSize.iterator();
			do {
				final boolean b2 = b;
				if (!iterator.hasNext()) {
					return b2;
				}
//				final Camera.Size camera.Size = iterator.next();
//				width = camera.Size.width;
//				height = camera.Size.height;
			} while (open == null);
			break;
		}
		try {
//			parameters.setPreviewSize(height, width);
			open.setParameters(parameters);
			return true;
		} catch (Exception ex2) {
			return false;
		} finally {
			this.closeCamera(open);
		}
	}
}
