package com.zed3.sipua.ui;

import com.zed3.sipua.ui.anta.Linkman;

import java.util.ArrayList;

public class MeetingMem {
	public static ArrayList<Linkman> inviteContact;
	public static ArrayList<Linkman> selectContact;
	public static String toSend;

	static {
		MeetingMem.toSend = "";
	}
}
