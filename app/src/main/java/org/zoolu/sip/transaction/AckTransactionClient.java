package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

public class AckTransactionClient extends Transaction {
	TransactionClientListener transaction_listener;

	public AckTransactionClient(final SipProvider sipProvider, final Message message, final TransactionClientListener transaction_listener) {
		super(sipProvider);
		this.request = new Message(message);
		this.transaction_listener = transaction_listener;
		this.transaction_id = this.request.getTransactionId();
		this.printLog("id: " + String.valueOf(this.transaction_id), 1);
		this.printLog("created", 1);
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("AckTransactionClient#" + this.transaction_sqn + ": " + s, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	public void request() {
		this.printLog("start", 5);
		this.sip_provider.sendMessage(this.request);
		this.changeStatus(7);
		this.transaction_listener = null;
	}

	@Override
	public void terminate() {
		this.changeStatus(7);
		this.transaction_listener = null;
	}
}
