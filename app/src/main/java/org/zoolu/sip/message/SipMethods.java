package org.zoolu.sip.message;

public class SipMethods extends BaseSipMethods {
	public static final String MESSAGE = "MESSAGE";
	public static final String NOTIFY = "NOTIFY";
	public static final String OPTIONS = "OPTIONS";
	public static final String PUBLISH = "PUBLISH";
	public static final String REFER = "REFER";
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String[] dialog_methods;
	public static final String[] methods;

	static {
		methods = new String[]{"INVITE", "ACK", "CANCEL", "BYE", "INFO", "OPTION", "REGISTER", "UPDATE", "SUBSCRIBE", "NOTIFY", "MESSAGE", "REFER", "PUBLISH"};
		dialog_methods = new String[]{"INVITE", "SUBSCRIBE"};
	}

	public static boolean isMessage(final String s) {
		return BaseSipMethods.same(s, "MESSAGE");
	}

	public static boolean isNotify(final String s) {
		return BaseSipMethods.same(s, "NOTIFY");
	}

	public static boolean isPublish(final String s) {
		return BaseSipMethods.same(s, "PUBLISH");
	}

	public static boolean isRefer(final String s) {
		return BaseSipMethods.same(s, "REFER");
	}

	public static boolean isSubscribe(final String s) {
		return BaseSipMethods.same(s, "SUBSCRIBE");
	}
}
