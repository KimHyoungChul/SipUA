package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActvityNotify extends Activity {
	public void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		final Intent intent = new Intent();
		if (this.getIntent().getExtras() != null) {
			intent.putExtras(this.getIntent().getExtras());
		}
		intent.setClass((Context) this, (Class) GrpCallNotify.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivity(intent);
		this.finish();
	}
}
