package com.zed3.sipua.message;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.utils.LogUtil;

public class MessageDialogueCursorAdapter extends CursorAdapter {
	private LayoutInflater mInflater;

	public MessageDialogueCursorAdapter(final Context context, final Cursor cursor) {
		super(context, cursor);
		this.mInflater = LayoutInflater.from(context);
	}

	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int int1 = cursor.getInt(cursor.getColumnIndex("mark"));
		final int int2 = cursor.getInt(cursor.getColumnIndex("send"));
		final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.send);
		final RelativeLayout relativeLayout2 = (RelativeLayout) view.findViewById(R.id.receive);
		if (int1 == 0) {
			relativeLayout.setVisibility(View.GONE);
			relativeLayout2.setVisibility(View.VISIBLE);
			final TextView textView = (TextView) view.findViewById(R.id.txtDate_receive);
			final TextView textView2 = (TextView) view.findViewById(R.id.txtMsgContent_receive);
			LogUtil.makeLog("MessageDialogueCursorAdapter", "--++>>\u6536\u5230\u7684\u77ed\u4fe1\uff0c\u5728DB\u4e2d\u5f97\u5230\u7684\u6570\u636e\uff1a" + cursor.getString(cursor.getColumnIndex("body")));
			textView2.setText((CharSequence) cursor.getString(cursor.getColumnIndex("body")));
			textView.setText((CharSequence) cursor.getString(cursor.getColumnIndex("date")));
			((ImageView) view.findViewById(R.id.imgLeftDot)).setBackgroundResource(R.drawable.dot);
			return;
		}
		relativeLayout.setVisibility(View.VISIBLE);
		relativeLayout2.setVisibility(View.GONE);
		((TextView) view.findViewById(R.id.txtMsgContent_send)).setText((CharSequence) cursor.getString(cursor.getColumnIndex("body")));
		LogUtil.makeLog("MessageDialogueCursorAdapter", "--++>>\u53d1\u9001\u7684\u77ed\u4fe1\uff0c\u5728DB\u4e2d\u5f97\u5230\u7684\u6570\u636e\uff1a" + cursor.getString(cursor.getColumnIndex("body")) + "\t\u72b6\u6001\u503csend:" + int2);
		((TextView) view.findViewById(R.id.txtDate_send)).setText((CharSequence) cursor.getString(cursor.getColumnIndex("date")));
		final ImageView imageView = (ImageView) view.findViewById(R.id.imgRightDot);
		switch (int2) {
			default: {
			}
			case 0: {
				imageView.setImageResource(R.drawable.dot);
			}
			case 1: {
				imageView.setImageResource(R.drawable.led_error);
			}
			case 2: {
				imageView.setImageResource(R.drawable.led_ing);
			}
		}
	}

	public View newView(final Context context, final Cursor cursor, final ViewGroup viewGroup) {
		return this.mInflater.inflate(R.layout.msg_show, (ViewGroup) null);
	}
}
