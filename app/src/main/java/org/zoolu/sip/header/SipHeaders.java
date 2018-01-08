package org.zoolu.sip.header;

public class SipHeaders extends BaseSipHeaders {
	public static final String Accept_Contact = "Accept-Contact";
	public static final String Allow_Events = "Allow-Events";
	public static final String Event = "Event";
	public static final String Event_short = "o";
	public static final String OFFLINE_DATA_ATTRIBUTE = "OfflineData-Attribute";
	public static final String OFFLINE_DATA_CLIENT_CHECK_ID = "OfflineData-Client-CheckID";
	public static final String OFFLINE_DATA_CONNECTION = "OfflineData-Connection";
	public static final String OFFLINE_DATA_ID = "OfflineData-ID";
	public static final String OFFLINE_DATA_NUM_TYPE = "OfflineData-NumType";
	public static final String OFFLINE_DATA_REPLY = "OfflineData-Reply";
	public static final String OFFLINE_DATA_SIZE = "OfflineData-Size";
	public static final String OFFLINE_DATA_TYPE = "OfflineData-Type";
	public static final String Refer_To = "Refer-To";
	public static final String Referred_By = "Referred-By";
	public static final String Subscription_State = "Subscription-State";

	public static boolean isAcceptContact(final String s) {
		return BaseSipHeaders.same(s, "Accept-Contact");
	}

	public static boolean isAllowEvents(final String s) {
		return BaseSipHeaders.same(s, "Allow-Events");
	}

	public static boolean isEvent(final String s) {
		return BaseSipHeaders.same(s, "Event") || BaseSipHeaders.same(s, "o");
	}

	public static boolean isReferTo(final String s) {
		return BaseSipHeaders.same(s, "Refer-To");
	}

	public static boolean isReferredBy(final String s) {
		return BaseSipHeaders.same(s, "Referred-By");
	}

	public static boolean isSubscriptionState(final String s) {
		return BaseSipHeaders.same(s, "Subscription-State");
	}
}
