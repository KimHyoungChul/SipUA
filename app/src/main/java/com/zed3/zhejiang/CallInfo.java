package com.zed3.zhejiang;

public class CallInfo {
	public long date;
	public int direction;
	public String name;
	public String number;
	public int status;
	public long time;
	public int type;

	public CallInfo() {
		this.name = "";
		this.number = "";
		this.type = -1;
		this.direction = -1;
		this.status = 2;
		this.time = 0L;
		this.date = 0L;
	}
}
