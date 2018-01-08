package com.zed3.sipua.phone;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Contacts;

public class CallerInfo {
	public static final String PRIVATE_NUMBER = "-2";
	private static final String TAG = "CallerInfo";
	public static final String UNKNOWN_NUMBER = "-1";
	public Drawable cachedPhoto;
	public Uri contactRefUri;
	public Uri contactRingtoneUri;
	public boolean isCachedPhotoCurrent;
	public String name;
	public boolean needUpdate;
	public String numberLabel;
	public int numberType;
	public long person_id;
	public String phoneLabel;
	public String phoneNumber;
	public int photoResource;
	public boolean shouldSendToVoicemail;

	public static CallerInfo getCallerInfo(final Context context, final Uri uri) {
		return getCallerInfo(context, uri, context.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null));
	}

	public static CallerInfo getCallerInfo(final Context context, final Uri contactRefUri, final Cursor cursor) {
		boolean shouldSendToVoicemail = true;
		final CallerInfo callerInfo = new CallerInfo();
		callerInfo.photoResource = 0;
		callerInfo.phoneLabel = null;
		callerInfo.numberType = 0;
		callerInfo.numberLabel = null;
		callerInfo.cachedPhoto = null;
		callerInfo.isCachedPhotoCurrent = false;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				final int columnIndex = cursor.getColumnIndex("name");
				if (columnIndex != -1) {
					callerInfo.name = cursor.getString(columnIndex);
				}
				final int columnIndex2 = cursor.getColumnIndex("number");
				if (columnIndex2 != -1) {
					callerInfo.phoneNumber = cursor.getString(columnIndex2);
				}
				final int columnIndex3 = cursor.getColumnIndex("label");
				if (columnIndex3 != -1) {
					final int columnIndex4 = cursor.getColumnIndex("type");
					if (columnIndex4 != -1) {
						callerInfo.numberType = cursor.getInt(columnIndex4);
						callerInfo.numberLabel = cursor.getString(columnIndex3);
						callerInfo.phoneLabel = Contacts.Phones.getDisplayLabel(context, callerInfo.numberType, (CharSequence) callerInfo.numberLabel).toString();
					}
				}
				final int columnIndex5 = cursor.getColumnIndex("person");
				if (columnIndex5 != -1) {
					callerInfo.person_id = cursor.getLong(columnIndex5);
				} else {
					final int columnIndex6 = cursor.getColumnIndex("_id");
					if (columnIndex6 != -1) {
						callerInfo.person_id = cursor.getLong(columnIndex6);
					}
				}
				final int columnIndex7 = cursor.getColumnIndex("custom_ringtone");
				if (columnIndex7 != -1 && cursor.getString(columnIndex7) != null) {
					callerInfo.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex7));
				} else {
					callerInfo.contactRingtoneUri = null;
				}
				final int columnIndex8 = cursor.getColumnIndex("send_to_voicemail");
				if (columnIndex8 == -1 || cursor.getInt(columnIndex8) != 1) {
					shouldSendToVoicemail = false;
				}
				callerInfo.shouldSendToVoicemail = shouldSendToVoicemail;
			}
			cursor.close();
		}
		callerInfo.needUpdate = false;
		callerInfo.name = normalize(callerInfo.name);
		callerInfo.contactRefUri = contactRefUri;
		return callerInfo;
	}

	private static String normalize(final String s) {
		if (s == null || s.length() > 0) {
			return s;
		}
		return null;
	}
}
