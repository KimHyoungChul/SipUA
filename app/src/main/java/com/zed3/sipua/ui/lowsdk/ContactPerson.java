package com.zed3.sipua.ui.lowsdk;

public class ContactPerson {
	private int callTimes;
	private String contact_name;
	private String contact_num;

	public int getCallTimes() {
		return this.callTimes;
	}

	public String getContact_name() {
		return this.contact_name;
	}

	public String getContact_num() {
		return this.contact_num;
	}

	public void setCallTimes(final int callTimes) {
		this.callTimes = callTimes;
	}

	public void setContact_name(final String contact_name) {
		this.contact_name = contact_name;
	}

	public void setContact_num(final String contact_num) {
		this.contact_num = contact_num;
	}
}
