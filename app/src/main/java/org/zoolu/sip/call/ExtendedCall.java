package org.zoolu.sip.call;

import android.text.TextUtils;

import com.zed3.sipua.CallManager;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.dialog.ExtendedInviteDialog;
import org.zoolu.sip.dialog.ExtendedInviteDialogListener;
import org.zoolu.sip.dialog.InviteDialog;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import java.util.UUID;

public class ExtendedCall extends Call implements ExtendedInviteDialogListener {
	long callBeginTime;
	int callPtime;
	CallManager.CallState callState;
	public CallManager.CallType callType;
	int caller;
	String callerName;
	String callerNumber;
	String extCallId;
	public boolean isGroupCall;
	String next_nonce;
	String passwd;
	CallManager.CallState preCallState;
	String qop;
	String realm;
	Message refer;
	String username;
	ExtendedCallListener xcall_listener;

	public ExtendedCall(final SipProvider sipProvider, final String s, final String s2, final String username, final String realm, final String passwd, final ExtendedCallListener xcall_listener) {
		super(sipProvider, s, s2, xcall_listener);
		this.isGroupCall = false;
		this.callType = CallManager.CallType.UNKNOW;
		this.callState = CallManager.CallState.UNKNOW;
		this.preCallState = CallManager.CallState.UNKNOW;
		this.caller = -1;
		this.callPtime = 20;
		this.xcall_listener = xcall_listener;
		this.refer = null;
		this.username = username;
		this.realm = realm;
		this.passwd = passwd;
		this.next_nonce = null;
		this.qop = null;
		this.extCallId = UUID.randomUUID().toString();
	}

	public ExtendedCall(final SipProvider sipProvider, final String s, final String s2, final ExtendedCallListener xcall_listener) {
		super(sipProvider, s, s2, xcall_listener);
		this.isGroupCall = false;
		this.callType = CallManager.CallType.UNKNOW;
		this.callState = CallManager.CallState.UNKNOW;
		this.preCallState = CallManager.CallState.UNKNOW;
		this.caller = -1;
		this.callPtime = 20;
		this.xcall_listener = xcall_listener;
		this.refer = null;
		this.username = null;
		this.realm = null;
		this.passwd = null;
		this.next_nonce = null;
		this.qop = null;
		this.extCallId = UUID.randomUUID().toString();
	}

	private void call(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2, final String s6, final String s7) {
		this.call(s, s2, s3, s4, s5, b, b2, s6, s7, null);
	}

	private void call(final String s, String contact_url, final String s2, final String local_sdp, final String s3, final boolean b, final boolean b2, final String s4, final String s5, final String s6) {
		this.printLog("calling " + s, 3);
		if (this.username != null) {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this.username, this.realm, this.passwd, this);
		} else {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this);
		}
		String from_url = contact_url;
		if (contact_url == null) {
			from_url = this.from_url;
		}
		if ((contact_url = s2) == null) {
			contact_url = this.contact_url;
		}
		if (local_sdp != null) {
			this.local_sdp = local_sdp;
		}
		if (this.local_sdp != null) {
			if (!b) {
				this.dialog.invite(s, from_url, contact_url, this.local_sdp, s3, s6);
				return;
			}
			if (s4 == null) {
				this.dialog.groupinvite(s, from_url, contact_url, this.local_sdp, s3, b2);
				return;
			}
			this.dialog.tempGrpInvite(s, from_url, contact_url, this.local_sdp, s3, b2, s4, s5);
		} else {
			if (!b) {
				this.dialog.inviteWithoutOffer(s, from_url, contact_url);
				return;
			}
			if (s4 == null) {
				this.dialog.groupinviteWithoutOffer(s, from_url, contact_url, b2);
				return;
			}
			this.dialog.tempGroupInviteWithoutOffer(s, from_url, contact_url, b2, s4, s5);
		}
	}

	public void acceptTransfer() {
		((ExtendedInviteDialog) this.dialog).acceptRefer(this.refer);
	}

	public void antaCall4(final String s, final String s2, String contact_url, final String s3, final String local_sdp, final String s4, final boolean b) {
		this.printLog("calling " + s, 3);
		if (this.username != null) {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this.username, this.realm, this.passwd, this);
		} else {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this);
		}
		String from_url = contact_url;
		if (contact_url == null) {
			from_url = this.from_url;
		}
		if ((contact_url = s3) == null) {
			contact_url = this.contact_url;
		}
		if (local_sdp != null) {
			this.local_sdp = local_sdp;
		}
		if (this.local_sdp != null) {
			this.dialog.antaInvite(s, s2, from_url, contact_url, this.local_sdp, s4, b);
			return;
		}
		this.dialog.antaInviteWithoutOffer(s, s2, from_url, contact_url, b);
	}

	public void call(final String s, final String s2, final String s3, final String s4, final String s5) {
		this.call(s, s2, s3, s4, s5, false, false, null, null);
	}

	public void call(final String s, final String s2, final String s3, final String s4, final String s5, final String s6) {
		this.call(s, s2, s3, s4, s5, false, false, null, null, s6);
	}

	@Override
	public void call(final Message message) {
		this.dialog = new ExtendedInviteDialog(this.sip_provider, this);
		this.local_sdp = message.getBody();
		if (this.local_sdp != null) {
			this.dialog.invite(message);
			return;
		}
		this.dialog.inviteWithoutOffer(message);
	}

	public long getCallBeginTime() {
		return this.callBeginTime;
	}

	public int getCallDirection() {
		if (this.getDialog() != null) {
			return this.getDialog().getCallDirection();
		}
		return -1;
	}

	public int getCallPtime() {
		return this.callPtime;
	}

	public CallManager.CallState getCallState() {
		return this.callState;
	}

	public CallManager.CallType getCallType() {
		return this.callType;
	}

	public int getCallTypeEx() {
		final boolean b = false;
		if (this.getDialog() != null) {
			final Message inviteMessage = this.getDialog().getInviteMessage();
			int n;
			if (inviteMessage != null && inviteMessage.getBody().contains("video")) {
				if (!inviteMessage.hasAntaExtensionHeader()) {
					return 1;
				}
				n = 4;
			} else {
				if (inviteMessage == null || !inviteMessage.getBody().contains("audio")) {
					return -1;
				}
				if (inviteMessage.hasPttExtensionHeader()) {
					if (inviteMessage.getPttExtensionHeader().getValue().contains("3ghandset tmp")) {
						return 3;
					}
					return 2;
				} else {
					n = (b ? 1 : 0);
					if (inviteMessage.hasAntaExtensionHeader()) {
						if (inviteMessage.getAntaExtensionHeader().getValue().contains("conference") || inviteMessage.getAntaExtensionHeader().getValue().contains("speaker")) {
							return 5;
						}
						n = (b ? 1 : 0);
						if (inviteMessage.getAntaExtensionHeader().getValue().contains("groupbroadcast")) {
							return 6;
						}
					}
				}
			}
			return n;
		}
		return -1;
	}

	public String getCallerName() {
		return this.callerName;
	}

	public String getCallerNumber() {
		return this.callerNumber;
	}

	public int getCallerState() {
		return this.caller;
	}

	public InviteDialog getDialog() {
		return this.dialog;
	}

	public String getExtCallId() {
		return this.extCallId;
	}

	public String getPeerNumber() {
		if (this.getDialog() != null && this.getDialog().getInviteMessage() != null) {
			if (this.getCallDirection() == 0) {
				String s2;
				final String s = s2 = this.getDialog().getInviteMessage().getToHeader().getValue();
				if (!TextUtils.isEmpty((CharSequence) s)) {
					s2 = s;
					if (s.contains("sip")) {
						s2 = s.substring(s.indexOf("sip"));
					}
				}
				return new SipURL(s2).getUserName();
			}
			if (this.getCallDirection() == 1) {
				String s4;
				final String s3 = s4 = this.getDialog().getInviteMessage().getFromHeader().getValue();
				if (!TextUtils.isEmpty((CharSequence) s3)) {
					s4 = s3;
					if (s3.contains("sip")) {
						s4 = s3.substring(s3.indexOf("sip"));
					}
				}
				return new SipURL(s4).getUserName();
			}
		}
		return null;
	}

	public CallManager.CallState getPreCallState() {
		return this.preCallState;
	}

	public void groupcall(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b) {
		this.call(s, s2, s3, s4, s5, true, b, null, null);
	}

	public void info(final char c, final int n) {
		((ExtendedInviteDialog) this.dialog).info(c, n);
	}

	public boolean isEmergenCall() {
		if (this.getDialog() != null) {
			final Message inviteMessage = this.getDialog().getInviteMessage();
			if (inviteMessage != null && inviteMessage.getHeader("ptt-emergency-call") != null) {
				return true;
			}
		}
		return false;
	}

	public boolean isTmpCall() {
		return this.getDialog() != null && this.getDialog().getInviteMessage().hasPttExtensionHeader() && this.getDialog().getInviteMessage().getPttExtensionHeader().getValue().contains("3ghandset tmp");
	}

	@Override
	public void listen() {
		if (this.username != null) {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this.username, this.realm, this.passwd, this);
		} else {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this);
		}
		this.dialog.listen();
	}

	public void notify(final int n, final String s) {
		((ExtendedInviteDialog) this.dialog).notify(n, s);
	}

	@Override
	public void onDlgAltRequest(final InviteDialog inviteDialog, final String s, final String s2, final Message message) {
	}

	@Override
	public void onDlgAltResponse(final InviteDialog inviteDialog, final String s, final int n, final String s2, final String s3, final Message message) {
	}

	@Override
	public void onDlgNotify(final InviteDialog inviteDialog, final String s, final String s2, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			this.printLog("onDlgNotify()", 5);
			if (s.equals("refer")) {
				final Message message2 = new Message(s2);
				this.printLog("Notify: " + s2, 1);
				if (message2.isResponse()) {
					final StatusLine statusLine = message2.getStatusLine();
					final int code = statusLine.getCode();
					final String reason = statusLine.getReason();
					if (code >= 200 && code < 300) {
						this.printLog("Call successfully transferred", 3);
						if (this.xcall_listener != null) {
							this.xcall_listener.onCallTransferSuccess(this, message);
						}
					} else if (code >= 300) {
						this.printLog("Call NOT transferred", 3);
						if (this.xcall_listener != null) {
							this.xcall_listener.onCallTransferFailure(this, reason, message);
						}
					}
				}
			}
		}
	}

	@Override
	public void onDlgRefer(final InviteDialog inviteDialog, final NameAddress nameAddress, final NameAddress nameAddress2, final Message refer) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			this.printLog("onDlgRefer(" + nameAddress.toString() + ")", 5);
			this.refer = refer;
			if (this.xcall_listener != null) {
				this.xcall_listener.onCallTransfer(this, nameAddress, nameAddress2, refer);
			}
		}
	}

	@Override
	public void onDlgReferResponse(final InviteDialog inviteDialog, final int n, final String s, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			this.printLog("onDlgReferResponse(" + n + " " + s + ")", 5);
			if (n >= 200 && n < 300) {
				if (this.xcall_listener != null) {
					this.xcall_listener.onCallTransferAccepted(this, message);
				}
			} else if (n >= 300 && this.xcall_listener != null) {
				this.xcall_listener.onCallTransferRefused(this, s, message);
			}
		}
	}

	@Override
	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("ExtendedCall: " + s, SipStack.LOG_LEVEL_CALL + n);
		}
	}

	public void refuseTransfer() {
		((ExtendedInviteDialog) this.dialog).refuseRefer(this.refer);
	}

	public void setCallBeginTime(final long callBeginTime) {
		this.callBeginTime = callBeginTime;
	}

	public void setCallPtime(final int callPtime) {
		this.callPtime = callPtime;
	}

	public void setCallState(final CallManager.CallState callState) {
		if (CallManager.CallState.OUTGOING == callState) {
			this.caller = 0;
		} else if (CallManager.CallState.INCOMING == callState) {
			this.caller = 1;
		}
		if (this.callState == callState) {
			return;
		}
		if (this.callState != CallManager.CallState.UNKNOW) {
			this.preCallState = this.callState;
		}
		this.callState = callState;
	}

	public void setCallType(final CallManager.CallType callType) {
		this.callType = callType;
	}

	public void setCallerName(final String callerName) {
		this.callerName = callerName;
	}

	public void setCallerNumber(final String callerNumber) {
		this.callerNumber = callerNumber;
	}

	public void setExtCallId(final String extCallId) {
		this.extCallId = extCallId;
	}

	public void tempGroupCall() {
		if (this.username != null) {
			this.dialog = new ExtendedInviteDialog(this.sip_provider, this.username, this.realm, this.passwd, this);
			return;
		}
		this.dialog = new ExtendedInviteDialog(this.sip_provider, this);
	}

	public void tempGroupcall(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final String s6, final String s7) {
		this.call(s, s2, s3, s4, s5, true, b, s6, s7);
	}

	public void transfer(final String s) {
		((ExtendedInviteDialog) this.dialog).refer(new NameAddress(s));
	}
}
