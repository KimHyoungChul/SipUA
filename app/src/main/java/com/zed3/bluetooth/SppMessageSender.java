package com.zed3.bluetooth;

import android.os.Handler;
import android.util.Log;

public class SppMessageSender extends Thread {
	private boolean isRunning;
	private SppMessageStorage.SppMessage lastSendMessage;
	private long lastSendMessageTime;
	private SppMessageStorage mStorage;
	private Handler reSendHandler;
	private String tag;

	public SppMessageSender(final SppMessageStorage mStorage) {
		this.isRunning = true;
		this.tag = "SppMessageSender";
		this.mStorage = mStorage;
	}

	@Override
	public void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO
			}
		}).start();
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender   start sending");
		Log.i(this.tag, "SppMessageSender   start sending");
		Label_0139_Outer:
		while (this.isRunning) {
			final SppMessageStorage.SppMessage value = this.mStorage.get();
			if (value != null) {
				final String message = value.getMessage();
				if (!value.isAvailable()) {
					Log.i(this.tag, "while (isRunning) , message.isAvailable() is false   continue");
				} else {
					ZMBluetoothManager.getInstance().send(message);
					while (true) {
						try {
							Thread.sleep(50L);
							if (!value.needResend()) {
								Log.i(this.tag, "while (isRunning) , lastSendMessage.needResend() is false  continue ");
								continue Label_0139_Outer;
							}
						} catch (InterruptedException ex) {
							this.stopSending();
							ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender  InterruptedException  stop sending");
							Log.i(this.tag, "SppMessageSender  InterruptedException  stop sending");
							ex.printStackTrace();
							continue;
						}
						break;
					}
					this.lastSendMessage = value;
					this.lastSendMessageTime = this.lastSendMessage.getTime();
					this.lastSendMessage.setSendCount(1);
					this.reSendHandler.sendMessageDelayed(this.reSendHandler.obtainMessage(), 200L);
					Log.i(this.tag, "while (isRunning) , message.needResend() is true   reSendHandler.sendMessageDelayed(handlerMessage, 200);");
				}
			} else {
				ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender  mStorage.get() return null");
				Log.i(this.tag, "SppMessageSender  mStorage.get() return null");
			}
		}
		ZMBluetoothManager.getInstance().writeLog2File("SppMessageSender   stop sending");
		Log.i(this.tag, "SppMessageSender   stop sending");
		if (this.reSendHandler != null && this.reSendHandler.getLooper() != null) {
			this.reSendHandler.getLooper().quit();
		}
	}

	public void startSending() {
		this.isRunning = true;
	}

	public void stopSending() {
		this.isRunning = false;
	}
}
