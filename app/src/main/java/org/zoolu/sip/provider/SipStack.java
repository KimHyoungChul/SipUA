package org.zoolu.sip.provider;

import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;
import org.zoolu.tools.Timer;

public class SipStack extends Configure {
	public static int LOG_LEVEL_CALL = 0;
	public static int LOG_LEVEL_DIALOG = 0;
	public static int LOG_LEVEL_TRANSACTION = 0;
	public static int LOG_LEVEL_TRANSPORT = 0;
	public static int LOG_LEVEL_UA = 0;
	public static final String authors = "Luca Veltri - University of Parma (Italy)";
	public static long clearing_timeout = 0L;
	public static int debug_level = 0;
	public static int default_expires = 0;
	public static int default_nmax_connections = 0;
	public static int default_port = 0;
	public static String[] default_transport_protocols;
	public static boolean early_dialog = false;
	public static boolean force_rport = false;
	public static final int heartBeatCircle = 30;
	public static long invite_transaction_timeout = 0L;
	private static boolean is_init = false;
	public static String log_path;
	private static String log_rotation_time;
	public static int log_rotations = 0;
	public static int max_forwards = 0;
	public static int max_logsize = 0;
	public static long max_retransmission_timeout = 0L;
	public static final String release = "mjsip stack 1.6";
	public static long retransmission_timeout;
	public static int rotation_scale;
	public static int rotation_time;
	public static String server_info;
	public static boolean single_timer;
	public static long transaction_timeout;
	public static String ua_info;
	public static boolean use_rport;

	static {
		SipStack.is_init = false;
		SipStack.default_port = 7080;
		SipStack.default_transport_protocols = new String[]{"udp"};
		SipStack.default_nmax_connections = 32;
		SipStack.use_rport = true;
		SipStack.force_rport = false;
		SipStack.max_forwards = 70;
		SipStack.retransmission_timeout = 1000L;
		SipStack.max_retransmission_timeout = 4000L;
		SipStack.transaction_timeout = 8000L;
		SipStack.clearing_timeout = 5000L;
		SipStack.single_timer = false;
		SipStack.invite_transaction_timeout = 200000L;
		SipStack.early_dialog = false;
		SipStack.default_expires = 1800;
		SipStack.ua_info = "mjsip stack 1.6";
		SipStack.server_info = "mjsip stack 1.6";
		SipStack.LOG_LEVEL_TRANSPORT = 1;
		SipStack.LOG_LEVEL_TRANSACTION = 2;
		SipStack.LOG_LEVEL_DIALOG = 2;
		SipStack.LOG_LEVEL_CALL = 1;
		SipStack.LOG_LEVEL_UA = 0;
		SipStack.debug_level = 1;
		SipStack.log_path = "log";
		SipStack.max_logsize = 2048;
		SipStack.log_rotations = 0;
		SipStack.log_rotation_time = null;
		SipStack.rotation_scale = 2;
		SipStack.rotation_time = 2;
	}

	public static void init() {
		init(null);
	}

	public static void init(String string) {
		new SipStack().loadFile(string);
		if (SipStack.ua_info != null && (SipStack.ua_info.length() == 0 || SipStack.ua_info.equalsIgnoreCase(Configure.NONE) || SipStack.ua_info.equalsIgnoreCase("NO-UA-INFO"))) {
			SipStack.ua_info = null;
		}
		if (SipStack.server_info != null && (SipStack.server_info.length() == 0 || SipStack.server_info.equalsIgnoreCase(Configure.NONE) || SipStack.server_info.equalsIgnoreCase("NO-SERVER-INFO"))) {
			SipStack.server_info = null;
		}
		Timer.SINGLE_THREAD = SipStack.single_timer;
		if (SipStack.debug_level > 0 && SipStack.log_rotation_time != null) {
			final SipParser sipParser = new SipParser(SipStack.log_rotation_time);
			SipStack.rotation_time = sipParser.getInt();
			if ((string = sipParser.getString()) == null) {
				string = "null";
			}
			if (string.toUpperCase().startsWith("MONTH")) {
				SipStack.rotation_scale = 2;
			} else if (string.toUpperCase().startsWith("DAY")) {
				SipStack.rotation_scale = 5;
			} else if (string.toUpperCase().startsWith("HOUR")) {
				SipStack.rotation_scale = 10;
			} else if (string.toUpperCase().startsWith("MINUTE")) {
				SipStack.rotation_scale = 12;
			} else {
				SipStack.rotation_time = 7;
				SipStack.rotation_scale = 5;
				printLog("Error with the log rotation time. Logs will rotate every week.");
			}
		}
		SipStack.is_init = true;
	}

	public static boolean isInit() {
		return SipStack.is_init;
	}

	private static void printLog(final String s) {
		System.out.println("SipStack: time=" + System.currentTimeMillis() + "..." + s);
	}

	@Override
	protected void parseLine(String s) {
		final int index = s.indexOf("=");
		Parser parser;
		if (index > 0) {
			final String trim = s.substring(0, index).trim();
			parser = new Parser(s, index + 1);
			s = trim;
		} else {
			parser = new Parser("");
		}
		if (s.equals("default_port")) {
			SipStack.default_port = parser.getInt();
		} else {
			if (s.equals("default_transport_protocols")) {
				SipStack.default_transport_protocols = parser.getWordArray(new char[]{' ', ','});
				return;
			}
			if (s.equals("default_nmax_connections")) {
				SipStack.default_nmax_connections = parser.getInt();
				return;
			}
			if (s.equals("use_rport")) {
				SipStack.use_rport = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("force_rport")) {
				SipStack.force_rport = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("max_forwards")) {
				SipStack.max_forwards = parser.getInt();
				return;
			}
			if (s.equals("retransmission_timeout")) {
				SipStack.retransmission_timeout = parser.getInt();
				return;
			}
			if (s.equals("max_retransmission_timeout")) {
				SipStack.max_retransmission_timeout = parser.getInt();
				return;
			}
			if (s.equals("transaction_timeout")) {
				SipStack.transaction_timeout = parser.getInt();
				return;
			}
			if (s.equals("clearing_timeout")) {
				SipStack.clearing_timeout = parser.getInt();
				return;
			}
			if (s.equals("single_timer")) {
				SipStack.single_timer = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("early_dialog")) {
				SipStack.early_dialog = parser.getString().toLowerCase().startsWith("y");
				return;
			}
			if (s.equals("default_expires")) {
				SipStack.default_expires = parser.getInt();
				return;
			}
			if (s.equals("ua_info")) {
				SipStack.ua_info = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("server_info")) {
				SipStack.server_info = parser.getRemainingString().trim();
				return;
			}
			if (s.equals("debug_level")) {
				SipStack.debug_level = parser.getInt();
				return;
			}
			if (s.equals("log_path")) {
				SipStack.log_path = parser.getString();
				return;
			}
			if (s.equals("max_logsize")) {
				SipStack.max_logsize = parser.getInt();
				return;
			}
			if (s.equals("log_rotations")) {
				SipStack.log_rotations = parser.getInt();
				return;
			}
			if (s.equals("log_rotation_time")) {
				SipStack.log_rotation_time = parser.getRemainingString();
				return;
			}
			if (s.equals("host_addr")) {
				printLog("WARNING: parameter 'host_addr' is no more supported; use 'via_addr' instead.");
			}
			if (s.equals("all_interfaces")) {
				printLog("WARNING: parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
			}
			if (s.equals("use_outbound")) {
				printLog("WARNING: parameter 'use_outbound' is no more supported; use 'outbound_addr' for setting an outbound proxy or let it undefined.");
			}
			if (s.equals("log_file")) {
				printLog("WARNING: parameter 'log_file' is no more supported.");
			}
		}
	}

	@Override
	protected String toLines() {
		return "SipStack/mjsip stack 1.6";
	}
}
