package com.zed3.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

public class HttpPasswordChange extends NetworkAbstract {
	private static final int AuthPwd = 999999;
	private static final int AuthUser = 999999;
	private static final String TAG = "HttpPasswordChange";
	private static final String httpMethod = "SetPwd";
	private static final String nameSpace = "http://schemas.xmlsoap.org/soap/encoding/";
	private volatile boolean reSend;

	public HttpPasswordChange(final String s, final int n, final ResponseListener responseListener) {
		super(s, n, responseListener);
		this.reSend = true;
	}

	private void onFailure(final String s) {
		if (this.responseListener != null) {
			this.responseListener.onError(s);
		}
	}

	private void onSuccess(final String s) {
		if (this.responseListener != null) {
			this.responseListener.onSuccess(s);
		}
	}

	private void onTimeout() {
		if (this.responseListener != null) {
			this.responseListener.onTimeOut();
		}
	}

	private void parseRes(final String s) {
		Log.i("GUOK", s);
		if ("OK".equals(s.trim().toUpperCase(Locale.getDefault()))) {
			this.onSuccess(s);
			return;
		}
		this.onFailure(s);
	}

	private void sendViaHttpClient(final String s) {
		// TODO
	}

	private String sendViaHttpUrlConnection(final String p0) {
		// TODO
		return "";
	}

	private void sendViaSoap(String string, final String s, String s2, final int n) throws TimeoutException {
		final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		if (TextUtils.isEmpty((CharSequence) s2)) {
			s2 = sharedPreferences.getString("server", "");
		}
		if (TextUtils.isEmpty((CharSequence) string)) {
			string = sharedPreferences.getString("username", "");
		}
		s2 = "http://" + s2 + "/nusoap/ISetPwd.php";
		final LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
		linkedHashMap.put("AuthUser", 999999);
		linkedHashMap.put("AuthPwd", 999999);
		linkedHashMap.put("Number", string);
		linkedHashMap.put("Password", s);
		SoapSender.send(s2, "SetPwd", linkedHashMap, (SoapSender.ParseSoapReponse) new SoapSender.ParseSoapReponse() {
			@Override
			public void getException(final Exception ex) {
				HttpPasswordChange.this.onFailure(ex.toString());
				MyLog.e("HttpPasswordChange", ex.toString());
			}

			@Override
			public Object parseReponse(final Object o) {
				HttpPasswordChange.this.parseRes(o.toString());
				return null;
			}
		});
	}

	@Override
	public void send(final String s, final String s2) {
		try {
			this.sendViaSoap(s, s2, this.serverIP, this.serverPort);
		} catch (TimeoutException ex) {
			ex.printStackTrace();
			MyLog.e("HttpPasswordChange", ex.toString());
			this.onTimeout();
		}
	}
}
