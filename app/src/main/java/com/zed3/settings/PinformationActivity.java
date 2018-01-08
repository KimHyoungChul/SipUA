package com.zed3.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.Tools;

public class PinformationActivity extends BaseActivity {
	private static SharedPreferences mSharedPreferences;
	private TextView accountT;
	LinearLayout btn_left;
	private TextView locationT;
	int mode;
	private TextView nameT;

	private void commit(final String s, final int n) {
		final SharedPreferences.Editor edit = PinformationActivity.mSharedPreferences.edit();
		edit.putInt(s, n);
		edit.commit();
	}

	private int findWhich(final String s, final int n) {
		return PinformationActivity.mSharedPreferences.getInt(s, n);
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_pinformation);
		PinformationActivity.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		((TextView) this.findViewById(R.id.title)).setText(R.string.personal_information);
		this.nameT = (TextView) this.findViewById(R.id.name);
		this.accountT = (TextView) this.findViewById(R.id.account);
		this.locationT = (TextView) this.findViewById(R.id.locationType);
		final String string = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("displayname", "");
		if (!TextUtils.isEmpty((CharSequence) DeviceInfo.AutoVNoName)) {
			final String autoVNoName = DeviceInfo.AutoVNoName;
		}
		this.nameT.setText((CharSequence) (String.valueOf(this.getString(R.string.name)) + string));
		this.accountT.setText((CharSequence) (String.valueOf(this.getString(R.string.account)) + new AutoConfigManager((Context) this).fetchLocalUserName()));
		final int currentGpsMode = Tools.getCurrentGpsMode();
		MyLog.i("gpsmode", "PinformationActivitymode++++" + currentGpsMode);
		this.locationT.setText((CharSequence) (String.valueOf(this.getString(R.string.setting_position_1)) + ":"));
		if (currentGpsMode == 0) {
			this.locationT.append((CharSequence) this.getString(R.string.setting_position_3));
		} else if (currentGpsMode == 1) {
			this.locationT.append((CharSequence) this.getString(R.string.setting_position_4));
		} else if (currentGpsMode == 2) {
			this.locationT.append((CharSequence) this.getString(R.string.setting_position_5));
		} else if (currentGpsMode == 3) {
			this.locationT.append((CharSequence) this.getString(R.string.setting_position_6));
		} else if (currentGpsMode == 4) {
			this.locationT.append((CharSequence) this.getString(R.string.setting_position_9));
		}
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.settings);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				PinformationActivity.this.finish();
			}
		});
	}
}
