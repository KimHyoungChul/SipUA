package com.zed3.addressbook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.UserDetails;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressLint({"ResourceAsColor"})
public class DepartmentAdapter extends BaseAdapter {
	private int count;
	DataBaseService dbService;
	Context mContext;
	List<Map<String, String>> mData;
	private LayoutInflater mInflater;
	String mName;
	private ArrayList<String> tempGrpList;

	public DepartmentAdapter(final Context mContext, final String mName, final DataBaseService dbService) {
		this.tempGrpList = new ArrayList<String>();
		this.count = 0;
		this.mContext = mContext;
		this.mName = mName;
		this.mInflater = LayoutInflater.from(mContext);
		this.dbService = dbService;
	}

	private int getCount(final String s) {
		final List<Map<String, String>> teamsByPid = this.dbService.getTeamsByPid(s, false);
		if (this.count == 0) {
			this.count += this.dbService.getMembersNumber("tid='" + s + "' and mtype!='" + Member.UserType.GRP_NUM.convert() + "'");
		}
		Log.i("jiangkai", String.valueOf(s) + "   " + teamsByPid);
		this.count += this.dbService.getMembersNumber("tid='" + s + "' and mtype!='" + Member.UserType.GRP_NUM.convert() + "'");
		if (teamsByPid.size() > 0) {
			for (final Map<String, String> map : teamsByPid) {
				this.count += this.dbService.getMembersNumber("tid='" + map.get("tid") + "' and mtype!='" + Member.UserType.GRP_NUM.convert() + "'");
				MyLog.e("lilong", String.valueOf(map.get("tid")) + "   count   " + this.count);
				if (Integer.valueOf(map.get("tid")) == Integer.valueOf(map.get("pid"))) {
					Log.e("jiangkai", Integer.valueOf(map.get("tid")) + "   " + Integer.valueOf(map.get("pid")));
					return 0;
				}
				this.getCount(map.get("tid"));
			}
		}
		return this.count;
	}

	private CharSequence getHighLightText(final String s, final String s2) {
		final int index = s.toLowerCase().indexOf(s2.toLowerCase());
		final int length = s2.length();
		return (CharSequence) Html.fromHtml(String.valueOf(s.substring(0, index)) + "<u><font color=#FF0000>" + s.substring(index, index + length) + "</font></u>" + s.substring(index + length, s.length()));
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

	private Spanned getStringText(String s, String s2, final String s3) {
		final Spanned spanned = null;
		final int index = s.toLowerCase().indexOf(s2.toLowerCase());
		final String substring = s.substring(index, s2.length() + index);
		System.out.println(index);
		Spanned fromHtml;
		if (index == 0) {
			if (substring.length() < 2) {
				s = s.substring(substring.length() + index, s.length());
			} else {
				s = s.substring(substring.length(), s.length());
			}
			if (!s3.equals("1")) {
				return Html.fromHtml("<font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
			}
			fromHtml = Html.fromHtml("<font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
		} else {
			fromHtml = spanned;
			if (index > 0) {
				if (substring.length() < 2 && substring.length() + index < s.length()) {
					s2 = s.substring(0, substring.length() + index - 1);
					s = s.substring(substring.length() + index, s.length());
					if (s3.equals("1")) {
						return Html.fromHtml("<font color=red>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
					}
					return Html.fromHtml("<font color=balck>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
				} else if (substring.length() + index < s.length()) {
					s2 = s.substring(0, index);
					s = s.substring(substring.length() + index, s.length());
					if (s3.equals("1")) {
						return Html.fromHtml("<font color=red>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
					}
					return Html.fromHtml("<font color=balck>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
				} else {
					s = s.substring(0, s.length() - substring.length());
					if (s3.equals("1")) {
						return Html.fromHtml("<font color=red>" + s + "</font><font color=red><u>" + substring + "</u></font>");
					}
					return Html.fromHtml("<font color=balck>" + s + "</font><font color=red><u>" + substring + "</u></font>");
				}
			}
		}
		return fromHtml;
	}

	private boolean isVisibleContactVideo(final String s, final String s2) {
		return (s != null && Member.UserType.toUserType(s) == Member.UserType.MOBILE_GQT && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1 || DeviceInfo.CONFIG_VIDEO_MONITOR == 1) && s2 != null && s2.equalsIgnoreCase("1")) || (s != null && Member.UserType.toUserType(s) == Member.UserType.SVP && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) && s2 != null && s2.equalsIgnoreCase("1"));
	}

	public int getCount() {
		return this.mData.size();
	}

	public void getData(final List<Map<String, String>> mData) {
		this.mData = mData;
	}

	public Object getItem(final int n) {
		return this.mData.get(n);
	}

	public long getItemId(final int n) {
		return n;
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public View getView(final int n, final View view, final ViewGroup viewGroup) {
		Log.d("contact_video", "contact_video " + n);
		DepartmentViewHolder tag;
		View inflate;
		if (view == null) {
			tag = new DepartmentViewHolder();
			inflate = this.mInflater.inflate(R.layout.contact_grp_name_list, (ViewGroup) null);
			tag.grp_name = (TextView) inflate.findViewById(R.id.grp_name);
			tag.grp_count = (TextView) inflate.findViewById(R.id.grp_count);
			tag.members_layout = (LinearLayout) inflate.findViewById(R.id.member_layout);
			tag.team_layout = (RelativeLayout) inflate.findViewById(R.id.team_layout);
			tag.contact_name = (TextView) inflate.findViewById(R.id.contact_name);
			tag.contact_num = (TextView) inflate.findViewById(R.id.contact_num);
			tag.contact_image = (ImageView) inflate.findViewById(R.id.contact_image);
			tag.call_msg_btn2 = (ImageView) inflate.findViewById(R.id.call_msg_btn2);
			tag.call_voice_btn = (ImageView) inflate.findViewById(R.id.call_voice_btn);
			tag.contact_video = (ImageView) inflate.findViewById(R.id.contact_video);
			tag.line1 = (LinearLayout) inflate.findViewById(R.id.line_sub);
			tag.line2 = (LinearLayout) inflate.findViewById(R.id.line_sub2);
			inflate.setTag((Object) tag);
		} else {
			tag = (DepartmentViewHolder) view.getTag();
			inflate = view;
		}
		final String text = this.mData.get(n).get("name");
		final String s = this.mData.get(n).get("tid");
		if (text != null) {
			tag.members_layout.setVisibility(View.GONE);
			tag.team_layout.setVisibility(View.VISIBLE);
			tag.grp_name.setText((CharSequence) text);
			this.count = 0;
			tag.grp_count.setText((CharSequence) (String.valueOf(this.getCount(s)) + this.mContext.getResources().getString(R.string.person)));
			tag.team_layout.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final Intent intent = new Intent(DepartmentAdapter.this.mContext, (Class) DepartmentActivity.class);
					intent.putExtra("tid", s);
					intent.putExtra("mname", text);
					DepartmentAdapter.this.mContext.startActivity(intent);
				}
			});
		} else {
			tag.members_layout.setVisibility(View.VISIBLE);
			tag.team_layout.setVisibility(View.GONE);
			String s2 = "0";
			if (this.mData.get(n).get("status") != null) {
				s2 = this.mData.get(n).get("status");
			}
			final String s3 = this.mData.get(n).get("number");
			System.out.println("-----number=" + s3);
			final String s4 = this.mData.get(n).get("mname");
			final String s5 = this.mData.get(n).get("mtype");
			final String s6 = this.mData.get(n).get("position");
			final String s7 = this.mData.get(n).get("sex");
			final String s8 = this.mData.get(n).get("phone");
			final String s9 = this.mData.get(n).get("dtype");
			final String s10 = this.mData.get(n).get("video");
			final String s11 = this.mData.get(n).get("audio");
			final String s12 = this.mData.get(n).get("pttmap");
			final String s13 = this.mData.get(n).get("gps");
			final String s14 = this.mData.get(n).get("pictureupload");
			final String s15 = this.mData.get(n).get("smsswitch");
			final String s16 = this.mData.get(n).get("tid");
			final String memberType = DataBaseService.getInstance().getMemberType(s3);
			System.out.println("-----type:" + memberType);
			final boolean customGroupCreator = CustomGroupUtil.getInstance().isCustomGroupCreator(this.mContext, s3);
			tag.contact_image.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					if (DepartmentAdapter.this.dbService.sameCopmany(s3)) {
						final Intent intent = new Intent(DepartmentAdapter.this.mContext, (Class) UserDetails.class);
						intent.putExtra("user_nam", s4);
						intent.putExtra("user_num", s3);
						intent.putExtra("mtype", s5);
						intent.putExtra("tid", s16);
						intent.putExtra("aorv", String.valueOf(s11) + "," + s10);
						DepartmentAdapter.this.mContext.startActivity(intent);
						return;
					}
					MyToast.showToast(true, DepartmentAdapter.this.mContext, R.string.member_service_not);
				}
			});
			if (customGroupCreator) {
				tag.contact_name.setTextColor(this.mContext.getResources().getColor(R.color.onLine));
				tag.contact_num.setTextColor(this.mContext.getResources().getColor(R.color.onLine));
				tag.contact_image.setImageResource(R.drawable.icon_contact);
				tag.call_msg_btn2.setVisibility(View.GONE);
				tag.call_voice_btn.setVisibility(View.GONE);
				tag.contact_video.setVisibility(View.GONE);
				tag.line1.setVisibility(View.GONE);
				tag.line2.setVisibility(View.GONE);
			} else {
				tag.contact_name.setTextColor(this.mContext.getResources().getColor(R.color.black));
				tag.contact_num.setTextColor(this.mContext.getResources().getColor(R.color.black));
				tag.call_msg_btn2.setImageResource(R.drawable.ptt_listitem_msgbtn);
				tag.call_voice_btn.setImageResource(R.drawable.ptt_listitem_voicebtn);
				tag.contact_video.setImageResource(R.drawable.ptt_videonormal);
				if (Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT) {
					if (this.getServerListArray() == -1) {
						tag.call_msg_btn2.setVisibility(View.GONE);
						tag.line2.setVisibility(View.GONE);
					} else if (!TextUtils.isEmpty((CharSequence) s15) && !TextUtils.isEmpty((CharSequence) s14)) {
						if (s15.equalsIgnoreCase("0") && s14.equalsIgnoreCase("0")) {
							tag.call_msg_btn2.setVisibility(View.GONE);
							tag.line2.setVisibility(View.GONE);
						} else if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && s15.equalsIgnoreCase("0")) {
							tag.call_msg_btn2.setVisibility(View.GONE);
							tag.line2.setVisibility(View.GONE);
						} else if (!DeviceInfo.CONFIG_SUPPORT_IM && s14.equalsIgnoreCase("0")) {
							tag.call_msg_btn2.setVisibility(View.GONE);
							tag.line2.setVisibility(View.GONE);
						} else {
							tag.call_msg_btn2.setVisibility(View.VISIBLE);
							tag.line2.setVisibility(View.VISIBLE);
						}
					} else {
						tag.call_msg_btn2.setVisibility(View.GONE);
						tag.line2.setVisibility(View.GONE);
					}
					if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE || (!TextUtils.isEmpty((CharSequence) s11) && s11.equalsIgnoreCase("0"))) {
						tag.call_voice_btn.setVisibility(View.GONE);
						tag.line1.setVisibility(View.GONE);
					} else {
						tag.call_voice_btn.setVisibility(View.VISIBLE);
						tag.line1.setVisibility(View.VISIBLE);
						if (tag.call_msg_btn2.getVisibility() == View.VISIBLE) {
							tag.line2.setVisibility(View.VISIBLE);
						} else {
							tag.line2.setVisibility(View.GONE);
						}
					}
					if (!this.isVisibleContactVideo(s5, s10)) {
						tag.contact_video.setVisibility(View.GONE);
						tag.line1.setVisibility(View.GONE);
					} else {
						tag.contact_video.setVisibility(View.VISIBLE);
						if (tag.call_voice_btn.getVisibility() == View.VISIBLE) {
							tag.line1.setVisibility(View.VISIBLE);
						} else {
							tag.line1.setVisibility(View.GONE);
						}
					}
					tag.contact_image.setImageResource(R.drawable.icon_contact);
				} else if (Member.UserType.toUserType(memberType) == Member.UserType.SVP) {
					if (this.getServerListArray() == -1) {
						tag.call_msg_btn2.setVisibility(View.GONE);
						tag.line2.setVisibility(View.GONE);
					} else {
						tag.call_msg_btn2.setVisibility(View.VISIBLE);
						tag.line2.setVisibility(View.VISIBLE);
					}
					if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE || (!TextUtils.isEmpty((CharSequence) s11) && s11.equalsIgnoreCase("0"))) {
						tag.call_voice_btn.setVisibility(View.GONE);
						tag.line1.setVisibility(View.GONE);
					} else {
						tag.call_voice_btn.setVisibility(View.VISIBLE);
						tag.line1.setVisibility(View.VISIBLE);
					}
					if (!this.isVisibleContactVideo(s5, s10)) {
						tag.contact_video.setVisibility(View.GONE);
						tag.line1.setVisibility(View.GONE);
					} else {
						tag.contact_video.setVisibility(View.VISIBLE);
						tag.line1.setVisibility(View.VISIBLE);
					}
					tag.contact_image.setImageResource(R.drawable.icon_dispatcher);
				} else if (Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GVS || Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GB28181) {
					tag.contact_image.setImageResource(R.drawable.icon_gvs);
					tag.call_msg_btn2.setVisibility(View.GONE);
					tag.call_voice_btn.setVisibility(View.GONE);
					tag.line1.setVisibility(View.GONE);
					tag.line2.setVisibility(View.GONE);
					tag.call_voice_btn.setVisibility(View.GONE);
					if (DeviceInfo.CONFIG_VIDEO_MONITOR == 1) {
						tag.contact_video.setVisibility(View.VISIBLE);
					} else {
						tag.contact_video.setVisibility(View.GONE);
					}
				} else {
					tag.call_msg_btn2.setVisibility(View.GONE);
					tag.call_voice_btn.setVisibility(View.GONE);
					tag.line1.setVisibility(View.GONE);
					tag.line2.setVisibility(View.GONE);
					tag.contact_video.setVisibility(View.GONE);
					if (DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						tag.call_voice_btn.setVisibility(View.VISIBLE);
					} else {
						tag.call_voice_btn.setVisibility(View.GONE);
					}
				}
			}
			tag.contact_name.setText((CharSequence) this.mData.get(n).get("mname"));
			tag.contact_num.setText((CharSequence) this.mData.get(n).get("number"));
			tag.call_msg_btn2.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					Log.i("jiangkai", "mtype  " + s5 + " smsswitch  " + s15 + " pictureupload " + s14);
					if ((s5 != null && Member.UserType.toUserType(s5) == Member.UserType.SVP) || (s15 != null && s14 != null && s14.equalsIgnoreCase("1") && s15.equalsIgnoreCase("1"))) {
						if (DepartmentAdapter.this.getServerListArray() != -1) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(DepartmentAdapter.this.getServerListArray(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DepartmentAdapter.this.getServerListArray() != R.array.msgDialogList2) {
												this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MessageDialogueActivity.class);
												this.intent.putExtra("userName", s4);
												this.intent.putExtra("address", s3);
												this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
												DepartmentAdapter.this.mContext.startActivity(this.intent);
												return;
											}
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
										case 1: {
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
									}
								}
							}).show();
						}
					} else {
						if (s5 != null && Member.UserType.toUserType(s5) == Member.UserType.MOBILE_GQT && s15 != null && s14 != null && s14.equalsIgnoreCase("0") && s15.equalsIgnoreCase("1")) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.msgDialogList1, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DepartmentAdapter.this.getServerListArray() != R.array.msgDialogList2) {
												this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MessageDialogueActivity.class);
												this.intent.putExtra("userName", s4);
												this.intent.putExtra("address", s3);
												this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
												DepartmentAdapter.this.mContext.startActivity(this.intent);
												return;
											}
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
									}
								}
							}).show();
							return;
						}
						if (s5 != null && Member.UserType.toUserType(s5) == Member.UserType.MOBILE_GQT && s15 != null && s14 != null && s14.equalsIgnoreCase("1") && s15.equalsIgnoreCase("0")) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.msgDialogList2, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
									}
								}
							}).show();
							return;
						}
						if (s5 != null && Member.UserType.toUserType(s5) == Member.UserType.MOBILE_GQT && s15 != null && s14 != null && s14.equalsIgnoreCase("1") && s15.equalsIgnoreCase("1") && DepartmentAdapter.this.getServerListArray() != -1) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(DepartmentAdapter.this.getServerListArray(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DepartmentAdapter.this.getServerListArray() != R.array.msgDialogList2) {
												this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MessageDialogueActivity.class);
												this.intent.putExtra("userName", s4);
												this.intent.putExtra("address", s3);
												this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
												DepartmentAdapter.this.mContext.startActivity(this.intent);
												return;
											}
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
										case 1: {
											this.intent.setClass(DepartmentAdapter.this.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", s4);
											this.intent.putExtra("address", s3);
											DepartmentAdapter.this.mContext.startActivity(this.intent);
										}
									}
								}
							}).show();
						}
					}
				}
			});
			tag.call_voice_btn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					if (s3 == null) {
						DialogUtil.showCheckDialog(DepartmentAdapter.this.mContext, DepartmentAdapter.this.mContext.getResources().getString(R.string.information), DepartmentAdapter.this.mContext.getResources().getString(R.string.number_not_exist), DepartmentAdapter.this.mContext.getResources().getString(R.string.ok_know));
						return;
					}
					if (DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						if ((s5 != null && Member.UserType.toUserType(s5) == Member.UserType.SVP) || (s5 != null && Member.UserType.toUserType(s5) == Member.UserType.MOBILE_GQT && s11 != null && s11.equalsIgnoreCase("1"))) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
												if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
													CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
													return;
												}
												DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
												return;
											} else {
												if (MemoryMg.getInstance().PhoneType == 1) {
													CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
													return;
												}
												DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
												return;
											}
										}
									}
								}
							}).show();
							return;
						}
						if ((s5 != null && Member.UserType.toUserType(s5) == Member.UserType.VIDEO_MONITOR_GVS) || Member.UserType.toUserType(s5) == Member.UserType.VIDEO_MONITOR_GB28181) {
							new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
												if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
													CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
													return;
												}
												DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
												return;
											} else {
												if (MemoryMg.getInstance().PhoneType == 1) {
													CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
													return;
												}
												DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
												return;
											}
										}
									}
								}
							}).show();
							return;
						}
						new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
												return;
											}
											DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
												return;
											}
											DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
											return;
										}
									}
								}
							}
						}).show();
					} else {
						if ((!DeviceInfo.CONFIG_SUPPORT_AUDIO && s5 != null && Member.UserType.toUserType(s5) == Member.UserType.VIDEO_MONITOR_GVS) || Member.UserType.toUserType(s5) == Member.UserType.VIDEO_MONITOR_GB28181) {
							MyToast.showToast(true, DepartmentAdapter.this.mContext, R.string.number_no_sustain);
							return;
						}
						new AlertDialog.Builder(DepartmentAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
												return;
											}
											DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall(DepartmentAdapter.this.mContext, s3, null);
												return;
											}
											DepartmentAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s3)));
											return;
										}
									}
								}
							}
						}).show();
					}
				}
			});
			tag.contact_video.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					CallUtil.makeVideoCall(DepartmentAdapter.this.mContext, s3, null, "videobut");
				}
			});
			if (s3.equals(MemoryMg.getInstance().TerminalNum)) {
				tag.call_msg_btn2.setVisibility(View.GONE);
				tag.call_voice_btn.setVisibility(View.GONE);
				tag.line1.setVisibility(View.GONE);
				tag.line2.setVisibility(View.GONE);
				tag.contact_video.setVisibility(View.GONE);
				tag.call_voice_btn.setVisibility(View.GONE);
				s2 = "1";
			}
			if (s2.equals("0")) {
				tag.contact_name.setTextColor(this.mContext.getResources().getColor(R.color.black));
				tag.contact_num.setTextColor(this.mContext.getResources().getColor(R.color.black));
			} else {
				tag.contact_name.setTextColor(this.mContext.getResources().getColor(R.color.onLine));
				tag.contact_num.setTextColor(this.mContext.getResources().getColor(R.color.onLine));
			}
			if (this.mName != null && !this.mName.equals("")) {
				final String s17 = this.mData.get(n).get("mname");
				final String string = tag.contact_num.getText().toString();
				if (s17.contains(this.mName) || s17.toLowerCase().contains(this.mName.toLowerCase())) {
					tag.contact_name.setText((CharSequence) this.getStringText(s17, this.mName, s2));
				}
				if (string.contains(this.mName)) {
					tag.contact_num.setText((CharSequence) this.getStringText(string, this.mName, s2));
					return inflate;
				}
			}
		}
		return inflate;
	}
}
