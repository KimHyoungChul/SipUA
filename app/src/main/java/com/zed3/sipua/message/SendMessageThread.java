package com.zed3.sipua.message;

import android.content.Context;

import com.zed3.log.MyLog;

import java.io.OutputStream;
import java.net.Socket;

public class SendMessageThread extends Thread {
	private static final String TAG = "SendMessageThread";
	private String dataId;
	private String ip;
	private Context mContext;
	private int port;
	private byte[] send_byte;
	private Socket tcp_socket;

	public SendMessageThread(final String ip, final int port, final Context mContext, final byte[] send_byte, final String dataId) {
		this.dataId = null;
		this.ip = ip;
		this.dataId = dataId;
		this.port = port;
		this.mContext = mContext;
		this.send_byte = send_byte;
	}

	private void initSendSocket(final String s, final int n) {
		try {
			this.tcp_socket = new Socket(s, n);
			MyLog.i("SendMessageThread", "initSendSocket ");
		} catch (Exception ex) {
			MyLog.e("SendMessageThread", "initSendSocket error:");
			ex.printStackTrace();
		}
	}

	private void onSendCompleted() {
		MessageSender.updateMmsState(this.dataId, 3);
	}

	private void onSendError() {
		MessageSender.updateMmsState(this.dataId, 1);
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.initSendSocket(this.ip, this.port);
				final OutputStream outputStream = this.tcp_socket.getOutputStream();
				outputStream.write(this.send_byte);
				outputStream.flush();
				this.onSendCompleted();
				MyLog.i("SendMessageThread", "begin send data by socket");
				super.run();
			} catch (Exception ex) {
				MyLog.e("SendMessageThread", "send message thread error:");
				ex.printStackTrace();
				this.onSendError();
				continue;
			}
			break;
		}
	}
}
