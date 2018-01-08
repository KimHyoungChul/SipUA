package org.zoolu.sdp;

import org.zoolu.net.IpAddress;

import java.util.Vector;

public class SessionDescriptor {
	Vector<AttributeField> av;
	ConnectionField c;
	Vector<MediaDescriptor> media;
	OriginField o;
	SessionNameField s;
	TimeField t;
	SdpField v;

	public SessionDescriptor() {
		final String localIpAddress = IpAddress.localIpAddress;
		this.init(new OriginField("user@" + localIpAddress, "0", "0", localIpAddress), new SessionNameField("Session SIP/SDP"), new ConnectionField("IP4", localIpAddress), new TimeField());
	}

	public SessionDescriptor(final String s) {
		final SdpParser sdpParser = new SdpParser(s);
		this.v = sdpParser.parseSdpField('v');
		if (this.v == null) {
			this.v = new SdpField('v', "0");
		}
		this.o = sdpParser.parseOriginField();
		if (this.o == null) {
			this.o = new OriginField("unknown");
		}
		this.s = sdpParser.parseSessionNameField();
		if (this.s == null) {
			this.s = new SessionNameField();
		}
		this.c = sdpParser.parseConnectionField();
		if (this.c == null) {
			this.c = new ConnectionField("IP4", "0.0.0.0");
		}
		this.t = sdpParser.parseTimeField();
		if (this.t == null) {
			this.t = new TimeField();
		}
		while (sdpParser.hasMore() && !sdpParser.startsWith("a=") && !sdpParser.startsWith("m=")) {
			sdpParser.goToNextLine();
		}
		this.av = new Vector<AttributeField>();
		while (sdpParser.hasMore() && sdpParser.startsWith("a=")) {
			this.av.addElement(sdpParser.parseAttributeField());
		}
		this.media = new Vector<MediaDescriptor>();
		while (true) {
			final MediaDescriptor mediaDescriptor = sdpParser.parseMediaDescriptor();
			if (mediaDescriptor == null) {
				break;
			}
			this.addMediaDescriptor(mediaDescriptor);
		}
	}

	public SessionDescriptor(final String s, String string) {
		String localIpAddress = string;
		if (string == null) {
			localIpAddress = IpAddress.localIpAddress;
		}
		if ((string = s) == null) {
			string = "user@" + localIpAddress;
		}
		this.init(new OriginField(string, "0", "0", localIpAddress), new SessionNameField("Session SIP/SDP"), new ConnectionField("IP4", localIpAddress), new TimeField());
	}

	public SessionDescriptor(final String s, final String s2, final String s3, final String s4) {
		this.init(new OriginField(s), new SessionNameField(s2), new ConnectionField(s3), new TimeField(s4));
	}

	public SessionDescriptor(final OriginField originField, final SessionNameField sessionNameField, final ConnectionField connectionField, final TimeField timeField) {
		this.init(originField, sessionNameField, connectionField, timeField);
	}

	public SessionDescriptor(final SessionDescriptor sessionDescriptor) {
		this.init(new OriginField(sessionDescriptor.o), new SessionNameField(sessionDescriptor.s), new ConnectionField(sessionDescriptor.c), new TimeField(sessionDescriptor.t));
		for (int i = 0; i < sessionDescriptor.media.size(); ++i) {
			this.media.addElement(new MediaDescriptor(sessionDescriptor.media.elementAt(i)));
		}
	}

	private void init(final OriginField o, final SessionNameField s, final ConnectionField c, final TimeField t) {
		this.v = new SdpField('v', "0");
		this.o = o;
		this.s = s;
		this.c = c;
		this.t = t;
		this.av = new Vector<AttributeField>();
		this.media = new Vector<MediaDescriptor>();
	}

	public void IncrementOLine() {
		this.o = new OriginField(this.o.getUserName(), this.o.getSessionId(), Integer.toString(Integer.valueOf(Integer.valueOf(this.o.getSessionVersion()) + 1)), this.o.getAddress());
	}

	public SessionDescriptor addAttribute(final AttributeField attributeField) {
		this.av.addElement(new AttributeField(attributeField));
		return this;
	}

	public SessionDescriptor addAttributes(final Vector<AttributeField> vector) {
		for (int i = 0; i < vector.size(); ++i) {
			this.addAttribute(vector.elementAt(i));
		}
		return this;
	}

	public SessionDescriptor addMedia(final MediaField mediaField, final Vector<AttributeField> vector) {
		this.addMediaDescriptor(new MediaDescriptor(mediaField, null, vector));
		return this;
	}

	public SessionDescriptor addMedia(final MediaField mediaField, final AttributeField attributeField) {
		this.addMediaDescriptor(new MediaDescriptor(mediaField, null, attributeField));
		return this;
	}

	public SessionDescriptor addMediaDescriptor(final MediaDescriptor mediaDescriptor) {
		this.media.addElement(mediaDescriptor);
		return this;
	}

	public SessionDescriptor addMediaDescriptors(final Vector<MediaDescriptor> vector) {
		for (int i = 0; i < vector.size(); ++i) {
			this.media.addElement(vector.elementAt(i));
		}
		return this;
	}

	public AttributeField getAttribute(final String s) {
		for (int i = 0; i < this.av.size(); ++i) {
			final AttributeField attributeField;
			if ((attributeField = this.av.elementAt(i)).getAttributeName().equals(s)) {
				return attributeField;
			}
		}
		return null;
	}

	public Vector<AttributeField> getAttributes() {
		final Vector<AttributeField> vector = new Vector<AttributeField>(this.av.size());
		for (int i = 0; i < this.av.size(); ++i) {
			vector.addElement(this.av.elementAt(i));
		}
		return vector;
	}

	public Vector<AttributeField> getAttributes(final String s) {
		final Vector<AttributeField> vector = new Vector<AttributeField>(this.av.size());
		for (int i = 0; i < this.av.size(); ++i) {
			final AttributeField attributeField = this.av.elementAt(i);
			if (attributeField.getAttributeName().equals(s)) {
				vector.addElement(attributeField);
			}
		}
		return vector;
	}

	public ConnectionField getConnection() {
		return this.c;
	}

	public MediaDescriptor getMediaDescriptor(final String s) {
		for (int i = 0; i < this.media.size(); ++i) {
			final MediaDescriptor mediaDescriptor;
			if ((mediaDescriptor = this.media.elementAt(i)).getMedia().getMedia().equals(s)) {
				return mediaDescriptor;
			}
		}
		return null;
	}

	public Vector<MediaDescriptor> getMediaDescriptors() {
		return this.media;
	}

	public OriginField getOrigin() {
		return this.o;
	}

	public SessionNameField getSessionName() {
		return this.s;
	}

	public TimeField getTime() {
		return this.t;
	}

	public boolean hasAttribute(final String s) {
		for (int i = 0; i < this.av.size(); ++i) {
			if (this.av.elementAt(i).getAttributeName().equals(s)) {
				return true;
			}
		}
		return false;
	}

	public SessionDescriptor removeAttributes() {
		this.av.setSize(0);
		return this;
	}

	public SessionDescriptor removeMediaDescriptor(final String s) {
		for (int i = this.media.size() - 1; i >= 0; --i) {
			if (this.media.elementAt(i).getMedia().getMedia().equals(s)) {
				this.media.removeElementAt(i);
			}
		}
		return this;
	}

	public SessionDescriptor removeMediaDescriptors() {
		this.media.setSize(0);
		return this;
	}

	public SessionDescriptor setConnection(final ConnectionField c) {
		this.c = c;
		return this;
	}

	public SessionDescriptor setOrigin(final OriginField o) {
		this.o = o;
		return this;
	}

	public SessionDescriptor setSessionName(final SessionNameField s) {
		this.s = s;
		return this;
	}

	public SessionDescriptor setTime(final TimeField t) {
		this.t = t;
		return this;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if (this.v != null) {
			sb.append(this.v.toString());
		}
		if (this.o != null) {
			sb.append(this.o.toString());
		}
		if (this.s != null) {
			sb.append(this.s.toString());
		}
		if (this.c != null) {
			sb.append(this.c.toString());
		}
		if (this.t != null) {
			sb.append(this.t.toString());
		}
		for (int i = 0; i < this.av.size(); ++i) {
			sb.append(this.av.elementAt(i).toString());
		}
		for (int j = 0; j < this.media.size(); ++j) {
			sb.append(this.media.elementAt(j).toString());
		}
		return sb.toString();
	}
}
