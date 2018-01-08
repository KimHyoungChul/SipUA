package com.zed3.sipua.ui.lowsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

import org.zoolu.tools.GroupListInfo;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SipdroidActivity extends BaseActivity implements DialogInterface.OnDismissListener, View.OnClickListener, View.OnLongClickListener {
	public static final String COLOR_LIGHT = "#FFBDBDBD";
	public static final String NULLSTR = "--";
	private static final int TONE_LENGTH_MS = 150;
	private static Activity mContext;
	public static final HashMap<Character, Integer> mToneMap;
	public static final boolean market = false;
	private static PopupWindow menuPopupWindow;
	private static PopupWindow popupWindow;
	public static final boolean release = true;
	private String CurGrpID;
	private AutoConfigManager acm;
	MyAdapter adpter;
	private String audio;
	ImageButton back_btn;
	private LinearLayout bottomLayout;
	private ImageButton btn0;
	private ImageButton btnShowKeyboard;
	private ImageButton btndel;
	private ImageButton btndialy;
	private ImageButton btnenight;
	private ImageButton btnfive;
	private ImageButton btnfour;
	private ImageButton btnjing;
	private ImageButton btnmi;
	private ImageButton btnnine;
	private ImageButton btnone;
	private ImageButton btnseven;
	private ImageButton btnsix;
	private ImageButton btnthree;
	private ImageButton btntwo;
	private ListView callHistoryListView;
	private View callHistoryOverView;
	private View callHistoryView;
	private String callNum;
	private String contactName;
	private View currentClickedView;
	private CallHistoryDatabase db;
	private boolean isEditMode;
	private boolean isFirstResume;
	private boolean isKeyBoardHided;
	boolean isKeyboard;
	protected boolean isResumed;
	private boolean isUserHideKeyboard;
	private boolean isUserShowKeyboard;
	protected boolean isWaitingForNewData;
	private ScaleAnimation keyBoardHideSA;
	private ScaleAnimation keyBoardShowSA;
	BroadcastReceiver keyboardReceiver;
	private View keyboardView;
	ImageView keyboard_img;
	protected String mClickedItemName;
	public String mClickedItemNumber;
	private Cursor mCursor;
	private boolean mDTMFToneEnabled;
	private IntentFilter mFilter;
	Handler mHandle;
	private String mIntoString;
	protected String mNumber;
	protected int mPosition;
	private View mRootView;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock;
	private Member mem;
	private boolean needShowKeyBoardOnResume;
	private String num;
	private EditText numTxt;
	protected boolean numTxtCursor;
	private String numberString;
	protected String numberViewText;
	private SearchView.OnQueryTextListener oqtl;
	private LinearLayout popup_video;
	BroadcastReceiver refreshlistReceiver;
	protected int scrollCount;
	private SearchView searchView;
	private String tag;
	private ArrayList<String> tempGrpList;
	private String type;
	private ImageButton videoCall;

	static {
		mToneMap = new HashMap<Character, Integer>();
		SipdroidActivity.menuPopupWindow = null;
	}

	public SipdroidActivity() {
		this.numTxt = null;
		this.btnone = null;
		this.btntwo = null;
		this.btnthree = null;
		this.btnfour = null;
		this.btnfive = null;
		this.btnsix = null;
		this.btnseven = null;
		this.btnenight = null;
		this.btnnine = null;
		this.btn0 = null;
		this.btnmi = null;
		this.btnjing = null;
		this.btndialy = null;
		this.btndel = null;
		this.CurGrpID = "";
		this.mToneGeneratorLock = new Object();
		this.mIntoString = "";
		this.scrollCount = 0;
		this.tag = "SipdroidActivity";
		this.adpter = null;
		this.tempGrpList = new ArrayList<String>();
		this.isKeyboard = false;
		this.oqtl = (SearchView.OnQueryTextListener) new SearchView.OnQueryTextListener() {
			public boolean onQueryTextChange(final String s) {
//				SipdroidActivity.access .0 (SipdroidActivity.this, s);
				if (!TextUtils.isEmpty((CharSequence) s)) {
					SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB(s)));
				} else {
					SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB()));
				}
				return false;
			}

			public boolean onQueryTextSubmit(final String s) {
				((InputMethodManager) SipdroidActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SipdroidActivity.this.getCurrentFocus().getWindowToken(), 2);
				return false;
			}
		};
		this.isFirstResume = true;
		this.mHandle = new Handler() {
			public void handleMessage(final Message message) {
				if (message.what == 1) {
					final List list = (List) message.obj;
					if (SipdroidActivity.this.adpter != null) {
						SipdroidActivity.this.adpter.refreshListView(list);
						SipdroidActivity.this.adpter.notifyDataSetChanged();
						return;
					}
					SipdroidActivity.this.adpter = new MyAdapter((Context) SipdroidActivity.mContext, list);
					SipdroidActivity.this.callHistoryListView.setAdapter((ListAdapter) SipdroidActivity.this.adpter);
				}
			}
		};
		this.refreshlistReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB()));
						} catch (Exception ex) {
							MyLog.e(SipdroidActivity.this.tag, ex.toString());
							ex.printStackTrace();
						}
					}
				}).start();
			}
		};
		this.keyboardReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equalsIgnoreCase("keyboardpopup")) {
					MyLog.i("dd", "1111isKeyboard" + SipdroidActivity.this.isKeyboard);
					if (SipdroidActivity.this.isKeyboard) {
						SipdroidActivity.this.keyboard_img.setImageResource(R.drawable.keyboardup_release);
						SipdroidActivity.this.keyboardView.setVisibility(View.INVISIBLE);
						SipdroidActivity.this.isKeyboard = false;
						return;
					}
					SipdroidActivity.this.keyboardView.setVisibility(View.VISIBLE);
					SipdroidActivity.this.keyboard_img.setImageResource(R.drawable.keyboarddown_release);
					SipdroidActivity.this.isKeyboard = true;
				} else if (intent.getAction().equalsIgnoreCase("keyboardpopupss")) {
					MyLog.i("dd", "444444");
					SipdroidActivity.this.keyboardView.setVisibility(View.INVISIBLE);
					SipdroidActivity.this.keyboard_img.setImageResource(R.drawable.keyboardup_release);
					SipdroidActivity.this.isKeyboard = false;
				}
			}
		};
	}

	private List<Map<String, Object>> GetDataFromDB() {
		return null;
	}

	private List<Map<String, Object>> GetDataFromDB(final String s) {
		return null;
	}

	private void InitCallScreen() {
		SipdroidActivity.mToneMap.put('1', 1);
		SipdroidActivity.mToneMap.put('2', 2);
		SipdroidActivity.mToneMap.put('3', 3);
		SipdroidActivity.mToneMap.put('4', 4);
		SipdroidActivity.mToneMap.put('5', 5);
		SipdroidActivity.mToneMap.put('6', 6);
		SipdroidActivity.mToneMap.put('7', 7);
		SipdroidActivity.mToneMap.put('8', 8);
		SipdroidActivity.mToneMap.put('9', 9);
		SipdroidActivity.mToneMap.put('0', 0);
		SipdroidActivity.mToneMap.put('#', 11);
		SipdroidActivity.mToneMap.put('*', 10);
		SipdroidActivity.mToneMap.put('d', 12);
	}

	private void ShowCurrentGrp() {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null) {
			this.CurGrpID = getCurGrp.grpID;
		}
	}

	private boolean chanageEditMode() {
		if (this.currentClickedView != null) {
			this.currentClickedView.setBackgroundColor(Color.parseColor("#FFBDBDBD"));
		}
		return this.isEditMode = !this.isEditMode;
	}

	private void companyNumber(final String s) {
		if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
			MyToast.showToast(true, (Context) this, R.string.vc_service_not);
			return;
		}
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			if (DeviceInfo.CONFIG_AUDIO_MODE != 1) {
				this.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
				return;
			}
			if (this.audio != null && this.audio.equals("0")) {
				MyToast.showToast(true, (Context) this, R.string.audio_service_not);
				return;
			}
			CallUtil.AudioCall((Context) SipdroidActivity.mContext, s, null, null, null);
		} else {
			if (MemoryMg.getInstance().PhoneType == 1) {
				CallUtil.AudioCall((Context) SipdroidActivity.mContext, s, null, null, null);
				return;
			}
			this.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
		}
	}

	private void delete() {
		final StringBuffer sb = new StringBuffer(this.numTxt.getText().toString().trim());
		int n2;
		StringBuffer sb2;
		if (this.numTxtCursor) {
			final int n = n2 = this.numTxt.getSelectionStart();
			sb2 = sb;
			if (n > 0) {
				sb2 = sb.delete(n - 1, n);
				n2 = n;
			}
		} else {
			final int n3 = n2 = this.numTxt.length();
			sb2 = sb;
			if (n3 > 0) {
				sb2 = sb.delete(n3 - 1, n3);
				n2 = n3;
			}
		}
		this.numTxt.setText((CharSequence) sb2.toString());
		if (n2 > 0) {
			Selection.setSelection((Spannable) this.numTxt.getText(), n2 - 1);
		}
		if (this.numTxt.getText().toString().trim().length() <= 0) {
			this.numTxt.setCursorVisible(false);
			this.numTxtCursor = false;
			this.numTxt.setGravity(19);
		}
	}

	public static boolean dismissMenuPopupWindows() {
		if (SipdroidActivity.menuPopupWindow != null && SipdroidActivity.menuPopupWindow.isShowing()) {
			SipdroidActivity.menuPopupWindow.dismiss();
			return true;
		}
		return false;
	}

	public static void dismissPopupWindows() {
		dismissMenuPopupWindows();
		if (SipdroidActivity.popupWindow != null && SipdroidActivity.popupWindow.isShowing()) {
			SipdroidActivity.popupWindow.dismiss();
		}
	}

	public static int getCurrentWeek() {
		final Calendar instance = Calendar.getInstance();
		instance.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return Integer.parseInt(String.valueOf(instance.get(Calendar.DAY_OF_WEEK)));
	}

	private int getIcon(final String s) {
		int n = R.drawable.person_icon;
		if (s != null) {
			if (Member.UserType.toUserType(s) != Member.UserType.SVP) {
				if (Member.UserType.toUserType(s) != Member.UserType.VIDEO_MONITOR_GB28181) {
					n = n;
					if (Member.UserType.toUserType(s) != Member.UserType.VIDEO_MONITOR_GVS) {
						return n;
					}
				}
				return R.drawable.icon_gvs;
			}
			n = R.drawable.icon_dispatcher;
		}
		return n;
	}

	private Map<String, ArrayList<GroupListInfo>> getPermanentChildrenData(final HashMap<PttGrp, ArrayList<GroupListInfo>> hashMap) {
		final HashMap<String, ArrayList<GroupListInfo>> hashMap2 = new HashMap<String, ArrayList<GroupListInfo>>();
		if (!hashMap.isEmpty()) {
			for (final Map.Entry<PttGrp, ArrayList<GroupListInfo>> entry : hashMap.entrySet()) {
				final PttGrp pttGrp = entry.getKey();
				if (pttGrp != null && pttGrp.getType() == 0) {
					hashMap2.put(pttGrp.getGrpID(), entry.getValue());
				}
			}
		}
		return hashMap2;
	}

	private int getServerListArray() {
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList;
		}
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList1;
		}
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && !DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList2;
		}
		return -1;
	}

	private Spanned getStringText(String s, String s2, final String s3) {
		final Spanned spanned = null;
		final int index = s.toLowerCase().indexOf(s2.toLowerCase());
		final String substring = s.substring(index, s2.length() + index);
		MyLog.i("red", "newtext----" + substring);
		System.out.println(index);
		Spanned fromHtml;
		if (index == 0) {
			if (substring.length() < 2) {
				s = s.substring(substring.length() + index, s.length());
			} else {
				s = s.substring(substring.length(), s.length());
			}
			if (!s3.equals("CallUnak")) {
				return Html.fromHtml("<font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
			}
			fromHtml = Html.fromHtml("<font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
		} else {
			fromHtml = spanned;
			if (index > 0) {
				if (substring.length() < 2 && substring.length() + index < s.length()) {
					s2 = s.substring(0, substring.length() + index - 1);
					s = s.substring(substring.length() + index, s.length());
					if (s3.equals("CallUnak")) {
						return Html.fromHtml("<font color=red>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
					}
					return Html.fromHtml("<font color=balck>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
				} else if (substring.length() + index < s.length()) {
					s2 = s.substring(0, index);
					s = s.substring(substring.length() + index, s.length());
					if (s3.equals("CallUnak")) {
						return Html.fromHtml("<font color=red>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=red>" + s + "</font>");
					}
					return Html.fromHtml("<font color=balck>" + s2 + "</font><font color=red><u>" + substring + "</u></font><font color=balck>" + s + "</font>");
				} else {
					s = s.substring(0, s.length() - substring.length());
					if (s3.equals("CallUnak")) {
						return Html.fromHtml("<font color=red>" + s + "</font><font color=red><u>" + substring + "</u></font>");
					}
					return Html.fromHtml("<font color=balck>" + s + "</font><font color=red><u>" + substring + "</u></font>");
				}
			}
		}
		return fromHtml;
	}

	private static String getStringTime(final long n) {
		if (getTime(6, n) > n && n > getTime(3, n)) {
			return SipUAApp.mContext.getResources().getString(R.string.lingchen);
		}
		if (getTime(8, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.zaochen);
		}
		if (getTime(11, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.shangwu);
		}
		if (getTime(13, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.zhongwu);
		}
		if (getTime(17, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.xiawu);
		}
		if (getTime(19, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.bangwan);
		}
		if (getTime(23, n) > n) {
			return SipUAApp.mContext.getResources().getString(R.string.wanshang);
		}
		return SipUAApp.mContext.getResources().getString(R.string.shenye);
	}

	public static long getTime(final int n, final long timeInMillis) {
		final Calendar instance = Calendar.getInstance();
//		final Calendar instance2 = Calendar.getInstance();
//		instance2.setTimeInMillis(timeInMillis);
//		instance.set(2, instance2.get(2));
//		instance.set(1, instance2.get(1));
//		instance.set(5, instance2.get(5));
//		instance.set(11, n);
//		instance.set(12, 0);
//		instance.set(13, 0);
		return instance.getTimeInMillis();
	}

	public static String getVersion() {
		return getVersion(Receiver.mContext);
	}

	public static String getVersion(final Context context) {
		String versionName;
		if (context == null) {
			versionName = "Unknown";
		} else {
			try {
				final String s = versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				if (s.contains(" + ")) {
					return String.valueOf(s.substring(0, s.indexOf(" + "))) + "b";
				}
			} catch (PackageManager.NameNotFoundException ex) {
				return "Unknown";
			}
		}
		return versionName;
	}

	private void hideKeyboard(final boolean b) {
		if (!b) {
			if (this.keyBoardShowSA == null) {
				(this.keyBoardShowSA = new ScaleAnimation(1.0f, 1.0f, 0.1f, 1.0f)).setDuration(200L);
			}
			this.keyboardView.startAnimation((Animation) this.keyBoardShowSA);
			this.keyboardView.setVisibility(View.VISIBLE);
			this.callHistoryOverView.setVisibility(View.VISIBLE);
			this.isKeyBoardHided = false;
			return;
		}
		if (this.isKeyBoardHided) {
			return;
		}
		if (this.keyBoardHideSA == null) {
			(this.keyBoardHideSA = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.1f)).setDuration(200L);
		}
		this.keyboardView.startAnimation((Animation) this.keyBoardHideSA);
		this.callHistoryOverView.setVisibility(View.INVISIBLE);
		this.keyboardView.setVisibility(View.INVISIBLE);
		this.isKeyBoardHided = true;
	}

	private void initCallHistoryViews() {
		(this.callHistoryListView = (ListView) this.mRootView.findViewById(R.id.call_history_list)).setOnItemLongClickListener((AdapterView.OnItemLongClickListener) new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				new AlertDialog.Builder((Context) SipdroidActivity.this).setTitle(R.string.options_one).setItems(R.array.calllist_longclick, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialogInterface, final int n) {
//						switch (n) {
//							default: {
//							}
//							case 0: {
//								new AlertDialog.Builder((Context) SipdroidActivity.this).setTitle(R.string.delete_member_log).setMessage((CharSequence) SipdroidActivity.this.getResources().getString(R.string.delete_call_notify)).setPositiveButton((CharSequence) SipdroidActivity.this.getResources().getString(R.string.delete_ok), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//									public void onClick(final DialogInterface dialogInterface, final int n) {
//										SipdroidActivity.this.db.delete1(CallHistoryDatabase.Table_Name, "number = ?", new String[]{((Map) SipdroidActivity.this.adpter.getItem(n)).get("number")});
//										SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB()));
//									}
//								}).setNegativeButton((CharSequence) SipdroidActivity.this.getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//									public void onClick(final DialogInterface dialogInterface, final int n) {
//									}
//								}).show();
//							}
//							case 1: {
//								new AlertDialog.Builder((Context) SipdroidActivity.this).setTitle(R.string.delete_all_log).setMessage((CharSequence) SipdroidActivity.this.getResources().getString(R.string.delete_allCall_notify)).setPositiveButton((CharSequence) SipdroidActivity.this.getResources().getString(R.string.delete_all_ok), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//									public void onClick(final DialogInterface dialogInterface, final int n) {
//										SipdroidActivity.this.db.delete(CallHistoryDatabase.Table_Name, null);
//										SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB()));
//									}
//								}).setNegativeButton((CharSequence) SipdroidActivity.this.getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
//									public void onClick(final DialogInterface dialogInterface, final int n) {
//									}
//								}).show();
//							}
//						}
					}
				}).show();
				return true;
			}
		});
		this.callHistoryListView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				final Map map = (Map) SipdroidActivity.this.adpter.getItem(n);
				CallUtil.makeVideoCall((Context) SipdroidActivity.mContext, map.get("number").toString(), map.get("name").toString(), "");
			}
		});
	}

	private void initKeyBoard() {
		(this.numTxt = (EditText) this.findViewById(R.id.p_digits)).setText((CharSequence) "");
		this.numTxt.setEnabled(false);
		this.numTxt.setInputType(0);
		this.numTxt.setCursorVisible(false);
		this.numTxtCursor = false;
		this.numTxt.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SipdroidActivity.this.numTxt.setInputType(2);
				((InputMethodManager) SipdroidActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SipdroidActivity.this.numTxt.getWindowToken(), 0);
				SipdroidActivity.this.numTxt.setCursorVisible(true);
				SipdroidActivity.this.numTxtCursor = true;
			}
		});
		this.numTxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
		this.numTxt.setDrawingCacheEnabled(true);
		(this.btnjing = (ImageButton) this.mRootView.findViewById(R.id.pjing)).setOnClickListener((View.OnClickListener) this);
		(this.btnone = (ImageButton) this.mRootView.findViewById(R.id.pone)).setOnClickListener((View.OnClickListener) this);
		(this.btntwo = (ImageButton) this.mRootView.findViewById(R.id.ptwo)).setOnClickListener((View.OnClickListener) this);
		(this.btnthree = (ImageButton) this.mRootView.findViewById(R.id.pthree)).setOnClickListener((View.OnClickListener) this);
		(this.btnfour = (ImageButton) this.mRootView.findViewById(R.id.pfour)).setOnClickListener((View.OnClickListener) this);
		(this.btnfive = (ImageButton) this.mRootView.findViewById(R.id.pfive)).setOnClickListener((View.OnClickListener) this);
		(this.btnsix = (ImageButton) this.mRootView.findViewById(R.id.psix)).setOnClickListener((View.OnClickListener) this);
		(this.btnseven = (ImageButton) this.mRootView.findViewById(R.id.pseven)).setOnClickListener((View.OnClickListener) this);
		(this.btnenight = (ImageButton) this.mRootView.findViewById(R.id.penight)).setOnClickListener((View.OnClickListener) this);
		(this.btnnine = (ImageButton) this.mRootView.findViewById(R.id.pnine)).setOnClickListener((View.OnClickListener) this);
		(this.btn0 = (ImageButton) this.mRootView.findViewById(R.id.p0)).setOnClickListener((View.OnClickListener) this);
		(this.btnmi = (ImageButton) this.mRootView.findViewById(R.id.pmi)).setOnClickListener((View.OnClickListener) this);
	}

	private void initMenuViews() {
		(this.btndialy = (ImageButton) this.mRootView.findViewById(R.id.pphone)).setOnClickListener((View.OnClickListener) this);
		(this.videoCall = (ImageButton) this.mRootView.findViewById(R.id.video_call)).setOnClickListener((View.OnClickListener) this);
		(this.btndel = (ImageButton) this.mRootView.findViewById(R.id.pdel)).setOnClickListener((View.OnClickListener) this);
		this.btndel.setOnLongClickListener((View.OnLongClickListener) this);
		(this.keyboardView = this.mRootView.findViewById(R.id.call_keyboard)).setOnClickListener((View.OnClickListener) this);
		this.keyboardView.setVisibility(View.INVISIBLE);
		(this.callHistoryView = this.mRootView.findViewById(R.id.call_history)).setVisibility(View.VISIBLE);
		(this.callHistoryOverView = this.mRootView.findViewById(R.id.call_history_coverview)).setVisibility(View.INVISIBLE);
		this.callHistoryOverView.setOnClickListener((View.OnClickListener) this);
	}

	private void judgeNumber(final String s, final String s2, final String s3) {
		if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
			MyToast.showToast(true, (Context) this, R.string.vc_service_not);
			return;
		}
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			if (DeviceInfo.CONFIG_AUDIO_MODE != 1) {
				this.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
				return;
			}
			if ((s2 != null && s2.equals("0")) || Member.UserType.toUserType(s3) == Member.UserType.VIDEO_MONITOR_GVS || Member.UserType.toUserType(s3) == Member.UserType.VIDEO_MONITOR_GB28181) {
				MyToast.showToast(true, (Context) this, R.string.audio_service_not);
				return;
			}
			CallUtil.AudioCall((Context) SipdroidActivity.mContext, s, null, s2, s3);
		} else {
			if (MemoryMg.getInstance().PhoneType != 1) {
				this.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
				return;
			}
			if ((s2 != null && s2.equals("0")) || Member.UserType.toUserType(s3) == Member.UserType.VIDEO_MONITOR_GVS || Member.UserType.toUserType(s3) == Member.UserType.VIDEO_MONITOR_GB28181) {
				MyToast.showToast(true, (Context) this, R.string.audio_service_not);
				return;
			}
			CallUtil.AudioCall((Context) SipdroidActivity.mContext, s, null, s2, s3);
		}
	}

	private void releaseToneGenerator() {
		if (this.mToneGenerator == null) {
			return;
		}
		try {
			this.mToneGenerator.release();
		} catch (Exception ex) {
			if (ex != null) {
				ex.printStackTrace();
			}
		} finally {
			this.mToneGenerator = null;
		}
	}

	public static String startTimeContainsDetails(final long timeInMillis) {
		final String format = new SimpleDateFormat(SipUAApp.mContext.getResources().getString(R.string.timeformat)).format(timeInMillis);
		final long currentTimeMillis = System.currentTimeMillis();
		final long n = currentTimeMillis - currentTimeMillis % 86400000L;
		final Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(currentTimeMillis);
		final Calendar instance2 = Calendar.getInstance();
		instance2.setTimeInMillis(timeInMillis);
//		if (instance2.get(1) != instance.get(1)) {
//			return format.substring(0, 11);
//		}
//		if (instance2.get(6) > instance.get(6)) {
//			return format.substring(0, 11);
//		}
		if (currentTimeMillis - timeInMillis < 60000L) {
			return SipUAApp.mContext.getResources().getString(R.string.ganggang);
		}
		if (currentTimeMillis - timeInMillis < 3600000L) {
			return String.valueOf((currentTimeMillis - timeInMillis) / 1000L / 60L) + " " + SipUAApp.mContext.getResources().getString(R.string.minute_ago);
		}
		if (timeInMillis > n) {
			return String.valueOf(getStringTime(timeInMillis)) + " " + format.substring(11, format.length());
		}
		if (timeInMillis < n && n - timeInMillis < 86400000L) {
			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.yesterday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
		}
//		if (instance2.get(1) != instance.get(1)) {
//			return format.substring(0, 11);
//		}
//		if (instance.get(4) != instance2.get(4) && instance.get(7) != 1) {
//			return format.substring(5, 11);
//		}
//		if (instance2.get(7) == 1) {
//			return format.substring(5, 11);
//		}
//		if (instance2.get(7) == 2) {
//			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.monday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
//		}
//		if (instance2.get(7) == 3) {
//			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.tuesday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
//		}
//		if (instance2.get(7) == 4) {
//			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.wednesday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
//		}
//		if (instance2.get(7) == 5) {
//			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.thurday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
//		}
//		if (instance2.get(7) == 6) {
//			return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.friday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
//		}
		return String.valueOf(SipUAApp.mContext.getResources().getString(R.string.saturday)) + " " + getStringTime(timeInMillis) + " " + format.substring(11, format.length());
	}

	private void toVibrate() {
	}

	public void downKey(final String s) {
		this.numTxt.setGravity(17);
		if (!this.numTxtCursor) {
			this.numTxt.setCursorVisible(true);
			this.numTxtCursor = true;
		}
		if (this.numTxtCursor) {
			final int selectionStart = this.numTxt.getSelectionStart();
			this.numTxt.setText((CharSequence) new StringBuffer(this.numTxt.getText().toString().trim()).insert(selectionStart, s).toString());
			Selection.setSelection((Spannable) this.numTxt.getText(), selectionStart + 1);
		} else {
			this.numTxt.setText((CharSequence) (String.valueOf(this.numTxt.getText().toString().trim()) + s));
		}
		this.toVibrate();
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void onClick(final View view) {
		dismissMenuPopupWindows();
//		switch (view.getId()) {
//			default: {
//			}
//			case R.id.call_history_coverview: {
//				this.hideKeyboard(this.isUserHideKeyboard = true);
//			}
//			case R.id.pone: {
//				this.downKey("1");
//				this.playTone('1');
//			}
//			case R.id.ptwo: {
//				this.downKey("2");
//				this.playTone('2');
//			}
//			case R.id.pthree: {
//				this.downKey("3");
//				this.playTone('3');
//			}
//			case R.id.pfour: {
//				this.downKey("4");
//				this.playTone('4');
//			}
//			case R.id.pfive: {
//				this.downKey("5");
//				this.playTone('5');
//			}
//			case R.id.psix: {
//				this.downKey("6");
//				this.playTone('6');
//			}
//			case R.id.pseven: {
//				this.downKey("7");
//				this.playTone('7');
//			}
//			case R.id.penight: {
//				this.downKey("8");
//				this.playTone('8');
//			}
//			case R.id.pnine: {
//				this.downKey("9");
//				this.playTone('9');
//			}
//			case R.id.p0: {
//				this.downKey("0");
//				this.playTone('0');
//			}
//			case R.id.pmi: {
//				this.downKey("*");
//				this.playTone('*');
//			}
//			case R.id.pjing: {
//				this.downKey("#");
//				this.playTone('#');
//				(this.numberString = this.numTxt.getText().toString().trim()).equals("999999#");
//			}
//			case R.id.video_call: {
//				if (!DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE && DeviceInfo.CONFIG_VIDEO_MONITOR != 1 && DeviceInfo.CONFIG_VIDEO_UPLOAD != 1) {
//					MyToast.showToast(true, (Context) this, R.string.ve_service_not);
//					return;
//				}
//				final String companyId = DataBaseService.getInstance().getCompanyId();
//				this.numberString = this.numTxt.getText().toString().trim();
//				MyLog.i("dd", "eide =" + companyId);
//				if (this.numberString.length() == 5) {
//					this.num = this.numberString;
//					CallUtil.makeVideoCall((Context) SipdroidActivity.mContext, this.num, null, "videobut");
//					return;
//				}
//				CallUtil.makeVideoCall((Context) SipdroidActivity.mContext, this.numberString, null, "videobut");
//			}
//			case R.id.pphone: {
//				MyLog.i("dd", "eid=" + DataBaseService.getInstance().getCompanyId());
//				this.numberString = this.numTxt.getText().toString().trim();
//				if (TextUtils.isEmpty((CharSequence) this.numberString)) {
//					DeviceInfo.isEmergency = false;
//					return;
//				}
//				if (this.numberString.length() == 11) {
//					if (DataBaseService.getInstance().sameCopmany(this.numberString)) {
//						this.mem = DataBaseService.getInstance().getStringbyItem(this.numberString);
//						this.audio = this.mem.getAudio();
//						this.type = this.mem.getMtype();
//						this.judgeNumber(this.numberString, this.audio, this.type);
//						return;
//					}
//					this.companyNumber(this.numberString);
//					return;
//				} else if (this.numberString.length() == 5) {
//					this.callNum = this.numberString;
//					if (DataBaseService.getInstance().sameCopmany(this.callNum)) {
//						this.mem = DataBaseService.getInstance().getStringbyItem(this.callNum);
//						this.audio = this.mem.getAudio();
//						MyLog.i("dd", "audio=" + this.audio);
//						this.type = this.mem.getMtype();
//						MyLog.i("dd", "audio=" + this.audio);
//						this.judgeNumber(this.callNum, this.audio, this.type);
//						return;
//					}
//					this.companyNumber(this.callNum);
//					return;
//				} else {
//					if (DataBaseService.getInstance().sameCopmany(this.numberString)) {
//						this.mem = DataBaseService.getInstance().getStringbyItem(this.numberString);
//						this.audio = this.mem.getAudio();
//						this.type = this.mem.getMtype();
//						this.judgeNumber(this.numberString, this.audio, this.type);
//						return;
//					}
//					this.companyNumber(this.numberString);
//					return;
//				}
//				break;
//			}
//			case R.id.pdel: {
//				this.delete();
//				this.playTone('d');
//			}
//			case R.id.back_button: {
//				this.finish();
//			}
//		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		MyLog.i("sipdroidActivity", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
		SipdroidActivity.mContext = this;
		(this.mRootView = this.getLayoutInflater().inflate(R.layout.sipdroid_lowsdk, (ViewGroup) null)).setOnClickListener((View.OnClickListener) this);
		this.setContentView(this.mRootView);
		(this.mFilter = new IntentFilter()).addAction("keyboardpopup");
		this.mFilter.addAction("keyboardpopupss");
		this.registerReceiver(this.keyboardReceiver, this.mFilter);
		this.db = CallHistoryDatabase.getInstance((Context) SipdroidActivity.mContext);
		this.initCallHistoryViews();
		this.InitCallScreen();
		this.initKeyBoard();
		this.initMenuViews();
		(this.searchView = (SearchView) this.findViewById(R.id.msearch_view)).setOnQueryTextListener(this.oqtl);
		(this.back_btn = (ImageButton) this.findViewById(R.id.back_button)).setOnClickListener((View.OnClickListener) this);
		this.bottomLayout = (LinearLayout) this.findViewById(R.id.history_baseline);
		(this.keyboard_img = (ImageView) this.findViewById(R.id.keyboard_img)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final ImageView imageView = (ImageView) SipdroidActivity.this.findViewById(R.id.keyboard_img);
				switch (motionEvent.getAction()) {
					case 0: {
						imageView.setBackgroundResource(R.color.btn_click_bg);
						if (SipdroidActivity.this.isKeyboard) {
							imageView.setImageResource(R.drawable.keyboarddown);
							break;
						}
						imageView.setImageResource(R.drawable.keyboardup);
						break;
					}
					case 1: {
						imageView.setBackgroundResource(R.color.whole_bg);
						if (SipdroidActivity.this.isKeyboard) {
							imageView.setImageResource(R.drawable.keyboarddown_release);
							break;
						}
						imageView.setImageResource(R.drawable.keyboardup_release);
						break;
					}
				}
				return false;
			}
		});
		this.keyboard_img.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final ImageView imageView = (ImageView) view;
				if (SipdroidActivity.this.isKeyboard) {
					SipdroidActivity.this.isKeyboard = false;
					imageView.setImageResource(R.drawable.keyboardup_release);
					SipdroidActivity.this.keyboardView.setVisibility(View.INVISIBLE);
					return;
				}
				SipdroidActivity.this.isKeyboard = true;
				imageView.setImageResource(R.drawable.keyboarddown_release);
				SipdroidActivity.this.keyboardView.setVisibility(View.VISIBLE);
				SipdroidActivity.this.numTxt.setText((CharSequence) "");
			}
		});
		this.registerReceiver(this.refreshlistReceiver, new IntentFilter("sipdroid.history.fresh"));
	}

	protected void onDestroy() {
		this.releaseToneGenerator();
		this.unregisterReceiver(this.keyboardReceiver);
		MyLog.i("dd", "3333");
		this.unregisterReceiver(this.refreshlistReceiver);
		if (this.db != null) {
			this.db.close();
		}
		super.onDestroy();
	}

	public void onDismiss(final DialogInterface dialogInterface) {
		this.onResume();
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n == 4) {
			final Intent intent = new Intent("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.HOME");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
			return false;
		}
		switch (n) {
			case 82: {
				if (dismissMenuPopupWindows()) {
					return true;
				}
				break;
			}
		}
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onLongClick(final View view) {
		switch (view.getId()) {
			case R.id.pdel: {
				this.numTxt.setText((CharSequence) "");
				break;
			}
		}
		return false;
	}

	public boolean onOptionsItemSelected(final MenuItem menuItem) {
		final boolean onOptionsItemSelected = super.onOptionsItemSelected(menuItem);
		switch (menuItem.getItemId()) {
			default: {
				return onOptionsItemSelected;
			}
			case 1: {
				Tools.exitApp((Context) this);
				return onOptionsItemSelected;
			}
		}
	}

	public void onPause() {
		dismissPopupWindows();
		super.onPause();
	}

	public void onResume() {
		super.onResume();
		this.searchView.setIconified(true);
		this.searchView.setIconified(true);
		this.keyboardView.setVisibility(View.INVISIBLE);
		this.keyboard_img.setImageResource(R.drawable.keyboardup_release);
		Object popup_video = new ContentValues();
		((ContentValues) popup_video).put("status", 1);
		this.db.update(CallHistoryDatabase.Table_Name, "type='CallUnak' and status=0", (ContentValues) popup_video);
		MyLog.i("dd", "db update ok");
		this.sendBroadcast(new Intent("com.zed3.sipua_clear_missedcall"));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					MyLog.i("dd", "GetDataFromDB()" + SipdroidActivity.this.GetDataFromDB());
					SipdroidActivity.this.mHandle.sendMessage(SipdroidActivity.this.mHandle.obtainMessage(1, (Object) SipdroidActivity.this.GetDataFromDB()));
				} catch (Exception ex) {
					MyLog.e(SipdroidActivity.this.tag, ex.toString());
					ex.printStackTrace();
				}
			}
		}).start();
		Receiver.engine((Context) SipdroidActivity.mContext);
	}

	public void onStart() {
		this.scrollCount = 0;
		final NotificationManager notificationManager = (NotificationManager) SipdroidActivity.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(3);
		notificationManager.cancel(4);
		super.onStart();
	}

	void playTone(final Character c) {
		if (this.mDTMFToneEnabled) {
			final int ringerMode = ((AudioManager) SipdroidActivity.mContext.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
			if (ringerMode != 0 && ringerMode != 1) {
				synchronized (this.mToneGeneratorLock) {
					if (this.mToneGenerator == null) {
						Log.w("tagdd", "playTone: mToneGenerator == null, tone: " + c);
						return;
					}
				}
//				this.mToneGenerator.startTone((int) SipdroidActivity.mToneMap.get(t), 150);
			}
			// monitorexit(o)
		}
	}

	public String twoDateDistance(long n) {
		if (n == 0L) {
			return "";
		}
		n = System.currentTimeMillis() - n;
		if (n < 60000L) {
			if (n / 1000L <= 1L) {
				return String.valueOf(n / 1000L) + " " + this.getResources().getString(R.string.second_ago);
			}
			return String.valueOf(n / 1000L) + " " + this.getResources().getString(R.string.seconds_ago);
		} else if (n < 3600000L) {
			n = n / 1000L / 60L;
			if (n <= 1L) {
				return String.valueOf(n) + " " + this.getResources().getString(R.string.minute_ago);
			}
			return String.valueOf(n) + " " + this.getResources().getString(R.string.minutes_ago);
		} else if (n < 86400000L) {
			n = n / 60L / 60L / 1000L;
			if (n <= 1L) {
				return String.valueOf(n) + " " + this.getResources().getString(R.string.hour_ago);
			}
			return String.valueOf(n) + " " + this.getResources().getString(R.string.hours_ago);
		} else if (n < 604800000L) {
			n = n / 1000L / 60L / 60L / 24L;
			if (n <= 1L) {
				return String.valueOf(n) + " " + this.getResources().getString(R.string.day_ago);
			}
			return String.valueOf(n) + " " + this.getResources().getString(R.string.days_ago);
		} else {
			n = n / 1000L / 60L / 60L / 24L / 7L;
			if (n <= 1L) {
				return String.valueOf(n) + " " + this.getResources().getString(R.string.week_ago);
			}
			return String.valueOf(n) + " " + this.getResources().getString(R.string.weeks_ago);
		}
	}

	public class MyAdapter extends BaseAdapter {
		private String audio;
		Long begin;
		String begin_str;
		List<Map<String, Object>> dbList;
		DataBaseService dbService;
		Long end;
		private int left;
		private LayoutInflater mInflater;
		private Member mem;
		private String mtype;
		String name;
		private String numPre;
		String number;
		private String pictureupload;
		private String smsswitch;
		String type;
		ViewHolder vHolder;
		private String video;

		public MyAdapter(final Context context, final List<Map<String, Object>> dbList) {
			this.dbService = DataBaseService.getInstance();
			this.vHolder = null;
			this.dbList = null;
			this.mInflater = LayoutInflater.from(context);
			this.dbList = dbList;
		}

		public int getCount() {
			if (this.dbList == null) {
				return 0;
			}
			return this.dbList.size();
		}

		public Object getItem(final int n) {
			return this.dbList.get(n);
		}

		public long getItemId(final int n) {
			return Integer.parseInt(String.valueOf(this.dbList.get(n).get("_id")));
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			if (inflate == null) {
				inflate = this.mInflater.inflate(R.layout.call_history_item, (ViewGroup) null);
//				this.vHolder = new ViewHolder((ViewHolder) null);
				this.vHolder.title = (TextView) inflate.findViewById(R.id.call_history_name);
				(this.vHolder.tp = this.vHolder.title.getPaint()).setFakeBoldText(true);
				this.vHolder.time = (TextView) inflate.findViewById(R.id.call_history_time);
				this.vHolder.img = (ImageView) inflate.findViewById(R.id.call_history_type);
				this.vHolder.photoImageView = (ImageView) inflate.findViewById(R.id.call_history_photo);
				this.vHolder.numberTextView = (TextView) inflate.findViewById(R.id.call_history_number);
				this.vHolder.videoBtn = (ImageView) inflate.findViewById(R.id.call_video_btn);
				this.vHolder.voiceBtn = (ImageView) inflate.findViewById(R.id.call_voice_btn);
				this.vHolder.msgBtn = (ImageView) inflate.findViewById(R.id.call_msg_btn);
				this.vHolder.line_sub = (LinearLayout) inflate.findViewById(R.id.line_sub);
				this.vHolder.line_sub2 = (LinearLayout) inflate.findViewById(R.id.line_sub2);
				this.vHolder.tempCallNameTv = (TextView) inflate.findViewById(R.id.tempCallName);
				this.vHolder.tempCallTimeTv = (TextView) inflate.findViewById(R.id.tempCallTime);
				this.vHolder.timeLayout = (RelativeLayout) inflate.findViewById(R.id.timeLayout);
				this.vHolder.infoLayout = (LinearLayout) inflate.findViewById(R.id.infoLayout);
				this.vHolder.typeLayout = (LinearLayout) inflate.findViewById(R.id.typeLayout);
				this.vHolder.funcLayout = (LinearLayout) inflate.findViewById(R.id.funcLayout);
				this.vHolder.intobut = (ImageView) inflate.findViewById(R.id.intobut);
				inflate.setTag((Object) this.vHolder);
			} else {
				this.vHolder = (ViewHolder) inflate.getTag();
			}
//			this.type = this.dbList.get(n).get("type");
//			this.name = this.dbList.get(n).get("name");
			if (this.name == null) {
				this.name = "null";
			}
//			this.number = this.dbList.get(n).get("number");
			MyLog.i("ee", "number+====" + this.number);
			if (this.number == null) {
				this.number = "nullnumber";
			}
//			this.begin_str = this.dbList.get(n).get("begin_str");
			if (this.number.length() <= 11) {
				this.mem = this.dbService.getStringbyItem(this.number);
				this.video = this.mem.getVideo();
				this.audio = this.mem.getAudio();
				this.pictureupload = this.mem.getPictureupload();
				this.smsswitch = this.mem.getSmsswitch();
				this.mtype = this.mem.getMtype();
				if (this.number.length() == 11) {
					this.numPre = this.number.substring(0, 6);
				}
				this.left = 0;
				if (!DataBaseService.getInstance().sameCopmany(this.number)) {
					if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
						this.vHolder.line_sub.setVisibility(View.GONE);
						this.vHolder.videoBtn.setVisibility(View.GONE);
					} else {
						this.vHolder.videoBtn.setVisibility(View.VISIBLE);
						++this.left;
					}
					if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
						this.vHolder.voiceBtn.setVisibility(View.VISIBLE);
					} else {
						this.vHolder.voiceBtn.setVisibility(View.VISIBLE);
						++this.left;
					}
					if (SipdroidActivity.this.getServerListArray() == -1) {
						this.vHolder.msgBtn.setVisibility(View.GONE);
					} else {
						this.vHolder.msgBtn.setVisibility(View.VISIBLE);
						++this.left;
					}
					if (this.left == 1) {
						this.vHolder.line_sub2.setVisibility(View.GONE);
						this.vHolder.line_sub.setVisibility(View.GONE);
					}
					if (DeviceInfo.CONFIG_SUPPORT_VIDEO && DeviceInfo.CONFIG_SUPPORT_AUDIO && !DeviceInfo.CONFIG_SUPPORT_IM) {
						this.vHolder.line_sub2.setVisibility(View.GONE);
					}
				} else {
					if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
						this.vHolder.line_sub.setVisibility(View.GONE);
						this.vHolder.videoBtn.setVisibility(View.GONE);
					} else if (!TextUtils.isEmpty((CharSequence) this.mtype) || !TextUtils.isEmpty((CharSequence) this.video)) {
						if (this.mtype.equals("GQT") && this.video.equalsIgnoreCase("0")) {
							this.vHolder.videoBtn.setVisibility(View.INVISIBLE);
						} else {
							this.vHolder.videoBtn.setVisibility(View.VISIBLE);
							++this.left;
						}
					} else {
						this.vHolder.videoBtn.setVisibility(View.INVISIBLE);
					}
					if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
						this.vHolder.voiceBtn.setVisibility(View.VISIBLE);
					} else {
						this.vHolder.voiceBtn.setVisibility(View.VISIBLE);
						++this.left;
					}
					if (SipdroidActivity.this.getServerListArray() == -1) {
						this.vHolder.msgBtn.setVisibility(View.GONE);
					} else if (!TextUtils.isEmpty((CharSequence) this.mtype) && !TextUtils.isEmpty((CharSequence) this.smsswitch) && !TextUtils.isEmpty((CharSequence) this.pictureupload)) {
						if (this.mtype.equals("GVS") || (this.mtype.equals("GQT") && this.pictureupload.equalsIgnoreCase("0") && this.smsswitch.equalsIgnoreCase("0"))) {
							this.vHolder.msgBtn.setVisibility(View.INVISIBLE);
						} else if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && this.mtype.equals("GQT") && this.smsswitch.equalsIgnoreCase("0")) {
							this.vHolder.msgBtn.setVisibility(View.INVISIBLE);
						} else if (!DeviceInfo.CONFIG_SUPPORT_IM && this.mtype.equals("GQT") && this.pictureupload.equalsIgnoreCase("0")) {
							this.vHolder.msgBtn.setVisibility(View.INVISIBLE);
						} else {
							this.vHolder.msgBtn.setVisibility(View.VISIBLE);
							++this.left;
						}
					} else {
						this.vHolder.msgBtn.setVisibility(View.INVISIBLE);
					}
					if (this.left == 1) {
						this.vHolder.line_sub2.setVisibility(View.GONE);
					}
					if (DeviceInfo.CONFIG_SUPPORT_VIDEO && DeviceInfo.CONFIG_SUPPORT_AUDIO && !DeviceInfo.CONFIG_SUPPORT_IM) {
						this.vHolder.line_sub2.setVisibility(View.GONE);
					}
				}
			}
			if (this.type.equals("TempGrpCall")) {
				this.vHolder.infoLayout.setVisibility(View.GONE);
				this.vHolder.typeLayout.setVisibility(View.GONE);
				this.vHolder.funcLayout.setVisibility(View.GONE);
				this.vHolder.timeLayout.setVisibility(View.VISIBLE);
				this.vHolder.tempCallNameTv.setVisibility(View.VISIBLE);
				this.vHolder.tempCallTimeTv.setVisibility(View.VISIBLE);
			} else {
				this.vHolder.infoLayout.setVisibility(View.VISIBLE);
				this.vHolder.typeLayout.setVisibility(View.VISIBLE);
				this.vHolder.timeLayout.setVisibility(View.GONE);
				this.vHolder.tempCallNameTv.setVisibility(View.GONE);
				this.vHolder.tempCallTimeTv.setVisibility(View.GONE);
			}
			this.vHolder.intobut.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final Intent intent = new Intent((Context) SipdroidActivity.mContext, (Class) MemberRecordActivity.class);
//					final String s = MyAdapter.this.dbList.get(n).get("number");
//					String s2;
//					if ((s2 = MyAdapter.this.dbList.get(n).get("name")) == null) {
//						s2 = s;
//					}
//					intent.putExtra("user_nam", s2);
//					intent.putExtra("user_num", s);
					SipdroidActivity.this.startActivity(intent);
				}
			});
			this.vHolder.photoImageView.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
//					final String s = MyAdapter.this.dbList.get(n).get("number");
//					final String s2 = MyAdapter.this.dbList.get(n).get("name");
//					if (DataBaseService.getInstance().sameCopmany(s)) {
//						final Intent intent = new Intent((Context) SipdroidActivity.mContext, (Class) UserDetails.class);
//						String s3;
//						if ((s3 = s2) == null) {
//							s3 = s;
//						}
//						intent.putExtra("user_nam", s3);
//						intent.putExtra("user_num", s);
//						SipdroidActivity.this.startActivity(intent);
//						return;
//					}
//					MyToast.showToast(true, (Context) SipdroidActivity.this, R.string.member_service_not);
				}
			});
			this.vHolder.videoBtn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
//					final String s = MyAdapter.this.dbList.get(n).get("number");
//					if (s == null) {
//						DialogUtil.showCheckDialog((Context) SipdroidActivity.mContext, SipdroidActivity.this.getResources().getString(R.string.information), SipdroidActivity.this.getResources().getString(R.string.number_not_exist), SipdroidActivity.this.getResources().getString(R.string.ok_know));
//						return;
//					}
//					CallUtil.makeVideoCall((Context) SipdroidActivity.mContext, s, null, "videobut");
				}
			});
			this.vHolder.msgBtn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final String s = ""; //MyAdapter.this.dbList.get(n).get("number");
//					MyAdapter.access .0 (MyAdapter.this, MyAdapter.this.dbService.getStringbyItem(s));
//					MyAdapter.access .2 (MyAdapter.this, MyAdapter.this.mem.getPictureupload());
//					MyAdapter.access .3 (MyAdapter.this, MyAdapter.this.mem.getSmsswitch());
//					MyAdapter.access .4 (MyAdapter.this, MyAdapter.this.mem.getMtype());
					if (!DataBaseService.getInstance().sameCopmany(s)) {
						MyToast.showToast(true, (Context) SipdroidActivity.mContext, R.string.audio_service_not);
					} else {
						if ((MyAdapter.this.mtype != null && MyAdapter.this.mtype.equals("Console")) || (MyAdapter.this.mtype != null && MyAdapter.this.mtype.equals("GQT") && MyAdapter.this.smsswitch != null && MyAdapter.this.pictureupload != null && MyAdapter.this.pictureupload.equalsIgnoreCase("1") && MyAdapter.this.smsswitch.equalsIgnoreCase("1"))) {
							new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(SipdroidActivity.this.getServerListArray(), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (SipdroidActivity.this.getServerListArray() != R.array.msgDialogList2) {
												this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MessageDialogueActivity.class);
												this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
												this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
												SipdroidActivity.this.startActivity(this.intent);
												return;
											}
											this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
											this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
											SipdroidActivity.this.startActivity(this.intent);
										}
										case 1: {
											this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
											this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
											SipdroidActivity.this.startActivity(this.intent);
										}
									}
								}
							}).show();
							return;
						}
						if (MyAdapter.this.mtype != null && MyAdapter.this.mtype.equals("GQT") && MyAdapter.this.smsswitch != null && MyAdapter.this.pictureupload != null && MyAdapter.this.pictureupload.equalsIgnoreCase("0") && MyAdapter.this.smsswitch.equalsIgnoreCase("1")) {
							new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.msgDialogList1, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (SipdroidActivity.this.getServerListArray() != R.array.msgDialogList2) {
												this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MessageDialogueActivity.class);
												this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
												this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
												SipdroidActivity.this.startActivity(this.intent);
												return;
											}
											this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
											this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
											SipdroidActivity.this.startActivity(this.intent);
										}
									}
								}
							}).show();
							return;
						}
						if (MyAdapter.this.mtype != null && MyAdapter.this.mtype.equals("GQT") && MyAdapter.this.smsswitch != null && MyAdapter.this.pictureupload != null && MyAdapter.this.pictureupload.equalsIgnoreCase("1") && MyAdapter.this.smsswitch.equalsIgnoreCase("0")) {
							new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.msgDialogList2, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								Intent intent = new Intent();

								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											this.intent.setClass((Context) SipdroidActivity.mContext, (Class) MainActivity.class);
											this.intent.putExtra("action", "fastMMS");
											this.intent.putExtra("userName", (String) MyAdapter.this.dbList.get(n).get("name"));
											this.intent.putExtra("address", (String) MyAdapter.this.dbList.get(n).get("number"));
											SipdroidActivity.this.startActivity(this.intent);
										}
									}
								}
							}).show();
						}
					}
				}
			});
			this.vHolder.voiceBtn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final String s = ""; // MyAdapter.this.dbList.get(n).get("number");
//					MyAdapter.access .0 (MyAdapter.this, MyAdapter.this.dbService.getStringbyItem(s));
//					MyAdapter.access .8 (MyAdapter.this, MyAdapter.this.mem.getAudio());
//					MyAdapter.access .4 (MyAdapter.this, MyAdapter.this.mem.getMtype());
					if (s == null) {
						DialogUtil.showCheckDialog((Context) SipdroidActivity.mContext, SipdroidActivity.this.getResources().getString(R.string.information), SipdroidActivity.this.getResources().getString(R.string.number_not_exist), SipdroidActivity.this.getResources().getString(R.string.ok_know));
						return;
					}
					if (DeviceInfo.CONFIG_SUPPORT_AUDIO) {
						if (!DataBaseService.getInstance().sameCopmany(s) || (MyAdapter.this.mtype != null && Member.UserType.toUserType(MyAdapter.this.type) == Member.UserType.SVP) || (MyAdapter.this.mtype != null && Member.UserType.toUserType(MyAdapter.this.type) == Member.UserType.MOBILE_GQT && MyAdapter.this.audio != null && MyAdapter.this.audio.equalsIgnoreCase("1"))) {
							new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
												if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
													CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
													return;
												}
												SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
												return;
											} else {
												if (MemoryMg.getInstance().PhoneType == 1) {
													CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
													return;
												}
												SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
												return;
											}
										}
									}
								}
							}).show();
							return;
						}
						if (MyAdapter.this.mtype != null && Member.UserType.toUserType(MyAdapter.this.type) == Member.UserType.VIDEO_MONITOR_GVS) {
							new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialogInterface, final int n) {
									switch (n) {
										default: {
										}
										case 0: {
											if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
												if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
													CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
													return;
												}
												SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
												return;
											} else {
												if (MemoryMg.getInstance().PhoneType == 1) {
													CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
													return;
												}
												SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
												return;
											}
										}
									}
								}
							}).show();
							return;
						}
						new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
												return;
											}
											SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
												return;
											}
											SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										}
									}
								}
							}
						}).show();
					} else {
						if (!DeviceInfo.CONFIG_SUPPORT_AUDIO && MyAdapter.this.mtype != null && Member.UserType.toUserType(MyAdapter.this.type) == Member.UserType.VIDEO_MONITOR_GVS) {
							MyToast.showToast(true, (Context) SipdroidActivity.mContext, R.string.audio_service_not);
							return;
						}
						new AlertDialog.Builder((Context) SipdroidActivity.this).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
												return;
											}
											SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall((Context) SipdroidActivity.mContext, s, null);
												return;
											}
											SipdroidActivity.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + s)));
											return;
										}
									}
								}
							}
						}).show();
					}
				}
			});
			if (this.type.equals("CallIn")) {
				this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_01);
			} else if (this.type.equals("CallUnak")) {
				this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_02);
			} else if (this.type.equals("CallOut")) {
				this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_03);
			} else if (this.type.equals("CallUnout")) {
				this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_03);
			}
			if ("TempGrpCall".equals(this.type)) {
				this.vHolder.tempCallNameTv.setText((CharSequence) this.name);
			} else {
				this.vHolder.title.setText((CharSequence) this.name);
				this.vHolder.numberTextView.setText((CharSequence) this.number);
			}
			if (!this.begin_str.equals("")) {
				final Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.begin_str.trim(), new ParsePosition(0));
				if ("TempGrpCall".equals(this.type)) {
					this.vHolder.tempCallTimeTv.setText((CharSequence) SipdroidActivity.this.twoDateDistance(parse.getTime()));
				} else {
					this.vHolder.time.setText((CharSequence) SipdroidActivity.startTimeContainsDetails(parse.getTime()));
					if (this.type.equals("CallUnak")) {
						this.vHolder.title.setText((CharSequence) Html.fromHtml("<font color=red>" + this.vHolder.title.getText().toString() + "</font>"));
//						this.begin = this.dbList.get(n).get("begin");
//						this.end = this.dbList.get(n).get("end");
//						this.begin_str = this.dbList.get(n).get("begin_str");
//						this.vHolder.time.setText((CharSequence) (String.valueOf(SipdroidActivity.startTimeContainsDetails(parse.getTime())) + " " + SipUAApp.mContext.getResources().getString(R.string.xl) + " " + new SimpleDateFormat(SipUAApp.mContext.getResources().getString(R.string.secformat)).format(this.end - this.begin)));
					}
				}
			}
			final String trim = this.vHolder.title.getText().toString().trim();
			final String trim2 = this.vHolder.numberTextView.getText().toString().trim();
			if (SipdroidActivity.this.mIntoString != null && !SipdroidActivity.this.mIntoString.equals("") && (trim.contains(SipdroidActivity.this.mIntoString) || trim.toLowerCase().contains(SipdroidActivity.this.mIntoString.toLowerCase()) || this.number.contains(SipdroidActivity.this.mIntoString))) {
				if (trim.contains(SipdroidActivity.this.mIntoString) || trim.toLowerCase().contains(SipdroidActivity.this.mIntoString.toLowerCase())) {
					this.vHolder.title.setText((CharSequence) SipdroidActivity.this.getStringText(trim, SipdroidActivity.this.mIntoString, this.type));
				}
				if (this.number.contains(SipdroidActivity.this.mIntoString)) {
					this.vHolder.numberTextView.setText((CharSequence) SipdroidActivity.this.getStringText(trim2, SipdroidActivity.this.mIntoString, ""));
				}
			}
			this.vHolder.photoImageView.setImageResource(SipdroidActivity.this.getIcon(this.mtype));
			return inflate;
		}

		public void refreshListView(final List<Map<String, Object>> dbList) {
			this.dbList = dbList;
		}
	}

	private class ViewHolder {
		TextView call_history_txt;
		LinearLayout funcLayout;
		ImageView img;
		TextView info;
		LinearLayout infoLayout;
		ImageView intobut;
		LinearLayout line_sub;
		LinearLayout line_sub2;
		ImageView msgBtn;
		TextView numberTextView;
		ImageView photoImageView;
		TextView tempCallNameTv;
		TextView tempCallTimeTv;
		TextView time;
		RelativeLayout timeLayout;
		TextView title;
		TextPaint tp;
		LinearLayout typeLayout;
		ImageView videoBtn;
		ImageView voiceBtn;
	}
}
