package com.zed3.sipua.ui.lowsdk;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

import java.util.ArrayList;
import java.util.List;

public class UserDetails extends BaseActivity {
	private LinearLayout btn_left;
	private DataBaseService db;
	private TextView departmentView;
	private TextView levelView;
	private String mAudioOrVideo;
	private String mName;
	private String mNumber;
	private String mTeam;
	private String mType;
	private TextView media_attributeView;
	private TextView nameView;
	private TextView numberView;
	private List<String> teams;
	private TextView terminalView;
	private TextView transcribe_attributeView;

	public UserDetails() {
		this.mTeam = "";
		this.teams = new ArrayList<String>();
	}

	private void InitTitle() {
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.back);
		((TextView) this.findViewById(R.id.title)).setText(R.string.user_mms);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				UserDetails.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) UserDetails.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) UserDetails.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						UserDetails.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(UserDetails.this.getResources().getColor(R.color.font_color3));
						UserDetails.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	private void InitView() {
		this.nameView = (TextView) this.findViewById(R.id.name);
		this.numberView = (TextView) this.findViewById(R.id.account);
		this.terminalView = (TextView) this.findViewById(R.id.terminal_type);
		this.levelView = (TextView) this.findViewById(R.id.level);
		this.departmentView = (TextView) this.findViewById(R.id.department);
		this.media_attributeView = (TextView) this.findViewById(R.id.media_attribute);
		this.transcribe_attributeView = (TextView) this.findViewById(R.id.transcribe_attribute);
	}

	private String getAttribute(String string) {
		final String s = "";
		final int intValue = Integer.valueOf(string.split(",")[0]);
		final int intValue2 = Integer.valueOf(string.split(",")[1]);
		if (intValue == 1 && intValue2 == 1) {
			string = String.valueOf(this.getResources().getString(R.string.rate_suspension_voice)) + "+" + this.getResources().getString(R.string.rate_suspension_video);
		} else {
			if (intValue == 1 && intValue2 == 0) {
				return this.getResources().getString(R.string.rate_suspension_voice);
			}
			string = s;
			if (intValue == 0) {
				string = s;
				if (intValue2 == 1) {
					return this.getResources().getString(R.string.rate_suspension_video);
				}
			}
		}
		return string;
	}

	private void getTeamString(final List<String> list) {
		if (list.size() >= 0) {
			for (int i = list.size() - 1; i > -1; --i) {
				if (i > 0) {
					this.mTeam = String.valueOf(this.mTeam) + this.db.getNameByTid(list.get(i)) + "->";
				} else {
					this.mTeam = String.valueOf(this.mTeam) + this.db.getNameByTid(list.get(i));
				}
			}
		}
	}

	private void getTeams(final String s) {
		final String pidByTid = this.db.getPidByTid(s);
		this.teams.add(s);
		if (pidByTid != null && !pidByTid.equals("-2")) {
			this.getTeams(pidByTid);
		}
	}

	private String getTypeString(final String s) {
		String string = "";
		if (Member.UserType.toUserType(s) == Member.UserType.SVP) {
			string = SipUAApp.mContext.getResources().getString(R.string.duzd);
		} else {
			if (Member.UserType.toUserType(s) == Member.UserType.MOBILE_GQT) {
				return "GQT";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.VIDEO_MONITOR_GVS) {
				return "GVS";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.VIDEO_MONITOR_GB28181) {
				return "GB28181";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.GTS) {
				return "GTS";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.PDT) {
				return "PDT";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.DMR) {
				return "DMR";
			}
			if (Member.UserType.toUserType(s) == Member.UserType.EMERGENCY_GRP) {
				return SipUAApp.mContext.getResources().getString(R.string.jjhm);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.TRIGGER_NUM) {
				return SipUAApp.mContext.getResources().getString(R.string.cfhm);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.MEET_NUM) {
				return SipUAApp.mContext.getResources().getString(R.string.gghys);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.AUDIO_MAIL) {
				return SipUAApp.mContext.getResources().getString(R.string.yyxx);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.RING_GRP) {
				return SipUAApp.mContext.getResources().getString(R.string.zlz);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.PRIORITY_RING_GRP) {
				return SipUAApp.mContext.getResources().getString(R.string.yxlxz);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.BALANCE_GRP) {
				return SipUAApp.mContext.getResources().getString(R.string.phlxz);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.EXTERNAL_UC) {
				return SipUAApp.mContext.getResources().getString(R.string.ucyh);
			}
			if (Member.UserType.toUserType(s) == Member.UserType.EXTERNAL_OTHER) {
				return SipUAApp.mContext.getResources().getString(R.string.qtwbyh);
			}
		}
		return string;
	}

	private void setUserDetails(final String s) {
		this.nameView.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.name)) + " " + this.mName));
		this.numberView.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.user_number)) + " " + this.mNumber));
		this.terminalView.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.terminal)) + " " + this.mType));
		this.departmentView.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.department)) + " " + this.mTeam));
		this.media_attributeView.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.media_attributeView)) + " " + this.mAudioOrVideo));
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.user_details);
		final Intent intent = this.getIntent();
		this.mName = intent.getStringExtra("user_nam");
		this.mNumber = intent.getStringExtra("user_num");
		this.mType = intent.getStringExtra("mtype");
		String s = intent.getStringExtra("tid");
		this.mAudioOrVideo = intent.getStringExtra("aorv");
		this.db = DataBaseService.getInstance();
		String s2 = null;
		Label_0164:
		{
			if (this.mType != null && !this.mType.equals("") && s != null && !s.equals("") && this.mAudioOrVideo != null) {
				s2 = s;
				if (!this.mAudioOrVideo.equals("")) {
					break Label_0164;
				}
			}
			final Cursor members = this.db.getMembers(this.mNumber);
			s2 = s;
			if (members != null) {
				while (members.moveToNext()) {
					s = members.getString(members.getColumnIndex("tid"));
					this.mType = members.getString(members.getColumnIndex("mtype"));
					this.mAudioOrVideo = String.valueOf(members.getString(members.getColumnIndex("audio"))) + "," + members.getString(members.getColumnIndex("video"));
				}
				members.close();
				s2 = s;
			}
		}
		this.mType = this.getTypeString(this.mType);
		this.getTeams(s2);
		this.getTeamString(this.teams);
		this.mAudioOrVideo = this.getAttribute(this.mAudioOrVideo);
		this.InitTitle();
		this.InitView();
	}

	protected void onDestroy() {
		super.onDestroy();
		if (this.db != null) {
			this.db = null;
		}
	}

	protected void onResume() {
		super.onResume();
		this.setUserDetails(this.mNumber);
	}
}
