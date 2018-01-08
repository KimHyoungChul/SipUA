package com.zed3.net;

import android.os.Build;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RtpSocket {
	DatagramPacket datagram;
	InetAddress r_addr;
	int r_port;
	SipdroidSocket socket;

	public RtpSocket(final RtpSocket rtpSocket) {
		this.socket = rtpSocket.socket;
		this.r_addr = rtpSocket.r_addr;
		this.r_port = rtpSocket.r_port;
		this.datagram = rtpSocket.datagram;
	}

	public RtpSocket(final SipdroidSocket socket) {
		this.socket = socket;
		this.r_addr = null;
		this.r_port = 0;
		this.datagram = new DatagramPacket(new byte[1], 1);
	}

	public RtpSocket(final SipdroidSocket socket, final InetAddress r_addr, final int r_port) {
		this.socket = socket;
		this.r_addr = r_addr;
		this.r_port = r_port;
		this.datagram = new DatagramPacket(new byte[1], 1);
	}

	public InetAddress GetAddress() {
		return this.r_addr;
	}

	public int GetPort() {
		return this.r_port;
	}

	public SipdroidSocket GetSocket() {
		return this.socket;
	}

	public void close() {
		this.socket.close();
	}

	public SipdroidSocket getDatagramSocket() {
		return this.socket;
	}

	public void receive(final RtpPacket rtpPacket) throws IOException {
		this.datagram.setData(rtpPacket.packet);
		this.datagram.setLength(rtpPacket.packet.length);
		this.socket.receive(this.datagram);
		if (!Build.MODEL.toLowerCase().contains("fh688") && !this.socket.isConnected()) {
			this.socket.connect(this.datagram.getAddress(), this.datagram.getPort());
		}
		rtpPacket.packet_len = this.datagram.getLength();
	}

	public int send(final RtpPacket rtpPacket) throws IOException {
		this.datagram.setData(rtpPacket.packet);
		this.datagram.setLength(rtpPacket.packet_len);
		this.datagram.setAddress(this.r_addr);
		this.datagram.setPort(this.r_port);
		this.socket.send(this.datagram);
		return this.datagram.getLength();
	}
}
