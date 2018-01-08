package com.zed3.sipua.ui.lowsdk;

import com.zed3.addressbook.DataBaseService;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MsgParser extends DefaultHandler {
	private String code;
	DataBaseService dbService;
	private List<String> msgList;
	private String msgVersion;
	private String preTag;
	StringBuffer sb;

	public MsgParser() {
		this.msgList = null;
		this.dbService = DataBaseService.getInstance();
		this.sb = new StringBuffer();
		this.preTag = null;
		this.msgVersion = null;
		this.code = null;
	}

	@Override
	public void characters(final char[] array, final int n, final int n2) throws SAXException {
		if (this.preTag != null) {
			this.sb.append(array, n, n2);
			final String string = this.sb.toString();
			if ("version".equals(this.preTag)) {
				this.msgVersion = string;
			} else {
				if ("code".equals(this.preTag)) {
					this.code = string;
					return;
				}
				if ("tpl".equals(this.preTag)) {
					this.msgList.add(string);
				}
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		this.dbService.insertMsgVersion(this.msgVersion);
	}

	@Override
	public void endElement(final String s, final String s2, final String s3) throws SAXException {
		if (!"msgtpl".equals(s3) && !"tplist".equals(s3)) {
			"tpl".equals(s3);
		}
		this.preTag = null;
	}

	public String getCode() {
		return this.code;
	}

	public String getCode(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final MsgParser dh = new MsgParser();
		saxParser.parse(is, dh);
		return dh.getCode();
	}

	public List<String> getMsg() {
		return this.msgList;
	}

	public List<String> getMsg(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final MsgParser dh = new MsgParser();
		saxParser.parse(is, dh);
		return dh.getMsg();
	}

	public String getVersion() {
		return this.msgVersion;
	}

	public String getVersion(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final MsgParser dh = new MsgParser();
		saxParser.parse(is, dh);
		return dh.getVersion();
	}

	@Override
	public void startDocument() throws SAXException {
		this.msgList = new ArrayList<String>();
	}

	@Override
	public void startElement(final String s, final String s2, final String preTag, final Attributes attributes) throws SAXException {
		this.sb.delete(0, this.sb.length());
		if (!"msgtpl ".equals(preTag) && !"tpllist".equals(preTag)) {
			"tpl".equals(preTag);
		}
		this.preTag = preTag;
	}
}
