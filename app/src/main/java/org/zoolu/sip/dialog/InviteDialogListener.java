package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.message.Message;

public interface InviteDialogListener {
	void onDlgAck(final InviteDialog p0, final String p1, final Message p2);

	void onDlgBye(final InviteDialog p0, final Message p1);

	void onDlgByeFailureResponse(final InviteDialog p0, final int p1, final String p2, final Message p3);

	void onDlgByeSuccessResponse(final InviteDialog p0, final int p1, final String p2, final Message p3);

	void onDlgCall(final InviteDialog p0);

	void onDlgCancel(final InviteDialog p0, final Message p1);

	void onDlgClose(final InviteDialog p0);

	void onDlgInvite(final InviteDialog p0, final NameAddress p1, final NameAddress p2, final String p3, final Message p4);

	void onDlgInviteFailureResponse(final InviteDialog p0, final int p1, final String p2, final Message p3);

	void onDlgInviteProvisionalResponse(final InviteDialog p0, final int p1, final String p2, final String p3, final Message p4);

	void onDlgInviteRedirectResponse(final InviteDialog p0, final int p1, final String p2, final MultipleHeader p3, final Message p4);

	void onDlgInviteSuccessResponse(final InviteDialog p0, final int p1, final String p2, final String p3, final Message p4);

	void onDlgReInvite(final InviteDialog p0, final String p1, final Message p2);

	void onDlgReInviteFailureResponse(final InviteDialog p0, final int p1, final String p2, final Message p3);

	void onDlgReInviteProvisionalResponse(final InviteDialog p0, final int p1, final String p2, final String p3, final Message p4);

	void onDlgReInviteSuccessResponse(final InviteDialog p0, final int p1, final String p2, final String p3, final Message p4);

	void onDlgReInviteTimeout(final InviteDialog p0);

	void onDlgTimeout(final InviteDialog p0);

	void onDlgUpdateSuccessResponse();
}
