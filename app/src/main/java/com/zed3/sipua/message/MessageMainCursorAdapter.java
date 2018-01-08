package com.zed3.sipua.message;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;

public class MessageMainCursorAdapter extends CursorAdapter {
	private String mAddress;
	private LayoutInflater mInflater;
	private String mUserName;
	private String sipName;

	public MessageMainCursorAdapter(Context context, Cursor c) {
		super(context, c, true);
//		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		this.mAddress = cursor.getString(cursor.getColumnIndex("address"));
		this.sipName = cursor.getString(cursor.getColumnIndex("sip_name"));
		MyLog.i("sipname Main", "sipname = " + this.sipName);
		this.mUserName = ContactUtil.getUserName(this.mAddress);
		if (this.mUserName == null) {
			if (this.sipName != null && this.sipName.equals("")) {
				this.mUserName = this.sipName;
			} else {
				this.mUserName = GroupListUtil.getUserName(this.mAddress);
				if (this.mUserName == null) {
					this.mUserName = this.mAddress;
				}
			}
		}
		final TextView textView = (TextView) view.findViewById(R.id.thread_list_item_message_name);
		final TextView textView2 = (TextView) view.findViewById(R.id.thread_list_item_message_summary);
		final TextView textView3 = (TextView) view.findViewById(R.id.thread_list_item_time);
		if (cursor.getInt(cursor.getColumnIndex("status")) == 0) {
			textView.setText((CharSequence) (String.valueOf(this.mUserName) + SipUAApp.mContext.getResources().getString(R.string.unread)));
		} else {
			textView.setText((CharSequence) this.mUserName);
		}
		textView2.setText((CharSequence) cursor.getString(cursor.getColumnIndex("body")));
		textView3.setText((CharSequence) cursor.getString(cursor.getColumnIndex("date")));
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup viewGroup) {
		return this.mInflater.inflate(R.layout.vlist_message, viewGroup, false);
	}

	public void setSelectItem(final int n) {
	}
}
