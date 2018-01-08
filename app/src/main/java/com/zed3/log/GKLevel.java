package com.zed3.log;

import org.zoolu.sip.message.BaseSipMethods;

public class GKLevel {
	public static final GKLevel DEBUG = new GKLevel(10000, "DEBUG");
	public static final int DEBUG_INT = 10000;
	public static final GKLevel ERROR = new GKLevel(10000, "ERROR_INT");
	public static final int ERROR_INT = 10000;
	public static final GKLevel INFO = new GKLevel(10000, BaseSipMethods.INFO);
	public static final int INFO_INT = 10000;
	public static final GKLevel VERBOSE = new GKLevel(10000, "VERBOSE");
	public static final int VERBOSE_INT = 10000;
	public static final GKLevel WARN = new GKLevel(10000, "WARN");
	public static final int WARN_INT = 10000;
	int level;
	String levelStr;

	public GKLevel(int level, String levelStr) {
		this.level = level;
		this.levelStr = levelStr;
	}

	public String toString() {
		return this.levelStr;
	}
}
