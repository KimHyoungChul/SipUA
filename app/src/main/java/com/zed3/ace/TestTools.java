package com.zed3.ace;

import android.content.Context;
import android.preference.PreferenceManager;

import com.zed3.sipua.ui.Receiver;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTools {
	public static int count;
	static DataOutputStream dos4encodeIn;
	static DataOutputStream dos4encodeIn2;
	static DataOutputStream dos4encodeOut;
	private static String fromNum;
	private static String name;
	public static DataOutputStream receiveStream;
	private static String toNum;
	private static boolean toggleState;
	File tmpFile1;
	File tmpFile2;

	static {
		TestTools.count = 0;
		TestTools.receiveStream = null;
		TestTools.dos4encodeOut = null;
		TestTools.dos4encodeIn = null;
		TestTools.dos4encodeIn2 = null;
		TestTools.fromNum = "from";
		TestTools.toNum = "to";
		TestTools.name = "";
		TestTools.toggleState = false;
	}

	private static short[] byteArray2ShortArray(final byte[] array) {
		final short[] array2 = new short[array.length / 2];
		ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(array2);
		return array2;
	}

	public static void formatFileName(final boolean b, final String s) {
		if (b) {
			TestTools.fromNum = s;
			TestTools.toNum = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("username", "");
			return;
		}
		TestTools.fromNum = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString("username", "");
		TestTools.toNum = s;
	}

	private static String formatTime() {
		return new SimpleDateFormat("ddHHmm").format(new Date());
	}

	public static String getSpeakerName() {
		return TestTools.name;
	}

	public static boolean isAECOPen(final Context context) {
		return context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getBoolean("AEC_SWITCH", true);
	}

	public static void release() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     3: ifnull          22
		//     6: getstatic       com/zed3/ace/TestTools.dos4encodeIn:Ljava/io/DataOutputStream;
		//     9: invokevirtual   java/io/DataOutputStream.flush:()V
		//    12: getstatic       com/zed3/ace/TestTools.dos4encodeIn:Ljava/io/DataOutputStream;
		//    15: invokevirtual   java/io/DataOutputStream.close:()V
		//    18: aconst_null
		//    19: putstatic       com/zed3/ace/TestTools.dos4encodeIn:Ljava/io/DataOutputStream;
		//    22: getstatic       com/zed3/ace/TestTools.dos4encodeOut:Ljava/io/DataOutputStream;
		//    25: ifnull          44
		//    28: getstatic       com/zed3/ace/TestTools.dos4encodeOut:Ljava/io/DataOutputStream;
		//    31: invokevirtual   java/io/DataOutputStream.flush:()V
		//    34: getstatic       com/zed3/ace/TestTools.dos4encodeOut:Ljava/io/DataOutputStream;
		//    37: invokevirtual   java/io/DataOutputStream.close:()V
		//    40: aconst_null
		//    41: putstatic       com/zed3/ace/TestTools.dos4encodeOut:Ljava/io/DataOutputStream;
		//    44: getstatic       com/zed3/ace/TestTools.dos4encodeIn2:Ljava/io/DataOutputStream;
		//    47: ifnull          66
		//    50: getstatic       com/zed3/ace/TestTools.dos4encodeIn2:Ljava/io/DataOutputStream;
		//    53: invokevirtual   java/io/DataOutputStream.flush:()V
		//    56: getstatic       com/zed3/ace/TestTools.dos4encodeIn2:Ljava/io/DataOutputStream;
		//    59: invokevirtual   java/io/DataOutputStream.close:()V
		//    62: aconst_null
		//    63: putstatic       com/zed3/ace/TestTools.dos4encodeIn2:Ljava/io/DataOutputStream;
		//    66: getstatic       com/zed3/ace/TestTools.receiveStream:Ljava/io/DataOutputStream;
		//    69: ifnull          88
		//    72: getstatic       com/zed3/ace/TestTools.receiveStream:Ljava/io/DataOutputStream;
		//    75: invokevirtual   java/io/DataOutputStream.flush:()V
		//    78: getstatic       com/zed3/ace/TestTools.receiveStream:Ljava/io/DataOutputStream;
		//    81: invokevirtual   java/io/DataOutputStream.close:()V
		//    84: aconst_null
		//    85: putstatic       com/zed3/ace/TestTools.receiveStream:Ljava/io/DataOutputStream;
		//    88: ldc             ""
		//    90: putstatic       com/zed3/ace/TestTools.name:Ljava/lang/String;
		//    93: return
		//    94: astore_0
		//    95: aload_0
		//    96: invokevirtual   java/io/IOException.printStackTrace:()V
		//    99: goto            22
		//   102: astore_0
		//   103: aload_0
		//   104: invokevirtual   java/io/IOException.printStackTrace:()V
		//   107: goto            40
		//   110: astore_0
		//   111: aload_0
		//   112: invokevirtual   java/io/IOException.printStackTrace:()V
		//   115: goto            62
		//   118: astore_0
		//   119: aload_0
		//   120: invokevirtual   java/io/IOException.printStackTrace:()V
		//   123: goto            84
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  6      22     94     102    Ljava/io/IOException;
		//  28     40     102    110    Ljava/io/IOException;
		//  50     62     110    118    Ljava/io/IOException;
		//  72     84     118    126    Ljava/io/IOException;
		//
		// The error that occurred was:
		//
		// java.lang.IndexOutOfBoundsException: Index: 51, Size: 51
		//     at java.util.ArrayList.rangeCheck(ArrayList.java:653)
		//     at java.util.ArrayList.get(ArrayList.java:429)
		//     at com.strobel.decompiler.ast.AstBuilder.convertToAst(AstBuilder.java:3321)
		//     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:113)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
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

	public static void toggleEnable() {
		AECManager.enable(TestTools.toggleState);
		NSManager.enable(TestTools.toggleState);
		TestTools.toggleState = !TestTools.toggleState;
	}

	public static void write2File(final byte[] array, final boolean b) {
		// TODO
	}

	public static void write2File(final short[] array) {
		// TODO
	}

	public static void write2FileMIC(final short[] array) {
		// TODO
	}

	public static void write2FileSpeaker(final short[] array) {
		// TODO
	}
}
