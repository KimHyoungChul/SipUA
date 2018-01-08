package org.zoolu.tools;

import java.util.TimerTask;

class InnerTimerST extends TimerTask {
	static Timer single_timer;
	InnerTimerListener listener;

	public InnerTimerST(final long n, final InnerTimerListener listener) {
		this.listener = listener;
//		InnerTimerST.single_timer.schedule(this, n);
	}

	@Override
	public void run() {
		if (this.listener != null) {
			this.listener.onInnerTimeout();
			this.listener = null;
		}
	}
}
