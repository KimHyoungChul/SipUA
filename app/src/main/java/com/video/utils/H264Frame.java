package com.video.utils;

public class H264Frame
{
    private byte[] data;
    private int frameType;
    
    public H264Frame(final byte[] data, final int frameType) {
        this.data = null;
        this.frameType = 0;
        this.data = data;
        this.frameType = frameType;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public int getFrameType() {
        return this.frameType;
    }
    
    public void setData(final byte[] data) {
        this.data = data;
    }
    
    public void setFrameType(final int frameType) {
        this.frameType = frameType;
    }
}
