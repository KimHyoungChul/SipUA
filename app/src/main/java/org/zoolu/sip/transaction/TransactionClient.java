package org.zoolu.sip.transaction;

import org.zoolu.sip.header.Header;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Timer;

public class TransactionClient extends Transaction {
	Timer clearing_to;
	Timer retransmission_to;
	TransactionClientListener transaction_listener;
	Timer transaction_to;

	protected TransactionClient(final SipProvider sipProvider) {
		super(sipProvider);
		this.transaction_listener = null;
	}

	public TransactionClient(final SipProvider sipProvider, final Message message, final TransactionClientListener transactionClientListener) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(transactionClientListener, this.request.getTransactionId());
	}

	public TransactionClient(final SipProvider sipProvider, final Message message, final TransactionClientListener transactionClientListener, final int n) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(transactionClientListener, this.request.getTransactionId());
		this.transaction_to = new Timer(n, "Transaction", this);
	}

	void init(final TransactionClientListener transaction_listener, final TransactionIdentifier transaction_id) {
		this.transaction_listener = transaction_listener;
		this.transaction_id = transaction_id;
		if (this.request.isMessage()) {
			final Header header = this.request.getHeader("Content-Type");
			if (header != null && header.getValue().equalsIgnoreCase("text/customGroup")) {
				this.retransmission_to = new Timer(SipStack.retransmission_timeout, "Retransmission", this);
			} else {
				this.retransmission_to = new Timer(SipStack.retransmission_timeout * 2L, "Retransmission", this);
			}
		} else {
			this.retransmission_to = new Timer(SipStack.retransmission_timeout, "Retransmission", this);
		}
		final Header header2 = this.request.getHeader("Content-Type");
		if (this.request.isMessage() && header2 != null && header2.getValue().equalsIgnoreCase("text/customGroup")) {
			this.transaction_to = new Timer(SipStack.transaction_timeout * 2L, "Transaction", this);
		} else {
			this.transaction_to = new Timer(SipStack.transaction_timeout, "Transaction", this);
		}
		this.clearing_to = new Timer(SipStack.clearing_timeout, "Clearing", this);
		this.printLog("id: " + String.valueOf(transaction_id), 1);
		this.printLog("created", 1);
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		if (message.isResponse()) {
			final int code = message.getStatusLine().getCode();
			if (code >= 100 && code < 200 && (this.statusIs(2) || this.statusIs(3))) {
				this.retransmission_to.halt();
				if (this.statusIs(2)) {
					this.changeStatus(3);
				}
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransProvisionalResponse(this, message);
				}
			} else if (code >= 200 && code < 700 && (this.statusIs(2) || this.statusIs(3))) {
				this.retransmission_to.halt();
				this.transaction_to.halt();
				this.changeStatus(4);
				if (code < 300) {
					if (this.transaction_listener != null) {
						this.transaction_listener.onTransSuccessResponse(this, message);
					}
				} else if (this.transaction_listener != null) {
					this.transaction_listener.onTransFailureResponse(this, message);
				}
				this.transaction_listener = null;
				this.clearing_to.start();
			}
		}
	}

	@Override
	public void onTimeout(final Timer timer) {
		try {
			if (timer.equals(this.retransmission_to) && (this.statusIs(2) || this.statusIs(3))) {
				this.printLog("Retransmission timeout expired", 1);
				this.sip_provider.sendMessage(this.request);
				long max_retransmission_timeout = 2L * this.retransmission_to.getTime();
				if (max_retransmission_timeout > SipStack.max_retransmission_timeout || this.statusIs(3)) {
					max_retransmission_timeout = SipStack.max_retransmission_timeout;
				}
				(this.retransmission_to = new Timer(max_retransmission_timeout, this.retransmission_to.getLabel(), this)).start();
			}
			if (timer.equals(this.transaction_to)) {
				this.printLog("Transaction timeout expired", 1);
				this.retransmission_to.halt();
				this.clearing_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransTimeout(this);
				}
				this.transaction_listener = null;
			}
			if (timer.equals(this.clearing_to)) {
				this.printLog("Clearing timeout expired", 1);
				this.retransmission_to.halt();
				this.transaction_to.halt();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.changeStatus(7);
			}
		} catch (Exception ex) {
			this.printException(ex, 1);
		}
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("TransactionClient#" + this.transaction_sqn + ": " + s, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	public void request() {
		this.printLog("start", 5);
		this.changeStatus(2);
		this.retransmission_to.start();
		this.transaction_to.start();
		this.sip_provider.addSipProviderListener(this.transaction_id, this);
		this.connection_id = this.sip_provider.sendMessage(this.request);
	}

	@Override
	public void terminate() {
		if (!this.statusIs(7)) {
			this.retransmission_to.halt();
			this.transaction_to.halt();
			this.clearing_to.halt();
			this.sip_provider.removeSipProviderListener(this.transaction_id);
			this.changeStatus(7);
			this.transaction_listener = null;
		}
	}
}
