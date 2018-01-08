package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

public interface NotifierDialogListener {
	void onDlgNotificationFailure(final NotifierDialog p0, final int p1, final String p2, final Message p3);

	void onDlgNotificationSuccess(final NotifierDialog p0, final int p1, final String p2, final Message p3);

	void onDlgNotifyTimeout(final NotifierDialog p0);

	void onDlgSubscribe(final NotifierDialog p0, final NameAddress p1, final NameAddress p2, final String p3, final String p4, final Message p5);
}
