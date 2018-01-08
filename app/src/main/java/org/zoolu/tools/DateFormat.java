package org.zoolu.tools;

import java.util.Calendar;
import java.util.Date;

public class DateFormat {
	private static final String[] MONTHS;
	private static final String[] WEEKDAYS;

	static {
		MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		WEEKDAYS = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	}

	public static String formatEEEddMMM(final Date time) {
		final Calendar instance = Calendar.getInstance();
		instance.setTime(time);
		final String s = DateFormat.WEEKDAYS[instance.get(Calendar.DAY_OF_WEEK) - 1];
		final String s2 = DateFormat.MONTHS[instance.get(Calendar.MONTH)];
		final String string = Integer.toString(instance.get(Calendar.YEAR));
		final String string2 = Integer.toString(instance.get(Calendar.DATE));
		final String string3 = Integer.toString(instance.get(Calendar.HOUR_OF_DAY));
		final String string4 = Integer.toString(instance.get(Calendar.MINUTE));
		final String string5 = Integer.toString(instance.get(Calendar.SECOND));
		String string6 = string2;
		if (string2.length() == 1) {
			string6 = "0" + string2;
		}
		String string7 = string3;
		if (string3.length() == 1) {
			string7 = "0" + string3;
		}
		String string8 = string4;
		if (string4.length() == 1) {
			string8 = "0" + string4;
		}
		String string9 = string5;
		if (string5.length() == 1) {
			string9 = "0" + string5;
		}
		return String.valueOf(s) + ", " + string6 + " " + s2 + " " + string + " " + string7 + ":" + string8 + ":" + string9 + " GMT";
	}

	public static String formatHHMMSS(final Date time) {
		final Calendar instance = Calendar.getInstance();
		instance.setTime(time);
		final String s = DateFormat.WEEKDAYS[instance.get(Calendar.DAY_OF_WEEK) - 1];
		final String s2 = DateFormat.MONTHS[instance.get(Calendar.MONTH)];
		final String string = Integer.toString(instance.get(Calendar.YEAR));
		final String string2 = Integer.toString(instance.get(Calendar.DATE));
		final String string3 = Integer.toString(instance.get(Calendar.HOUR_OF_DAY));
		final String string4 = Integer.toString(instance.get(Calendar.MINUTE));
		final String string5 = Integer.toString(instance.get(Calendar.SECOND));
		final String string6 = Integer.toString(instance.get(Calendar.MILLISECOND));
		String string7 = string2;
		if (string2.length() == 1) {
			string7 = "0" + string2;
		}
		String string8 = string3;
		if (string3.length() == 1) {
			string8 = "0" + string3;
		}
		String string9 = string4;
		if (string4.length() == 1) {
			string9 = "0" + string4;
		}
		String string10 = string5;
		if (string5.length() == 1) {
			string10 = "0" + string5;
		}
		String s3;
		if (string6.length() == 1) {
			s3 = "00" + string6;
		} else {
			s3 = string6;
			if (string6.length() == 2) {
				s3 = "0" + string6;
			}
		}
		return String.valueOf(string8) + ":" + string9 + ":" + string10 + "." + s3 + " " + s + " " + string7 + " " + s2 + " " + string;
	}

	public static String formatYYYYMMDD(final Date time) {
		final Calendar instance = Calendar.getInstance();
		instance.setTime(time);
		final String string = Integer.toString(instance.get(Calendar.YEAR));
		final String string2 = Integer.toString(instance.get(Calendar.DATE));
		final String string3 = Integer.toString(instance.get(Calendar.HOUR_OF_DAY));
		final String string4 = Integer.toString(instance.get(Calendar.MINUTE));
		final String string5 = Integer.toString(instance.get(Calendar.SECOND));
		final String string6 = Integer.toString(instance.get(Calendar.MILLISECOND));
		String string7 = string2;
		if (string2.length() == 1) {
			string7 = "0" + string2;
		}
		String string8 = string3;
		if (string3.length() == 1) {
			string8 = "0" + string3;
		}
		String string9 = string4;
		if (string4.length() == 1) {
			string9 = "0" + string4;
		}
		String string10 = string5;
		if (string5.length() == 1) {
			string10 = "0" + string5;
		}
		String s;
		if (string6.length() == 1) {
			s = "00" + string6;
		} else {
			s = string6;
			if (string6.length() == 2) {
				s = "0" + string6;
			}
		}
		String s3;
		final String s2 = s3 = Integer.toString(instance.get(Calendar.MONTH) + 1);
		if (s2.length() == 1) {
			s3 = "0" + s2;
		}
		return String.valueOf(string) + "-" + s3 + "-" + string7 + " " + string8 + ":" + string9 + ":" + string10 + "." + s;
	}

	public static Date parseEEEddMMM(final String s, int n) {
		final Calendar instance = Calendar.getInstance();
		final char[] array2;
		final char[] array = array2 = new char[3];
		array2[0] = ' ';
		array2[1] = ',';
		array2[2] = ':';
		final Parser parser = new Parser(s, n);
		final int int1 = parser.getInt();
		String string;
		for (string = parser.getString(), n = 0; n < 12 && !string.equalsIgnoreCase(DateFormat.MONTHS[n]); ++n) {
		}
		if (n == 12) {
			return null;
		}
		final int int2 = parser.getInt();
		final int int3 = Integer.parseInt(parser.getWord(array));
		final int int4 = Integer.parseInt(parser.getWord(array));
		final int int5 = Integer.parseInt(parser.getWord(array));
		instance.set(Calendar.YEAR, int2);
		instance.set(Calendar.MONTH, n);
		instance.set(Calendar.DATE, int1);
		instance.set(Calendar.HOUR_OF_DAY, int3);
		instance.set(Calendar.MINUTE, int4);
		instance.set(Calendar.SECOND, int5);
		return instance.getTime();
	}
}
