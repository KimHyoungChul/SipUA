package com.zed3.sipua.ui.contact;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Xml;

import com.zed3.addressbook.DataBaseService;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.ParseXML;
import com.zed3.sipua.ui.Receiver;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactUtil {
	public static final String INDEX = "index";
	public static final String NEW_NAME = "newName";
	public static final String OLD_NAME = "oldName";
	public static final String USER_NAME = "title";
	public static final String USER_NUMBER = "info";
	private static Intent intent;
	public static boolean isContactsHasNewChanged;
	private static Context mContext;
	private static List<Map<String, Object>> mData;

	static {
		ContactUtil.isContactsHasNewChanged = false;
		ContactUtil.mContext = SipUAApp.mContext;
		ContactUtil.mData = new ArrayList<Map<String, Object>>();
	}

	private static List<Map<String, Object>> ReadContact() {
		ContactUtil.mData.clear();
		try {
			final FileInputStream openFileInput = Receiver.mContext.openFileInput("contacts.xml");
			final ParseXML parseXML = new ParseXML();
			parseXML.SetData(ContactUtil.mData);
			Xml.parse((InputStream) openFileInput, Xml.Encoding.UTF_8, (ContentHandler) parseXML);
			openFileInput.close();
			return ContactUtil.mData;
		} catch (FileNotFoundException ex) {
			MyLog.e("ReadContact", "contacts.xml is null ");
			return ContactUtil.mData;
		} catch (SAXException ex2) {
			return ContactUtil.mData;
		} catch (IOException ex3) {
			return ContactUtil.mData;
		}
	}

	public static void change(final Context context, final int n, final String s, final String s2) {
		ContactUtil.mData.get(n).put("title", s);
		ContactUtil.mData.get(n).put("info", s2);
		try {
			saveData();
		} catch (FileNotFoundException ex) {
		}
	}

	public static void changeName(final Context context, final int n, final String s) {
		ContactUtil.mData.get(n).put("title", s);
		try {
			saveData();
		} catch (FileNotFoundException ex) {
		}
	}

	public static List<Map<String, Object>> copyUsers(final List<Map<String, Object>> list) {
		if (ContactUtil.mData.size() == 0) {
			ReadContact();
		}
		for (int i = 0; i < ContactUtil.mData.size(); ++i) {
			list.add(ContactUtil.mData.get(i));
		}
		return list;
	}

	public static void deleteAll() {
		ContactUtil.mData.clear();
		try {
			saveData();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public static String getUserName(String nameByNum) {
		if (TextUtils.isEmpty((CharSequence) (nameByNum = DataBaseService.getInstance().getNameByNum(nameByNum)))) {
			nameByNum = null;
		}
		return nameByNum;
	}

	public static List<Map<String, Object>> getUsers() {
		if (ContactUtil.mData.size() == 0) {
			ReadContact();
		}
		return ContactUtil.mData;
	}

	public static void removeContact(final String s) {
		if (s != null && !s.equals("")) {
			for (int i = 0; i < ContactUtil.mData.size(); ++i) {
				if (((String) ContactUtil.mData.get(i).get("info")).equals(s)) {
					ContactUtil.mData.remove(i);
					try {
						saveData();
						return;
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
						return;
					}
				}
			}
		}
	}

	public static boolean removeContact(final int n) {
		if (n > -1 && n < ContactUtil.mData.size()) {
			ContactUtil.mData.remove(n);
			try {
				saveData();
				return true;
			} catch (FileNotFoundException ex) {
			}
		}
		return false;
	}

	public static void replace(final ArrayList<CharSequence> list) {
		try {
			ContactUtil.mData.clear();
			for (final CharSequence charSequence : list) {
				if (charSequence != null && !charSequence.equals("")) {
					final String[] split = charSequence.toString().split(",");
					if (split == null || split.length != 2) {
						continue;
					}
					// TODO
//					final HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
//					hashMap.put("info", split[0]);
//					hashMap.put("title", (Integer) split[1]);
//					hashMap.put("img", R.drawable.icon_contact);
//					ContactUtil.mData.add((Map<String, Object>) hashMap);
				}
			}
			saveData();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public static void saveData() throws FileNotFoundException {
		final XmlSerializer serializer = Xml.newSerializer();
		final FileOutputStream openFileOutput = SipUAApp.mContext.openFileOutput("contacts.xml", 0);
		try {
			serializer.setOutput(openFileOutput, "UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "contactsInfo");
			for (int i = 0; i < ContactUtil.mData.size(); ++i) {
				serializer.startTag("", "contacts");
				serializer.startTag("", "name");
				serializer.text((String) ContactUtil.mData.get(i).get("title"));
				serializer.endTag("", "name");
				serializer.startTag("", "phone");
				serializer.text((String) ContactUtil.mData.get(i).get("info"));
				serializer.endTag("", "phone");
				serializer.endTag("", "contacts");
			}
			serializer.endTag("", "contactsInfo");
			serializer.endDocument();
			openFileOutput.close();
			SipUAApp.mContext.sendBroadcast(new Intent("com.zed3.sipua_contact_changed"));
			ContactUtil.isContactsHasNewChanged = true;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
