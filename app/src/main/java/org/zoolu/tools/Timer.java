package org.zoolu.tools;

public class Timer implements InnerTimerListener {
	public static boolean SINGLE_THREAD;
	boolean active;
	String label;
	TimerListener listener;
	long time;

	static {
		Timer.SINGLE_THREAD = true;
	}

	public Timer(final long n, final String s, final TimerListener timerListener) {
		this.init(n, s, timerListener);
	}

	public Timer(final long n, final TimerListener timerListener) {
		this.init(n, null, timerListener);
	}

	public String getLabel() {
		return this.label;
	}

	public long getTime() {
		return this.time;
	}

	public void halt() {
		this.active = false;
		this.listener = null;
	}

	void init(final long time, final String label, final TimerListener listener) {
		this.listener = listener;
		this.time = time;
		this.label = label;
		this.active = false;
	}

	@Override
	public void onInnerTimeout() {
		if (this.active && this.listener != null) {
			this.listener.onTimeout(this);
		}
		this.listener = null;
		this.active = false;
	}

	public void start() {
		this.active = true;
		if (Timer.SINGLE_THREAD) {
			new InnerTimerST(this.time, this);
			return;
		}
		new InnerTimer(this.time, this);
	}
}
