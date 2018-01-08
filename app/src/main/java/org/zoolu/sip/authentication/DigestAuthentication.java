package org.zoolu.sip.authentication;

import org.zoolu.sip.header.AuthenticationHeader;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.ProxyAuthorizationHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.tools.MD5;
import org.zoolu.tools.MessageDigest;
import org.zoolu.tools.Random;

public class DigestAuthentication {
	protected String algorithm;
	protected String body;
	protected String cnonce;
	protected String method;
	protected String nc;
	protected String nonce;
	protected String opaque;
	protected String passwd;
	protected String qop;
	protected String realm;
	protected String response;
	protected String uri;
	protected String username;

	protected DigestAuthentication() {
	}

	public DigestAuthentication(final String s, final String uri, final WwwAuthenticateHeader wwwAuthenticateHeader, final String qop, final String s2, final String username, final String s3) {
		this.init(s, wwwAuthenticateHeader, s2, s3);
		this.uri = uri;
		this.qop = qop;
		if (qop != null && this.cnonce == null) {
			this.cnonce = Random.nextHexString(16);
			this.nc = "00000001";
		}
		this.username = username;
	}

	public DigestAuthentication(final String s, final AuthorizationHeader authorizationHeader, final String s2, final String s3) {
		this.init(s, authorizationHeader, s2, s3);
	}

	private byte[] A1() {
		final StringBuffer sb = new StringBuffer();
		if (this.username != null) {
			sb.append(this.username);
		}
		sb.append(":");
		if (this.realm != null) {
			sb.append(this.realm);
		}
		sb.append(":");
		if (this.passwd != null) {
			sb.append(this.passwd);
		}
		if (this.algorithm == null || !this.algorithm.equalsIgnoreCase("MD5-sess")) {
			return sb.toString().getBytes();
		}
		final StringBuffer sb2 = new StringBuffer();
		sb2.append(":");
		if (this.nonce != null) {
			sb2.append(this.nonce);
		}
		sb2.append(":");
		if (this.cnonce != null) {
			sb2.append(this.cnonce);
		}
		return cat(MD5(sb.toString()), sb2.toString().getBytes());
	}

	private String A2() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.method);
		sb.append(":");
		if (this.uri != null) {
			sb.append(this.uri);
		}
		if (this.qop != null && this.qop.equalsIgnoreCase("auth-int")) {
			sb.append(":");
			if (this.body == null) {
				sb.append(HEX(MD5("")));
			} else {
				sb.append(HEX(MD5(this.body)));
			}
		}
		return sb.toString();
	}

	private static String HEX(final byte[] array) {
		return MessageDigest.asHex(array);
	}

	private byte[] KD(final String s, final String s2) {
		final StringBuffer sb = new StringBuffer();
		sb.append(s).append(":").append(s2);
		return MD5(sb.toString());
	}

	private static byte[] MD5(final String s) {
		return MD5.digest(s);
	}

	private static byte[] MD5(final byte[] array) {
		return MD5.digest(array);
	}

	private static byte[] cat(final byte[] array, final byte[] array2) {
		final byte[] array3 = new byte[array.length + array2.length];
		for (int i = 0; i < array.length; ++i) {
			array3[i] = array[i];
		}
		for (int j = 0; j < array2.length; ++j) {
			array3[array.length + j] = array2[j];
		}
		return array3;
	}

	private void init(final String method, final AuthenticationHeader authenticationHeader, final String body, final String passwd) {
		this.method = method;
		this.username = authenticationHeader.getUsernameParam();
		this.passwd = passwd;
		this.realm = authenticationHeader.getRealmParam();
		this.opaque = authenticationHeader.getOpaqueParam();
		this.nonce = authenticationHeader.getNonceParam();
		this.algorithm = authenticationHeader.getAlgorithParam();
		this.qop = authenticationHeader.getQopParam();
		this.uri = authenticationHeader.getUriParam();
		this.cnonce = authenticationHeader.getCnonceParam();
		this.nc = authenticationHeader.getNcParam();
		this.response = authenticationHeader.getResponseParam();
		this.body = body;
	}

	public static void main(final String[] array) {
		final DigestAuthentication digestAuthentication = new DigestAuthentication();
		digestAuthentication.method = "GET";
		digestAuthentication.passwd = "Circle Of Life";
		digestAuthentication.realm = "testrealm@host.com";
		digestAuthentication.nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";
		digestAuthentication.uri = "/dir/index.html";
		digestAuthentication.qop = "auth";
		digestAuthentication.nc = "00000001";
		digestAuthentication.cnonce = "0a4f113b";
		digestAuthentication.username = "Mufasa";
		System.out.println(digestAuthentication.getResponse());
		System.out.println("6629fae49393a05397450978507c4ef1");
		System.out.println(" ");
		final DigestAuthentication digestAuthentication2 = new DigestAuthentication("GET", new AuthorizationHeader("Digest username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\", qop=auth, nc=00000001, cnonce=\"0a4f113b\", response=\"6629fae49393a05397450978507c4ef1\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\n"), null, "Circle Of Life");
		System.out.println(digestAuthentication2.getResponse());
		System.out.println("6629fae49393a05397450978507c4ef1");
		System.out.println(digestAuthentication2.checkResponse());
	}

	public boolean checkResponse() {
		return this.response != null && this.response.equals(this.getResponse());
	}

	public AuthorizationHeader getAuthorizationHeader() {
		final AuthorizationHeader authorizationHeader = new AuthorizationHeader("Digest");
		authorizationHeader.addUsernameParam(this.username);
		authorizationHeader.addRealmParam(this.realm);
		authorizationHeader.addNonceParam(this.nonce);
		authorizationHeader.addUriParam(this.uri);
		if (this.algorithm != null) {
			authorizationHeader.addAlgorithParam(this.algorithm);
		}
		if (this.opaque != null) {
			authorizationHeader.addOpaqueParam(this.opaque);
		}
		if (this.qop != null) {
			authorizationHeader.addQopParam(this.qop);
		}
		if (this.nc != null) {
			authorizationHeader.addNcParam(this.nc);
		}
		if (this.cnonce != null) {
			authorizationHeader.addCnonceParam(this.cnonce);
		}
		authorizationHeader.addResponseParam(this.getResponse());
		return authorizationHeader;
	}

	public ProxyAuthorizationHeader getProxyAuthorizationHeader() {
		return new ProxyAuthorizationHeader(this.getAuthorizationHeader().getValue());
	}

	public String getResponse() {
		final String hex = HEX(MD5(this.A1()));
		final StringBuffer sb = new StringBuffer();
		if (this.nonce != null) {
			sb.append(this.nonce);
		}
		sb.append(":");
		if (this.qop != null) {
			if (this.nc != null) {
				sb.append(this.nc);
			}
			sb.append(":");
			if (this.cnonce != null) {
				sb.append(this.cnonce);
			}
			sb.append(":");
			sb.append(this.qop);
			sb.append(":");
		}
		sb.append(HEX(MD5(this.A2())));
		return HEX(this.KD(hex, sb.toString()));
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("method=").append(this.method).append("\n");
		sb.append("username=").append(this.username).append("\n");
		sb.append("passwd=").append(this.passwd).append("\n");
		sb.append("realm=").append(this.realm).append("\n");
		sb.append("nonce=").append(this.nonce).append("\n");
		sb.append("opaque=").append(this.opaque).append("\n");
		sb.append("algorithm=").append(this.algorithm).append("\n");
		sb.append("qop=").append(this.qop).append("\n");
		sb.append("uri=").append(this.uri).append("\n");
		sb.append("cnonce=").append(this.cnonce).append("\n");
		sb.append("nc=").append(this.nc).append("\n");
		sb.append("response=").append(this.response).append("\n");
		sb.append("body=").append(this.body).append("\n");
		return sb.toString();
	}
}
