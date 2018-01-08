package org.zoolu.sip.message;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.dialog.Dialog;
import org.zoolu.sip.header.EventHeader;
import org.zoolu.sip.header.ReferToHeader;
import org.zoolu.sip.header.ReferredByHeader;
import org.zoolu.sip.header.SubjectHeader;
import org.zoolu.sip.provider.SipProvider;

public class MessageFactory extends BaseMessageFactory {
	public static Message createMessageRequest(final SipProvider sipProvider, final NameAddress nameAddress, final NameAddress nameAddress2, final String s, final String s2, final String s3) {
		final Message request = BaseMessageFactory.createRequest(sipProvider, "MESSAGE", nameAddress.getAddress(), nameAddress, nameAddress2, null, sipProvider.pickCallId(), SipProvider.pickInitialCSeq(), SipProvider.pickTag(), null, null, null, null);
		if (s != null) {
			request.setSubjectHeader(new SubjectHeader(s));
		}
		request.setBody(s2, s3);
		return request;
	}

	public static Message createNotifyRequest(final Dialog dialog, final String s, final String s2, final String s3) {
		final Message request = BaseMessageFactory.createRequest(dialog, "NOTIFY", null);
		request.removeExpiresHeader();
		request.setEventHeader(new EventHeader(s, s2));
		request.setBody("message/sipfrag;version=2.0", s3);
		return request;
	}

	public static Message createNotifyRequest(final Dialog dialog, final String s, final String s2, final String s3, final String s4) {
		final Message request = BaseMessageFactory.createRequest(dialog, "NOTIFY", null);
		request.removeExpiresHeader();
		request.setEventHeader(new EventHeader(s, s2));
		request.setBody(s3, s4);
		return request;
	}

	public static Message createReferRequest(final Dialog dialog, final NameAddress nameAddress, final NameAddress nameAddress2) {
		final Message request = BaseMessageFactory.createRequest(dialog, "REFER", null);
		request.setReferToHeader(new ReferToHeader(nameAddress));
		if (nameAddress2 != null) {
			request.setReferredByHeader(new ReferredByHeader(nameAddress2));
			return request;
		}
		request.setReferredByHeader(new ReferredByHeader(dialog.getLocalName()));
		return request;
	}

	public static Message createReferRequest(final SipProvider sipProvider, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final NameAddress nameAddress4) {
		final Message request = BaseMessageFactory.createRequest(sipProvider, "REFER", nameAddress.getAddress(), nameAddress, nameAddress2, nameAddress3, sipProvider.pickCallId(), SipProvider.pickInitialCSeq(), SipProvider.pickTag(), null, null, null, null);
		request.setReferToHeader(new ReferToHeader(nameAddress4));
		request.setReferredByHeader(new ReferredByHeader(nameAddress2));
		return request;
	}

	public static Message createSubscribeRequest(final Dialog dialog, final String s, final String s2, final String s3, final String s4) {
		final Message request = BaseMessageFactory.createRequest(dialog, "SUBSCRIBE", null);
		request.setEventHeader(new EventHeader(s, s2));
		request.setBody(s3, s4);
		return request;
	}

	public static Message createSubscribeRequest(final SipProvider sipProvider, final SipURL sipURL, final NameAddress nameAddress, final NameAddress nameAddress2, final NameAddress nameAddress3, final String s, final String s2, final String s3, final String s4) {
		final Message request = BaseMessageFactory.createRequest(sipProvider, "SUBSCRIBE", sipURL, nameAddress, nameAddress2, nameAddress3, null);
		request.setEventHeader(new EventHeader(s, s2));
		request.setBody(s3, s4);
		return request;
	}
}
