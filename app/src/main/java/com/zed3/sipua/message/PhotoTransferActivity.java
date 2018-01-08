package com.zed3.sipua.message;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.location.GpsTools;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.DensityUtil;
import com.zed3.utils.ExifWriter;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PhotoTransferActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
	private static final int MAX_RECT = 800;
	private static PopupWindow Setting_Transfer_View;
	private static SharedPreferences mSharedPreferences;
	private int CHOOSE_PICTURE;
	String E_id;
	private View PopupView;
	private int Presult;
	private BroadcastReceiver Rece;
	private String Spell;
	private String TAG;
	private int TAKE_PICTURE;
	private ImageView action_imv;
	private String bodyValue;
	private LinearLayout bottomLayout;
	CheckBox cb_original;
	private String companyId;
	private DataBaseService dbService;
	private Dialog dialog;
	private int flag;
	private GroupMemberAdapter groupMemberAdapter;
	private String imageFilePathCompressed;
	private String imageFilePathOriginal;
	private Uri imageFileUriCompressed;
	private Uri imageFileUriOriginal;
	private ImageView imbContact;
	boolean isOriginalPic;
	private boolean isSendMode;
	private ImageView keyboard_img;
	private ListView listView;
	private Context mContext;
	private IntentFilter mFilter;
	private Intent mIntent;
	private View mRootView;
	private List<Member> member;
	private TextView photo_send;
	private TextView photo_send_cancel;
	private ImageView popTextPoint;
	private int returncode;
	private ScaleAnimation sa;
	private int tag;
	private String toValue;
	private EditText transfer_edit_content;
	private EditText transfer_edit_num;
	private ImageView txtPoint;
	private String userName;
	private String userNum;

	static {
		PhotoTransferActivity.Setting_Transfer_View = null;
	}

	public PhotoTransferActivity() {
		this.TAG = "PhotoTransferActivity";
		this.isSendMode = false;
		this.CHOOSE_PICTURE = 88;
		this.TAKE_PICTURE = 888;
		this.tag = 1;
		this.isOriginalPic = false;
		this.Rece = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final String action = intent.getAction();
				if ("com.zed3.action.READ_MMS".equalsIgnoreCase(action)) {
					PhotoTransferActivity.this.initPhotoMsg();
				}
				if ("com.zed3.action.RECEIVE_MMS".equalsIgnoreCase(action)) {
					PhotoTransferActivity.this.initPhotoMsg();
				}
			}
		};
	}

	private void AttributeCopy(final String s, final Uri uri) {
		final String realFilePath = getRealFilePath((Context) this, uri);
		try {
			final ExifInterface exifInterface = new ExifInterface(realFilePath);
			final String attribute = exifInterface.getAttribute("Flash");
			final String attribute2 = exifInterface.getAttribute("GPSLatitude");
			final String attribute3 = exifInterface.getAttribute("GPSLongitudeRef");
			final String attribute4 = exifInterface.getAttribute("GPSLatitudeRef");
			final String attribute5 = exifInterface.getAttribute("GPSLongitude");
			final String attribute6 = exifInterface.getAttribute("Make");
			final ExifWriter create = ExifWriter.create(s, (ExifWriter.OnExifWriteListener) new ExifWriter.OnExifWriteListener() {
				@Override
				public void onCompleted() {
				}

				@Override
				public void onException() {
					Toast.makeText(PhotoTransferActivity.this.getApplicationContext(), (CharSequence) "write gps exif exception 4", Toast.LENGTH_LONG).show();
				}
			});
			if (!TextUtils.isEmpty((CharSequence) attribute) && !"8080".equals(attribute)) {
				if (!TextUtils.isEmpty((CharSequence) attribute2)) {
					create.setAttribute("GPSLatitude", attribute2);
				}
				if (!TextUtils.isEmpty((CharSequence) attribute3)) {
					create.setAttribute("GPSLongitudeRef", attribute3);
				}
				if (!TextUtils.isEmpty((CharSequence) attribute4)) {
					create.setAttribute("GPSLatitudeRef", attribute4);
				}
				if (!TextUtils.isEmpty((CharSequence) attribute5)) {
					create.setAttribute("GPSLongitude", attribute5);
				}
				if (!TextUtils.isEmpty((CharSequence) attribute6)) {
					create.setAttribute("Make", attribute6);
				}
			} else {
				create.setAttribute("Flash", "8080");
			}
			create.startWrite();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private String currentUserSection() {
		return DataBaseService.getInstance().getPid(Settings.getUserName());
	}

	private void dismissPop() {
		if (PhotoTransferActivity.Setting_Transfer_View != null) {
			PhotoTransferActivity.Setting_Transfer_View.dismiss();
		}
	}

	private void getCompanyId(final String companyId) {
		if (!this.dbService.isNoPid(companyId)) {
			this.getCompanyId(this.dbService.getId(companyId));
			return;
		}
		this.companyId = companyId;
	}

	public static String getRealFilePath(final Context context, final Uri uri) {
		if (uri == null) {
			return null;
		}
		final String scheme = uri.getScheme();
		final String s = null;
		final String s2 = null;
		String s3;
		if (scheme == null) {
			s3 = uri.getPath();
		} else if ("file".equals(scheme)) {
			s3 = uri.getPath();
		} else {
			s3 = s2;
			if ("content".equals(scheme)) {
				final Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, (String) null, (String[]) null, (String) null);
				s3 = s2;
				if (query != null) {
					s3 = s;
					if (query.moveToFirst()) {
						final int columnIndex = query.getColumnIndex("_data");
						s3 = s;
						if (columnIndex > -1) {
							s3 = query.getString(columnIndex);
						}
					}
					query.close();
				}
			}
		}
		return s3;
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

	private void initPhotoMsg() {
		MyLog.i(this.TAG, "initPhotoMsg");
//		new PhotoMsgTask((PhotoMsgTask) null).execute((Object[]) new Void[0]);
	}

	private boolean isSDCard() {
		return Environment.getExternalStorageState().equals("mounted");
	}

	private void refreshAdapter(final List<Member> list, final String s) {
		if (list.size() != 0) {
			this.listView.setAdapter((ListAdapter) this.groupMemberAdapter);
		} else {
			this.listView.setVisibility(View.GONE);
		}
		this.listView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				PhotoTransferActivity.this.transfer_edit_num.setText((CharSequence) PhotoTransferActivity.this.groupMemberAdapter.getmMember(n));
				PhotoTransferActivity.this.listView.setVisibility(View.GONE);
			}
		});
		this.groupMemberAdapter.notifyDataSetChanged();
	}

	private void showPop() {
		this.PopupView = View.inflate(this.mContext, R.layout.photo_fun_list, (ViewGroup) null);
		PhotoTransferActivity.Setting_Transfer_View = new PopupWindow(this.PopupView, DensityUtil.dip2px(this.mContext, 100.0f), DensityUtil.dip2px(this.mContext, 120.0f));
		this.popTextPoint = (ImageView) this.PopupView.findViewById(R.id.msgpoint_photo_pop_R);
		this.PopupView.findViewById(R.id.popup_send).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				PhotoTransferActivity.this.dismissPop();
				PhotoTransferActivity.this.startActivity(new Intent(PhotoTransferActivity.this.mContext, (Class) PhotoTransferSentActivity.class));
			}
		});
		this.PopupView.findViewById(R.id.popup_receive).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				PhotoTransferActivity.this.dismissPop();
				PhotoTransferActivity.this.startActivity(new Intent(PhotoTransferActivity.this.mContext, (Class) PhotoTransferReceiveActivity.class));
			}
		});
		PhotoTransferActivity.Setting_Transfer_View.setOutsideTouchable(true);
		PhotoTransferActivity.Setting_Transfer_View.setFocusable(true);
		PhotoTransferActivity.Setting_Transfer_View.setBackgroundDrawable((Drawable) new BitmapDrawable());
		PhotoTransferActivity.Setting_Transfer_View.setBackgroundDrawable((Drawable) new ColorDrawable(0));
		(this.sa = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 0.1f)).setDuration(200L);
		PhotoTransferActivity.Setting_Transfer_View.showAsDropDown(this.findViewById(R.id.li_photo), ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() - PhotoTransferActivity.Setting_Transfer_View.getWidth() - 15, 0);
		if (this.Presult > 0) {
			this.popTextPoint.setVisibility(View.VISIBLE);
			return;
		}
		this.popTextPoint.setVisibility(View.INVISIBLE);
	}

	private void writeGpsExif(String s) {
		final ExifWriter create = ExifWriter.create(s, (ExifWriter.OnExifWriteListener) new ExifWriter.OnExifWriteListener() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onException() {
				Toast.makeText(PhotoTransferActivity.this.getApplicationContext(), (CharSequence) "write gps exif exception", Toast.LENGTH_LONG).show();
			}
		});
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		final double lastLatitude = SipUAApp.getLastLatitude();
		final double lastLongitude = SipUAApp.getLastLongitude();
		final StringBuilder sb = new StringBuilder("writeGpsExif()");
		if (lastLatitude != 0.0 && lastLongitude != 0.0) {
			final int getGPSX = GpsTools.GetGPSX(lastLatitude);
			final int getGPSY = GpsTools.GetGPSY(lastLongitude);
			final double x = GpsTools.getX(getGPSX);
			final double y = GpsTools.getY(getGPSY);
			sb.append(" [" + x + "," + y + "]");
			final ExifWriter setAttribute = create.setAttribute("GPSLatitude", ExifWriter.convertGps(x));
			if (y > 0.0) {
				s = "E";
			} else {
				s = "W";
			}
			final ExifWriter setAttribute2 = setAttribute.setAttribute("GPSLongitudeRef", s);
			if (x > 0.0) {
				s = "N";
			} else {
				s = "S";
			}
			setAttribute2.setAttribute("GPSLatitudeRef", s).setAttribute("GPSLongitude", ExifWriter.convertGps(y));
		} else {
			sb.append(" emty");
			create.setAttribute("Flash", "8080");
		}
		LogUtil.makeLog(this.TAG, "GIS-20150109" + sb.toString());
		MyLog.i("dd", "DeviceInfo.GPS_REMOTE===" + DeviceInfo.GPS_REMOTE);
		final int currentGpsMode = Tools.getCurrentGpsMode();
		MyLog.i("dd", "mode===" + currentGpsMode);
		if (currentGpsMode == 1 || currentGpsMode == 2) {
			s = String.valueOf(autoConfigManager.fetchLocalUserName()) + "-Baidu";
		} else {
			s = autoConfigManager.fetchLocalUserName();
		}
		create.setAttribute("Make", s).startWrite();
	}

	public String getE_id() {
		return UUID.randomUUID().toString().trim().replaceAll("-", "");
	}

	protected void onActivityResult(final int returncode, final int n, final Intent intent) {
		this.returncode = returncode;
		// TODO
	}

	public void onCheckedChanged(final CompoundButton compoundButton, final boolean isOriginalPic) {
		this.isOriginalPic = isOriginalPic;
		int buttonDrawable;
		if (isOriginalPic) {
			buttonDrawable = R.drawable.select_on;
		} else {
			buttonDrawable = R.drawable.select_off;
		}
		compoundButton.setButtonDrawable(buttonDrawable);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.photo_send_cancel: {
				this.transfer_edit_content.setText((CharSequence) "");
				this.action_imv.setImageResource(R.drawable.aa_photo_selector);
				this.isSendMode = false;
				this.cb_original.setVisibility(View.INVISIBLE);
			}
			case R.id.action_imv: {
				if (!this.isSDCard()) {
					MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.sd_notify));
				}
				(this.dialog = new MyDialog(this.mContext, R.style.MyDialog)).show();
				this.dialog.setCanceledOnTouchOutside(true);
			}
			case R.id.photo_send: {
				if (!this.isSDCard()) {
					MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.sd_notify));
				}
				if (!this.isSendMode) {
					MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.upload_notify_1));
					return;
				}
				this.toValue = this.transfer_edit_num.getText().toString().trim();
				this.bodyValue = this.transfer_edit_content.getText().toString().trim();
				if (this.toValue == null || this.toValue.length() == 0) {
					MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.enter_ds_number));
					return;
				}
				int length = this.toValue.length();
				int n;
				do {
					n = length - 1;
					if (n < 0) {
						if (!Pattern.compile("[0-9]*").matcher(this.toValue).matches()) {
							MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.enter_yz_title));
							return;
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								Uri uri;
								if (PhotoTransferActivity.this.isOriginalPic) {
									uri = PhotoTransferActivity.this.imageFileUriOriginal;
								} else {
									uri = PhotoTransferActivity.this.imageFileUriCompressed;
								}
								new MessageSender(PhotoTransferActivity.this.mContext, PhotoTransferActivity.this.toValue, PhotoTransferActivity.this.bodyValue, uri, "image/jpg", String.valueOf(PhotoTransferActivity.this.E_id.substring(3, 12)) + ".jpg", PhotoTransferActivity.this.E_id).sendMultiMessage();
							}
						}).start();
						this.transfer_edit_content.setText((CharSequence) "");
						this.action_imv.setImageResource(R.drawable.aa_photo_selector);
						this.isSendMode = false;
						this.cb_original.setChecked(false);
						this.cb_original.setVisibility(View.INVISIBLE);
						return;
					} else {
						length = n;
					}
				} while (Character.isLetterOrDigit(this.toValue.charAt(n)));
				MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.enter_yz_title));
			}
			case R.id.keyboard_img: {
				this.showPop();
			}
			case R.id.contact: {
				final Intent intent = new Intent((Context) this, (Class) MessageToContact.class);
				intent.putExtra("intentActivity", "PhotoTransferActivity");
				this.startActivityForResult(intent, 0);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
//		this.setContentView(this.mRootView = this.getLayoutInflater().inflate(R.layout.aa_photo_transfer, (ViewGroup) null));
//		this.mContext = (Context) this;
//		(this.mFilter = new IntentFilter()).addAction(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
//		this.mFilter.addAction(MainActivity.READ_MESSAGE);
//		this.mFilter.addAction("com.zed3.action.READ_MMS");
//		this.mFilter.addAction("com.zed3.action.RECEIVE_MMS");
//		this.mFilter.addAction("com.zed3.sipua_clear_missedcall");
//		this.mFilter.addAction("com.zed3.sipua_callhistory_changed");
//		this.mFilter.addAction(MainActivity.ACTION_UI_REFRESH);
//		this.mContext.registerReceiver(this.Rece, this.mFilter);
//		this.mRootView.setOnClickListener((View.OnClickListener) this);
//		PhotoTransferActivity.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
//		this.mContext = (Context) this;
//		this.dbService = DataBaseService.getInstance();
//		(this.transfer_edit_content = (EditText) this.findViewById(R.id.transfer_edit_content)).setOnClickListener((View.OnClickListener) this);
//		this.transfer_edit_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
//		this.transfer_edit_content.setOnFocusChangeListener((View.OnFocusChangeListener) new View.OnFocusChangeListener() {
//			public void onFocusChange(final View view, final boolean b) {
//				if (b) {
//					MainActivity.dismissPopupWindow();
//				}
//			}
//		});
//		(this.cb_original = (CheckBox) this.findViewById(R.id.original)).setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
//		this.cb_original.setVisibility(View.INVISIBLE);
//		(this.action_imv = (ImageView) this.findViewById(R.id.action_imv)).setOnClickListener((View.OnClickListener) this);
//		(this.imbContact = (ImageView) this.findViewById(R.id.contact)).setOnClickListener((View.OnClickListener) this);
//		(this.photo_send = (TextView) this.findViewById(R.id.photo_send)).setOnClickListener((View.OnClickListener) this);
//		(this.photo_send_cancel = (TextView) this.findViewById(R.id.photo_send_cancel)).setOnClickListener((View.OnClickListener) this);
//		(this.keyboard_img = (ImageView) this.findViewById(R.id.keyboard_img)).setOnClickListener((View.OnClickListener) this);
//		this.transfer_edit_num = (EditText) this.findViewById(R.id.transfer_edit_num);
//		this.bottomLayout = (LinearLayout) this.findViewById(R.id.paichuan);
//		this.listView = (ListView) this.findViewById(R.id.list_view);
//		if (!DeviceInfo.defaultrecnum.equals("")) {
//			this.transfer_edit_num.setText((CharSequence) DeviceInfo.defaultrecnum);
//		}
//		this.transfer_edit_num.addTextChangedListener((TextWatcher) new TextWatcher() {
//			public void afterTextChanged(final Editable editable) {
//			}
//
//			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
//			}
//
//			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
//				final String string = charSequence.toString();
//				final String companyShowflag = PhotoTransferActivity.this.dbService.getCompanyShowflag();
//				if (!TextUtils.isEmpty((CharSequence) string) && !"".equals(string)) {
//					// TODO
//				}
//				PhotoTransferActivity.this.listView.setVisibility(View.GONE);
//			}
//		});
//		this.transfer_edit_num.setOnFocusChangeListener((View.OnFocusChangeListener) new View.OnFocusChangeListener() {
//			public void onFocusChange(final View view, final boolean b) {
//				if (b) {
//					MainActivity.dismissPopupWindow();
//				}
//			}
//		});
//		this.mIntent = this.getIntent();
//		if (this.mIntent != null && this.mIntent.getStringExtra("action") != null) {
//			if (this.mIntent.getStringExtra("action").equals("resend")) {
//				// TODO
//			} else if (this.mIntent.getStringExtra("action").equals("fastMMS")) {
//				final String stringExtra = this.mIntent.getStringExtra("address");
//				this.listView.setVisibility(View.GONE);
//				this.transfer_edit_num.setText((CharSequence) stringExtra);
//				this.transfer_edit_content.requestFocus();
//			}
//		}
//		this.txtPoint = (ImageView) this.findViewById(R.id.msgpoint_photoR);
//		this.initPhotoMsg();
//		super.onCreate(bundle);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onNewIntent(final Intent intent) {
		if (intent != null && "fastMMS".equals(intent.getStringExtra("action"))) {
			this.transfer_edit_num.setText((CharSequence) intent.getStringExtra("address"));
			this.listView.setVisibility(View.GONE);
			this.transfer_edit_content.requestFocus();
		}
		super.onNewIntent(intent);
	}

	public boolean onOptionsItemSelected(final MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case 1: {
				Tools.exitApp((Context) this);
				this.finish();
				break;
			}
		}
		return super.onOptionsItemSelected(menuItem);
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onStart() {
		this.listView.setVisibility(View.GONE);
		super.onStart();
	}

	protected void onStop() {
		super.onStop();
	}

	public class GroupMemberAdapter extends BaseAdapter {
		private Context mContext;
		private List<Member> mList;
		private String search_word;

		public GroupMemberAdapter(final Context mContext, final List<Member> mList) {
			this.mList = new ArrayList<Member>();
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

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(this.mContext).inflate(R.layout.contact_member_item, null);
				viewHolder.grp_img = (CheckBox) convertView.findViewById(R.id.grp_img);
				viewHolder.grp_uName = (TextView) convertView.findViewById(R.id.grp_uName);
				viewHolder.grp_uNumber = (TextView) convertView.findViewById(R.id.grp_uNumber);
				viewHolder.grp_uDept = (TextView) convertView.findViewById(R.id.grp_uDept);
				viewHolder.grp_img.setVisibility(View.GONE);
				viewHolder.grp_uDept.setVisibility(View.GONE);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			Member groupInfoItem = (Member) this.mList.get(position);
			String name = groupInfoItem.getmName();
			String number = groupInfoItem.getNumber();
			if (name == null || !name.toLowerCase().contains(this.search_word.toLowerCase())) {
				viewHolder.grp_uName.setText(name);
			} else {
				viewHolder.grp_uName.setText(getHighLightText(name, this.search_word));
			}
			if (number == null || !number.contains(this.search_word)) {
				viewHolder.grp_uNumber.setText(number);
			} else {
				viewHolder.grp_uNumber.setText(getHighLightText(number, this.search_word));
			}
			return convertView;
		}

		public String getmMember(final int n) {
			return this.mList.get(n).getNumber();
		}
	}

	public class MyDialog extends Dialog implements View.OnClickListener {
		Context context;
		TextView tv_1;
		TextView tv_2;

		public MyDialog(final Context context) {
			super(context);
			this.context = context;
		}

		public MyDialog(final Context context, final int n) {
			super(context, n);
			this.context = context;
		}

		public void onClick(final View view) {
			// TODO
			if (view.getId() == R.id.take_photo) {
				if (PhotoTransferActivity.this.dialog != null) {
					PhotoTransferActivity.this.dialog.dismiss();
				}
				final File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/smsmms");
				if (!file.exists()) {
					file.mkdirs();
				}
				PhotoTransferActivity.this.E_id = PhotoTransferActivity.this.getE_id();
				final Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra("output", (Parcelable) PhotoTransferActivity.this.imageFileUriOriginal);
				((Activity) PhotoTransferActivity.this.mContext).startActivityForResult(intent, PhotoTransferActivity.this.TAKE_PICTURE);
			} else if (view.getId() == R.id.take_picture) {
				if (PhotoTransferActivity.this.dialog != null) {
					PhotoTransferActivity.this.dialog.dismiss();
				}
				PhotoTransferActivity.this.E_id = PhotoTransferActivity.this.getE_id();
				PhotoTransferActivity.this.startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PhotoTransferActivity.this.CHOOSE_PICTURE);
			}
		}

		protected void onCreate(final Bundle bundle) {
			super.onCreate(bundle);
			this.setContentView(R.layout.aa_dialog);
			this.tv_1 = (TextView) this.findViewById(R.id.take_photo);
			this.tv_2 = (TextView) this.findViewById(R.id.take_picture);
			this.tv_1.setOnClickListener((View.OnClickListener) this);
			this.tv_2.setOnClickListener((View.OnClickListener) this);
		}
	}

	private final class PhotoMsgTask extends AsyncTask<Void, Integer, Integer> {
		protected Integer doInBackground(Void... array) {
			final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(PhotoTransferActivity.this.mContext);
			int n = 0;
			final Void[] array2 = null;
			Object mQuery;
			final Object o = mQuery = null;
			array = array2;
			try {
				final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
				mQuery = o;
				array = array2;
				final String fetchLocalServer = autoConfigManager.fetchLocalServer();
				mQuery = o;
				array = array2;
				final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
				mQuery = o;
				array = array2;
				final Object o2 = array = (Void[]) (mQuery = smsMmsDatabase.mQuery("message_talk", "type = 'mms' and mark = 0 and status = 0 and server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'", null, null));
				int count;
				n = (count = ((Cursor) o2).getCount());
				if (o2 != null) {
					((Cursor) o2).close();
					count = n;
				}
				return count;
			} catch (Exception ex) {
				array = (Void[]) mQuery;
				MyLog.e("PhotoMsgTask", "query table message_talk error:" + ex.toString());
				int count = n;
				if (mQuery != null) {
					((Cursor) mQuery).close();
					count = n;
					return count;
				}
				return count;
			} finally {
				if (array != null) {
					((Cursor) (Object) array).close();
				}
			}
		}

		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result > 0) {
				PhotoTransferActivity.this.txtPoint.setVisibility(View.VISIBLE);
				return;
			}
			PhotoTransferActivity.this.txtPoint.setVisibility(View.INVISIBLE);
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
