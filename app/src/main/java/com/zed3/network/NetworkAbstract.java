package com.zed3.network;

public abstract class NetworkAbstract {
	public static final int ConnectTimeout = 5000;
	public static final int ReadTimeout = 1000;
	protected ResponseListener responseListener;
	protected String serverIP;
	protected int serverPort;

	public NetworkAbstract() {
		this.serverIP = "";
		this.serverPort = 0;
	}

	public NetworkAbstract(final String serverIP, final int serverPort, final ResponseListener responseListener) {
		this.serverIP = "";
		this.serverPort = 0;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.responseListener = responseListener;
	}

	public ResponseListener getResponseListener() {
		return this.responseListener;
	}

	public abstract void send(final String p0, final String p1);

	public void setResponseListener(final ResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	public interface ResponseListener {
		void onError(final String p0);

		void onSuccess(final String p0);

		void onTimeOut();
	}
}
