package org.zoolu.sip.message;

public abstract class BaseSipMethods {
	public static final String ACK = "ACK";
	public static final String BYE = "BYE";
	public static final String CANCEL = "CANCEL";
	public static final String INFO = "INFO";
	public static final String INVITE = "INVITE";
	public static final String OPTION = "OPTION";
	public static final String OPTIONS = "OPTIONS";
	public static final String REGISTER = "REGISTER";
	public static final String UPDATE = "UPDATE";
	public static final String[] dialog_methods;
	public static final String[] methods;

	static {
		methods = new String[]{"INVITE", "ACK", "CANCEL", "BYE", "INFO", "OPTION", "REGISTER", "UPDATE"};
		dialog_methods = new String[]{"INVITE"};
	}

	public static boolean isAck(final String s) {
		return same(s, "ACK");
	}

	public static boolean isBye(final String s) {
		return same(s, "BYE");
	}

	public static boolean isCancel(final String s) {
		return same(s, "CANCEL");
	}

	public static boolean isInfo(final String s) {
		return same(s, "INFO");
	}

	public static boolean isInvite(final String s) {
		return same(s, "INVITE");
	}

	public static boolean isOption(final String s) {
		return same(s, "OPTION");
	}

	public static boolean isOptions(final String s) {
		return same(s, "OPTIONS");
	}

	public static boolean isRegister(final String s) {
		return same(s, "REGISTER");
	}

	public static boolean isUpdate(final String s) {
		return same(s, "UPDATE");
	}

	protected static boolean same(final String s, final String s2) {
		return s.equalsIgnoreCase(s2);
	}
}
