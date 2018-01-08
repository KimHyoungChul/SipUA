package org.zoolu.sip.message;

import org.zoolu.net.UdpPacket;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.AcceptContactHeader;
import org.zoolu.sip.header.AcceptHeader;
import org.zoolu.sip.header.AlertInfoHeader;
import org.zoolu.sip.header.AllowHeader;
import org.zoolu.sip.header.AuthenticationInfoHeader;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.CSeqHeader;
import org.zoolu.sip.header.CallIdHeader;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.ContentLengthHeader;
import org.zoolu.sip.header.ContentTypeHeader;
import org.zoolu.sip.header.DateHeader;
import org.zoolu.sip.header.Expireheader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.FromHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.ProxyAuthenticateHeader;
import org.zoolu.sip.header.ProxyAuthorizationHeader;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.RequireHeader;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.ServerHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.SubjectHeader;
import org.zoolu.sip.header.SupportedHeader;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.header.UserAgentHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.sip.provider.ConnectionIdentifier;
import org.zoolu.sip.provider.DialogIdentifier;
import org.zoolu.sip.provider.MethodIdentifier;
import org.zoolu.sip.provider.SipParser;
import org.zoolu.sip.provider.TransactionIdentifier;

import java.util.Vector;

public abstract class BaseMessage {
	protected static int MAX_PKT_SIZE = 0;
	public static final String PROTO_SCTP = "sctp";
	public static final String PROTO_TCP = "tcp";
	public static final String PROTO_TLS = "tls";
	public static final String PROTO_UDP = "udp";
	protected ConnectionIdentifier connection_id;
	private String message;
	protected String remote_addr;
	protected int remote_port;
	protected String transport_proto;

	static {
		BaseMessage.MAX_PKT_SIZE = 8000;
	}

	public BaseMessage() {
		this.init();
		this.message = "";
	}

	public BaseMessage(final String s) {
		this.init();
		this.message = new String(s);
	}

	public BaseMessage(final UdpPacket udpPacket) {
		this.init();
		this.message = new String(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getLength());
	}

	public BaseMessage(final BaseMessage baseMessage) {
		this.message = baseMessage.message;
		this.remote_addr = baseMessage.remote_addr;
		this.remote_port = baseMessage.remote_port;
		this.transport_proto = baseMessage.transport_proto;
		this.connection_id = baseMessage.connection_id;
	}

	public BaseMessage(final byte[] array, final int n, final int n2) {
		this.init();
		this.message = new String(array, n, n2);
	}

	private void init() {
		this.remote_addr = null;
		this.remote_port = 0;
		this.transport_proto = null;
		this.connection_id = null;
	}

	public void addContactHeader(final ContactHeader contactHeader, final boolean b) {
		this.addHeader(contactHeader, b);
	}

	public void addContacts(final MultipleHeader multipleHeader, final boolean b) {
		this.addHeaders(multipleHeader, b);
	}

	public void addHeader(final Header header, final boolean b) {
		this.addHeaders(header.toString(), b);
	}

	public void addHeaderAfter(final Header header, final String s) {
		this.addHeadersAfter(header.toString(), s);
	}

	public void addHeaderBefore(final Header header, final String s) {
		this.addHeadersBefore(header.toString(), s);
	}

	protected void addHeaders(final String s, final int n) {
		int length = n;
		if (n > this.message.length()) {
			length = this.message.length();
		}
		this.message = String.valueOf(this.message.substring(0, length)) + s + this.message.substring(length);
	}

	protected void addHeaders(final String s, final boolean b) {
		int pos;
		if (b) {
			if (this.hasRequestLine() || this.hasStatusLine()) {
				final SipParser sipParser = new SipParser(this.message);
				sipParser.goToNextHeader();
				pos = sipParser.getPos();
			} else {
				pos = 0;
			}
		} else {
			final int pos2 = new SipParser(this.message).goToEndOfLastHeader().goToNextLine().getPos();
			final SipParser sipParser2 = new SipParser(this.message);
			final int indexOfHeader = sipParser2.indexOfHeader("Content-Length");
			int n;
			if (indexOfHeader < (n = pos2)) {
				n = indexOfHeader;
			}
			final int indexOfHeader2 = sipParser2.indexOfHeader("Content-Type");
			if (indexOfHeader2 < (pos = n)) {
				pos = indexOfHeader2;
			}
		}
		this.message = this.message.substring(0, pos).concat(s).concat(this.message.substring(pos));
	}

	public void addHeaders(final Vector<Header> vector, final boolean b) {
		String string = "";
		for (int i = 0; i < vector.size(); ++i) {
			string = String.valueOf(string) + vector.elementAt(i).toString();
		}
		this.addHeaders(string, b);
	}

	public void addHeaders(final MultipleHeader multipleHeader, final boolean b) {
		this.addHeaders(multipleHeader.toString(), b);
	}

	protected void addHeadersAfter(final String s, final String s2) {
		if (!this.hasHeader(s2)) {
			this.addHeaders(s, false);
			return;
		}
		final SipParser sipParser = new SipParser(this.message);
		sipParser.goTo(s2);
		final int indexOfNextHeader = sipParser.indexOfNextHeader();
		this.message = String.valueOf(this.message.substring(0, indexOfNextHeader)) + s + this.message.substring(indexOfNextHeader);
	}

	public void addHeadersAfter(final MultipleHeader multipleHeader, final String s) {
		this.addHeadersAfter(multipleHeader.toString(), s);
	}

	protected void addHeadersBefore(final String s, final String s2) {
		if (!this.hasHeader(s2)) {
			this.addHeaders(s, true);
			return;
		}
		final SipParser sipParser = new SipParser(this.message);
		sipParser.goTo(s2);
		final int pos = sipParser.getPos();
		this.message = String.valueOf(this.message.substring(0, pos)) + s + this.message.substring(pos);
	}

	public void addHeadersBefore(final MultipleHeader multipleHeader, final String s) {
		this.addHeadersBefore(multipleHeader.toString(), s);
	}

	public void addRecordRouteHeader(final RecordRouteHeader recordRouteHeader) {
		this.addHeaderAfter(recordRouteHeader, "CSeq");
	}

	public void addRecordRoutes(final MultipleHeader multipleHeader) {
		this.addHeadersAfter(multipleHeader, "CSeq");
	}

	public void addRouteHeader(final RouteHeader routeHeader) {
		this.addHeaderAfter(routeHeader, "Via");
	}

	public void addRoutes(final MultipleHeader multipleHeader) {
		this.addHeadersAfter(multipleHeader, "Via");
	}

	public void addViaHeader(final ViaHeader viaHeader) {
		this.addHeader(viaHeader, true);
	}

	public void addVias(final MultipleHeader multipleHeader, final boolean b) {
		this.addHeaders(multipleHeader, b);
	}

	public abstract Object clone();

	public boolean createsDialog() {
		if (this.isRequest()) {
			final String method = this.getRequestLine().getMethod();
			for (int i = 0; i < SipMethods.dialog_methods.length; ++i) {
				if (method.equalsIgnoreCase(SipMethods.dialog_methods[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public AcceptHeader getAcceptHeader() {
		final Header header = this.getHeader("Accept");
		if (header == null) {
			return null;
		}
		return new AcceptHeader(header);
	}

	public AlertInfoHeader getAlertInfoHeader() {
		final Header header = this.getHeader("Alert-Info");
		if (header == null) {
			return null;
		}
		return new AlertInfoHeader(header);
	}

	public AllowHeader getAllowHeader() {
		final Header header = this.getHeader("Allow");
		if (header == null) {
			return null;
		}
		return new AllowHeader(header);
	}

	public AuthenticationInfoHeader getAuthenticationInfoHeader() {
		final Header header = this.getHeader("Authentication-Info");
		if (header == null) {
			return null;
		}
		return new AuthenticationInfoHeader(header);
	}

	public AuthorizationHeader getAuthorizationHeader() {
		final Header header = this.getHeader("Authorization");
		if (header == null) {
			return null;
		}
		return new AuthorizationHeader(header);
	}

	public String getBody() {
		if (!this.hasBody()) {
			return null;
		}
		final int pos = new SipParser(this.message).goToBody().getPos();
		int contentLength;
		if (this.hasContentLengthHeader()) {
			contentLength = this.getContentLengthHeader().getContentLength();
		} else {
			contentLength = this.message.length() - pos;
		}
		int length;
		if ((length = pos + contentLength) > this.message.length()) {
			length = this.message.length();
		}
		return this.message.substring(pos, length);
	}

	public String getBodyType() {
		return this.getContentTypeHeader().getContentType();
	}

	public CSeqHeader getCSeqHeader() {
		final Header header = this.getHeader("CSeq");
		if (header == null) {
			return null;
		}
		return new CSeqHeader(header);
	}

	public CallIdHeader getCallIdHeader() {
		final Header header = this.getHeader("Call-ID");
		if (header == null) {
			return null;
		}
		return new CallIdHeader(header);
	}

	public ConnectionIdentifier getConnectionId() {
		return this.connection_id;
	}

	public ContactHeader getContactHeader() {
		final MultipleHeader contacts = this.getContacts();
		if (contacts == null) {
			return null;
		}
		return new ContactHeader(contacts.getTop());
	}

	public MultipleHeader getContacts() {
		final Vector<Header> headers = this.getHeaders("Contact");
		if (headers.size() > 0) {
			return new MultipleHeader(headers);
		}
		return null;
	}

	public ContentLengthHeader getContentLengthHeader() {
		final Header header = this.getHeader("Content-Length");
		if (header == null) {
			return null;
		}
		return new ContentLengthHeader(header);
	}

	public ContentTypeHeader getContentTypeHeader() {
		final Header header = this.getHeader("Content-Type");
		if (header == null) {
			return null;
		}
		return new ContentTypeHeader(header);
	}

	public DateHeader getDateHeader() {
		final Header header = this.getHeader("Date");
		if (header == null) {
			return null;
		}
		return new DateHeader(header);
	}

	public DialogIdentifier getDialogId() {
		final String callId = this.getCallIdHeader().getCallId();
		String s;
		String s2;
		if (this.isRequest()) {
			s = this.getToHeader().getTag();
			s2 = this.getFromHeader().getTag();
		} else {
			s = this.getFromHeader().getTag();
			s2 = this.getToHeader().getTag();
		}
		return new DialogIdentifier(callId, s, s2);
	}

	public ExpiresHeader getExpiresHeader() {
		final Header header = this.getHeader("Expires");
		if (header == null) {
			return null;
		}
		return new ExpiresHeader(header);
	}

	public String getFirstLine() {
		if (this.isRequest()) {
			return this.getRequestLine().toString();
		}
		if (this.isResponse()) {
			return this.getStatusLine().toString();
		}
		return null;
	}

	public FromHeader getFromHeader() {
		final Header header = this.getHeader("From");
		if (header == null) {
			return null;
		}
		return new FromHeader(header);
	}

	public Header getHeader(final String s) {
		return new SipParser(this.message).getHeader(s);
	}

	public Vector<Header> getHeaders(final String s) {
		final Vector<Header> vector = new Vector<Header>();
		final SipParser sipParser = new SipParser(this.message);
		while (true) {
			final Header header = sipParser.getHeader(s);
			if (header == null) {
				break;
			}
			vector.addElement(header);
		}
		return vector;
	}

	public int getLength() {
		return this.message.length();
	}

	public MaxForwardsHeader getMaxForwardsHeader() {
		final Header header = this.getHeader("Max-Forwards");
		if (header == null) {
			return null;
		}
		return new MaxForwardsHeader(header);
	}

	public MethodIdentifier getMethodId() {
		return new MethodIdentifier(this.getCSeqHeader().getMethod());
	}

	public ProxyAuthenticateHeader getProxyAuthenticateHeader() {
		final Header header = this.getHeader("Proxy-Authenticate");
		if (header == null) {
			return null;
		}
		return new ProxyAuthenticateHeader(header);
	}

	public ProxyAuthorizationHeader getProxyAuthorizationHeader() {
		final Header header = this.getHeader("Proxy-Authorization");
		if (header == null) {
			return null;
		}
		return new ProxyAuthorizationHeader(header);
	}

	public RecordRouteHeader getRecordRouteHeader() {
		final MultipleHeader recordRoutes = this.getRecordRoutes();
		if (recordRoutes == null) {
			return null;
		}
		return new RecordRouteHeader(recordRoutes.getTop());
	}

	public MultipleHeader getRecordRoutes() {
		final Vector<Header> headers = this.getHeaders("Record-Route");
		if (headers.size() > 0) {
			return new MultipleHeader(headers);
		}
		return null;
	}

	public String getRemoteAddress() {
		return this.remote_addr;
	}

	public int getRemotePort() {
		return this.remote_port;
	}

	public RequestLine getRequestLine() {
		if (!this.isRequest()) {
			return null;
		}
		final SipParser sipParser = new SipParser(this.message);
		final String string = sipParser.getString();
		sipParser.skipWSP();
		return new RequestLine(string, new SipParser(sipParser.subParser(sipParser.indexOfEOH() - sipParser.getPos())).getSipURL());
	}

	public RouteHeader getRouteHeader() {
		final MultipleHeader routes = this.getRoutes();
		if (routes == null) {
			return null;
		}
		return new RouteHeader(routes.getTop());
	}

	public MultipleHeader getRoutes() {
		final Vector<Header> headers = this.getHeaders("Route");
		if (headers.size() > 0) {
			return new MultipleHeader(headers);
		}
		return null;
	}

	public ServerHeader getServerHeader() {
		final Header header = this.getHeader("Server");
		if (header == null) {
			return null;
		}
		return new ServerHeader(header);
	}

	public StatusLine getStatusLine() {
		if (!this.isResponse()) {
			return null;
		}
		final SipParser sipParser = new SipParser(this.message);
		sipParser.skipString().skipWSP();
		return new StatusLine(sipParser.getInt(), this.message.substring(sipParser.getPos(), sipParser.indexOfEOH()).trim());
	}

	public SubjectHeader getSubjectHeader() {
		final Header header = this.getHeader("Subject");
		if (header == null) {
			return null;
		}
		return new SubjectHeader(header);
	}

	public SupportedHeader getSupportedHeader() {
		final Header header = this.getHeader("Supported");
		if (header == null) {
			return null;
		}
		return new SupportedHeader(header);
	}

	public ToHeader getToHeader() {
		final Header header = this.getHeader("To");
		if (header == null) {
			return null;
		}
		return new ToHeader(header);
	}

	public TransactionIdentifier getTransactionId() {
		final String callId = this.getCallIdHeader().getCallId();
		final ViaHeader viaHeader = this.getViaHeader();
		String branch = null;
		if (viaHeader != null) {
			branch = branch;
			if (viaHeader.hasBranch()) {
				branch = viaHeader.getBranch();
			}
		}
		final CSeqHeader cSeqHeader = this.getCSeqHeader();
		return new TransactionIdentifier(callId, cSeqHeader.getSequenceNumber(), cSeqHeader.getMethod(), null, branch);
	}

	public String getTransactionMethod() {
		return this.getCSeqHeader().getMethod();
	}

	public String getTransportProtocol() {
		return this.transport_proto;
	}

	public UserAgentHeader getUserAgentHeader() {
		final Header header = this.getHeader("User-Agent");
		if (header == null) {
			return null;
		}
		return new UserAgentHeader(header);
	}

	public ViaHeader getViaHeader() {
		final MultipleHeader vias = this.getVias();
		if (vias == null) {
			return null;
		}
		return new ViaHeader(vias.getTop());
	}

	public MultipleHeader getVias() {
		final Vector<Header> headers = this.getHeaders("Via");
		if (headers.size() > 0) {
			return new MultipleHeader(headers);
		}
		return null;
	}

	public WwwAuthenticateHeader getWwwAuthenticateHeader() {
		final Header header = this.getHeader("WWW-Authenticate");
		if (header == null) {
			return null;
		}
		return new WwwAuthenticateHeader(header);
	}

	public boolean hasAcceptHeader() {
		return this.hasHeader("Accept");
	}

	public boolean hasAlertInfoHeader() {
		return this.hasHeader("Alert-Info");
	}

	public boolean hasAllowHeader() {
		return this.hasHeader("Allow");
	}

	public boolean hasAuthenticationInfoHeader() {
		return this.hasHeader("Authentication-Info");
	}

	public boolean hasAuthorizationHeader() {
		return this.hasHeader("Authorization");
	}

	public boolean hasBody() {
		if (this.hasContentLengthHeader()) {
			return this.getContentLengthHeader().getContentLength() > 0;
		}
		return this.hasContentTypeHeader();
	}

	public boolean hasCSeqHeader() {
		return this.hasHeader("CSeq");
	}

	public boolean hasCallIdHeader() {
		return this.hasHeader("Call-ID");
	}

	public boolean hasContactHeader() {
		return this.hasHeader("Contact");
	}

	public boolean hasContentLengthHeader() {
		return this.hasHeader("Content-Length");
	}

	public boolean hasContentTypeHeader() {
		return this.hasHeader("Content-Type");
	}

	public boolean hasDateHeader() {
		return this.hasHeader("Date");
	}

	public boolean hasExpiresHeader() {
		return this.hasHeader("Expires");
	}

	public boolean hasFromHeader() {
		return this.hasHeader("From");
	}

	public boolean hasHeader(final String s) {
		return this.getHeader(s) != null;
	}

	public boolean hasMaxForwardsHeader() {
		return this.hasHeader("Max-Forwards");
	}

	public boolean hasProxyAuthenticateHeader() {
		return this.hasHeader("Proxy-Authenticate");
	}

	public boolean hasProxyAuthorizationHeader() {
		return this.hasHeader("Proxy-Authorization");
	}

	public boolean hasRecordRouteHeader() {
		return this.hasHeader("Record-Route");
	}

	protected boolean hasRequestLine() {
		return this.isRequest();
	}

	public boolean hasRouteHeader() {
		return this.hasHeader("Route");
	}

	public boolean hasServerHeader() {
		return this.hasHeader("Server");
	}

	protected boolean hasStatusLine() {
		return this.isResponse();
	}

	public boolean hasSubjectHeader() {
		return this.hasHeader("Subject");
	}

	public boolean hasToHeader() {
		return this.hasHeader("To");
	}

	public boolean hasUserAgentHeader() {
		return this.hasHeader("User-Agent");
	}

	public boolean hasViaHeader() {
		return this.hasHeader("Via");
	}

	public boolean hasWwwAuthenticateHeader() {
		return this.hasHeader("WWW-Authenticate");
	}

	public boolean isAck() {
		return this.isRequest("ACK");
	}

	public boolean isBye() {
		return this.isRequest("BYE");
	}

	public boolean isCancel() {
		return this.isRequest("CANCEL");
	}

	public boolean isInfo() {
		return this.isRequest("INFO");
	}

	public boolean isInvite() {
		return this.isRequest("INVITE");
	}

	public boolean isOption() {
		return this.isRequest("OPTION");
	}

	public boolean isRegister() {
		return this.isRequest("REGISTER");
	}

	public boolean isRequest() throws NullPointerException {
		if (this.message != null && !this.isResponse()) {
			final String string = new SipParser(new SipParser(this.message).getLine()).skipString().skipString().getString();
			if (string != null && string.length() >= 4 && string.substring(0, 4).equalsIgnoreCase("SIP/")) {
				return true;
			}
		}
		return false;
	}

	public boolean isRequest(final String s) {
		return this.message.startsWith(s);
	}

	public boolean isResponse() throws NullPointerException {
		return this.message != null && this.message.length() >= 4 && this.message.substring(0, 4).equalsIgnoreCase("SIP/");
	}

	public void removeAcceptHeader() {
		this.removeHeader("Accept");
	}

	public void removeAlertInfoHeader() {
		this.removeHeader("Alert-Info");
	}

	public void removeAllHeaders(final String s) {
		final String[] array = {String.valueOf('\n') + s, String.valueOf('\r') + s};
		SipParser sipParser = new SipParser(this.message);
		sipParser.goTo(array);
		while (sipParser.hasMore()) {
			sipParser.skipChar();
			this.message = this.message.substring(0, sipParser.getPos()).concat(this.message.substring(sipParser.indexOfNextHeader()));
			sipParser = new SipParser(this.message, sipParser.getPos() - 1);
			sipParser.goTo(array);
		}
	}

	public void removeAllowHeader() {
		this.removeHeader("Allow");
	}

	public void removeAuthenticationInfoHeader() {
		this.removeHeader("Authentication-Info");
	}

	public void removeAuthorizationHeader() {
		this.removeHeader("Authorization");
	}

	public void removeBody() {
		this.message = this.message.substring(0, new SipParser(this.message).goToEndOfLastHeader().goToNextLine().getPos());
		this.removeContentLengthHeader();
		this.removeContentTypeHeader();
	}

	public void removeCSeqHeader() {
		this.removeHeader("CSeq");
	}

	public void removeCallIdHeader() {
		this.removeHeader("Call-ID");
	}

	public void removeContacts() {
		this.removeAllHeaders("Contact");
	}

	protected void removeContentLengthHeader() {
		this.removeHeader("Content-Length");
	}

	protected void removeContentTypeHeader() {
		this.removeHeader("Content-Type");
	}

	public void removeDateHeader() {
		this.removeHeader("Date");
	}

	public void removeExpiresHeader() {
		this.removeHeader("Expires");
	}

	protected void removeFirstLine() {
		this.message = this.message.substring(new SipParser(this.message).indexOfNextHeader());
	}

	public void removeFromHeader() {
		this.removeHeader("From");
	}

	public void removeHeader(final String s) {
		this.removeHeader(s, true);
	}

	public void removeHeader(final String s, final boolean b) {
		final String[] array = {String.valueOf('\n') + s, String.valueOf('\r') + s};
		final SipParser sipParser = new SipParser(this.message);
		sipParser.goTo(array);
		if (!sipParser.hasMore()) {
			return;
		}
		if (!b) {
			while (true) {
				final int index = sipParser.indexOf(array);
				if (index < 0) {
					break;
				}
				sipParser.setPos(index);
			}
		}
		sipParser.skipChar();
		final String substring = this.message.substring(0, sipParser.getPos());
		sipParser.goToNextHeader();
		this.message = substring.concat(this.message.substring(sipParser.getPos()));
	}

	public void removeMaxForwardsHeader() {
		this.removeHeader("Max-Forwards");
	}

	public void removeProxyAuthenticateHeader() {
		this.removeHeader("Proxy-Authenticate");
	}

	public void removeProxyAuthorizationHeader() {
		this.removeHeader("Proxy-Authorization");
	}

	public void removeRecordRouteHeader() {
		final MultipleHeader recordRoutes = this.getRecordRoutes();
		recordRoutes.removeTop();
		this.setRecordRoutes(recordRoutes);
	}

	public void removeRecordRoutes() {
		this.removeAllHeaders("Record-Route");
	}

	public void removeRequestLine() {
		if (!this.isRequest()) {
			return;
		}
		this.removeFirstLine();
	}

	public void removeRouteHeader() {
		final MultipleHeader routes = this.getRoutes();
		routes.removeTop();
		this.setRoutes(routes);
	}

	public void removeRoutes() {
		this.removeAllHeaders("Route");
	}

	public void removeServerHeader() {
		this.removeHeader("Server");
	}

	public void removeStatusLine() {
		if (!this.isResponse()) {
			return;
		}
		this.removeFirstLine();
	}

	public void removeSubjectHeader() {
		this.removeHeader("Subject");
	}

	public void removeToHeader() {
		this.removeHeader("To");
	}

	public void removeUserAgentHeader() {
		this.removeHeader("User-Agent");
	}

	public void removeViaHeader() {
		final MultipleHeader vias = this.getVias();
		vias.removeTop();
		this.setVias(vias);
	}

	public void removeVias() {
		this.removeAllHeaders("Via");
	}

	public void removeWwwAuthenticateHeader() {
		this.removeHeader("WWW-Authenticate");
	}

	public void rfc2543RouteAdapt() {
		if (this.hasRouteHeader()) {
			final MultipleHeader routes = this.getRoutes();
			if (!new RouteHeader(routes.getTop()).getNameAddress().getAddress().hasLr()) {
				final SipURL address = new RouteHeader(routes.getTop()).getNameAddress().getAddress();
				final SipURL address2 = this.getRequestLine().getAddress();
				routes.removeTop();
				routes.addBottom(new RouteHeader(new NameAddress(address2)));
				this.setRoutes(routes);
				this.setRequestLine(new RequestLine(this.getRequestLine().getMethod(), address));
			}
		}
	}

	public void rfc2543toRfc3261RouteUpdate() {
		final RequestLine requestLine = this.getRequestLine();
		final SipURL address = requestLine.getAddress();
		final MultipleHeader routes = this.getRoutes();
		final SipURL address2 = new RouteHeader(routes.getBottom()).getNameAddress().getAddress();
		routes.removeBottom();
		address.addLr();
		routes.addTop(new RouteHeader(new NameAddress(address)));
		this.removeRoutes();
		this.addRoutes(routes);
		this.setRequestLine(new RequestLine(requestLine.getMethod(), address2));
	}

	public void setAcceptContactHeader(final AcceptContactHeader header) {
		this.setHeader(header);
	}

	public void setAcceptHeader(final AcceptHeader header) {
		this.setHeader(header);
	}

	public void setAlertInfoHeader(final AlertInfoHeader header) {
		this.setHeader(header);
	}

	public void setAllowHeader(final AllowHeader header) {
		this.setHeader(header);
	}

	public void setAuthenticationInfoHeader(final AuthenticationInfoHeader header) {
		this.setHeader(header);
	}

	public void setAuthorizationHeader(final AuthorizationHeader header) {
		this.setHeader(header);
	}

	public void setBody(final String s) {
		this.setBody("application/sdp", s);
	}

	public void setBody(final String s, final String s2) {
		this.removeBody();
		if (s2 != null && s2.length() > 0) {
			this.setContentTypeHeader(new ContentTypeHeader(s));
			this.setContentLengthHeader(new ContentLengthHeader(s2.getBytes().length));
			this.message = String.valueOf(this.message) + "\r\n" + s2;
			return;
		}
		this.setContentLengthHeader(new ContentLengthHeader(0));
		this.message = String.valueOf(this.message) + "\r\n";
	}

	public void setCSeqHeader(final CSeqHeader header) {
		this.setHeader(header);
	}

	public void setCallIdHeader(final CallIdHeader header) {
		this.setHeader(header);
	}

	public void setConnectionId(final ConnectionIdentifier connection_id) {
		this.connection_id = connection_id;
	}

	public void setContactHeader(final ContactHeader contactHeader) {
		if (this.hasContactHeader()) {
			this.removeContacts();
		}
		this.addHeader(contactHeader, false);
	}

	public void setContacts(final MultipleHeader multipleHeader) {
		if (this.hasContactHeader()) {
			this.removeContacts();
		}
		this.addContacts(multipleHeader, false);
	}

	protected void setContentLengthHeader(final ContentLengthHeader header) {
		this.setHeader(header);
	}

	protected void setContentTypeHeader(final ContentTypeHeader header) {
		this.setHeader(header);
	}

	public void setDateHeader(final DateHeader header) {
		this.setHeader(header);
	}

	public void setExpiresHeader(final ExpiresHeader header) {
		this.setHeader(header);
	}

	public void setFromHeader(final FromHeader header) {
		this.setHeader(header);
	}

	public void setHeader(final Header header) {
		final String name = header.getName();
		if (this.hasHeader(name)) {
			final int indexOfHeader = new SipParser(this.message).indexOfHeader(name);
			this.removeAllHeaders(name);
			this.addHeaders(header.toString(), indexOfHeader);
			return;
		}
		this.addHeader(header, false);
	}

	public void setHeaders(final MultipleHeader multipleHeader) {
		final String name = multipleHeader.getName();
		if (this.hasHeader(name)) {
			final int indexOfHeader = new SipParser(this.message).indexOfHeader(name);
			this.removeAllHeaders(name);
			this.addHeaders(multipleHeader.toString(), indexOfHeader);
			return;
		}
		this.addHeaders(multipleHeader, false);
	}

	public void setMaxForwardsHeader(final MaxForwardsHeader header) {
		this.setHeader(header);
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setProxyAuthenticateHeader(final ProxyAuthenticateHeader header) {
		this.setHeader(header);
	}

	public void setProxyAuthorizationHeader(final ProxyAuthorizationHeader header) {
		this.setHeader(header);
	}

	public void setRecordRoutes(final MultipleHeader multipleHeader) {
		if (this.hasRecordRouteHeader()) {
			this.removeRecordRoutes();
		}
		this.addRecordRoutes(multipleHeader);
	}

	public void setRemoteAddress(final String remote_addr) {
		this.remote_addr = remote_addr;
	}

	public void setRemotePort(final int remote_port) {
		this.remote_port = remote_port;
	}

	public void setRequestLine(final RequestLine requestLine) {
		if (this.hasRequestLine()) {
			this.removeRequestLine();
		}
		this.message = String.valueOf(requestLine.toString()) + this.message;
	}

	public void setRequireheader(final RequireHeader header) {
		this.setHeader(header);
	}

	public void setRoutes(final MultipleHeader multipleHeader) {
		if (this.hasRouteHeader()) {
			this.removeRoutes();
		}
		this.addRoutes(multipleHeader);
	}

	public void setServerHeader(final ServerHeader header) {
		this.setHeader(header);
	}

	public void setSessionExpireheader(final Expireheader header) {
		this.setHeader(header);
	}

	public void setStatusLine(final StatusLine statusLine) {
		if (this.hasStatusLine()) {
			this.removeStatusLine();
		}
		this.message = String.valueOf(statusLine.toString()) + this.message;
	}

	public void setSubjectHeader(final SubjectHeader header) {
		this.setHeader(header);
	}

	public void setSupportedheader(final SupportedHeader header) {
		this.setHeader(header);
	}

	public void setToHeader(final ToHeader header) {
		this.setHeader(header);
	}

	public void setTransport(final String transport_proto) {
		this.transport_proto = transport_proto;
	}

	public void setUserAgentHeader(final UserAgentHeader header) {
		this.setHeader(header);
	}

	public void setVias(final MultipleHeader multipleHeader) {
		if (this.hasViaHeader()) {
			this.removeVias();
		}
		this.addContacts(multipleHeader, true);
	}

	public void setWwwAuthenticateHeader(final WwwAuthenticateHeader header) {
		this.setHeader(header);
	}

	@Override
	public String toString() {
		return this.message;
	}
}
