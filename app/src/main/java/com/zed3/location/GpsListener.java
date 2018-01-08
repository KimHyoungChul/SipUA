package com.zed3.location;

public interface GpsListener
{
    void LoginResult(final int p0);
    
    void UploadResult(final int p0, final int p1, final String p2);
    
    void UploadResult(final int p0, final String[] p1);
}
