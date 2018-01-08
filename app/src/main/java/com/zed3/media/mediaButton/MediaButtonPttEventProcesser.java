package com.zed3.media.mediaButton;

import android.os.Looper;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.UserAgent.PttPRMode;
import com.zed3.sipua.ui.DemoCallScreen;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.utils.LogUtil;
import com.zed3.utils.RtpStreamReceiverUtil;
import com.zed3.utils.RtpStreamSenderUtil;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Deprecated
public class MediaButtonPttEventProcesser extends Thread {
	public static boolean mIsPttDowned;
	private Condition con4In;
	private Condition con4Out;
	private Queue<PttEvent> invalidEventstorage;
	private boolean isEmpty;
	private boolean isRunning;
	private Lock lock;
	private String logMsg;
	private PttEvent mLastEvent;
	private Queue<PttEvent> storage;
	private final String tag;

	private static final class InstanceCreater {
		public static MediaButtonPttEventProcesser sInstance = new MediaButtonPttEventProcesser();

		private InstanceCreater() {
		}
	}

	public static class PttEvent {
		public static final String PTT_DOWN = "PTT_DOWN";
		public static final String PTT_UP = "PTT_UP";
		public static int TYPE_RECEIVE = 1;
		public static int TYPE_SEND = 0;
		private boolean available = true;
		private String message;
		private int sendCount;
		private long time;
		private int type;

		public int getType() {
			return this.type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public PttEvent(long time, String message, int type) {
			this.time = time;
			this.message = message;
			this.type = type;
		}

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public long getTime() {
			return this.time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public void setAvailable(boolean available) {
			this.available = available;
		}

		public boolean isAvailable() {
			return this.available;
		}

		public void setSendCount(int count) {
			this.sendCount = count;
		}

		public int getSendCount() {
			return this.sendCount;
		}

		public boolean needResend() {
			if (this.message.equals("PTT_DOWN") || this.message.equals("PTT_UP")) {
				return true;
			}
			return false;
		}

		public void recycle() {
			this.time = 0;
			this.message = "";
			this.type = -1;
		}
	}

	public PttEvent obtainMessage() {
		if (this.invalidEventstorage.size() <= 1) {
			return new PttEvent(System.currentTimeMillis(), "", 0);
		}
		PttEvent msg = (PttEvent) this.invalidEventstorage.poll();
		if (msg == null) {
			msg = new PttEvent(System.currentTimeMillis(), "", 0);
		}
		msg.recycle();
		msg.setAvailable(true);
		return msg;
	}

	public void recycleMessage(PttEvent msg) {
		this.invalidEventstorage.offer(msg);
	}

	public PttEvent get() {
		boolean z = false;
		try {
			this.lock.lock();
			if (this.storage.size() == 0) {
				z = true;
			}
			this.isEmpty = z;
			if (this.isEmpty) {
				this.con4Out.await();
			}
		} catch (InterruptedException e) {
			System.out.println(Thread.currentThread().getName());
			LogUtil.makeLog("MediaButtonPttEventProcesser", "get() InterruptedException set flag to exit while");
			this.isEmpty = false;
		} catch (Throwable th) {
			this.lock.unlock();
		}
		PttEvent msg = (PttEvent) this.storage.poll();
		this.lock.unlock();
		return msg;
	}

	public void put(PttEvent msg) {
		try {
			this.lock.lock();
			switch (msg.getType()) {
				case 0:
					checkSendMsg(msg);
					break;
				case 1:
					checkRecieveMsg(msg);
					break;
			}
			this.storage.offer(msg);
			this.con4Out.signal();
		} finally {
			this.lock.unlock();
		}
	}

	private void checkRecieveMsg(PttEvent message) {
		String msg = message.getMessage();
		if (msg.equals("PTT_DOWN")) {
			disableMsgs("PTT_DOWN");
			disableMsgs("PTT_UP");
		} else if (msg.equals("PTT_UP")) {
			disableMsgs("PTT_DOWN");
			disableMsgs("PTT_UP");
		}
	}

	private void checkSendMsg(PttEvent message) {
		String msg = message.getMessage();
		if (msg.equals("PTT_DOWN")) {
			disableMsgs("PTT_DOWN");
			disableMsgs("PTT_UP");
		} else if (msg.equals("PTT_UP")) {
			disableMsgs("PTT_UP");
			disableMsgs("PTT_DOWN");
		}
	}

	private void disableMsgs(String removeMsg) {
		for (PttEvent message : this.storage) {
			if (message.getMessage().equals(removeMsg)) {
				message.setAvailable(false);
			}
		}
	}

	private MediaButtonPttEventProcesser() {
		this.isEmpty = true;
		this.lock = new ReentrantLock();
		this.con4In = this.lock.newCondition();
		this.con4Out = this.lock.newCondition();
		this.storage = new LinkedList();
		this.invalidEventstorage = new LinkedList();
		this.tag = "MediaButtonPttEventProcesser";
	}

	public static MediaButtonPttEventProcesser getInstance() {
		return InstanceCreater.sInstance;
	}

	public void startProcessing() {
		this.isRunning = true;
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.makeLog("MediaButtonPttEventProcesser", "startProcessing() Exception " + e.getMessage());
		} finally {
			LogUtil.makeLog("MediaButtonPttEventProcesser", "MediaButtonPttEventProcesser   startProcessing()");
		}
	}

	public void stopProcessing() {
		this.isRunning = false;
		try {
			interrupt();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.makeLog("MediaButtonPttEventProcesser", "stopProcessing() Exception " + e.getMessage());
		} finally {
			LogUtil.makeLog("MediaButtonPttEventProcesser", "MediaButtonPttEventProcesser   stopProcessing()");
		}
	}

	public void run() {
		Looper.prepare();
		LogUtil.makeLog("MediaButtonPttEventProcesser", "MediaButtonPttEventProcesser   run begin");
		StringBuilder builder = new StringBuilder();
		while (this.isRunning) {
			if (builder.length() > 0) {
				builder.delete(0, builder.length());
			}
			PttEvent message = get();
			builder.append(" get()");
			builder.append(" storage.size() " + this.storage.size());
			if (message != null) {
				builder.append(" delay " + (System.currentTimeMillis() - message.getTime()));
				String msg = message.getMessage();
				if (message.isAvailable()) {
					boolean pressed = msg.equals("PTT_DOWN");
					builder.append("  msg.equals(PttEvent.PTT_DOWN) is " + pressed);
					mIsPttDowned = pressed;
					if (Receiver.call_state == 1) {
						if (pressed) {
							builder.append("  downPTT(" + pressed + ") UserAgent.UA_STATE_INCOMING_CALL answercall()");
							GroupCallUtil.makeGroupCall(false, true, PttPRMode.Idle);
							Receiver.engine(Receiver.mContext).answercall();
							if (DemoCallScreen.getInstance() != null) {
								this.logMsg += " DemoCallScreen.getInstance() != null  DemoCallScreen.answerCall()";
								DemoCallScreen.getInstance().answerCall();
							} else {
								this.logMsg += " CallUtil.answerCall()";
								CallUtil.answerCall();
							}
						}
					} else if (UserAgent.ua_ptt_mode) {
						if (msg.equals("PTT_DOWN")) {
							builder.append(" MediaButtonPttEventProcesser   GroupCallUtil.makeGroupCall(true, true)");
							GroupCallUtil.makeGroupCall(true, true, PttPRMode.SideKeyPress);
						} else if (msg.equals("PTT_UP")) {
							builder.append(" MediaButtonPttEventProcesser   GroupCallUtil.makeGroupCall(false, true);");
							GroupCallUtil.makeGroupCall(false, true, PttPRMode.Idle);
						} else {
							builder.append(" MediaButtonPttEventProcesser   unkown msg " + msg);
						}
					} else if (Receiver.call_state == 3) {
						boolean z;
						if (mIsPttDowned) {
							z = false;
						} else {
							z = true;
						}
						RtpStreamReceiverUtil.setNeedWriteAudioData(z);
						RtpStreamSenderUtil.reCheckNeedSendMuteData("MediaButtonPttEventProcesser");
					}
					this.mLastEvent = message;
					recycleMessage(message);
				} else {
					builder.append("  message.isAvailable() is false   continue");
					LogUtil.makeLog("MediaButtonPttEventProcesser", builder.toString());
				}
			} else {
				builder.append(" return null ");
			}
			LogUtil.makeLog("MediaButtonPttEventProcesser", builder.toString());
		}
		LogUtil.makeLog("MediaButtonPttEventProcesser", "MediaButtonPttEventProcesser   run end");
		Looper.loop();
	}
}
