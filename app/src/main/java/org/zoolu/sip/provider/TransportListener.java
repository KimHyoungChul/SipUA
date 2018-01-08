package org.zoolu.sip.provider;

import org.zoolu.sip.message.Message;

interface TransportListener {
	void onReceivedMessage(final Transport p0, final Message p1);

	void onTransportTerminated(final Transport p0, final Exception p1);
}
