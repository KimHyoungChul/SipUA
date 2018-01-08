package org.zoolu.sip.provider;

import org.zoolu.net.IpAddress;
import org.zoolu.sip.message.Message;

import java.io.IOException;

interface ConnectedTransport extends Transport {
	long getLastTimeMillis();

	IpAddress getRemoteAddress();

	int getRemotePort();

	void sendMessage(final Message p0) throws IOException;
}
