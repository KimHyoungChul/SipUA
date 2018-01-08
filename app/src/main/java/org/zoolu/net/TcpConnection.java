package org.zoolu.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TcpConnection extends Thread {
	static final int BUFFER_SIZE = 65535;
	public static final int DEFAULT_SOCKET_TIMEOUT = 2000;
	long alive_time;
	Exception error;
	boolean is_running;
	InputStream istream;
	TcpConnectionListener listener;
	OutputStream ostream;
	TcpSocket socket;
	int socket_timeout;
	boolean stop;

	public TcpConnection(final TcpSocket tcpSocket, final long n, final TcpConnectionListener tcpConnectionListener) {
		this.init(tcpSocket, n, tcpConnectionListener);
		this.start();
	}

	public TcpConnection(final TcpSocket tcpSocket, final TcpConnectionListener tcpConnectionListener) {
		this.init(tcpSocket, 0L, tcpConnectionListener);
		this.start();
	}

	private void init(final TcpSocket socket, final long alive_time, final TcpConnectionListener listener) {
		this.listener = listener;
		this.socket = socket;
		this.socket_timeout = 2000;
		this.alive_time = alive_time;
		this.stop = false;
		this.is_running = true;
		this.istream = null;
		this.ostream = null;
		this.error = null;
		try {
			this.istream = new BufferedInputStream(socket.getInputStream());
			this.ostream = new BufferedOutputStream(socket.getOutputStream());
		} catch (Exception error) {
			this.error = error;
		}
	}

	public IpAddress getRemoteAddress() {
		return this.socket.getAddress();
	}

	public int getRemotePort() {
		return this.socket.getPort();
	}

	public TcpSocket getSocket() {
		return this.socket;
	}

	public void halt() {
		this.stop = true;
	}

	public boolean isRunning() {
		return this.is_running;
	}

	@Override
	public void run() {
		// TODO
	}

	public void send(final byte[] array) throws IOException {
		this.send(array, 0, array.length);
	}

	public void send(final byte[] array, final int n, final int n2) throws IOException {
		if (!this.stop && this.ostream != null) {
			this.ostream.write(array, n, n2);
			this.ostream.flush();
		}
	}

	@Override
	public String toString() {
		return "tcp:";
	}
}
