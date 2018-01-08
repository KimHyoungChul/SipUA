package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

public interface ExtendedInviteDialogListener extends InviteDialogListener {
	void onDlgAltRequest(final InviteDialog p0, final String p1, final String p2, final Message p3);

	void onDlgAltResponse(final InviteDialog p0, final String p1, final int p2, final String p3, final String p4, final Message p5);

	void onDlgNotify(final InviteDialog p0, final String p1, final String p2, final Message p3);

	void onDlgRefer(final InviteDialog p0, final NameAddress p1, final NameAddress p2, final Message p3);

	void onDlgReferResponse(final InviteDialog p0, final int p1, final String p2, final Message p3);
}
