package org.zoolu.sip.call;

import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

import java.util.Vector;

public abstract class CallListenerAdapter implements ExtendedCallListener {
	@Override
	public void onCallAccepted(final Call call, final String s, final Message message) {
	}

	@Override
	public void onCallCanceling(final Call call, final Message message) {
	}

	@Override
	public void onCallClosed(final Call call, final Message message) {
	}

	@Override
	public void onCallClosing(final Call call, final Message message) {
	}

	@Override
	public void onCallConfirmed(final Call call, final String s, final Message message) {
	}

	@Override
	public void onCallIncoming(final Call call, final NameAddress nameAddress, final NameAddress nameAddress2, final String s, final Message message) {
		String s2;
		if (s != null && s.length() > 0) {
			final SessionDescriptor sessionDescriptor = new SessionDescriptor(s);
			final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(call.getLocalSessionDescriptor());
			final SessionDescriptor sessionDescriptor3 = new SessionDescriptor(sessionDescriptor.getOrigin(), sessionDescriptor.getSessionName(), sessionDescriptor2.getConnection(), sessionDescriptor2.getTime());
			sessionDescriptor3.addMediaDescriptors(sessionDescriptor2.getMediaDescriptors());
			s2 = SdpTools.sdpAttirbuteSelection(SdpTools.sdpMediaProduct(sessionDescriptor3, sessionDescriptor.getMediaDescriptors()), "rtpmap").toString();
		} else {
			s2 = call.getLocalSessionDescriptor();
		}
		call.ring(s2);
		call.accept(s2);
	}

	@Override
	public void onCallModifying(final Call call, String s, final Message message) {
		if (s != null && s.length() > 0) {
			final SessionDescriptor sessionDescriptor = new SessionDescriptor(s);
			final SessionDescriptor sessionDescriptor2 = new SessionDescriptor(call.getLocalSessionDescriptor());
			final SessionDescriptor sessionDescriptor3 = new SessionDescriptor(sessionDescriptor.getOrigin(), sessionDescriptor.getSessionName(), sessionDescriptor2.getConnection(), sessionDescriptor2.getTime());
			sessionDescriptor3.addMediaDescriptors(sessionDescriptor2.getMediaDescriptors());
			s = SdpTools.sdpAttirbuteSelection(SdpTools.sdpMediaProduct(sessionDescriptor3, sessionDescriptor.getMediaDescriptors(), (ExtendedCall) call), "rtpmap").toString();
		} else {
			s = call.getLocalSessionDescriptor();
		}
		call.accept(s);
	}

	@Override
	public void onCallReInviteAccepted(final Call call, final String s, final Message message) {
	}

	@Override
	public void onCallReInviteRefused(final Call call, final String s, final Message message) {
	}

	@Override
	public void onCallReInviteTimeout(final Call call) {
	}

	@Override
	public void onCallRedirection(final Call call, final String s, final Vector<String> vector, final Message message) {
		call.call(vector.elementAt(0));
	}

	@Override
	public void onCallRefused(final Call call, final String s, final Message message) {
	}

	@Override
	public void onCallRinging(final Call call, final Message message) {
	}

	@Override
	public void onCallTimeout(final Call call) {
	}

	@Override
	public void onCallTransfer(final ExtendedCall extendedCall, final NameAddress nameAddress, final NameAddress nameAddress2, final Message message) {
	}

	@Override
	public void onCallTransferAccepted(final ExtendedCall extendedCall, final Message message) {
	}

	@Override
	public void onCallTransferFailure(final ExtendedCall extendedCall, final String s, final Message message) {
	}

	@Override
	public void onCallTransferRefused(final ExtendedCall extendedCall, final String s, final Message message) {
	}

	@Override
	public void onCallTransferSuccess(final ExtendedCall extendedCall, final Message message) {
	}
}
