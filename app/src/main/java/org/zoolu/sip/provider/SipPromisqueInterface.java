package org.zoolu.sip.provider;

public class SipPromisqueInterface extends SipInterface {
	public SipPromisqueInterface(final SipProvider sipProvider, final SipInterfaceListener sipInterfaceListener) {
		super(sipProvider, SipProvider.PROMISQUE, sipInterfaceListener);
	}
}
