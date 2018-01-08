package com.zed3.location;

import java.io.*;

public class GpsInfo implements Serializable
{
    private static final long serialVersionUID = -1335431462438445144L;
    public String E_id;
    public long UnixTime;
    public String gps_date;
    public int gps_direction;
    public float gps_height;
    public float gps_speed;
    public String gps_status;
    public String gps_time;
    public double gps_x;
    public double gps_y;
    
    public GpsInfo() {
        this.gps_time = "";
        this.gps_x = 0.0;
        this.gps_y = 0.0;
        this.gps_speed = 0.0f;
        this.gps_height = 0.0f;
        this.gps_direction = 0;
        this.gps_date = "";
        this.gps_status = "";
        this.UnixTime = 0L;
        this.E_id = "";
    }
    
    @Override
    public String toString() {
        return "GpsInfo [gps_time=" + this.gps_time + ", gps_x=" + this.gps_x + ", gps_y=" + this.gps_y + ", gps_speed=" + this.gps_speed + ", gps_height=" + this.gps_height + ", gps_direction=" + this.gps_direction + ", gps_date=" + this.gps_date + ", gps_status=" + this.gps_status + ", UnixTime=" + this.UnixTime + ", E_id=" + this.E_id + "]";
    }
}
