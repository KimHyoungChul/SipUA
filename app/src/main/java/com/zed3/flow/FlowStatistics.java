package com.zed3.flow;

public class FlowStatistics
{
    public static int DownLoad_APK;
    public static String Gps_Receive;
    public static int Gps_Receive_Data;
    public static String Gps_Send;
    public static int Gps_Send_Data;
    public static String Sip_Receive;
    public static int Sip_Receive_Data;
    public static String Sip_Send;
    public static int Sip_Send_Data;
    public static String Total;
    public static double Total_Flow;
    public static int Video_Packet_Lost;
    public static String Video_Receive;
    public static int Video_Receive_Data;
    public static String Video_Send;
    public static int Video_Send_Data;
    public static String Voice_Receive;
    public static int Voice_Receive_Data;
    public static String Voice_Send;
    public static int Voice_Send_Data;
    
    static {
        FlowStatistics.Sip_Send_Data = 0;
        FlowStatistics.Sip_Receive_Data = 0;
        FlowStatistics.Gps_Send_Data = 0;
        FlowStatistics.Gps_Receive_Data = 0;
        FlowStatistics.Voice_Send_Data = 0;
        FlowStatistics.Voice_Receive_Data = 0;
        FlowStatistics.Video_Send_Data = 0;
        FlowStatistics.Video_Receive_Data = 0;
        FlowStatistics.Video_Packet_Lost = 0;
        FlowStatistics.Total_Flow = 0.0;
        FlowStatistics.DownLoad_APK = 0;
    }
}
