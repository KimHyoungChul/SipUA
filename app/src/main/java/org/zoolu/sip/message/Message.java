package org.zoolu.sip.message;

import org.zoolu.net.UdpPacket;
import org.zoolu.sip.header.AllowEventsHeader;
import org.zoolu.sip.header.EventHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.PttExtension2Header;
import org.zoolu.sip.header.PttExtensionHeader;
import org.zoolu.sip.header.ReferToHeader;
import org.zoolu.sip.header.ReferredByHeader;
import org.zoolu.sip.header.SubscriptionStateHeader;

public class Message extends BaseMessage {
	public Message() {
	}

	public Message(final String s) {
		super(s);
	}

	public Message(final UdpPacket udpPacket) {
		super(udpPacket);
	}

	public Message(final Message message) {
		super(message);
	}

	public Message(final byte[] array, final int n, final int n2) {
		super(array, n, n2);
	}

	@Override
	public Object clone() {
		return new Message(this);
	}

	public AllowEventsHeader getAllowEventsHeader() {
		final Header header = this.getHeader("Allow-Events");
		if (header == null) {
			return null;
		}
		return new AllowEventsHeader(header);
	}

	public Header getAntaExtensionHeader() {
		final Header header = this.getHeader("Anta-Extension");
		if (header == null) {
			return null;
		}
		return new Header(header);
	}

	public EventHeader getEventHeader() {
		final Header header = this.getHeader("Event");
		if (header == null) {
			return null;
		}
		return new EventHeader(header);
	}

	public PttExtension2Header getPttExtension2Header() {
		final Header header = this.getHeader("Ptt-Extension2");
		if (header == null) {
			return null;
		}
		return new PttExtension2Header(header);
	}

	public PttExtensionHeader getPttExtensionHeader() {
		final Header header = this.getHeader("Ptt-Extension");
		if (header == null) {
			return null;
		}
		return new PttExtensionHeader(header);
	}

	public ReferToHeader getReferToHeader() {
		final Header header = this.getHeader("Refer-To");
		if (header == null) {
			return null;
		}
		return new ReferToHeader(header);
	}

	public ReferredByHeader getReferredByHeader() {
		final Header header = this.getHeader("Referred-By");
		if (header == null) {
			return null;
		}
		return new ReferredByHeader(header);
	}

	public SubscriptionStateHeader getSubscriptionStateHeader() {
		final Header header = this.getHeader("Subscription-State");
		if (header == null) {
			return null;
		}
		return new SubscriptionStateHeader(header);
	}

	public boolean hasAllowEventsHeader() {
		return this.hasHeader("Allow-Events");
	}

	public boolean hasAntaExtensionHeader() {
		return this.hasHeader("Anta-Extension");
	}

	public boolean hasEventHeader() {
		return this.hasHeader("Event");
	}

	public boolean hasPttExtensionHeader() {
		return this.hasHeader("Ptt-Extension");
	}

	public boolean hasReferToHeader() {
		return this.hasHeader("Refer-To");
	}

	public boolean hasReferredByHeader() {
		return this.hasHeader("Refer-To");
	}

	public boolean hasSubscriptionStateHeader() {
		return this.hasHeader("Subscription-State");
	}

	public boolean isMessage() throws NullPointerException {
		return this.isRequest("MESSAGE");
	}

	public boolean isNotify() throws NullPointerException {
		return this.isRequest("NOTIFY");
	}

	public boolean isPublish() throws NullPointerException {
		return this.isRequest("PUBLISH");
	}

	public boolean isRefer() throws NullPointerException {
		return this.isRequest("REFER");
	}

	public boolean isSubscribe() throws NullPointerException {
		return this.isRequest("SUBSCRIBE");
	}

	public void removeAllowEventsHeader() {
		this.removeHeader("Allow-Events");
	}

	public void removeEventHeader() {
		this.removeHeader("Event");
	}

	public void removeReferToHeader() {
		this.removeHeader("Refer-To");
	}

	public void removeReferredByHeader() {
		this.removeHeader("Referred-By");
	}

	public void removeSubscriptionStateHeader() {
		this.removeHeader("Subscription-State");
	}

	public void setAllowEventsHeader(final AllowEventsHeader header) {
		this.setHeader(header);
	}

	public void setEventHeader(final EventHeader header) {
		this.setHeader(header);
	}

	public void setReferToHeader(final ReferToHeader header) {
		this.setHeader(header);
	}

	public void setReferredByHeader(final ReferredByHeader header) {
		this.setHeader(header);
	}

	public void setSubscriptionStateHeader(final SubscriptionStateHeader header) {
		this.setHeader(header);
	}
}
