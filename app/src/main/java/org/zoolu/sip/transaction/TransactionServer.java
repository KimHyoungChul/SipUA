package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.ConnectionIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Timer;

public class TransactionServer extends Transaction {
	Timer clearing_to;
	Message response;
	TransactionServerListener transaction_listener;

	protected TransactionServer(final SipProvider sipProvider) {
		super(sipProvider);
		this.transaction_listener = null;
		this.response = null;
	}

	public TransactionServer(final SipProvider sipProvider, final String s, final TransactionServerListener transactionServerListener) {
		super(sipProvider);
		this.init(transactionServerListener, new TransactionIdentifier(s), null);
	}

	public TransactionServer(final SipProvider sipProvider, final Message message, final TransactionServerListener transactionServerListener) {
		super(sipProvider);
		this.request = new Message(message);
		this.init(transactionServerListener, this.request.getTransactionId(), this.request.getConnectionId());
		this.printLog("start", 5);
		this.changeStatus(2);
		this.sip_provider.addSipProviderListener(this.transaction_id, this);
	}

	void init(final TransactionServerListener transaction_listener, final TransactionIdentifier transaction_id, final ConnectionIdentifier connection_id) {
		this.transaction_listener = transaction_listener;
		this.transaction_id = transaction_id;
		this.connection_id = connection_id;
		this.response = null;
		this.clearing_to = new Timer(SipStack.transaction_timeout, "Clearing", this);
		this.printLog("id: " + String.valueOf(transaction_id), 1);
		this.printLog("created", 1);
	}

	public void listen() {
		if (this.statusIs(0)) {
			this.printLog("start", 5);
			this.changeStatus(1);
			this.sip_provider.addSipProviderListener(this.transaction_id, this);
		}
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		if (message.isRequest()) {
			if (this.statusIs(1)) {
				this.request = new Message(message);
				this.connection_id = message.getConnectionId();
				this.sip_provider.removeSipProviderListener(this.transaction_id);
				this.transaction_id = this.request.getTransactionId();
				this.sip_provider.addSipProviderListener(this.transaction_id, this);
				this.changeStatus(2);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransRequest(this, message);
				}
			} else if (this.statusIs(3) || this.statusIs(4)) {
				this.printLog("response retransmission", 5);
				this.sip_provider.sendMessage(this.response, this.connection_id);
			}
		}
	}

	@Override
	public void onTimeout(final Timer timer) {
		try {
			if (timer.equals(this.clearing_to)) {
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
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("TransactionServer#" + this.transaction_sqn + ": " + s, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	public void respondWith(final Message response) {
		this.response = response;
		if (this.statusIs(2) || this.statusIs(3)) {
			this.sip_provider.sendMessage(this.response, this.connection_id);
			final int code = this.response.getStatusLine().getCode();
			if (code >= 100 && code < 200 && this.statusIs(2)) {
				this.changeStatus(3);
			}
			if (code >= 200 && code < 700) {
				this.changeStatus(4);
				this.clearing_to.start();
			}
		}
	}

	@Override
	public void terminate() {
		if (!this.statusIs(7)) {
			this.clearing_to.halt();
			this.sip_provider.removeSipProviderListener(this.transaction_id);
			this.changeStatus(7);
			this.transaction_listener = null;
		}
	}
}
