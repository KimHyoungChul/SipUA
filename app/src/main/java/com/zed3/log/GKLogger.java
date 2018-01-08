package com.zed3.log;

import java.io.IOException;

public class GKLogger {
	private static final GKLogger instance = new GKLogger();
	private IAppender appender;

	public static GKLogger getInstance() {
		return instance;
	}

	public static synchronized void init() {
		synchronized (GKLogger.class) {
			GKFileUtils.delete0Files(GKFileUtils.makeFileDir());
			if (getInstance().appender == null) {
//					getInstance().appender = new GKRollingFileAppender();
			}
		}
	}

	public static synchronized void init(String tag) {
		synchronized (GKLogger.class) {
			try {
				GKFileUtils.delete0Files(GKFileUtils.makeFileDir());
				if (getInstance().appender == null) {
					getInstance().appender = new GKRollingFileAppender(tag);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void init(GKLayout layout) {
		synchronized (GKLogger.class) {
			try {
				GKFileUtils.delete0Files(GKFileUtils.makeFileDir());
				if (getInstance().appender == null) {
					getInstance().appender = new GKRollingFileAppender(layout);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void init(String tag, GKLayout layout) {
		synchronized (GKLogger.class) {
			try {
				GKFileUtils.delete0Files(GKFileUtils.makeFileDir());
				if (getInstance().appender == null) {
					getInstance().appender = new GKRollingFileAppender(tag, layout);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void debug(Object message) {
		getInstance().callAppenders(new GKLoggingEvent(GKLevel.DEBUG, message));
	}

	public static void debug(String tag, Object message) {
		getInstance().callAppenders(new GKLoggingEvent(tag, GKLevel.DEBUG, message));
	}

	public static void info(Object message) {
		getInstance().callAppenders(new GKLoggingEvent(GKLevel.INFO, message));
	}

	public static void info(String tag, Object message) {
		getInstance().callAppenders(new GKLoggingEvent(tag, GKLevel.INFO, message));
	}

	public static void warn(Object message) {
		getInstance().callAppenders(new GKLoggingEvent(GKLevel.WARN, message));
	}

	public static void warn(String tag, Object message) {
		getInstance().callAppenders(new GKLoggingEvent(tag, GKLevel.WARN, message));
	}

	public static void error(Object message) {
		getInstance().callAppenders(new GKLoggingEvent(GKLevel.ERROR, message));
	}

	public static void error(String tag, Object message) {
		getInstance().callAppenders(new GKLoggingEvent(tag, GKLevel.ERROR, message));
	}

	public static void verbose(Object message) {
		getInstance().callAppenders(new GKLoggingEvent(GKLevel.VERBOSE, message));
	}

	public static void verbose(String tag, Object message) {
		getInstance().callAppenders(new GKLoggingEvent(tag, GKLevel.VERBOSE, message));
	}

	private void callAppenders(GKLoggingEvent event) {
		if (this.appender == null) {
			init();
		}
		this.appender.append(event);
	}

	public static IAppender getAppender() {
		return getInstance().appender;
	}

	public static synchronized void setAppender(IAppender appender) {
		synchronized (GKLogger.class) {
			if (getInstance().appender != null) {
				getInstance().appender.setNull();
			}
			getInstance().appender = appender;
		}
	}

	public static GKLogger setLayout(GKLayout layout) {
		getInstance().appender.setLayout(layout);
		return getInstance();
	}

	public static GKLogger setTag(String tag) {
		getInstance().appender.setTag(tag);
		return getInstance();
	}

	public static String getTag() {
		return getInstance().appender.getTag();
	}
}
