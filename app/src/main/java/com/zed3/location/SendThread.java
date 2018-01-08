package com.zed3.location;

import android.content.Intent;
import android.util.Log;

import com.zed3.flow.FlowStatistics;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class SendThread extends Thread {
	public static final String TAG = "SendThread";
	public static int sSendCount;
	int count;
	int flow;
	GpsInfo gpsInfo;
	List<GpsInfo> infoList;
	private byte[] m_content;
	private int m_port;
	private InetAddress m_server;
	private DatagramSocket m_socket;
	private DatagramPacket packet;
	int sendType;

	static {
		SendThread.sSendCount = 18;
	}

	public SendThread(final DatagramSocket socket, final InetAddress server, final int port) {
		this.flow = 0;
		this.count = 0;
		this.sendType = 0;
		this.m_server = server;
		this.m_port = port;
		this.m_socket = socket;
	}

	public SendThread(final DatagramSocket socket, final InetAddress server, final int port, final int sendType, final List<GpsInfo> infoList) {
		this.flow = 0;
		this.count = 0;
		this.sendType = 0;
		this.m_server = server;
		this.m_port = port;
		this.m_socket = socket;
		this.infoList = infoList;
		this.sendType = sendType;
	}

	private void sendBroadCast4Send(final InetAddress inetAddress, final int n) {
		if (Settings.needSendLocateBroadcast) {
			final Intent intent = new Intent("com.zed3.sipua_upload_sended");
			intent.putExtra("server", inetAddress.getHostAddress());
			intent.putExtra("port", new StringBuilder(String.valueOf(n)).toString());
			SipUAApp.mContext.sendBroadcast(intent);
		}
	}

	private void sendGPSInfo() {
		Log.i("SendThread", "sendGPSInfo " + this.infoList);
		int n = 0;
		int n2 = 0;
		if (this.infoList != null && this.infoList.size() > 0) {
			Log.i("secondTrace", "send gps pkg size = " + this.infoList.size());
			final int size = this.infoList.size();
			if (size < -1) {
				int n3;
				if (size % 20 == 0) {
					n3 = size / 20;
				} else {
					n3 = size / 20 + 1;
				}
				for (int i = 0; i < n3; ++i) {
					this.SetContent(GpsTools.GpsByte(MemoryMg.getInstance().TerminalNum, this.sendType, this.infoList, i * 20));
					this.sendPackage();
				}
				n2 = size;
			} else {
				int n4 = 0;
				while (true) {
					n2 = n;
					if (n4 >= this.infoList.size()) {
						break;
					}
					final GpsInfo gpsInfo = this.infoList.get(n4);
					MyLog.d("testgps", "SendThread#sendGPSInfo sendGPSInfo = " + gpsInfo);
					if (!MyLocationManager.getDefault().onPrepareToSend(gpsInfo)) {
						MyLog.d("testgps", "SendThread#sendGPSInfo onPrepareToSend() return false");
					} else {
						MyLog.d("testgps", "SendThread#sendGPSInfo sendGPSInfo = " + gpsInfo);
						this.SetContent(GpsTools.GpsByte(MemoryMg.getInstance().TerminalNum, this.sendType, gpsInfo.gps_x, gpsInfo.gps_y, gpsInfo.gps_speed, gpsInfo.gps_height, gpsInfo.gps_direction, gpsInfo.UnixTime, gpsInfo.E_id));
						if (this.sendPackage()) {
							MyLocationManager.getDefault().onSended(gpsInfo);
						}
						++n;
					}
					++n4;
				}
			}
			MyLocationManager.getDefault().checkLocations();
		} else {
			this.sendPackage();
		}
		String string = new StringBuilder().append(this.sendType).toString();
		switch (this.sendType) {
			case 12: {
				string = "\u7535\u91cf\u4f4e";
				break;
			}
			case 21: {
				string = "\u641c\u661f\u5931\u8d25";
				break;
			}
			case 129: {
				string = "\u5b9a\u65f6\u4e0a\u62a5";
				break;
			}
			case 255: {
				string = "\u5012\u5730";
				break;
			}
		}
		MyLog.d("gps", String.valueOf(string) + " upload " + n2 + " locations");
	}

	private boolean sendPackage() {
		if (this.m_content == null) {
			LogUtil.makeLog("testgps", "sendPackage() m_content == null ignore");
			return false;
		}
		MyLog.i("GPSSend", "transfer content length:" + this.m_content.length);
		final byte[] content = this.m_content;
		final int length = content.length;
		MyLog.d("testgps", "SendThread#sendPackage gpsPacket = " + this.m_server + " , port = " + this.m_port);
		if (this.packet == null) {
			this.packet = new DatagramPacket(content, length, this.m_server, this.m_port);
		}
		while (true) {
			this.packet.setAddress(this.m_server);
			this.packet.setPort(this.m_port);
			this.packet.setData(content);
			this.packet.setLength(length);
			MyLog.i("GPSSend", "transfer server & port:" + this.m_server + " &" + this.m_port);
			this.sendBroadCast4Send(this.m_server, this.m_port);
			while (true) {
				try {
					this.m_socket.send(this.packet);
					if (this.packet.getLength() + 42 > 60) {
						this.flow = this.packet.getLength();
						FlowStatistics.Gps_Send_Data += this.flow;
						return true;
					}
				} catch (IOException ex) {
					Log.i("xxxx", "SendThread#sendPackage exception");
					ex.printStackTrace();
					return false;
				}
				this.flow = 60;
				continue;
			}
		}
	}

	private void setInfoData() {
		++SendThread.sSendCount;
		this.sendType = 129;
		if (this.infoList == null || this.infoList.size() < 2) {
			this.infoList = new ArrayList<GpsInfo>();
			for (int i = 0; i < SendThread.sSendCount; ++i) {
				final GpsInfo gpsInfo = new GpsInfo();
				gpsInfo.E_id = "6c2e18fb77c944bc87b7ad8413bfab32";
				gpsInfo.gps_date = "";
				gpsInfo.gps_direction = 0;
				gpsInfo.gps_height = 10.0f;
				gpsInfo.gps_speed = 10.0f;
				gpsInfo.gps_status = "";
				gpsInfo.UnixTime = 1433841373L;
				gpsInfo.gps_x = 116.316834;
				gpsInfo.gps_y = 39.983356;
				this.infoList.add(gpsInfo);
			}
		}
	}

	public void SetContent(final byte[] content) {
		this.m_content = content;
	}

	@Override
	public void run() {
		this.sendGPSInfo();
		Log.i("secondTrace", "send gps pkg");
	}
}
