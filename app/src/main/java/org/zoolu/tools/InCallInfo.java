package org.zoolu.tools;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.message.Message;

import java.io.Serializable;

public class InCallInfo implements Serializable {
	private static final long serialVersionUID = -3744607845079809322L;
	public Call call;
	public NameAddress callee;
	public NameAddress caller;
	public Message invite;
	public String sdp;

	public InCallInfo() {
		this.call = null;
		this.callee = null;
		this.caller = null;
		this.sdp = "";
		this.invite = null;
	}
}
