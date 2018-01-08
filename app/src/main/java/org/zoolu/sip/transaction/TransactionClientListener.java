package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;

public interface TransactionClientListener {
	void onTransFailureResponse(final TransactionClient p0, final Message p1);

	void onTransProvisionalResponse(final TransactionClient p0, final Message p1);

	void onTransSuccessResponse(final TransactionClient p0, final Message p1);

	void onTransTimeout(final TransactionClient p0);
}
