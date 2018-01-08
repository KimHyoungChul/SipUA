package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.AcceptHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

public class SubscriberDialog extends Dialog implements TransactionClientListener {
	protected static final String ACTIVE = "active";
	protected static final int D_ACCEPTED = 2;
	protected static final int D_ACTIVE = 4;
	protected static final int D_INIT = 0;
	protected static final int D_PENDING = 3;
	protected static final int D_SUBSCRIBING = 1;
	protected static final int D_TERMINATED = 9;
	protected static final String PENDING = "pending";
	protected static final String TERMINATED = "terminated";
	String event;
	String id;
	SubscriberDialogListener listener;
	TransactionClient subscribe_transaction;

	public SubscriberDialog(final SipProvider sipProvider, final String event, final String s, final SubscriberDialogListener listener) {
		super(sipProvider);
		this.listener = listener;
		this.subscribe_transaction = null;
		this.event = event;
		this.id = null;
		this.changeStatus(0);
	}

	public String getEvent() {
		return this.event;
	}

	public String getId() {
		return this.id;
	}

	@Override
	protected int getStatus() {
		return this.status;
	}

	@Override
	protected String getStatusDescription() {
		switch (this.status) {
			default: {
				return null;
			}
			case 0: {
				return "D_INIT";
			}
			case 1: {
				return "D_SUBSCRIBING";
			}
			case 2: {
				return "D_ACCEPTED";
			}
			case 3: {
				return "D_PENDING";
			}
			case 4: {
				return "D_ACTIVE";
			}
			case 9: {
				return "D_TERMINATED";
			}
		}
	}

	@Override
	public boolean isConfirmed() {
		return this.status >= 2 && this.status < 9;
	}

	@Override
	public boolean isEarly() {
		return this.status < 2;
	}

	public boolean isSubscriptionActive() {
		return this.status == 4;
	}

	public boolean isSubscriptionPending() {
		return this.status >= 2 && this.status < 4;
	}

	public boolean isSubscriptionTerminated() {
		return this.status == 9;
	}

	@Override
	public boolean isTerminated() {
		return this.status == 9;
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		this.printLog("onReceivedMessage()", 3);
		if (this.statusIs(9)) {
			this.printLog("subscription already terminated: message discarded", 3);
		} else {
			if (!message.isRequest() || !message.isNotify()) {
				this.printLog("message is not a NOTIFY: message discarded", 1);
				return;
			}
			new TransactionServer(sipProvider, message, null).respondWith(BaseMessageFactory.createResponse(message, 200, SipResponses.reasonOf(200), null));
			final NameAddress nameAddress = message.getToHeader().getNameAddress();
			final NameAddress nameAddress2 = message.getFromHeader().getNameAddress();
			NameAddress nameAddress3 = null;
			if (message.hasContactHeader()) {
				nameAddress3 = message.getContactHeader().getNameAddress();
			}
			String state = null;
			if (message.hasSubscriptionStateHeader()) {
				state = message.getSubscriptionStateHeader().getState();
			}
			String contentType = null;
			if (message.hasContentTypeHeader()) {
				contentType = message.getContentTypeHeader().getContentType();
			}
			String body = null;
			if (message.hasBody()) {
				body = message.getBody();
			}
			if (this.listener != null) {
				this.listener.onDlgNotify(this, nameAddress, nameAddress2, nameAddress3, state, contentType, body, message);
			}
			if (state != null) {
				if (state.equalsIgnoreCase("active") && !this.statusIs(9)) {
					this.changeStatus(4);
					return;
				}
				if (state.equalsIgnoreCase("pending") && this.statusIs(2)) {
					this.changeStatus(3);
					return;
				}
				if (state.equalsIgnoreCase("terminated") && !this.statusIs(9)) {
					this.changeStatus(9);
					if (this.listener != null) {
						this.listener.onDlgSubscriptionTerminated(this);
					}
				}
			}
		}
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransFailureResponse()", 3);
		this.changeStatus(9);
		final StatusLine statusLine = message.getStatusLine();
		if (this.listener != null) {
			this.listener.onDlgSubscriptionFailure(this, statusLine.getCode(), statusLine.getReason(), message);
		}
	}

	@Override
	public void onTransProvisionalResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransProvisionalResponse()", 3);
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransSuccessResponse()", 3);
		if (!this.statusIs(4)) {
			this.changeStatus(2);
			this.update(0, message);
			final StatusLine statusLine = message.getStatusLine();
			if (this.listener != null) {
				this.listener.onDlgSubscriptionSuccess(this, statusLine.getCode(), statusLine.getReason(), message);
			}
		} else if (this.statusIs(4)) {
			final StatusLine statusLine2 = message.getStatusLine();
			if (this.listener != null) {
				this.listener.onDlgSubscriptionSuccess(this, statusLine2.getCode(), statusLine2.getReason(), message);
			}
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		this.printLog("onTransTimeout()", 3);
		this.changeStatus(9);
		if (this.listener != null) {
			this.listener.onDlgSubscribeTimeout(this);
		}
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("SubscriberDialog#" + this.dialog_sqn + ": " + s, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	public void reSubscribe(final String s, final String s2, final String s3, final int n) {
		this.subscribe(s, s2, s3, n);
	}

	public void subscribe(final String s, final String s2, final String s3, final int n) {
		this.printLog("inside subscribe(target=" + s + ",subscriber=" + s2 + ",contact=" + s3 + ",id=" + this.id + ",expires=" + n + ")", 3);
		final SipURL sipURL = new SipURL(s);
		final NameAddress nameAddress = new NameAddress(s);
		final NameAddress nameAddress2 = new NameAddress(s2);
		NameAddress nameAddress3;
		if (s3 != null) {
			nameAddress3 = new NameAddress(s3);
		} else {
			nameAddress3 = nameAddress2;
		}
		final Message subscribeRequest = MessageFactory.createSubscribeRequest(this.sip_provider, sipURL, nameAddress, nameAddress2, nameAddress3, this.event, this.id, null, null);
		subscribeRequest.setHeader(new AcceptHeader("application/pidf+xml"));
		subscribeRequest.setExpiresHeader(new ExpiresHeader(n));
		this.subscribe(subscribeRequest);
	}

	public void subscribe(final Message message) {
		this.printLog("inside subscribe(req)", 3);
		if (this.statusIs(9)) {
			this.printLog("subscription already terminated: request aborted", 3);
			return;
		}
		if (this.statusIs(0)) {
			this.changeStatus(1);
		}
		this.update(0, message);
		(this.subscribe_transaction = new TransactionClient(this.sip_provider, message, this)).request();
	}
}
