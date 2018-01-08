package org.zoolu.sip.dialog;

import android.text.TextUtils;

import com.zed3.log.MyLog;
import com.zed3.sipua.ui.Settings;
import com.zed3.video.VideoManagerService;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.AntaExtensionHeader;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.Expireheader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.SupportedHeader;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.AckTransactionClient;
import org.zoolu.sip.transaction.AckTransactionServer;
import org.zoolu.sip.transaction.AckTransactionServerListener;
import org.zoolu.sip.transaction.InviteTransactionClient;
import org.zoolu.sip.transaction.InviteTransactionServer;
import org.zoolu.sip.transaction.InviteTransactionServerListener;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

import java.util.Vector;

public class InviteDialog extends Dialog implements TransactionClientListener, InviteTransactionServerListener, AckTransactionServerListener, SipProviderListener {
	protected static final int D_ACCEPTED = 5;
	protected static final int D_BYED = 8;
	protected static final int D_BYEING = 7;
	protected static final int D_CALL = 6;
	protected static final int D_CLOSE = 9;
	protected static final int D_INIT = 0;
	protected static final int D_INVITED = 3;
	protected static final int D_INVITING = 2;
	protected static final int D_REFUSED = 4;
	protected static final int D_ReACCEPTED = 15;
	protected static final int D_ReINVITED = 13;
	protected static final int D_ReINVITING = 12;
	protected static final int D_ReREFUSED = 14;
	protected static final int D_ReWAITING = 11;
	protected static final int D_WAITING = 1;
	private static int call_direction;
	Message ack_req;
	AckTransactionServer ack_ts;
	TransactionServer bye_ts;
	boolean invite_offer;
	Message invite_req;
	InviteTransactionServer invite_ts;
	InviteDialogListener listener;

	static {
		InviteDialog.call_direction = -1;
	}

	public InviteDialog(final SipProvider sipProvider, final InviteDialogListener inviteDialogListener) {
		super(sipProvider);
		this.init(inviteDialogListener);
	}

	public InviteDialog(final SipProvider sipProvider, final Message invite_req, final InviteDialogListener inviteDialogListener) {
		super(sipProvider);
		this.init(inviteDialogListener);
		this.changeStatus(3);
		this.invite_req = invite_req;
		this.invite_ts = new InviteTransactionServer(sipProvider, this.invite_req, this);
		this.update(1, this.invite_req);
	}

	private void bye(final boolean b) {
		this.printLog("inside bye()", 3);
		if (this.statusIs(6)) {
			final Message byeRequest = BaseMessageFactory.createByeRequest(this);
			if (b) {
				byeRequest.setHeader(new Header("Ptt-Extension", "3ghandset"));
			}
			this.bye(byeRequest);
		}
	}

	private void init(final InviteDialogListener listener) {
		this.log = this.sip_provider.getLog();
		this.listener = listener;
		this.invite_req = null;
		this.ack_req = null;
		this.invite_offer = true;
		this.changeStatus(0);
	}

	private void invite(final String s, final String s2, final String s3, final String s4, final String s5, final String s6, final boolean b) {
		this.printLog("inside invite(callee,caller,contact,sdp)", 3);
		if (!this.statusIs(0)) {
			return;
		}
		final NameAddress nameAddress = new NameAddress(s);
		final NameAddress nameAddress2 = new NameAddress(s3);
		final SipURL address = nameAddress.getAddress();
		NameAddress nameAddress3;
		if (s4 != null) {
			if (s4.indexOf("sip:") >= 0) {
				nameAddress3 = new NameAddress(s4);
			} else {
				nameAddress3 = new NameAddress(new SipURL(s4, this.sip_provider.getViaAddress(), this.sip_provider.getPort()));
			}
		} else {
			nameAddress3 = nameAddress2;
		}
		final Message inviteRequest = BaseMessageFactory.createInviteRequest(this.sip_provider, address, nameAddress, nameAddress2, nameAddress3, s5, s6);
		if (!b) {
			inviteRequest.setHeader(new Header("Anta-Extension", "conference " + s2));
		} else {
			inviteRequest.setHeader(new Header("Anta-Extension", "groupbroadcast " + s2));
		}
		this.invite(inviteRequest);
	}

	private void invite(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2, final String s6, final String s7) {
		this.invite(s, s2, s3, s4, s5, b, b2, s6, s7, null);
	}

	private void invite(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2, final String s6, final String s7, final String s8) {
		this.printLog("inside invite(callee,caller,contact,sdp)", 3);
		if (!this.statusIs(0)) {
			return;
		}
		final NameAddress nameAddress = new NameAddress(s);
		final NameAddress nameAddress2 = new NameAddress(s2);
		final SipURL address = nameAddress.getAddress();
		NameAddress nameAddress3;
		if (s3 != null) {
			if (s3.indexOf("sip:") >= 0) {
				nameAddress3 = new NameAddress(s3);
			} else {
				nameAddress3 = new NameAddress(new SipURL(s3, this.sip_provider.getViaAddress(), this.sip_provider.getPort()));
			}
		} else {
			nameAddress3 = nameAddress2;
		}
		final Message inviteRequest = BaseMessageFactory.createInviteRequest(this.sip_provider, address, nameAddress, nameAddress2, nameAddress3, s4, s5);
		if (b) {
			if (s6 == null) {
				if (!b2) {
					inviteRequest.setHeader(new Header("Ptt-Extension", "3ghandset create"));
				} else {
					inviteRequest.setHeader(new Header("Ptt-Extension", "3ghandset rejoin"));
				}
			} else {
				final Vector<Header> vector = new Vector<Header>();
				vector.add(new Header("Ptt-Extension", "3ghandset tmpgrpcreate " + s6));
				vector.add(new Header("Ptt-Member", s7));
				inviteRequest.addHeaders(vector, false);
			}
		} else {
			final VideoManagerService default1 = VideoManagerService.getDefault();
			if (default1.isCurrentVideoMonitor() || default1.isCurrentVideoUpload()) {
				inviteRequest.setHeader(new AntaExtensionHeader(default1.getCurrentVideoParameter()));
			} else if (default1.isVideoOutgoingCall() && default1.isCurrentVideoTRANSCRIBE()) {
				inviteRequest.setHeader(new AntaExtensionHeader("VideoRecord"));
			}
		}
		if (!TextUtils.isEmpty((CharSequence) s8) && s8.equals("Emergency")) {
			inviteRequest.setHeader(new Header("ptt-emergency-call", Settings.getUserName()));
		}
		this.invite(inviteRequest);
	}

	private void inviteWithoutOffer(final String s, final String s2, final String s3, final boolean b, final boolean b2, final String s4, final String s5) {
		this.invite_offer = false;
		if (!b) {
			this.invite(s, s2, s3, null, null);
			return;
		}
		if (s4 == null) {
			this.groupinvite(s, s2, s3, null, null, b2);
			return;
		}
		this.tempGrpInvite(s, s2, s3, null, null, b2, s4, s5);
	}

	public void accept(final String s, final String s2) {
		this.printLog("inside accept(sdp)", 3);
		this.respond(200, SipResponses.reasonOf(200), s, s2);
	}

	public void ackWithAnswer(final String s, final String s2) {
		if (s != null) {
			this.setLocalContact(new NameAddress(s));
		}
		this.ackWithAnswer(BaseMessageFactory.create2xxAckRequest(this, s2));
	}

	public void ackWithAnswer(final Message ack_req) {
		this.ack_req = ack_req;
		this.invite_offer = true;
		new AckTransactionClient(this.sip_provider, ack_req, null).request();
	}

	public void antaInvite(final String s, final String s2, final String s3, final String s4, final String s5, final String s6, final boolean b) {
		this.invite(s, s2, s3, s4, s5, s6, b);
	}

	public void antaInviteWithoutOffer(final String s, final String s2, final String s3, final String s4, final boolean b) {
		this.invite_offer = false;
		this.antaInvite(s, s2, s3, s4, null, null, b);
	}

	public void busy() {
		MyLog.e("eeeeeeeeee", "here called222222222222222");
		this.refuse(486, SipResponses.reasonOf(486));
	}

	public void bye() {
		this.bye(false);
	}

	public void bye(final Message message) {
		this.printLog("inside bye(bye)", 3);
		if (this.statusIs(6)) {
			this.changeStatus(7);
			new TransactionClient(this.sip_provider, message, this).request();
		}
	}

	public void cancel() {
		this.printLog("inside cancel()", 3);
		if (this.statusIs(2) || this.statusIs(12)) {
			this.cancel(BaseMessageFactory.createCancelRequest(this.invite_req, this));
			this.changeStatus(9);
		} else if (this.statusIs(1) || this.statusIs(11)) {
			this.invite_ts.terminate();
			this.changeStatus(9);
		}
	}

	public void cancel(final Message message) {
		this.printLog("inside cancel(cancel)", 3);
		if (this.statusIs(2) || this.statusIs(12)) {
			new TransactionClient(this.sip_provider, message, null).request();
		} else if (this.statusIs(1) || this.statusIs(11)) {
			this.invite_ts.terminate();
		}
	}

	@Override
	protected void changeStatus(final int n) {
		if (n == 2) {
			InviteDialog.call_direction = 0;
		} else if (n == 3) {
			InviteDialog.call_direction = 1;
		}
		super.changeStatus(n);
	}

	public int getCallDirection() {
		return InviteDialog.call_direction;
	}

	public Message getInviteMessage() {
		return this.invite_req;
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
				return "D_INVITING";
			}
			case 3: {
				return "D_INVITED";
			}
			case 4: {
				return "D_REFUSED";
			}
			case 5: {
				return "D_ACCEPTED";
			}
			case 6: {
				return "D_CALL";
			}
			case 11: {
				return "D_ReWAITING";
			}
			case 12: {
				return "D_ReINVITING";
			}
			case 13: {
				return "D_ReINVITED";
			}
			case 14: {
				return "D_ReREFUSED";
			}
			case 15: {
				return "D_ReACCEPTED";
			}
			case 7: {
				return "D_BYEING";
			}
			case 8: {
				return "D_BYED";
			}
			case 9: {
				return "D_CLOSE";
			}
		}
	}

	public void groupbye() {
		this.bye(true);
	}

	public void groupinvite(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b) {
		this.invite(s, s2, s3, s4, s5, true, b, null, null);
	}

	public void groupinviteWithoutOffer(final String s, final String s2, final String s3, final boolean b) {
		this.invite_offer = false;
		this.inviteWithoutOffer(s, s2, s3, true, b, null, null);
	}

	public void invite(final String s, final String s2, final String s3, final String s4, final String s5) {
		this.invite(s, s2, s3, s4, s5, null);
	}

	public void invite(final String s, final String s2, final String s3, final String s4, final String s5, final String s6) {
		this.invite(s, s2, s3, s4, s5, false, false, null, null, s6);
	}

	public void invite(final Message invite_req) {
		this.printLog("inside invite(invite)", 3);
		if (!this.statusIs(0)) {
			return;
		}
		this.changeStatus(2);
		this.update(0, this.invite_req = invite_req);
		new InviteTransactionClient(this.sip_provider, this.invite_req, this).request();
	}

	public void inviteWithoutOffer(final String s, final String s2, final String s3) {
		this.inviteWithoutOffer(s, s2, s3, this.invite_offer = false, false, null, null);
	}

	public void inviteWithoutOffer(final Message message) {
		this.invite_offer = false;
		this.invite(message);
	}

	@Override
	public boolean isConfirmed() {
		return this.status >= 5 && this.status < 9;
	}

	@Override
	public boolean isEarly() {
		return this.status < 5;
	}

	public boolean isInInviting() {
		return this.statusIs(2);
	}

	public boolean isSessionActive() {
		return this.status == 6 || this.status == 5;
	}

	public boolean isSessionClosed() {
		return this.status == 8 || this.status == 4 || this.status == 14 || this.status == 9;
	}

	@Override
	public boolean isTerminated() {
		return this.status == 9;
	}

	public boolean isWaitingOrReWaiting() {
		boolean b = true;
		if (!this.statusIs(1)) {
			b = b;
			if (!this.statusIs(11)) {
				b = false;
			}
		}
		return b;
	}

	public void listen() {
		if (!this.statusIs(0)) {
			return;
		}
		this.changeStatus(1);
		(this.invite_ts = new InviteTransactionServer(this.sip_provider, this)).listen();
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message invite_req) {
		this.printLog("inside onReceivedMessage(sip_provider,message)", 3);
		if (invite_req.isRequest() && !invite_req.isAck() && !invite_req.isCancel() && invite_req.getCSeqHeader().getSequenceNumber() <= this.getRemoteCSeq()) {
			this.printLog("Request message is too late (CSeq too small): Message discarded", 1);
		} else if (invite_req.isRequest() && invite_req.isInvite()) {
			this.verifyStatus(this.statusIs(0) || this.statusIs(6));
			if (this.statusIs(0)) {
				this.changeStatus(3);
			} else {
				this.changeStatus(13);
			}
			this.invite_req = invite_req;
			this.invite_ts = new InviteTransactionServer(sipProvider, this.invite_req, this);
			this.update(1, this.invite_req);
			if (this.statusIs(3)) {
				this.listener.onDlgInvite(this, this.invite_req.getToHeader().getNameAddress(), this.invite_req.getFromHeader().getNameAddress(), this.invite_req.getBody(), this.invite_req);
				return;
			}
			this.listener.onDlgReInvite(this, this.invite_req.getBody(), this.invite_req);
		} else if (invite_req.isRequest() && invite_req.isAck()) {
			if (this.verifyStatus(this.statusIs(5) || this.statusIs(15))) {
				this.changeStatus(6);
				this.ack_ts.terminate();
				this.listener.onDlgAck(this, invite_req.getBody(), invite_req);
				this.listener.onDlgCall(this);
			}
		} else if (invite_req.isResponse()) {
			if (this.verifyStatus(this.statusIs(6))) {
				final int code = invite_req.getStatusLine().getCode();
				this.verifyThat(code >= 200 && code < 300, "code 2xx was expected");
				if (this.ack_req != null) {
					new AckTransactionClient(sipProvider, this.ack_req, null).request();
				}
			}
		} else if (invite_req.isRequest() && invite_req.isBye()) {
			if (this.verifyStatus(this.statusIs(6) || this.statusIs(7))) {
				this.changeStatus(8);
				this.bye_ts = new TransactionServer(sipProvider, invite_req, this);
				this.respond(BaseMessageFactory.createResponse(invite_req, 200, SipResponses.reasonOf(200), null));
				this.listener.onDlgBye(this, invite_req);
				this.changeStatus(9);
				this.listener.onDlgClose(this);
			}
		} else if (invite_req.isRequest() && invite_req.isCancel()) {
			if (this.verifyStatus(this.statusIs(3) || this.statusIs(13))) {
				new TransactionServer(sipProvider, invite_req, null).respondWith(BaseMessageFactory.createResponse(invite_req, 200, SipResponses.reasonOf(200), null));
				this.respond(BaseMessageFactory.createResponse(this.invite_req, 487, SipResponses.reasonOf(487), null));
				this.listener.onDlgCancel(this, invite_req);
			}
		} else if (invite_req.isRequest()) {
			new TransactionServer(sipProvider, invite_req, null).respondWith(BaseMessageFactory.createResponse(invite_req, 405, SipResponses.reasonOf(405), null));
		}
	}

	@Override
	public void onTransAckTimeout(final AckTransactionServer ackTransactionServer) {
		this.printLog("inside onAckSrvTimeout(ts)", 5);
		if (!this.verifyStatus(this.statusIs(5) || this.statusIs(15) || this.statusIs(4) || this.statusIs(14))) {
			return;
		}
		this.printLog("No ACK received..", 1);
	}

	@Override
	public void onTransFailureAck(final InviteTransactionServer inviteTransactionServer, final Message message) {
		this.printLog("inside onTransFailureAck(ts,msg)", 5);
		if (!this.verifyStatus(this.statusIs(4) || this.statusIs(14))) {
			return;
		}
		if (this.statusIs(14)) {
			this.changeStatus(6);
			return;
		}
		this.changeStatus(9);
		this.listener.onDlgClose(this);
	}

	@Override
	public void onTransFailureResponse(final TransactionClient transactionClient, final Message message) {
		boolean b = true;
		final boolean b2 = true;
		this.printLog("inside onTransFailureResponse(" + transactionClient.getTransactionId() + ",msg)", 5);
		if (transactionClient.getTransactionMethod().equals("INVITE")) {
			if (this.verifyStatus(this.statusIs(2) || this.statusIs(12))) {
				final StatusLine statusLine = message.getStatusLine();
				final int code = statusLine.getCode();
				this.verifyThat(code >= 300 && code < 700 && b2, "error code was expected");
				if (this.statusIs(12)) {
					this.changeStatus(6);
					this.listener.onDlgReInviteFailureResponse(this, code, statusLine.getReason(), message);
					return;
				}
				this.changeStatus(9);
				if (code >= 300 && code < 400) {
					this.listener.onDlgInviteRedirectResponse(this, code, statusLine.getReason(), message.getContacts(), message);
				} else {
					this.listener.onDlgInviteFailureResponse(this, code, statusLine.getReason(), message);
				}
				this.listener.onDlgClose(this);
			}
		} else if (transactionClient.getTransactionMethod().equals("BYE") && this.verifyStatus(this.statusIs(7))) {
			final StatusLine statusLine2 = message.getStatusLine();
			final int code2 = statusLine2.getCode();
			if (code2 < 300 || code2 >= 700) {
				b = false;
			}
			this.verifyThat(b, "error code was expected");
			this.changeStatus(6);
			this.listener.onDlgByeFailureResponse(this, code2, statusLine2.getReason(), message);
		}
	}

	@Override
	public void onTransProvisionalResponse(final TransactionClient transactionClient, final Message message) {
		this.printLog("inside onTransProvisionalResponse(tc,mdg)", 5);
		if (transactionClient.getTransactionMethod().equals("INVITE")) {
			final StatusLine statusLine = message.getStatusLine();
			this.listener.onDlgInviteProvisionalResponse(this, statusLine.getCode(), statusLine.getReason(), message.getBody(), message);
		}
	}

	@Override
	public void onTransRequest(final TransactionServer transactionServer, final Message invite_req) {
		this.printLog("inside onTransRequest(ts,msg)", 5);
		if (!transactionServer.getTransactionMethod().equals("INVITE") || !this.verifyStatus(this.statusIs(1))) {
			return;
		}
		this.changeStatus(3);
		this.update(1, this.invite_req = invite_req);
		this.listener.onDlgInvite(this, this.invite_req.getToHeader().getNameAddress(), this.invite_req.getFromHeader().getNameAddress(), this.invite_req.getBody(), this.invite_req);
	}

	@Override
	public void onTransSuccessResponse(final TransactionClient transactionClient, final Message message) {
		boolean b = true;
		final boolean b2 = true;
		this.printLog("inside onTransSuccessResponse(tc,msg)", 5);
		if (transactionClient.getTransactionMethod().equals("INVITE")) {
			if (this.verifyStatus(this.statusIs(2) || this.statusIs(12))) {
				final StatusLine statusLine = message.getStatusLine();
				final int code = statusLine.getCode();
				if (this.verifyThat(code >= 200 && code < 300 && message.getTransactionMethod().equals("INVITE") && b2, "2xx for invite was expected")) {
					final boolean statusIs = this.statusIs(12);
					this.changeStatus(6);
					this.update(0, message);
					if (this.invite_offer) {
						this.ack_req = BaseMessageFactory.create2xxAckRequest(this, null);
						new AckTransactionClient(this.sip_provider, this.ack_req, null).request();
					}
					if (!statusIs) {
						this.listener.onDlgInviteSuccessResponse(this, code, statusLine.getReason(), message.getBody(), message);
						this.listener.onDlgCall(this);
						return;
					}
					this.listener.onDlgReInviteSuccessResponse(this, code, statusLine.getReason(), message.getBody(), message);
				}
			}
		} else if (transactionClient.getTransactionMethod().equals("BYE") && this.verifyStatus(this.statusIs(7))) {
			final StatusLine statusLine2 = message.getStatusLine();
			final int code2 = statusLine2.getCode();
			if (code2 < 200 || code2 >= 300) {
				b = false;
			}
			this.verifyThat(b, "2xx for bye was expected");
			this.changeStatus(9);
			this.listener.onDlgByeSuccessResponse(this, code2, statusLine2.getReason(), message);
			this.listener.onDlgClose(this);
		}
	}

	@Override
	public void onTransTimeout(final TransactionClient transactionClient) {
		this.printLog("inside onTransTimeout(tc,msg)", 5);
		if (transactionClient.getTransactionMethod().equals("INVITE")) {
			if (this.verifyStatus(this.statusIs(2) || this.statusIs(12))) {
				this.cancel();
				this.changeStatus(9);
				this.listener.onDlgTimeout(this);
				this.listener.onDlgClose(this);
			}
		} else if (transactionClient.getTransactionMethod().equals("BYE") && this.verifyStatus(this.statusIs(7))) {
			this.changeStatus(9);
			this.listener.onDlgClose(this);
		}
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("InviteDialog#" + this.dialog_sqn + ": " + s, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	public void reInvite(final String s, final String s2) {
		this.printLog("inside reInvite(contact,sdp)", 3);
		if (!this.statusIs(6)) {
			return;
		}
		final Message inviteRequest = BaseMessageFactory.createInviteRequest(this, s2);
		if (s != null) {
			NameAddress nameAddress;
			if (s.indexOf("sip:") >= 0) {
				nameAddress = new NameAddress(s);
			} else {
				nameAddress = new NameAddress(new SipURL(s, this.sip_provider.getViaAddress(), this.sip_provider.getPort()));
			}
			inviteRequest.setContactHeader(new ContactHeader(nameAddress));
			inviteRequest.setSupportedheader(new SupportedHeader("timer"));
			inviteRequest.setSessionExpireheader(new Expireheader("90;refresher=uac"));
		}
		this.reInvite(inviteRequest);
	}

	public void reInvite(final Message invite_req) {
		this.printLog("inside reInvite(invite)", 3);
		if (!this.statusIs(6)) {
			return;
		}
		this.changeStatus(12);
		this.update(0, this.invite_req = invite_req);
		new InviteTransactionClient(this.sip_provider, this.invite_req, this).request();
	}

	public void reInviteWithoutOffer(final String s, final String s2) {
		this.invite_offer = false;
		this.reInvite(s, s2);
	}

	public void reInviteWithoutOffer(final Message message) {
		this.invite_offer = false;
		this.reInvite(message);
	}

	public void redirect(final int n, final String s, final String s2) {
		this.printLog("inside redirect(" + n + "," + s + "," + s2 + ")", 3);
		this.respond(n, s, s2, null);
	}

	public void refuse() {
		this.printLog("inside refuse()", 3);
		MyLog.e("eeeeeeeeee", "here called");
		this.refuse(486, SipResponses.reasonOf(486));
	}

	public void refuse(final int n, final String s) {
		this.printLog("inside refuse(" + n + "," + s + ")", 3);
		this.respond(n, s, null, null);
	}

	public void respond(final int n, final String s, final String s2, final String body) {
		this.printLog("inside respond(" + n + "," + s + ")", 3);
		if (this.statusIs(3) || this.statusIs(13)) {
			NameAddress nameAddress = null;
			if (s2 != null) {
				nameAddress = new NameAddress(s2);
			}
			final Message response = BaseMessageFactory.createResponse(this.invite_req, n, s, nameAddress);
			response.setSupportedheader(new SupportedHeader("timer"));
			response.setSessionExpireheader(new Expireheader("90;refresher=uac"));
			response.setBody(body);
			this.respond(response);
			return;
		}
		this.printWarning("Dialog isn't in \"invited\" state: cannot respond (" + n + "/" + this.getStatus() + "/" + this.getDialogID() + ")", 3);
	}

	public void respond(final Message message) {
		this.printLog("inside respond(resp)", 3);
		final String method = message.getCSeqHeader().getMethod();
		if (method.equals("INVITE")) {
			if (!this.verifyStatus(this.statusIs(3) || this.statusIs(13))) {
				this.printLog("respond(): InviteDialog not in (re)invited state: No response now", 1);
			} else {
				final int code = message.getStatusLine().getCode();
				if (code >= 100 && code < 200) {
					this.invite_ts.respondWith(message);
					return;
				}
				if (code >= 200) {
					this.update(1, message);
				}
				if (code >= 200 && code < 300) {
					if (this.statusIs(3)) {
						this.changeStatus(5);
					} else {
						this.changeStatus(15);
					}
					this.invite_ts.terminate();
					(this.ack_ts = new AckTransactionServer(this.sip_provider, this.invite_ts.getConnectionId(), message, this)).respond();
					return;
				}
				if (this.statusIs(3)) {
					this.changeStatus(4);
				} else {
					this.changeStatus(14);
				}
				this.invite_ts.respondWith(message);
			}
		} else if (method.equals("BYE") && this.verifyStatus(this.statusIs(8))) {
			this.bye_ts.respondWith(message);
		}
	}

	public void ring(final String s) {
		this.printLog("inside ring()", 3);
		this.respond(180, SipResponses.reasonOf(180), null, s);
	}

	public void sendUpdateMessage() {
		if (this.statusIs(6)) {
			final Message request = BaseMessageFactory.createRequest(this, "UPDATE", null);
			request.setSupportedheader(new SupportedHeader("timer"));
			request.setSessionExpireheader(new Expireheader("90;refresher=uac"));
			new TransactionClient(this.sip_provider, request, this).request();
		}
	}

	public void tempGroupInviteWithoutOffer(final String s, final String s2, final String s3, final boolean b, final String s4, final String s5) {
		this.invite_offer = false;
		this.inviteWithoutOffer(s, s2, s3, true, b, s4, s5);
	}

	public void tempGrpInvite(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final String s6, final String s7) {
		this.invite(s, s2, s3, s4, s5, true, b, s6, s7);
	}
}
