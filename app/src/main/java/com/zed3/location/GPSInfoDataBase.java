package com.zed3.location;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.log.MyLog;
import com.zed3.sipua.ui.Receiver;

import java.util.List;

public class GPSInfoDataBase extends SQLiteOpenHelper
{
    private static final String DB_NAME = "gpsInfo.db";
    private static final int DB_VERSION = 1;
    private static final String SQL_CREATE_GPSINFO_TABLE = "CREATE TABLE gps_info(_id integer PRIMARY KEY AUTOINCREMENT , gps_x text ,  gps_y text , gps_speed text, gps_height text , gps_direction  text ,UnixTime text ,real_time text ,E_id text )";
    private static final String TAG = "GpsInfoDataBase";
    private static GPSInfoDataBase instance;
    
    public GPSInfoDataBase(final Context context) {
        super(context, "gpsInfo.db", (SQLiteDatabase.CursorFactory)null, 1);
    }
    
    public static List<GpsInfo> getInfos() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     2: monitorenter   
        //     3: new             Ljava/util/ArrayList;
        //     6: dup            
        //     7: invokespecial   java/util/ArrayList.<init>:()V
        //    10: astore_2       
        //    11: invokestatic    com/zed3/location/GPSInfoDataBase.getInstance:()Lcom/zed3/location/GPSInfoDataBase;
        //    14: ldc             "gps_info"
        //    16: aconst_null    
        //    17: invokespecial   com/zed3/location/GPSInfoDataBase.query:(Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
        //    20: astore_1       
        //    21: aload_1        
        //    22: ifnull          78
        //    25: aconst_null    
        //    26: astore_0       
        //    27: aload_1        
        //    28: invokeinterface android/database/Cursor.moveToNext:()Z
        //    33: ifne            83
        //    36: ldc             "GpsInfoDataBase"
        //    38: new             Ljava/lang/StringBuilder;
        //    41: dup            
        //    42: ldc             "gpsList:"
        //    44: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
        //    47: aload_2        
        //    48: invokeinterface java/util/List.size:()I
        //    53: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
        //    56: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
        //    59: invokestatic    android/util/Log.i:(Ljava/lang/String;Ljava/lang/String;)I
        //    62: pop            
        //    63: aload_1        
        //    64: invokeinterface android/database/Cursor.close:()V
        //    69: invokestatic    com/zed3/location/GPSInfoDataBase.getInstance:()Lcom/zed3/location/GPSInfoDataBase;
        //    72: invokevirtual   com/zed3/location/GPSInfoDataBase.getReadableDatabase:()Landroid/database/sqlite/SQLiteDatabase;
        //    75: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //    78: ldc             Lcom/zed3/location/GPSInfoDataBase;.class
        //    80: monitorexit    
        //    81: aload_2        
        //    82: areturn        
        //    83: new             Lcom/zed3/location/GpsInfo;
        //    86: dup            
        //    87: invokespecial   com/zed3/location/GpsInfo.<init>:()V
        //    90: astore_0       
        //    91: aload_0        
        //    92: aload_1        
        //    93: aload_1        
        //    94: ldc             "gps_x"
        //    96: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   101: invokeinterface android/database/Cursor.getDouble:(I)D
        //   106: putfield        com/zed3/location/GpsInfo.gps_x:D
        //   109: aload_0        
        //   110: aload_1        
        //   111: aload_1        
        //   112: ldc             "gps_y"
        //   114: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   119: invokeinterface android/database/Cursor.getDouble:(I)D
        //   124: putfield        com/zed3/location/GpsInfo.gps_y:D
        //   127: aload_0        
        //   128: aload_1        
        //   129: aload_1        
        //   130: ldc             "gps_speed"
        //   132: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   137: invokeinterface android/database/Cursor.getFloat:(I)F
        //   142: putfield        com/zed3/location/GpsInfo.gps_speed:F
        //   145: aload_0        
        //   146: aload_1        
        //   147: aload_1        
        //   148: ldc             "gps_height"
        //   150: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   155: invokeinterface android/database/Cursor.getFloat:(I)F
        //   160: putfield        com/zed3/location/GpsInfo.gps_height:F
        //   163: aload_0        
        //   164: aload_1        
        //   165: aload_1        
        //   166: ldc             "UnixTime"
        //   168: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   173: invokeinterface android/database/Cursor.getLong:(I)J
        //   178: putfield        com/zed3/location/GpsInfo.UnixTime:J
        //   181: aload_0        
        //   182: aload_1        
        //   183: aload_1        
        //   184: ldc             "E_id"
        //   186: invokeinterface android/database/Cursor.getColumnIndex:(Ljava/lang/String;)I
        //   191: invokeinterface android/database/Cursor.getString:(I)Ljava/lang/String;
        //   196: putfield        com/zed3/location/GpsInfo.E_id:Ljava/lang/String;
        //   199: aload_2        
        //   200: aload_0        
        //   201: invokeinterface java/util/List.add:(Ljava/lang/Object;)Z
        //   206: pop            
        //   207: goto            27
        //   210: astore_0       
        //   211: aload_1        
        //   212: invokeinterface android/database/Cursor.close:()V
        //   217: invokestatic    com/zed3/location/GPSInfoDataBase.getInstance:()Lcom/zed3/location/GPSInfoDataBase;
        //   220: invokevirtual   com/zed3/location/GPSInfoDataBase.getReadableDatabase:()Landroid/database/sqlite/SQLiteDatabase;
        //   223: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //   226: aload_0        
        //   227: athrow         
        //   228: astore_0       
        //   229: ldc             Lcom/zed3/location/GPSInfoDataBase;.class
        //   231: monitorexit    
        //   232: aload_0        
        //   233: athrow         
        //   234: astore_0       
        //   235: goto            211
        //    Signature:
        //  ()Ljava/util/List<Lcom/zed3/location/GpsInfo;>;
        //    Exceptions:
        //  Try           Handler
        //  Start  End    Start  End    Type
        //  -----  -----  -----  -----  ----
        //  3      21     228    234    Any
        //  27     63     210    211    Any
        //  63     78     228    234    Any
        //  83     91     210    211    Any
        //  91     207    234    238    Any
        //  211    228    228    234    Any
        // 
        // The error that occurred was:
        // 
        // java.lang.IllegalStateException: Expression is linked from several locations: Label_0211:
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
    
    public static GPSInfoDataBase getInstance() {
        synchronized (GPSInfoDataBase.class) {
            if (GPSInfoDataBase.instance == null) {
                GPSInfoDataBase.instance = new GPSInfoDataBase(Receiver.mContext);
            }
            return GPSInfoDataBase.instance;
        }
    }
    
    private Cursor query(final String s, final String s2) {
        // TODO
        synchronized (this) {
            final SQLiteDatabase readableDatabase = this.getReadableDatabase();
            Cursor query = null;
            try {
                final Cursor cursor = query = readableDatabase.query(s, (String[])null, (String)null, (String[])null, (String)null, (String)null, s2);
                MyLog.i("GpsInfoDataBase", "cursor.count = " + cursor.getCount());
                return cursor;
            }
            catch (Exception ex) {
                MyLog.e("GpsInfoDataBase", "query from " + s + "error:");
                ex.printStackTrace();
                final Cursor cursor = query;
            }
        }
        return null;
    }
    
    public void addInfo(final GpsInfo p0) {
        // TODO
    }
    
    public void clear(final String s) {
        synchronized (this) {
            final SQLiteDatabase writableDatabase = this.getWritableDatabase();
            try {
                writableDatabase.beginTransaction();
                this.getWritableDatabase().execSQL("DROP TABLE " + s);
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                writableDatabase.close();
                Log.i("GpsInfoDataBase", "clear table!" + s);
                this.onCreate(this.getWritableDatabase());
            }
            finally {
                writableDatabase.endTransaction();
                writableDatabase.close();
            }
        }
    }
    
    public void delete(final String s) {
        // monitorenter(this)
        try {
            if (TextUtils.isEmpty((CharSequence)s)) {
                return;
            }
            final SQLiteDatabase writableDatabase = this.getWritableDatabase();
            try {
                writableDatabase.beginTransaction();
                writableDatabase.delete("gps_info", "E_id = '" + s + "'", (String[])null);
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
                writableDatabase.close();
            }
            catch (Exception ex) {
                writableDatabase.endTransaction();
                writableDatabase.close();
            }
            finally {
                writableDatabase.endTransaction();
                writableDatabase.close();
            }
        }
        finally {}
    }
    
    public long getCount(final String p0) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: monitorenter   
        //     2: aload_0        
        //     3: invokevirtual   com/zed3/location/GPSInfoDataBase.getReadableDatabase:()Landroid/database/sqlite/SQLiteDatabase;
        //     6: astore          7
        //     8: aconst_null    
        //     9: astore          5
        //    11: aconst_null    
        //    12: astore          4
        //    14: aload           7
        //    16: new             Ljava/lang/StringBuilder;
        //    19: dup            
        //    20: ldc_w           "select count(1) from "
        //    23: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
        //    26: aload_1        
        //    27: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //    30: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
        //    33: aconst_null    
        //    34: invokevirtual   android/database/sqlite/SQLiteDatabase.rawQuery:(Ljava/lang/String;[Ljava/lang/String;)Landroid/database/Cursor;
        //    37: astore          6
        //    39: aload           6
        //    41: ifnull          98
        //    44: aload           6
        //    46: astore          4
        //    48: aload           6
        //    50: astore          5
        //    52: aload           6
        //    54: invokeinterface android/database/Cursor.moveToFirst:()Z
        //    59: pop            
        //    60: aload           6
        //    62: astore          4
        //    64: aload           6
        //    66: astore          5
        //    68: aload           6
        //    70: iconst_0       
        //    71: invokeinterface android/database/Cursor.getLong:(I)J
        //    76: lstore_2       
        //    77: aload           6
        //    79: ifnull          89
        //    82: aload           6
        //    84: invokeinterface android/database/Cursor.close:()V
        //    89: aload           7
        //    91: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //    94: aload_0        
        //    95: monitorexit    
        //    96: lload_2        
        //    97: lreturn        
        //    98: aload           6
        //   100: astore          4
        //   102: aload           6
        //   104: astore          5
        //   106: ldc             "GpsInfoDataBase"
        //   108: new             Ljava/lang/StringBuilder;
        //   111: dup            
        //   112: ldc             "cursor.count = "
        //   114: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
        //   117: aload           6
        //   119: invokeinterface android/database/Cursor.getCount:()I
        //   124: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
        //   127: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
        //   130: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
        //   133: aload           6
        //   135: ifnull          145
        //   138: aload           6
        //   140: invokeinterface android/database/Cursor.close:()V
        //   145: aload           7
        //   147: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //   150: ldc2_w          -1
        //   153: lstore_2       
        //   154: goto            94
        //   157: astore          6
        //   159: aload           4
        //   161: astore          5
        //   163: ldc             "GpsInfoDataBase"
        //   165: new             Ljava/lang/StringBuilder;
        //   168: dup            
        //   169: ldc             "query from "
        //   171: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
        //   174: aload_1        
        //   175: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   178: ldc             "error:"
        //   180: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   183: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
        //   186: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
        //   189: aload           4
        //   191: astore          5
        //   193: aload           6
        //   195: invokevirtual   java/lang/Exception.printStackTrace:()V
        //   198: aload           4
        //   200: ifnull          210
        //   203: aload           4
        //   205: invokeinterface android/database/Cursor.close:()V
        //   210: aload           7
        //   212: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //   215: goto            150
        //   218: astore_1       
        //   219: aload_0        
        //   220: monitorexit    
        //   221: aload_1        
        //   222: athrow         
        //   223: astore_1       
        //   224: aload           5
        //   226: ifnull          236
        //   229: aload           5
        //   231: invokeinterface android/database/Cursor.close:()V
        //   236: aload           7
        //   238: invokevirtual   android/database/sqlite/SQLiteDatabase.close:()V
        //   241: aload_1        
        //   242: athrow         
        //    Exceptions:
        //  Try           Handler
        //  Start  End    Start  End    Type                 
        //  -----  -----  -----  -----  ---------------------
        //  2      8      218    223    Any
        //  14     39     157    218    Ljava/lang/Exception;
        //  14     39     223    243    Any
        //  52     60     157    218    Ljava/lang/Exception;
        //  52     60     223    243    Any
        //  68     77     157    218    Ljava/lang/Exception;
        //  68     77     223    243    Any
        //  82     89     218    223    Any
        //  89     94     218    223    Any
        //  106    133    157    218    Ljava/lang/Exception;
        //  106    133    223    243    Any
        //  138    145    218    223    Any
        //  145    150    218    223    Any
        //  163    189    223    243    Any
        //  193    198    223    243    Any
        //  203    210    218    223    Any
        //  210    215    218    223    Any
        //  229    236    218    223    Any
        //  236    243    218    223    Any
        // 
        // The error that occurred was:
        // 
        // java.lang.IllegalStateException: Expression is linked from several locations: Label_0089:
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
    
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL("CREATE TABLE gps_info(_id integer PRIMARY KEY AUTOINCREMENT , gps_x text ,  gps_y text , gps_speed text, gps_height text , gps_direction  text ,UnixTime text ,real_time text ,E_id text )");
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int n, final int n2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS gps_info");
        System.out.println("onUpgrade\u5220\u9664\u8868");
        this.onCreate(sqLiteDatabase);
    }
    
    public Cursor query(final String s, final String s2, final int n, final int n2) {
    	// TODO
        synchronized (this) {
            final SQLiteDatabase readableDatabase = this.getReadableDatabase();
            Cursor query = null;
            try {
                final Cursor cursor = query = readableDatabase.query(s, (String[])null, (String)null, (String[])null, (String)null, (String)null, s2, String.valueOf(n2));
                MyLog.i("GpsInfoDataBase", "cursor.count = " + cursor.getCount());
                return cursor;
            }
            catch (Exception ex) {
                MyLog.e("GpsInfoDataBase", "query from " + s + "error:");
                ex.printStackTrace();
                final Cursor cursor = query;
            }
        }
        return null;
    }
}
