package com.zed3.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.zed3.sipua.welcome.DeviceInfo;

import java.util.Locale;

public class LanguageChange {
	private static void commit(final Context context, final String s, final boolean b) {
		final SharedPreferences.Editor edit = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putBoolean(s, b);
		edit.commit();
	}

	public static void upDateLanguage(final Context context) {
		final int int1 = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("languageId", 0);
		System.out.println(String.valueOf(int1) + "ladddd");
		final Resources resources = context.getResources();
		final Configuration configuration = resources.getConfiguration();
		final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		switch (int1) {
			case 0: {
				configuration.locale = Locale.getDefault();
				if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
					DeviceInfo.CONFIG_MAP_TYPE = 0;
					break;
				}
				DeviceInfo.CONFIG_MAP_TYPE = 1;
				break;
			}
			case 1: {
				configuration.locale = Locale.SIMPLIFIED_CHINESE;
				DeviceInfo.CONFIG_MAP_TYPE = 0;
				break;
			}
			case 2: {
				configuration.locale = Locale.ENGLISH;
				DeviceInfo.CONFIG_MAP_TYPE = 1;
				break;
			}
			case 3: {
				configuration.locale = Locale.TAIWAN;
				DeviceInfo.CONFIG_MAP_TYPE = 1;
				break;
			}
		}
		resources.updateConfiguration(configuration, displayMetrics);
	}
}
