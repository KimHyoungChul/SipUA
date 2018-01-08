package org.zoolu.sip.call;

import com.zed3.codecs.CodecBase;
import com.zed3.log.MyLog;

import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.SessionDescriptor;

import java.util.Enumeration;
import java.util.Vector;

public class SdpTools {
	private static CodecBase codecbase;
	private static boolean needLog;
	private static String tag;

	static {
		SdpTools.tag = "SdpTools";
		SdpTools.needLog = true;
	}

	public static SessionDescriptor sdpAttirbuteSelection(final SessionDescriptor sessionDescriptor, final String s) {
		final Vector<MediaDescriptor> vector = new Vector<MediaDescriptor>();
		final Enumeration<MediaDescriptor> elements = sessionDescriptor.getMediaDescriptors().elements();
		while (elements.hasMoreElements()) {
			final MediaDescriptor mediaDescriptor = elements.nextElement();
			final AttributeField attribute = mediaDescriptor.getAttribute(s);
			if (attribute != null) {
				vector.addElement(new MediaDescriptor(mediaDescriptor.getMedia(), mediaDescriptor.getConnection(), attribute));
			}
		}
		final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(sessionDescriptor);
		sessionDescriptor2.removeMediaDescriptors();
		sessionDescriptor2.addMediaDescriptors(vector);
		return sessionDescriptor2;
	}

	public static SessionDescriptor sdpMediaProduct(final SessionDescriptor sessionDescriptor, final Vector<MediaDescriptor> vector) {
		return sdpMediaProduct(sessionDescriptor, vector, null);
	}

	public static SessionDescriptor sdpMediaProduct(final SessionDescriptor sessionDescriptor, final Vector<MediaDescriptor> vector, final ExtendedCall extendedCall) {
		final Vector<MediaDescriptor> vector2 = new Vector<MediaDescriptor>();
		int n = 0;
		if (vector != null) {
			if (extendedCall != null && extendedCall.getCallDirection() == 0) {
				extendedCall.setCallPtime(20);
			}
			final Enumeration<MediaDescriptor> elements = vector.elements();
			// TODO
		}
		MyLog.i("cccc1", sessionDescriptor.toString());
		final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(sessionDescriptor);
		sessionDescriptor2.removeMediaDescriptors();
		sessionDescriptor2.addMediaDescriptors(vector2);
		MyLog.i("cccc2", sessionDescriptor2.toString());
		return sessionDescriptor2;
	}
}
