package com.zed3.sipua;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;

public class CallHistoryDatabase extends SQLiteOpenHelper {
	private static final String CREATE_TEMP_TABLE = "alter table call_history rename to temp_call_history";
	private static final String DB_NAME = "callhistory.db";
	private static final int DB_VERSION = 2;
	private static final String DROP_TEMP_TABLE = "drop table temp_call_history";
	private static final String INSERT_DATA = "insert into call_history(_id,type,begin,end,begin_str,name,number) select _id,type,begin,end,begin_str,name,number from temp_call_history";
	public static String Table_Name = ("callhistory_" + MemoryMg.getInstance().TerminalNum);
	private static CallHistoryDatabase mCallHistroy;
	private static String SQL_CREATE_CALL_HISTORY_TABLE = ("CREATE TABLE IF NOT EXISTS " + Table_Name + " (_id integer PRIMARY KEY AUTOINCREMENT , " + "type text ,  " + "begin integer , " + "end integer , " + "begin_str text , " + "name text , " + "number text ," + "status integer DEFAULT 0)");
	private static final String TAG = "guojunfeng-CallHistoryDatabase";

	public void delete1(java.lang.String r7, java.lang.String r8, java.lang.String[] r9) {
		// TODO
	}

	private CallHistoryDatabase(Context context) {
		super(context, DB_NAME, null, 2);
	}

	public static CallHistoryDatabase getInstance(Context context) {
		if (mCallHistroy == null) {
			mCallHistroy = new CallHistoryDatabase(context);
		}
		return mCallHistroy;
	}

	private void setSQL(String table) {
		SQL_CREATE_CALL_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + table + " (_id integer PRIMARY KEY AUTOINCREMENT , " + "type text ,  " + "begin integer , " + "end integer , " + "begin_str text , " + "name text , " + "number text ," + "status integer DEFAULT 0)";
	}

	public synchronized void createTable(String table) {
		setSQL(table);
		SQLiteDatabase database = getWritableDatabase();
		try {
			Log.i("jiangkai", "SQL_CREATE_CALL_HISTORY_TABLE    " + SQL_CREATE_CALL_HISTORY_TABLE);
			database.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onCreate(SQLiteDatabase db) {
		try {
			Log.i("jiangkai", "onCreate  SQL_CREATE_CALL_HISTORY_TABLE    " + SQL_CREATE_CALL_HISTORY_TABLE);
			db.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
		} catch (SQLException e) {
			MyLog.e(TAG, "create table error");
			e.printStackTrace();
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) {
			switch (newVersion) {
				case 2:
					db.beginTransaction();
					db.execSQL(CREATE_TEMP_TABLE);
					db.execSQL(SQL_CREATE_CALL_HISTORY_TABLE);
					db.execSQL(INSERT_DATA);
					db.execSQL(DROP_TEMP_TABLE);
					db.setTransactionSuccessful();
					db.endTransaction();
					return;
				default:
					return;
			}
		}
	}

	public synchronized Cursor query(String table_name, String order) {
		Cursor cursor;
		cursor = null;
		try {
			cursor = getReadableDatabase().query(table_name, null, null, null, null, null, order);
			MyLog.i(TAG, "cursor.count = " + cursor.getCount());
		} catch (Exception e) {
			MyLog.e(TAG, "query from " + table_name + "error:");
			e.printStackTrace();
		}
		return cursor;
	}

	public synchronized Cursor mQuery(String table_name, String selection) {
		Cursor cursor;
		cursor = null;
		try {
			cursor = getWritableDatabase().query(table_name, null, selection, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public synchronized Cursor memberRecordQuery(String table_name) {
		Cursor cursor;
		cursor = null;
		try {
			cursor = getWritableDatabase().rawQuery("select *, count(distinct number) from " + table_name + " group by number order by begin_str desc", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public synchronized Cursor memberRecordQuery(String table_name, String num) {
		Cursor cursor;
		cursor = null;
		try {
			cursor = getWritableDatabase().rawQuery("select * from " + table_name + "  where number='" + num + "' order by begin_str desc", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public synchronized void insert(String table_name, ContentValues values) {
		SQLiteDatabase database = getWritableDatabase();
		try {
			database.insert(table_name, null, values);
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Exception e) {
			MyLog.e(TAG, "insert into " + table_name + "error:");
			e.printStackTrace();
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Throwable th) {
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		}
	}

	public synchronized void delete(String table_name, String where) {
		SQLiteDatabase database = null;
		try {
			database = getWritableDatabase();
			database.delete(table_name, where, null);
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Exception e) {
			MyLog.e(TAG, "delete from " + table_name + "error:");
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Throwable th) {
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		}
	}

	public synchronized void update(String table_name, String where, ContentValues values) {
		SQLiteDatabase database = getWritableDatabase();
		try {
			database.update(table_name, values, where, null);
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Exception e) {
			MyLog.e(TAG, "update table " + table_name + "error, where = " + where);
			e.printStackTrace();
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		} catch (Throwable th) {
			if (database != null) {
				if (database.isOpen()) {
					database.close();
				}
			}
		}
	}
}
