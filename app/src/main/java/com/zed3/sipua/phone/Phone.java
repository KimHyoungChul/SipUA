package com.zed3.sipua.phone;

import android.content.Context;
import android.telephony.ServiceState;

public interface Phone {
	Call getBackgroundCall();

	Context getContext();

	Call getForegroundCall();

	Call getRingingCall();

	ServiceState getServiceState();

	State getState();

	public enum State {
		IDLE("IDLE", 0),
		OFFHOOK("OFFHOOK", 2),
		RINGING("RINGING", 1);

		private State(final String s, final int n) {
		}
	}

	public enum SuppService {
		CONFERENCE("CONFERENCE", 4),
		HANGUP("HANGUP", 6),
		REJECT("REJECT", 5),
		SEPARATE("SEPARATE", 2),
		SWITCH("SWITCH", 1),
		TRANSFER("TRANSFER", 3),
		UNKNOWN("UNKNOWN", 0);

		private SuppService(final String s, final int n) {
		}
	}
}
