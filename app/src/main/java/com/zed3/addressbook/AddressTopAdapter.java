package com.zed3.addressbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zed3.sipua.R;

import java.util.List;
import java.util.Map;

public class AddressTopAdapter extends BaseAdapter {
	Context context;
	DataBaseService dbService;
	List<Map<String, String>> mData;
	private LayoutInflater mInflater;

	public AddressTopAdapter(final Context context) {
		this.mInflater = LayoutInflater.from(context);
		this.context = context;
		this.dbService = DataBaseService.getInstance();
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
		return 0L;
	}

	public View getView(final int n, View inflate, final ViewGroup viewGroup) {
		AddressViewHolder tag;
		if (inflate == null) {
			tag = new AddressViewHolder();
			inflate = this.mInflater.inflate(R.layout.addressbook_title_item, (ViewGroup) null);
			tag.name = (TextView) inflate.findViewById(R.id.tv_company_name);
			tag.memberNum = (TextView) inflate.findViewById(R.id.tv_company_num);
			inflate.setTag((Object) tag);
		} else {
			tag = (AddressViewHolder) inflate.getTag();
		}
		tag.name.setText((CharSequence) this.mData.get(n).get("name"));
		tag.memberNum.setText((CharSequence) "");
		return inflate;
	}

	public String getmId(final int n) {
		final String s = this.mData.get(n).get("id");
		System.out.println("-----mid" + s);
		return s;
	}

	public String getmName(final int n) {
		return this.mData.get(n).get("name");
	}
}
