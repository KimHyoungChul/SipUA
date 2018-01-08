package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;

public interface InviteTransactionServerListener extends TransactionServerListener {
	void onTransFailureAck(final InviteTransactionServer p0, final Message p1);
}
