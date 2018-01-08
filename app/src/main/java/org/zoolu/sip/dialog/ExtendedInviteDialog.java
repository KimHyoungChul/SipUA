package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.authentication.DigestAuthentication;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.Expireheader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.SupportedHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionIdentifier;
import org.zoolu.sip.transaction.InviteTransactionClient;
import org.zoolu.sip.transaction.Transaction;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionServer;

import java.util.Hashtable;

public class ExtendedInviteDialog extends InviteDialog {
	static final int MAX_ATTEMPTS = 3;
	int attempts;
	ExtendedInviteDialogListener dialog_listener;
	String next_nonce;
	String passwd;
	String qop;
	String realm;
	Hashtable<TransactionIdentifier, Transaction> transactions;
	String username;

	public ExtendedInviteDialog(final SipProvider sipProvider, final String username, final String realm, final String passwd, final ExtendedInviteDialogListener extendedInviteDialogListener) {
		super(sipProvider, extendedInviteDialogListener);
		this.init(extendedInviteDialogListener);
		this.username = username;
		this.realm = realm;
		this.passwd = passwd;
	}

	public ExtendedInviteDialog(final SipProvider sipProvider, final ExtendedInviteDialogListener extendedInviteDialogListener) {
		super(sipProvider, extendedInviteDialogListener);
		this.init(extendedInviteDialogListener);
	}

	private void init(final ExtendedInviteDialogListener dialog_listener) {
		this.dialog_listener = dialog_listener;
		this.transactions = new Hashtable<TransactionIdentifier, Transaction>();
		this.username = null;
		this.realm = null;
		this.passwd = null;
		this.next_nonce = null;
		this.qop = null;
		this.attempts = 0;
	}

	public void acceptRefer(final Message message) {
		this.printLog("inside acceptRefer(refer)", 3);
		this.respond(BaseMessageFactory.createResponse(message, 202, SipResponses.reasonOf(200), null));
	}

	public void info(final char c, final int n) {
		final Message request = BaseMessageFactory.createRequest(this, "INFO", null);
		request.setBody("application/dtmf-relay", "Signal=" + c + "\r\n+Duration=" + n);
		this.request(request);
	}

	public void notify(final int n, final String s) {
		this.notify(new StatusLine(n, s).toString());
	}

	public void notify(final String s) {
		this.request(MessageFactory.createNotifyRequest(this, "refer", null, s));
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		this.printLog("Message received: " + message.getFirstLine().substring(0, message.toString().indexOf(13)), 5);
		if (message.isResponse()) {
			super.onReceivedMessage(sipProvider, message);
			return;
		}
		if (message.isInvite() || message.isAck() || message.isCancel() || message.isBye()) {
			super.onReceivedMessage(sipProvider, message);
			return;
		}
		final TransactionServer transactionServer = new TransactionServer(this.sip_provider, message, this);
		this.transactions.put(transactionServer.getTransactionId(), transactionServer);
		if (message.isRefer()) {
			final NameAddress nameAddress = message.getReferToHeader().getNameAddress();
			NameAddress nameAddress2 = null;
			if (message.hasReferredByHeader()) {
				nameAddress2 = message.getReferredByHeader().getNameAddress();
			}
			this.dialog_listener.onDlgRefer(this, nameAddress, nameAddress2, message);
			return;
		}
		if (message.isNotify()) {
			this.respond(BaseMessageFactory.createResponse(message, 200, SipResponses.reasonOf(200), null));
			this.dialog_listener.onDlgNotify(this, message.getEventHeader().getValue(), message.getBody(), message);
			return;
		}
		this.printLog("Received alternative request " + message.getRequestLine().getMethod(), 3);
		this.dialog_listener.onDlgAltRequest(this, message.getRequestLine().getMethod(), message.getBody(), message);
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("inside onTransFailureResponse(" + transactionClient.getTransactionId() + ",msg)", 5);
		final String transactionMethod = transactionClient.getTransactionMethod();
		final StatusLine statusLine = message.getStatusLine();
		final int code = statusLine.getCode();
		final String reason = statusLine.getReason();
		final boolean b = false;
		final boolean b2 = false;
		boolean b3 = b;
		boolean b4 = b2;
		if (this.attempts < 3) {
			switch (code) {
				default: {
					b4 = b2;
					b3 = b;
					break;
				}
				case 401: {
					b3 = b;
					b4 = b2;
					if (message.hasWwwAuthenticateHeader()) {
						this.realm = message.getWwwAuthenticateHeader().getRealmParam();
						b3 = true;
						b4 = b2;
						break;
					}
					break;
				}
				case 407: {
					b3 = b;
					b4 = b2;
					if (message.hasProxyAuthenticateHeader()) {
						this.realm = message.getProxyAuthenticateHeader().getRealmParam();
						b4 = true;
						b3 = b;
						break;
					}
					break;
				}
			}
		}
		if (b3 | b4) {
			++this.attempts;
			final Message requestMessage = transactionClient.getRequestMessage();
			requestMessage.setCSeqHeader(requestMessage.getCSeqHeader().incSequenceNumber());
			final ViaHeader viaHeader = requestMessage.getViaHeader();
			viaHeader.setBranch(SipProvider.pickBranch());
			requestMessage.removeViaHeader();
			requestMessage.addViaHeader(viaHeader);
			WwwAuthenticateHeader wwwAuthenticateHeader;
			if (code == 401) {
				wwwAuthenticateHeader = message.getWwwAuthenticateHeader();
			} else {
				wwwAuthenticateHeader = message.getProxyAuthenticateHeader();
			}
			String qop;
			if (wwwAuthenticateHeader.getQopOptionsParam() != null) {
				qop = "auth";
			} else {
				qop = null;
			}
			this.qop = qop;
			final RequestLine requestLine = requestMessage.getRequestLine();
			final DigestAuthentication digestAuthentication = new DigestAuthentication(requestLine.getMethod(), requestLine.getAddress().toString(), wwwAuthenticateHeader, this.qop, null, this.username, this.passwd);
			AuthorizationHeader authorizationHeader;
			if (code == 401) {
				authorizationHeader = digestAuthentication.getAuthorizationHeader();
			} else {
				authorizationHeader = digestAuthentication.getProxyAuthorizationHeader();
			}
			requestMessage.setAuthorizationHeader(authorizationHeader);
			if (!requestMessage.hasPttExtensionHeader()) {
				requestMessage.setSupportedheader(new SupportedHeader("timer"));
				requestMessage.setSessionExpireheader(new Expireheader("90"));
			}
			this.transactions.remove(transactionClient.getTransactionId());
			final InviteTransactionClient inviteTransactionClient = new InviteTransactionClient(this.sip_provider, requestMessage, this);
			this.transactions.put(inviteTransactionClient.getTransactionId(), inviteTransactionClient);
			inviteTransactionClient.request();
			this.invite_req = requestMessage;
			return;
		}
		if (transactionMethod.equals("INVITE") || transactionMethod.equals("CANCEL") || transactionMethod.equals("BYE")) {
			super.onTransFailureResponse(transactionClient, message);
			return;
		}
		if (transactionClient.getTransactionMethod().equals("REFER")) {
			this.transactions.remove(transactionClient.getTransactionId());
			this.dialog_listener.onDlgReferResponse(this, code, reason, message);
			return;
		}
		final String body = message.getBody();
		this.transactions.remove(transactionClient.getTransactionId());
		this.dialog_listener.onDlgAltResponse(this, transactionMethod, code, reason, body, message);
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("inside onTransSuccessResponse(" + transactionClient.getTransactionId() + ",msg)", 5);
		this.attempts = 0;
		final String transactionMethod = transactionClient.getTransactionMethod();
		final StatusLine statusLine = message.getStatusLine();
		final int code = statusLine.getCode();
		final String reason = statusLine.getReason();
		if (transactionMethod.equals("INVITE") || transactionMethod.equals("CANCEL") || transactionMethod.equals("BYE")) {
			super.onTransSuccessResponse(transactionClient, message);
		} else {
			if (transactionClient.getTransactionMethod().equals("REFER")) {
				this.transactions.remove(transactionClient.getTransactionId());
				this.dialog_listener.onDlgReferResponse(this, code, reason, message);
				return;
			}
			if (!transactionMethod.equals("UPDATE")) {
				final String body = message.getBody();
				this.transactions.remove(transactionClient.getTransactionId());
				this.dialog_listener.onDlgAltResponse(this, transactionMethod, code, reason, body, message);
				return;
			}
			if (this.statusIs(6)) {
				this.listener.onDlgUpdateSuccessResponse();
			}
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		this.printLog("inside onTransTimeout(" + transactionClient.getTransactionId() + ",msg)", 5);
		final String transactionMethod = transactionClient.getTransactionMethod();
		if (transactionMethod.equals("INVITE") || transactionMethod.equals("BYE")) {
			super.onTransTimeout(transactionClient);
			return;
		}
		this.transactions.remove(transactionClient.getTransactionId());
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("ExtendedInviteDialog#" + this.dialog_sqn + ": " + s, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	public void refer(final NameAddress nameAddress) {
		this.refer(nameAddress, null);
	}

	public void refer(final NameAddress nameAddress, final NameAddress nameAddress2) {
		this.request(MessageFactory.createReferRequest(this, nameAddress, nameAddress2));
	}

	public void refuseRefer(final Message message) {
		this.printLog("inside refuseRefer(refer)", 3);
		this.respond(BaseMessageFactory.createResponse(message, 603, SipResponses.reasonOf(603), null));
	}

	public void request(final Message message) {
		final TransactionClient transactionClient = new TransactionClient(this.sip_provider, message, this);
		this.transactions.put(transactionClient.getTransactionId(), transactionClient);
		transactionClient.request();
	}

	@Override
	public void respond(final Message message) {
		this.printLog("inside respond(resp)", 3);
		final String method = message.getCSeqHeader().getMethod();
		if (method.equals("INVITE") || method.equals("CANCEL") || method.equals("BYE")) {
			super.respond(message);
			return;
		}
		final TransactionIdentifier transactionId = message.getTransactionId();
		this.printLog("transaction-id=" + transactionId, 3);
		if (this.transactions.containsKey(transactionId)) {
			this.printLog("responding", 5);
			((TransactionServer) this.transactions.get(transactionId)).respondWith(message);
			return;
		}
		this.printLog("transaction server not found; message discarded", 3);
	}
}
