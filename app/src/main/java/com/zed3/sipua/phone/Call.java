package com.zed3.sipua.phone;

public class Call {
	public long base;
	Connection earliest;
	State mState;

	public Call() {
		this.mState = State.IDLE;
	}

	public Connection getEarliestConnection() {
		return this.earliest;
	}

	public State getState() {
		return this.mState;
	}

	public boolean hasConnections() {
		return true;
	}

	public boolean isDialingOrAlerting() {
		return this.getState().isDialing();
	}

	public boolean isIdle() {
		return !this.getState().isAlive();
	}

	public boolean isRinging() {
		return this.getState().isRinging();
	}

	public void setConn(final Connection earliest) {
		this.earliest = earliest;
	}

	public void setState(final State mState) {
		this.mState = mState;
	}

	public enum State {
		ACTIVE("ACTIVE", 1),
		ALERTING("ALERTING", 4),
		DIALING("DIALING", 3),
		DISCONNECTED("DISCONNECTED", 7),
		HOLDING("HOLDING", 2),
		IDLE("IDLE", 0),
		INCOMING("INCOMING", 5),
		WAITING("WAITING", 6);

		private State(final String s, final int n) {
		}

		public boolean isAlive() {
			return this != State.IDLE && this != State.DISCONNECTED;
		}

		public boolean isDialing() {
			return this == State.DIALING || this == State.ALERTING;
		}

		public boolean isRinging() {
			return this == State.INCOMING || this == State.WAITING;
		}
	}
}
