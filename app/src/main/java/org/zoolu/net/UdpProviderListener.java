package org.zoolu.net;

public interface UdpProviderListener {
	void onReceivedPacket(final UdpProvider p0, final UdpPacket p1);

	void onServiceTerminated(final UdpProvider p0, final Exception p1);
}
