package com.zed3.sipua.ui;

import com.zed3.sipua.R;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseXML extends DefaultHandler {
	private StringBuffer buffer;
	List<Map<String, Object>> list;
	Map<String, Object> map;

	public ParseXML() {
		this.buffer = new StringBuffer();
	}

	public void SetData(final List<Map<String, Object>> list) {
		this.list = list;
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		this.buffer.append(ch, start, length);
		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (localName.equalsIgnoreCase("contacts")) {
			if (this.map != null) {
				this.map.put("img", R.drawable.icon_contact);
				this.list.add(this.map);
				this.buffer.setLength(0);
			}
		} else if (localName.equalsIgnoreCase("name")) {
			if (this.map != null) {
				this.map.put("title", this.buffer.toString().trim());
			}
			this.buffer.setLength(0);
		} else if (localName.equalsIgnoreCase("phone")) {
			if (this.map != null) {
				this.map.put("info", this.buffer.toString().trim());
			}
			this.buffer.setLength(0);
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if (localName.equalsIgnoreCase("contacts")) {
			this.map = new HashMap<String, Object>();
		}
		super.startElement(uri, localName, qName, attributes);
	}
}
