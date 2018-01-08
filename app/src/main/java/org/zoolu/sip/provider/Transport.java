package org.zoolu.sip.provider;

import org.zoolu.net.IpAddress;
import org.zoolu.sip.message.Message;

import java.io.IOException;

interface Transport {
	String getProtocol();

	void halt();

	void sendMessage(final Message p0, final IpAddress p1, final int p2) throws IOException;

	String toString();
}
