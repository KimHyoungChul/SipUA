package org.zoolu.sip.provider;

import org.zoolu.sip.message.Message;

public class SipInterface implements SipProviderListener {
	Identifier id;
	SipInterfaceListener listener;
	SipProvider sip_provider;

	public SipInterface(final SipProvider sip_provider, final Identifier id, final SipInterfaceListener listener) {
		this.sip_provider = sip_provider;
		this.listener = listener;
		sip_provider.addSipProviderListener(this.id = id, this);
	}

	public SipInterface(final SipProvider sip_provider, final SipInterfaceListener listener) {
		this.sip_provider = sip_provider;
		this.listener = listener;
		sip_provider.addSipProviderListener(this.id = SipProvider.ANY, this);
	}

	public void close() {
		this.sip_provider.removeSipProviderListener(this.id);
	}

	public SipProvider getSipProvider() {
		return this.sip_provider;
	}

	@Override
	public void onReceivedMessage(final SipProvider sipProvider, final Message message) {
		if (this.listener != null) {
			this.listener.onReceivedMessage(this, message);
		}
	}

	public ConnectionIdentifier sendMessage(final Message message) {
		return this.sip_provider.sendMessage(message);
	}

	public ConnectionIdentifier sendMessage(final Message message, final String s, final String s2, final int n, final int n2) {
		return this.sip_provider.sendMessage(message, s, s2, n, n2);
	}

	public ConnectionIdentifier sendMessage(final Message message, final ConnectionIdentifier connectionIdentifier) {
		return this.sip_provider.sendMessage(message, connectionIdentifier);
	}
}
