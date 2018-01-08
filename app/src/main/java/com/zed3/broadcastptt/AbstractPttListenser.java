package com.zed3.broadcastptt;

public abstract class AbstractPttListenser implements IpttKeyListener {
	public AbstractPttListenser(final PttBroadcastReceiver pttBroadcastReceiver) {
		this.addAction(pttBroadcastReceiver);
	}

	public abstract void addAction(final PttBroadcastReceiver p0);
}
