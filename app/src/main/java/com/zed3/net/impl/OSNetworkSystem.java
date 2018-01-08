package com.zed3.net.impl;

public final class OSNetworkSystem {
	private static final int ERRORCODE_SOCKET_TIMEOUT = -209;
	private static final int INETADDR_REACHABLE = 0;
	private static boolean isNetworkInited;
	private static OSNetworkSystem ref;

	static {
		OSNetworkSystem.ref = new OSNetworkSystem();
		OSNetworkSystem.isNetworkInited = false;
	}

//	static native void acceptSocketImpl(final FileDescriptor p0, final SocketImpl p1, final FileDescriptor p2, final int p3) throws IOException;
//
//	static native int availableStreamImpl(final FileDescriptor p0) throws SocketException;
//
//	static native void connectDatagramImpl2(final FileDescriptor p0, final int p1, final int p2, final InetAddress p3) throws SocketException;
//
//	static native int connectSocketImpl(final FileDescriptor p0, final int p1, final InetAddress p2, final int p3);
//
//	static native void connectStreamWithTimeoutSocketImpl(final FileDescriptor p0, final int p1, final int p2, final int p3, final InetAddress p4) throws IOException;
//
//	static native int connectWithTimeoutSocketImpl(final FileDescriptor p0, final int p1, final int p2, final InetAddress p3, final int p4, final int p5, final byte[] p6);
//
//	static native void createDatagramSocketImpl(final FileDescriptor p0, final boolean p1) throws SocketException;
//
//	static native void createMulticastSocketImpl(final FileDescriptor p0, final boolean p1) throws SocketException;
//
//	static native void createServerStreamSocketImpl(final FileDescriptor p0, final boolean p1) throws SocketException;
//
//	static native void createSocketImpl(final FileDescriptor p0, final boolean p1);
//
//	static native void disconnectDatagramImpl(final FileDescriptor p0) throws SocketException;
//
//	static native InetAddress getHostByAddrImpl(final byte[] p0) throws UnknownHostException;
//
//	static native InetAddress getHostByNameImpl(final String p0, final boolean p1) throws UnknownHostException;
//
//	public static OSNetworkSystem getOSNetworkSystem() {
//		return OSNetworkSystem.ref;
//	}
//
//	static native int getSocketFlagsImpl();
//
//	static native InetAddress getSocketLocalAddressImpl(final FileDescriptor p0, final boolean p1);
//
//	static native int getSocketLocalPortImpl(final FileDescriptor p0, final boolean p1);
//
//	static native Object getSocketOptionImpl(final FileDescriptor p0, final int p1) throws SocketException;
//
//	static native void listenStreamSocketImpl(final FileDescriptor p0, final int p1) throws SocketException;
//
//	static native int peekDatagramImpl(final FileDescriptor p0, final InetAddress p1, final int p2) throws IOException;
//
//	static native int readSocketDirectImpl(final FileDescriptor p0, final int p1, final int p2, final int p3, final int p4) throws IOException;
//
//	static native int readSocketImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3, final int p4) throws IOException;
//
//	static native int receiveDatagramDirectImpl(final FileDescriptor p0, final DatagramPacket p1, final int p2, final int p3, final int p4, final int p5, final boolean p6) throws IOException;
//
//	static native int receiveDatagramImpl(final FileDescriptor p0, final DatagramPacket p1, final byte[] p2, final int p3, final int p4, final int p5, final boolean p6) throws IOException;
//
//	static native int receiveStreamImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3, final int p4) throws IOException;
//
//	static native int recvConnectedDatagramDirectImpl(final FileDescriptor p0, final DatagramPacket p1, final int p2, final int p3, final int p4, final int p5, final boolean p6) throws IOException;
//
//	static native int recvConnectedDatagramImpl(final FileDescriptor p0, final DatagramPacket p1, final byte[] p2, final int p3, final int p4, final int p5, final boolean p6) throws IOException;
//
//	static native int selectImpl(final FileDescriptor[] p0, final FileDescriptor[] p1, final int p2, final int p3, final int[] p4, final long p5);
//
//	static native int sendConnectedDatagramDirectImpl(final FileDescriptor p0, final int p1, final int p2, final int p3, final boolean p4) throws IOException;
//
//	static native int sendConnectedDatagramImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3, final boolean p4) throws IOException;
//
//	static native int sendDatagramDirectImpl(final FileDescriptor p0, final int p1, final int p2, final int p3, final int p4, final boolean p5, final int p6, final InetAddress p7) throws IOException;
//
//	static native int sendDatagramImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3, final int p4, final boolean p5, final int p6, final InetAddress p7) throws IOException;
//
//	static native int sendDatagramImpl2(final FileDescriptor p0, final byte[] p1, final int p2, final int p3, final int p4, final InetAddress p5) throws IOException;
//
//	static native int sendStreamImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3) throws IOException;
//
//	static native void sendUrgentDataImpl(final FileDescriptor p0, final byte p1);
//
//	static native void setNonBlockingImpl(final FileDescriptor p0, final boolean p1);
//
//	static native void setSocketOptionImpl(final FileDescriptor p0, final int p1, final Object p2) throws SocketException;
//
//	private native void shutdownInputImpl(final FileDescriptor p0) throws IOException;
//
//	private native void shutdownOutputImpl(final FileDescriptor p0) throws IOException;
//
//	static native void socketBindImpl(final FileDescriptor p0, final int p1, final InetAddress p2) throws SocketException;
//
//	static native boolean socketBindImpl2(final FileDescriptor p0, final int p1, final boolean p2, final InetAddress p3) throws SocketException;
//
//	static native void socketCloseImpl(final FileDescriptor p0);
//
//	static native boolean supportsUrgentDataImpl(final FileDescriptor p0);
//
//	static native int writeSocketDirectImpl(final FileDescriptor p0, final int p1, final int p2, final int p3) throws IOException;
//
//	static native int writeSocketImpl(final FileDescriptor p0, final byte[] p1, final int p2, final int p3) throws IOException;
//
//	public void accept(final FileDescriptor fileDescriptor, final SocketImpl socketImpl, final FileDescriptor fileDescriptor2, final int n) throws IOException {
//		acceptSocketImpl(fileDescriptor, socketImpl, fileDescriptor2, n);
//	}
//
//	public int availableStream(final FileDescriptor fileDescriptor) throws SocketException {
//		return availableStreamImpl(fileDescriptor);
//	}
//
//	public void bind(final FileDescriptor fileDescriptor, final int n, final InetAddress inetAddress) throws SocketException {
//		socketBindImpl(fileDescriptor, n, inetAddress);
//	}
//
//	public boolean bind2(final FileDescriptor fileDescriptor, final int n, final boolean b, final InetAddress inetAddress) throws SocketException {
//		return socketBindImpl2(fileDescriptor, n, b, inetAddress);
//	}
//
//	public int connect(final FileDescriptor fileDescriptor, final int n, final InetAddress inetAddress, final int n2) throws IOException {
//		return connectSocketImpl(fileDescriptor, n, inetAddress, n2);
//	}
//
//	public void connectDatagram(final FileDescriptor fileDescriptor, final int n, final int n2, final InetAddress inetAddress) throws SocketException {
//		connectDatagramImpl2(fileDescriptor, n, n2, inetAddress);
//	}
//
//	public void connectStreamWithTimeoutSocket(final FileDescriptor fileDescriptor, final int n, final int n2, final int n3, final InetAddress inetAddress) throws IOException {
//		connectStreamWithTimeoutSocketImpl(fileDescriptor, n, n2, n3, inetAddress);
//	}
//
//	public int connectWithTimeout(final FileDescriptor fileDescriptor, final int n, final int n2, final InetAddress inetAddress, final int n3, final int n4, final byte[] array) throws IOException {
//		return connectWithTimeoutSocketImpl(fileDescriptor, n, n2, inetAddress, n3, n4, array);
//	}
//
//	public void createDatagramSocket(final FileDescriptor fileDescriptor, final boolean b) throws SocketException {
//		createDatagramSocketImpl(fileDescriptor, b);
//	}
//
//	public void createMulticastSocket(final FileDescriptor fileDescriptor, final boolean b) throws SocketException {
//		createMulticastSocketImpl(fileDescriptor, b);
//	}
//
//	public void createServerStreamSocket(final FileDescriptor fileDescriptor, final boolean b) throws SocketException {
//		createServerStreamSocketImpl(fileDescriptor, b);
//	}
//
//	public void createSocket(final FileDescriptor fileDescriptor, final boolean b) throws IOException {
//		createSocketImpl(fileDescriptor, b);
//	}
//
//	public void disconnectDatagram(final FileDescriptor fileDescriptor) throws SocketException {
//		disconnectDatagramImpl(fileDescriptor);
//	}
//
//	public InetAddress getHostByAddr(final byte[] array) throws UnknownHostException {
//		return getHostByAddrImpl(array);
//	}
//
//	public InetAddress getHostByName(final String s, final boolean b) throws UnknownHostException {
//		return getHostByNameImpl(s, b);
//	}
//
//	public int getSocketFlags() {
//		return getSocketFlagsImpl();
//	}
//
//	public InetAddress getSocketLocalAddress(final FileDescriptor fileDescriptor, final boolean b) {
//		return getSocketLocalAddressImpl(fileDescriptor, b);
//	}
//
//	public int getSocketLocalPort(final FileDescriptor fileDescriptor, final boolean b) {
//		return getSocketLocalPortImpl(fileDescriptor, b);
//	}
//
//	public Object getSocketOption(final FileDescriptor fileDescriptor, final int n) throws SocketException {
//		return getSocketOptionImpl(fileDescriptor, n);
//	}
//
//	public Channel inheritedChannel() {
//		return this.inheritedChannelImpl();
//	}
//
//	native Channel inheritedChannelImpl();
//
//	public void listenStreamSocket(final FileDescriptor fileDescriptor, final int n) throws SocketException {
//		listenStreamSocketImpl(fileDescriptor, n);
//	}
//
//	public void oneTimeInitialization(final boolean b) {
//		if (!OSNetworkSystem.isNetworkInited) {
//			this.oneTimeInitializationImpl(b);
//			OSNetworkSystem.isNetworkInited = true;
//		}
//	}
//
//	native void oneTimeInitializationImpl(final boolean p0);
//
//	public int peekDatagram(final FileDescriptor fileDescriptor, final InetAddress inetAddress, final int n) throws IOException {
//		return peekDatagramImpl(fileDescriptor, inetAddress, n);
//	}
//
//	public int read(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2, final int n3) throws IOException {
//		return readSocketImpl(fileDescriptor, array, n, n2, n3);
//	}
//
//	public int readDirect(final FileDescriptor fileDescriptor, final int n, final int n2, final int n3, final int n4) throws IOException {
//		return readSocketDirectImpl(fileDescriptor, n, n2, n3, n4);
//	}
//
//	public int receiveDatagram(final FileDescriptor fileDescriptor, final DatagramPacket datagramPacket, final byte[] array, final int n, final int n2, final int n3, final boolean b) throws IOException {
//		return receiveDatagramImpl(fileDescriptor, datagramPacket, array, n, n2, n3, b);
//	}
//
//	public int receiveDatagramDirect(final FileDescriptor fileDescriptor, final DatagramPacket datagramPacket, final int n, final int n2, final int n3, final int n4, final boolean b) throws IOException {
//		return receiveDatagramDirectImpl(fileDescriptor, datagramPacket, n, n2, n3, n4, b);
//	}
//
//	public int receiveStream(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2, final int n3) throws IOException {
//		return receiveStreamImpl(fileDescriptor, array, n, n2, n3);
//	}
//
//	public int recvConnectedDatagram(final FileDescriptor fileDescriptor, final DatagramPacket datagramPacket, final byte[] array, final int n, final int n2, final int n3, final boolean b) throws IOException {
//		return recvConnectedDatagramImpl(fileDescriptor, datagramPacket, array, n, n2, n3, b);
//	}
//
//	public int recvConnectedDatagramDirect(final FileDescriptor fileDescriptor, final DatagramPacket datagramPacket, final int n, final int n2, final int n3, final int n4, final boolean b) throws IOException {
//		return recvConnectedDatagramDirectImpl(fileDescriptor, datagramPacket, n, n2, n3, n4, b);
//	}
//
//	public int[] select(final FileDescriptor[] array, final FileDescriptor[] array2, final long n) throws SocketException {
//		final int length = array.length;
//		final int length2 = array2.length;
//		int[] array3;
//		if (length + length2 == 0) {
//			array3 = new int[0];
//		} else {
//			final int[] array4 = new int[length + length2];
//			final int selectImpl = selectImpl(array, array2, length, length2, array4, n);
//			array3 = array4;
//			if (selectImpl < 0) {
//				if (-209 == selectImpl) {
//					return new int[0];
//				}
//				throw new SocketException();
//			}
//		}
//		return array3;
//	}
//
//	public int sendConnectedDatagram(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2, final boolean b) throws IOException {
//		return sendConnectedDatagramImpl(fileDescriptor, array, n, n2, b);
//	}
//
//	public int sendConnectedDatagramDirect(final FileDescriptor fileDescriptor, final int n, final int n2, final int n3, final boolean b) throws IOException {
//		return sendConnectedDatagramDirectImpl(fileDescriptor, n, n2, n3, b);
//	}
//
//	public int sendDatagram(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2, final int n3, final boolean b, final int n4, final InetAddress inetAddress) throws IOException {
//		return sendDatagramImpl(fileDescriptor, array, n, n2, n3, b, n4, inetAddress);
//	}
//
//	public int sendDatagram2(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2, final int n3, final InetAddress inetAddress) throws IOException {
//		return sendDatagramImpl2(fileDescriptor, array, n, n2, n3, inetAddress);
//	}
//
//	public int sendDatagramDirect(final FileDescriptor fileDescriptor, final int n, final int n2, final int n3, final int n4, final boolean b, final int n5, final InetAddress inetAddress) throws IOException {
//		return sendDatagramDirectImpl(fileDescriptor, n, n2, n3, n4, b, n5, inetAddress);
//	}
//
//	public int sendStream(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2) throws IOException {
//		return sendStreamImpl(fileDescriptor, array, n, n2);
//	}
//
//	public void sendUrgentData(final FileDescriptor fileDescriptor, final byte b) {
//		sendUrgentDataImpl(fileDescriptor, b);
//	}
//
//	public void setInetAddress(final InetAddress inetAddress, final byte[] array) {
//		this.setInetAddressImpl(inetAddress, array);
//	}
//
//	native void setInetAddressImpl(final InetAddress p0, final byte[] p1);
//
//	public void setNonBlocking(final FileDescriptor fileDescriptor, final boolean b) throws IOException {
//		setNonBlockingImpl(fileDescriptor, b);
//	}
//
//	public void setSocketOption(final FileDescriptor fileDescriptor, final int n, final Object o) throws SocketException {
//		setSocketOptionImpl(fileDescriptor, n, o);
//	}
//
//	public void shutdownInput(final FileDescriptor fileDescriptor) throws IOException {
//		this.shutdownInputImpl(fileDescriptor);
//	}
//
//	public void shutdownOutput(final FileDescriptor fileDescriptor) throws IOException {
//		this.shutdownOutputImpl(fileDescriptor);
//	}
//
//	public void socketClose(final FileDescriptor fileDescriptor) throws IOException {
//		socketCloseImpl(fileDescriptor);
//	}
//
//	public boolean supportsUrgentData(final FileDescriptor fileDescriptor) {
//		return supportsUrgentDataImpl(fileDescriptor);
//	}
//
//	public int write(final FileDescriptor fileDescriptor, final byte[] array, final int n, final int n2) throws IOException {
//		return writeSocketImpl(fileDescriptor, array, n, n2);
//	}
//
//	public int writeDirect(final FileDescriptor fileDescriptor, final int n, final int n2, final int n3) throws IOException {
//		return writeSocketDirectImpl(fileDescriptor, n, n2, n3);
//	}
}
