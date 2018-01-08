package org.zoolu.sip.provider;

import org.zoolu.sip.message.Message;

public interface SipInterfaceListener {
	void onReceivedMessage(final SipInterface p0, final Message p1);
}
