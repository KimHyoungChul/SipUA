package com.zed3.addressbook;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.PinnedHeaderExpandableListView;
import com.zed3.sipua.ui.lowsdk.PinnedHeaderExpandableListView.HeaderAdapter;
import com.zed3.sipua.ui.lowsdk.UserDetails;
import com.zed3.sipua.welcome.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class AddressAdapter extends BaseExpandableListAdapter implements HeaderAdapter {
	Context context;
	private float endX;
	private SparseIntArray groupStatusMap;
	PinnedHeaderExpandableListView listView;
	List<Map<String, String>> mData;
	List<Map<String, String>> mGVS;
	List<Map<String, String>> mSVP;
	List<Map<String, String>> mTeam;
	private int position;
	private float startX;
	private View view;
	private boolean visibleBtn;
	private boolean visibleBtn1;
	private boolean visibleBtn2;

	public AddressAdapter(final Context context) {
		this.visibleBtn = false;
		this.visibleBtn1 = false;
		this.visibleBtn2 = false;
		this.groupStatusMap = new SparseIntArray();
		this.context = context;
	}

	private Object getChangeChild(final String s, final int n) {
		if (s.equals("all")) {
			return this.mTeam.get(n);
		}
		if (s.equals("console")) {
			return this.mSVP.get(n);
		}
		if (s.equals("gvs")) {
			return this.mGVS.get(n);
		}
		return null;
	}

	private void getChangeChildView(final String s, final ViewHolder viewHolder, final int n) {
		if (s.equals("all")) {
			this.itemOne(viewHolder, n);
		} else {
			if (s.equals("console")) {
				this.itemTwo(viewHolder, n);
				return;
			}
			if (s.equals("gvs")) {
				this.itemThree(viewHolder, n);
			}
		}
	}

	private int getChangeCildrenCount(final String s) {
		if (s.equals("all")) {
			if (this.mTeam != null) {
				return this.mTeam.size();
			}
		} else if (s.equals("console")) {
			if (this.mSVP != null) {
				return this.mSVP.size();
			}
		} else if (s.equals("gvs") && this.mGVS != null) {
			return this.mGVS.size();
		}
		return 0;
	}

	private int getServerListArray() {
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList;
		}
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList1;
		}
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && !DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList2;
		}
		return -1;
	}

	private boolean isVisibleContactVideo(final String s, final String s2) {
		return (s != null && Member.UserType.toUserType(s) == Member.UserType.MOBILE_GQT && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1 || DeviceInfo.CONFIG_VIDEO_MONITOR == 1) && s2 != null && s2.equalsIgnoreCase("1")) || (s != null && Member.UserType.toUserType(s) == Member.UserType.SVP && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) && s2 != null && s2.equalsIgnoreCase("1"));
	}

	private void itemOne(final ViewHolder viewHolder, final int n) {
		final String text = this.mTeam.get(n).get("name");
		final String s = this.mTeam.get(n).get("count");
		final String s2 = this.mTeam.get(n).get("tid");
		viewHolder.members_layout.setVisibility(View.GONE);
		viewHolder.team_layout.setVisibility(View.VISIBLE);
		viewHolder.grp_name.setText((CharSequence) text);
		viewHolder.grp_count.setText((CharSequence) (String.valueOf(s) + this.context.getResources().getString(R.string.person)));
		viewHolder.team_layout.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final Intent intent = new Intent(AddressAdapter.this.context, (Class) DepartmentActivity.class);
				intent.putExtra("tid", s2);
				intent.putExtra("mname", text);
				AddressAdapter.this.context.startActivity(intent);
			}
		});
	}

	private void itemThree(final ViewHolder viewHolder, final int n) {
		final String s = this.mGVS.get(n).get("number");
		System.out.println("-----number=" + s);
		final String s2 = this.mGVS.get(n).get("mname");
		final String s3 = this.mGVS.get(n).get("mtype");
		final String s4 = this.mGVS.get(n).get("position");
		final String s5 = this.mGVS.get(n).get("sex");
		final String s6 = this.mGVS.get(n).get("phone");
		final String s7 = this.mGVS.get(n).get("dtype");
		final String s8 = this.mGVS.get(n).get("video");
		final String s9 = this.mGVS.get(n).get("audio");
		final String s10 = this.mGVS.get(n).get("pttmap");
		final String s11 = this.mGVS.get(n).get("gps");
		final String s12 = this.mGVS.get(n).get("pictureupload");
		final String s13 = this.mGVS.get(n).get("smsswitch");
		final String s14 = this.mGVS.get(n).get("tid");
		viewHolder.members_layout.setVisibility(View.VISIBLE);
		viewHolder.team_layout.setVisibility(View.GONE);
		viewHolder.contact_name.setText((CharSequence) this.mGVS.get(n).get("mname"));
		viewHolder.contact_num.setText((CharSequence) this.mGVS.get(n).get("number"));
		viewHolder.contact_image.setImageResource(R.drawable.icon_gvs);
		viewHolder.call_msg_btn2.setVisibility(View.GONE);
		viewHolder.call_voice_btn.setVisibility(View.GONE);
		viewHolder.line1.setVisibility(View.GONE);
		viewHolder.line2.setVisibility(View.GONE);
		viewHolder.call_voice_btn.setVisibility(View.GONE);
		if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
			viewHolder.contact_video.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contact_video.setVisibility(View.GONE);
		}
		viewHolder.contact_video.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CallUtil.makeVideoCall(AddressAdapter.this.context, s, null, "videobut");
			}
		});
		viewHolder.contact_image.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final Intent intent = new Intent(AddressAdapter.this.context, (Class) UserDetails.class);
				intent.putExtra("user_nam", s2);
				intent.putExtra("user_num", s);
				intent.putExtra("mtype", s3);
				intent.putExtra("tid", s14);
				intent.putExtra("aorv", String.valueOf(s9) + "," + s8);
				AddressAdapter.this.context.startActivity(intent);
			}
		});
		if (s.equals(MemoryMg.getInstance().TerminalNum)) {
			viewHolder.call_msg_btn2.setVisibility(View.GONE);
			viewHolder.call_voice_btn.setVisibility(View.GONE);
			viewHolder.line1.setVisibility(View.GONE);
			viewHolder.line2.setVisibility(View.GONE);
			viewHolder.contact_video.setVisibility(View.GONE);
			viewHolder.call_voice_btn.setVisibility(View.GONE);
			viewHolder.contact_name.setTextColor(this.context.getResources().getColor(R.color.onLine));
			viewHolder.contact_num.setTextColor(this.context.getResources().getColor(R.color.onLine));
			return;
		}
		viewHolder.contact_name.setTextColor(this.context.getResources().getColor(R.color.black));
		viewHolder.contact_num.setTextColor(this.context.getResources().getColor(R.color.black));
	}

	private void itemTwo(final ViewHolder viewHolder, final int n) {
		final String s = this.mSVP.get(n).get("number");
		final String s2 = this.mSVP.get(n).get("mname");
		final String s3 = this.mSVP.get(n).get("mtype");
		final String s4 = this.mSVP.get(n).get("position");
		final String s5 = this.mSVP.get(n).get("sex");
		final String s6 = this.mSVP.get(n).get("phone");
		final String s7 = this.mSVP.get(n).get("dtype");
		final String s8 = this.mSVP.get(n).get("video");
		final String s9 = this.mSVP.get(n).get("audio");
		final String s10 = this.mSVP.get(n).get("pttmap");
		final String s11 = this.mSVP.get(n).get("gps");
		final String s12 = this.mSVP.get(n).get("pictureupload");
		final String s13 = this.mSVP.get(n).get("smsswitch");
		final String s14 = this.mSVP.get(n).get("tid");
		viewHolder.members_layout.setVisibility(View.VISIBLE);
		viewHolder.team_layout.setVisibility(View.GONE);
		viewHolder.contact_name.setText((CharSequence) this.mSVP.get(n).get("mname"));
		viewHolder.contact_num.setText((CharSequence) this.mSVP.get(n).get("number"));
		if (this.getServerListArray() == -1) {
			viewHolder.call_msg_btn2.setVisibility(View.GONE);
			viewHolder.line2.setVisibility(View.GONE);
		} else {
			viewHolder.call_msg_btn2.setVisibility(View.VISIBLE);
			viewHolder.line2.setVisibility(View.VISIBLE);
		}
		if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE || (!TextUtils.isEmpty((CharSequence) s9) && s9.equalsIgnoreCase("0"))) {
			viewHolder.call_voice_btn.setVisibility(View.GONE);
			viewHolder.line1.setVisibility(View.GONE);
		} else {
			viewHolder.call_voice_btn.setVisibility(View.VISIBLE);
			viewHolder.line1.setVisibility(View.VISIBLE);
		}
		if (!this.isVisibleContactVideo(s3, s8)) {
			viewHolder.contact_video.setVisibility(View.GONE);
			viewHolder.line1.setVisibility(View.GONE);
		} else {
			viewHolder.contact_video.setVisibility(View.VISIBLE);
			viewHolder.line1.setVisibility(View.VISIBLE);
		}
		viewHolder.contact_image.setImageResource(R.drawable.icon_dispatcher);
		viewHolder.call_msg_btn2.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (((s3 != null && Member.UserType.toUserType(s3) == Member.UserType.SVP) || (s13 != null && s12 != null && s12.equalsIgnoreCase("1") && s13.equalsIgnoreCase("1"))) && AddressAdapter.this.getServerListArray() != -1) {
					new AlertDialog.Builder(AddressAdapter.this.context).setItems(AddressAdapter.this.getServerListArray(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						Intent intent = new Intent();

						public void onClick(final DialogInterface dialogInterface, final int n) {
							switch (n) {
								default: {
								}
								case 0: {
									if (AddressAdapter.this.getServerListArray() != R.array.msgDialogList2) {
										this.intent.setClass(AddressAdapter.this.context, (Class) MessageDialogueActivity.class);
										this.intent.putExtra("userName", s2);
										this.intent.putExtra("address", s);
										this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										AddressAdapter.this.context.startActivity(this.intent);
										return;
									}
									this.intent.setClass(AddressAdapter.this.context, (Class) MainActivity.class);
									this.intent.putExtra("action", "fastMMS");
									this.intent.putExtra("userName", s2);
									this.intent.putExtra("address", s);
									AddressAdapter.this.context.startActivity(this.intent);
								}
								case 1: {
									this.intent.setClass(AddressAdapter.this.context, (Class) MainActivity.class);
									this.intent.putExtra("action", "fastMMS");
									this.intent.putExtra("userName", s2);
									this.intent.putExtra("address", s);
									AddressAdapter.this.context.startActivity(this.intent);
								}
							}
						}
					}).show();
				}
			}
		});
		// TODO
		viewHolder.contact_video.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CallUtil.makeVideoCall(AddressAdapter.this.context, s, null, "videobut");
			}
		});
		viewHolder.contact_image.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final Intent intent = new Intent(AddressAdapter.this.context, (Class) UserDetails.class);
				intent.putExtra("user_nam", s2);
				intent.putExtra("user_num", s);
				intent.putExtra("mtype", s3);
				intent.putExtra("tid", s14);
				intent.putExtra("aorv", String.valueOf(s9) + "," + s8);
				AddressAdapter.this.context.startActivity(intent);
			}
		});
		if (s.equals(MemoryMg.getInstance().TerminalNum)) {
			viewHolder.call_msg_btn2.setVisibility(View.GONE);
			viewHolder.call_voice_btn.setVisibility(View.GONE);
			viewHolder.line1.setVisibility(View.GONE);
			viewHolder.line2.setVisibility(View.GONE);
			viewHolder.contact_video.setVisibility(View.GONE);
			viewHolder.call_voice_btn.setVisibility(View.GONE);
			viewHolder.contact_name.setTextColor(this.context.getResources().getColor(R.color.onLine));
			viewHolder.contact_num.setTextColor(this.context.getResources().getColor(R.color.onLine));
			return;
		}
		viewHolder.contact_name.setTextColor(this.context.getResources().getColor(R.color.black));
		viewHolder.contact_num.setTextColor(this.context.getResources().getColor(R.color.black));
	}

	private void putName(String flag, String newName, Editor editor) {
		if (flag.equals("all")) {
			editor.putString("allName", newName);
		} else if (flag.equals("console")) {
			editor.putString("consoleName", newName);
		} else if (flag.equals("gvs")) {
			editor.putString("gvsName", newName);
		}
	}

	private void putSort(final int n, final String s) {
		final Map<String, String> map = this.mData.get(n);
		final String s2 = map.get("flag");
		map.put("sort", s);
		final SharedPreferences.Editor edit = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		if (s2.equals("all")) {
			edit.putString("allSort", s);
		} else if (s2.equals("console")) {
			edit.putString("consoleSort", s);
		} else if (s2.equals("gvs")) {
			edit.putString("gvsSort", s);
		}
		edit.commit();
	}

	public void configureHeader(final View view, final int n, final int n2, final int n3) {
		final String text = this.mData.get(n).get("name");
		final String s = this.mData.get(n).get("count");
		((TextView) view.findViewById(R.id.grp_name)).setText((CharSequence) text);
		((TextView) view.findViewById(R.id.grp_count)).setText((CharSequence) (String.valueOf(s) + this.context.getResources().getString(R.string.person)));
		final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.team_layout);
		view.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.member_layout).setVisibility(View.GONE);
		final ImageView imageView = (ImageView) view.findViewById(R.id.imageicon);
		relativeLayout.setBackgroundResource(R.color.whole_bg);
		imageView.setVisibility(View.VISIBLE);
		imageView.setImageResource(R.drawable.iconfont_zhankai);
	}

	public Object getChild(final int n, final int n2) {
		final String s = this.mData.get(n).get("flag");
		Object changeChild = null;
		if (n == 0) {
			changeChild = this.getChangeChild(s, n2);
		} else {
			if (n == 1) {
				return this.getChangeChild(s, n2);
			}
			if (n == 2) {
				return this.getChangeChild(s, n2);
			}
		}
		return changeChild;
	}

	public long getChildId(final int n, final int n2) {
		return n2;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(this.context).inflate(R.layout.contact_grp_name_list, null);
			viewHolder.grp_name = (TextView) convertView.findViewById(R.id.grp_name);
			viewHolder.grp_count = (TextView) convertView.findViewById(R.id.grp_count);
			viewHolder.members_layout = (LinearLayout) convertView.findViewById(R.id.member_layout);
			viewHolder.team_layout = (RelativeLayout) convertView.findViewById(R.id.team_layout);
			viewHolder.contact_name = (TextView) convertView.findViewById(R.id.contact_name);
			viewHolder.contact_num = (TextView) convertView.findViewById(R.id.contact_num);
			viewHolder.contact_image = (ImageView) convertView.findViewById(R.id.contact_image);
			viewHolder.call_msg_btn2 = (ImageView) convertView.findViewById(R.id.call_msg_btn2);
			viewHolder.call_voice_btn = (ImageView) convertView.findViewById(R.id.call_voice_btn);
			viewHolder.contact_video = (ImageView) convertView.findViewById(R.id.contact_video);
			viewHolder.line1 = (LinearLayout) convertView.findViewById(R.id.line_sub);
			viewHolder.line2 = (LinearLayout) convertView.findViewById(R.id.line_sub2);
			convertView.setTag(viewHolder);
			convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
			convertView.setTag(R.id.grp_count, Integer.valueOf(childPosition));
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String flag = (String) ((Map) this.mData.get(groupPosition)).get("flag");
		if (groupPosition == 0) {
			getChangeChildView(flag, viewHolder, childPosition);
		} else if (groupPosition == 1) {
			getChangeChildView(flag, viewHolder, childPosition);
		} else if (groupPosition == 2) {
			getChangeChildView(flag, viewHolder, childPosition);
		}
		return convertView;
	}

	public int getChildrenCount(final int n) {
		final String s = this.mData.get(n).get("flag");
		int changeCildrenCount = 0;
		if (n == 0) {
			changeCildrenCount = this.getChangeCildrenCount(s);
		} else {
			if (n == 1) {
				return this.getChangeCildrenCount(s);
			}
			if (n == 2) {
				return this.getChangeCildrenCount(s);
			}
		}
		return changeCildrenCount;
	}

	public void getData(final List<Map<String, String>> mData, final List<Map<String, String>> mTeam, final List<Map<String, String>> msvp, final List<Map<String, String>> mgvs, final PinnedHeaderExpandableListView listView) {
		this.mData = mData;
		this.mTeam = mTeam;
		this.mSVP = msvp;
		this.mGVS = mgvs;
		this.listView = listView;
	}

	public Object getGroup(final int n) {
		return this.mData.get(n);
	}

	public int getGroupClickStatus(final int n) {
		if (this.groupStatusMap.keyAt(n) >= 0) {
			return this.groupStatusMap.get(n);
		}
		return 0;
	}

	public int getGroupCount() {
		return this.mData.size();
	}

	public long getGroupId(final int n) {
		return n;
	}

	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(this.context).inflate(R.layout.contact_grp_name_list, null);
			viewHolder.grp_name = (TextView) convertView.findViewById(R.id.grp_name);
			viewHolder.grp_count = (TextView) convertView.findViewById(R.id.grp_count);
			viewHolder.btnRename = (Button) convertView.findViewById(R.id.rename);
			viewHolder.btnUp = (Button) convertView.findViewById(R.id.up);
			viewHolder.btnDown = (Button) convertView.findViewById(R.id.down);
			viewHolder.showView = convertView.findViewById(R.id.view);
			viewHolder.slip_layout = (LinearLayout) convertView.findViewById(R.id.slip_layout);
			viewHolder.team_layout = (RelativeLayout) convertView.findViewById(R.id.team_layout);
			convertView.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
			convertView.findViewById(R.id.member_layout).setVisibility(View.GONE);
			viewHolder.grp_icon = (ImageView) convertView.findViewById(R.id.imageicon);
			viewHolder.team_layout.setBackgroundResource(R.color.whole_bg);
			convertView.setTag(viewHolder);
			convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
			convertView.setTag(R.id.grp_count, Integer.valueOf(-1));
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (groupPosition == 0) {
			putSort(groupPosition, "0");
			if (this.visibleBtn) {
				viewHolder.slip_layout.setVisibility(View.VISIBLE);
				viewHolder.btnUp.setVisibility(View.GONE);
				viewHolder.grp_count.setVisibility(View.GONE);
			} else {
				viewHolder.slip_layout.setVisibility(View.GONE);
				viewHolder.grp_count.setVisibility(View.VISIBLE);
			}
		} else if (groupPosition == 1) {
			putSort(groupPosition, "1");
			if (this.visibleBtn1) {
				viewHolder.slip_layout.setVisibility(View.VISIBLE);
				viewHolder.grp_count.setVisibility(View.GONE);
			} else {
				viewHolder.slip_layout.setVisibility(View.GONE);
				viewHolder.grp_count.setVisibility(View.VISIBLE);
			}
		} else if (groupPosition == 2) {
			putSort(groupPosition, SettingVideoSize.R720P);
			if (this.visibleBtn2) {
				viewHolder.slip_layout.setVisibility(View.VISIBLE);
				viewHolder.btnDown.setVisibility(View.GONE);
				viewHolder.grp_count.setVisibility(View.GONE);
			} else {
				viewHolder.slip_layout.setVisibility(View.GONE);
				viewHolder.grp_count.setVisibility(View.VISIBLE);
			}
		}
		convertView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				ViewHolder holder = (ViewHolder) arg0.getTag();
				boolean mboolean = false;
				switch (arg1.getAction()) {
					case 0:
						AddressAdapter.this.startX = arg1.getX();
						if (groupPosition == 0) {
							if (!AddressAdapter.this.visibleBtn) {
								holder.slip_layout.setVisibility(View.GONE);
								holder.grp_count.setVisibility(View.VISIBLE);
							}
						} else if (groupPosition == 1) {
							if (!AddressAdapter.this.visibleBtn1) {
								holder.slip_layout.setVisibility(View.GONE);
								holder.grp_count.setVisibility(View.VISIBLE);
							}
						} else if (groupPosition == 2 && !AddressAdapter.this.visibleBtn2) {
							holder.slip_layout.setVisibility(View.GONE);
							holder.grp_count.setVisibility(View.VISIBLE);
						}
						for (int i = 0; i < AddressAdapter.this.getGroupCount(); i++) {
							if (AddressAdapter.this.listView.isGroupExpanded(i)) {
								mboolean = true;
								if (isExpanded || mboolean) {
									return false;
								}
								return true;
							}
						}
						if (!isExpanded) {
						}
						return false;
					case 1:
						AddressAdapter.this.endX = arg1.getX();
						if (AddressAdapter.this.endX - AddressAdapter.this.startX < -15.0f) {
							if (groupPosition == 0) {
								AddressAdapter.this.visibleBtn = true;
								if (AddressAdapter.this.visibleBtn) {
									holder.slip_layout.setVisibility(View.VISIBLE);
									holder.btnUp.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.GONE);
									holder.showView.setVisibility(View.GONE);
									AddressAdapter.this.visibleBtn1 = false;
									AddressAdapter.this.visibleBtn2 = false;
								}
							} else if (groupPosition == 1) {
								AddressAdapter.this.visibleBtn1 = true;
								if (AddressAdapter.this.visibleBtn1) {
									holder.slip_layout.setVisibility(View.VISIBLE);
									holder.grp_count.setVisibility(View.GONE);
									holder.showView.setVisibility(View.VISIBLE);
									AddressAdapter.this.visibleBtn = false;
									AddressAdapter.this.visibleBtn2 = false;
								}
							} else if (groupPosition == 2) {
								AddressAdapter.this.visibleBtn2 = true;
								if (AddressAdapter.this.visibleBtn2) {
									holder.slip_layout.setVisibility(View.VISIBLE);
									holder.btnDown.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.GONE);
									holder.showView.setVisibility(View.VISIBLE);
									AddressAdapter.this.visibleBtn1 = false;
									AddressAdapter.this.visibleBtn = false;
								}
							}
							AddressAdapter.this.notifyDataSetChanged();
							return true;
						} else if (AddressAdapter.this.endX - AddressAdapter.this.startX > 15.0f) {
							if (groupPosition == 0) {
								AddressAdapter.this.visibleBtn = false;
								if (!AddressAdapter.this.visibleBtn) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
							} else if (groupPosition == 1) {
								AddressAdapter.this.visibleBtn1 = false;
								if (!AddressAdapter.this.visibleBtn1) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
							} else if (groupPosition == 2) {
								AddressAdapter.this.visibleBtn2 = false;
								if (!AddressAdapter.this.visibleBtn2) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
							}
							return true;
						} else if (Math.abs(AddressAdapter.this.endX - AddressAdapter.this.startX) >= 5.0f) {
							return false;
						} else {
							if (!AddressAdapter.this.visibleBtn && !AddressAdapter.this.visibleBtn1 && !AddressAdapter.this.visibleBtn2) {
								AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
							} else if (groupPosition == 0) {
								AddressAdapter.this.visibleBtn = false;
								if (!AddressAdapter.this.visibleBtn) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
								if (AddressAdapter.this.visibleBtn1) {
									AddressAdapter.this.visibleBtn1 = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
								if (AddressAdapter.this.visibleBtn2) {
									AddressAdapter.this.visibleBtn2 = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
							} else if (groupPosition == 1) {
								AddressAdapter.this.visibleBtn1 = false;
								if (!AddressAdapter.this.visibleBtn1) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
								if (AddressAdapter.this.visibleBtn) {
									AddressAdapter.this.visibleBtn = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
								if (AddressAdapter.this.visibleBtn2) {
									AddressAdapter.this.visibleBtn2 = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
							} else if (groupPosition == 2) {
								AddressAdapter.this.visibleBtn2 = false;
								if (!AddressAdapter.this.visibleBtn2) {
									holder.slip_layout.setVisibility(View.GONE);
									holder.grp_count.setVisibility(View.VISIBLE);
								}
								if (AddressAdapter.this.visibleBtn1) {
									AddressAdapter.this.visibleBtn1 = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
								if (AddressAdapter.this.visibleBtn) {
									AddressAdapter.this.visibleBtn = false;
									AddressAdapter.this.notifyDataSetChanged();
									AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
								}
							}
							return false;
						}
					case 2:
						return true;
					default:
						return false;
				}
			}
		});
		viewHolder.btnRename.setOnClickListener(new OnClickListener() {
			private String tempName;

			class C08553 implements DialogInterface.OnClickListener {
				C08553() {
				}

				public void onClick(DialogInterface arg0, int arg1) {
					AddressAdapter.this.visibleBtn = false;
					AddressAdapter.this.visibleBtn1 = false;
					AddressAdapter.this.visibleBtn2 = false;
					AddressAdapter.this.notifyDataSetChanged();
				}
			}

			public void onClick(View arg0) {
				ViewHolder holder = (ViewHolder) arg0.getTag();
				String currentName = null;
				final List<String> namesArray = new ArrayList();
				View textEntryView = LayoutInflater.from(AddressAdapter.this.context).inflate(R.layout.contact_rename, null);
				final EditText et_name = (EditText) textEntryView.findViewById(R.id.contact_user_name);
				new Timer().schedule(new TimerTask() {
					public void run() {
						((InputMethodManager) et_name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, 2);
					}
				}, 300);
				for (int i = 0; i < AddressAdapter.this.mData.size(); i++) {
					Map<String, String> data = (Map) AddressAdapter.this.mData.get(i);
					if (i == groupPosition) {
						currentName = (String) data.get("name");
						this.tempName = (String) currentName;
					} else {
						namesArray.add((String) data.get("name"));
					}
				}
				et_name.setText(currentName);
				et_name.setSelection(currentName.length());
				et_name.setFilters(new InputFilter[]{new LengthFilter(20)});
				Builder view = new Builder(AddressAdapter.this.context).setView(textEntryView);
				CharSequence string = AddressAdapter.this.context.getResources().getString(R.string.save);
				final int i2 = groupPosition;
				view.setPositiveButton(string, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						String newName;
						if (et_name.getText().toString().equals("")) {
							newName = tempName;
							Toast.makeText(AddressAdapter.this.context, AddressAdapter.this.context.getResources().getString(R.string.toast_two), Toast.LENGTH_SHORT).show();
						} else {
							newName = et_name.getText().toString();
							if (namesArray.contains(newName)) {
								Toast.makeText(AddressAdapter.this.context, AddressAdapter.this.context.getResources().getString(R.string.toast_one), Toast.LENGTH_SHORT).show();
								newName = tempName;
							}
						}
						Editor editor = AddressAdapter.this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
						Map<String, String> data = (Map) AddressAdapter.this.mData.get(i2);
						String flag = (String) data.get("flag");
						if (i2 == 0) {
							AddressAdapter.this.putName(flag, newName, editor);
						} else if (i2 == 1) {
							AddressAdapter.this.putName(flag, newName, editor);
						} else if (i2 == 2) {
							AddressAdapter.this.putName(flag, newName, editor);
						}
						((Map) AddressAdapter.this.mData.get(i2)).put("name", newName);
						AddressAdapter.this.mData.set(i2, data);
						editor.commit();
						AddressAdapter.this.visibleBtn = false;
						AddressAdapter.this.visibleBtn1 = false;
						AddressAdapter.this.visibleBtn2 = false;
						AddressAdapter.this.notifyDataSetChanged();
					}
				}).setNegativeButton(AddressAdapter.this.context.getResources().getString(R.string.cancel), new C08553()).show();
			}
		});
		viewHolder.btnUp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Map<String, String> data1 = (Map) AddressAdapter.this.mData.get(groupPosition - 1);
				AddressAdapter.this.mData.set(groupPosition - 1, (Map) AddressAdapter.this.mData.get(groupPosition));
				AddressAdapter.this.mData.set(groupPosition, data1);
				if (groupPosition == 1) {
					AddressAdapter.this.visibleBtn = true;
					AddressAdapter.this.visibleBtn1 = false;
					if (isExpanded) {
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition - 1), groupPosition - 1, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition - 1));
					}
				} else if (groupPosition == 2) {
					AddressAdapter.this.visibleBtn1 = true;
					AddressAdapter.this.visibleBtn2 = false;
					if (isExpanded) {
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition - 1), groupPosition - 1, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition - 1));
					}
				}
				AddressAdapter.this.notifyDataSetChanged();
			}
		});
		viewHolder.btnDown.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Map<String, String> data = (Map) AddressAdapter.this.mData.get(groupPosition);
				AddressAdapter.this.mData.set(groupPosition, (Map) AddressAdapter.this.mData.get(groupPosition + 1));
				AddressAdapter.this.mData.set(groupPosition + 1, data);
				if (groupPosition == 0) {
					AddressAdapter.this.visibleBtn1 = true;
					AddressAdapter.this.visibleBtn = false;
					if (isExpanded) {
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition + 1), groupPosition + 1, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition + 1));
					}
				} else if (groupPosition == 1) {
					AddressAdapter.this.visibleBtn2 = true;
					AddressAdapter.this.visibleBtn1 = false;
					if (isExpanded) {
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition), groupPosition, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition));
						AddressAdapter.this.listView.performItemClick(AddressAdapter.this.listView.getChildAt(groupPosition + 1), groupPosition + 1, AddressAdapter.this.listView.getItemIdAtPosition(groupPosition + 1));
					}
				}
				AddressAdapter.this.notifyDataSetChanged();
			}
		});
		viewHolder.grp_icon.setVisibility(View.VISIBLE);
		viewHolder.grp_icon.setImageResource(R.drawable.iconfont_zhankai);
		if (!isExpanded) {
			viewHolder.grp_icon.setImageResource(R.drawable.iconfont_shousuozhankai);
		}
		viewHolder.grp_name.setText((CharSequence) ((Map) this.mData.get(groupPosition)).get("name"));
		viewHolder.grp_count.setText(new StringBuilder(String.valueOf((String) ((Map) this.mData.get(groupPosition)).get("count"))).append(this.context.getResources().getString(R.string.person)).toString());
		convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
		convertView.setTag(R.id.grp_count, Integer.valueOf(-1));
		return convertView;
	}

	public int getHeaderState(final int n, final int n2) {
		if (n2 == this.getChildrenCount(n) - 1) {
			return 2;
		}
		if (n2 == -1 && !this.listView.isGroupExpanded(n)) {
			return 0;
		}
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(final int n, final int n2) {
		return true;
	}

	public void setGroupClickStatus(final int n, final int n2) {
		this.groupStatusMap.put(n, n2);
	}

	private static class ViewHolder {
		private Button btnDown;
		private Button btnRename;
		private Button btnUp;
		private ImageView call_msg_btn2;
		private ImageView call_voice_btn;
		private ImageView contact_image;
		private TextView contact_name;
		private TextView contact_num;
		private ImageView contact_video;
		private TextView grp_count;
		private ImageView grp_icon;
		private TextView grp_name;
		private LinearLayout line1;
		private LinearLayout line2;
		private LinearLayout members_layout;
		private View showView;
		private LinearLayout slip_layout;
		private RelativeLayout team_layout;

		private ViewHolder() {
		}
	}
}
