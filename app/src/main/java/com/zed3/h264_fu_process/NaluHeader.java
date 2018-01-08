package com.zed3.h264_fu_process;

public class NaluHeader {
    private byte f848F;
    private byte NRI;
    private byte TYPE;
    private byte f849b;

    public NaluHeader(byte b) {
        this.f849b = b;
    }

    public byte getTYPE() {
        return FUUtils.getType(this.f849b);
    }

    public void setTYPE(byte tYPE) {
        this.f849b = FUUtils.setType(this.f849b, tYPE);
    }

    public byte getNRI() {
        return FUUtils.getNri(this.f849b);
    }

    public void setNRI(byte nRI) {
        this.f849b = FUUtils.setNri(this.f849b, nRI);
    }

    public byte getF() {
        return FUUtils.getF(this.f849b);
    }

    public void setF(byte f) {
        this.f848F = f;
        this.f849b = FUUtils.setF(this.f849b, f);
    }

    public byte getByte() {
        return this.f849b;
    }
}
