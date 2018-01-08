package com.zed3.sipua.ui.lowsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.customgroup.GroupInfoItem;
import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;

import java.util.ArrayList;
import java.util.List;

public class SelectPersonsActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {
	private MemberListAdapter adapter;
	private boolean isInvite;
	private TextView mCancel;
	private List<GroupInfoItem> mGroupMembers;
	private ArrayList<String> mInviteList;
	private ListView mMemberList;
	private TextView mOk;
	private SearchView mSearchView;
	private ArrayList<String> mSelectedList;
	private String mTempGroupName;

	public SelectPersonsActivity() {
		this.mGroupMembers = new ArrayList<GroupInfoItem>();
		this.mSelectedList = new ArrayList<String>();
		this.mInviteList = new ArrayList<String>();
		this.isInvite = false;
	}

	private List<GroupInfoItem> getMembersInfoFromDB() {
		final List<GroupInfoItem> allMembers = DataBaseService.getInstance().getAllMembers();
		Object o = null;
		for (final GroupInfoItem groupInfoItem : allMembers) {
			if (Settings.getUserName().equals(groupInfoItem.getGrp_uNumber())) {
				o = groupInfoItem;
				break;
			}
		}
		if (o != null) {
			allMembers.remove(o);
		}
		return allMembers;
	}

	private void init() {
		this.mOk = (TextView) this.findViewById(R.id.tv_ok);
		this.mCancel = (TextView) this.findViewById(R.id.tv_cancel);
		this.mOk.setOnClickListener((View.OnClickListener) this);
		this.mCancel.setOnClickListener((View.OnClickListener) this);
		(this.mSearchView = (SearchView) this.findViewById(R.id.sv_search_person)).setOnQueryTextListener((SearchView.OnQueryTextListener) this);
		this.mMemberList = (ListView) this.findViewById(R.id.lv_member_list);
		this.adapter = new MemberListAdapter(this.mGroupMembers);
		this.mMemberList.setAdapter((ListAdapter) this.adapter);
		this.mMemberList.setOnItemClickListener((AdapterView.OnItemClickListener) this);
	}

	public CharSequence getHighLightText(final String s, final String s2) {
		final int index = s.toLowerCase().indexOf(s2.toLowerCase());
		final int length = s2.length();
		return (CharSequence) Html.fromHtml(String.valueOf(s.substring(0, index)) + "<u><font color=#FF0000>" + s.substring(index, index + length) + "</font></u>" + s.substring(index + length, s.length()));
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.tv_ok: {
				if (!this.isInvite) {
					if (!this.mSelectedList.contains(Settings.getUserName())) {
						Log.i("zdx", "username" + Settings.getUserName());
						this.mSelectedList.add(Settings.getUserName());
					}
					TempGroupCallUtil.makeTempGroupCall((Context) this, this.mTempGroupName, this.mSelectedList, true);
				} else {
					final StringBuilder sb = new StringBuilder();
					for (int i = 0; i < this.mInviteList.size(); ++i) {
						sb.append(this.mInviteList.get(i));
						if (i != this.mInviteList.size() - 1) {
							sb.append(",");
						}
					}
					Receiver.GetCurUA().JoinTmpGrpCall(Receiver.GetCurUA().GetCurGrp(), sb.toString());
					this.setResult(-1, new Intent().putStringArrayListExtra("inviteMembers", (ArrayList) this.mInviteList));
				}
				this.finish();
			}
			case R.id.tv_cancel: {
				this.finish();
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.activity_select_person_list);
		final Intent intent = this.getIntent();
		if (!intent.getBooleanExtra("isInvite", false)) {
			this.mTempGroupName = intent.getStringExtra("tempGroupName");
		} else {
			this.isInvite = true;
			this.mTempGroupName = intent.getStringExtra("tempGroupName");
			this.mSelectedList = (ArrayList<String>) intent.getStringArrayListExtra("selectedList");
		}
		this.mGroupMembers = this.getMembersInfoFromDB();
		this.init();
	}

	protected void onDestroy() {
		super.onDestroy();
		this.isInvite = false;
	}

	public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
		final List<GroupInfoItem> dataList = this.adapter.getDataList();
		if (!this.isInvite) {
			((CheckBox) view.findViewById(R.id.grp_img)).toggle();
		} else if (!this.mSelectedList.contains(dataList.get(n).getGrp_uNumber())) {
			((CheckBox) view.findViewById(R.id.grp_img)).toggle();
		}
	}

	public boolean onQueryTextChange(final String s) {
		List<GroupInfoItem> list;
		if (!TextUtils.isEmpty((CharSequence) s)) {
			list = CustomGroupUtil.getInstance().searchListBykeyWord(s, this.mGroupMembers);
		} else {
			list = this.getMembersInfoFromDB();
		}
//        MemberListAdapter.access.0(this.adapter, s);
//        MemberListAdapter.access.1(this.adapter, list);
		this.adapter.notifyDataSetChanged();
		return false;
	}

	public boolean onQueryTextSubmit(final String s) {
		return false;
	}

	class MemberListAdapter extends BaseAdapter {
		private List<GroupInfoItem> dataList;
		private ViewHolder holder;
		private boolean ischecked;
		private String keyword;

		public MemberListAdapter(final List<GroupInfoItem> dataList) {
			this.keyword = "";
			this.ischecked = false;
			this.dataList = dataList;
		}

		public int getCount() {
			return this.dataList.size();
		}

		public List<GroupInfoItem> getDataList() {
			return this.dataList;
		}

		public Object getItem(final int n) {
			return this.dataList.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			if (inflate == null) {
				inflate = LayoutInflater.from((Context) SelectPersonsActivity.this).inflate(R.layout.contact_member_item, (ViewGroup) null);
				this.holder = new ViewHolder();
				this.holder.checkBox = (CheckBox) inflate.findViewById(R.id.grp_img);
				this.holder.name = (TextView) inflate.findViewById(R.id.grp_uName);
				this.holder.number = (TextView) inflate.findViewById(R.id.grp_uNumber);
				this.holder.department = (TextView) inflate.findViewById(R.id.grp_uDept);
				inflate.setTag((Object) this.holder);
			} else {
				this.holder = (ViewHolder) inflate.getTag();
			}
			final GroupInfoItem groupInfoItem = this.dataList.get(n);
			if (groupInfoItem != null) {
				this.holder.checkBox.setTag((Object) groupInfoItem.getGrp_uNumber());
				if (SelectPersonsActivity.this.mInviteList.contains(groupInfoItem.getGrp_uNumber())) {
					this.holder.checkBox.setChecked(true);
				} else if (SelectPersonsActivity.this.mSelectedList.contains(groupInfoItem.getGrp_uNumber())) {
					this.holder.checkBox.setChecked(true);
					if (SelectPersonsActivity.this.isInvite) {
						this.holder.checkBox.setEnabled(false);
						MyLog.e("gengjibin", "checkbox.setEnabled false");
					}
				} else {
					this.holder.checkBox.setChecked(false);
					this.holder.checkBox.setEnabled(true);
				}
				if (groupInfoItem.getGrp_uName() != null && groupInfoItem.getGrp_uName().toLowerCase().contains(this.keyword.toLowerCase())) {
					this.holder.name.setText(SelectPersonsActivity.this.getHighLightText(groupInfoItem.getGrp_uName(), this.keyword));
				} else {
					this.holder.name.setText((CharSequence) groupInfoItem.getGrp_uName());
				}
				if (groupInfoItem.getGrp_uNumber() != null && groupInfoItem.getGrp_uNumber().toLowerCase().contains(this.keyword.toLowerCase())) {
					this.holder.number.setText(SelectPersonsActivity.this.getHighLightText(groupInfoItem.getGrp_uNumber(), this.keyword));
				} else {
					this.holder.number.setText((CharSequence) groupInfoItem.getGrp_uNumber());
				}
				if (groupInfoItem.getGrp_uDept() != null && groupInfoItem.getGrp_uDept().toLowerCase().contains(this.keyword.toLowerCase())) {
					this.holder.department.setText(SelectPersonsActivity.this.getHighLightText(groupInfoItem.getGrp_uDept(), this.keyword));
				} else {
					this.holder.department.setText((CharSequence) groupInfoItem.getGrp_uDept());
				}
				this.holder.checkBox.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
						if (!SelectPersonsActivity.this.isInvite) {
							if (b) {
								if (!SelectPersonsActivity.this.mSelectedList.contains(compoundButton.getTag().toString())) {
									SelectPersonsActivity.this.mSelectedList.add(compoundButton.getTag().toString());
								}
							} else {
								SelectPersonsActivity.this.mSelectedList.remove(compoundButton.getTag().toString());
							}
							Log.i("zdx", "selectlist size : " + SelectPersonsActivity.this.mSelectedList.size());
							return;
						}
						if (b) {
							if (!SelectPersonsActivity.this.mSelectedList.contains(compoundButton.getTag().toString()) && !SelectPersonsActivity.this.mInviteList.contains(compoundButton.getTag().toString())) {
								SelectPersonsActivity.this.mInviteList.add(compoundButton.getTag().toString());
								MyLog.e("gengjibin", "setChecked 2");
							}
						} else {
							SelectPersonsActivity.this.mInviteList.remove(compoundButton.getTag().toString());
						}
						Log.i("zdx", "mInviteList size : " + SelectPersonsActivity.this.mInviteList.size());
					}
				});
			}
			return inflate;
		}
	}

	class ViewHolder {
		CheckBox checkBox;
		TextView department;
		TextView name;
		TextView number;
	}
}
