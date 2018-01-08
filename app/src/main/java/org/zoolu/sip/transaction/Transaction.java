package org.zoolu.sip.transaction;

import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.ConnectionIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.tools.Log;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;

public abstract class Transaction implements SipProviderListener, TimerListener {
	static final int STATE_COMPLETED = 4;
	static final int STATE_CONFIRMED = 5;
	static final int STATE_IDLE = 0;
	static final int STATE_PROCEEDING = 3;
	static final int STATE_TERMINATED = 7;
	static final int STATE_TRYING = 2;
	static final int STATE_WAITING = 1;
	protected static int transaction_counter;
	ConnectionIdentifier connection_id;
	Log log;
	Message request;
	SipProvider sip_provider;
	int status;
	TransactionIdentifier transaction_id;
	int transaction_sqn;

	static {
		Transaction.transaction_counter = 0;
	}

	protected Transaction(final SipProvider sip_provider) {
		this.sip_provider = sip_provider;
		this.log = sip_provider.getLog();
		this.transaction_id = null;
		this.request = null;
		this.connection_id = null;
		final int transaction_counter = Transaction.transaction_counter;
		Transaction.transaction_counter = transaction_counter + 1;
		this.transaction_sqn = transaction_counter;
		this.status = 0;
	}

	static String getStatus(final int n) {
		switch (n) {
			default: {
				return null;
			}
			case 0: {
				return "T_Idle";
			}
			case 1: {
				return "T_Waiting";
			}
			case 2: {
				return "T_Trying";
			}
			case 3: {
				return "T_Proceeding";
			}
			case 4: {
				return "T_Completed";
			}
			case 5: {
				return "T_Confirmed";
			}
			case 7: {
				return "T_Terminated";
			}
		}
	}

	void changeStatus(final int status) {
		this.status = status;
		this.printLog("changed transaction state: " + this.getStatus(), 3);
	}

	public ConnectionIdentifier getConnectionId() {
		return this.connection_id;
	}

	public Message getRequestMessage() {
		return this.request;
	}

	public SipProvider getSipProvider() {
		return this.sip_provider;
	}

	String getStatus() {
		return getStatus(this.status);
	}

	public TransactionIdentifier getTransactionId() {
		return this.transaction_id;
	}

	public String getTransactionMethod() {
		return this.request.getTransactionMethod();
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
	}

	@Override
	public void onTimeout(final Timer timer) {
	}

	protected void printException(final Exception ex, final int n) {
		if (this.log != null) {
			this.log.printException(ex, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("Transaction#" + this.transaction_sqn + ": " + s, SipStack.LOG_LEVEL_TRANSACTION + n);
		}
	}

	protected void printWarning(final String s, final int n) {
		this.printLog("WARNING: " + s, n);
	}

	boolean statusIs(final int n) {
		return this.status == n;
	}

	public abstract void terminate();
}
