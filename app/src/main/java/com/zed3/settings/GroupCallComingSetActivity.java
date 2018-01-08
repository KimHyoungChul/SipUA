package com.zed3.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.SwitchButton;

public class GroupCallComingSetActivity extends BaseActivity implements View.OnClickListener {
	LinearLayout btn_left;
	LinearLayout high;
	TextView high_summary;
	String keyVal = "";
	LinearLayout low;
	TextView low_summary;
	SharedPreferences mypre = null;
	TextView ptt_summary;
	LinearLayout pttkey;
	LinearLayout restore;
	TextView restore_summary;
	SwitchButton restore_switcher;
	LinearLayout same;
	TextView same_summary;
	private final String sharedPrefsFile = "com.zed3.sipua_preferences";

	class C10466 implements DialogInterface.OnClickListener {
		C10466() {
		}

		public void onClick(DialogInterface dialog, int whichButton) {
		}
	}

	class C10488 implements DialogInterface.OnClickListener {
		C10488() {
		}

		public void onClick(DialogInterface dialog, int whichButton) {
		}
	}

	private void commit(final String s, final int n) {
		final SharedPreferences.Editor edit = this.mypre.edit();
		edit.putInt(s, n);
		edit.commit();
	}

	private void commit(final String s, final boolean b) {
		final SharedPreferences.Editor edit = this.mypre.edit();
		edit.putBoolean(s, b);
		edit.commit();
	}

	private Dialog createDialog(final int title, int pos) {
		return new Builder(this).setTitle(title).setSingleChoiceItems(R.array.name_list, pos, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (title == R.string.setting_intercom_1) {
					GroupCallComingSetActivity.this.commit(Settings.HIGH_PRI_KEY, whichButton);
				} else if (title == R.string.setting_intercom_2) {
					GroupCallComingSetActivity.this.commit(Settings.SAME_PRI_KEY, whichButton);
				} else if (title == R.string.setting_intercom_3) {
					GroupCallComingSetActivity.this.commit(Settings.LOW_PRI_KEY, whichButton);
				}
				GroupCallComingSetActivity.this.updateSunmary();
				GroupCallComingSetActivity.this.updateGrpCallConfig();
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.cancel, new C10488()).create();
	}

	private int findWhich(final String s, final int n) {
		return this.mypre.getInt(s, n);
	}

	private UserAgent.GrpCallSetupType getPriority(final int n) {
		if (n == 0) {
			return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_TIP;
		}
		if (n == 1) {
			return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_ACCEPT;
		}
		return UserAgent.GrpCallSetupType.GRPCALLSETUPTYPE_REJECT;
	}

	private void showDialog_Layout() {
		View textEntryView = LayoutInflater.from(this).inflate(R.layout.dialoglayout, null);
		final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
		edtInput.setText(this.keyVal);
		Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon22);
		builder.setTitle(R.string.key_set);
		builder.setView(textEntryView);
		builder.setPositiveButton(getResources().getString(R.string.key), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				GroupCallComingSetActivity.this.mypre = GroupCallComingSetActivity.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				Editor editor = GroupCallComingSetActivity.this.mypre.edit();
				if (!edtInput.getText().toString().equals("")) {
					editor.putString("pttkey", edtInput.getText().toString());
				}
				editor.commit();
				GroupCallComingSetActivity.this.ptt_summary.setText(new StringBuilder(String.valueOf(GroupCallComingSetActivity.this.getResources().getString(R.string.key))).append(edtInput.getText().toString()).toString());
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancel), new C10466());
		builder.show();
	}

	private void updateGrpCallConfig() {
		Receiver.GetCurUA().SetGrpCallConfig(this.getPriority(this.mypre.getInt("highPriority", 1)), this.getPriority(this.mypre.getInt("samePriority", 0)), this.getPriority(this.mypre.getInt("lowPriority", 2)));
	}

	private void updateSunmary() {
		this.high_summary.setText((CharSequence) this.getResources().getStringArray(R.array.name_list)[this.mypre.getInt("highPriority", 1)]);
		this.same_summary.setText((CharSequence) this.getResources().getStringArray(R.array.name_list)[this.mypre.getInt("samePriority", 0)]);
		this.low_summary.setText((CharSequence) this.getResources().getStringArray(R.array.name_list)[this.mypre.getInt("lowPriority", 2)]);
		this.keyVal = this.mypre.getString("pttkey", "140");
		this.ptt_summary.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.key)) + this.keyVal));
		if (this.mypre.getBoolean("restoreAfterOtherGrp", true)) {
			this.restore_switcher.setChecked(true);
			this.restore_summary.setText(R.string.rate_suspension_2);
			return;
		}
		this.restore_switcher.setChecked(false);
		this.restore_summary.setText(R.string.rate_suspension_1);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.high: {
				this.createDialog(R.string.setting_intercom_1, this.findWhich("highPriority", 1)).show();
			}
			case R.id.same: {
				this.createDialog(R.string.setting_intercom_2, this.findWhich("samePriority", 0)).show();
			}
			case R.id.low: {
				this.createDialog(R.string.setting_intercom_3, this.findWhich("lowPriority", 2)).show();
			}
			case R.id.pttkey: {
				this.showDialog_Layout();
			}
			case R.id.restore: {
				this.restore_switcher.toggle();
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_groupcallcoming);
		((TextView) this.findViewById(R.id.title)).setText(R.string.setting_intercom_call);
		((ImageButton) this.findViewById(R.id.back)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				GroupCallComingSetActivity.this.finish();
			}
		});
		(this.high = (LinearLayout) this.findViewById(R.id.high)).setOnClickListener((View.OnClickListener) this);
		(this.same = (LinearLayout) this.findViewById(R.id.same)).setOnClickListener((View.OnClickListener) this);
		(this.low = (LinearLayout) this.findViewById(R.id.low)).setOnClickListener((View.OnClickListener) this);
		(this.pttkey = (LinearLayout) this.findViewById(R.id.pttkey)).setOnClickListener((View.OnClickListener) this);
		(this.restore = (LinearLayout) this.findViewById(R.id.restore)).setOnClickListener((View.OnClickListener) this);
		this.restore_switcher = (SwitchButton) this.findViewById(R.id.restore_switcher);
		this.high_summary = (TextView) this.findViewById(R.id.high_summary);
		this.same_summary = (TextView) this.findViewById(R.id.same_summary);
		this.low_summary = (TextView) this.findViewById(R.id.low_summary);
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		this.ptt_summary = (TextView) this.findViewById(R.id.pttsummary);
		this.restore_summary = (TextView) this.findViewById(R.id.restore_summary);
		this.updateSunmary();
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.advanced);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				GroupCallComingSetActivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) GroupCallComingSetActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) GroupCallComingSetActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						GroupCallComingSetActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(GroupCallComingSetActivity.this.getResources().getColor(R.color.font_color3));
						GroupCallComingSetActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.restore_switcher.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				GroupCallComingSetActivity.this.commit("restoreAfterOtherGrp", b);
				GroupCallComingSetActivity.this.updateSunmary();
			}
		});
	}
}
