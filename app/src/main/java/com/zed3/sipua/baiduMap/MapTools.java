package com.zed3.sipua.baiduMap;

import com.baidu.platform.comapi.basestruct.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MapTools {
	private static double PI;
	private static double R;

	static {
		MapTools.PI = 3.141592653589793;
		MapTools.R = 6371.004;
	}

	public static double getDistance(final double n, final double n2, final double n3, final double n4) {
		return Math.hypot((n3 - n) * MapTools.PI * MapTools.R * Math.cos((n2 + n4) / 2.0 * MapTools.PI / 180.0) / 180.0, (n4 - n2) * MapTools.PI * MapTools.R / 180.0);
	}

	public static double getDistance(final GeoPoint geoPoint, final GeoPoint geoPoint2) {
		final double n = geoPoint.getLongitudeE6() / 1000000.0;
		final double n2 = geoPoint2.getLongitudeE6() / 1000000.0;
		final double n3 = geoPoint.getLatitudeE6() / 1000000.0;
		final double n4 = geoPoint2.getLatitudeE6() / 1000000.0;
		return Math.hypot((n2 - n) * MapTools.PI * MapTools.R * Math.cos((n3 + n4) / 2.0 * MapTools.PI / 180.0) / 180.0, (n4 - n3) * MapTools.PI * MapTools.R / 180.0);
	}

	public static double getDistance(final MapPoint mapPoint, final MapPoint mapPoint2) {
		final double longitude = mapPoint.getLongitude();
		final double longitude2 = mapPoint2.getLongitude();
		final double latitude = mapPoint.getLatitude();
		final double latitude2 = mapPoint2.getLatitude();
		return Math.hypot((longitude2 - longitude) * MapTools.PI * MapTools.R * Math.cos((latitude + latitude2) / 2.0 * MapTools.PI / 180.0) / 180.0, (latitude2 - latitude) * MapTools.PI * MapTools.R / 180.0);
	}

	public static List<GroupMember> getMemInMiles(final GeoPoint geoPoint, final int n, final List<GroupMember> list) {
		final ArrayList<GroupMember> list2 = new ArrayList<GroupMember>();
		if (list != null && list.size() > 0) {
			for (final GroupMember groupMember : list) {
				if (getDistance(geoPoint, groupMember.getGeo()) <= n) {
					list2.add(groupMember);
				}
			}
		}
		return list2;
	}

	public static List<GroupMember> getMemInMiles(final MapPoint mapPoint, final int n, final List<GroupMember> list) {
		final ArrayList<GroupMember> list2 = new ArrayList<GroupMember>();
		if (list != null && list.size() > 0) {
			for (final GroupMember groupMember : list) {
				if (getDistance(mapPoint, groupMember.getPoint()) <= n) {
					list2.add(groupMember);
				}
			}
		}
		return list2;
	}
}
