package com.zed3.log;

public class GKMethodLayout extends GKLayout {
	private static final String BOTTOM_BORDER = "╚════════════════════════════════════════════════════════════════════════════════════════";
	private static final char BOTTOM_LEFT_CORNER = '╚';
	private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
	private static final char HORIZONTAL_DOUBLE_LINE = '║';
	private static final String MIDDLE_BORDER = "╟────────────────────────────────────────────────────────────────────────────────────────";
	private static final char MIDDLE_CORNER = '╟';
	public static final int MIN_STACK_OFFSET = 3;
	private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
	private static final String TOP_BORDER = "╔════════════════════════════════════════════════════════════════════════════════════════";
	private static final char TOP_LEFT_CORNER = '╔';

	public String format(GKLoggingEvent event) {
		StringBuilder sbuf = new StringBuilder(128);
		sbuf.setLength(0);
		dateFormat(sbuf, event);
		return null;
	}

	private StringBuilder logHeader(StringBuilder sbuf) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String level = "";
		int stackOffset = getStackOffset(trace);
		int methodCount = 2;
		if (2 + stackOffset > trace.length) {
			methodCount = (trace.length - stackOffset) - 1;
		}
		for (int i = methodCount; i > 0; i--) {
			int stackIndex = i + stackOffset;
			if (stackIndex < trace.length) {
				sbuf.append("║ ").append(level).append(getSimpleClassName(trace[stackIndex].getClassName())).append(".").append(trace[stackIndex].getMethodName()).append(" ").append(" (").append(trace[stackIndex].getFileName()).append(":").append(trace[stackIndex].getLineNumber()).append(")");
				level = new StringBuilder(String.valueOf(level)).append("    ").toString();
			}
		}
		return sbuf;
	}

	private int getStackOffset(StackTraceElement[] trace) {
		for (int i = 3; i < trace.length; i++) {
			String name = trace[i].getClassName();
			if (!name.equals(GKMethodLayout.class.getName()) && !name.equals(GKLogger.class.getName())) {
				return i - 1;
			}
		}
		return -1;
	}

	private String getSimpleClassName(String name) {
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private void logTopBorder(StringBuilder sbuf) {
		sbuf.append(TOP_BORDER);
	}
}
