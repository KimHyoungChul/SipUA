package com.zed3.net;

import android.os.Build;

import com.zed3.log.MyLog;
import com.zed3.net.impl.OSNetworkSystem;
import com.zed3.net.impl.PlainDatagramSocketImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SipdroidSocket extends DatagramSocket {
	private static final String LIB_ART = "libart.so";
	private static final String LIB_ART_D = "libartd.so";
	private static final String LIB_DALVIK = "libdvm.so";
	private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
	public static boolean loaded;
	PlainDatagramSocketImpl impl;

	static {
		SipdroidSocket.loaded = false;
		final String vm = getVM();
		System.out.println("-----runTimeValue:" + vm);
		if (vm.contains("ART") || Build.VERSION.SDK_INT > 20) {
		}
		try {
			System.out.println("-----static:" + vm);
			System.loadLibrary("OSNetworkSystem");
//			OSNetworkSystem.getOSNetworkSystem().oneTimeInitialization(true);
			SipdroidSocket.loaded = true;
		} catch (Throwable t) {
		}
	}

	public SipdroidSocket(final int n) throws SocketException, UnknownHostException {
		// TODO
	}

	public static String getVM() {
		// TODO
		return "";
	}

	@Override
	public void close() {
		super.close();
		if (SipdroidSocket.loaded) {
			this.impl.close();
		}
	}

	@Override
	public void connect(final InetAddress inetAddress, final int n) {
		if (!SipdroidSocket.loaded) {
			super.connect(inetAddress, n);
		}
	}

	@Override
	public void disconnect() {
		if (!SipdroidSocket.loaded) {
			super.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return SipdroidSocket.loaded || super.isConnected();
	}

	@Override
	public void receive(final DatagramPacket datagramPacket) throws IOException {
		if (SipdroidSocket.loaded) {
			this.impl.receive(datagramPacket);
			return;
		}
		super.receive(datagramPacket);
	}

	@Override
	public void send(final DatagramPacket datagramPacket) throws IOException {
		try {
			if (SipdroidSocket.loaded) {
				this.impl.send(datagramPacket);
				return;
			}
			super.send(datagramPacket);
		} catch (IllegalArgumentException ex) {
			MyLog.e("SipdroidSocket", "send function exception:" + ex.toString());
			throw new IOException(ex.toString());
		}
	}

	@Override
	public void setSoTimeout(final int soTimeout) throws SocketException {
		if (SipdroidSocket.loaded) {
			this.impl.setOption(4102, soTimeout);
			return;
		}
		super.setSoTimeout(soTimeout);
	}
}
