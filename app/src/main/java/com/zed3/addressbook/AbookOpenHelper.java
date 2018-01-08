package com.zed3.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zed3.sipua.SipUAApp;

public class AbookOpenHelper extends SQLiteOpenHelper {
	public static String DB_NAME;
	private static final int DB_VERSION = 3;
	private static final String SQL_CREATE_TABLE_ALVERSION = "CREATE TABLE IF NOT EXISTS alversion (id integer PRIMARY KEY AUTOINCREMENT, alversion text )";
	private static final String SQL_CREATE_TABLE_MEMBERS = "CREATE TABLE IF NOT EXISTS  members (id integer PRIMARY KEY AUTOINCREMENT, number text ,  mname text ,  mtype text ,  showflag text , dtype text ,  sex text ,  position text ,  phone text ,  video text ,  audio text ,  pttmap text ,  gps text , pictureupload text , smsswitch text , tid text )";
	private static final String SQL_CREATE_TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS messages (id integer PRIMARY KEY AUTOINCREMENT, message text )";
	private static final String SQL_CREATE_TABLE_MSGVERSION = "CREATE TABLE IF NOT EXISTS msgversion (id integer PRIMARY KEY AUTOINCREMENT, msgversion text )";
	private static final String SQL_CREATE_TABLE_TEAMS = "CREATE TABLE IF NOT EXISTS teams (tid text PRIMARY KEY, name text ,  pid text , pbxid text)";
	public static final String TABLE_ALVERSION = "alversion";
	public static final String TABLE_ID = "id";
	public static final String TABLE_MEMBERS = "members";
	public static final String TABLE_MESSAGES = "messages";
	public static final String TABLE_MSGVERSION = "msgversion";
	public static final String TABLE_PBXID = "pbxid";
	public static final String TABLE_SHOWFLAG = "showflag";
	public static final String TABLE_TEAMS = "teams";
	private static final String TAG = "AbookOpenHelper";
	private static AbookOpenHelper gInst;

	static {
		AbookOpenHelper.DB_NAME = "";
		AbookOpenHelper.gInst = null;
	}

	private AbookOpenHelper(final Context context) {
		super(context, AbookOpenHelper.DB_NAME, (SQLiteDatabase.CursorFactory) null, 3);
	}

	public static String getDBName() {
		final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		return String.valueOf(sharedPreferences.getString("username", "")) + sharedPreferences.getString("server", "") + ".db";
	}

	public static AbookOpenHelper getInstance(final Context context) {
		synchronized (AbookOpenHelper.class) {
			if (AbookOpenHelper.gInst == null) {
				(AbookOpenHelper.gInst = new AbookOpenHelper(context)).getWritableDatabase();
				AbookOpenHelper.gInst.setWriteAheadLoggingEnabled(true);
			}
			return AbookOpenHelper.gInst;
		}
	}

	public static void setDBName() {
		AbookOpenHelper.DB_NAME = getDBName();
		Log.i("xxxxx", "dbname = " + AbookOpenHelper.DB_NAME);
	}

	public void delete(final String s, final String s2) {
		synchronized (this) {
			try {
				this.getWritableDatabase().delete(s, s2, (String[]) null);
			} catch (Exception ex) {
				Log.e("AbookOpenHelper", "delete from " + s + "error:");
			}
		}
	}

	public void insert(final String s, final ContentValues contentValues) {
		synchronized (this) {
			final SQLiteDatabase writableDatabase = this.getWritableDatabase();
			try {
				writableDatabase.insert(s, (String) null, contentValues);
			} catch (Exception ex) {
				Log.e("AbookOpenHelper", "insert into " + s + "error:");
				ex.printStackTrace();
			}
		}
	}

	public Cursor mQuery(final String s, final String s2, final String s3) {
		synchronized (this) {
			final SQLiteDatabase writableDatabase = this.getWritableDatabase();
			final Cursor cursor = null;
			try {
				return writableDatabase.query(s, (String[]) null, s2, (String[]) null, (String) null, (String) null, s3);
			} catch (Exception ex) {
				ex.printStackTrace();
				final Cursor query = cursor;
			}
			return cursor;
		}
	}

	public Member mQueryMember(final String p0, final String p1, final String p2) {
		// TODO
		return null;
	}

	public String mQueryMemberNameByNum(final String s, String s2, String string) {
		while (true) {
			Label_0129:
			{
				synchronized (this) {
					final SQLiteDatabase writableDatabase = this.getWritableDatabase();
					Cursor cursor = null;
					Cursor cursor2 = null;
					try {
						final Cursor query = writableDatabase.query(s, (String[]) null, s2, (String[]) null, (String) null, (String) null, string);
						if (query == null) {
							break Label_0129;
						}
						cursor2 = query;
						cursor = query;
						if (query.moveToNext()) {
							cursor2 = query;
							cursor = query;
							s2 = (string = query.getString(query.getColumnIndex("mname")));
							if (query != null) {
								query.close();
								string = s2;
							}
							return string;
						}
					} catch (Exception ex) {
						cursor = cursor2;
						ex.printStackTrace();
					} finally {
						if (cursor != null) {
							cursor.close();
						}
					}
				}
			}
			final Cursor cursor3 = null;
			if (cursor3 != null) {
				cursor3.close();
			}
			string = "";
			return string;
		}
	}

	public Member mQueryMembers(final String p0, final String p1, final String p2) {
		// TODO
		return null;
	}

	public void onCreate(final SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS teams (tid text PRIMARY KEY, name text ,  pid text , pbxid text)");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS  members (id integer PRIMARY KEY AUTOINCREMENT, number text ,  mname text ,  mtype text ,  showflag text , dtype text ,  sex text ,  position text ,  phone text ,  video text ,  audio text ,  pttmap text ,  gps text , pictureupload text , smsswitch text , tid text )");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS alversion (id integer PRIMARY KEY AUTOINCREMENT, alversion text )");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages (id integer PRIMARY KEY AUTOINCREMENT, message text )");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS msgversion (id integer PRIMARY KEY AUTOINCREMENT, msgversion text )");
	}

	public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int n, final int n2) {
		switch (n2) {
			case 2: {
				sqLiteDatabase.execSQL("delete from teams");
				sqLiteDatabase.execSQL("delete from members");
				sqLiteDatabase.execSQL("delete from alversion");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD showflag text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD dtype text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD sex text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD position text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD phone text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD video text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD audio text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD pttmap text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD gps text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD pictureupload text");
				sqLiteDatabase.execSQL("ALTER TABLE members ADD smsswitch text");
				Log.v("huangfujian", "\u589e\u52a0\u5b57\u6bb5\uff01\uff01\uff01\uff01\uff01");
			}
			case 3: {
				if (n == 1 && n2 == 3) {
					sqLiteDatabase.execSQL("delete from teams");
					sqLiteDatabase.execSQL("delete from members");
					sqLiteDatabase.execSQL("delete from alversion");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD showflag text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD dtype text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD sex text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD position text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD phone text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD video text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD audio text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD pttmap text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD gps text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD pictureupload text");
					sqLiteDatabase.execSQL("ALTER TABLE members ADD smsswitch text");
					Log.v("huangfujian", "1-3\u589e\u52a0\u5b57\u6bb5\uff01\uff01\uff01\uff01\uff01");
					return;
				}
				if (n == 2 && n2 == 3) {
					sqLiteDatabase.execSQL("delete from teams");
					sqLiteDatabase.execSQL("delete from members");
					sqLiteDatabase.execSQL("delete from alversion");
					sqLiteDatabase.execSQL("delete from eid");
					Log.v("rr", "2-3\u589e\u52a0\u5b57\u6bb5\uff01\uff01\uff01\uff01\uff01");
					return;
				}
				break;
			}
		}
	}

	public Cursor query(final String s, final String s2) {
		synchronized (this) {
			final SQLiteDatabase readableDatabase = this.getReadableDatabase();
			Cursor query = null;
			try {
				final Cursor cursor = query = readableDatabase.query(s, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, s2);
				Log.i("AbookOpenHelper", "cursor.count = " + cursor.getCount());
				return cursor;
			} catch (Exception ex) {
				Log.e("AbookOpenHelper", "query from " + s + "error:");
				ex.printStackTrace();
				final Cursor cursor = query;
			}
			return null;
		}
	}

	public void update(final String s, final String s2, final ContentValues contentValues) {
		synchronized (this) {
			final SQLiteDatabase writableDatabase = this.getWritableDatabase();
			try {
				writableDatabase.update(s, contentValues, s2, (String[]) null);
			} catch (Exception ex) {
				Log.e("AbookOpenHelper", "update table " + s + "error, where = " + s2);
				ex.printStackTrace();
			}
		}
	}
}
