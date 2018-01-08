package org.zoolu.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpSocket {
	static boolean lock;
	Socket socket;

	TcpSocket() {
		this.socket = null;
	}

	TcpSocket(final Socket socket) {
		this.socket = socket;
	}

	public TcpSocket(final IpAddress ipAddress, int n) throws IOException {
		this.socket = new Socket();
		if (TcpSocket.lock) {
			throw new IOException();
		}
		TcpSocket.lock = true;
		try {
			final Socket socket = this.socket;
			final InetSocketAddress inetSocketAddress = new InetSocketAddress(ipAddress.toString(), n);
			if (Thread.currentThread().getName().equals("main")) {
				n = 1000;
			} else {
				n = 10000;
			}
			socket.connect(inetSocketAddress, n);
			TcpSocket.lock = false;
		} catch (IOException ex) {
			TcpSocket.lock = false;
			throw ex;
		}
	}

	public void close() throws IOException {
		this.socket.close();
	}

	public IpAddress getAddress() {
		return new IpAddress(this.socket.getInetAddress());
	}

	public InputStream getInputStream() throws IOException {
		return this.socket.getInputStream();
	}

	public IpAddress getLocalAddress() {
		return new IpAddress(this.socket.getLocalAddress());
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	public OutputStream getOutputStream() throws IOException {
		return this.socket.getOutputStream();
	}

	public int getPort() {
		return this.socket.getPort();
	}

	public int getSoTimeout() throws SocketException {
		return this.socket.getSoTimeout();
	}

	public void setSoTimeout(final int soTimeout) throws SocketException {
		this.socket.setSoTimeout(soTimeout);
	}

	@Override
	public String toString() {
		return this.socket.toString();
	}
}
