package com.zed3.bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zed3.sipua.R;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {
	private ArrayList<ZMBluetoothSelectActivity.SiriListItem> list;
	private LayoutInflater mInflater;

	public ChatListAdapter(final Context context, final ArrayList<ZMBluetoothSelectActivity.SiriListItem> list) {
		this.list = list;
		this.mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(final int n) {
		return this.list.get(n);
	}

	public long getItemId(final int n) {
		return n;
	}

	public int getItemViewType(final int n) {
		return n;
	}

	public View getView(final int n, View inflate, final ViewGroup viewGroup) {
		final ZMBluetoothSelectActivity.SiriListItem siriListItem = this.list.get(n);
		ViewHolder tag;
		if (inflate == null) {
			inflate = this.mInflater.inflate(R.layout.list_item, (ViewGroup) null);
			tag = new ViewHolder(inflate.findViewById(R.id.list_child), (TextView) inflate.findViewById(R.id.chat_msg));
			inflate.setTag((Object) tag);
		} else {
			tag = (ViewHolder) inflate.getTag();
		}
		if (siriListItem.isSiri) {
			tag.child.setBackgroundResource(R.drawable.msgbox_rec);
		} else {
			tag.child.setBackgroundResource(R.drawable.msgbox_send);
		}
		tag.msg.setText((CharSequence) siriListItem.message);
		return inflate;
	}

	class ViewHolder {
		protected View child;
		protected TextView msg;

		public ViewHolder(final View child, final TextView msg) {
			this.child = child;
			this.msg = msg;
		}
	}
}
