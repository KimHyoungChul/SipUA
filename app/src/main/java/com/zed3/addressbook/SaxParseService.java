package com.zed3.addressbook;

import android.util.Log;

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

public class SaxParseService extends DefaultHandler {
	DataBaseService dbService;
	private boolean isChild;
	private Member mCurrentParseMember;
	private Team mCurrentParseTeam;
	private Team mParentTeam;
	private ArrayList<Member> memberList;
	private String preTag;
	StringBuffer sb;
	private Teams teams;

	public SaxParseService() {
		this.memberList = null;
		this.teams = null;
		this.mCurrentParseTeam = null;
		this.mCurrentParseMember = null;
		this.preTag = null;
		this.isChild = false;
		this.dbService = DataBaseService.getInstance();
		this.sb = new StringBuffer();
	}

	private int calc(int n) {
		int n2 = 0;
		while (true) {
			final int n3 = n;
			if (n3 == 0) {
				break;
			}
			n = n3 / 2;
			if (n3 % 2 == 1) {
				break;
			}
			++n2;
		}
		return n2;
	}

	private String getType(final int n) {
		return String.valueOf(this.calc(n & 0xFFFF)) + "," + this.calc(n >>> 16 & 0xFF);
	}

	@Override
	public void characters(final char[] array, int intValue, final int n) throws SAXException {
		if (this.preTag != null) {
			this.sb.append(array, intValue, n);
			final String string = this.sb.toString();
			System.out.println("----characters-preTag:" + this.preTag + ",content:" + string);
			if ("alversion".equals(this.preTag)) {
				this.teams.setAlversion(string);
				Log.i("xxxxx", "SaxParseService alversion = " + string);
			} else {
				if ("name".equals(this.preTag) && !this.isChild) {
					this.mCurrentParseTeam.setName(string);
					return;
				}
				if ("id".equals(this.preTag) && !this.isChild) {
					this.mCurrentParseTeam.setId(string);
					return;
				}
				if ("number".equals(this.preTag)) {
					this.mCurrentParseMember.setNumber(string);
					return;
				}
				if ("mname".equals(this.preTag)) {
					this.mCurrentParseMember.setmName(string);
					return;
				}
				if ("showflag".equals(this.preTag)) {
					this.mCurrentParseMember.setShowflag(string);
					return;
				}
				if ("subtype".equals(this.preTag)) {
					intValue = Integer.valueOf(string);
					this.mCurrentParseMember.setMtype(this.getType(intValue));
					return;
				}
				if ("dtype".equals(this.preTag)) {
					this.mCurrentParseMember.setDtype(string);
					return;
				}
				if ("sex".equals(this.preTag)) {
					this.mCurrentParseMember.setSex(string);
					return;
				}
				if ("position".equals(this.preTag)) {
					this.mCurrentParseMember.setPosition(string);
					return;
				}
				if ("phone".equals(this.preTag)) {
					this.mCurrentParseMember.setPhone(string);
					return;
				}
				if ("video".equals(this.preTag)) {
					this.mCurrentParseMember.setVideo(string);
					return;
				}
				if ("audio".equals(this.preTag)) {
					this.mCurrentParseMember.setAudio(string);
					return;
				}
				if ("pttmap".equals(this.preTag)) {
					this.mCurrentParseMember.setPttmap(string);
					return;
				}
				if ("gps".equals(this.preTag)) {
					this.mCurrentParseMember.setGps(string);
					return;
				}
				if ("pictureupload".equals(this.preTag)) {
					this.mCurrentParseMember.setPictureupload(string);
					return;
				}
				if ("smsswitch".equals(this.preTag)) {
					this.mCurrentParseMember.setSmsswitch(string);
				}
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		this.dbService.insertAlVersion(this.teams.getAlversion());
		Log.v("huangfujian", "endDocument() " + this.teams.getAlversion());
		super.endDocument();
	}

	@Override
	public void endElement(final String s, final String s2, final String s3) throws SAXException {
		System.out.println("----endElement-qName:" + s3);
		if (!"teams".equals(s3)) {
			if ("team".equals(s3)) {
				if (this.mCurrentParseTeam == null) {
					this.mParentTeam = null;
					return;
				}
				final Team parent = this.mCurrentParseTeam.getParent();
				System.out.println("-----endElement:parentTeam" + parent);
				if (parent != null) {
					Log.i("jiangkai", String.valueOf(s3) + "1  content: " + this.mCurrentParseTeam.getName() + "   id  " + this.mCurrentParseTeam.getId());
					this.teams.addParent(this.mCurrentParseTeam);
					this.mCurrentParseTeam = parent;
				} else {
					Log.i("jiangkai", String.valueOf(s3) + "2  content: " + this.mCurrentParseTeam.getName() + "   id  " + this.mCurrentParseTeam.getId());
					this.teams.addParent(this.mCurrentParseTeam);
					this.mCurrentParseTeam = null;
					this.mParentTeam = null;
				}
			} else if ("member".equals(s3)) {
				if (this.mCurrentParseTeam != null) {
					this.mCurrentParseTeam.addMember(this.mCurrentParseMember);
				}
				this.mCurrentParseMember = null;
			}
		}
		this.preTag = null;
	}

	public ArrayList<Member> getMember(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final SaxParseService dh = new SaxParseService();
		saxParser.parse(is, dh);
		return dh.getMembers();
	}

	public ArrayList<Member> getMembers() {
		return this.memberList;
	}

	public List<Team> getTeam() {
		return this.teams.getParentTeams();
	}

	public List<Team> getTeam(final InputStream is) throws SAXException, IOException, ParserConfigurationException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final SaxParseService dh = new SaxParseService();
		saxParser.parse(is, dh);
		return dh.getTeam();
	}

	public Teams getTeams() {
		return this.teams;
	}

	@Override
	public void startDocument() throws SAXException {
		this.memberList = new ArrayList<Member>();
	}

	@Override
	public void startElement(final String s, final String s2, final String preTag, final Attributes attributes) throws SAXException {
		System.out.println("----startElement-qName:" + preTag);
		this.sb.delete(0, this.sb.length());
		if ("teams".equals(preTag)) {
			this.teams = new Teams();
		} else if ("team".equals(preTag)) {
			final Team mCurrentParseTeam = new Team();
			if (this.mParentTeam == null) {
				this.mParentTeam = mCurrentParseTeam;
				this.mCurrentParseTeam = this.mParentTeam;
				System.out.println("----mParentTeam==null-mCurrentParseTeam" + this.mCurrentParseTeam.toString());
			} else {
				System.out.println("-----mParentTeam" + this.mParentTeam.toString());
				if (this.mCurrentParseTeam != null) {
					mCurrentParseTeam.setParent(this.mCurrentParseTeam);
					this.mCurrentParseTeam = mCurrentParseTeam;
				} else {
					(this.mCurrentParseTeam = mCurrentParseTeam).setParent(this.mParentTeam);
					this.mParentTeam.addTeam(mCurrentParseTeam);
				}
			}
		} else if ("member".equals(preTag)) {
			(this.mCurrentParseMember = new Member()).setParent(this.mCurrentParseTeam);
		}
		this.preTag = preTag;
	}
}
