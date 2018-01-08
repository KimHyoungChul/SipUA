package org.zoolu.net;

import java.net.DatagramPacket;

public class UdpPacket {
	DatagramPacket packet;

	UdpPacket(final DatagramPacket packet) {
		this.packet = packet;
	}

	public UdpPacket(final byte[] array, final int n) {
		this.packet = new DatagramPacket(array, n);
	}

	public UdpPacket(final byte[] array, final int n, final int n2) {
		this.packet = new DatagramPacket(array, n, n2);
	}

	public UdpPacket(final byte[] array, final int n, final int n2, final IpAddress ipAddress, final int n3) {
		this.packet = new DatagramPacket(array, n, n2, ipAddress.getInetAddress(), n3);
	}

	public UdpPacket(final byte[] array, final int n, final IpAddress ipAddress, final int n2) {
		this.packet = new DatagramPacket(array, n, ipAddress.getInetAddress(), n2);
	}

	public byte[] getData() {
		return this.packet.getData();
	}

	DatagramPacket getDatagramPacket() {
		return this.packet;
	}

	public IpAddress getIpAddress() {
		return new IpAddress(this.packet.getAddress());
	}

	public int getLength() {
		return this.packet.getLength();
	}

	public int getOffset() {
		return this.packet.getOffset();
	}

	public int getPort() {
		return this.packet.getPort();
	}

	public void setData(final byte[] data) {
		this.packet.setData(data);
	}

	public void setData(final byte[] array, final int n, final int n2) {
		this.packet.setData(array, n, n2);
	}

	void setDatagramPacket(final DatagramPacket packet) {
		this.packet = packet;
	}

	public void setIpAddress(final IpAddress ipAddress) {
		this.packet.setAddress(ipAddress.getInetAddress());
	}

	public void setLength(final int length) {
		this.packet.setLength(length);
	}

	public void setPort(final int port) {
		this.packet.setPort(port);
	}
}
