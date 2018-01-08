package com.zed3.sipua;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

public class UserAgentProfile extends Configure {
	public static String contacts_file;
	public static String ua_jar;
	public int accept_time;
	public boolean audio;
	public int[] audio_codecs;
	public int audio_frame_size;
	public int audio_port;
	public int audio_sample_rate;
	public int audio_sample_size;
	public String call_to;
	public String contact_url;
	public boolean do_register;
	public boolean do_unregister;
	public boolean do_unregister_all;
	public int dtmf_avp;
	public int expires;
	public String from_url;
	public boolean gps;
	public int gps_port;
	public int hangup_time;
	public long keepalive_time;
	public boolean mmtel;
	public boolean no_offer;
	public boolean no_prompt;
	public String passwd;
	public boolean pub;
	public String qvalue;
	public int re_invite_time;
	public String realm;
	public String realm_orig;
	public String recv_file;
	public boolean recv_only;
	public String redirect_to;
	public String send_file;
	public boolean send_only;
	public boolean send_tone;
	public int transfer_time;
	public String transfer_to;
	public String username;
	public boolean video;
	public int video_avp;
	public int video_avps;
	public int video_port;

	static {
		UserAgentProfile.ua_jar = "lib/ua.jar";
		UserAgentProfile.contacts_file = "contacts.lst";
	}

	public UserAgentProfile() {
		this.from_url = null;
		this.contact_url = null;
		this.username = null;
		this.realm = null;
		this.realm_orig = null;
		this.passwd = null;
		this.qvalue = null;
		this.mmtel = false;
		this.pub = false;
		this.do_register = false;
		this.do_unregister = false;
		this.do_unregister_all = false;
		this.expires = 60;
		this.keepalive_time = 0L;
		this.call_to = null;
		this.accept_time = -1;
		this.hangup_time = -1;
		this.transfer_time = -1;
		this.re_invite_time = -1;
		this.redirect_to = null;
		this.transfer_to = null;
		this.no_offer = false;
		this.no_prompt = false;
		this.audio = true;
		this.video = true;
		this.recv_only = false;
		this.send_only = false;
		this.send_tone = false;
		this.send_file = null;
		this.recv_file = null;
		this.audio_port = 21000;
		this.audio_codecs = new int[]{3, 8, 0};
		this.dtmf_avp = 101;
		this.audio_sample_rate = 8000;
		this.audio_sample_size = 1;
		this.audio_frame_size = 160;
		this.video_port = 21070;
		this.video_avp = 126;
		this.video_avps = 125;
		this.gps = true;
		this.gps_port = 5678;
		this.init();
	}

	public UserAgentProfile(final String s) {
		this.from_url = null;
		this.contact_url = null;
		this.username = null;
		this.realm = null;
		this.realm_orig = null;
		this.passwd = null;
		this.qvalue = null;
		this.mmtel = false;
		this.pub = false;
		this.do_register = false;
		this.do_unregister = false;
		this.do_unregister_all = false;
		this.expires = 60;
		this.keepalive_time = 0L;
		this.call_to = null;
		this.accept_time = -1;
		this.hangup_time = -1;
		this.transfer_time = -1;
		this.re_invite_time = -1;
		this.redirect_to = null;
		this.transfer_to = null;
		this.no_offer = false;
		this.no_prompt = false;
		this.audio = true;
		this.video = true;
		this.recv_only = false;
		this.send_only = false;
		this.send_tone = false;
		this.send_file = null;
		this.recv_file = null;
		this.audio_port = 21000;
		this.audio_codecs = new int[]{3, 8, 0};
		this.dtmf_avp = 101;
		this.audio_sample_rate = 8000;
		this.audio_sample_size = 1;
		this.audio_frame_size = 160;
		this.video_port = 21070;
		this.video_avp = 126;
		this.video_avps = 125;
		this.gps = true;
		this.gps_port = 5678;
		this.loadFile(s);
		this.init();
	}

	private void init() {
		if (this.realm == null && this.contact_url != null) {
			this.realm = new NameAddress(this.contact_url).getAddress().getHost();
		}
		if (this.username == null) {
			String userName;
			if (this.contact_url != null) {
				userName = new NameAddress(this.contact_url).getAddress().getUserName();
			} else {
				userName = "user";
			}
			this.username = userName;
		}
		if (this.call_to != null && this.call_to.equalsIgnoreCase(Configure.NONE)) {
			this.call_to = null;
		}
		if (this.redirect_to != null && this.redirect_to.equalsIgnoreCase(Configure.NONE)) {
			this.redirect_to = null;
		}
		if (this.transfer_to != null && this.transfer_to.equalsIgnoreCase(Configure.NONE)) {
			this.transfer_to = null;
		}
		if (this.send_file != null && this.send_file.equalsIgnoreCase(Configure.NONE)) {
			this.send_file = null;
		}
		if (this.recv_file != null && this.recv_file.equalsIgnoreCase(Configure.NONE)) {
			this.recv_file = null;
		}
	}

	public void initContactAddress(final SipProvider sipProvider) {
		if (this.contact_url == null) {
			this.contact_url = "sip:" + this.username + "@" + sipProvider.getViaAddress();
			if (sipProvider.getPort() != SipStack.default_port) {
				this.contact_url = String.valueOf(this.contact_url) + ":" + sipProvider.getPort();
			}
			if (!sipProvider.getDefaultTransport().equals("udp")) {
				this.contact_url = String.valueOf(this.contact_url) + ";transport=" + sipProvider.getDefaultTransport();
			}
		}
		if (this.from_url == null) {
			this.from_url = this.contact_url;
		}
	}

	@Override
	protected void parseLine(String s) {
		int accept_time = 0;
		final int index = s.indexOf("=");
		Parser parser;
		if (index > 0) {
			final String trim = s.substring(0, index).trim();
			parser = new Parser(s, index + 1);
			s = trim;
		} else {
			parser = new Parser("");
		}
		if (s.equals("from_url")) {
			this.from_url = parser.getRemainingString().trim();
		} else {
			if (s.equals("contact_url")) {
				this.contact_url = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("username")) {
				this.username = parser.getString();
				return;
			}
			if (s.equals("realm")) {
				this.realm = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("passwd")) {
				this.passwd = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("ua_jar")) {
				UserAgentProfile.ua_jar = parser.getStringUnquoted();
				return;
			}
			if (s.equals("contacts_file")) {
				UserAgentProfile.contacts_file = parser.getStringUnquoted();
				return;
			}
			if (s.equals("do_register")) {
				this.do_register = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("do_unregister")) {
				this.do_unregister = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("do_unregister_all")) {
				this.do_unregister_all = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("expires")) {
				this.expires = parser.getInt();
				return;
			}
			if (s.equals("keepalive_time")) {
				this.keepalive_time = parser.getInt();
				return;
			}
			if (s.equals("call_to")) {
				this.call_to = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("accept_time")) {
				this.accept_time = parser.getInt();
				return;
			}
			if (s.equals("hangup_time")) {
				this.hangup_time = parser.getInt();
				return;
			}
			if (s.equals("transfer_time")) {
				this.transfer_time = parser.getInt();
				return;
			}
			if (s.equals("re_invite_time")) {
				this.re_invite_time = parser.getInt();
				return;
			}
			if (s.equals("redirect_to")) {
				this.redirect_to = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("transfer_to")) {
				this.transfer_to = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("no_offer")) {
				this.no_offer = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("no_prompt")) {
				this.no_prompt = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("audio")) {
				this.audio = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("video")) {
				this.video = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("recv_only")) {
				this.recv_only = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("send_only")) {
				this.send_only = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("send_tone")) {
				this.send_tone = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("send_file")) {
				this.send_file = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("recv_file")) {
				this.recv_file = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("audio_port")) {
				this.audio_port = parser.getInt();
				return;
			}
			if (s.equals("audio_sample_rate")) {
				this.audio_sample_rate = parser.getInt();
				return;
			}
			if (s.equals("audio_sample_size")) {
				this.audio_sample_size = parser.getInt();
				return;
			}
			if (s.equals("audio_frame_size")) {
				this.audio_frame_size = parser.getInt();
				return;
			}
			if (s.equals("video_port")) {
				this.video_port = parser.getInt();
				return;
			}
			if (s.equals("video_avp")) {
				this.video_avp = parser.getInt();
				return;
			}
			if (s.equals("contact_user")) {
				this.username = parser.getString();
				return;
			}
			if (s.equals("auto_accept")) {
				if (!parser.getString().toLowerCase().startsWith("y")) {
					accept_time = -1;
				}
				this.accept_time = accept_time;
			}
		}
	}

	@Override
	protected String toLines() {
		return this.contact_url;
	}
}
