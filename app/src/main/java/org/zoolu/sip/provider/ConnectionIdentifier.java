package org.zoolu.sip.provider;

import org.zoolu.net.IpAddress;

public class ConnectionIdentifier extends Identifier {
	public ConnectionIdentifier(final String s) {
		super(s);
	}

	public ConnectionIdentifier(final String s, final IpAddress ipAddress, final int n) {
		super(getId(s, ipAddress, n));
	}

	public ConnectionIdentifier(final ConnectedTransport connectedTransport) {
		super(getId(connectedTransport.getProtocol(), connectedTransport.getRemoteAddress(), connectedTransport.getRemotePort()));
	}

	public ConnectionIdentifier(final ConnectionIdentifier connectionIdentifier) {
		super(connectionIdentifier);
	}

	private static String getId(final String s, final IpAddress ipAddress, final int n) {
		return String.valueOf(s) + ":" + ipAddress + ":" + n;
	}
}
