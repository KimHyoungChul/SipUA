package org.zoolu.sip.message;

public abstract class BaseSipResponses {
	private static boolean is_init;
	protected static String[] reasons;

	static {
		BaseSipResponses.is_init = false;
	}

	protected static void init() {
		if (BaseSipResponses.is_init) {
			return;
		}
		BaseSipResponses.reasons = new String[700];
		for (int i = 0; i < 700; ++i) {
			BaseSipResponses.reasons[i] = null;
		}
		BaseSipResponses.reasons[0] = "Internal error";
		BaseSipResponses.reasons[100] = "Trying";
		BaseSipResponses.reasons[180] = "Ringing";
		BaseSipResponses.reasons[181] = "Call Is Being Forwarded";
		BaseSipResponses.reasons[182] = "Queued";
		BaseSipResponses.reasons[183] = "Session Progress";
		BaseSipResponses.reasons[200] = "OK";
		BaseSipResponses.reasons[300] = "Multiple Choices";
		BaseSipResponses.reasons[301] = "Moved Permanently";
		BaseSipResponses.reasons[302] = "Moved Temporarily";
		BaseSipResponses.reasons[305] = "Use Proxy";
		BaseSipResponses.reasons[380] = "Alternative Service";
		BaseSipResponses.reasons[400] = "Bad Request";
		BaseSipResponses.reasons[401] = "Unauthorized";
		BaseSipResponses.reasons[402] = "Payment Required";
		BaseSipResponses.reasons[403] = "Forbidden";
		BaseSipResponses.reasons[404] = "Not Found";
		BaseSipResponses.reasons[405] = "Method Not Allowed";
		BaseSipResponses.reasons[406] = "Not Acceptable";
		BaseSipResponses.reasons[407] = "Proxy Authentication Required";
		BaseSipResponses.reasons[408] = "Request Timeout";
		BaseSipResponses.reasons[410] = "Gone";
		BaseSipResponses.reasons[413] = "Request Entity Too Large";
		BaseSipResponses.reasons[414] = "Request-URI Too Large";
		BaseSipResponses.reasons[415] = "Unsupported Media Type";
		BaseSipResponses.reasons[416] = "Unsupported URI Scheme";
		BaseSipResponses.reasons[420] = "Bad Extension";
		BaseSipResponses.reasons[421] = "Extension Required";
		BaseSipResponses.reasons[423] = "Interval Too Brief";
		BaseSipResponses.reasons[480] = "Temporarily not available";
		BaseSipResponses.reasons[481] = "Call Leg/Transaction Does Not Exist";
		BaseSipResponses.reasons[482] = "Loop Detected";
		BaseSipResponses.reasons[483] = "Too Many Hops";
		BaseSipResponses.reasons[484] = "Address Incomplete";
		BaseSipResponses.reasons[485] = "Ambiguous";
		BaseSipResponses.reasons[486] = "Busy Here";
		BaseSipResponses.reasons[487] = "Request Terminated";
		BaseSipResponses.reasons[488] = "Not Acceptable Here";
		BaseSipResponses.reasons[491] = "Request Pending";
		BaseSipResponses.reasons[493] = "Undecipherable";
		BaseSipResponses.reasons[500] = "Internal Server Error";
		BaseSipResponses.reasons[501] = "Not Implemented";
		BaseSipResponses.reasons[502] = "Bad Gateway";
		BaseSipResponses.reasons[503] = "Service Unavailable";
		BaseSipResponses.reasons[504] = "Server Time-out";
		BaseSipResponses.reasons[505] = "SIP Version not supported";
		BaseSipResponses.reasons[513] = "Message Too Large";
		BaseSipResponses.reasons[600] = "Busy Everywhere";
		BaseSipResponses.reasons[603] = "Decline";
		BaseSipResponses.reasons[604] = "Does not exist anywhere";
		BaseSipResponses.reasons[606] = "Not Acceptable";
		BaseSipResponses.is_init = true;
	}

	public static String reasonOf(final int n) {
		if (!BaseSipResponses.is_init) {
			init();
		}
		if (BaseSipResponses.reasons[n] != null) {
			return BaseSipResponses.reasons[n];
		}
		return BaseSipResponses.reasons[n / 100 * 100];
	}
}
