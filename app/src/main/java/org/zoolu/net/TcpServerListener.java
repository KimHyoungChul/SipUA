package org.zoolu.net;

public interface TcpServerListener {
	void onIncomingConnection(final TcpServer p0, final TcpSocket p1);

	void onServerTerminated(final TcpServer p0, final Exception p1);
}
