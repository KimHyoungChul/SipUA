package org.zoolu.sip.message;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.dialog.Dialog;
import org.zoolu.sip.header.AcceptContactHeader;
import org.zoolu.sip.header.CSeqHeader;
import org.zoolu.sip.header.CallIdHeader;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.FromHeader;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.header.UserAgentHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import java.util.Enumeration;
import java.util.Vector;

public abstract class BaseMessageFactory {
	public static Message create2xxAckRequest(final Dialog dialog, final String s) {
		return createRequest(dialog, "ACK", s);
	}

	public static Message createByeRequest(final Dialog dialog) {
		final Message request = createRequest(dialog, "BYE", null);
		request.removeExpiresHeader();
		request.removeContacts();
		return request;
	}

	public static Message createCancelRequest(final Message message, final Dialog dialog) {
		final ToHeader toHeader = message.getToHeader();
		final FromHeader fromHeader = message.getFromHeader();
		final SipURL address = message.getRequestLine().getAddress();
		final NameAddress nameAddress = message.getContactHeader().getNameAddress();
		final ViaHeader viaHeader = message.getViaHeader();
		return createRequest("CANCEL", address, toHeader.getNameAddress(), fromHeader.getNameAddress(), nameAddress, viaHeader.getProtocol(), viaHeader.getHost(), viaHeader.getPort(), viaHeader.hasRport(), message.getCallIdHeader().getCallId(), message.getCSeqHeader().getSequenceNumber(), fromHeader.getParameter("tag"), toHeader.getParameter("tag"), message.getViaHeader().getBranch(), "", null, null);
	}

	public static Message createInviteRequest(final Dialog dialog, final String s) {
		return createRequest(dialog, "INVITE", s);
	}

	public static Message createInviteRequest(final SipProvider sipProvider, final SipURL sipURL, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s, final String s2) {
		final String pickCallId = sipProvider.pickCallId();
		final int pickInitialCSeq = SipProvider.pickInitialCSeq();
		final String pickTag = SipProvider.pickTag();
		NameAddress nameAddress4 = nameAddress3;
		if (nameAddress3 == null) {
			nameAddress4 = nameAddress2;
		}
		return createRequest(sipProvider, "INVITE", sipURL, nameAddress, nameAddress2, nameAddress4, pickCallId, pickInitialCSeq, pickTag, null, null, s, s2);
	}

	public static Message createNon2xxAckRequest(final SipProvider sipProvider, final Message message, final Message message2) {
		final SipURL address = message.getRequestLine().getAddress();
		final FromHeader fromHeader = message.getFromHeader();
		final ToHeader toHeader = message2.getToHeader();
		final String viaAddress = sipProvider.getViaAddress();
		final int port = sipProvider.getPort();
		final boolean rportSet = sipProvider.isRportSet();
		String s;
		if (address.hasTransport()) {
			s = address.getTransport();
		} else {
			s = sipProvider.getDefaultTransport();
		}
		final Message request = createRequest("ACK", address, toHeader.getNameAddress(), fromHeader.getNameAddress(), null, s, viaAddress, port, rportSet, message.getCallIdHeader().getCallId(), message.getCSeqHeader().getSequenceNumber(), fromHeader.getParameter("tag"), toHeader.getParameter("tag"), message.getViaHeader().getBranch(), null, null, null);
		request.removeExpiresHeader();
		if (message.hasRouteHeader()) {
			request.setRoutes(message.getRoutes());
		}
		return request;
	}

	public static Message createRegisterRequest(final SipProvider sipProvider, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s, final String s2) {
		final SipURL address = nameAddress.getAddress();
		final SipURL sipURL = new SipURL(address.getHost(), address.getPort());
		final String viaAddress = sipProvider.getViaAddress();
		final int port = sipProvider.getPort();
		final boolean rportSet = sipProvider.isRportSet();
		String s3;
		if (address.hasTransport()) {
			s3 = address.getTransport();
		} else {
			s3 = sipProvider.getDefaultTransport();
		}
		final Message request = createRequest("REGISTER", sipURL, nameAddress, nameAddress2, nameAddress3, s3, viaAddress, port, rportSet, sipProvider.pickCallId(), SipProvider.pickInitialCSeq(), SipProvider.pickTag(), null, null, null, s, s2);
		if (nameAddress3 == null) {
			request.setContactHeader(new ContactHeader());
			request.setExpiresHeader(new ExpiresHeader(String.valueOf(SipStack.default_expires)));
		}
		return request;
	}

	public static Message createRequest(final String s, final SipURL sipURL, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s2, final String s3, final int n, final boolean b, final String s4, final long n2, final String s5, final String s6, final String s7, final String body, final String s8, final String s9) {
		final Message message = new Message();
		message.setRequestLine(new RequestLine(s, sipURL));
		final ViaHeader viaHeader = new ViaHeader(s2, s3, n);
		if (b) {
			viaHeader.setRport();
		}
		String pickBranch;
		if ((pickBranch = s7) == null) {
			pickBranch = SipProvider.pickBranch();
		}
		viaHeader.setBranch(pickBranch);
		message.addViaHeader(viaHeader);
		message.setMaxForwardsHeader(new MaxForwardsHeader(70));
		if (s6 == null) {
			message.setToHeader(new ToHeader(nameAddress));
		} else {
			message.setToHeader(new ToHeader(nameAddress, s6));
		}
		message.setFromHeader(new FromHeader(nameAddress2, s5));
		message.setCallIdHeader(new CallIdHeader(s4));
		message.setCSeqHeader(new CSeqHeader(n2, s));
		if (nameAddress3 != null) {
			if ((s == "REGISTER" || s == "INVITE") && s9 != null) {
				final MultipleHeader contacts = new MultipleHeader("Contact");
				contacts.addBottom(new ContactHeader(nameAddress3, s8, s9));
				message.setContacts(contacts);
			} else {
				final MultipleHeader contacts2 = new MultipleHeader("Contact");
				contacts2.addBottom(new ContactHeader(nameAddress3));
				message.setContacts(contacts2);
			}
		}
		if (s == "INVITE" && s9 != null) {
			message.setAcceptContactHeader(new AcceptContactHeader(s9));
		}
		message.setUserAgentHeader(new UserAgentHeader("Zed-3-PDA"));
		message.setBody(body);
		return message;
	}

	public static Message createRequest(final Dialog dialog, final String s, final String s2) {
		final NameAddress remoteName = dialog.getRemoteName();
		final NameAddress localName = dialog.getLocalName();
		NameAddress remoteContact;
		if ((remoteContact = dialog.getRemoteContact()) == null) {
			remoteContact = remoteName;
		}
		SipURL sipURL;
		if ((sipURL = remoteContact.getAddress()) == null) {
			sipURL = dialog.getRemoteName().getAddress();
		}
		final SipProvider sipProvider = dialog.getSipProvider();
		final String viaAddress = sipProvider.getViaAddress();
		final int port = sipProvider.getPort();
		final boolean rportSet = sipProvider.isRportSet();
		String s3;
		if (remoteContact.getAddress().hasTransport()) {
			s3 = remoteContact.getAddress().getTransport();
		} else {
			s3 = sipProvider.getDefaultTransport();
		}
		NameAddress localContact;
		if ((localContact = dialog.getLocalContact()) == null) {
			localContact = localName;
		}
		if (!BaseSipMethods.isAck(s) && !BaseSipMethods.isCancel(s)) {
			dialog.incLocalCSeq();
		}
		final Message request = createRequest(s, sipURL, remoteName, localName, localContact, s3, viaAddress, port, rportSet, dialog.getCallID(), dialog.getLocalCSeq(), dialog.getLocalTag(), dialog.getRemoteTag(), null, s2, null, null);
		final Vector<NameAddress> route = dialog.getRoute();
		if (route != null && route.size() > 0) {
			final Vector vector = new Vector<String>(route.size());
			final Enumeration<NameAddress> elements = route.elements();
			while (elements.hasMoreElements()) {
				vector.add(elements.nextElement().toString());
			}
			request.addRoutes(new MultipleHeader("Route", (Vector<String>) vector));
		}
		request.rfc2543RouteAdapt();
		return request;
	}

	public static Message createRequest(final SipProvider sipProvider, final String s, final NameAddress nameAddress, final NameAddress nameAddress2, final String s2) {
		return createRequest(sipProvider, s, nameAddress.getAddress(), nameAddress, nameAddress2, new NameAddress(new SipURL(nameAddress2.getAddress().getUserName(), sipProvider.getViaAddress(), sipProvider.getPort())), s2);
	}

	public static Message createRequest(final SipProvider sipProvider, final String s, final SipURL sipURL, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s2) {
		return createRequest(sipProvider, s, sipURL, nameAddress, nameAddress2, nameAddress3, sipProvider.pickCallId(), SipProvider.pickInitialCSeq(), SipProvider.pickTag(), null, null, s2, null);
	}

	public static Message createRequest(final SipProvider sipProvider, final String s, final SipURL sipURL, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s2, final long n, final String s3, final String s4, final String s5, final String s6, final String s7) {
		final String viaAddress = sipProvider.getViaAddress();
		final int port = sipProvider.getPort();
		final boolean rportSet = sipProvider.isRportSet();
		String s8;
		if (sipURL.hasTransport()) {
			s8 = sipURL.getTransport();
		} else {
			s8 = sipProvider.getDefaultTransport();
		}
		return createRequest(s, sipURL, nameAddress, nameAddress2, nameAddress3, s8, viaAddress, port, rportSet, s2, n, s3, s4, s5, s6, null, s7);
	}

	public static Message createResponse(final Message message, final int n, final String s, final String s2, final NameAddress nameAddress, final String s3, final String body) {
		final Message message2 = new Message();
		message2.setStatusLine(new StatusLine(n, s));
		message2.setVias(message.getVias());
		if (n >= 180 && n < 300 && message.hasRecordRouteHeader()) {
			message2.setRecordRoutes(message.getRecordRoutes());
		}
		final ToHeader toHeader = message.getToHeader();
		if (s2 != null) {
			toHeader.setParameter("tag", s2);
		}
		message2.setToHeader(toHeader);
		message2.setFromHeader(message.getFromHeader());
		message2.setCallIdHeader(message.getCallIdHeader());
		message2.setCSeqHeader(message.getCSeqHeader());
		if (nameAddress != null) {
			message2.setContactHeader(new ContactHeader(nameAddress));
		}
		message2.setUserAgentHeader(new UserAgentHeader("Zed-3-PDA"));
		if (s3 == null) {
			message2.setBody(body);
			return message2;
		}
		message2.setBody(s3, body);
		return message2;
	}

	public static Message createResponse(final Message message, final int n, final String s, final NameAddress nameAddress) {
		String pickTag;
		final String s2 = pickTag = null;
		if (message.createsDialog()) {
			pickTag = s2;
			if (!message.getToHeader().hasTag()) {
				if (!SipStack.early_dialog) {
					pickTag = s2;
					if (n < 101) {
						return createResponse(message, n, s, pickTag, nameAddress, null, null);
					}
					pickTag = s2;
					if (n >= 300) {
						return createResponse(message, n, s, pickTag, nameAddress, null, null);
					}
				}
				pickTag = SipProvider.pickTag(message);
			}
		}
		return createResponse(message, n, s, pickTag, nameAddress, null, null);
	}
}
