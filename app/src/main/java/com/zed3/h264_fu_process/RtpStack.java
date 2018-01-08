package com.zed3.h264_fu_process;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import com.zed3.log.MyLog;
import com.zed3.net.SipdroidSocket;
import com.zed3.sipua.R;
import com.zed3.sipua.DialogListener;
import com.zed3.sipua.VideoUdpThread;
import com.zed3.toast.MyToast;

public class RtpStack {
    public static final String TAG = "RtpStack";
    int FRANGMENT_BUG_SIZE = 1300;
    private Context context = null;
    byte[] fu = new byte[(this.FRANGMENT_BUG_SIZE + 2)];
    private LinearLayout loadProgressx = null;
    private final Handler myHandler = new C09761();
    private VideoUdpThread vHandle = null;

    class C09761 extends Handler {
        C09761() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                MyToast.showToastInBg(true, RtpStack.this.context, R.string.viewerror);
            } else if (msg.what == 2 && RtpStack.this.loadProgressx != null) {
                RtpStack.this.loadProgressx.setVisibility(View.GONE);
            }
        }
    }

    class C19262 implements DialogListener {
        C19262() {
        }

        public void DialogCancel(int flag) {
            if (flag == 1) {
                RtpStack.this.myHandler.sendEmptyMessage(2);
            }
            if (flag == 0) {
                RtpStack.this.myHandler.sendEmptyMessage(1);
            }
        }
    }

    public RtpStack(SurfaceView videoView, LinearLayout loadProgress, Context context, String videourl, int videoport, SipdroidSocket socket) {
        this.context = context;
        Log.i("GUOK", "SipdroidSocket: port=" + socket.getLocalPort() + " isconnetc:" + socket.isBound() + " videourl=" + videourl + " videoport:" + videoport);
        this.loadProgressx = loadProgress;
        this.vHandle = new VideoUdpThread(videoView, videourl, videoport, socket);
        this.vHandle.setDialogListener(new C19262());
    }

    public void resetDecode() {
        this.vHandle.resetDecode();
    }

    public void setSurfaceAviable(boolean isAviable) {
        this.vHandle.setSurfaceAviable(isAviable);
    }

    public boolean doesSetSfview() {
        return this.vHandle.doesSetSfview();
    }

    public void setSfview(SurfaceView videoView) {
        this.vHandle.setSfview(videoView);
    }

    public void CloseUdpSocket() {
        this.vHandle.CloseUdpSocket();
    }

    public void SendEmptyPacket() {
        byte[] t = "111111111111".getBytes();
        this.vHandle.sendNewByte(t, t.length);
    }

    public void rotateRemoteView() {
        this.vHandle.rotateDecoder();
    }

    public void transmitH264FU(byte[] inbuffer, int inbufferLen, long timeStamp) {
        MyLog.i("encodeTimeStamp", "function enter!!!");
        if (inbufferLen <= this.FRANGMENT_BUG_SIZE) {
            this.vHandle.VideoPacketToH264(inbuffer, inbufferLen, 1, timeStamp);
            return;
        }
        NaluHeader pNaluHdr = new NaluHeader(inbuffer[0]);
        FUIndicator pFuIndicator = new FUIndicator();
        pFuIndicator.setTYPE((byte) 28);
        pFuIndicator.setNRI(pNaluHdr.getNRI());
        pFuIndicator.setF(pNaluHdr.getF());
        FUHeader pFUHdrStart = new FUHeader();
        pFUHdrStart.setTYPE(pNaluHdr.getTYPE());
        pFUHdrStart.setE((byte) 0);
        pFUHdrStart.setR((byte) 0);
        pFUHdrStart.setS((byte) 1);
        FUHeader pFUHdrMid = new FUHeader();
        pFUHdrMid.setTYPE(pNaluHdr.getTYPE());
        pFUHdrMid.setE((byte) 0);
        pFUHdrMid.setR((byte) 0);
        pFUHdrMid.setS((byte) 0);
        FUHeader pFUHdrEnd = new FUHeader();
        pFUHdrEnd.setTYPE(pNaluHdr.getTYPE());
        pFUHdrEnd.setE((byte) 1);
        pFUHdrEnd.setR((byte) 0);
        pFUHdrEnd.setS((byte) 0);
        int k = inbufferLen / this.FRANGMENT_BUG_SIZE;
        int len = inbufferLen % this.FRANGMENT_BUG_SIZE;
        int t = 0;
        int marker = 0;
        while (t <= k) {
            int marker2;
            if (t == 0) {
                System.arraycopy(inbuffer, 0, this.fu, 1, this.FRANGMENT_BUG_SIZE);
                this.fu[0] = pFuIndicator.getByte();
                this.fu[1] = pFUHdrStart.getByte();
                this.vHandle.VideoPacketToH264(this.fu, this.FRANGMENT_BUG_SIZE + 1, 0, timeStamp);
                marker2 = marker;
            } else if (k == t && len > 0) {
                System.arraycopy(inbuffer, this.FRANGMENT_BUG_SIZE * t, this.fu, 2, len);
                this.fu[0] = pFuIndicator.getByte();
                this.fu[1] = pFUHdrEnd.getByte();
                MyLog.i("timeStampTest", "marker=  1 enter,timeStamp = " + timeStamp);
                this.vHandle.VideoPacketToH264(this.fu, len + 2, 1, timeStamp);
                marker2 = marker;
            } else if (t >= k || t == 0) {
                marker2 = marker;
            } else {
                System.arraycopy(inbuffer, this.FRANGMENT_BUG_SIZE * t, this.fu, 2, this.FRANGMENT_BUG_SIZE);
                this.fu[0] = pFuIndicator.getByte();
                if (t == k - 1 && len == 0) {
                    this.fu[1] = pFUHdrEnd.getByte();
                    marker2 = 1;
                } else {
                    this.fu[1] = pFUHdrMid.getByte();
                    marker2 = 0;
                }
                this.vHandle.VideoPacketToH264(this.fu, this.FRANGMENT_BUG_SIZE + 2, marker2, timeStamp);
            }
            t++;
            marker = marker2;
        }
    }
}
