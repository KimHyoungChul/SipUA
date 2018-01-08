package com.video.utils;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.SurfaceView;

import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.video.CodecInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H264Dec {
	String MIME_TYPE = CodecInfo.MEDIA_CODEC_TYPE_H264;
	public boolean bFristDec = false;
	private MediaCodec codec;
	private int decodeH;
	private int decodeW;
	private int height;
	private volatile boolean isConfig = false;
	private volatile boolean isSurfaceAviable = false;
	private int mRotation = 0;
	private byte[] pps;
	private SurfaceView sfview;
	private byte[] sps;
	private int type = 2;
	private int width;

	public H264Dec(SurfaceView holderview) {
		this.sfview = holderview;
		this.type = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getInt("videoshowtype", 2);
	}

	public void createCodec() {
		if (VERSION.SDK_INT >= 16) {
			try {
				this.codec = MediaCodec.createDecoderByType(this.MIME_TYPE);
				this.isConfig = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void tryConfig(int width, int height, byte[] sps, byte[] pps) {
		if (this.codec != null && this.isSurfaceAviable && width != 0 && height != 0) {
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(this.MIME_TYPE, width, height);
			if (!(sps == null && pps == null)) {
				mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
				mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
			}
			mediaFormat.setInteger("rotation-degrees", this.mRotation);
			this.codec.configure(mediaFormat, this.sfview.getHolder().getSurface(), null, 0);
			this.codec.start();
			this.isConfig = true;
			MyLog.i("GUOK", "isConfig = true");
		}
	}

	public void reConfig(int width, int height, byte[] sps, byte[] pps) {
		releaseCodec();
		createCodec();
		this.width = width;
		this.height = height;
		this.sps = sps;
		this.pps = pps;
		tryConfig(width, height, sps, pps);
		MyLog.i("GUOK", "reConfig width=" + width + " height=" + height);
	}

	public synchronized void releaseCodec() {
		if (this.codec != null) {
			try {
				if (this.isConfig) {
					this.codec.stop();
				}
				this.codec.release();
				this.codec = null;
				this.isConfig = false;
				MyLog.i("GUOK", "releaseCodec");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void decodeAndPlayBack(byte[] in, int offset, int length, int type) {
		MyLog.i("video_tag", "decodeAndPlayBack called,in.length = " + in.length);
		try {
			int inputBufferIndex = this.codec.dequeueInputBuffer(100);
			MyLog.i("video_tag", "inputBufferIndex = " + inputBufferIndex);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = this.codec.getInputBuffers()[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(in, offset, length);
				this.codec.queueInputBuffer(inputBufferIndex, 0, length, 0, type);
			}
			MyLog.i("video_tag", "to release!1111");
			BufferInfo bufferInfo = new BufferInfo();
			MyLog.i("video_tag", "to release!2222");
			int outputBufferIndex = this.codec.dequeueOutputBuffer(bufferInfo, 100);
			while (outputBufferIndex >= 0) {
				this.codec.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = this.codec.dequeueOutputBuffer(bufferInfo, 0);
			}
			MyLog.i("video_tag", "to release!3333 outputBufferIndex=" + outputBufferIndex);
		} catch (IllegalStateException e) {
			Log.i("GUOK", "IllegalStateException ");
			e.printStackTrace();
			reConfig();
		}
	}

	public void PlayDecode(byte[] recBuffer, int type) {
		if (this.codec != null) {
			if (this.isConfig) {
				decodeAndPlayBack(recBuffer, 0, recBuffer.length, type);
			}
			MyLog.i("video_tag", "recBuffer size = " + recBuffer.length);
		}
	}

	public int getmRotation() {
		return this.mRotation;
	}

	public void setmRotation(int mRotation) {
		this.mRotation = mRotation;
	}

	public void addmRotation() {
		this.mRotation = (this.mRotation + 90) % 360;
		reConfig();
	}

	private void reConfig() {
		reConfig(this.width, this.height, this.sps, this.pps);
	}

	public SurfaceView getSfview() {
		return this.sfview;
	}

	public void setSfview(SurfaceView sfview) {
		this.sfview = sfview;
		reConfig();
	}

	public boolean isSurfaceAviable() {
		return this.isSurfaceAviable;
	}

	public void setSurfaceAviable(boolean isSurfaceAviable) {
		this.isSurfaceAviable = isSurfaceAviable;
		reConfig();
	}

	public MediaCodec getCodec() {
		return this.codec;
	}
}
