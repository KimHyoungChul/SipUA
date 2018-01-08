package com.zed3.sipua.baiduMap;

import com.baidu.platform.comapi.basestruct.GeoPoint;

public class GroupMember {
	private GeoPoint geo;
	private boolean isOnline;
	private double mLatitude;
	private double mLongitude;
	private String name;
	private String num;
	private MapPoint point;

	public GroupMember() {
		this.isOnline = false;
	}

	public GeoPoint getGeo() {
		return this.geo;
	}

	public double getLatitude() {
		return this.mLatitude;
	}

	public double getLongitude() {
		return this.mLongitude;
	}

	public String getName() {
		return this.name;
	}

	public String getNum() {
		return this.num;
	}

	public MapPoint getPoint() {
		return this.point;
	}

	public boolean isOnline() {
		return this.isOnline;
	}

	public void setGeo(final GeoPoint geo) {
		this.geo = geo;
	}

	public void setLatitude(final double mLatitude) {
		this.mLatitude = mLatitude;
	}

	public void setLongitude(final double mLongitude) {
		this.mLongitude = mLongitude;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNum(final String num) {
		this.num = num;
	}

	public void setOnline(final boolean isOnline) {
		this.isOnline = isOnline;
	}

	public void setPoint(final MapPoint point) {
		this.point = point;
	}

	@Override
	public String toString() {
		return "GroupMember [geo=" + this.geo + ", name=" + this.name + ", num=" + this.num + ", isOnline=" + this.isOnline + ", mLongitude=" + this.mLongitude + ", mLatitude=" + this.mLatitude + "]";
	}
}
