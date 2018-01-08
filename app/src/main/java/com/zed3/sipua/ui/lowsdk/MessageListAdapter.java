package com.zed3.sipua.ui.lowsdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zed3.sipua.R;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends BaseAdapter {
	private Context context;
	private List<String> messageList;

	public MessageListAdapter(final Context context, final List messageList) {
		this.messageList = new ArrayList<String>();
		this.context = context;
		this.messageList = (List<String>) messageList;
	}

	public int getCount() {
		return this.messageList.size();
	}

	public Object getItem(final int n) {
		return null;
	}

	public long getItemId(final int n) {
		return 0L;
	}

	public View getView(final int n, View inflate, final ViewGroup viewGroup) {
		ViewHolder tag;
		if (inflate == null) {
			tag = new ViewHolder();
			inflate = LayoutInflater.from(this.context).inflate(R.layout.message_list_item, (ViewGroup) null);
//			ViewHolder.access .0 (tag, (TextView) inflate.findViewById(R.id.messagetext));
			inflate.setTag((Object) tag);
		} else {
			tag = (ViewHolder) inflate.getTag();
		}
		tag.messageView.setText((CharSequence) this.messageList.get(n));
		return inflate;
	}

	class ViewHolder {
		private TextView messageView;
	}

}
