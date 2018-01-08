package org.zoolu.sip.dialog;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.FromHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.DialogIdentifier;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;

import java.util.Vector;

public abstract class Dialog extends DialogInfo implements SipProviderListener {
	public static final int UAC = 0;
	public static final int UAS = 1;
	private static int dialog_counter;
	protected DialogIdentifier dialog_id;
	protected int dialog_sqn;
	protected Log log;
	protected SipProvider sip_provider;
	protected int status;

	static {
		Dialog.dialog_counter = 0;
	}

	protected Dialog(final SipProvider sip_provider) {
		this.sip_provider = sip_provider;
		this.log = this.sip_provider.getLog();
		final int dialog_counter = Dialog.dialog_counter;
		Dialog.dialog_counter = dialog_counter + 1;
		this.dialog_sqn = dialog_counter;
		this.status = 0;
		this.dialog_id = null;
	}

	protected void changeStatus(final int status) {
		this.status = status;
		this.printLog("changed dialog state: " + this.getStatus(), 3);
		if (this.isTerminated()) {
			if (this.dialog_id != null && this.sip_provider.getListeners().containsKey(this.dialog_id)) {
				this.sip_provider.removeSipProviderListener(this.dialog_id);
			}
		} else if ((this.isEarly() || this.isConfirmed()) && this.dialog_id != null && !this.sip_provider.getListeners().containsKey(this.dialog_id)) {
			this.sip_provider.addSipProviderListener(this.dialog_id, this);
		}
	}

	public DialogIdentifier getDialogID() {
		return this.dialog_id;
	}

	public SipProvider getSipProvider() {
		return this.sip_provider;
	}

	protected abstract int getStatus();

	protected abstract String getStatusDescription();

	public abstract boolean isConfirmed();

	public abstract boolean isEarly();

	public abstract boolean isTerminated();

	@Override
	public abstract void onReceivedMessage(final SipProvider p0, final Message p1);

	protected final void printException(final Exception ex, final int n) {
		if (this.log != null) {
			this.log.printException(ex, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	protected void printLog(final String s, final int n) {
		if (this.log != null) {
			this.log.println("Dialog#" + this.dialog_sqn + ": " + s, SipStack.LOG_LEVEL_DIALOG + n);
		}
	}

	protected final void printWarning(final String s, final int n) {
		this.printLog("WARNING: " + s, n);
	}

	protected boolean statusIs(final int n) {
		return this.status == n;
	}

	public void update(int i, final Message message) {
		if (this.isTerminated()) {
			this.printWarning("trying to update a terminated dialog: do nothing.", 1);
		} else {
			if (this.call_id == null) {
				this.call_id = message.getCallIdHeader().getCallId();
			}
			if (i == 0) {
				if (this.remote_name == null || this.remote_tag == null) {
					final ToHeader toHeader = message.getToHeader();
					if (this.remote_name == null) {
						this.remote_name = toHeader.getNameAddress();
					}
					if (this.remote_tag == null) {
						this.remote_tag = toHeader.getTag();
					}
				}
				if (this.local_name == null || this.local_tag == null) {
					final FromHeader fromHeader = message.getFromHeader();
					if (this.local_name == null) {
						this.local_name = fromHeader.getNameAddress();
					}
					if (this.local_tag == null) {
						this.local_tag = fromHeader.getTag();
					}
				}
				this.local_cseq = message.getCSeqHeader().getSequenceNumber();
			} else {
				if (this.local_name == null || this.local_tag == null) {
					final ToHeader toHeader2 = message.getToHeader();
					if (this.local_name == null) {
						this.local_name = toHeader2.getNameAddress();
					}
					if (this.local_tag == null) {
						this.local_tag = toHeader2.getTag();
					}
				}
				if (this.remote_name == null || this.remote_tag == null) {
					final FromHeader fromHeader2 = message.getFromHeader();
					if (this.remote_name == null) {
						this.remote_name = fromHeader2.getNameAddress();
					}
					if (this.remote_tag == null) {
						this.remote_tag = fromHeader2.getTag();
					}
				}
				this.remote_cseq = message.getCSeqHeader().getSequenceNumber();
				if (this.local_cseq == -1L) {
					this.local_cseq = SipProvider.pickInitialCSeq() - 1;
				}
			}
			if (message.hasContactHeader()) {
				if ((i == 0 && message.isRequest()) || (i == 1 && message.isResponse())) {
					this.local_contact = message.getContactHeader().getNameAddress();
				} else {
					this.remote_contact = message.getContactHeader().getNameAddress();
				}
			}
			if (i == 0) {
				if (message.isRequest() && message.hasRouteHeader() && this.route == null) {
					final Vector<String> values = message.getRoutes().getValues();
					this.route = new Vector<NameAddress>(values.size());
					for (int size = values.size(), j = 0; j < size; ++j) {
						this.route.insertElementAt(new NameAddress(values.elementAt(j)), j);
					}
				}
				if (i == 0 && message.isResponse() && message.hasRecordRouteHeader()) {
					final Vector<Header> headers = message.getRecordRoutes().getHeaders();
					final int size2 = headers.size();
					this.route = new Vector<NameAddress>(size2);
					for (i = 0; i < size2; ++i) {
						this.route.insertElementAt(new RecordRouteHeader(headers.elementAt(size2 - 1 - i)).getNameAddress(), i);
					}
				}
			} else {
				if (message.isRequest() && message.hasRouteHeader() && this.route == null) {
					final Vector<String> values2 = message.getRoutes().getValues();
					final int size3 = values2.size();
					this.route = new Vector<NameAddress>(size3);
					for (i = 0; i < size3; ++i) {
						this.route.insertElementAt(new NameAddress(values2.elementAt(size3 - 1 - i)), i);
					}
				}
				if (message.isRequest() && message.hasRecordRouteHeader()) {
					final Vector<Header> headers2 = message.getRecordRoutes().getHeaders();
					final int size4 = headers2.size();
					this.route = new Vector<NameAddress>(size4);
					for (i = 0; i < size4; ++i) {
						this.route.insertElementAt(new RecordRouteHeader(headers2.elementAt(i)).getNameAddress(), i);
					}
				}
			}
			final DialogIdentifier dialog_id = new DialogIdentifier(this.call_id, this.local_tag, this.remote_tag);
			if (this.dialog_id == null || !this.dialog_id.equals(dialog_id)) {
				if (this.dialog_id != null && this.sip_provider != null && this.sip_provider.getListeners().containsKey(this.dialog_id)) {
					this.sip_provider.removeSipProviderListener(this.dialog_id);
				}
				this.dialog_id = dialog_id;
				this.printLog("new dialog id: " + this.dialog_id, 1);
				if (this.sip_provider != null) {
					this.sip_provider.addSipProviderListener(this.dialog_id, this);
				}
			}
		}
	}

	protected final boolean verifyStatus(final boolean b) {
		return this.verifyThat(b, "dialog state mismatching");
	}

	protected final boolean verifyThat(final boolean b, final String s) {
		if (!b) {
			if (s != null && s.length() != 0) {
				this.printWarning(s, 1);
				return b;
			}
			this.printWarning("expression check failed. ", 1);
		}
		return b;
	}
}
