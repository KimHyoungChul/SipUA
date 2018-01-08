package org.zoolu.sip.transaction;

import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.ConnectionIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Timer;

public class InviteTransactionServer extends TransactionServer {
	public static boolean AUTO_TRYING;
	boolean auto_trying;
	Timer end_to;
	Timer retransmission_to;
	InviteTransactionServerListener transaction_listener;

	static {
		InviteTransactionServer.AUTO_TRYING = true;
	}

	public InviteTransactionServer(final SipProvider sipProvider, final Message message, final InviteTransactionServerListener inviteTransactionServerListener) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(inviteTransactionServerListener, this.request.getTransactionId(), this.request.getConnectionId());
		this.changeStatus(2);
		sipProvider.addSipProviderListener(this.transaction_id, this);
		if (this.auto_trying) {
			this.respondWith(BaseMessageFactory.createResponse(this.request, 100, SipResponses.reasonOf(100), null));
		}
	}

	public InviteTransactionServer(final SipProvider sipProvider, final Message message, final boolean auto_trying, final InviteTransactionServerListener inviteTransactionServerListener) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(inviteTransactionServerListener, this.request.getTransactionId(), this.request.getConnectionId());
		this.auto_trying = auto_trying;
		this.changeStatus(2);
		sipProvider.addSipProviderListener(this.transaction_id, this);
		if (auto_trying) {
			this.respondWith(BaseMessageFactory.createResponse(this.request, 100, SipResponses.reasonOf(100), null));
		}
	}

	public InviteTransactionServer(final SipProvider sipProvider, final InviteTransactionServerListener inviteTransactionServerListener) {
		super(sipProvider);
		this.init(inviteTransactionServerListener, new TransactionIdentifier("INVITE"), null);
	}

	void init(final InviteTransactionServerListener transaction_listener, final TransactionIdentifier transaction_id, final ConnectionIdentifier connection_id) {
		this.transaction_listener = transaction_listener;
		this.transaction_id = transaction_id;
		this.connection_id = connection_id;
		this.auto_trying = InviteTransactionServer.AUTO_TRYING;
		this.retransmission_to = new Timer(SipStack.retransmission_timeout, "Retransmission", this);
		this.end_to = new Timer(SipStack.transaction_timeout, "End", this);
		this.clearing_to = new Timer(SipStack.clearing_timeout, "Clearing", this);
		this.printLog("id: " + String.valueOf(transaction_id), 1);
		this.printLog("created", 1);
	}

	public boolean isWaiting() {
		return this.statusIs(1);
	}

	@Override
	public void listen() {
		this.printLog("start", 5);
		if (this.statusIs(0)) {
			this.changeStatus(1);
			this.sip_provider.addSipProviderInviteListener(this);
		}
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		if (message.isRequest()) {
			final String method = message.getRequestLine().getMethod();
			if (method.equals("INVITE")) {
				if (this.statusIs(1)) {
					this.request = new Message(message);
					this.connection_id = this.request.getConnectionId();
					this.transaction_id = this.request.getTransactionId();
					this.sip_provider.addSipProviderListener(this.transaction_id, this);
					this.sip_provider.removeSipProviderInviteListener(this);
					this.changeStatus(2);
					if (this.auto_trying) {
						this.respondWith(BaseMessageFactory.createResponse(this.request, 100, SipResponses.reasonOf(100), null));
					}
					if (this.transaction_listener != null) {
						this.transaction_listener.onTransRequest(this, message);
					}
					return;
				} else if (this.statusIs(3) || this.statusIs(4)) {
					this.sip_provider.sendMessage(this.response, this.connection_id);
					return;
				}
			}
			if (method.equals("OPTIONS")) {
				final Message response = BaseMessageFactory.createResponse(message, 200, SipResponses.reasonOf(200), null);
				response.removeServerHeader();
				response.addContactHeader(new ContactHeader(response.getToHeader().getNameAddress()), false);
				this.sip_provider.sendMessage(response, this.connection_id);
				return;
			}
			if (method.equals("ACK") && this.statusIs(4)) {
				this.retransmission_to.halt();
				this.end_to.halt();
				this.changeStatus(5);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransFailureAck(this, message);
				}
				this.clearing_to.start();
			}
		}
	}

	@Override
	public void onTimeout(final Timer timer) {
		try {
			if (timer.equals(this.retransmission_to) && this.statusIs(4)) {
				this.printLog("Retransmission timeout expired", 1);
				long max_retransmission_timeout;
				if ((max_retransmission_timeout = 2L * this.retransmission_to.getTime()) > SipStack.max_retransmission_timeout) {
					max_retransmission_timeout = SipStack.max_retransmission_timeout;
				}
				(this.retransmission_to = new Timer(max_retransmission_timeout, this.retransmission_to.getLabel(), this)).start();
				this.sip_provider.sendMessage(this.response, this.connection_id);
			}
			if (timer.equals(this.end_to) && this.statusIs(4)) {
				this.printLog("End timeout expired", 1);
				this.retransmission_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				this.transaction_listener = null;
			}
			if (timer.equals(this.clearing_to) && this.statusIs(5)) {
				this.printLog("Clearing timeout expired", 1);
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				this.transaction_listener = null;
			}
		} catch (Exception ex) {
			this.printException(ex, 1);
		}
	}

	@Override
	public void respondWith(final Message response) {
		this.response = response;
		final int code = this.response.getStatusLine().getCode();
		if (this.statusIs(2) || this.statusIs(3)) {
			this.sip_provider.sendMessage(this.response, this.connection_id);
		}
		if (code >= 100 && code < 200 && this.statusIs(2)) {
			this.changeStatus(3);
		} else {
			if (code >= 200 && code < 300 && (this.statusIs(2) || this.statusIs(3))) {
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				this.transaction_listener = null;
				return;
			}
			if (code >= 300 && code < 700 && (this.statusIs(2) || this.statusIs(3))) {
				this.changeStatus(4);
				this.retransmission_to.start();
				this.end_to.start();
			}
		}
	}

	public void setAutoTrying(final boolean auto_trying) {
		this.auto_trying = auto_trying;
	}

	@Override
	public void terminate() {
		this.retransmission_to.halt();
		this.clearing_to.halt();
		this.end_to.halt();
		if (this.statusIs(2)) {
			this.sip_provider.removeSipProviderInviteListener(this);
		} else {
			this.sip_provider.removeSipProviderListener(this.transaction_id);
		}
		this.changeStatus(7);
		this.transaction_listener = null;
	}
}
