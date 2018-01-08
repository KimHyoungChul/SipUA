package org.zoolu.tools;

class InnerTimer extends Thread {
	InnerTimerListener listener;
	long timeout;

	public InnerTimer(final long timeout, final InnerTimerListener listener) {
		this.timeout = timeout;
		this.listener = listener;
		this.start();
	}

	@Override
	public void run() {
		if (this.listener == null) {
			return;
		}
		while (true) {
			try {
				Thread.sleep(this.timeout);
				this.listener.onInnerTimeout();
				this.listener = null;
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}
}
