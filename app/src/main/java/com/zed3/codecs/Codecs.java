package com.zed3.codecs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;

import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Codecs {
	public static final Vector<Codec> codecs;
	private static final HashMap<String, Codec> codecsNames;
	private static final HashMap<Integer, Codec> codecsNumbers;
	private static Editor editor;
	private static SharedPreferences mSp;

	static {
		codecs = new Vector<Codec>();
		codecs.add(new AmrNB());

		final int size = codecs.size();
		codecsNumbers = new HashMap<Integer, Codec>(size);
		codecsNames = new HashMap<String, Codec>(size);
		for (final Codec codec : codecs) {
			codecsNames.put(codec.name(), codec);
			codecsNumbers.put(codec.number(), codec);
		}

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		String prefs = sp.getString(Settings.PREF_CODECS, Settings.DEFAULT_CODECS);
		if (prefs == null) {
			String string2 = "";
			Editor edit = sp.edit();
			final Iterator<Codec> iterator2 = Codecs.codecs.iterator();
			while (iterator2.hasNext()) {
				string2 = String.valueOf(string2) + iterator2.next().number() + " ";
			}
			edit.putString(Settings.PREF_CODECS, string2);
			edit.commit();
		}
		for (String s : prefs.split(" ")) {
			try {
				Codec codec2 = codecsNumbers.get(Integer.valueOf(Integer.parseInt(s)));
				if (codec2 != null) {
					Codecs.codecs.remove(codec2);
					Codecs.codecs.add(codec2);
				}
			} catch (Exception e) {
			}
		}
	}

	public static class CodecSettings extends PreferenceActivity {
		private static final int MENU_DOWN = 1;
		private static final int MENU_UP = 0;

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.codec_settings);
			registerForContextMenu(getListView());
			Codecs.addPreferences(getPreferenceScreen());
		}

		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.setHeaderTitle(R.string.codecs_move);
			menu.add(0, 0, 0, R.string.codecs_move_up);
			menu.add(0, 1, 0, R.string.codecs_move_down);
		}

		public boolean onContextItemSelected(MenuItem item) {
			int posn = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
			Codec c = (Codec) Codecs.codecs.elementAt(posn);
			Codec tmp;
			if (item.getItemId() == 0) {
				if (posn == 0) {
					return super.onContextItemSelected(item);
				}
				tmp = (Codec) Codecs.codecs.elementAt(posn - 1);
				Codecs.codecs.set(posn - 1, c);
				Codecs.codecs.set(posn, tmp);
			} else if (item.getItemId() == 1) {
				if (posn == Codecs.codecs.size() - 1) {
					return super.onContextItemSelected(item);
				}
				tmp = (Codec) Codecs.codecs.elementAt(posn + 1);
				Codecs.codecs.set(posn + 1, c);
				Codecs.codecs.set(posn, tmp);
			}
			PreferenceScreen ps = getPreferenceScreen();
			String v = "";
			Editor e = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).edit();
			Iterator it = Codecs.codecs.iterator();
			while (it.hasNext()) {
				v = new StringBuilder(String.valueOf(v)).append(((Codec) it.next()).number()).append(" ").toString();
			}
			e.putString(Settings.PREF_CODECS, v);
			e.commit();
			ps.removeAll();
			Codecs.addPreferences(ps);
			return super.onContextItemSelected(item);
		}

		public boolean onPreferenceTreeClick(PreferenceScreen ps, Preference p) {
			ListPreference l = (ListPreference) p;
			Iterator it = Codecs.codecs.iterator();
			while (it.hasNext()) {
				Codec c = (Codec) it.next();
				if (c.key().equals(l.getKey())) {
					c.init();
					if (!c.isLoaded()) {
						l.setValue("never");
						c.fail();
						l.setEnabled(false);
						l.setSummary(l.getEntry());
						if (l.getDialog() != null) {
							l.getDialog().dismiss();
						}
					}
				}
			}
			return super.onPreferenceTreeClick(ps, p);
		}

		public void onDestroy() {
			super.onDestroy();
			unregisterForContextMenu(getListView());
		}
	}

	public static class Map {
		public Codec codec;
		Vector<Codec> codecs;
		public int number;
		Vector<Integer> numbers;

		Map(int n, Codec c, Vector<Integer> ns, Vector<Codec> cs) {
			this.number = n;
			this.codec = c;
			this.numbers = ns;
			this.codecs = cs;
		}

		public boolean change(int n) {
			int i = this.numbers.indexOf(Integer.valueOf(n));
			if (i < 0 || this.codecs.elementAt(i) == null) {
				return false;
			}
			this.codec.close();
			this.number = n;
			this.codec = (Codec) this.codecs.elementAt(i);
			return true;
		}

		public String toString() {
			return "Codecs.Map { " + this.number + ": " + this.codec + "}";
		}
	}

	public static Codec get(int key) {
		return (Codec) codecsNumbers.get(Integer.valueOf(key));
	}

	public static Codec getName(String name) {
		return (Codec) codecsNames.get(name);
	}

	public static void check() {
		HashMap<String, String> hashMap = new HashMap<String, String>(Codecs.codecs.size());
		for (final Codec codec : codecs) {
			codec.update();
			hashMap.put(codec.name(), codec.getValue());
			if (!codec.isLoaded()) {
				if (Codecs.mSp == null) {
					Codecs.mSp = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
				}
				editor = mSp.edit();
				editor.putString(codec.key(), "never");
				editor.commit();
			}
		}

		for (final Codec codec2 : codecs) {
			if (!hashMap.get(codec2.name()).equals("never")) {
				codec2.init();
				if (codec2.isLoaded()) {
					if (Codecs.mSp == null) {
						Codecs.mSp = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
					}
					(editor = Codecs.mSp.edit()).putString(codec2.key(), (String) hashMap.get(codec2.name()));
					editor.commit();
					codec2.init();
				} else {
					codec2.fail();
				}
			}
		}
	}

	private static void addPreferences(PreferenceScreen ps) {
		Context cx = ps.getContext();
		Resources r = cx.getResources();
		ps.setOrderingAsAdded(true);
		Iterator it = codecs.iterator();
		while (it.hasNext()) {
			Codec c = (Codec) it.next();
			ListPreference l = new ListPreference(cx);
			l.setEntries(r.getStringArray(R.array.compression_display_values));
			l.setEntryValues(r.getStringArray(R.array.compression_values));
			l.setKey(c.key());
			l.setPersistent(true);
			l.setEnabled(!c.isFailed());
			c.setListPreference(l);
			if (c.number() != 9) {
				l.setSummary(l.getEntry());
			} else if (ps.getSharedPreferences().getString("server", "").equals("")) {
				l.setSummary(l.getEntry() + " (" + r.getString(R.string.settings_improve2) + ")");
			} else {
				l.setSummary(l.getEntry() + " (" + r.getString(R.string.settings_hdvoice) + ")");
			}
			l.setTitle(c.getTitle());
			ps.addPreference(l);
		}
	}

	public static int[] getCodecs() {
		Vector<Integer> v = new Vector(codecs.size());
		Iterator it = codecs.iterator();
		while (it.hasNext()) {
			Codec c = (Codec) it.next();
			if (c.isValid()) {
				v.add(Integer.valueOf(c.number()));
			}
		}
		int[] i = new int[v.size()];
		for (int j = 0; j < i.length; j++) {
			i[j] = ((Integer) v.elementAt(j)).intValue();
		}
		int s = 0;
		while (s < i.length) {
			if (s != 0 && i[s] == 114) {
				i[s] = i[0];
				i[0] = 114;
			}
			s++;
		}
		return i;
	}

	public static Map getCodec(SessionDescriptor offers) {
		if (offers.getMediaDescriptor(UserMinuteActivity.USER_AUDIO) == null) {
			return null;
		}
		MediaField m = offers.getMediaDescriptor(UserMinuteActivity.USER_AUDIO).getMedia();
		if (m == null) {
			return null;
		}
		String proto = m.getTransport();
		if (!proto.equals("RTP/AVP") && !proto.equals("RTP/SAVP")) {
			return null;
		}
		int index;
		Vector<String> formats = m.getFormatList();
		Vector<String> names = new Vector(formats.size());
		Vector<Integer> vector = new Vector(formats.size());
		Vector<Codec> codecmap = new Vector(formats.size());
		Iterator it = formats.iterator();
		while (it.hasNext()) {
			try {
				vector.add(Integer.valueOf(Integer.parseInt((String) it.next())));
				names.add("");
				codecmap.add(null);
			} catch (NumberFormatException e) {
			}
		}
		it = offers.getMediaDescriptor(UserMinuteActivity.USER_AUDIO).getAttributes("rtpmap").iterator();
		while (it.hasNext()) {
			String s = ((AttributeField) it.next()).getValue();
			s = s.substring(7, s.indexOf("/"));
			int i = s.indexOf(" ");
			try {
				String name = s.substring(i + 1);
				index = vector.indexOf(Integer.valueOf(Integer.parseInt(s.substring(0, i))));
				if (index >= 0) {
					names.set(index, name.toLowerCase());
				}
			} catch (NumberFormatException e2) {
			}
		}
		Codec codec = null;
		index = formats.size() + 1;
		Iterator it2 = codecs.iterator();
		while (it2.hasNext()) {
			Codec c = (Codec) it2.next();
			if (c.isValid()) {
				int i = names.indexOf(c.userName().toLowerCase());
				if (i >= 0) {
					codecmap.set(i, c);
					if (codec == null || i < index) {
						codec = c;
						index = i;
					}
				}
				i = vector.indexOf(Integer.valueOf(c.number()));
				if (i >= 0 && ((String) names.elementAt(i)).equals("")) {
					codecmap.set(i, c);
					if (codec == null || i < index) {
						codec = c;
						index = i;
					}
				}
			}
		}
		if (codec != null) {
			return new Map(((Integer) vector.elementAt(index)).intValue(), codec, vector, codecmap);
		}
		return null;
	}
}
