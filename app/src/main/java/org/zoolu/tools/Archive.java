package org.zoolu.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Archive {
	public static String BASE_PATH;

	static {
		Archive.BASE_PATH = new File("").getAbsolutePath();
	}

	public static URL getFileURL(final String s) {
		if (s == null) {
			return null;
		}
		final String string = "file:" + Archive.BASE_PATH + "/" + s;
		try {
			return new URL("file:" + Archive.BASE_PATH + "/" + s);
		} catch (MalformedURLException ex) {
			System.err.println("ERROR: malformed url " + string);
			return null;
		}
	}

	public static InputStream getInputStream(final URL url) {
		if (url == null) {
			return null;
		}
		try {
			return url.openStream();
		} catch (IOException ex) {
			System.err.println("ERROR: can't read the file " + url.toString());
			return null;
		}
	}

	public static URL getJarURL(String string, final String s) {
		if (string == null || s == null) {
			return null;
		}
		string = "jar:file:" + Archive.BASE_PATH + "/" + string + "!/" + s;
		try {
			return new URL(string);
		} catch (MalformedURLException ex) {
			System.err.println("ERROR: malformed url " + string);
			return null;
		}
	}
}
