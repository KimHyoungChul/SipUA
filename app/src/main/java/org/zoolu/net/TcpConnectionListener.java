package org.zoolu.net;

public interface TcpConnectionListener {
	void onConnectionTerminated(final TcpConnection p0, final Exception p1);

	void onReceivedData(final TcpConnection p0, final byte[] p1, final int p2);
}
