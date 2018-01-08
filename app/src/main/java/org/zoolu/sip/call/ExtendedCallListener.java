package org.zoolu.sip.call;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

public interface ExtendedCallListener extends CallListener {
	void onCallTransfer(final ExtendedCall p0, final NameAddress p1, final NameAddress p2, final Message p3);

	void onCallTransferAccepted(final ExtendedCall p0, final Message p1);

	void onCallTransferFailure(final ExtendedCall p0, final String p1, final Message p2);

	void onCallTransferRefused(final ExtendedCall p0, final String p1, final Message p2);

	void onCallTransferSuccess(final ExtendedCall p0, final Message p1);
}
