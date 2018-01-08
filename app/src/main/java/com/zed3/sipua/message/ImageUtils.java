package com.zed3.sipua.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageUtils {
	private static final int IMAGE_COMPRESSION_QUALITY = 80;
	private static final int MAX_IMAGE_HEIGHT = 480;
	private static final int MAX_IMAGE_WIDTH = 640;
	private static final int MAX_MESSAGE_SIZE = 2097152;
	public static final int MESSAGE_OVERHEAD = 5000;
	private static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;
	private static final int NUMBER_OF_RESIZE_ATTEMPTS = 4;
	private static final String TAG = "ImageUtils";
	private static int mMaxImageHeight = 0;
	private static int mMaxImageWidth = 0;
	private static final int mMaxMessageSize = 2097152;
	private Context mContext;
	private int mHeight;
	private int mWidth;

	static {
		ImageUtils.mMaxImageHeight = 480;
		ImageUtils.mMaxImageWidth = 640;
	}

	public ImageUtils(final Context mContext) {
		this.mContext = mContext;
	}

	public Bitmap Bytes2Bimap(final byte[] array) {
		if (array.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	public void decodeBoundsInfo(final Uri p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: astore_3
		//     2: aconst_null
		//     3: astore_2
		//     4: aload_0
		//     5: getfield        com/zed3/sipua/message/ImageUtils.mContext:Landroid/content/Context;
		//     8: invokevirtual   android/content/Context.getContentResolver:()Landroid/content/ContentResolver;
		//    11: aload_1
		//    12: invokevirtual   android/content/ContentResolver.openInputStream:(Landroid/net/Uri;)Ljava/io/InputStream;
		//    15: astore_1
		//    16: aload_1
		//    17: astore_2
		//    18: aload_1
		//    19: astore_3
		//    20: new             Landroid/graphics/BitmapFactory.Options;
		//    23: dup
		//    24: invokespecial   android/graphics/BitmapFactory.Options.<init>:()V
		//    27: astore          4
		//    29: aload_1
		//    30: astore_2
		//    31: aload_1
		//    32: astore_3
		//    33: aload           4
		//    35: iconst_1
		//    36: putfield        android/graphics/BitmapFactory.Options.inJustDecodeBounds:Z
		//    39: aload_1
		//    40: astore_2
		//    41: aload_1
		//    42: astore_3
		//    43: aload_1
		//    44: aconst_null
		//    45: aload           4
		//    47: invokestatic    android/graphics/BitmapFactory.decodeStream:(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory.Options;)Landroid/graphics/Bitmap;
		//    50: pop
		//    51: aload_1
		//    52: astore_2
		//    53: aload_1
		//    54: astore_3
		//    55: aload_0
		//    56: aload           4
		//    58: getfield        android/graphics/BitmapFactory.Options.outWidth:I
		//    61: putfield        com/zed3/sipua/message/ImageUtils.mWidth:I
		//    64: aload_1
		//    65: astore_2
		//    66: aload_1
		//    67: astore_3
		//    68: aload_0
		//    69: aload           4
		//    71: getfield        android/graphics/BitmapFactory.Options.outHeight:I
		//    74: putfield        com/zed3/sipua/message/ImageUtils.mHeight:I
		//    77: aload_1
		//    78: ifnull          85
		//    81: aload_1
		//    82: invokevirtual   java/io/InputStream.close:()V
		//    85: return
		//    86: astore_1
		//    87: aload_2
		//    88: astore_3
		//    89: ldc             "ImageUtils"
		//    91: ldc             "IOException caught while opening stream"
		//    93: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//    96: aload_2
		//    97: astore_3
		//    98: aload_1
		//    99: invokevirtual   java/io/FileNotFoundException.printStackTrace:()V
		//   102: aload_2
		//   103: ifnull          85
		//   106: aload_2
		//   107: invokevirtual   java/io/InputStream.close:()V
		//   110: return
		//   111: astore_1
		//   112: ldc             "ImageUtils"
		//   114: ldc             "IOException caught while closing stream"
		//   116: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   119: aload_1
		//   120: invokevirtual   java/io/IOException.printStackTrace:()V
		//   123: return
		//   124: astore_1
		//   125: aload_3
		//   126: ifnull          133
		//   129: aload_3
		//   130: invokevirtual   java/io/InputStream.close:()V
		//   133: aload_1
		//   134: athrow
		//   135: astore_2
		//   136: ldc             "ImageUtils"
		//   138: ldc             "IOException caught while closing stream"
		//   140: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   143: aload_2
		//   144: invokevirtual   java/io/IOException.printStackTrace:()V
		//   147: goto            133
		//   150: astore_1
		//   151: ldc             "ImageUtils"
		//   153: ldc             "IOException caught while closing stream"
		//   155: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   158: aload_1
		//   159: invokevirtual   java/io/IOException.printStackTrace:()V
		//   162: return
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  -------------------------------
		//  4      16     86     124    Ljava/io/FileNotFoundException;
		//  4      16     124    150    Any
		//  20     29     86     124    Ljava/io/FileNotFoundException;
		//  20     29     124    150    Any
		//  33     39     86     124    Ljava/io/FileNotFoundException;
		//  33     39     124    150    Any
		//  43     51     86     124    Ljava/io/FileNotFoundException;
		//  43     51     124    150    Any
		//  55     64     86     124    Ljava/io/FileNotFoundException;
		//  55     64     124    150    Any
		//  68     77     86     124    Ljava/io/FileNotFoundException;
		//  68     77     124    150    Any
		//  81     85     150    163    Ljava/io/IOException;
		//  89     96     124    150    Any
		//  98     102    124    150    Any
		//  106    110    111    124    Ljava/io/IOException;
		//  129    133    135    150    Ljava/io/IOException;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0085:
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

	public int getHeight() {
		return this.mHeight;
	}

	public int getMaxImageHeight() {
		return ImageUtils.mMaxImageHeight;
	}

	public int getMaxImageWidth() {
		return ImageUtils.mMaxImageWidth;
	}

	public int getMaxMessageSize() {
		return 2097152;
	}

	public byte[] getResizedImageData(final int p0, final int p1, final int p2, final Uri p3) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: getfield        com/zed3/sipua/message/ImageUtils.mWidth:I
		//     4: istore          8
		//     6: aload_0
		//     7: getfield        com/zed3/sipua/message/ImageUtils.mHeight:I
		//    10: istore          9
		//    12: iconst_1
		//    13: istore          5
		//    15: iload           8
		//    17: iload           5
		//    19: idiv
		//    20: iload_1
		//    21: if_icmpgt       167
		//    24: iload           9
		//    26: iload           5
		//    28: idiv
		//    29: iload_2
		//    30: if_icmpgt       167
		//    33: aconst_null
		//    34: astore          11
		//    36: iconst_1
		//    37: istore          7
		//    39: aconst_null
		//    40: astore          13
		//    42: iload           5
		//    44: istore          6
		//    46: iload           7
		//    48: istore          5
		//    50: aload           11
		//    52: astore          12
		//    54: aload           11
		//    56: astore          14
		//    58: aload           13
		//    60: astore          17
		//    62: new             Landroid/graphics/BitmapFactory.Options;
		//    65: dup
		//    66: invokespecial   android/graphics/BitmapFactory.Options.<init>:()V
		//    69: astore          19
		//    71: aload           11
		//    73: astore          12
		//    75: aload           11
		//    77: astore          14
		//    79: aload           13
		//    81: astore          17
		//    83: aload           19
		//    85: iload           6
		//    87: putfield        android/graphics/BitmapFactory.Options.inSampleSize:I
		//    90: aload           11
		//    92: astore          12
		//    94: aload           11
		//    96: astore          14
		//    98: aload           13
		//   100: astore          17
		//   102: aload_0
		//   103: getfield        com/zed3/sipua/message/ImageUtils.mContext:Landroid/content/Context;
		//   106: invokevirtual   android/content/Context.getContentResolver:()Landroid/content/ContentResolver;
		//   109: aload           4
		//   111: invokevirtual   android/content/ContentResolver.openInputStream:(Landroid/net/Uri;)Ljava/io/InputStream;
		//   114: astore          15
		//   116: aload           15
		//   118: astore          11
		//   120: aload           13
		//   122: astore          15
		//   124: aload           11
		//   126: astore          12
		//   128: aload           11
		//   130: astore          14
		//   132: aload           13
		//   134: astore          17
		//   136: aload           11
		//   138: aconst_null
		//   139: aload           19
		//   141: invokestatic    android/graphics/BitmapFactory.decodeStream:(Ljava/io/InputStream;Landroid/graphics/Rect;Landroid/graphics/BitmapFactory.Options;)Landroid/graphics/Bitmap;
		//   144: astore          18
		//   146: aload           18
		//   148: ifnonnull       196
		//   151: aload           11
		//   153: ifnull          161
		//   156: aload           11
		//   158: invokevirtual   java/io/InputStream.close:()V
		//   161: aconst_null
		//   162: astore          12
		//   164: aload           12
		//   166: areturn
		//   167: iload           5
		//   169: iconst_2
		//   170: imul
		//   171: istore          5
		//   173: goto            15
		//   176: astore          4
		//   178: ldc             "ImageUtils"
		//   180: aload           4
		//   182: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   185: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   188: aload           4
		//   190: invokevirtual   java/io/IOException.printStackTrace:()V
		//   193: goto            161
		//   196: aload           13
		//   198: astore          15
		//   200: aload           11
		//   202: astore          12
		//   204: aload           11
		//   206: astore          14
		//   208: aload           13
		//   210: astore          17
		//   212: aload           19
		//   214: getfield        android/graphics/BitmapFactory.Options.outWidth:I
		//   217: iload_1
		//   218: if_icmpgt       250
		//   221: aload           18
		//   223: astore          16
		//   225: aload           13
		//   227: astore          15
		//   229: aload           11
		//   231: astore          12
		//   233: aload           11
		//   235: astore          14
		//   237: aload           13
		//   239: astore          17
		//   241: aload           19
		//   243: getfield        android/graphics/BitmapFactory.Options.outHeight:I
		//   246: iload_2
		//   247: if_icmple       423
		//   250: aload           13
		//   252: astore          15
		//   254: aload           11
		//   256: astore          12
		//   258: aload           11
		//   260: astore          14
		//   262: aload           13
		//   264: astore          17
		//   266: iload           8
		//   268: iload           6
		//   270: idiv
		//   271: istore          7
		//   273: aload           13
		//   275: astore          15
		//   277: aload           11
		//   279: astore          12
		//   281: aload           11
		//   283: astore          14
		//   285: aload           13
		//   287: astore          17
		//   289: iload           9
		//   291: iload           6
		//   293: idiv
		//   294: istore          10
		//   296: aload           13
		//   298: astore          15
		//   300: aload           11
		//   302: astore          12
		//   304: aload           11
		//   306: astore          14
		//   308: aload           13
		//   310: astore          17
		//   312: ldc             "ImageUtils"
		//   314: new             Ljava/lang/StringBuilder;
		//   317: dup
		//   318: ldc             "getResizedImageData: retry scaling using Bitmap.createScaledBitmap: w="
		//   320: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   323: iload           7
		//   325: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   328: ldc             ", h="
		//   330: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   333: iload           10
		//   335: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   338: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   341: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   344: aload           13
		//   346: astore          15
		//   348: aload           11
		//   350: astore          12
		//   352: aload           11
		//   354: astore          14
		//   356: aload           13
		//   358: astore          17
		//   360: aload           18
		//   362: iload           8
		//   364: iload           6
		//   366: idiv
		//   367: iload           9
		//   369: iload           6
		//   371: idiv
		//   372: iconst_0
		//   373: invokestatic    android/graphics/Bitmap.createScaledBitmap:(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;
		//   376: astore          16
		//   378: aload           16
		//   380: astore          12
		//   382: aload           12
		//   384: astore          16
		//   386: aload           12
		//   388: ifnonnull       423
		//   391: aload           11
		//   393: ifnull          401
		//   396: aload           11
		//   398: invokevirtual   java/io/InputStream.close:()V
		//   401: aconst_null
		//   402: areturn
		//   403: astore          4
		//   405: ldc             "ImageUtils"
		//   407: aload           4
		//   409: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   412: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   415: aload           4
		//   417: invokevirtual   java/io/IOException.printStackTrace:()V
		//   420: goto            401
		//   423: aload           13
		//   425: astore          15
		//   427: aload           11
		//   429: astore          12
		//   431: aload           11
		//   433: astore          14
		//   435: aload           13
		//   437: astore          17
		//   439: new             Ljava/io/ByteArrayOutputStream;
		//   442: dup
		//   443: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//   446: astore          17
		//   448: aload           11
		//   450: astore          12
		//   452: aload           17
		//   454: astore          14
		//   456: aload           16
		//   458: getstatic       android/graphics/Bitmap.CompressFormat.JPEG:Landroid/graphics/Bitmap.CompressFormat;
		//   461: bipush          80
		//   463: aload           17
		//   465: invokevirtual   android/graphics/Bitmap.compress:(Landroid/graphics/Bitmap.CompressFormat;ILjava/io/OutputStream;)Z
		//   468: pop
		//   469: aload           11
		//   471: astore          12
		//   473: aload           17
		//   475: astore          14
		//   477: aload           17
		//   479: invokevirtual   java/io/ByteArrayOutputStream.size:()I
		//   482: istore          7
		//   484: aload           17
		//   486: astore          13
		//   488: iload           7
		//   490: iload_3
		//   491: if_icmple       598
		//   494: aload           11
		//   496: astore          12
		//   498: aload           17
		//   500: astore          14
		//   502: bipush          80
		//   504: iload_3
		//   505: imul
		//   506: iload           7
		//   508: idiv
		//   509: istore          7
		//   511: aload           17
		//   513: astore          13
		//   515: iload           7
		//   517: bipush          50
		//   519: if_icmplt       598
		//   522: aload           11
		//   524: astore          12
		//   526: aload           17
		//   528: astore          14
		//   530: ldc             "ImageUtils"
		//   532: new             Ljava/lang/StringBuilder;
		//   535: dup
		//   536: ldc             "getResizedImageData: compress(2) w/ quality="
		//   538: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   541: iload           7
		//   543: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   546: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   549: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//   552: aload           11
		//   554: astore          12
		//   556: aload           17
		//   558: astore          14
		//   560: new             Ljava/io/ByteArrayOutputStream;
		//   563: dup
		//   564: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//   567: astore          13
		//   569: aload           13
		//   571: astore          15
		//   573: aload           11
		//   575: astore          12
		//   577: aload           11
		//   579: astore          14
		//   581: aload           13
		//   583: astore          17
		//   585: aload           16
		//   587: getstatic       android/graphics/Bitmap.CompressFormat.JPEG:Landroid/graphics/Bitmap.CompressFormat;
		//   590: iload           7
		//   592: aload           13
		//   594: invokevirtual   android/graphics/Bitmap.compress:(Landroid/graphics/Bitmap.CompressFormat;ILjava/io/OutputStream;)Z
		//   597: pop
		//   598: aload           11
		//   600: astore          12
		//   602: aload           13
		//   604: astore          14
		//   606: aload           16
		//   608: invokevirtual   android/graphics/Bitmap.recycle:()V
		//   611: iload           6
		//   613: iconst_2
		//   614: imul
		//   615: istore          6
		//   617: iload           5
		//   619: iconst_1
		//   620: iadd
		//   621: istore          5
		//   623: aload           13
		//   625: ifnull          645
		//   628: aload           11
		//   630: astore          12
		//   632: aload           13
		//   634: invokevirtual   java/io/ByteArrayOutputStream.size:()I
		//   637: istore          7
		//   639: iload           7
		//   641: iload_3
		//   642: if_icmple       651
		//   645: iload           5
		//   647: iconst_4
		//   648: if_icmplt       926
		//   651: aload           13
		//   653: ifnonnull       781
		//   656: aconst_null
		//   657: astore          4
		//   659: aload           4
		//   661: astore          12
		//   663: aload           11
		//   665: ifnull          164
		//   668: aload           11
		//   670: invokevirtual   java/io/InputStream.close:()V
		//   673: aload           4
		//   675: areturn
		//   676: astore          11
		//   678: ldc             "ImageUtils"
		//   680: aload           11
		//   682: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   685: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   688: aload           11
		//   690: invokevirtual   java/io/IOException.printStackTrace:()V
		//   693: aload           4
		//   695: areturn
		//   696: astore          13
		//   698: aload           15
		//   700: astore          14
		//   702: aload           11
		//   704: astore          12
		//   706: ldc             "ImageUtils"
		//   708: new             Ljava/lang/StringBuilder;
		//   711: dup
		//   712: ldc             "getResizedImageData - image too big (OutOfMemoryError), will try  with smaller scale factor, cur scale factor: "
		//   714: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   717: iload           6
		//   719: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   722: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   725: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   728: aload           11
		//   730: astore          12
		//   732: aload           13
		//   734: invokevirtual   java/lang/OutOfMemoryError.printStackTrace:()V
		//   737: aload           14
		//   739: astore          13
		//   741: goto            611
		//   744: astore          4
		//   746: aload           11
		//   748: astore          12
		//   750: ldc             "ImageUtils"
		//   752: aload           4
		//   754: invokevirtual   java/io/FileNotFoundException.getMessage:()Ljava/lang/String;
		//   757: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   760: aload           11
		//   762: astore          12
		//   764: aload           4
		//   766: invokevirtual   java/io/FileNotFoundException.printStackTrace:()V
		//   769: aload           11
		//   771: ifnull          779
		//   774: aload           11
		//   776: invokevirtual   java/io/InputStream.close:()V
		//   779: aconst_null
		//   780: areturn
		//   781: aload           11
		//   783: astore          12
		//   785: aload           13
		//   787: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//   790: astore          4
		//   792: goto            659
		//   795: astore          4
		//   797: ldc             "ImageUtils"
		//   799: aload           4
		//   801: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   804: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   807: aload           4
		//   809: invokevirtual   java/io/IOException.printStackTrace:()V
		//   812: goto            779
		//   815: astore          4
		//   817: aload           11
		//   819: astore          12
		//   821: ldc             "ImageUtils"
		//   823: aload           4
		//   825: invokevirtual   java/lang/OutOfMemoryError.getMessage:()Ljava/lang/String;
		//   828: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   831: aload           11
		//   833: astore          12
		//   835: aload           4
		//   837: invokevirtual   java/lang/OutOfMemoryError.printStackTrace:()V
		//   840: aload           11
		//   842: ifnull          850
		//   845: aload           11
		//   847: invokevirtual   java/io/InputStream.close:()V
		//   850: aconst_null
		//   851: areturn
		//   852: astore          4
		//   854: ldc             "ImageUtils"
		//   856: aload           4
		//   858: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   861: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   864: aload           4
		//   866: invokevirtual   java/io/IOException.printStackTrace:()V
		//   869: goto            850
		//   872: astore          4
		//   874: aload           12
		//   876: ifnull          884
		//   879: aload           12
		//   881: invokevirtual   java/io/InputStream.close:()V
		//   884: aload           4
		//   886: athrow
		//   887: astore          11
		//   889: ldc             "ImageUtils"
		//   891: aload           11
		//   893: invokevirtual   java/io/IOException.getMessage:()Ljava/lang/String;
		//   896: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   899: aload           11
		//   901: invokevirtual   java/io/IOException.printStackTrace:()V
		//   904: goto            884
		//   907: astore          4
		//   909: goto            817
		//   912: astore          4
		//   914: aload           14
		//   916: astore          11
		//   918: goto            746
		//   921: astore          13
		//   923: goto            702
		//   926: goto            50
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  -------------------------------
		//  62     71     912    921    Ljava/io/FileNotFoundException;
		//  62     71     815    817    Ljava/lang/OutOfMemoryError;
		//  62     71     872    907    Any
		//  83     90     912    921    Ljava/io/FileNotFoundException;
		//  83     90     815    817    Ljava/lang/OutOfMemoryError;
		//  83     90     872    907    Any
		//  102    116    912    921    Ljava/io/FileNotFoundException;
		//  102    116    815    817    Ljava/lang/OutOfMemoryError;
		//  102    116    872    907    Any
		//  136    146    696    702    Ljava/lang/OutOfMemoryError;
		//  136    146    912    921    Ljava/io/FileNotFoundException;
		//  136    146    872    907    Any
		//  156    161    176    196    Ljava/io/IOException;
		//  212    221    696    702    Ljava/lang/OutOfMemoryError;
		//  212    221    912    921    Ljava/io/FileNotFoundException;
		//  212    221    872    907    Any
		//  241    250    696    702    Ljava/lang/OutOfMemoryError;
		//  241    250    912    921    Ljava/io/FileNotFoundException;
		//  241    250    872    907    Any
		//  266    273    696    702    Ljava/lang/OutOfMemoryError;
		//  266    273    912    921    Ljava/io/FileNotFoundException;
		//  266    273    872    907    Any
		//  289    296    696    702    Ljava/lang/OutOfMemoryError;
		//  289    296    912    921    Ljava/io/FileNotFoundException;
		//  289    296    872    907    Any
		//  312    344    696    702    Ljava/lang/OutOfMemoryError;
		//  312    344    912    921    Ljava/io/FileNotFoundException;
		//  312    344    872    907    Any
		//  360    378    696    702    Ljava/lang/OutOfMemoryError;
		//  360    378    912    921    Ljava/io/FileNotFoundException;
		//  360    378    872    907    Any
		//  396    401    403    423    Ljava/io/IOException;
		//  439    448    696    702    Ljava/lang/OutOfMemoryError;
		//  439    448    912    921    Ljava/io/FileNotFoundException;
		//  439    448    872    907    Any
		//  456    469    921    926    Ljava/lang/OutOfMemoryError;
		//  456    469    744    746    Ljava/io/FileNotFoundException;
		//  456    469    872    907    Any
		//  477    484    921    926    Ljava/lang/OutOfMemoryError;
		//  477    484    744    746    Ljava/io/FileNotFoundException;
		//  477    484    872    907    Any
		//  502    511    921    926    Ljava/lang/OutOfMemoryError;
		//  502    511    744    746    Ljava/io/FileNotFoundException;
		//  502    511    872    907    Any
		//  530    552    921    926    Ljava/lang/OutOfMemoryError;
		//  530    552    744    746    Ljava/io/FileNotFoundException;
		//  530    552    872    907    Any
		//  560    569    921    926    Ljava/lang/OutOfMemoryError;
		//  560    569    744    746    Ljava/io/FileNotFoundException;
		//  560    569    872    907    Any
		//  585    598    696    702    Ljava/lang/OutOfMemoryError;
		//  585    598    912    921    Ljava/io/FileNotFoundException;
		//  585    598    872    907    Any
		//  606    611    921    926    Ljava/lang/OutOfMemoryError;
		//  606    611    744    746    Ljava/io/FileNotFoundException;
		//  606    611    872    907    Any
		//  632    639    744    746    Ljava/io/FileNotFoundException;
		//  632    639    907    912    Ljava/lang/OutOfMemoryError;
		//  632    639    872    907    Any
		//  668    673    676    696    Ljava/io/IOException;
		//  706    728    744    746    Ljava/io/FileNotFoundException;
		//  706    728    907    912    Ljava/lang/OutOfMemoryError;
		//  706    728    872    907    Any
		//  732    737    744    746    Ljava/io/FileNotFoundException;
		//  732    737    907    912    Ljava/lang/OutOfMemoryError;
		//  732    737    872    907    Any
		//  750    760    872    907    Any
		//  764    769    872    907    Any
		//  774    779    795    815    Ljava/io/IOException;
		//  785    792    744    746    Ljava/io/FileNotFoundException;
		//  785    792    907    912    Ljava/lang/OutOfMemoryError;
		//  785    792    872    907    Any
		//  821    831    872    907    Any
		//  835    840    872    907    Any
		//  845    850    852    872    Ljava/io/IOException;
		//  879    884    887    907    Ljava/io/IOException;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0598:
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

	public int getWidth() {
		return this.mWidth;
	}
}
