package com.zed3.sipua.baiduMap;

public class MapPoint {
	private double latitude;
	private double longitude;

	public MapPoint(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "Point [latitude=" + this.latitude + ", longitude=" + this.longitude + "]";
	}
}
