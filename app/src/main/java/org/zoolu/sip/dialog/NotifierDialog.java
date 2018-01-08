package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.EventHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.SubscriptionStateHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.MethodIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

public class NotifierDialog extends Dialog implements TransactionClientListener {
	protected static final String ACTIVE = "active";
	protected static final int D_ACTIVE = 4;
	protected static final int D_INIT = 0;
	protected static final int D_PENDING = 3;
	protected static final int D_SUBSCRIBED = 2;
	protected static final int D_TERMINATED = 9;
	protected static final int D_WAITING = 1;
	protected static final String PENDING = "pending";
	protected static final String TERMINATED = "terminated";
	String event;
	String id;
	NotifierDialogListener listener;
	TransactionClient notify_transaction;
	Message subscribe_req;
	TransactionServer subscribe_transaction;

	public NotifierDialog(final SipProvider sipProvider, final NotifierDialogListener notifierDialogListener) {
		super(sipProvider);
		this.init(notifierDialogListener);
	}

	public NotifierDialog(final SipProvider sipProvider, final Message subscribe_req, final NotifierDialogListener notifierDialogListener) {
		super(sipProvider);
		this.init(notifierDialogListener);
		this.changeStatus(2);
		this.subscribe_req = subscribe_req;
		this.subscribe_transaction = new TransactionServer(sipProvider, subscribe_req, null);
		this.update(1, subscribe_req);
		final EventHeader eventHeader = subscribe_req.getEventHeader();
		if (eventHeader != null) {
			this.event = eventHeader.getEvent();
			this.id = eventHeader.getId();
		}
	}

	private void init(final NotifierDialogListener listener) {
		this.listener = listener;
		this.subscribe_transaction = null;
		this.notify_transaction = null;
		this.subscribe_req = null;
		this.event = null;
		this.id = null;
		this.changeStatus(0);
	}

	public void accept(final int n, final String s) {
		this.printLog("inside accept()", 3);
		this.respond(202, SipResponses.reasonOf(202), n, s, null, null);
	}

	public void activate() {
		this.activate(SipStack.default_expires);
	}

	public void activate(final int n) {
		this.notify("active", n, null, null);
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
				return "D_WAITING";
			}
			case 2: {
				return "D_SUBSCRIBED";
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
		return this.status >= 3 && this.status < 9;
	}

	@Override
	public boolean isEarly() {
		return this.status < 3;
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

	public void listen() {
		this.printLog("inside method listen()", 3);
		if (!this.statusIs(0)) {
			this.printLog("first subscription already received", 3);
			return;
		}
		this.changeStatus(1);
		this.sip_provider.addSipProviderListener(new MethodIdentifier("SUBSCRIBE"), this);
	}

	public void notify(final String s, final int expires, final String s2, final String s3) {
		final Message notifyRequest = MessageFactory.createNotifyRequest(this, this.event, this.id, s2, s3);
		if (s != null) {
			final SubscriptionStateHeader subscriptionStateHeader = new SubscriptionStateHeader(s);
			if (expires >= 0) {
				subscriptionStateHeader.setExpires(expires);
			}
			notifyRequest.setSubscriptionStateHeader(subscriptionStateHeader);
		}
		this.notify(notifyRequest);
	}

	public void notify(final Message message) {
		final String state = message.getSubscriptionStateHeader().getState();
		if (state.equalsIgnoreCase("active") && (this.statusIs(2) || this.statusIs(3))) {
			this.changeStatus(4);
		} else if (state.equalsIgnoreCase("pending") && this.statusIs(2)) {
			this.changeStatus(3);
		} else if (state.equalsIgnoreCase("terminated") && !this.statusIs(9)) {
			this.changeStatus(9);
		}
		new TransactionClient(this.sip_provider, message, this).request();
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message subscribe_req) {
		this.printLog("onReceivedMessage()", 3);
		if (this.statusIs(9)) {
			this.printLog("subscription already terminated: message discarded", 3);
		} else {
			if (!subscribe_req.isRequest() || !subscribe_req.isSubscribe()) {
				this.printLog("message is not a SUBSCRIBE: message discarded", 1);
				return;
			}
			if (this.statusIs(1)) {
				this.changeStatus(2);
				this.sip_provider.removeSipProviderListener(new MethodIdentifier("SUBSCRIBE"));
			}
			this.subscribe_req = subscribe_req;
			final NameAddress nameAddress = subscribe_req.getToHeader().getNameAddress();
			final NameAddress nameAddress2 = subscribe_req.getFromHeader().getNameAddress();
			final EventHeader eventHeader = subscribe_req.getEventHeader();
			if (eventHeader != null) {
				this.event = eventHeader.getEvent();
				this.id = eventHeader.getId();
			}
			this.update(1, subscribe_req);
			this.subscribe_transaction = new TransactionServer(this.sip_provider, subscribe_req, null);
			if (this.listener != null) {
				this.listener.onDlgSubscribe(this, nameAddress, nameAddress2, this.event, this.id, subscribe_req);
			}
		}
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransFailureResponse()", 3);
		final StatusLine statusLine = message.getStatusLine();
		if (this.listener != null) {
			this.listener.onDlgNotificationFailure(this, statusLine.getCode(), statusLine.getReason(), message);
		}
	}

	@Override
	public void onTransProvisionalResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransProvisionalResponse()", 3);
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("onTransSuccessResponse()", 3);
		final StatusLine statusLine = message.getStatusLine();
		if (this.listener != null) {
			this.listener.onDlgNotificationSuccess(this, statusLine.getCode(), statusLine.getReason(), message);
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		this.printLog("onTransTimeout()", 3);
		if (!this.statusIs(9)) {
			this.changeStatus(9);
			if (this.listener != null) {
				this.listener.onDlgNotifyTimeout(this);
			}
		}
	}

	public void pending() {
		this.pending(SipStack.default_expires);
	}

	public void pending(final int n) {
		this.notify("pending", n, null, null);
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("NotifierDialog#" + this.dialog_sqn + ": " + s, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	public void refuse() {
		this.printLog("inside refuse()", 3);
		this.respond(403, SipResponses.reasonOf(403), -1, null, null, null);
	}

	public void respond(final int n, final String s, final int n2, final String s2, final String s3, final String s4) {
		this.printLog("inside respond(" + n + "," + s + ")", 3);
		NameAddress nameAddress = null;
		if (s2 != null) {
			nameAddress = new NameAddress(s2);
		}
		final Message response = BaseMessageFactory.createResponse(this.subscribe_req, n, SipResponses.reasonOf(n), nameAddress);
		if (n2 >= 0) {
			response.setExpiresHeader(new ExpiresHeader(n2));
		}
		if (s4 != null) {
			response.setBody(s3, s4);
		}
		this.respond(response);
	}

	public void respond(final Message message) {
		this.printLog("inside respond(resp)", 3);
		if (message.getStatusLine().getCode() >= 200) {
			this.update(1, message);
		}
		this.subscribe_transaction.respondWith(message);
	}

	public void terminate() {
		this.terminate(null);
	}

	public void terminate(final String reason) {
		final Message notifyRequest = MessageFactory.createNotifyRequest(this, this.event, this.id, null, null);
		final SubscriptionStateHeader subscriptionStateHeader = new SubscriptionStateHeader("terminated");
		if (reason != null) {
			subscriptionStateHeader.setReason(reason);
		}
		notifyRequest.setSubscriptionStateHeader(subscriptionStateHeader);
		this.notify(notifyRequest);
	}
}
