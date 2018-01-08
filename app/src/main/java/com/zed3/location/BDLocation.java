package com.zed3.location;

import android.content.Context;

import com.zed3.log.MyLog;

public class BDLocation {
	// TODO
	private final String TAG = "BDLocation";
	Context context;
	private boolean mIsStarted = false;

	public BDLocation(Context context) {
		this.context = context;
		GpsTools.Previous_gps_x = 0.0d;
		GpsTools.Previous_gps_y = 0.0d;
		GpsTools.Previous_UnixTime = 0;
		GpsTools.D_UnixTime = 0;
		MyLog.i("BDLocation", "BDLocation init");
	}

	public void StartBDGPS() {
		MyLog.d("testgps", "BDLocation#StartBDGPS exit");
		MyLog.i("BDLocation", "StartBDGPS");
	}

	public void StopBDGPS() {
		MyLog.d("testgps", "BDLocation#StopBDGPS exit");
		MyLog.i("BDLocation", "StopBDGPS");
	}

	public void restart() {
		StopBDGPS();
		StartBDGPS();
	}
}
