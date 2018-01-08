package com.zed3.broadcastptt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zed3.sipua.ui.Receiver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PttBroadcastReceiver extends BroadcastReceiver {
	private final Set<String> actionSet;
	private final List<AbstractPttListenser> pttListeners;

	public PttBroadcastReceiver() {
		this.pttListeners = new ArrayList<AbstractPttListenser>();
		this.actionSet = new HashSet<String>();
		this.addKeyListeners();
	}

	public boolean addIpttKeyListener(final AbstractPttListenser abstractPttListenser) {
		return abstractPttListenser != null && !this.pttListeners.contains(abstractPttListenser) && this.pttListeners.add(abstractPttListenser);
	}

	public void addKeyListeners() {
		this.addIpttKeyListener(new SettingPttListener(this));
		this.addIpttKeyListener(new HT200PttListener(this));
		this.addIpttKeyListener(new EarinPttListener(this));
		this.addIpttKeyListener(new AlarmPttListener(this));
		this.addIpttKeyListener(new NormalPttListener(this));
		this.addIpttKeyListener(new SosPttListener(this));
		this.addIpttKeyListener(new FengHuoPttListener(this));
		this.addIpttKeyListener(new S6PttListener(this));
		this.addIpttKeyListener(new ZdzlPttListener(this));
		this.addIpttKeyListener(new BYRTPttListener(this));
		this.addIpttKeyListener(new HeadSetPttListener(this));
	}

	public Set<String> getActionSet() {
		return this.actionSet;
	}

	public List<AbstractPttListenser> getPttListeners() {
		return this.pttListeners;
	}

	public void onReceive(final Context context, final Intent intent) {
		if (Receiver.mSipdroidEngine == null || !Receiver.mSipdroidEngine.isRegistered()) {
			return;
		}
		if (this.pttListeners.size() == 0) {
			this.addKeyListeners();
		}
		final Iterator<AbstractPttListenser> iterator = this.pttListeners.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().pttKeyClick(context, intent, this)) {
				Log.i("GUOK", "pttKeyClick return");
				return;
			}
		}
		this.abortBroadcast();
	}

	public void removeAllListener() {
		this.pttListeners.clear();
	}

	public boolean removeIpttKeyListener(final IpttKeyListener ipttKeyListener) {
		return this.pttListeners.remove(ipttKeyListener);
	}
}
