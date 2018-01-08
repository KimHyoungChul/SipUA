package com.zed3.log;

public class GKSimpleLayout extends GKLayout {
	public String format(GKLoggingEvent event) {
		StringBuilder sbuf = new StringBuilder(128);
		sbuf.setLength(0);
		dateFormat(sbuf, event);
		String tag = event.getTag();
		if (!(tag == null || tag.equals(""))) {
			sbuf.append(tag);
			sbuf.append(" - ");
		}
		sbuf.append(event.getLevel().toString());
		sbuf.append(" - ");
		sbuf.append(event.getThreadName());
		sbuf.append(" - ");
		sbuf.append(event.getMessage());
		sbuf.append(LINE_SEP);
		return sbuf.toString();
	}
}
