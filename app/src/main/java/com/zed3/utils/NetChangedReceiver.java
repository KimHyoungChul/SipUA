package com.zed3.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.zed3.log.MyLog;
import com.zed3.media.mediaButton.HeadsetPlugReceiver;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.AutoConfigManager;

public class NetChangedReceiver extends BroadcastReceiver {
	public static String lastGrpID;
	private static String mobileSubTypeName;
	private static String networkTypeName;
	private static boolean networkdown;
	private static NetChangedReceiver sReceiver;
	private final String TAG;

	static {
		NetChangedReceiver.lastGrpID = "";
		NetChangedReceiver.networkdown = false;
		NetChangedReceiver.networkTypeName = "";
		NetChangedReceiver.mobileSubTypeName = "";
		// TODO delete  @SuppressLint("WrongConstant")
		@SuppressLint("WrongConstant") final NetworkInfo activeNetworkInfo = ((ConnectivityManager) SipUAApp.mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			NetChangedReceiver.networkTypeName = activeNetworkInfo.getTypeName();
			if ("mobile".equalsIgnoreCase(NetChangedReceiver.networkTypeName)) {
				NetChangedReceiver.mobileSubTypeName = activeNetworkInfo.getSubtypeName();
				MyLog.i("zzhan-3-29", "init subTypeName is :" + NetChangedReceiver.mobileSubTypeName);
			}
			MyLog.i("zzhan-3-29", "Initialization networkTypeName is :" + NetChangedReceiver.networkTypeName);
		}
	}

	public NetChangedReceiver() {
		this.TAG = "NetChangedReceiver";
	}

	public static void registerSelf() {
		synchronized (NetChangedReceiver.class) {
			if (NetChangedReceiver.sReceiver == null) {
				final IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
				intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
				intentFilter.addAction("android.intent.action.SCREEN_ON");
				intentFilter.addAction("android.intent.action.SCREEN_OFF");
				final NetChangedReceiver sReceiver = new NetChangedReceiver();
				SipUAApp.getAppContext().registerReceiver((BroadcastReceiver) sReceiver, intentFilter);
				NetChangedReceiver.sReceiver = sReceiver;
			}
		}
	}

	public static void unregisterSelf() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     2: monitorenter
		//     3: getstatic       com/zed3/utils/NetChangedReceiver.sReceiver:Lcom/zed3/utils/NetChangedReceiver;
		//     6: astore_0
		//     7: aload_0
		//     8: ifnull          24
		//    11: invokestatic    com/zed3/sipua/SipUAApp.getAppContext:()Landroid/content/Context;
		//    14: getstatic       com/zed3/utils/NetChangedReceiver.sReceiver:Lcom/zed3/utils/NetChangedReceiver;
		//    17: invokevirtual   android/content/Context.unregisterReceiver:(Landroid/content/BroadcastReceiver;)V
		//    20: aconst_null
		//    21: putstatic       com/zed3/utils/NetChangedReceiver.sReceiver:Lcom/zed3/utils/NetChangedReceiver;
		//    24: ldc             Lcom/zed3/utils/NetChangedReceiver;.class
		//    26: monitorexit
		//    27: return
		//    28: astore_0
		//    29: ldc             Lcom/zed3/utils/NetChangedReceiver;.class
		//    31: monitorexit
		//    32: aload_0
		//    33: athrow
		//    34: astore_0
		//    35: goto            20
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------
		//  3      7      28     34     Any
		//  11     20     34     38     Ljava/lang/Exception;
		//  11     20     28     34     Any
		//  20     24     28     34     Any
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0020:
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

	boolean checkloginInfo() {
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.mContext);
		return !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalServer()) && !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalPwd()) && !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalUserName());
	}

	public void onReceive(final Context context, Intent intent) {
		final StringBuilder sb = new StringBuilder("NetChangedReceiver#onReceive");
		final String action = intent.getAction();
		if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			sb.append(" android.net.conn.CONNECTIVITY_CHANGE");
			// TODO delete  @SuppressLint("WrongConstant")
			@SuppressLint("WrongConstant") final ConnectivityManager connectivityManager = (ConnectivityManager) SipUAApp.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			assert connectivityManager != null;
			final NetworkInfo networkInfo = connectivityManager.getNetworkInfo(0);
			if (networkInfo != null) {
				final NetworkInfo.State state = networkInfo.getState();
				networkInfo.isAvailable();
				networkInfo.isConnected();
				networkInfo.isConnectedOrConnecting();
				MyLog.i("Receiver", "mobile state is : " + state.toString());
			}
			final NetworkInfo networkInfo2 = connectivityManager.getNetworkInfo(1);
			if (networkInfo2 != null) {
				final NetworkInfo.State state2 = networkInfo2.getState();
				networkInfo2.isAvailable();
				networkInfo2.isConnected();
				networkInfo2.isConnectedOrConnecting();
				MyLog.i("Receiver", "wifi state is : " + state2.toString());
			}
			final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			final StringBuilder sb2 = new StringBuilder(" NetWorkInfoactiveInfo:");
			String string;
			if (activeNetworkInfo == null) {
				string = "null";
			} else {
				string = activeNetworkInfo.toString();
			}
			sb.append(sb2.append(string).toString());
			final StringBuilder sb3 = new StringBuilder("NetWorkInfoactiveInfo:");
			String string2;
			if (activeNetworkInfo == null) {
				string2 = "null";
			} else {
				string2 = activeNetworkInfo.toString();
			}
			MyLog.i("zzhan-3-29", sb3.append(string2).toString());
			if (activeNetworkInfo != null) {
				MyLog.i("Receiver", "activeInfo is not null.");
				final String typeName = activeNetworkInfo.getTypeName();
				final NetworkInfo.State state3 = activeNetworkInfo.getState();
				MyLog.i("Receiver", "active network is : " + typeName);
				MyLog.i("Receiver", "active network state is " + state3.toString());
				if (!NetChangedReceiver.networkTypeName.equals(typeName)) {
					if (!NetChangedReceiver.networkdown) {
						LogUtil.makeLog("NetChangedReceiver", " register traces 1220 NetChangedReceiver#onReceive() haltNotCloseGps()");
						Receiver.engine(context).haltNotCloseGps();
						MyLog.i("Receiver", "engine halt.");
						if (Receiver.GetCurUA() != null && Receiver.GetCurUA().GetCurGrp() != null) {
							NetChangedReceiver.lastGrpID = Receiver.GetCurUA().GetCurGrp().grpID;
						}
					}
					if (this.checkloginInfo()) {
						System.out.println("------NetChangedReceiver-activeInfo!=null-StartEngine-------");
						LogUtil.makeLog("NetChangedReceiver", " register traces 1220 NetChangedReceiver#onReceive() StartEngine()");
						Receiver.engine(context).StartEngine();
					}
					MyLog.i("Receiver", "engine start.");
				} else {
					if ("mobile".equalsIgnoreCase(NetChangedReceiver.networkTypeName) && !NetChangedReceiver.mobileSubTypeName.equalsIgnoreCase(activeNetworkInfo.getSubtypeName()) && !NetChangedReceiver.networkdown) {
						LogUtil.makeLog("NetChangedReceiver", " register traces 1220 NetChangedReceiver#onReceive() haltNotCloseGps()");
						Receiver.engine(context).haltNotCloseGps();
						MyLog.i("Receiver", "engine halt.");
						if (Receiver.GetCurUA() != null && Receiver.GetCurUA().GetCurGrp() != null) {
							NetChangedReceiver.lastGrpID = Receiver.GetCurUA().GetCurGrp().grpID;
						}
						NetChangedReceiver.networkdown = true;
						MyLog.i("zzhan-3-29", "\u5207\u6362\u5e7f\u64ad : halt()last subNet:" + NetChangedReceiver.mobileSubTypeName + "," + "this subNet:" + activeNetworkInfo.getSubtypeName());
						NetChangedReceiver.mobileSubTypeName = activeNetworkInfo.getSubtypeName();
					}
					if (NetChangedReceiver.networkdown) {
						if ("mobile".equalsIgnoreCase(NetChangedReceiver.networkTypeName)) {
							NetChangedReceiver.mobileSubTypeName = activeNetworkInfo.getSubtypeName();
						}
						MyLog.i("zzhan-3-29", "\u5207\u6362\u6210\u529f\u5e7f\u64ad: StartEngine()this subNet:" + NetChangedReceiver.mobileSubTypeName);
						LogUtil.makeLog("NetChangedReceiver", " register traces 1220 NetChangedReceiver#onReceive() checkloginInfo()");
						if (this.checkloginInfo()) {
							System.out.println("------NetChangedReceiver-activeInfo==null-StartEngine-------");
							LogUtil.makeLog("NetChangedReceiver", " register traces 1220 NetChangedReceiver#onReceive() StartEngine()");
							Receiver.engine(context).StartEngine();
						}
						MyLog.i("Receiver", "engine start.");
					}
				}
				NetChangedReceiver.networkdown = false;
				NetChangedReceiver.networkTypeName = typeName;
				intent = new Intent();
				intent.setAction("com.zed3.sipua_network_changed");
				intent.putExtra("network_state", 1);
				context.sendBroadcast(intent);
			} else {
				MyLog.i("Receiver", "activeInfo is null.");
				if (!NetChangedReceiver.networkdown) {
					Receiver.engine(context).haltNotCloseGps();
					MyLog.i("Receiver", "engine halt.");
					MyLog.i("zzhan-3-29", "\u5207\u6362\u6210\u529f\u5e7f\u64ad:activeInfo = null, halt()");
				}
				if (Receiver.GetCurUA() != null && Receiver.GetCurUA().GetCurGrp() != null) {
					NetChangedReceiver.lastGrpID = Receiver.GetCurUA().GetCurGrp().grpID;
				}
				NetChangedReceiver.networkdown = true;
				final Intent intent2 = new Intent();
				intent2.setAction("com.zed3.sipua_network_changed");
				intent2.putExtra("network_state", 0);
				context.sendBroadcast(intent2);
			}
		} else if (action.equals("android.intent.action.SCREEN_ON")) {
			sb.append(" ACTION_SCREEN_ON");
			HeadsetPlugReceiver.onScreamStateChanged(true);
		} else if (action.equals("android.intent.action.SCREEN_OFF")) {
			sb.append(" ACTION_SCREEN_OFF");
			HeadsetPlugReceiver.onScreamStateChanged(false);
		}
		LogUtil.makeLog("NetChangedReceiver", sb.toString());
	}
}
