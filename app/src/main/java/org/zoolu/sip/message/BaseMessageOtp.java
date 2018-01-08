package org.zoolu.sip.message;

import org.zoolu.net.UdpPacket;
import org.zoolu.sip.header.ContentLengthHeader;
import org.zoolu.sip.header.ContentTypeHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.provider.SipParser;

import java.util.Vector;

public abstract class BaseMessageOtp extends BaseMessage {
	protected String body;
	protected Vector<Header> headers;
	protected RequestLine request_line;
	protected StatusLine status_line;

	public BaseMessageOtp() {
		this.init();
		this.headers = new Vector<Header>();
	}

	public BaseMessageOtp(final String s) {
		this.init();
		this.parseIt(s);
	}

	public BaseMessageOtp(final UdpPacket udpPacket) {
		this.init();
		this.parseIt(new String(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getLength()));
	}

	public BaseMessageOtp(final BaseMessageOtp baseMessageOtp) {
		this.init();
		this.remote_addr = baseMessageOtp.remote_addr;
		this.remote_port = baseMessageOtp.remote_port;
		this.transport_proto = baseMessageOtp.transport_proto;
		this.connection_id = baseMessageOtp.connection_id;
		this.request_line = baseMessageOtp.request_line;
		this.status_line = baseMessageOtp.status_line;
		this.headers = new Vector<Header>();
		for (int i = 0; i < baseMessageOtp.headers.size(); ++i) {
			this.headers.addElement(baseMessageOtp.headers.elementAt(i));
		}
		this.body = baseMessageOtp.body;
	}

	public BaseMessageOtp(final byte[] array, final int n, final int n2) {
		this.init();
		this.parseIt(new String(array, n, n2));
	}

	private void init() {
		this.request_line = null;
		this.status_line = null;
		this.headers = null;
		this.body = null;
	}

	private void parseIt(final String s) {
		final SipParser sipParser = new SipParser(s);
		if (s.substring(0, 4).equalsIgnoreCase("SIP/")) {
			this.status_line = sipParser.getStatusLine();
		} else {
			this.request_line = sipParser.getRequestLine();
		}
		this.headers = new Vector<Header>();
		for (Header header = sipParser.getHeader(); header != null; header = sipParser.getHeader()) {
			this.headers.addElement(header);
		}
		final ContentLengthHeader contentLengthHeader = this.getContentLengthHeader();
		if (contentLengthHeader != null) {
			this.body = sipParser.getString(contentLengthHeader.getContentLength());
		} else if (this.getContentTypeHeader() != null) {
			this.body = sipParser.getRemainingString();
			if (this.body.length() == 0) {
				this.body = null;
			}
		}
	}

	@Override
	public void addHeader(final Header header, final boolean b) {
		if (b) {
			this.headers.insertElementAt(header, 0);
			return;
		}
		this.headers.addElement(header);
	}

	@Override
	public void addHeaderAfter(final Header header, final String s) {
		final int indexOfHeader = this.indexOfHeader(s);
		int size;
		if (indexOfHeader >= 0) {
			size = indexOfHeader + 1;
		} else {
			size = this.headers.size();
		}
		this.headers.insertElementAt(header, size);
	}

	@Override
	public void addHeaderBefore(final Header header, final String s) {
		int indexOfHeader;
		if ((indexOfHeader = this.indexOfHeader(s)) < 0) {
			indexOfHeader = 0;
		}
		this.headers.insertElementAt(header, indexOfHeader);
	}

	@Override
	public void addHeaders(final Vector<Header> vector, final boolean b) {
		for (int i = 0; i < vector.size(); ++i) {
			if (b) {
				this.headers.insertElementAt(vector.elementAt(i), i);
			} else {
				this.headers.addElement(vector.elementAt(i));
			}
		}
	}

	@Override
	public void addHeaders(final MultipleHeader multipleHeader, final boolean b) {
		if (multipleHeader.isCommaSeparated()) {
			this.addHeader(multipleHeader.toHeader(), b);
			return;
		}
		this.addHeaders(multipleHeader.getHeaders(), b);
	}

	@Override
	public void addHeadersAfter(final MultipleHeader multipleHeader, final String s) {
		if (multipleHeader.isCommaSeparated()) {
			this.addHeaderAfter(multipleHeader.toHeader(), s);
		} else {
			final int indexOfHeader = this.indexOfHeader(s);
			int size;
			if (indexOfHeader >= 0) {
				size = indexOfHeader + 1;
			} else {
				size = this.headers.size();
			}
			final Vector<Header> headers = multipleHeader.getHeaders();
			for (int i = 0; i < headers.size(); ++i) {
				this.headers.insertElementAt(headers.elementAt(i), size + i);
			}
		}
	}

	@Override
	public void addHeadersBefore(final MultipleHeader multipleHeader, final String s) {
		if (multipleHeader.isCommaSeparated()) {
			this.addHeaderBefore(multipleHeader.toHeader(), s);
		} else {
			int indexOfHeader;
			if ((indexOfHeader = this.indexOfHeader(s)) < 0) {
				indexOfHeader = 0;
			}
			final Vector<Header> headers = multipleHeader.getHeaders();
			for (int i = 0; i < headers.size(); ++i) {
				this.headers.insertElementAt(headers.elementAt(i), indexOfHeader + i);
			}
		}
	}

	@Override
	public String getBody() {
		return this.body;
	}

	@Override
	public String getBodyType() {
		return this.getContentTypeHeader().getContentType();
	}

	@Override
	public Header getHeader(final String s) {
		final int indexOfHeader = this.indexOfHeader(s);
		if (indexOfHeader < 0) {
			return null;
		}
		return this.headers.elementAt(indexOfHeader);
	}

	@Override
	public Vector<Header> getHeaders(final String s) {
		final Vector<Header> vector = new Vector<Header>();
		for (int i = 0; i < this.headers.size(); ++i) {
			final Header header = this.headers.elementAt(i);
			if (s.equalsIgnoreCase(header.getName())) {
				vector.addElement(header);
			}
		}
		return vector;
	}

	@Override
	public int getLength() {
		return this.toString().length();
	}

	@Override
	public RequestLine getRequestLine() {
		return this.request_line;
	}

	@Override
	public StatusLine getStatusLine() {
		return this.status_line;
	}

	@Override
	public boolean hasBody() {
		return this.body != null;
	}

	@Override
	protected boolean hasRequestLine() {
		return this.request_line != null;
	}

	@Override
	protected boolean hasStatusLine() {
		return this.status_line != null;
	}

	protected int indexOfHeader(final String s) {
		for (int i = 0; i < this.headers.size(); ++i) {
			final int n = i;
			if (s.equalsIgnoreCase(this.headers.elementAt(i).getName())) {
				return n;
			}
		}
		return -1;
	}

	@Override
	public boolean isRequest() {
		return this.request_line != null;
	}

	@Override
	public boolean isRequest(final String s) {
		return this.request_line != null && this.request_line.getMethod().equalsIgnoreCase(s);
	}

	@Override
	public boolean isResponse() throws NullPointerException {
		return this.status_line != null;
	}

	@Override
	public void removeAllHeaders(final String s) {
		int n;
		for (int i = 0; i < this.headers.size(); i = n + 1) {
			n = i;
			if (s.equalsIgnoreCase(this.headers.elementAt(i).getName())) {
				this.headers.removeElementAt(i);
				n = i - 1;
			}
		}
	}

	@Override
	public void removeBody() {
		this.removeContentLengthHeader();
		this.removeContentTypeHeader();
		this.body = null;
	}

	@Override
	protected void removeFirstLine() {
		this.removeRequestLine();
		this.removeStatusLine();
	}

	@Override
	public void removeHeader(final String s) {
		this.removeHeader(s, true);
	}

	@Override
	public void removeHeader(final String s, final boolean b) {
		int n = -1;
		int size;
		for (int i = 0; i < this.headers.size(); i = size + 1) {
			size = i;
			if (s.equalsIgnoreCase(this.headers.elementAt(i).getName())) {
				final int n2 = i;
				size = i;
				n = n2;
				if (b) {
					size = this.headers.size();
					n = n2;
				}
			}
		}
		if (n >= 0) {
			this.headers.removeElementAt(n);
		}
	}

	@Override
	public void removeRequestLine() {
		this.request_line = null;
	}

	@Override
	public void removeStatusLine() {
		this.status_line = null;
	}

	@Override
	public void setBody(final String s, final String body) {
		this.removeBody();
		if (body != null && body.length() > 0) {
			this.setContentTypeHeader(new ContentTypeHeader(s));
			this.setContentLengthHeader(new ContentLengthHeader(body.length()));
			this.body = body;
			return;
		}
		this.setContentLengthHeader(new ContentLengthHeader(0));
		this.body = null;
	}

	@Override
	public void setHeader(final Header header) {
		int n = 1;
		final String name = header.getName();
		int n2;
		int n3;
		for (int i = 0; i < this.headers.size(); i = n3 + 1, n = n2) {
			final Header header2 = this.headers.elementAt(i);
			n2 = n;
			n3 = i;
			if (name.equalsIgnoreCase(header2.getName())) {
				if (n != 0) {
					this.headers.setElementAt(header2, i);
					n2 = 0;
					n3 = i;
				} else {
					this.headers.removeElementAt(i);
					n3 = i - 1;
					n2 = n;
				}
			}
		}
		if (n != 0) {
			this.headers.addElement(header);
		}
	}

	@Override
	public void setHeaders(final MultipleHeader multipleHeader) {
		if (multipleHeader.isCommaSeparated()) {
			this.setHeader(multipleHeader.toHeader());
		} else {
			int n = 1;
			final String name = multipleHeader.getName();
			int n2;
			int n3;
			for (int i = 0; i < this.headers.size(); i = n3 + 1, n = n2) {
				n2 = n;
				n3 = i;
				if (name.equalsIgnoreCase(this.headers.elementAt(i).getName())) {
					if (n != 0) {
						final Vector<Header> headers = multipleHeader.getHeaders();
						for (int j = 0; j < headers.size(); ++j) {
							this.headers.insertElementAt(headers.elementAt(j), i + j);
						}
						n2 = 0;
						n3 = i + (headers.size() - 1);
					} else {
						this.headers.removeElementAt(i);
						n3 = i - 1;
						n2 = n;
					}
				}
			}
		}
	}

	@Override
	public void setMessage(final String s) {
		this.parseIt(s);
	}

	@Override
	public void setRequestLine(final RequestLine request_line) {
		this.request_line = request_line;
	}

	@Override
	public void setStatusLine(final StatusLine status_line) {
		this.status_line = status_line;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if (this.request_line != null) {
			sb.append(this.request_line.toString());
		} else if (this.status_line != null) {
			sb.append(this.status_line.toString());
		}
		for (int i = 0; i < this.headers.size(); ++i) {
			sb.append(this.headers.elementAt(i).toString());
		}
		sb.append("\r\n");
		if (this.body != null) {
			sb.append(this.body);
		}
		return sb.toString();
	}
}
