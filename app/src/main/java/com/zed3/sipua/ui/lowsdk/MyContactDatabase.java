package com.zed3.sipua.ui.lowsdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zed3.log.MyLog;

public class MyContactDatabase extends SQLiteOpenHelper {
	public static final String CONTACT_NAME = "contact_name";
	public static final String CONTACT_NUM = "contact_num";
	public static final String CUR_LOGINER = "cur_loginer";
	private static final String DB_NAME = "pttcontact_login.db";
	private static final int DB_VERSION = 1;
	private static final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE pttcontact_login (_id integer PRIMARY KEY AUTOINCREMENT , cur_loginer text ,  contact_num text ,  contact_name text , used_times integer DEFAULT 0)";
	private static final String TABLE_NAME = "pttcontact_login";
	private static final String TAG = "MyContactDatabase";
	public static final String USED_TIMES = "used_times";

	public MyContactDatabase(final Context context) {
		super(context, "pttcontact_login.db", (SQLiteDatabase.CursorFactory) null, 1);
	}

	public void delete(final String s) {
		try {
			this.getWritableDatabase().delete("pttcontact_login", s, (String[]) null);
		} catch (Exception ex) {
			MyLog.e("MyContactDatabase", "delete from pttcontact_loginerror:" + ex.toString());
		}
	}

	public void insert(final ContentValues contentValues) {
		try {
			this.getWritableDatabase().insert("pttcontact_login", (String) null, contentValues);
		} catch (Exception ex) {
			MyLog.e("MyContactDatabase", "insert into pttcontact_loginerror:" + ex.toString());
		}
	}

	public Cursor mQuery(final String s, final String s2, final String s3) {
		try {
			return this.getReadableDatabase().query("pttcontact_login", (String[]) null, s, (String[]) null, s2, (String) null, s3);
		} catch (Exception ex) {
			MyLog.e("MyContactDatabase", "query from pttcontact_loginerror:" + ex.toString());
			return null;
		}
	}

	public void onCreate(final SQLiteDatabase sqLiteDatabase) {
		try {
			sqLiteDatabase.execSQL("CREATE TABLE pttcontact_login (_id integer PRIMARY KEY AUTOINCREMENT , cur_loginer text ,  contact_num text ,  contact_name text , used_times integer DEFAULT 0)");
		} catch (Exception ex) {
			MyLog.e("MyContactDatabase", "create table error: " + ex.toString());
		}
	}

	public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int n, final int n2) {
	}

	public void update(final String s, final ContentValues contentValues) {
		try {
			this.getWritableDatabase().update("pttcontact_login", contentValues, s, (String[]) null);
		} catch (Exception ex) {
			MyLog.e("MyContactDatabase", "update table pttcontact_loginerror, where = " + s + " " + ex.toString());
		}
	}
}
