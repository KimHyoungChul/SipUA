package com.zed3.sipua.message;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;

public class MessageSender {
	private static final double DE2RA = 0.01745329252;
	private static final boolean DEFAULT_DELIVERY_REPORT_MODE = true;
	private static final boolean DEFAULT_READ_REPORT_MODE = true;
	private static final boolean DEFAULT_SAVE_SENT_MESSAGE_MODE = true;
	private static final String MMS_DRAFT_TABLE = "mms_draft";
	private static final String MMS_SENT_TABLE = "mms_sent";
	public static final int PHOTO_UPLOAD_STATE_FAILED = 1;
	public static final int PHOTO_UPLOAD_STATE_FINISHED = 3;
	public static final int PHOTO_UPLOAD_STATE_OFFLINE_SPACE_FULL = 4;
	public static final int PHOTO_UPLOAD_STATE_SUCCESS = 0;
	public static final int PHOTO_UPLOAD_STATE_UPLOADING = 2;
	private static final double RA2DE = 57.2957795129;
	private static final String SMS_DRAFT_TABLE = "sms_draft";
	private static final String SMS_SENT_TABLE = "sms_sent";
	private static final String TAG = "MessageSender";
	private static final String TEXT_PLAIN = "text/plain";
	private static String sDataId;
	private int attach_count;
	private String[] contacts;
	private SmsMmsDatabase database;
	private boolean isDeliveryReportOn;
	private boolean isReadReportOn;
	private boolean isSaveSentMessageOn;
	private String mAttachName;
	private Uri mAttachmentUri;
	private String mBodyValue;
	private String mContentType;
	private final Context mContext;
	private String mE_id;
	private String mReportAttrubute;
	private int mSmsMessageType;
	private String mToValue;
	private MultiMessage mms;

	public MessageSender(final Context mContext) {
		this.attach_count = 0;
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		this.isSaveSentMessageOn = true;
		this.mSmsMessageType = 0;
		this.mReportAttrubute = "";
		this.mContext = mContext;
	}

	public MessageSender(final Context mContext, final String mToValue, final String mBodyValue) {
		this.attach_count = 0;
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		this.isSaveSentMessageOn = true;
		this.mSmsMessageType = 0;
		this.mReportAttrubute = "";
		this.mToValue = mToValue;
		this.mBodyValue = mBodyValue;
		this.mContext = mContext;
	}

	public MessageSender(final Context mContext, final String mToValue, final String mBodyValue, final Uri mAttachmentUri, final String mContentType, final String mAttachName, final String me_id) {
		this.attach_count = 0;
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		this.isSaveSentMessageOn = true;
		this.mSmsMessageType = 0;
		this.mReportAttrubute = "";
		this.mToValue = mToValue;
		this.mBodyValue = mBodyValue;
		this.mAttachmentUri = mAttachmentUri;
		this.mContext = mContext;
		this.mContentType = mContentType;
		this.mAttachName = mAttachName;
		this.mE_id = me_id;
	}

	public MessageSender(final Context mContext, final String mToValue, final String mBodyValue, final String me_id) {
		this.attach_count = 0;
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		this.isSaveSentMessageOn = true;
		this.mSmsMessageType = 0;
		this.mReportAttrubute = "";
		this.mToValue = mToValue;
		this.mBodyValue = mBodyValue;
		this.mContext = mContext;
		this.mE_id = me_id;
	}

	private int getAttachmentCount() {
		// TODO
		return 0;
	}

	private String getMmsReportAttribute() {
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		this.isSaveSentMessageOn = true;
		if (this.isDeliveryReportOn) {
			if (this.isReadReportOn) {
				return "65535";
			}
			return "65533";
		} else {
			if (this.isReadReportOn) {
				return "65534";
			}
			return "65532";
		}
	}

	public static String getRandomString(final int n) {
		final StringBuffer sb = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		final StringBuffer sb2 = new StringBuffer();
		final Random random = new Random();
		final int length = sb.length();
		for (int i = 0; i < n; ++i) {
			sb2.append(sb.charAt(random.nextInt(length)));
		}
		return sb2.toString();
	}

	public static String getSendDataId() {
		return MessageSender.sDataId;
	}

	private String getSmsReportState() {
		PreferenceManager.getDefaultSharedPreferences(this.mContext);
		this.isDeliveryReportOn = true;
		this.isReadReportOn = true;
		MyLog.i("MessageSender", "delivery report = " + this.isDeliveryReportOn + ",read report = " + this.isReadReportOn);
		if (this.isDeliveryReportOn) {
			if (this.isReadReportOn) {
				return "65535";
			}
			return "65533";
		} else {
			if (this.isReadReportOn) {
				return "65534";
			}
			return "65532";
		}
	}

	private void sendMmsMessage() {
		if (!Tools.isConnect(SipUAApp.getAppContext())) {
			updateMmsState(this.mE_id, 1);
			MyToast.showToast(true, this.mContext, R.string.network_exception);
			return;
		}
		Receiver.GetCurUA().sendMultiMessage(this.mToValue, null, this.mE_id, this.mReportAttrubute, "mms", this.getMmsTxtByte(this.mE_id).length);
	}

	public static void setSendDataId(final String sDataId) {
		MessageSender.sDataId = sDataId;
	}

	public static void updateMmsState(final String s, final int n) {
		synchronized (MessageSender.class) {
			if (!TextUtils.isEmpty((CharSequence) s)) {
				final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(SipUAApp.getAppContext());
				final ContentValues contentValues = new ContentValues();
				contentValues.put("send", n);
				smsMmsDatabase.update("message_talk", "type = 'mms' and mark = 1 and E_id = '" + s + "'", contentValues);
			}
		}
	}

	private void writeMmsInfoByteToTxt(final String p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: astore          10
		//     3: aconst_null
		//     4: astore          11
		//     6: aconst_null
		//     7: astore          15
		//     9: aconst_null
		//    10: astore          12
		//    12: aconst_null
		//    13: astore          16
		//    15: aconst_null
		//    16: astore          17
		//    18: aconst_null
		//    19: astore          18
		//    21: aconst_null
		//    22: astore          4
		//    24: aconst_null
		//    25: astore          13
		//    27: aconst_null
		//    28: astore          14
		//    30: aload           4
		//    32: astore          7
		//    34: aload           16
		//    36: astore          5
		//    38: aload           15
		//    40: astore          6
		//    42: aload           17
		//    44: astore          8
		//    46: aload           18
		//    48: astore          9
		//    50: aload_0
		//    51: new             Lcom/zed3/sipua/message/MultiMessage;
		//    54: dup
		//    55: aload_0
		//    56: getfield        com/zed3/sipua/message/MessageSender.mContext:Landroid/content/Context;
		//    59: invokespecial   com/zed3/sipua/message/MultiMessage.<init>:(Landroid/content/Context;)V
		//    62: putfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//    65: aload           4
		//    67: astore          7
		//    69: aload           16
		//    71: astore          5
		//    73: aload           15
		//    75: astore          6
		//    77: aload           17
		//    79: astore          8
		//    81: aload           18
		//    83: astore          9
		//    85: aload_0
		//    86: aload_0
		//    87: invokespecial   com/zed3/sipua/message/MessageSender.getAttachmentCount:()I
		//    90: putfield        com/zed3/sipua/message/MessageSender.attach_count:I
		//    93: aload           4
		//    95: astore          7
		//    97: aload           16
		//    99: astore          5
		//   101: aload           15
		//   103: astore          6
		//   105: aload           17
		//   107: astore          8
		//   109: aload           18
		//   111: astore          9
		//   113: aload_0
		//   114: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   117: aload_0
		//   118: getfield        com/zed3/sipua/message/MessageSender.attach_count:I
		//   121: invokevirtual   com/zed3/sipua/message/MultiMessage.setAttachments:(I)V
		//   124: aload           4
		//   126: astore          7
		//   128: aload           16
		//   130: astore          5
		//   132: aload           15
		//   134: astore          6
		//   136: aload           17
		//   138: astore          8
		//   140: aload           18
		//   142: astore          9
		//   144: aload_0
		//   145: getfield        com/zed3/sipua/message/MessageSender.mContext:Landroid/content/Context;
		//   148: invokevirtual   android/content/Context.getContentResolver:()Landroid/content/ContentResolver;
		//   151: aload_0
		//   152: getfield        com/zed3/sipua/message/MessageSender.mAttachmentUri:Landroid/net/Uri;
		//   155: invokevirtual   android/content/ContentResolver.openInputStream:(Landroid/net/Uri;)Ljava/io/InputStream;
		//   158: astore          4
		//   160: aload           4
		//   162: astore          7
		//   164: aload           4
		//   166: astore          5
		//   168: aload           15
		//   170: astore          6
		//   172: aload           4
		//   174: astore          8
		//   176: aload           4
		//   178: astore          9
		//   180: new             Ljava/io/ByteArrayOutputStream;
		//   183: dup
		//   184: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//   187: astore          16
		//   189: aload           4
		//   191: astore          7
		//   193: aload           4
		//   195: astore          5
		//   197: aload           15
		//   199: astore          6
		//   201: aload           4
		//   203: astore          8
		//   205: aload           4
		//   207: astore          9
		//   209: sipush          1023
		//   212: newarray        B
		//   214: astore          17
		//   216: aload           4
		//   218: astore          7
		//   220: aload           4
		//   222: astore          5
		//   224: aload           15
		//   226: astore          6
		//   228: aload           4
		//   230: astore          8
		//   232: aload           4
		//   234: astore          9
		//   236: new             Ljava/io/File;
		//   239: dup
		//   240: new             Ljava/lang/StringBuilder;
		//   243: dup
		//   244: invokestatic    android/os/Environment.getExternalStorageDirectory:()Ljava/io/File;
		//   247: invokevirtual   java/io/File.getAbsolutePath:()Ljava/lang/String;
		//   250: invokestatic    java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
		//   253: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   256: ldc_w           "/smsmms"
		//   259: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   262: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   265: invokespecial   java/io/File.<init>:(Ljava/lang/String;)V
		//   268: astore          18
		//   270: aload           4
		//   272: astore          7
		//   274: aload           4
		//   276: astore          5
		//   278: aload           15
		//   280: astore          6
		//   282: aload           4
		//   284: astore          8
		//   286: aload           4
		//   288: astore          9
		//   290: aload           18
		//   292: invokevirtual   java/io/File.exists:()Z
		//   295: ifne            324
		//   298: aload           4
		//   300: astore          7
		//   302: aload           4
		//   304: astore          5
		//   306: aload           15
		//   308: astore          6
		//   310: aload           4
		//   312: astore          8
		//   314: aload           4
		//   316: astore          9
		//   318: aload           18
		//   320: invokevirtual   java/io/File.mkdirs:()Z
		//   323: pop
		//   324: aload_1
		//   325: ifnonnull       357
		//   328: aload           4
		//   330: astore          7
		//   332: aload           4
		//   334: astore          5
		//   336: aload           15
		//   338: astore          6
		//   340: aload           4
		//   342: astore          8
		//   344: aload           4
		//   346: astore          9
		//   348: ldc_w           "11"
		//   351: ldc_w           "123"
		//   354: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   357: aload           4
		//   359: astore          7
		//   361: aload           4
		//   363: astore          5
		//   365: aload           15
		//   367: astore          6
		//   369: aload           4
		//   371: astore          8
		//   373: aload           4
		//   375: astore          9
		//   377: new             Ljava/io/File;
		//   380: dup
		//   381: aload           18
		//   383: new             Ljava/lang/StringBuilder;
		//   386: dup
		//   387: ldc_w           "mms_"
		//   390: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   393: aload_1
		//   394: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   397: ldc_w           ".txt"
		//   400: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   403: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   406: invokespecial   java/io/File.<init>:(Ljava/io/File;Ljava/lang/String;)V
		//   409: astore_1
		//   410: aload           4
		//   412: astore          7
		//   414: aload           4
		//   416: astore          5
		//   418: aload           15
		//   420: astore          6
		//   422: aload           4
		//   424: astore          8
		//   426: aload           4
		//   428: astore          9
		//   430: aload_1
		//   431: invokevirtual   java/io/File.exists:()Z
		//   434: ifeq            1571
		//   437: aload           4
		//   439: astore          7
		//   441: aload           4
		//   443: astore          5
		//   445: aload           15
		//   447: astore          6
		//   449: aload           4
		//   451: astore          8
		//   453: aload           4
		//   455: astore          9
		//   457: aload_1
		//   458: invokevirtual   java/io/File.delete:()Z
		//   461: pop
		//   462: aload           4
		//   464: astore          7
		//   466: aload           4
		//   468: astore          5
		//   470: aload           15
		//   472: astore          6
		//   474: aload           4
		//   476: astore          8
		//   478: aload           4
		//   480: astore          9
		//   482: aload_1
		//   483: invokevirtual   java/io/File.createNewFile:()Z
		//   486: pop
		//   487: goto            1571
		//   490: aload           4
		//   492: astore          7
		//   494: aload           4
		//   496: astore          5
		//   498: aload           15
		//   500: astore          6
		//   502: aload           4
		//   504: astore          8
		//   506: aload           4
		//   508: astore          9
		//   510: aload           4
		//   512: aload           17
		//   514: invokevirtual   java/io/InputStream.read:([B)I
		//   517: istore_3
		//   518: iload_3
		//   519: iconst_m1
		//   520: if_icmpne       937
		//   523: aload           4
		//   525: ifnull          553
		//   528: aload           4
		//   530: astore          7
		//   532: aload           4
		//   534: astore          5
		//   536: aload           15
		//   538: astore          6
		//   540: aload           4
		//   542: astore          8
		//   544: aload           4
		//   546: astore          9
		//   548: aload           4
		//   550: invokevirtual   java/io/InputStream.close:()V
		//   553: aload           4
		//   555: astore          7
		//   557: aload           4
		//   559: astore          5
		//   561: aload           15
		//   563: astore          6
		//   565: aload           4
		//   567: astore          8
		//   569: aload           4
		//   571: astore          9
		//   573: aload_0
		//   574: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   577: aload_0
		//   578: getfield        com/zed3/sipua/message/MessageSender.mAttachName:Ljava/lang/String;
		//   581: invokevirtual   com/zed3/sipua/message/MultiMessage.setFile_name:(Ljava/lang/String;)V
		//   584: aload           4
		//   586: astore          7
		//   588: aload           4
		//   590: astore          5
		//   592: aload           15
		//   594: astore          6
		//   596: aload           4
		//   598: astore          8
		//   600: aload           4
		//   602: astore          9
		//   604: aload_0
		//   605: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   608: aload_0
		//   609: getfield        com/zed3/sipua/message/MessageSender.mContentType:Ljava/lang/String;
		//   612: invokevirtual   com/zed3/sipua/message/MultiMessage.setAttachment_type:(Ljava/lang/String;)V
		//   615: aload           4
		//   617: astore          7
		//   619: aload           4
		//   621: astore          5
		//   623: aload           15
		//   625: astore          6
		//   627: aload           4
		//   629: astore          8
		//   631: aload           4
		//   633: astore          9
		//   635: aload_0
		//   636: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   639: iload_2
		//   640: i2l
		//   641: invokevirtual   com/zed3/sipua/message/MultiMessage.setAttachment_length:(J)V
		//   644: aload           4
		//   646: astore          7
		//   648: aload           4
		//   650: astore          5
		//   652: aload           15
		//   654: astore          6
		//   656: aload           4
		//   658: astore          8
		//   660: aload           4
		//   662: astore          9
		//   664: new             Ljava/lang/StringBuffer;
		//   667: dup
		//   668: invokespecial   java/lang/StringBuffer.<init>:()V
		//   671: astore          18
		//   673: aload           4
		//   675: astore          7
		//   677: aload           4
		//   679: astore          5
		//   681: aload           15
		//   683: astore          6
		//   685: aload           4
		//   687: astore          8
		//   689: aload           4
		//   691: astore          9
		//   693: ldc_w           "application/octet-stream"
		//   696: aload_0
		//   697: getfield        com/zed3/sipua/message/MessageSender.mContentType:Ljava/lang/String;
		//   700: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   703: ifeq            1060
		//   706: aload           4
		//   708: astore          7
		//   710: aload           4
		//   712: astore          5
		//   714: aload           15
		//   716: astore          6
		//   718: aload           4
		//   720: astore          8
		//   722: aload           4
		//   724: astore          9
		//   726: aload_0
		//   727: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   730: aconst_null
		//   731: invokevirtual   com/zed3/sipua/message/MultiMessage.setBody_type:(Ljava/lang/String;)V
		//   734: aload           4
		//   736: astore          7
		//   738: aload           4
		//   740: astore          5
		//   742: aload           15
		//   744: astore          6
		//   746: aload           4
		//   748: astore          8
		//   750: aload           4
		//   752: astore          9
		//   754: aload_0
		//   755: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   758: lconst_0
		//   759: invokevirtual   com/zed3/sipua/message/MultiMessage.setBody_length:(J)V
		//   762: aload           4
		//   764: astore          7
		//   766: aload           4
		//   768: astore          5
		//   770: aload           15
		//   772: astore          6
		//   774: aload           4
		//   776: astore          8
		//   778: aload           4
		//   780: astore          9
		//   782: aload           18
		//   784: aload_0
		//   785: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//   788: invokevirtual   com/zed3/sipua/message/MultiMessage.getMessageHeader:()Ljava/lang/String;
		//   791: invokevirtual   java/lang/StringBuffer.append:(Ljava/lang/String;)Ljava/lang/StringBuffer;
		//   794: pop
		//   795: aload           4
		//   797: astore          7
		//   799: aload           4
		//   801: astore          5
		//   803: aload           15
		//   805: astore          6
		//   807: aload           4
		//   809: astore          8
		//   811: aload           4
		//   813: astore          9
		//   815: new             Ljava/io/OutputStreamWriter;
		//   818: dup
		//   819: new             Ljava/io/FileOutputStream;
		//   822: dup
		//   823: aload_1
		//   824: invokespecial   java/io/FileOutputStream.<init>:(Ljava/io/File;)V
		//   827: ldc_w           "UTF-8"
		//   830: invokespecial   java/io/OutputStreamWriter.<init>:(Ljava/io/OutputStream;Ljava/lang/String;)V
		//   833: astore_1
		//   834: aload_1
		//   835: aload           18
		//   837: invokevirtual   java/lang/StringBuffer.toString:()Ljava/lang/String;
		//   840: invokevirtual   java/io/OutputStreamWriter.write:(Ljava/lang/String;)V
		//   843: aload           14
		//   845: astore          5
		//   847: aload           13
		//   849: astore          6
		//   851: aload_0
		//   852: getfield        com/zed3/sipua/message/MessageSender.mContext:Landroid/content/Context;
		//   855: invokevirtual   android/content/Context.getContentResolver:()Landroid/content/ContentResolver;
		//   858: aload_0
		//   859: getfield        com/zed3/sipua/message/MessageSender.mAttachmentUri:Landroid/net/Uri;
		//   862: invokevirtual   android/content/ContentResolver.openInputStream:(Landroid/net/Uri;)Ljava/io/InputStream;
		//   865: astore          7
		//   867: aload           7
		//   869: astore          5
		//   871: aload           7
		//   873: astore          6
		//   875: aload           7
		//   877: aload           17
		//   879: invokevirtual   java/io/InputStream.read:([B)I
		//   882: istore_2
		//   883: iload_2
		//   884: iconst_m1
		//   885: if_icmpne       1176
		//   888: aload           16
		//   890: ifnull          898
		//   893: aload           16
		//   895: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//   898: aload           7
		//   900: ifnull          908
		//   903: aload           7
		//   905: invokevirtual   java/io/InputStream.close:()V
		//   908: aload           4
		//   910: ifnull          918
		//   913: aload           4
		//   915: invokevirtual   java/io/InputStream.close:()V
		//   918: aload_1
		//   919: ifnull          926
		//   922: aload_1
		//   923: invokevirtual   java/io/OutputStreamWriter.close:()V
		//   926: aload           4
		//   928: ifnull          1538
		//   931: aload           4
		//   933: invokevirtual   java/io/InputStream.close:()V
		//   936: return
		//   937: aload           4
		//   939: astore          7
		//   941: aload           4
		//   943: astore          5
		//   945: aload           15
		//   947: astore          6
		//   949: aload           4
		//   951: astore          8
		//   953: aload           4
		//   955: astore          9
		//   957: aload           16
		//   959: invokevirtual   java/io/ByteArrayOutputStream.reset:()V
		//   962: aload           4
		//   964: astore          7
		//   966: aload           4
		//   968: astore          5
		//   970: aload           15
		//   972: astore          6
		//   974: aload           4
		//   976: astore          8
		//   978: aload           4
		//   980: astore          9
		//   982: aload           16
		//   984: aload           17
		//   986: iconst_0
		//   987: iload_3
		//   988: invokevirtual   java/io/ByteArrayOutputStream.write:([BII)V
		//   991: aload           4
		//   993: astore          7
		//   995: aload           4
		//   997: astore          5
		//   999: aload           15
		//  1001: astore          6
		//  1003: aload           4
		//  1005: astore          8
		//  1007: aload           4
		//  1009: astore          9
		//  1011: aload           16
		//  1013: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//  1016: astore          18
		//  1018: aload           4
		//  1020: astore          7
		//  1022: aload           4
		//  1024: astore          5
		//  1026: aload           15
		//  1028: astore          6
		//  1030: aload           4
		//  1032: astore          8
		//  1034: aload           4
		//  1036: astore          9
		//  1038: iload_2
		//  1039: aload           18
		//  1041: iconst_0
		//  1042: aload           18
		//  1044: arraylength
		//  1045: iconst_0
		//  1046: invokestatic    android/util/Base64.encodeToString:([BIII)Ljava/lang/String;
		//  1049: ldc             "GBK"
		//  1051: invokevirtual   java/lang/String.getBytes:(Ljava/lang/String;)[B
		//  1054: arraylength
		//  1055: iadd
		//  1056: istore_2
		//  1057: goto            490
		//  1060: aload           4
		//  1062: astore          7
		//  1064: aload           4
		//  1066: astore          5
		//  1068: aload           15
		//  1070: astore          6
		//  1072: aload           4
		//  1074: astore          8
		//  1076: aload           4
		//  1078: astore          9
		//  1080: aload           18
		//  1082: aload_0
		//  1083: getfield        com/zed3/sipua/message/MessageSender.mms:Lcom/zed3/sipua/message/MultiMessage;
		//  1086: invokevirtual   com/zed3/sipua/message/MultiMessage.getMessageHeader:()Ljava/lang/String;
		//  1089: invokevirtual   java/lang/StringBuffer.append:(Ljava/lang/String;)Ljava/lang/StringBuffer;
		//  1092: pop
		//  1093: aload           4
		//  1095: astore          7
		//  1097: aload           4
		//  1099: astore          5
		//  1101: aload           15
		//  1103: astore          6
		//  1105: aload           4
		//  1107: astore          8
		//  1109: aload           4
		//  1111: astore          9
		//  1113: aload           18
		//  1115: aload_0
		//  1116: getfield        com/zed3/sipua/message/MessageSender.mBodyValue:Ljava/lang/String;
		//  1119: invokevirtual   java/lang/StringBuffer.append:(Ljava/lang/String;)Ljava/lang/StringBuffer;
		//  1122: pop
		//  1123: goto            795
		//  1126: astore          5
		//  1128: aload           12
		//  1130: astore_1
		//  1131: aload           7
		//  1133: astore          4
		//  1135: aload           5
		//  1137: astore          7
		//  1139: aload           4
		//  1141: astore          5
		//  1143: aload_1
		//  1144: astore          6
		//  1146: aload           7
		//  1148: invokevirtual   java/io/FileNotFoundException.printStackTrace:()V
		//  1151: aload_1
		//  1152: ifnull          1159
		//  1155: aload_1
		//  1156: invokevirtual   java/io/OutputStreamWriter.close:()V
		//  1159: aload           4
		//  1161: ifnull          936
		//  1164: aload           4
		//  1166: invokevirtual   java/io/InputStream.close:()V
		//  1169: return
		//  1170: astore_1
		//  1171: aload_1
		//  1172: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  1175: return
		//  1176: aload           7
		//  1178: astore          5
		//  1180: aload           7
		//  1182: astore          6
		//  1184: aload           16
		//  1186: invokevirtual   java/io/ByteArrayOutputStream.reset:()V
		//  1189: aload           7
		//  1191: astore          5
		//  1193: aload           7
		//  1195: astore          6
		//  1197: aload           16
		//  1199: aload           17
		//  1201: iconst_0
		//  1202: iload_2
		//  1203: invokevirtual   java/io/ByteArrayOutputStream.write:([BII)V
		//  1206: aload           7
		//  1208: astore          5
		//  1210: aload           7
		//  1212: astore          6
		//  1214: aload           16
		//  1216: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//  1219: astore          8
		//  1221: aload           7
		//  1223: astore          5
		//  1225: aload           7
		//  1227: astore          6
		//  1229: aload_1
		//  1230: aload           8
		//  1232: iconst_0
		//  1233: aload           8
		//  1235: arraylength
		//  1236: iconst_0
		//  1237: invokestatic    android/util/Base64.encodeToString:([BIII)Ljava/lang/String;
		//  1240: invokevirtual   java/io/OutputStreamWriter.write:(Ljava/lang/String;)V
		//  1243: goto            867
		//  1246: astore          7
		//  1248: aload           5
		//  1250: astore          6
		//  1252: aload           7
		//  1254: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1257: aload           16
		//  1259: ifnull          1267
		//  1262: aload           16
		//  1264: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//  1267: aload           5
		//  1269: ifnull          1277
		//  1272: aload           5
		//  1274: invokevirtual   java/io/InputStream.close:()V
		//  1277: aload           4
		//  1279: ifnull          918
		//  1282: aload           4
		//  1284: invokevirtual   java/io/InputStream.close:()V
		//  1287: goto            918
		//  1290: astore          5
		//  1292: aload           5
		//  1294: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1297: goto            918
		//  1300: astore          6
		//  1302: aload           6
		//  1304: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1307: goto            1267
		//  1310: astore          7
		//  1312: aload           4
		//  1314: astore          5
		//  1316: aload_1
		//  1317: astore          6
		//  1319: aload           7
		//  1321: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1324: aload_1
		//  1325: ifnull          1332
		//  1328: aload_1
		//  1329: invokevirtual   java/io/OutputStreamWriter.close:()V
		//  1332: aload           4
		//  1334: ifnull          936
		//  1337: aload           4
		//  1339: invokevirtual   java/io/InputStream.close:()V
		//  1342: return
		//  1343: astore_1
		//  1344: aload_1
		//  1345: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  1348: return
		//  1349: astore          5
		//  1351: aload           5
		//  1353: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1356: goto            1277
		//  1359: astore          7
		//  1361: aload           4
		//  1363: astore          5
		//  1365: aload_1
		//  1366: astore          6
		//  1368: aload           7
		//  1370: invokevirtual   java/lang/NullPointerException.printStackTrace:()V
		//  1373: aload_1
		//  1374: ifnull          1381
		//  1377: aload_1
		//  1378: invokevirtual   java/io/OutputStreamWriter.close:()V
		//  1381: aload           4
		//  1383: ifnull          936
		//  1386: aload           4
		//  1388: invokevirtual   java/io/InputStream.close:()V
		//  1391: return
		//  1392: astore_1
		//  1393: aload_1
		//  1394: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  1397: return
		//  1398: astore          5
		//  1400: aload           16
		//  1402: ifnull          1410
		//  1405: aload           16
		//  1407: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//  1410: aload           6
		//  1412: ifnull          1420
		//  1415: aload           6
		//  1417: invokevirtual   java/io/InputStream.close:()V
		//  1420: aload           4
		//  1422: ifnull          1430
		//  1425: aload           4
		//  1427: invokevirtual   java/io/InputStream.close:()V
		//  1430: aload           5
		//  1432: athrow
		//  1433: astore          5
		//  1435: aload_1
		//  1436: astore          6
		//  1438: aload           5
		//  1440: astore_1
		//  1441: aload           6
		//  1443: ifnull          1451
		//  1446: aload           6
		//  1448: invokevirtual   java/io/OutputStreamWriter.close:()V
		//  1451: aload           4
		//  1453: ifnull          1461
		//  1456: aload           4
		//  1458: invokevirtual   java/io/InputStream.close:()V
		//  1461: aload_1
		//  1462: athrow
		//  1463: astore          7
		//  1465: aload           7
		//  1467: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1470: goto            1410
		//  1473: astore          6
		//  1475: aload           6
		//  1477: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1480: goto            1420
		//  1483: astore          6
		//  1485: aload           6
		//  1487: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1490: goto            1430
		//  1493: astore          5
		//  1495: aload           5
		//  1497: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1500: goto            898
		//  1503: astore          5
		//  1505: aload           5
		//  1507: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1510: goto            908
		//  1513: astore          5
		//  1515: aload           5
		//  1517: invokevirtual   java/io/IOException.printStackTrace:()V
		//  1520: goto            918
		//  1523: astore          4
		//  1525: aload           4
		//  1527: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  1530: goto            1461
		//  1533: astore_1
		//  1534: aload_1
		//  1535: invokevirtual   java/lang/Exception.printStackTrace:()V
		//  1538: return
		//  1539: astore_1
		//  1540: aload           5
		//  1542: astore          4
		//  1544: goto            1441
		//  1547: astore          7
		//  1549: aload           8
		//  1551: astore          4
		//  1553: aload           11
		//  1555: astore_1
		//  1556: goto            1361
		//  1559: astore          7
		//  1561: aload           9
		//  1563: astore          4
		//  1565: aload           10
		//  1567: astore_1
		//  1568: goto            1312
		//  1571: iconst_0
		//  1572: istore_2
		//  1573: goto            490
		//  1576: astore          7
		//  1578: goto            1139
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  --------------------------------
		//  50     65     1126   1139   Ljava/io/FileNotFoundException;
		//  50     65     1559   1571   Ljava/io/IOException;
		//  50     65     1547   1559   Ljava/lang/NullPointerException;
		//  50     65     1539   1547   Any
		//  85     93     1126   1139   Ljava/io/FileNotFoundException;
		//  85     93     1559   1571   Ljava/io/IOException;
		//  85     93     1547   1559   Ljava/lang/NullPointerException;
		//  85     93     1539   1547   Any
		//  113    124    1126   1139   Ljava/io/FileNotFoundException;
		//  113    124    1559   1571   Ljava/io/IOException;
		//  113    124    1547   1559   Ljava/lang/NullPointerException;
		//  113    124    1539   1547   Any
		//  144    160    1126   1139   Ljava/io/FileNotFoundException;
		//  144    160    1559   1571   Ljava/io/IOException;
		//  144    160    1547   1559   Ljava/lang/NullPointerException;
		//  144    160    1539   1547   Any
		//  180    189    1126   1139   Ljava/io/FileNotFoundException;
		//  180    189    1559   1571   Ljava/io/IOException;
		//  180    189    1547   1559   Ljava/lang/NullPointerException;
		//  180    189    1539   1547   Any
		//  209    216    1126   1139   Ljava/io/FileNotFoundException;
		//  209    216    1559   1571   Ljava/io/IOException;
		//  209    216    1547   1559   Ljava/lang/NullPointerException;
		//  209    216    1539   1547   Any
		//  236    270    1126   1139   Ljava/io/FileNotFoundException;
		//  236    270    1559   1571   Ljava/io/IOException;
		//  236    270    1547   1559   Ljava/lang/NullPointerException;
		//  236    270    1539   1547   Any
		//  290    298    1126   1139   Ljava/io/FileNotFoundException;
		//  290    298    1559   1571   Ljava/io/IOException;
		//  290    298    1547   1559   Ljava/lang/NullPointerException;
		//  290    298    1539   1547   Any
		//  318    324    1126   1139   Ljava/io/FileNotFoundException;
		//  318    324    1559   1571   Ljava/io/IOException;
		//  318    324    1547   1559   Ljava/lang/NullPointerException;
		//  318    324    1539   1547   Any
		//  348    357    1126   1139   Ljava/io/FileNotFoundException;
		//  348    357    1559   1571   Ljava/io/IOException;
		//  348    357    1547   1559   Ljava/lang/NullPointerException;
		//  348    357    1539   1547   Any
		//  377    410    1126   1139   Ljava/io/FileNotFoundException;
		//  377    410    1559   1571   Ljava/io/IOException;
		//  377    410    1547   1559   Ljava/lang/NullPointerException;
		//  377    410    1539   1547   Any
		//  430    437    1126   1139   Ljava/io/FileNotFoundException;
		//  430    437    1559   1571   Ljava/io/IOException;
		//  430    437    1547   1559   Ljava/lang/NullPointerException;
		//  430    437    1539   1547   Any
		//  457    462    1126   1139   Ljava/io/FileNotFoundException;
		//  457    462    1559   1571   Ljava/io/IOException;
		//  457    462    1547   1559   Ljava/lang/NullPointerException;
		//  457    462    1539   1547   Any
		//  482    487    1126   1139   Ljava/io/FileNotFoundException;
		//  482    487    1559   1571   Ljava/io/IOException;
		//  482    487    1547   1559   Ljava/lang/NullPointerException;
		//  482    487    1539   1547   Any
		//  510    518    1126   1139   Ljava/io/FileNotFoundException;
		//  510    518    1559   1571   Ljava/io/IOException;
		//  510    518    1547   1559   Ljava/lang/NullPointerException;
		//  510    518    1539   1547   Any
		//  548    553    1126   1139   Ljava/io/FileNotFoundException;
		//  548    553    1559   1571   Ljava/io/IOException;
		//  548    553    1547   1559   Ljava/lang/NullPointerException;
		//  548    553    1539   1547   Any
		//  573    584    1126   1139   Ljava/io/FileNotFoundException;
		//  573    584    1559   1571   Ljava/io/IOException;
		//  573    584    1547   1559   Ljava/lang/NullPointerException;
		//  573    584    1539   1547   Any
		//  604    615    1126   1139   Ljava/io/FileNotFoundException;
		//  604    615    1559   1571   Ljava/io/IOException;
		//  604    615    1547   1559   Ljava/lang/NullPointerException;
		//  604    615    1539   1547   Any
		//  635    644    1126   1139   Ljava/io/FileNotFoundException;
		//  635    644    1559   1571   Ljava/io/IOException;
		//  635    644    1547   1559   Ljava/lang/NullPointerException;
		//  635    644    1539   1547   Any
		//  664    673    1126   1139   Ljava/io/FileNotFoundException;
		//  664    673    1559   1571   Ljava/io/IOException;
		//  664    673    1547   1559   Ljava/lang/NullPointerException;
		//  664    673    1539   1547   Any
		//  693    706    1126   1139   Ljava/io/FileNotFoundException;
		//  693    706    1559   1571   Ljava/io/IOException;
		//  693    706    1547   1559   Ljava/lang/NullPointerException;
		//  693    706    1539   1547   Any
		//  726    734    1126   1139   Ljava/io/FileNotFoundException;
		//  726    734    1559   1571   Ljava/io/IOException;
		//  726    734    1547   1559   Ljava/lang/NullPointerException;
		//  726    734    1539   1547   Any
		//  754    762    1126   1139   Ljava/io/FileNotFoundException;
		//  754    762    1559   1571   Ljava/io/IOException;
		//  754    762    1547   1559   Ljava/lang/NullPointerException;
		//  754    762    1539   1547   Any
		//  782    795    1126   1139   Ljava/io/FileNotFoundException;
		//  782    795    1559   1571   Ljava/io/IOException;
		//  782    795    1547   1559   Ljava/lang/NullPointerException;
		//  782    795    1539   1547   Any
		//  815    834    1126   1139   Ljava/io/FileNotFoundException;
		//  815    834    1559   1571   Ljava/io/IOException;
		//  815    834    1547   1559   Ljava/lang/NullPointerException;
		//  815    834    1539   1547   Any
		//  834    843    1576   1581   Ljava/io/FileNotFoundException;
		//  834    843    1310   1312   Ljava/io/IOException;
		//  834    843    1359   1361   Ljava/lang/NullPointerException;
		//  834    843    1433   1441   Any
		//  851    867    1246   1359   Ljava/io/IOException;
		//  851    867    1398   1493   Any
		//  875    883    1246   1359   Ljava/io/IOException;
		//  875    883    1398   1493   Any
		//  893    898    1493   1503   Ljava/io/IOException;
		//  893    898    1576   1581   Ljava/io/FileNotFoundException;
		//  893    898    1359   1361   Ljava/lang/NullPointerException;
		//  893    898    1433   1441   Any
		//  903    908    1503   1513   Ljava/io/IOException;
		//  903    908    1576   1581   Ljava/io/FileNotFoundException;
		//  903    908    1359   1361   Ljava/lang/NullPointerException;
		//  903    908    1433   1441   Any
		//  913    918    1513   1523   Ljava/io/IOException;
		//  913    918    1576   1581   Ljava/io/FileNotFoundException;
		//  913    918    1359   1361   Ljava/lang/NullPointerException;
		//  913    918    1433   1441   Any
		//  922    926    1533   1538   Ljava/lang/Exception;
		//  931    936    1533   1538   Ljava/lang/Exception;
		//  957    962    1126   1139   Ljava/io/FileNotFoundException;
		//  957    962    1559   1571   Ljava/io/IOException;
		//  957    962    1547   1559   Ljava/lang/NullPointerException;
		//  957    962    1539   1547   Any
		//  982    991    1126   1139   Ljava/io/FileNotFoundException;
		//  982    991    1559   1571   Ljava/io/IOException;
		//  982    991    1547   1559   Ljava/lang/NullPointerException;
		//  982    991    1539   1547   Any
		//  1011   1018   1126   1139   Ljava/io/FileNotFoundException;
		//  1011   1018   1559   1571   Ljava/io/IOException;
		//  1011   1018   1547   1559   Ljava/lang/NullPointerException;
		//  1011   1018   1539   1547   Any
		//  1038   1057   1126   1139   Ljava/io/FileNotFoundException;
		//  1038   1057   1559   1571   Ljava/io/IOException;
		//  1038   1057   1547   1559   Ljava/lang/NullPointerException;
		//  1038   1057   1539   1547   Any
		//  1080   1093   1126   1139   Ljava/io/FileNotFoundException;
		//  1080   1093   1559   1571   Ljava/io/IOException;
		//  1080   1093   1547   1559   Ljava/lang/NullPointerException;
		//  1080   1093   1539   1547   Any
		//  1113   1123   1126   1139   Ljava/io/FileNotFoundException;
		//  1113   1123   1559   1571   Ljava/io/IOException;
		//  1113   1123   1547   1559   Ljava/lang/NullPointerException;
		//  1113   1123   1539   1547   Any
		//  1146   1151   1539   1547   Any
		//  1155   1159   1170   1176   Ljava/lang/Exception;
		//  1164   1169   1170   1176   Ljava/lang/Exception;
		//  1184   1189   1246   1359   Ljava/io/IOException;
		//  1184   1189   1398   1493   Any
		//  1197   1206   1246   1359   Ljava/io/IOException;
		//  1197   1206   1398   1493   Any
		//  1214   1221   1246   1359   Ljava/io/IOException;
		//  1214   1221   1398   1493   Any
		//  1229   1243   1246   1359   Ljava/io/IOException;
		//  1229   1243   1398   1493   Any
		//  1252   1257   1398   1493   Any
		//  1262   1267   1300   1310   Ljava/io/IOException;
		//  1262   1267   1576   1581   Ljava/io/FileNotFoundException;
		//  1262   1267   1359   1361   Ljava/lang/NullPointerException;
		//  1262   1267   1433   1441   Any
		//  1272   1277   1349   1359   Ljava/io/IOException;
		//  1272   1277   1576   1581   Ljava/io/FileNotFoundException;
		//  1272   1277   1359   1361   Ljava/lang/NullPointerException;
		//  1272   1277   1433   1441   Any
		//  1282   1287   1290   1300   Ljava/io/IOException;
		//  1282   1287   1576   1581   Ljava/io/FileNotFoundException;
		//  1282   1287   1359   1361   Ljava/lang/NullPointerException;
		//  1282   1287   1433   1441   Any
		//  1292   1297   1576   1581   Ljava/io/FileNotFoundException;
		//  1292   1297   1310   1312   Ljava/io/IOException;
		//  1292   1297   1359   1361   Ljava/lang/NullPointerException;
		//  1292   1297   1433   1441   Any
		//  1302   1307   1576   1581   Ljava/io/FileNotFoundException;
		//  1302   1307   1310   1312   Ljava/io/IOException;
		//  1302   1307   1359   1361   Ljava/lang/NullPointerException;
		//  1302   1307   1433   1441   Any
		//  1319   1324   1539   1547   Any
		//  1328   1332   1343   1349   Ljava/lang/Exception;
		//  1337   1342   1343   1349   Ljava/lang/Exception;
		//  1351   1356   1576   1581   Ljava/io/FileNotFoundException;
		//  1351   1356   1310   1312   Ljava/io/IOException;
		//  1351   1356   1359   1361   Ljava/lang/NullPointerException;
		//  1351   1356   1433   1441   Any
		//  1368   1373   1539   1547   Any
		//  1377   1381   1392   1398   Ljava/lang/Exception;
		//  1386   1391   1392   1398   Ljava/lang/Exception;
		//  1405   1410   1463   1473   Ljava/io/IOException;
		//  1405   1410   1576   1581   Ljava/io/FileNotFoundException;
		//  1405   1410   1359   1361   Ljava/lang/NullPointerException;
		//  1405   1410   1433   1441   Any
		//  1415   1420   1473   1483   Ljava/io/IOException;
		//  1415   1420   1576   1581   Ljava/io/FileNotFoundException;
		//  1415   1420   1359   1361   Ljava/lang/NullPointerException;
		//  1415   1420   1433   1441   Any
		//  1425   1430   1483   1493   Ljava/io/IOException;
		//  1425   1430   1576   1581   Ljava/io/FileNotFoundException;
		//  1425   1430   1359   1361   Ljava/lang/NullPointerException;
		//  1425   1430   1433   1441   Any
		//  1430   1433   1576   1581   Ljava/io/FileNotFoundException;
		//  1430   1433   1310   1312   Ljava/io/IOException;
		//  1430   1433   1359   1361   Ljava/lang/NullPointerException;
		//  1430   1433   1433   1441   Any
		//  1446   1451   1523   1533   Ljava/lang/Exception;
		//  1456   1461   1523   1533   Ljava/lang/Exception;
		//  1465   1470   1576   1581   Ljava/io/FileNotFoundException;
		//  1465   1470   1310   1312   Ljava/io/IOException;
		//  1465   1470   1359   1361   Ljava/lang/NullPointerException;
		//  1465   1470   1433   1441   Any
		//  1475   1480   1576   1581   Ljava/io/FileNotFoundException;
		//  1475   1480   1310   1312   Ljava/io/IOException;
		//  1475   1480   1359   1361   Ljava/lang/NullPointerException;
		//  1475   1480   1433   1441   Any
		//  1485   1490   1576   1581   Ljava/io/FileNotFoundException;
		//  1485   1490   1310   1312   Ljava/io/IOException;
		//  1485   1490   1359   1361   Ljava/lang/NullPointerException;
		//  1485   1490   1433   1441   Any
		//  1495   1500   1576   1581   Ljava/io/FileNotFoundException;
		//  1495   1500   1310   1312   Ljava/io/IOException;
		//  1495   1500   1359   1361   Ljava/lang/NullPointerException;
		//  1495   1500   1433   1441   Any
		//  1505   1510   1576   1581   Ljava/io/FileNotFoundException;
		//  1505   1510   1310   1312   Ljava/io/IOException;
		//  1505   1510   1359   1361   Ljava/lang/NullPointerException;
		//  1505   1510   1433   1441   Any
		//  1515   1520   1576   1581   Ljava/io/FileNotFoundException;
		//  1515   1520   1310   1312   Ljava/io/IOException;
		//  1515   1520   1359   1361   Ljava/lang/NullPointerException;
		//  1515   1520   1433   1441   Any
		//
		// The error that occurred was:
		//
		// java.lang.IndexOutOfBoundsException: Index: 762, Size: 762
		//     at java.util.ArrayList.rangeCheck(ArrayList.java:653)
		//     at java.util.ArrayList.get(ArrayList.java:429)
		//     at com.strobel.decompiler.ast.AstBuilder.convertToAst(AstBuilder.java:3321)
		//     at com.strobel.decompiler.ast.AstBuilder.convertToAst(AstBuilder.java:3569)
		//     at com.strobel.decompiler.ast.AstBuilder.convertToAst(AstBuilder.java:3435)
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

	public void beginSendMultiMessage() {
		this.mReportAttrubute = this.getMmsReportAttribute();
		if (this.mE_id != null) {
			final ContentValues contentValues = new ContentValues();
			contentValues.put("E_id", this.mE_id);
			contentValues.put("address", this.mToValue);
			contentValues.put("body", this.mBodyValue);
			contentValues.put("date", this.getCurrentTime());
			contentValues.put("attachment", this.mAttachmentUri.toString());
			contentValues.put("attachment_name", this.mAttachName);
			contentValues.put("type", "mms");
			contentValues.put("mark", 1);
			contentValues.put("send", 2);
			final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
			contentValues.put("server_ip", autoConfigManager.fetchLocalServer());
			contentValues.put("local_number", autoConfigManager.fetchLocalUserName());
			MyLog.v("guojunfeng", "..mE_id= " + this.mE_id);
			new SmsMmsDatabase(this.mContext).insert("message_talk", contentValues);
			MyLog.v("MessageSender", "put in message_talk succsed");
		}
		this.sendMmsMessage();
	}

	public String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			return null;
		}
	}

	public String getE_id() {
		return UUID.randomUUID().toString().trim().replaceAll("-", "");
	}

	public String getLocalIpAddress() {
		String s = null;
		String s2 = null;
		try {
			final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (true) {
				s = s2;
				if (!networkInterfaces.hasMoreElements()) {
					break;
				}
				s = s2;
				final Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
				String string = s2;
				while (true) {
					s2 = string;
					s = string;
					if (!inetAddresses.hasMoreElements()) {
						break;
					}
					s = string;
					final InetAddress inetAddress = inetAddresses.nextElement();
					s = string;
					if (inetAddress.isLoopbackAddress()) {
						continue;
					}
					s = string;
					string = inetAddress.getHostAddress().toString();
				}
			}
			return s2;
		} catch (SocketException ex) {
			MyLog.e("MessageSender", "getLocalIpAddress error:");
			ex.printStackTrace();
			return s;
		}
	}

	public byte[] getMmsTxtByte(final String p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     3: dup
		//     4: new             Ljava/lang/StringBuilder;
		//     7: dup
		//     8: invokestatic    android/os/Environment.getExternalStorageDirectory:()Ljava/io/File;
		//    11: invokevirtual   java/io/File.getAbsolutePath:()Ljava/lang/String;
		//    14: invokestatic    java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
		//    17: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    20: ldc_w           "/smsmms/mms_"
		//    23: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    26: aload_1
		//    27: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    30: ldc_w           ".txt"
		//    33: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    36: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    39: invokespecial   java/io/File.<init>:(Ljava/lang/String;)V
		//    42: astore_1
		//    43: aload_1
		//    44: invokevirtual   java/io/File.exists:()Z
		//    47: ifne            55
		//    50: aload_1
		//    51: invokevirtual   java/io/File.createNewFile:()Z
		//    54: pop
		//    55: new             Ljava/io/ByteArrayOutputStream;
		//    58: dup
		//    59: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//    62: astore_3
		//    63: new             Ljava/io/FileInputStream;
		//    66: dup
		//    67: aload_1
		//    68: invokespecial   java/io/FileInputStream.<init>:(Ljava/io/File;)V
		//    71: astore_1
		//    72: sipush          1024
		//    75: newarray        B
		//    77: astore          4
		//    79: aload_1
		//    80: aload           4
		//    82: invokevirtual   java/io/InputStream.read:([B)I
		//    85: istore_2
		//    86: iload_2
		//    87: iconst_m1
		//    88: if_icmpne       120
		//    91: aload_3
		//    92: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//    95: aload_1
		//    96: invokevirtual   java/io/InputStream.close:()V
		//    99: aload_3
		//   100: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//   103: areturn
		//   104: astore_3
		//   105: aload_3
		//   106: invokevirtual   java/io/IOException.printStackTrace:()V
		//   109: ldc             "MessageSender"
		//   111: ldc_w           "getMmsTxtByte   createNewFile() fail"
		//   114: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   117: goto            55
		//   120: aload_3
		//   121: aload           4
		//   123: iconst_0
		//   124: iload_2
		//   125: invokevirtual   java/io/ByteArrayOutputStream.write:([BII)V
		//   128: goto            79
		//   131: astore_1
		//   132: ldc             "MessageSender"
		//   134: ldc_w           "getMmsTxtByte error:"
		//   137: invokestatic    com/zed3/log/MyLog.e:(Ljava/lang/String;Ljava/lang/String;)V
		//   140: aload_1
		//   141: invokevirtual   java/lang/Exception.printStackTrace:()V
		//   144: goto            99
		//   147: astore_1
		//   148: goto            132
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  50     55     104    120    Ljava/io/IOException;
		//  63     72     147    151    Ljava/lang/Exception;
		//  72     79     131    132    Ljava/lang/Exception;
		//  79     86     131    132    Ljava/lang/Exception;
		//  91     99     131    132    Ljava/lang/Exception;
		//  120    128    131    132    Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0079:
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

	public int getRodomDerictor(double acos, double n, double n2, double n3) {
		final double n4 = 0.0;
		final int n5 = (int) (0.5 + 360000.0 * acos);
		final int n6 = (int) (0.5 + 360000.0 * n2);
		final int n7 = (int) (0.5 + 360000.0 * n);
		final int n8 = (int) (0.5 + 360000.0 * n3);
		final double n9 = acos * 0.01745329252;
		n *= 0.01745329252;
		n2 *= 0.01745329252;
		n3 *= 0.01745329252;
		if (n5 == n6) {
			acos = n4;
			if (n7 == n8) {
				return (int) acos;
			}
		}
		if (n7 == n8) {
			acos = n4;
			if (n5 > n6) {
				acos = 180.0;
			}
		} else {
			acos = Math.acos(Math.sin(n9) * Math.sin(n2) + Math.cos(n9) * Math.cos(n2) * Math.cos(n3 - n));
			n = Math.asin(Math.cos(n2) * Math.sin(n3 - n) / Math.sin(acos)) * 57.2957795129;
			if (n6 > n5) {
				acos = n;
				if (n8 > n7) {
					return (int) acos;
				}
			}
			if (n6 < n5 && n8 < n7) {
				acos = 180.0 - n;
			} else if (n6 < n5 && n8 > n7) {
				acos = 180.0 - n;
			} else {
				acos = n;
				if (n6 > n5) {
					acos = n;
					if (n8 < n7) {
						acos = n + 360.0;
					}
				}
			}
		}
		return (int) acos;
	}

	public void reUploadPhoto(final String me_id) {
		this.writeMmsInfoByteToTxt(me_id);
		this.mReportAttrubute = this.getMmsReportAttribute();
		if (!TextUtils.isEmpty((CharSequence) me_id)) {
			updateMmsState(this.mE_id = me_id, 2);
		}
		this.sendMmsMessage();
	}

	public byte[] readInStream(final InputStream p0) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     3: dup
		//     4: invokespecial   java/io/ByteArrayOutputStream.<init>:()V
		//     7: astore_3
		//     8: sipush          1024
		//    11: newarray        B
		//    13: astore          4
		//    15: aload_1
		//    16: aload           4
		//    18: invokevirtual   java/io/InputStream.read:([B)I
		//    21: istore_2
		//    22: iload_2
		//    23: iconst_m1
		//    24: if_icmpne       48
		//    27: aload_3
		//    28: ifnull          35
		//    31: aload_3
		//    32: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//    35: aload_1
		//    36: ifnull          43
		//    39: aload_1
		//    40: invokevirtual   java/io/InputStream.close:()V
		//    43: aload_3
		//    44: invokevirtual   java/io/ByteArrayOutputStream.toByteArray:()[B
		//    47: areturn
		//    48: aload_3
		//    49: aload           4
		//    51: iconst_0
		//    52: iload_2
		//    53: invokevirtual   java/io/ByteArrayOutputStream.write:([BII)V
		//    56: goto            15
		//    59: astore          4
		//    61: aload           4
		//    63: invokevirtual   java/io/IOException.printStackTrace:()V
		//    66: aload_3
		//    67: ifnull          74
		//    70: aload_3
		//    71: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//    74: aload_1
		//    75: ifnull          43
		//    78: aload_1
		//    79: invokevirtual   java/io/InputStream.close:()V
		//    82: goto            43
		//    85: astore_1
		//    86: aload_1
		//    87: invokevirtual   java/io/IOException.printStackTrace:()V
		//    90: goto            43
		//    93: astore          4
		//    95: aload           4
		//    97: invokevirtual   java/io/IOException.printStackTrace:()V
		//   100: goto            74
		//   103: astore          4
		//   105: aload_3
		//   106: ifnull          113
		//   109: aload_3
		//   110: invokevirtual   java/io/ByteArrayOutputStream.close:()V
		//   113: aload_1
		//   114: ifnull          121
		//   117: aload_1
		//   118: invokevirtual   java/io/InputStream.close:()V
		//   121: aload           4
		//   123: athrow
		//   124: astore_3
		//   125: aload_3
		//   126: invokevirtual   java/io/IOException.printStackTrace:()V
		//   129: goto            113
		//   132: astore_1
		//   133: aload_1
		//   134: invokevirtual   java/io/IOException.printStackTrace:()V
		//   137: goto            121
		//   140: astore          4
		//   142: aload           4
		//   144: invokevirtual   java/io/IOException.printStackTrace:()V
		//   147: goto            35
		//   150: astore_1
		//   151: aload_1
		//   152: invokevirtual   java/io/IOException.printStackTrace:()V
		//   155: goto            43
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  15     22     59     103    Ljava/io/IOException;
		//  15     22     103    140    Any
		//  31     35     140    150    Ljava/io/IOException;
		//  39     43     150    158    Ljava/io/IOException;
		//  48     56     59     103    Ljava/io/IOException;
		//  48     56     103    140    Any
		//  61     66     103    140    Any
		//  70     74     93     103    Ljava/io/IOException;
		//  78     82     85     93     Ljava/io/IOException;
		//  109    113    124    132    Ljava/io/IOException;
		//  117    121    132    140    Ljava/io/IOException;
		//
		// The error that occurred was:
		//
		// java.lang.IndexOutOfBoundsException: Index: 78, Size: 78
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

	public void sendMultiMessage() {
		this.writeMmsInfoByteToTxt(this.mE_id);
		this.contacts = this.mToValue.split(",");
		if (this.contacts.length > 1) {
			for (int i = 0; i < this.contacts.length; ++i) {
				this.mToValue = this.contacts[i];
				final int index = this.mToValue.indexOf("<");
				final int lastIndex = this.mToValue.lastIndexOf(">");
				if (index != -1 && lastIndex != -1) {
					this.mToValue = this.mToValue.substring(index + 1, lastIndex);
					this.mToValue = this.mToValue.replace("-", "");
				}
				MyLog.i("MessageSender", "mToValue = " + this.mToValue);
				if (this.mToValue == null) {
					break;
				}
				this.beginSendMultiMessage();
			}
		} else {
			final int index2 = this.mToValue.indexOf("<");
			final int lastIndex2 = this.mToValue.lastIndexOf(">");
			if (index2 != -1 && lastIndex2 != -1) {
				this.mToValue = this.mToValue.substring(index2 + 1, lastIndex2);
				this.mToValue = this.mToValue.replace("-", "");
			}
			if (this.mToValue != null) {
				this.beginSendMultiMessage();
			}
		}
	}
}
