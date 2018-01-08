package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.ConnectionIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Timer;

public class AckTransactionServer extends Transaction {
	Message response;
	Timer retransmission_to;
	AckTransactionServerListener transaction_listener;
	Timer transaction_to;

	public AckTransactionServer(final SipProvider sipProvider, final Message response, final AckTransactionServerListener ackTransactionServerListener) {
		super(sipProvider);
		this.response = response;
		this.init(ackTransactionServerListener, new TransactionIdentifier("ACK"), null);
	}

	public AckTransactionServer(final SipProvider sipProvider, final ConnectionIdentifier connectionIdentifier, final Message response, final AckTransactionServerListener ackTransactionServerListener) {
		super(sipProvider);
		this.response = response;
		this.init(ackTransactionServerListener, new TransactionIdentifier("ACK"), connectionIdentifier);
	}

	void init(final AckTransactionServerListener transaction_listener, final TransactionIdentifier transaction_id, final ConnectionIdentifier connection_id) {
		this.transaction_listener = transaction_listener;
		this.transaction_id = transaction_id;
		this.connection_id = connection_id;
		this.transaction_to = new Timer(SipStack.transaction_timeout, "Transaction", this);
		this.retransmission_to = new Timer(SipStack.retransmission_timeout, "Retransmission", this);
		this.printLog("id: " + String.valueOf(transaction_id), 1);
		this.printLog("created", 1);
	}

	@Override
	public void onTimeout(final Timer timer) {
		try {
			if (timer.equals(this.retransmission_to) && this.statusIs(3)) {
				this.printLog("Retransmission timeout expired", 1);
				long max_retransmission_timeout;
				if ((max_retransmission_timeout = 2L * this.retransmission_to.getTime()) > SipStack.max_retransmission_timeout) {
					max_retransmission_timeout = SipStack.max_retransmission_timeout;
				}
				(this.retransmission_to = new Timer(max_retransmission_timeout, this.retransmission_to.getLabel(), this)).start();
				this.sip_provider.sendMessage(this.response, this.connection_id);
			}
			if (timer.equals(this.transaction_to) && this.statusIs(3)) {
				this.printLog("Transaction timeout expired", 1);
				this.changeStatus(7);
				if (this.transaction_listener != null) {
					this.transaction_listener.onTransAckTimeout(this);
				}
				this.transaction_listener = null;
			}
		} catch (Exception ex) {
			this.printException(ex, 1);
		}
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("AckTransactionServer#" + this.transaction_sqn + ": " + s, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	public void respond() {
		this.printLog("start", 5);
		this.changeStatus(3);
		this.transaction_to.start();
		this.retransmission_to.start();
		this.sip_provider.sendMessage(this.response, this.connection_id);
	}

	@Override
	public void terminate() {
		this.retransmission_to.halt();
		this.transaction_to.halt();
		this.changeStatus(7);
		this.transaction_listener = null;
	}
}
