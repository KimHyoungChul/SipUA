package org.zoolu.sip.provider;

import org.zoolu.sip.message.Message;

public interface SipProviderExceptionListener {
	void onMessageException(final Message p0, final Exception p1);
}
