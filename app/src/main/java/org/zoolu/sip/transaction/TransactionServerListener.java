package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;

public interface TransactionServerListener {
	void onTransRequest(final TransactionServer p0, final Message p1);
}
