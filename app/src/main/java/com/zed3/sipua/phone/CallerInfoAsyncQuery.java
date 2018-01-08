package com.zed3.sipua.phone;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Contacts.Phones;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

public class CallerInfoAsyncQuery {
	private static final boolean DBG = false;
	private static final int EVENT_ADD_LISTENER = 2;
	private static final int EVENT_EMERGENCY_NUMBER = 4;
	private static final int EVENT_END_OF_QUEUE = 3;
	private static final int EVENT_NEW_QUERY = 1;
	private static final int EVENT_VOICEMAIL_NUMBER = 5;
	private static final String LOG_TAG = "CallerInfoAsyncQuery";
	private CallerInfoAsyncQueryHandler mHandler;

	private class CallerInfoAsyncQueryHandler extends AsyncQueryHandler {
		private CallerInfo mCallerInfo;
		private Context mQueryContext;
		private Uri mQueryUri;

		protected class CallerInfoWorkerHandler extends WorkerHandler {
			public CallerInfoWorkerHandler(Looper looper) {
				// TODO
				super(looper);
			}

			public void handleMessage(Message msg) {
				// TODO
			}
		}

		private CallerInfoAsyncQueryHandler(Context context) {
			super(context.getContentResolver());
		}

		protected Handler createHandler(Looper looper) {
			return new CallerInfoWorkerHandler(looper);
		}

		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			CookieWrapper cw = (CookieWrapper) cookie;
			if (cw != null) {
				if (cw.event == 3) {
					CallerInfoAsyncQuery.this.release();
					return;
				}
				if (this.mCallerInfo == null) {
					if (this.mQueryContext == null || this.mQueryUri == null) {
						throw new QueryPoolException("Bad context or query uri, or CallerInfoAsyncQuery already released.");
					}
					this.mCallerInfo = CallerInfo.getCallerInfo(this.mQueryContext, this.mQueryUri, cursor);
					if (!TextUtils.isEmpty(cw.number)) {
						if (this.mCallerInfo.name == null && !cw.number.equals(cw.number2)) {
							this.mCallerInfo.name = cw.number2;
						}
						this.mCallerInfo.phoneNumber = PhoneNumberUtils.formatNumber(cw.number);
					}
					CookieWrapper endMarker = new CookieWrapper();
					endMarker.event = 3;
					startQuery(token, endMarker, null, null, null, null, null);
				}
				if (cw.listener != null) {
					cw.listener.onQueryComplete(token, cw.cookie, this.mCallerInfo);
				}
			}
		}
	}

	private static final class CookieWrapper {
		public Object cookie;
		public int event;
		public OnQueryCompleteListener listener;
		public String number;
		public String number2;

		private CookieWrapper() {
		}
	}

	public interface OnQueryCompleteListener {
		void onQueryComplete(int i, Object obj, CallerInfo callerInfo);
	}

	public static class QueryPoolException extends SQLException {
		public QueryPoolException(String error) {
			super(error);
		}
	}

	private CallerInfoAsyncQuery() {
	}

	public static CallerInfoAsyncQuery startQuery(int token, Context context, Uri contactRef, OnQueryCompleteListener listener, Object cookie) {
		CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
		c.allocate(context, contactRef);
		CookieWrapper cw = new CookieWrapper();
		cw.listener = listener;
		cw.cookie = cookie;
		cw.event = 1;
		c.mHandler.startQuery(token, cw, contactRef, null, null, null, null);
		return c;
	}

	public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, String number2, OnQueryCompleteListener listener, Object cookie) {
		String number_search = number;
		if (number.contains("&")) {
			number_search = number.substring(0, number.indexOf("&"));
		}
		Uri contactRef = Uri.withAppendedPath(Phones.CONTENT_FILTER_URL, number_search);
		CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
		c.allocate(context, contactRef);
		CookieWrapper cw = new CookieWrapper();
		cw.listener = listener;
		cw.cookie = cookie;
		cw.number = number;
		cw.number2 = number2;
		cw.event = 1;
		c.mHandler.startQuery(token, cw, contactRef, null, null, null, null);
		return c;
	}

	public void addQueryListener(int token, OnQueryCompleteListener listener, Object cookie) {
		CookieWrapper cw = new CookieWrapper();
		cw.listener = listener;
		cw.cookie = cookie;
		cw.event = 2;
		this.mHandler.startQuery(token, cw, null, null, null, null, null);
	}

	private void allocate(Context context, Uri contactRef) {
		if (context == null || contactRef == null) {
			throw new QueryPoolException("Bad context or query uri.");
		}
		this.mHandler = new CallerInfoAsyncQueryHandler(context);
		this.mHandler.mQueryContext = context;
		this.mHandler.mQueryUri = contactRef;
	}

	private void release() {
		this.mHandler.mQueryContext = null;
		this.mHandler.mQueryUri = null;
		this.mHandler.mCallerInfo = null;
		this.mHandler = null;
	}

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}
}
