package com.zed3.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;

public class GpsManage implements Runnable {
	private static final String TAG = "GpsManage";
	private static GpsManage instance;
	Context context;
	private int count;
	private Location location;
	private final LocationListener locationListener;
	private double lstLat;
	private double lstLng;
	private LocationManager mLocationManager;
	private Looper myLooper;
	private String provider;
	private int upTime;
	private int upTimeCycle;
	private int upTimeOut;

	static {
		GpsManage.instance = null;
	}

	public GpsManage(final Context context) {
		this.location = null;
		this.provider = "";
		this.count = 0;
		this.upTime = 0;
		this.upTimeCycle = 0;
		this.upTimeOut = 0;
		this.lstLat = 0.0;
		this.lstLng = 0.0;
		this.locationListener = (LocationListener) new LocationListener() {
			public void onLocationChanged(final Location location) {
				MyLog.i("GpsManage", "onLocationChanged");
				if (location != null) {
//					GpsManage.access .0 (GpsManage.this, 0);
					MyLog.i("GpsManage", "onLocationChanged Latitude:" + location.getLatitude() * 1000000.0 + " Longitude:" + location.getLongitude() * 1000000.0);
					GpsTools.onLocationChanged(location);
					return;
				}
				MyLog.e("GpsManage", "onLocationChanged location is null");
			}

			public void onProviderDisabled(final String s) {
				GpsManage.this.ShowInfo(null);
				MyLog.i("GpsManage", "onProviderDisabled");
			}

			public void onProviderEnabled(final String s) {
				MyLog.i("GpsManage", "onProviderEnabled");
			}

			public void onStatusChanged(final String s, final int n, final Bundle bundle) {
				MyLog.i("GpsManage", "onStatusChanged");
			}
		};
		this.context = context;
//		this.count = 0;
//		this.upTime = 0;
	}

	private GpsInfo ShowInfo(final GpsInfo gpsInfo) {
		return gpsInfo;
	}

//	static /* synthetic */ void access.0(
//	final GpsManage gpsManage, final int count)
//
//	{
//		gpsManage.count = count;
//	}

	public static GpsManage getInstance(final Context context) {
		if (GpsManage.instance == null) {
			MyLog.e("GpsManage", "gps manager init thread name = " + Thread.currentThread().getName());
			GpsManage.instance = new GpsManage(context);
		}
		return GpsManage.instance;
	}

	private GpsInfo getLastPosition() {
		final MyHandlerThread getmHandlerThread = SipUAApp.getInstance().getmHandlerThread();
		GpsInfo gpsInfo = new GpsInfo();
		if (getmHandlerThread == null) {
			return gpsInfo;
		}
		while (true) {
			while (true) {
				try {
//					this.location = this.mLocationManager.getLastKnownLocation(this.provider);
					if (this.location != null) {
						synchronized (this) {
							gpsInfo.gps_x = this.location.getLongitude();
							gpsInfo.gps_y = this.location.getLatitude();
							gpsInfo.gps_speed = this.location.getSpeed();
							gpsInfo.gps_height = (float) this.location.getAltitude();
							gpsInfo.gps_direction = 0;
							gpsInfo.UnixTime = GpsTools.getUnixTime(System.currentTimeMillis() / 1000L);
							gpsInfo.E_id = GpsTools.getE_id();
							getmHandlerThread.sendMessage(Message.obtain((Handler) getmHandlerThread.mInnerHandler, 1, (Object) gpsInfo));
							// monitorexit(this)
							MyLog.i("GpsManage", "gpsState is true");
							return gpsInfo;
						}
					}
				} catch (Exception ex) {
					MyLog.e("GpsManage", "info exception is null " + ex.toString());
					gpsInfo = null;
					return gpsInfo;
				}
				MyLog.e("GpsManage", "info is null");
				gpsInfo = null;
				continue;
			}
		}
	}

	private void startLocation() {
		MyLog.i("GpsManage", "start location");
		this.mLocationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		final Criteria criteria = new Criteria();
		criteria.setAccuracy(2);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(3);
		if (this.mLocationManager.isProviderEnabled("gps") || this.mLocationManager.isProviderEnabled("network")) {
			this.provider = this.mLocationManager.getBestProvider(criteria, true);
			if (this.upTime == 0) {
				this.upTime = 15;
				this.upTimeCycle = 13;
				this.upTimeOut = 14;
			} else {
				if (this.upTime == 1 || this.upTime == 2) {
					this.upTimeCycle = 1;
					this.upTimeOut = 1;
//					this.mLocationManager.requestLocationUpdates(this.provider, 1000L, 1.0f, this.locationListener);
					return;
				}
				this.upTimeCycle = this.upTime - 2;
				this.upTimeOut = this.upTime - 1;
			}
//			this.mLocationManager.requestLocationUpdates(this.provider, (long) (GpsTools.GetLocationTimeValByModel(MemoryMg.getInstance().GpsSetTimeModel) * 1000), 1.0f, this.locationListener);
			MyLog.i("GpsManage", "GpsManage 1");
			return;
		}
		this.ShowInfo(null);
		MyLog.i("GpsManage", "GpsManage 2");
	}

	public void CloseGPS() {
		if (GpsManage.instance == null) {
			return;
		}
		if (this.myLooper != null) {
			this.myLooper.quit();
			this.myLooper = null;
		}
		this.count = 0;
		while (true) {
			try {
				if (this.locationListener != null) {
					this.mLocationManager.removeUpdates(this.locationListener);
				}
				MyLog.i("GpsManage", "CloseGPS");
			} catch (Exception ex) {
				Log.e("", "", (Throwable) ex);
				continue;
			}
			break;
		}
	}

	public GpsInfo GetValueGpsStr() {
		return this.ShowInfo(this.getLastPosition());
	}

	@Override
	public void run() {
		MyLog.i("GpsManage", "gps manager thread start");
		Looper.prepare();
		this.myLooper = Looper.myLooper();
		this.startLocation();
		Looper.loop();
		MyLog.i("GpsManage", "gps manager thread end");
	}

	public void startGps() {
		new Thread(GpsManage.instance).start();
	}
}
