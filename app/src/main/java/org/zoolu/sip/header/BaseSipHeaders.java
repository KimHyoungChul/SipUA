package org.zoolu.sip.header;

public abstract class BaseSipHeaders {
	public static final String Accept = "Accept";
	public static final String Alert_Info = "Alert-Info";
	public static final String Allow = "Allow";
	public static final String Anta_Extension = "Anta-Extension";
	public static final String Authentication_Info = "Authentication-Info";
	public static final String Authorization = "Authorization";
	public static final String CSeq = "CSeq";
	public static final String Call_ID = "Call-ID";
	public static final String Call_ID_short = "i";
	public static final String Contact = "Contact";
	public static final String Contact_short = "m";
	public static final String Content_Length = "Content-Length";
	public static final String Content_Length_short = "l";
	public static final String Content_Type = "Content-Type";
	public static final String Content_Type_short = "c";
	public static final String Date = "Date";
	public static final String EnhanceSMS_Attribute = "EnhanceSMS-Attribute";
	public static final String EnhanceSMS_ID = "EnhanceSMS-ID";
	public static final String EnhanceSMS_Type = "EnhanceSMS-Type";
	public static final String Expires = "Expires";
	public static final String From = "From";
	public static final String From_short = "f";
	public static final String Max_Forwards = "Max-Forwards";
	public static final String Proxy_Authenticate = "Proxy-Authenticate";
	public static final String Proxy_Authorization = "Proxy-Authorization";
	public static final String Proxy_Require = "Proxy-Require";
	public static final String Ptt_Attribute = "Ptt-Attribute";
	public static final String Ptt_Extension = "Ptt-Extension";
	public static final String Ptt_Extension2 = "Ptt-Extension2";
	public static final String Ptt_Member = "Ptt-Member";
	public static final String Ptt_emergency_call = "ptt-emergency-call";
	public static final String Record_Route = "Record-Route";
	public static final String Refresher = "refresher";
	public static final String Require = "Require";
	public static final String Route = "Route";
	public static final String Server = "Server";
	public static final String SessionExpires = "Session-Expires";
	public static final String Subject = "Subject";
	public static final String Subject_short = "s";
	public static final String Supported = "Supported";
	public static final String Supported_short = "k";
	public static final String To = "To";
	public static final String To_short = "t";
	public static final String Unsupported = "Unsupported";
	public static final String User_Agent = "User-Agent";
	public static final String Via = "Via";
	public static final String Via_short = "v";
	public static final String WWW_Authenticate = "WWW-Authenticate";

	public static boolean isAccept(final String s) {
		return same(s, "Accept");
	}

	public static boolean isAlert_Info(final String s) {
		return same(s, "Alert-Info");
	}

	public static boolean isAllow(final String s) {
		return same(s, "Allow");
	}

	public static boolean isAuthentication_Info(final String s) {
		return same(s, "Authentication-Info");
	}

	public static boolean isAuthorization(final String s) {
		return same(s, "Authorization");
	}

	public static boolean isCSeq(final String s) {
		return same(s, "CSeq");
	}

	public static boolean isCallId(final String s) {
		return same(s, "Call-ID") || same(s, "i");
	}

	public static boolean isContact(final String s) {
		return same(s, "Contact") || same(s, "m");
	}

	public static boolean isContent_Length(final String s) {
		return same(s, "Content-Length") || same(s, "l");
	}

	public static boolean isContent_Type(final String s) {
		return same(s, "Content-Type") || same(s, "c");
	}

	public static boolean isDate(final String s) {
		return same(s, "Date");
	}

	public static boolean isExpires(final String s) {
		return same(s, "Expires");
	}

	public static boolean isFrom(final String s) {
		return same(s, "From") || same(s, "f");
	}

	public static boolean isMax_Forwards(final String s) {
		return same(s, "Max-Forwards");
	}

	public static boolean isProxy_Authenticate(final String s) {
		return same(s, "Proxy-Authenticate");
	}

	public static boolean isProxy_Authorization(final String s) {
		return same(s, "Proxy-Authorization");
	}

	public static boolean isProxy_Require(final String s) {
		return same(s, "Proxy-Require");
	}

	public static boolean isPtt_Extension(final String s) {
		return same(s, "Ptt-Extension");
	}

	public static boolean isRecord_Route(final String s) {
		return same(s, "Record-Route");
	}

	public static boolean isRequire(final String s) {
		return same(s, "Require");
	}

	public static boolean isRoute(final String s) {
		return same(s, "Route");
	}

	public static boolean isServer(final String s) {
		return same(s, "Server");
	}

	public static boolean isSubject(final String s) {
		return same(s, "Subject") || same(s, "s");
	}

	public static boolean isSupported(final String s) {
		return same(s, "Supported") || same(s, "k");
	}

	public static boolean isTo(final String s) {
		return same(s, "To") || same(s, "t");
	}

	public static boolean isUnsupported(final String s) {
		return same(s, "Unsupported");
	}

	public static boolean isUser_Agent(final String s) {
		return same(s, "User-Agent");
	}

	public static boolean isVia(final String s) {
		return same(s, "Via") || same(s, "v");
	}

	public static boolean isWWW_Authenticate(final String s) {
		return same(s, "WWW-Authenticate");
	}

	protected static boolean same(final String s, final String s2) {
		return s.equalsIgnoreCase(s2);
	}
}
