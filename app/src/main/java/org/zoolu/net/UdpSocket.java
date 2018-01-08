package org.zoolu.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpSocket {
	DatagramSocket socket;

	public UdpSocket() throws SocketException {
		this.socket = new DatagramSocket();
	}

	public UdpSocket(final int n) throws SocketException {
		this.socket = new DatagramSocket(n);
	}

	public UdpSocket(final int n, final IpAddress ipAddress) throws SocketException {
		this.socket = new DatagramSocket(n, ipAddress.getInetAddress());
	}

	UdpSocket(final DatagramSocket socket) {
		this.socket = socket;
	}

	public void close() {
		this.socket.close();
	}

	public IpAddress getLocalAddress() {
		return new IpAddress(this.socket.getInetAddress());
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	public int getSoTimeout() throws SocketException {
		return this.socket.getSoTimeout();
	}

	public void receive(final UdpPacket udpPacket) throws IOException {
		final DatagramPacket datagramPacket = udpPacket.getDatagramPacket();
		this.socket.receive(datagramPacket);
		udpPacket.setDatagramPacket(datagramPacket);
	}

	public void send(final UdpPacket udpPacket) throws IOException {
		this.socket.send(udpPacket.getDatagramPacket());
	}

	public void setSoTimeout(final int soTimeout) throws SocketException {
		this.socket.setSoTimeout(soTimeout);
	}

	@Override
	public String toString() {
		return this.socket.toString();
	}
}
