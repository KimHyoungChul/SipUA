package com.zed3.sipua.message;

import android.content.Context;

import com.zed3.log.MyLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiveMessageThread extends Thread {
	private static final String TAG = "ReceiveMessageThread";
	private static final int TCP_CONNECTION_ERROR = 1;
	private static final int TCP_RECONNECTION = 0;
	private String E_id;
	private boolean _isclose;
	private String check_id;
	private boolean conn_state;
	private int count;
	private String ip;
	private Context mContext;
	private int port;
	private Socket receive_socket;
	private String recipient_num;
	private String report_attr;
	private int size;
	private String str_header;

	public ReceiveMessageThread(final String ip, final int port, final Context mContext, final int size, final String e_id, final String check_id, final boolean isclose, final String recipient_num, final String report_attr) {
		this.conn_state = false;
		this.str_header = null;
		this._isclose = false;
		this.count = 0;
		this.E_id = null;
		this.check_id = null;
		this.recipient_num = null;
		this.report_attr = null;
		this.ip = ip;
		this.port = port;
		this.mContext = mContext;
		this.size = size;
		this.E_id = e_id;
		this.check_id = check_id;
		this.recipient_num = recipient_num;
		this.report_attr = report_attr;
		this._isclose = isclose;
		this.str_header = String.valueOf(e_id) + check_id;
		this.initReceiveSocket(ip, port);
	}

	private void closeThread() {
		this._isclose = false;
		try {
			this.receive_socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void initReceiveSocket(final String s, final int n) {
		try {
			this.receive_socket = new Socket(s, n);
			this.conn_state = true;
		} catch (Exception ex) {
			MyLog.e("ReceiveMessageThread", "initReceiveSocket error:");
			ex.printStackTrace();
		}
	}

	private byte[] readInputStream(final InputStream p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: astore          4
		//     3: aconst_null
		//     4: astore          5
		//     6: sipush          1024
		//     9: newarray        B
		//    11: astore          6
		//    13: new             Ljava/io/ByteArrayOutputStream;
		//    16: dup
		//    17: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//    20: astore_3
		//    21: aload_1
		//    22: aload           6
		//    24: invokevirtual   java/io/InputStream.read:([B)I
		//    27: istore_2
		//    28: iload_2
		//    29: iconst_m1
		//    30: if_icmpne       58
		//    33: aload_3
		//    34: ifnull          41
		//    37: aload_3
		//    38: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//    41: aload_1
		//    42: ifnull          190
		//    45: aload_1
		//    46: invokevirtual   java/io/InputStream.close:()V
		//    49: aload_3
		//    50: astore          4
		//    52: aload           4
		//    54: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//    57: areturn
		//    58: aload_3
		//    59: aload           6
		//    61: iconst_0
		//    62: iload_2
		//    63: invokevirtual   java/io/ByteArrayOutputStream.write:([BII)V
		//    66: goto            21
		//    69: astore          5
		//    71: aload_3
		//    72: astore          4
		//    74: aload           5
		//    76: invokevirtual   java/io/IOException.printStackTrace:()V
		//    79: aload_3
		//    80: ifnull          87
		//    83: aload_3
		//    84: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//    87: aload_3
		//    88: astore          4
		//    90: aload_1
		//    91: ifnull          52
		//    94: aload_1
		//    95: invokevirtual   java/io/InputStream.close:()V
		//    98: aload_3
		//    99: astore          4
		//   101: goto            52
		//   104: astore_1
		//   105: ldc             "ReceiveMessageThread"
		//   107: ldc             "readInputStream error"
		//   109: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   112: aload_1
		//   113: invokevirtual   java/io/IOException.printStackTrace:()V
		//   116: aconst_null
		//   117: areturn
		//   118: astore_3
		//   119: aload           4
		//   121: ifnull          129
		//   124: aload           4
		//   126: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//   129: aload_1
		//   130: ifnull          137
		//   133: aload_1
		//   134: invokevirtual   java/io/InputStream.close:()V
		//   137: aload_3
		//   138: athrow
		//   139: astore_1
		//   140: ldc             "ReceiveMessageThread"
		//   142: ldc             "readInputStream error"
		//   144: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   147: aload_1
		//   148: invokevirtual   java/io/IOException.printStackTrace:()V
		//   151: aconst_null
		//   152: areturn
		//   153: astore_1
		//   154: ldc             "ReceiveMessageThread"
		//   156: ldc             "readInputStream error"
		//   158: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   161: aload_1
		//   162: invokevirtual   java/io/IOException.printStackTrace:()V
		//   165: aconst_null
		//   166: areturn
		//   167: astore          5
		//   169: aload_3
		//   170: astore          4
		//   172: aload           5
		//   174: astore_3
		//   175: goto            119
		//   178: astore          4
		//   180: aload           5
		//   182: astore_3
		//   183: aload           4
		//   185: astore          5
		//   187: goto            71
		//   190: aload_3
		//   191: astore          4
		//   193: goto            52
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  13     21     178    190    Ljava/io/IOException;
		//  13     21     118    119    Any
		//  21     28     69     71     Ljava/io/IOException;
		//  21     28     167    178    Any
		//  37     41     153    167    Ljava/io/IOException;
		//  45     49     153    167    Ljava/io/IOException;
		//  58     66     69     71     Ljava/io/IOException;
		//  58     66     167    178    Any
		//  74     79     118    119    Any
		//  83     87     104    118    Ljava/io/IOException;
		//  94     98     104    118    Ljava/io/IOException;
		//  124    129    139    153    Ljava/io/IOException;
		//  133    137    139    153    Ljava/io/IOException;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0041:
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

	@Override
	public void run() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: monitorenter
		//     2: aconst_null
		//     3: astore          9
		//     5: aconst_null
		//     6: astore          8
		//     8: aconst_null
		//     9: astore          6
		//    11: aconst_null
		//    12: astore          5
		//    14: aload           8
		//    16: astore          4
		//    18: aload           9
		//    20: astore_3
		//    21: aload_0
		//    22: getfield        com/zed3/sipua/message/ReceiveMessageThread.receive_socket:Ljava/net/Socket;
		//    25: invokevirtual   java/net/Socket.getOutputStream:()Ljava/io/OutputStream;
		//    28: astore          7
		//    30: aload           8
		//    32: astore          4
		//    34: aload           7
		//    36: astore          5
		//    38: aload           9
		//    40: astore_3
		//    41: aload           7
		//    43: astore          6
		//    45: aload           7
		//    47: aload_0
		//    48: getfield        com/zed3/sipua/message/ReceiveMessageThread.str_header:Ljava/lang/String;
		//    51: invokevirtual   java/lang/String.getBytes:()[B
		//    54: invokevirtual   java/io/OutputStream.write:([B)V
		//    57: aload           8
		//    59: astore          4
		//    61: aload           7
		//    63: astore          5
		//    65: aload           9
		//    67: astore_3
		//    68: aload           7
		//    70: astore          6
		//    72: aload           7
		//    74: invokevirtual   java/io/OutputStream.flush:()V
		//    77: aload           8
		//    79: astore          4
		//    81: aload           7
		//    83: astore          5
		//    85: aload           9
		//    87: astore_3
		//    88: aload           7
		//    90: astore          6
		//    92: aload_0
		//    93: getfield        com/zed3/sipua/message/ReceiveMessageThread.receive_socket:Ljava/net/Socket;
		//    96: invokevirtual   java/net/Socket.getInputStream:()Ljava/io/InputStream;
		//    99: astore          8
		//   101: aload           8
		//   103: astore          4
		//   105: aload           7
		//   107: astore          5
		//   109: aload           8
		//   111: astore_3
		//   112: aload           7
		//   114: astore          6
		//   116: ldc             "ReceiveMessageThread"
		//   118: ldc             "begin receive message from socket"
		//   120: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   123: aload           8
		//   125: astore          4
		//   127: aload           7
		//   129: astore          5
		//   131: aload           8
		//   133: astore_3
		//   134: aload           7
		//   136: astore          6
		//   138: aload_0
		//   139: aload           8
		//   141: invokespecial   com/zed3/sipua/message/ReceiveMessageThread.readInputStream:(Ljava/io/InputStream;)[B
		//   144: astore          9
		//   146: aload           8
		//   148: astore          4
		//   150: aload           7
		//   152: astore          5
		//   154: aload           8
		//   156: astore_3
		//   157: aload           7
		//   159: astore          6
		//   161: aload           9
		//   163: arraylength
		//   164: aload_0
		//   165: getfield        com/zed3/sipua/message/ReceiveMessageThread.size:I
		//   168: if_icmpne       607
		//   171: aload           8
		//   173: astore          4
		//   175: aload           7
		//   177: astore          5
		//   179: aload           8
		//   181: astore_3
		//   182: aload           7
		//   184: astore          6
		//   186: new             Lcom/zed3/sipua/message/MessageParse;
		//   189: dup
		//   190: aload_0
		//   191: getfield        com/zed3/sipua/message/ReceiveMessageThread.mContext:Landroid/content/Context;
		//   194: aload_0
		//   195: getfield        com/zed3/sipua/message/ReceiveMessageThread.E_id:Ljava/lang/String;
		//   198: aload_0
		//   199: getfield        com/zed3/sipua/message/ReceiveMessageThread.recipient_num:Ljava/lang/String;
		//   202: invokespecial   com/zed3/sipua/message/MessageParse.<init>:(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V
		//   205: astore          11
		//   207: aload           8
		//   209: astore          4
		//   211: aload           7
		//   213: astore          5
		//   215: aload           8
		//   217: astore_3
		//   218: aload           7
		//   220: astore          6
		//   222: aload           11
		//   224: aload           9
		//   226: invokevirtual   com/zed3/sipua/message/MessageParse.parseMmsInfoFromTxt:([B)I
		//   229: iconst_1
		//   230: if_icmpne       607
		//   233: aload           8
		//   235: astore          4
		//   237: aload           7
		//   239: astore          5
		//   241: aload           8
		//   243: astore_3
		//   244: aload           7
		//   246: astore          6
		//   248: aload           11
		//   250: invokevirtual   com/zed3/sipua/message/MessageParse.saveMmsInfoToInbox:()Landroid/os/Message;
		//   253: astore          9
		//   255: aload           9
		//   257: ifnull          607
		//   260: ldc             "\u8bf7\u6253\u5f00\u6587\u4ef6"
		//   262: astore          10
		//   264: iconst_0
		//   265: istore_2
		//   266: aload           8
		//   268: astore          4
		//   270: aload           7
		//   272: astore          5
		//   274: aload           8
		//   276: astore_3
		//   277: aload           7
		//   279: astore          6
		//   281: aload           9
		//   283: getfield        android/os/Message.obj:Ljava/lang/Object;
		//   286: astore          12
		//   288: aload           10
		//   290: astore          9
		//   292: iload_2
		//   293: istore_1
		//   294: aload           12
		//   296: ifnull          355
		//   299: aload           10
		//   301: astore          9
		//   303: iload_2
		//   304: istore_1
		//   305: aload           8
		//   307: astore          4
		//   309: aload           7
		//   311: astore          5
		//   313: aload           8
		//   315: astore_3
		//   316: aload           7
		//   318: astore          6
		//   320: aload           12
		//   322: instanceof      Lcom/zed3/sipua/message/PhotoTransferReceiveActivity.PhotoReceiveMessage;
		//   325: ifeq            355
		//   328: aload           8
		//   330: astore          4
		//   332: aload           7
		//   334: astore          5
		//   336: aload           8
		//   338: astore_3
		//   339: aload           7
		//   341: astore          6
		//   343: aload           12
		//   345: checkcast       Lcom/zed3/sipua/message/PhotoTransferReceiveActivity.PhotoReceiveMessage;
		//   348: getfield        com/zed3/sipua/message/PhotoTransferReceiveActivity.PhotoReceiveMessage.mBody:Ljava/lang/String;
		//   351: astore          9
		//   353: iconst_1
		//   354: istore_1
		//   355: aload           8
		//   357: astore          4
		//   359: aload           7
		//   361: astore          5
		//   363: aload           8
		//   365: astore_3
		//   366: aload           7
		//   368: astore          6
		//   370: new             Landroid/content/Intent;
		//   373: dup
		//   374: invokespecial   android/content/Intent.<init>:()V
		//   377: astore          10
		//   379: aload           8
		//   381: astore          4
		//   383: aload           7
		//   385: astore          5
		//   387: aload           8
		//   389: astore_3
		//   390: aload           7
		//   392: astore          6
		//   394: aload           10
		//   396: ldc             "E_id"
		//   398: aload_0
		//   399: getfield        com/zed3/sipua/message/ReceiveMessageThread.E_id:Ljava/lang/String;
		//   402: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
		//   405: pop
		//   406: aload           8
		//   408: astore          4
		//   410: aload           7
		//   412: astore          5
		//   414: aload           8
		//   416: astore_3
		//   417: aload           7
		//   419: astore          6
		//   421: aload           10
		//   423: ldc             "recipient_num"
		//   425: aload_0
		//   426: getfield        com/zed3/sipua/message/ReceiveMessageThread.recipient_num:Ljava/lang/String;
		//   429: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
		//   432: pop
		//   433: aload           8
		//   435: astore          4
		//   437: aload           7
		//   439: astore          5
		//   441: aload           8
		//   443: astore_3
		//   444: aload           7
		//   446: astore          6
		//   448: aload           10
		//   450: ldc             "contentType"
		//   452: aload           11
		//   454: invokevirtual   com/zed3/sipua/message/MessageParse.getContentType:()Ljava/lang/String;
		//   457: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
		//   460: pop
		//   461: aload           8
		//   463: astore          4
		//   465: aload           7
		//   467: astore          5
		//   469: aload           8
		//   471: astore_3
		//   472: aload           7
		//   474: astore          6
		//   476: aload           10
		//   478: ldc             "report_attr"
		//   480: aload_0
		//   481: getfield        com/zed3/sipua/message/ReceiveMessageThread.report_attr:Ljava/lang/String;
		//   484: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
		//   487: pop
		//   488: aload           8
		//   490: astore          4
		//   492: aload           7
		//   494: astore          5
		//   496: aload           8
		//   498: astore_3
		//   499: aload           7
		//   501: astore          6
		//   503: aload           10
		//   505: ldc             "body"
		//   507: aload           9
		//   509: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
		//   512: pop
		//   513: aload           8
		//   515: astore          4
		//   517: aload           7
		//   519: astore          5
		//   521: aload           8
		//   523: astore_3
		//   524: aload           7
		//   526: astore          6
		//   528: aload           10
		//   530: ldc             "type"
		//   532: iload_1
		//   533: invokevirtual   android/content/Intent.putExtra:(Ljava/lang/String;I)Landroid/content/Intent;
		//   536: pop
		//   537: aload           8
		//   539: astore          4
		//   541: aload           7
		//   543: astore          5
		//   545: aload           8
		//   547: astore_3
		//   548: aload           7
		//   550: astore          6
		//   552: invokestatic    com/zed3/media/TipSoundPlayer.getInstance:()Lcom/zed3/media/TipSoundPlayer;
		//   555: getstatic       com/zed3/media/TipSoundPlayer.Sound.MESSAGE_ACCEPT:Lcom/zed3/media/TipSoundPlayer.Sound;
		//   558: invokevirtual   com/zed3/media/TipSoundPlayer.play:(Lcom/zed3/media/TipSoundPlayer.Sound;)V
		//   561: aload           8
		//   563: astore          4
		//   565: aload           7
		//   567: astore          5
		//   569: aload           8
		//   571: astore_3
		//   572: aload           7
		//   574: astore          6
		//   576: aload           10
		//   578: ldc             "com.zed3.action.RECEIVE_MMS"
		//   580: invokevirtual   android/content/Intent.setAction:(Ljava/lang/String;)Landroid/content/Intent;
		//   583: pop
		//   584: aload           8
		//   586: astore          4
		//   588: aload           7
		//   590: astore          5
		//   592: aload           8
		//   594: astore_3
		//   595: aload           7
		//   597: astore          6
		//   599: getstatic       com/zed3/sipua/ui/Receiver.mContext:Landroid/content/Context;
		//   602: aload           10
		//   604: invokevirtual   android/content/Context.sendBroadcast:(Landroid/content/Intent;)V
		//   607: aload           8
		//   609: ifnull          617
		//   612: aload           8
		//   614: invokevirtual   java/io/InputStream.close:()V
		//   617: aload           7
		//   619: ifnull          627
		//   622: aload           7
		//   624: invokevirtual   java/io/OutputStream.close:()V
		//   627: iconst_0
		//   628: ifeq            639
		//   631: new             Ljava/lang/NullPointerException;
		//   634: dup
		//   635: invokespecial   java/lang/NullPointerException.<init>:()V
		//   638: athrow
		//   639: aload_0
		//   640: invokespecial   java/lang/Thread.run:()V
		//   643: aload_0
		//   644: monitorexit
		//   645: return
		//   646: astore          7
		//   648: aload           4
		//   650: astore_3
		//   651: aload           5
		//   653: astore          6
		//   655: ldc             "ReceiveMessageThread"
		//   657: ldc             "receive message thread error:"
		//   659: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   662: aload           4
		//   664: astore_3
		//   665: aload           5
		//   667: astore          6
		//   669: aload           7
		//   671: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   674: aload           4
		//   676: ifnull          684
		//   679: aload           4
		//   681: invokevirtual   java/io/InputStream.close:()V
		//   684: aload           5
		//   686: ifnull          694
		//   689: aload           5
		//   691: invokevirtual   java/io/OutputStream.close:()V
		//   694: iconst_0
		//   695: ifeq            639
		//   698: new             Ljava/lang/NullPointerException;
		//   701: dup
		//   702: invokespecial   java/lang/NullPointerException.<init>:()V
		//   705: athrow
		//   706: astore_3
		//   707: aload_3
		//   708: invokevirtual   java/io/IOException.printStackTrace:()V
		//   711: goto            639
		//   714: astore_3
		//   715: aload_0
		//   716: monitorexit
		//   717: aload_3
		//   718: athrow
		//   719: astore          4
		//   721: aload_3
		//   722: ifnull          729
		//   725: aload_3
		//   726: invokevirtual   java/io/InputStream.close:()V
		//   729: aload           6
		//   731: ifnull          739
		//   734: aload           6
		//   736: invokevirtual   java/io/OutputStream.close:()V
		//   739: iconst_0
		//   740: ifeq            751
		//   743: new             Ljava/lang/NullPointerException;
		//   746: dup
		//   747: invokespecial   java/lang/NullPointerException.<init>:()V
		//   750: athrow
		//   751: aload           4
		//   753: athrow
		//   754: astore_3
		//   755: aload_3
		//   756: invokevirtual   java/io/IOException.printStackTrace:()V
		//   759: goto            751
		//   762: astore_3
		//   763: aload_3
		//   764: invokevirtual   java/io/IOException.printStackTrace:()V
		//   767: goto            639
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  21     30     646    714    Ljava/lang/Exception;
		//  21     30     719    762    Any
		//  45     57     646    714    Ljava/lang/Exception;
		//  45     57     719    762    Any
		//  72     77     646    714    Ljava/lang/Exception;
		//  72     77     719    762    Any
		//  92     101    646    714    Ljava/lang/Exception;
		//  92     101    719    762    Any
		//  116    123    646    714    Ljava/lang/Exception;
		//  116    123    719    762    Any
		//  138    146    646    714    Ljava/lang/Exception;
		//  138    146    719    762    Any
		//  161    171    646    714    Ljava/lang/Exception;
		//  161    171    719    762    Any
		//  186    207    646    714    Ljava/lang/Exception;
		//  186    207    719    762    Any
		//  222    233    646    714    Ljava/lang/Exception;
		//  222    233    719    762    Any
		//  248    255    646    714    Ljava/lang/Exception;
		//  248    255    719    762    Any
		//  281    288    646    714    Ljava/lang/Exception;
		//  281    288    719    762    Any
		//  320    328    646    714    Ljava/lang/Exception;
		//  320    328    719    762    Any
		//  343    353    646    714    Ljava/lang/Exception;
		//  343    353    719    762    Any
		//  370    379    646    714    Ljava/lang/Exception;
		//  370    379    719    762    Any
		//  394    406    646    714    Ljava/lang/Exception;
		//  394    406    719    762    Any
		//  421    433    646    714    Ljava/lang/Exception;
		//  421    433    719    762    Any
		//  448    461    646    714    Ljava/lang/Exception;
		//  448    461    719    762    Any
		//  476    488    646    714    Ljava/lang/Exception;
		//  476    488    719    762    Any
		//  503    513    646    714    Ljava/lang/Exception;
		//  503    513    719    762    Any
		//  528    537    646    714    Ljava/lang/Exception;
		//  528    537    719    762    Any
		//  552    561    646    714    Ljava/lang/Exception;
		//  552    561    719    762    Any
		//  576    584    646    714    Ljava/lang/Exception;
		//  576    584    719    762    Any
		//  599    607    646    714    Ljava/lang/Exception;
		//  599    607    719    762    Any
		//  612    617    762    770    Ljava/io/IOException;
		//  612    617    714    719    Any
		//  622    627    762    770    Ljava/io/IOException;
		//  622    627    714    719    Any
		//  631    639    762    770    Ljava/io/IOException;
		//  631    639    714    719    Any
		//  639    643    714    719    Any
		//  655    662    719    762    Any
		//  669    674    719    762    Any
		//  679    684    706    714    Ljava/io/IOException;
		//  679    684    714    719    Any
		//  689    694    706    714    Ljava/io/IOException;
		//  689    694    714    719    Any
		//  698    706    706    714    Ljava/io/IOException;
		//  698    706    714    719    Any
		//  707    711    714    719    Any
		//  725    729    754    762    Ljava/io/IOException;
		//  725    729    714    719    Any
		//  734    739    754    762    Ljava/io/IOException;
		//  734    739    714    719    Any
		//  743    751    754    762    Ljava/io/IOException;
		//  743    751    714    719    Any
		//  751    754    714    719    Any
		//  755    759    714    719    Any
		//  763    767    714    719    Any
		//
		// The error that occurred was:
		//
		// java.util.ConcurrentModificationException
		//     at java.util.ArrayList.Itr.checkForComodification(ArrayList.java:901)
		//     at java.util.ArrayList.Itr.next(ArrayList.java:851)
		//     at com.strobel.decompiler.ast.AstBuilder.convertLocalVariables(AstBuilder.java:2863)
		//     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2445)
		//     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
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
}
