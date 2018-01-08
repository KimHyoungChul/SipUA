package com.zed3.addressbook;

import org.ksoap2.transport.ServiceConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MyServiceConnection implements ServiceConnection {
	private HttpURLConnection connection;
	HostnameVerifier hv;
	TrustManager[] trustAllCerts;

	public MyServiceConnection(final String s) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		this.trustAllCerts = new TrustManager[]{new X509TrustManager() {
			@Override
			public void checkClientTrusted(final X509Certificate[] array, final String s) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] array, final String s) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		}};
		this.hv = new HostnameVerifier() {
			@Override
			public boolean verify(final String s, final SSLSession sslSession) {
				if (!s.equalsIgnoreCase(sslSession.getPeerHost())) {
					System.out.println("Warning: URL host '" + s + "' is different to SSLSession host '" + sslSession.getPeerHost() + "'.");
				}
				return true;
			}
		};
		final SSLContext instance = SSLContext.getInstance("SSL");
		instance.init(null, this.trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(instance.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(this.hv);
		this.connection = (HttpsURLConnection) new URL(s).openConnection();
	}

	@Override
	public void connect() throws IOException {
		this.connection.connect();
	}

	@Override
	public void disconnect() {
		this.connection.disconnect();
	}

	@Override
	public InputStream getErrorStream() {
		return this.connection.getErrorStream();
	}

	@Override
	public String getHost() {
		return null;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public int getResponseCode() throws IOException {
		return 0;
	}

	@Override
	public List getResponseProperties() throws IOException {
		return null;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return this.connection.getInputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return this.connection.getOutputStream();
	}

	public void setConnectionTimeOut(final int connectTimeout) {
		this.connection.setConnectTimeout(connectTimeout);
	}

	@Override
	public void setFixedLengthStreamingMode(final int n) {
	}

	@Override
	public void setRequestMethod(final String requestMethod) throws IOException {
		this.connection.setRequestMethod(requestMethod);
	}

	@Override
	public void setRequestProperty(final String s, final String s2) {
		this.connection.setRequestProperty(s, s2);
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(final String s, final SSLSession sslSession) {
			return true;
		}
	}

	private static class TrustAnyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(final X509Certificate[] array, final String s) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] array, final String s) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}
