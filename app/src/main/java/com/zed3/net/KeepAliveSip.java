package com.zed3.net;

import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;

import java.io.IOException;

public class KeepAliveSip extends KeepAliveUdp {
	Message message;
	SipProvider sip_provider;

	public KeepAliveSip(final SipProvider sipProvider, final long n) {
		super(null, n);
		this.init(sipProvider, this.message = null);
		this.start();
	}

	public KeepAliveSip(final SipProvider sipProvider, final Message message, final long n) {
		super(null, n);
		this.message = null;
		this.init(sipProvider, message);
		this.start();
	}

	private void init(final SipProvider sip_provider, final Message message) {
		this.sip_provider = sip_provider;
		Message message2 = message;
		if (message == null) {
			message2 = new Message("\r\n");
		}
		this.message = message2;
	}

	@Override
	public void run() {
		super.run();
		this.sip_provider = null;
	}

	@Override
	public void sendToken() throws IOException {
		if (!this.stop && this.sip_provider != null) {
			this.sip_provider.sendMessage(this.message);
		}
	}

	@Override
	public String toString() {
		Object string = null;
		if (this.sip_provider != null) {
			string = "sip:" + this.sip_provider.getViaAddress() + ":" + this.sip_provider.getPort();
		}
		return String.valueOf(string) + " (" + this.delta_time + "ms)";
	}
}
