package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

public interface SubscriberDialogListener {
	void onDlgNotify(final SubscriberDialog p0, final NameAddress p1, final NameAddress p2, final NameAddress p3, final String p4, final String p5, final String p6, final Message p7);

	void onDlgSubscribeTimeout(final SubscriberDialog p0);

	void onDlgSubscriptionFailure(final SubscriberDialog p0, final int p1, final String p2, final Message p3);

	void onDlgSubscriptionSuccess(final SubscriberDialog p0, final int p1, final String p2, final Message p3);

	void onDlgSubscriptionTerminated(final SubscriberDialog p0);
}
