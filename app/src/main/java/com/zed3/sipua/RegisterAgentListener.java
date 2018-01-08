package com.zed3.sipua;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

public interface RegisterAgentListener {
	void onMWIUpdate(final RegisterAgent p0, final boolean p1, final int p2, final String p3);

	void onUaRegistrationFailure(final RegisterAgent p0, final NameAddress p1, final NameAddress p2, final String p3);

	void onUaRegistrationSuccess(final RegisterAgent p0, final NameAddress p1, final NameAddress p2, final String p3, final Message p4);
}
