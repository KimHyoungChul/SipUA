package com.zed3.net.impl;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PlainDatagramSocketImpl extends DatagramSocketImpl {
	static final int FLAG_SHUTDOWN = 8;
	static final int IP_MULTICAST_ADD = 19;
	static final int IP_MULTICAST_DROP = 20;
	static final int IP_MULTICAST_TTL = 17;
	static final int MULTICAST_IF = 1;
	static final int MULTICAST_TTL = 2;
	static final int REUSEADDR_AND_REUSEPORT = 10001;
	private static final int SO_BROADCAST = 32;
	static final int TCP_NODELAY = 4;
	private boolean bindToDevice;
	private InetAddress connectedAddress;
	private int connectedPort;
	private byte[] ipaddress;
	private volatile boolean isNativeConnected;
	private OSNetworkSystem netImpl;
	public int receiveTimeout;
	public boolean shutdownInput;
	public boolean streaming;
	private int trafficClass;
	private int ttl;

	public PlainDatagramSocketImpl() {
		this.ipaddress = new byte[4];
		this.ttl = 1;
//		this.netImpl = OSNetworkSystem.getOSNetworkSystem();
		this.streaming = true;
		this.connectedPort = -1;
		this.fd = new FileDescriptor();
	}

	public PlainDatagramSocketImpl(final FileDescriptor fd, final int localPort) {
		this.ipaddress = new byte[4];
		this.ttl = 1;
//		this.netImpl = OSNetworkSystem.getOSNetworkSystem();
		this.streaming = true;
		this.connectedPort = -1;
		this.fd = fd;
		this.localPort = localPort;
	}

	private void updatePacketRecvAddress(final DatagramPacket datagramPacket) {
		datagramPacket.setAddress(this.connectedAddress);
		datagramPacket.setPort(this.connectedPort);
	}

	public void bind(final int localPort, final InetAddress inetAddress) throws SocketException {
		// TODO
	}

	public void close() {
		// TODO
	}

	public void connect(final InetAddress inetAddress, final int connectedPort) throws SocketException {
//		this.netImpl.connectDatagram(this.fd, connectedPort, this.trafficClass, inetAddress);
		try {
			this.connectedAddress = InetAddress.getByAddress(inetAddress.getAddress());
			this.connectedPort = connectedPort;
			this.isNativeConnected = true;
		} catch (UnknownHostException ex) {
			throw new SocketException("K0317 " + inetAddress.getHostName());
		}
	}

	public void create() throws SocketException {
//		this.netImpl.createDatagramSocket(this.fd, false);
	}

	public void disconnect() {
		while (true) {
			try {
//				this.netImpl.disconnectDatagram(this.fd);
				this.connectedPort = -1;
				this.connectedAddress = null;
				this.isNativeConnected = false;
			} catch (Exception ex) {
				continue;
			}
			break;
		}
	}

	@Override
	protected void finalize() {
		this.close();
	}

	@Override
	public Object getOption(final int n) throws SocketException {
		// TODO
		return null;
	}

	public byte getTTL() throws IOException {
		byte byteValue = (byte) this.getOption(17);
//		if ((this.netImpl.getSocketFlags() & 0x2) != 0x0) {
//			byteValue = (byte) this.ttl;
//		}
		return byteValue;
	}

	public int getTimeToLive() throws IOException {
		int ttl = (byte) this.getOption(17) & 0xFF;
//		if ((this.netImpl.getSocketFlags() & 0x2) != 0x0) {
//			ttl = this.ttl;
//		}
		return ttl;
	}

	public void join(final InetAddress inetAddress) throws IOException {
	}

	public void joinGroup(final SocketAddress socketAddress, final NetworkInterface networkInterface) throws IOException {
		if (socketAddress instanceof InetSocketAddress) {
			((InetSocketAddress) socketAddress).getAddress();
		}
	}

	public void leave(final InetAddress inetAddress) throws IOException {
	}

	public void leaveGroup(final SocketAddress socketAddress, final NetworkInterface networkInterface) throws IOException {
		if (socketAddress instanceof InetSocketAddress) {
			((InetSocketAddress) socketAddress).getAddress();
		}
	}

	@Override
	protected int peek(final InetAddress inetAddress) throws IOException {
		if (this.isNativeConnected) {
			final byte[] array = new byte[10];
			final DatagramPacket datagramPacket = new DatagramPacket(array, array.length);
//			this.netImpl.recvConnectedDatagram(this.fd, datagramPacket, datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), this.receiveTimeout, true);
//			this.netImpl.setInetAddress(inetAddress, this.connectedAddress.getAddress());
			return this.connectedPort;
		}
//		return this.netImpl.peekDatagram(this.fd, inetAddress, this.receiveTimeout);
		return 0;
	}

	public int peekData(final DatagramPacket datagramPacket) throws IOException {
		// TODO
		return 0;
	}

	public void receive(final DatagramPacket datagramPacket) throws IOException {
		// TODO
	}

	public void send(final DatagramPacket datagramPacket) throws IOException {
		// TODO
	}

	@Override
	public void setOption(final int p0, final Object p1) throws SocketException {
		// TODO
	}

	public void setTTL(final byte ttl) throws IOException {
		this.setOption(17, ttl);
//		if ((this.netImpl.getSocketFlags() & 0x2) != 0x0) {
//			this.ttl = ttl;
//		}
	}

	public void setTimeToLive(final int ttl) throws IOException {
		this.setOption(17, (byte) (ttl & 0xFF));
//		if ((this.netImpl.getSocketFlags() & 0x2) != 0x0) {
//			this.ttl = ttl;
//		}
	}
}
