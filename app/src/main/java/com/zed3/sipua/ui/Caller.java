package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.zed3.media.RtpStreamReceiver_signal;
import com.zed3.sipua.SipUAApp;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Caller extends BroadcastReceiver {
	static long noexclude;
	String last_number;
	long last_time;

	private String searchReplaceNumber(String s, final String s2) {
		final String[] split = s.split(",");
		if (split.length != 2) {
			return s2;
		}
		final String s3 = split[1];
		String s4;
		try {
			final Matcher matcher = Pattern.compile(split[0]).matcher(s2);
			s = s3;
			if (matcher.matches()) {
				int i = 0;
				s = s3;
				while (i < matcher.groupCount() + 1) {
					final String group = matcher.group(i);
					String replace = s;
					if (group != null) {
						replace = s.replace("\\" + i, group);
					}
					++i;
					s = replace;
				}
			}
			s4 = s;
			if (s.equals(split[1])) {
				s4 = s2;
			}
		} catch (PatternSyntaxException ex) {
			s4 = s2;
		}
		return s4;
	}

	Vector<String> getTokens(final String s, final String s2) {
		final Vector<String> vector = new Vector<String>();
		int i;
		int index;
		for (i = 0; i < s.lastIndexOf(s2); i = index + 1) {
			index = s.indexOf(s2, i);
			vector.add(s.substring(i, index).trim());
		}
		if (i < s.length()) {
			vector.add(s.substring(i, s.length()).trim());
		}
		return vector;
	}

	boolean isExcludedNum(final Vector<String> vector, final String s) {
		for (int i = 0; i < vector.size(); ++i) {
			try {
				final Matcher matcher = Pattern.compile(vector.get(i)).matcher(s);
				if (matcher != null && matcher.find()) {
					return true;
				}
			} catch (PatternSyntaxException ex) {
				return false;
			}
		}
		return false;
	}

	boolean isExcludedType(final Vector<Integer> vector, final String s, final Context context) {
		final Cursor query = context.getContentResolver().query(Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, s), new String[]{"number", "type"}, (String) null, (String[]) null, (String) null);
		if (query != null) {
			while (query.moveToNext()) {
				if (vector.contains(query.getInt(1))) {
					return true;
				}
			}
			query.close();
		}
		return false;
	}

	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		final String resultData = this.getResultData();
		Boolean b = false;
		if (action.equals("android.intent.action.NEW_OUTGOING_CALL") && resultData != null && SipUAApp.on(context)) {
			final boolean b2 = !PreferenceManager.getDefaultSharedPreferences(context).getString("pref", "PSTN").equals("PSTN");
			final boolean equals = PreferenceManager.getDefaultSharedPreferences(context).getString("pref", "PSTN").equals("ASK");
			if (Receiver.call_state != 0 && RtpStreamReceiver_signal.isBluetoothAvailable()) {
				this.setResultData((String) null);
				switch (Receiver.call_state) {
					case 1: {
						Receiver.engine(context).answercall();
						if (RtpStreamReceiver_signal.bluetoothmode) {
							return;
						}
						break;
					}
				}
				if (RtpStreamReceiver_signal.bluetoothmode) {
					Receiver.engine(context).rejectcall();
					return;
				}
				Receiver.engine(context).togglebluetooth();
			} else {
				if (this.last_number != null && this.last_number.equals(resultData) && SystemClock.elapsedRealtime() - this.last_time < 3000L) {
					this.setResultData((String) null);
					return;
				}
				this.last_time = SystemClock.elapsedRealtime();
				this.last_number = resultData;
				String substring = resultData;
				boolean b3 = b2;
				if (resultData.endsWith("+")) {
					if (b2) {
						b3 = false;
					} else {
						b3 = true;
					}
					substring = resultData.substring(0, resultData.length() - 1);
					b = true;
				}
				if (SystemClock.elapsedRealtime() < Caller.noexclude + 10000L) {
					Caller.noexclude = 0L;
					b = true;
				}
				boolean b4 = false;
				Label_0450:
				{
					if (b4 = b3) {
						b4 = b3;
						if (!b) {
							final String string = PreferenceManager.getDefaultSharedPreferences(context).getString("excludepat", "");
							final boolean b5 = false;
							int n = 0;
							boolean excludedType = false;
							boolean excludedNum = b5;
							if (string.length() > 0) {
								final Vector<String> tokens = this.getTokens(string, ",");
								final Vector<String> vector = new Vector<String>();
								final Vector<Integer> vector2 = new Vector<Integer>();
								for (int i = 0; i < tokens.size(); ++i) {
									if (tokens.get(i).startsWith("h") || tokens.get(i).startsWith("H")) {
										vector2.add(1);
									} else if (tokens.get(i).startsWith("m") || tokens.get(i).startsWith("M")) {
										vector2.add(2);
									} else if (tokens.get(i).startsWith("w") || tokens.get(i).startsWith("W")) {
										vector2.add(3);
									} else {
										vector.add(tokens.get(i));
									}
								}
								if (vector2.size() > 0) {
									excludedType = this.isExcludedType(vector2, substring, context);
								}
								excludedNum = b5;
								n = (excludedType ? 1 : 0);
								if (vector.size() > 0) {
									excludedNum = this.isExcludedNum(vector, substring);
									n = (excludedType ? 1 : 0);
								}
							}
							if (n == 0) {
								b4 = b3;
								if (!excludedNum) {
									break Label_0450;
								}
							}
							b4 = false;
						}
					}
				}
				if (!b4) {
					this.setResultData(substring);
					return;
				}
				if (substring != null && !intent.getBooleanExtra("android.phone.extra.ALREADY_CALLED", false)) {
					final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
					if (defaultSharedPreferences.contains("prefix")) {
						final String string2 = defaultSharedPreferences.getString("prefix", "");
						final SharedPreferences.Editor edit = defaultSharedPreferences.edit();
						if (!string2.trim().equals("")) {
							edit.putString("search", "(.*)," + string2 + "\\1");
						}
						edit.remove("prefix");
						edit.commit();
					}
					final String string3 = defaultSharedPreferences.getString("search", "");
					final String searchReplaceNumber = this.searchReplaceNumber(string3, substring);
					String s;
					if (!equals && !b && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("par", false)) {
						final Cursor query = context.getContentResolver().query(Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, substring), new String[]{"number", "type"}, (String) null, (String[]) null, "isprimary DESC");
						if (query != null) {
							String string4 = "";
							while (query.moveToNext()) {
								final int int1 = query.getInt(1);
								final String string5 = query.getString(0);
								if (!TextUtils.isEmpty((CharSequence) string5) && (int1 == 2 || int1 == 1 || int1 == 3)) {
									String string6 = string4;
									if (!string4.equals("")) {
										string6 = String.valueOf(string4) + "&";
									}
									string4 = String.valueOf(string6) + this.searchReplaceNumber(string3, PhoneNumberUtils.stripSeparators(string5));
								}
							}
							query.close();
							s = string4;
							if (string4.equals("")) {
								s = searchReplaceNumber;
							}
						} else {
							s = searchReplaceNumber;
						}
					} else {
						s = searchReplaceNumber;
					}
					if (PreferenceManager.getDefaultSharedPreferences(context).getString("pref", "PSTN").equals("SIPONLY")) {
						b = true;
					}
					if (!equals && Receiver.engine(context).call(s, b, false)) {
						this.setResultData((String) null);
						return;
					}
					if (!equals && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("callthru", false)) {
						final String string7 = PreferenceManager.getDefaultSharedPreferences(context).getString("callthru2", "");
						if (string7.length() > 0) {
							this.setResultData((String.valueOf(string7) + "," + searchReplaceNumber + "#").replaceAll(",", ",p"));
							return;
						}
					}
					if (equals || b) {
						this.setResultData((String) null);
						new Thread() {
							@Override
							public void run() {
								while (true) {
									try {
										Thread.sleep(200L);
//										final Intent intent = new Intent("android.intent.action.CALL", Uri.fromParts("sipdroid", Uri.decode(s), (String) null));
//										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//										context.startActivity(intent);
									} catch (InterruptedException ex) {
										continue;
									}
									break;
								}
							}
						}.start();
					}
				}
			}
		}
	}
}
