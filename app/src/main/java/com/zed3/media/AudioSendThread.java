package com.zed3.media;

import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.zed3.net.RtpPacket;
import com.zed3.net.RtpSocket;
import com.zed3.sipua.ui.Receiver;

public class AudioSendThread extends HandlerThread implements Callback {
	public static final int AUDIO_MSG = 4669;
	public static final int DFMT_MSG = 48636;
	int dtframesize = 4;
	private int dtmf_payload_type;
	int frame_size;
	RtpPacket rtp_packet;
	RtpSocket rtp_socket;

	public AudioSendThread(String name) {
		super(name);
	}

	public AudioSendThread(String name, RtpSocket rtp_socket, int dtframesize, int frame_size, RtpPacket rtp_packet, int dtmf_payload_type) {
		super(name);
		this.rtp_socket = rtp_socket;
		this.dtframesize = dtframesize;
		this.frame_size = frame_size;
		this.rtp_packet = rtp_packet;
		this.dtmf_payload_type = dtmf_payload_type;
	}

	@Override
	public boolean handleMessage(Message message) {
		switch (message.what) {
			case AUDIO_MSG:
				sendDtmf((DtmfDataEntity)message.obj);
				break;
			case DFMT_MSG:
				sendAudio((AudioEncodedEntity)message.obj);
				break;
		}
		return false;
	}

	private void sendAudio(AudioEncodedEntity audioEncodedEntity) {
		final long elapsedRealtime = SystemClock.elapsedRealtime();
		final int m = audioEncodedEntity.m;
		final RtpPacket rtp_packet = audioEncodedEntity.rtp_packet;
		if (RtpStreamReceiver_signal.timeout != 0 && !Receiver.on_wlan) {
			if (elapsedRealtime - 0L <= 500L) {
				return;
			}
		}
		// TODO
	}

	private void sendDtmf(DtmfDataEntity dataEntity) {
		long time = dataEntity.time;
		int seqn = dataEntity.seqn;
		String dtmf = dataEntity.dtmf;
		Log.e("GUOK", "dtmf " + dtmf);
		byte[] dtmfbuf = new byte[(this.dtframesize + 12)];
		RtpPacket dt_packet = new RtpPacket(dtmfbuf, 0, "0");
		dt_packet.setPayloadType(this.dtmf_payload_type);
		dt_packet.setPayloadLength(this.dtframesize);
		dt_packet.setSscr(this.rtp_packet.getSscr());
		// TODO
	}
}
