package org.zoolu.sdp;

import java.util.Vector;

public class MediaDescriptor {
	Vector<AttributeField> av;
	ConnectionField c;
	MediaField m;

	public MediaDescriptor(final String s, final String s2, final String s3) {
		this.m = new MediaField(s);
		if (s2 != null) {
			this.c = new ConnectionField(s2);
		}
		this.av = new Vector<AttributeField>();
		if (s3 != null) {
			this.av.addElement(new AttributeField(s3));
		}
	}

	public MediaDescriptor(final MediaDescriptor mediaDescriptor) {
		this.m = new MediaField(mediaDescriptor.m);
		if (mediaDescriptor.c != null) {
			this.c = new ConnectionField(mediaDescriptor.c);
		} else {
			this.c = null;
		}
		this.av = new Vector<AttributeField>();
		for (int i = 0; i < mediaDescriptor.av.size(); ++i) {
			this.av.addElement(new AttributeField(mediaDescriptor.av.elementAt(i)));
		}
	}

	public MediaDescriptor(final MediaField m, final ConnectionField c) {
		this.m = m;
		this.c = c;
		this.av = new Vector<AttributeField>();
	}

	public MediaDescriptor(final MediaField m, final ConnectionField c, final Vector<AttributeField> vector) {
		this.m = m;
		this.c = c;
		(this.av = new Vector<AttributeField>(vector.size())).setSize(vector.size());
		for (int i = 0; i < vector.size(); ++i) {
			this.av.setElementAt(vector.elementAt(i), i);
		}
	}

	public MediaDescriptor(final MediaField m, final ConnectionField c, final AttributeField attributeField) {
		this.m = m;
		this.c = c;
		this.av = new Vector<AttributeField>();
		if (attributeField != null) {
			this.av.addElement(attributeField);
		}
	}

	public MediaDescriptor addAttribute(final AttributeField attributeField) {
		this.av.addElement(new AttributeField(attributeField));
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

	public MediaField getMedia() {
		return this.m;
	}

	public boolean hasAttribute(final String s) {
		for (int i = 0; i < this.av.size(); ++i) {
			if (this.av.elementAt(i).getAttributeName().equals(s)) {
				return true;
			}
		}
		return false;
	}

	public String hasCodec(final String s) {
		for (int i = 0; i < this.av.size(); ++i) {
			final AttributeField attributeField = this.av.elementAt(i);
			if (attributeField.getAttributeName().equalsIgnoreCase("rtpmap")) {
				final String[] split = attributeField.getAttributeValue().split(" +", 2);
				if (split.length == 2 && split[1].toLowerCase().startsWith(s.toLowerCase())) {
					return split[1];
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		String s2;
		final String s = s2 = String.valueOf("") + this.m;
		if (this.c != null) {
			s2 = String.valueOf(s) + this.c;
		}
		for (int i = 0; i < this.av.size(); ++i) {
			s2 = String.valueOf(s2) + this.av.elementAt(i);
		}
		return s2;
	}
}
