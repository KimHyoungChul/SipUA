package com.zed3.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextPaint;

import com.zed3.sipua.SipUAApp;

import java.util.Locale;

public class DialogMessageTool {
	public static String getString(final int n, float measureText, final String s) {
		final int int1 = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("languageId", 0);
		final Configuration configuration = SipUAApp.mContext.getResources().getConfiguration();
		new AlertDialog.Builder(SipUAApp.mContext);
		// TODO
		Label_0073:
		{
			if (int1 != 2) {
				if (int1 == 0) {
					final Locale locale = configuration.locale;
					if (Locale.getDefault().getLanguage().equals("en")) {
						break Label_0073;
					}
				}
				return s;
			}
		}
		final StringBuffer sb = new StringBuffer();
		final StringBuffer sb2 = new StringBuffer();
		final String[] split = newString(s).split(" ");
		final TextPaint textPaint = new TextPaint();
		textPaint.setTextSize(measureText);
		measureText = textPaint.measureText(" ");
		int i = 0;
		StringBuffer sb3 = sb2;
		while (i < split.length) {
			if (textPaint.measureText(sb3.toString()) + textPaint.measureText(split[i]) + measureText < n) {
				if (i == 0) {
					sb.append(split[i]);
					sb3.append(split[i]);
				} else {
					sb.append(" " + split[i]);
					sb3.append(" " + split[i]);
				}
			} else {
				sb.append("\n" + split[i]);
				sb3 = new StringBuffer();
				sb3.append(split[i]);
			}
			++i;
		}
		return sb.toString();
	}

	private static String newString(final String s) {
		return s.replaceAll("\\,", ", ").replaceAll("\\.", ". ").replaceAll("\\?", "? ").replaceAll("\\!", "! ");
	}
}
