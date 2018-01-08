package org.zoolu.sip.call;

import com.zed3.log.MyLog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.dialog.InviteDialog;
import org.zoolu.sip.dialog.InviteDialogListener;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;

import java.util.Timer;
import java.util.TimerTask;

public class Call implements InviteDialogListener {
	protected String contact_url;
	protected InviteDialog dialog;
	protected String from_url;
	CallListener listener;
	protected String local_sdp;
	Log log;
	protected String remote_sdp;
	protected SipProvider sip_provider;
	private TimerTask updateMessageRunnable;

	public Call(final SipProvider sip_provider, final String from_url, final String contact_url, final CallListener listener) {
		this.updateMessageRunnable = null;
		this.sip_provider = sip_provider;
		this.log = sip_provider.getLog();
		this.listener = listener;
		this.from_url = from_url;
		this.contact_url = contact_url;
		this.dialog = null;
		this.local_sdp = null;
		this.remote_sdp = null;
	}

	private void call(final String s, String contact_url, final String s2, final String local_sdp, final String s3, final boolean b, final boolean b2) {
		this.printLog("calling " + s, 1);
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
		this.dialog = new InviteDialog(this.sip_provider, this);
		if (this.local_sdp != null) {
			if (!b) {
				this.dialog.invite(s, from_url, contact_url, this.local_sdp, s3);
				return;
			}
			this.dialog.groupinvite(s, from_url, contact_url, this.local_sdp, s3, b2);
		} else {
			if (!b) {
				this.dialog.inviteWithoutOffer(s, from_url, contact_url);
				return;
			}
			this.dialog.groupinviteWithoutOffer(s, from_url, contact_url, b2);
		}
	}

	private void hangup(final boolean b) {
		if (this.dialog != null) {
			MyLog.e("Call", "hangup .....  Exceptioin Log for test");
			new Exception("---print trace----").printStackTrace();
			this.dialog.refuse();
			this.dialog.cancel();
			if (!b) {
				this.dialog.bye();
			} else {
				this.dialog.groupbye();
			}
		}
		this.stopUpdateMessage();
	}

	private void sendUpdateMessage() {
		if (this.dialog != null) {
			this.dialog.sendUpdateMessage();
		}
	}

	public void accept(final String local_sdp) {
		this.local_sdp = local_sdp;
		if (this.dialog != null) {
			this.dialog.accept(this.contact_url, this.local_sdp);
		}
	}

	public void ackWithAnswer(final String local_sdp) {
		this.local_sdp = local_sdp;
		this.dialog.ackWithAnswer(this.contact_url, local_sdp);
	}

	public void busy() {
		if (this.dialog != null) {
			this.dialog.busy();
		}
	}

	public void bye() {
		if (this.dialog != null) {
			this.dialog.bye();
		}
	}

	public void call(final String s) {
		this.call(s, null, null, null, null, false, false);
	}

	public void call(final String s, final String s2, final String s3) {
		this.call(s, null, null, s2, s3, false, false);
	}

	public void call(final Message message) {
		this.dialog = new InviteDialog(this.sip_provider, this);
		this.local_sdp = message.getBody();
		if (this.local_sdp != null) {
			this.dialog.invite(message);
			return;
		}
		this.dialog.inviteWithoutOffer(message);
	}

	public void cancel() {
		if (this.dialog != null) {
			this.dialog.cancel();
		}
	}

	public String getLocalSessionDescriptor() {
		return this.local_sdp;
	}

	public String getRemoteSessionDescriptor() {
		return this.remote_sdp;
	}

	public void groupcall(final String s) {
		this.call(s, null, null, null, null, true, false);
	}

	public void groupcall(final String s, final String s2, final String s3, final boolean b) {
		this.call(s, null, null, s2, s3, true, b);
	}

	public void grouphangup() {
		this.hangup(true);
	}

	public void hangup() {
		this.hangup(false);
	}

	public boolean isCanceledOrByed() {
		return this.dialog.isSessionClosed();
	}

	public boolean isOnCall() {
		return this.dialog.isSessionActive();
	}

	public void listen() {
		(this.dialog = new InviteDialog(this.sip_provider, this)).listen();
	}

	public void modify(final String s, final String local_sdp) {
		this.local_sdp = local_sdp;
		if (this.dialog != null) {
			this.dialog.reInvite(s, this.local_sdp);
		}
	}

	@Override
	public void onDlgAck(final InviteDialog inviteDialog, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if (this.listener != null) {
				this.listener.onCallConfirmed(this, remote_sdp, message);
			}
		}
	}

	@Override
	public void onDlgBye(final InviteDialog inviteDialog, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallClosing(this, message);
		}
	}

	@Override
	public void onDlgByeFailureResponse(final InviteDialog inviteDialog, final int n, final String s, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallClosed(this, message);
		}
	}

	@Override
	public void onDlgByeSuccessResponse(final InviteDialog inviteDialog, final int n, final String s, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallClosed(this, message);
		}
	}

	@Override
	public void onDlgCall(final InviteDialog inviteDialog) {
	}

	@Override
	public void onDlgCancel(final InviteDialog inviteDialog, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallCanceling(this, message);
		}
	}

	@Override
	public void onDlgClose(final InviteDialog inviteDialog) {
	}

	@Override
	public void onDlgInvite(final InviteDialog inviteDialog, final NameAddress nameAddress, final NameAddress nameAddress2, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if (this.listener != null) {
				this.listener.onCallIncoming(this, nameAddress, nameAddress2, remote_sdp, message);
			}
		}
	}

	@Override
	public void onDlgInviteFailureResponse(final InviteDialog inviteDialog, final int n, final String s, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallRefused(this, s, message);
		}
	}

	@Override
	public void onDlgInviteProvisionalResponse(final InviteDialog inviteDialog, final int n, final String s, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if ((n == 180 || n == 183) && this.listener != null) {
				this.listener.onCallRinging(this, message);
			}
		}
	}

	@Override
	public void onDlgInviteRedirectResponse(final InviteDialog inviteDialog, final int n, final String s, final MultipleHeader multipleHeader, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallRedirection(this, s, multipleHeader.getValues(), message);
		}
	}

	@Override
	public void onDlgInviteSuccessResponse(final InviteDialog inviteDialog, final int n, final String s, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if (this.listener != null) {
				this.listener.onCallAccepted(this, remote_sdp, message);
			}
		}
	}

	@Override
	public void onDlgReInvite(final InviteDialog inviteDialog, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if (this.listener != null) {
				this.listener.onCallModifying(this, remote_sdp, message);
			}
		}
	}

	@Override
	public void onDlgReInviteFailureResponse(final InviteDialog inviteDialog, final int n, final String s, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallReInviteRefused(this, s, message);
		}
	}

	@Override
	public void onDlgReInviteProvisionalResponse(final InviteDialog inviteDialog, final int n, final String s, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (remote_sdp != null && remote_sdp.length() != 0) {
			this.remote_sdp = remote_sdp;
		}
	}

	@Override
	public void onDlgReInviteSuccessResponse(final InviteDialog inviteDialog, final int n, final String s, final String remote_sdp, final Message message) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else {
			if (remote_sdp != null && remote_sdp.length() != 0) {
				this.remote_sdp = remote_sdp;
			}
			if (this.listener != null) {
				this.listener.onCallReInviteAccepted(this, remote_sdp, message);
			}
		}
	}

	@Override
	public void onDlgReInviteTimeout(final InviteDialog inviteDialog) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallReInviteTimeout(this);
		}
	}

	@Override
	public void onDlgTimeout(final InviteDialog inviteDialog) {
		if (inviteDialog != this.dialog) {
			this.printLog("NOT the current dialog", 1);
		} else if (this.listener != null) {
			this.listener.onCallTimeout(this);
		}
	}

	@Override
	public void onDlgUpdateSuccessResponse() {
		this.startUpdateMessage();
	}

	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("Call: " + s, SipStack.LOG_LEVEL_CALL + n);
		}
	}

	public void redirect(final String s) {
		if (this.dialog != null) {
			this.dialog.redirect(302, "Moved Temporarily", s);
		}
	}

	public void refuse() {
		if (this.dialog != null) {
			this.dialog.refuse();
		}
		MyLog.e("eeeeeeeeee", "refuse ......");
	}

	public void respond(final Message message) {
		if (this.dialog != null) {
			this.dialog.respond(message);
		}
	}

	public void ring(final String local_sdp) {
		this.local_sdp = local_sdp;
		if (this.dialog != null) {
			this.dialog.ring(null);
		}
	}

	public void setLocalSessionDescriptor(final String local_sdp) {
		this.local_sdp = local_sdp;
	}

	public void startUpdateMessage() {
		if (this.updateMessageRunnable == null) {
			this.updateMessageRunnable = new UpdateMessageTimer();
		} else {
			this.updateMessageRunnable.cancel();
			this.updateMessageRunnable = null;
			this.updateMessageRunnable = new UpdateMessageTimer();
		}
		android.util.Log.i("update45", this + "update  startUpdateMessage" + this.updateMessageRunnable);
		new Timer().schedule(this.updateMessageRunnable, 45000L, 5000L);
	}

	public void stopUpdateMessage() {
		if (this.updateMessageRunnable != null) {
			android.util.Log.i("update45", this + "update  stopUpdateMessage");
			this.updateMessageRunnable.cancel();
			this.updateMessageRunnable = null;
		}
	}

	class UpdateMessageTimer extends TimerTask {
		@Override
		public void run() {
			android.util.Log.i("update45", "update  sendUpdateMess 45-----------");
			Call.this.sendUpdateMessage();
		}
	}
}
