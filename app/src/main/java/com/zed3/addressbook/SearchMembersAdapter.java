package com.zed3.addressbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.addressbook.PinnedHeaderListView.PinnedHeaderAdapter;
import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.sipua.R;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchMembersAdapter extends BaseAdapter implements AbsListView.OnScrollListener, PinnedHeaderAdapter {
	private static final String TAG;
	private Context mContext;
	private List<Map<String, String>> mData;
	private LayoutInflater mLayoutInflater;
	private ArrayList<String> tempGrpList;

	static {
		TAG = SearchMembersAdapter.class.getSimpleName();
	}

	public SearchMembersAdapter(final Context mContext, final List<Map<String, String>> mData) {
		this.tempGrpList = new ArrayList<String>();
		this.mContext = mContext;
		this.mData = mData;
		this.mLayoutInflater = LayoutInflater.from(this.mContext);
	}

	private int getServerList() {
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList;
		}
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList1;
		}
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD || DeviceInfo.CONFIG_SUPPORT_IM) {
			return -1;
		}
		return R.array.msgDialogList2;
	}

	private boolean isMove(int position) {
		Map<String, String> currentEntity = (Map) getItem(position);
		Map<String, String> nextEntity = (Map) getItem(position + 1);
		if (currentEntity == null || nextEntity == null) {
			return false;
		}
		String currentTitle = (String) currentEntity.get("title");
		String nextTitle = (String) nextEntity.get("title");
		if (currentTitle == null || nextTitle == null || currentTitle.equals(nextTitle)) {
			return false;
		}
		return true;
	}

	private boolean needTitle(int position) {
		if (position == 0) {
			return true;
		}
		if (position < 0) {
			return false;
		}
		Map<String, String> currentEntity = (Map) getItem(position);
		Map<String, String> previousEntity = (Map) getItem(position - 1);
		if (currentEntity == null || previousEntity == null) {
			return false;
		}
		String currentTitle = (String) currentEntity.get("title");
		String previousTitle = (String) previousEntity.get("title");
		if (previousTitle == null || currentTitle == null) {
			return false;
		}
		if (currentTitle.equals(previousTitle)) {
			return false;
		}
		return true;
	}

	public void configurePinnedHeader(View headerView, int position, int alpaha) {
		String headerValue = (String) ((Map) getItem(position)).get("title");
		Log.e(TAG, "header = " + headerValue);
		if (!TextUtils.isEmpty(headerValue)) {
			((TextView) headerView.findViewById(R.id.header)).setText(headerValue);
		}
	}

	public int getCount() {
		if (this.mData != null) {
			return this.mData.size();
		}
		return 0;
	}

	public Object getItem(final int n) {
		if (this.mData != null && n < this.getCount()) {
			return this.mData.get(n);
		}
		return null;
	}

	public long getItemId(final int n) {
		return n;
	}

	public int getPinnedHeaderState(final int n) {
		if (this.getCount() == 0 || n < 0) {
			return 0;
		}
		if (this.isMove(n)) {
			return 2;
		}
		return 1;
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public View getView(final int n, View inflate, final ViewGroup viewGroup) {
		DepartmentViewHolder tag;
		if (inflate == null) {
			tag = new DepartmentViewHolder();
			inflate = this.mLayoutInflater.inflate(R.layout.contact_item, (ViewGroup) null);
			tag.contact_name = (TextView) inflate.findViewById(R.id.contact_name);
			tag.contact_num = (TextView) inflate.findViewById(R.id.contact_num);
			tag.mTitles = (TextView) inflate.findViewById(R.id.contact_item_title);
			tag.contact_image = (ImageView) inflate.findViewById(R.id.contact_image);
			tag.call_msg_btn2 = (ImageView) inflate.findViewById(R.id.call_msg_btn2);
			tag.call_voice_btn = (ImageView) inflate.findViewById(R.id.call_voice_btn);
			tag.contact_video = (ImageView) inflate.findViewById(R.id.contact_video);
			tag.line1 = (LinearLayout) inflate.findViewById(R.id.line_sub);
			tag.line2 = (LinearLayout) inflate.findViewById(R.id.line_sub2);
			inflate.setTag((Object) tag);
		} else {
			tag = (DepartmentViewHolder) inflate.getTag();
		}
		final String s = this.mData.get(n).get("number");
		System.out.println("-----number=" + s);
		final String s2 = this.mData.get(n).get("mname");
		final String s3 = this.mData.get(n).get("mtype");
		final String s4 = this.mData.get(n).get("position");
		final String s5 = this.mData.get(n).get("sex");
		final String s6 = this.mData.get(n).get("phone");
		final String s7 = this.mData.get(n).get("dtype");
		final String s8 = this.mData.get(n).get("video");
		final String s9 = this.mData.get(n).get("audio");
		final String s10 = this.mData.get(n).get("pttmap");
		final String s11 = this.mData.get(n).get("gps");
		final String s12 = this.mData.get(n).get("pictureupload");
		final String s13 = this.mData.get(n).get("smsswitch");
		final String s14 = this.mData.get(n).get("title");
		final String memberType = DataBaseService.getInstance().getMemberType(s);
		System.out.println("-----type:" + memberType);
		inflate.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final Intent intent = new Intent(SearchMembersAdapter.this.mContext, (Class) UserMinuteActivity.class);
				intent.putExtra("mname", s2);
				intent.putExtra("position", s4);
				intent.putExtra("sex", s5);
				intent.putExtra("phone", s6);
				intent.putExtra("dtype", s7);
				intent.putExtra("video", s8);
				intent.putExtra("audio", s9);
				intent.putExtra("pttmap", s10);
				intent.putExtra("gps", s11);
				intent.putExtra("pictureupload", s12);
				intent.putExtra("smsswitch", s13);
				intent.putExtra("department", s14);
				intent.putExtra("mtype", s3);
				intent.putExtra("number", s);
				SearchMembersAdapter.this.mContext.startActivity(intent);
			}
		});
		if (Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT) {
			if (this.getServerList() == -1) {
				tag.call_msg_btn2.setVisibility(View.GONE);
			} else if (!TextUtils.isEmpty((CharSequence) s13) && !TextUtils.isEmpty((CharSequence) s12)) {
				if (s12.equalsIgnoreCase("0") && s13.equalsIgnoreCase("0")) {
					tag.call_msg_btn2.setVisibility(View.INVISIBLE);
				} else if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && s13.equalsIgnoreCase("0")) {
					tag.call_msg_btn2.setVisibility(View.INVISIBLE);
				} else if (!DeviceInfo.CONFIG_SUPPORT_IM && s12.equalsIgnoreCase("0")) {
					tag.call_msg_btn2.setVisibility(View.INVISIBLE);
				} else {
					tag.call_msg_btn2.setVisibility(View.VISIBLE);
				}
			} else {
				tag.call_msg_btn2.setVisibility(View.INVISIBLE);
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				tag.call_voice_btn.setVisibility(View.VISIBLE);
			} else {
				if (tag.call_msg_btn2.getVisibility() == View.VISIBLE) {
					tag.line1.setVisibility(View.VISIBLE);
				} else {
					tag.line1.setVisibility(View.GONE);
				}
				tag.call_voice_btn.setVisibility(View.VISIBLE);
			}
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				tag.line2.setVisibility(View.GONE);
				tag.contact_video.setVisibility(View.GONE);
			} else if (memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT && s8 != null && s8.equalsIgnoreCase("1")) {
				if (tag.call_msg_btn2.getVisibility() == View.VISIBLE || tag.call_voice_btn.getVisibility() == View.VISIBLE) {
					tag.line2.setVisibility(View.VISIBLE);
					tag.line1.setVisibility(View.VISIBLE);
				} else {
					tag.line2.setVisibility(View.GONE);
				}
				tag.contact_video.setVisibility(View.VISIBLE);
			} else {
				tag.line2.setVisibility(View.VISIBLE);
				tag.contact_video.setVisibility(View.INVISIBLE);
			}
			tag.contact_image.setImageResource(R.drawable.icon_contact);
		} else if (Member.UserType.toUserType(memberType) == Member.UserType.SVP) {
			if (!DeviceInfo.CONFIG_SUPPORT_IM) {
				tag.call_msg_btn2.setVisibility(View.GONE);
			} else {
				tag.call_msg_btn2.setVisibility(View.VISIBLE);
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				tag.call_voice_btn.setVisibility(View.VISIBLE);
			} else {
				if (tag.call_msg_btn2.getVisibility() == View.VISIBLE) {
					tag.line1.setVisibility(View.VISIBLE);
				} else {
					tag.line1.setVisibility(View.GONE);
				}
				tag.call_voice_btn.setVisibility(View.VISIBLE);
			}
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				tag.line2.setVisibility(View.GONE);
				tag.contact_video.setVisibility(View.GONE);
			} else {
				if (tag.call_msg_btn2.getVisibility() == View.VISIBLE || tag.call_voice_btn.getVisibility() == View.VISIBLE) {
					tag.line2.setVisibility(View.VISIBLE);
				} else {
					tag.line2.setVisibility(View.GONE);
				}
				tag.contact_video.setVisibility(View.VISIBLE);
			}
			tag.contact_image.setImageResource(R.drawable.icon_dispatcher);
		} else if (Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GVS || Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GB28181) {
			tag.contact_image.setImageResource(R.drawable.icon_gvs);
			tag.call_msg_btn2.setVisibility(View.GONE);
			tag.call_voice_btn.setVisibility(View.GONE);
			tag.line2.setVisibility(View.GONE);
			tag.call_voice_btn.setVisibility(View.VISIBLE);
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				tag.contact_video.setVisibility(View.GONE);
			} else {
				tag.contact_video.setVisibility(View.VISIBLE);
			}
		}
		tag.contact_name.setText((CharSequence) this.mData.get(n).get("mname"));
		tag.contact_num.setText((CharSequence) this.mData.get(n).get("number"));
		if (this.needTitle(n)) {
			tag.mTitles.setText((CharSequence) DataBaseService.getInstance().getTeamName(DataBaseService.getInstance().getPid(s)));
			tag.mTitles.setVisibility(View.VISIBLE);
		} else {
			tag.mTitles.setVisibility(View.GONE);
		}
		tag.call_msg_btn2.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (s.equals(MemoryMg.getInstance().TerminalNum)) {
					MyToast.showToast(true, SearchMembersAdapter.this.mContext, R.string.operation_notify);
				} else if ((memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.SVP) || (s13 != null && s12 != null && s12.equalsIgnoreCase("1") && s13.equalsIgnoreCase("1"))) {
					if (SearchMembersAdapter.this.getServerList() != -1) {
						new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(SearchMembersAdapter.this.getServerList(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							Intent intent = new Intent();

							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (SearchMembersAdapter.this.getServerList() != R.array.msgDialogList2) {
											this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MessageDialogueActivity.class);
											this.intent.putExtra("userName", s2);
											this.intent.putExtra("address", s);
											this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											SearchMembersAdapter.this.mContext.startActivity(this.intent);
											return;
										}
										this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MainActivity.class);
										this.intent.putExtra("action", "fastMMS");
										this.intent.putExtra("userName", s2);
										this.intent.putExtra("address", s);
										SearchMembersAdapter.this.mContext.startActivity(this.intent);
									}
									case 1: {
										this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MainActivity.class);
										this.intent.putExtra("action", "fastMMS");
										this.intent.putExtra("userName", s2);
										this.intent.putExtra("address", s);
										SearchMembersAdapter.this.mContext.startActivity(this.intent);
									}
								}
							}
						}).show();
					}
				} else {
					if (memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT && s13 != null && s12 != null && s12.equalsIgnoreCase("0") && s13.equalsIgnoreCase("1")) {
						new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.msgDialogList1, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							Intent intent = new Intent();

							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (SearchMembersAdapter.this.getServerList() != R.array.msgDialogList2) {
											this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MessageDialogueActivity.class);
											this.intent.putExtra("userName", s2);
											this.intent.putExtra("address", s);
											this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											SearchMembersAdapter.this.mContext.startActivity(this.intent);
											return;
										}
										this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MainActivity.class);
										this.intent.putExtra("action", "fastMMS");
										this.intent.putExtra("userName", s2);
										this.intent.putExtra("address", s);
										SearchMembersAdapter.this.mContext.startActivity(this.intent);
									}
								}
							}
						}).show();
						return;
					}
					if (memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT && s13 != null && s12 != null && s12.equalsIgnoreCase("1") && s13.equalsIgnoreCase("0")) {
						new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.msgDialogList2, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							Intent intent = new Intent();

							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										this.intent.setClass(SearchMembersAdapter.this.mContext, (Class) MainActivity.class);
										this.intent.putExtra("action", "fastMMS");
										this.intent.putExtra("userName", s2);
										this.intent.putExtra("address", s);
										SearchMembersAdapter.this.mContext.startActivity(this.intent);
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
				if (s == null) {
					DialogUtil.showCheckDialog(SearchMembersAdapter.this.mContext, SearchMembersAdapter.this.mContext.getResources().getString(R.string.information), SearchMembersAdapter.this.mContext.getResources().getString(R.string.number_not_exist), SearchMembersAdapter.this.mContext.getResources().getString(R.string.ok_know));
					return;
				}
				if (s.equals(MemoryMg.getInstance().TerminalNum)) {
					MyToast.showToast(true, SearchMembersAdapter.this.mContext, R.string.call_notify);
					return;
				}
				if (DeviceInfo.CONFIG_SUPPORT_AUDIO) {
					if ((memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.SVP) || (memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.MOBILE_GQT && s9 != null && s9.equalsIgnoreCase("1"))) {
						new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
												return;
											}
											SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
												return;
											}
											SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										}
									}
								}
							}
						}).show();
						return;
					}
					if ((memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GVS) || Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GB28181) {
						new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
												return;
											}
											SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
												return;
											}
											SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										}
									}
								}
							}
						}).show();
						return;
					}
					new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialogInterface, final int n) {
							switch (n) {
								default: {
								}
								case 0: {
									if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
										if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
											CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
											return;
										}
										SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
										return;
									} else {
										if (MemoryMg.getInstance().PhoneType == 1) {
											CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
											return;
										}
										SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
										return;
									}
								}
							}
						}
					}).show();
				} else {
					if ((!DeviceInfo.CONFIG_SUPPORT_AUDIO && memberType != null && Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GVS) || Member.UserType.toUserType(memberType) == Member.UserType.VIDEO_MONITOR_GB28181) {
						MyToast.showToast(true, SearchMembersAdapter.this.mContext, R.string.number_no_sustain);
						return;
					}
					new AlertDialog.Builder(SearchMembersAdapter.this.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialogInterface, final int n) {
							switch (n) {
								default: {
								}
								case 0: {
									if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
										if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
											CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
											return;
										}
										SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
										return;
									} else {
										if (MemoryMg.getInstance().PhoneType == 1) {
											CallUtil.makeAudioCall(SearchMembersAdapter.this.mContext, s, null);
											return;
										}
										SearchMembersAdapter.this.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
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
				CallUtil.makeVideoCall(SearchMembersAdapter.this.mContext, s, null, "videobut");
			}
		});
		return inflate;
	}

	public void onScroll(final AbsListView absListView, final int n, final int n2, final int n3) {
		if (absListView instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) absListView).controlPinnedHeader(n);
		}
	}

	public void onScrollStateChanged(final AbsListView absListView, final int n) {
	}
}
