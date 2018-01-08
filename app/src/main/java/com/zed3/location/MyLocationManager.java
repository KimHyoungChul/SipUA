package com.zed3.location;

import android.database.sqlite.SQLiteDatabase;

import com.zed3.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyLocationManager {
	private static final String TAG = "testgps";
	public static GPSInfoDataBase gpsDB;
	private static MyLocationManager sDefault;
	SQLiteDatabase db;
	private List<String> mDeleteLocationKeys;
	private Map<String, MyLocation> mMyLocationMap;

	static {
		MyLocationManager.sDefault = new MyLocationManager();
		MyLocationManager.gpsDB = GPSInfoDataBase.getInstance();
	}

	public MyLocationManager() {
		this.mMyLocationMap = new HashMap<String, MyLocation>();
		this.mDeleteLocationKeys = new ArrayList<String>();
	}

	private void deleteLocation(final MyLocation p0) {
		// TODO
	}

	public static MyLocationManager getDefault() {
		return MyLocationManager.sDefault;
	}

	public void checkLocations() {
		// TODO
	}

	public boolean onPrepareToSend(final GpsInfo gpsInfo) {
		// TODO
		return false;
	}

	public void onSendSuccess(final String s) {
		synchronized (this) {
			LogUtil.makeLog("testgps", "LocationManager#onSendSuccess GpsInfo EID is " + s);
			final MyLocation myLocation = this.mMyLocationMap.get(s);
			if (myLocation != null) {
				myLocation.mSendSuccess = true;
				LogUtil.makeLog("testgps", "LocationManager#onSendSuccess myLocation is " + myLocation.toString());
			}
		}
	}

	public void onSended(final GpsInfo gpsInfo) {
		// TODO
	}

	public class MyLocation {
		GpsInfo mGpsInfo;
		int mSendCount;
		Boolean mSendSuccess;
		Boolean mSended;

		public MyLocation(final GpsInfo mGpsInfo) {
			this.mSendCount = 0;
			this.mSended = false;
			this.mSendSuccess = false;
			this.mGpsInfo = mGpsInfo;
		}

		public int getSendCount() {
			return this.mSendCount;
		}

		public void setSendCount(final int mSendCount) {
			this.mSendCount = mSendCount;
		}

		@Override
		public String toString() {
			final StringBuilder append = new StringBuilder("MyLocation [mSendCount=").append(this.mSendCount).append(", mSended=").append(this.mSended).append(", mSendSuccess=").append(this.mSendSuccess).append(", mGpsInfo=");
			String string;
			if (this.mGpsInfo != null) {
				string = this.mGpsInfo.toString();
			} else {
				string = "null";
			}
			return append.append(string).append("]").toString();
		}
	}
}
