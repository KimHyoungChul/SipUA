package com.zed3.sipua.message;

import android.content.Context;
import android.text.TextUtils;

import com.zed3.log.MyLog;
import com.zed3.sipua.ui.Receiver;

public class MmsMessageService {
	private static final String TAG = "MmsMessageService";
	private String E_id;
	private String check_id;
	private String connection;
	private int flag;
	private String recipient_num;
	private String report_attr;
	private int size;
	private String str_header;

	public MmsMessageService(final String connection, final int flag, final int size, final String e_id, final String check_id, final String recipient_num, final String report_attr) {
		this.connection = null;
		this.flag = -1;
		this.size = 0;
		this.str_header = null;
		this.E_id = null;
		this.check_id = null;
		this.recipient_num = null;
		this.report_attr = null;
		this.connection = connection;
		this.flag = flag;
		this.size = size;
		this.E_id = e_id;
		this.check_id = check_id;
		this.str_header = String.valueOf(e_id) + check_id;
		this.recipient_num = recipient_num;
		this.report_attr = report_attr;
	}

	private byte[] getSendByte() {
		final byte[] bytes = this.str_header.getBytes();
		final MessageSender messageSender = new MessageSender(Receiver.mContext);
		String s;
		if (TextUtils.isEmpty((CharSequence) this.E_id)) {
			s = MessageSender.getSendDataId();
		} else {
			s = this.E_id;
		}
		final byte[] mmsTxtByte = messageSender.getMmsTxtByte(s);
		final byte[] array = new byte[bytes.length + mmsTxtByte.length];
		System.arraycopy(bytes, 0, array, 0, bytes.length);
		System.arraycopy(mmsTxtByte, 0, array, bytes.length, mmsTxtByte.length);
		return array;
	}

	public void initSocket() {
		final String[] split = this.connection.split("/");
		final String s = split[0];
		final int int1 = Integer.parseInt(split[1]);
		if (this.flag == 0) {
			final Context mContext = Receiver.mContext;
			final byte[] sendByte = this.getSendByte();
			String s2;
			if (TextUtils.isEmpty((CharSequence) this.E_id)) {
				s2 = MessageSender.getSendDataId();
			} else {
				s2 = this.E_id;
			}
			new SendMessageThread(s, int1, mContext, sendByte, s2).start();
			return;
		}
		if (this.flag == 1) {
			new ReceiveMessageThread(s, int1, Receiver.mContext, this.size, this.E_id, this.check_id, true, this.recipient_num, this.report_attr).start();
			return;
		}
		MyLog.i("MmsMessageService", "initSocket error, flag = " + this.flag);
	}
}
