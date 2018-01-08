package org.zoolu.sip.provider;

import org.zoolu.net.IpAddress;
import org.zoolu.net.TcpConnection;
import org.zoolu.net.TcpConnectionListener;
import org.zoolu.net.TcpSocket;
import org.zoolu.sip.message.Message;

import java.io.IOException;

class TcpTransport implements ConnectedTransport, TcpConnectionListener {
	public static final String PROTO_TCP = "tcp";
	ConnectionIdentifier connection_id;
	long last_time;
	TransportListener listener;
	TcpConnection tcp_conn;
	String text;

	public TcpTransport(final IpAddress ipAddress, final int n, final TransportListener listener) throws IOException {
		this.listener = listener;
		this.tcp_conn = new TcpConnection(new TcpSocket(ipAddress, n), this);
		this.connection_id = new ConnectionIdentifier(this);
		this.last_time = System.currentTimeMillis();
		this.text = "";
	}

	public TcpTransport(final TcpSocket tcpSocket, final TransportListener listener) {
		this.listener = listener;
		this.tcp_conn = new TcpConnection(tcpSocket, this);
		this.connection_id = null;
		this.last_time = System.currentTimeMillis();
		this.text = "";
	}

	@Override
	public long getLastTimeMillis() {
		return this.last_time;
	}

	@Override
	public String getProtocol() {
		return "tcp";
	}

	@Override
	public IpAddress getRemoteAddress() {
		if (this.tcp_conn != null) {
			return this.tcp_conn.getRemoteAddress();
		}
		return null;
	}

	@Override
	public int getRemotePort() {
		if (this.tcp_conn != null) {
			return this.tcp_conn.getRemotePort();
		}
		return 0;
	}

	@Override
	public void halt() {
		if (this.tcp_conn != null) {
			this.tcp_conn.halt();
		}
	}

	@Override
	public void onConnectionTerminated(final TcpConnection tcpConnection, final Exception ex) {
		if (this.listener != null) {
			this.listener.onTransportTerminated(this, ex);
		}
		final TcpSocket socket = tcpConnection.getSocket();
		// TODO
	}

	@Override
	public void onReceivedData(final TcpConnection tcpConnection, final byte[] array, final int n) {
		this.last_time = System.currentTimeMillis();
		this.text = String.valueOf(this.text) + new String(array, 0, n);
		SipParser sipParser = new SipParser(this.text);
		for (Message message = sipParser.getSipMessage(); message != null; message = sipParser.getSipMessage()) {
			message.setRemoteAddress(tcpConnection.getRemoteAddress().toString());
			message.setRemotePort(tcpConnection.getRemotePort());
			message.setTransport("tcp");
			message.setConnectionId(this.connection_id);
			if (this.listener != null) {
				this.listener.onReceivedMessage(this, message);
			}
			this.text = sipParser.getRemainingString();
			sipParser = new SipParser(this.text);
		}
	}

	@Override
	public void sendMessage(final Message message) throws IOException {
		if (this.tcp_conn != null) {
			this.last_time = System.currentTimeMillis();
			this.tcp_conn.send(message.toString().getBytes());
		}
	}

	@Override
	public void sendMessage(final Message message, final IpAddress ipAddress, final int n) throws IOException {
		this.sendMessage(message);
	}

	@Override
	public String toString() {
		if (this.tcp_conn != null) {
			return this.tcp_conn.toString();
		}
		return null;
	}
}
