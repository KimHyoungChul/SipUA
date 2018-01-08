package com.zed3.sipua.message;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.MessageListAdapter;
import com.zed3.toast.MyToast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageComposeActivity extends BaseActivity implements View.OnClickListener {
	private static int MAXNUMBER = 0;
	public static final int REQUEST_SELECT_SYS_CONTACT = 1;
	public static String edtTransfer;
	private String Spell;
	private String bodyValue;
	private Button btnSelectMsg;
	private Button btnSendMsg;
	private View btn_home_message2;
	private String companyId;
	private Context context;
	private DataBaseService dbService;
	private EditText edtInputMsg;
	private EditText edtInputMsger;
	private int flag;
	private GroupMemberAdapter groupMemberAdapter;
	private ImageView imbContact;
	private RelativeLayout inputLayout;
	private boolean isContent;
	private boolean isdown;
	private boolean isshowing;
	private ListView listView;
	String mbody;
	private List<Member> member;
	private ListView messageList;
	private View messageView;
	private PopupWindow popview;
	List<String> strings;
	private String toValue;
	private String userName;
	private String userNum;

	static {
		MessageComposeActivity.edtTransfer = "";
		MessageComposeActivity.MAXNUMBER = 100;
	}

	public MessageComposeActivity() {
		this.isContent = false;
		this.isshowing = false;
		this.isdown = false;
	}

	public static File byteToFile(final byte[] array, final String s) throws Exception {
		final File file = new File(s);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		bufferedOutputStream.write(array);
		if (bufferedOutputStream != null) {
			bufferedOutputStream.close();
		}
		if (fileOutputStream != null) {
			fileOutputStream.close();
		}
		return file;
	}

	private String currentUserSection() {
		return DataBaseService.getInstance().getPid(Settings.getUserName());
	}

	public static String fileToString(final String s) throws Exception {
		final FileInputStream fileInputStream = new FileInputStream(new File(s));
		final byte[] array = new byte[1024];
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		while (true) {
			final int read = fileInputStream.read(array);
			if (read == -1) {
				break;
			}
			byteArrayOutputStream.write(array, 0, read);
		}
		final String s2 = new String(Base64.encode(byteArrayOutputStream.toByteArray(), 0));
		byteArrayOutputStream.close();
		fileInputStream.close();
		return s2;
	}

	private void getCompanyId(final String companyId) {
		if (!this.dbService.isNoPid(companyId)) {
			this.getCompanyId(this.dbService.getId(companyId));
			return;
		}
		this.companyId = companyId;
	}

	private List<Map<String, String>> getSearchMemberData(List<Member> member) {
		List<Map<String, String>> mList = new ArrayList();
		MyLog.e("dd", "new member start " + System.currentTimeMillis());
		for (int i = 0; i < member.size(); i++) {
			Map<String, String> map = new HashMap();
			String pid = DataBaseService.getInstance().getPid(((Member) member.get(i)).getNumber());
			map.put(UserMinuteActivity.USER_MNAME, ((Member) member.get(i)).getmName());
			map.put("number", ((Member) member.get(i)).getNumber());
			mList.add(map);
		}
		MyLog.e("dd", "new member end " + System.currentTimeMillis());
		return mList;
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

	private void init() {
		this.context = (Context) this;
		this.btnSendMsg = (Button) this.findViewById(R.id.btnSendMsg);
		this.inputLayout = (RelativeLayout) this.findViewById(R.id.rllMsgBottomBar03);
		this.btnSelectMsg = (Button) this.findViewById(R.id.btnselectmsg);
		(this.edtInputMsg = (EditText) this.findViewById(R.id.edtInputMsg)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
		this.edtInputMsger = (EditText) this.findViewById(R.id.edtInputMsger);
		final String stringExtra = this.getIntent().getStringExtra("content");
		if (stringExtra != null && !stringExtra.equals("")) {
			this.edtInputMsg.setText((CharSequence) stringExtra);
			MessageComposeActivity.edtTransfer = stringExtra;
		} else if (!MessageComposeActivity.edtTransfer.equals("")) {
			this.edtInputMsg.setText((CharSequence) MessageComposeActivity.edtTransfer);
		}
		(this.imbContact = (ImageView) this.findViewById(R.id.contact)).setOnClickListener((View.OnClickListener) this);
		final Intent intent = this.getIntent();
		if (intent.getExtras() != null) {
			this.mbody = (String) intent.getExtras().get("body");
			if (intent.getExtras().get("body") != null) {
				this.edtInputMsg.setText((CharSequence) this.mbody);
			}
		}
		this.edtInputMsg.addTextChangedListener((TextWatcher) new TextWatcher() {
			public void afterTextChanged(final Editable editable) {
				MessageComposeActivity.edtTransfer = MessageComposeActivity.this.edtInputMsger.getText().toString();
			}

			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}

			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
				// TODO
			}
		});
		this.edtInputMsger.addTextChangedListener((TextWatcher) new TextWatcher() {
			public void afterTextChanged(final Editable editable) {
				if (!TextUtils.isEmpty((CharSequence) editable) && (editable.charAt(0) == ',' || editable.charAt(0) == '\uff0c')) {
					MessageComposeActivity.this.edtInputMsger.setText((CharSequence) "");
				} else {
					if (!TextUtils.isEmpty((CharSequence) editable) && editable.toString().replaceAll("\uff0c", ",").split(",").length > MessageComposeActivity.MAXNUMBER) {
//						Toast.makeText((Context) MessageComposeActivity.this, (CharSequence) ("最多支持" + MessageComposeActivity.MAXNUMBER + "个号码的群发"), 2000L).show();
						MessageComposeActivity.this.edtInputMsger.setText((CharSequence) editable.delete(editable.length() - 2, editable.length() - 1));
						MessageComposeActivity.this.edtInputMsger.setSelection(MessageComposeActivity.this.edtInputMsger.getText().length());
					}
					if (!TextUtils.isEmpty((CharSequence) editable) && (editable.charAt(editable.length() - 1) == ',' || editable.charAt(editable.length() - 1) == '\uff0c') && editable.length() > 2) {
						final char char1 = editable.charAt(editable.length() - 2);
						if (char1 == ',' || char1 == '\uff0c') {
							MessageComposeActivity.this.edtInputMsger.setText((CharSequence) editable.delete(editable.length() - 2, editable.length() - 1));
							MessageComposeActivity.this.edtInputMsger.setSelection(MessageComposeActivity.this.edtInputMsger.getText().length());
						}
					}
				}
			}

			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}

			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
				// TODO
			}
		});
	}

	private void refreshAdapter(final List<Map<String, String>> list, final String s) {
		this.listView.setAdapter((ListAdapter) this.groupMemberAdapter);
		this.listView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				MessageComposeActivity.this.edtInputMsger.setText((CharSequence) MessageComposeActivity.this.groupMemberAdapter.getmMember(n));
				MessageComposeActivity.this.listView.setVisibility(View.GONE);
			}
		});
		this.groupMemberAdapter.notifyDataSetChanged();
	}

	public static File stringToFile(final String s) throws Exception {
		final byte[] decode = Base64.decode(s, 0);
		final File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + File.separator + "rjcache" + File.separator + "chatRecord");
		if (!file.exists()) {
			file.mkdirs();
		}
		return byteToFile(decode, File.createTempFile("recRecord", ".xml", file).getAbsolutePath());
	}

	void dismissPop() {
		if (this.popview == null || !this.popview.isShowing()) {
			return;
		}
		while (true) {
			try {
				this.popview.dismiss();
				this.isshowing = false;
			} catch (Exception ex) {
				continue;
			}
			break;
		}
	}

	public String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			return null;
		}
	}

	protected void onActivityResult(final int n, final int n2, final Intent intent) {
		if (n == 0 && n2 == 0 && intent != null) {
			intent.getStringExtra("name");
			String s = intent.getStringExtra("number");
			final String string = this.edtInputMsger.getText().toString();
			if (!TextUtils.isEmpty((CharSequence) string)) {
				if (string.lastIndexOf(",") == string.length() - 1 || string.lastIndexOf("\uff0c") == string.length() - 1) {
					s = String.valueOf(string) + s;
				} else {
					s = String.valueOf(string) + "," + s;
				}
			}
			final String replaceAll = s.replaceAll("\uff0c", ",");
			this.edtInputMsger.setText((CharSequence) replaceAll);
			this.edtInputMsger.setSelection(replaceAll.length());
		}
		super.onActivityResult(n, n2, intent);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.btnSendMsg: {
				if (!this.isContent || !NetChecker.check((Context) this, true)) {
					break;
				}
				this.bodyValue = this.edtInputMsg.getText().toString();
				this.toValue = this.edtInputMsger.getText().toString();
				if (this.toValue.contains("#") || this.toValue.contains("*")) {
					MyToast.showToast(true, this.context, this.context.getResources().getString(R.string.invalid_char));
					return;
				}
				this.toValue = this.toValue.replaceAll("\uff0c", ",");
				if (this.toValue.charAt(this.toValue.length() - 1) == ',') {
					this.toValue = this.toValue.substring(0, this.toValue.length() - 1);
				}
				if (!this.toValue.contains(",") && this.toValue.equals(MemoryMg.getInstance().TerminalNum)) {
//					Toast.makeText((Context) this, (CharSequence) "不能给自己发短信", 1500L).show();
					this.edtInputMsger.setText((CharSequence) "");
					return;
				}
				this.toValue = this.toValue.trim().replaceAll(String.valueOf(MemoryMg.getInstance().TerminalNum) + ",", "");
				this.toValue = this.toValue.trim().replaceAll(MemoryMg.getInstance().TerminalNum, "");
				if (this.toValue.charAt(this.toValue.length() - 1) == ',') {
					this.toValue = this.toValue.substring(0, this.toValue.length() - 1);
				}
				final Intent intent = new Intent(this.context, (Class) MessageDialogueActivity.class);
				intent.putExtra("userName", this.toValue);
				intent.putExtra("address", this.toValue);
				intent.putExtra("0", "compose");
				intent.putExtra("head", "members=");
				intent.putExtra("bodyValue", this.bodyValue);
				intent.putExtra("toValue", this.toValue);
				MessageComposeActivity.edtTransfer = "";
				this.startActivity(intent);
				this.finish();
			}
			case R.id.btnselectmsg: {
				if (this.isshowing) {
					this.dismissPop();
				}
				this.showMessagePopWindow(view);
			}
			case R.id.contact: {
				final Intent intent2 = new Intent((Context) this, (Class) MessageToContact.class);
				intent2.putExtra("intentActivity", "MessageComposeActivity");
				this.startActivityForResult(intent2, 0);
				MessageComposeActivity.edtTransfer = this.edtInputMsg.getText().toString();
			}
		}
	}

	@Override
	protected void onCreate(Bundle extras) {
		super.onCreate(extras);
		this.getWindow().setSoftInputMode(16);
		this.setContentView(R.layout.activity_new_message);
		this.listView = (ListView) this.findViewById(R.id.listview1);
		this.dbService = DataBaseService.getInstance();
		this.init();
		this.isdown = false;
		this.btnSendMsg.setOnClickListener((View.OnClickListener) this);
		this.btnSelectMsg.setOnClickListener((View.OnClickListener) this);
		extras = this.getIntent().getExtras();
		if (extras != null) {
			this.userNum = extras.getString("number");
			this.userName = extras.getString("name");
			if (this.userNum != null) {
				this.edtInputMsger.setText((CharSequence) this.userNum);
			}
		}
		if (this.edtInputMsger.getText().toString().length() > 0 && this.edtInputMsg.getText().length() > 0) {
			this.isContent = true;
			this.btnSendMsg.setTextColor(this.getResources().getColor(R.color.tab_wihte));
		} else {
			this.isContent = false;
			this.btnSendMsg.setTextColor(this.getResources().getColor(R.color.disable_color));
		}
		(this.btn_home_message2 = this.findViewById(R.id.btn_home_message2)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				MessageComposeActivity.this.finish();
			}
		});
		this.btn_home_message2.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) MessageComposeActivity.this.findViewById(R.id.photo_sent_home3);
				final TextView textView2 = (TextView) MessageComposeActivity.this.findViewById(R.id.left_photo3);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						MessageComposeActivity.this.btn_home_message2.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(MessageComposeActivity.this.getResources().getColor(R.color.font_color3));
						MessageComposeActivity.this.btn_home_message2.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onStart() {
		this.listView.setVisibility(View.GONE);
		super.onStart();
	}

	void showMessagePopWindow(final View view) {
		if (this.popview == null) {
			this.messageView = View.inflate(this.context, R.layout.message_list, (ViewGroup) null);
			this.messageList = (ListView) this.messageView.findViewById(R.id.messagelistview);
			this.strings = DataBaseService.getInstance().getAllMessages();
			this.messageList.setAdapter((ListAdapter) new MessageListAdapter(this.context, this.strings));
			this.popview = new PopupWindow(this.messageView, -1, 350);
		}
		this.popview.setFocusable(true);
		this.popview.setOutsideTouchable(false);
		this.popview.setTouchable(true);
		this.popview.setBackgroundDrawable((Drawable) new BitmapDrawable());
		Log.i("coder", "xPos:" + (((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() / 2 - this.popview.getWidth() / 2));
		final int[] array = new int[2];
		view.getLocationOnScreen(array);
		this.popview.showAtLocation(view, 0, array[0], array[1] - this.popview.getHeight() - 20);
		this.messageList.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				MessageComposeActivity.this.edtInputMsg.setText((CharSequence) MessageComposeActivity.this.strings.get(n));
				MessageComposeActivity.this.dismissPop();
			}
		});
		this.isshowing = true;
	}

	public class GroupMemberAdapter extends BaseAdapter {
		private Context mContext;
		private List<Map<String, String>> mList;
		private String search_word;

		public GroupMemberAdapter(final Context mContext, final List<Map<String, String>> mList) {
			this.mList = new ArrayList<Map<String, String>>();
			this.mContext = mContext;
			this.mList = mList;
			this.search_word = "";
		}

		private CharSequence getHighLightText(final String s, final String s2) {
			final int index = s.toLowerCase().indexOf(s2.toLowerCase());
			final int length = s2.length();
			return (CharSequence) Html.fromHtml(String.valueOf(s.substring(0, index)) + "<u><font color=#FF0000>" + s.substring(index, index + length) + "</font></u>" + s.substring(index + length, s.length()));
		}

		public int getCount() {
			if (this.mList != null) {
				return this.mList.size();
			}
			return 0;
		}

		public Object getItem(final int n) {
			return this.mList.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			ViewHolder tag;
			if (inflate == null) {
				tag = new ViewHolder();
				inflate = LayoutInflater.from(this.mContext).inflate(R.layout.contact_member_item, (ViewGroup) null);
				// TODO
				tag.grp_img.setVisibility(View.GONE);
				tag.grp_uDept.setVisibility(View.GONE);
				inflate.setTag((Object) tag);
			} else {
				tag = (ViewHolder) inflate.getTag();
			}
			final Map<String, String> map = this.mList.get(n);
			final String text = map.get("mname");
			final String text2 = map.get("number");
			if (text != null && text.toLowerCase().contains(this.search_word.toLowerCase())) {
				tag.grp_uName.setText(this.getHighLightText(text, this.search_word));
			} else {
				tag.grp_uName.setText((CharSequence) text);
			}
			if (text2 != null && text2.contains(this.search_word)) {
				tag.grp_uNumber.setText(this.getHighLightText(text2, this.search_word));
				return inflate;
			}
			tag.grp_uNumber.setText((CharSequence) text2);
			return inflate;
		}

		public String getmMember(final int n) {
			return this.mList.get(n).get("number");
		}
	}

	private static class ViewHolder {
		private CheckBox grp_img;
		private TextView grp_uDept;
		private TextView grp_uName;
		private TextView grp_uNumber;

		private ViewHolder() {
		}
	}
}
