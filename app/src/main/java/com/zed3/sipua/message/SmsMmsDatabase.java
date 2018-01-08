package com.zed3.sipua.message;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;

public class SmsMmsDatabase extends SQLiteOpenHelper {
	private static final String CREATE_TEMP_TABLE = "alter table message_talk rename to temp_message_talk";
	private static final String DB_NAME = "message.db";
	private static final int DB_VERSION = 2;
	private static final String DROP_TEMP_TABLE = "drop table temp_message_talk";
	private static final String INSERT_DATA = "insert into message_talk(_id,E_id,local_number,server_ip,address,contact_name,sip_name,body,status,mark,attachment,attachment_name,send,type,date) select _id,E_id,local_number,server_ip,address,contact_name,sip_name,body,status,mark,attachment,attachment_name,send,type,date from temp_message_talk";
	public static final String SMS_MMS_DATABASE_CHANGED = "database_changed";
	private static final String SQL_CREATE_MESSAGE_DRAFT_TABLE = "CREATE TABLE message_draft (_id integer PRIMARY KEY AUTOINCREMENT , address text , body text , save_time text)";
	private static final String SQL_CREATE_MESSAGE_TALK_TABLE = "CREATE TABLE message_talk (_id integer PRIMARY KEY AUTOINCREMENT , E_id text ,  local_number text ,  server_ip text ,  address text , contact_name text , sip_name text , body text , status integer DEFAULT 0 , mark integer DEFAULT 0 , attachment text , attachment_name text, send integer DEFAULT 2, type text , date text)";
	private static final String SQL_DROP_MESSAGE_TALK_TABLE = "DROP TABLE message_talk";
	public static final String TABLE_MESSAGE_TALK = "message_talk";
	private static final String TAG = "SmsMmsDatabase";
	protected static final int TYPE_RECEIVE = 0;
	protected static final int TYPE_SEND = 1;

	public SmsMmsDatabase(final Context context) {
		super(context, "message.db", (SQLiteDatabase.CursorFactory) null, 2);
	}

	private void sendDataBaseChangedBroadCast() {
		SipUAApp.getAppContext().sendBroadcast(new Intent("database_changed"));
	}

	public void delete(final String s, final String s2) {
		SQLiteDatabase sqLiteDatabase = null;
		SQLiteDatabase writableDatabase = null;
		while (true) {
			try {
				final SQLiteDatabase sqLiteDatabase2 = sqLiteDatabase = (writableDatabase = this.getWritableDatabase());
				sqLiteDatabase2.delete(s, s2, (String[]) null);
				if (sqLiteDatabase2 != null && sqLiteDatabase2.isOpen()) {
					sqLiteDatabase2.close();
				}
				this.sendDataBaseChangedBroadCast();
			} catch (Exception ex) {
				sqLiteDatabase = writableDatabase;
				MyLog.e("SmsMmsDatabase", "delete from " + s + "error:" + ex.toString());
				sqLiteDatabase = writableDatabase;
				Toast.makeText(Receiver.mContext, R.string.database_error, Toast.LENGTH_SHORT).show();
				if (writableDatabase != null && writableDatabase.isOpen()) {
					writableDatabase.close();
				}
				continue;
			} finally {
				if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
					sqLiteDatabase.close();
				}
			}
			break;
		}
	}

	public String getIdByE_id(String s, final String s2) {
		final String s3 = null;
		final String s4 = null;
		SQLiteDatabase sqLiteDatabase = null;
		SQLiteDatabase readableDatabase = null;
		final Cursor cursor = null;
		Cursor cursor3;
		final Cursor cursor2 = cursor3 = null;
		Cursor cursor4 = cursor;
		try {
			final SQLiteDatabase sqLiteDatabase2 = readableDatabase = this.getReadableDatabase();
			cursor3 = cursor2;
			sqLiteDatabase = sqLiteDatabase2;
			cursor4 = cursor;
			final Cursor query = sqLiteDatabase2.query(s, (String[]) null, "E_id = ?", new String[]{s2}, (String) null, (String) null, (String) null);
			String string = s4;
			if (query != null) {
				string = s4;
				readableDatabase = sqLiteDatabase2;
				cursor3 = query;
				sqLiteDatabase = sqLiteDatabase2;
				cursor4 = query;
				if (query.moveToNext()) {
					readableDatabase = sqLiteDatabase2;
					cursor3 = query;
					sqLiteDatabase = sqLiteDatabase2;
					cursor4 = query;
					string = query.getString(query.getColumnIndexOrThrow("_id"));
				}
			}
			if (query != null) {
				query.close();
			}
			s = string;
			if (sqLiteDatabase2 != null) {
				s = string;
				if (sqLiteDatabase2.isOpen()) {
					sqLiteDatabase2.close();
					s = string;
				}
			}
			return s;
		} catch (Exception ex) {
			sqLiteDatabase = readableDatabase;
			cursor4 = cursor3;
			MyLog.e("SmsMmsDatabase", "getIdByE_id from " + s + "error, E_id = " + s2 + " " + ex.toString());
			if (cursor3 != null) {
				cursor3.close();
			}
			s = s3;
			if (readableDatabase == null) {
				return s;
			}
			s = s3;
			if (readableDatabase.isOpen()) {
				readableDatabase.close();
				return null;
			}
			return s;
		} finally {
			if (cursor4 != null) {
				cursor4.close();
			}
			if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
				sqLiteDatabase.close();
			}
		}
	}

	public void insert(final String s, final ContentValues contentValues) {
		SQLiteDatabase sqLiteDatabase = null;
		SQLiteDatabase writableDatabase = null;
		while (true) {
			try {
				final SQLiteDatabase sqLiteDatabase2 = sqLiteDatabase = (writableDatabase = this.getWritableDatabase());
				sqLiteDatabase2.insert(s, (String) null, contentValues);
				if (sqLiteDatabase2 != null && sqLiteDatabase2.isOpen()) {
					sqLiteDatabase2.close();
				}
				this.sendDataBaseChangedBroadCast();
			} catch (Exception ex) {
				sqLiteDatabase = writableDatabase;
				MyLog.e("SmsMmsDatabase", "insert into " + s + "error:" + ex.toString());
				sqLiteDatabase = writableDatabase;
				Toast.makeText(Receiver.mContext, R.string.database_error, Toast.LENGTH_SHORT).show();
				if (writableDatabase != null && writableDatabase.isOpen()) {
					writableDatabase.close();
				}
				continue;
			} finally {
				if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
					sqLiteDatabase.close();
				}
			}
			break;
		}
	}

	public Cursor mQuery(final String p0, final String p1, final String p2, final String p3) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: monitorenter
		//     2: aconst_null
		//     3: astore          7
		//     5: aconst_null
		//     6: astore          5
		//     8: aconst_null
		//     9: astore          9
		//    11: aload           9
		//    13: astore          6
		//    15: aload_0
		//    16: invokevirtual   com/zed3/sipua/message/SmsMmsDatabase.getReadableDatabase:()Landroid/database/sqlite/SQLiteDatabase;
		//    19: astore          8
		//    21: aload           8
		//    23: astore          5
		//    25: aload           9
		//    27: astore          6
		//    29: aload           8
		//    31: astore          7
		//    33: aload           8
		//    35: aload_1
		//    36: aconst_null
		//    37: aload_2
		//    38: aconst_null
		//    39: aload_3
		//    40: aconst_null
		//    41: aload           4
		//    43: invokevirtual   android/database/sqlite/SQLiteDatabase.query:(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
		//    46: astore_2
		//    47: aload           8
		//    49: astore          5
		//    51: aload_2
		//    52: astore          6
		//    54: aload           8
		//    56: astore          7
		//    58: ldc             "SmsMmsDatabase"
		//    60: new             Ljava/lang/StringBuilder;
		//    63: dup
		//    64: ldc             "cursor.count = "
		//    66: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    69: aload_2
		//    70: invokeinterface android/database/Cursor.getCount:()I
		//    75: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//    78: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    81: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//    84: aload_2
		//    85: astore_1
		//    86: aload           8
		//    88: ifnull          108
		//    91: aload_2
		//    92: astore_1
		//    93: aload           8
		//    95: invokevirtual   android/database/sqlite/SQLiteDatabase.isOpen:()Z
		//    98: ifeq            108
		//   101: aload           8
		//   103: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
		//   106: aload_2
		//   107: astore_1
		//   108: aload_0
		//   109: monitorexit
		//   110: aload_1
		//   111: areturn
		//   112: astore_2
		//   113: aload           5
		//   115: astore          7
		//   117: ldc             "SmsMmsDatabase"
		//   119: new             Ljava/lang/StringBuilder;
		//   122: dup
		//   123: ldc             "query from "
		//   125: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   128: aload_1
		//   129: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   132: ldc             "error:"
		//   134: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   137: aload_2
		//   138: invokevirtual   java/lang/Exception.toString:()Ljava/lang/String;
		//   141: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   144: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   147: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   150: aload           5
		//   152: astore          7
		//   154: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//   157: ldc             R.string.database_error
		//   159: iconst_0
		//   160: invokestatic    android/widget/Toast.makeText:(Landroid/content/Context;II)Landroid/widget/Toast;
		//   163: invokevirtual   android/widget/Toast.show:()V
		//   166: aload           6
		//   168: astore_1
		//   169: aload           5
		//   171: ifnull          108
		//   174: aload           6
		//   176: astore_1
		//   177: aload           5
		//   179: invokevirtual   android/database/sqlite/SQLiteDatabase.isOpen:()Z
		//   182: ifeq            108
		//   185: aload           5
		//   187: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
		//   190: aload           6
		//   192: astore_1
		//   193: goto            108
		//   196: astore_1
		//   197: aload_0
		//   198: monitorexit
		//   199: aload_1
		//   200: athrow
		//   201: astore_1
		//   202: aload           7
		//   204: ifnull          220
		//   207: aload           7
		//   209: invokevirtual   android/database/sqlite/SQLiteDatabase.isOpen:()Z
		//   212: ifeq            220
		//   215: aload           7
		//   217: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
		//   220: aload_1
		//   221: athrow
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  15     21     112    196    Ljava/lang/Exception;
		//  15     21     201    222    Any
		//  33     47     112    196    Ljava/lang/Exception;
		//  33     47     201    222    Any
		//  58     84     112    196    Ljava/lang/Exception;
		//  58     84     201    222    Any
		//  93     106    196    201    Any
		//  117    150    201    222    Any
		//  154    166    201    222    Any
		//  177    190    196    201    Any
		//  207    220    196    201    Any
		//  220    222    196    201    Any
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0108:
		//     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
		//     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2596)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		//     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		//     at us.deathmarine.luyten.FileSaver.access.300(FileSaver.java:45)
		//     at us.deathmarine.luyten.FileSaver.4.run(FileSaver.java:112)
		//     at java.lang.Thread.run(Thread.java:745)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
	}

	public void mUpdate(final String s, final String s2, final ContentValues contentValues, final String[] array) {
		SQLiteDatabase sqLiteDatabase = null;
		SQLiteDatabase writableDatabase = null;
		while (true) {
			try {
				final SQLiteDatabase sqLiteDatabase2 = sqLiteDatabase = (writableDatabase = this.getWritableDatabase());
				sqLiteDatabase2.update(s, contentValues, s2, array);
				if (sqLiteDatabase2 != null && sqLiteDatabase2.isOpen()) {
					sqLiteDatabase2.close();
				}
				this.sendDataBaseChangedBroadCast();
			} catch (Exception ex) {
				sqLiteDatabase = writableDatabase;
				MyLog.e("SmsMmsDatabase", "update table " + s + "error, where = " + s2 + " " + ex.toString());
				sqLiteDatabase = writableDatabase;
				Toast.makeText(Receiver.mContext, R.string.database_error, Toast.LENGTH_SHORT).show();
				if (writableDatabase != null && writableDatabase.isOpen()) {
					writableDatabase.close();
				}
				continue;
			} finally {
				if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
					sqLiteDatabase.close();
				}
			}
			break;
		}
	}

	public void onCreate(final SQLiteDatabase sqLiteDatabase) {
		MyLog.i("SmsMmsDatabase", "begin create table");
		try {
			sqLiteDatabase.execSQL("CREATE TABLE message_talk (_id integer PRIMARY KEY AUTOINCREMENT , E_id text ,  local_number text ,  server_ip text ,  address text , contact_name text , sip_name text , body text , status integer DEFAULT 0 , mark integer DEFAULT 0 , attachment text , attachment_name text, send integer DEFAULT 2, type text , date text)");
			sqLiteDatabase.execSQL("CREATE TABLE message_draft (_id integer PRIMARY KEY AUTOINCREMENT , address text , body text , save_time text)");
		} catch (Exception ex) {
			MyLog.e("SmsMmsDatabase", "create table error: " + ex.toString());
		}
	}

	public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int n, final int n2) {
		// TODO
	}

	public Cursor query(String o, final String s) {
		// TODO
		return null;
	}

	public void update(final String s, final String s2, final ContentValues contentValues) {
		SQLiteDatabase sqLiteDatabase = null;
		SQLiteDatabase writableDatabase = null;
		while (true) {
			try {
				final SQLiteDatabase sqLiteDatabase2 = sqLiteDatabase = (writableDatabase = this.getWritableDatabase());
				sqLiteDatabase2.update(s, contentValues, s2, (String[]) null);
				if (sqLiteDatabase2 != null && sqLiteDatabase2.isOpen()) {
					sqLiteDatabase2.close();
				}
				this.sendDataBaseChangedBroadCast();
			} catch (Exception ex) {
				sqLiteDatabase = writableDatabase;
				MyLog.e("SmsMmsDatabase", "update table " + s + "error, where = " + s2 + " " + ex.toString());
				sqLiteDatabase = writableDatabase;
				Toast.makeText(Receiver.mContext, R.string.database_error, Toast.LENGTH_SHORT).show();
				if (writableDatabase != null && writableDatabase.isOpen()) {
					writableDatabase.close();
				}
				continue;
			} finally {
				if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
					sqLiteDatabase.close();
				}
			}
			break;
		}
	}
}
