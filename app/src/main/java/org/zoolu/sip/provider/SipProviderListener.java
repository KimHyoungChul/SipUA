package org.zoolu.sip.provider;

import org.zoolu.sip.message.Message;

public interface SipProviderListener {
	void onReceivedMessage(final SipProvider p0, final Message p1);
}
