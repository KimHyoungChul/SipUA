package org.zoolu.sip.call;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

import java.util.Vector;

public interface CallListener {
	void onCallAccepted(final Call p0, final String p1, final Message p2);

	void onCallCanceling(final Call p0, final Message p1);

	void onCallClosed(final Call p0, final Message p1);

	void onCallClosing(final Call p0, final Message p1);

	void onCallConfirmed(final Call p0, final String p1, final Message p2);

	void onCallIncoming(final Call p0, final NameAddress p1, final NameAddress p2, final String p3, final Message p4);

	void onCallModifying(final Call p0, final String p1, final Message p2);

	void onCallReInviteAccepted(final Call p0, final String p1, final Message p2);

	void onCallReInviteRefused(final Call p0, final String p1, final Message p2);

	void onCallReInviteTimeout(final Call p0);

	void onCallRedirection(final Call p0, final String p1, final Vector<String> p2, final Message p3);

	void onCallRefused(final Call p0, final String p1, final Message p2);

	void onCallRinging(final Call p0, final Message p1);

	void onCallTimeout(final Call p0);
}
