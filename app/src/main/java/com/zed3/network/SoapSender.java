package com.zed3.network;

import android.text.TextUtils;

import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SoapSender {
	private static int Timeout = 0;
	public static final String nameSpace = "http://schemas.xmlsoap.org/soap/encoding/";

	static {
		SoapSender.Timeout = 8000;
	}

	public static void send(final String s, final String s2, final LinkedHashMap<String, Object> linkedHashMap, final ParseSoapReponse parseSoapReponse) {
		if (TextUtils.isEmpty((CharSequence) s) || TextUtils.isEmpty((CharSequence) s2) || linkedHashMap == null || parseSoapReponse == null) {
			return;
		}
		final SoapObject soapObject = new SoapObject("http://schemas.xmlsoap.org/soap/encoding/", s2);
		final Iterator<Map.Entry<String, Object>> iterator = linkedHashMap.entrySet().iterator();
		while (true) {
			if (!iterator.hasNext()) {
				final SoapSerializationEnvelope soapSerializationEnvelope = new SoapSerializationEnvelope(110);
				soapSerializationEnvelope.bodyOut = soapObject;
				soapSerializationEnvelope.dotNet = true;
				soapSerializationEnvelope.setOutputSoapObject(soapObject);
				final HttpTransportSE httpTransportSE = new HttpTransportSE(s, SoapSender.Timeout);
				httpTransportSE.debug = true;
				try {
					final ArrayList<HeaderProperty> list = new ArrayList<HeaderProperty>();
					list.add(new HeaderProperty("Connection", "close"));
					httpTransportSE.call(null, soapSerializationEnvelope, list);
					parseSoapReponse.parseReponse(soapSerializationEnvelope.getResponse());
					return;
				} catch (Exception ex) {
					ex.printStackTrace();
					parseSoapReponse.getException(ex);
					return;
				}
			}
			final Map.Entry<String, Object> entry = iterator.next();
			soapObject.addProperty(entry.getKey(), entry.getValue());
		}
	}

	public interface ParseSoapReponse {
		void getException(final Exception p0);

		Object parseReponse(final Object p0);
	}
}
