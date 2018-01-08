package org.zoolu.sip.message;

public class SipResponses extends BaseSipResponses {
	private static boolean is_init;

	static {
		SipResponses.is_init = false;
	}

	public static void init() {
		if (SipResponses.is_init) {
			return;
		}
		BaseSipResponses.init();
		SipResponses.reasons[202] = "Accepted";
		SipResponses.reasons[489] = "Bad Event";
		SipResponses.is_init = true;
	}

	public static String reasonOf(final int n) {
		if (!SipResponses.is_init) {
			init();
		}
		return BaseSipResponses.reasonOf(n);
	}
}
