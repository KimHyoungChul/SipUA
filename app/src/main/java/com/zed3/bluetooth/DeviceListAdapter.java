package com.zed3.bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zed3.sipua.R;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
	private ArrayList<deviceListItem> list;
	private LayoutInflater mInflater;

	public DeviceListAdapter(final Context context, final ArrayList<deviceListItem> list) {
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
		final deviceListItem deviceListItem = this.list.get(n);
		ViewHolder tag;
		if (inflate == null) {
			inflate = this.mInflater.inflate(R.layout.list_item, (ViewGroup) null);
			tag = new ViewHolder(inflate.findViewById(R.id.list_child), (TextView) inflate.findViewById(R.id.chat_msg));
			inflate.setTag((Object) tag);
		} else {
			tag = (ViewHolder) inflate.getTag();
		}
		if (deviceListItem.isSiri) {
			tag.child.setBackgroundResource(R.drawable.msgbox_rec);
		} else {
			tag.child.setBackgroundResource(R.drawable.msgbox_send);
		}
		tag.msg.setText((CharSequence) deviceListItem.message);
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

	public class deviceListItem {
		boolean isSiri;
		String message;

		public deviceListItem(final String message, final boolean isSiri) {
			this.message = message;
			this.isSiri = isSiri;
		}
	}
}
