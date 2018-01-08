package com.zed3.sipua.phone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Contacts;
import android.text.TextUtils;

import com.zed3.sipua.ui.Receiver;

public class Connection {
	private static final String ACTION_CM_SIP = "de.ub0r.android.callmeter.SAVE_SIPCALL";
	private static final String EXTRA_SIP_PROVIDER = "provider";
	private static final String EXTRA_SIP_URI = "uri";
	String address;
	String address2;
	Call c;
	public long date;
	boolean incoming;
	Object userData;

	public static Uri addCall(final CallerInfo callerInfo, final Context context, String substring, final boolean b, final int n, final long n2, final int n3) {
		final ContentResolver contentResolver = context.getContentResolver();
		String s = substring;
		if (TextUtils.isEmpty((CharSequence) substring)) {
			if (b) {
				s = "-2";
			} else {
				s = "-1";
			}
		}
		final ContentValues contentValues = new ContentValues(5);
		substring = s;
		if (s.contains("&")) {
			substring = s.substring(0, s.indexOf("&"));
		}
		contentValues.put("number", substring);
		contentValues.put("type", n);
		contentValues.put("date", n2);
		contentValues.put("duration", (long) n3);
		contentValues.put("new", 1);
		if (callerInfo != null) {
			contentValues.put("name", callerInfo.name);
			contentValues.put("numbertype", callerInfo.numberType);
			contentValues.put("numberlabel", callerInfo.numberLabel);
		}
		if (callerInfo != null && callerInfo.person_id > 0L) {
			Contacts.People.markAsContacted(contentResolver, callerInfo.person_id);
		}
		// TODO
//		final Uri insert = contentResolver.insert(CallLog.Calls.CONTENT_URI, contentValues);
//		if (insert != null) {
//			final Intent intent = new Intent("de.ub0r.android.callmeter.SAVE_SIPCALL");
//			intent.putExtra("uri", insert.toString());
//			context.sendBroadcast(intent);
//		}
//		return insert;
		return null;
	}

	public String getAddress() {
		return this.address;
	}

	public String getAddress2() {
		return this.address2;
	}

	public Call getCall() {
		return this.c;
	}

	public DisconnectCause getDisconnectCause() {
		return DisconnectCause.NORMAL;
	}

	public Call.State getState() {
		final Call call = this.getCall();
		if (call == null) {
			return Call.State.IDLE;
		}
		return call.getState();
	}

	public Object getUserData() {
		return this.userData;
	}

	public boolean isAlive() {
		return this.getState().isAlive();
	}

	public boolean isIncoming() {
		return this.incoming;
	}

	public boolean isRinging() {
		return this.getState().isRinging();
	}

	public void log(long n) {
		final String address = this.getAddress();
		if (n != 0L) {
			n = SystemClock.elapsedRealtime() - n;
		} else {
			n = 0L;
		}
		int n2;
		if (this.isIncoming()) {
			if (n == 0L) {
				n2 = 3;
			} else {
				n2 = 1;
			}
		} else {
			n2 = 2;
		}
		final Object userData = this.getUserData();
		CallerInfo currentInfo;
		if (userData == null || userData instanceof CallerInfo) {
			currentInfo = (CallerInfo) userData;
		} else {
			currentInfo = ((PhoneUtils.CallerInfoToken) userData).currentInfo;
		}
		if (n2 == 3) {
			String name = address;
			if (currentInfo != null) {
				name = address;
				if (currentInfo.name != null) {
					name = currentInfo.name;
				}
			}
			Receiver.onText(3, name, 17301631, 0L);
		}
	}

	public void setAddress(final String address, final String address2) {
		this.address = address;
		this.address2 = address2;
	}

	public void setCall(final Call c) {
		this.c = c;
	}

	public void setIncoming(final boolean incoming) {
		this.incoming = incoming;
	}

	public void setUserData(final Object userData) {
		this.userData = userData;
	}

	public enum DisconnectCause {
		BUSY("BUSY", 4),
		CALL_BARRED("CALL_BARRED", 14),
		CONGESTION("CONGESTION", 5),
		FDN_BLOCKED("FDN_BLOCKED", 15),
		INCOMING_MISSED("INCOMING_MISSED", 1),
		INCOMING_REJECTED("INCOMING_REJECTED", 10),
		INVALID_NUMBER("INVALID_NUMBER", 7),
		LIMIT_EXCEEDED("LIMIT_EXCEEDED", 9),
		LOCAL("LOCAL", 3),
		LOST_SIGNAL("LOST_SIGNAL", 8),
		MMI("MMI", 6),
		NORMAL("NORMAL", 2),
		NOT_DISCONNECTED("NOT_DISCONNECTED", 0),
		OUT_OF_SERVICE("OUT_OF_SERVICE", 12),
		POWER_OFF("POWER_OFF", 11),
		SIM_ERROR("SIM_ERROR", 13);

		private DisconnectCause(final String s, final int n) {
		}
	}
}
