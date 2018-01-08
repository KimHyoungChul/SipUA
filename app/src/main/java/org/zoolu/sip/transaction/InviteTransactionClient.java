package org.zoolu.sip.transaction;

import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Timer;

public class InviteTransactionClient extends TransactionClient {
	Message ack;
	Timer end_to;
	Timer invite_transaction_to;
	TransactionClientListener transaction_listener;

	public InviteTransactionClient(final SipProvider sipProvider, final Message message, final TransactionClientListener transactionClientListener) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(transactionClientListener, this.request.getTransactionId());
	}

	@Override
	void init(final TransactionClientListener transaction_listener, final TransactionIdentifier transaction_id) {
		this.transaction_listener = transaction_listener;
		this.transaction_id = transaction_id;
		this.ack = null;
		this.retransmission_to = new Timer(SipStack.retransmission_timeout, "Retransmission", this);
		this.transaction_to = new Timer(SipStack.transaction_timeout, "Transaction", this);
		this.end_to = new Timer(SipStack.transaction_timeout, "End", this);
		this.invite_transaction_to = new Timer(SipStack.invite_transaction_timeout, "InviteTransaction", this);
		this.printLog("id: " + String.valueOf(transaction_id), 1);
		this.printLog("created", 1);
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		if (message.isResponse()) {
			final int code = message.getStatusLine().getCode();
			if (code >= 100 && code < 200 && (this.statusIs(2) || this.statusIs(3))) {
				if (this.statusIs(2)) {
					this.retransmission_to.halt();
					this.transaction_to.halt();
					this.changeStatus(3);
				}
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransProvisionalResponse(this, message);
				}
			} else if (code >= 300 && code < 700 && (this.statusIs(2) || this.statusIs(3) || this.statusIs(4))) {
				if (this.statusIs(2) || this.statusIs(3)) {
					this.retransmission_to.halt();
					this.transaction_to.halt();
					this.invite_transaction_to.halt();
					this.ack = BaseMessageFactory.createNon2xxAckRequest(this.sip_provider, this.request, message);
					this.changeStatus(4);
					this.connection_id = this.sip_provider.sendMessage(this.ack);
					if (this.transaction_listener != null) {
						this.transaction_listener.onTransFailureResponse(this, message);
					}
					this.transaction_listener = null;
					this.end_to.start();
					return;
				}
				this.sip_provider.sendMessage(this.ack);
			} else if (code >= 200 && code < 300 && (this.statusIs(2) || this.statusIs(3))) {
				this.retransmission_to.halt();
				this.transaction_to.halt();
				this.invite_transaction_to.halt();
				this.end_to.halt();
				this.changeStatus(7);
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransSuccessResponse(this, message);
				}
				this.transaction_listener = null;
			}
		}
	}

	@Override
	public void onTimeout(final Timer timer) {
		try {
			if (timer.equals(this.retransmission_to) && this.statusIs(2)) {
				this.printLog("Retransmission timeout expired", 1);
				this.sip_provider.sendMessage(this.request);
				(this.retransmission_to = new Timer(2L * this.retransmission_to.getTime(), this.retransmission_to.getLabel(), this)).start();
			}
			if (timer.equals(this.transaction_to)) {
				this.printLog("Transaction timeout expired", 1);
				this.retransmission_to.halt();
				this.invite_transaction_to.halt();
				this.end_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransTimeout(this);
				}
				this.transaction_listener = null;
			}
			if (timer.equals(this.invite_transaction_to)) {
				this.printLog("Invite Transaction timeout expired", 1);
				this.retransmission_to.halt();
				this.transaction_to.halt();
				this.end_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransTimeout(this);
				}
				this.transaction_listener = null;
			}
			if (timer.equals(this.end_to)) {
				this.printLog("End timeout expired", 1);
				this.retransmission_to.halt();
				this.transaction_to.halt();
				this.invite_transaction_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				this.transaction_listener = null;
			}
		} catch (Exception ex) {
			this.printException(ex, 1);
		}
	}

	@Override
	public void request() {
		this.printLog("start", 5);
		this.changeStatus(2);
		this.retransmission_to.start();
		this.transaction_to.start();
		this.invite_transaction_to.start();
		this.sip_provider.addSipProviderListener(this.transaction_id, this);
		this.connection_id = this.sip_provider.sendMessage(this.request);
	}

	@Override
	public void terminate() {
		if (!this.statusIs(7)) {
			this.retransmission_to.halt();
			this.transaction_to.halt();
			this.invite_transaction_to.halt();
			this.end_to.halt();
			this.sip_provider.removeSipProviderListener(this.transaction_id);
			this.changeStatus(7);
			this.transaction_listener = null;
		}
	}
}
