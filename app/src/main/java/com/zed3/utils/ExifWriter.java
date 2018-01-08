package com.zed3.utils;

import android.location.Location;
import android.media.ExifInterface;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public final class ExifWriter extends Handler implements Runnable {
	public static final String EMPTY_GPS_LOCAITON = "8080";
	private static final String LOG_TAG;
	public static final String TAG_EMPTY_GPS_LOCATION = "Flash";
	private HashMap<String, String> mAttributes;
	private String mFilePath;
	private OnExifWriteListener mListener;

	static {
		LOG_TAG = ExifWriter.class.getSimpleName();
	}

	private ExifWriter(final String mFilePath) {
		this.mAttributes = new HashMap<String, String>();
		this.mFilePath = mFilePath;
	}

	private ExifWriter(final String s, final OnExifWriteListener mListener) {
		this(s);
		this.mListener = mListener;
	}

	public static String convertGps(final double n) {
		final String[] split = Location.convert(Math.abs(n), 2).split(":");
		return String.valueOf(split[0]) + "," + split[1] + "," + split[2];
	}

	public static ExifWriter create(final String s, final OnExifWriteListener onExifWriteListener) {
		return new ExifWriter(s, onExifWriteListener);
	}

	private void onCompledtedNotifier() {
		this.post((Runnable) new Runnable() {
			@Override
			public void run() {
				if (ExifWriter.this.mListener != null) {
					ExifWriter.this.mListener.onCompleted();
				}
			}
		});
	}

	private void onExceptionNotifier(final Exception ex) {
		this.post((Runnable) new Runnable() {
			@Override
			public void run() {
				if (ExifWriter.this.mListener != null) {
					ExifWriter.this.mListener.onException();
				}
			}
		});
	}

	private void putAttibutesExifFromMap(final HashMap<String, String> hashMap, final ExifInterface exifInterface) {
		if (hashMap != null && hashMap.size() > 0 && exifInterface != null) {
			for (final Map.Entry<String, String> entry : hashMap.entrySet()) {
				final String s = entry.getKey();
				final String s2 = entry.getValue();
				Log.i(ExifWriter.LOG_TAG, "key = " + s);
				Log.i(ExifWriter.LOG_TAG, "value = " + s2);
				exifInterface.setAttribute(s, s2);
			}
		}
	}

	public static void startWriteExif(final String s, final OnExifWriteListener onExifWriteListener, final HashMap<String, String> attributes) {
		create(s, onExifWriteListener).setAttributes(attributes).startWrite();
	}

	public void run() {
		try {
			final ExifInterface exifInterface = new ExifInterface(this.mFilePath);
			Log.i(ExifWriter.LOG_TAG, "exifInterface = " + exifInterface);
			final String attribute = exifInterface.getAttribute("GPSLatitude");
			final String attribute2 = exifInterface.getAttribute("GPSLongitude");
			Log.i(ExifWriter.LOG_TAG, "gps Laitude = " + attribute);
			Log.i(ExifWriter.LOG_TAG, "gps Longitude = " + attribute2);
			this.putAttibutesExifFromMap(this.mAttributes, exifInterface);
			exifInterface.saveAttributes();
			this.onCompledtedNotifier();
		} catch (Exception ex) {
			ex.printStackTrace();
			this.onExceptionNotifier(ex);
		}
	}

	public ExifWriter setAttribute(final String s, final String s2) {
		this.mAttributes.put(s, s2);
		return this;
	}

	public ExifWriter setAttributes(final HashMap<String, String> mAttributes) {
		this.mAttributes = mAttributes;
		return this;
	}

	public void startWrite() {
		new Thread(this).start();
	}

	public abstract static class OnExifWriteListener {
		public void onCompleted() {
		}

		public void onException() {
		}

		public void onStart() {
		}
	}
}
