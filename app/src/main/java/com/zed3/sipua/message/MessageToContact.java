package com.zed3.sipua.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.ContactPerson;

import java.util.List;

public class MessageToContact extends BaseActivity {
	private String Spell;
	private List<ContactPerson> adapterlist;
	private View btn_home_message;
	private String companyId;
	private DataBaseService dbService;
	private int flag;
	String intentActivity;
	private MyAdapter mAdapter;
	private Member mContactPerson;
	private Context mContext;
	ListView mUserList;
	private List<Member> member;

	private String currentUserSection() {
		return DataBaseService.getInstance().getPid(Settings.getUserName());
	}

	private void getCompanyId(final String companyId) {
		if (!this.dbService.isNoPid(companyId)) {
			this.getCompanyId(this.dbService.getId(companyId));
			return;
		}
		this.companyId = companyId;
	}

	private void getSection(String sectionId, final String s) {
		if (!this.dbService.isNoTeams(s)) {
			final String string = "'or pid = '" + s;
			++this.flag;
			this.Spell = String.valueOf(string) + sectionId;
			sectionId = this.dbService.getSectionId(s);
			this.getSection(this.Spell, sectionId);
		} else if (this.flag != 0) {
			this.Spell = String.valueOf(this.Spell) + "'";
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.contact_from_message);
		this.mContext = (Context) this;
		this.mUserList = (ListView) this.findViewById(R.id.contact_message);
		this.intentActivity = this.getIntent().getStringExtra("intentActivity");
		this.dbService = DataBaseService.getInstance();
		final String companyShowflag = this.dbService.getCompanyShowflag();
		if (!this.dbService.isNoPid(this.currentUserSection()) && !TextUtils.isEmpty((CharSequence) companyShowflag) && companyShowflag.equals("1")) {
			if (this.dbService.isNoPid(this.dbService.getId(this.currentUserSection()))) {
				this.member = this.dbService.queryMembersByKeyword(this.mContext, "", this.currentUserSection(), this.dbService.getId(this.currentUserSection()), null);
			} else {
				final String sectionId = this.dbService.getSectionId(this.currentUserSection());
				MyLog.i("gengjibin", "sectionid=" + sectionId);
				this.getSection("", sectionId);
				this.getCompanyId(this.currentUserSection());
				MyLog.i("gengjibin", "Spell=" + this.Spell);
				this.member = this.dbService.queryMembersByKeyword(this.mContext, "", this.currentUserSection(), this.companyId, this.Spell);
			}
		} else {
			this.member = this.dbService.queryMembersByKeyword(this.mContext, "");
		}
		this.mAdapter = new MyAdapter(this.member);
		this.mUserList.setAdapter((ListAdapter) this.mAdapter);
		this.mUserList.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				if (MessageToContact.this.member != null) {
//					MessageToContact.access .2
//					(MessageToContact.this, MessageToContact.this.member.get(n));
				}
				if (MessageToContact.this.mContactPerson != null && MessageToContact.this.intentActivity != null) {
					if (MessageToContact.this.intentActivity.equals("MessageComposeActivity")) {
						final String number = MessageToContact.this.mContactPerson.getNumber();
						if (!TextUtils.isEmpty((CharSequence) number) && number.equals(MemoryMg.getInstance().TerminalNum)) {
//							Toast.makeText((Context) MessageToContact.this, (CharSequence) "不能给自己发短信", 1500L).show();
							return;
						}
						final Intent intent = new Intent(MessageToContact.this.mContext, (Class) MessageComposeActivity.class);
						intent.putExtra("number", number);
						intent.putExtra("name", MessageToContact.this.mContactPerson.getmName());
						MessageToContact.this.setResult(0, intent);
						MessageToContact.this.finish();
					} else if (MessageToContact.this.intentActivity.equals("PhotoTransferActivity")) {
						final Intent intent2 = new Intent();
						intent2.putExtra("number", MessageToContact.this.mContactPerson.getNumber());
						intent2.putExtra("name", MessageToContact.this.mContactPerson.getmName());
						MessageToContact.this.setResult(0, intent2);
						MessageToContact.this.finish();
					}
				}
			}
		});
		(this.btn_home_message = this.findViewById(R.id.btn_home_message)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (MessageToContact.this.intentActivity.equals("MessageComposeActivity")) {
					MessageToContact.this.finish();
				} else if (MessageToContact.this.intentActivity.equals("PhotoTransferActivity")) {
//					MainActivity.getInstance().startIntent(PhotoTransferActivity.class);
				}
				MessageToContact.this.finish();
			}
		});
		this.btn_home_message.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) MessageToContact.this.findViewById(R.id.photo_sent_home2);
				final TextView textView2 = (TextView) MessageToContact.this.findViewById(R.id.left_photo2);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						MessageToContact.this.btn_home_message.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(MessageToContact.this.getResources().getColor(R.color.font_color3));
						MessageToContact.this.btn_home_message.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		super.onCreate(bundle);
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n == 4) {
			this.finish();
		}
		return super.onKeyDown(n, keyEvent);
	}

	class MyAdapter extends BaseAdapter {
		private List<Member> mData;
		private LayoutInflater mInflater;

		public MyAdapter(final List<Member> mData) {
			this.mData = mData;
			this.mInflater = LayoutInflater.from(MessageToContact.this.mContext);
		}

		public boolean areAllItemsEnabled() {
			return true;
		}

		public int getCount() {
			return this.mData.size();
		}

		public Object getItem(final int n) {
			return this.mData.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			ViewHolder tag;
			if (inflate == null) {
				tag = new ViewHolder();
				inflate = this.mInflater.inflate(R.layout.contact_user_list_item, (ViewGroup) null);
				inflate.setSelected(true);
				inflate.setEnabled(true);
				tag.img = (ImageView) inflate.findViewById(R.id.img);
				tag.title = (TextView) inflate.findViewById(R.id.title);
				tag.info = (TextView) inflate.findViewById(R.id.info);
				inflate.setTag((Object) tag);
			} else {
				tag = (ViewHolder) inflate.getTag();
			}
			tag.title.setText((CharSequence) this.mData.get(n).getmName());
			tag.info.setText((CharSequence) this.mData.get(n).getNumber());
			return inflate;
		}

		public boolean isEnabled(final int n) {
			return super.isEnabled(n);
		}

		public void setData(final List<Member> mData) {
			this.mData = mData;
		}
	}

	public final class ViewHolder {
		public ImageView img;
		public TextView info;
		public TextView title;
	}
}
