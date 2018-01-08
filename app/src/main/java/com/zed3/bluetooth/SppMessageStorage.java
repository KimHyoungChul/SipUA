package com.zed3.bluetooth;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SppMessageStorage {
	private Condition con4In;
	private Condition con4Out;
	private boolean isEmpty;
	private Lock lock;
	private Queue<SppMessage> storage;
	private final String tag;

	public SppMessageStorage() {
		this.isEmpty = true;
		this.lock = new ReentrantLock();
		this.con4In = this.lock.newCondition();
		this.con4Out = this.lock.newCondition();
		this.storage = new LinkedList<SppMessage>();
		this.tag = "SppMessageStorage";
	}

	private void checkRecieveMsg(final SppMessage sppMessage) {
		final String message = sppMessage.getMessage();
		if (message.equals("PTT_DOWN")) {
			this.disableMsgs("PTT_DOWN");
			this.disableMsgs("PTT_UP");
		} else {
			if (message.equals("PTT_UP")) {
				this.disableMsgs("PTT_DOWN");
				this.disableMsgs("PTT_UP");
				return;
			}
			if (message.equals("VOL_SHORT_DOWN")) {
				this.disableMsgs("VOL_SHORT_DOWN");
				this.disableMsgs("VOL_SHORT_UP");
				return;
			}
			if (message.equals("VOL_SHORT_UP")) {
				this.disableMsgs("VOL_SHORT_DOWN");
				this.disableMsgs("VOL_SHORT_UP");
				return;
			}
			if (message.equals("VOL_LONG_DOWN")) {
				this.disableMsgs("VOL_LONG_DOWN");
				this.disableMsgs("VOL_LONG_UP");
				return;
			}
			if (message.equals("VOL_LONG_UP")) {
				this.disableMsgs("VOL_LONG_DOWN");
				this.disableMsgs("VOL_LONG_UP");
				return;
			}
			if (message.equals("FUNCTION")) {
				this.disableMsgs("FUNCTION");
			}
		}
	}

	private void checkSendMsg(final SppMessage sppMessage) {
		final String message = sppMessage.getMessage();
		if (message.equals("R_START")) {
			this.disableMsgs("R_START");
			this.disableMsgs("R_STOP");
			this.disableMsgs("PA_OFF");
		} else {
			if (message.equals("R_STOP")) {
				this.disableMsgs("R_STOP");
				this.disableMsgs("R_START");
				this.disableMsgs("PTT_SUCC");
				return;
			}
			if (message.equals("PTT_SUCC")) {
				this.disableMsgs("PTT_SUCC");
				this.disableMsgs("R_START");
				this.disableMsgs("R_STOP");
				return;
			}
			if (message.equals("PTT_WAIT")) {
				this.disableMsgs("PTT_WAIT");
				this.disableMsgs("R_START");
				this.disableMsgs("R_STOP");
				this.disableMsgs("PTT_SUCC");
				return;
			}
			if (message.equals("PA_ON")) {
				this.disableMsgs("PA_ON");
				this.disableMsgs("PA_OFF");
				return;
			}
			if (message.equals("PA_OFF")) {
				this.disableMsgs("PA_OFF");
				this.disableMsgs("PA_ON");
			}
		}
	}

	private void disableMsgs(final String s) {
		for (final SppMessage sppMessage : this.storage) {
			if (sppMessage.getMessage().equals(s)) {
				sppMessage.setAvailable(false);
				Log.i("SppMessageStorage", "disableMsg(" + sppMessage.getMessage() + ")");
			}
		}
	}

	public SppMessage get() {
		while (true) {
			boolean isEmpty = false;
			while (true) {
				try {
					this.lock.lock();
					if (this.storage.size() == 0) {
						isEmpty = true;
					}
					this.isEmpty = isEmpty;
					Label_0048:
					{
						if (!this.isEmpty) {
							break Label_0048;
						}
						try {
							this.con4Out.await();
							final SppMessage sppMessage = this.storage.poll();
							final StringBuilder append = new StringBuilder("get() storage.size()").append(this.storage.size()).append("return ");
							if (sppMessage != null) {
								final String message = sppMessage.getMessage();
								Log.i("SppMessageStorage", append.append(message).toString());
								return sppMessage;
							}
						} catch (InterruptedException ex) {
							System.out.println(Thread.currentThread().getName());
							Log.i("SppMessageStorage", "get() InterruptedException set flag to exit while");
							this.isEmpty = false;
						}
					}
				} finally {
					this.lock.unlock();
				}
				final String message = "null";
				continue;
			}
		}
	}

	public void put(final SppMessage sppMessage) {
		while (true) {
			while (true) {
				Label_0134:
				{
					try {
						this.lock.lock();
						Log.i("SppMessageStorage", "put(" + sppMessage.getMessage() + ") storage.size()" + this.storage.size());
						switch (sppMessage.getType()) {
							case 0: {
								this.checkSendMsg(sppMessage);
								this.storage.offer(sppMessage);
								this.con4Out.signal();
								return;
							}
							case 1: {
								break;
							}
							default: {
								break Label_0134;
							}
						}
					} finally {
						this.lock.unlock();
					}
					this.checkRecieveMsg(sppMessage);
					continue;
				}
				continue;
			}
		}
	}

	public static class SppMessage {
		public static final String FUNCTION = "FUNCTION";
		protected static final int MAX_SEND_COUNT = 3;
		public static final String PTT_DOWN = "PTT_DOWN";
		public static final String PTT_PA_OFF = "PA_OFF";
		public static final String PTT_PA_ON = "PA_ON";
		public static final String PTT_START = "R_START";
		public static final String PTT_STOP = "R_STOP";
		public static final String PTT_SUCCESS = "PTT_SUCC";
		public static final String PTT_UP = "PTT_UP";
		public static final String PTT_WAITING = "PTT_WAIT";
		public static final String REQUEST_ADDRESS = "get addr";
		public static final String REQUEST_DEVICE_NAME = "request device name";
		public static final long RESEND_SYCLE = 200L;
		public static final String RESPOND_ADDRESS_HEAD = "addr:";
		public static final String RESPOND_DEVICE_NAME_HEAD = "device name:";
		public static final String RESPOND_PTT_PA_OFF = "PA_OFF_OK";
		public static final String RESPOND_PTT_PA_ON = "PA_ON_OK";
		public static final String RESPOND_PTT_START = "R_START_OK";
		public static final String RESPOND_PTT_STOP = "R_STOP_OK";
		public static final String RESPOND_PTT_SUCCESS = "PTT_SUCC_OK";
		public static final String RESPOND_PTT_WAITING = "PTT_WAIT_OK";
		public static int TYPE_RECEIVE = 0;
		public static int TYPE_SEND = 0;
		public static final String VOL_LONG_DOWN = "VOL_LONG_DOWN";
		public static final String VOL_LONG_UP = "VOL_LONG_UP";
		public static final String VOL_SHORT_DOWN = "VOL_SHORT_DOWN";
		public static final String VOL_SHORT_UP = "VOL_SHORT_UP";
		private boolean available;
		public String message;
		private int sendCount;
		public long time;
		private int type;

		static {
			SppMessage.TYPE_SEND = 0;
			SppMessage.TYPE_RECEIVE = 1;
		}

		public SppMessage(final long time, final String message, final int type) {
			this.available = true;
			this.time = time;
			this.message = message;
			this.type = type;
		}

		public String getMessage() {
			return this.message;
		}

		public int getSendCount() {
			return this.sendCount;
		}

		public long getTime() {
			return this.time;
		}

		public int getType() {
			return this.type;
		}

		public boolean isAvailable() {
			return this.available;
		}

		public boolean needResend() {
			return this.message.equals("PA_ON") || this.message.equals("PA_ON") || this.message.equals("R_START") || this.message.equals("R_STOP") || this.message.equals("PTT_SUCC") || this.message.equals("PTT_WAIT");
		}

		public void setAvailable(final boolean available) {
			this.available = available;
		}

		public void setMessage(final String message) {
			this.message = message;
		}

		public void setSendCount(final int sendCount) {
			this.sendCount = sendCount;
		}

		public void setTime(final long time) {
			this.time = time;
		}
	}
}
