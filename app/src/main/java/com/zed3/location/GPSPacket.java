package com.zed3.location;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.zed3.log.MyLog;
import com.zed3.sipua.LocalConfigSettings;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GPSPacket
{
    public static ReceiveThread thread;
    private BDLocation bdloc;
    private Context context;
    SQLiteDatabase db;
    final Handler handler;
    private GpsInfo info;
    public boolean loginFlag;
    volatile MyHandlerThread mHandlerThread;
//    MyOnLocationChangedListener mMyOnLocationChangedListener;
    boolean mNeedLocation;
    private DatagramSocket socket;
    private InetAddress socketAdd;
    private String tag;
    private String usernum;
    
    static {
        GPSPacket.thread = null;
    }
    
    public GPSPacket(final Context context, final String usernum, final String s, final String ipAddress) {
        final boolean b = false;
        this.tag = "GPSPacket";
        this.usernum = "";
        this.info = null;
        this.bdloc = null;
        this.loginFlag = false;
//        this.mMyOnLocationChangedListener = new MyOnLocationChangedListener() {
//            @Override
//            public void onLocationChanged(final Location location) {
//                GpsTools.onLocationChanged(location);
//            }
//        };
        this.handler = new Handler() {
            public void handleMessage(final Message message) {
                if (message.what == 1) {
                    MyToast.showToast(true, GPSPacket.this.context, GPSPacket.this.context.getResources().getString(R.string.sis_loginning));
                }
            }
        };
        boolean mNeedLocation = false;
        Label_0116: {
            if (LocalConfigSettings.SdcardConfig.pool() != null) {
                mNeedLocation = b;
                if (LocalConfigSettings.SdcardConfig.pool() == null) {
                    break Label_0116;
                }
                mNeedLocation = b;
                if (!LocalConfigSettings.SdcardConfig.pool().mGoogleLocation) {
                    break Label_0116;
                }
            }
            if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
                mNeedLocation = b;
                if (!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
                    break Label_0116;
                }
                mNeedLocation = b;
                if (DeviceInfo.GPS_REMOTE == 0) {
                    break Label_0116;
                }
            }
            mNeedLocation = true;
        }
        this.mNeedLocation = mNeedLocation;
        this.context = context;
        this.usernum = usernum;
        MyLog.d("testgps", "GPSPacket ipAdd = " + ipAddress);
        MemoryMg.getInstance().IPAddress = ipAddress;
        MemoryMg.getInstance().IPPort = 5070;
        MemoryMg.getInstance().GpsLockState = true;
        this.mHandlerThread = SipUAApp.getInstance().getmHandlerThread();
        switch (Tools.getCurrentGpsMode()) {
            case 1:
            case 2: {
                if (this.bdloc == null) {
                    this.bdloc = new BDLocation(context);
                    return;
                }
                break;
            }
        }
    }
    
    private void Init() {
        try {
            this.socket = MemoryMg.getInstance().getSocket();
            MyLog.d("testgps", "GPSPacket#Init ipAdd = " + GpsTools.ServerIP);
            this.socketAdd = InetAddress.getByName(MemoryMg.getInstance().IPAddress);
            MyLog.d("testgps", "GPSPacket#Init ipAdd soket add = " + this.socketAdd);
        }
        catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    
    public void ExitGPS(final boolean b) {
        MyLog.d("testgps", "GPSPacket#ExitGPS enter isCloseGps = " + b);
        if (b) {
            MyLog.i("dd", "Tools.getCurrentGpsMode()=" + Tools.getCurrentGpsMode());
            switch (Tools.getCurrentGpsMode()) {
                case 0: {
                    GpsManage.getInstance(SipUAApp.getAppContext()).CloseGPS();
                    break;
                }
                case 1:
                case 2: {
                    if (this.bdloc != null) {
                        MyLog.i("GPSPacket", "----StopBDGPS==");
                        this.bdloc.StopBDGPS();
                        break;
                    }
                    break;
                }
                case 4: {
                    if (this.mNeedLocation) {
//                        GoogleApiLocationManager.getInstance().stop();
//                        GoogleApiLocationManager.getInstance().removeMyOnLocationChangedListener(this.mMyOnLocationChangedListener);
                        break;
                    }
                    break;
                }
            }
        }
        if (GPSPacket.thread != null) {
            GPSPacket.thread.ExitSys(b);
            GPSPacket.thread = null;
        }
    }
    
    public void SendLoginUdp() {
        MyLog.d("testgps", "GPSPacket#SendLoginUdp enter ");
        try {
            final SendThread sendThread = new SendThread(this.socket, this.socketAdd, GpsTools.Port);
            sendThread.SetContent(GpsTools.LoginByte(this.usernum, 0.0, 0.0, 0.0f, 0.0f, 0));
            sendThread.start();
            MyLog.i("GPSPacket", "SendLoginUdp gps login...");
        }
        catch (Exception ex) {
            MyLog.d("testgps", "GPSPacket#SendLoginUdp exception =  " + ex.getMessage());
            MyLog.e("gpsPacket SendLoginUdp error:", ex.toString());
        }
    }
    
    public void StartGPS(final boolean b) {
        MyLog.d("testgps", "GPSPacket#StartGPS enter usernum = " + this.usernum);
        if (this.usernum != null && this.usernum.length() > 0) {
            if (b) {
                MemoryMg.getInstance().TerminalNum = this.usernum.trim();
                MyLog.i("dd", "Tools.getCurrentGpsMode====" + Tools.getCurrentGpsMode());
                switch (Tools.getCurrentGpsMode()) {
                    case 0: {
                        GpsManage.getInstance(SipUAApp.getAppContext()).startGps();
                        break;
                    }
                    case 1:
                    case 2: {
                        System.out.println("-------\u5f00\u542f\u767e\u5ea6\u5b9a\u4f4d-----");
                        if (this.bdloc != null) {
                            this.bdloc.StartBDGPS();
                            break;
                        }
                        break;
                    }
                    case 4: {
                        if (this.mNeedLocation) {
                            MyLog.d(this.tag, "start google locating");
//                            GoogleApiLocationManager.getInstance().addMyOnLocationChangedListener(this.mMyOnLocationChangedListener);
//                            GoogleApiLocationManager.getInstance().start(SipUAApp.getAppContext());
                            break;
                        }
                        break;
                    }
                }
            }
            if (GPSPacket.thread != null) {
                GPSPacket.thread.ExitSys(false);
            }
            this.Init();
            (GPSPacket.thread = new ReceiveThread(MemoryMg.getInstance().getSocket(), this.context, this)).setGpsListener(new GpsListener() {
                @Override
                public void LoginResult(final int n) {
                    if (n == 0) {
                        GPSPacket.this.loginFlag = true;
                        MyLog.d("testgps", "GPSPacket#run login success");
                        MyLog.i("gpsloginok", "----ok");
                        GPSPacket.thread.StartHandler();
                        return;
                    }
                    MyLog.i("gpsloginok", "----failed");
                    GPSPacket.this.SendLoginUdp();
                    GPSPacket.this.handler.sendMessage(GPSPacket.this.handler.obtainMessage(1));
                }
                
                @Override
                public void UploadResult(final int n, final int n2, final String s) {
                    if (GPSPacket.this.loginFlag && n == 0 && n2 == 20) {
                        MyLog.d("testgps", "GPSPacket#setGpsListener upload success E_id=" + s);
                        GPSPacket.this.mHandlerThread.sendMessage(Message.obtain((Handler)GPSPacket.this.mHandlerThread.mInnerHandler, 2, (Object)s));
                    }
                }
                
                @Override
                public void UploadResult(final int n, final String[] array) {
                    if (n == 0) {
                        MyLog.d("testgps", "GPSPacket#setGpsListener upload success unixE_ids.length =" + array.length);
                        if (GPSPacket.this.mHandlerThread != null) {
                            GPSPacket.this.mHandlerThread.sendMessage(Message.obtain((Handler)GPSPacket.this.mHandlerThread.mInnerHandler, 3, (Object)array));
                        }
                    }
                }
            });
            GPSPacket.thread.start();
            this.SendLoginUdp();
        }
    }
    
    public void restartGPS() {
        MyLog.d("testgps", "GPSPacket#StartGPS enter usernum = " + this.usernum);
        if (this.bdloc != null) {
            this.bdloc.restart();
        }
        LogUtil.makeLog("testgps", "GPSPacket#restartGps() BDLocation is " + this.bdloc);
    }
}
