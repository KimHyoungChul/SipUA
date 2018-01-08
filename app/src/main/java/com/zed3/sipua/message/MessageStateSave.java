package com.zed3.sipua.message;

import android.content.Context;
import android.content.SharedPreferences;

public class MessageStateSave {
	Context context;

	public MessageStateSave(final Context context) {
		this.context = context;
	}

	public boolean getFlag() {
		return this.context.getSharedPreferences("flag", 0).getBoolean("flag", true);
	}

	public void putFlag(final Boolean b) {
		final SharedPreferences.Editor edit = this.context.getSharedPreferences("flag", 0).edit();
		edit.putBoolean("flag", (boolean) b);
		edit.commit();
	}
}
