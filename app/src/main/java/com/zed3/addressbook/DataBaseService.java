package com.zed3.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.addressbook.Member.UserType;
import com.zed3.customgroup.GroupInfoItem;
import com.zed3.log.MyLog;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.ContactPerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class DataBaseService extends Observable {
	public static final String TABLE_MEMBERS = "members";
	public static final String TABLE_MESSAGES = "messages";
	public static final String TABLE_MSGVERSION = "msgversion";
	private static final String TAG = DataBaseService.class.getSimpleName();
	static AbookOpenHelper dbOpenHelper;
	public static DataBaseService dbService;
	SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

	public enum ChangedType {
		GET_ALL_TEAMS,
		DELETE_ALL,
		GET_MEMBERS_NUMBER,
		GET_MEMBER_TYPE,
		GET_TEAM_NAME,
		GET_MEMBERS_BY_PID,
		GET_PID,
		GET_MEMBERS,
		INSERT_ALVERSION,
		GET_ALVERSION,
		INSERT_TEAM,
		INSERT_MEMBERS,
		QUERY_MEMBERS_BY_KEYWORD,
		QUERY_ALL_MEMBERS,
		GET_TEAMS_BY_PID,
		GET_MEMBERS_BY_TYPE
	}

	public static final class WorkArgs {
		public Object object;
		public ChangedType type;

		public static WorkArgs obtain(ChangedType type, Object object) {
			MyLog.d("xxxxxx", "DataBaseService#WorkArgs obtain enter");
			WorkArgs args = new WorkArgs();
			args.type = type;
			args.object = object;
			MyLog.d("xxxxxx", "DataBaseService#WorkArgs obtain exit");
			return args;
		}
	}

	public boolean isNoMember(java.lang.String r7) {
		// TODO
		return false;
	}

	public boolean isNoMemberByType(java.lang.String r7) {
		// TODO
		return false;
	}

	public boolean isNoMemberByType(java.lang.String r7, java.lang.String r8) {
		// TODO
		return false;
	}

	public boolean isNoTeam(java.lang.String r7) {
		// TODO
		return false;
	}

	public boolean isNoTeams(java.lang.String r7) {
		// TODO
		return false;
	}

	public void onDatasetChanged(ChangedType type, Object object) {
		MyLog.d("xxxxxx", "DataBaseService#onDatasetChanged enter type=" + type);
		setChanged();
		hasChanged();
		MyLog.d("xxxxxx", "DataBaseService#onDatasetChanged hasChanged()" + hasChanged());
		MyLog.d("xxxxxx", "DataBaseService#onDatasetChanged countObservers()" + countObservers());
		notifyObservers(WorkArgs.obtain(type, object));
		Log.v("huangfujianDB", "onDatasetChangedobject!!!" + object);
		MyLog.d("xxxxxx", "DataBaseService#onDatasetChanged exit ");
		Log.v("huangfujianDB", "onDatasetChanged");
	}

	public static DataBaseService getInstance() {
		if (dbService == null) {
			dbService = new DataBaseService(Receiver.mContext);
		}
		return dbService;
	}

	public DataBaseService(Context context) {
		AbookOpenHelper.setDBName();
		dbOpenHelper = AbookOpenHelper.getInstance(context);
	}

	public void deleteAll() {
		dbOpenHelper.delete("members", null);
		dbOpenHelper.delete(AbookOpenHelper.TABLE_TEAMS, null);
		Log.v("huangfujianDB", "deleteAll()");
	}

	public void deleteMsg() {
		dbOpenHelper.delete("messages", null);
	}

	public List<GroupInfoItem> getAllMembers() {
		Cursor cursor = null;
		String pid = "";
		List<GroupInfoItem> list = new ArrayList();
		try {
			cursor = dbOpenHelper.mQuery("members", null, UserMinuteActivity.USER_MNAME);
			MyLog.i("cursortest", "cursor count = " + cursor.getCount());
			cursor.moveToFirst();
			GroupInfoItem groupInfoItem = new GroupInfoItem();
			groupInfoItem.setGrp_uName(cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
			groupInfoItem.setGrp_uNumber(cursor.getString(cursor.getColumnIndex("number")));
			groupInfoItem.setGrp_uDept(getTeamName(cursor.getString(cursor.getColumnIndex("tid"))));
			groupInfoItem.setGrp_img(false);
			list.add(groupInfoItem);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					groupInfoItem = new GroupInfoItem();
					groupInfoItem.setGrp_uName(cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
					groupInfoItem.setGrp_uNumber(cursor.getString(cursor.getColumnIndex("number")));
					groupInfoItem.setGrp_uDept(getTeamName(cursor.getString(cursor.getColumnIndex("tid"))));
					groupInfoItem.setGrp_img(false);
					list.add(groupInfoItem);
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<ContactPerson> getAllContactMembers() {
		Cursor cursor = null;
		List<ContactPerson> list = new ArrayList();
		try {
			cursor = dbOpenHelper.mQuery("members", null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ContactPerson cPerson = new ContactPerson();
					cPerson.setContact_name(cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
					cPerson.setContact_num(cursor.getString(cursor.getColumnIndex("number")));
					list.add(cPerson);
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public int getMembersNumber(String tpye) {
		Cursor cursor = null;
		int count = 0;
		try {
			cursor = dbOpenHelper.mQuery("members", tpye, null);
			if (cursor != null) {
				count = cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	public int getMembersNumber() {
		Cursor cursor = null;
		int count = 0;
		try {
			cursor = dbOpenHelper.query("members", null);
			if (cursor != null) {
				count = cursor.getCount();
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	public String getMemberType(String number) {
		Cursor cursor = null;
		String type = "";
		try {
			cursor = getMembers(number);
			if (cursor != null && cursor.moveToNext()) {
				type = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return type;
	}

	public String getMemberAudioType(String number) {
		Cursor cursor = null;
		String type = "";
		try {
			cursor = getMembers(number);
			if (cursor != null && cursor.moveToNext()) {
				type = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return type;
	}

	public String getMemberVideoType(String number) {
		Cursor cursor = null;
		String type = "";
		try {
			cursor = getMembers(number);
			if (cursor != null && cursor.moveToNext()) {
				type = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return type;
	}

	public String getId(String id) {
		Cursor cursor = null;
		String pid = "";
		try {
			cursor = getTeams(id);
			if (cursor != null && cursor.moveToNext()) {
				pid = cursor.getString(cursor.getColumnIndex("pid"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return pid;
	}

	public String getTeamName(String id) {
		Cursor cursor = null;
		String name = "";
		try {
			cursor = getTeams(id);
			if (cursor != null && cursor.moveToNext()) {
				name = cursor.getString(cursor.getColumnIndex("name"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return name;
	}

	public List<Map<String, String>> getMembersByPid(final String tid) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByPid enter");
		final List<Map<String, String>> list = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				Cursor cursor = DataBaseService.dbOpenHelper.mQuery("members", "tid= '" + tid + "'", null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						try {
							if (!cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)).equals(UserType.GRP_NUM.convert())) {
								Map<String, String> map = new HashMap();
								map.put("number", cursor.getString(cursor.getColumnIndex("number")));
								map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
								map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
								map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
								map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
								map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
								map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
								map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
								map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
								map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
								map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
								map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
								map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
								map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
								map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
								list.add(map);
							}
						} catch (Exception e) {
							MyLog.e(DataBaseService.TAG, e.toString());
							if (cursor != null) {
								try {
									cursor.close();
									return;
								} catch (Exception e2) {
									MyLog.e(DataBaseService.TAG, e2.toString());
									return;
								}
							}
							return;
						} catch (Throwable th) {
							if (cursor != null) {
								try {
									cursor.close();
								} catch (Exception e22) {
									MyLog.e(DataBaseService.TAG, e22.toString());
								}
							}
						}
					}
					DataBaseService.this.onDatasetChanged(ChangedType.GET_MEMBERS_BY_PID, list);
					if (cursor != null) {
						try {
							cursor.close();
						} catch (Exception e222) {
							MyLog.e(DataBaseService.TAG, e222.toString());
						}
					}
				}
			}
		}).start();
		MyLog.d("xxxxxx", " dataBaseService getMembersByPid enter");
		return list;
	}

	public synchronized List<Map<String, String>> getMembersByType(String type, String tid) {
		List<Map<String, String>> list;
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		list = new ArrayList();
		MyLog.i("gengjibin", "type" + type + "tid" + tid);
		Cursor cursor = dbOpenHelper.mQuery("members", "mtype = '" + type + "' and tid = '" + tid + "'", null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				try {
					Map<String, String> map = new HashMap();
					map.put("number", cursor.getString(cursor.getColumnIndex("number")));
					map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
					map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
					map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
					map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
					map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
					map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
					map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
					map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
					map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
					map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
					map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
					map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
					map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
					map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
					list.add(map);
				} catch (Throwable th) {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Map<String, String>> getMemberByType(final String type, final String tid) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		final List<Map<String, String>> list = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				Cursor cursor = DataBaseService.dbOpenHelper.mQuery("members", "mtype = '" + type + "' and tid = '" + tid + "'", null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						try {
							Map<String, String> map = new HashMap();
							map.put("number", cursor.getString(cursor.getColumnIndex("number")));
							map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
							map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
							map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
							map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
							map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
							map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
							map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
							map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
							map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
							map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
							map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
							map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
							map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
							map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
							list.add(map);
						} finally {
							if (cursor != null) {
								cursor.close();
							}
						}
					}
					DataBaseService.this.onDatasetChanged(ChangedType.GET_MEMBERS_BY_TYPE, list);
				}
			}
		}).start();
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		return list;
	}

	public List<Map<String, String>> getMemberByType(String type, String tid, String spell) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		final List<Map<String, String>> list = new ArrayList();
		final String str = spell;
		final String str2 = type;
		final String str3 = tid;
		new Thread(new Runnable() {
			private Cursor cursor;

			public void run() {
				if (TextUtils.isEmpty(str)) {
					this.cursor = DataBaseService.dbOpenHelper.mQuery("members", "mtype = '" + str2 + "' and tid = '" + str3 + "'", null);
				} else {
					MyLog.i("gengjibin", "@spell" + str);
					this.cursor = DataBaseService.dbOpenHelper.mQuery("members", "mtype = '" + str2 + "' and tid = '" + str3 + str, null);
				}
				if (this.cursor != null) {
					while (this.cursor.moveToNext()) {
						try {
							Map<String, String> map = new HashMap();
							map.put("number", this.cursor.getString(this.cursor.getColumnIndex("number")));
							map.put(UserMinuteActivity.USER_MNAME, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
							map.put(UserMinuteActivity.USER_MTYPE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
							map.put(UserMinuteActivity.USER_POSITION, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
							map.put(UserMinuteActivity.USER_SEX, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
							map.put(AbookOpenHelper.TABLE_SHOWFLAG, this.cursor.getString(this.cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
							map.put(UserMinuteActivity.USER_PHONE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
							map.put(UserMinuteActivity.USER_DTYPE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
							map.put(UserMinuteActivity.USER_VIDEO, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
							map.put(UserMinuteActivity.USER_AUDIO, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
							map.put(UserMinuteActivity.USER_PTTMAP, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
							map.put(UserMinuteActivity.USER_GPS, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
							map.put(UserMinuteActivity.USER_PICTUREUPLPAD, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
							map.put(UserMinuteActivity.USER_SMSSWITCH, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
							map.put("tid", this.cursor.getString(this.cursor.getColumnIndex("tid")));
							list.add(map);
						} finally {
							if (this.cursor != null) {
								this.cursor.close();
							}
						}
					}
					DataBaseService.this.onDatasetChanged(ChangedType.GET_MEMBERS_BY_TYPE, list);
				}
			}
		}).start();
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		return list;
	}

	public List<Map<String, String>> getMemberByType(final String spell) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		final List<Map<String, String>> list = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				Cursor cursor = DataBaseService.dbOpenHelper.mQuery("members", spell, null);
				if (cursor != null) {
					while (cursor.moveToNext()) {
						try {
							Map<String, String> map = new HashMap();
							map.put("number", cursor.getString(cursor.getColumnIndex("number")));
							map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
							map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
							map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
							map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
							map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
							map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
							map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
							map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
							map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
							map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
							map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
							map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
							map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
							map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
							list.add(map);
						} finally {
							if (cursor != null) {
								cursor.close();
							}
						}
					}
					DataBaseService.this.onDatasetChanged(ChangedType.GET_MEMBERS_BY_TYPE, list);
				}
			}
		}).start();
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		return list;
	}

	public List<Map<String, String>> getMemberByType(String type, String tid, String id, String spell) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		final List<Map<String, String>> list = new ArrayList();
		final String str = spell;
		final String str2 = type;
		final String str3 = tid;
		final String str4 = id;
		new Thread(new Runnable() {
			Cursor cursor;

			public void run() {
				if (TextUtils.isEmpty(str)) {
					this.cursor = DataBaseService.dbOpenHelper.mQuery("members", "mtype = '" + str2 + "' and tid = '" + str3 + "'or mtype ='" + str2 + "'and tid = '" + str4 + "'", null);
				} else {
					this.cursor = DataBaseService.dbOpenHelper.mQuery("members", "mtype = '" + str2 + "' and tid = '" + str3 + "'or mtype ='" + str2 + "'and tid = '" + str4 + str, null);
				}
				if (this.cursor != null) {
					while (this.cursor.moveToNext()) {
						try {
							Map<String, String> map = new HashMap();
							map.put("number", this.cursor.getString(this.cursor.getColumnIndex("number")));
							map.put(UserMinuteActivity.USER_MNAME, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
							map.put(UserMinuteActivity.USER_MTYPE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
							map.put(UserMinuteActivity.USER_POSITION, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
							map.put(AbookOpenHelper.TABLE_SHOWFLAG, this.cursor.getString(this.cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
							map.put(UserMinuteActivity.USER_SEX, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
							map.put(UserMinuteActivity.USER_PHONE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
							map.put(UserMinuteActivity.USER_DTYPE, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
							map.put(UserMinuteActivity.USER_VIDEO, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
							map.put(UserMinuteActivity.USER_AUDIO, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
							map.put(UserMinuteActivity.USER_PTTMAP, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
							map.put(UserMinuteActivity.USER_GPS, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
							map.put(UserMinuteActivity.USER_PICTUREUPLPAD, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
							map.put(UserMinuteActivity.USER_SMSSWITCH, this.cursor.getString(this.cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
							map.put("tid", this.cursor.getString(this.cursor.getColumnIndex("tid")));
							list.add(map);
						} finally {
							if (this.cursor != null) {
								this.cursor.close();
							}
						}
					}
					DataBaseService.this.onDatasetChanged(ChangedType.GET_MEMBERS_BY_TYPE, list);
				}
			}
		}).start();
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		return list;
	}

	public List<Map<String, String>> getMembersByType(String type) {
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		List<Map<String, String>> list = new ArrayList();
		Cursor cursor = dbOpenHelper.mQuery("members", type, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				try {
					Map<String, String> map = new HashMap();
					map.put("number", cursor.getString(cursor.getColumnIndex("number")));
					map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
					map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
					map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
					map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
					map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
					map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
					map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
					map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
					map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
					map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
					map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
					map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
					map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
					map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
					list.add(map);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
			onDatasetChanged(ChangedType.GET_MEMBERS_BY_TYPE, list);
		}
		MyLog.d("xxxxxx", " dataBaseService getMembersByType enter");
		return list;
	}

	public String getPid(String number) {
		Cursor cursor = null;
		String pid = "";
		try {
			cursor = getMembers(number);
			if (cursor != null && cursor.moveToNext()) {
				pid = cursor.getString(cursor.getColumnIndex("tid"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return pid;
	}

	public Cursor getMembers(String number) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery("members", "number= '" + number + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public Member getStringbyItem(String number) {
		return dbOpenHelper.mQueryMember("members", "number='" + number + "'", null);
	}

	public Member getStringbyItems(String number) {
		return dbOpenHelper.mQueryMembers("members", "number='" + number + "'", null);
	}

	public Member getStringbyphone(String number) {
		return dbOpenHelper.mQueryMembers("members", "phone='" + number + "'", null);
	}

	public String getMemberNameByNum(String number) {
		return dbOpenHelper.mQueryMemberNameByNum("members", "number='" + number + "'", null);
	}

	public boolean sameCopmany(String number) {
		boolean find = false;
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery("members", "number='" + number + "'", null);
			if (cursor != null) {
				find = cursor.getCount() > 0;
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return find;
	}

	public void insertAlVersion(String version) {
		MyLog.d("xxxxxx", " dataBaseService insertAlVersion enter");
		dbOpenHelper.delete(AbookOpenHelper.TABLE_ALVERSION, null);
		ContentValues values = new ContentValues();
		values.put(AbookOpenHelper.TABLE_ALVERSION, version);
		dbOpenHelper.insert(AbookOpenHelper.TABLE_ALVERSION, values);
		MyLog.d("xxxxxx", " dataBaseService insertAlVersion exit");
	}

	public void insertMsgVersion(String version) {
		dbOpenHelper.delete("msgversion", null);
		ContentValues values = new ContentValues();
		values.put("msgversion", version);
		dbOpenHelper.insert("msgversion", values);
	}

	public String getCompanyShowflag() {
		return "0";
	}

	public List<String> getAllMessages() {
		Cursor cursor = null;
		List<String> msgList = new ArrayList();
		try {
			cursor = dbOpenHelper.mQuery("messages", null, "message");
			MyLog.e("lan", "cursor count = " + cursor.getCount());
			cursor.moveToFirst();
			msgList.add(cursor.getString(cursor.getColumnIndex("message")));
			if (cursor != null) {
				while (cursor.moveToNext()) {
					msgList.add(cursor.getString(cursor.getColumnIndex("message")));
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return msgList;
	}

	public String getMsgVersion() {
		Cursor cursor = null;
		String alversion = "0";
		try {
			cursor = dbOpenHelper.mQuery("msgversion", null, null);
			if (cursor != null && cursor.moveToNext()) {
				alversion = cursor.getString(cursor.getColumnIndex("msgversion"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return alversion;
	}

	public String getAlversion() {
		Cursor cursor = null;
		String alversion = "0";
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_ALVERSION, null, null);
			if (cursor != null && cursor.moveToNext()) {
				alversion = cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_ALVERSION));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return alversion;
	}

	public String getCompanyId() {
		return "0";
	}

	public Cursor getTeams(String name) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "name= '" + name + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public Cursor getmsg(String msg) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery("messages", "message= '" + msg + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public Cursor getTeamByPid(String pid) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "pid='" + pid + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public Cursor getTeamByid(String pid) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "tid= '" + pid + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public List<Map<String, String>> getTeamsByPid(String pid, boolean isupdate) {
		Cursor cursor = null;
		List<Map<String, String>> list = new ArrayList();
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "pid= '" + pid + "'", null);
			if (cursor != null) {
				do {
					if (cursor.moveToNext()) {
						Map<String, String> map = new HashMap();
						map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
						map.put("name", cursor.getString(cursor.getColumnIndex("name")));
						map.put("pid", cursor.getString(cursor.getColumnIndex("pid")));
						list.add(map);
					} else {
						MyLog.i("ee", "list===>" + list);
						if (isupdate) {
							onDatasetChanged(ChangedType.GET_TEAMS_BY_PID, list);
						}
						if (cursor != null) {
							cursor.close();
						}
					}
				}
				while (!cursor.getString(cursor.getColumnIndex("tid")).equals(cursor.getString(cursor.getColumnIndex("pid"))));
				if (cursor != null) {
					cursor.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Map<String, String>> getTeamsById(final String pid) {
		final List<Map<String, String>> list = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				Cursor cursor = null;
				try {
					cursor = DataBaseService.dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "tid = '" + pid + "'", null);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							Map<String, String> map = new HashMap();
							map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
							map.put("name", cursor.getString(cursor.getColumnIndex("name")));
							map.put("pid", cursor.getString(cursor.getColumnIndex("pid")));
							list.add(map);
						}
						MyLog.i("ee", "list===>" + list);
						DataBaseService.this.onDatasetChanged(ChangedType.GET_TEAMS_BY_PID, list);
						if (cursor != null) {
							cursor.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Throwable th) {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}).start();
		return list;
	}

	public boolean isNoPid(String id) {
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "tid = '" + id + "'", null);
			if (cursor != null) {
				do {
					if (cursor.moveToNext()) {
					}
				} while (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex("pid"))));
				if (cursor != null) {
					cursor.close();
				}
				return false;
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return true;
	}

	public String getPidByTid(String tid) {
		Cursor cursor = null;
		String id = "";
		try {
			cursor = getTeamByid(tid);
			if (cursor != null && cursor.moveToNext()) {
				id = cursor.getString(cursor.getColumnIndex("pid"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;
	}

	public String getNameByTid(String tid) {
		Cursor cursor = null;
		String id = "";
		try {
			cursor = getTeamByid(tid);
			if (cursor != null && cursor.moveToNext()) {
				id = cursor.getString(cursor.getColumnIndex("name"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;
	}

	public String getSectionId(String pid) {
		Cursor cursor = null;
		String id = "";
		try {
			cursor = getTeamByPid(pid);
			if (cursor != null && cursor.moveToNext()) {
				id = cursor.getString(cursor.getColumnIndex("tid"));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;
	}

	public List<Map<String, String>> getTeamsByUser(final String number) {
		final List<Map<String, String>> list = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				Cursor cursor = null;
				try {
					cursor = DataBaseService.dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, "number = '" + number + "'", null);
					if (cursor != null) {
						while (cursor.moveToNext()) {
							Map<String, String> map = new HashMap();
							map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
							map.put("name", cursor.getString(cursor.getColumnIndex("name")));
							map.put("pid", cursor.getString(cursor.getColumnIndex("pid")));
							list.add(map);
						}
						MyLog.i("ee", "list===>" + list);
						DataBaseService.this.onDatasetChanged(ChangedType.GET_TEAMS_BY_PID, list);
						if (cursor != null) {
							cursor.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Throwable th) {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}).start();
		return list;
	}

	public void getAllTeams(final int type) {
		MyLog.d("xxxxxx", "DataBaseService#getAllTeams enter");
		if (AddressBookUtils.ISREQUEST && type == 1) {
			MyLog.d("xxxxxx", "AddressBookUtils.ISREQUEST && (type == 1)");
			return;
		}
		final List<Team> teamlist = new ArrayList();
		new Thread(new Runnable() {
			public void run() {
				MyLog.d("xxxxxx", "DataBaseService#getAllTeams#run enter");
				Cursor cursor = null;
				cursor = DataBaseService.dbOpenHelper.mQuery(AbookOpenHelper.TABLE_TEAMS, null, null);
				if (cursor != null) {
					Team team;
					Team team2 = null;
					while (cursor.moveToNext()) {
						try {
							team = new Team();
						} catch (Exception e) {
							Exception e2 = e;
							team = team2;
						} catch (Throwable th) {
							Throwable th2 = th;
							team = team2;
						}
						try {
							team.setName(cursor.getString(cursor.getColumnIndex("name")));
							team.setId(cursor.getString(cursor.getColumnIndex("tid")));
							teamlist.add(team);
							team2 = team;
						} catch (Exception e3) {
						}
					}
					team = team2;
				}
				MyLog.i("ee", "teamlist==" + teamlist);
				DataBaseService.this.onDatasetChanged(ChangedType.GET_ALL_TEAMS, teamlist);
				Log.v("huangfujianDB", "List<Team> teamlist" + teamlist);
				if (cursor != null) {
					cursor.close();
				}
				if (type == 0) {
					AddressBookUtils.ISREQUEST = false;
				}
				MyLog.d("xxxxxx", "DataBaseService#getAllTeams#run exit");
				try {
					if (cursor != null) {
						cursor.close();
					}
					if (type == 0) {
						AddressBookUtils.ISREQUEST = false;
					}
					MyLog.d("xxxxxx", "DataBaseService#getAllTeams#run exit");
				} catch (Throwable th3) {
					if (cursor != null) {
						cursor.close();
					}
					if (type == 0) {
						AddressBookUtils.ISREQUEST = false;
					}
				}
			}
		}).start();
		MyLog.d("xxxxxx", "DataBaseService#getAllTeams exit");
	}

	public void insertTeam(ContentValues values) {
		MyLog.d("xxxxx", "DataBaseService insertTeam enter");
		if (values != null) {
			dbOpenHelper.insert(AbookOpenHelper.TABLE_TEAMS, values);
		}
		MyLog.d("xxxxx", "DataBaseService insertTeam exit");
	}

	public void insertMembers(ContentValues values) {
		MyLog.d("xxxxx", "DataBaseService insertMembers enter");
		if (values != null) {
			dbOpenHelper.insert("members", values);
		}
		MyLog.d("xxxxx", "DataBaseService insertMembers exit");
	}

	public void insertMsgs(ContentValues values) {
		if (values != null) {
			dbOpenHelper.insert("messages", values);
		}
	}

	public List<Member> queryMembersByKeyword(Context mContext, String keyword, String pid, String id, String sepll) {
		List<Member> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			if (TextUtils.isEmpty(sepll)) {
				cursor = dbOpenHelper.mQuery("members", "pid = '" + pid + "' or pid = '" + id + "'", null);
			} else {
				cursor = dbOpenHelper.mQuery("members", "pid = '" + pid + "' or pid = '" + id + sepll, null);
			}
			MyLog.i("dd", "cursor sou suo = " + cursor);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					String audio = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO));
					String sex = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX));
					String mtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE));
					String positions = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION));
					String gps = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS));
					String pttmap = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP));
					String dtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE));
					String phone = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE));
					String video = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO));
					String pictureupload = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD));
					String smsswitch = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH));
					if (name.contains(keyword) || name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)) {
						Member mem = new Member();
						mem.setmName(name);
						mem.setAudio(audio);
						mem.setNumber(num);
						mem.setDtype(dtype);
						mem.setVideo(video);
						mem.setPictureupload(pictureupload);
						mem.setSmsswitch(smsswitch);
						mem.setGps(gps);
						mem.setPosition(positions);
						mem.setMtype(mtype);
						mem.setPhone(phone);
						mem.setSex(sex);
						mem.setPttmap(pttmap);
						list.add(mem);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Member> queryMembersByKeyword(Context mContext, String keyword, String tid) {
		List<Member> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.mQuery("members", tid, null);
			MyLog.i("dd", "cursor sou suo = " + cursor);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					String audio = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO));
					String showflag = cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG));
					String sex = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX));
					String mtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE));
					String positions = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION));
					String gps = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS));
					String pttmap = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP));
					String dtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE));
					String phone = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE));
					String video = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO));
					String pictureupload = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD));
					String smsswitch = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH));
					if (name.contains(keyword) || name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)) {
						Member mem = new Member();
						mem.setmName(name);
						mem.setAudio(audio);
						mem.setNumber(num);
						mem.setShowflag(showflag);
						mem.setDtype(dtype);
						mem.setVideo(video);
						mem.setPictureupload(pictureupload);
						mem.setSmsswitch(smsswitch);
						mem.setGps(gps);
						mem.setPosition(positions);
						mem.setMtype(mtype);
						mem.setPhone(phone);
						mem.setSex(sex);
						mem.setPttmap(pttmap);
						list.add(mem);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Member> queryMembersByKeyword(Context mContext, String keyword) {
		List<Member> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.query("members", null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					String showflag = cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG));
					String audio = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO));
					String sex = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX));
					String mtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE));
					String positions = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION));
					String gps = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS));
					String pttmap = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP));
					String dtype = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE));
					String phone = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE));
					String video = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO));
					String pictureupload = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD));
					String smsswitch = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH));
					if (name.contains(keyword) || name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)) {
						Member mem = new Member();
						mem.setmName(name);
						mem.setAudio(audio);
						mem.setNumber(num);
						mem.setShowflag(showflag);
						mem.setDtype(dtype);
						mem.setVideo(video);
						mem.setPictureupload(pictureupload);
						mem.setSmsswitch(smsswitch);
						mem.setGps(gps);
						mem.setPosition(positions);
						mem.setMtype(mtype);
						mem.setPhone(phone);
						mem.setSex(sex);
						mem.setPttmap(pttmap);
						list.add(mem);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<ContactPerson> queryContactPersonByKeyword(Context mContext, String keyword) {
		List<ContactPerson> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.query("members", UserMinuteActivity.USER_MNAME);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					if (name.contains(keyword.toLowerCase()) || num.contains(keyword)) {
						ContactPerson cPerson = new ContactPerson();
						cPerson.setContact_name(name);
						cPerson.setContact_num(num);
						list.add(cPerson);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Member> queryAllMembers() {
		List<Member> list = new ArrayList();
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.query("members", null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					Member mem = new Member();
					mem.setmName(name);
					mem.setNumber(num);
					list.add(mem);
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public String getNameByNum(String userNumber) {
		Cursor cursor = null;
		String name = "";
		try {
			cursor = getMembers(userNumber);
			if (cursor != null && cursor.moveToNext()) {
				name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return name;
	}

	public Map<String, String> getMember(String number) {
		Cursor cursor = null;
		Map<String, String> map = new HashMap();
		try {
			cursor = getMembers(number);
			if (cursor != null && cursor.moveToNext()) {
				map.put("number", cursor.getString(cursor.getColumnIndex("number")));
				map.put(UserMinuteActivity.USER_MNAME, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME)));
				map.put(UserMinuteActivity.USER_MTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MTYPE)));
				map.put(UserMinuteActivity.USER_POSITION, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_POSITION)));
				map.put(AbookOpenHelper.TABLE_SHOWFLAG, cursor.getString(cursor.getColumnIndex(AbookOpenHelper.TABLE_SHOWFLAG)));
				map.put(UserMinuteActivity.USER_SEX, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SEX)));
				map.put(UserMinuteActivity.USER_PHONE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PHONE)));
				map.put(UserMinuteActivity.USER_DTYPE, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_DTYPE)));
				map.put(UserMinuteActivity.USER_VIDEO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_VIDEO)));
				map.put(UserMinuteActivity.USER_AUDIO, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_AUDIO)));
				map.put(UserMinuteActivity.USER_PTTMAP, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PTTMAP)));
				map.put(UserMinuteActivity.USER_GPS, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_GPS)));
				map.put(UserMinuteActivity.USER_PICTUREUPLPAD, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_PICTUREUPLPAD)));
				map.put(UserMinuteActivity.USER_SMSSWITCH, cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_SMSSWITCH)));
				map.put("tid", cursor.getString(cursor.getColumnIndex("tid")));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return map;
	}

	public List<Member> queryMembersByKey(Context mContext, String keyword, String pid, String id, String sepll) {
		List<Member> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			if (TextUtils.isEmpty(sepll)) {
				cursor = dbOpenHelper.mQuery("members", "pid = '" + pid + "' or pid = '" + id + "'", null);
			} else {
				cursor = dbOpenHelper.mQuery("members", "pid = '" + pid + "' or pid = '" + id + sepll, null);
			}
			MyLog.i("dd", "cursor sou suo = " + cursor);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					if (name.contains(keyword) || name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)) {
						Member mem = new Member();
						mem.setmName(name);
						mem.setNumber(num);
						list.add(mem);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public List<Member> queryMembersByKey(Context mContext, String keyword) {
		List<Member> list = new ArrayList();
		dbOpenHelper = AbookOpenHelper.getInstance(mContext);
		Cursor cursor = null;
		try {
			cursor = dbOpenHelper.query("members", null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String name = cursor.getString(cursor.getColumnIndex(UserMinuteActivity.USER_MNAME));
					String num = cursor.getString(cursor.getColumnIndex("number"));
					if (name.contains(keyword) || name.toLowerCase().contains(keyword.toLowerCase()) || num.contains(keyword)) {
						Member mem = new Member();
						mem.setmName(name);
						mem.setNumber(num);
						list.add(mem);
					}
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		} catch (Throwable th) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}
}
