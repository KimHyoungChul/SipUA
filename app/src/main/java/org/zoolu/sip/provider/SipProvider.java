package org.zoolu.sip.provider;

import android.content.Context;
import android.os.PowerManager;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.IHeartBeatListener;

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.TcpServer;
import org.zoolu.net.TcpServerListener;
import org.zoolu.net.TcpSocket;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.tools.Configurable;
import org.zoolu.tools.Configure;
import org.zoolu.tools.Log;
import org.zoolu.tools.Parser;
import org.zoolu.tools.Random;
import org.zoolu.tools.RotatingLog;
import org.zoolu.tools.SimpleDigest;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

public class SipProvider implements Configurable, TransportListener, TcpServerListener {
	public static final String ALL_INTERFACES = "ALL-INTERFACES";
	public static final Identifier ANY;
	public static final String AUTO_CONFIGURATION = "AUTO-CONFIGURATION";
	private static final int MIN_MESSAGE_LENGTH = 12;
	public static final Identifier PROMISQUE;
	public static final String PROTO_SCTP = "sctp";
	public static final String PROTO_TCP = "tcp";
	public static final String PROTO_TLS = "tls";
	public static final String PROTO_UDP = "udp";
	Hashtable<ConnectionIdentifier, ConnectedTransport> connections;
	String default_transport;
	protected Log event_log;
	HashSet exception_listeners;
	boolean force_rport;
	private IHeartBeatListener heartBeatListener;
	String host_ifaddr;
	IpAddress host_ipaddr;
	int host_port;
	Vector<SipProviderListener> invite_listeners;
	Hashtable<Identifier, SipProviderListener> listeners;
	boolean log_all_packets;
	protected Log message_log;
	int nmax_connections;
	private String outbound_addr;
	private int outbound_port;
	SocketAddress outbound_proxy;
	PowerManager pm;
	boolean rport;
	TcpServer tcp_server;
	String[] transport_protocols;
	boolean transport_sctp;
	boolean transport_tcp;
	boolean transport_tls;
	boolean transport_udp;
	UdpTransport udp;
	String via_addr;
	PowerManager.WakeLock wl;

	static {
		ANY = new Identifier("ANY");
		PROMISQUE = new Identifier("PROMISQUE");
	}

	public SipProvider(final String s) {
		this.via_addr = null;
		this.host_port = 0;
		this.host_ifaddr = null;
		this.transport_protocols = null;
		this.nmax_connections = 0;
		this.outbound_proxy = null;
		this.log_all_packets = false;
		this.outbound_addr = null;
		this.outbound_port = -1;
		this.event_log = null;
		this.message_log = null;
		this.host_ipaddr = null;
		this.default_transport = null;
		this.transport_udp = false;
		this.transport_tcp = false;
		this.transport_tls = false;
		this.transport_sctp = false;
		this.rport = true;
		this.force_rport = false;
		this.listeners = null;
		this.invite_listeners = null;
		this.exception_listeners = null;
		this.udp = null;
		this.tcp_server = null;
		this.connections = null;
		this.heartBeatListener = null;
		if (!SipStack.isInit()) {
			SipStack.init(s);
		}
		new Configure(this, s);
		this.init(this.via_addr, this.host_port, this.transport_protocols, this.host_ifaddr);
		this.initlog();
		this.startTrasport();
	}

	public SipProvider(final String s, final int n) {
		this.via_addr = null;
		this.host_port = 0;
		this.host_ifaddr = null;
		this.transport_protocols = null;
		this.nmax_connections = 0;
		this.outbound_proxy = null;
		this.log_all_packets = false;
		this.outbound_addr = null;
		this.outbound_port = -1;
		this.event_log = null;
		this.message_log = null;
		this.host_ipaddr = null;
		this.default_transport = null;
		this.transport_udp = false;
		this.transport_tcp = false;
		this.transport_tls = false;
		this.transport_sctp = false;
		this.rport = true;
		this.force_rport = false;
		this.listeners = null;
		this.invite_listeners = null;
		this.exception_listeners = null;
		this.udp = null;
		this.tcp_server = null;
		this.connections = null;
		this.heartBeatListener = null;
		this.init(s, n, null, null);
		this.initlog();
		this.startTrasport();
	}

	public SipProvider(final String s, final int n, final String[] array, final String s2) {
		this.via_addr = null;
		this.host_port = 0;
		this.host_ifaddr = null;
		this.transport_protocols = null;
		this.nmax_connections = 0;
		this.outbound_proxy = null;
		this.log_all_packets = false;
		this.outbound_addr = null;
		this.outbound_port = -1;
		this.event_log = null;
		this.message_log = null;
		this.host_ipaddr = null;
		this.default_transport = null;
		this.transport_udp = false;
		this.transport_tcp = false;
		this.transport_tls = false;
		this.transport_sctp = false;
		this.rport = true;
		this.force_rport = false;
		this.listeners = null;
		this.invite_listeners = null;
		this.exception_listeners = null;
		this.udp = null;
		this.tcp_server = null;
		this.connections = null;
		this.heartBeatListener = null;
		this.init(s, n, array, s2);
		this.initlog();
		this.startTrasport();
	}

	private void addConnection(ConnectedTransport connectedTransport) {
		final ConnectionIdentifier connectionIdentifier = new ConnectionIdentifier(connectedTransport);
		if (this.connections.containsKey(connectionIdentifier)) {
			this.printLog("trying to add the already established connection " + connectionIdentifier, 1);
			this.printLog("connection " + connectionIdentifier + " will be replaced", 1);
			this.connections.get(connectionIdentifier).halt();
			this.connections.remove(connectionIdentifier);
		} else if (this.connections.size() >= this.nmax_connections) {
			this.printLog("reached the maximum number of connection: removing the older unused connection", 1);
			final long currentTimeMillis = System.currentTimeMillis();
			ConnectionIdentifier connectionIdentifier2 = null;
			final Enumeration<ConnectedTransport> elements = this.connections.elements();
			while (elements.hasMoreElements()) {
				final ConnectedTransport connectedTransport2 = elements.nextElement();
				if (connectedTransport2.getLastTimeMillis() < currentTimeMillis) {
					connectionIdentifier2 = new ConnectionIdentifier(connectedTransport2);
				}
			}
			if (connectionIdentifier2 != null) {
				this.removeConnection(connectionIdentifier2);
			}
		}
		this.connections.put(connectionIdentifier, connectedTransport);
		connectedTransport = this.connections.get(new ConnectionIdentifier(connectedTransport));
		this.printLog("active connenctions:", 5);
		final Enumeration<ConnectionIdentifier> keys = this.connections.keys();
		while (keys.hasMoreElements()) {
			final ConnectionIdentifier connectionIdentifier3 = keys.nextElement();
			this.printLog("conn-id=" + connectionIdentifier3 + ": " + this.connections.get(connectionIdentifier3).toString(), 5);
		}
	}

	private SipURL completeSipURL(String s) {
		if (!s.startsWith("sip:") && s.indexOf("@") < 0 && s.indexOf(".") < 0 && s.indexOf(":") < 0) {
			s = "sip:" + s + "@";
			if (this.outbound_proxy != null) {
				final String string = String.valueOf(s) + this.outbound_proxy.getAddress().toString();
				final int port = this.outbound_proxy.getPort();
				s = string;
				if (port > 0) {
					s = string;
					if (port != SipStack.default_port) {
						s = String.valueOf(string) + ":" + port;
					}
				}
			} else {
				final String s2 = s = String.valueOf(s) + this.getViaAddress();
				if (this.host_port > 0) {
					s = s2;
					if (this.host_port != SipStack.default_port) {
						s = String.valueOf(s2) + ":" + this.host_port;
					}
				}
			}
			return new SipURL(s);
		}
		return new SipURL(s);
	}

	private void init(final String via_addr, int host_port, final String[] transport_protocols, final String s) {
		if (!SipStack.isInit()) {
			SipStack.init();
		}
		this.via_addr = via_addr;
		if (this.via_addr == null || this.via_addr.equalsIgnoreCase("AUTO-CONFIGURATION")) {
			this.via_addr = IpAddress.localIpAddress;
		}
		this.host_port = host_port;
		if (this.host_port < 0) {
			this.host_port = SipStack.default_port;
		}
		this.host_ipaddr = null;
		Label_0088:
		while (true) {
			if (s == null || s.equalsIgnoreCase("ALL-INTERFACES")) {
				break Label_0088;
			}
			Label_0194_Outer:
			while (true) {
				while (true) {
					Label_0333:
					while (true) {
						try {
							this.host_ipaddr = IpAddress.getByName(s);
							this.transport_protocols = transport_protocols;
							if (this.transport_protocols == null) {
								this.transport_protocols = SipStack.default_transport_protocols;
							}
							this.default_transport = this.transport_protocols[0];
							host_port = 0;
							if (host_port >= this.transport_protocols.length) {
								if (this.nmax_connections <= 0) {
									this.nmax_connections = SipStack.default_nmax_connections;
								}
								if (this.outbound_port < 0) {
									this.outbound_port = SipStack.default_port;
								}
								if (this.outbound_addr != null) {
									if (!this.outbound_addr.equalsIgnoreCase(Configure.NONE) && !this.outbound_addr.equalsIgnoreCase("NO-OUTBOUND")) {
										break Label_0333;
									}
									this.outbound_proxy = null;
								}
								this.rport = SipStack.use_rport;
								this.force_rport = SipStack.force_rport;
								this.exception_listeners = new HashSet();
								this.listeners = new Hashtable<Identifier, SipProviderListener>(10);
								this.invite_listeners = new Vector<SipProviderListener>();
								this.connections = new Hashtable<ConnectionIdentifier, ConnectedTransport>(10);
								return;
							}
						} catch (IOException ex) {
							ex.printStackTrace();
							this.host_ipaddr = null;
							continue Label_0088;
						}
						this.transport_protocols[host_port] = this.transport_protocols[host_port].toLowerCase();
						if (this.transport_protocols[host_port].equals("udp")) {
							this.transport_udp = true;
						} else if (this.transport_protocols[host_port].equals("tcp")) {
							this.transport_tcp = true;
						}
						++host_port;
						continue Label_0194_Outer;
					}
					this.outbound_proxy = new SocketAddress(this.outbound_addr, this.outbound_port);
					continue;
				}
			}
		}
	}

	private void initlog() {
		if (SipStack.debug_level > 0) {
			final String string = String.valueOf(SipStack.log_path) + "//" + this.via_addr + "." + this.host_port;
			this.event_log = new RotatingLog(String.valueOf(string) + "_events.log", null, SipStack.debug_level, SipStack.max_logsize * 1024, SipStack.log_rotations, SipStack.rotation_scale, SipStack.rotation_time);
			this.message_log = new RotatingLog(String.valueOf(string) + "_messages.log", null, SipStack.debug_level, SipStack.max_logsize * 1024, SipStack.log_rotations, SipStack.rotation_scale, SipStack.rotation_time);
		}
		this.printLog("SipStack: mjsip stack 1.6", 1);
		this.printLog("new SipProvider(): " + this.toString(), 1);
	}

	public static String pickBranch() {
		return "z9hG4bK" + Random.nextNumString(5);
	}

	public static int pickInitialCSeq() {
		return 1;
	}

	public static String pickTag() {
		return "z9hG4bK" + Random.nextNumString(8);
	}

	public static String pickTag(final Message message) {
		return new SimpleDigest(8, message.toString()).asHex();
	}

	private final void printException(final Exception ex, final int n) {
		if (this.event_log != null) {
			this.event_log.printException(ex, SipStack.LOG_LEVEL_TRANSPORT + n);
		}
	}

	private final void printHeartBeatMessageLog(String string, final int n, final int n2, final String s, final String s2) {
		string = String.valueOf(s2) + "\r\n" + string + ":" + n + "/" + " (" + n2 + " bytes)";
		if (s != null) {
			new StringBuilder(String.valueOf(string)).append(": \r\n").append(s.toString()).append("\r\n").toString();
		}
	}

	private final void printLog(final String s, final int n) {
	}

	private final void printMessageLog(String string, final String s, final int n, final int n2, final Message message, final String s2) {
		string = String.valueOf(s2) + "\r\n" + s + ":" + n + "/" + string + " (" + n2 + " bytes)";
		if (message != null) {
			new StringBuilder(String.valueOf(string)).append(": \r\n").append(message.toString()).append("\r\n").toString();
		}
	}

	private final void printWarning(final String s, final int n) {
		this.printLog("WARNING: " + s, n);
	}

	private void processHeartBeatMessage(final String s) {
		if (this.heartBeatListener != null) {
			this.printHeartBeatMessageLog("", 0, s.length(), s, "receive");
			this.heartBeatListener.onReceiveHeatBeatMsg(s);
		}
	}

	private void removeConnection(final ConnectionIdentifier connectionIdentifier) {
		if (this.connections != null && this.connections.containsKey(connectionIdentifier)) {
			final ConnectedTransport connectedTransport = this.connections.get(connectionIdentifier);
			if (connectedTransport != null) {
				connectedTransport.halt();
			}
			this.connections.remove(connectionIdentifier);
			this.printLog("active connenctions:", 5);
			final Enumeration<ConnectedTransport> elements = this.connections.elements();
			while (elements.hasMoreElements()) {
				this.printLog("conn " + elements.nextElement().toString(), 5);
			}
		}
	}

	private ConnectionIdentifier sendMessage(final Message message, final String s, final IpAddress ipAddress, final int n, final int n2) {
		final ConnectionIdentifier connectionIdentifier = new ConnectionIdentifier(s, ipAddress, n);
		if (this.log_all_packets || message.getLength() > 12) {
			this.printLog("Sending message to " + connectionIdentifier, 3);
		}
		while (true) {
			Label_0278_Outer:
			while (true) {
				Label_0113:
				{
					if (!this.transport_udp || !s.equals("udp")) {
						break Label_0113;
					}
					final ConnectionIdentifier connectionIdentifier2 = null;
					try {
						this.udp.sendMessage(message, ipAddress, n);
						this.printMessageLog(s, ipAddress.toString(), n, message.getLength(), message, "sent");
						return connectionIdentifier2;
					} catch (IOException ex) {
						this.printException(ex, 1);
						return null;
					}
				}
				if (!this.transport_tcp || !s.equals("tcp")) {
					this.printWarning("Unsupported protocol (" + s + "): Message discarded", 1);
					return null;
				}
				if (this.connections == null || !this.connections.containsKey(connectionIdentifier)) {
					this.printLog("no active connection found matching " + connectionIdentifier, 3);
					this.printLog("open " + s + " connection to " + ipAddress + ":" + n, 3);
					this.printLog("ERROR: conn " + connectionIdentifier + " not found: abort.", 3);
					return null;
				}
			}
		}
	}

	private void startTrasport() {
		// TODO
	}

	private void stopTrasport() {
		if (this.udp != null) {
			this.printLog("udp is going down", 9);
			this.udp.halt();
			this.udp = null;
		}
		if (this.tcp_server != null) {
			this.printLog("tcp is going down", 9);
			this.tcp_server.halt();
			this.tcp_server = null;
		}
		this.haltConnections();
		this.connections = null;
	}

	private String transportProtocolsToString() {
		String string = this.transport_protocols[0];
		for (int i = 1; i < this.transport_protocols.length; ++i) {
			string = String.valueOf(string) + "/" + this.transport_protocols[i];
		}
		return string;
	}

	public boolean addSipProviderExceptionListener(final SipProviderExceptionListener sipProviderExceptionListener) {
		this.printLog("adding SipProviderExceptionListener", 3);
		if (this.exception_listeners.contains(sipProviderExceptionListener)) {
			this.printWarning("trying to add an already present SipProviderExceptionListener.", 1);
			return false;
		}
		this.exception_listeners.add(sipProviderExceptionListener);
		return true;
	}

	public void addSipProviderInviteListener(final SipProviderListener sipProviderListener) {
		this.invite_listeners.add(sipProviderListener);
	}

	public boolean addSipProviderListener(final Identifier identifier, final SipProviderListener sipProviderListener) {
		this.printLog("adding SipProviderListener: " + identifier, 3);
		if (this.listeners.containsKey(identifier)) {
			this.printWarning("trying to add a SipProviderListener with a id that is already in use.", 1);
			return false;
		}
		this.listeners.put(identifier, sipProviderListener);
		return true;
	}

	public boolean addSipProviderListener(final SipProviderListener sipProviderListener) {
		return this.addSipProviderListener(SipProvider.ANY, sipProviderListener);
	}

	public boolean addSipProviderPromisqueListener(final SipProviderListener sipProviderListener) {
		return this.addSipProviderListener(SipProvider.PROMISQUE, sipProviderListener);
	}

	public NameAddress completeNameAddress(final String s) {
		if (s.indexOf("<sip:") >= 0) {
			return new NameAddress(s);
		}
		return new NameAddress(this.completeSipURL(s));
	}

	public String getDefaultTransport() {
		return this.default_transport;
	}

	public IpAddress getInterfaceAddress() {
		return this.host_ipaddr;
	}

	public Hashtable<Identifier, SipProviderListener> getListeners() {
		return this.listeners;
	}

	public Log getLog() {
		return this.event_log;
	}

	public int getNMaxConnections() {
		return this.nmax_connections;
	}

	public SocketAddress getOutboundProxy() {
		return this.outbound_proxy;
	}

	public int getPort() {
		return this.host_port;
	}

	public String[] getTransportProtocols() {
		return this.transport_protocols;
	}

	public String getViaAddress() {
		return this.via_addr = IpAddress.localIpAddress;
	}

	public void halt() {
		this.printLog("halt: SipProvider is going down", 3);
		this.stopTrasport();
		this.listeners = new Hashtable<Identifier, SipProviderListener>(10);
		this.exception_listeners = new HashSet();
	}

	public void haltConnections() {
		if (this.connections != null) {
			this.printLog("connections are going down", 9);
			final Enumeration<ConnectedTransport> elements = this.connections.elements();
			while (elements.hasMoreElements()) {
				elements.nextElement().halt();
			}
			this.connections = new Hashtable<ConnectionIdentifier, ConnectedTransport>(10);
		}
	}

	public boolean hasOutboundProxy() {
		return this.outbound_proxy != null;
	}

	public boolean isAllInterfaces() {
		return this.host_ipaddr == null;
	}

	public boolean isForceRportSet() {
		return this.force_rport;
	}

	public boolean isRportSet() {
		return this.rport;
	}

	@Override
	public void onIncomingConnection(final TcpServer tcpServer, final TcpSocket tcpSocket) {
		this.printLog("incoming connection from " + tcpSocket.getAddress() + ":" + tcpSocket.getPort(), 3);
		final TcpTransport tcpTransport = new TcpTransport(tcpSocket, this);
		this.printLog("tcp connection " + tcpTransport + " opened", 3);
		this.addConnection(tcpTransport);
	}

	@Override
	public void onReceivedMessage(final Transport transport, final Message message) {
		if (this.pm == null) {
			this.pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
			this.wl = this.pm.newWakeLock(1, "Sipdroid.SipProvider");
		}
		this.wl.acquire();
		this.processReceivedMessage(message);
		this.wl.release();
	}

	@Override
	public void onServerTerminated(final TcpServer tcpServer, final Exception ex) {
		this.printLog("tcp server " + tcpServer + " terminated", 3);
	}

	@Override
	public void onTransportTerminated(final Transport transport, final Exception ex) {
		this.printLog("transport " + transport + " terminated", 3);
		if (transport.getProtocol().equals("tcp")) {
			this.removeConnection(new ConnectionIdentifier((ConnectedTransport) transport));
			if (SipUAApp.on(Receiver.mContext)) {
				Receiver.engine(Receiver.mContext).register(true);
			}
		}
		if (ex != null) {
			this.printException(ex, 1);
		}
	}

	@Override
	public void parseLine(String string) {
		final int index = string.indexOf("=");
		Parser parser;
		if (index > 0) {
			final String trim = string.substring(0, index).trim();
			parser = new Parser(string, index + 1);
			string = trim;
		} else {
			parser = new Parser("");
		}
		if (string.equals("via_addr")) {
			this.via_addr = parser.getString();
		} else {
			if (string.equals("host_port")) {
				this.host_port = parser.getInt();
				return;
			}
			if (string.equals("host_ifaddr")) {
				this.host_ifaddr = parser.getString();
				return;
			}
			if (string.equals("transport_protocols")) {
				this.transport_protocols = parser.getWordArray(new char[]{' ', ','});
				return;
			}
			if (string.equals("nmax_connections")) {
				this.nmax_connections = parser.getInt();
				return;
			}
			if (string.equals("outbound_proxy")) {
				string = parser.getString();
				if (string == null || string.length() == 0 || string.equalsIgnoreCase(Configure.NONE) || string.equalsIgnoreCase("NO-OUTBOUND")) {
					this.outbound_proxy = null;
					return;
				}
				this.outbound_proxy = new SocketAddress(string);
			} else {
				if (string.equals("log_all_packets")) {
					this.log_all_packets = parser.getString().toLowerCase().startsWith("y");
					return;
				}
				if (string.equals("host_addr")) {
					System.err.println("WARNING: parameter 'host_addr' is no more supported; use 'via_addr' instead.");
				}
				if (string.equals("all_interfaces")) {
					System.err.println("WARNING: parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
				}
				if (string.equals("use_outbound")) {
					System.err.println("WARNING: parameter 'use_outbound' is no more supported; use 'outbound_proxy' for setting an outbound proxy or let it undefined.");
				}
				if (string.equals("outbound_addr")) {
					System.err.println("WARNING: parameter 'outbound_addr' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
					this.outbound_addr = parser.getString();
					return;
				}
				if (string.equals("outbound_port")) {
					System.err.println("WARNING: parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
					this.outbound_port = parser.getInt();
				}
			}
		}
	}

	public String pickBranch(final Message message) {
		final StringBuffer sb = new StringBuffer();
		sb.append(message.getRequestLine().getAddress().toString());
		sb.append(String.valueOf(this.getViaAddress()) + this.getPort());
		final ViaHeader viaHeader = message.getViaHeader();
		if (viaHeader.hasBranch()) {
			sb.append(viaHeader.getBranch());
		} else {
			sb.append(String.valueOf(viaHeader.getHost()) + viaHeader.getPort());
			sb.append(message.getCSeqHeader().getSequenceNumber());
			sb.append(message.getCallIdHeader().getCallId());
			sb.append(message.getFromHeader().getTag());
			sb.append(message.getToHeader().getTag());
		}
		return "z9hG4bK" + new SimpleDigest(5, sb.toString()).asHex();
	}

	public String pickCallId() {
		return Random.nextNumString(12);
	}

	protected void processReceivedMessage(final Message message) {
		// TODO
	}

	public boolean removeSipProviderExceptionListener(final SipProviderExceptionListener sipProviderExceptionListener) {
		this.printLog("removing SipProviderExceptionListener", 3);
		if (!this.exception_listeners.contains(sipProviderExceptionListener)) {
			this.printWarning("trying to remove a missed SipProviderExceptionListener.", 1);
			return false;
		}
		this.exception_listeners.remove(sipProviderExceptionListener);
		return true;
	}

	public void removeSipProviderInviteListener(final SipProviderListener sipProviderListener) {
		this.invite_listeners.remove(sipProviderListener);
	}

	public boolean removeSipProviderListener(final Identifier identifier) {
		this.printLog("removing SipProviderListener: " + identifier, 3);
		boolean b;
		if (!this.listeners.containsKey(identifier)) {
			this.printWarning("trying to remove a missed SipProviderListener.", 1);
			b = false;
		} else {
			this.listeners.remove(identifier);
			b = true;
		}
		if (this.listeners != null) {
			String string = "";
			final Enumeration<Identifier> keys = this.listeners.keys();
			while (keys.hasMoreElements()) {
				string = String.valueOf(string) + keys.nextElement() + ", ";
			}
			this.printLog(String.valueOf(this.listeners.size()) + " listeners: " + string, 5);
		}
		return b;
	}

	public ConnectionIdentifier sendMessage(final Message message) {
		this.printLog("Sending message:\r\n" + message.toString(), 9);
		final ViaHeader viaHeader = message.getViaHeader();
		String s;
		if (viaHeader != null) {
			s = viaHeader.getProtocol().toLowerCase();
		} else {
			s = this.getDefaultTransport().toLowerCase();
		}
		this.printLog("using transport " + s, 3);
		final int n = 0;
		int ttl = 0;
		final int n2 = 0;
		String maddr;
		int n3;
		int n4;
		if (!message.isResponse()) {
			if (this.outbound_proxy != null) {
				maddr = this.outbound_proxy.getAddress().toString();
				n3 = this.outbound_proxy.getPort();
				n4 = n2;
			} else if (message.hasRouteHeader() && message.getRouteHeader().getNameAddress().getAddress().hasLr()) {
				final SipURL address = message.getRouteHeader().getNameAddress().getAddress();
				maddr = address.getHost();
				n3 = address.getPort();
				n4 = n2;
			} else {
				final SipURL address2 = message.getRequestLine().getAddress();
				maddr = address2.getHost();
				n3 = address2.getPort();
				n4 = n2;
				if (address2.hasMaddr()) {
					maddr = address2.getMaddr();
					if (address2.hasTtl()) {
						ttl = address2.getTtl();
					}
					viaHeader.setMaddr(maddr);
					if (ttl > 0) {
						viaHeader.setTtl(ttl);
					}
					message.removeViaHeader();
					message.addViaHeader(viaHeader);
					n3 = n3;
					n4 = ttl;
				}
			}
		} else {
			final SipURL sipURL = viaHeader.getSipURL();
			String s2;
			if (viaHeader.hasReceived()) {
				s2 = viaHeader.getReceived();
			} else {
				s2 = sipURL.getHost();
			}
			int rport = n;
			if (viaHeader.hasRport()) {
				rport = viaHeader.getRport();
			}
			maddr = s2;
			n3 = rport;
			n4 = n2;
			if (rport <= 0) {
				n3 = sipURL.getPort();
				maddr = s2;
				n4 = n2;
			}
		}
		int default_port;
		if ((default_port = n3) <= 0) {
			default_port = SipStack.default_port;
		}
		return this.sendMessage(message, s, maddr, default_port, n4);
	}

	public ConnectionIdentifier sendMessage(final Message message, final String s, final String s2, final int n, final int n2) {
		if (this.log_all_packets || message.getLength() > 12) {
			this.printLog("Resolving host address '" + s2 + "'", 3);
		}
		try {
			return this.sendMessage(message, s, IpAddress.getByName(s2), n, n2);
		} catch (Exception ex) {
			this.printException(ex, 1);
			return null;
		}
	}

	public ConnectionIdentifier sendMessage(final Message message, final ConnectionIdentifier connectionIdentifier) {
		if (this.log_all_packets || message.getLength() > 12) {
			this.printLog("Sending message through conn " + connectionIdentifier, 1);
		}
		this.printLog("message:\r\n" + message.toString(), 9);
		if (connectionIdentifier != null && this.connections.containsKey(connectionIdentifier)) {
			this.printLog("active connection found matching " + connectionIdentifier, 3);
			final ConnectedTransport connectedTransport = this.connections.get(connectionIdentifier);
			try {
				connectedTransport.sendMessage(message);
				this.printMessageLog(connectedTransport.getProtocol(), connectedTransport.getRemoteAddress().toString(), connectedTransport.getRemotePort(), message.getLength(), message, "sent");
				return connectionIdentifier;
			} catch (Exception ex) {
				this.printException(ex, 1);
			}
		}
		this.printLog("no active connection found matching " + connectionIdentifier, 3);
		return this.sendMessage(message);
	}

	public void sendMessage(final String s) {
		try {
			if (this.outbound_proxy != null) {
				final String string = this.outbound_proxy.getAddress().toString();
				final int port = this.outbound_proxy.getPort();
				if (this.udp != null) {
					this.udp.sendMessage(s, string, port);
					this.printHeartBeatMessageLog(string, port, s.length(), s, "sent");
				}
			}
		} catch (IOException ex) {
			this.printException(ex, 1);
			ex.printStackTrace();
		}
	}

	public void setDefaultTransport(final String default_transport) {
		this.default_transport = default_transport;
	}

	public void setForceRport(final boolean force_rport) {
		this.force_rport = force_rport;
	}

	public void setHeartBeatListner(final IHeartBeatListener heartBeatListener) {
		if (heartBeatListener == null) {
			return;
		}
		this.heartBeatListener = heartBeatListener;
	}

	public void setNMaxConnections(final int nmax_connections) {
		this.nmax_connections = nmax_connections;
	}

	public void setOutboundProxy(final SocketAddress outbound_proxy) {
		this.outbound_proxy = outbound_proxy;
	}

	public void setRport(final boolean rport) {
		this.rport = rport;
	}

	protected String toLines() {
		return this.toString();
	}

	@Override
	public String toString() {
		if (this.host_ipaddr == null) {
			return String.valueOf(this.host_port) + "/" + this.transportProtocolsToString();
		}
		return String.valueOf(this.host_ipaddr.toString()) + ":" + this.host_port + "/" + this.transportProtocolsToString();
	}
}
