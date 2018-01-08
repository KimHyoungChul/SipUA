package com.zed3.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class GKLayout {
	public static final String LINE_SEP = System.getProperty("line.separator");
	public static final int LINE_SEP_LEN = LINE_SEP.length();
	protected DateFormat dateFormat;
	protected volatile boolean isResetFormat = false;
	protected final ThreadLocal<DateFormat> threadLocal = new ThreadLocal();

	public abstract String format(GKLoggingEvent gKLoggingEvent);

	public GKLayout() {
		setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
	}

	public String getContentType() {
		return "text/plain";
	}

	public String getHeader() {
		return null;
	}

	public String getFooter() {
		return null;
	}

	protected void dateFormat(StringBuilder buf, GKLoggingEvent event) {
		if (this.dateFormat != null) {
			DateFormat dateFormat = getDateFormatFromLocal();
			Date date = new Date();
			date.setTime(event.getTimeStamp());
			buf.append(dateFormat.format(date));
			buf.append(" - ");
		}
	}

	public DateFormat getDateFormat() {
		return this.dateFormat;
	}

	public synchronized void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		this.isResetFormat = true;
	}

	public DateFormat getDateFormatFromLocal() {
		DateFormat dFormat = (DateFormat) this.threadLocal.get();
		if (dFormat != null && !this.isResetFormat) {
			return dFormat;
		}
		this.threadLocal.set((DateFormat) this.dateFormat.clone());
		dFormat = (DateFormat) this.threadLocal.get();
		this.isResetFormat = false;
		return dFormat;
	}
}
