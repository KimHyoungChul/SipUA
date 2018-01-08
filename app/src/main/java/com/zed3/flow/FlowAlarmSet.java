package com.zed3.flow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;

public class FlowAlarmSet extends Activity {
	ImageButton cancelBtn;
	ImageButton confirmBtn;
	EditText editTxt;
	SharedPreferences mypre;
	private final String sharedPrefsFile;

	public FlowAlarmSet() {
		this.sharedPrefsFile = "com.zed3.sipua_preferences";
		this.confirmBtn = null;
		this.cancelBtn = null;
		this.editTxt = null;
		this.mypre = null;
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.flowalarmset);
		(this.confirmBtn = (ImageButton) this.findViewById(R.id.confrim)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final String string = FlowAlarmSet.this.editTxt.getText().toString();
				if (!string.toString().equals("")) {
					if (Double.parseDouble(string) * 1024.0 * 1024.0 <= MemoryMg.getInstance().User_3GTotal) {
						FlowAlarmSet.this.mypre = FlowAlarmSet.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
						final SharedPreferences.Editor edit = FlowAlarmSet.this.mypre.edit();
						MemoryMg.getInstance().User_3GFlowOut = Double.parseDouble(FlowAlarmSet.this.editTxt.getText().toString());
						edit.putString("3gflowoutval", new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GFlowOut)).toString());
						edit.commit();
					} else {
						Toast.makeText((Context) FlowAlarmSet.this, (CharSequence) "\u6d41\u91cf\u503c\u8bbe\u7f6e\u9519\u8bef", Toast.LENGTH_SHORT).show();
					}
				}
				FlowAlarmSet.this.finish();
			}
		});
		(this.cancelBtn = (ImageButton) this.findViewById(R.id.cancel)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				FlowAlarmSet.this.finish();
			}
		});
		(this.editTxt = (EditText) this.findViewById(R.id.edittxt)).setText((CharSequence) new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GFlowOut)).toString());
	}
}
